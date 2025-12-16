package com.sandhyasofttech.gstbillingpro.Activity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.firebase.database.*;
import com.sandhyasofttech.gstbillingpro.Adapter.InvAdapter;
import com.sandhyasofttech.gstbillingpro.Adapter.PaymentHistoryAdapter;
import com.sandhyasofttech.gstbillingpro.Model.InvModel;
import com.sandhyasofttech.gstbillingpro.Model.PaymentHistoryModel;
import com.sandhyasofttech.gstbillingpro.R;

import java.util.ArrayList;
import java.util.Locale;

public class InvDetailsActivity extends AppCompatActivity {

    // Header Views
    TextView tvBusinessName, tvBusinessAddress, tvBusinessGstin;

    // Invoice Info Views
    TextView tvInvoiceNo, tvCustomerName, tvInvoiceDate, tvCustomerPhone;
    Chip chipPaymentStatus;

    // Bill Summary Views
    TextView tvTotalTaxable, tvCGST, tvSGST, tvIGST, tvGrandTotal;
    LinearLayout layoutIGST;

    // Payment Details Views
    TextView tvPaymentStatus, tvPaymentMode, tvPaymentModeIcon;
    TextView tvPaidAmount, tvPendingAmount;
    LinearLayout layoutPending;

    // RecyclerViews
    RecyclerView rvItems, rvPayments;
    MaterialCardView cvPaymentHistory;

    DatabaseReference invoiceRef;

    ArrayList<InvModel> itemList = new ArrayList<>();
    ArrayList<PaymentHistoryModel> paymentList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inv_detailss);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        String invoiceNumber = getIntent().getStringExtra("invoiceNumber");
        String mobile = getSharedPreferences("APP_PREFS", MODE_PRIVATE)
                .getString("USER_MOBILE", "");

        invoiceRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(mobile)
                .child("invoices")
                .child(invoiceNumber);

        initViews();
        loadInvoiceDetails();
        loadItems();
        loadPaymentHistory();
    }

    private void initViews() {
        // Header
        tvBusinessName = findViewById(R.id.tvBusinessName);
        tvBusinessAddress = findViewById(R.id.tvBusinessAddress);
        tvBusinessGstin = findViewById(R.id.tvBusinessGstin);

        // Invoice Info
        tvInvoiceNo = findViewById(R.id.tvInvoiceNo);
        tvCustomerName = findViewById(R.id.tvCustomerName);
        tvInvoiceDate = findViewById(R.id.tvInvoiceDate);
        tvCustomerPhone = findViewById(R.id.tvCustomerPhone);
        chipPaymentStatus = findViewById(R.id.chipPaymentStatus);

        // Bill Summary
        tvTotalTaxable = findViewById(R.id.tvTotalTaxable);
        tvCGST = findViewById(R.id.tvCGST);
        tvSGST = findViewById(R.id.tvSGST);
        tvIGST = findViewById(R.id.tvIGST);
        tvGrandTotal = findViewById(R.id.tvGrandTotal);
        layoutIGST = findViewById(R.id.layoutIGST);

        // Payment Details
        tvPaymentStatus = findViewById(R.id.tvPaymentStatus);
        tvPaymentMode = findViewById(R.id.tvPaymentMode);
        tvPaymentModeIcon = findViewById(R.id.tvPaymentModeIcon);
        tvPaidAmount = findViewById(R.id.tvPaidAmount);
        tvPendingAmount = findViewById(R.id.tvPendingAmount);
        layoutPending = findViewById(R.id.layoutPending);

        // RecyclerViews
        rvItems = findViewById(R.id.rvItems);
        rvPayments = findViewById(R.id.rvPayments);
        cvPaymentHistory = findViewById(R.id.cvPaymentHistory);

        rvItems.setLayoutManager(new LinearLayoutManager(this));
        rvPayments.setLayoutManager(new LinearLayoutManager(this));
    }

    private void loadInvoiceDetails() {
        invoiceRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot s) {
                // Business Details
                String businessName = s.child("businessName").getValue(String.class);
                String businessAddress = s.child("businessAddress").getValue(String.class);
                String gstin = s.child("businessGstin").getValue(String.class);

                tvBusinessName.setText(businessName != null ? businessName : "Business Name");
                tvBusinessAddress.setText(businessAddress != null ? businessAddress : "Address");

                if (gstin != null && !gstin.isEmpty()) {
                    tvBusinessGstin.setVisibility(View.VISIBLE);
                    tvBusinessGstin.setText("GSTIN: " + gstin);
                }

                // Invoice Details
                String invoiceNo = s.child("invoiceNumber").getValue(String.class);
                String customerName = s.child("customerName").getValue(String.class);
                String customerPhone = s.child("customerPhone").getValue(String.class);
                String invoiceDate = s.child("invoiceDate").getValue(String.class);

                tvInvoiceNo.setText(invoiceNo != null ? invoiceNo : "N/A");
                tvCustomerName.setText(customerName != null ? customerName : "N/A");
                tvCustomerPhone.setText(customerPhone != null ? customerPhone : "N/A");
                tvInvoiceDate.setText(invoiceDate != null ? invoiceDate : "N/A");

                // Bill Summary
                Double totalTaxable = s.child("totalTaxableValue").getValue(Double.class);
                Double cgst = s.child("totalCGST").getValue(Double.class);
                Double sgst = s.child("totalSGST").getValue(Double.class);
                Double igst = s.child("totalIGST").getValue(Double.class);
                Double grandTotal = s.child("grandTotal").getValue(Double.class);

                tvTotalTaxable.setText(String.format(Locale.getDefault(), "₹%.2f",
                        totalTaxable != null ? totalTaxable : 0.0));
                tvCGST.setText(String.format(Locale.getDefault(), "₹%.2f",
                        cgst != null ? cgst : 0.0));
                tvSGST.setText(String.format(Locale.getDefault(), "₹%.2f",
                        sgst != null ? sgst : 0.0));

                if (igst != null && igst > 0) {
                    layoutIGST.setVisibility(View.VISIBLE);
                    tvIGST.setText(String.format(Locale.getDefault(), "₹%.2f", igst));
                }

                tvGrandTotal.setText(String.format(Locale.getDefault(), "₹%.2f",
                        grandTotal != null ? grandTotal : 0.0));

                // Payment Details
                String paymentStatus = s.child("paymentStatus").getValue(String.class);
                String paymentMode = s.child("paymentMode").getValue(String.class);
                Double paidAmount = s.child("paidAmount").getValue(Double.class);
                Double pendingAmount = s.child("pendingAmount").getValue(Double.class);

                // Set Payment Status
                if (paymentStatus != null) {
                    tvPaymentStatus.setText(paymentStatus);
                    updatePaymentStatusUI(paymentStatus);
                }

                // 🔥 Set Payment Mode with Icon
                if (paymentMode != null) {
                    tvPaymentMode.setText(paymentMode);
                    if (paymentMode.equalsIgnoreCase("Cash")) {
                        tvPaymentModeIcon.setText("💵");
                    } else if (paymentMode.equalsIgnoreCase("Online")) {
                        tvPaymentModeIcon.setText("💳");
                    } else {
                        tvPaymentModeIcon.setText("💰");
                    }
                } else {
                    tvPaymentMode.setText("Cash");
                    tvPaymentModeIcon.setText("💵");
                }

                // Set Paid Amount
                tvPaidAmount.setText(String.format(Locale.getDefault(), "₹%.2f",
                        paidAmount != null ? paidAmount : 0.0));

                // Set Pending Amount
                if (pendingAmount != null && pendingAmount > 0) {
                    layoutPending.setVisibility(View.VISIBLE);
                    tvPendingAmount.setText(String.format(Locale.getDefault(), "₹%.2f", pendingAmount));
                } else {
                    layoutPending.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });
    }

    private void updatePaymentStatusUI(String status) {
        if (status == null) return;

        switch (status.toLowerCase()) {
            case "paid":
                chipPaymentStatus.setText("Paid");
                chipPaymentStatus.setChipBackgroundColorResource(android.R.color.holo_green_dark);
                tvPaymentStatus.setTextColor(Color.parseColor("#4CAF50"));
                break;

            case "partial":
                chipPaymentStatus.setText("Partial");
                chipPaymentStatus.setChipBackgroundColorResource(android.R.color.holo_orange_dark);
                tvPaymentStatus.setTextColor(Color.parseColor("#FF9800"));
                break;

            case "pending":
                chipPaymentStatus.setText("Pending");
                chipPaymentStatus.setChipBackgroundColorResource(android.R.color.holo_red_dark);
                tvPaymentStatus.setTextColor(Color.parseColor("#F44336"));
                break;

            default:
                chipPaymentStatus.setText(status);
                chipPaymentStatus.setChipBackgroundColorResource(android.R.color.darker_gray);
                tvPaymentStatus.setTextColor(Color.parseColor("#757575"));
                break;
        }
    }

    private void loadItems() {
        invoiceRef.child("items").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                itemList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    InvModel item = ds.getValue(InvModel.class);
                    if (item != null) {
                        itemList.add(item);
                    }
                }

                if (!itemList.isEmpty()) {
                    rvItems.setAdapter(new InvAdapter(itemList));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });
    }

    private void loadPaymentHistory() {
        invoiceRef.child("history").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                paymentList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    PaymentHistoryModel payment = ds.getValue(PaymentHistoryModel.class);
                    if (payment != null) {
                        paymentList.add(payment);
                    }
                }

                if (!paymentList.isEmpty()) {
                    cvPaymentHistory.setVisibility(View.VISIBLE);
                    rvPayments.setAdapter(new PaymentHistoryAdapter(paymentList));
                } else {
                    cvPaymentHistory.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });
    }
}