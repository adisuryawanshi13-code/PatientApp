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

public class PatientDetailsFragment extends Fragment {

    private TextView tvName, tvGender, tvBloodGroup, tvHeight;

    public PatientDetailsFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {

        // 1️⃣ Inflate layout
        View view = inflater.inflate(
                R.layout.fragment_patient_details,
                container,
                false
        );

        // 2️⃣ Bind views
        tvName = view.findViewById(R.id.tvName);
        tvGender = view.findViewById(R.id.tvGender);
        tvBloodGroup = view.findViewById(R.id.tvBloodGroup);
        tvHeight = view.findViewById(R.id.tvHeight);

        // 3️⃣ Get patient UID from arguments
        if (getArguments() != null) {
            String patientUid = getArguments().getString("PATIENT_UID");
            fetchPatientDetails(patientUid);
        }

        return view;
    }

    // 🔥 BACKEND: Fetch patient data
    private void fetchPatientDetails(String patientUid) {

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(patientUid);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d("PATIENT_DEBUG", "UID path exists: " + snapshot.exists());
                Log.d("PATIENT_DEBUG", "Snapshot key: " + snapshot.getKey());

                if (!snapshot.exists()) {
                    tvName.setText("Patient not found");
                    return;
                }

                String name = snapshot.child("fullName").getValue(String.class);
                String gender = snapshot.child("gender").getValue(String.class);
                String bloodGroup = snapshot.child("bloodGroup").getValue(String.class);
                Long height = snapshot.child("heightCm").getValue(Long.class);

                tvName.setText(name != null ? name : "-");
                tvGender.setText(gender != null ? gender : "-");
                tvBloodGroup.setText(bloodGroup != null ? bloodGroup : "-");
                tvHeight.setText(height != null ? height + " cm" : "-");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                tvName.setText("Error loading data");
            }

        });
    }
}
