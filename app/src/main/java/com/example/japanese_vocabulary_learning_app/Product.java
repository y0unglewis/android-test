package com.example.japanese_vocabulary_learning_app;

// Model lưu dữ liệu của MỘT loại trái cây; không tạo giao diện.
public class Product {
    // Trường private final chỉ gán khi tạo Product; class khác đọc qua getter.
    private final String name;
    private final String origin;
    private final String description;
    private final int price;
    private final int imageResId;

    // Constructor: tên, xuất xứ, mô tả, giá, ID drawable.
    public Product(String name, String origin, String description,
                   int price, int imageResId) {
        this.name = name;
        this.origin = origin;
        this.description = description;
        this.price = price;
        this.imageResId = imageResId;
    }

    // Getter để Adapter lấy thông tin mà không truy cập trực tiếp thuộc tính private.
    public String getName() {
        return name;
    }

    public String getOrigin() {
        return origin;
    }

    public String getDescription() {
        return description;
    }

    public int getPrice() {
        return price;
    }

    public int getImageResId() {
        return imageResId;
    }
}
