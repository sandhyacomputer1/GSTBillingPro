package com.sandhyasofttech.gstbillingpro.soldproduct;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.sandhyasofttech.gstbillingpro.R;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SoldProductAdapter extends RecyclerView.Adapter<SoldProductAdapter.ViewHolder> {

    private List<SoldProductEntry> soldProducts;

    public SoldProductAdapter(List<SoldProductEntry> soldProducts) {
        this.soldProducts = new ArrayList<>(soldProducts);
    }

    public void updateList(List<SoldProductEntry> newList) {
        soldProducts.clear();
        soldProducts.addAll(newList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_sold_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SoldProductEntry entry = soldProducts.get(position);

        holder.tvProductName.setText(entry.productName);
        holder.tvCustomerName.setText("Customer: " + entry.customerName);
        holder.tvQuantity.setText(String.format(Locale.getDefault(), "Qty: %.2f", entry.quantity));
        holder.tvInvoiceDate.setText(entry.invoiceDate);
        holder.tvInvoiceNumber.setText("Invoice: " + entry.invoiceNumber);
    }

    @Override
    public int getItemCount() {
        return soldProducts.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvProductName, tvCustomerName, tvQuantity, tvInvoiceDate, tvInvoiceNumber;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvCustomerName = itemView.findViewById(R.id.tvCustomerName);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvInvoiceDate = itemView.findViewById(R.id.tvInvoiceDate);
            tvInvoiceNumber = itemView.findViewById(R.id.tvInvoiceNumber);
        }
    }
}