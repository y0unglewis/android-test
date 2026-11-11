package com.example.japanese_vocabulary_learning_app;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Khởi tạo màn Register theo bố cục XML.
        setContentView(R.layout.activity_register);

        // Kết nối nút mũi tên và các ô nhập với Java.
        ImageButton btnBackLogin = findViewById(R.id.btnBackLogin);
        TextView tvAlreadyHaveAccount = findViewById(R.id.tvAlreadyHaveAccount);
        EditText edtUsername = findViewById(R.id.edtRegisterUsername);
        EditText edtPassword = findViewById(R.id.edtRegisterPassword);
        EditText edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        Button btnRegister = findViewById(R.id.btnRegister);

        // finish() đóng Register và quay lại Login cũ trong back stack.
        btnBackLogin.setOnClickListener(v -> finish());
        tvAlreadyHaveAccount.setOnClickListener(v -> finish());

        // Đọc form, kiểm tra từng lỗi rồi mới lưu tài khoản.
        btnRegister.setOnClickListener(v -> {
            String username = edtUsername.getText().toString().trim();
            String password = edtPassword.getText().toString();
            String confirm = edtConfirmPassword.getText().toString();

            // Regex yêu cầu 3–20 ký tự gồm chữ Latin, số hoặc dấu gạch dưới.
            if (!username.matches("[A-Za-z0-9_]{3,20}")) {
                edtUsername.setError("Dùng 3–20 ký tự: chữ, số hoặc _");
                edtUsername.requestFocus();
                return;
            }
            // Quy định mật khẩu demo có ít nhất 6 ký tự.
            if (password.length() < 6) {
                edtPassword.setError("Mật khẩu cần ít nhất 6 ký tự");
                edtPassword.requestFocus();
                return;
            }
            // equals() kiểm tra nội dung mật khẩu và mật khẩu xác nhận có khớp không.
            if (!password.equals(confirm)) {
                edtConfirmPassword.setError("Xác nhận mật khẩu chưa khớp");
                edtConfirmPassword.requestFocus();
                return;
            }
            if ("demo".equalsIgnoreCase(username)) {
                edtUsername.setError("Tên demo đã được sử dụng");
                return;
            }
            // Lưu thành công thì hiện Toast và về Login để đăng nhập.
            if (DemoAuth.register(this, username, password)) {
                Toast.makeText(
                        this,
                        "Tạo tài khoản demo thành công! Hãy đăng nhập.",
                        Toast.LENGTH_LONG
                ).show();
                finish();
            } else {
                Toast.makeText(
                        this,
                        "Không lưu được tài khoản. Vui lòng thử lại.",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }
}
