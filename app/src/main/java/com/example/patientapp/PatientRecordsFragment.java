package com.example.patientapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class PatientRecordsFragment extends Fragment {

    public PatientRecordsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the professional records layout
        View view = inflater.inflate(R.layout.fragment_patient_records, container, false);

        // Handle the Back Button click
        view.findViewById(R.id.btn_back_records).setOnClickListener(v -> {
            if (getActivity() != null) {
                // Hide the container and return to the main home screen
                getActivity().findViewById(R.id.doctor_fragment_container).setVisibility(View.GONE);
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        return view;
    }
}