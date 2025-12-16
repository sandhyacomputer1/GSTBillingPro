package com.sandhyasofttech.gstbillingpro.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

public class PendingPaymentsFragment extends Fragment {

    private static final String ARG_USER_MOBILE = "user_mobile";
    
    private RecyclerView rvPendingPayments;
    private ProgressBar progressBar;
    private LinearLayout layoutEmpty;
    private TextView tvTotalPending, tvPendingCount;
    private MaterialButton btnFilter;
    private PendingPaymentsAdapter adapter;

    private List<PendingPayment> pendingPayments = new ArrayList<>();
    private List<PendingPayment> allPayments = new ArrayList<>();
    private DatabaseReference pendingPaymentsRef, invoicesRef, completedPaymentsRef;
    private String userMobile;
    private double totalPendingAmount = 0;

    public static PendingPaymentsFragment newInstance(String userMobile) {
        PendingPaymentsFragment fragment = new PendingPaymentsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_USER_MOBILE, userMobile);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userMobile = getArguments().getString(ARG_USER_MOBILE);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pending_payments, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        rvPendingPayments = view.findViewById(R.id.rvPendingPayments);
        progressBar = view.findViewById(R.id.progressBar);
        layoutEmpty = view.findViewById(R.id.layoutEmpty);
        tvTotalPending = view.findViewById(R.id.tvTotalPending);
        tvPendingCount = view.findViewById(R.id.tvPendingCount);
        btnFilter = view.findViewById(R.id.btnFilter);

        // Initialize Firebase
        DatabaseReference usersRef = FirebaseDatabase.getInstance()
                .getReference("users").child(userMobile);
        pendingPaymentsRef = usersRef.child("pendingPayments");
        invoicesRef = usersRef.child("invoices");
        completedPaymentsRef = usersRef.child("completedPayments");

        // Setup RecyclerView
        setupRecyclerView();

        // Load pending payments
        loadPendingPayments();

        // Filter button
        btnFilter.setOnClickListener(v -> showFilterDialog());
    }

    private void setupRecyclerView() {
        adapter = new PendingPaymentsAdapter(pendingPayments, this::showPaymentDialog);
        rvPendingPayments.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvPendingPayments.setAdapter(adapter);
    }

    private void loadPendingPayments() {
        progressBar.setVisibility(View.VISIBLE);
        layoutEmpty.setVisibility(View.GONE);

        pendingPaymentsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                pendingPayments.clear();
                allPayments.clear();
                totalPendingAmount = 0;

                for (DataSnapshot ds : snapshot.getChildren()) {
                    PendingPayment payment = ds.getValue(PendingPayment.class);
                    if (payment != null && payment.pendingAmount > 0) {
                        pendingPayments.add(payment);
                        allPayments.add(payment);
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
                Toast.makeText(requireContext(), "Failed to load payments", Toast.LENGTH_SHORT).show();
                updateUI();
            }
        });
    }

    private void updateUI() {
        if (pendingPayments.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            rvPendingPayments.setVisibility(View.GONE);
            tvTotalPending.setText("₹0");
            tvPendingCount.setText("0 Pending");
        } else {
            layoutEmpty.setVisibility(View.GONE);
            rvPendingPayments.setVisibility(View.VISIBLE);
            tvTotalPending.setText(String.format(Locale.getDefault(), "₹%,.0f", totalPendingAmount));
            tvPendingCount.setText(String.format(Locale.getDefault(), "%d Pending", pendingPayments.size()));
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

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
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
                Toast.makeText(requireContext(), "Please enter amount", Toast.LENGTH_SHORT).show();
                return;
            }

            double amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                Toast.makeText(requireContext(), "Amount must be greater than 0", Toast.LENGTH_SHORT).show();
                return;
            }

            if (amount > payment.pendingAmount) {
                Toast.makeText(requireContext(), "Amount exceeds pending amount", Toast.LENGTH_SHORT).show();
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
        boolean isFullyPaid = newPendingAmount <= 0;
        String newStatus = isFullyPaid ? "Paid" : "Partial";

        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        Map<String, Object> invoiceUpdates = new HashMap<>();
        invoiceUpdates.put("paidAmount", newPaidAmount);
        invoiceUpdates.put("pendingAmount", Math.max(0, newPendingAmount));
        invoiceUpdates.put("paymentStatus", newStatus);
        invoiceUpdates.put("lastPaymentDate", todayDate);
        invoiceUpdates.put("completionDate", isFullyPaid ? todayDate : null);

        invoicesRef.child(payment.invoiceNumber)
                .updateChildren(invoiceUpdates)
                .addOnSuccessListener(aVoid -> {
                    addInvoiceHistory(payment.invoiceNumber, collectedAmount, newPaidAmount,
                            Math.max(0, newPendingAmount), "Cash");

                    if (isFullyPaid) {
                        // Move to completed payments
                        moveToCompleted(payment, newPaidAmount, todayDate);
                    } else {
                        // Update pending payment
                        updatePendingPayment(payment, newPaidAmount, newPendingAmount, todayDate);
                    }

                    String message = isFullyPaid ? "Payment completed successfully!" : "Partial payment collected";
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(), "Failed to save payment", Toast.LENGTH_SHORT).show()
                );
    }

    private void moveToCompleted(PendingPayment payment, double paidAmount, String completionDate) {
        // Create completed payment object
        Map<String, Object> completedPayment = new HashMap<>();
        completedPayment.put("invoiceNumber", payment.invoiceNumber);
        completedPayment.put("customerName", payment.customerName);
        completedPayment.put("totalAmount", payment.totalAmount);
        completedPayment.put("paidAmount", paidAmount);
        completedPayment.put("pendingAmount", 0);
        completedPayment.put("paymentStatus", "Paid");
        completedPayment.put("completionDate", completionDate);
        completedPayment.put("timestamp", ServerValue.TIMESTAMP);

        // Add to completed payments
        completedPaymentsRef.child(payment.invoiceNumber).setValue(completedPayment)
                .addOnSuccessListener(aVoid -> {
                    // Remove from pending payments
                    pendingPaymentsRef.child(payment.invoiceNumber).removeValue();
                });
    }

    private void updatePendingPayment(PendingPayment payment, double newPaidAmount,
                                     double newPendingAmount, String date) {
        Map<String, Object> pendingUpdates = new HashMap<>();
        pendingUpdates.put("paidAmount", newPaidAmount);
        pendingUpdates.put("pendingAmount", newPendingAmount);
        pendingUpdates.put("paymentStatus", "Partial");
        pendingUpdates.put("lastPaymentDate", date);

        pendingPaymentsRef.child(payment.invoiceNumber).updateChildren(pendingUpdates);
    }

    private void addInvoiceHistory(String invoiceNumber, double paidNow, double totalPaid,
                                   double pendingAfter, String paymentMode) {
        DatabaseReference historyRef = invoicesRef.child(invoiceNumber).child("history");
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
        String[] filterOptions = {
                "All Payments",
                "High Amount (>₹5000)",
                "Low Amount (<₹5000)",
                "Sort: Newest First",
                "Sort: Oldest First",
                "Sort: Highest Amount",
                "Sort: Lowest Amount"
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Filter & Sort")
                .setItems(filterOptions, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            resetFilter();
                            break;
                        case 1:
                            filterByAmount(5000, true);
                            break;
                        case 2:
                            filterByAmount(5000, false);
                            break;
                        case 3:
                            sortPayments(true, false);
                            break;
                        case 4:
                            sortPayments(false, false);
                            break;
                        case 5:
                            sortPayments(true, true);
                            break;
                        case 6:
                            sortPayments(false, true);
                            break;
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void resetFilter() {
        pendingPayments.clear();
        pendingPayments.addAll(allPayments);
        adapter.updateList(pendingPayments);
        Toast.makeText(requireContext(), "Filter cleared", Toast.LENGTH_SHORT).show();
    }

    private void filterByAmount(double threshold, boolean greaterThan) {
        List<PendingPayment> filteredList = new ArrayList<>();
        for (PendingPayment payment : allPayments) {
            if (greaterThan ? payment.pendingAmount > threshold : payment.pendingAmount < threshold) {
                filteredList.add(payment);
            }
        }

        if (filteredList.isEmpty()) {
            Toast.makeText(requireContext(), "No payments found", Toast.LENGTH_SHORT).show();
        } else {
            pendingPayments.clear();
            pendingPayments.addAll(filteredList);
            adapter.updateList(pendingPayments);
            Toast.makeText(requireContext(), filteredList.size() + " payment(s) found", Toast.LENGTH_SHORT).show();
        }
    }

    private void sortPayments(boolean descending, boolean byAmount) {
        List<PendingPayment> sortedList = new ArrayList<>(allPayments);

        if (byAmount) {
            sortedList.sort((p1, p2) -> descending ?
                    Double.compare(p2.pendingAmount, p1.pendingAmount) :
                    Double.compare(p1.pendingAmount, p2.pendingAmount));
        } else {
            sortedList.sort((p1, p2) -> descending ?
                    Long.compare(p2.timestamp, p1.timestamp) :
                    Long.compare(p1.timestamp, p2.timestamp));
        }

        pendingPayments.clear();
        pendingPayments.addAll(sortedList);
        adapter.updateList(pendingPayments);
        Toast.makeText(requireContext(), "Sorted successfully", Toast.LENGTH_SHORT).show();
    }
}