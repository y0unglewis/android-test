package com.example.androidpractice;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.ArrayList;
import android.widget.Toast;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class DetailActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Layout Detail chứa nút Back, bộ đếm, ListView và thanh menu dưới.
        setContentView(R.layout.activity_detail);

        // finish() đóng Detail và trở lại màn hình trước, không tạo Home mới.
        ImageButton btnBackHome = findViewById(R.id.btnBackHome);
        btnBackHome.setOnClickListener(v -> finish());

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

        TextView tvProductCount = findViewById(R.id.tvProductCount);
        ListView listView = findViewById(R.id.listViewProducts);

        loadProductsFromFirestore(listView, tvProductCount);

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

    private void loadProductsFromFirestore(
            ListView listView,
            TextView tvProductCount
    ) {

        tvProductCount.setText("Đang tải dữ liệu...");

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("fruits")
                .orderBy("sort_order")
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    ArrayList<Product> products = new ArrayList<>();

                    // Duyệt từng document trên Firebase.
                    for (QueryDocumentSnapshot document : querySnapshot) {

                        String name = document.getString("name");
                        String origin = document.getString("origin");
                        String description = document.getString("description");
                        Long price = document.getLong("price");
                        String imageName = document.getString("image_name");

                        // Lấy hình ảnh từ drawable dựa trên tên lưu ở Firebase.
                        int imageResId = 0;

                        if (imageName != null) {
                            imageResId = getResources().getIdentifier(
                                    imageName,
                                    "drawable",
                                    getPackageName()
                            );
                        }

                        if (imageResId == 0) {
                            imageResId = R.drawable.fruit_apple;
                        }

                        //
                        Product product = new Product(
                                name != null ? name : "",
                                origin != null ? origin : "",
                                description != null ? description : "",
                                price != null ? price.intValue() : 0,
                                imageResId
                        );

                        products.add(product);
                    }

                    tvProductCount.setText(
                            products.size() + " loại trái cây"
                    );

                    ProductAdapter adapter = new ProductAdapter(
                            this,
                            products
                    );

                    listView.setAdapter(adapter);

                    int selected = getIntent().getIntExtra(
                            "selected_position",
                            -1
                    );

                    if (selected >= 0 && selected < products.size()) {
                        listView.post(() ->
                                listView.setSelection(selected));
                    }
                })
                .addOnFailureListener(e -> {

                    tvProductCount.setText("Không tải được dữ liệu");

                    Toast.makeText(
                            this,
                            "Lỗi Firestore: " + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }
}
