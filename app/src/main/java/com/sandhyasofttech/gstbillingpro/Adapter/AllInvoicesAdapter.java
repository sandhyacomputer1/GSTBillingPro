package com.sandhyasofttech.gstbillingpro.Adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.sandhyasofttech.gstbillingpro.Model.RecentInvoiceItem;
import com.sandhyasofttech.gstbillingpro.R;

import java.util.List;
import java.util.Locale;

public class AllInvoicesAdapter extends RecyclerView.Adapter<AllInvoicesAdapter.ViewHolder> {

    public interface OnInvoiceClick {
        void onClick(String invoiceNumber);
    }

    private final List<RecentInvoiceItem> list;
    private final OnInvoiceClick listener;

    public AllInvoicesAdapter(List<RecentInvoiceItem> list, OnInvoiceClick listener) {
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
        Context context = h.itemView.getContext();

        h.tvInvoiceNo.setText("Invoice #" + item.invoiceNo);
        h.tvCustomer.setText(item.customerName);
        h.tvDate.setText(item.date == null ? "" : item.date);

        h.tvTotal.setText(String.format(Locale.getDefault(), "₹%,.2f", item.grandTotal));

        if (item.pendingAmount > 0) {
            h.tvPending.setVisibility(View.VISIBLE);
            h.tvPending.setText(String.format(Locale.getDefault(), "Pending ₹%,.0f", item.pendingAmount));

            // Show reminder button for pending invoices
            h.tvReminder.setVisibility(View.VISIBLE);
            h.tvReminder.setOnClickListener(v -> sendWhatsAppReminder(context, item));
        } else {
            h.tvPending.setVisibility(View.GONE);
            h.tvReminder.setVisibility(View.GONE);
        }

        h.card.setOnClickListener(v -> listener.onClick(item.invoiceNo));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    // Send WhatsApp reminder for individual invoice
    private void sendWhatsAppReminder(Context context, RecentInvoiceItem item) {
        String message = String.format(Locale.getDefault(),
                "🔔 *Payment Reminder*\n\n" +
                        "Dear %s,\n\n" +
                        "This is a gentle reminder for your pending payment:\n\n" +
                        "📄 Invoice Number: *%s*\n" +
                        "📅 Invoice Date: %s\n" +
                        "💰 Total Amount: ₹%.2f\n" +
                        "⚠️ Pending Amount: *₹%.0f*\n\n" +
                        "Please make the payment at your earliest convenience.\n\n" +
                        "Thank you for your business! 🙏",
                item.customerName,
                item.invoiceNo,
                item.date != null ? item.date : "N/A",
                item.grandTotal,
                item.pendingAmount);

        try {
            // If customer mobile is available, send directly to that number
            String url;
            if (item.customerId != null && !item.customerId.isEmpty()) {
                // Remove any non-digit characters and ensure it starts with country code
                String phone = item.customerId.replaceAll("[^0-9]", "");
                if (!phone.startsWith("91") && phone.length() == 10) {
                    phone = "91" + phone; // Add India country code
                }
                url = "https://wa.me/" + phone + "?text=" + Uri.encode(message);
            } else {
                url = "https://wa.me/?text=" + Uri.encode(message);
            }

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            intent.setPackage("com.whatsapp");
            context.startActivity(intent);
        } catch (Exception e) {
            // If WhatsApp is not installed, try web version
            try {
                String url;
                if (item.customerId != null && !item.customerId.isEmpty()) {
                    String phone = item.customerId.replaceAll("[^0-9]", "");
                    if (!phone.startsWith("91") && phone.length() == 10) {
                        phone = "91" + phone;
                    }
                    url = "https://web.whatsapp.com/send?phone=" + phone + "&text=" + Uri.encode(message);
                } else {
                    url = "https://web.whatsapp.com/send?text=" + Uri.encode(message);
                }

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                context.startActivity(intent);
            } catch (Exception ex) {
                Toast.makeText(context, "WhatsApp not installed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView card;
        TextView tvInvoiceNo, tvCustomer, tvDate, tvTotal, tvPending, tvReminder;

        ViewHolder(@NonNull View v) {
            super(v);
            card = v.findViewById(R.id.cardInvoice);
            tvInvoiceNo = v.findViewById(R.id.tvInvoiceNo);
            tvCustomer = v.findViewById(R.id.tvCustomerName);
            tvDate = v.findViewById(R.id.tvDate);
            tvTotal = v.findViewById(R.id.tvTotalAmount);
            tvPending = v.findViewById(R.id.tvPendingAmount);
            tvReminder = v.findViewById(R.id.tvReminder);
        }
    }
}