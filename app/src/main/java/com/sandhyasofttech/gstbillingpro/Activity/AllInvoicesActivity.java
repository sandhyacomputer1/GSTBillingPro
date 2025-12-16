package com.sandhyasofttech.gstbillingpro.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;   // ✅ IMPORTANT
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sandhyasofttech.gstbillingpro.Adapter.AllInvoicesAdapter;
import com.sandhyasofttech.gstbillingpro.Model.RecentInvoiceItem;
import com.sandhyasofttech.gstbillingpro.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

public class AllInvoicesActivity extends AppCompatActivity {

    private RecyclerView rvAllInvoices;
    private SearchView etSearchInvoice;

    private DatabaseReference userRef;
    private final ArrayList<RecentInvoiceItem> fullList = new ArrayList<>();
    private final ArrayList<RecentInvoiceItem> filteredList = new ArrayList<>();

    private AllInvoicesAdapter adapter;
    private String userMobile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_invoices);


        // ================= TOOLBAR =================
        Toolbar toolbar = findViewById(R.id.toolbarAllInvoices);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        // ================= VIEWS =================
        rvAllInvoices = findViewById(R.id.rvAllInvoices);
        etSearchInvoice = findViewById(R.id.etSearchInvoice);

        // ================= SESSION =================
        userMobile = getSharedPreferences("APP_PREFS", MODE_PRIVATE)
                .getString("USER_MOBILE", null);

        if (userMobile == null) {
            Toast.makeText(this, "Session expired. Please login again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // ================= FIREBASE =================
        userRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userMobile);

        // ================= RECYCLER VIEW =================
        rvAllInvoices.setLayoutManager(new LinearLayoutManager(this));

        adapter = new AllInvoicesAdapter(filteredList, invoiceNo -> {
            Intent intent = new Intent(AllInvoicesActivity.this, InvDetailsActivity.class);
            intent.putExtra("invoiceNumber", invoiceNo);
            startActivity(intent);
        });

        rvAllInvoices.setAdapter(adapter);

        // ================= LOAD DATA =================
        loadAllInvoices();

        // ================= SEARCH =================
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
    }

    // ================= LOAD ALL INVOICES =================
    private void loadAllInvoices() {
        userRef.child("invoices")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        fullList.clear();

                        for (DataSnapshot ds : snapshot.getChildren()) {

                            String invoiceNo =
                                    ds.child("invoiceNumber").getValue(String.class);

                            String customerName =
                                    ds.child("customerName").getValue(String.class);

                            String date =
                                    ds.child("invoiceDate").getValue(String.class);

                            Double total =
                                    ds.child("grandTotal").getValue(Double.class);

                            Double pending =
                                    ds.child("pendingAmount").getValue(Double.class);

                            if (invoiceNo != null && customerName != null && total != null) {
                                fullList.add(new RecentInvoiceItem(
                                        invoiceNo,
                                        "",
                                        customerName,
                                        total,
                                        pending == null ? 0 : pending,
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

    // ================= FILTER =================
    private void filterInvoices(String query) {

        filteredList.clear();

        if (query == null || query.trim().isEmpty()) {
            filteredList.addAll(fullList);
        } else {
            String q = query.toLowerCase(Locale.ROOT);
            for (RecentInvoiceItem item : fullList) {
                if (item.invoiceNo.toLowerCase(Locale.ROOT).contains(q)
                        || item.customerName.toLowerCase(Locale.ROOT).contains(q)) {
                    filteredList.add(item);
                }
            }
        }

        adapter.notifyDataSetChanged();
    }
}
