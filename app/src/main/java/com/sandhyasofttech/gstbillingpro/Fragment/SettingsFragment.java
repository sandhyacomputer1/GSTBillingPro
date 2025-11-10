package com.sandhyasofttech.gstbillingpro.Fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.BuildConfig;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.sandhyasofttech.gstbillingpro.Activity.ChangePasswordBottomSheet;
import com.sandhyasofttech.gstbillingpro.Activity.EditFieldBottomSheet;
import com.sandhyasofttech.gstbillingpro.R;
import com.sandhyasofttech.gstbillingpro.Registration.LoginActivity;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsFragment extends Fragment {

    private CircleImageView imgUserProfile;
    private TextView tvOwnerName, tvEmail, tvBusinessName, tvBusinessType, tvGstin, tvAddress, tvMobile, tvAppVersion;
    private SwitchMaterial switchDarkMode;

    private DatabaseReference userInfoRef;
    private DatabaseReference userRef;
    private String userMobile;

    public SettingsFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        // Initialize Views
        imgUserProfile = view.findViewById(R.id.imgUserProfile);
        tvOwnerName = view.findViewById(R.id.tvOwnerName);
        tvEmail = view.findViewById(R.id.tvEmail); // Now in Business Info
        tvBusinessName = view.findViewById(R.id.tvBusinessName);
        tvBusinessType = view.findViewById(R.id.tvBusinessType);
        tvGstin = view.findViewById(R.id.tvGstin);
        tvAddress = view.findViewById(R.id.tvAddress);
        tvMobile = view.findViewById(R.id.tvMobile);
        tvAppVersion = view.findViewById(R.id.tvAppVersion);
        switchDarkMode = view.findViewById(R.id.switchDarkMode);

        // App Version
        tvAppVersion.setText(BuildConfig.VERSION_NAME);

        // Logout
        view.findViewById(R.id.btnLogout).setOnClickListener(v -> logoutUser());

        // Dark Mode
        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Toast.makeText(requireContext(), "Dark Mode: " + (isChecked ? "ON" : "OFF"), Toast.LENGTH_SHORT).show();
        });

        // Load Data
        loadUserData();

        // Clickable Rows → Open Bottom Sheet
        view.findViewById(R.id.layoutProfile).setOnClickListener(v ->
                openEditDialog("Owner Name", tvOwnerName, "ownerName"));

        view.findViewById(R.id.layoutEmail).setOnClickListener(v ->
                openEditDialog("Email", tvEmail, "email"));

        view.findViewById(R.id.layoutBusinessName).setOnClickListener(v ->
                openEditDialog("Business Name", tvBusinessName, "businessName"));

        view.findViewById(R.id.layoutBusinessType).setOnClickListener(v ->
                openEditDialog("Business Type", tvBusinessType, "businessType"));

        view.findViewById(R.id.layoutGstin).setOnClickListener(v ->
                openEditDialog("GSTIN", tvGstin, "gstin"));

        view.findViewById(R.id.layoutAddress).setOnClickListener(v ->
                openEditDialog("Address", tvAddress, "address"));

        view.findViewById(R.id.layoutMobile).setOnClickListener(v ->
                openEditDialog("Mobile", tvMobile, "mobile"));

        // Change Password (4-digit PIN)
        view.findViewById(R.id.layoutChangePassword).setOnClickListener(v -> {
            if (userRef == null) {
                Toast.makeText(requireContext(), "User not loaded", Toast.LENGTH_SHORT).show();
                return;
            }
            ChangePasswordBottomSheet dialog = ChangePasswordBottomSheet.newInstance(userRef, userMobile);
            dialog.show(getParentFragmentManager(), "change_pin");
        });

        // Other Actions
        view.findViewById(R.id.layoutBackup).setOnClickListener(v -> backupRestore());
        view.findViewById(R.id.layoutRateApp).setOnClickListener(v -> rateApp());
        view.findViewById(R.id.layoutShareApp).setOnClickListener(v -> shareApp());

        return view;
    }

    // Open Edit Bottom Sheet
    private void openEditDialog(String fieldName, TextView textView, String fieldKey) {
        if (userInfoRef == null) {
            Toast.makeText(requireContext(), "Loading data...", Toast.LENGTH_SHORT).show();
            return;
        }

        EditFieldBottomSheet dialog = EditFieldBottomSheet.newInstance(
                fieldName,
                textView.getText().toString(),
                fieldKey,
                userInfoRef
        );

        dialog.setOnFieldUpdatedListener(newValue -> textView.setText(newValue));
        dialog.show(getParentFragmentManager(), "edit_field");
    }

    // Load User Data
    private void loadUserData() {
        SharedPreferences prefs = requireContext().getSharedPreferences("APP_PREFS", requireContext().MODE_PRIVATE);
        userMobile = prefs.getString("USER_MOBILE", null);

        if (userMobile == null) {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(getActivity(), LoginActivity.class));
            requireActivity().finish();
            return;
        }

        userRef = FirebaseDatabase.getInstance().getReference("users").child(userMobile);
        userInfoRef = userRef.child("info");

        userInfoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    tvOwnerName.setText(getStringOrDefault(snapshot, "ownerName"));
                    tvEmail.setText(getStringOrDefault(snapshot, "email"));
                    tvBusinessName.setText(getStringOrDefault(snapshot, "businessName"));
                    tvBusinessType.setText(getStringOrDefault(snapshot, "businessType"));
                    tvGstin.setText(getStringOrDefault(snapshot, "gstin"));
                    tvAddress.setText(getStringOrDefault(snapshot, "address"));
                    tvMobile.setText(getStringOrDefault(snapshot, "mobile"));
                } else {
                    Toast.makeText(getContext(), "User info not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getStringOrDefault(DataSnapshot snapshot, String key) {
        String value = snapshot.child(key).getValue(String.class);
        return (value != null && !value.isEmpty()) ? value : "Not set";
    }

    // Action Handlers
    private void backupRestore() {
        Toast.makeText(requireContext(), "Backup & Restore", Toast.LENGTH_SHORT).show();
    }

    private void rateApp() {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=" + requireContext().getPackageName())));
        } catch (Exception e) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=" + requireContext().getPackageName())));
        }
    }

    private void shareApp() {
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        share.putExtra(Intent.EXTRA_TEXT,
                "Download GST Billing Pro: https://play.google.com/store/apps/details?id=" + requireContext().getPackageName());
        startActivity(Intent.createChooser(share, "Share App"));
    }

    private void logoutUser() {
        SharedPreferences prefs = requireContext().getSharedPreferences("APP_PREFS", requireContext().MODE_PRIVATE);
        prefs.edit().clear().apply();

        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }
}