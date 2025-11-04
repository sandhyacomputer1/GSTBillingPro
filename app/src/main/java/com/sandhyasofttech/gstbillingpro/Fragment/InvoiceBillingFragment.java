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
import android.os.Environment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.*;
import com.sandhyasofttech.gstbillingpro.R;
import com.sandhyasofttech.gstbillingpro.custmore.Customer;
import com.sandhyasofttech.gstbillingpro.Model.Product;
import com.sandhyasofttech.gstbillingpro.invoice.*;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.*;

public class InvoiceBillingFragment extends Fragment {

    private Spinner spCustomer;
    private RecyclerView rvInvoiceItems;
    private Button btnAddProduct, btnSaveInvoice;
    private TextView tvTaxableTotal, tvTaxTotal, tvGrandTotal;

    private List<Customer> customers = new ArrayList<>();
    private List<Product> products = new ArrayList<>();
    private ArrayList<InvoiceItem> invoiceItems = new ArrayList<>();
    private InvoiceItemAdapter itemAdapter;

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

        spCustomer = view.findViewById(R.id.spCustomer);
        rvInvoiceItems = view.findViewById(R.id.rvInvoiceItems);
        btnAddProduct = view.findViewById(R.id.btnAddProduct);
        btnSaveInvoice = view.findViewById(R.id.btnSaveInvoice);

        tvTaxableTotal = view.findViewById(R.id.tvTaxableTotal);
        tvTaxTotal = view.findViewById(R.id.tvTaxTotal);
        tvGrandTotal = view.findViewById(R.id.tvGrandTotal);

        SharedPreferences prefs = requireContext().getSharedPreferences("APP_PREFS", Activity.MODE_PRIVATE);
        userMobile = prefs.getString("USER_MOBILE", null);
        if(userMobile == null) {
            Toast.makeText(getContext(), "Please login", Toast.LENGTH_SHORT).show();
            return view;
        }

        usersRef = FirebaseDatabase.getInstance().getReference("users").child(userMobile);
        invoicesRef = usersRef.child("invoices");
        infoRef = usersRef.child("info");

        fetchBusinessInfo();
        setupCustomerSpinner();
        setupInvoiceRecyclerView();
        loadProductsFromFirebase();

        btnAddProduct.setOnClickListener(v -> showAddProductDialog());
        btnSaveInvoice.setOnClickListener(v -> onSaveInvoice());

        return view;
    }

    private void fetchBusinessInfo() {
        infoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    businessName = snapshot.child("businessName").getValue(String.class);
                    businessGstin = snapshot.child("gstin").getValue(String.class);
                    businessAddress = snapshot.child("address").getValue(String.class);
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void setupCustomerSpinner() {
        usersRef.child("customers").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                customers.clear();
                for(DataSnapshot ds : snapshot.getChildren()) {
                    Customer c = ds.getValue(Customer.class);
                    if (c != null) customers.add(c);
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                        android.R.layout.simple_spinner_dropdown_item,
                        getCustomerNames(customers));
                spCustomer.setAdapter(adapter);
                spCustomer.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        isIntraState = true; // Customize if needed
                        recalculateTotals();
                    }
                    @Override public void onNothingSelected(AdapterView<?> parent) {}
                });
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(requireContext(), "Failed to load customers.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private List<String> getCustomerNames(List<Customer> customers) {
        List<String> names = new ArrayList<>();
        for(Customer c : customers) names.add(c.name);
        return names;
    }

    private void setupInvoiceRecyclerView() {
        itemAdapter = new InvoiceItemAdapter(invoiceItems, this::onInvoiceItemChanged);
        rvInvoiceItems.setLayoutManager(new LinearLayoutManager(getContext()));
        rvInvoiceItems.setAdapter(itemAdapter);
    }

    private void onInvoiceItemChanged() {
        recalculateTotals();
    }

    private void recalculateTotals() {
        totalTaxable = 0; totalCGST = 0; totalSGST = 0; totalIGST = 0;
        for(InvoiceItem item : invoiceItems) {
            double val = item.getTaxableValue();
            totalTaxable += val;
            GstCalculationUtil.GstDetails gst = GstCalculationUtil.calculateGst(val, item.taxPercent, isIntraState);
            totalCGST += gst.cgst;
            totalSGST += gst.sgst;
            totalIGST += gst.igst;
        }
        grandTotal = totalTaxable + totalCGST + totalSGST + totalIGST;

        tvTaxableTotal.setText(String.format(Locale.getDefault(), "Taxable Value: ₹ %.2f", totalTaxable));
        tvTaxTotal.setText(String.format(Locale.getDefault(), "CGST: ₹ %.2f, SGST: ₹ %.2f, IGST: ₹ %.2f", totalCGST, totalSGST, totalIGST));
        tvGrandTotal.setText(String.format(Locale.getDefault(), "Grand Total: ₹ %.2f", grandTotal));
    }

    private void showAddProductDialog() {
        if(products.isEmpty()) {
            Toast.makeText(getContext(), "No products available.", Toast.LENGTH_SHORT).show();
            loadProductsFromFirebase();
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Add Product");
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_product, null);

        Spinner spProducts = dialogView.findViewById(R.id.spProducts);
        EditText etQuantity = dialogView.findViewById(R.id.etQuantity);

        ArrayAdapter<String> productAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                getProductNames(products));
        spProducts.setAdapter(productAdapter);

        builder.setView(dialogView);

        builder.setPositiveButton("Add", (dialog, which) -> {
            int pos = spProducts.getSelectedItemPosition();
            if(pos < 0 || pos >= products.size()) {
                Toast.makeText(getContext(), "Select a product", Toast.LENGTH_SHORT).show();
                return;
            }
            String qtyStr = etQuantity.getText().toString().trim();
            if(TextUtils.isEmpty(qtyStr)) {
                Toast.makeText(getContext(), "Enter quantity", Toast.LENGTH_SHORT).show();
                return;
            }
            double qty;
            try {
                qty = Double.parseDouble(qtyStr);
            } catch(Exception e) {
                Toast.makeText(getContext(), "Invalid quantity", Toast.LENGTH_SHORT).show();
                return;
            }
            Product p = products.get(pos);
            InvoiceItem item = new InvoiceItem(p.getProductId(), p.getName(), qty, p.getPrice(), p.getGstRate());
            invoiceItems.add(item);
            itemAdapter.notifyDataSetChanged();
            recalculateTotals();
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private List<String> getProductNames(List<Product> products) {
        List<String> names = new ArrayList<>();
        for(Product p : products) names.add(p.getName());
        return names;
    }

    private void loadProductsFromFirebase() {
        usersRef.child("products").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                products.clear();
                for(DataSnapshot ds : snapshot.getChildren()) {
                    Product p = ds.getValue(Product.class);
                    if(p != null) products.add(p);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load products", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void onSaveInvoice() {
        if(invoiceItems.isEmpty()) {
            Toast.makeText(getContext(), "Please add products.", Toast.LENGTH_SHORT).show();
            return;
        }
        int custPos = spCustomer.getSelectedItemPosition();
        if(custPos < 0 || custPos >= customers.size()) {
            Toast.makeText(getContext(), "Please select customer.", Toast.LENGTH_SHORT).show();
            return;
        }

        Customer customer = customers.get(custPos);
        String invoiceNumber = generateInvoiceNumber();
        String invoiceDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        Invoice invoice = new Invoice(invoiceNumber, customer.phone, customer.name, invoiceDate,
                new ArrayList<>(invoiceItems), totalTaxable, totalCGST, totalSGST, totalIGST, grandTotal,
                businessName, businessAddress);

        invoicesRef.child(invoiceNumber).setValue(invoice).addOnSuccessListener(aVoid -> {
            Toast.makeText(getContext(), "Invoice saved", Toast.LENGTH_SHORT).show();

            invoiceItems.clear();
            itemAdapter.notifyDataSetChanged();
            recalculateTotals();

            generateAndOpenPdf(invoice);

            File pdfFile = new File(requireContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)+"/Invoices/"+invoiceNumber+".pdf");
            sharePdfToWhatsapp(pdfFile, customer.phone);

        }).addOnFailureListener(e -> Toast.makeText(getContext(), "Error saving invoice: "+ e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private String generateInvoiceNumber() {
        String datePart = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());
        return "INV-" + datePart + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    private void generateAndOpenPdf(Invoice invoice) {
        try {
            PdfDocument pdf = new PdfDocument();
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595,842,1).create();
            PdfDocument.Page page = pdf.startPage(pageInfo);
            Canvas canvas = page.getCanvas();

            Paint paint = new Paint();
            int margin = 40, yPos = 50;

            paint.setTypeface(Typeface.DEFAULT_BOLD);
            paint.setTextSize(22);
            canvas.drawText(businessName != null ? businessName : "Business Name", margin, yPos, paint);

            paint.setTypeface(Typeface.DEFAULT);
            paint.setTextSize(12);
            yPos += 25;
            if(businessAddress != null) canvas.drawText(businessAddress, margin, yPos, paint);
            yPos += 15;

            canvas.drawText("Invoice No: " + invoice.invoiceNumber, margin, yPos, paint);
            canvas.drawText("Date: " + invoice.invoiceDate, margin + 300, yPos, paint);
            yPos += 25;

            paint.setTypeface(Typeface.DEFAULT_BOLD);
            canvas.drawText("Bill To:", margin, yPos, paint);
            yPos += 15;
            paint.setTypeface(Typeface.DEFAULT);
            canvas.drawText(invoice.customerName, margin + 10, yPos, paint);
            yPos += 25;

            paint.setTypeface(Typeface.DEFAULT_BOLD);
            canvas.drawText("Product", margin, yPos, paint);
            canvas.drawText("Qty", margin + 200, yPos, paint);
            canvas.drawText("Rate", margin + 270, yPos, paint);
            canvas.drawText("GST%", margin + 340, yPos, paint);
            canvas.drawText("Taxable", margin + 400, yPos, paint);
            canvas.drawText("Tax Amt", margin + 480, yPos, paint);
            yPos += 18;

            paint.setTypeface(Typeface.DEFAULT);
            for(InvoiceItem item : invoice.items){
                canvas.drawText(item.productName, margin, yPos, paint);
                canvas.drawText(String.format(Locale.getDefault(), "%.2f", item.quantity), margin+200, yPos, paint);
                canvas.drawText(String.format(Locale.getDefault(), "₹%.2f", item.rate), margin+270, yPos, paint);
                canvas.drawText(String.format(Locale.getDefault(), "%.1f%%", item.taxPercent), margin+340, yPos, paint);
                canvas.drawText(String.format(Locale.getDefault(), "₹%.2f", item.getTaxableValue()), margin+400, yPos, paint);

                GstCalculationUtil.GstDetails gst = GstCalculationUtil.calculateGst(item.getTaxableValue(), item.taxPercent, isIntraState);
                double taxAmount = gst.cgst + gst.sgst + gst.igst;
                canvas.drawText(String.format(Locale.getDefault(), "₹%.2f", taxAmount), margin+480, yPos, paint);
                yPos += 20;
            }

            yPos += 20;
            paint.setTypeface(Typeface.DEFAULT_BOLD);
            canvas.drawText(String.format(Locale.getDefault(), "Taxable Value: ₹%.2f", invoice.totalTaxableValue), margin, yPos, paint);
            yPos += 18;
            canvas.drawText(String.format(Locale.getDefault(), "CGST: ₹%.2f", invoice.totalCGST), margin, yPos, paint);
            yPos += 18;
            canvas.drawText(String.format(Locale.getDefault(), "SGST: ₹%.2f", invoice.totalSGST), margin, yPos, paint);
            yPos += 18;
            canvas.drawText(String.format(Locale.getDefault(), "IGST: ₹%.2f", invoice.totalIGST), margin, yPos, paint);
            yPos += 18;
            canvas.drawText(String.format(Locale.getDefault(), "Grand Total: ₹%.2f", invoice.grandTotal), margin, yPos, paint);

            pdf.finishPage(page);

            File invoiceDir = new File(requireContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "Invoices");
            if(!invoiceDir.exists()) invoiceDir.mkdirs();

            File file = new File(invoiceDir, invoice.invoiceNumber + ".pdf");
            pdf.writeTo(new FileOutputStream(file));
            pdf.close();

            openPdf(file);
        }catch(Exception e){
            e.printStackTrace();
            Toast.makeText(requireContext(), "Error generating PDF: "+e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void openPdf(File file) {
        Uri pdfUri = FileProvider.getUriForFile(requireContext(),
                requireContext().getPackageName()+".fileprovider", file);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(pdfUri, "application/pdf");
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_GRANT_READ_URI_PERMISSION);

        try {
            startActivity(intent);
        } catch(ActivityNotFoundException e) {
            Toast.makeText(requireContext(), "No PDF viewer installed.", Toast.LENGTH_LONG).show();
        }
    }

    private void sharePdfToWhatsapp(File pdfFile, String customerPhoneNumber) {
        try {
            if (customerPhoneNumber == null || customerPhoneNumber.isEmpty()) {
                Toast.makeText(requireContext(), "Customer phone number not found!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Clean number (remove +, spaces, etc.)
            customerPhoneNumber = customerPhoneNumber.replaceAll("[^0-9]", "");
            if (!customerPhoneNumber.startsWith("91")) { // add India code if missing
                customerPhoneNumber = "91" + customerPhoneNumber;
            }

            Uri pdfUri = FileProvider.getUriForFile(
                    requireContext(),
                    requireContext().getPackageName() + ".fileprovider",
                    pdfFile
            );

            Intent sendIntent = new Intent(Intent.ACTION_SEND);
            sendIntent.setType("application/pdf");
            sendIntent.putExtra(Intent.EXTRA_STREAM, pdfUri);
            sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            // ✅ Direct WhatsApp chat
            sendIntent.setPackage("com.whatsapp");
            sendIntent.putExtra("jid", customerPhoneNumber + "@s.whatsapp.net"); // direct to chat

            startActivity(sendIntent);

        } catch (ActivityNotFoundException e) {
            Toast.makeText(requireContext(), "WhatsApp not installed!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

}
