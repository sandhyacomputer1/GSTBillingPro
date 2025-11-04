//package com.sandhyasofttech.gstbillingpro.Activity;
//
//import android.app.AlertDialog;
//import android.content.DialogInterface;
//import android.os.Bundle;
//import android.text.Editable;
//import android.text.TextWatcher;
//import android.widget.EditText;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.google.firebase.database.DataSnapshot;
//import com.google.firebase.database.DatabaseError;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
//import com.google.firebase.database.ValueEventListener;
//import com.sandhyasofttech.gstbillingpro.Adapter.AllInvoicesAdapter;
//import com.sandhyasofttech.gstbillingpro.Model.RecentInvoiceItem;
//import com.sandhyasofttech.gstbillingpro.R;
//
//import java.util.ArrayList;
//import java.util.Collections;
//
//public class AllInvoicesActivity extends AppCompatActivity {
//
//    private RecyclerView rvAllInvoices;
//    private EditText etSearchInvoice;
//    private DatabaseReference userRef;
//    private ArrayList<RecentInvoiceItem> fullList = new ArrayList<>();
//    private ArrayList<RecentInvoiceItem> filteredList = new ArrayList<>();
//    private AllInvoicesAdapter adapter;
//    private String userMobile = "";
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_all_invoices);
//
//        rvAllInvoices = findViewById(R.id.rvAllInvoices);
//        etSearchInvoice = findViewById(R.id.etSearchInvoice);
//
//        // Get user mobile from SharedPreferences
//        userMobile = getSharedPreferences("APP_PREFS", MODE_PRIVATE)
//                .getString("USER_MOBILE", null);
//
//        if (userMobile == null) {
//            Toast.makeText(this, "Session expired. Please log in again.", Toast.LENGTH_LONG).show();
//            finish();
//            return;
//        }
//
//        userRef = FirebaseDatabase.getInstance().getReference("users").child(userMobile);
//
//        rvAllInvoices.setLayoutManager(new LinearLayoutManager(this));
//        adapter = new AllInvoicesAdapter(filteredList, userRef);
//        rvAllInvoices.setAdapter(adapter);
//
//        loadAllInvoices();
//
//        etSearchInvoice.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                filterInvoices(s.toString());
//            }
//            @Override
//            public void afterTextChanged(Editable s) {}
//        });
//    }
//
//    private void loadAllInvoices() {
//        userRef.child("invoices").addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                fullList.clear();
//                for (DataSnapshot ds : snapshot.getChildren()) {
//                    String invoiceNo = ds.child("invoiceNumber").getValue(String.class);
//                    String customerId = ds.child("customerId").getValue(String.class);
//                    String customerName = ds.child("customerName").getValue(String.class);
//                    Double grandTotal = ds.child("grandTotal").getValue(Double.class);
//                    String date = ds.child("invoiceDate").getValue(String.class);
//
//                    if (invoiceNo != null && customerName != null && grandTotal != null && date != null) {
//                        fullList.add(new RecentInvoiceItem(invoiceNo, customerId, customerName, grandTotal, date));
//                    }
//                }
//                Collections.reverse(fullList);
//                filteredList.clear();
//                filteredList.addAll(fullList);
//                adapter.notifyDataSetChanged();
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Toast.makeText(AllInvoicesActivity.this, "Failed to load invoices", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
//
//    private void filterInvoices(String query) {
//        filteredList.clear();
//        if (query.isEmpty()) {
//            filteredList.addAll(fullList);
//        } else {
//            for (RecentInvoiceItem item : fullList) {
//                if (item.customerName.toLowerCase().contains(query.toLowerCase()) ||
//                        item.invoiceNo.toLowerCase().contains(query.toLowerCase())) {
//                    filteredList.add(item);
//                }
//            }
//        }
//        adapter.notifyDataSetChanged();
//    }
//}



package com.sandhyasofttech.gstbillingpro.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sandhyasofttech.gstbillingpro.Adapter.AllInvoicesAdapter;
import com.sandhyasofttech.gstbillingpro.MainActivity;
import com.sandhyasofttech.gstbillingpro.Model.RecentInvoiceItem;
import com.sandhyasofttech.gstbillingpro.R;

import java.util.ArrayList;
import java.util.Collections;

public class AllInvoicesActivity extends AppCompatActivity {

    private RecyclerView rvAllInvoices;
    private EditText etSearchInvoice;
    private DatabaseReference userRef;
    private ArrayList<RecentInvoiceItem> fullList = new ArrayList<>();
    private ArrayList<RecentInvoiceItem> filteredList = new ArrayList<>();
    private AllInvoicesAdapter adapter;
    private String userMobile = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_invoices);

        // 🔹 Toolbar setup
        Toolbar toolbar = findViewById(R.id.toolbarAllInvoices);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> {
            // Navigate back to HomeFragment in MainActivity
            Intent intent = new Intent(AllInvoicesActivity.this, MainActivity.class);
            intent.putExtra("openFragment", "HomeFragment");
            startActivity(intent);
            finish();
        });

        rvAllInvoices = findViewById(R.id.rvAllInvoices);
        etSearchInvoice = findViewById(R.id.etSearchInvoice);

        // Get user mobile from SharedPreferences
        userMobile = getSharedPreferences("APP_PREFS", MODE_PRIVATE)
                .getString("USER_MOBILE", null);

        if (userMobile == null) {
            Toast.makeText(this, "Session expired. Please log in again.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        userRef = FirebaseDatabase.getInstance().getReference("users").child(userMobile);

        rvAllInvoices.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AllInvoicesAdapter(filteredList, userRef);
        rvAllInvoices.setAdapter(adapter);

        loadAllInvoices();

        etSearchInvoice.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterInvoices(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadAllInvoices() {
        userRef.child("invoices").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                fullList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String invoiceNo = ds.child("invoiceNumber").getValue(String.class);
                    String customerId = ds.child("customerId").getValue(String.class);
                    String customerName = ds.child("customerName").getValue(String.class);
                    Double grandTotal = ds.child("grandTotal").getValue(Double.class);
                    String date = ds.child("invoiceDate").getValue(String.class);

                    if (invoiceNo != null && customerName != null && grandTotal != null && date != null) {
                        fullList.add(new RecentInvoiceItem(invoiceNo, customerId, customerName, grandTotal, date));
                    }
                }
                Collections.reverse(fullList);
                filteredList.clear();
                filteredList.addAll(fullList);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AllInvoicesActivity.this, "Failed to load invoices", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterInvoices(String query) {
        filteredList.clear();
        if (query.isEmpty()) {
            filteredList.addAll(fullList);
        } else {
            for (RecentInvoiceItem item : fullList) {
                if (item.customerName.toLowerCase().contains(query.toLowerCase()) ||
                        item.invoiceNo.toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(item);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }
}
