package com.sandhyasofttech.gstbillingpro.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sandhyasofttech.gstbillingpro.Model.Product;
import com.sandhyasofttech.gstbillingpro.R;

import java.util.ArrayList;

public class ProductsAdapter extends RecyclerView.Adapter<ProductsAdapter.ProductViewHolder> {

    private ArrayList<Product> products;
    private OnProductActionListener listener;

    public interface OnProductActionListener {
        void onEditProduct(Product product, int position);
        void onDeleteProduct(Product product, int position);
    }

    public ProductsAdapter(ArrayList<Product> products, OnProductActionListener listener) {
        this.products = products;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = products.get(position);

        holder.tvName.setText(product.getName());

        holder.tvPrice.setText(" ₹" + String.format("%.2f", product.getPrice()));

        holder.tvQuantity.setText(" " + product.getStockQuantity());

        holder.tvHsn.setText(product.getHsnCode());

        holder.tvGst.setText(String.format("%.2f%%", product.getGstRate()));

        double amount = product.getPrice() * product.getStockQuantity() * (1 + product.getGstRate() / 100.0);
        holder.tvAmount.setText(" ₹" + String.format("%.2f", amount));

        holder.ivEdit.setOnClickListener(v -> listener.onEditProduct(product, position));
        holder.ivDelete.setOnClickListener(v -> listener.onDeleteProduct(product, position));
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPrice, tvQuantity, tvHsn, tvGst, tvAmount;
        ImageView ivEdit, ivDelete;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvProductName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvHsn = itemView.findViewById(R.id.tvHsn);
            tvGst = itemView.findViewById(R.id.tvGst);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            ivEdit = itemView.findViewById(R.id.btnEdit);
            ivDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
