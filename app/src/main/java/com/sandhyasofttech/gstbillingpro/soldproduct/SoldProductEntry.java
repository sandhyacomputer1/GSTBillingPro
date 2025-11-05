package com.sandhyasofttech.gstbillingpro.soldproduct;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class SoldProductEntry {
    public String invoiceNumber;
    public String invoiceDate;
    public String customerName;
    public String productName;
    public double quantity;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.getDefault());

    public SoldProductEntry() {} // Required for Firebase

    public SoldProductEntry(String invoiceNumber, String invoiceDate, String customerName,
                            String productName, double quantity) {
        this.invoiceNumber = invoiceNumber;
        this.invoiceDate = invoiceDate;
        this.customerName = customerName;
        this.productName = productName;
        this.quantity = quantity;
    }

    public boolean isForDate(LocalDate date) {
        try {
            LocalDate invoiceLocalDate = LocalDate.parse(invoiceDate, formatter);
            return invoiceLocalDate.equals(date);
        } catch (Exception e) {
            return false;
        }
    }
}
