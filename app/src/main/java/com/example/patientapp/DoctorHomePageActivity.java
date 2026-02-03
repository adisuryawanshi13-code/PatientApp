package com.example.patientapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class DoctorHomePageActivity extends AppCompatActivity {
    Button btnScanQr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_doctor_home_page);
        btnScanQr = findViewById(R.id.btnScanQr);

        btnScanQr.setOnClickListener(v -> {
            IntentIntegrator integrator = new IntentIntegrator(this);
            integrator.setPrompt("Scan Patient QR");
            integrator.setBeepEnabled(true);
            integrator.setOrientationLocked(true);
            integrator.initiateScan();
        });

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

        Bundle bundle = new Bundle();
        bundle.putString("PATIENT_UID", patientUid);

        PatientDetailsFragment fragment = new PatientDetailsFragment();
        fragment.setArguments(bundle);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.doctor_fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }


}