package com.example.androidpractice;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

/**
 * Chi tiết sản phẩm theo phong cách sàn TMĐT:
 * mô tả / thông tin / đánh giá do người dùng nhập và sản phẩm liên quan.
 * Firestore chỉ được ĐỌC, đánh giá chỉ là dữ liệu demo cục bộ.
 */
public class ProductDetailActivity extends AppCompatActivity {
    private Product selectedProduct;
    private ReviewRepository reviews;
    private TabLayout detailTabs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        selectedProduct = readProductFromIntent();
        if (selectedProduct == null) {
            Toast.makeText(this, "Không tìm thấy thông tin sản phẩm.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        reviews = new ReviewRepository(this);
        setupNavigation();
        setupTabs();
        showProductInformation();
        setupReviewForm();
        renderReviews();
        loadRelatedProducts();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (selectedProduct == null) return;
        int total = 0;
        for (OrderRepository.CartLine line : new OrderRepository(this).getCart()) {
            total += line.quantity;
        }
        TextView badge = findViewById(R.id.tvProductCartBadge);
        badge.setVisibility(total == 0 ? View.GONE : View.VISIBLE);
        badge.setText(total > 99 ? "99+" : String.valueOf(total));
    }

    @SuppressWarnings("deprecation")
    private Product readProductFromIntent() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return getIntent().getSerializableExtra(
                    AppConstants.EXTRA_SELECTED_PRODUCT, Product.class);
        }
        return (Product) getIntent().getSerializableExtra(
                AppConstants.EXTRA_SELECTED_PRODUCT);
    }

    private void setupNavigation() {
        findViewById(R.id.btnBackProductDetail).setOnClickListener(v -> finish());
        findViewById(R.id.btnViewCartFromDetail).setOnClickListener(v ->
                startActivity(new Intent(this, CartActivity.class)));

        findViewById(R.id.btnAddToCart).setOnClickListener(v -> {
            OrderRepository repository = new OrderRepository(this);
            if (repository.addToCart(selectedProduct)) {
                Toast.makeText(this, "Đã thêm vào giỏ hàng.", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, CartActivity.class));
            } else {
                Toast.makeText(this, "Không thêm được hoặc đã đạt 99 phần.",
                        Toast.LENGTH_LONG).show();
            }
        });

        // Mua ngay một sản phẩm riêng biệt: không gộp và không xóa giỏ hiện tại.
        findViewById(R.id.btnBuyNow).setOnClickListener(v -> {
            Intent intent = new Intent(this, CheckoutActivity.class);
            intent.putExtra(AppConstants.EXTRA_BUY_NOW_PRODUCT, selectedProduct);
            startActivity(intent);
        });
    }

    private void setupTabs() {
        detailTabs = findViewById(R.id.productDetailTabs);
        detailTabs.addTab(detailTabs.newTab().setText("Mô tả"));
        detailTabs.addTab(detailTabs.newTab().setText("Thông tin"));
        detailTabs.addTab(detailTabs.newTab().setText("Đánh giá"));
        detailTabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                showTab(tab.getPosition());
            }

            @Override public void onTabUnselected(TabLayout.Tab tab) { }
            @Override public void onTabReselected(TabLayout.Tab tab) { }
        });
        showTab(0);

        findViewById(R.id.tvProductRatingTeaser).setOnClickListener(v -> {
            TabLayout.Tab reviewTab = detailTabs.getTabAt(2);
            if (reviewTab != null) reviewTab.select();
            ScrollView scroll = findViewById(R.id.productDetailScroll);
            View tabCard = (View) detailTabs.getParent().getParent();
            scroll.post(() -> scroll.smoothScrollTo(0, tabCard.getTop()));
        });
    }

    private void showTab(int selectedIndex) {
        findViewById(R.id.panelProductDescription)
                .setVisibility(selectedIndex == 0 ? View.VISIBLE : View.GONE);
        findViewById(R.id.panelProductSpecifications)
                .setVisibility(selectedIndex == 1 ? View.VISIBLE : View.GONE);
        findViewById(R.id.panelProductReviews)
                .setVisibility(selectedIndex == 2 ? View.VISIBLE : View.GONE);
    }

    private void showProductInformation() {
        ImageView image = findViewById(R.id.imgDetailProduct);
        image.setImageResource(selectedProduct.getImageResId());
        image.setContentDescription(selectedProduct.getName());

        String productName = selectedProduct.getName();
        String origin = selectedProduct.getOrigin().isEmpty()
                ? "Chưa cập nhật" : selectedProduct.getOrigin();
        String price = String.format(Locale.forLanguageTag("vi-VN"),
                "%,d đ/kg", selectedProduct.getPrice());
        String description = selectedProduct.getDescription().isEmpty()
                ? "Danh mục chưa có mô tả chi tiết cho sản phẩm này."
                : selectedProduct.getDescription();

        setText(R.id.tvDetailProductName, productName);
        setText(R.id.tvDetailProductPrice, price);
        setText(R.id.tvDetailProductOrigin, origin);
        setText(R.id.tvDetailProductDescription, description);
        setText(R.id.tvSpecName, productName);
        setText(R.id.tvSpecOrigin, origin);
        setText(R.id.tvSpecPrice, price);
    }

    private void setupReviewForm() {
        findViewById(R.id.btnSubmitProductReview)
                .setOnClickListener(v -> submitReview());
        fillOwnReviewForm();
    }

    private void fillOwnReviewForm() {
        ReviewRepository.Review mine = reviews.getOwnReview(selectedProduct);
        RatingBar stars = findViewById(R.id.ratingInputProduct);
        TextInputEditText input = findViewById(R.id.etProductReview);
        MaterialButton submit = findViewById(R.id.btnSubmitProductReview);
        stars.setRating(mine == null ? 0 : mine.stars);
        input.setText(mine == null ? "" : mine.comment);
        submit.setText(mine == null ? "Gửi đánh giá" : "Cập nhật đánh giá");
        setText(R.id.tvOwnReviewHeader, mine == null
                ? "Viết đánh giá của bạn" : "Đánh giá của bạn · Có thể chỉnh sửa");
    }

    private void submitReview() {
        RatingBar stars = findViewById(R.id.ratingInputProduct);
        TextInputEditText input = findViewById(R.id.etProductReview);
        String content = input.getText() == null ? "" : input.getText().toString().trim();
        int rating = (int) stars.getRating();

        if (reviews.getCurrentUser().isEmpty()) {
            Toast.makeText(this, "Hãy đăng nhập tài khoản demo trước.",
                    Toast.LENGTH_LONG).show();
            return;
        }
        if (rating < 1 || rating > 5) {
            Toast.makeText(this, "Chọn từ 1 đến 5 sao nhé.", Toast.LENGTH_SHORT).show();
            stars.requestFocus();
            return;
        }
        if (content.length() < 10 || content.length() > 500) {
            input.setError("Nội dung cần từ 10 đến 500 ký tự.");
            input.requestFocus();
            return;
        }
        if (!reviews.saveReview(selectedProduct, rating, content)) {
            Toast.makeText(this, "Không thể lưu đánh giá. Hãy thử lại.",
                    Toast.LENGTH_LONG).show();
            return;
        }
        Toast.makeText(this, "Đã lưu đánh giá demo trên thiết bị.",
                Toast.LENGTH_SHORT).show();
        renderReviews();
        fillOwnReviewForm();
    }

    private void renderReviews() {
        ArrayList<ReviewRepository.Review> items = reviews.getReviews(selectedProduct);
        LinearLayout container = findViewById(R.id.productReviewsContainer);
        container.removeAllViews();
        findViewById(R.id.tvProductReviewEmpty).setVisibility(
                items.isEmpty() ? View.VISIBLE : View.GONE);

        int totalStars = 0;
        for (ReviewRepository.Review review : items) {
            totalStars += review.stars;
            View row = LayoutInflater.from(this).inflate(
                    R.layout.item_product_review, container, false);
            bindReview(row, review);
            container.addView(row);
        }

        if (items.isEmpty()) {
            setText(R.id.tvProductRatingTeaser, "☆  Chưa có đánh giá trên thiết bị · Xem đánh giá");
            setText(R.id.tvProductReviewSummary, "Chưa có đánh giá");
        } else {
            String score = String.format(Locale.forLanguageTag("vi-VN"),
                    "%.1f", totalStars / (double) items.size());
            String count = items.size() + " đánh giá trên thiết bị";
            setText(R.id.tvProductRatingTeaser, "★  " + score + "/5 · " + count
                    + " · Xem đánh giá");
            setText(R.id.tvProductReviewSummary, "★  " + score + "/5  ·  " + count);
        }
    }

    private void bindReview(View row, ReviewRepository.Review review) {
        setRowText(row, R.id.tvReviewAuthor, review.author);
        setRowText(row, R.id.tvReviewStars, stars(review.stars));
        setRowText(row, R.id.tvReviewComment, review.comment);
        SimpleDateFormat formatter = new SimpleDateFormat(
                "dd/MM/yyyy HH:mm", Locale.forLanguageTag("vi-VN"));
        String date = formatter.format(new Date(review.createdAt));
        setRowText(row, R.id.tvReviewDate, date
                + (review.wasEdited() ? " · Đã chỉnh sửa" : ""));

        View delete = row.findViewById(R.id.btnDeleteOwnReview);
        delete.setVisibility(reviews.isOwn(review) ? View.VISIBLE : View.GONE);
        delete.setOnClickListener(v -> new MaterialAlertDialogBuilder(this)
                .setTitle("Xóa đánh giá của bạn?")
                .setMessage("Đánh giá sẽ bị xóa khỏi dữ liệu demo trên thiết bị.")
                .setNegativeButton("Giữ lại", null)
                .setPositiveButton("Xóa đánh giá", (dialog, which) -> {
                    if (reviews.deleteOwnReview(selectedProduct)) {
                        renderReviews();
                        fillOwnReviewForm();
                    } else {
                        Toast.makeText(this, "Không xóa được đánh giá.",
                                Toast.LENGTH_LONG).show();
                    }
                })
                .show());
    }

    private String stars(int count) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            result.append(i < count ? '★' : '☆');
        }
        return result.toString();
    }

    private void loadRelatedProducts() {
        setText(R.id.tvRelatedProductsStatus, "Đang tìm sản phẩm trong danh mục...");
        findViewById(R.id.btnRetryRelatedProducts).setVisibility(View.GONE);
        FirebaseFirestore.getInstance()
                .collection(AppConstants.COLLECTION_FRUITS)
                .orderBy(AppConstants.FIELD_SORT_ORDER)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (!isFinishing() && !isDestroyed()) renderRelatedProducts(snapshot);
                })
                .addOnFailureListener(e -> {
                    if (isFinishing() || isDestroyed()) return;
                    setText(R.id.tvRelatedProductsStatus,
                            "Chưa tải được gợi ý. Kiểm tra mạng rồi thử lại.");
                    View retry = findViewById(R.id.btnRetryRelatedProducts);
                    retry.setVisibility(View.VISIBLE);
                    retry.setOnClickListener(v -> loadRelatedProducts());
                });
    }

    private void renderRelatedProducts(QuerySnapshot snapshot) {
        ArrayList<Product> products = new ArrayList<>();
        for (QueryDocumentSnapshot document : snapshot) {
            Product product = FirestoreProductMapper.fromDocument(this, document);
            if (product.getName().equalsIgnoreCase(selectedProduct.getName())
                    && product.getOrigin().equalsIgnoreCase(selectedProduct.getOrigin())) {
                continue;
            }
            products.add(product);
        }

        // Ưu tiên cùng xuất xứ, sau đó mức giá gần nhất, không tạo dữ liệu mới.
        Collections.sort(products, (a, b) -> {
            int groupA = sameOrigin(a) ? 0 : 1;
            int groupB = sameOrigin(b) ? 0 : 1;
            if (groupA != groupB) return Integer.compare(groupA, groupB);
            long gapA = Math.abs((long) a.getPrice() - selectedProduct.getPrice());
            long gapB = Math.abs((long) b.getPrice() - selectedProduct.getPrice());
            return Long.compare(gapA, gapB);
        });

        LinearLayout container = findViewById(R.id.relatedProductsContainer);
        container.removeAllViews();
        int count = Math.min(6, products.size());
        for (int i = 0; i < count; i++) {
            Product product = products.get(i);
            View card = LayoutInflater.from(this).inflate(
                    R.layout.item_related_product, container, false);
            bindRelatedProduct(card, product);
            container.addView(card);
        }
        setText(R.id.tvRelatedProductsStatus, count == 0
                ? "Hiện chưa có sản phẩm khác để gợi ý."
                : "Khám phá thêm " + count + " sản phẩm từ danh mục");
    }

    private boolean sameOrigin(Product product) {
        return !selectedProduct.getOrigin().isEmpty()
                && selectedProduct.getOrigin().equalsIgnoreCase(product.getOrigin());
    }

    private void bindRelatedProduct(View card, Product product) {
        ImageView image = card.findViewById(R.id.imgRelatedProduct);
        image.setImageResource(product.getImageResId());
        image.setContentDescription(product.getName());
        setRowText(card, R.id.tvRelatedName, product.getName());
        setRowText(card, R.id.tvRelatedOrigin,
                product.getOrigin().isEmpty() ? "Chưa rõ xuất xứ" : product.getOrigin());
        setRowText(card, R.id.tvRelatedPrice, String.format(
                Locale.forLanguageTag("vi-VN"), "%,d đ/kg", product.getPrice()));
        card.setContentDescription("Xem chi tiết " + product.getName());
        card.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProductDetailActivity.class);
            intent.putExtra(AppConstants.EXTRA_SELECTED_PRODUCT, product);
            startActivity(intent);
        });
    }

    private void setText(int id, String value) {
        ((TextView) findViewById(id)).setText(value);
    }

    private void setRowText(View parent, int id, String value) {
        ((TextView) parent.findViewById(id)).setText(value);
    }
}
