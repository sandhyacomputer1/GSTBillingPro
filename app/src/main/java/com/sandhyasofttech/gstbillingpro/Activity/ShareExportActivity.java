package com.sandhyasofttech.gstbillingpro.Activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sandhyasofttech.gstbillingpro.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ShareExportActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 100;

    private MaterialButton btnGenerateInvoicesPdf, btnGenerateCustomersPdf, btnGenerateProductsPdf;
    private TextView tvStatus;
    private ImageView imgBack;

    private String userMobile = "9000090000";

    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_export);

        imgBack = findViewById(R.id.imgBack);
        imgBack.setOnClickListener(v -> finish()); // Back button finishes activity

        btnGenerateInvoicesPdf = findViewById(R.id.btnGenerateInvoicesPdf);
        btnGenerateCustomersPdf = findViewById(R.id.btnGenerateCustomersPdf);
        btnGenerateProductsPdf = findViewById(R.id.btnGenerateProductsPdf);
        tvStatus = findViewById(R.id.tvStatus);

        userRef = FirebaseDatabase.getInstance().getReference("users").child(userMobile);

        btnGenerateInvoicesPdf.setOnClickListener(v -> checkPermissionAndGenerate("invoices"));
        btnGenerateCustomersPdf.setOnClickListener(v -> checkPermissionAndGenerate("customers"));
        btnGenerateProductsPdf.setOnClickListener(v -> checkPermissionAndGenerate("products"));
    }

    private void checkPermissionAndGenerate(String dataType) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
                tvStatus.setText("Permission requested. Please press button again.");
                return;
            }
        }
        generatePdfForType(dataType);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                tvStatus.setText("Permission granted. Please press button again.");
            } else {
                Toast.makeText(this, "Storage permission denied. Cannot generate PDF.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void generatePdfForType(String dataType) {
        tvStatus.setText("Fetching data for " + dataType + "...");
        DatabaseReference dataRef = userRef.child(dataType);

        dataRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    tvStatus.setText("No data found for " + dataType);
                    return;
                }

                try {
                    generatePdfDocument(dataType, snapshot);
                    tvStatus.setText(capitalize(dataType) + " PDF generated successfully.");
                } catch (Exception e) {
                    tvStatus.setText("Error generating PDF: " + e.getMessage());
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                tvStatus.setText("Firebase data fetch error: " + error.getMessage());
            }
        });
    }

    private void generatePdfDocument(String dataType, DataSnapshot snapshot) throws IOException {
        PdfDocument pdfDocument = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create(); // A4
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        int pageWidth = pageInfo.getPageWidth();
        int pageHeight = pageInfo.getPageHeight();

        // Margins
        int margin = 40;
        int startX = margin;
        int y = margin + 40;

        // Colors & Paints setup (same as you had)
        int headerColor = Color.rgb(0, 102, 204);     // Blue
        int accentColor = Color.rgb(230, 240, 255);   // Light blue-gray background
        int textPrimary = Color.BLACK;
        int textSecondary = Color.DKGRAY;

        Paint titlePaint = new Paint();
        titlePaint.setColor(headerColor);
        titlePaint.setTextSize(26f);
        titlePaint.setFakeBoldText(true);
        titlePaint.setAntiAlias(true);

        Paint subTitlePaint = new Paint();
        subTitlePaint.setColor(textSecondary);
        subTitlePaint.setTextSize(16f);
        subTitlePaint.setAntiAlias(true);

        Paint headerBgPaint = new Paint();
        headerBgPaint.setColor(headerColor);

        Paint tableBgPaint = new Paint();
        tableBgPaint.setColor(accentColor);

        Paint headerTextPaint = new Paint();
        headerTextPaint.setColor(Color.WHITE);
        headerTextPaint.setTextSize(14f);
        headerTextPaint.setFakeBoldText(true);

        Paint dataTextPaint = new Paint();
        dataTextPaint.setColor(textPrimary);
        dataTextPaint.setTextSize(12f);

        Paint borderPaint = new Paint();
        borderPaint.setColor(Color.LTGRAY);
        borderPaint.setStrokeWidth(1.5f);
        borderPaint.setStyle(Paint.Style.STROKE);

        // Header + Subtitle
        canvas.drawText("GST Billing Application", startX + 60, margin + 30, titlePaint);
        canvas.drawText("Business Data Report", startX + 60, margin + 60, subTitlePaint);
        canvas.drawLine(margin, margin + 70, pageWidth - margin, margin + 70, borderPaint);

        y += 60;
        Paint infoPaint = new Paint();
        infoPaint.setColor(textSecondary);
        infoPaint.setTextSize(12f);
        String date = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(new Date());
        canvas.drawText("Generated On: " + date, startX, y, infoPaint);
        y += 20;
        canvas.drawText("User Mobile: " + userMobile, startX, y, infoPaint);
        y += 30;

        Paint sectionTitle = new Paint();
        sectionTitle.setColor(headerColor);
        sectionTitle.setTextSize(18f);
        sectionTitle.setFakeBoldText(true);
        canvas.drawText(capitalize(dataType) + " Summary", startX, y, sectionTitle);
        y += 20;
        canvas.drawLine(startX, y, pageWidth - margin, y, borderPaint);
        y += 20;

        // Define headers and dynamic column widths
        String[] headers;
        int[] colWidths;

        if ("invoices".equals(dataType)) {
            headers = new String[]{"Invoice No", "Customer", "Date", "Total"};
            colWidths = new int[]{120, 190, 120, 100};
        } else if ("customers".equals(dataType)) {
            headers = new String[]{"ID", "Name", "Phone", "Email", "GSTIN"};
            colWidths = new int[]{80, 130, 100, 150, 80};
        } else {
            // Dynamic sizing for products: Product ID column wider
            headers = new String[]{"Product ID", "Name", "Price"};
            colWidths = new int[]{250, 200, 100}; // increased productId width
        }

        // Calculate column positions for drawing
        int[] colX = new int[headers.length];
        colX[0] = startX;
        for (int i = 1; i < headers.length; i++) {
            colX[i] = colX[i - 1] + colWidths[i - 1];
        }
        int tableRight = colX[headers.length - 1] + colWidths[colWidths.length - 1];
        int rowHeight = 30;

        // Draw table header background and text
        canvas.drawRect(startX, y, tableRight, y + rowHeight, headerBgPaint);
        for (int i = 0; i < headers.length; i++) {
            canvas.drawText(headers[i], colX[i] + 8, y + 20, headerTextPaint);
        }
        y += rowHeight;

        // Draw rows with alternating backgrounds and data
        boolean alt = false;
        int rowCount = 0;
        for (DataSnapshot ds : snapshot.getChildren()) {
            if (alt) canvas.drawRect(startX, y, tableRight, y + rowHeight, tableBgPaint);
            alt = !alt;

            if ("invoices".equals(dataType)) {
                canvas.drawText(ds.child("invoiceNumber").getValue(String.class), colX[0] + 8, y + 20, dataTextPaint);
                canvas.drawText(ds.child("customerName").getValue(String.class), colX[1] + 8, y + 20, dataTextPaint);
                canvas.drawText(ds.child("invoiceDate").getValue(String.class), colX[2] + 8, y + 20, dataTextPaint);
                Double total = ds.child("grandTotal").getValue(Double.class);
                canvas.drawText(String.format("₹%.2f", total != null ? total : 0.0), colX[3] + 8, y + 20, dataTextPaint);
            } else if ("customers".equals(dataType)) {
                canvas.drawText(ds.child("id").getValue(String.class), colX[0] + 8, y + 20, dataTextPaint);
                canvas.drawText(ds.child("name").getValue(String.class), colX[1] + 8, y + 20, dataTextPaint);
                canvas.drawText(ds.child("phone").getValue(String.class), colX[2] + 8, y + 20, dataTextPaint);
                canvas.drawText(ds.child("email").getValue(String.class), colX[3] + 8, y + 20, dataTextPaint);
                canvas.drawText(ds.child("gstin").getValue(String.class), colX[4] + 8, y + 20, dataTextPaint);
            } else {
                canvas.drawText(ds.getKey(), colX[0] + 8, y + 20, dataTextPaint);
                canvas.drawText(ds.child("name").getValue(String.class), colX[1] + 8, y + 20, dataTextPaint);
                Double price = ds.child("price").getValue(Double.class);
                canvas.drawText(String.format("₹%.2f", price != null ? price : 0.0), colX[2] + 8, y + 20, dataTextPaint);
            }

            // Draw vertical lines for columns
            for (int i = 0; i < colX.length; i++) {
                canvas.drawLine(colX[i], y, colX[i], y + rowHeight, borderPaint);
            }
            // Draw right border line
            canvas.drawLine(tableRight, y, tableRight, y + rowHeight, borderPaint);

            // Draw bottom horizontal border line for row
            canvas.drawLine(startX, y + rowHeight, tableRight, y + rowHeight, borderPaint);

            y += rowHeight;
            rowCount++;
            if (y + rowHeight > pageHeight - 100) break;
        }

        // Footer Section
        y += 40;
        Paint footerTitle = new Paint();
        footerTitle.setColor(headerColor);
        footerTitle.setTextSize(16f);
        footerTitle.setFakeBoldText(true);
        canvas.drawText("Report Summary", startX, y, footerTitle);
        y += 20;

        canvas.drawText("Total Records: " + rowCount, startX, y, dataTextPaint);
        y += 40;

        Paint footerPaint = new Paint();
        footerPaint.setColor(textSecondary);
        footerPaint.setTextSize(10f);
        canvas.drawLine(margin, pageHeight - 60, pageWidth - margin, pageHeight - 60, borderPaint);
        canvas.drawText("© 2025 GST Billing Pro | support@gstbillingpro.com", margin, pageHeight - 40, footerPaint);
        canvas.drawText("Signature: ___________________", pageWidth - 220, pageHeight - 40, footerPaint);

        pdfDocument.finishPage(page);

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File dir = new File(getExternalFilesDir(null), "GSTBillingPDFs");
        if (!dir.exists()) dir.mkdirs();
        File file = new File(dir, dataType + "_report_" + timeStamp + ".pdf");

        try (FileOutputStream fos = new FileOutputStream(file)) {
            pdfDocument.writeTo(fos);
        }

        pdfDocument.close();

        tvStatus.setText("PDF saved: " + file.getAbsolutePath());
        Toast.makeText(this, "PDF generated successfully!", Toast.LENGTH_SHORT).show();
        openPdfFile(file);
    }

//    // Helper method to capitalize first letter
//    private String capitalize(String s) {
//        if (s == null || s.isEmpty()) return s;
//        return s.substring(0, 1).toUpperCase() + s.substring(1);
//    }
//
//
//
//    // Helper method to capitalize first letter of string
//    private String capitalize(String s) {
//        if (s == null || s.isEmpty()) return s;
//        return s.substring(0, 1).toUpperCase() + s.substring(1);
//    }









    // Helper method to draw headers line and titles with underline
    private void drawTableHeaders(Canvas canvas, Paint paint, int y, String[] headers, int[] colX) {
        for (int i = 0; i < headers.length; i++) {
            canvas.drawText(headers[i], colX[i], y, paint);
        }
        // Draw a line under headers
        canvas.drawLine(colX[0], y + 5, colX[colX.length - 1] + 60, y + 5, paint);
    }

    // Open PDF with FileProvider
    private void openPdfFile(File file) {
        try {
            Uri pdfUri = FileProvider.getUriForFile(this,
                    "com.sandhyasofttech.gstbillingpro.fileprovider", file);

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(pdfUri, "application/pdf");
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivity(Intent.createChooser(intent, "Open PDF"));
        } catch (Exception e) {
            Toast.makeText(this, "No app found to open PDF", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}
