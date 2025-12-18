package com.sandhyasofttech.gstbillingpro.Model;

public class RecentInvoiceItem {

    // Fields
    public String invoiceNo;
    public String customerId;     // mobile number
    public String customerName;
    public double grandTotal;
    public String date;           // this already stores invoice date
    public double pendingAmount;

    public RecentInvoiceItem() {
    }

    // OLD constructor (without pending)
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

    // NEW constructor (with pending)
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

    // Use the existing 'date' field as invoice date
    public String getInvoiceDate() {
        return date;
    }

    public double getPendingAmount() {
        return pendingAmount;
    }

    public double getGrandTotal() {
        return grandTotal;
    }

    public String getInvoiceNo() {
        return invoiceNo;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getCustomerName() {
        return customerName;
    }
}