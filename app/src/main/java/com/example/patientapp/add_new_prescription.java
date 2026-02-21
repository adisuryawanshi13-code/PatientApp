package com.example.patientapp;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class add_new_prescription extends DialogFragment {

    private DatabaseReference databaseReference;
    private FirebaseAuth auth;

    private String patientId;
    private String folderId;

    public add_new_prescription() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setStyle(DialogFragment.STYLE_NORMAL,
                android.R.style.Theme_Black_NoTitleBar_Fullscreen);

        if (getArguments() != null) {
            patientId = getArguments().getString("patientId");
            folderId = getArguments().getString("folderId");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        if (dialog.getWindow() != null) {
            dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }

        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();

        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            );
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(
                R.layout.fragment_add_new_prescription,
                container,
                false
        );

        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        TextInputEditText medicineEt =
                view.findViewById(R.id.medicineNameEt);

        TextInputEditText dosageEt =
                view.findViewById(R.id.dosageEt);

        TextInputEditText durationEt =
                view.findViewById(R.id.durationEt);

        view.findViewById(R.id.saveBtn).setOnClickListener(v -> {

            String doctorId = auth.getCurrentUser().getUid();

            String medicineName = medicineEt.getText().toString().trim();
            String dosage = dosageEt.getText().toString().trim();
            String duration = durationEt.getText().toString().trim();

            if (medicineName.isEmpty() ||
                    dosage.isEmpty() ||
                    duration.isEmpty()) {

                Toast.makeText(getContext(),
                        "Please fill all fields",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            String prescriptionId = databaseReference
                    .child("prescriptions")
                    .push()
                    .getKey();

            Map<String, Object> medicine = new HashMap<>();
            medicine.put("name", medicineName);
            medicine.put("form", "Tablet");
            medicine.put("dosageMg", Integer.parseInt(dosage));
            medicine.put("timing", "Morning");
            medicine.put("durationDays", Integer.parseInt(duration));
            medicine.put("instructions", "After food");

            ArrayList<Map<String, Object>> medicinesList =
                    new ArrayList<>();
            medicinesList.add(medicine);

            Map<String, Object> prescriptionMap =
                    new HashMap<>();
            prescriptionMap.put("patientId", patientId);
            prescriptionMap.put("doctorId", doctorId);
            prescriptionMap.put("folderId", folderId);
            prescriptionMap.put("medicines", medicinesList);
            prescriptionMap.put("immutable", true);
            prescriptionMap.put("createdAt",
                    ServerValue.TIMESTAMP);

            databaseReference
                    .child("prescriptions")
                    .child(patientId)
                    .child(prescriptionId)
                    .setValue(prescriptionMap)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(getContext(),
                                "Prescription Saved",
                                Toast.LENGTH_SHORT).show();
                        dismiss();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(getContext(),
                                    "Failed to save",
                                    Toast.LENGTH_SHORT).show());

        });

        return view;
    }
}