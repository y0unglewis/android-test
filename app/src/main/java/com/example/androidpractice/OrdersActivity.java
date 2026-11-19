package com.example.androidpractice;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class OrdersActivity extends AppCompatActivity {
    private OrderRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);
        repository = new OrderRepository(this);
        setupNavigation();
    }

    private void setupNavigation() {
        findViewById(R.id.btnBackOrders).setOnClickListener(v -> finish());
        findViewById(R.id.btnShopFromOrders).setOnClickListener(v ->
                startActivity(new Intent(this, DetailActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        showOrders();
    }

    private void showOrders() {
        ArrayList<OrderRepository.Order> orders = repository.getOrders();
        LinearLayout container = findViewById(R.id.ordersContainer);
        container.removeAllViews();
        for (OrderRepository.Order order : orders) {
            View row = LayoutInflater.from(this).inflate(R.layout.item_order, container, false);
            bindOrder(row, order);
            container.addView(row);
        }
        ((TextView) findViewById(R.id.tvOrdersCount))
                .setText(orders.size() + " đơn hàng trên thiết bị");
        findViewById(R.id.ordersEmptyState)
                .setVisibility(orders.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void bindOrder(View row, OrderRepository.Order order) {
        ((TextView) row.findViewById(R.id.tvOrderId)).setText(order.id);
        TextView status = row.findViewById(R.id.tvOrderStatus);
        status.setText(OrderRepository.statusLabel(order.status));
        status.setTextColor(getColor(OrderRepository.STATUS_CANCELLED.equals(order.status)
                ? R.color.text_secondary : order.isEditable()
                ? R.color.price_color : R.color.brand_primary));

        SimpleDateFormat date = new SimpleDateFormat("dd/MM/yyyy HH:mm",
                Locale.forLanguageTag("vi-VN"));
        ((TextView) row.findViewById(R.id.tvOrderDate))
                .setText("Tạo lúc " + date.format(new Date(order.createdAt)));

        StringBuilder summary = new StringBuilder();
        for (int i = 0; i < order.lines.size(); i++) {
            if (i > 0) summary.append(" · ");
            summary.append(order.lines.get(i).product.getName())
                    .append(" ×").append(order.lines.get(i).quantity);
        }
        ((TextView) row.findViewById(R.id.tvOrderSummary)).setText(summary.toString());
        ((TextView) row.findViewById(R.id.tvOrderTotal))
                .setText(OrderRepository.formatPrice(order.total()));

        View.OnClickListener open = v -> {
            Intent intent = new Intent(this, OrderDetailActivity.class);
            // Gửi nguyên đơn hàng qua Intent, giống cách truyền Product.
            intent.putExtra(AppConstants.EXTRA_SELECTED_ORDER, order);
            startActivity(intent);
        };
        row.findViewById(R.id.btnOpenOrder).setOnClickListener(open);
        row.setOnClickListener(open);
    }
}
