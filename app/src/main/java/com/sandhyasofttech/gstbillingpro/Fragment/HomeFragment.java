package com.sandhyasofttech.gstbillingpro.Fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sandhyasofttech.gstbillingpro.Activity.AllInvoicesActivity;
import com.sandhyasofttech.gstbillingpro.Activity.ShareExportActivity;
import com.sandhyasofttech.gstbillingpro.Adapter.RecentInvoiceAdapter;
import com.sandhyasofttech.gstbillingpro.MainActivity;
import com.sandhyasofttech.gstbillingpro.Model.RecentInvoiceItem;
import com.sandhyasofttech.gstbillingpro.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class HomeFragment extends Fragment {

    // UI
    private TextView tvTodaysSales, tvMonthSales;
    private MaterialButton btnNewInvoice, btnAddCustomer, btnShareExport, btnViewAllInvoices;
    private RecyclerView rvRecentActivity;

    // Business Summary
    private TextView tvTotalCustomers, tvTotalProducts, tvLastBackup;

    private String userMobile;
    private DatabaseReference userRef, productsRef, invoicesRef;

    // For dynamic calculation
    private final SimpleDateFormat dateFmt = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    private final SimpleDateFormat monthFmt = new SimpleDateFormat("yyyy-MM", Locale.US);
    private final Set<String> uniqueProductIds = new HashSet<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize UI
        tvTodaysSales = view.findViewById(R.id.tvTodaysSales);
        tvMonthSales = view.findViewById(R.id.tvMonthSales);
        btnNewInvoice = view.findViewById(R.id.btnNewInvoice);
        btnAddCustomer = view.findViewById(R.id.btnAddCustomer);
        btnShareExport = view.findViewById(R.id.btnShareExport);
        btnViewAllInvoices = view.findViewById(R.id.btnViewAllInvoices);
        rvRecentActivity = view.findViewById(R.id.rvRecentActivity);

        tvTotalCustomers = view.findViewById(R.id.tvTotalCustomers);
        tvTotalProducts = view.findViewById(R.id.tvTotalProducts); // Now shows invoice count
        tvLastBackup = view.findViewById(R.id.tvLastBackup);

        // Get user mobile
        SharedPreferences prefs = requireActivity().getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE);
        userMobile = prefs.getString("USER_MOBILE", null);
        if (userMobile == null) {
            Toast.makeText(getContext(), "Session expired. Please log in again.", Toast.LENGTH_LONG).show();
            return;
        }

        // Firebase refs
        userRef = FirebaseDatabase.getInstance().getReference("users").child(userMobile);
        productsRef = FirebaseDatabase.getInstance().getReference("products");
        invoicesRef = userRef.child("invoices");

        // Load all data
        loadDynamicSalesAndProducts();   // <-- Still calculates sales + unique products (used for sales logic)
        loadRecentInvoices();
        listenToCustomerCount();
        listenToInvoiceCount();          // <-- NEW: Replaces product count
        loadLastBackup();

        // Button Actions
        btnNewInvoice.setOnClickListener(v -> {
            ((MainActivity) requireActivity()).syncNavigation(R.id.nav_invoice);
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                    .replace(R.id.fragment_container, new InvoiceBillingFragment())
                    .addToBackStack(null)
                    .commit();
        });

        btnAddCustomer.setOnClickListener(v -> {
            ((MainActivity) requireActivity()).syncNavigation(R.id.nav_customer);
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                    .replace(R.id.fragment_container, new CustomerFragment())
                    .addToBackStack(null)
                    .commit();
        });

        btnShareExport.setOnClickListener(v -> startActivity(new Intent(getContext(), ShareExportActivity.class)));
        btnViewAllInvoices.setOnClickListener(v -> startActivity(new Intent(getContext(), AllInvoicesActivity.class)));
    }

    // NEW: Dynamic Sales + Unique Products (Live)
    private void loadDynamicSalesAndProducts() {
        String today = dateFmt.format(new Date());
        String currentMonth = monthFmt.format(new Date());

        invoicesRef.addValueEventListener(new ValueEventListener() {
            double todaySale = 0.0;
            double monthSale = 0.0;

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                todaySale = 0.0;
                monthSale = 0.0;
                uniqueProductIds.clear();

                for (DataSnapshot invSnap : snapshot.getChildren()) {
                    String invDate = invSnap.child("invoiceDate").getValue(String.class);
                    Double grandTotal = invSnap.child("grandTotal").getValue(Double.class);

                    if (invDate == null || grandTotal == null) continue;

                    // Sales
                    if (invDate.equals(today)) {
                        todaySale += grandTotal;
                    }
                    if (invDate.startsWith(currentMonth)) {
                        monthSale += grandTotal;
                    }

                    // Unique Products (still used for internal logic if needed)
                    DataSnapshot items = invSnap.child("items");
                    for (DataSnapshot item : items.getChildren()) {
                        String productId = item.child("productId").getValue(String.class);
                        if (productId != null && !productId.isEmpty()) {
                            uniqueProductIds.add(productId);
                        }
                    }
                }

                // Update UI
                tvTodaysSales.setText(formatCurrency((long) todaySale));
                tvMonthSales.setText(formatCurrency((long) monthSale));
                // tvTotalProducts is now handled by listenToInvoiceCount()
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Sales Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Recent Invoices (Last 10)
    private void loadRecentInvoices() {
        invoicesRef.limitToLast(10).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<RecentInvoiceItem> list = new ArrayList<>();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String invoiceNo = ds.child("invoiceNumber").getValue(String.class);
                    String customerName = ds.child("customerName").getValue(String.class);
                    Double grandTotal = ds.child("grandTotal").getValue(Double.class);
                    String date = ds.child("invoiceDate").getValue(String.class);

                    if (invoiceNo != null && customerName != null && grandTotal != null && date != null) {
                        list.add(new RecentInvoiceItem(invoiceNo, null, customerName, grandTotal, date));
                    }
                }
                Collections.reverse(list);
                rvRecentActivity.setLayoutManager(new LinearLayoutManager(getContext()));
                rvRecentActivity.setAdapter(new RecentInvoiceAdapter(list));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load recent invoices", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // REAL-TIME CUSTOMER COUNT
    private void listenToCustomerCount() {
        userRef.child("customers").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long count = snapshot.getChildrenCount();
                tvTotalCustomers.setText(" " + count);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                tvTotalCustomers.setText("Total Customers : Error");
            }
        });
    }

    // NEW: REAL-TIME INVOICE COUNT (Replaces product count)
    private void listenToInvoiceCount() {
        invoicesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long count = snapshot.getChildrenCount();
                tvTotalProducts.setText(" " + count); // Now shows invoice count
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                tvTotalProducts.setText("Total Invoices : Error");
            }
        });
    }

    // LAST BACKUP
    private void loadLastBackup() {
        userRef.child("summary").child("lastBackup").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Long timestamp = snapshot.getValue(Long.class);
                String text = (timestamp == null || timestamp == 0)
                        ? "Last Backup: Never"
                        : "Last Backup: " + formatBackupTime(timestamp);
                tvLastBackup.setText(text);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                tvLastBackup.setText("Last Backup: Error");
            }
        });
    }

    // Format backup time
    private String formatBackupTime(long timestamp) {
        long now = System.currentTimeMillis();
        long diff = now - timestamp;

        long minute = 60 * 1000;
        long hour = 60 * minute;
        long day = 24 * hour;

        if (diff < hour) {
            long mins = diff / minute;
            return mins <= 1 ? "Just now" : mins + " mins ago";
        } else if (diff < day) {
            long hours = diff / hour;
            return hours == 1 ? "1 hour ago" : hours + " hours ago";
        } else if (diff < 2 * day) {
            return "Yesterday";
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
            return sdf.format(new Date(timestamp));
        }
    }

    // Format currency
    private String formatCurrency(long amount) {
        return "₹" + String.format("%,d", amount);
    }
}