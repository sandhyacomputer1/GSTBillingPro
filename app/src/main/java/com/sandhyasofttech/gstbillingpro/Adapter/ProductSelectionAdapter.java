package com.sandhyasofttech.gstbillingpro.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.sandhyasofttech.gstbillingpro.Model.Product;
import com.sandhyasofttech.gstbillingpro.R;

import java.util.List;
import java.util.Locale;

public class ProductSelectionAdapter extends RecyclerView.Adapter<ProductSelectionAdapter.ViewHolder> {

    private List<Product> products;
    private OnProductClickListener listener;

    public interface OnProductClickListener {
        void onProductClick(Product product);
    }

    public ProductSelectionAdapter(List<Product> products, OnProductClickListener listener) {
        this.products = products;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product_selection, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = products.get(position);
        holder.bind(product);
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardView;
        TextView tvName, tvPrice, tvStock, tvGst;

        ViewHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardProduct);
            tvName = itemView.findViewById(R.id.tvProductName);
            tvPrice = itemView.findViewById(R.id.tvProductPrice);
            tvStock = itemView.findViewById(R.id.tvProductStock);
            tvGst = itemView.findViewById(R.id.tvProductGst);
        }

        void bind(Product product) {
            tvName.setText(product.getName());
            tvPrice.setText(String.format(Locale.getDefault(), "₹%.2f", product.getPrice()));
            tvStock.setText("Stock: " + product.getEffectiveQuantity());
            tvGst.setText("GST: " + product.getGstRate() + "%");

            // Change stock color based on availability
            if (product.getEffectiveQuantity() < 10) {
                tvStock.setTextColor(0xFFFF5722); // Red for low stock
            } else {
                tvStock.setTextColor(0xFF4CAF50); // Green for good stock
            }

            cardView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onProductClick(product);
                }
            });
        }
    }
}