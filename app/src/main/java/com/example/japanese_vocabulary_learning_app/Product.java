package com.example.japanese_vocabulary_learning_app;

public class Product {
    private String icon;
    private String name;
    private String origin;
    private String description;
    private int price;

    public  Product(String name, String origin, String description, int price, String icon) {
        this.name = name;
        this.origin = origin;
        this.description = description;
        this.price = price;
        this.icon = icon;
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

    public String getIcon() {
        return icon;
    }

}
