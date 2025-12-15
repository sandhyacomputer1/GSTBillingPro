package com.sandhyasofttech.gstbillingpro.Activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.sandhyasofttech.gstbillingpro.Adapter.PendingPaymentsAdapter;
import com.sandhyasofttech.gstbillingpro.Model.PendingPayment;
import com.sandhyasofttech.gstbillingpro.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PendingPaymentsActivity extends AppCompatActivity {

    private RecyclerView rvPendingPayments;
    private ProgressBar progressBar;
    private LinearLayout layoutEmpty;
    private TextView tvTotalPending, tvPendingCount;
    private MaterialToolbar toolbar;
    private PendingPaymentsAdapter adapter;

    private List<PendingPayment> pendingPayments = new ArrayList<>();
    private DatabaseReference pendingPaymentsRef, invoicesRef;
    private String userMobile;
    private double totalPendingAmount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pending_payments);

        // Initialize views
        toolbar = findViewById(R.id.toolbar);
        rvPendingPayments = findViewById(R.id.rvPendingPayments);
        progressBar = findViewById(R.id.progressBar);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        tvTotalPending = findViewById(R.id.tvTotalPending);
        tvPendingCount = findViewById(R.id.tvPendingCount);

        // Setup toolbar
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        // Get user mobile
        SharedPreferences prefs = getSharedPreferences("APP_PREFS", MODE_PRIVATE);
        userMobile = prefs.getString("USER_MOBILE", null);

        if (userMobile == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize Firebase
        DatabaseReference usersRef = FirebaseDatabase.getInstance()
                .getReference("users").child(userMobile);
        pendingPaymentsRef = usersRef.child("pendingPayments");
        invoicesRef = usersRef.child("invoices");

        // Setup RecyclerView
        setupRecyclerView();

        // Load pending payments
        loadPendingPayments();

        // Filter button
        MaterialButton btnFilter = findViewById(R.id.btnFilter);
        if (btnFilter != null) {
            btnFilter.setOnClickListener(v -> showFilterDialog());
        }
    }

    private void setupRecyclerView() {
        adapter = new PendingPaymentsAdapter(pendingPayments, this::showPaymentDialog);
        rvPendingPayments.setLayoutManager(new LinearLayoutManager(this));
        rvPendingPayments.setAdapter(adapter);
    }

    private void loadPendingPayments() {
        progressBar.setVisibility(View.VISIBLE);
        layoutEmpty.setVisibility(View.GONE);

        pendingPaymentsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                pendingPayments.clear();
                totalPendingAmount = 0;

                for (DataSnapshot ds : snapshot.getChildren()) {
                    PendingPayment payment = ds.getValue(PendingPayment.class);
                    if (payment != null && payment.pendingAmount > 0) {
                        pendingPayments.add(payment);
                        totalPendingAmount += payment.pendingAmount;
                    }
                }

                adapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
                updateUI();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(PendingPaymentsActivity.this,
                        "Failed to load payments", Toast.LENGTH_SHORT).show();
                updateUI();
            }
        });
    }

    private void updateUI() {
        if (pendingPayments.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            rvPendingPayments.setVisibility(View.GONE);
            tvTotalPending.setText("₹0");
            tvPendingCount.setText("0 Pending Invoices");
        } else {
            layoutEmpty.setVisibility(View.GONE);
            rvPendingPayments.setVisibility(View.VISIBLE);
            tvTotalPending.setText(String.format(Locale.getDefault(),
                    "₹%,.0f", totalPendingAmount));
            tvPendingCount.setText(String.format(Locale.getDefault(),
                    "%d Pending Invoice%s", pendingPayments.size(),
                    pendingPayments.size() == 1 ? "" : "s"));
        }
    }

    private void showPaymentDialog(PendingPayment payment) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_collect_payment, null);

        TextView tvInvoiceNumber = dialogView.findViewById(R.id.tvInvoiceNumber);
        TextView tvCustomerName = dialogView.findViewById(R.id.tvCustomerName);
        TextView tvTotalAmount = dialogView.findViewById(R.id.tvTotalAmount);
        TextView tvPaidAmount = dialogView.findViewById(R.id.tvPaidAmount);
        TextView tvPendingAmount = dialogView.findViewById(R.id.tvPendingAmount);
        com.google.android.material.textfield.TextInputEditText etPaymentAmount =
                dialogView.findViewById(R.id.etPaymentAmount);
        MaterialButton btnCollectFull = dialogView.findViewById(R.id.btnCollectFull);
        MaterialButton btnCollectPartial = dialogView.findViewById(R.id.btnCollectPartial);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btnCancel);

        tvInvoiceNumber.setText("Invoice: " + payment.invoiceNumber);
        tvCustomerName.setText(payment.customerName);
        tvTotalAmount.setText(String.format(Locale.getDefault(), "₹%.2f", payment.totalAmount));
        tvPaidAmount.setText(String.format(Locale.getDefault(), "₹%.2f", payment.paidAmount));
        tvPendingAmount.setText(String.format(Locale.getDefault(), "₹%.2f", payment.pendingAmount));

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create();

        btnCollectFull.setOnClickListener(v -> {
            collectPayment(payment, payment.pendingAmount);
            dialog.dismiss();
        });

        btnCollectPartial.setOnClickListener(v -> {
            String amountStr = etPaymentAmount.getText().toString().trim();
            if (amountStr.isEmpty()) {
                Toast.makeText(this, "Please enter amount", Toast.LENGTH_SHORT).show();
                return;
            }

            double amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                Toast.makeText(this, "Amount must be greater than 0", Toast.LENGTH_SHORT).show();
                return;
            }

            if (amount > payment.pendingAmount) {
                Toast.makeText(this, "Amount exceeds pending amount", Toast.LENGTH_SHORT).show();
                return;
            }

            collectPayment(payment, amount);
            dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void collectPayment(PendingPayment payment, double collectedAmount) {
        double newPaidAmount = payment.paidAmount + collectedAmount;
        double newPendingAmount = payment.pendingAmount - collectedAmount;
        String newStatus = newPendingAmount <= 0 ? "Paid" : "Partial";

        String todayDate = new SimpleDateFormat(
                "yyyy-MM-dd", Locale.getDefault()
        ).format(new Date());

        Map<String, Object> invoiceUpdates = new HashMap<>();
        invoiceUpdates.put("paidAmount", newPaidAmount);
        invoiceUpdates.put("pendingAmount", Math.max(0, newPendingAmount));
        invoiceUpdates.put("paymentStatus", newStatus);
        invoiceUpdates.put("lastPaymentDate", todayDate);

        invoicesRef.child(payment.invoiceNumber)
                .updateChildren(invoiceUpdates)
                .addOnSuccessListener(aVoid -> {
                    addInvoiceHistory(
                            payment.invoiceNumber,
                            collectedAmount,
                            newPaidAmount,
                            Math.max(0, newPendingAmount),
                            "Cash"
                    );

                    if (newPendingAmount <= 0) {
                        pendingPaymentsRef.child(payment.invoiceNumber).removeValue();
                    } else {
                        Map<String, Object> pendingUpdates = new HashMap<>();
                        pendingUpdates.put("paidAmount", newPaidAmount);
                        pendingUpdates.put("pendingAmount", newPendingAmount);
                        pendingUpdates.put("paymentStatus", "Partial");
                        pendingUpdates.put("lastPaymentDate", todayDate);

                        pendingPaymentsRef.child(payment.invoiceNumber)
                                .updateChildren(pendingUpdates);
                    }

                    Toast.makeText(this, "Payment collected successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to save payment", Toast.LENGTH_SHORT).show()
                );
    }

    private void addInvoiceHistory(String invoiceNumber,
                                   double paidNow,
                                   double totalPaid,
                                   double pendingAfter,
                                   String paymentMode) {
        DatabaseReference historyRef = invoicesRef
                .child(invoiceNumber)
                .child("history");

        String historyId = historyRef.push().getKey();
        if (historyId == null) return;

        Map<String, Object> history = new HashMap<>();
        history.put("paidNow", paidNow);
        history.put("totalPaid", totalPaid);
        history.put("pendingAfter", pendingAfter);
        history.put("paymentMode", paymentMode);
        history.put("date", new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));
        history.put("time", new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date()));
        history.put("timestamp", ServerValue.TIMESTAMP);

        historyRef.child(historyId).setValue(history);
    }

    private void showFilterDialog() {
        String[] filterOptions = {"All Payments", "High Amount (>₹5000)", "Low Amount (<₹5000)", "Partial Only", "Sort: Newest First", "Sort: Oldest First", "Sort: Highest Amount", "Sort: Lowest Amount"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Filter & Sort")
                .setItems(filterOptions, (dialog, which) -> {
                    switch (which) {
                        case 0: // All Payments
                            loadPendingPayments();
                            break;
                        case 1: // High Amount
                            filterByAmount(5000, true);
                            break;
                        case 2: // Low Amount
                            filterByAmount(5000, false);
                            break;
                        case 3: // Partial Only
                            filterByStatus("Partial");
                            break;
                        case 4: // Newest First
                            sortPayments(true, false);
                            break;
                        case 5: // Oldest First
                            sortPayments(false, false);
                            break;
                        case 6: // Highest Amount
                            sortPayments(true, true);
                            break;
                        case 7: // Lowest Amount
                            sortPayments(false, true);
                            break;
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void filterByAmount(double threshold, boolean greaterThan) {
        List<PendingPayment> filteredList = new ArrayList<>();
        for (PendingPayment payment : pendingPayments) {
            if (greaterThan) {
                if (payment.pendingAmount > threshold) {
                    filteredList.add(payment);
                }
            } else {
                if (payment.pendingAmount < threshold) {
                    filteredList.add(payment);
                }
            }
        }

        if (filteredList.isEmpty()) {
            Toast.makeText(this, "No payments found for this filter", Toast.LENGTH_SHORT).show();
        } else {
            adapter = new PendingPaymentsAdapter(filteredList, this::showPaymentDialog);
            rvPendingPayments.setAdapter(adapter);
            Toast.makeText(this, filteredList.size() + " payment(s) found", Toast.LENGTH_SHORT).show();
        }
    }

    private void filterByStatus(String status) {
        List<PendingPayment> filteredList = new ArrayList<>();
        for (PendingPayment payment : pendingPayments) {
            if (status.equalsIgnoreCase(payment.paymentStatus)) {
                filteredList.add(payment);
            }
        }

        if (filteredList.isEmpty()) {
            Toast.makeText(this, "No partial payments found", Toast.LENGTH_SHORT).show();
        } else {
            adapter = new PendingPaymentsAdapter(filteredList, this::showPaymentDialog);
            rvPendingPayments.setAdapter(adapter);
            Toast.makeText(this, filteredList.size() + " partial payment(s) found", Toast.LENGTH_SHORT).show();
        }
    }

    private void sortPayments(boolean descending, boolean byAmount) {
        List<PendingPayment> sortedList = new ArrayList<>(pendingPayments);

        if (byAmount) {
            // Sort by pending amount
            sortedList.sort((p1, p2) -> {
                if (descending) {
                    return Double.compare(p2.pendingAmount, p1.pendingAmount);
                } else {
                    return Double.compare(p1.pendingAmount, p2.pendingAmount);
                }
            });
        } else {
            // Sort by timestamp
            sortedList.sort((p1, p2) -> {
                if (descending) {
                    return Long.compare(p2.timestamp, p1.timestamp);
                } else {
                    return Long.compare(p1.timestamp, p2.timestamp);
                }
            });
        }

        adapter = new PendingPaymentsAdapter(sortedList, this::showPaymentDialog);
        rvPendingPayments.setAdapter(adapter);
        Toast.makeText(this, "Sorted successfully", Toast.LENGTH_SHORT).show();
    }
}