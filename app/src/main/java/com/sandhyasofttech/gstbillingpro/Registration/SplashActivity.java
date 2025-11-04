package com.sandhyasofttech.gstbillingpro.Registration;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.airbnb.lottie.LottieAnimationView;
import com.sandhyasofttech.gstbillingpro.MainActivity;
import com.sandhyasofttech.gstbillingpro.R;

public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DURATION = 3200L; // Total splash time

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        LottieAnimationView lottie = findViewById(R.id.lottieAnimation);
        TextView appName = findViewById(R.id.appName);
        TextView tagline = findViewById(R.id.tagline);

        // Start text fade-in after animation starts
        lottie.addAnimatorListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                fadeInText(appName, 0);
                fadeInText(tagline, 300);
            }
        });

        // Navigate after full duration
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            boolean isLoggedIn = getSharedPreferences("APP_PREFS", MODE_PRIVATE)
                    .getBoolean("IS_LOGGED_IN", false);

            Intent intent = isLoggedIn
                    ? new Intent(this, MainActivity.class)
                    : new Intent(this, LoginActivity.class);

            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }, SPLASH_DURATION);
    }

    private void fadeInText(TextView view, long delay) {
        view.animate()
                .alpha(1f)
                .setStartDelay(delay)
                .setDuration(800)
                .setListener(null);
    }
}