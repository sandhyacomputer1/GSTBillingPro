//package com.sandhyasofttech.gstbillingpro.Fragment;
//
//import android.content.Context;
//import android.content.Intent;
//import android.content.SharedPreferences;
//import android.net.Uri;
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.Looper;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.SearchView;
//import android.widget.Toast;
//import java.util.List; // Make sure this import is present
//
//import androidx.activity.result.ActivityResultLauncher;
//import androidx.activity.result.contract.ActivityResultContracts;
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.appcompat.app.AlertDialog;
//import androidx.fragment.app.Fragment;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.google.android.material.button.MaterialButton;
//import com.google.android.material.floatingactionbutton.FloatingActionButton;
//import com.google.firebase.database.DataSnapshot;
//import com.google.firebase.database.DatabaseError;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
//import com.google.firebase.database.ValueEventListener;
//import com.itextpdf.kernel.pdf.PdfDocument;
//import com.itextpdf.kernel.pdf.PdfReader;
//import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;
//import com.itextpdf.kernel.pdf.canvas.parser.listener.LocationTextExtractionStrategy;
//import com.opencsv.CSVReader;
//import com.opencsv.exceptions.CsvValidationException;
//import com.sandhyasofttech.gstbillingpro.Activity.NewProductActivity;
//import com.sandhyasofttech.gstbillingpro.Adapter.ProductsAdapter;
//import com.sandhyasofttech.gstbillingpro.Model.Product;
//import com.sandhyasofttech.gstbillingpro.R;
//
//import org.apache.poi.ss.usermodel.Cell;
//import org.apache.poi.ss.usermodel.Row;
//import org.apache.poi.ss.usermodel.Sheet;
//import org.apache.poi.ss.usermodel.Workbook;
//import org.apache.poi.ss.usermodel.WorkbookFactory;
//
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.UUID;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//
//public class ProductFragment extends Fragment implements ProductsAdapter.OnProductActionListener {
//
//    private static final String TAG = "ProductFragment"; // For logging
//
//    private RecyclerView rvProducts;
//    private FloatingActionButton fabAddProduct;
//    private MaterialButton btnImportProducts;
//    private SearchView searchView;
//
//    private ProductsAdapter adapter;
//    private final List<Product> productList = new ArrayList<>();
//    private final List<Product> filteredList = new ArrayList<>();
//
//    private DatabaseReference productsRef;
//    private String userMobile;
//
//    // Use Java's modern ExecutorService for cleaner background threading
//    private final ExecutorService executor = Executors.newSingleThreadExecutor();
//    private final Handler handler = new Handler(Looper.getMainLooper());
//
//    // Modern way to handle Activity results, replacing onActivityResult
//    private final ActivityResultLauncher<Intent> filePickerLauncher = registerForActivityResult(
//            new ActivityResultContracts.StartActivityForResult(),
//            result -> {
//                if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {
//                    Uri uri = result.getData().getData();
//                    if (uri != null) {
//                        importDataFromFile(uri);
//                    } else {
//                        showToast("Failed to get file URI.");
//                    }
//                }
//            });
//
//    @Nullable
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        return inflater.inflate(R.layout.fragment_product, container, false);
//    }
//
//    @Override
//    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);
//
//        // Initialize UI components
//        rvProducts = view.findViewById(R.id.rvProducts);
//        fabAddProduct = view.findViewById(R.id.fabAddProduct);
//        btnImportProducts = view.findViewById(R.id.btnImportProducts);
//        searchView = view.findViewById(R.id.searchView);
//
//        // Get user session data
//        SharedPreferences prefs = requireActivity().getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE);
//        userMobile = prefs.getString("USER_MOBILE", null);
//
//        // Ensure user is logged in
//        if (userMobile == null || userMobile.isEmpty()) {
//            showToast("User not logged in. Cannot load data.");
//            return;
//        }
//
//        // Setup Firebase reference
//        productsRef = FirebaseDatabase.getInstance().getReference("users").child(userMobile).child("products");
//
//        // Setup RecyclerView
//        setupRecyclerView();
//
//        // Load existing products from Firebase
//        loadProducts();
//
//        // Setup button and search listeners
//        setupListeners();
//    }
//
//    private void setupRecyclerView() {
//        adapter = new ProductsAdapter(filteredList, this);
//        rvProducts.setLayoutManager(new LinearLayoutManager(getContext()));
//        rvProducts.setAdapter(adapter);
//    }
//
//    private void setupListeners() {
//        fabAddProduct.setOnClickListener(v -> startActivity(new Intent(getContext(), NewProductActivity.class)));
//        btnImportProducts.setOnClickListener(v -> openFilePicker());
//
//        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//            @Override
//            public boolean onQueryTextSubmit(String query) {
//                return false; // No action on submit
//            }
//
//            @Override
//            public boolean onQueryTextChange(String newText) {
//                filterProducts(newText);
//                return true;
//            }
//        });
//    }
//
//    private void openFilePicker() {
//        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//        intent.setType("*/*"); // Allows selection of any file type
//        intent.addCategory(Intent.CATEGORY_OPENABLE);
//        // Specify the MIME types you want to handle
//        intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{
//                "application/pdf",                                          // .pdf
//                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", // .xlsx
//                "application/vnd.ms-excel",                                 // .xls
//                "text/csv"                                                  // .csv
//        });
//
//        filePickerLauncher.launch(Intent.createChooser(intent, "Select a file to import"));
//    }
//
//    private void importDataFromFile(final Uri uri) {
//        // Run file processing on a background thread
//        executor.execute(() -> {
//            List<Product> importedProducts = new ArrayList<>();
//            String errorMsg = null;
//
//            try (InputStream inputStream = requireContext().getContentResolver().openInputStream(uri)) {
//                if (inputStream == null) {
//                    throw new IOException("Unable to open input stream for URI");
//                }
//
//                String fileName = uri.getLastPathSegment(); // A simple way to guess file type
//                if (fileName == null) fileName = "";
//
//                if (fileName.toLowerCase().endsWith(".pdf")) {
//                    importedProducts = parsePdf(inputStream);
//                } else if (fileName.toLowerCase().endsWith(".xlsx") || fileName.toLowerCase().endsWith(".xls")) {
//                    importedProducts = parseExcel(inputStream);
//                } else if (fileName.toLowerCase().endsWith(".csv")) {
//                    importedProducts = parseCsv(inputStream);
//                } else {
//                    errorMsg = "Unsupported file type. Please select a PDF, Excel, or CSV file.";
//                }
//
//            } catch (Exception e) {
//                Log.e(TAG, "Import failed", e);
//                errorMsg = "Import failed: " + e.getMessage();
//            }
//
//            // Post result back to the main thread
//            final String finalErrorMsg = errorMsg;
//            final List<Product> finalImportedProducts = importedProducts;
//            handler.post(() -> {
//                if (finalErrorMsg != null) {
//                    showToast(finalErrorMsg);
//                } else if (finalImportedProducts.isEmpty()) {
//                    showToast("No products found in the file or file is not formatted correctly.");
//                } else {
//                    uploadProductsToFirebase(finalImportedProducts);
//                }
//            });
//        });
//    }
//
//    // --- PARSING LOGIC ---
//
//    private List<Product> parsePdf(InputStream inputStream) throws IOException {
//        List<Product> list = new ArrayList<>();
//        try (PdfReader reader = new PdfReader(inputStream); PdfDocument pdfDoc = new PdfDocument(reader)) {
//            for (int i = 1; i <= pdfDoc.getNumberOfPages(); i++) {
//                String text = PdfTextExtractor.getTextFromPage(pdfDoc.getPage(i), new LocationTextExtractionStrategy());
//                String[] lines = text.split("\n");
//                boolean headerPassed = false; // To skip header row
//
//                for (String line : lines) {
//                    if (line.trim().isEmpty()) continue;
//
//                    // Simple header detection logic
//                    if (!headerPassed) {
//                        String lowerLine = line.toLowerCase();
//                        if (lowerLine.contains("name") && lowerLine.contains("hsn")) {
//                            headerPassed = true;
//                        }
//                        continue; // Skip the header line itself
//                    }
//
//                    // Split by 2 or more spaces, a common format for text tables
//                    String[] parts = line.split("\\s{2,}");
//                    if (parts.length < 4) continue; // Ensure there's enough data
//
//                    Product p = new Product();
//                    p.setProductId(UUID.randomUUID().toString());
//                    p.setName(parts[0].trim());
//                    p.setHsnCode(parts[1].trim());
//                    p.setPrice(parseDouble(parts[2].trim()));
//                    p.setGstRate(parseDouble(parts[3].trim()));
//                    p.setStockQuantity(parts.length > 4 ? parseInt(parts[4].trim()) : 0);
//                    list.add(p);
//                }
//            }
//        }
//        return list;
//    }
//
//    private List<Product> parseExcel(InputStream inputStream) throws IOException {
//        List<Product> list = new ArrayList<>();
//        try (Workbook workbook = WorkbookFactory.create(inputStream)) {
//            Sheet sheet = workbook.getSheetAt(0);
//            for (Row row : sheet) {
//                if (row.getRowNum() == 0) continue; // Skip header row
//
//                // Simple validation to skip empty rows
//                if (getCellValueAsString(row.getCell(0)).isEmpty()) {
//                    continue;
//                }
//
//                Product p = new Product();
//                p.setProductId(UUID.randomUUID().toString());
//                p.setName(getCellValueAsString(row.getCell(0)));
//                p.setHsnCode(getCellValueAsString(row.getCell(1)));
//                p.setPrice(parseDouble(getCellValueAsString(row.getCell(2))));
//                p.setGstRate(parseDouble(getCellValueAsString(row.getCell(3))));
//                p.setStockQuantity(parseInt(getCellValueAsString(row.getCell(4))));
//                list.add(p);
//            }
//        }
//        return list;
//    }
//
//    private List<Product> parseCsv(InputStream inputStream) throws IOException, CsvValidationException {
//        List<Product> list = new ArrayList<>();
//        // Use try-with-resources to ensure the reader is closed
//        try (CSVReader csvReader = new CSVReader(new InputStreamReader(inputStream))) {
//            csvReader.readNext(); // Skip header line
//            String[] line;
//            while ((line = csvReader.readNext()) != null) {
//                if (line.length < 4) continue; // Ensure there's enough data
//
//                Product p = new Product();
//                p.setProductId(UUID.randomUUID().toString());
//                p.setName(line[0].trim());
//                p.setHsnCode(line[1].trim());
//                p.setPrice(parseDouble(line[2].trim()));
//                p.setGstRate(parseDouble(line[3].trim()));
//                p.setStockQuantity(line.length > 4 ? parseInt(line[4].trim()) : 0);
//                list.add(p);
//            }
//        }
//        return list;
//    }
//
//    // --- FIREBASE AND DATA HANDLING ---
//
//    private void uploadProductsToFirebase(List<Product> productsToUpload) {
//        if (productsToUpload.isEmpty()) return;
//
//        // Use a single batch update for efficiency
//        Map<String, Object> batchUpdate = new HashMap<>();
//        for (Product product : productsToUpload) {
//            batchUpdate.put(product.getProductId(), product);
//        }
//
//        productsRef.updateChildren(batchUpdate)
//                .addOnSuccessListener(aVoid ->
//                        showToast(productsToUpload.size() + " products imported successfully!"))
//                .addOnFailureListener(e -> {
//                    Log.e(TAG, "Firebase batch upload failed", e);
//                    showToast("Failed to upload products to the database.");
//                });
//    }
//
//    private void loadProducts() {
//        productsRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                productList.clear();
//                for (DataSnapshot ds : snapshot.getChildren()) {
//                    Product p = ds.getValue(Product.class);
//                    if (p != null) {
//                        productList.add(p);
//                    }
//                }
//                // Refresh the filtered list and update the adapter
//                filterProducts(searchView.getQuery().toString());
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Log.w(TAG, "Failed to read products.", error.toException());
//                showToast("Failed to load products: " + error.getMessage());
//            }
//        });
//    }
//
//    private void filterProducts(String query) {
//        filteredList.clear();
//        if (query == null || query.isEmpty()) {
//            filteredList.addAll(productList);
//        } else {
//            String lowerCaseQuery = query.toLowerCase();
//            for (Product product : productList) {
//                if (product.getName() != null && product.getName().toLowerCase().contains(lowerCaseQuery)) {
//                    filteredList.add(product);
//                }
//            }
//        }
//        adapter.notifyDataSetChanged();
//    }
//
//    // --- HELPER METHODS ---
//
//    private String getCellValueAsString(Cell cell) {
//        if (cell == null) return "";
//        switch (cell.getCellType()) {
//            case STRING:
//                return cell.getStringCellValue().trim();
//            case NUMERIC:
//                // Handle both integers and doubles gracefully
//                double numericValue = cell.getNumericCellValue();
//                if (numericValue == (long) numericValue) {
//                    return String.valueOf((long) numericValue);
//                } else {
//                    return String.valueOf(numericValue);
//                }
//            case BOOLEAN:
//                return String.valueOf(cell.getBooleanCellValue());
//            case FORMULA:
//                // Note: This returns the formula string, not the calculated value.
//                // For calculated value, you'd need a FormulaEvaluator.
//                return cell.getCellFormula();
//            default:
//                return "";
//        }
//    }
//
//    private double parseDouble(String s) {
//        if (s == null || s.isEmpty()) return 0.0;
//        try {
//            return Double.parseDouble(s);
//        } catch (NumberFormatException e) {
//            return 0.0;
//        }
//    }
//
//    private int parseInt(String s) {
//        if (s == null || s.isEmpty()) return 0;
//        try {
//            // Handle potential decimals from Excel files by parsing as double first
//            return (int) Double.parseDouble(s);
//        } catch (NumberFormatException e) {
//            return 0;
//        }
//    }
//
//    private void showToast(String message) {
//        if (getContext() != null) {
//            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
//        }
//    }
//
//    // --- ADAPTER ACTIONS (EDIT/DELETE) ---
//
//    @Override
//    public void onEditProduct(Product product, int position) {
//        // Your existing showEditDialog logic remains here
//        showEditDialog(product);
//    }
//
//    @Override
//    public void onDeleteProduct(Product product, int position) {
//        // Your existing delete logic remains here
//        new AlertDialog.Builder(requireContext())
//                .setTitle("Delete Product")
//                .setMessage("Are you sure you want to delete " + product.getName() + "?")
//                .setPositiveButton("Delete", (dialog, which) -> {
//                    productsRef.child(product.getProductId()).removeValue()
//                            .addOnSuccessListener(aVoid -> showToast("Product deleted."))
//                            .addOnFailureListener(e -> showToast("Failed to delete product."));
//                })
//                .setNegativeButton("Cancel", null)
//                .show();
//    }
//
//    // Copy your existing showEditDialog method here
//    private void showEditDialog(Product product) {
//        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_edit_product, null);
//        AlertDialog dialog = new AlertDialog.Builder(getContext())
//                .setView(dialogView)
//                .create();
//
//        EditText etName = dialogView.findViewById(R.id.etProductName);
//        EditText etHsn = dialogView.findViewById(R.id.etHsn);
//        EditText etPrice = dialogView.findViewById(R.id.etPrice);
//        EditText etGst = dialogView.findViewById(R.id.etGst);
//        EditText etQuantity = dialogView.findViewById(R.id.etQuantity);
//        Button btnUpdate = dialogView.findViewById(R.id.btnUpdate);
//
//        // Pre-fill data
//        etName.setText(product.getName());
//        etHsn.setText(product.getHsnCode());
//        etPrice.setText(String.valueOf(product.getPrice()));
//        etGst.setText(String.valueOf(product.getGstRate()));
//        etQuantity.setText(String.valueOf(product.getStockQuantity()));
//
//        btnUpdate.setOnClickListener(v -> {
//            String name = etName.getText().toString().trim();
//            String hsn = etHsn.getText().toString().trim();
//            double price = parseDouble(etPrice.getText().toString());
//            double gst = parseDouble(etGst.getText().toString());
//            int quantity = parseInt(etQuantity.getText().toString());
//
//            // Update product object
//            product.setName(name);
//            product.setHsnCode(hsn);
//            product.setPrice(price);
//            product.setGstRate(gst);
//            product.setStockQuantity(quantity);
//
//            // Update in Firebase
//            productsRef.child(product.getProductId()).setValue(product)
//                    .addOnSuccessListener(aVoid -> {
//                        showToast("Updated successfully");
//                        dialog.dismiss();
//                    })
//                    .addOnFailureListener(e ->
//                            showToast("Failed to update"));
//        });
//
//        dialog.show();
//    }
//}
//





package com.sandhyasofttech.gstbillingpro.Fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;
import com.itextpdf.kernel.pdf.canvas.parser.listener.LocationTextExtractionStrategy;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import com.sandhyasofttech.gstbillingpro.Activity.NewProductActivity;
import com.sandhyasofttech.gstbillingpro.Adapter.ProductsAdapter;
import com.sandhyasofttech.gstbillingpro.Model.Product;
import com.sandhyasofttech.gstbillingpro.R;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProductFragment extends Fragment implements ProductsAdapter.OnProductActionListener {

    private static final String TAG = "ProductFragment";

    private RecyclerView rvProducts;
    private FloatingActionButton fabAddProduct;
    private MaterialButton btnImportProducts;
    private SearchView searchView;
    private TextView tvTotalProducts; // <-- ADDED

    private ProductsAdapter adapter;
    private final List<Product> productList = new ArrayList<>();
    private final List<Product> filteredList = new ArrayList<>();

    private DatabaseReference productsRef;
    private String userMobile;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());

    private final ActivityResultLauncher<Intent> filePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        importDataFromFile(uri);
                    } else {
                        showToast("Failed to get file URI.");
                    }
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_product, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize UI components
        rvProducts = view.findViewById(R.id.rvProducts);
        fabAddProduct = view.findViewById(R.id.fabAddProduct);
        btnImportProducts = view.findViewById(R.id.btnImportProducts);
        searchView = view.findViewById(R.id.searchView);
        tvTotalProducts = view.findViewById(R.id.tvTotalProducts); // <-- ADDED

        SharedPreferences prefs = requireActivity().getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE);
        userMobile = prefs.getString("USER_MOBILE", null);

        if (userMobile == null || userMobile.isEmpty()) {
            showToast("User not logged in. Cannot load data.");
            return;
        }

        productsRef = FirebaseDatabase.getInstance().getReference("users").child(userMobile).child("products");
        setupRecyclerView();
        loadProducts();
        setupListeners();
    }

    private void setupRecyclerView() {
        // Assuming your adapter constructor accepts List<Product> now
        adapter = new ProductsAdapter(filteredList, this);
        rvProducts.setLayoutManager(new LinearLayoutManager(getContext()));
        rvProducts.setAdapter(adapter);
    }

    private void setupListeners() {
        fabAddProduct.setOnClickListener(v -> startActivity(new Intent(getContext(), NewProductActivity.class)));
        btnImportProducts.setOnClickListener(v -> openFilePicker());

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterProducts(newText);
                return true;
            }
        });
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{
                "application/pdf", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "application/vnd.ms-excel", "text/csv"
        });
        filePickerLauncher.launch(Intent.createChooser(intent, "Select a file to import"));
    }

    private void importDataFromFile(final Uri uri) {
        executor.execute(() -> {
            List<Product> importedProducts = new ArrayList<>();
            String errorMsg = null;

            try (InputStream inputStream = requireContext().getContentResolver().openInputStream(uri)) {
                if (inputStream == null) throw new IOException("Unable to open input stream for URI");

                String fileName = uri.getLastPathSegment() != null ? uri.getLastPathSegment() : "";
                String lowerCaseFileName = fileName.toLowerCase();

                if (lowerCaseFileName.endsWith(".pdf")) {
                    importedProducts = parsePdf(inputStream);
                } else if (lowerCaseFileName.endsWith(".xlsx") || lowerCaseFileName.endsWith(".xls")) {
                    importedProducts = parseExcel(inputStream);
                } else if (lowerCaseFileName.endsWith(".csv")) {
                    importedProducts = parseCsv(inputStream);
                } else {
                    errorMsg = "Unsupported file type. Please select a PDF, Excel, or CSV file.";
                }

            } catch (Exception e) {
                Log.e(TAG, "Import failed", e);
                errorMsg = "Import failed: " + e.getMessage();
            }

            final String finalErrorMsg = errorMsg;
            final List<Product> finalImportedProducts = importedProducts;
            handler.post(() -> {
                if (finalErrorMsg != null) {
                    showToast(finalErrorMsg);
                } else if (finalImportedProducts.isEmpty()) {
                    showToast("No valid products found in the file.");
                } else {
                    uploadProductsToFirebase(finalImportedProducts);
                }
            });
        });
    }

    private List<Product> parsePdf(InputStream inputStream) throws IOException {
        List<Product> list = new ArrayList<>();
        try (PdfReader reader = new PdfReader(inputStream); PdfDocument pdfDoc = new PdfDocument(reader)) {
            for (int i = 1; i <= pdfDoc.getNumberOfPages(); i++) {
                String text = PdfTextExtractor.getTextFromPage(pdfDoc.getPage(i), new LocationTextExtractionStrategy());
                String[] lines = text.split("\n");
                boolean headerPassed = false;

                for (String line : lines) {
                    if (line.trim().isEmpty()) continue;
                    if (!headerPassed) {
                        if (line.toLowerCase().contains("name") && line.toLowerCase().contains("hsn")) {
                            headerPassed = true;
                        }
                        continue;
                    }

                    String[] parts = line.split("\\s{2,}");
                    if (parts.length < 5) continue; // Name, HSN, Price, GST, Qty

                    Product p = new Product();
                    p.setProductId(UUID.randomUUID().toString());
                    p.setName(parts[0].trim());
                    p.setHsnCode(parts[1].trim());
                    p.setPrice(parseDouble(parts[2].trim()));
                    p.setGstRate(parseDouble(parts[3].trim()));
                    p.setStockQuantity(parseInt(parts[4].trim()));
                    p.setUnit(parts.length > 5 ? parts[5].trim() : "pcs"); // Add Unit
                    list.add(p);
                }
            }
        }
        return list;
    }

    private List<Product> parseExcel(InputStream inputStream) throws IOException {
        List<Product> list = new ArrayList<>();
        try (Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                if (row.getRowNum() == 0 || getCellValueAsString(row.getCell(0)).isEmpty()) continue;

                Product p = new Product();
                p.setProductId(UUID.randomUUID().toString());
                p.setName(getCellValueAsString(row.getCell(0)));
                p.setHsnCode(getCellValueAsString(row.getCell(1)));
                p.setPrice(parseDouble(getCellValueAsString(row.getCell(2))));
                p.setGstRate(parseDouble(getCellValueAsString(row.getCell(3))));
                p.setStockQuantity(parseInt(getCellValueAsString(row.getCell(4))));
                String unit = getCellValueAsString(row.getCell(5));
                p.setUnit(unit.isEmpty() ? "pcs" : unit); // Add Unit
                list.add(p);
            }
        }
        return list;
    }

    private List<Product> parseCsv(InputStream inputStream) throws IOException, CsvValidationException {
        List<Product> list = new ArrayList<>();
        try (CSVReader csvReader = new CSVReader(new InputStreamReader(inputStream))) {
            csvReader.readNext(); // Skip header
            String[] line;
            while ((line = csvReader.readNext()) != null) {
                if (line.length < 5) continue;

                Product p = new Product();
                p.setProductId(UUID.randomUUID().toString());
                p.setName(line[0].trim());
                p.setHsnCode(line[1].trim());
                p.setPrice(parseDouble(line[2].trim()));
                p.setGstRate(parseDouble(line[3].trim()));
                p.setStockQuantity(parseInt(line[4].trim()));
                p.setUnit(line.length > 5 && !line[5].trim().isEmpty() ? line[5].trim() : "pcs"); // Add Unit
                list.add(p);
            }
        }
        return list;
    }

    private void uploadProductsToFirebase(List<Product> productsToUpload) {
        if (productsToUpload.isEmpty()) return;

        // Create a set of existing product names for quick duplicate checking
        Set<String> existingProductNames = new HashSet<>();
        for (Product p : productList) {
            if (p.getName() != null) {
                existingProductNames.add(p.getName().toLowerCase());
            }
        }

        Map<String, Object> batchUpdate = new HashMap<>();
        int newProductsCount = 0;
        for (Product product : productsToUpload) {
            // Check for duplicates (case-insensitive)
            if (product.getName() != null && !existingProductNames.contains(product.getName().toLowerCase())) {
                batchUpdate.put(product.getProductId(), product);
                existingProductNames.add(product.getName().toLowerCase()); // Add to set to prevent duplicates within the same file
                newProductsCount++;
            }
        }

        int skippedCount = productsToUpload.size() - newProductsCount;

        if (batchUpdate.isEmpty()) {
            showToast("No new products to import. All " + skippedCount + " products already exist.");
            return;
        }

        int finalNewProductsCount = newProductsCount;
        productsRef.updateChildren(batchUpdate)
                .addOnSuccessListener(aVoid -> {
                    String message = finalNewProductsCount + " new products imported successfully.";
                    if (skippedCount > 0) {
                        message += "\n" + skippedCount + " products were skipped as duplicates.";
                    }
                    showToast(message);
                })
                .addOnFailureListener(e -> showToast("Database error: Failed to upload products."));
    }

    private void loadProducts() {
        productsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                productList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Product p = ds.getValue(Product.class);
                    if (p != null) {
                        productList.add(p);
                    }
                }
                filterProducts(searchView.getQuery().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w(TAG, "Failed to read products.", error.toException());
                showToast("Failed to load products: " + error.getMessage());
            }
        });
    }

    private void filterProducts(String query) {
        filteredList.clear();
        if (query == null || query.isEmpty()) {
            filteredList.addAll(productList);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (Product product : productList) {
                if (product.getName() != null && product.getName().toLowerCase().contains(lowerCaseQuery)) {
                    filteredList.add(product);
                }
            }
        }
        tvTotalProducts.setText("Total Products: " + filteredList.size()); // Update total count
        adapter.notifyDataSetChanged();
    }

    // --- HELPER METHODS ---
    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING: return cell.getStringCellValue().trim();
            case NUMERIC:
                double val = cell.getNumericCellValue();
                return val == (long) val ? String.valueOf((long) val) : String.valueOf(val);
            case BOOLEAN: return String.valueOf(cell.getBooleanCellValue());
            case FORMULA: return cell.getCellFormula();
            default: return "";
        }
    }
    private double parseDouble(String s) { if (s == null || s.isEmpty()) return 0.0; try { return Double.parseDouble(s); } catch (NumberFormatException e) { return 0.0; } }
    private int parseInt(String s) { if (s == null || s.isEmpty()) return 0; try { return (int) Double.parseDouble(s); } catch (NumberFormatException e) { return 0; } }
    private void showToast(String message) { if (getContext() != null) Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show(); }


    // --- ADAPTER ACTIONS ---
    @Override
    public void onEditProduct(Product product, int position) {
        showEditDialog(product);
    }

    @Override
    public void onDeleteProduct(Product product, int position) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Product")
                .setMessage("Are you sure you want to delete '" + product.getName() + "'?")
                .setPositiveButton("Delete", (d, w) -> productsRef.child(product.getProductId()).removeValue()
                        .addOnSuccessListener(a -> showToast("Product deleted."))
                        .addOnFailureListener(e -> showToast("Failed to delete product.")))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showEditDialog(Product product) {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_edit_product, null);
        AlertDialog dialog = new AlertDialog.Builder(getContext()).setView(dialogView).create();

        EditText etName = dialogView.findViewById(R.id.etProductName);
        EditText etHsn = dialogView.findViewById(R.id.etHsn);
        EditText etPrice = dialogView.findViewById(R.id.etPrice);
        EditText etGst = dialogView.findViewById(R.id.etGst);
        EditText etQuantity = dialogView.findViewById(R.id.etQuantity);
        EditText etUnit = dialogView.findViewById(R.id.etProductUnit); // <-- ADDED
        Button btnUpdate = dialogView.findViewById(R.id.btnUpdate);

        etName.setText(product.getName());
        etHsn.setText(product.getHsnCode());
        etPrice.setText(String.valueOf(product.getPrice()));
        etGst.setText(String.valueOf(product.getGstRate()));
        etQuantity.setText(String.valueOf(product.getStockQuantity()));
        etUnit.setText(product.getUnit()); // <-- ADDED

        btnUpdate.setOnClickListener(v -> {
            product.setName(etName.getText().toString().trim());
            product.setHsnCode(etHsn.getText().toString().trim());
            product.setPrice(parseDouble(etPrice.getText().toString()));
            product.setGstRate(parseDouble(etGst.getText().toString()));
            product.setStockQuantity(parseInt(etQuantity.getText().toString()));
            product.setUnit(etUnit.getText().toString().trim()); // <-- ADDED

            productsRef.child(product.getProductId()).setValue(product)
                    .addOnSuccessListener(aVoid -> {
                        showToast("Updated successfully");
                        dialog.dismiss();
                    })
                    .addOnFailureListener(e -> showToast("Failed to update"));
        });

        dialog.show();
    }
}
