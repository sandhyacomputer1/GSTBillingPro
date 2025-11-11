package com.sandhyasofttech.gstbillingpro.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sandhyasofttech.gstbillingpro.Model.Product;
import com.sandhyasofttech.gstbillingpro.R;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class NewProductActivity extends AppCompatActivity {

    private TextInputEditText etProductName, etHSNCode, etPrice, etGSTRate, etStockQuantity;
    private TextInputLayout tilProductName, tilHSNCode, tilPrice, tilGSTRate, tilStockQuantity;
    private MaterialButton btnSaveProduct, btnAddYours;
    private LinearLayout llCustomFieldsContainer;

    private String userMobile;
    private DatabaseReference productsRef;
    private List<String> customFields;
    private Map<String, TextInputEditText> customFieldEditTexts;
    private boolean keepDefaultFields;
    private String primaryField;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_product);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Add Product");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

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
        btnAddYours = findViewById(R.id.customfields);
        llCustomFieldsContainer = findViewById(R.id.llCustomFieldsContainer);

        SharedPreferences prefs = getSharedPreferences("APP_PREFS", MODE_PRIVATE);
        userMobile = prefs.getString("USER_MOBILE", null);
        keepDefaultFields = prefs.getBoolean("KEEP_DEFAULT_FIELDS", true);
        primaryField = prefs.getString("PRIMARY_FIELD", null);
        productsRef = FirebaseDatabase.getInstance().getReference("users").child(userMobile).child("products");

        loadCustomFields(prefs);
        updateDefaultFieldsVisibility();
        addCustomFieldViews();

        btnSaveProduct.setOnClickListener(v -> saveProduct());

        btnAddYours.setOnClickListener(v -> {
            Intent intent = new Intent(NewProductActivity.this, CustomFieldsActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        // Reload fields when returning from CustomFieldsActivity
        recreate();
    }

    private void loadCustomFields(SharedPreferences prefs) {
        Gson gson = new Gson();
        String json = prefs.getString("CUSTOM_FIELDS", null);
        if (json != null) {
            Type type = new TypeToken<ArrayList<String>>() {}.getType();
            customFields = gson.fromJson(json, type);
        } else {
            customFields = new ArrayList<>();
        }
    }

    private void updateDefaultFieldsVisibility() {
        if (!keepDefaultFields) {
            tilProductName.setVisibility(View.GONE);
            tilHSNCode.setVisibility(View.GONE);
            tilPrice.setVisibility(View.GONE);
            tilGSTRate.setVisibility(View.GONE);
            tilStockQuantity.setVisibility(View.GONE);
        }
    }

    private void addCustomFieldViews() {
        customFieldEditTexts = new HashMap<>();
        LayoutInflater inflater = LayoutInflater.from(this);
        for (String fieldName : customFields) {
            TextInputLayout textInputLayout = (TextInputLayout) inflater.inflate(R.layout.custom_field_input, llCustomFieldsContainer, false);
            TextInputEditText editText = textInputLayout.findViewById(R.id.etCustomField);
            textInputLayout.setHint(fieldName);

            llCustomFieldsContainer.addView(textInputLayout);
            customFieldEditTexts.put(fieldName, editText);
        }
    }

    private void saveProduct() {
        clearErrors();

        String name, hsn;
        String priceStr, gstStr, stockStr;

        boolean valid = true;
        Map<String, String> customFieldValues = new HashMap<>();
        for (Map.Entry<String, TextInputEditText> entry : customFieldEditTexts.entrySet()) {
            customFieldValues.put(entry.getKey(), entry.getValue().getText().toString().trim());
        }

        if (keepDefaultFields) {
            name = etProductName.getText().toString().trim();
            hsn = etHSNCode.getText().toString().trim();
            priceStr = etPrice.getText().toString().trim();
            gstStr = etGSTRate.getText().toString().trim();
            stockStr = etStockQuantity.getText().toString().trim();

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
        } else {
            if (primaryField != null && customFieldValues.containsKey(primaryField)) {
                name = customFieldValues.get(primaryField);
                if (TextUtils.isEmpty(name)) {
                    // Find the TextInputLayout for the primary field to set an error
                    for (int i = 0; i < llCustomFieldsContainer.getChildCount(); i++) {
                        View child = llCustomFieldsContainer.getChildAt(i);
                        if (child instanceof TextInputLayout) {
                            TextInputLayout til = (TextInputLayout) child;
                            if (til.getHint() != null && til.getHint().toString().equals(primaryField)) {
                                til.setError("Please enter a value for the primary field");
                                break;
                            }
                        }
                    }
                    valid = false;
                }
            } else {
                Toast.makeText(this, "Please select a primary field in the custom fields settings.", Toast.LENGTH_LONG).show();
                return;
            }
            // Set default values for hidden fields
            hsn = "";
            priceStr = "0";
            gstStr = "0";
            stockStr = "0";
        }

        if (!valid) return;

        double price = Double.parseDouble(priceStr);
        double gstRate = Double.parseDouble(gstStr);
        int stockQty = Integer.parseInt(stockStr);

        String productId = UUID.randomUUID().toString();

        Product product = new Product(productId, name, hsn, price, gstRate, stockQty, null);
        product.setCustomFields(customFieldValues);

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
        if (keepDefaultFields) {
            tilProductName.setError(null);
            tilHSNCode.setError(null);
            tilPrice.setError(null);
            tilGSTRate.setError(null);
            tilStockQuantity.setError(null);
        }
        for (int i = 0; i < llCustomFieldsContainer.getChildCount(); i++) {
            View child = llCustomFieldsContainer.getChildAt(i);
            if (child instanceof TextInputLayout) {
                ((TextInputLayout) child).setError(null);
            }
        }
    }
}
