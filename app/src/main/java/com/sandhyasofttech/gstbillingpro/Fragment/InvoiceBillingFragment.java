package com.sandhyasofttech.gstbillingpro.Fragment;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.sandhyasofttech.gstbillingpro.Activity.CustomerSelectionActivity;
import com.sandhyasofttech.gstbillingpro.R;

public class InvoiceBillingFragment extends Fragment {

    private Button btnCreateInvoice;
    private TextView tvWelcome, tvInstructions;
    private String userMobile;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_invoice_billing, container, false);

        // Initialize views
        btnCreateInvoice = view.findViewById(R.id.btnCreateInvoice);
        tvWelcome = view.findViewById(R.id.tvWelcome);
        tvInstructions = view.findViewById(R.id.tvInstructions);

        // Get user mobile
        SharedPreferences prefs = requireContext().getSharedPreferences("APP_PREFS", Activity.MODE_PRIVATE);
        userMobile = prefs.getString("USER_MOBILE", null);

        if (userMobile == null) {
            Toast.makeText(getContext(), "Please login", Toast.LENGTH_SHORT).show();
            return view;
        }

        // Set up click listener for create invoice button
        btnCreateInvoice.setOnClickListener(v -> {
            // Launch the 3-part invoice flow starting with Customer Selection
            Intent intent = new Intent(getActivity(), CustomerSelectionActivity.class);
            startActivity(intent);
        });

        return view;
    }
}