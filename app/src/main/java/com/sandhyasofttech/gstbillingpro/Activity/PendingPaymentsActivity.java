package com.sandhyasofttech.gstbillingpro.Activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.sandhyasofttech.gstbillingpro.Adapter.PaymentPagerAdapter;
import com.sandhyasofttech.gstbillingpro.R;

public class PendingPaymentsActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private PaymentPagerAdapter pagerAdapter;
    private String userMobile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pending_payments);

        // Initialize views
        toolbar = findViewById(R.id.toolbar);
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);

        // Setup toolbar
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        // Get user mobile
        SharedPreferences prefs = getSharedPreferences("APP_PREFS", MODE_PRIVATE);
        userMobile = prefs.getString("USER_MOBILE", null);

        if (userMobile == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Setup ViewPager2 with TabLayout
        setupViewPager();
    }

    private void setupViewPager() {
        pagerAdapter = new PaymentPagerAdapter(this, userMobile);
        viewPager.setAdapter(pagerAdapter);

        // Connect TabLayout with ViewPager2
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    switch (position) {
                        case 0:
                            tab.setText("Pending");
                            tab.setIcon(R.drawable.ic_pending);
                            break;
                        case 1:
                            tab.setText("Completed");
                            tab.setIcon(R.drawable.ic_completed);
                            break;
                    }
                }).attach();

        // Set default tab
        viewPager.setCurrentItem(0, false);
    }

    public String getUserMobile() {
        return userMobile;
    }
}