package com.sandhyasofttech.gstbillingpro.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
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

    private DatabaseReference userRef;

    private String userMobile = "6666666666"; // Use actual logged-in user's mobile or get dynamically

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate custom layout
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        // Initialize views
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

        // Setup Firebase Realtime Database reference
        userRef = FirebaseDatabase.getInstance().getReference("users").child(userMobile);

        // Fetch user data
        fetchUserData();

        // Logout button click listener
        btnLogout.setOnClickListener(v -> logoutUser());

        return view;
    }

    private void fetchUserData() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    // Set UI with data from Firebase
                    tvOwnerName.setText(snapshot.child("ownerName").getValue(String.class));
                    tvEmail.setText(snapshot.child("email").getValue(String.class));
                    tvBusinessName.setText("Business Name: " + snapshot.child("businessName").getValue(String.class));
                    tvBusinessType.setText("Business Type: " + snapshot.child("businessType").getValue(String.class));
                    tvGstin.setText("GSTIN: " + snapshot.child("gstin").getValue(String.class));
                    tvAddress.setText("Address: " + snapshot.child("address").getValue(String.class));
                    tvPin.setText("PIN: " + snapshot.child("pin").getValue(String.class));
                    tvMobile.setText("Mobile: " + snapshot.child("mobile").getValue(String.class));
                    // You could load profile image here if available
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });
    }

    private void logoutUser() {
        // Clear session or auth here

        // Redirect to login screen and clear back stack
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        getActivity().finish();
    }
}