package com.sandhyasofttech.gstbillingpro.custmore;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sandhyasofttech.gstbillingpro.R;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CustomerDetailsActivity extends AppCompatActivity {

    private static final String TAG = "CustomerDetails";
    private static final int PERMISSION_CALL = 101;

    // Views
// Views
    private TextView tvName, tvPhone, tvEmail, tvGstin, tvAddress;
    private MaterialButton btnCall, btnWhatsApp, btnEdit, btnDelete, btnExportPdf;
    private RecyclerView rvProducts, rvInvoices;

    // Portfolio views
    private TextView tvTotalInvoices, tvPaidCount, tvUnpaidCount, tvAmountSummary;


    // Data
    private String customerPhone, customerName, userMobile;
    private DatabaseReference userRef, invoicesRef;

    private final List<String> productNames = new ArrayList<>();
    private final List<InvoiceSummary> invoiceList = new ArrayList<>();
    private ProductAdapter productAdapter;
    private InvoiceAdapter invoiceAdapter;
    private int totalInvoices = 0, paidCount = 0, partialCount = 0, unpaidCount = 0;
    private double totalAmount = 0, totalPaid = 0, totalPending = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_details);

        initViews();
        getUserMobile();
        setupFirebase();
        loadCustomerData();
        setupToolbar();
        setupButtons();
        setupRecyclers();
        loadRelatedData();
    }

    private void initViews() {
        tvName = findViewById(R.id.tvDetailName);
        tvPhone = findViewById(R.id.tvDetailPhone);
        tvEmail = findViewById(R.id.tvDetailEmail);
        tvGstin = findViewById(R.id.tvDetailGstin);
        tvAddress = findViewById(R.id.tvDetailAddress);
        btnCall = findViewById(R.id.btnCall);
        btnWhatsApp = findViewById(R.id.btnWhatsApp);
        btnEdit = findViewById(R.id.btnEdit);
        btnDelete = findViewById(R.id.btnDelete);
        btnExportPdf = findViewById(R.id.btnExportPdf);
        rvProducts = findViewById(R.id.rvProducts);
        rvInvoices = findViewById(R.id.rvInvoices);

        tvTotalInvoices = findViewById(R.id.tvTotalInvoices);
        tvPaidCount = findViewById(R.id.tvPaidCount);
        tvUnpaidCount = findViewById(R.id.tvUnpaidCount);
        tvAmountSummary = findViewById(R.id.tvAmountSummary);
    }

    private void getUserMobile() {
        userMobile = getSharedPreferences("APP_PREFS", MODE_PRIVATE).getString("USER_MOBILE", null);
        if (userMobile == null) {
            Toast.makeText(this, "Session expired", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void setupFirebase() {
        userRef = FirebaseDatabase.getInstance().getReference("users").child(userMobile);
        invoicesRef = userRef.child("invoices");
    }

    private void loadCustomerData() {
        customerName = getIntent().getStringExtra("customer_name");
        customerPhone = getIntent().getStringExtra("customer_phone");
        String email = getIntent().getStringExtra("customer_email");
        String gstin = getIntent().getStringExtra("customer_gstin");
        String address = getIntent().getStringExtra("customer_address");

        tvName.setText(customerName != null ? customerName : "N/A");
        tvPhone.setText(customerPhone != null ? customerPhone : "N/A");
        tvEmail.setText(email != null && !email.isEmpty() ? email : "Not provided");
        tvGstin.setText(gstin != null && !gstin.isEmpty() ? "GSTIN: " + gstin : "GSTIN: Not provided");
        tvAddress.setText(address != null && !address.isEmpty() ? address : "Address not provided");
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Customer Information");  // Only this text
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void setupButtons() {
        btnCall.setOnClickListener(v -> makeCall());
        btnWhatsApp.setOnClickListener(v -> openWhatsApp());
        btnEdit.setOnClickListener(v -> editCustomer());
        btnDelete.setOnClickListener(v -> deleteCustomer());
        btnExportPdf.setOnClickListener(v -> exportToPdf());
    }

    private void makeCall() {
        if (customerPhone == null) return;
        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + customerPhone));
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            startActivity(intent);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CALL_PHONE}, PERMISSION_CALL);
        }
    }

    private void openWhatsApp() {
        if (customerPhone == null) return;
        String phone = customerPhone.replaceAll("[^0-9]", "");
        String url = "https://api.whatsapp.com/send?phone=91" + phone;
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
    }

    private void editCustomer() {
        Intent intent = new Intent(this, AddCustomerActivity.class);
        intent.putExtra(AddCustomerActivity.EXTRA_IS_EDIT, true);
        intent.putExtra(AddCustomerActivity.EXTRA_CUSTOMER_ID, customerPhone);
        intent.putExtra(AddCustomerActivity.EXTRA_CUSTOMER_NAME, customerName);
        intent.putExtra(AddCustomerActivity.EXTRA_CUSTOMER_PHONE, customerPhone);
        intent.putExtra(AddCustomerActivity.EXTRA_CUSTOMER_EMAIL, tvEmail.getText().toString());
        intent.putExtra(AddCustomerActivity.EXTRA_CUSTOMER_GSTIN, tvGstin.getText().toString().replace("GSTIN: ", ""));
        intent.putExtra(AddCustomerActivity.EXTRA_CUSTOMER_ADDRESS, tvAddress.getText().toString());
        startActivity(intent);
        finish();
    }

    private void deleteCustomer() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Customer")
                .setMessage("Delete " + customerName + "?")
                .setPositiveButton("Delete", (d, w) -> {
                    userRef.child("customers").child(customerPhone).removeValue()
                            .addOnSuccessListener(a -> {
                                Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show();
                                finish();
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void exportToPdf() {
        try {
            File file = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), customerName + "_Profile.pdf");
            PdfWriter writer = new PdfWriter(file);
            PdfDocument pdf = new PdfDocument(writer);
            Document doc = new Document(pdf);

            doc.add(new Paragraph("Customer Profile").setBold().setFontSize(20).setTextAlignment(TextAlignment.CENTER));
            doc.add(new Paragraph("\nName: " + customerName));
            doc.add(new Paragraph("Phone: " + customerPhone));
            doc.add(new Paragraph("Email: " + tvEmail.getText()));
            doc.add(new Paragraph(tvGstin.getText().toString()));
            doc.add(new Paragraph(tvAddress.getText().toString()));
            doc.add(new Paragraph("\nSummary:").setBold());
            doc.add(new Paragraph("Total Invoices: " + totalInvoices));
            doc.add(new Paragraph("Total Amount: ₹" + String.format("%,.0f", totalAmount)));
            doc.add(new Paragraph("Total Paid: ₹" + String.format("%,.0f", totalPaid)));
            doc.add(new Paragraph("Total Pending: ₹" + String.format("%,.0f", totalPending)));

            doc.add(new Paragraph("\nProducts Used:").setBold());
            for (String p : productNames) doc.add(new Paragraph("• " + p));

            doc.add(new Paragraph("\nInvoices:").setBold());
            for (InvoiceSummary i : invoiceList) {
                doc.add(new Paragraph("• #" + i.number + " | " + i.date + " | ₹" + String.format("%,.0f", i.total)));
            }

            doc.close();

            Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", file);
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("application/pdf");
            share.putExtra(Intent.EXTRA_STREAM, uri);
            startActivity(Intent.createChooser(share, "Share PDF"));
        } catch (Exception e) {
            Toast.makeText(this, "PDF Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void setupRecyclers() {
        productAdapter = new ProductAdapter(productNames);
        rvProducts.setLayoutManager(new LinearLayoutManager(this));
        rvProducts.setAdapter(productAdapter);

        invoiceAdapter = new InvoiceAdapter(invoiceList, id -> {
            Intent intent = new Intent(this, InvoiceDetailsActivity.class);
            intent.putExtra("invoice_id", id);
            startActivity(intent);
        });
        rvInvoices.setLayoutManager(new LinearLayoutManager(this));
        rvInvoices.setAdapter(invoiceAdapter);
    }

    private void loadRelatedData() {
        loadProductsUsed();
        loadInvoices();
    }

    private void loadProductsUsed() {
        invoicesRef.addValueEventListener(new ValueEventListener() {
            final Set<String> unique = new HashSet<>();

            @Override
            public void onDataChange(DataSnapshot snapshot) {
                unique.clear();
                for (DataSnapshot inv : snapshot.getChildren()) {
                    String custId = inv.child("customerId").getValue(String.class);
                    String custName = inv.child("customerName").getValue(String.class);
                    if ((customerPhone != null && customerPhone.equals(custId)) ||
                            (customerName != null && customerName.equals(custName))) {
                        for (DataSnapshot item : inv.child("items").getChildren()) {
                            String name = item.child("productName").getValue(String.class);
                            if (name != null) unique.add(name.trim());
                        }
                    }
                }
                productNames.clear();
                productNames.addAll(unique);
                Collections.sort(productNames);
                productAdapter.notifyDataSetChanged();
            }


            @Override public void onCancelled(DatabaseError e) { }
        });
    }

    private void loadInvoices() {
        invoicesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                invoiceList.clear();
                for (DataSnapshot inv : snapshot.getChildren()) {
                    String custId = inv.child("customerId").getValue(String.class);
                    String custName = inv.child("customerName").getValue(String.class);
                    if ((customerPhone != null && customerPhone.equals(custId)) ||
                            (customerName != null && customerName.equals(custName))) {

                        String no = inv.child("invoiceNumber").getValue(String.class);
                        String date = inv.child("invoiceDate").getValue(String.class);
                        Double total = inv.child("grandTotal").getValue(Double.class);
                        Double paid = inv.child("paidAmount").getValue(Double.class);
                        Double pending = inv.child("pendingAmount").getValue(Double.class);
                        String status = inv.child("paymentStatus").getValue(String.class);
                        String id = inv.getKey();

                        if (no != null && date != null && total != null && id != null) {
                            invoiceList.add(new InvoiceSummary(no, date, total, id));

                            totalInvoices++;
                            totalAmount += total;
                            if (paid != null) totalPaid += paid;
                            if (pending != null) totalPending += pending;

                            if ("Paid".equalsIgnoreCase(status)) paidCount++;
                            else if ("Partial".equalsIgnoreCase(status)) partialCount++;
                            else unpaidCount++;
                        }
                    }

                }
                updatePortfolioUI();
                invoiceAdapter.notifyDataSetChanged();
            }

            @Override public void onCancelled(DatabaseError e) { }
        });
    }

    private void updatePortfolioUI() {
        // safety check (optional)
        if (tvTotalInvoices == null || tvAmountSummary == null) {
            Log.e(TAG, "Portfolio views not bound properly");
            return;
        }

        tvTotalInvoices.setText(String.valueOf(totalInvoices));
        tvPaidCount.setText(String.valueOf(paidCount));
        tvUnpaidCount.setText(String.valueOf(unpaidCount));

        String summary = "Total: ₹" + String.format("%,.0f", totalAmount) +
                " | Paid: ₹" + String.format("%,.0f", totalPaid) +
                " | Pending: ₹" + String.format("%,.0f", totalPending);
        tvAmountSummary.setText(summary);
    }


    // ────────────────────── ADAPTERS ──────────────────────
    static class ProductAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        final List<String> list;
        ProductAdapter(List<String> list) { this.list = list; }
        @Override public RecyclerView.ViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            return new RecyclerView.ViewHolder(
                    android.view.LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.item_product_simple, parent, false)) {};
        }
        @Override public void onBindViewHolder(RecyclerView.ViewHolder holder, int pos) {
            ((TextView) holder.itemView.findViewById(R.id.tvProductName)).setText("• " + list.get(pos));
        }

        @Override public int getItemCount() { return list.size(); }
    }

    static class InvoiceAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        final List<InvoiceSummary> list;
        final OnClick click;
        interface OnClick { void onClick(String id); }
        InvoiceAdapter(List<InvoiceSummary> list, OnClick click) { this.list = list; this.click = click; }
        @Override public RecyclerView.ViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            android.view.View v = android.view.LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_invoice_summary, parent, false);
            return new RecyclerView.ViewHolder(v) {};
        }
        @Override public void onBindViewHolder(RecyclerView.ViewHolder holder, int pos) {
            InvoiceSummary i = list.get(pos);
            ((TextView) holder.itemView.findViewById(R.id.tvInvoiceNo)).setText(i.number);
            ((TextView) holder.itemView.findViewById(R.id.tvInvoiceDate)).setText(i.date);
            ((TextView) holder.itemView.findViewById(R.id.tvInvoiceTotal)).setText("₹" + String.format("%,.0f", i.total));
            holder.itemView.setOnClickListener(v -> click.onClick(i.id));
        }
        @Override public int getItemCount() { return list.size(); }
    }

    static class InvoiceSummary {
        String number, date, id; double total;
        InvoiceSummary(String n, String d, double t, String id) { number = n; date = d; total = t; this.id = id; }
    }

}