// File: Invoice.java
package com.sandhyasofttech.gstbillingpro.invoice;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@IgnoreExtraProperties
public class Invoice {

    public String invoiceNumber;
    public String customerId;
    public String customerName;
    public String invoiceDate;
    public List<InvoiceItem> items = new ArrayList<>();
    public double totalTaxableValue;
    public double totalCGST;
    public double totalSGST;
    public double totalIGST;
    public double grandTotal;
    public String companyLogoUrl;
    public String signatureUrl;

    public Invoice() {}

    public Invoice(String invoiceNumber, String customerId, String customerName, String invoiceDate,
                   List<InvoiceItem> items, double totalTaxableValue, double totalCGST, double totalSGST,
                   double totalIGST, double grandTotal, String companyLogoUrl, String signatureUrl) {
        this.invoiceNumber = invoiceNumber;
        this.customerId = customerId;
        this.customerName = customerName;
        this.invoiceDate = invoiceDate;
        this.items = items != null ? items : new ArrayList<>();
        this.totalTaxableValue = totalTaxableValue;
        this.totalCGST = totalCGST;
        this.totalSGST = totalSGST;
        this.totalIGST = totalIGST;
        this.grandTotal = grandTotal;
        this.companyLogoUrl = companyLogoUrl;
        this.signatureUrl = signatureUrl;
    }

    // UNIVERSAL SETTER: Accepts both Map (old) and List (new)
    @SuppressWarnings("unchecked")
    public void setItems(Object data) {
        this.items = new ArrayList<>();

        if (data instanceof List) {
            // New format: [ {...}, {...} ]
            for (Object obj : (List<?>) data) {
                if (obj instanceof Map) {
                    addItemFromMap((Map<String, Object>) obj);
                }
            }
        } else if (data instanceof Map) {
            // Old format: { "0": {...}, "1": {...} }
            Map<String, Object> map = (Map<String, Object>) data;
            for (Object obj : map.values()) {
                if (obj instanceof Map) {
                    addItemFromMap((Map<String, Object>) obj);
                }
            }
        }
    }

    private void addItemFromMap(Map<String, Object> itemMap) {
        InvoiceItem item = new InvoiceItem();
        item.productId = (String) itemMap.get("productId");
        item.productName = (String) itemMap.get("productName");
        item.quantity = getDouble(itemMap, "quantity");
        item.rate = getDouble(itemMap, "rate");
        item.taxPercent = getDouble(itemMap, "taxPercent");
        this.items.add(item);
    }

    private double getDouble(Map<String, Object> map, String key) {
        Object val = map.get(key);
        return (val instanceof Number) ? ((Number) val).doubleValue() : 0.0;
    }
}