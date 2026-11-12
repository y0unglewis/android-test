package com.example.japanese_vocabulary_learning_app;

import java.util.ArrayList;

// Dữ liệu khởi tạo 15 trái cây khi SQLite tạo bảng lần đầu.
// Sau khi khởi tạo, Home và Detail luôn đọc từ database, không đọc trực tiếp từ đây.
public class DataSource {
    // Chỉ gọi một lần trong FruitDatabaseHelper.onCreate() để seed dữ liệu mẫu.
    public static ArrayList<Product> getProducts() {
        ArrayList<Product> products = new ArrayList<>();
        // Mỗi new Product(...) tạo một sản phẩm rồi add() vào danh sách.
        products.add(new Product(
                "Apple",
                "New Zealand",
                "Táo giòn, vị ngọt thanh.",
                50000,
                // R.drawable.* là ID tài nguyên ảnh, không phải chuỗi đường dẫn.
                R.drawable.fruit_apple
        ));

        products.add(new Product(
                "Banana",
                "Việt Nam",
                "Chuối chín tự nhiên, giàu dinh dưỡng.",
                25000,
                R.drawable.fruit_banana
        ));

        products.add(new Product(
                "Cherry",
                "Mỹ",
                "Cherry tươi, quả mọng, vị ngọt.",
                180000,
                R.drawable.fruit_cherry
        ));

        products.add(new Product(
                "Chà là (Date)",
                "Tunisia",
                "Chà là có vị ngọt đậm.",
                120000,
                R.drawable.fruit_date
        ));

        products.add(new Product(
                "Nho (Grapes)",
                "Việt Nam",
                "Nho tươi, vị ngọt dịu.",
                75000,
                R.drawable.fruit_grapes
        ));

        products.add(new Product(
                "Watermelon",
                "Long An",
                "Dưa hấu ruột đỏ ngọt lịm, giải nhiệt mùa hè.",
                15000,
                R.drawable.fruit_watermelon
        ));

        products.add(new Product(
                "Pineapple",
                "Tiền Giang",
                "Dứa mật thơm, mọng nước và giàu vitamin C.",
                20000,
                R.drawable.fruit_pineapple
        ));

        products.add(new Product(
                "Mango",
                "Đồng Tháp",
                "Xoài cát Hòa Lộc chín vàng, vị ngọt thanh.",
                45000,
                R.drawable.fruit_mango
        ));

        products.add(new Product(
                "Lemon",
                "Đà Lạt",
                "Chanh vàng mọng nước, vỏ thơm, vị chua thanh.",
                35000,
                R.drawable.fruit_lemon
        ));

        products.add(new Product(
                "Papaya",
                "Bình Phước",
                "Đu đủ chín cây, ruột đỏ, ngọt đậm đà.",
                18000,
                R.drawable.fruit_papaya
        ));

        products.add(new Product(
                "Avocado",
                "Đắk Lắk",
                "Bơ sáp dẻo, thịt dày, béo ngậy.",
                40000,
                R.drawable.fruit_avocado
        ));

        products.add(new Product(
                "Coconut",
                "Bến Tre",
                "Dừa xiêm xanh, nước ngọt thanh mát.",
                12000,
                R.drawable.fruit_coconut
        ));

        products.add(new Product(
                "Peach",
                "Sa Pa",
                "Đào tươi giòn, ruột vàng, vị chua ngọt nhẹ.",
                50000,
                R.drawable.fruit_peach
        ));

        products.add(new Product(
                "Pear",
                "Hàn Quốc",
                "Lê quả to, cùi trắng giòn, nhiều nước.",
                65000,
                R.drawable.fruit_pear
        ));

        products.add(new Product(
                "Lime",
                "Long An",
                "Chanh ta vỏ xanh mỏng, vị chua gắt.",
                10000,
                R.drawable.fruit_lime
        ));
        // Dùng làm dữ liệu seed cho SQLite khi cài app lần đầu.
        return products;
    }
}
