package com.sandhyasofttech.gstbillingpro.soldproduct;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sandhyasofttech.gstbillingpro.R;

import java.util.List;

public class SoldProductAdapter extends RecyclerView.Adapter<SoldProductAdapter.SoldProductViewHolder> {

    private List<SoldProductEntry> soldProducts;

    public SoldProductAdapter(List<SoldProductEntry> soldProducts) {
        this.soldProducts = soldProducts;
    }

    public void updateList(List<SoldProductEntry> newList) {
        soldProducts = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SoldProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sold_product, parent, false);
        return new SoldProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SoldProductViewHolder holder, int position) {
        SoldProductEntry entry = soldProducts.get(position);

        holder.tvProductName.setText(entry.productName);
        holder.tvCustomerName.setText("Customer: " + entry.customerName);
        holder.tvQuantity.setText(String.format("Qty: %.2f", entry.quantity));
        holder.tvInvoiceDate.setText(entry.invoiceDate);
        holder.tvInvoiceNumber.setText("Invoice: " + entry.invoiceNumber);

        // Toggle visibility on click
        holder.detailsLayout.setVisibility(View.GONE);
        holder.itemView.setOnClickListener(v -> {
            if (holder.detailsLayout.getVisibility() == View.VISIBLE) {
                holder.detailsLayout.setVisibility(View.GONE);
            } else {
                holder.detailsLayout.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public int getItemCount() {
        return soldProducts == null ? 0 : soldProducts.size();
    }

    static class SoldProductViewHolder extends RecyclerView.ViewHolder {
        TextView tvProductName, tvCustomerName, tvQuantity, tvInvoiceDate, tvInvoiceNumber;
        LinearLayout detailsLayout;

        public SoldProductViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvCustomerName = itemView.findViewById(R.id.tvCustomerName);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvInvoiceDate = itemView.findViewById(R.id.tvInvoiceDate);
            tvInvoiceNumber = itemView.findViewById(R.id.tvInvoiceNumber);
            detailsLayout = itemView.findViewById(R.id.detailsLayout);
        }
    }
}
