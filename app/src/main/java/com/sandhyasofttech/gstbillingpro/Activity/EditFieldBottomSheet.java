package com.sandhyasofttech.gstbillingpro.Activity;

import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DatabaseReference;
import com.sandhyasofttech.gstbillingpro.R;

import java.util.HashMap;
import java.util.Map;

public class EditFieldBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_FIELD_NAME = "field_name";
    private static final String ARG_CURRENT_VALUE = "current_value";
    private static final String ARG_FIELD_KEY = "field_key";

    private String fieldName;
    private String currentValue;
    private String fieldKey;
    private DatabaseReference userInfoRef;

    private OnFieldUpdatedListener listener;

    public interface OnFieldUpdatedListener {
        void onUpdated(String newValue);
    }

    public static EditFieldBottomSheet newInstance(
            String fieldName,
            String currentValue,
            String fieldKey,
            DatabaseReference userInfoRef) {

        EditFieldBottomSheet fragment = new EditFieldBottomSheet();
        Bundle args = new Bundle();
        args.putString(ARG_FIELD_NAME, fieldName);
        args.putString(ARG_CURRENT_VALUE, currentValue);
        args.putString(ARG_FIELD_KEY, fieldKey);
        fragment.setArguments(args);
        fragment.userInfoRef = userInfoRef;
        return fragment;
    }

    public void setOnFieldUpdatedListener(OnFieldUpdatedListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_edit_field, container, false);

        // Get arguments
        if (getArguments() != null) {
            fieldName = getArguments().getString(ARG_FIELD_NAME);
            currentValue = getArguments().getString(ARG_CURRENT_VALUE);
            fieldKey = getArguments().getString(ARG_FIELD_KEY);
        }

        TextInputLayout inputLayout = view.findViewById(R.id.inputLayout);
        TextInputEditText etField = view.findViewById(R.id.etField);
        view.findViewById(R.id.btnCancel).setOnClickListener(v -> dismiss());
        view.findViewById(R.id.btnSave).setOnClickListener(v -> saveField(etField, inputLayout));

        // Set title and value
        ((TextView) view.findViewById(R.id.tvTitle)).setText("Edit " + fieldName);
        etField.setText(currentValue);
        etField.setSelection(currentValue.length());
        etField.setInputType(getInputType(fieldKey));
        etField.requestFocus();

        return view;
    }

    private int getInputType(String key) {
        switch (key) {
            case "mobile":
                return InputType.TYPE_CLASS_PHONE;
            case "email":
                return InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS;
            case "gstin":
                return InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS;
            case "address":
                return InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE;
            default:
                return InputType.TYPE_CLASS_TEXT;
        }
    }

    private void saveField(TextInputEditText etField, TextInputLayout inputLayout) {
        String newValue = etField.getText().toString().trim();

        if (newValue.isEmpty()) {
            inputLayout.setError("This field is required");
            return;
        }

        if (fieldKey.equals("gstin") && !isValidGSTIN(newValue)) {
            inputLayout.setError("Invalid GSTIN format");
            return;
        }

        if (fieldKey.equals("mobile") && newValue.length() != 10) {
            inputLayout.setError("Mobile must be 10 digits");
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put(fieldKey, newValue);

        userInfoRef.updateChildren(updates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(getContext(), fieldName + " updated", Toast.LENGTH_SHORT).show();
                if (listener != null) {
                    listener.onUpdated(newValue);
                }
                dismiss();
            } else {
                Toast.makeText(getContext(), "Update failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isValidGSTIN(String gstin) {
        return gstin.matches("^\\d{2}[A-Z]{5}\\d{4}[A-Z]{1}[A-Z\\d]{1}Z[A-Z\\d]{1}$");
    }
}