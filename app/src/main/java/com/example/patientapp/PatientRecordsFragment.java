package com.example.patientapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class PatientRecordsFragment extends Fragment {

    private TextView tvHeartRate, tvBp, tvSpo2, tvTemp;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_patient_records, container, false);

        // --- NEW LOGIC: Hide the Activity's Top Bar (Doctor Name bar) ---
        if (getActivity() != null) {
            View activityTopBar = getActivity().findViewById(R.id.top_bar);
            if (activityTopBar != null) {
                activityTopBar.setVisibility(View.GONE);
            }
        }

        // Initialize TextViews for vitals values
        tvHeartRate = view.findViewById(R.id.tv_heart_rate_value);
        tvBp = view.findViewById(R.id.tv_bp_value);
        tvSpo2 = view.findViewById(R.id.tv_spo2_value);
        tvTemp = view.findViewById(R.id.tv_temp_value);

        // Back button logic
        view.findViewById(R.id.btn_back_records).setOnClickListener(v -> {
            if (getActivity() != null) {
                // Restore the Activity's Top Bar before going back
                View activityTopBar = getActivity().findViewById(R.id.top_bar);
                if (activityTopBar != null) {
                    activityTopBar.setVisibility(View.VISIBLE);
                }

                getActivity().findViewById(R.id.doctor_fragment_container).setVisibility(View.GONE);
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        // "Add Vitals" button click logic
        view.findViewById(R.id.btn_add_vitals).setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.doctor_fragment_container, new AddVitalsFragment())
                        .addToBackStack(null)
                        .commit();
            }
        });

        // Fragment Result Listener for all 4 vitals (including Temperature)
        getParentFragmentManager().setFragmentResultListener("vitalsKey", this, (requestKey, bundle) -> {
            String bp = bundle.getString("bp");
            String pulse = bundle.getString("pulse");
            String spo2 = bundle.getString("spo2");
            String temp = bundle.getString("temp");

            // Update the UI with the new values
            if (pulse != null && !pulse.isEmpty() && tvHeartRate != null) {
                tvHeartRate.setText(pulse + " bpm");
            }
            if (bp != null && !bp.isEmpty() && tvBp != null) {
                tvBp.setText(bp + " mmHg");
            }
            if (spo2 != null && !spo2.isEmpty() && tvSpo2 != null) {
                tvSpo2.setText(spo2 + "%");
            }
            if (temp != null && !temp.isEmpty() && tvTemp != null) {
                tvTemp.setText(temp + "°C");
            }
        });

        return view;
    }
}