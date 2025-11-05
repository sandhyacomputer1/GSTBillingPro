package com.sandhyasofttech.gstbillingpro;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.IdRes;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.sandhyasofttech.gstbillingpro.Fragment.CustomerFragment;
import com.sandhyasofttech.gstbillingpro.Fragment.HomeFragment;
import com.sandhyasofttech.gstbillingpro.Fragment.InvoiceBillingFragment;
import com.sandhyasofttech.gstbillingpro.Fragment.ProductFragment;
import com.sandhyasofttech.gstbillingpro.Fragment.SettingsFragment;
import com.sandhyasofttech.gstbillingpro.Registration.LoginActivity;
import com.sandhyasofttech.gstbillingpro.soldproduct.SoldProductsActivity;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private boolean isUpdating = false;
    private final Map<Integer, String> titleMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Title Map
        titleMap.put(R.id.nav_home, "Home");
        titleMap.put(R.id.nav_invoice, "Invoice");
        titleMap.put(R.id.nav_customer, "Customer");
        titleMap.put(R.id.nav_product, "Product");
        titleMap.put(R.id.nav_settings, "Settings");
        titleMap.put(R.id.nav_soldproduct, "Sold Products");
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navView = findViewById(R.id.navigation_view);
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        // Drawer Click
        navView.setNavigationItemSelectedListener(item -> {
            if (isUpdating) return false;
            int id = item.getItemId();


            // Handle Logout with Confirmation
            if (id == R.id.nav_logout) {
                showLogoutDialog();
                return true;
            }

            selectFragment(id);
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        // Bottom Nav Click
        bottomNav.setOnItemSelectedListener(item -> {
            if (isUpdating) return false;
            selectFragment(item.getItemId());
            return true;
        });

        if (savedInstanceState == null) {
            selectFragment(R.id.nav_home);
        }

    }
    private void showLogoutDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Clear session
                    getSharedPreferences("APP_PREFS", MODE_PRIVATE)
                            .edit()
                            .clear()
                            .apply();

                    // Go to Login
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    finish();
                })
                .setNegativeButton("No", null)
                .setCancelable(true)
                .show();
    }
    private void selectFragment(@IdRes int itemId) {

        if (itemId == R.id.nav_soldproduct) {
            // Launch SoldProductsActivity
            startActivity(new Intent(this, SoldProductsActivity.class));
            drawerLayout.closeDrawer(GravityCompat.START);
            return; // no fragment transaction
        }
        Fragment fragment = getFragment(itemId);
        if (fragment == null) return;



        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(itemId != R.id.nav_home ? null : null)
                .commit();

        // Wait for transaction to complete
        getSupportFragmentManager().executePendingTransactions();

        // NOW sync
        syncNavigation(itemId);
    }
    // Java 11 Compatible
    private Fragment getFragment(int id) {
        if (id == R.id.nav_home) {
            return new HomeFragment();
        } else if (id == R.id.nav_invoice) {
            return new InvoiceBillingFragment();
        } else if (id == R.id.nav_customer) {
            return new CustomerFragment();
        } else if (id == R.id.nav_product) {
            return new ProductFragment();
        } else if (id == R.id.nav_settings) {
            return new SettingsFragment();
        } else {
            return null;
        }
    }

    // PUBLIC METHOD – Fragments can call this
    public void syncNavigation(int itemId) {
        if (isUpdating) return;
        isUpdating = true;

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        NavigationView navView = findViewById(R.id.navigation_view);

        // Use post() to ensure UI is ready
        bottomNav.post(() -> {
            if (bottomNav.getSelectedItemId() != itemId) {
                bottomNav.setSelectedItemId(itemId);
            }

            if (navView.getCheckedItem() == null || navView.getCheckedItem().getItemId() != itemId) {
                navView.setCheckedItem(itemId);
            }

            String title = titleMap.get(itemId);
            if (title != null && getSupportActionBar() != null) {
                getSupportActionBar().setTitle(title);
            }

            isUpdating = false;
        });
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
            int topId = getCurrentNavId();
            syncNavigation(topId);
        } else {
            super.onBackPressed();
        }
    }

    // Java 11 Compatible
    private int getCurrentNavId() {
        FragmentManager fm = getSupportFragmentManager();
        if (fm.getFragments().isEmpty()) return R.id.nav_home;

        Fragment top = fm.getFragments().get(fm.getFragments().size() - 1);
        String tag = top.getClass().getSimpleName();

        if ("HomeFragment".equals(tag)) {
            return R.id.nav_home;
        } else if ("InvoiceBillingFragment".equals(tag)) {
            return R.id.nav_invoice;
        } else if ("CustomerFragment".equals(tag)) {
            return R.id.nav_customer;
        } else if ("ProductFragment".equals(tag)) {
            return R.id.nav_product;
        } else if ("SettingsFragment".equals(tag)) {
            return R.id.nav_settings;
        } else {
            return R.id.nav_home;
        }
    }
}