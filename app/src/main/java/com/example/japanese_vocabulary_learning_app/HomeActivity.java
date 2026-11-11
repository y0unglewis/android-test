package com.example.japanese_vocabulary_learning_app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Layout Home chứa danh sách tên trái cây và thanh điều hướng phía dưới.
        setContentView(R.layout.activity_home);

        // Home dùng ArrayAdapter<String> vì mỗi dòng chỉ cần tên sản phẩm.
        String[] items = DataSource.getItems();
        ListView listView = findViewById(R.id.listView);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_list_item_1, items);
        // Gắn Adapter vào ListView để Android lấy dữ liệu và hiển thị từng dòng.
        listView.setAdapter(adapter);

        // Nút xem tất cả gửi -1; chọn một dòng gửi position của trái cây đó.
        Button btnOpenProducts = findViewById(R.id.btnOpenProducts);
        btnOpenProducts.setOnClickListener(v -> openProducts(-1));
        listView.setOnItemClickListener((parent, view, position, id) ->
                openProducts(position));

        // Xóa task khi đăng xuất để Back không trở lại Home đã mở.
        Button btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        // Thanh điều hướng có hai mục: Home và Products.
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_home);
        bottomNav.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_products) {
                openProducts(-1);
                return true;
            }
            return item.getItemId() == R.id.nav_home;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Khi quay về từ Detail, đánh dấu tab Home là đang chọn.
        BottomNavigationView nav = findViewById(R.id.bottomNav);
        if (nav != null) {
            nav.setSelectedItemId(R.id.nav_home);
        }
    }

    // Hàm tự viết: mở Detail và có thể gửi vị trí item qua Intent.putExtra().
    private void openProducts(int position) {
        Intent intent = new Intent(this, DetailActivity.class);
        if (position >= 0) {
            intent.putExtra("selected_position", position);
        }
        startActivity(intent);
    }
}
