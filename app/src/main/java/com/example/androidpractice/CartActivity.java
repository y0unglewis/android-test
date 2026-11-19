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

import android.widget.ImageButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;

public class CartActivity extends AppCompatActivity {
    private OrderRepository orders;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);
        orders = new OrderRepository(this);

        setupNavigation();
        renderCart();
    }

    private void setupNavigation() {
        findViewById(R.id.btnBackCart).setOnClickListener(v -> finish());
        findViewById(R.id.btnShopFromCart).setOnClickListener(v ->
                startActivity(new Intent(this, DetailActivity.class)));
        findViewById(R.id.btnOrdersFromCart).setOnClickListener(v ->
                startActivity(new Intent(this, OrdersActivity.class)));
        findViewById(R.id.btnCheckout).setOnClickListener(v -> {
            if (orders.getCart().isEmpty()) return;
            startActivity(new Intent(this, CheckoutActivity.class));
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (orders != null) renderCart();
    }

    private void renderCart() {
        ArrayList<OrderRepository.CartLine> lines = orders.getCart();
        LinearLayout container = findViewById(R.id.cartItemsContainer);
        container.removeAllViews();

        int quantity = 0;
        for (OrderRepository.CartLine line : lines) {
            quantity += line.quantity;
            View row = LayoutInflater.from(this).inflate(R.layout.item_cart, container, false);
            bindCartLine(row, line);
            container.addView(row);
        }

        ((TextView) findViewById(R.id.tvCartCount)).setText(quantity + " sản phẩm");
        ((TextView) findViewById(R.id.tvCartTotal))
                .setText(OrderRepository.formatPrice(OrderRepository.totalOf(lines)));
        findViewById(R.id.cartEmptyState).setVisibility(
                lines.isEmpty() ? View.VISIBLE : View.GONE);
        findViewById(R.id.btnCheckout).setEnabled(!lines.isEmpty());
    }

    private void bindCartLine(View row, OrderRepository.CartLine line) {
        Product product = line.product;
        ImageView image = row.findViewById(R.id.imgCartProduct);
        image.setImageResource(product.getImageResId());
        image.setContentDescription(product.getName());
        ((TextView) row.findViewById(R.id.tvCartProductName)).setText(product.getName());
        ((TextView) row.findViewById(R.id.tvCartUnitPrice))
                .setText(OrderRepository.formatPrice(product.getPrice()) + "/kg");
        ((TextView) row.findViewById(R.id.tvCartQuantity))
                .setText(String.valueOf(line.quantity));
        ((TextView) row.findViewById(R.id.tvCartLineTotal))
                .setText(OrderRepository.formatPrice(line.total()));

        ImageButton minus = row.findViewById(R.id.btnCartDecrease);
        ImageButton plus = row.findViewById(R.id.btnCartIncrease);
        minus.setEnabled(line.quantity > 1);
        plus.setEnabled(line.quantity < 99);
        minus.setAlpha(line.quantity > 1 ? 1f : 0.35f);
        plus.setAlpha(line.quantity < 99 ? 1f : 0.35f);
        minus.setOnClickListener(v -> changeQuantity(product, line.quantity - 1));
        plus.setOnClickListener(v -> changeQuantity(product, line.quantity + 1));
        row.findViewById(R.id.btnCartRemove).setOnClickListener(v ->
                new MaterialAlertDialogBuilder(this)
                        .setTitle("Xóa sản phẩm khỏi giỏ?")
                        .setMessage(product.getName() + " sẽ được gỡ khỏi giỏ hàng.")
                        .setNegativeButton("Giữ lại", null)
                        .setPositiveButton("Xóa", (dialog, which) -> {
                            if (orders.removeFromCart(product)) {
                                renderCart();
                            } else {
                                showSaveError();
                            }
                        })
                        .show());
    }

    private void changeQuantity(Product product, int quantity) {
        if (orders.changeCartQuantity(product, quantity)) {
            renderCart();
        } else {
            showSaveError();
        }
    }

    private void showSaveError() {
        Toast.makeText(this, "Không lưu được giỏ hàng. Hãy thử lại.",
                Toast.LENGTH_SHORT).show();
    }
}
