package com.sandhyasofttech.gstbillingpro.Adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sandhyasofttech.gstbillingpro.Model.RecentInvoiceItem;
import com.sandhyasofttech.gstbillingpro.R;

import java.util.ArrayList;
import java.util.Locale;

public class RecentInvoiceAdapter
        extends RecyclerView.Adapter<RecentInvoiceAdapter.ViewHolder> {

    private final ArrayList<RecentInvoiceItem> invoices;
    private Context context;

    public RecentInvoiceAdapter(ArrayList<RecentInvoiceItem> invoices) {
        this.invoices = invoices;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_recent_invoice, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        RecentInvoiceItem item = invoices.get(position);

        holder.tvInvoiceNo.setText(item.invoiceNo);
        holder.tvCustomerName.setText(item.customerName);
        holder.tvCustomerId.setText(item.customerId);
        holder.tvDate.setText(item.date);

        // ✅ SAME AS PendingPaymentsAdapter
        holder.tvTotalAmount.setText(String.format(
                Locale.getDefault(), "₹%,.2f", item.grandTotal
        ));

        if (item.pendingAmount > 0) {
            holder.tvPendingAmount.setVisibility(View.VISIBLE);
            holder.tvPendingAmount.setText(String.format(
                    Locale.getDefault(),
                    "Pending: ₹%,.0f",
                    item.pendingAmount
            ));
        } else {
            holder.tvPendingAmount.setVisibility(View.GONE);
        }

        // SHARE → WHATSAPP
        holder.tvShare.setOnClickListener(v -> {

            if (item.customerId == null || item.customerId.isEmpty()) {
                Toast.makeText(context,
                        "Customer mobile not available",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            String message =
                    "Hello " + item.customerName + ",\n\n" +
                            "Invoice No: " + item.invoiceNo + "\n" +
                            "Total Amount: ₹" + String.format("%.2f", item.grandTotal) + "\n" +
                            "Pending Amount: ₹" + String.format("%.0f", item.pendingAmount) + "\n\n" +
                            "Thank you.";

            String url = "https://wa.me/91" + item.customerId +
                    "?text=" + Uri.encode(message);

            context.startActivity(
                    new Intent(Intent.ACTION_VIEW, Uri.parse(url))
            );
        });

        holder.itemView.setAlpha(0f);
        holder.itemView.animate().alpha(1f).setDuration(300).start();
    }

    @Override
    public int getItemCount() {
        return invoices.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvInvoiceNo, tvCustomerName, tvCustomerId,
                tvTotalAmount, tvPendingAmount, tvDate;

        ImageView tvViewDetails, tvShare;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvInvoiceNo = itemView.findViewById(R.id.tvInvoiceNo);
            tvCustomerName = itemView.findViewById(R.id.tvCustomerName);
            tvCustomerId = itemView.findViewById(R.id.tvCustomerId);
            tvTotalAmount = itemView.findViewById(R.id.tvTotalAmount);
            tvPendingAmount = itemView.findViewById(R.id.tvPendingAmount);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvViewDetails = itemView.findViewById(R.id.tvViewDetails);
            tvShare = itemView.findViewById(R.id.tvShare);
        }
    }
}
