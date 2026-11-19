package com.example.androidpractice;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;

// Dùng chung một form: tạo đơn từ giỏ hoặc sửa đơn đang chờ.
public class CheckoutActivity extends AppCompatActivity {
    private static final String STATE_EDITED_LINES = "checkout.edited_lines";
    private OrderRepository repository;
    private final ArrayList<OrderRepository.CartLine> draftLines = new ArrayList<>();
    private OrderRepository.Order editingOrder;
    private boolean isBuyNow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);
        repository = new OrderRepository(this);

        if (!loadDraft()) return;
        restoreEditedLines(savedInstanceState);
        setupForm();
        renderItems();
    }

    // Khi xoay màn hình, giữ những thay đổi số lượng chưa bấm Lưu.
    @SuppressWarnings({"deprecation", "unchecked"})
    private void restoreEditedLines(Bundle state) {
        if (state == null || (editingOrder == null && !isBuyNow)) return;
        Object saved;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            saved = state.getSerializable(STATE_EDITED_LINES, ArrayList.class);
        } else {
            saved = state.getSerializable(STATE_EDITED_LINES);
        }
        if (!(saved instanceof ArrayList<?>)) return;
        ArrayList<?> list = (ArrayList<?>) saved;
        if (list.isEmpty()) return;
        for (Object item : list) {
            if (!(item instanceof OrderRepository.CartLine)) return;
        }
        draftLines.clear();
        for (Object item : list) {
            draftLines.add((OrderRepository.CartLine) item);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (editingOrder != null || isBuyNow) {
            outState.putSerializable(STATE_EDITED_LINES, draftLines);
        }
        super.onSaveInstanceState(outState);
    }

    @SuppressWarnings("deprecation")
    private boolean loadDraft() {
        OrderRepository.Order selected = (OrderRepository.Order) getIntent()
                .getSerializableExtra(AppConstants.EXTRA_SELECTED_ORDER);
        if (selected != null) {
            // Object trong Intent có thể cũ: lấy phiên bản hiện tại trước khi sửa.
            editingOrder = repository.findOrder(selected.id);
            if (editingOrder == null || !editingOrder.isEditable()) {
                Toast.makeText(this, "Đơn này không thể chỉnh sửa.", Toast.LENGTH_SHORT).show();
                finish();
                return false;
            }
            for (OrderRepository.CartLine line : editingOrder.lines) {
                draftLines.add(line.copy());
            }
        } else if (getIntent().hasExtra(AppConstants.EXTRA_BUY_NOW_PRODUCT)) {
            Product directProduct = readBuyNowProduct();
            if (directProduct != null) {
                isBuyNow = true;
                draftLines.add(new OrderRepository.CartLine(directProduct, 1));
            }
        } else {
            for (OrderRepository.CartLine line : repository.getCart()) {
                draftLines.add(line.copy());
            }
        }

        if (draftLines.isEmpty()) {
            Toast.makeText(this, "Chưa có sản phẩm để đặt hàng.", Toast.LENGTH_SHORT).show();
            finish();
            return false;
        }
        return true;
    }

    @SuppressWarnings("deprecation")
    private Product readBuyNowProduct() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return getIntent().getSerializableExtra(AppConstants.EXTRA_BUY_NOW_PRODUCT,
                    Product.class);
        }
        return (Product) getIntent().getSerializableExtra(AppConstants.EXTRA_BUY_NOW_PRODUCT);
    }

    private void setupForm() {
        findViewById(R.id.btnBackCheckout).setOnClickListener(v -> finish());
        MaterialButton save = findViewById(R.id.btnConfirmCheckout);
        if (editingOrder != null) {
            ((TextView) findViewById(R.id.tvCheckoutTitle)).setText("Chỉnh sửa đơn hàng");
            ((TextView) findViewById(R.id.tvCheckoutItemsHint))
                    .setText("Điều chỉnh số lượng trước khi lưu thay đổi");
            save.setText("Lưu thay đổi");
            input(R.id.etCheckoutRecipient).setText(editingOrder.recipient);
            input(R.id.etCheckoutPhone).setText(editingOrder.phone);
            input(R.id.etCheckoutAddress).setText(editingOrder.address);
            input(R.id.etCheckoutNote).setText(editingOrder.note);
            ((RadioGroup) findViewById(R.id.rgCheckoutPayment)).check(
                    OrderRepository.PAYMENT_COD.equals(editingOrder.paymentMethod)
                            ? R.id.rbCheckoutCOD : R.id.rbCheckoutTransfer);
        } else if (isBuyNow) {
            ((TextView) findViewById(R.id.tvCheckoutTitle)).setText("Mua ngay · Đơn demo");
            ((TextView) findViewById(R.id.tvCheckoutItemsHint))
                    .setText("Điều chỉnh số lượng; giỏ hàng khác vẫn được giữ nguyên");
            save.setText("Đặt mua ngay (demo)");
            String username = getSharedPreferences(AppConstants.PREF_USER_SESSION, MODE_PRIVATE)
                    .getString(AppConstants.KEY_USERNAME, "");
            input(R.id.etCheckoutRecipient).setText(username);
        } else {
            String username = getSharedPreferences(AppConstants.PREF_USER_SESSION, MODE_PRIVATE)
                    .getString(AppConstants.KEY_USERNAME, "");
            input(R.id.etCheckoutRecipient).setText(username);
        }
        save.setOnClickListener(v -> reviewAndSubmit());
    }

    private void renderItems() {
        LinearLayout container = findViewById(R.id.checkoutItemsContainer);
        container.removeAllViews();

        for (OrderRepository.CartLine line : draftLines) {
            View row = LayoutInflater.from(this).inflate(
                    R.layout.item_checkout_line, container, false);
            Product product = line.product;
            ImageView image = row.findViewById(R.id.imgCheckoutProduct);
            image.setImageResource(product.getImageResId());
            image.setContentDescription(product.getName());
            ((TextView) row.findViewById(R.id.tvCheckoutProductName))
                    .setText(product.getName());
            ((TextView) row.findViewById(R.id.tvCheckoutProductPrice))
                    .setText(OrderRepository.formatPrice(product.getPrice()) + "/kg");
            ((TextView) row.findViewById(R.id.tvCheckoutLineTotal))
                    .setText(OrderRepository.formatPrice(line.total()));
            ((TextView) row.findViewById(R.id.tvCheckoutQty))
                    .setText(String.valueOf(line.quantity));

            if (editingOrder != null || isBuyNow) {
                setupEditableItem(row, line);
            }
            container.addView(row);
        }
        ((TextView) findViewById(R.id.tvCheckoutTotal))
                .setText(OrderRepository.formatPrice(OrderRepository.totalOf(draftLines)));
    }

    private void setupEditableItem(View row, OrderRepository.CartLine line) {
        MaterialButton minus = row.findViewById(R.id.btnCheckoutDecrease);
        MaterialButton plus = row.findViewById(R.id.btnCheckoutIncrease);
        View remove = row.findViewById(R.id.btnCheckoutRemove);
        minus.setVisibility(View.VISIBLE);
        plus.setVisibility(View.VISIBLE);
        remove.setVisibility(editingOrder != null ? View.VISIBLE : View.GONE);
        minus.setEnabled(line.quantity > 1);
        plus.setEnabled(line.quantity < 99);
        minus.setOnClickListener(v -> {
            line.quantity--;
            renderItems();
        });
        plus.setOnClickListener(v -> {
            line.quantity++;
            renderItems();
        });
        remove.setOnClickListener(v -> {
            if (draftLines.size() == 1) {
                Toast.makeText(this, "Đơn hàng cần ít nhất một sản phẩm.",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            draftLines.remove(line);
            renderItems();
        });
    }

    private TextInputEditText input(int id) {
        return findViewById(id);
    }

    private String value(int id) {
        TextInputEditText field = input(id);
        return field.getText() == null ? "" : field.getText().toString().trim();
    }

    private String selectedPayment() {
        RadioGroup group = findViewById(R.id.rgCheckoutPayment);
        return group.getCheckedRadioButtonId() == R.id.rbCheckoutTransfer
                ? OrderRepository.PAYMENT_TRANSFER_DEMO : OrderRepository.PAYMENT_COD;
    }

    private boolean validateForm() {
        TextInputEditText recipient = input(R.id.etCheckoutRecipient);
        TextInputEditText phone = input(R.id.etCheckoutPhone);
        TextInputEditText address = input(R.id.etCheckoutAddress);
        recipient.setError(null);
        phone.setError(null);
        address.setError(null);

        if (value(R.id.etCheckoutRecipient).length() < 2) {
            recipient.setError("Nhập tên người nhận (ít nhất 2 ký tự)");
            recipient.requestFocus();
            return false;
        }
        if (!value(R.id.etCheckoutPhone).matches("[0-9]{9,11}")) {
            phone.setError("Số điện thoại cần 9–11 chữ số");
            phone.requestFocus();
            return false;
        }
        if (value(R.id.etCheckoutAddress).length() < 8) {
            address.setError("Nhập địa chỉ rõ hơn (ít nhất 8 ký tự)");
            address.requestFocus();
            return false;
        }
        return !draftLines.isEmpty();
    }

    private void reviewAndSubmit() {
        if (!validateForm()) return;
        String action = editingOrder == null ? "Tạo đơn demo" : "Lưu đơn demo";
        String message = "Tổng tạm tính: "
                + OrderRepository.formatPrice(OrderRepository.totalOf(draftLines))
                + "\n" + OrderRepository.paymentLabel(selectedPayment())
                + "\n\nĐây chỉ là mô phỏng. Không có giao dịch ngân hàng hoặc giao hàng thật.";
        new MaterialAlertDialogBuilder(this)
                .setTitle(action + "?")
                .setMessage(message)
                .setNegativeButton("Kiểm tra lại", null)
                .setPositiveButton("Xác nhận", (dialog, which) -> saveOrder())
                .show();
    }

    private void saveOrder() {
        String recipient = value(R.id.etCheckoutRecipient);
        String phone = value(R.id.etCheckoutPhone);
        String address = value(R.id.etCheckoutAddress);
        String note = value(R.id.etCheckoutNote);
        String payment = selectedPayment();
        if (editingOrder != null) {
            editingOrder.recipient = recipient;
            editingOrder.phone = phone;
            editingOrder.address = address;
            editingOrder.note = note;
            editingOrder.paymentMethod = payment;
            editingOrder.lines = new ArrayList<>(draftLines);
            if (repository.editOrder(editingOrder)) {
                Toast.makeText(this, "Đã lưu thay đổi đơn hàng.", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                showSaveError();
            }
        } else {
            OrderRepository.Order placed = isBuyNow
                    ? repository.placeDirectOrder(draftLines.get(0).product,
                            draftLines.get(0).quantity, recipient, phone, address, note, payment)
                    : repository.placeOrder(recipient, phone, address, note, payment);
            if (placed == null) {
                showSaveError();
                return;
            }
            Intent intent = new Intent(this, OrderDetailActivity.class);
            intent.putExtra(AppConstants.EXTRA_SELECTED_ORDER, placed);
            startActivity(intent);
            finish();
        }
    }

    private void showSaveError() {
        Toast.makeText(this, "Không lưu được đơn. Dữ liệu cũ vẫn được giữ.",
                Toast.LENGTH_LONG).show();
    }
}
