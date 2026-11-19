package com.example.androidpractice;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

/**
 * Giỏ hàng và đơn hàng DEMO lưu riêng theo username trên máy.
 * Không ghi Firestore, không gọi cổng thanh toán, không thực hiện giao dịch tiền thật.
 */
public class OrderRepository {
    public static final String PAYMENT_COD = "cod";
    public static final String PAYMENT_TRANSFER_DEMO = "transfer_demo";

    public static final String STATUS_CONFIRMING = "confirming";
    public static final String STATUS_WAITING_PAYMENT = "waiting_payment";
    public static final String STATUS_PAID_DEMO = "paid_demo";
    public static final String STATUS_DELIVERED_DEMO = "delivered_demo";
    public static final String STATUS_CANCELLED = "cancelled";

    private static final String PREF_NAME = "fresh_market_local_order_demo";
    private static final String TAG = "OrderRepository";
    private static final int MAX_QUANTITY = 99;

    private final Context context;
    private final SharedPreferences prefs;
    private final String cartKey;
    private final String ordersKey;
    private boolean readError = false;

    public static class CartLine implements Serializable {
        private static final long serialVersionUID = 1L;

        public final Product product;
        public int quantity;

        public CartLine(Product product, int quantity) {
            this.product = product;
            this.quantity = quantity;
        }

        public long total() {
            return (long) product.getPrice() * quantity;
        }

        public CartLine copy() {
            return new CartLine(product, quantity);
        }
    }

    public static class Order implements Serializable {
        private static final long serialVersionUID = 1L;

        public String id;
        public String recipient;
        public String phone;
        public String address;
        public String note;
        public String paymentMethod;
        public String status;
        public long createdAt;
        public long updatedAt;
        public ArrayList<CartLine> lines = new ArrayList<>();

        public long total() {
            return totalOf(lines);
        }

        public boolean isEditable() {
            return STATUS_CONFIRMING.equals(status) || STATUS_WAITING_PAYMENT.equals(status);
        }
    }

    public OrderRepository(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = this.context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String username = this.context.getSharedPreferences(
                AppConstants.PREF_USER_SESSION, Context.MODE_PRIVATE)
                .getString(AppConstants.KEY_USERNAME, "");
        // Mỗi tài khoản demo có giỏ và lịch sử riêng; không dùng chung dữ liệu.
        // Giữ nguyên chữ hoa/thường như tài khoản đăng nhập để không lẫn dữ liệu.
        String userKey = username == null ? "" : username.trim();
        cartKey = "cart:" + userKey;
        ordersKey = "orders:" + userKey;
    }

    public ArrayList<CartLine> getCart() {
        try {
            return parseLines(new JSONArray(prefs.getString(cartKey, "[]")));
        } catch (JSONException e) {
            readError = true;
            Log.e(TAG, "Không đọc được giỏ hàng; giữ nguyên dữ liệu cũ.", e);
            return new ArrayList<>();
        }
    }

    public boolean addToCart(Product product) {
        if (product == null) return false;
        ArrayList<CartLine> lines = getCart();
        for (CartLine line : lines) {
            if (sameProduct(line.product, product)) {
                if (line.quantity >= MAX_QUANTITY) return false;
                line.quantity++;
                return saveCart(lines);
            }
        }
        lines.add(new CartLine(product, 1));
        return saveCart(lines);
    }

    public boolean changeCartQuantity(Product product, int quantity) {
        if (quantity < 1 || quantity > MAX_QUANTITY) return false;
        ArrayList<CartLine> lines = getCart();
        for (CartLine line : lines) {
            if (sameProduct(line.product, product)) {
                line.quantity = quantity;
                return saveCart(lines);
            }
        }
        return false;
    }

    public boolean removeFromCart(Product product) {
        ArrayList<CartLine> lines = getCart();
        boolean removed = lines.removeIf(line -> sameProduct(line.product, product));
        return removed && saveCart(lines);
    }

    public ArrayList<Order> getOrders() {
        ArrayList<Order> orders = new ArrayList<>();
        try {
            JSONArray array = new JSONArray(prefs.getString(ordersKey, "[]"));
            for (int i = 0; i < array.length(); i++) {
                JSONObject raw = array.getJSONObject(i);
                Order order = new Order();
                order.id = raw.getString("id");
                order.recipient = raw.optString("recipient");
                order.phone = raw.optString("phone");
                order.address = raw.optString("address");
                order.note = raw.optString("note");
                order.paymentMethod = raw.getString("payment");
                order.status = raw.getString("status");
                order.createdAt = raw.getLong("createdAt");
                order.updatedAt = raw.optLong("updatedAt", order.createdAt);
                order.lines = parseLines(raw.getJSONArray("lines"));
                orders.add(order);
            }
        } catch (JSONException e) {
            readError = true;
            Log.e(TAG, "Không đọc được lịch sử đơn; không tự xóa dữ liệu.", e);
            return new ArrayList<>();
        }
        return orders;
    }

    public Order findOrder(String orderId) {
        for (Order order : getOrders()) {
            if (order.id.equals(orderId)) return order;
        }
        return null;
    }

    // Tạo đơn từ giỏ; chỉ xóa giỏ khi việc lưu đơn thành công.
    public Order placeOrder(List<CartLine> checkedLines, String recipient, String phone, String address,
                            String note, String paymentMethod) {
        ArrayList<CartLine> lines = getCart();
        // Không lưu đơn nếu giỏ đã khác với thông tin khách vừa xác nhận.
        if (!sameCart(checkedLines, lines)) return null;
        return createOrder(lines, recipient, phone, address, note, paymentMethod, true);
    }

    public boolean cartMatches(List<CartLine> checkedLines) {
        return !readError && sameCart(checkedLines, getCart()) && !readError;
    }

    // Mua ngay một mặt hàng, không xóa hoặc gộp với giỏ hiện tại.
    public Order placeDirectOrder(Product product, int quantity, String recipient,
                                  String phone, String address, String note,
                                  String paymentMethod) {
        if (product == null || quantity < 1 || quantity > MAX_QUANTITY) return null;
        ArrayList<CartLine> lines = new ArrayList<>();
        lines.add(new CartLine(product, quantity));
        return createOrder(lines, recipient, phone, address, note, paymentMethod, false);
    }

    private Order createOrder(ArrayList<CartLine> lines, String recipient, String phone,
                              String address, String note, String paymentMethod,
                              boolean clearCart) {
        if (lines.isEmpty() || readError || !validPayment(paymentMethod)) return null;

        Order order = new Order();
        order.id = "FM-" + System.currentTimeMillis() + "-"
                + UUID.randomUUID().toString().substring(0, 4).toUpperCase(Locale.ROOT);
        order.recipient = recipient;
        order.phone = phone;
        order.address = address;
        order.note = note;
        order.paymentMethod = paymentMethod;
        order.status = waitingStatus(paymentMethod);
        order.createdAt = System.currentTimeMillis();
        order.updatedAt = order.createdAt;
        order.lines = lines;

        ArrayList<Order> orders = getOrders();
        if (readError) return null;
        orders.add(0, order);
        try {
            SharedPreferences.Editor editor = prefs.edit()
                    .putString(ordersKey, encodeOrders(orders).toString());
            if (clearCart) editor.remove(cartKey);
            return editor.commit() ? order : null;
        } catch (JSONException e) {
            Log.e(TAG, "Không lưu được đơn hàng.", e);
            return null;
        }
    }

    // Đơn đã thanh toán/hủy/hoàn tất KHÔNG cho chỉnh sửa.
    public boolean editOrder(Order edited) {
        if (edited == null || edited.lines.isEmpty()
                || !validPayment(edited.paymentMethod)) return false;
        ArrayList<Order> orders = getOrders();
        if (readError) return false;
        for (int i = 0; i < orders.size(); i++) {
            Order previous = orders.get(i);
            if (previous.id.equals(edited.id) && previous.isEditable()) {
                edited.createdAt = previous.createdAt;
                edited.updatedAt = System.currentTimeMillis();
                edited.status = waitingStatus(edited.paymentMethod);
                orders.set(i, edited);
                return saveOrders(orders);
            }
        }
        return false;
    }

    public boolean cancelOrder(String orderId) {
        return transitionStatus(orderId, null, STATUS_CANCELLED);
    }

    public boolean confirmPaymentDemo(String orderId) {
        return transitionStatus(orderId, STATUS_WAITING_PAYMENT, STATUS_PAID_DEMO);
    }

    public boolean confirmDeliveryDemo(String orderId) {
        return transitionStatus(orderId, STATUS_CONFIRMING, STATUS_DELIVERED_DEMO);
    }

    private boolean transitionStatus(String orderId, String expected, String next) {
        ArrayList<Order> orders = getOrders();
        if (readError) return false;
        for (Order order : orders) {
            if (order.id.equals(orderId) && order.isEditable()
                    && (expected == null || expected.equals(order.status))) {
                order.status = next;
                order.updatedAt = System.currentTimeMillis();
                return saveOrders(orders);
            }
        }
        return false;
    }

    private boolean saveCart(List<CartLine> lines) {
        if (readError) return false;
        try {
            return prefs.edit().putString(cartKey, encodeLines(lines).toString()).commit();
        } catch (JSONException e) {
            Log.e(TAG, "Không lưu được giỏ.", e);
            return false;
        }
    }

    private boolean saveOrders(List<Order> orders) {
        if (readError) return false;
        try {
            return prefs.edit().putString(ordersKey, encodeOrders(orders).toString()).commit();
        } catch (JSONException e) {
            Log.e(TAG, "Không lưu được lịch sử.", e);
            return false;
        }
    }

    private JSONArray encodeOrders(List<Order> orders) throws JSONException {
        JSONArray array = new JSONArray();
        for (Order order : orders) {
            JSONObject raw = new JSONObject();
            raw.put("id", order.id);
            raw.put("recipient", order.recipient);
            raw.put("phone", order.phone);
            raw.put("address", order.address);
            raw.put("note", order.note);
            raw.put("payment", order.paymentMethod);
            raw.put("status", order.status);
            raw.put("createdAt", order.createdAt);
            raw.put("updatedAt", order.updatedAt);
            raw.put("lines", encodeLines(order.lines));
            array.put(raw);
        }
        return array;
    }

    private JSONArray encodeLines(List<CartLine> lines) throws JSONException {
        JSONArray array = new JSONArray();
        for (CartLine line : lines) {
            JSONObject raw = new JSONObject();
            raw.put("name", line.product.getName());
            raw.put("origin", line.product.getOrigin());
            raw.put("description", line.product.getDescription());
            raw.put("price", line.product.getPrice());
            raw.put("quantity", line.quantity);
            try {
                raw.put("image", context.getResources().getResourceEntryName(
                        line.product.getImageResId()));
            } catch (Resources.NotFoundException e) {
                raw.put("image", "fruit_apple");
            }
            array.put(raw);
        }
        return array;
    }

    private ArrayList<CartLine> parseLines(JSONArray array) throws JSONException {
        ArrayList<CartLine> lines = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject raw = array.getJSONObject(i);
            String imageName = raw.optString("image", "fruit_apple");
            int imageRes = context.getResources().getIdentifier(
                    imageName, "drawable", context.getPackageName());
            if (imageRes == 0) imageRes = R.drawable.fruit_apple;

            Product product = new Product(raw.getString("name"),
                    raw.optString("origin"), raw.optString("description"),
                    raw.getInt("price"), imageRes);
            int quantity = raw.getInt("quantity");
            if (quantity < 1 || quantity > MAX_QUANTITY) {
                throw new JSONException("Số lượng không hợp lệ");
            }
            lines.add(new CartLine(product, quantity));
        }
        return lines;
    }

    private boolean sameProduct(Product a, Product b) {
        return Objects.equals(a.getName(), b.getName())
                && Objects.equals(a.getOrigin(), b.getOrigin());
    }

    private boolean sameCart(List<CartLine> expected, List<CartLine> current) {
        if (expected == null || expected.size() != current.size()) return false;
        for (int i = 0; i < expected.size(); i++) {
            CartLine before = expected.get(i);
            CartLine now = current.get(i);
            if (!sameProduct(before.product, now.product)
                    || before.product.getPrice() != now.product.getPrice()
                    || before.quantity != now.quantity) return false;
        }
        return true;
    }

    private boolean validPayment(String method) {
        return PAYMENT_COD.equals(method) || PAYMENT_TRANSFER_DEMO.equals(method);
    }

    private String waitingStatus(String method) {
        return PAYMENT_COD.equals(method) ? STATUS_CONFIRMING : STATUS_WAITING_PAYMENT;
    }

    public static long totalOf(List<CartLine> lines) {
        long total = 0;
        for (CartLine line : lines) total += line.total();
        return total;
    }

    public static String formatPrice(long price) {
        return String.format(Locale.forLanguageTag("vi-VN"), "%,d đ", price);
    }

    public static String paymentLabel(String method) {
        return PAYMENT_COD.equals(method) ? "Thanh toán khi nhận hàng (demo)"
                : "Chuyển khoản (mô phỏng)";
    }

    public static String statusLabel(String status) {
        switch (status) {
            case STATUS_CONFIRMING: return "Chờ xác nhận";
            case STATUS_WAITING_PAYMENT: return "Chờ thanh toán (demo)";
            case STATUS_PAID_DEMO: return "Đã thanh toán (demo)";
            case STATUS_DELIVERED_DEMO: return "Đã nhận hàng (demo)";
            case STATUS_CANCELLED: return "Đã hủy";
            default: return "Không xác định";
        }
    }
}
