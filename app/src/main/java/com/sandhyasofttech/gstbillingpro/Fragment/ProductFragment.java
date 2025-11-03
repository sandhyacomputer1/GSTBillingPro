package com.sandhyasofttech.gstbillingpro.Fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.sandhyasofttech.gstbillingpro.Model.Product;
import com.sandhyasofttech.gstbillingpro.R;

import java.util.UUID;

public class ProductFragment extends Fragment {

    private EditText etProductName, etHSNCode, etPrice, etGSTRate, etStockQuantity;
    private Button btnSaveProduct;

    private String userMobile;
    private DatabaseReference productsRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_product, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etProductName = view.findViewById(R.id.etProductName);
        etHSNCode = view.findViewById(R.id.etHSNCode);
        etPrice = view.findViewById(R.id.etPrice);
        etGSTRate = view.findViewById(R.id.etGSTRate);
        etStockQuantity = view.findViewById(R.id.etStockQuantity);
        btnSaveProduct = view.findViewById(R.id.btnSaveProduct);

        SharedPreferences prefs = requireActivity().getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE);
        userMobile = prefs.getString("USER_MOBILE", null);

        if (userMobile == null) {
            Toast.makeText(getContext(), "Session expired. Please login again.", Toast.LENGTH_LONG).show();
            // TODO: Redirect to login
            return;
        }

        productsRef = FirebaseDatabase.getInstance().getReference("users").child(userMobile).child("products");

        btnSaveProduct.setOnClickListener(v -> saveProduct());
    }

    private void saveProduct() {
        String name = etProductName.getText().toString().trim();
        String hsn = etHSNCode.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();
        String gstStr = etGSTRate.getText().toString().trim();
        String stockStr = etStockQuantity.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            etProductName.setError("Enter product name");
            etProductName.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(priceStr)) {
            etPrice.setError("Enter price");
            etPrice.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(gstStr)) {
            etGSTRate.setError("Enter GST rate");
            etGSTRate.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(stockStr)) {
            etStockQuantity.setError("Enter stock quantity");
            etStockQuantity.requestFocus();
            return;
        }

        double price = Double.parseDouble(priceStr);
        double gstRate = Double.parseDouble(gstStr);
        int stockQuantity = Integer.parseInt(stockStr);

        // Create unique product ID
        String productId = UUID.randomUUID().toString();

        Product product = new Product(productId, name, hsn, price, gstRate, stockQuantity);

        productsRef.child(productId).setValue(product).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(getContext(), "Product saved successfully", Toast.LENGTH_SHORT).show();
                clearFields();
            } else {
                Toast.makeText(getContext(), "Failed to save product", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void clearFields() {
        etProductName.setText("");
        etHSNCode.setText("");
        etPrice.setText("");
        etGSTRate.setText("");
        etStockQuantity.setText("");
    }
}
