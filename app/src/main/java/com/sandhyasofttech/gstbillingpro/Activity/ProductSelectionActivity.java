package com.sandhyasofttech.gstbillingpro.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sandhyasofttech.gstbillingpro.Adapter.ProductSelectionAdapter;
import com.sandhyasofttech.gstbillingpro.Adapter.CartAdapter;
import com.sandhyasofttech.gstbillingpro.Model.Product;
import com.sandhyasofttech.gstbillingpro.Model.CartItem;
import com.sandhyasofttech.gstbillingpro.R;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProductSelectionActivity extends AppCompatActivity {

    private RecyclerView rvProducts, rvCart;
    private EditText etSearch;
    private ProgressBar progressBar;
    private TextView tvEmpty, tvCustomerName, tvCartTotal, tvCartCount;
    private MaterialButton btnProceed;

    private ProductSelectionAdapter productAdapter;
    private CartAdapter cartAdapter;
    private List<Product> productList = new ArrayList<>();
    private List<Product> filteredList = new ArrayList<>();
    private ArrayList<CartItem> cartItems = new ArrayList<>();

    private DatabaseReference productsRef;
    private String userMobile, customerName, customerPhone, customerAddress;
    private double cartTotal = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_selection);

        // Get customer details
        customerName = getIntent().getStringExtra("CUSTOMER_NAME");
        customerPhone = getIntent().getStringExtra("CUSTOMER_PHONE");
        customerAddress = getIntent().getStringExtra("CUSTOMER_ADDRESS");

        // Initialize views
        rvProducts = findViewById(R.id.rvProducts);
        rvCart = findViewById(R.id.rvCart);
        etSearch = findViewById(R.id.etSearch);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);
        tvCustomerName = findViewById(R.id.tvCustomerName);
        tvCartTotal = findViewById(R.id.tvCartTotal);
        tvCartCount = findViewById(R.id.tvCartCount);
        btnProceed = findViewById(R.id.btnProceed);

        tvCustomerName.setText("Customer: " + customerName);

        // Get user mobile
        SharedPreferences prefs = getSharedPreferences("APP_PREFS", MODE_PRIVATE);
        userMobile = prefs.getString("USER_MOBILE", null);

        if (userMobile == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize Firebase
        productsRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userMobile)
                .child("products");

        // Setup RecyclerViews
        setupProductRecyclerView();
        setupCartRecyclerView();

        // Setup search
        setupSearch();

        // Load products
        loadProducts();

        // Proceed button
        btnProceed.setOnClickListener(v -> {
            if (cartItems.isEmpty()) {
                Toast.makeText(this, "Please add at least one product", Toast.LENGTH_SHORT).show();
                return;
            }
            proceedToPayment();
        });

        // Back button
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        updateCartSummary();
    }

    private void setupProductRecyclerView() {
        productAdapter = new ProductSelectionAdapter(filteredList, this::showQuantityDialog);
        rvProducts.setLayoutManager(new LinearLayoutManager(this));
        rvProducts.setAdapter(productAdapter);
    }

    private void setupCartRecyclerView() {
        cartAdapter = new CartAdapter(cartItems, new CartAdapter.CartListener() {
            @Override
            public void onQuantityChanged() {
                updateCartSummary();
            }

            @Override
            public void onItemRemoved(int position) {
                cartItems.remove(position);
                cartAdapter.notifyItemRemoved(position);
                updateCartSummary();
            }
        });
        rvCart.setLayoutManager(new LinearLayoutManager(this));
        rvCart.setAdapter(cartAdapter);
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterProducts(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterProducts(String query) {
        filteredList.clear();
        if (query.isEmpty()) {
            filteredList.addAll(productList);
        } else {
            String lowerQuery = query.toLowerCase();
            for (Product product : productList) {
                if (product.getName().toLowerCase().contains(lowerQuery)) {
                    filteredList.add(product);
                }
            }
        }
        productAdapter.notifyDataSetChanged();
        updateEmptyView();
    }

    private void loadProducts() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);

        productsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                productList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Product product = ds.getValue(Product.class);
                    if (product != null && product.getEffectiveQuantity() > 0) {
                        productList.add(product);
                    }
                }
                filteredList.clear();
                filteredList.addAll(productList);
                productAdapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
                updateEmptyView();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ProductSelectionActivity.this, 
                    "Failed to load products", Toast.LENGTH_SHORT).show();
                updateEmptyView();
            }
        });
    }

    private void showQuantityDialog(Product product) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_quantity, null);
        builder.setView(dialogView);

        TextView tvProductName = dialogView.findViewById(R.id.tvProductName);
        TextView tvStock = dialogView.findViewById(R.id.tvStock);
        TextView tvPrice = dialogView.findViewById(R.id.tvPrice);
        EditText etQuantity = dialogView.findViewById(R.id.etQuantity);
        MaterialButton btnAdd = dialogView.findViewById(R.id.btnAdd);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btnCancel);

        String unit = product.getUnit() != null ? product.getUnit() : "";

        tvProductName.setText(product.getName());
        tvStock.setText("Available: " + product.getEffectiveQuantity() + " " + unit);
        tvPrice.setText(String.format(
                Locale.getDefault(),
                "₹%.2f per %s",
                product.getPrice(),
                unit.isEmpty() ? "unit" : unit
        ));

        AlertDialog dialog = builder.create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnAdd.setOnClickListener(v -> {

            String qtyStr = etQuantity.getText().toString().trim();
            if (qtyStr.isEmpty()) {
                Toast.makeText(this, "Enter quantity", Toast.LENGTH_SHORT).show();
                return;
            }

            double qty = Double.parseDouble(qtyStr);

            if (qty <= 0 || qty > product.getEffectiveQuantity()) {
                Toast.makeText(this,
                        "Only " + product.getEffectiveQuantity() + " " + unit + " available",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            boolean found = false;
            for (CartItem item : cartItems) {
                if (item.getProductId().equals(product.getProductId())) {
                    item.setQuantity(item.getQuantity() + qty);
                    found = true;
                    break;
                }
            }

            if (!found) {
                CartItem cartItem = new CartItem(
                        product.getProductId(),
                        product.getName(),
                        qty,                     // ✅ USER ENTERED QTY
                        product.getPrice(),
                        product.getGstRate(),
                        product.getStockQuantity(),
                        product.getUnit()         // ✅ UNIT PASSED
                );
                cartItems.add(cartItem);
            }

            cartAdapter.notifyDataSetChanged();
            updateCartSummary();
            dialog.dismiss();
            Toast.makeText(this, "Added to cart", Toast.LENGTH_SHORT).show();
        });

        dialog.show();
    }

    private void updateCartSummary() {
        cartTotal = 0;
        for (CartItem item : cartItems) {
            cartTotal += item.getTaxableValue();
        }

        tvCartCount.setText(cartItems.size() + " items");
        tvCartTotal.setText(String.format(Locale.getDefault(), "Total: ₹%.2f", cartTotal));

        // Show/hide cart section
        findViewById(R.id.cvCart).setVisibility(cartItems.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void proceedToPayment() {
        Intent intent = new Intent(this, PaymentActivity.class);
        intent.putExtra("CUSTOMER_NAME", customerName);
        intent.putExtra("CUSTOMER_PHONE", customerPhone);
        intent.putExtra("CUSTOMER_ADDRESS", customerAddress);
        intent.putExtra("CART_ITEMS", cartItems);
        intent.putExtra("CART_TOTAL", cartTotal);
        startActivity(intent);
        finish();
    }

    private void updateEmptyView() {
        if (filteredList.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            tvEmpty.setText(productList.isEmpty() ? 
                "No products available" : 
                "No results found");
        } else {
            tvEmpty.setVisibility(View.GONE);
        }
    }
}