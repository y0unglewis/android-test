package com.example.androidpractice;

import android.os.Build;
import android.os.Bundle;
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

public class DetailActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Layout Detail chứa nút Back, bộ đếm, ListView và thanh menu dưới.
        setContentView(R.layout.activity_detail);

        setupBackButton();
        setupProductList();
        setupBottomNavigation();
    }

    private void setupBackButton() {
        // finish() đóng Detail và trở lại màn hình trước, không tạo Home mới.
        ImageButton btnBackHome = findViewById(R.id.btnBackHome);
        btnBackHome.setOnClickListener(v -> finish());
    }

    private void setupProductList() {
        TextView tvProductCount = findViewById(R.id.tvProductCount);
        ListView listView = findViewById(R.id.listViewProducts);
        loadProductsFromFirestore(listView, tvProductCount);
    }

    // ===== BÀI CŨ: SQLITE TRONG DETAIL (GIỮ LẠI ĐỂ CÔ KIỂM TRA) =====
//        // Lấy danh sách trái cây thật từ bảng fruits trong SQLite.
//        FruitDatabaseHelper dbHelper = new FruitDatabaseHelper(this);
//        ArrayList<Product> products = dbHelper.getAllProducts();
//        dbHelper.close();

//        TextView tvProductCount = findViewById(R.id.tvProductCount);
//        tvProductCount.setText(products.size() + " loại trái cây");
//
//        // ProductAdapter tùy chỉnh mỗi dòng bằng item_product.xml.
//        ListView listView = findViewById(R.id.listViewProducts);
//        ProductAdapter adapter = new ProductAdapter(this, products);
//        listView.setAdapter(adapter);
//
//        // Nhận vị trí sản phẩm từ Home bằng getIntExtra(); -1 nghĩa là không chọn.
//        int selected = getIntent().getIntExtra("selected_position", -1);
//        if (selected >= 0 && selected < products.size()) {
//            // Sau khi View sẵn sàng, setSelection() cuộn đến sản phẩm được chọn.
//            listView.post(() -> listView.setSelection(selected));
//        }

    private void setupBottomNavigation() {
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

    private void loadProductsFromFirestore(ListView listView, TextView tvProductCount) {
        tvProductCount.setText("Đang tải dữ liệu...");

        FirebaseFirestore.getInstance()
                .collection(AppConstants.COLLECTION_FRUITS)
                .orderBy(AppConstants.FIELD_SORT_ORDER)
                .get()
                .addOnSuccessListener(querySnapshot ->
                        showProducts(querySnapshot, listView, tvProductCount))
                .addOnFailureListener(e -> {
                    tvProductCount.setText("Không tải được dữ liệu");
                    Toast.makeText(this,
                            "Lỗi Firestore: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    private void showProducts(QuerySnapshot querySnapshot,
                              ListView listView, TextView tvProductCount) {
        ArrayList<Product> products = new ArrayList<>();
        // Duyệt từng document trên Firebase.
        for (QueryDocumentSnapshot document : querySnapshot) {
            products.add(FirestoreProductMapper.fromDocument(this, document));
        }

        tvProductCount.setText(products.size() + " loại trái cây");
        listView.setAdapter(new ProductAdapter(this, products));
        scrollToSelectedProduct(listView, products);
    }

    private void scrollToSelectedProduct(ListView listView, ArrayList<Product> products) {
        int position = getIntent().getIntExtra(AppConstants.EXTRA_SELECTED_POSITION, -1);
        Product selectedProduct = getSelectedProduct();

        // Dùng Product được truyền qua Intent để tìm lại đúng mặt hàng.
        if (selectedProduct != null) {
            for (int i = 0; i < products.size(); i++) {
                Product product = products.get(i);
                if (product.getName().equals(selectedProduct.getName())
                        && product.getOrigin().equals(selectedProduct.getOrigin())) {
                    position = i;
                    break;
                }
            }
        }

        if (position >= 0 && position < products.size()) {
            int selectedPosition = position;
            listView.post(() -> listView.setSelection(selectedPosition));
        }
    }

    @SuppressWarnings("deprecation")
    private Product getSelectedProduct() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return getIntent().getSerializableExtra(
                    AppConstants.EXTRA_SELECTED_PRODUCT, Product.class);
        }
        return (Product) getIntent().getSerializableExtra(
                AppConstants.EXTRA_SELECTED_PRODUCT);
    }
}
