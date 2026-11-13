package com.example.japanese_vocabulary_learning_app;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Trang hồ sơ demo: chỉ đọc tên tài khoản đăng nhập từ SharedPreferences.
 * Không hiển thị hoặc lưu mật khẩu của người dùng trong giao diện này.
 */
public class ProfileActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Đóng Profile và quay lại Home đang nằm dưới trong back stack.
        ImageButton btnBackProfile = findViewById(R.id.btnBackProfile);
        btnBackProfile.setOnClickListener(v -> finish());

        // Mở CÙNG vùng user_session mà LoginActivity đã ghi username vào.
        SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
        String username = prefs.getString("username", "bạn");
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
