package com.example.patientapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class AddVitalsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_vitals, container, false);

        // Back button logic to return to Patient Records
        view.findViewById(R.id.btn_back_camp).setOnClickListener(v -> {
            if (getParentFragmentManager() != null) {
                getParentFragmentManager().popBackStack();
            }
        });

        // Save Record button logic
        view.findViewById(R.id.btn_save_record).setOnClickListener(v -> {
            Toast.makeText(getContext(), "Patient Record Saved Successfully!", Toast.LENGTH_SHORT).show();
            if (getParentFragmentManager() != null) {
                getParentFragmentManager().popBackStack();
            }
        });

        return view;
    }
}