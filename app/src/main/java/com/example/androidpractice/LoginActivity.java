package com.example.androidpractice;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    private EditText edtUsername;
    private EditText edtPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        bindInputFields();
        setupLoginButton();
        setupRegisterLink();
    }

    private void bindInputFields() {
        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
    }

    private void setupLoginButton() {
        Button btnLogin = findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(v -> login());
    }

    private void setupRegisterLink() {
        TextView tvGoRegister = findViewById(R.id.tvGoRegister);
        tvGoRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void login() {
        String username = edtUsername.getText().toString().trim();
        String password = edtPassword.getText().toString();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đủ tài khoản và mật khẩu",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (DemoAuth.signIn(this, username, password)) {
            saveSession(username);
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        } else {
            edtPassword.setText("");
            Toast.makeText(this, "Tài khoản hoặc mật khẩu không đúng",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void saveSession(String username) {
        getSharedPreferences(AppConstants.PREF_USER_SESSION, MODE_PRIVATE)
                .edit()
                .putString(AppConstants.KEY_USERNAME, username)
                .apply();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (edtUsername != null) {
            String registered = DemoAuth.getRegisteredUsername(this);
            if (!registered.isEmpty()) {
                edtUsername.setText(registered);
            }
        }
    }
}
