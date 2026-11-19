package com.example.androidpractice;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class OrderDetailActivity extends AppCompatActivity {
    private OrderRepository repository;
    private String orderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);
        repository = new OrderRepository(this);
        // Nhận nguyên object, sau đó đọc lại bản mới nhất từ SharedPreferences.
        OrderRepository.Order selected = readSelectedOrder();
        if (selected == null) {
            Toast.makeText(this, "Không tìm thấy đơn hàng.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        orderId = selected.id;

        setupNavigation();
    }

    @SuppressWarnings("deprecation")
    private OrderRepository.Order readSelectedOrder() {
        return (OrderRepository.Order) getIntent().getSerializableExtra(
                AppConstants.EXTRA_SELECTED_ORDER);
    }

    private void setupNavigation() {
        findViewById(R.id.btnBackOrderDetail).setOnClickListener(v -> finish());
        findViewById(R.id.btnShopFromOrderDetail).setOnClickListener(v ->
                startActivity(new Intent(this, DetailActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        OrderRepository.Order order = repository.findOrder(orderId);
        if (order == null) {
            Toast.makeText(this, "Không tìm thấy đơn hàng.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        renderOrder(order);
    }

    private void renderOrder(OrderRepository.Order order) {
        TextView status = findViewById(R.id.tvOrderDetailStatus);
        status.setText(OrderRepository.statusLabel(order.status));
        status.setTextColor(getColor(OrderRepository.STATUS_CANCELLED.equals(order.status)
                ? R.color.text_secondary : order.isEditable()
                ? R.color.price_color : R.color.brand_primary));

        ((TextView) findViewById(R.id.tvOrderDetailId)).setText("Mã đơn: " + order.id);
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm",
                Locale.forLanguageTag("vi-VN"));
        ((TextView) findViewById(R.id.tvOrderDetailDate))
                .setText("Tạo lúc " + format.format(new Date(order.createdAt)));
        ((TextView) findViewById(R.id.tvOrderDetailRecipient)).setText(order.recipient);
        ((TextView) findViewById(R.id.tvOrderDetailPhone)).setText(order.phone);
        ((TextView) findViewById(R.id.tvOrderDetailAddress)).setText(order.address);
        TextView note = findViewById(R.id.tvOrderDetailNote);
        note.setText(order.note.isEmpty() ? "" : "Ghi chú: " + order.note);
        note.setVisibility(order.note.isEmpty() ? View.GONE : View.VISIBLE);
        ((TextView) findViewById(R.id.tvOrderDetailPayment))
                .setText(OrderRepository.paymentLabel(order.paymentMethod));
        ((TextView) findViewById(R.id.tvOrderDetailTotal))
                .setText(OrderRepository.formatPrice(order.total()));
        renderProducts(order);
        setupOrderActions(order);
    }

    private void renderProducts(OrderRepository.Order order) {
        LinearLayout container = findViewById(R.id.orderDetailItemsContainer);
        container.removeAllViews();
        for (OrderRepository.CartLine line : order.lines) {
            View row = LayoutInflater.from(this).inflate(
                    R.layout.item_checkout_line, container, false);
            ImageView image = row.findViewById(R.id.imgCheckoutProduct);
            image.setImageResource(line.product.getImageResId());
            image.setContentDescription(line.product.getName());
            ((TextView) row.findViewById(R.id.tvCheckoutProductName))
                    .setText(line.product.getName());
            ((TextView) row.findViewById(R.id.tvCheckoutProductPrice))
                    .setText(OrderRepository.formatPrice(line.product.getPrice()) + "/kg");
            ((TextView) row.findViewById(R.id.tvCheckoutLineTotal))
                    .setText(OrderRepository.formatPrice(line.total()));
            ((TextView) row.findViewById(R.id.tvCheckoutQty))
                    .setText(String.valueOf(line.quantity));
            container.addView(row);
        }
    }

    private void setupOrderActions(OrderRepository.Order order) {
        View actions = findViewById(R.id.orderDetailEditActions);
        MaterialButton transition = findViewById(R.id.btnConfirmOrderDemo);
        actions.setVisibility(order.isEditable() ? View.VISIBLE : View.GONE);
        transition.setVisibility(order.isEditable() ? View.VISIBLE : View.GONE);

        if (!order.isEditable()) return;

        findViewById(R.id.btnEditOrder).setOnClickListener(v -> {
            Intent intent = new Intent(this, CheckoutActivity.class);
            intent.putExtra(AppConstants.EXTRA_SELECTED_ORDER, order);
            startActivity(intent);
        });
        findViewById(R.id.btnCancelOrder).setOnClickListener(v ->
                new MaterialAlertDialogBuilder(this)
                        .setTitle("Hủy đơn hàng?")
                        .setMessage("Đơn sẽ được giữ trong lịch sử với trạng thái Đã hủy. "
                                + "Không thể sửa lại sau khi hủy.")
                        .setNegativeButton("Giữ đơn", null)
                        .setPositiveButton("Hủy đơn", (dialog, which) -> {
                            if (repository.cancelOrder(order.id)) {
                                reloadOrder();
                            } else {
                                showSaveError();
                            }
                        })
                        .show());

        boolean transfer = OrderRepository.STATUS_WAITING_PAYMENT.equals(order.status);
        transition.setText(transfer ? "Xác nhận thanh toán (mô phỏng)"
                : "Đánh dấu đã nhận hàng (demo)");
        transition.setOnClickListener(v -> new MaterialAlertDialogBuilder(this)
                .setTitle(transfer ? "Giả lập thanh toán thành công?"
                        : "Giả lập đã nhận hàng?")
                .setMessage("Chỉ thay đổi trạng thái đơn trên máy. "
                        + "Không có giao dịch hoặc vận chuyển thật. Sau bước này sẽ khóa sửa đơn.")
                .setNegativeButton("Quay lại", null)
                .setPositiveButton("Xác nhận demo", (dialog, which) -> {
                    boolean saved = transfer
                            ? repository.confirmPaymentDemo(order.id)
                            : repository.confirmDeliveryDemo(order.id);
                    if (saved) {
                        reloadOrder();
                    } else {
                        showSaveError();
                    }
                })
                .show());
    }

    private void reloadOrder() {
        OrderRepository.Order fresh = repository.findOrder(orderId);
        if (fresh != null) renderOrder(fresh);
    }

    private void showSaveError() {
        Toast.makeText(this, "Không cập nhật được trạng thái. Hãy thử lại.",
                Toast.LENGTH_LONG).show();
    }
}
