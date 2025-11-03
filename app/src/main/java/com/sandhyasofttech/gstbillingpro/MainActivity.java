package com.sandhyasofttech.gstbillingpro;

import android.os.Bundle;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.sandhyasofttech.gstbillingpro.Fragment.CustomerFragment;
import com.sandhyasofttech.gstbillingpro.Fragment.HomeFragment;
import com.sandhyasofttech.gstbillingpro.Fragment.InvoiceBillingFragment;
import com.sandhyasofttech.gstbillingpro.Fragment.ProductFragment;
import com.sandhyasofttech.gstbillingpro.Fragment.SettingsFragment;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private boolean isNavigationUpdating = false; // Prevent recursion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(item -> {
            if (isNavigationUpdating) return false;

            boolean handled = handleNavigationSelection(item.getItemId());
            if (handled) {
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
            return false;
        });

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(item -> {
            if (isNavigationUpdating) return false;
            return handleNavigationSelection(item.getItemId());
        });

        if (savedInstanceState == null) {
            loadFragment(new HomeFragment(), false);
            syncNavigationSelection(R.id.nav_home);
        }
    }

    private boolean handleNavigationSelection(int itemId) {
        Fragment selectedFragment;

        if (itemId == R.id.nav_home) {
            selectedFragment = new HomeFragment();
        } else if (itemId == R.id.nav_invoice) {
            selectedFragment = new InvoiceBillingFragment();
        } else if (itemId == R.id.nav_customer) {
            selectedFragment = new CustomerFragment();
        } else if (itemId == R.id.nav_settings) {
            selectedFragment = new SettingsFragment();
        } else if (itemId == R.id.nav_product) {
            selectedFragment = new ProductFragment();
        } else {
            return false;
        }

        loadFragment(selectedFragment, true);
        syncNavigationSelection(itemId);
        return true;
    }

    /** ✅ Made public so fragments can call this */
    public void syncNavigationSelection(int itemId) {
        isNavigationUpdating = true;

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        NavigationView navigationView = findViewById(R.id.navigation_view);

        if (bottomNav.getSelectedItemId() != itemId) {
            bottomNav.setSelectedItemId(itemId);
        }
        if (navigationView.getCheckedItem() == null
                || navigationView.getCheckedItem().getItemId() != itemId) {
            navigationView.setCheckedItem(itemId);
        }
        isNavigationUpdating = false;
    }

    private void loadFragment(Fragment fragment, boolean addToBackStack) {
        var transaction = getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(
                        android.R.anim.fade_in,
                        android.R.anim.fade_out,
                        android.R.anim.fade_in,
                        android.R.anim.fade_out)
                .replace(R.id.fragment_container, fragment);

        if (addToBackStack) transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }
}
