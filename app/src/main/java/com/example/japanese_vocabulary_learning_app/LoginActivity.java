package com.example.japanese_vocabulary_learning_app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    // Biến cấp class để cả onCreate() và onResume() cùng sử dụng.
    private EditText edtUsername;
    private EditText edtPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Nạp layout XML trước khi tìm các View qua ID.
        setContentView(R.layout.activity_login);

        // findViewById() lấy các View đã tạo từ activity_login.xml.
        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        Button btnLogin = findViewById(R.id.btnLogin);
        TextView tvGoRegister = findViewById(R.id.tvGoRegister);

        // Callback được Android gọi khi người dùng nhấn nút Đăng nhập.
        btnLogin.setOnClickListener(v -> {
            // Đọc username và password; chỉ bỏ khoảng trắng hai đầu username.
            String username = edtUsername.getText().toString().trim();
            String password = edtPassword.getText().toString();
            // Nếu thiếu thông tin, hiện Toast và dừng xử lý lần nhấn này.
            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đủ tài khoản và mật khẩu", Toast.LENGTH_SHORT).show();
                return;
            }
            // Xác thực demo; thành công thì Intent mở Home, finish() đóng Login.
            if (DemoAuth.signIn(this, username, password)) {
                startActivity(new Intent(this, HomeActivity.class));
                finish();
            } else {
                edtPassword.setText("");
                Toast.makeText(this, "Tài khoản hoặc mật khẩu không đúng", Toast.LENGTH_SHORT).show();
            }
        });

        // TextView cũng có thể đăng ký sự kiện nhấn để mở Register.
        tvGoRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
    }

    @Override
    // Khi Activity xuất hiện lại (ví dụ quay về từ Register), đọc username đã lưu.
    protected void onResume() {
        super.onResume();
        if (edtUsername != null) {
            // Không tự điền password, chỉ hỗ trợ điền tên tài khoản.
            String registered = DemoAuth.getRegisteredUsername(this);
            if (!registered.isEmpty()) {
                edtUsername.setText(registered);
            }
        }
    }
}
