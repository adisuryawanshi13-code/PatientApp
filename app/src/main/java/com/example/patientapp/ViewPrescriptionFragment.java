package com.example.patientapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.*;

import java.util.List;

public class ViewPrescriptionFragment extends Fragment {

    private String prescriptionId;
    private String patientId;

    private TextView tvMedicationCount, tvCourseDuration;
    private View rootView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        rootView = inflater.inflate(
                R.layout.fragment_view_prescription,
                container,
                false
        );

        tvMedicationCount = rootView.findViewById(R.id.tvMedicationCount);
        tvCourseDuration = rootView.findViewById(R.id.tvCourseDuration);

        if (getArguments() != null) {
            prescriptionId = getArguments().getString("prescriptionId");
            patientId = getArguments().getString("patientId");
        }

        tvMedicationCount.setText("Loading...");
        loadPrescription();

        return rootView;
    }

    private void loadPrescription() {

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("prescriptions")
                .child(patientId)
                .child(prescriptionId);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (!snapshot.exists()) return;

                DataSnapshot medicinesSnap = snapshot.child("medicines");

                int count = 0;
                int maxDuration = 0;

                for (DataSnapshot medSnap : medicinesSnap.getChildren()) {

                    count++;

                    String name = medSnap.child("name").getValue(String.class);
                    Long dosage = medSnap.child("dosageMg").getValue(Long.class);
                    Long duration = medSnap.child("durationDays").getValue(Long.class);
                    String form = medSnap.child("form").getValue(String.class);
                    String timing = medSnap.child("timing").getValue(String.class);
                    String instructions = medSnap.child("instructions").getValue(String.class);

                    if (duration != null && duration > maxDuration) {
                        maxDuration = duration.intValue();
                    }

                    TextView tvName = rootView.findViewById(R.id.tvMedicineName);
                    TextView tvDosage = rootView.findViewById(R.id.tvDosage);
                    TextView tvMorning = rootView.findViewById(R.id.tvMorning);
                    TextView tvNoon = rootView.findViewById(R.id.tvNoon);
                    TextView tvNight = rootView.findViewById(R.id.tvNight);
                    TextView tvFood = rootView.findViewById(R.id.tvFoodInstruction);

                    tvName.setText(name);
                    tvDosage.setText(dosage + "mg • " + form);

                    if (timing != null) {
                        tvMorning.setText(timing.contains("Morning") ? "1 Pill" : "-");
                        tvNoon.setText(timing.contains("Noon") ? "1 Pill" : "-");
                        tvNight.setText(timing.contains("Night") ? "1 Pill" : "-");
                    }

                    if (instructions != null) {
                        tvFood.setText("Take " + instructions);
                    }

                    break;
                }

                tvMedicationCount.setText("Medications (" + count + ")");
                tvCourseDuration.setText(maxDuration + " Days Course");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }
}