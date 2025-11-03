// InvoiceItem.java
package com.sandhyasofttech.gstbillingpro.invoice;

public class InvoiceItem {
    public String productId, productName;
    public double quantity, rate, taxPercent;

    public InvoiceItem() { }
    public InvoiceItem(String productId, String productName, double quantity, double rate, double taxPercent) {
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.rate = rate;
        this.taxPercent = taxPercent;
    }

    public double getTaxableValue() { return quantity * rate; }
    public double getTaxAmount() { return getTaxableValue() * taxPercent / 100; }
}

