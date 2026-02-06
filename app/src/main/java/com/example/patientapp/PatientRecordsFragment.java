package com.example.patientapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class PatientRecordsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_patient_records, container, false);

        // Back button to home
        view.findViewById(R.id.btn_back_records).setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().findViewById(R.id.doctor_fragment_container).setVisibility(View.GONE);
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        // "Add Vitals" button click logic - Opens the Camp Activity screen
        view.findViewById(R.id.btn_add_vitals).setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.doctor_fragment_container, new AddVitalsFragment())
                        .addToBackStack(null)
                        .commit();
            }
        });

        return view;
    }
}