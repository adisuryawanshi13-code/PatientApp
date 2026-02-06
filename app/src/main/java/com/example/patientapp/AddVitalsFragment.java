package com.example.patientapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class AddVitalsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_vitals, container, false);

        // Link input fields
        EditText etBp = view.findViewById(R.id.et_bp);
        EditText etPulse = view.findViewById(R.id.et_pulse);
        EditText etSpo2 = view.findViewById(R.id.et_spo2);
        EditText etTemp = view.findViewById(R.id.et_temp); // Link the temperature input

        // Back button
        view.findViewById(R.id.btn_back_camp).setOnClickListener(v -> {
            getParentFragmentManager().popBackStack();
        });

        // Save Button Logic
        view.findViewById(R.id.btn_save_record).setOnClickListener(v -> {
            String bpValue = etBp.getText().toString();
            String pulseValue = etPulse.getText().toString();
            String spo2Value = etSpo2.getText().toString();
            String tempValue = etTemp.getText().toString();

            // Prepare data bundle
            Bundle result = new Bundle();
            result.putString("bp", bpValue);
            result.putString("pulse", pulseValue);
            result.putString("spo2", spo2Value);
            result.putString("temp", tempValue); // Put temperature in bundle

            // Send back to PatientRecordsFragment
            getParentFragmentManager().setFragmentResult("vitalsKey", result);

            Toast.makeText(getContext(), "Patient Record Saved Successfully!", Toast.LENGTH_SHORT).show();

            // Return to previous screen
            getParentFragmentManager().popBackStack();
        });

        return view;
    }
}