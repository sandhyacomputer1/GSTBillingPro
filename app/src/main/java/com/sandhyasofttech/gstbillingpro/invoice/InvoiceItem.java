package com.sandhyasofttech.gstbillingpro.invoice;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class InvoiceItem {
    public String productId;
    public String productName;
    public double quantity;
    public double rate;
    public double taxPercent;

    public InvoiceItem() {}

    public InvoiceItem(String productId, String productName, double quantity, double rate, double taxPercent) {
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.rate = rate;
        this.taxPercent = taxPercent;
    }

    public void setProductId(String productId) { this.productId = productId; }
    public void setProductName(String productName) { this.productName = productName; }
    public void setQuantity(double quantity) { this.quantity = quantity; }
    public void setRate(double rate) { this.rate = rate; }
    public void setTaxPercent(double taxPercent) { this.taxPercent = taxPercent; }

    public double getTaxableValue() { return quantity * rate; }
    public double getTaxAmount() { return getTaxableValue() * taxPercent / 100; }
}