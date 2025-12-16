package com.sandhyasofttech.gstbillingpro.Registration;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sandhyasofttech.gstbillingpro.MainActivity;
import com.sandhyasofttech.gstbillingpro.R;

public class SplashActivity extends AppCompatActivity {

    private static final long POST_ANIMATION_DELAY = 1500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        final ImageView logo = findViewById(R.id.companyLogo);
        final TextView appName = findViewById(R.id.appName);
        final TextView tagline = findViewById(R.id.tagline);
        final TextView companyName = findViewById(R.id.companyName);

        // Animate logo scaling for cinematic effect
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(logo, "scaleX", 0.8f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(logo, "scaleY", 0.8f, 1f);
        scaleX.setDuration(2000);
        scaleY.setDuration(2000);
        scaleX.setInterpolator(new AccelerateDecelerateInterpolator());
        scaleY.setInterpolator(new AccelerateDecelerateInterpolator());

        scaleX.start();
        scaleY.start();

        // When logo animation finishes, animate texts sequentially
        scaleY.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                animateTextView(appName, 0);
                animateTextView(tagline, 400);
                animateTextView(companyName, 700);

                new Handler(Looper.getMainLooper()).postDelayed(SplashActivity.this::navigateNext, POST_ANIMATION_DELAY);
            }
        });
    }

    private void animateTextView(TextView textView, long delay) {
        textView.setAlpha(0f);
        textView.setTranslationY(30f);
        textView.animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay(delay)
                .setDuration(700)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();
    }

    private void navigateNext() {

        SharedPreferences prefs = getSharedPreferences("APP_PREFS", MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean("IS_LOGGED_IN", false);
        String mobile = prefs.getString("USER_MOBILE", null);

        if (!isLoggedIn || mobile == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // 🔥 VERIFY STATUS FROM FIREBASE AGAIN
        FirebaseDatabase.getInstance()
                .getReference("users")
                .child(mobile)
                .child("info")
                .child("status")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {

                        Boolean status = snapshot.getValue(Boolean.class);

                        if (status != null && status) {
                            // ✅ ALLOW
                            startActivity(new Intent(SplashActivity.this, MainActivity.class));
                        } else {
                            // ❌ BLOCK
                            prefs.edit().clear().apply();
                            Toast.makeText(SplashActivity.this,
                                    "Your account is inactive. Please contact admin.",
                                    Toast.LENGTH_LONG).show();
                            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                        }
                        finish();
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                        finish();
                    }
                });
    }

}
