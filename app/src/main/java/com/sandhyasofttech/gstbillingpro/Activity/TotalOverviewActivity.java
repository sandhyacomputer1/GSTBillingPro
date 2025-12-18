package com.sandhyasofttech.gstbillingpro.Activity;

import static org.bouncycastle.asn1.cmc.CMCStatus.pending;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sandhyasofttech.gstbillingpro.Adapter.RecentInvoiceAdapter;
import com.sandhyasofttech.gstbillingpro.Model.RecentInvoiceItem;
import com.sandhyasofttech.gstbillingpro.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class TotalOverviewActivity extends AppCompatActivity {

    // KPI + labels
    private TextView tvTodaySales, tvTodayReceived, tvTodayPending,
            tvTodayInvoices, tvTodayProducts,
            tvTodayLabel, tvReceivedLabel, tvPendingLabel, tvInvoicesLabel,
            tvTodayGrowth, tvPendingHint, tvRecentTitle, tvChartTitle;

    private MaterialCardView cardSales, cardReceived, cardPending, cardInvoices;

    // List + empty + progress
    private RecyclerView rvRecent;
    private View layoutEmpty;
    private ProgressBar progressBar;

    // Filters (inside today only)
    private ChipGroup chipGroupFilter;
    private Chip chipAllToday, chipPaid, chipPartial, chipPendingOnly;

    // Charts (today only)
    private BarChart chartSalesBar;
    private PieChart chartPaymentPie;

    // Firebase
    private DatabaseReference invoicesRef;
    private final SimpleDateFormat dateFmt = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    // Data
    private final ArrayList<RecentInvoiceItem> todayList = new ArrayList<>();
    private String today;

    private double maxPending = 0;
    private String maxPendingInvoice = null;
    private String maxPendingCustomer = null;
    private int pendingCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_total_overview);

        initViews();
        setupToolbar();
        setupFirebase();
        loadTodayData();
    }

    private void initViews() {
        // KPI labels + values
        tvTodaySales = findViewById(R.id.tvTodaySales);
        tvTodayLabel = findViewById(R.id.tvTodayLabel);
        tvTodayGrowth = findViewById(R.id.tvTodayGrowth);

        tvTodayReceived = findViewById(R.id.tvTodayReceived);
        tvReceivedLabel = findViewById(R.id.tvReceivedLabel);

        tvTodayPending = findViewById(R.id.tvTodayPending);
        tvPendingLabel = findViewById(R.id.tvPendingLabel);
        tvPendingHint = findViewById(R.id.tvPendingHint);

        tvTodayInvoices = findViewById(R.id.tvTodayInvoices);
        tvTodayProducts = findViewById(R.id.tvTodayProducts);
        tvInvoicesLabel = findViewById(R.id.tvInvoicesLabel);

        tvRecentTitle = findViewById(R.id.tvRecentTitle);
        tvChartTitle = findViewById(R.id.tvChartTitle);

        // Cards
        cardSales = findViewById(R.id.cardSales);
        cardReceived = findViewById(R.id.cardReceived);
        cardPending = findViewById(R.id.cardPending);
        cardInvoices = findViewById(R.id.cardInvoices);

        // List + empty + progress
        rvRecent = findViewById(R.id.rvRecent);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        progressBar = findViewById(R.id.progressOverview);
        rvRecent.setLayoutManager(new LinearLayoutManager(this));

        // Filter chips (today only)
        chipGroupFilter = findViewById(R.id.chipGroupFilterToday);
        chipAllToday = findViewById(R.id.chipAllToday);
        chipPaid = findViewById(R.id.chipPaidToday);
        chipPartial = findViewById(R.id.chipPartialToday);
        chipPendingOnly = findViewById(R.id.chipPendingToday);

        // Charts
        chartSalesBar = findViewById(R.id.chartSalesBarToday);
        chartPaymentPie = findViewById(R.id.chartPaymentPieToday);

        if (chartSalesBar != null) {
            chartSalesBar.setNoDataText("No sales today");
            chartSalesBar.getDescription().setEnabled(false);
            chartSalesBar.getAxisRight().setEnabled(false);
            chartSalesBar.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
            chartSalesBar.getLegend().setEnabled(false);
        }

        if (chartPaymentPie != null) {
            chartPaymentPie.setNoDataText("No payments today");
            chartPaymentPie.getDescription().setEnabled(false);
            chartPaymentPie.setUsePercentValues(true);
            chartPaymentPie.setEntryLabelTextSize(10f);
        }

        setupCardClicks();
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbarTodayOverview);
        toolbar.setTitle("Today Overview");
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupCardClicks() {
        cardSales.setOnClickListener(v ->
                Toast.makeText(this, "Detail view of today's sales (future)", Toast.LENGTH_SHORT).show());

        cardReceived.setOnClickListener(v ->
                Toast.makeText(this, "Detail view of today's collections (future)", Toast.LENGTH_SHORT).show());

        cardPending.setOnClickListener(v ->
                Toast.makeText(this, "Today's pending invoices (future)", Toast.LENGTH_SHORT).show());

        cardInvoices.setOnClickListener(v ->
                Toast.makeText(this, "Today's invoice list (future)", Toast.LENGTH_SHORT).show());
    }

    private void setupFirebase() {
        SharedPreferences prefs = getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE);
        String userMobile = prefs.getString("USER_MOBILE", null);

        if (userMobile == null) {
            Toast.makeText(this, "Session expired. Please log in again.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        invoicesRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userMobile)
                .child("invoices");
    }

    private void loadTodayData() {
        progressBar.setVisibility(View.VISIBLE);
        layoutEmpty.setVisibility(View.GONE);
        rvRecent.setVisibility(View.GONE);

        today = dateFmt.format(new Date());

        invoicesRef.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                double todaySale = 0.0;
                double todayReceived = 0.0;
                double todayPending = 0.0;
                long todayInvoicesCount = 0;
                Set<String> todayProductIds = new HashSet<>();
                todayList.clear();
                pendingCount = 0;

                maxPending = 0;
                maxPendingInvoice = null;
                maxPendingCustomer = null;

                // index -> amount (for bar chart, one bar per invoice)
                ArrayList<Double> invoiceAmounts = new ArrayList<>();

                for (DataSnapshot invSnap : snapshot.getChildren()) {

                    String invDate = invSnap.child("invoiceDate").getValue(String.class);
                    if (invDate == null || !invDate.equals(today)) {
                        // skip non-today invoices
                        continue;
                    }

                    todayInvoicesCount++;

                    Double grandTotal = invSnap.child("grandTotal").getValue(Double.class);
                    Double pending = invSnap.child("pendingAmount").getValue(Double.class);
                    Double paidAmount = invSnap.child("paidAmount").getValue(Double.class);
                    String status = invSnap.child("paymentStatus").getValue(String.class);

                    if (grandTotal != null) {
                        todaySale += grandTotal;
                        invoiceAmounts.add(grandTotal);
                    }

                    if (paidAmount != null) {
                        todayReceived += paidAmount;
                    }

                    String invoiceNo = invSnap.child("invoiceNumber").getValue(String.class);
                    String customerName = invSnap.child("customerName").getValue(String.class);

                    if (pending != null && pending > 0) {
                        todayPending += pending;
                        pendingCount++;

                        if (pending > maxPending) {
                            maxPending = pending;
                            maxPendingInvoice = invoiceNo;
                            maxPendingCustomer = customerName;
                        }
                    }

                    // products (only for today)
                    for (DataSnapshot item : invSnap.child("items").getChildren()) {
                        String productId = item.child("productId").getValue(String.class);
                        if (productId != null) {
                            todayProductIds.add(productId);
                        }
                    }

                    // today recent list
                    String customerPhone = invSnap.child("customerPhone").getValue(String.class);
                    Double invGrand = grandTotal;
                    double pendingAmount = pending != null ? pending : 0;
                    String date = invDate;

                    if (invoiceNo != null && customerName != null &&
                            invGrand != null && date != null) {
                        RecentInvoiceItem item = new RecentInvoiceItem(
                                invoiceNo,
                                customerPhone == null ? "" : customerPhone,
                                customerName,
                                invGrand,
                                pendingAmount,
                                date
                        );
                        // optionally, you can store status inside model if you added field
                        todayList.add(item);
                    }
                }

                // newest first
                Collections.reverse(todayList);

                bindTodayOverview(todaySale, todayReceived, todayPending,
                        todayInvoicesCount, todayProductIds.size());

                updateTodaySalesBar(invoiceAmounts);
                updateTodayPaymentPie(todayReceived, todayPending);

                setupTodayFilterChips();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(TotalOverviewActivity.this,
                        "Failed to load today overview", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void bindTodayOverview(double todaySale, double todayReceived, double todayPending,
                                   long todayInvoicesCount, int todayProductsCount) {

        progressBar.setVisibility(View.GONE);

        tvTodaySales.setText(formatCurrency((long) todaySale));
        tvTodayReceived.setText(formatCurrency((long) todayReceived));
        tvTodayPending.setText(formatCurrency((long) todayPending));
        tvTodayInvoices.setText(String.valueOf(todayInvoicesCount));
        tvTodayProducts.setText(todayProductsCount + " Products");

        // Simple growth hint (can be replaced later with real comparison)
        tvTodayGrowth.setText(todaySale > 0 ? "+ Good Day" : "No sales yet");

        if (maxPending > 0 && maxPendingInvoice != null) {
            String who = (maxPendingCustomer != null && !maxPendingCustomer.isEmpty())
                    ? maxPendingCustomer : "Unknown";
            tvPendingHint.setText(
                    pendingCount + " invoices | Max: " + formatCurrency((long) maxPending) +
                            " (" + who + ", " + maxPendingInvoice + ")"
            );
        } else {
            tvPendingHint.setText("No pending invoices today");
        }

        updateTodayList(todayList);
    }

    private void updateTodaySalesBar(ArrayList<Double> invoiceAmounts) {
        if (chartSalesBar == null) return;

        if (invoiceAmounts == null || invoiceAmounts.isEmpty()) {
            chartSalesBar.clear();
            chartSalesBar.invalidate();
            return;
        }

        ArrayList<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < invoiceAmounts.size(); i++) {
            entries.add(new BarEntry(i, invoiceAmounts.get(i).floatValue()));
        }

        BarDataSet dataSet = new BarDataSet(entries, "Today invoices");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextSize(9f);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.6f);

        chartSalesBar.setData(barData);
        chartSalesBar.getXAxis().setLabelCount(entries.size());
        chartSalesBar.invalidate();
    }

    private void updateTodayPaymentPie(double totalReceived, double totalPending) {
        if (chartPaymentPie == null) return;

        if (totalReceived <= 0 && totalPending <= 0) {
            chartPaymentPie.clear();
            chartPaymentPie.invalidate();
            return;
        }

        ArrayList<PieEntry> entries = new ArrayList<>();
        if (totalReceived > 0) {
            entries.add(new PieEntry((float) totalReceived, "Received"));
        }
        if (totalPending > 0) {
            entries.add(new PieEntry((float) totalPending, "Pending"));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setSliceSpace(2f);
        dataSet.setValueTextSize(10f);

        PieData pieData = new PieData(dataSet);
        chartPaymentPie.setData(pieData);
        chartPaymentPie.invalidate();
    }

    private void setupTodayFilterChips() {
        chipGroupFilter.setOnCheckedChangeListener((group, checkedId) -> {
            ArrayList<RecentInvoiceItem> filtered = new ArrayList<>();

            for (RecentInvoiceItem item : todayList) {
                String d = item.getInvoiceDate();
                // we already know all are today, but keep check
                if (d == null || !d.equals(today)) continue;

                DataSnapshot dummy = null; // not needed; use status if you added it in model

                // If you stored paymentStatus in RecentInvoiceItem, use that here.
                // For now, just use pendingAmount.
                double pending = item.getPendingAmount();

                if (checkedId == R.id.chipPaidToday) {
                    if (pending == 0) filtered.add(item);
                } else if (checkedId == R.id.chipPartialToday) {
                    // partial = >0 but < grandTotal
                    if (pending > 0 && pending < item.getGrandTotal()) filtered.add(item);
                } else if (checkedId == R.id.chipPendingToday) {
                    if (pending > 0) filtered.add(item);
                } else { // All today
                    filtered.add(item);
                }
            }

            updateTodayList(filtered);
        });

        chipAllToday.setChecked(true);
    }

    private void updateTodayList(ArrayList<RecentInvoiceItem> list) {
        if (list.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            rvRecent.setVisibility(View.GONE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            rvRecent.setVisibility(View.VISIBLE);
            rvRecent.setAdapter(new RecentInvoiceAdapter(list));
        }
    }

    private String formatCurrency(long amount) {
        return "₹" + String.format("%,d", amount);
    }
}
