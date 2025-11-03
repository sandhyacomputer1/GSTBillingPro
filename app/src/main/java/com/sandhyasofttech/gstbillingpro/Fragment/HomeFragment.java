package com.sandhyasofttech.gstbillingpro.Fragment;


import android.content.Context;
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
import com.sandhyasofttech.gstbillingpro.R;

import java.util.ArrayList;

    public class HomeFragment extends Fragment {

        private TextView tvTodaysSales, tvMonthSales, tvOutstanding, tvGstDue;
        private MaterialButton btnNewInvoice, btnAddCustomer, btnShareExport;
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

            // Bind UI elements
            tvTodaysSales = view.findViewById(R.id.tvTodaysSales);
            tvMonthSales = view.findViewById(R.id.tvMonthSales);
            tvOutstanding = view.findViewById(R.id.tvOutstanding);
            tvGstDue = view.findViewById(R.id.tvGstDue);

            btnNewInvoice = view.findViewById(R.id.btnNewInvoice);
            btnAddCustomer = view.findViewById(R.id.btnAddCustomer);
            btnShareExport = view.findViewById(R.id.btnShareExport);

            rvRecentActivity = view.findViewById(R.id.rvRecentActivity);

            tvLowStock = view.findViewById(R.id.tvLowStock);
            tvPaymentDue = view.findViewById(R.id.tvPaymentDue);
            tvSystemUpdates = view.findViewById(R.id.tvSystemUpdates);

            // Get logged-in user mobile from SharedPreferences
            SharedPreferences prefs = requireActivity().getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE);
            userMobile = prefs.getString("USER_MOBILE", null);
            if (userMobile == null) {
                Toast.makeText(getContext(), "Session expired. Please log in again.", Toast.LENGTH_LONG).show();
                // TODO: Redirect to login
                return;
            }

            // Firebase user reference
            userRef = FirebaseDatabase.getInstance().getReference("users").child(userMobile);

            // Load data from Firebase
            loadQuickStats();
            loadRecentActivity();
            loadAlerts();

            // Button click listeners
            btnNewInvoice.setOnClickListener(v -> {
                // TODO: Navigate to new invoice screen
            });

            btnAddCustomer.setOnClickListener(v -> {
                // TODO: Navigate to add customer screen
            });

            btnShareExport.setOnClickListener(v -> {
                // TODO: Launch share/export functionality
            });
        }

        private void loadQuickStats() {
            DatabaseReference statsRef = userRef.child("stats");
            statsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    long todaysSales = snapshot.child("todaysSales").getValue(Long.class) != null ? snapshot.child("todaysSales").getValue(Long.class) : 0L;
                    long monthSales = snapshot.child("monthSales").getValue(Long.class) != null ? snapshot.child("monthSales").getValue(Long.class) : 0L;
                    long outstanding = snapshot.child("outstanding").getValue(Long.class) != null ? snapshot.child("outstanding").getValue(Long.class) : 0L;
                    long gstDue = snapshot.child("gstDue").getValue(Long.class) != null ? snapshot.child("gstDue").getValue(Long.class) : 0L;

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

        private void loadRecentActivity() {
            DatabaseReference activityRef = userRef.child("activities");
            activityRef.limitToLast(5).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    ArrayList<String> activities = new ArrayList<>();
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        String activity = ds.getValue(String.class);
                        if (activity != null) activities.add(activity);
                    }
                    setupRecentActivityRecyclerView(activities);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getContext(), "Could not load recent activities", Toast.LENGTH_SHORT).show();
                }
            });
        }

        private void setupRecentActivityRecyclerView(ArrayList<String> activities) {
            rvRecentActivity.setLayoutManager(new LinearLayoutManager(getContext()));
            rvRecentActivity.setAdapter(new RecentActivityAdapter(activities));
        }

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

        // Currency formatting helper
        private String formatCurrency(long amount) {
            return "₹" + String.format("%,d", amount);
        }

        // RecyclerView Adapter for Recent Activity
        private static class RecentActivityAdapter extends RecyclerView.Adapter<RecentActivityAdapter.ViewHolder> {

            private final ArrayList<String> activities;

            public RecentActivityAdapter(ArrayList<String> activities) {
                this.activities = activities;
            }

            @NonNull
            @Override
            public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(android.R.layout.simple_list_item_1, parent, false);
                return new ViewHolder(view);
            }

            @Override
            public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
                holder.tvActivity.setText(activities.get(position));
            }

            @Override
            public int getItemCount() {
                return activities.size();
            }

            static class ViewHolder extends RecyclerView.ViewHolder {
                TextView tvActivity;

                public ViewHolder(@NonNull View itemView) {
                    super(itemView);
                    tvActivity = itemView.findViewById(android.R.id.text1);
                }
            }
        }
    }

