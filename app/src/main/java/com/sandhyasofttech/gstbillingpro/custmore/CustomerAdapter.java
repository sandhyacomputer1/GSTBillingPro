package com.sandhyasofttech.gstbillingpro.custmore;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sandhyasofttech.gstbillingpro.R;

import java.util.ArrayList;
import java.util.List;

public class CustomerAdapter extends RecyclerView.Adapter<CustomerAdapter.CustomerViewHolder> implements Filterable {

    public interface OnCustomerClickListener {
        void onEditClicked(Customer customer);
        void onDeleteClicked(Customer customer);
    }

    private final Context context;
    private List<Customer> customerList;
    private final List<Customer> customerListFull; // full list for filtering
    private final OnCustomerClickListener listener;

    public CustomerAdapter(Context context, List<Customer> customerList, OnCustomerClickListener listener) {
        this.context = context;
        this.customerList = customerList;
        this.listener = listener;
        customerListFull = new ArrayList<>(customerList);
    }

    /**
     * Updates both the filtered and full customer lists and refreshes adapter.
     */
    public void updateData(List<Customer> newList) {
        customerList.clear();
        customerList.addAll(newList);
        customerListFull.clear();
        customerListFull.addAll(newList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CustomerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_customer, parent, false);
        return new CustomerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomerViewHolder holder, int position) {
        Customer c = customerList.get(position);
        holder.tvName.setText(c.name);
        holder.tvPhone.setText(c.phone);
        holder.tvEmail.setText(c.email);
        holder.tvGstin.setText(c.gstin);
        holder.tvAddress.setText(c.address);

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditClicked(c);
            }
        });
        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClicked(c);
            }
        });
    }

    @Override
    public int getItemCount() {
        return customerList.size();
    }

    @Override
    public Filter getFilter() {
        return customerFilter;
    }

    private final Filter customerFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Customer> filteredList = new ArrayList<>();
            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(customerListFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();
                for (Customer item : customerListFull) {
                    if (item.name.toLowerCase().contains(filterPattern) ||
                            item.phone.contains(filterPattern)) {
                        filteredList.add(item);
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            customerList.clear();
            //noinspection unchecked
            customerList.addAll((List<Customer>) results.values);
            notifyDataSetChanged();
        }
    };

    static class CustomerViewHolder extends RecyclerView.ViewHolder {
        final TextView tvName, tvPhone, tvEmail, tvGstin, tvAddress;
        final ImageButton btnEdit, btnDelete;

        public CustomerViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvCustomerName);
            tvPhone = itemView.findViewById(R.id.tvCustomerPhone);
            tvEmail = itemView.findViewById(R.id.tvCustomerEmail);
            tvGstin = itemView.findViewById(R.id.tvCustomerGstin);
            tvAddress = itemView.findViewById(R.id.tvCustomerAddress);
            btnEdit = itemView.findViewById(R.id.btnEditCustomer);
            btnDelete = itemView.findViewById(R.id.btnDeleteCustomer);
        }
    }
}
