package com.sandhyasofttech.gstbillingpro.Fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sandhyasofttech.gstbillingpro.R;
import com.sandhyasofttech.gstbillingpro.Registration.LoginActivity;

public class SettingsFragment extends Fragment {

    private ImageView imgUserProfile;
    private TextView tvOwnerName, tvEmail, tvBusinessName, tvBusinessType, tvGstin, tvAddress, tvMobile;
    private ImageView btnEditBusinessName, btnEditBusinessType, btnEditGstin, btnEditAddress, btnEditMobile;
    private Button btnLogout;

    private DatabaseReference userInfoRef;
    private String userMobile;

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        // Initialize views
        imgUserProfile = view.findViewById(R.id.imgUserProfile);
        tvOwnerName = view.findViewById(R.id.tvOwnerName);
        tvEmail = view.findViewById(R.id.tvEmail);
        tvBusinessName = view.findViewById(R.id.tvBusinessName);
        tvBusinessType = view.findViewById(R.id.tvBusinessType);
        tvGstin = view.findViewById(R.id.tvGstin);
        tvAddress = view.findViewById(R.id.tvAddress);
        tvMobile = view.findViewById(R.id.tvMobile);

        btnEditBusinessName = view.findViewById(R.id.btnEditBusinessName);
        btnEditBusinessType = view.findViewById(R.id.btnEditBusinessType);
        btnEditGstin = view.findViewById(R.id.btnEditGstin);
        btnEditAddress = view.findViewById(R.id.btnEditAddress);
        btnEditMobile = view.findViewById(R.id.btnEditMobile);

        btnLogout = view.findViewById(R.id.btnLogout);

        // Get logged-in user's mobile number from SharedPreferences
        SharedPreferences prefs = requireContext().getSharedPreferences("APP_PREFS", getContext().MODE_PRIVATE);
        userMobile = prefs.getString("USER_MOBILE", null);

        if (userMobile == null) {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(getActivity(), LoginActivity.class));
            getActivity().finish();
            return view;
        }

        // Firebase reference
        userInfoRef = FirebaseDatabase.getInstance().getReference("users").child(userMobile).child("info");

        // Load user data
        fetchUserData();

        // Logout
        btnLogout.setOnClickListener(v -> logoutUser());

        // Optional: handle edit buttons clicks
        btnEditBusinessName.setOnClickListener(v -> Toast.makeText(getContext(), "Edit Business Name", Toast.LENGTH_SHORT).show());
        btnEditBusinessType.setOnClickListener(v -> Toast.makeText(getContext(), "Edit Business Type", Toast.LENGTH_SHORT).show());
        btnEditGstin.setOnClickListener(v -> Toast.makeText(getContext(), "Edit GSTIN", Toast.LENGTH_SHORT).show());
        btnEditAddress.setOnClickListener(v -> Toast.makeText(getContext(), "Edit Address", Toast.LENGTH_SHORT).show());
        btnEditMobile.setOnClickListener(v -> Toast.makeText(getContext(), "Edit Mobile", Toast.LENGTH_SHORT).show());

        return view;
    }

    private void fetchUserData() {
        userInfoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    tvOwnerName.setText(snapshot.child("ownerName").getValue(String.class));
                    tvEmail.setText(snapshot.child("email").getValue(String.class));
                    tvBusinessName.setText(snapshot.child("businessName").getValue(String.class));
                    tvBusinessType.setText(snapshot.child("businessType").getValue(String.class));
                    tvGstin.setText(snapshot.child("gstin").getValue(String.class));
                    tvAddress.setText(snapshot.child("address").getValue(String.class));
                    tvMobile.setText(snapshot.child("mobile").getValue(String.class));
                } else {
                    Toast.makeText(getContext(), "User info not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load user info: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void logoutUser() {
        SharedPreferences prefs = requireContext().getSharedPreferences("APP_PREFS", getContext().MODE_PRIVATE);
        prefs.edit().clear().apply();

        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        getActivity().finish();
    }
}
