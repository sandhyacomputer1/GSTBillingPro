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
import com.google.firebase.database.ValueEventListener;
import com.sandhyasofttech.gstbillingpro.Adapter.CompletedPaymentsAdapter;
import com.sandhyasofttech.gstbillingpro.Model.PendingPayment;
import com.sandhyasofttech.gstbillingpro.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CompletedPaymentsFragment extends Fragment {

    private static final String ARG_USER_MOBILE = "user_mobile";

    private RecyclerView rvCompletedPayments;
    private ProgressBar progressBar;
    private LinearLayout layoutEmpty;
    private TextView tvTotalCompleted, tvCompletedCount;
    private MaterialButton btnFilter;
    private CompletedPaymentsAdapter adapter;

    private List<PendingPayment> completedPayments = new ArrayList<>();
    private List<PendingPayment> allCompletedPayments = new ArrayList<>();
    private DatabaseReference completedPaymentsRef;
    private String userMobile;
    private double totalCompletedAmount = 0;

    public static CompletedPaymentsFragment newInstance(String userMobile) {
        CompletedPaymentsFragment fragment = new CompletedPaymentsFragment();
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
        return inflater.inflate(R.layout.fragment_completed_payments, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        rvCompletedPayments = view.findViewById(R.id.rvCompletedPayments);
        progressBar = view.findViewById(R.id.progressBar);
        layoutEmpty = view.findViewById(R.id.layoutEmpty);
        tvTotalCompleted = view.findViewById(R.id.tvTotalCompleted);
        tvCompletedCount = view.findViewById(R.id.tvCompletedCount);
        btnFilter = view.findViewById(R.id.btnFilter);

        // Initialize Firebase
        DatabaseReference usersRef = FirebaseDatabase.getInstance()
                .getReference("users").child(userMobile);
        completedPaymentsRef = usersRef.child("completedPayments");

        // Setup RecyclerView
        setupRecyclerView();

        // Load completed payments
        loadCompletedPayments();

        // Filter button
        btnFilter.setOnClickListener(v -> showFilterDialog());
    }

    private void setupRecyclerView() {
        adapter = new CompletedPaymentsAdapter(completedPayments, this::showPaymentDetails);
        rvCompletedPayments.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvCompletedPayments.setAdapter(adapter);
    }

    private void loadCompletedPayments() {
        progressBar.setVisibility(View.VISIBLE);
        layoutEmpty.setVisibility(View.GONE);

        completedPaymentsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                completedPayments.clear();
                allCompletedPayments.clear();
                totalCompletedAmount = 0;

                for (DataSnapshot ds : snapshot.getChildren()) {
                    PendingPayment payment = ds.getValue(PendingPayment.class);
                    if (payment != null) {
                        completedPayments.add(payment);
                        allCompletedPayments.add(payment);
                        totalCompletedAmount += payment.totalAmount;
                    }
                }

                adapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
                updateUI();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "Failed to load completed payments", Toast.LENGTH_SHORT).show();
                updateUI();
            }
        });
    }

    private void updateUI() {
        if (completedPayments.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            rvCompletedPayments.setVisibility(View.GONE);
            tvTotalCompleted.setText("₹0");
            tvCompletedCount.setText("0 Completed");
        } else {
            layoutEmpty.setVisibility(View.GONE);
            rvCompletedPayments.setVisibility(View.VISIBLE);
            tvTotalCompleted.setText(String.format(Locale.getDefault(), "₹%,.0f", totalCompletedAmount));
            tvCompletedCount.setText(String.format(Locale.getDefault(), "%d Completed", completedPayments.size()));
        }
    }

    private void showPaymentDetails(PendingPayment payment) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_payment_details, null);

        TextView tvInvoiceNumber = dialogView.findViewById(R.id.tvInvoiceNumber);
        TextView tvCustomerName = dialogView.findViewById(R.id.tvCustomerName);
        TextView tvTotalAmount = dialogView.findViewById(R.id.tvTotalAmount);
        TextView tvCompletionDate = dialogView.findViewById(R.id.tvCompletionDate);
        MaterialButton btnClose = dialogView.findViewById(R.id.btnClose);

        tvInvoiceNumber.setText("Invoice: " + payment.invoiceNumber);
        tvCustomerName.setText(payment.customerName);
        tvTotalAmount.setText(String.format(Locale.getDefault(), "₹%.2f", payment.totalAmount));
        tvCompletionDate.setText("Completed on: " + payment.completionDate);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setCancelable(true)
                .create();

        btnClose.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void showFilterDialog() {
        String[] filterOptions = {
                "All Payments",
                "This Month",
                "Last Month",
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
                            filterByMonth(0);
                            break;
                        case 2:
                            filterByMonth(1);
                            break;
                        case 3:
                            filterByAmount(5000, true);
                            break;
                        case 4:
                            filterByAmount(5000, false);
                            break;
                        case 5:
                            sortPayments(true, false);
                            break;
                        case 6:
                            sortPayments(false, false);
                            break;
                        case 7:
                            sortPayments(true, true);
                            break;
                        case 8:
                            sortPayments(false, true);
                            break;
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void resetFilter() {
        completedPayments.clear();
        completedPayments.addAll(allCompletedPayments);
        adapter.updateList(completedPayments);
        Toast.makeText(requireContext(), "Filter cleared", Toast.LENGTH_SHORT).show();
    }

    private void filterByMonth(int monthsAgo) {
        // Implementation for filtering by month
        Toast.makeText(requireContext(), "Month filter applied", Toast.LENGTH_SHORT).show();
    }

    private void filterByAmount(double threshold, boolean greaterThan) {
        List<PendingPayment> filteredList = new ArrayList<>();
        for (PendingPayment payment : allCompletedPayments) {
            if (greaterThan ? payment.totalAmount > threshold : payment.totalAmount < threshold) {
                filteredList.add(payment);
            }
        }

        if (filteredList.isEmpty()) {
            Toast.makeText(requireContext(), "No payments found", Toast.LENGTH_SHORT).show();
        } else {
            completedPayments.clear();
            completedPayments.addAll(filteredList);
            adapter.updateList(completedPayments);
            Toast.makeText(requireContext(), filteredList.size() + " payment(s) found", Toast.LENGTH_SHORT).show();
        }
    }

    private void sortPayments(boolean descending, boolean byAmount) {
        List<PendingPayment> sortedList = new ArrayList<>(allCompletedPayments);

        if (byAmount) {
            sortedList.sort((p1, p2) -> descending ?
                    Double.compare(p2.totalAmount, p1.totalAmount) :
                    Double.compare(p1.totalAmount, p2.totalAmount));
        } else {
            sortedList.sort((p1, p2) -> descending ?
                    Long.compare(p2.timestamp, p1.timestamp) :
                    Long.compare(p1.timestamp, p2.timestamp));
        }

        completedPayments.clear();
        completedPayments.addAll(sortedList);
        adapter.updateList(completedPayments);
        Toast.makeText(requireContext(), "Sorted successfully", Toast.LENGTH_SHORT).show();
    }
}