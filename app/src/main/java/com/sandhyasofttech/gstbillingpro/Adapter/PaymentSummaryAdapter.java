package com.sandhyasofttech.gstbillingpro.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sandhyasofttech.gstbillingpro.Model.CartItem;
import com.sandhyasofttech.gstbillingpro.R;

import java.util.List;
import java.util.Locale;

public class PaymentSummaryAdapter extends RecyclerView.Adapter<PaymentSummaryAdapter.ViewHolder> {

    private List<CartItem> cartItems;

    public PaymentSummaryAdapter(List<CartItem> cartItems) {
        this.cartItems = cartItems;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_payment_summary, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CartItem item = cartItems.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDetails, tvTotal;

        ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvProductName);
            tvDetails = itemView.findViewById(R.id.tvProductDetails);
            tvTotal = itemView.findViewById(R.id.tvProductTotal);
        }

        void bind(CartItem item) {
            tvName.setText(item.getProductName());
            tvDetails.setText(String.format(Locale.getDefault(), 
                "%.0f × ₹%.2f", item.getQuantity(), item.getRate()));
            tvTotal.setText(String.format(Locale.getDefault(), 
                "₹%.2f", item.getTaxableValue()));
        }
    }
}