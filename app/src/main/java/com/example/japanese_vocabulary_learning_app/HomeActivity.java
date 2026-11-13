package com.example.japanese_vocabulary_learning_app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.content.SharedPreferences;
import android.widget.TextView;

public class HomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        TextView tvWelcome = findViewById(R.id.tvWelcome);

        SharedPreferences prefs = getSharedPreferences(
                "user_session",
                MODE_PRIVATE
        );

        String username = prefs.getString("username", "bạn");

        tvWelcome.setText("Xin chào, " + username + "!");

        // Đọc danh sách tên từ SQLite thay vì mảng viết sẵn trong DataSource.
        // Lần đầu mở database, SQLiteOpenHelper sẽ tự tạo bảng và lưu 15 trái cây mẫu.
        FruitDatabaseHelper dbHelper = new FruitDatabaseHelper(this);
        String[] items = dbHelper.getAllNames();
        dbHelper.close();
        ListView listView = findViewById(R.id.listView);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_list_item_1, items);
        listView.setAdapter(adapter);

        Button btnOpenProducts = findViewById(R.id.btnOpenProducts);
        btnOpenProducts.setOnClickListener(v -> openProducts(-1));
        listView.setOnItemClickListener((parent, view, position, id) ->
                openProducts(position));

        // Nhấn icon ở Home để mở hồ sơ. Giữ lại Home trong back stack.
        ImageButton btnProfile = findViewById(R.id.btnProfile);
        btnProfile.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
        });

        Button btnLogout = findViewById(R.id.btnLogout);

        btnLogout.setOnClickListener(v -> {

            prefs.edit()
                    .remove("username")
                    .apply();

            Intent intent = new Intent(this, LoginActivity.class);

            startActivity(intent);
            finish();
        });

        // Navigation bar
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
        BottomNavigationView nav = findViewById(R.id.bottomNav);
        if (nav != null) {
            nav.setSelectedItemId(R.id.nav_home);
        }
    }

    private void openProducts(int position) {
        Intent intent = new Intent(this, DetailActivity.class);
        if (position >= 0) {
            intent.putExtra("selected_position", position);
        }
        startActivity(intent);
    }
}
