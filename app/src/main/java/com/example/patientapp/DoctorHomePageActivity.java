package com.example.patientapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class DoctorHomePageActivity extends AppCompatActivity {
    Button btnScanQr;
    CardView cardRecords; // Added for the Records Quick Action

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_doctor_home_page);

        btnScanQr = findViewById(R.id.btnScanQr);
        cardRecords = findViewById(R.id.card_patient_records); // Link to XML ID

        // Existing QR Scan Logic
        btnScanQr.setOnClickListener(v -> {
            IntentIntegrator integrator = new IntentIntegrator(this);
            integrator.setPrompt("Scan Patient QR");
            integrator.setBeepEnabled(true);
            integrator.setOrientationLocked(true);
            integrator.initiateScan();
        });

        // New Logic: Open Records Fragment when card is clicked
        cardRecords.setOnClickListener(v -> {
            openRecordsFragment();
        });

    }

    // Helper method to open the new Patient Records page
    private void openRecordsFragment() {
        // Show the container so it covers the home screen
        findViewById(R.id.doctor_fragment_container).setVisibility(View.VISIBLE);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.doctor_fragment_container, new PatientRecordsFragment())
                .addToBackStack(null)
                .commit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null && result.getContents() != null) {

            String scannedText = result.getContents();

            if (scannedText.startsWith("patient_uid:")) {
                String patientUid = scannedText.replace("patient_uid:", "");
                openPatientDetailsFragment(patientUid);
            } else {
                Toast.makeText(this, "Invalid QR Code", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openPatientDetailsFragment(String patientUid) {
        // Show container for fragment
        findViewById(R.id.doctor_fragment_container).setVisibility(View.VISIBLE);

        Bundle bundle = new Bundle();
        bundle.putString("PATIENT_UID", patientUid);

        PatientRecordsFragment fragment = new PatientRecordsFragment();
        fragment.setArguments(bundle);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.doctor_fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }
}