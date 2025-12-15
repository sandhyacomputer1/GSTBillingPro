package com.sandhyasofttech.gstbillingpro.Model;

public class InvModel {

    public String productId;
    public String productName;
    public double quantity;
    public double rate;
    public double taxPercent;
    public double taxAmount;
    public double taxableValue;

    public InvModel() {}

    public InvModel(String productId, String productName, double quantity,
                    double rate, double taxPercent, double taxAmount, double taxableValue) {
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.rate = rate;
        this.taxPercent = taxPercent;
        this.taxAmount = taxAmount;
        this.taxableValue = taxableValue;
    }
}
