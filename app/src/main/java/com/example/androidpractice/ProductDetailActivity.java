package com.example.androidpractice;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

// Màn hình CHI TIẾT riêng: nhận Product từ Intent, không gọi thêm Firestore.
public class ProductDetailActivity extends AppCompatActivity {
    private Product selectedProduct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        readProductFromIntent();
        setupNavigation();
        showProductInformation();
    }

    @SuppressWarnings("deprecation")
    private void readProductFromIntent() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            selectedProduct = getIntent().getSerializableExtra(
                    AppConstants.EXTRA_SELECTED_PRODUCT, Product.class);
        } else {
            selectedProduct = (Product) getIntent().getSerializableExtra(
                    AppConstants.EXTRA_SELECTED_PRODUCT);
        }
        // Tránh hiển thị trang trắng nếu Activity mở mà không truyền Product.
        if (selectedProduct == null) {
            finish();
        }
    }

    private void setupNavigation() {
        ImageButton back = findViewById(R.id.btnBackProductDetail);
        back.setOnClickListener(v -> finish());

        findViewById(R.id.btnBrowseMoreProducts).setOnClickListener(v -> {
            Intent intent = new Intent(this, DetailActivity.class);
            startActivity(intent);
        });
    }

    private void showProductInformation() {
        if (selectedProduct == null) {
            return;
        }

        ImageView image = findViewById(R.id.imgDetailProduct);
        TextView name = findViewById(R.id.tvDetailProductName);
        TextView price = findViewById(R.id.tvDetailProductPrice);
        TextView origin = findViewById(R.id.tvDetailProductOrigin);
        TextView description = findViewById(R.id.tvDetailProductDescription);

        image.setImageResource(selectedProduct.getImageResId());
        image.setContentDescription(selectedProduct.getName());
        name.setText(selectedProduct.getName());
        price.setText(String.format(Locale.forLanguageTag("vi-VN"),
                "%,d đ/kg", selectedProduct.getPrice()));
        origin.setText(selectedProduct.getOrigin().isEmpty()
                ? "Chưa cập nhật" : selectedProduct.getOrigin());
        description.setText(selectedProduct.getDescription().isEmpty()
                ? "Chưa có mô tả cho sản phẩm này." : selectedProduct.getDescription());
    }
}

