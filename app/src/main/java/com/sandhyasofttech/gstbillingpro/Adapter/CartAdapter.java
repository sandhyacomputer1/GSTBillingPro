package com.sandhyasofttech.gstbillingpro.Adapter;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sandhyasofttech.gstbillingpro.Model.CartItem;
import com.sandhyasofttech.gstbillingpro.R;

import java.util.List;
import java.util.Locale;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {

    private List<CartItem> cartItems;
    private CartListener listener;

    public interface CartListener {
        void onQuantityChanged();
        void onItemRemoved(int position);
    }

    public CartAdapter(List<CartItem> cartItems, CartListener listener) {
        this.cartItems = cartItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cart, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CartItem item = cartItems.get(position);
        holder.bind(item, position);
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPrice, tvTotal;
        EditText etQuantity;
        ImageButton btnRemove, btnMinus, btnPlus;

        ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvProductName);
            tvPrice = itemView.findViewById(R.id.tvProductPrice);
            tvTotal = itemView.findViewById(R.id.tvProductTotal);
            etQuantity = itemView.findViewById(R.id.etQuantity);
            btnRemove = itemView.findViewById(R.id.btnRemove);
            btnMinus = itemView.findViewById(R.id.btnMinus);
            btnPlus = itemView.findViewById(R.id.btnPlus);
        }

        void bind(CartItem item, int position) {
            tvName.setText(item.getProductName());
            tvPrice.setText(String.format(Locale.getDefault(), "₹%.2f x %.0f", 
                item.getRate(), item.getQuantity()));
            tvTotal.setText(String.format(Locale.getDefault(), "₹%.2f", 
                item.getTaxableValue()));
            
            etQuantity.setText(String.format(Locale.getDefault(), "%.0f", item.getQuantity()));

            // Remove button
            btnRemove.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemRemoved(position);
                }
            });

            // Minus button
            btnMinus.setOnClickListener(v -> {
                double currentQty = item.getQuantity();
                if (currentQty > 1) {
                    item.setQuantity(currentQty - 1);
                    etQuantity.setText(String.format(Locale.getDefault(), "%.0f", item.getQuantity()));
                    updateDisplay(item);
                    if (listener != null) listener.onQuantityChanged();
                }
            });

            // Plus button
            btnPlus.setOnClickListener(v -> {
                double currentQty = item.getQuantity();
                if (currentQty < item.getMaxStock()) {
                    item.setQuantity(currentQty + 1);
                    etQuantity.setText(String.format(Locale.getDefault(), "%.0f", item.getQuantity()));
                    updateDisplay(item);
                    if (listener != null) listener.onQuantityChanged();
                }
            });

            // Quantity text change
            etQuantity.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (!s.toString().isEmpty()) {
                        try {
                            double qty = Double.parseDouble(s.toString());
                            if (qty > 0 && qty <= item.getMaxStock()) {
                                item.setQuantity(qty);
                                updateDisplay(item);
                                if (listener != null) listener.onQuantityChanged();
                            }
                        } catch (NumberFormatException e) {
                            // Ignore invalid input
                        }
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        void updateDisplay(CartItem item) {
            tvPrice.setText(String.format(Locale.getDefault(), "₹%.2f x %.0f", 
                item.getRate(), item.getQuantity()));
            tvTotal.setText(String.format(Locale.getDefault(), "₹%.2f", 
                item.getTaxableValue()));
        }
    }
}