package com.sandhyasofttech.gstbillingpro.Adapter;

import android.graphics.Color;
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

public class CompletedPaymentsAdapter extends RecyclerView.Adapter<CompletedPaymentsAdapter.ViewHolder> {

    private List<PendingPayment> payments;
    private OnPaymentClickListener listener;

    public interface OnPaymentClickListener {
        void onPaymentClick(PendingPayment payment);
    }

    public CompletedPaymentsAdapter(List<PendingPayment> payments, OnPaymentClickListener listener) {
        this.payments = payments;
        this.listener = listener;
    }

    public void updateList(List<PendingPayment> newPayments) {
        this.payments = newPayments;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_completed_payment, parent, false);
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
        MaterialCardView cardPayment;
        TextView tvInvoiceNumber, tvCustomerName, tvCompletionDate;
        TextView tvTotalAmount, tvStatusBadge;

        ViewHolder(View itemView) {
            super(itemView);
            cardPayment = itemView.findViewById(R.id.cardPayment);
            tvInvoiceNumber = itemView.findViewById(R.id.tvInvoiceNumber);
            tvCustomerName = itemView.findViewById(R.id.tvCustomerName);
            tvCompletionDate = itemView.findViewById(R.id.tvCompletionDate);
            tvTotalAmount = itemView.findViewById(R.id.tvTotalAmount);
            tvStatusBadge = itemView.findViewById(R.id.tvStatusBadge);
        }

        void bind(PendingPayment payment) {
            tvInvoiceNumber.setText(payment.invoiceNumber);
            tvCustomerName.setText(payment.customerName);

            if (payment.completionDate != null && !payment.completionDate.isEmpty()) {
                tvCompletionDate.setText("Completed: " + payment.completionDate);
                tvCompletionDate.setVisibility(View.VISIBLE);
            } else {
                tvCompletionDate.setVisibility(View.GONE);
            }

            tvTotalAmount.setText(String.format(Locale.getDefault(), "₹%,.2f", payment.totalAmount));

            tvStatusBadge.setText("PAID");
            tvStatusBadge.setTextColor(Color.parseColor("#2E7D32"));
            tvStatusBadge.setBackgroundColor(Color.parseColor("#E8F5E9"));

            cardPayment.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onPaymentClick(payment);
                }
            });
        }
    }
}