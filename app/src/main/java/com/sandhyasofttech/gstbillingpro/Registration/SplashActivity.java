package com.sandhyasofttech.gstbillingpro.Registration;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.sandhyasofttech.gstbillingpro.MainActivity;


public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if user is already logged in (using SharedPreferences)
        boolean isLoggedIn = getSharedPreferences("APP_PREFS", MODE_PRIVATE)
                .getBoolean("IS_LOGGED_IN", false);

        new Handler().postDelayed(() -> {
            if (isLoggedIn) {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
            } else {
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            }
            finish();
        }, 2000); // 2 seconds splash delay
    }
}
