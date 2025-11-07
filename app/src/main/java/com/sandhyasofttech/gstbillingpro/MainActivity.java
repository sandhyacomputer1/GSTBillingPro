package com.sandhyasofttech.gstbillingpro;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.IdRes;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.appbar.MaterialToolbar;
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
    private int previousFragmentId = R.id.nav_home; // Track previous

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Title map
        titleMap.put(R.id.nav_home, "Home");
        titleMap.put(R.id.nav_invoice, "Invoice");
        titleMap.put(R.id.nav_customer, "Customer");
        titleMap.put(R.id.nav_product, "Product");
        titleMap.put(R.id.nav_settings, "Settings");
        titleMap.put(R.id.nav_soldproduct, "Sold Products");

        // Toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Drawer
        drawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navView = findViewById(R.id.navigation_view);
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        // FORCE LABELS TO SHOW (NO STYLES)
        bottomNav.setLabelVisibilityMode(BottomNavigationView.LABEL_VISIBILITY_LABELED);
        bottomNav.setItemIconTintList(null);

        // Drawer listener
        navView.setNavigationItemSelectedListener(item -> {
            if (isUpdating) return false;
            int id = item.getItemId();
            if (id == R.id.nav_logout) {
                showLogoutDialog();
                return true;
            }
            selectFragment(id);
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        // Bottom listener
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
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (d, w) -> {
                    getSharedPreferences("APP_PREFS", MODE_PRIVATE)
                            .edit()
                            .clear()
                            .apply();
                    startActivity(new Intent(this, LoginActivity.class));
                    finish();
                })
                .setNegativeButton("No", null)
                .setCancelable(true)
                .show();
    }

    // SLIDE ANIMATION + FRAGMENT SWITCH
    private void selectFragment(@IdRes int itemId) {
        if (itemId == R.id.nav_soldproduct) {
            startActivity(new Intent(this, SoldProductsActivity.class));
            drawerLayout.closeDrawer(GravityCompat.START);
            return;
        }

        Fragment fragment = getFragment(itemId);
        if (fragment == null) return;

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();

        // Animation: Forward or Backward
        if (itemId != previousFragmentId) {
            if (isForwardNavigation(itemId)) {
                transaction.setCustomAnimations(
                        R.anim.slide_in_right,
                        R.anim.slide_out_left
                );
            } else {
                transaction.setCustomAnimations(
                        R.anim.slide_in_left,
                        R.anim.slide_out_right
                );
            }
        }

        transaction.replace(R.id.fragment_container, fragment);
        if (itemId != R.id.nav_home) {
            transaction.addToBackStack(null);
        }
        transaction.commit();

        fm.executePendingTransactions();
        syncNavigation(itemId);

        previousFragmentId = itemId;
    }

    private Fragment getFragment(int id) {
        if (id == R.id.nav_home) return new HomeFragment();
        else if (id == R.id.nav_invoice) return new InvoiceBillingFragment();
        else if (id == R.id.nav_customer) return new CustomerFragment();
        else if (id == R.id.nav_product) return new ProductFragment();
        else if (id == R.id.nav_settings) return new SettingsFragment();
        else return null;
    }

    // Check if moving forward in menu order
    private boolean isForwardNavigation(int newId) {
        return getMenuIndex(newId) > getMenuIndex(previousFragmentId);
    }

    private int getMenuIndex(int id) {
        if (id == R.id.nav_home) return 0;
        else if (id == R.id.nav_invoice) return 1;
        else if (id == R.id.nav_customer) return 2;
        else if (id == R.id.nav_product) return 3;
        else if (id == R.id.nav_settings) return 4;
        else return 0;
    }

    // SYNC NAVIGATION
    public void syncNavigation(int itemId) {
        if (isUpdating) return;
        isUpdating = true;

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        NavigationView navView = findViewById(R.id.navigation_view);

        bottomNav.post(() -> {
            if (isBottomNavItem(itemId)) {
                if (bottomNav.getSelectedItemId() != itemId) {
                    bottomNav.setSelectedItemId(itemId);
                }
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

    private boolean isBottomNavItem(int id) {
        return id == R.id.nav_home || id == R.id.nav_invoice ||
                id == R.id.nav_customer || id == R.id.nav_product ||
                id == R.id.nav_settings;
    }

    // BACK BUTTON WITH SLIDE ANIMATION
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();

            // Animate backward
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction transaction = fm.beginTransaction();
            transaction.setCustomAnimations(
                    R.anim.slide_in_left,
                    R.anim.slide_out_right
            );
            transaction.commit();

            int topId = getCurrentNavId();
            syncNavigation(topId);
        } else {
            super.onBackPressed();
        }
    }

    private int getCurrentNavId() {
        FragmentManager fm = getSupportFragmentManager();
        if (fm.getFragments().isEmpty()) return R.id.nav_home;

        Fragment top = fm.getFragments().get(fm.getFragments().size() - 1);
        String tag = top.getClass().getSimpleName();

        if ("HomeFragment".equals(tag)) return R.id.nav_home;
        else if ("InvoiceBillingFragment".equals(tag)) return R.id.nav_invoice;
        else if ("CustomerFragment".equals(tag)) return R.id.nav_customer;
        else if ("ProductFragment".equals(tag)) return R.id.nav_product;
        else if ("SettingsFragment".equals(tag)) return R.id.nav_settings;
        else return R.id.nav_home;
    }
}