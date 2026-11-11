package com.example.japanese_vocabulary_learning_app;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.ArrayList;

public class DetailActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Layout Detail chứa nút Back, bộ đếm, ListView và thanh menu dưới.
        setContentView(R.layout.activity_detail);

        // finish() đóng Detail và trở lại màn hình trước, không tạo Home mới.
        ImageButton btnBackHome = findViewById(R.id.btnBackHome);
        btnBackHome.setOnClickListener(v -> finish());

        // Danh sách chứa các Product, mỗi Product có 5 thuộc tính.
        ArrayList<Product> products = DataSource.getProducts();
        // Đếm phần tử bằng size() để giao diện cập nhật theo dữ liệu.
        TextView tvProductCount = findViewById(R.id.tvProductCount);
        tvProductCount.setText(products.size() + " loại trái cây");

        // ProductAdapter tùy chỉnh mỗi dòng bằng item_product.xml.
        ListView listView = findViewById(R.id.listViewProducts);
        ProductAdapter adapter = new ProductAdapter(this, products);
        listView.setAdapter(adapter);

        // Nhận vị trí sản phẩm từ Home bằng getIntExtra(); -1 nghĩa là không chọn.
        int selected = getIntent().getIntExtra("selected_position", -1);
        if (selected >= 0 && selected < products.size()) {
            // Sau khi View sẵn sàng, setSelection() cuộn đến sản phẩm được chọn.
            listView.post(() -> listView.setSelection(selected));
        }

        // Highlight tab Products; chọn Home thì finish() quay về.
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_products);
        bottomNav.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) {
                finish();
                return true;
            }
            return item.getItemId() == R.id.nav_products;
        });
    }
}
