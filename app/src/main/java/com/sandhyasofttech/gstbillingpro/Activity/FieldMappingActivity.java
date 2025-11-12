package com.sandhyasofttech.gstbillingpro.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sandhyasofttech.gstbillingpro.R;
import com.sandhyasofttech.gstbillingpro.adapter.FieldMappingAdapter;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FieldMappingActivity extends AppCompatActivity {

    private RecyclerView rvFieldMapping;
    private FieldMappingAdapter adapter;
    private List<String> appFields;
    private ArrayList<String> fileColumns;
    private SharedPreferences prefs;
    private Gson gson;
    private Uri fileUri;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_field_mapping);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Map Fields");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        fileUri = getIntent().getParcelableExtra("fileUri");
        fileColumns = getIntent().getStringArrayListExtra("fileColumns");
        fileColumns.add(0, "-- Not Mapped --"); // Add a default "not mapped" option

        prefs = getSharedPreferences("APP_PREFS", MODE_PRIVATE);
        gson = new Gson();

        loadAppFields();
        loadSavedMapping();

        rvFieldMapping = findViewById(R.id.rvFieldMapping);
        rvFieldMapping.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FieldMappingAdapter(this, appFields, fileColumns, loadSavedMapping());
        rvFieldMapping.setAdapter(adapter);

        findViewById(R.id.btnSaveMapping).setOnClickListener(v -> saveMappingAndStartImport());
    }

    private void loadAppFields() {
        appFields = new ArrayList<>();
        appFields.add("Product Name");
        appFields.add("HSN Code");
        appFields.add("Price");
        appFields.add("GST Rate");
        appFields.add("Stock Quantity");
        appFields.add("Unit");

        // Load custom fields
        String json = prefs.getString("CUSTOM_FIELDS", null);
        if (json != null) {
            Type type = new TypeToken<ArrayList<String>>() {}.getType();
            List<String> customFields = gson.fromJson(json, type);
            appFields.addAll(customFields);
        }
    }

    private Map<String, Integer> loadSavedMapping() {
        String json = prefs.getString("FIELD_MAPPING", null);
        if (json != null) {
            Type type = new TypeToken<Map<String, Integer>>() {}.getType();
            return gson.fromJson(json, type);
        }
        return new HashMap<>();
    }

    private void saveMappingAndStartImport() {
        Map<String, Integer> mapping = new HashMap<>();
        for (int i = 0; i < adapter.getItemCount(); i++) {
            View view = rvFieldMapping.getChildAt(i);
            Spinner spinner = view.findViewById(R.id.spinnerFileColumn);
            mapping.put(appFields.get(i), spinner.getSelectedItemPosition());
        }

        String json = gson.toJson(mapping);
        prefs.edit().putString("FIELD_MAPPING", json).apply();

        Toast.makeText(this, "Mapping saved!", Toast.LENGTH_SHORT).show();

        // Pass the URI and the mapping back to the fragment
        Intent resultIntent = new Intent();
        resultIntent.putExtra("fileUri", fileUri);
        setResult(RESULT_OK, resultIntent);
        finish();
    }
}
