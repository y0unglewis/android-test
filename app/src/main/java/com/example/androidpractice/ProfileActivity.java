package com.example.androidpractice;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        setupBackButton();
        showProfileUsername();
    }

    private void setupBackButton() {
        ImageButton btnBackProfile = findViewById(R.id.btnBackProfile);
        btnBackProfile.setOnClickListener(v -> finish());
    }

    private void showProfileUsername() {
        // Mở CÙNG vùng user_session mà LoginActivity đã ghi username vào.
        SharedPreferences prefs = getSharedPreferences(
                AppConstants.PREF_USER_SESSION, MODE_PRIVATE);
        String username = prefs.getString(AppConstants.KEY_USERNAME, "bạn");
        if (username == null || username.isEmpty()) {
            username = "bạn";
        }

        // Cập nhật tên tại tiêu đề hồ sơ và trong thẻ thông tin.
        TextView tvProfileUsername = findViewById(R.id.tvProfileUsername);
        TextView tvProfileUsernameDetail = findViewById(R.id.tvProfileUsernameDetail);
        tvProfileUsername.setText(username);
        tvProfileUsernameDetail.setText(username);
    }
}
