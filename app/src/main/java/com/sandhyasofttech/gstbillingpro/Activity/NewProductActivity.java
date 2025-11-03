package com.sandhyasofttech.gstbillingpro.Activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.sandhyasofttech.gstbillingpro.Model.Product;
import com.sandhyasofttech.gstbillingpro.R;

import java.util.UUID;

public class NewProductActivity extends AppCompatActivity {

    private TextInputEditText etProductName, etHSNCode, etPrice, etGSTRate, etStockQuantity;
    private TextInputLayout tilProductName, tilHSNCode, tilPrice, tilGSTRate, tilStockQuantity;
    private MaterialButton btnSaveProduct;

    private String userMobile;
    private DatabaseReference productsRef;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_product);

        etProductName = findViewById(R.id.etProductName);
        etHSNCode = findViewById(R.id.etHSNCode);
        etPrice = findViewById(R.id.etPrice);
        etGSTRate = findViewById(R.id.etGSTRate);
        etStockQuantity = findViewById(R.id.etStockQuantity);

        tilProductName = findViewById(R.id.tilProductName);
        tilHSNCode = findViewById(R.id.tilHSNCode);
        tilPrice = findViewById(R.id.tilPrice);
        tilGSTRate = findViewById(R.id.tilGSTRate);
        tilStockQuantity = findViewById(R.id.tilStockQuantity);

        btnSaveProduct = findViewById(R.id.btnSaveProduct);

        SharedPreferences prefs = getSharedPreferences("APP_PREFS", MODE_PRIVATE);
        userMobile = prefs.getString("USER_MOBILE", null);
        productsRef = FirebaseDatabase.getInstance().getReference("users").child(userMobile).child("products");

        btnSaveProduct.setOnClickListener(v -> saveProduct());
    }

    private void saveProduct() {
        clearErrors();

        String name = etProductName.getText().toString().trim();
        String hsn = etHSNCode.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();
        String gstStr = etGSTRate.getText().toString().trim();
        String stockStr = etStockQuantity.getText().toString().trim();

        boolean valid = true;

        if (TextUtils.isEmpty(name)) {
            tilProductName.setError("Enter product name");
            valid = false;
        }
        if (TextUtils.isEmpty(priceStr)) {
            tilPrice.setError("Enter price");
            valid = false;
        }
        if (TextUtils.isEmpty(gstStr)) {
            tilGSTRate.setError("Enter GST rate");
            valid = false;
        }
        if (TextUtils.isEmpty(stockStr)) {
            tilStockQuantity.setError("Enter stock quantity");
            valid = false;
        }
        if (!valid) return;

        double price = Double.parseDouble(priceStr);
        double gstRate = Double.parseDouble(gstStr);
        int stockQty = Integer.parseInt(stockStr);

        String productId = UUID.randomUUID().toString();

        Product product = new Product(productId, name, hsn, price, gstRate, stockQty);

        productsRef.child(productId).setValue(product).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Product saved successfully", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, "Failed to save product", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void clearErrors() {
        tilProductName.setError(null);
        tilHSNCode.setError(null);
        tilPrice.setError(null);
        tilGSTRate.setError(null);
        tilStockQuantity.setError(null);
    }
}
