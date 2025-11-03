package com.sandhyasofttech.gstbillingpro.Registration;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.sandhyasofttech.gstbillingpro.R;

public class RegistrationActivity extends AppCompatActivity {

    private EditText businessName, businessType, ownerName, gstin, mobile, email, address, pin;
    private Button registerBtn;
    private DatabaseReference databaseUsers;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        // Toolbar setup for back navigation
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Registration");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        businessName = findViewById(R.id.businessName);
        businessType = findViewById(R.id.businessType);
        ownerName = findViewById(R.id.ownerName);
        gstin = findViewById(R.id.gstin);
        mobile = findViewById(R.id.mobile);
        email = findViewById(R.id.email);
        address = findViewById(R.id.address);
        pin = findViewById(R.id.pin);
        registerBtn = findViewById(R.id.registerBtn);

        databaseUsers = FirebaseDatabase.getInstance().getReference("users");

        registerBtn.setOnClickListener(v -> {
            String bName = businessName.getText().toString().trim();
            String bType = businessType.getText().toString().trim();
            String oName = ownerName.getText().toString().trim();
            String gst = gstin.getText().toString().trim();
            String mob = mobile.getText().toString().trim();
            String mail = email.getText().toString().trim();
            String addr = address.getText().toString().trim();
            String p = pin.getText().toString().trim();

            if (bName.isEmpty() || bType.isEmpty() || oName.isEmpty() || gst.isEmpty() || mob.isEmpty()
                    || mail.isEmpty() || addr.isEmpty() || p.isEmpty()) {
                Toast.makeText(RegistrationActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }
            if (mob.length() != 10) {
                mobile.setError("Enter valid 10-digit mobile number");
                mobile.requestFocus();
                return;
            }
            if (p.length() != 4) {
                pin.setError("Enter 4-digit PIN");
                pin.requestFocus();
                return;
            }

            User user = new User(bName, bType, oName, gst, mob, mail, addr, p);

            // Save user data under mobile/info node
            databaseUsers.child(mob).child("info").setValue(user)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(RegistrationActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(RegistrationActivity.this, LoginActivity.class));
                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(RegistrationActivity.this, "Registration failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        });
    }

    public static class User {
        public String businessName, businessType, ownerName, gstin;
        public String mobile, email, address, pin;

        public User() {
            // Empty constructor needed for Firebase
        }

        public User(String businessName, String businessType, String ownerName, String gstin,
                    String mobile, String email, String address, String pin) {
            this.businessName = businessName;
            this.businessType = businessType;
            this.ownerName = ownerName;
            this.gstin = gstin;
            this.mobile = mobile;
            this.email = email;
            this.address = address;
            this.pin = pin;
        }
    }
}
