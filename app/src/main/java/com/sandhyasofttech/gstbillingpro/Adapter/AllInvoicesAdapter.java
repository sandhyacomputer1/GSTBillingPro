package com.sandhyasofttech.gstbillingpro.Adapter;

import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.sandhyasofttech.gstbillingpro.Model.RecentInvoiceItem;
import com.sandhyasofttech.gstbillingpro.R;

import java.util.ArrayList;

public class AllInvoicesAdapter extends RecyclerView.Adapter<AllInvoicesAdapter.ViewHolder> {

    private final ArrayList<RecentInvoiceItem> invoices;
    private final DatabaseReference userRef;

    // Interfaces for separate click events
    public interface OnItemClickListener {
        void onItemClick(String invoiceNumber);
    }
    public interface OnEditClickListener {
        void onEditClick(String invoiceNumber);
    }

    private OnItemClickListener itemClickListener;
    private OnEditClickListener editClickListener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
    }

    public void setOnEditClickListener(OnEditClickListener listener) {
        this.editClickListener = listener;
    }

    public AllInvoicesAdapter(ArrayList<RecentInvoiceItem> invoices, DatabaseReference userRef) {
        this.invoices = invoices;
        this.userRef = userRef;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_invoice_manage, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RecentInvoiceItem item = invoices.get(position);

        holder.tvInvoiceNo.setText(item.invoiceNo);
        holder.tvCustomerName.setText(item.customerName);
        holder.tvTotal.setText("₹" + String.format("%.2f", item.grandTotal));
        holder.tvDate.setText(item.date);

        holder.btnEdit.setOnClickListener(v -> {
            if (editClickListener != null && position != RecyclerView.NO_POSITION) {
                editClickListener.onEditClick(item.invoiceNo);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(v.getContext())
                    .setTitle("Delete Invoice")
                    .setMessage("Are you sure you want to delete " + item.invoiceNo + "?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        userRef.child("invoices").child(item.invoiceNo).removeValue()
                                .addOnSuccessListener(aVoid -> {
                                    invoices.remove(position);
                                    notifyItemRemoved(position);
                                    Toast.makeText(v.getContext(), "Deleted", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(v.getContext(), "Failed to delete", Toast.LENGTH_SHORT).show());
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return invoices.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvInvoiceNo, tvCustomerName, tvTotal, tvDate;
        ImageButton btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvInvoiceNo = itemView.findViewById(R.id.tvInvoiceNo);
            tvCustomerName = itemView.findViewById(R.id.tvCustomerName);
            tvTotal = itemView.findViewById(R.id.tvTotalAmount);
            tvDate = itemView.findViewById(R.id.tvDate);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (itemClickListener != null && position != RecyclerView.NO_POSITION) {
                    RecentInvoiceItem clickedInvoice = invoices.get(position);
                    itemClickListener.onItemClick(clickedInvoice.invoiceNo);
                }
            });
        }
    }
}
