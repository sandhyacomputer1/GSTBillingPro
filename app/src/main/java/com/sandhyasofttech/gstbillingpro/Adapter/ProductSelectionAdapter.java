//package com.sandhyasofttech.gstbillingpro.Adapter;
//
//import android.text.Editable;
//import android.text.TextWatcher;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.EditText;
//import android.widget.ImageButton;
//import android.widget.LinearLayout;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.google.android.material.card.MaterialCardView;
//import com.sandhyasofttech.gstbillingpro.Model.Product;
//import com.sandhyasofttech.gstbillingpro.R;
//
//import java.util.HashMap;
//import java.util.List;
//import java.util.Locale;
//import java.util.Map;
//
//public class ProductSelectionAdapter extends RecyclerView.Adapter<ProductSelectionAdapter.ViewHolder> {
//
//    private final List<Product> products;
//    private final OnSelectionChangeListener listener;
//
//    // Store selected products separately - DON'T modify Product objects
//    private final Map<String, SelectedProduct> selectedProducts = new HashMap<>();
//
//    public interface OnSelectionChangeListener {
//        void onSelectionChanged();
//    }
//
//    public static class SelectedProduct {
//        public double quantity;
//        public double price;
//
//        public SelectedProduct(double quantity, double price) {
//            this.quantity = quantity;
//            this.price = price;
//        }
//    }
//
//    public ProductSelectionAdapter(List<Product> products, OnSelectionChangeListener listener) {
//        this.products = products;
//        this.listener = listener;
//    }
//
//    // Public method to get selected products
//    public Map<String, SelectedProduct> getSelectedProducts() {
//        return selectedProducts;
//    }
//
//    @NonNull
//    @Override
//    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(parent.getContext())
//                .inflate(R.layout.item_product_selection, parent, false);
//        return new ViewHolder(view);
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
//        Product product = products.get(position);
//        holder.bind(product, selectedProducts.get(product.getProductId()));
//    }
//
//    @Override
//    public int getItemCount() {
//        return products.size();
//    }
//
//    class ViewHolder extends RecyclerView.ViewHolder {
//
//        MaterialCardView cardProduct;
//        TextView tvProductName, tvStock, tvHsnCode, tvSelectedInfo;
//        EditText etPrice, etQuantity;
//        LinearLayout layoutSelected;
//        ImageButton btnRemove;
//
//        private boolean isUpdating = false; // Prevent recursive updates
//
//        ViewHolder(View itemView) {
//            super(itemView);
//            cardProduct = itemView.findViewById(R.id.cardProduct);
//            tvProductName = itemView.findViewById(R.id.tvProductName);
//            tvStock = itemView.findViewById(R.id.tvStock);
//            tvHsnCode = itemView.findViewById(R.id.tvHsnCode);
//            etPrice = itemView.findViewById(R.id.etPrice);
//            etQuantity = itemView.findViewById(R.id.etQuantity);
//            layoutSelected = itemView.findViewById(R.id.layoutSelected);
//            tvSelectedInfo = itemView.findViewById(R.id.tvSelectedInfo);
//            btnRemove = itemView.findViewById(R.id.btnRemove);
//        }
//
//        void bind(Product product, SelectedProduct selected) {
//            if (isUpdating) return;
//            isUpdating = true;
//
//            String unit = product.getUnit() != null && !product.getUnit().isEmpty()
//                    ? product.getUnit() : "units";
//
//            // Set product details
//            tvProductName.setText(product.getName());
//            tvStock.setText(String.format("Stock: %d %s", product.getEffectiveQuantity(), unit));
//            tvHsnCode.setText(String.format("HSN: %s",
//                    product.getHsnCode() != null ? product.getHsnCode() : "N/A"));
//
//            // Clear previous listeners
//            etPrice.removeTextChangedListener(priceWatcher);
//            etQuantity.removeTextChangedListener(quantityWatcher);
//
//            // Set values based on selection state
//            if (selected != null) {
//                // Product is selected - show selected values
//                etPrice.setText(String.format(Locale.getDefault(), "%.2f", selected.price));
//                etQuantity.setText(String.format(Locale.getDefault(), "%.0f", selected.quantity));
//                updateSelectionUI(product, selected, unit);
//            } else {
//                // Product is not selected - show defaults
//                etPrice.setText(String.format(Locale.getDefault(), "%.2f", product.getPrice()));
//                etQuantity.setText("");
//                clearSelectionUI();
//            }
//
//            // Add listeners after setting values
//            etPrice.addTextChangedListener(priceWatcher);
//            etQuantity.addTextChangedListener(quantityWatcher);
//
//            // Remove button
//            btnRemove.setOnClickListener(v -> {
//                selectedProducts.remove(product.getProductId());
//                etQuantity.setText("");
//                clearSelectionUI();
//                notifyListener();
//                Toast.makeText(v.getContext(), "Removed from selection", Toast.LENGTH_SHORT).show();
//            });
//
//            isUpdating = false;
//        }
//
//        private final TextWatcher priceWatcher = new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {}
//
//            @Override
//            public void afterTextChanged(Editable s) {
//                if (isUpdating) return;
//
//                int position = getAdapterPosition();
//                if (position == RecyclerView.NO_POSITION) return;
//
//                Product product = products.get(position);
//                SelectedProduct selected = selectedProducts.get(product.getProductId());
//
//                String priceStr = s.toString().trim();
//                if (priceStr.isEmpty() || priceStr.equals(".")) return;
//
//                try {
//                    double price = Double.parseDouble(priceStr);
//                    if (price > 0 && selected != null) {
//                        // Update price for selected product
//                        selected.price = price;
//                        updateSelectionUI(product, selected,
//                                product.getUnit() != null ? product.getUnit() : "units");
//                        notifyListener();
//                    }
//                } catch (NumberFormatException ignored) {}
//            }
//        };
//
//        private final TextWatcher quantityWatcher = new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {}
//
//            @Override
//            public void afterTextChanged(Editable s) {
//                if (isUpdating) return;
//
//                int position = getAdapterPosition();
//                if (position == RecyclerView.NO_POSITION) return;
//
//                Product product = products.get(position);
//                String qtyStr = s.toString().trim();
//                String unit = product.getUnit() != null ? product.getUnit() : "units";
//
//                // If quantity is empty or 0, remove selection
//                if (qtyStr.isEmpty()) {
//                    selectedProducts.remove(product.getProductId());
//                    clearSelectionUI();
//                    notifyListener();
//                    return;
//                }
//
//                try {
//                    double qty = Double.parseDouble(qtyStr);
//
//                    if (qty <= 0) {
//                        // Remove selection
//                        selectedProducts.remove(product.getProductId());
//                        clearSelectionUI();
//                        notifyListener();
//                        return;
//                    }
//
//                    if (qty > product.getEffectiveQuantity()) {
//                        Toast.makeText(itemView.getContext(),
//                                "Only " + product.getEffectiveQuantity() + " " + unit + " available",
//                                Toast.LENGTH_SHORT).show();
//
//                        isUpdating = true;
//                        etQuantity.setText(String.valueOf(product.getEffectiveQuantity()));
//                        etQuantity.setSelection(etQuantity.getText().length());
//                        isUpdating = false;
//                        return;
//                    }
//
//                    // Get current price
//                    String priceStr = etPrice.getText().toString().trim();
//                    if (priceStr.isEmpty() || priceStr.equals(".")) {
//                        Toast.makeText(itemView.getContext(),
//                                "Enter valid price", Toast.LENGTH_SHORT).show();
//                        isUpdating = true;
//                        etQuantity.setText("");
//                        isUpdating = false;
//                        return;
//                    }
//
//                    double price = Double.parseDouble(priceStr);
//                    if (price <= 0) {
//                        Toast.makeText(itemView.getContext(),
//                                "Price must be greater than 0", Toast.LENGTH_SHORT).show();
//                        isUpdating = true;
//                        etQuantity.setText("");
//                        isUpdating = false;
//                        return;
//                    }
//
//                    // Add or update selection
//                    SelectedProduct selected = selectedProducts.get(product.getProductId());
//                    if (selected == null) {
//                        selected = new SelectedProduct(qty, price);
//                        selectedProducts.put(product.getProductId(), selected);
//                    } else {
//                        selected.quantity = qty;
//                    }
//
//                    updateSelectionUI(product, selected, unit);
//                    notifyListener();
//
//                } catch (NumberFormatException e) {
//                    selectedProducts.remove(product.getProductId());
//                    clearSelectionUI();
//                    notifyListener();
//                }
//            }
//        };
//
//        private void updateSelectionUI(Product product, SelectedProduct selected, String unit) {
//            if (selected != null && selected.quantity > 0) {
//                layoutSelected.setVisibility(View.VISIBLE);
//                cardProduct.setStrokeColor(0xFF4CAF50); // Green
//                cardProduct.setStrokeWidth(4);
//
//                tvSelectedInfo.setText(String.format(Locale.getDefault(),
//                        "Selected: %.0f %s @ ₹%.2f = ₹%.2f",
//                        selected.quantity,
//                        unit,
//                        selected.price,
//                        selected.quantity * selected.price));
//            }
//        }
//
//        private void clearSelectionUI() {
//            layoutSelected.setVisibility(View.GONE);
//            cardProduct.setStrokeColor(0xFFE0E0E0); // Gray
//            cardProduct.setStrokeWidth(2);
//        }
//
//        private void notifyListener() {
//            if (listener != null) {
//                listener.onSelectionChanged();
//            }
//        }
//    }
//}




package com.sandhyasofttech.gstbillingpro.Adapter;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.sandhyasofttech.gstbillingpro.Model.Product;
import com.sandhyasofttech.gstbillingpro.R;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ProductSelectionAdapter extends RecyclerView.Adapter<ProductSelectionAdapter.ViewHolder> {

    private final List<Product> products;
    private final OnSelectionChangeListener listener;

    // Store selected products separately - DON'T modify Product objects
    private final Map<String, SelectedProduct> selectedProducts = new HashMap<>();

    public interface OnSelectionChangeListener {
        void onSelectionChanged();
    }

    public static class SelectedProduct {
        public double quantity;
        public double price;

        public SelectedProduct(double quantity, double price) {
            this.quantity = quantity;
            this.price = price;
        }
    }

    public ProductSelectionAdapter(List<Product> products, OnSelectionChangeListener listener) {
        this.products = products;
        this.listener = listener;
    }

    // Public method to get selected products
    public Map<String, SelectedProduct> getSelectedProducts() {
        return selectedProducts;
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
        holder.bind(product, selectedProducts.get(product.getProductId()));
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        MaterialCardView cardProduct;
        TextView tvProductName, tvStock, tvSelectedInfo;
        EditText etPrice, etQuantity;
        LinearLayout layoutSelected;
        ImageButton btnRemove;

        private boolean isUpdating = false; // Prevent recursive updates

        ViewHolder(View itemView) {
            super(itemView);
            cardProduct = itemView.findViewById(R.id.cardProduct);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvStock = itemView.findViewById(R.id.tvStock);
            etPrice = itemView.findViewById(R.id.etPrice);
            etQuantity = itemView.findViewById(R.id.etQuantity);
            layoutSelected = itemView.findViewById(R.id.layoutSelected);
            tvSelectedInfo = itemView.findViewById(R.id.tvSelectedInfo);
            btnRemove = itemView.findViewById(R.id.btnRemove);
        }

        void bind(Product product, SelectedProduct selected) {
            if (isUpdating) return;
            isUpdating = true;

            String unit = product.getUnit() != null && !product.getUnit().isEmpty()
                    ? product.getUnit() : "units";

            // Set product details
            tvProductName.setText(product.getName());
            tvStock.setText(String.format(Locale.getDefault(), "%d %s",
                    product.getEffectiveQuantity(), unit));

            // Clear previous listeners
            etPrice.removeTextChangedListener(priceWatcher);
            etQuantity.removeTextChangedListener(quantityWatcher);

            // Set values based on selection state
            if (selected != null) {
                // Product is selected - show selected values
                etPrice.setText(String.format(Locale.getDefault(), "%.2f", selected.price));
                etQuantity.setText(String.format(Locale.getDefault(), "%.0f", selected.quantity));
                updateSelectionUI(product, selected, unit);
            } else {
                // Product is not selected - show defaults
                etPrice.setText(String.format(Locale.getDefault(), "%.2f", product.getPrice()));
                etQuantity.setText("");
                clearSelectionUI();
            }

            // Add listeners after setting values
            etPrice.addTextChangedListener(priceWatcher);
            etQuantity.addTextChangedListener(quantityWatcher);

            // Remove button
            btnRemove.setOnClickListener(v -> {
                selectedProducts.remove(product.getProductId());
                etQuantity.setText("");
                clearSelectionUI();
                notifyListener();
                Toast.makeText(v.getContext(), "Removed from selection", Toast.LENGTH_SHORT).show();
            });

            isUpdating = false;
        }

        private final TextWatcher priceWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (isUpdating) return;

                int position = getAdapterPosition();
                if (position == RecyclerView.NO_POSITION) return;

                Product product = products.get(position);
                SelectedProduct selected = selectedProducts.get(product.getProductId());

                String priceStr = s.toString().trim();
                if (priceStr.isEmpty() || priceStr.equals(".")) return;

                try {
                    double price = Double.parseDouble(priceStr);
                    if (price > 0 && selected != null) {
                        // Update price for selected product
                        selected.price = price;
                        updateSelectionUI(product, selected,
                                product.getUnit() != null ? product.getUnit() : "units");
                        notifyListener();
                    }
                } catch (NumberFormatException ignored) {}
            }
        };

        private final TextWatcher quantityWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (isUpdating) return;

                int position = getAdapterPosition();
                if (position == RecyclerView.NO_POSITION) return;

                Product product = products.get(position);
                String qtyStr = s.toString().trim();
                String unit = product.getUnit() != null ? product.getUnit() : "units";

                // If quantity is empty or 0, remove selection
                if (qtyStr.isEmpty()) {
                    selectedProducts.remove(product.getProductId());
                    clearSelectionUI();
                    notifyListener();
                    return;
                }

                try {
                    double qty = Double.parseDouble(qtyStr);

                    if (qty <= 0) {
                        // Remove selection
                        selectedProducts.remove(product.getProductId());
                        clearSelectionUI();
                        notifyListener();
                        return;
                    }

                    if (qty > product.getEffectiveQuantity()) {
                        Toast.makeText(itemView.getContext(),
                                "Only " + product.getEffectiveQuantity() + " " + unit + " available",
                                Toast.LENGTH_SHORT).show();

                        isUpdating = true;
                        etQuantity.setText(String.valueOf(product.getEffectiveQuantity()));
                        etQuantity.setSelection(etQuantity.getText().length());
                        isUpdating = false;
                        return;
                    }

                    // Get current price
                    String priceStr = etPrice.getText().toString().trim();
                    if (priceStr.isEmpty() || priceStr.equals(".")) {
                        Toast.makeText(itemView.getContext(),
                                "Enter valid price", Toast.LENGTH_SHORT).show();
                        isUpdating = true;
                        etQuantity.setText("");
                        isUpdating = false;
                        return;
                    }

                    double price = Double.parseDouble(priceStr);
                    if (price <= 0) {
                        Toast.makeText(itemView.getContext(),
                                "Price must be greater than 0", Toast.LENGTH_SHORT).show();
                        isUpdating = true;
                        etQuantity.setText("");
                        isUpdating = false;
                        return;
                    }

                    // Add or update selection
                    SelectedProduct selected = selectedProducts.get(product.getProductId());
                    if (selected == null) {
                        selected = new SelectedProduct(qty, price);
                        selectedProducts.put(product.getProductId(), selected);
                    } else {
                        selected.quantity = qty;
                    }

                    updateSelectionUI(product, selected, unit);
                    notifyListener();

                } catch (NumberFormatException e) {
                    selectedProducts.remove(product.getProductId());
                    clearSelectionUI();
                    notifyListener();
                }
            }
        };

        private void updateSelectionUI(Product product, SelectedProduct selected, String unit) {
            if (selected != null && selected.quantity > 0) {
                layoutSelected.setVisibility(View.VISIBLE);
                cardProduct.setStrokeColor(0xFF4CAF50); // Green
                cardProduct.setStrokeWidth(4);

                tvSelectedInfo.setText(String.format(Locale.getDefault(),
                        "%.0f %s × ₹%.2f = ₹%.2f",
                        selected.quantity,
                        unit,
                        selected.price,
                        selected.quantity * selected.price));
            }
        }

        private void clearSelectionUI() {
            layoutSelected.setVisibility(View.GONE);
            cardProduct.setStrokeColor(0xFFE0E0E0); // Gray
            cardProduct.setStrokeWidth(2);
        }

        private void notifyListener() {
            if (listener != null) {
                listener.onSelectionChanged();
            }
        }
    }
}