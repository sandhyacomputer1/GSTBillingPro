package com.sandhyasofttech.gstbillingpro.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.sandhyasofttech.gstbillingpro.R;
import com.sandhyasofttech.gstbillingpro.custmore.Customer;

import java.util.List;

public class CustomerAdapter extends RecyclerView.Adapter<CustomerAdapter.ViewHolder> {

    private List<Customer> customers;
    private OnCustomerClickListener listener;

    public interface OnCustomerClickListener {
        void onCustomerClick(Customer customer);
    }

    public CustomerAdapter(List<Customer> customers, OnCustomerClickListener listener) {
        this.customers = customers;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_customer, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Customer customer = customers.get(position);
        holder.bind(customer);
    }

    @Override
    public int getItemCount() {
        return customers.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardView;
        TextView tvName, tvPhone, tvAddress;

        ViewHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardCustomer);
            tvName = itemView.findViewById(R.id.tvCustomerName);
            tvPhone = itemView.findViewById(R.id.tvCustomerPhone);
            tvAddress = itemView.findViewById(R.id.tvCustomerAddress);
        }

        void bind(Customer customer) {
            tvName.setText(customer.name);
            tvPhone.setText(customer.phone);
            tvAddress.setText(customer.address != null && !customer.address.isEmpty() 
                ? customer.address : "No address provided");

            cardView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCustomerClick(customer);
                }
            });
        }
    }
}