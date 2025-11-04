package com.sandhyasofttech.gstbillingpro.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sandhyasofttech.gstbillingpro.Model.RecentInvoiceItem;
import com.sandhyasofttech.gstbillingpro.R;

import java.util.ArrayList;

public class RecentInvoiceAdapter extends RecyclerView.Adapter<RecentInvoiceAdapter.ViewHolder> {

    private final ArrayList<RecentInvoiceItem> invoices;

    public RecentInvoiceAdapter(ArrayList<RecentInvoiceItem> invoices) {
        this.invoices = invoices;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recent_invoice, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RecentInvoiceItem item = invoices.get(position);

        holder.tvInvoiceNo.setText("Invoice #" + item.invoiceNo);
        holder.tvCustomerName.setText(item.customerName);
        holder.tvCustomerId.setText("ID: " + item.customerId);
        holder.tvDate.setText(item.date);
        holder.tvTotalAmount.setText("₹" + String.format("%.2f", item.grandTotal));

        // Optional fade animation
        holder.itemView.setAlpha(0f);
        holder.itemView.animate().alpha(1f).setDuration(300).start();
    }

    @Override
    public int getItemCount() {
        return invoices.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvInvoiceNo, tvCustomerName, tvCustomerId, tvTotalAmount, tvDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvInvoiceNo = itemView.findViewById(R.id.tvInvoiceNo);
            tvCustomerName = itemView.findViewById(R.id.tvCustomerName);
            tvCustomerId = itemView.findViewById(R.id.tvCustomerId);
            tvTotalAmount = itemView.findViewById(R.id.tvTotalAmount);
            tvDate = itemView.findViewById(R.id.tvDate);
        }
    }
}
