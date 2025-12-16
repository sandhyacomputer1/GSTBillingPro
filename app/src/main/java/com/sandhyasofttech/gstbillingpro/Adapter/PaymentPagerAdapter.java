package com.sandhyasofttech.gstbillingpro.Adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.sandhyasofttech.gstbillingpro.Fragment.CompletedPaymentsFragment;
import com.sandhyasofttech.gstbillingpro.Fragment.PendingPaymentsFragment;

public class PaymentPagerAdapter extends FragmentStateAdapter {

    private String userMobile;

    public PaymentPagerAdapter(@NonNull FragmentActivity fragmentActivity, String userMobile) {
        super(fragmentActivity);
        this.userMobile = userMobile;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return PendingPaymentsFragment.newInstance(userMobile);
            case 1:
                return CompletedPaymentsFragment.newInstance(userMobile);
            default:
                return PendingPaymentsFragment.newInstance(userMobile);
        }
    }

    @Override
    public int getItemCount() {
        return 2; // Pending and Completed tabs
    }
}