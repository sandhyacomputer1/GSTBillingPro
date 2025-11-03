// Invoice.java
package com.sandhyasofttech.gstbillingpro.invoice;

import java.util.List;

public class Invoice {
    public String invoiceNumber, customerId, customerName, invoiceDate;
    public List<InvoiceItem> items;
    public double totalTaxableValue, totalCGST, totalSGST, totalIGST, grandTotal;
    public String companyLogoUrl, signatureUrl;

    public Invoice() { }

    public Invoice(String invoiceNumber, String customerId, String customerName, String invoiceDate,
                   List<InvoiceItem> items, double totalTaxableValue, double totalCGST, double totalSGST,
                   double totalIGST, double grandTotal, String companyLogoUrl, String signatureUrl) {
        this.invoiceNumber = invoiceNumber;
        this.customerId = customerId;
        this.customerName = customerName;
        this.invoiceDate = invoiceDate;
        this.items = items;
        this.totalTaxableValue = totalTaxableValue;
        this.totalCGST = totalCGST;
        this.totalSGST = totalSGST;
        this.totalIGST = totalIGST;
        this.grandTotal = grandTotal;
        this.companyLogoUrl = companyLogoUrl;
        this.signatureUrl = signatureUrl;
    }
}
