package com.sandhyasofttech.gstbillingpro.Model;

public class RecentInvoiceItem {
    public String invoiceNo;
    public String customerId;
    public String customerName;
    public double grandTotal;
    public String date;

    public RecentInvoiceItem() { }

    public RecentInvoiceItem(String invoiceNo, String customerId, String customerName, double grandTotal, String date) {
        this.invoiceNo = invoiceNo;
        this.customerId = customerId;
        this.customerName = customerName;
        this.grandTotal = grandTotal;
        this.date = date;
    }
}
