package com.sandhyasofttech.gstbillingpro.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.sandhyasofttech.gstbillingpro.Model.PendingPayment;
import com.sandhyasofttech.gstbillingpro.R;

import java.util.List;
import java.util.Locale;

public class PendingPaymentsAdapter extends RecyclerView.Adapter<PendingPaymentsAdapter.ViewHolder> {

    private List<PendingPayment> payments;
    private OnPaymentClickListener listener;

    public interface OnPaymentClickListener {
        void onPaymentClick(PendingPayment payment);
    }

    public PendingPaymentsAdapter(List<PendingPayment> payments, OnPaymentClickListener listener) {
        this.payments = payments;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pending_payment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PendingPayment payment = payments.get(position);
        holder.bind(payment);
    }

    @Override
    public int getItemCount() {
        return payments.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardView;
        TextView tvInvoiceNumber, tvCustomerName, tvTotalAmount, tvPaidAmount, tvPendingAmount, tvStatus;

        ViewHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardPayment);
            tvInvoiceNumber = itemView.findViewById(R.id.tvInvoiceNumber);
            tvCustomerName = itemView.findViewById(R.id.tvCustomerName);
            tvTotalAmount = itemView.findViewById(R.id.tvTotalAmount);
            tvPaidAmount = itemView.findViewById(R.id.tvPaidAmount);
            tvPendingAmount = itemView.findViewById(R.id.tvPendingAmount);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }

        void bind(PendingPayment payment) {
            tvInvoiceNumber.setText(payment.invoiceNumber);
            tvCustomerName.setText(payment.customerName);
            tvTotalAmount.setText(String.format(Locale.getDefault(), "Total: ₹%.2f", payment.totalAmount));
            tvPaidAmount.setText(String.format(Locale.getDefault(), "Paid: ₹%.2f", payment.paidAmount));
            tvPendingAmount.setText(String.format(Locale.getDefault(), "₹%.2f", payment.pendingAmount));
            tvStatus.setText(payment.paymentStatus);

            // Color code status
            if ("Paid".equals(payment.paymentStatus)) {
                tvStatus.setTextColor(0xFF4CAF50); // Green
            } else if ("Partial".equals(payment.paymentStatus)) {
                tvStatus.setTextColor(0xFFFFC107); // Orange
            } else {
                tvStatus.setTextColor(0xFFFF5722); // Red
            }

            cardView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onPaymentClick(payment);
                }
            });
        }
    }
}