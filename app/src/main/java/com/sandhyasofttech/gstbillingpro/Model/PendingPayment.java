package com.sandhyasofttech.gstbillingpro.Model;

public class PendingPayment {
    public String invoiceNumber;
    public String customerName;
    public double totalAmount;
    public double paidAmount;
    public double pendingAmount;
    public String paymentStatus; // "Pending", "Partial", "Paid"
    public String lastPaymentDate;
    public String completionDate; // For completed payments
    public long timestamp;

    public PendingPayment() {
        // Default constructor required for Firebase
    }

    public PendingPayment(String invoiceNumber, String customerName, double totalAmount,
                          double paidAmount, double pendingAmount, String paymentStatus,
                          String lastPaymentDate, long timestamp) {
        this.invoiceNumber = invoiceNumber;
        this.customerName = customerName;
        this.totalAmount = totalAmount;
        this.paidAmount = paidAmount;
        this.pendingAmount = pendingAmount;
        this.paymentStatus = paymentStatus;
        this.lastPaymentDate = lastPaymentDate;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public double getPaidAmount() {
        return paidAmount;
    }

    public void setPaidAmount(double paidAmount) {
        this.paidAmount = paidAmount;
    }

    public double getPendingAmount() {
        return pendingAmount;
    }

    public void setPendingAmount(double pendingAmount) {
        this.pendingAmount = pendingAmount;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getLastPaymentDate() {
        return lastPaymentDate;
    }

    public void setLastPaymentDate(String lastPaymentDate) {
        this.lastPaymentDate = lastPaymentDate;
    }

    public String getCompletionDate() {
        return completionDate;
    }

    public void setCompletionDate(String completionDate) {
        this.completionDate = completionDate;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}