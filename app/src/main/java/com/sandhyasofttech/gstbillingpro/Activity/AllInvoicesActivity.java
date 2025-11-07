package com.sandhyasofttech.gstbillingpro.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sandhyasofttech.gstbillingpro.Adapter.AllInvoicesAdapter;
import com.sandhyasofttech.gstbillingpro.MainActivity;
import com.sandhyasofttech.gstbillingpro.Model.RecentInvoiceItem;
import com.sandhyasofttech.gstbillingpro.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

public class AllInvoicesActivity extends AppCompatActivity {

    private RecyclerView rvAllInvoices;
    private SearchView etSearchInvoice;
    private DatabaseReference userRef;
    private ArrayList<RecentInvoiceItem> fullList = new ArrayList<>();
    private ArrayList<RecentInvoiceItem> filteredList = new ArrayList<>();
    private AllInvoicesAdapter adapter;
    private String userMobile = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_invoices);

        Toolbar toolbar = findViewById(R.id.toolbarAllInvoices);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> {
            Intent intent = new Intent(AllInvoicesActivity.this, MainActivity.class);
            intent.putExtra("openFragment", "HomeFragment");
            startActivity(intent);
            finish();
        });

        rvAllInvoices = findViewById(R.id.rvAllInvoices);
        etSearchInvoice = findViewById(R.id.etSearchInvoice);

        userMobile = getSharedPreferences("APP_PREFS", MODE_PRIVATE)
                .getString("USER_MOBILE", null);

        if (userMobile == null) {
            Toast.makeText(this, "Session expired. Please log in again.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        userRef = FirebaseDatabase.getInstance().getReference("users").child(userMobile);

        rvAllInvoices.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AllInvoicesAdapter(filteredList, userRef);
        rvAllInvoices.setAdapter(adapter);

        loadAllInvoices();

        etSearchInvoice.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterInvoices(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterInvoices(newText);
                return false;
            }
        });

        // Set both item click and edit click listener
        adapter.setOnItemClickListener(invoiceNumber -> showInvoiceDetailsPopup(invoiceNumber));
        adapter.setOnEditClickListener(this::showEditInvoicePopup);
    }

    private void loadAllInvoices() {
        userRef.child("invoices").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                fullList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String invoiceNo = ds.child("invoiceNumber").getValue(String.class);
                    String customerId = ds.child("customerId").getValue(String.class);
                    String customerName = ds.child("customerName").getValue(String.class);
                    Double grandTotal = ds.child("grandTotal").getValue(Double.class);
                    String date = ds.child("invoiceDate").getValue(String.class);

                    if (invoiceNo != null && customerName != null && grandTotal != null && date != null) {
                        fullList.add(new RecentInvoiceItem(invoiceNo, customerId, customerName, grandTotal, date));
                    }
                }
                Collections.reverse(fullList);
                filteredList.clear();
                filteredList.addAll(fullList);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AllInvoicesActivity.this, "Failed to load invoices", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterInvoices(String query) {
        filteredList.clear();
        if (query == null || query.isEmpty()) {
            filteredList.addAll(fullList);
        } else {
            String lowerQuery = query.toLowerCase(Locale.ROOT);
            for (RecentInvoiceItem item : fullList) {
                if (item.customerName.toLowerCase(Locale.ROOT).contains(lowerQuery) ||
                        item.invoiceNo.toLowerCase(Locale.ROOT).contains(lowerQuery)) {
                    filteredList.add(item);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void showInvoiceDetailsPopup(String invoiceNumber) {
        DatabaseReference invoiceRef = userRef.child("invoices").child(invoiceNumber);

        invoiceRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(AllInvoicesActivity.this, "Invoice data not found", Toast.LENGTH_SHORT).show();
                    return;
                }

                View dialogView = LayoutInflater.from(AllInvoicesActivity.this).inflate(R.layout.dialog_invoice_details, null);

                TextView tvInvoiceNumber = dialogView.findViewById(R.id.tvInvoiceNumber);
                TextView tvInvoiceDate = dialogView.findViewById(R.id.tvInvoiceDate);
                TextView tvCustomerId = dialogView.findViewById(R.id.tvCustomerId);
                TextView tvCustomerName = dialogView.findViewById(R.id.tvCustomerName);
                LinearLayout containerInvoiceItems = dialogView.findViewById(R.id.containerInvoiceItems);
                TextView tvTotalTaxableValue = dialogView.findViewById(R.id.tvTotalTaxableValue);
                TextView tvTotalCGST = dialogView.findViewById(R.id.tvTotalCGST);
                TextView tvTotalSGST = dialogView.findViewById(R.id.tvTotalSGST);
                TextView tvTotalIGST = dialogView.findViewById(R.id.tvTotalIGST);
                TextView tvGrandTotal = dialogView.findViewById(R.id.tvGrandTotal);

                tvInvoiceNumber.setText("Invoice Number: " + snapshot.child("invoiceNumber").getValue(String.class));
                tvInvoiceDate.setText("Invoice Date: " + snapshot.child("invoiceDate").getValue(String.class));
                tvCustomerId.setText("Customer ID: " + snapshot.child("customerId").getValue(String.class));
                tvCustomerName.setText("Customer Name: " + snapshot.child("customerName").getValue(String.class));

                containerInvoiceItems.removeAllViews();

                for (DataSnapshot itemSnap : snapshot.child("items").getChildren()) {
                    View itemRow = LayoutInflater.from(AllInvoicesActivity.this)
                            .inflate(R.layout.item_billing_invoice, containerInvoiceItems, false);

                    TextView tvProductName = itemRow.findViewById(R.id.tvProductName);
                    TextView tvQuantity = itemRow.findViewById(R.id.tvQuantity);
                    TextView tvRate = itemRow.findViewById(R.id.tvRate);
                    TextView tvTaxPercent = itemRow.findViewById(R.id.tvTaxPercent);
                    TextView tvTaxableValue = itemRow.findViewById(R.id.tvTaxableValue);
                    TextView tvTaxAmount = itemRow.findViewById(R.id.tvTaxAmount);

                    String productName = itemSnap.child("productName").getValue(String.class);
                    Double quantity = itemSnap.child("quantity").getValue(Double.class);
                    Double rate = itemSnap.child("rate").getValue(Double.class);
                    Double taxPercent = itemSnap.child("taxPercent").getValue(Double.class);

                    double taxableValue = (quantity != null && rate != null) ? quantity * rate : 0;
                    double taxAmount = taxableValue * ((taxPercent != null) ? taxPercent : 0) / 100;

                    tvProductName.setText(productName);
                    tvQuantity.setText(quantity != null ? String.format(Locale.getDefault(), "%.2f", quantity) : "0");
                    tvRate.setText(rate != null ? String.format(Locale.getDefault(), "₹%.2f", rate) : "₹0.00");
                    tvTaxPercent.setText(taxPercent != null ? String.format(Locale.getDefault(), "%.1f%%", taxPercent) : "0%");
                    tvTaxableValue.setText(String.format(Locale.getDefault(), "₹%.2f", taxableValue));
                    tvTaxAmount.setText(String.format(Locale.getDefault(), "₹%.2f", taxAmount));

                    containerInvoiceItems.addView(itemRow);
                }

                Double totalTaxableValue = snapshot.child("totalTaxableValue").getValue(Double.class);
                Double totalCGST = snapshot.child("totalCGST").getValue(Double.class);
                Double totalSGST = snapshot.child("totalSGST").getValue(Double.class);
                Double totalIGST = snapshot.child("totalIGST").getValue(Double.class);
                Double grandTotal = snapshot.child("grandTotal").getValue(Double.class);

                tvTotalTaxableValue.setText(String.format(Locale.getDefault(), "Taxable Value: ₹%.2f", totalTaxableValue != null ? totalTaxableValue : 0));
                tvTotalCGST.setText(String.format(Locale.getDefault(), "CGST: ₹%.2f", totalCGST != null ? totalCGST : 0));
                tvTotalSGST.setText(String.format(Locale.getDefault(), "SGST: ₹%.2f", totalSGST != null ? totalSGST : 0));
                tvTotalIGST.setText(String.format(Locale.getDefault(), "IGST: ₹%.2f", totalIGST != null ? totalIGST : 0));
                tvGrandTotal.setText(String.format(Locale.getDefault(), "Grand Total: ₹%.2f", grandTotal != null ? grandTotal : 0));

                new AlertDialog.Builder(AllInvoicesActivity.this)
                        .setView(dialogView)
                        .setPositiveButton("Close", null)
                        .show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AllInvoicesActivity.this, "Failed to load invoice details", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showEditInvoicePopup(String invoiceNumber) {
        DatabaseReference invoiceRef = userRef.child("invoices").child(invoiceNumber);
        invoiceRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(AllInvoicesActivity.this, "Invoice data not found", Toast.LENGTH_SHORT).show();
                    return;
                }

                View dialogView = LayoutInflater.from(AllInvoicesActivity.this).inflate(R.layout.dialog_edit_invoice, null);

                EditText etCustomerName = dialogView.findViewById(R.id.etCustomerName);
                EditText etInvoiceDate = dialogView.findViewById(R.id.etInvoiceDate);
                LinearLayout containerEditableInvoiceItems = dialogView.findViewById(R.id.containerEditableInvoiceItems);
                Button btnSaveEditedInvoice = dialogView.findViewById(R.id.btnSaveEditedInvoice);

                String customerName = snapshot.child("customerName").getValue(String.class);
                String invoiceDate = snapshot.child("invoiceDate").getValue(String.class);

                etCustomerName.setText(customerName != null ? customerName : "");
                etInvoiceDate.setText(invoiceDate != null ? invoiceDate : "");

                containerEditableInvoiceItems.removeAllViews();

                for (DataSnapshot itemSnap : snapshot.child("items").getChildren()) {
                    View itemRow = LayoutInflater.from(AllInvoicesActivity.this)
                            .inflate(R.layout.item_edit_invoice_row, containerEditableInvoiceItems, false);

                    TextView tvProductName = itemRow.findViewById(R.id.tvProductName);
                    EditText etQuantity = itemRow.findViewById(R.id.etQuantity);
                    TextView tvRate = itemRow.findViewById(R.id.tvRate);
                    TextView tvTaxPercent = itemRow.findViewById(R.id.tvTaxPercent);

                    String productName = itemSnap.child("productName").getValue(String.class);
                    Double quantity = itemSnap.child("quantity").getValue(Double.class);
                    Double rate = itemSnap.child("rate").getValue(Double.class);
                    Double taxPercent = itemSnap.child("taxPercent").getValue(Double.class);

                    tvProductName.setText(productName != null ? productName : "Unknown");
                    etQuantity.setText(quantity != null ? String.valueOf(quantity) : "0");
                    tvRate.setText(rate != null ? String.format(Locale.getDefault(), "₹%.2f", rate) : "₹0.00");
                    tvTaxPercent.setText(taxPercent != null ? String.format(Locale.getDefault(), "%.1f%%", taxPercent) : "0%");

                    etQuantity.setTag(itemSnap.child("productId").getValue(String.class));

                    containerEditableInvoiceItems.addView(itemRow);
                }

                AlertDialog dialog = new AlertDialog.Builder(AllInvoicesActivity.this)
                        .setView(dialogView)
                        .setCancelable(true)
                        .create();

                btnSaveEditedInvoice.setOnClickListener(v -> {
                    String newCustomerName = etCustomerName.getText().toString().trim();
                    String newInvoiceDate = etInvoiceDate.getText().toString().trim();

                    if (newCustomerName.isEmpty()) {
                        etCustomerName.setError("Enter customer name");
                        return;
                    }
                    if (newInvoiceDate.isEmpty()) {
                        etInvoiceDate.setError("Enter date");
                        return;
                    }

                    snapshot.getRef().child("customerName").setValue(newCustomerName);
                    snapshot.getRef().child("invoiceDate").setValue(newInvoiceDate);

                    for (int i = 0; i < containerEditableInvoiceItems.getChildCount(); i++) {
                        View itemRow = containerEditableInvoiceItems.getChildAt(i);
                        EditText etQty = itemRow.findViewById(R.id.etQuantity);
                        String productId = (String) etQty.getTag();

                        String qtyStr = etQty.getText().toString().trim();
                        double qty = qtyStr.isEmpty() ? 0 : Double.parseDouble(qtyStr);

                        snapshot.getRef().child("items").child(productId).child("quantity").setValue(qty);
                    }

                    Toast.makeText(AllInvoicesActivity.this, "Invoice updated", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    loadAllInvoices();
                });

                dialog.show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AllInvoicesActivity.this, "Failed to load invoice for edit", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
