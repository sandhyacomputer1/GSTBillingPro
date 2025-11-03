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
    private TextView tvOwnerName, tvEmail, tvBusinessName, tvBusinessType, tvGstin, tvAddress, tvPin, tvMobile;
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

        // Initialize Views
        imgUserProfile = view.findViewById(R.id.imgUserProfile);
        tvOwnerName = view.findViewById(R.id.tvOwnerName);
        tvEmail = view.findViewById(R.id.tvEmail);
        tvBusinessName = view.findViewById(R.id.tvBusinessName);
        tvBusinessType = view.findViewById(R.id.tvBusinessType);
        tvGstin = view.findViewById(R.id.tvGstin);
        tvAddress = view.findViewById(R.id.tvAddress);
        tvPin = view.findViewById(R.id.tvPin);
        tvMobile = view.findViewById(R.id.tvMobile);
        btnLogout = view.findViewById(R.id.btnLogout);

        // Get logged-in user's mobile number from SharedPreferences
        SharedPreferences prefs = requireContext().getSharedPreferences("APP_PREFS", getContext().MODE_PRIVATE);
        userMobile = prefs.getString("USER_MOBILE", null);

        if (userMobile == null) {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            // Optionally redirect to login
            startActivity(new Intent(getActivity(), LoginActivity.class));
            getActivity().finish();
            return view;
        }

        // Firebase reference to user's info node
        userInfoRef = FirebaseDatabase.getInstance().getReference("users").child(userMobile).child("info");

        // Fetch and display user data
        fetchUserData();

        // Logout button listener clears session and navigates to login
        btnLogout.setOnClickListener(v -> logoutUser());

        return view;
    }

    private void fetchUserData() {
        userInfoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String ownerName = snapshot.child("ownerName").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);
                    String businessName = snapshot.child("businessName").getValue(String.class);
                    String businessType = snapshot.child("businessType").getValue(String.class);
                    String gstin = snapshot.child("gstin").getValue(String.class);
                    String address = snapshot.child("address").getValue(String.class);
                    String pin = snapshot.child("pin").getValue(String.class);
                    String mobile = snapshot.child("mobile").getValue(String.class);

                    tvOwnerName.setText(ownerName != null ? ownerName : "N/A");
                    tvEmail.setText(email != null ? email : "N/A");
                    tvBusinessName.setText("Business Name: " + (businessName != null ? businessName : "N/A"));
                    tvBusinessType.setText("Business Type: " + (businessType != null ? businessType : "N/A"));
                    tvGstin.setText("GSTIN: " + (gstin != null ? gstin : "N/A"));
                    tvAddress.setText("Address: " + (address != null ? address : "N/A"));
                    tvPin.setText("PIN: " + (pin != null ? pin : "N/A"));
                    tvMobile.setText("Mobile: " + (mobile != null ? mobile : "N/A"));

                    // You can load profile image here if available with a library like Glide or Picasso
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
        // Clear login status and user info in SharedPreferences
        SharedPreferences prefs = requireContext().getSharedPreferences("APP_PREFS", getContext().MODE_PRIVATE);
        prefs.edit().clear().apply();

        // Navigate to login activity and clear back stack
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        getActivity().finish();
    }
}
