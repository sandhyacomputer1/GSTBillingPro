package com.sandhyasofttech.gstbillingpro.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sandhyasofttech.gstbillingpro.R;

import java.util.List;
import java.util.Map;

public class FieldMappingAdapter extends RecyclerView.Adapter<FieldMappingAdapter.ViewHolder> {

    private final Context context;
    private final List<String> appFields;
    private final List<String> fileColumns;
    private final Map<String, Integer> currentMapping;

    public FieldMappingAdapter(Context context, List<String> appFields, List<String> fileColumns, Map<String, Integer> currentMapping) {
        this.context = context;
        this.appFields = appFields;
        this.fileColumns = fileColumns;
        this.currentMapping = currentMapping;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_field_mapping, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String appField = appFields.get(position);
        holder.tvAppField.setText(appField);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, fileColumns);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        holder.spinnerFileColumn.setAdapter(adapter);

        // Set the current selection
        if (currentMapping.containsKey(appField)) {
            holder.spinnerFileColumn.setSelection(currentMapping.get(appField));
        }
    }

    @Override
    public int getItemCount() {
        return appFields.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvAppField;
        Spinner spinnerFileColumn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAppField = itemView.findViewById(R.id.tvAppField);
            spinnerFileColumn = itemView.findViewById(R.id.spinnerFileColumn);
        }
    }
}
