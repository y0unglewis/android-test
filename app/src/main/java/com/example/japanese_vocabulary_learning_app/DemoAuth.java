package com.example.japanese_vocabulary_learning_app;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Dùng để học Android: chỉ lưu một tài khoản đăng ký trên thiết bị, không có máy chủ.
 * Mật khẩu demo viết cố định và SHA-256 + salt ở đây không đạt chuẩn xác thực production.
 */
public final class DemoAuth {
    // Các khóa dùng để lưu tài khoản demo cục bộ qua SharedPreferences.
    private static final String PREFERENCES = "fruit_demo_account";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_SALT = "salt";
    private static final String KEY_HASH = "password_hash";

    // Chặn tạo đối tượng mới: các phương thức bên dưới đều là static.
    private DemoAuth() { }

    // Kiểm tra tài khoản nhập: hỗ trợ demo/123456 và một tài khoản người dùng đăng ký.
    public static boolean signIn(Context context, String username, String password) {
        // Tài khoản mẫu cố định để thử app, không phải phương án đăng nhập thực tế.
        if ("demo".equals(username) && "123456".equals(password)) {
            return true;
        }
        // Lấy username, salt và mã băm được lưu từ lần đăng ký trước.
        SharedPreferences prefs = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        String savedUsername = prefs.getString(KEY_USERNAME, "");
        String savedSalt = prefs.getString(KEY_SALT, "");
        String savedHash = prefs.getString(KEY_HASH, "");
        if (!username.equals(savedUsername) || savedSalt.isEmpty() || savedHash.isEmpty()) {
            return false;
        }
        try {
            // Băm mật khẩu vừa nhập cùng salt để so sánh với dữ liệu đã lưu.
            String actualHash = hash(password, Base64.decode(savedSalt, Base64.NO_WRAP));
            return MessageDigest.isEqual(
                    savedHash.getBytes(StandardCharsets.US_ASCII),
                    actualHash.getBytes(StandardCharsets.US_ASCII));
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    // Đăng ký một tài khoản trên máy; đăng ký lần nữa sẽ ghi đè tài khoản trước.
    public static boolean register(Context context, String username, String password) {
        if ("demo".equalsIgnoreCase(username)) {
            return false; // Giữ riêng tên này cho tài khoản kiểm thử.
        }
        // Salt ngẫu nhiên giúp hai mật khẩu giống nhau không luôn cho mã băm giống nhau.
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        String encodedSalt = Base64.encodeToString(salt, Base64.NO_WRAP);
        String passwordHash = hash(password, salt);
        // putString() ghi dữ liệu theo khóa; commit() lưu và trả về true/false.
        return context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_USERNAME, username)
                .putString(KEY_SALT, encodedSalt)
                .putString(KEY_HASH, passwordHash)
                .commit();
    }

    // Lấy username để điền lại ở Login khi trở về từ Register.
    public static String getRegisteredUsername(Context context) {
        return context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
                .getString(KEY_USERNAME, "");
    }

    // SHA-256 chỉ để minh họa thao tác băm; không dùng thay hệ thống mật khẩu production.
    private static String hash(String password, byte[] salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(salt);
            byte[] result = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return Base64.encodeToString(result, Base64.NO_WRAP);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }
}
