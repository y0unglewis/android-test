package com.example.japanese_vocabulary_learning_app;

public class Product {
    private String name;
    private String origin;
    private String description;
    private int price;

    public  Product(String name, String origin, String description, int price) {
        this.name = name;
        this.origin = origin;
        this.description = description;
        this.price = price;
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

}
