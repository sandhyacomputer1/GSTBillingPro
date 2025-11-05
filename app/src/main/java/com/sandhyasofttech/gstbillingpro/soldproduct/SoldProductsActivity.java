package com.sandhyasofttech.gstbillingpro.soldproduct;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.*;
import com.sandhyasofttech.gstbillingpro.R;
import com.sandhyasofttech.gstbillingpro.invoice.Invoice;
import com.sandhyasofttech.gstbillingpro.invoice.InvoiceItem;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SoldProductsActivity extends AppCompatActivity {

    private RecyclerView rvSoldProducts;
    private SoldProductAdapter adapter;
    private List<SoldProductEntry> soldProductList = new ArrayList<>();
    private List<SoldProductEntry> filteredList = new ArrayList<>();
    private SearchView searchView;
    private TextView tvEmpty;
    private TabLayout tabDateFilter;

    private DatabaseReference invoicesRef;
    private String userMobile;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sold_products);

        rvSoldProducts = findViewById(R.id.rvSoldProducts);
        searchView = findViewById(R.id.searchSoldProducts);
        tvEmpty = findViewById(R.id.tvEmpty);
        tabDateFilter = findViewById(R.id.tabDateFilter);

        rvSoldProducts.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SoldProductAdapter(filteredList);
        rvSoldProducts.setAdapter(adapter);

        userMobile = getSharedPreferences("APP_PREFS", MODE_PRIVATE).getString("USER_MOBILE", null);
        if (userMobile == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        invoicesRef = FirebaseDatabase.getInstance().getReference("users").child(userMobile).child("invoices");

        setupTabs();
        loadSoldProducts();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String query) {
                filterSoldProducts(query);
                return true;
            }
            @Override public boolean onQueryTextChange(String newText) {
                filterSoldProducts(newText);
                return true;
            }
        });
    }

    private void setupTabs() {
        tabDateFilter.addTab(tabDateFilter.newTab().setText("Today"));
        tabDateFilter.addTab(tabDateFilter.newTab().setText("Yesterday"));
        tabDateFilter.addTab(tabDateFilter.newTab().setText("All"));
        tabDateFilter.selectTab(tabDateFilter.getTabAt(0)); // default Today

        tabDateFilter.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                filterByTab(tab.getPosition());
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {
                filterByTab(tab.getPosition());
            }
        });
    }

    private void filterByTab(int position) {
        LocalDate today = LocalDate.now();
        LocalDate filterDate = null;
        if (position == 0) filterDate = today;
        else if (position == 1) filterDate = today.minusDays(1);

        filteredList.clear();
        for (SoldProductEntry entry : soldProductList) {
            if (position == 2) {
                // All
                filteredList.add(entry);
            } else if (filterDate != null && entry.isForDate(filterDate)) {
                filteredList.add(entry);
            }
        }
        adapter.updateList(filteredList);
        tvEmpty.setVisibility(filteredList.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void loadSoldProducts() {
        invoicesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                soldProductList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Invoice invoice = ds.getValue(Invoice.class);
                    if (invoice != null && invoice.items != null) {
                        for (InvoiceItem item : invoice.items) {
                            SoldProductEntry entry = new SoldProductEntry(
                                    invoice.invoiceNumber,
                                    invoice.invoiceDate,
                                    invoice.customerName,
                                    item.productName,
                                    item.quantity
                            );
                            soldProductList.add(entry);
                        }
                    }
                }
                // Apply the current tab filter, default is Today
                filterByTab(tabDateFilter.getSelectedTabPosition());
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SoldProductsActivity.this, "Failed to load sold products.", Toast.LENGTH_SHORT).show();
                tvEmpty.setVisibility(View.VISIBLE);
            }
        });
    }

    private void filterSoldProducts(String query) {
        query = query.toLowerCase(Locale.ROOT).trim();
        List<SoldProductEntry> tempList = new ArrayList<>();
        for (SoldProductEntry entry : filteredList) {
            if (entry.productName.toLowerCase(Locale.ROOT).contains(query)
                    || entry.customerName.toLowerCase(Locale.ROOT).contains(query)
                    || entry.invoiceDate.toLowerCase(Locale.ROOT).contains(query)
                    || entry.invoiceNumber.toLowerCase(Locale.ROOT).contains(query)) {
                tempList.add(entry);
            }
        }
        adapter.updateList(tempList);
        tvEmpty.setVisibility(tempList.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
