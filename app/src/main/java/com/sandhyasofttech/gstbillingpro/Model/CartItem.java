package com.sandhyasofttech.gstbillingpro.Model;

import java.io.Serializable;

public class CartItem implements Serializable {

    private String productId;
    private String productName;
    private double quantity;
    private double rate;
    private double taxPercent;
    private int maxStock;

    // ✅ ONLY ADDED FIELD
    private String unit;

    public CartItem() {}

    // ❌ OLD CONSTRUCTOR (KEEP AS IS – DO NOT REMOVE)
    public CartItem(String productId, String productName, double quantity,
                    double rate, double taxPercent, int maxStock) {
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.rate = rate;
        this.taxPercent = taxPercent;
        this.maxStock = maxStock;
    }

    // ✅ NEW CONSTRUCTOR (WITH UNIT)
    public CartItem(String productId, String productName, double quantity,
                    double rate, double taxPercent, int maxStock, String unit) {
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.rate = rate;
        this.taxPercent = taxPercent;
        this.maxStock = maxStock;
        this.unit = unit;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    public double getTaxPercent() {
        return taxPercent;
    }

    public void setTaxPercent(double taxPercent) {
        this.taxPercent = taxPercent;
    }

    public int getMaxStock() {
        return maxStock;
    }

    public void setMaxStock(int maxStock) {
        this.maxStock = maxStock;
    }

    // ✅ ONLY ADDED GETTER & SETTER
    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public double getTaxableValue() {
        return quantity * rate;
    }

    public double getTotalWithTax() {
        double taxable = getTaxableValue();
        double tax = (taxable * taxPercent) / 100;
        return taxable + tax;
    }
}
