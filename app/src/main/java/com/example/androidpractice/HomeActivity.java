package com.example.androidpractice;

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
import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class HomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
//        uploadFruitsOnce();

        TextView tvWelcome = findViewById(R.id.tvWelcome);

        SharedPreferences prefs = getSharedPreferences(
                "user_session",
                MODE_PRIVATE
        );

        String username = prefs.getString("username", "bạn");

        tvWelcome.setText("Xin chào, " + username + "!");

//        FruitDatabaseHelper dbHelper = new FruitDatabaseHelper(this);
//        String[] items = dbHelper.getAllNames();
//        dbHelper.close();
//        ListView listView = findViewById(R.id.listView);
//        ArrayAdapter<String> adapter = new ArrayAdapter<>(
//                this, android.R.layout.simple_list_item_1, items);
//        listView.setAdapter(adapter);

        // Tìm ListView trên giao diện Home.
        ListView listView = findViewById(R.id.listView);

        // Lấy danh sách trái cây từ Cloud Firestore.
        loadFruitNamesFromFirestore(listView);

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

    private void uploadFruitsOnce() {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        ArrayList<Product> products = DataSource.getProducts();

        // Duyệt từng trái cây.
        for (int i = 0; i < products.size(); i++) {

            Product product = products.get(i);

            // Tạo dữ liệu cho một document.
            Map<String, Object> data = new HashMap<>();

            data.put("name", product.getName());
            data.put("origin", product.getOrigin());
            data.put("description", product.getDescription());
            data.put("price", (long) product.getPrice());

            String imageName = getResources()
                    .getResourceEntryName(product.getImageResId());

            data.put("image_name", imageName);
            data.put("sort_order", (long) (i + 1));

            int documentId = i + 1;

            // Ghi một document lên Firebase.
            db.collection("fruits")
                    .document(String.valueOf(documentId))
                    .set(data)
                    .addOnSuccessListener(unused ->
                            Log.d("Firestore", "Đã upload: " + documentId))
                    .addOnFailureListener(e ->
                            Log.e("Firestore", "Lỗi upload: " + documentId, e));
        }
    }

    private void loadFruitNamesFromFirestore(ListView listView) {

        ArrayList<String> names = new ArrayList<>();

        // Tạo Adapter để hiển thị dữ liệu lên ListView.
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                names
        );

        listView.setAdapter(adapter);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Lấy dữ liệu từ collection fruits.
        db.collection("fruits")
                .orderBy("sort_order")
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    // Xóa dữ liệu cũ trong danh sách.
                    names.clear();

                    // Duyệt từng document trả về.
                    for (QueryDocumentSnapshot document : querySnapshot) {

                        String name = document.getString("name");

                        if (name != null) {
                            names.add(name);
                        }
                    }

                    // Thông báo dữ liệu đã thay đổi để ListView cập nhật.
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {

                    Toast.makeText(
                            this,
                            "Không thể tải dữ liệu: " + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    private void openProducts(int position) {
        Intent intent = new Intent(this, DetailActivity.class);
        if (position >= 0) {
            intent.putExtra("selected_position", position);
        }
        startActivity(intent);
    }
}
