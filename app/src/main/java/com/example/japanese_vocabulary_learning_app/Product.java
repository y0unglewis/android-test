package com.example.japanese_vocabulary_learning_app;

public class Product {
    private final String name;
    private final String origin;
    private final String description;
    private final int price;
    private final int imageResId;

    public Product(String name, String origin, String description,
                   int price, int imageResId) {
        this.name = name;
        this.origin = origin;
        this.description = description;
        this.price = price;
        this.imageResId = imageResId;
    }

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
