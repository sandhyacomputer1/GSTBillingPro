package com.sandhyasofttech.gstbillingpro.Model;

public class PendingPayment {
    public String invoiceNumber;
    public String customerName;
    public String customerPhone;
    public double totalAmount;
    public double paidAmount;
    public double pendingAmount;
    public String paymentStatus;
    public String lastPaymentDate;
    public long timestamp;

    public PendingPayment() {
        // Required for Firebase
    }

    public PendingPayment(String invoiceNumber, String customerName, String customerPhone,
                          double totalAmount, double paidAmount, double pendingAmount,
                          String paymentStatus, String lastPaymentDate, long timestamp) {
        this.invoiceNumber = invoiceNumber;
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.totalAmount = totalAmount;
        this.paidAmount = paidAmount;
        this.pendingAmount = pendingAmount;
        this.paymentStatus = paymentStatus;
        this.lastPaymentDate = lastPaymentDate;
        this.timestamp = timestamp;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public double getPaidAmount() {
        return paidAmount;
    }

    public double getPendingAmount() {
        return pendingAmount;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public String getLastPaymentDate() {
        return lastPaymentDate;
    }

    public long getTimestamp() {
        return timestamp;
    }
}