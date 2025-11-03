package com.sandhyasofttech.gstbillingpro.Fragment;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.*;
import com.sandhyasofttech.gstbillingpro.R;
import com.sandhyasofttech.gstbillingpro.custmore.Customer;
import com.sandhyasofttech.gstbillingpro.Model.Product;
import com.sandhyasofttech.gstbillingpro.invoice.*;

import java.text.SimpleDateFormat;
import java.util.*;

public class InvoiceBillingFragment extends Fragment {

    private Spinner spCustomer;
    private RecyclerView rvInvoiceItems;
    private Button btnAddProduct, btnSaveInvoice;
    private TextView tvTaxableTotal, tvTaxTotal, tvGrandTotal;

    private List<Customer> customers = new ArrayList<>(); // Customer model you already have
    private List<Product> products = new ArrayList<>();  // Your Product model
    private ArrayList<InvoiceItem> invoiceItems = new ArrayList<>();
    private InvoiceItemAdapter itemAdapter;

    private DatabaseReference usersRef, invoicesRef;
    private String userMobile;

    private double totalTaxable = 0, totalCGST = 0, totalSGST = 0, totalIGST = 0, grandTotal = 0;
    private boolean isIntraState = true; // Load or select based on user/company state & customer state

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_invoice_billing, container, false);

        spCustomer = view.findViewById(R.id.spCustomer);
        rvInvoiceItems = view.findViewById(R.id.rvInvoiceItems);
        btnAddProduct = view.findViewById(R.id.btnAddProduct);
        btnSaveInvoice = view.findViewById(R.id.btnSaveInvoice);

        tvTaxableTotal = view.findViewById(R.id.tvTaxableTotal);
        tvTaxTotal = view.findViewById(R.id.tvTaxTotal);
        tvGrandTotal = view.findViewById(R.id.tvGrandTotal);

        SharedPreferences prefs = requireContext().getSharedPreferences("APP_PREFS", 0);
        userMobile = prefs.getString("USER_MOBILE", null);
        if(userMobile == null) {
            Toast.makeText(getContext(), "Please login", Toast.LENGTH_SHORT).show();
            // redirect to login or handle
            return view;
        }

        usersRef = FirebaseDatabase.getInstance().getReference("users").child(userMobile);
        invoicesRef = usersRef.child("invoices");

        setupCustomerSpinner();
        setupInvoiceRecyclerView();

        btnAddProduct.setOnClickListener(v -> showAddProductDialog());
        btnSaveInvoice.setOnClickListener(v -> saveInvoice());

        return view;
    }

    private void setupCustomerSpinner() {
        DatabaseReference customersRef = usersRef.child("customers");
        customersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                customers.clear();
                for(DataSnapshot ds : snapshot.getChildren()) {
                    Customer c = ds.getValue(Customer.class);
                    if (c != null) customers.add(c);
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                        android.R.layout.simple_spinner_dropdown_item,
                        getCustomerNames(customers));
                spCustomer.setAdapter(adapter);
                spCustomer.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        // You can determine intra/inter state here based on customer state and company state
                        // For demo, fixed as intra-state
                        isIntraState = true;
                        recalculateTotals();
                    }
                    @Override public void onNothingSelected(AdapterView<?> parent) {}
                });
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private List<String> getCustomerNames(List<Customer> customers) {
        List<String> names = new ArrayList<>();
        for(Customer c : customers) {
            names.add(c.name);
        }
        return names;
    }

    private void setupInvoiceRecyclerView() {
        itemAdapter = new InvoiceItemAdapter(invoiceItems, this::onInvoiceItemChanged);
        rvInvoiceItems.setAdapter(itemAdapter);
        rvInvoiceItems.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private void onInvoiceItemChanged() {
        recalculateTotals();
    }

    private void recalculateTotals() {
        totalTaxable = 0;
        totalCGST = 0;
        totalSGST = 0;
        totalIGST = 0;

        for (InvoiceItem item : invoiceItems) {
            double taxableValue = item.getTaxableValue();
            totalTaxable += taxableValue;
            GstCalculationUtil.GstDetails gstDetails = GstCalculationUtil.calculateGst(taxableValue, item.taxPercent, isIntraState);
            totalCGST += gstDetails.cgst;
            totalSGST += gstDetails.sgst;
            totalIGST += gstDetails.igst;
        }
        grandTotal = totalTaxable + totalCGST + totalSGST + totalIGST;

        tvTaxableTotal.setText(String.format("Taxable Value: ₹ %.2f", totalTaxable));
        tvTaxTotal.setText(String.format("CGST: ₹ %.2f, SGST: ₹ %.2f, IGST: ₹ %.2f", totalCGST, totalSGST, totalIGST));
        tvGrandTotal.setText(String.format("Grand Total: ₹ %.2f", grandTotal));
    }

    private void showAddProductDialog() {
        if (products.isEmpty()) {
            // Load products first or show message
            Toast.makeText(getContext(), "No products available. Please add products first.", Toast.LENGTH_SHORT).show();
            loadProductsFromFirebase();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Add Product");

        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_product, null);

        Spinner spProducts = dialogView.findViewById(R.id.spProducts);
        EditText etQuantity = dialogView.findViewById(R.id.etQuantity);

        // Set up Spinner adapter
        ArrayAdapter<String> productAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                getProductNames(products));
        spProducts.setAdapter(productAdapter);

        builder.setView(dialogView);

        builder.setPositiveButton("Add", (dialog, which) -> {
            int selectedPos = spProducts.getSelectedItemPosition();
            if (selectedPos < 0 || selectedPos >= products.size()) {
                Toast.makeText(getContext(), "Please select a product", Toast.LENGTH_SHORT).show();
                return;
            }
            String qtyStr = etQuantity.getText().toString().trim();
            if (qtyStr.isEmpty()) {
                Toast.makeText(getContext(), "Enter quantity", Toast.LENGTH_SHORT).show();
                return;
            }
            double qty = 0;
            try {
                qty = Double.parseDouble(qtyStr);
            } catch (Exception e) {
                Toast.makeText(getContext(), "Invalid quantity", Toast.LENGTH_SHORT).show();
                return;
            }
            Product selectedProduct = products.get(selectedPos);
            InvoiceItem item = new InvoiceItem(
                    selectedProduct.getProductId(),
                    selectedProduct.getName(),
                    qty,
                    selectedProduct.getPrice(),
                    selectedProduct.getGstRate()
            );
            invoiceItems.add(item);
            itemAdapter.notifyDataSetChanged();
            recalculateTotals();
        });

        builder.setNegativeButton("Cancel", null);

        builder.show();
    }

    private List<String> getProductNames(List<Product> products) {
        List<String> names = new ArrayList<>();
        for (Product p : products) {
            names.add(p.getName());
        }
        return names;
    }

    // Load products from Firebase (call this at appropriate place)
    private void loadProductsFromFirebase() {
        usersRef.child("products").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                products.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Product p = ds.getValue(Product.class);
                    if (p != null) products.add(p);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load products", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void saveInvoice() {
        // Generate invoice number (e.g. INV-YYYYMMDD-XXX)
        String invoiceNumber = generateInvoiceNumber();

        // Get selected customer details, date
        int pos = spCustomer.getSelectedItemPosition();
        if (pos < 0 || pos >= customers.size()) {
            Toast.makeText(getContext(), "Please select a customer", Toast.LENGTH_SHORT).show();
            return;
        }
        Customer customer = customers.get(pos);
        String invoiceDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        Invoice invoice = new Invoice(invoiceNumber, customer.phone, customer.name, invoiceDate,
                invoiceItems, totalTaxable, totalCGST, totalSGST, totalIGST, grandTotal, null, null);

        invoicesRef.child(invoiceNumber).setValue(invoice).addOnSuccessListener(aVoid -> {
            Toast.makeText(getContext(), "Invoice saved", Toast.LENGTH_SHORT).show();
            // Optionally clear form for new invoice
        }).addOnFailureListener(e -> Toast.makeText(getContext(), "Error saving invoice: "+ e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private String generateInvoiceNumber() {
        String datePart = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());
        return "INV-" + datePart + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

}
