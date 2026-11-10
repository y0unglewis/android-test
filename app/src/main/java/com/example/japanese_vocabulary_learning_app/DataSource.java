package com.example.japanese_vocabulary_learning_app;

import java.util.Arrays;
import java.util.ArrayList;

public class DataSource {

    public static String[] getItems() {
        return new String[] {
                "Apple",
                "Banana",
                "Cherry",
                "Date",
                "Grapes",
                "Watermelon",
                "Pineapple",
                "Mango",
                "Lemon",
                "Papaya",
                "Avocado",
                "Coconut",
                "Peach",
                "Pear",
                "Lime"
        };
    }

    public static ArrayList<Product> getProducts() {
        ArrayList<Product> products = new ArrayList<>();

        products.add(new Product(
           "Apple", "New Zealand", "Táo giòn, vị ngọt thanh.", 50000, "🍎"
        ));

        products.add(new Product(
                "Banana", "Việt Nam", "Chuối chín tự nhiên, giàu dinh dưỡng.", 25000, "🍌"
        ));

        products.add(new Product(
                "Cherry", "Mỹ", "Cherry tươi, quả mọng, vị ngọt.", 180000, "🍒"
        ));

        products.add(new Product(
                "Chà là (Date)", "Tunisia", "Chà là có vị ngọt đậm.", 120000, "🌴"
        ));

        products.add(new Product(
                "Nho (Grapes)", "Việt Nam", "Nho tươi, vị ngọt dịu.", 75000, "🍇"
        ));

        products.add(new Product(
                "Watermelon", "Long An", "Dưa hấu ruột đỏ ngọt lịm, giải nhiệt mùa hè cực tốt.", 15000, "🍉"
        ));

        products.add(new Product(
                "Pineapple", "Tiền Giang", "Dứa mật thơm lừng, mọng nước và giàu vitamin C.", 20000, "🍍"
        ));

        products.add(new Product(
                "Mango", "Đồng Tháp", "Xoài cát Hòa Lộc chín vàng, vị ngọt thanh và rất thơm.", 45000, "🥭"
        ));

        products.add(new Product(
                "Lemon", "Đà Lạt", "Chanh tây (chanh vàng) mọng nước, vỏ thơm, vị chua thanh.", 35000, "🍋"
        ));

        products.add(new Product(
                "Papaya", "Bình Phước", "Đu đủ chín cây tự nhiên, ruột đỏ, ngọt đậm đà.", 18000, "🍈"
        ));

        products.add(new Product(
                "Avocado", "Đắk Lắk", "Bơ sáp dẻo, thịt dày, béo ngậy và hạt nhỏ.", 40000, "🥑"
        ));

        products.add(new Product(
                "Coconut", "Bến Tre", "Dừa xiêm xanh chuẩn gốc, nước ngọt thanh mát.", 12000, "🥥"
        ));

        products.add(new Product(
                "Peach", "Sapa", "Đào tươi giòn sần sật, ruột vàng có vị chua ngọt nhẹ.", 50000, "🍑"
        ));

        products.add(new Product(
                "Pear", "Hàn Quốc", "Lê nhập khẩu quả to, cùi trắng giòn và cực kỳ nhiều nước.", 65000, "🍐"
        ));

        products.add(new Product(
                "Lime", "Long An", "Chanh ta vỏ xanh mỏng, vị chua gắt đặc trưng, rất nhiều nước.", 10000, "🍋"
        ));

        return products;
    }
}
