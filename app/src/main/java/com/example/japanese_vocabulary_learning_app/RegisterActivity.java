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
        setContentView(R.layout.activity_register);

        ImageButton btnBackLogin = findViewById(R.id.btnBackLogin);
        TextView tvAlreadyHaveAccount = findViewById(R.id.tvAlreadyHaveAccount);
        EditText edtUsername = findViewById(R.id.edtRegisterUsername);
        EditText edtPassword = findViewById(R.id.edtRegisterPassword);
        EditText edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        Button btnRegister = findViewById(R.id.btnRegister);

        btnBackLogin.setOnClickListener(v -> finish());
        tvAlreadyHaveAccount.setOnClickListener(v -> finish());

        btnRegister.setOnClickListener(v -> {
            String username = edtUsername.getText().toString().trim();
            String password = edtPassword.getText().toString();
            String confirm = edtConfirmPassword.getText().toString();

            if (!username.matches("[A-Za-z0-9_]{3,20}")) {
                edtUsername.setError("Dùng 3–20 ký tự: chữ, số hoặc _");
                edtUsername.requestFocus();
                return;
            }
            if (password.length() < 6) {
                edtPassword.setError("Mật khẩu cần ít nhất 6 ký tự");
                edtPassword.requestFocus();
                return;
            }
            if (!password.equals(confirm)) {
                edtConfirmPassword.setError("Xác nhận mật khẩu chưa khớp");
                edtConfirmPassword.requestFocus();
                return;
            }
            if ("demo".equalsIgnoreCase(username)) {
                edtUsername.setError("Tên demo đã được sử dụng");
                return;
            }
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
