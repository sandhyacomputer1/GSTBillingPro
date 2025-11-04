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
import com.sandhyasofttech.gstbillingpro.Adapter.RecentInvoiceAdapter;
import com.sandhyasofttech.gstbillingpro.MainActivity;
import com.sandhyasofttech.gstbillingpro.Model.RecentInvoiceItem;
import com.sandhyasofttech.gstbillingpro.R;


import java.util.ArrayList;
import java.util.Collections;

public class HomeFragment extends Fragment {

    private TextView tvTodaysSales, tvMonthSales, tvOutstanding, tvGstDue;
    private MaterialButton btnNewInvoice, btnAddCustomer, btnShareExport, btnViewAllInvoices;
    private RecyclerView rvRecentActivity;
    private TextView tvLowStock, tvPaymentDue, tvSystemUpdates;

    private String userMobile;
    private DatabaseReference userRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 🔹 Initialize UI components
        tvTodaysSales = view.findViewById(R.id.tvTodaysSales);
        tvMonthSales = view.findViewById(R.id.tvMonthSales);
        tvOutstanding = view.findViewById(R.id.tvOutstanding);
        tvGstDue = view.findViewById(R.id.tvGstDue);
        btnNewInvoice = view.findViewById(R.id.btnNewInvoice);
        btnAddCustomer = view.findViewById(R.id.btnAddCustomer);
        btnShareExport = view.findViewById(R.id.btnShareExport);
        btnViewAllInvoices = view.findViewById(R.id.btnViewAllInvoices);
        rvRecentActivity = view.findViewById(R.id.rvRecentActivity);
        tvLowStock = view.findViewById(R.id.tvLowStock);
        tvPaymentDue = view.findViewById(R.id.tvPaymentDue);
        tvSystemUpdates = view.findViewById(R.id.tvSystemUpdates);

        // 🔹 Get current user mobile from SharedPreferences
        SharedPreferences prefs = requireActivity().getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE);
        userMobile = prefs.getString("USER_MOBILE", null);
        if (userMobile == null) {
            Toast.makeText(getContext(), "Session expired. Please log in again.", Toast.LENGTH_LONG).show();
            return;
        }

        userRef = FirebaseDatabase.getInstance().getReference("users").child(userMobile);

        // 🔹 Load data
        loadQuickStats();
        loadRecentInvoices();
        loadAlerts();

        // 🔹 Button actions
        btnNewInvoice.setOnClickListener(v -> {
            ((MainActivity) requireActivity()).syncNavigationSelection(R.id.nav_invoice);
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                    .replace(R.id.fragment_container, new InvoiceBillingFragment())
                    .addToBackStack(null)
                    .commit();
        });

        btnAddCustomer.setOnClickListener(v -> {
            ((MainActivity) requireActivity()).syncNavigationSelection(R.id.nav_customer);
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                    .replace(R.id.fragment_container, new CustomerFragment())
                    .addToBackStack(null)
                    .commit();
        });

        btnShareExport.setOnClickListener(v ->
                Toast.makeText(getContext(), "Share/Export feature coming soon!", Toast.LENGTH_SHORT).show());

        // ✅ View All button → open AllInvoicesActivity
        btnViewAllInvoices.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), AllInvoicesActivity.class);
            startActivity(intent);
        });
    }

    // 🔹 Fetch summary values
    private void loadQuickStats() {
        DatabaseReference statsRef = userRef.child("stats");
        statsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long todaysSales = snapshot.child("todaysSales").getValue(Long.class) != null
                        ? snapshot.child("todaysSales").getValue(Long.class) : 0L;
                long monthSales = snapshot.child("monthSales").getValue(Long.class) != null
                        ? snapshot.child("monthSales").getValue(Long.class) : 0L;
                long outstanding = snapshot.child("outstanding").getValue(Long.class) != null
                        ? snapshot.child("outstanding").getValue(Long.class) : 0L;
                long gstDue = snapshot.child("gstDue").getValue(Long.class) != null
                        ? snapshot.child("gstDue").getValue(Long.class) : 0L;

                tvTodaysSales.setText(formatCurrency(todaysSales));
                tvMonthSales.setText(formatCurrency(monthSales));
                tvOutstanding.setText(formatCurrency(outstanding));
                tvGstDue.setText(formatCurrency(gstDue));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Could not load quick stats", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 🔹 Fetch recent 10 invoices
    private void loadRecentInvoices() {
        DatabaseReference invoicesRef = userRef.child("invoices");

        invoicesRef.limitToLast(10).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<RecentInvoiceItem> invoiceList = new ArrayList<>();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    String invoiceNo = ds.child("invoiceNumber").getValue(String.class);
                    String customerId = ds.child("customerId").getValue(String.class);
                    String customerName = ds.child("customerName").getValue(String.class);
                    Double grandTotal = ds.child("grandTotal").getValue(Double.class);
                    String date = ds.child("invoiceDate").getValue(String.class);

                    if (invoiceNo != null && customerId != null && customerName != null && grandTotal != null && date != null) {
                        invoiceList.add(new RecentInvoiceItem(invoiceNo, customerId, customerName, grandTotal, date));
                    }
                }

                Collections.reverse(invoiceList); // Show newest first
                rvRecentActivity.setLayoutManager(new LinearLayoutManager(getContext()));
                rvRecentActivity.setAdapter(new RecentInvoiceAdapter(invoiceList));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Could not load recent invoices", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 🔹 Fetch system alerts
    private void loadAlerts() {
        DatabaseReference alertsRef = userRef.child("alerts");
        alertsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String lowStock = snapshot.child("lowStock").getValue(String.class);
                String paymentDue = snapshot.child("paymentDue").getValue(String.class);
                String systemUpdates = snapshot.child("systemUpdates").getValue(String.class);

                tvLowStock.setText("Low stock: " + (lowStock == null ? "None" : lowStock));
                tvPaymentDue.setText("Payment Due: " + (paymentDue == null ? "None" : paymentDue));
                tvSystemUpdates.setText("System Updates: " + (systemUpdates == null ? "No new updates" : systemUpdates));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Could not load alerts", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String formatCurrency(long amount) {
        return "₹" + String.format("%,d", amount);
    }
}
