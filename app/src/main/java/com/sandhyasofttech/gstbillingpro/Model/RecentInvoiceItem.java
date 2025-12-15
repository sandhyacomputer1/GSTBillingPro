package com.sandhyasofttech.gstbillingpro.Model;

public class RecentInvoiceItem {

    // EXISTING
    public String invoiceNo;
    public String customerId;     // mobile number
    public String customerName;
    public double grandTotal;
    public String date;

    // 🔥 NEW
    public double pendingAmount;

    public RecentInvoiceItem() {}

    // EXISTING CONSTRUCTOR (KEEP)
    public RecentInvoiceItem(String invoiceNo,
                             String customerId,
                             String customerName,
                             double grandTotal,
                             String date) {

        this.invoiceNo = invoiceNo;
        this.customerId = customerId;
        this.customerName = customerName;
        this.grandTotal = grandTotal;
        this.date = date;
        this.pendingAmount = 0;
    }

    // 🔥 NEW CONSTRUCTOR (HOME / RECENT)
    public RecentInvoiceItem(String invoiceNo,
                             String customerId,
                             String customerName,
                             double grandTotal,
                             double pendingAmount,
                             String date) {

        this.invoiceNo = invoiceNo;
        this.customerId = customerId;
        this.customerName = customerName;
        this.grandTotal = grandTotal;
        this.pendingAmount = pendingAmount;
        this.date = date;
    }
}
