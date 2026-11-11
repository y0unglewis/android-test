package com.example.japanese_vocabulary_learning_app;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Local classroom demo only. One registered user is stored on this device;
 * there is no server, email verification, or production authentication.
 */
public final class DemoAuth {
    private static final String PREFERENCES = "fruit_demo_account";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_SALT = "salt";
    private static final String KEY_HASH = "password_hash";

    private DemoAuth() { }

    public static boolean signIn(Context context, String username, String password) {
        if ("demo".equals(username) && "123456".equals(password)) {
            return true;
        }
        SharedPreferences prefs = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        String savedUsername = prefs.getString(KEY_USERNAME, "");
        String savedSalt = prefs.getString(KEY_SALT, "");
        String savedHash = prefs.getString(KEY_HASH, "");
        if (!username.equals(savedUsername) || savedSalt.isEmpty() || savedHash.isEmpty()) {
            return false;
        }
        try {
            String actualHash = hash(password, Base64.decode(savedSalt, Base64.NO_WRAP));
            return MessageDigest.isEqual(
                    savedHash.getBytes(StandardCharsets.US_ASCII),
                    actualHash.getBytes(StandardCharsets.US_ASCII));
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    public static boolean register(Context context, String username, String password) {
        if ("demo".equalsIgnoreCase(username)) {
            return false; // Reserved classroom demo username.
        }
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        String encodedSalt = Base64.encodeToString(salt, Base64.NO_WRAP);
        String passwordHash = hash(password, salt);
        return context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_USERNAME, username)
                .putString(KEY_SALT, encodedSalt)
                .putString(KEY_HASH, passwordHash)
                .commit();
    }

    public static String getRegisteredUsername(Context context) {
        return context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
                .getString(KEY_USERNAME, "");
    }

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
