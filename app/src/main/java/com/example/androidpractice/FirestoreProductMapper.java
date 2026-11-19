package com.example.androidpractice;

import android.content.Context;

import com.google.firebase.firestore.DocumentSnapshot;

public final class FirestoreProductMapper {
    private FirestoreProductMapper() { }

    public static Product fromDocument(Context context, DocumentSnapshot document) {
        String name = document.getString(AppConstants.FIELD_NAME);
        String origin = document.getString(AppConstants.FIELD_ORIGIN);
        String description = document.getString(AppConstants.FIELD_DESCRIPTION);
        Long price = document.getLong(AppConstants.FIELD_PRICE);
        String imageName = document.getString(AppConstants.FIELD_IMAGE_NAME);

        // Lấy hình ảnh từ drawable dựa trên tên lưu ở Firebase.
        int imageResId = 0;
        if (imageName != null) {
            imageResId = context.getResources().getIdentifier(
                    imageName, "drawable", context.getPackageName());
        }
        if (imageResId == 0) {
            imageResId = R.drawable.fruit_apple;
        }

        return new Product(
                name != null ? name : "",
                origin != null ? origin : "",
                description != null ? description : "",
                price != null ? price.intValue() : 0,
                imageResId
        );
    }
}
