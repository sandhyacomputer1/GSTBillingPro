package com.sandhyasofttech.gstbillingpro.invoice;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sandhyasofttech.gstbillingpro.R;

import java.util.List;

public class InvoiceItemAdapter extends RecyclerView.Adapter<InvoiceItemAdapter.ItemViewHolder> {

    public interface OnItemChangedListener {
        void onItemChanged();
    }

    private final List<InvoiceItem> items;
    private final OnItemChangedListener listener;

    public InvoiceItemAdapter(List<InvoiceItem> items, OnItemChangedListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_invoice_product, parent, false);
        return new ItemViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        InvoiceItem item = items.get(position);
        holder.tvProductName.setText(item.productName);
        holder.etQuantity.setText(String.valueOf(item.quantity));
        holder.tvRate.setText(String.format("Rate: ₹%.2f", item.rate));
        holder.tvTaxPercent.setText(String.format("GST: %.1f%%", item.taxPercent));

        holder.etQuantity.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override
            public void afterTextChanged(Editable s) {
                try {
                    double qty = Double.parseDouble(s.toString());
                    item.quantity = qty;
                    listener.onItemChanged();
                } catch(Exception e) {
                    // Invalid input; ignore or reset if desired
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView tvProductName, tvRate, tvTaxPercent;
        EditText etQuantity;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvRate = itemView.findViewById(R.id.tvRate);
            tvTaxPercent = itemView.findViewById(R.id.tvTaxPercent);
            etQuantity = itemView.findViewById(R.id.etQuantity);
        }
    }
}
