package com.sandhyasofttech.gstbillingpro.Model;

public class Product {
    private String productId;
    private String name;
    private String hsnCode;
    private double price;
    private double gstRate;
    private int stockQuantity;

    // Default constructor (required for Firebase)
    public Product() {
    }

    // Parameterized constructor (optional)
    public Product(String productId, String name, String hsnCode, double price, double gstRate, int stockQuantity) {
        this.productId = productId;
        this.name = name;
        this.hsnCode = hsnCode;
        this.price = price;
        this.gstRate = gstRate;
        this.stockQuantity = stockQuantity;
    }

    // Getters
    public String getProductId() {
        return productId;
    }
    public String getName() {
        return name;
    }
    public String getHsnCode() {
        return hsnCode;
    }
    public double getPrice() {
        return price;
    }
    public double getGstRate() {
        return gstRate;
    }
    public int getStockQuantity() {
        return stockQuantity;
    }

    // Setters
    public void setProductId(String productId) {
        this.productId = productId;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setHsnCode(String hsnCode) {
        this.hsnCode = hsnCode;
    }
    public void setPrice(double price) {
        this.price = price;
    }
    public void setGstRate(double gstRate) {
        this.gstRate = gstRate;
    }
    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }
}
