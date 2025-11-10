// com.sandhyasofttech.gstbillingpro.Dialog.ChangePasswordBottomSheet.java
package com.sandhyasofttech.gstbillingpro.Activity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.sandhyasofttech.gstbillingpro.R;

import java.util.HashMap;
import java.util.Map;

public class ChangePasswordBottomSheet extends BottomSheetDialogFragment {

    private DatabaseReference userRef;  // users/+919876543210
    private String userMobile;

    public static ChangePasswordBottomSheet newInstance(DatabaseReference userRef, String userMobile) {
        ChangePasswordBottomSheet fragment = new ChangePasswordBottomSheet();
        fragment.userRef = userRef;
        fragment.userMobile = userMobile;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_change_password, container, false);

        TextInputLayout inputCurrentPin = view.findViewById(R.id.inputCurrentPin);
        TextInputLayout inputNewPin = view.findViewById(R.id.inputNewPin);
        TextInputLayout inputConfirmPin = view.findViewById(R.id.inputConfirmPin);

        TextInputEditText etCurrentPin = view.findViewById(R.id.etCurrentPin);
        TextInputEditText etNewPin = view.findViewById(R.id.etNewPin);
        TextInputEditText etConfirmPin = view.findViewById(R.id.etConfirmPin);

        view.findViewById(R.id.btnCancel).setOnClickListener(v -> dismiss());
        view.findViewById(R.id.btnSave).setOnClickListener(v -> validateAndUpdate(
                etCurrentPin, etNewPin, etConfirmPin,
                inputCurrentPin, inputNewPin, inputConfirmPin
        ));

        // Real-time validation for 4-digit PIN
        etNewPin.addTextChangedListener(new PinValidator(etNewPin, inputNewPin, 4));
        etConfirmPin.addTextChangedListener(new PinValidator(etConfirmPin, inputConfirmPin, 4));

        return view;
    }

    private void validateAndUpdate(TextInputEditText etCurrent, TextInputEditText etNew, TextInputEditText etConfirm,
                                   TextInputLayout layoutCurrent, TextInputLayout layoutNew, TextInputLayout layoutConfirm) {

        String current = etCurrent.getText().toString().trim();
        String newPin = etNew.getText().toString().trim();
        String confirm = etConfirm.getText().toString().trim();

        // Reset errors
        layoutCurrent.setError(null);
        layoutNew.setError(null);
        layoutConfirm.setError(null);

        if (current.isEmpty()) {
            layoutCurrent.setError("Enter current PIN");
            return;
        }
        if (newPin.length() != 4) {
            layoutNew.setError("PIN must be 4 digits");
            return;
        }
        if (!newPin.equals(confirm)) {
            layoutConfirm.setError("PINs do not match");
            return;
        }

        // CORRECT PATH: info/pin
        DatabaseReference pinRef = userRef.child("info").child("pin");

        pinRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String storedPin = snapshot.getValue(String.class);

                if (storedPin == null || !storedPin.equals(current)) {
                    layoutCurrent.setError("Incorrect current PIN");
                    return;
                }

                // Update PIN in info/pin
                Map<String, Object> updates = new HashMap<>();
                updates.put("info/pin", newPin);

                userRef.updateChildren(updates).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(), "PIN updated successfully", Toast.LENGTH_SHORT).show();
                        dismiss();
                    } else {
                        Toast.makeText(getContext(), "Update failed", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Generic PIN validator (4-digit)
    private static class PinValidator implements TextWatcher {
        private final TextInputEditText editText;
        private final TextInputLayout layout;
        private final int requiredLength;

        PinValidator(TextInputEditText editText, TextInputLayout layout, int length) {
            this.editText = editText;
            this.layout = layout;
            this.requiredLength = length;
        }

        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(Editable s) {
            String text = s.toString().trim();
            if (text.length() > 0 && text.length() != requiredLength) {
                layout.setError("Enter " + requiredLength + " digits");
            } else {
                layout.setError(null);
            }
        }
    }
}