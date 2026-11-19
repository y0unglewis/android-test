package com.example.androidpractice;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Đánh giá DEMO do người học chủ động nhập. Không tạo rating/review giả;
 * chỉ hiển thị cho các tài khoản demo trên cùng thiết bị này.
 */
public class ReviewRepository {
    private static final String PREF_NAME = "fresh_market_product_reviews_demo";
    private static final String TAG = "ReviewRepository";
    private final SharedPreferences preferences;
    private final String currentUser;
    private boolean readError;

    public static class Review {
        public String author;
        public int stars;
        public String comment;
        public long createdAt;
        public long updatedAt;

        public boolean wasEdited() {
            return updatedAt > createdAt;
        }
    }

    public ReviewRepository(Context context) {
        preferences = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String username = context.getSharedPreferences(
                AppConstants.PREF_USER_SESSION, Context.MODE_PRIVATE)
                .getString(AppConstants.KEY_USERNAME, "");
        currentUser = username == null ? "" : username.trim();
    }

    public String getCurrentUser() {
        return currentUser;
    }

    public ArrayList<Review> getReviews(Product product) {
        ArrayList<Review> result = new ArrayList<>();
        if (product == null) return result;
        try {
            JSONArray array = new JSONArray(preferences.getString(key(product), "[]"));
            for (int i = 0; i < array.length(); i++) {
                JSONObject raw = array.getJSONObject(i);
                Review review = new Review();
                review.author = raw.getString("author");
                review.stars = raw.getInt("stars");
                review.comment = raw.getString("comment");
                review.createdAt = raw.getLong("createdAt");
                review.updatedAt = raw.optLong("updatedAt", review.createdAt);
                if (review.stars < 1 || review.stars > 5) {
                    throw new JSONException("Số sao không hợp lệ");
                }
                result.add(review);
            }
        } catch (JSONException e) {
            readError = true;
            Log.e(TAG, "Không đọc được đánh giá; không ghi đè dữ liệu cũ.", e);
            return new ArrayList<>();
        }
        return result;
    }

    public Review getOwnReview(Product product) {
        for (Review review : getReviews(product)) {
            if (isOwn(review)) return review;
        }
        return null;
    }

    public boolean isOwn(Review review) {
        return !currentUser.isEmpty() && review.author.equals(currentUser);
    }

    // Mỗi người chỉ có một đánh giá/sản phẩm; gửi lại sẽ cập nhật đánh giá cũ.
    public boolean saveReview(Product product, int stars, String comment) {
        if (product == null || currentUser.isEmpty() || stars < 1 || stars > 5
                || comment == null || comment.trim().length() < 10
                || comment.trim().length() > 500) return false;
        ArrayList<Review> reviews = getReviews(product);
        if (readError) return false;
        Review mine = null;
        for (Review review : reviews) {
            if (isOwn(review)) {
                mine = review;
                break;
            }
        }
        long now = System.currentTimeMillis();
        if (mine == null) {
            mine = new Review();
            mine.author = currentUser;
            mine.createdAt = now;
            reviews.add(0, mine);
        }
        mine.stars = stars;
        mine.comment = comment.trim();
        mine.updatedAt = now;
        return save(product, reviews);
    }

    public boolean deleteOwnReview(Product product) {
        ArrayList<Review> reviews = getReviews(product);
        if (readError || currentUser.isEmpty()) return false;
        boolean removed = reviews.removeIf(this::isOwn);
        return removed && save(product, reviews);
    }

    private boolean save(Product product, ArrayList<Review> reviews) {
        if (readError) return false;
        try {
            JSONArray array = new JSONArray();
            for (Review review : reviews) {
                JSONObject raw = new JSONObject();
                raw.put("author", review.author);
                raw.put("stars", review.stars);
                raw.put("comment", review.comment);
                raw.put("createdAt", review.createdAt);
                raw.put("updatedAt", review.updatedAt);
                array.put(raw);
            }
            return preferences.edit().putString(key(product), array.toString()).commit();
        } catch (JSONException e) {
            Log.e(TAG, "Không lưu được đánh giá.", e);
            return false;
        }
    }

    private String key(Product product) {
        String name = product.getName().trim().toLowerCase(Locale.ROOT);
        String origin = product.getOrigin().trim().toLowerCase(Locale.ROOT);
        return "review:" + name.length() + ":" + name + ":" + origin;
    }
}
