package com.sandhyasofttech.gstbillingpro.Registration;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sandhyasofttech.gstbillingpro.MainActivity;
import com.sandhyasofttech.gstbillingpro.R;

public class LoginActivity extends AppCompatActivity {

    private EditText phoneInput, pinInput;
    private Button loginBtn;
    private TextView registrationLink;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        phoneInput = findViewById(R.id.phoneInput);
        pinInput = findViewById(R.id.pinInput);
        loginBtn = findViewById(R.id.loginBtn);
        registrationLink = findViewById(R.id.registrationLink);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        usersRef = database.getReference("users");

        loginBtn.setOnClickListener(v -> {
            String mobile = phoneInput.getText().toString().trim();
            String pin = pinInput.getText().toString().trim();

            if (mobile.isEmpty() || mobile.length() != 10) {
                phoneInput.setError("Enter valid 10-digit mobile number");
                phoneInput.requestFocus();
                return;
            }
            if (pin.isEmpty() || pin.length() != 4) {
                pinInput.setError("Enter 4-digit PIN");
                pinInput.requestFocus();
                return;
            }

            // Read from Firebase under mobile / info / pin
            usersRef.child(mobile).child("info").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String storedPin = snapshot.child("pin").getValue(String.class);
                        Boolean status = snapshot.child("status").getValue(Boolean.class);

                        if (storedPin != null && storedPin.equals(pin)) {

                            if (status != null && status) {
                                // ✅ STATUS = TRUE → LOGIN ALLOWED

                                SharedPreferences prefs = getSharedPreferences("APP_PREFS", MODE_PRIVATE);
                                prefs.edit()
                                        .putBoolean("IS_LOGGED_IN", true)
                                        .putString("USER_MOBILE", mobile)
                                        .apply();

                                Toast.makeText(LoginActivity.this,
                                        "Login successful ✔️", Toast.LENGTH_SHORT).show();

                                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                finish();

                            } else {
                                // ❌ STATUS = FALSE → FORCE LOGOUT
                                SharedPreferences prefs = getSharedPreferences("APP_PREFS", MODE_PRIVATE);
                                prefs.edit().clear().apply();

                                Toast.makeText(LoginActivity.this,
                                        "Your account is not activated yet.\nPlease contact admin.",
                                        Toast.LENGTH_LONG).show();

                            }

                        } else {
                            Toast.makeText(LoginActivity.this,
                                    "Incorrect PIN", Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        Toast.makeText(LoginActivity.this, "User not found, please register first", Toast.LENGTH_LONG).show();
                        startActivity(new Intent(LoginActivity.this, RegistrationActivity.class));
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Toast.makeText(LoginActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        // Link to open registration screen
        registrationLink.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegistrationActivity.class));
        });
    }
}
