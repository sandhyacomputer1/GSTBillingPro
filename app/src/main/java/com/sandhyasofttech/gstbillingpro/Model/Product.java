package com.sandhyasofttech.gstbillingpro.Model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Product implements Serializable {
    private String productId;
    private String name;
    private String hsnCode;
    private double price;
    private double gstRate;
    private int stockQuantity;
    private String unit;
    private String customerName;
    private Map<String, String> customFields;

    // Default constructor (required for Firebase)
    public Product() { }

    // Parameterized constructor
    public Product(String productId, String name, String hsnCode, double price, double gstRate, int stockQuantity, String unit) {
        this.productId = productId;
        this.name = name;
        this.hsnCode = hsnCode;
        this.price = price;
        this.gstRate = gstRate;
        this.stockQuantity = stockQuantity;
        this.unit = unit;
        this.customFields = new HashMap<>();
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

    public String getUnit() {
        return unit;
    }

    public String getCustomerName() {
        return customerName;
    }

    public Map<String, String> getCustomFields() {
        return customFields;
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

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public void setCustomFields(Map<String, String> customFields) {
        this.customFields = customFields;
    }

    // Helper method to get the quantity from default or custom fields
    public int getEffectiveQuantity() {
        if (stockQuantity > 0) {
            return stockQuantity;
        }
        if (customFields != null) {
            for (Map.Entry<String, String> entry : customFields.entrySet()) {
                String key = entry.getKey().toLowerCase();
                if (key.contains("quantity") || key.contains("qty")) {
                    try {
                        return Integer.parseInt(entry.getValue());
                    } catch (NumberFormatException e) {
                        // Ignore if the value is not a valid number
                    }
                }
            }
        }
        return 0; // Return 0 if no quantity is found
    }
}
