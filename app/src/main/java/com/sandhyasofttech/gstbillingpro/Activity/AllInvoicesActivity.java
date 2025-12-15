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

        userRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userMobile);

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

        /* 🔥 UPDATED CLICK HANDLING */

        // Open Invoice Details Activity (NO POPUP)
        adapter.setOnItemClickListener(invoiceNumber -> {
            Intent intent = new Intent(AllInvoicesActivity.this, InvDetailsActivity.class);
            intent.putExtra("invoiceNumber", invoiceNumber);
            startActivity(intent);
        });

        // Edit popup remains same
        adapter.setOnEditClickListener(this::showEditInvoicePopup);
    }

    private void loadAllInvoices() {
        userRef.child("invoices")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        fullList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {

                            String invoiceNo = ds.child("invoiceNumber").getValue(String.class);
                            String customerId = ds.child("customerId").getValue(String.class);
                            String customerName = ds.child("customerName").getValue(String.class);
                            Double grandTotal = ds.child("grandTotal").getValue(Double.class);
                            String date = ds.child("invoiceDate").getValue(String.class);

                            if (invoiceNo != null && customerName != null
                                    && grandTotal != null && date != null) {

                                fullList.add(new RecentInvoiceItem(
                                        invoiceNo,
                                        customerId,
                                        customerName,
                                        grandTotal,
                                        date
                                ));
                            }
                        }

                        Collections.reverse(fullList);
                        filteredList.clear();
                        filteredList.addAll(fullList);
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(AllInvoicesActivity.this,
                                "Failed to load invoices",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void filterInvoices(String query) {
        filteredList.clear();

        if (query == null || query.trim().isEmpty()) {
            filteredList.addAll(fullList);
        } else {
            String lowerQuery = query.toLowerCase(Locale.ROOT);

            for (RecentInvoiceItem item : fullList) {
                if (item.customerName.toLowerCase(Locale.ROOT).contains(lowerQuery)
                        || item.invoiceNo.toLowerCase(Locale.ROOT).contains(lowerQuery)) {
                    filteredList.add(item);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    /* ================= EDIT INVOICE POPUP (UNCHANGED) ================= */

    private void showEditInvoicePopup(String invoiceNumber) {
        DatabaseReference invoiceRef = userRef.child("invoices").child(invoiceNumber);

        invoiceRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (!snapshot.exists()) {
                    Toast.makeText(AllInvoicesActivity.this,
                            "Invoice data not found",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                View dialogView = LayoutInflater.from(AllInvoicesActivity.this)
                        .inflate(R.layout.dialog_edit_invoice, null);

                EditText etCustomerName = dialogView.findViewById(R.id.etCustomerName);
                EditText etInvoiceDate = dialogView.findViewById(R.id.etInvoiceDate);
                LinearLayout containerEditableInvoiceItems =
                        dialogView.findViewById(R.id.containerEditableInvoiceItems);
                Button btnSaveEditedInvoice =
                        dialogView.findViewById(R.id.btnSaveEditedInvoice);

                etCustomerName.setText(snapshot.child("customerName")
                        .getValue(String.class));
                etInvoiceDate.setText(snapshot.child("invoiceDate")
                        .getValue(String.class));

                containerEditableInvoiceItems.removeAllViews();

                for (DataSnapshot itemSnap : snapshot.child("items").getChildren()) {

                    View itemRow = LayoutInflater.from(AllInvoicesActivity.this)
                            .inflate(R.layout.item_edit_invoice_row,
                                    containerEditableInvoiceItems, false);

                    TextView tvProductName = itemRow.findViewById(R.id.tvProductName);
                    EditText etQuantity = itemRow.findViewById(R.id.etQuantity);
                    TextView tvRate = itemRow.findViewById(R.id.tvRate);
                    TextView tvTaxPercent = itemRow.findViewById(R.id.tvTaxPercent);

                    tvProductName.setText(itemSnap.child("productName")
                            .getValue(String.class));
                    etQuantity.setText(String.valueOf(
                            itemSnap.child("quantity").getValue(Double.class)));
                    tvRate.setText("₹" + itemSnap.child("rate").getValue(Double.class));
                    tvTaxPercent.setText(
                            itemSnap.child("taxPercent").getValue(Double.class) + "%");

                    etQuantity.setTag(itemSnap.child("productId").getValue(String.class));
                    containerEditableInvoiceItems.addView(itemRow);
                }

                AlertDialog dialog = new AlertDialog.Builder(AllInvoicesActivity.this)
                        .setView(dialogView)
                        .setCancelable(true)
                        .create();

                btnSaveEditedInvoice.setOnClickListener(v -> {

                    snapshot.getRef().child("customerName")
                            .setValue(etCustomerName.getText().toString().trim());
                    snapshot.getRef().child("invoiceDate")
                            .setValue(etInvoiceDate.getText().toString().trim());

                    for (int i = 0; i < containerEditableInvoiceItems.getChildCount(); i++) {
                        View row = containerEditableInvoiceItems.getChildAt(i);
                        EditText etQty = row.findViewById(R.id.etQuantity);
                        String productId = (String) etQty.getTag();
                        double qty = Double.parseDouble(etQty.getText().toString());

                        snapshot.getRef()
                                .child("items")
                                .child(productId)
                                .child("quantity")
                                .setValue(qty);
                    }

                    Toast.makeText(AllInvoicesActivity.this,
                            "Invoice updated",
                            Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    loadAllInvoices();
                });

                dialog.show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AllInvoicesActivity.this,
                        "Failed to load invoice",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
