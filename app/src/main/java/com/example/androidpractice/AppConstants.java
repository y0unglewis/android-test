package com.example.androidpractice;

// Tập trung các tên key dùng để truyền dữ liệu và lưu phiên đăng nhập.
public final class AppConstants {
    private AppConstants() { }

    // Intent: dữ liệu gửi từ Home sang Detail.
    public static final String EXTRA_SELECTED_PRODUCT = "selected_product";
    public static final String EXTRA_SELECTED_POSITION = "selected_position";

    // SharedPreferences: phiên đăng nhập.
    public static final String PREF_USER_SESSION = "user_session";
    public static final String KEY_USERNAME = "username";

    // Firestore: collection và các field sản phẩm.
    public static final String COLLECTION_FRUITS = "fruits";
    public static final String FIELD_SORT_ORDER = "sort_order";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_ORIGIN = "origin";
    public static final String FIELD_DESCRIPTION = "description";
    public static final String FIELD_PRICE = "price";
    public static final String FIELD_IMAGE_NAME = "image_name";
}
