package com.example.patientapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class PatientRecordsFragment extends Fragment {

    // Tabs
    private TextView tabMedicines, tabXRays, tabReports, tabDocuments;

    // Dropdown container
    private LinearLayout layoutRecordsDropdown;

    // Sections inside included layout
    private LinearLayout layoutMedicines, layoutXRays, layoutReports, layoutDocuments;

    public PatientRecordsFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {

        View rootView = inflater.inflate(
                R.layout.fragment_patient_records,
                container,
                false
        );

        // -----------------------------
        // Back Button
        // -----------------------------
        rootView.findViewById(R.id.btn_back_records).setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().findViewById(R.id.doctor_fragment_container)
                        .setVisibility(View.GONE);
                getActivity().getSupportFragmentManager()
                        .popBackStack();
            }
        });

        // -----------------------------
        // Tabs
        // -----------------------------
        tabMedicines = rootView.findViewById(R.id.tabMedicines);
        tabXRays = rootView.findViewById(R.id.tabXRays);
        tabReports = rootView.findViewById(R.id.tabReports);
        tabDocuments = rootView.findViewById(R.id.tabDocuments);

        // -----------------------------
        // Dropdown container
        // -----------------------------
        layoutRecordsDropdown = rootView.findViewById(R.id.layoutRecordsDropdown);

        // -----------------------------
        // Included layout views
        // -----------------------------
        View includedView = rootView.findViewById(R.id.includeRecords);

        layoutMedicines = includedView.findViewById(R.id.layoutMedicines);
        layoutXRays = includedView.findViewById(R.id.layoutXRays);
        layoutReports = includedView.findViewById(R.id.layoutReports);
        layoutDocuments = includedView.findViewById(R.id.layoutDocuments);

        // Initial state
        hideAllSections();
        layoutRecordsDropdown.setVisibility(View.GONE);

        // -----------------------------
        // Tab clicks
        // -----------------------------
        tabMedicines.setOnClickListener(v -> showSection(layoutMedicines));
        tabXRays.setOnClickListener(v -> showSection(layoutXRays));
        tabReports.setOnClickListener(v -> showSection(layoutReports));
        tabDocuments.setOnClickListener(v -> showSection(layoutDocuments));

        return rootView;
    }

    // -----------------------------
    // Helpers
    // -----------------------------
    private void showSection(LinearLayout section) {
        layoutRecordsDropdown.setVisibility(View.VISIBLE);
        hideAllSections();
        section.setVisibility(View.VISIBLE);
    }

    private void hideAllSections() {
        layoutMedicines.setVisibility(View.GONE);
        layoutXRays.setVisibility(View.GONE);
        layoutReports.setVisibility(View.GONE);
        layoutDocuments.setVisibility(View.GONE);
    }
}
