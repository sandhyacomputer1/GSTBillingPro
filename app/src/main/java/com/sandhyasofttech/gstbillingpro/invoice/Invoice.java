package com.sandhyasofttech.gstbillingpro.invoice;

import java.util.List;

public class Invoice {
    public String invoiceNumber;
    public String customerPhone;
    public String customerName;
    public String invoiceDate;
    public List<InvoiceItem> items;
    public double totalTaxableValue;
    public double totalCGST;
    public double totalSGST;
    public double totalIGST;
    public double grandTotal;
    public String businessName;
    public String businessAddress;

    // Payment tracking fields
    public double paidAmount;
    public double pendingAmount;
    public String paymentStatus;

    // Additional fields for better tracking
    public String invoiceTime;
    public long timestamp;
    public String customerAddress;
    public String paymentDate;

    // Default constructor (required for Firebase)
    public Invoice() {
        this.paidAmount = 0;
        this.pendingAmount = 0;
        this.paymentStatus = "Pending";
    }

    // Constructor with all original parameters
    public Invoice(String invoiceNumber, String customerPhone, String customerName,
                   String invoiceDate, List<InvoiceItem> items, double totalTaxableValue,
                   double totalCGST, double totalSGST, double totalIGST, double grandTotal,
                   String businessName, String businessAddress) {
        this.invoiceNumber = invoiceNumber;
        this.customerPhone = customerPhone;
        this.customerName = customerName;
        this.invoiceDate = invoiceDate;
        this.items = items;
        this.totalTaxableValue = totalTaxableValue;
        this.totalCGST = totalCGST;
        this.totalSGST = totalSGST;
        this.totalIGST = totalIGST;
        this.grandTotal = grandTotal;
        this.businessName = businessName;
        this.businessAddress = businessAddress;

        // Initialize new fields with default values
        this.paidAmount = 0;
        this.pendingAmount = grandTotal;
        this.paymentStatus = "Pending";
    }

    // Getters and Setters for new fields
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

    // You can also add getters/setters for other fields if needed
    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(String invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    public List<InvoiceItem> getItems() {
        return items;
    }

    public void setItems(List<InvoiceItem> items) {
        this.items = items;
    }

    public double getTotalTaxableValue() {
        return totalTaxableValue;
    }

    public void setTotalTaxableValue(double totalTaxableValue) {
        this.totalTaxableValue = totalTaxableValue;
    }

    public double getTotalCGST() {
        return totalCGST;
    }

    public void setTotalCGST(double totalCGST) {
        this.totalCGST = totalCGST;
    }

    public double getTotalSGST() {
        return totalSGST;
    }

    public void setTotalSGST(double totalSGST) {
        this.totalSGST = totalSGST;
    }

    public double getTotalIGST() {
        return totalIGST;
    }

    public void setTotalIGST(double totalIGST) {
        this.totalIGST = totalIGST;
    }

    public double getGrandTotal() {
        return grandTotal;
    }

    public void setGrandTotal(double grandTotal) {
        this.grandTotal = grandTotal;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getBusinessAddress() {
        return businessAddress;
    }

    public void setBusinessAddress(String businessAddress) {
        this.businessAddress = businessAddress;
    }
}