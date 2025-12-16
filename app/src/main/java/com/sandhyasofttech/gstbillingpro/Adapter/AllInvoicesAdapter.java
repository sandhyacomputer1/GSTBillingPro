package com.sandhyasofttech.gstbillingpro.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.sandhyasofttech.gstbillingpro.Model.RecentInvoiceItem;
import com.sandhyasofttech.gstbillingpro.R;

import java.util.List;
import java.util.Locale;

public class AllInvoicesAdapter
        extends RecyclerView.Adapter<AllInvoicesAdapter.ViewHolder> {

    public interface OnInvoiceClick {
        void onClick(String invoiceNumber);
    }

    private final List<RecentInvoiceItem> list;
    private final OnInvoiceClick listener;

    public AllInvoicesAdapter(List<RecentInvoiceItem> list,
                              OnInvoiceClick listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_invoice_manage, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
        RecentInvoiceItem item = list.get(pos);

        h.tvInvoiceNo.setText("Invoice #" + item.invoiceNo);
        h.tvCustomer.setText(item.customerName);
        h.tvDate.setText(item.date == null ? "" : item.date);

        h.tvTotal.setText(String.format(
                Locale.getDefault(), "₹%,.2f", item.grandTotal));

        if (item.pendingAmount > 0) {
            h.tvPending.setVisibility(View.VISIBLE);
            h.tvPending.setText(String.format(
                    Locale.getDefault(), "Pending ₹%,.0f", item.pendingAmount));
        } else {
            h.tvPending.setVisibility(View.GONE);
        }

        h.card.setOnClickListener(v ->
                listener.onClick(item.invoiceNo));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        MaterialCardView card;
        TextView tvInvoiceNo, tvCustomer, tvDate, tvTotal, tvPending;

        ViewHolder(@NonNull View v) {
            super(v);
            card = v.findViewById(R.id.cardInvoice);
            tvInvoiceNo = v.findViewById(R.id.tvInvoiceNo);
            tvCustomer = v.findViewById(R.id.tvCustomerName);
            tvDate = v.findViewById(R.id.tvDate);
            tvTotal = v.findViewById(R.id.tvTotalAmount);
            tvPending = v.findViewById(R.id.tvPendingAmount);
        }
    }
}
