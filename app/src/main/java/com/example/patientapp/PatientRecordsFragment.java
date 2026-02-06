package com.example.patientapp;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class PatientRecordsFragment extends Fragment {

    private TextView tvName, tvVitals;

    public PatientRecordsFragment() {}

        @Nullable
        @Override
        public View onCreateView(
                @NonNull LayoutInflater inflater,
                @Nullable ViewGroup container,
                @Nullable Bundle savedInstanceState
    ) {

            View view = inflater.inflate(
                    R.layout.fragment_patient_records,
                    container,
                    false
            );

            // Bind views
            tvName = view.findViewById(R.id.tvName);
            tvVitals = view.findViewById(R.id.tvVitals);

            // Get UID
            if (getArguments() != null) {
                String patientUid = getArguments().getString("PATIENT_UID");
                fetchPatientDetails(patientUid);
            }

            return view;
        }

private void fetchPatientDetails(String patientUid) {

    DatabaseReference ref = FirebaseDatabase.getInstance()
            .getReference("users")
            .child(patientUid);

    ref.addListenerForSingleValueEvent(new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {

            if (!snapshot.exists()) {
                tvName.setText("Patient not found");
                tvVitals.setText("-");
                return;
            }

            String name = snapshot.child("fullName").getValue(String.class);
            String gender = snapshot.child("gender").getValue(String.class);
            String bloodGroup = snapshot.child("bloodGroup").getValue(String.class);
            Long height = snapshot.child("heightCm").getValue(Long.class);

            // Name
            tvName.setText(name != null ? name : "-");

            // Build vitals string safely
            StringBuilder vitalsBuilder = new StringBuilder();

            if (gender != null && !gender.isEmpty()) {
                vitalsBuilder.append(gender);
            }

            if (height != null) {
                if (vitalsBuilder.length() > 0) vitalsBuilder.append(" • ");
                vitalsBuilder.append(height).append("cm");
            }

            if (bloodGroup != null && !bloodGroup.isEmpty()) {
                if (vitalsBuilder.length() > 0) vitalsBuilder.append(" • ");
                vitalsBuilder.append(bloodGroup);
            }

            tvVitals.setText(
                    vitalsBuilder.length() > 0 ? vitalsBuilder.toString() : "-"
            );
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            tvName.setText("Error loading data");
            tvVitals.setText("-");
        }
    });
}
}
