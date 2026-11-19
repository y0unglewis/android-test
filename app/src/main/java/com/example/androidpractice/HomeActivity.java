package com.example.androidpractice;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class HomeActivity extends AppCompatActivity {
    // Product lấy từ Firestore, được hiển thị bằng card có ảnh, tên và giá.
    private final ArrayList<Product> products = new ArrayList<>();
    private ProductAdapter fruitAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
//        uploadFruitsOnce(); // Bài Firebase: để comment, không upload tự động.

        showWelcomeMessage();
        setupFruitList();
        setupProductsButton();
        setupProfileButton();
        setupLogoutButton();
        setupBottomNavigation();
    }

    private void showWelcomeMessage() {
        SharedPreferences prefs = getSharedPreferences(
                AppConstants.PREF_USER_SESSION, MODE_PRIVATE);
        String username = prefs.getString(AppConstants.KEY_USERNAME, "bạn");

        TextView tvWelcome = findViewById(R.id.tvWelcome);
        tvWelcome.setText("Xin chào, " + username + "!");
    }

    private void setupFruitList() {
        // Tìm ListView trên giao diện Home.
        ListView listView = findViewById(R.id.listView);
        // Bài UI mới: tận dụng ProductAdapter có sẵn của bài ListView/Adapter.
        fruitAdapter = new ProductAdapter(this, products);
        listView.setAdapter(fruitAdapter);
        listView.setOnItemClickListener((parent, view, position, id) ->
                openProducts(position));

        loadFruitsFromFirestore();
    }

    // ===== BÀI CŨ: SQLITE TRONG HOME (GIỮ LẠI ĐỂ CÔ KIỂM TRA) =====
//        FruitDatabaseHelper dbHelper = new FruitDatabaseHelper(this);
//        String[] items = dbHelper.getAllNames();
//        dbHelper.close();
//        ListView listView = findViewById(R.id.listView);
//        ArrayAdapter<String> adapter = new ArrayAdapter<>(
//                this, android.R.layout.simple_list_item_1, items);
//        listView.setAdapter(adapter);

    private void setupProductsButton() {
        Button btnOpenProducts = findViewById(R.id.btnOpenProducts);
        btnOpenProducts.setOnClickListener(v -> openProducts(-1));
    }

    private void setupProfileButton() {
        ImageButton btnProfile = findViewById(R.id.btnProfile);
        btnProfile.setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class)));
    }

    private void setupLogoutButton() {
        Button btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> logout());
    }

    private void logout() {
        getSharedPreferences(AppConstants.PREF_USER_SESSION, MODE_PRIVATE)
                .edit()
                .remove(AppConstants.KEY_USERNAME)
                .apply();

        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    private void setupBottomNavigation() {
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

    // Ví dụ upload dữ liệu một lần: đang tắt để tránh ghi đè Firestore.
//    private void uploadFruitsOnce() {
//
//        FirebaseFirestore db = FirebaseFirestore.getInstance();
//
//        ArrayList<Product> products = DataSource.getProducts();
//
//        // Duyệt từng trái cây.
//        for (int i = 0; i < products.size(); i++) {
//
//            Product product = products.get(i);
//
//            Map<String, Object> data = new HashMap<>();
//
//            data.put("name", product.getName());
//            data.put("origin", product.getOrigin());
//            data.put("description", product.getDescription());
//            data.put("price", (long) product.getPrice());
//
//            String imageName = getResources()
//                    .getResourceEntryName(product.getImageResId());
//
//            data.put("image_name", imageName);
//            data.put("sort_order", (long) (i + 1));
//
//            int documentId = i + 1;
//
//            // Ghi một document lên Firebase.
//            db.collection("fruits")
//                    .document(String.valueOf(documentId))
//                    .set(data)
//                    .addOnSuccessListener(unused ->
//                            Log.d("Firestore", "Đã upload: " + documentId))
//                    .addOnFailureListener(e ->
//                            Log.e("Firestore", "Lỗi upload: " + documentId, e));
//        }
//    }


    private void loadFruitsFromFirestore() {
        showFruitStatus("Đang tải danh sách trái cây...", true);
        // Lấy danh sách trái cây từ Cloud Firestore.
        // Lấy dữ liệu từ collection fruits.
        FirebaseFirestore.getInstance()
                .collection(AppConstants.COLLECTION_FRUITS)
                .orderBy(AppConstants.FIELD_SORT_ORDER)
                .get()
                .addOnSuccessListener(this::showFruitCards)
                .addOnFailureListener(e -> {
                    showFruitStatus("Không thể tải trái cây. Hãy kiểm tra kết nối mạng.", true);
                    Toast.makeText(this,
                            "Không thể tải dữ liệu: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    private void showFruitCards(QuerySnapshot querySnapshot) {
        // Xóa dữ liệu cũ trước khi vẽ lại card.
        products.clear();

        // Duyệt từng document trả về.
        for (QueryDocumentSnapshot document : querySnapshot) {
            Product product = FirestoreProductMapper.fromDocument(this, document);
            products.add(product);
        }
        // Thông báo danh sách Product thay đổi để Adapter cập nhật ListView.
        fruitAdapter.notifyDataSetChanged();

        if (products.isEmpty()) {
            showFruitStatus("Hiện chưa có trái cây nào trong danh sách.", true);
        } else {
            showFruitStatus("", false);
        }
    }

    private void showFruitStatus(String message, boolean visible) {
        TextView status = findViewById(R.id.tvFruitStatus);
        status.setText(message);
        status.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    private void openProducts(int position) {
        Intent intent = new Intent(this, DetailActivity.class);

        if (position >= 0 && position < products.size()) {
            // Một Intent có thể mang nhiều cặp key-value.
            intent.putExtra(AppConstants.EXTRA_SELECTED_POSITION, position);
            intent.putExtra(AppConstants.EXTRA_SELECTED_PRODUCT, products.get(position));
        }

        startActivity(intent);
    }
}
