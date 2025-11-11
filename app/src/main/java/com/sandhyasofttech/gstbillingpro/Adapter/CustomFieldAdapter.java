package com.sandhyasofttech.gstbillingpro.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sandhyasofttech.gstbillingpro.R;

import java.util.List;

public class CustomFieldAdapter extends RecyclerView.Adapter<CustomFieldAdapter.ViewHolder> {

    private List<String> customFields;
    private OnFieldInteractionListener onFieldInteractionListener;
    private int selectedPosition = -1;

    // Interface is now explicitly public and static
    public static interface OnFieldInteractionListener {
        void onDeleteClick(int position);
        void onPrimaryFieldSelected(String fieldName);
    }

    public CustomFieldAdapter(List<String> customFields, String primaryField, OnFieldInteractionListener onFieldInteractionListener) {
        this.customFields = customFields;
        this.onFieldInteractionListener = onFieldInteractionListener;
        if (primaryField != null) {
            this.selectedPosition = customFields.indexOf(primaryField);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_custom_field, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String fieldName = customFields.get(position);
        holder.tvFieldName.setText(fieldName);
        holder.rbIsPrimary.setChecked(position == selectedPosition);

        holder.rbIsPrimary.setOnClickListener(v -> {
            if (holder.getAdapterPosition() != selectedPosition) {
                int previousSelected = selectedPosition;
                selectedPosition = holder.getAdapterPosition();
                notifyItemChanged(previousSelected);
                notifyItemChanged(selectedPosition);
                onFieldInteractionListener.onPrimaryFieldSelected(customFields.get(selectedPosition));
            }
        });

        holder.ivDeleteField.setOnClickListener(v -> onFieldInteractionListener.onDeleteClick(holder.getAdapterPosition()));
    }

    @Override
    public int getItemCount() {
        return customFields.size();
    }

    // ViewHolder is already public and static, which is correct.
    public static class ViewHolder extends RecyclerView.ViewHolder {
        RadioButton rbIsPrimary;
        TextView tvFieldName;
        ImageView ivDeleteField;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            rbIsPrimary = itemView.findViewById(R.id.rbIsPrimary);
            tvFieldName = itemView.findViewById(R.id.tvFieldName);
            ivDeleteField = itemView.findViewById(R.id.ivDeleteField);
        }
    }
}
