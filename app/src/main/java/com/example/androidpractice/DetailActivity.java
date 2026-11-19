package com.example.androidpractice;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class DetailActivity extends AppCompatActivity {
    // Bộ lọc hoạt động trên danh sách lấy MỘT LẦN từ Firestore, không đổi dữ liệu gốc.
    private static final int PRICE_ALL = 0;
    private static final int PRICE_UNDER_30K = 1;
    private static final int PRICE_30K_TO_60K = 2;
    private static final int PRICE_ABOVE_60K = 3;

    private static final int SORT_DEFAULT = 0;
    private static final int SORT_PRICE_ASC = 1;
    private static final int SORT_PRICE_DESC = 2;
    private static final int SORT_NAME_ASC = 3;

    private final ArrayList<Product> allProducts = new ArrayList<>();
    private final ArrayList<Product> visibleProducts = new ArrayList<>();
    private ProductAdapter productAdapter;

    private String searchQuery = "";
    private String selectedOrigin = null;
    private int priceFilter = PRICE_ALL;
    private int sortMode = SORT_DEFAULT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        setupBackButton();
        setupProductList();
        setupSearchAndFilters();
        setupBottomNavigation();
    }

    private void setupBackButton() {
        ImageButton btnBackHome = findViewById(R.id.btnBackHome);
        btnBackHome.setOnClickListener(v -> finish());
    }

    private void setupProductList() {
        ListView listView = findViewById(R.id.listViewProducts);
        productAdapter = new ProductAdapter(this, visibleProducts);
        listView.setAdapter(productAdapter);
        listView.setOnItemClickListener((parent, view, position, id) ->
                openProductDetails(visibleProducts.get(position)));

        // Nút thử lại chỉ có tác dụng nếu Firestore tải lỗi.
        findViewById(R.id.btnRetryProducts).setOnClickListener(v ->
                loadProductsFromFirestore());
        loadProductsFromFirestore();
    }

    private void setupSearchAndFilters() {
        TextInputEditText search = findViewById(R.id.etSearchProducts);
        search.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = s.toString();
                applyFilters();
            }
            @Override public void afterTextChanged(Editable s) { }
        });

        findViewById(R.id.btnFilterOrigin).setOnClickListener(v -> showOriginChoices());
        findViewById(R.id.btnFilterPrice).setOnClickListener(v -> showPriceChoices());
        findViewById(R.id.btnSortProducts).setOnClickListener(v -> showSortChoices());
        findViewById(R.id.btnClearFilters).setOnClickListener(v -> clearFilters());
        updateFilterLabels();
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

    private void loadProductsFromFirestore() {
        TextView count = findViewById(R.id.tvProductCount);
        count.setText("Đang tải...");
        showEmptyState("Đang tải trái cây từ Firebase...", false, false);

        FirebaseFirestore.getInstance()
                .collection(AppConstants.COLLECTION_FRUITS)
                .orderBy(AppConstants.FIELD_SORT_ORDER)
                .get()
                .addOnSuccessListener(this::showProducts)
                .addOnFailureListener(e -> {
                    count.setText("Không thể tải");
                    showEmptyState("Không tải được sản phẩm. Kiểm tra kết nối rồi thử lại nhé.", true, true);
                    Toast.makeText(this, "Lỗi Firestore: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void showProducts(QuerySnapshot snapshot) {
        allProducts.clear();
        for (QueryDocumentSnapshot document : snapshot) {
            allProducts.add(FirestoreProductMapper.fromDocument(this, document));
        }
        applyFilters();
        scrollToSelectedProduct();
    }

    private void applyFilters() {
        visibleProducts.clear();
        String normalizedQuery = normalizeForSearch(searchQuery.trim());

        for (Product product : allProducts) {
            boolean matchesSearch = normalizedQuery.isEmpty()
                    || normalizeForSearch(product.getName()).contains(normalizedQuery)
                    || normalizeForSearch(product.getOrigin()).contains(normalizedQuery)
                    || normalizeForSearch(product.getDescription()).contains(normalizedQuery);

            boolean matchesOrigin = selectedOrigin == null
                    || selectedOrigin.equalsIgnoreCase(product.getOrigin());

            int price = product.getPrice();
            boolean matchesPrice = priceFilter == PRICE_ALL
                    || (priceFilter == PRICE_UNDER_30K && price < 30000)
                    || (priceFilter == PRICE_30K_TO_60K && price >= 30000 && price <= 60000)
                    || (priceFilter == PRICE_ABOVE_60K && price > 60000);

            if (matchesSearch && matchesOrigin && matchesPrice) {
                visibleProducts.add(product);
            }
        }

        if (sortMode == SORT_PRICE_ASC) {
            Collections.sort(visibleProducts, Comparator.comparingInt(Product::getPrice));
        } else if (sortMode == SORT_PRICE_DESC) {
            Collections.sort(visibleProducts, (a, b) ->
                    Integer.compare(b.getPrice(), a.getPrice()));
        } else if (sortMode == SORT_NAME_ASC) {
            Collections.sort(visibleProducts, (a, b) ->
                    normalizeForSearch(a.getName()).compareTo(normalizeForSearch(b.getName())));
        }

        productAdapter.notifyDataSetChanged();
        TextView count = findViewById(R.id.tvProductCount);
        count.setText(visibleProducts.size() + "/" + allProducts.size() + " sản phẩm");

        boolean noResults = visibleProducts.isEmpty();
        String emptyMessage = allProducts.isEmpty()
                ? "Chưa có sản phẩm nào trên Firestore."
                : "Không tìm thấy sản phẩm phù hợp. Hãy thử bộ lọc khác nhé.";
        showEmptyState(emptyMessage, noResults, false);
    }

    private void showEmptyState(String message, boolean visible, boolean showRetry) {
        View empty = findViewById(R.id.productEmptyState);
        empty.setVisibility(visible ? View.VISIBLE : View.GONE);
        ((TextView) findViewById(R.id.tvProductEmpty)).setText(message);
        findViewById(R.id.btnRetryProducts).setVisibility(showRetry ? View.VISIBLE : View.GONE);
    }

    private void showOriginChoices() {
        List<String> origins = new ArrayList<>();
        origins.add("Tất cả xuất xứ");
        for (Product product : allProducts) {
            String origin = product.getOrigin();
            if (origin != null && !origin.trim().isEmpty() && !origins.contains(origin)) {
                origins.add(origin);
            }
        }
        Collections.sort(origins.subList(1, origins.size()), String.CASE_INSENSITIVE_ORDER);
        new MaterialAlertDialogBuilder(this)
                .setTitle("Lọc theo xuất xứ")
                .setItems(origins.toArray(new String[0]), (dialog, which) -> {
                    selectedOrigin = which == 0 ? null : origins.get(which);
                    updateFilterLabels();
                    applyFilters();
                })
                .show();
    }

    private void showPriceChoices() {
        String[] choices = {"Mọi mức giá", "Dưới 30.000 đ/kg", "30.000–60.000 đ/kg",
                "Trên 60.000 đ/kg"};
        new MaterialAlertDialogBuilder(this)
                .setTitle("Lọc theo giá")
                .setSingleChoiceItems(choices, priceFilter, (dialog, which) -> {
                    priceFilter = which;
                    updateFilterLabels();
                    applyFilters();
                    dialog.dismiss();
                })
                .setNegativeButton("Đóng", null)
                .show();
    }

    private void showSortChoices() {
        String[] choices = {"Thứ tự mặc định", "Giá: thấp đến cao",
                "Giá: cao đến thấp", "Tên: A đến Z"};
        new MaterialAlertDialogBuilder(this)
                .setTitle("Sắp xếp sản phẩm")
                .setSingleChoiceItems(choices, sortMode, (dialog, which) -> {
                    sortMode = which;
                    updateFilterLabels();
                    applyFilters();
                    dialog.dismiss();
                })
                .setNegativeButton("Đóng", null)
                .show();
    }

    private void updateFilterLabels() {
        MaterialButton originButton = findViewById(R.id.btnFilterOrigin);
        MaterialButton priceButton = findViewById(R.id.btnFilterPrice);
        MaterialButton sortButton = findViewById(R.id.btnSortProducts);
        originButton.setText(selectedOrigin == null ? "Xuất xứ  ⌄" : selectedOrigin + "  ⌄");
        priceButton.setText(priceFilter == PRICE_ALL ? "Mức giá  ⌄" : "Đã lọc giá  ✓");
        sortButton.setText(sortMode == SORT_DEFAULT ? "Sắp xếp  ⌄" : "Đã sắp xếp  ✓");

        boolean hasFilters = selectedOrigin != null || priceFilter != PRICE_ALL
                || sortMode != SORT_DEFAULT || !searchQuery.trim().isEmpty();
        findViewById(R.id.btnClearFilters).setVisibility(
                hasFilters ? View.VISIBLE : View.GONE);
    }

    private void clearFilters() {
        searchQuery = "";
        selectedOrigin = null;
        priceFilter = PRICE_ALL;
        sortMode = SORT_DEFAULT;
        TextInputEditText search = findViewById(R.id.etSearchProducts);
        if (search.getText() != null) {
            search.setText("");
        }
        updateFilterLabels();
        applyFilters();
    }

    private String normalizeForSearch(String text) {
        String lower = (text == null ? "" : text).toLowerCase(Locale.ROOT).replace('đ', 'd');
        return Normalizer.normalize(lower, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");
    }

    private void openProductDetails(Product product) {
        Intent intent = new Intent(this, ProductDetailActivity.class);
        intent.putExtra(AppConstants.EXTRA_SELECTED_PRODUCT, product);
        startActivity(intent);
    }

    private void scrollToSelectedProduct() {
        int position = getIntent().getIntExtra(AppConstants.EXTRA_SELECTED_POSITION, -1);
        Product selectedProduct = getSelectedProduct();

        if (selectedProduct != null) {
            for (int i = 0; i < visibleProducts.size(); i++) {
                Product product = visibleProducts.get(i);
                if (product.getName().equals(selectedProduct.getName())
                        && product.getOrigin().equals(selectedProduct.getOrigin())) {
                    position = i;
                    break;
                }
            }
        }
        if (position >= 0 && position < visibleProducts.size()) {
            int selectedPosition = position;
            ListView listView = findViewById(R.id.listViewProducts);
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

