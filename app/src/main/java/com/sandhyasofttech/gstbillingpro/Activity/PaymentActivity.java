package com.sandhyasofttech.gstbillingpro.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.*;
import com.sandhyasofttech.gstbillingpro.Adapter.PaymentSummaryAdapter;
import com.sandhyasofttech.gstbillingpro.Model.CartItem;
import com.sandhyasofttech.gstbillingpro.R;
import com.sandhyasofttech.gstbillingpro.invoice.Invoice;
import com.sandhyasofttech.gstbillingpro.invoice.InvoiceItem;
import com.sandhyasofttech.gstbillingpro.invoice.GstCalculationUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class PaymentActivity extends AppCompatActivity {

    private TextView tvCustomerName, tvSubtotal, tvTax, tvGrandTotal;
    private TextView tvAmountPaid, tvBalance, tvPendingAmount;
    private EditText etPaidAmount;
    private RadioGroup rgPaymentStatus;
    private MaterialButton btnGenerateInvoice;
    private RecyclerView rvSummary;
    private boolean isGstEnabled = false;   // 🔥 GST OFF by default
    private boolean isIntraState = true;
    private String customerName, customerPhone, customerAddress, userMobile;
    private ArrayList<CartItem> cartItems;
    private double cartTotal, totalTaxable, totalCGST, totalSGST, totalIGST, grandTotal;
    private double paidAmount = 0, pendingAmount = 0;

    private DatabaseReference usersRef, invoicesRef, infoRef;
    private String businessName = "Your Business", businessGstin = "", businessAddress = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        // Get intent data
        customerName = getIntent().getStringExtra("CUSTOMER_NAME");
        customerPhone = getIntent().getStringExtra("CUSTOMER_PHONE");
        customerAddress = getIntent().getStringExtra("CUSTOMER_ADDRESS");
        cartItems = (ArrayList<CartItem>) getIntent().getSerializableExtra("CART_ITEMS");
        cartTotal = getIntent().getDoubleExtra("CART_TOTAL", 0);

        // Initialize views
        tvCustomerName = findViewById(R.id.tvCustomerName);
        tvSubtotal = findViewById(R.id.tvSubtotal);
        tvTax = findViewById(R.id.tvTax);
        tvGrandTotal = findViewById(R.id.tvGrandTotal);
        tvAmountPaid = findViewById(R.id.tvAmountPaid);
        tvBalance = findViewById(R.id.tvBalance);
        tvPendingAmount = findViewById(R.id.tvPendingAmount);
        etPaidAmount = findViewById(R.id.etPaidAmount);
        rgPaymentStatus = findViewById(R.id.rgPaymentStatus);
        btnGenerateInvoice = findViewById(R.id.btnGenerateInvoice);
        rvSummary = findViewById(R.id.rvSummary);

        tvCustomerName.setText("Bill To: " + customerName);

        // Get user mobile
        SharedPreferences prefs = getSharedPreferences("APP_PREFS", MODE_PRIVATE);
        userMobile = prefs.getString("USER_MOBILE", null);

        if (userMobile == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize Firebase
        usersRef = FirebaseDatabase.getInstance().getReference("users").child(userMobile);
        invoicesRef = usersRef.child("invoices");
        infoRef = usersRef.child("info");

        // Load business info
        fetchBusinessInfo();

        // Calculate totals
        calculateTotals();

        // Setup summary recycler
        setupSummaryRecycler();

        // Payment amount listener
        etPaidAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                calculatePayment();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Payment status listener
        rgPaymentStatus.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbFullyPaid) {
                etPaidAmount.setText(String.valueOf(grandTotal));
                etPaidAmount.setEnabled(false);
            } else {
                etPaidAmount.setEnabled(true);
            }
        });

        // Generate invoice button
        btnGenerateInvoice.setOnClickListener(v -> generateInvoice());

        // Back button
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void fetchBusinessInfo() {
        infoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    businessName = snapshot.child("businessName").getValue(String.class);
                    businessGstin = snapshot.child("gstin").getValue(String.class);
                    businessAddress = snapshot.child("address").getValue(String.class);
                    if (businessName == null) businessName = "Your Business";
                    if (businessGstin == null) businessGstin = "";
                    if (businessAddress == null) businessAddress = "";
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void calculateTotals() {

        totalTaxable = 0;
        totalCGST = 0;
        totalSGST = 0;
        totalIGST = 0;

        for (CartItem item : cartItems) {
            double taxable = item.getTaxableValue();
            totalTaxable += taxable;

            if (isGstEnabled) {
                GstCalculationUtil.GstDetails gst =
                        GstCalculationUtil.calculateGst(
                                taxable,
                                item.getTaxPercent(),
                                isIntraState
                        );

                totalCGST += gst.cgst;
                totalSGST += gst.sgst;
                totalIGST += gst.igst;
            }
        }

        // ✅ Grand Total Logic
        grandTotal = totalTaxable;
        if (isGstEnabled) {
            grandTotal += totalCGST + totalSGST + totalIGST;
        }

        // UI update
        tvSubtotal.setText(String.format(Locale.getDefault(), "₹%.2f", totalTaxable));

        if (isGstEnabled) {
            tvTax.setVisibility(View.VISIBLE);
            tvTax.setText(String.format(
                    Locale.getDefault(),
                    "CGST: ₹%.2f | SGST: ₹%.2f | IGST: ₹%.2f",
                    totalCGST, totalSGST, totalIGST
            ));
        } else {
            tvTax.setVisibility(View.GONE);
        }

        tvGrandTotal.setText(String.format(Locale.getDefault(), "₹%.2f", grandTotal));

        pendingAmount = grandTotal;
        tvPendingAmount.setText(String.format(Locale.getDefault(), "₹%.2f", pendingAmount));
    }

    private void setupSummaryRecycler() {
        PaymentSummaryAdapter adapter = new PaymentSummaryAdapter(cartItems);
        rvSummary.setLayoutManager(new LinearLayoutManager(this));
        rvSummary.setAdapter(adapter);
    }

    private void calculatePayment() {
        String paidStr = etPaidAmount.getText().toString().trim();
        if (paidStr.isEmpty()) {
            paidAmount = 0;
        } else {
            paidAmount = Double.parseDouble(paidStr);
        }

        double balance = paidAmount - grandTotal;
        pendingAmount = grandTotal - paidAmount;

        if (pendingAmount < 0) pendingAmount = 0;

        tvAmountPaid.setText(String.format(Locale.getDefault(), "₹%.2f", paidAmount));
        tvBalance.setText(String.format(Locale.getDefault(),
                balance >= 0 ? "₹%.2f" : "-₹%.2f", Math.abs(balance)));
        tvPendingAmount.setText(String.format(Locale.getDefault(), "₹%.2f", pendingAmount));
    }

    private void generateInvoice() {
        if (paidAmount == 0) {
            Toast.makeText(this, "Please enter paid amount", Toast.LENGTH_SHORT).show();
            return;
        }

        String invoiceNumber = generateInvoiceNumber();
        String invoiceDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String invoiceTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
        long timestamp = System.currentTimeMillis();

        // Convert CartItem to InvoiceItem
        ArrayList<InvoiceItem> invoiceItems = new ArrayList<>();
        for (CartItem cart : cartItems) {
            invoiceItems.add(new InvoiceItem(
                    cart.getProductId(),
                    cart.getProductName(),
                    cart.getQuantity(),
                    cart.getRate(),
                    cart.getTaxPercent()
            ));
        }

        // Determine payment status
        String paymentStatus;
        if (paidAmount >= grandTotal) {
            paymentStatus = "Paid";
            pendingAmount = 0;
        } else if (paidAmount > 0) {
            paymentStatus = "Partial";
        } else {
            paymentStatus = "Pending";
        }

        // Create invoice object
        Invoice invoice = new Invoice(
                invoiceNumber, customerPhone, customerName, invoiceDate,
                invoiceItems, totalTaxable, totalCGST, totalSGST, totalIGST, grandTotal,
                businessName, businessAddress
        );

        // Set payment details
        invoice.paidAmount = paidAmount;
        invoice.pendingAmount = pendingAmount;
        invoice.paymentStatus = paymentStatus;
        invoice.invoiceTime = invoiceTime;
        invoice.timestamp = timestamp;
        invoice.customerAddress = customerAddress;

        // Save to Firebase with detailed payment info
        Map<String, Object> invoiceData = new HashMap<>();
        invoiceData.put("invoiceNumber", invoice.invoiceNumber);
        invoiceData.put("customerPhone", invoice.customerPhone);
        invoiceData.put("customerName", invoice.customerName);
        invoiceData.put("customerAddress", customerAddress);
        invoiceData.put("invoiceDate", invoice.invoiceDate);
        invoiceData.put("invoiceTime", invoiceTime);
        invoiceData.put("timestamp", timestamp);
        invoiceData.put("items", invoiceItems);
        invoiceData.put("totalTaxableValue", invoice.totalTaxableValue);
        invoiceData.put("totalCGST", isGstEnabled ? invoice.totalCGST : 0);
        invoiceData.put("totalSGST", isGstEnabled ? invoice.totalSGST : 0);
        invoiceData.put("totalIGST", isGstEnabled ? invoice.totalIGST : 0);
        invoiceData.put("gstEnabled", isGstEnabled);

        invoiceData.put("grandTotal", invoice.grandTotal);
        invoiceData.put("businessName", invoice.businessName);
        invoiceData.put("businessAddress", invoice.businessAddress);

        // Payment details
        invoiceData.put("paidAmount", paidAmount);
        invoiceData.put("pendingAmount", pendingAmount);
        invoiceData.put("paymentStatus", paymentStatus);
        invoiceData.put("paymentDate", invoiceDate);

        // Save to Firebase
        invoicesRef.child(invoiceNumber).setValue(invoiceData)
                .addOnSuccessListener(aVoid -> {

                    // 🔥 FIRST HISTORY ENTRY (VERY IMPORTANT)
                    addInvoiceHistoryAtCreation(
                            invoiceNumber,
                            paidAmount,        // paid now
                            paidAmount,        // total paid
                            pendingAmount      // pending
                    );

                    if (!paymentStatus.equals("Paid")) {
                        savePendingPayment(
                                invoiceNumber,
                                customerName,
                                customerPhone,
                                grandTotal,
                                paidAmount,
                                pendingAmount
                        );
                    }

                    updateProductStocks();

                    File pdfFile = generatePdf(invoice);
                    if (pdfFile != null) {
                        showPostSaveDialog(pdfFile);
                    }
                });
    }

    private void savePendingPayment(String invoiceNumber, String customerName,
                                    String customerPhone, double totalAmount,
                                    double paidAmount, double pendingAmount) {
        DatabaseReference pendingPaymentsRef = usersRef.child("pendingPayments");

        Map<String, Object> paymentData = new HashMap<>();
        paymentData.put("invoiceNumber", invoiceNumber);
        paymentData.put("customerName", customerName);
        paymentData.put("customerPhone", customerPhone);
        paymentData.put("totalAmount", totalAmount);
        paymentData.put("paidAmount", paidAmount);
        paymentData.put("pendingAmount", pendingAmount);
        paymentData.put("paymentStatus", pendingAmount > 0 ? "Partial" : "Pending");
        paymentData.put("lastPaymentDate", new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));
        paymentData.put("timestamp", System.currentTimeMillis());

        pendingPaymentsRef.child(invoiceNumber).setValue(paymentData);
    }

    private void updateProductStocks() {
        for (CartItem item : cartItems) {
            DatabaseReference productRef = usersRef.child("products").child(item.getProductId());
            productRef.runTransaction(new Transaction.Handler() {
                @NonNull
                @Override
                public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                    Integer currentStock = mutableData.child("stockQuantity").getValue(Integer.class);
                    if (currentStock == null) return Transaction.success(mutableData);
                    int newStock = currentStock - (int) item.getQuantity();
                    mutableData.child("stockQuantity").setValue(Math.max(0, newStock));
                    return Transaction.success(mutableData);
                }

                @Override
                public void onComplete(DatabaseError error, boolean committed, DataSnapshot snapshot) {}
            });
        }
    }

    private String generateInvoiceNumber() {
        String datePart = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());
        return "INV-" + datePart + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    private File generatePdf(Invoice invoice) {
        try {
            File invoiceDir = new File(getFilesDir(), "Invoices");
            if (!invoiceDir.exists()) invoiceDir.mkdirs();
            File file = new File(invoiceDir, invoice.invoiceNumber + ".pdf");

            PdfDocument pdf = new PdfDocument();
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
            PdfDocument.Page page = pdf.startPage(pageInfo);
            Canvas canvas = page.getCanvas();

            Paint titlePaint = new Paint();
            titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            titlePaint.setTextSize(18);

            Paint boldPaint = new Paint();
            boldPaint.setTypeface(Typeface.DEFAULT_BOLD);
            boldPaint.setTextSize(12);

            Paint regularPaint = new Paint();
            regularPaint.setTextSize(11);

            int yPos = 40;
            int margin = 40;
            int pageWidth = pageInfo.getPageWidth();

            // Header
            canvas.drawText(businessName, margin, yPos, titlePaint);
            canvas.drawText("TAX INVOICE", pageWidth - margin - 120, yPos, titlePaint);
            yPos += 20;

            if (businessAddress != null && !businessAddress.isEmpty()) {
                canvas.drawText(businessAddress, margin, yPos, regularPaint);
                yPos += 15;
            }
            if (businessGstin != null && !businessGstin.isEmpty()) {
                canvas.drawText("GSTIN: " + businessGstin, margin, yPos, regularPaint);
                yPos += 15;
            }
            yPos += 15;

            canvas.drawLine(margin, yPos, pageWidth - margin, yPos, regularPaint);
            yPos += 20;

            // Invoice details
            canvas.drawText("Invoice No: " + invoice.invoiceNumber, margin, yPos, boldPaint);
            canvas.drawText("Date: " + invoice.invoiceDate, pageWidth - margin - 150, yPos, boldPaint);
            yPos += 25;

            // Customer details
            canvas.drawText("Bill To:", margin, yPos, boldPaint);
            yPos += 15;
            canvas.drawText(invoice.customerName, margin, yPos, regularPaint);
            yPos += 15;
            if (customerAddress != null && !customerAddress.isEmpty()) {
                canvas.drawText(customerAddress, margin, yPos, regularPaint);
                yPos += 15;
            }
            canvas.drawText("Phone: " + customerPhone, margin, yPos, regularPaint);
            yPos += 30;

            // Table header
            canvas.drawText("Item", margin, yPos, boldPaint);
            canvas.drawText("Qty", margin + 250, yPos, boldPaint);
            canvas.drawText("Rate", margin + 310, yPos, boldPaint);
            canvas.drawText("Amount", pageWidth - margin - 70, yPos, boldPaint);
            yPos += 5;
            canvas.drawLine(margin, yPos, pageWidth - margin, yPos, regularPaint);
            yPos += 20;

            // Table items
            for (InvoiceItem item : invoice.items) {
                canvas.drawText(trimText(item.productName, 30), margin, yPos, regularPaint);
                canvas.drawText(String.format(Locale.getDefault(), "%.2f", item.quantity),
                        margin + 250, yPos, regularPaint);
                canvas.drawText(String.format(Locale.getDefault(), "%.2f", item.rate),
                        margin + 310, yPos, regularPaint);
                canvas.drawText(String.format(Locale.getDefault(), "%.2f", item.getTaxableValue()),
                        pageWidth - margin - 70, yPos, regularPaint);
                yPos += 18;
            }

            yPos += 10;
            canvas.drawLine(margin, yPos, pageWidth - margin, yPos, regularPaint);
            yPos += 20;

            // Totals
            int totalsX = pageWidth - margin - 200;
            canvas.drawText("Taxable Amount:", totalsX, yPos, regularPaint);
            canvas.drawText("₹" + invoice.totalTaxableValue,
                    pageWidth - margin - 70, yPos, regularPaint);
            yPos += 18;

            if (isGstEnabled) {

                canvas.drawText("CGST:", totalsX, yPos, regularPaint);
                canvas.drawText("₹" + invoice.totalCGST,
                        pageWidth - margin - 70, yPos, regularPaint);
                yPos += 18;

                canvas.drawText("SGST:", totalsX, yPos, regularPaint);
                canvas.drawText("₹" + invoice.totalSGST,
                        pageWidth - margin - 70, yPos, regularPaint);
                yPos += 18;

                if (invoice.totalIGST > 0) {
                    canvas.drawText("IGST:", totalsX, yPos, regularPaint);
                    canvas.drawText("₹" + invoice.totalIGST,
                            pageWidth - margin - 70, yPos, regularPaint);
                    yPos += 18;
                }
            }
            canvas.drawText(
                    isGstEnabled ? "TAX INVOICE" : "INVOICE",
                    pageWidth - margin - 120,
                    yPos,
                    titlePaint
            );


            canvas.drawLine(totalsX - 10, yPos, pageWidth - margin, yPos, boldPaint);
            yPos += 18;

            boldPaint.setTextSize(14);
            canvas.drawText("Grand Total:", totalsX, yPos, boldPaint);
            canvas.drawText(String.format(Locale.getDefault(), "₹%.2f", invoice.grandTotal),
                    pageWidth - margin - 70, yPos, boldPaint);
            yPos += 25;

            boldPaint.setTextSize(12);
            canvas.drawText("Paid Amount:", totalsX, yPos, regularPaint);
            canvas.drawText(String.format(Locale.getDefault(), "₹%.2f", invoice.paidAmount),
                    pageWidth - margin - 70, yPos, regularPaint);
            yPos += 18;

            canvas.drawText("Pending Amount:", totalsX, yPos, regularPaint);
            canvas.drawText(String.format(Locale.getDefault(), "₹%.2f", invoice.pendingAmount),
                    pageWidth - margin - 70, yPos, regularPaint);
            yPos += 18;

            canvas.drawText("Status:", totalsX, yPos, regularPaint);
            canvas.drawText(invoice.paymentStatus, pageWidth - margin - 70, yPos, regularPaint);
            yPos += 40;

            // Footer
            regularPaint.setTextAlign(Paint.Align.CENTER);
            regularPaint.setTextSize(10);
            canvas.drawText("Thank you for your business!", pageWidth / 2, yPos, regularPaint);

            pdf.finishPage(page);
            pdf.writeTo(new FileOutputStream(file));
            pdf.close();

            return file;
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "PDF generation failed", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    private String trimText(String text, int maxLen) {
        return text.length() > maxLen ? text.substring(0, maxLen - 3) + "..." : text;
    }

    private void showPostSaveDialog(File pdfFile) {
        CharSequence[] options = {"View PDF", "Share on WhatsApp", "Done"};

        new AlertDialog.Builder(this)
                .setTitle("Invoice Generated!")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            openPdf(pdfFile);
                            break;
                        case 1:
                            shareOnWhatsApp(pdfFile);
                            break;
                        case 2:
                            navigateToHome();
                            break;
                    }
                })
                .setCancelable(false)
                .show();
    }

    private void openPdf(File file) {
        Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", file);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "application/pdf");
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NO_HISTORY);
        try {
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Install a PDF viewer", Toast.LENGTH_SHORT).show();
        }
    }

    private void shareOnWhatsApp(File pdfFile) {
        String phone = customerPhone.replaceAll("[^0-9]", "");
        if (!phone.startsWith("91")) phone = "91" + phone;

        Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", pdfFile);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("application/pdf");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setPackage("com.whatsapp");
        intent.putExtra("jid", phone + "@s.whatsapp.net");
        try {
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "WhatsApp not installed", Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToHome() {
        Intent intent = new Intent(this, CustomerSelectionActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void addInvoiceHistoryAtCreation(String invoiceNumber,
                                             double paidNow,
                                             double totalPaid,
                                             double pendingAfter) {

        DatabaseReference historyRef = invoicesRef.child(invoiceNumber).child("history");

        String historyId = historyRef.push().getKey();
        if (historyId == null) return;

        Map<String, Object> history = new HashMap<>();
        history.put("paidNow", paidNow);
        history.put("totalPaid", totalPaid);
        history.put("pendingAfter", pendingAfter);
        history.put("paymentMode", "Cash");
        history.put("date", new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));
        history.put("time", new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date()));
        history.put("timestamp", ServerValue.TIMESTAMP);

        historyRef.child(historyId).setValue(history);
    }

}