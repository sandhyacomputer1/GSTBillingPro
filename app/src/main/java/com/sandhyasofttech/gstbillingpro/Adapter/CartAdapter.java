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

    private final List<CartItem> cartItems;
    private final CartListener listener;

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
        holder.bind(cartItems.get(position), position);
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

            final String unit = item.getUnit() == null ? "" : item.getUnit();

            tvName.setText(item.getProductName());
            updateDisplay(item, unit);

            etQuantity.setText(unit.isEmpty()
                    ? String.valueOf((int) item.getQuantity())
                    : ((int) item.getQuantity()) + " " + unit);

            btnRemove.setOnClickListener(v -> {
                if (listener != null) listener.onItemRemoved(position);
            });

            btnMinus.setOnClickListener(v -> {
                if (item.getQuantity() > 1) {
                    item.setQuantity(item.getQuantity() - 1);
                    updateDisplay(item, unit);
                    if (listener != null) listener.onQuantityChanged();
                }
            });

            btnPlus.setOnClickListener(v -> {
                if (item.getQuantity() < item.getMaxStock()) {
                    item.setQuantity(item.getQuantity() + 1);
                    updateDisplay(item, unit);
                    if (listener != null) listener.onQuantityChanged();
                }
            });

            etQuantity.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
                @Override public void afterTextChanged(Editable s) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    try {
                        String clean = s.toString().replace(unit, "").trim();
                        if (!clean.isEmpty()) {
                            double qty = Double.parseDouble(clean);
                            if (qty > 0 && qty <= item.getMaxStock()) {
                                item.setQuantity(qty);
                                updateDisplay(item, unit);
                                if (listener != null) listener.onQuantityChanged();
                            }
                        }
                    } catch (Exception ignored) {}
                }
            });
        }

        void updateDisplay(CartItem item, String unit) {

            String qtyText = unit.isEmpty()
                    ? String.format(Locale.getDefault(), "%.0f", item.getQuantity())
                    : String.format(Locale.getDefault(), "%.0f %s", item.getQuantity(), unit);

            tvPrice.setText(String.format(
                    Locale.getDefault(),
                    "₹%.2f x %s",
                    item.getRate(),
                    qtyText
            ));

            tvTotal.setText(String.format(
                    Locale.getDefault(),
                    "₹%.2f",
                    item.getTaxableValue()
            ));
        }
    }
}
