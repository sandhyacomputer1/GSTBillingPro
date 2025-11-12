package com.sandhyasofttech.gstbillingpro.Fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.*;
import com.sandhyasofttech.gstbillingpro.R;
import com.sandhyasofttech.gstbillingpro.Adapter.SearchableAdapter;
import com.sandhyasofttech.gstbillingpro.custmore.Customer;
import com.sandhyasofttech.gstbillingpro.Model.Product;
import com.sandhyasofttech.gstbillingpro.invoice.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class InvoiceBillingFragment extends Fragment {

    private static final String TAG = "InvoiceBillingFragment";
    private TextView tvSelectedCustomer, tvTaxableTotal, tvTaxTotal, tvGrandTotal;
    private RecyclerView rvInvoiceItems;
    private FloatingActionButton btnAddProduct;
    private Button btnSaveInvoice;
    private LinearLayout llCustomerSelect;

    private List<Customer> customers = new ArrayList<>();
    private List<Product> products = new ArrayList<>();
    private ArrayList<InvoiceItem> invoiceItems = new ArrayList<>();
    private InvoiceItemAdapter itemAdapter;
    private Customer selectedCustomer;

    private DatabaseReference usersRef, invoicesRef, infoRef;
    private String userMobile;

    private String businessName = "Your Business Name";
    private String businessGstin = "";
    private String businessAddress = "";

    private double totalTaxable = 0, totalCGST = 0, totalSGST = 0, totalIGST = 0, grandTotal = 0;
    private boolean isIntraState = true;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_invoice_billing, container, false);

        llCustomerSelect = view.findViewById(R.id.llCustomerSelect);
        tvSelectedCustomer = view.findViewById(R.id.tvSelectedCustomer);
        rvInvoiceItems = view.findViewById(R.id.rvInvoiceItems);
        btnAddProduct = view.findViewById(R.id.btnAddProduct);
        btnSaveInvoice = view.findViewById(R.id.btnSaveInvoice);
        tvTaxableTotal = view.findViewById(R.id.tvTaxableTotal);
        tvTaxTotal = view.findViewById(R.id.tvTaxTotal);
        tvGrandTotal = view.findViewById(R.id.tvGrandTotal);

        SharedPreferences prefs = requireContext().getSharedPreferences("APP_PREFS", Activity.MODE_PRIVATE);
        userMobile = prefs.getString("USER_MOBILE", null);
        if (userMobile == null) {
            Toast.makeText(getContext(), "Please login", Toast.LENGTH_SHORT).show();
            return view;
        }

        usersRef = FirebaseDatabase.getInstance().getReference("users").child(userMobile);
        invoicesRef = usersRef.child("invoices");
        infoRef = usersRef.child("info");

        fetchBusinessInfo();
        loadCustomersFromFirebase();
        setupInvoiceRecyclerView();
        loadProductsFromFirebase();

        llCustomerSelect.setOnClickListener(v -> showCustomerSearchDialog());
        btnAddProduct.setOnClickListener(v -> showProductSearchDialog());
        btnSaveInvoice.setOnClickListener(v -> onSaveInvoice());

        return view;
    }

    private void fetchBusinessInfo() {
        infoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    businessName = getStringValue(snapshot, "businessName", businessName);
                    businessGstin = getStringValue(snapshot, "gstin", businessGstin);
                    businessAddress = getStringValue(snapshot, "address", businessAddress);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(requireContext(), "Failed to load business info.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getStringValue(DataSnapshot snapshot, String key, String fallback) {
        return snapshot.child(key).getValue(String.class) != null ? snapshot.child(key).getValue(String.class) : fallback;
    }

    private void loadCustomersFromFirebase() {
        usersRef.child("customers").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                customers.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Customer c = ds.getValue(Customer.class);
                    if (c != null) customers.add(c);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(requireContext(), "Failed to load customers.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupInvoiceRecyclerView() {
        itemAdapter = new InvoiceItemAdapter(invoiceItems, this::recalculateTotals);
        rvInvoiceItems.setLayoutManager(new LinearLayoutManager(getContext()));
        rvInvoiceItems.setAdapter(itemAdapter);
    }

    private void recalculateTotals() {
        totalTaxable = totalCGST = totalSGST = totalIGST = 0;
        for (InvoiceItem item : invoiceItems) {
            double val = item.getTaxableValue();
            totalTaxable += val;
            GstCalculationUtil.GstDetails gst = GstCalculationUtil.calculateGst(val, item.taxPercent, isIntraState);
            totalCGST += gst.cgst;
            totalSGST += gst.sgst;
            totalIGST += gst.igst;
        }
        grandTotal = totalTaxable + totalCGST + totalSGST + totalIGST;

        tvTaxableTotal.setText(String.format(Locale.getDefault(), "Taxable: ₹%.2f", totalTaxable));
        tvTaxTotal.setText(String.format(Locale.getDefault(), "CGST: ₹%.2f | SGST: ₹%.2f | IGST: ₹%.2f", totalCGST, totalSGST, totalIGST));
        tvGrandTotal.setText(String.format(Locale.getDefault(), "Grand Total: ₹%.2f", grandTotal));
    }

    private void showCustomerSearchDialog() {
        if (customers.isEmpty()) {
            Toast.makeText(getContext(), "No customers. Loading...", Toast.LENGTH_SHORT).show();
            loadCustomersFromFirebase();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_searchable_list, null);
        builder.setView(dialogView);

        TextView tvDialogTitle = dialogView.findViewById(R.id.tvDialogTitle);
        tvDialogTitle.setText("Select Customer");

        SearchView searchView = dialogView.findViewById(R.id.searchView);
        RecyclerView rvItems = dialogView.findViewById(R.id.rvItems);

        rvItems.setLayoutManager(new LinearLayoutManager(getContext()));
        List<String> customerNames = new ArrayList<>();
        for(Customer c : customers) customerNames.add(c.name);

        AlertDialog dialog = builder.create();

        SearchableAdapter searchableAdapter = new SearchableAdapter(customerNames, item -> {
            for (Customer c : customers) {
                if (c.name.equals(item)) {
                    selectedCustomer = c;
                    tvSelectedCustomer.setText(selectedCustomer.name);
                    isIntraState = true; // Logic to determine state can be added here
                    recalculateTotals();
                    break;
                }
            }
            dialog.dismiss();
        });

        rvItems.setAdapter(searchableAdapter);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String query) { return false; }
            @Override public boolean onQueryTextChange(String newText) {
                searchableAdapter.getFilter().filter(newText);
                return true;
            }
        });

        dialog.show();
    }

    private void showProductSearchDialog() {
        if (products.isEmpty()) {
            Toast.makeText(getContext(), "No products. Loading...", Toast.LENGTH_SHORT).show();
            loadProductsFromFirebase();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_searchable_list, null);
        builder.setView(dialogView);

        TextView tvDialogTitle = dialogView.findViewById(R.id.tvDialogTitle);
        tvDialogTitle.setText("Select Product");

        SearchView searchView = dialogView.findViewById(R.id.searchView);
        RecyclerView rvItems = dialogView.findViewById(R.id.rvItems);

        rvItems.setLayoutManager(new LinearLayoutManager(getContext()));
        List<String> productNames = new ArrayList<>();
        for(Product p : products) productNames.add(p.getName());

        AlertDialog dialog = builder.create();

        SearchableAdapter searchableAdapter = new SearchableAdapter(productNames, item -> {
            for (Product p : products) {
                if (p.getName().equals(item)) {
                    showQuantityDialog(p);
                    break;
                }
            }
            dialog.dismiss();
        });

        rvItems.setAdapter(searchableAdapter);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String query) { return false; }
            @Override public boolean onQueryTextChange(String newText) {
                searchableAdapter.getFilter().filter(newText);
                return true;
            }
        });

        dialog.show();
    }

    private void showQuantityDialog(Product p) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Enter Quantity");

        final EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        builder.setView(input);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String qtyStr = input.getText().toString();
            if (TextUtils.isEmpty(qtyStr)) { return; }
            double qty = Double.parseDouble(qtyStr);
            if (qty > p.getEffectiveQuantity()) {
                Toast.makeText(getContext(), "Only " + p.getEffectiveQuantity() + " in stock.", Toast.LENGTH_SHORT).show();
                return;
            }

            InvoiceItem item = new InvoiceItem(p.getProductId(), p.getName(), qty, p.getPrice(), p.getGstRate());
            invoiceItems.add(item);
            itemAdapter.notifyItemInserted(invoiceItems.size() - 1);
            recalculateTotals();
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

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

    private void onSaveInvoice() {
        if (invoiceItems.isEmpty()) {
            showSafeMessage("Add at least one product.");
            return;
        }
        if (selectedCustomer == null) {
            showSafeMessage("Select a customer.");
            return;
        }

        String invoiceNumber = generateInvoiceNumber();
        String invoiceDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        Invoice invoice = new Invoice(
                invoiceNumber, selectedCustomer.phone, selectedCustomer.name, invoiceDate,
                new ArrayList<>(invoiceItems), totalTaxable, totalCGST, totalSGST, totalIGST, grandTotal,
                businessName, businessAddress
        );

        invoicesRef.child(invoiceNumber).setValue(invoice).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                showSafeMessage("Invoice saved!");
                updateProductStocksAfterSale(invoice.items);

                File pdfFile = generatePdf(invoice);
                if (pdfFile != null) {
                    showPostSaveOptionsDialog(pdfFile, selectedCustomer.phone);
                }

                invoiceItems.clear();
                itemAdapter.notifyDataSetChanged();
                recalculateTotals();
            } else {
                showSafeMessage("Save failed: " + task.getException().getMessage());
            }
        });
    }

    private void showPostSaveOptionsDialog(File pdfFile, String customerPhoneNumber) {
        final CharSequence[] options = {"View PDF", "Share on WhatsApp", "Dismiss"};

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Invoice Saved");
        builder.setItems(options, (dialog, item) -> {
            if (options[item].equals("View PDF")) {
                openPdf(pdfFile);
            } else if (options[item].equals("Share on WhatsApp")) {
                sharePdfToWhatsapp(pdfFile, customerPhoneNumber);
            } else if (options[item].equals("Dismiss")) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private void showSafeMessage(String msg) {
        Log.d(TAG, msg);
        if (getView() != null) {
            Snackbar.make(getView(), msg, Snackbar.LENGTH_SHORT).show();
        }
    }

    private void updateProductStocksAfterSale(List<InvoiceItem> soldItems) {
        for (InvoiceItem item : soldItems) {
            DatabaseReference productRef = usersRef.child("products").child(item.productId);
            productRef.runTransaction(new Transaction.Handler() {
                @NonNull
                @Override
                public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                    Product p = mutableData.getValue(Product.class);
                    if (p == null) return Transaction.success(mutableData);
                    int newStock = p.getEffectiveQuantity() - (int) item.quantity;
                    mutableData.child("stockQuantity").setValue(Math.max(0, newStock));
                    return Transaction.success(mutableData);
                }

                @Override
                public void onComplete(DatabaseError error, boolean committed, DataSnapshot snapshot) {
                    if (error != null) Log.e(TAG, "Stock update failed: " + error.getMessage());
                    else loadProductsFromFirebase();
                }
            });
        }
    }

    private String generateInvoiceNumber() {
        String datePart = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());
        return "INV-" + datePart + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    private File generatePdf(Invoice invoice) {
        try {
            File invoiceDir = new File(requireContext().getFilesDir(), "Invoices");
            if (!invoiceDir.exists()) invoiceDir.mkdirs();
            File file = new File(invoiceDir, invoice.invoiceNumber + ".pdf");

            PdfDocument pdf = new PdfDocument();
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
            PdfDocument.Page page = pdf.startPage(pageInfo);
            Canvas canvas = page.getCanvas();

            Paint titlePaint = new Paint();
            titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            titlePaint.setTextSize(16);

            Paint boldPaint = new Paint();
            boldPaint.setTypeface(Typeface.DEFAULT_BOLD);

            Paint regularPaint = new Paint();

            int yPos = 40;
            int margin = 40;

            // Header
            canvas.drawText(businessName, margin, yPos, titlePaint);
            canvas.drawText("TAX INVOICE", page.getInfo().getPageWidth() - margin - 100, yPos, titlePaint);
            yPos += 20;
            if (!TextUtils.isEmpty(businessAddress)) canvas.drawText(businessAddress, margin, yPos, regularPaint);
            yPos += 15;
            if (!TextUtils.isEmpty(businessGstin)) canvas.drawText("GSTIN: " + businessGstin, margin, yPos, regularPaint);
            yPos += 30;

            // Line
            canvas.drawLine(margin, yPos, page.getInfo().getPageWidth() - margin, yPos, regularPaint);
            yPos += 20;

            // Invoice Details
            canvas.drawText("Invoice No: " + invoice.invoiceNumber, margin, yPos, boldPaint);
            canvas.drawText("Date: " + invoice.invoiceDate, page.getInfo().getPageWidth() - margin - 150, yPos, boldPaint);
            yPos += 20;

            // Customer Details
            canvas.drawText("Bill To:", margin, yPos, boldPaint);
            yPos += 15;
            canvas.drawText(invoice.customerName, margin, yPos, regularPaint);
            yPos += 30;

            // Table Header
            canvas.drawText("Item", margin, yPos, boldPaint);
            canvas.drawText("Qty", margin + 250, yPos, boldPaint);
            canvas.drawText("Rate", margin + 300, yPos, boldPaint);
            canvas.drawText("Amount", page.getInfo().getPageWidth() - margin - 50, yPos, boldPaint);
            yPos += 20;
            canvas.drawLine(margin, yPos, page.getInfo().getPageWidth() - margin, yPos, regularPaint);
            yPos += 20;

            // Table Items
            for (InvoiceItem item : invoice.items) {
                canvas.drawText(trimText(item.productName, 35), margin, yPos, regularPaint);
                canvas.drawText(String.format("%.2f", item.quantity), margin + 250, yPos, regularPaint);
                canvas.drawText(String.format("%.2f", item.rate), margin + 300, yPos, regularPaint);
                canvas.drawText(String.format("%.2f", item.getTaxableValue()), page.getInfo().getPageWidth() - margin - 50, yPos, regularPaint);
                yPos += 20;
            }

            // Line
            canvas.drawLine(margin, yPos, page.getInfo().getPageWidth() - margin, yPos, regularPaint);
            yPos += 20;

            // Totals Section
            int totalsX = page.getInfo().getPageWidth() - margin - 200;
            canvas.drawText("Taxable Amount:", totalsX, yPos, regularPaint);
            canvas.drawText(String.format("₹%.2f", invoice.totalTaxableValue), page.getInfo().getPageWidth() - margin - 50, yPos, regularPaint);
            yPos += 20;

            canvas.drawText("CGST:", totalsX, yPos, regularPaint);
            canvas.drawText(String.format("₹%.2f", invoice.totalCGST), page.getInfo().getPageWidth() - margin - 50, yPos, regularPaint);
            yPos += 20;

            canvas.drawText("SGST:", totalsX, yPos, regularPaint);
            canvas.drawText(String.format("₹%.2f", invoice.totalSGST), page.getInfo().getPageWidth() - margin - 50, yPos, regularPaint);
            yPos += 20;

            if (invoice.totalIGST > 0) {
                canvas.drawText("IGST:", totalsX, yPos, regularPaint);
                canvas.drawText(String.format("₹%.2f", invoice.totalIGST), page.getInfo().getPageWidth() - margin - 50, yPos, regularPaint);
                yPos += 20;
            }

            canvas.drawLine(totalsX - 20, yPos, page.getInfo().getPageWidth() - margin, yPos, regularPaint);
            yPos += 20;

            boldPaint.setTextAlign(Paint.Align.RIGHT);
            canvas.drawText("Grand Total:", page.getInfo().getPageWidth() - margin - 70, yPos, boldPaint);
            canvas.drawText(String.format("₹%.2f", invoice.grandTotal), page.getInfo().getPageWidth() - margin, yPos, boldPaint);
            yPos += 50;

            // Footer
            regularPaint.setTextSize(10);
            regularPaint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("Thank you for your business!", page.getInfo().getPageWidth() / 2, yPos, regularPaint);

            pdf.finishPage(page);
            pdf.writeTo(new FileOutputStream(file));
            pdf.close();

            return file;
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "PDF Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            return null;
        }
    }

    private String trimText(String text, int maxLen) {
        return text.length() > maxLen ? text.substring(0, maxLen - 3) + "..." : text;
    }

    private void openPdf(File file) {
        if (!file.exists()) { Toast.makeText(requireContext(), "PDF not found!", Toast.LENGTH_SHORT).show(); return; }
        Uri uri = FileProvider.getUriForFile(requireContext(), requireContext().getPackageName() + ".fileprovider", file);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "application/pdf");
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NO_HISTORY);
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(requireContext(), "Install a PDF viewer (e.g., Google Drive).", Toast.LENGTH_LONG).show();
        }
    }

    private void sharePdfToWhatsapp(File pdfFile, String customerPhoneNumber) {
        if (!pdfFile.exists()) { Toast.makeText(requireContext(), "PDF not ready!", Toast.LENGTH_SHORT).show(); return; }
        String phone = customerPhoneNumber.replaceAll("[^0-9]", "");
        if (!phone.startsWith("91")) phone = "91" + phone;
        Uri uri = FileProvider.getUriForFile(requireContext(), requireContext().getPackageName() + ".fileprovider", pdfFile);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("application/pdf");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setPackage("com.whatsapp");
        intent.putExtra("jid", phone + "@s.whatsapp.net");
        try {
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(requireContext(), "WhatsApp not installed!", Toast.LENGTH_SHORT).show();
        }
    }
}
