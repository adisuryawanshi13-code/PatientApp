package com.example.patientapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class DoctorHomePageFragment extends Fragment {

    private Button btnScanQr;
    private CardView cardRecords;
    private TextView doctorName;
    private FirebaseAuth auth;
    private DatabaseReference userRef;
    private View rootView;

    public DoctorHomePageFragment() {
        // Required empty constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_doctor_home_page, container, false);

        btnScanQr = rootView.findViewById(R.id.btnScanQr);
        cardRecords = rootView.findViewById(R.id.card_patient_records);
        doctorName = rootView.findViewById(R.id.doctorName);

        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        // Load Doctor Name from Firebase
        if (currentUser != null) {
            String uid = currentUser.getUid();
            userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String fullName = snapshot.child("fullName").getValue(String.class);
                        if (fullName != null) {
                            doctorName.setText("Dr. " + fullName);
                        }
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    if (isAdded()) {
                        Toast.makeText(requireContext(), "Failed to load name", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        // QR Scan Button Click
        btnScanQr.setOnClickListener(v -> {
            IntentIntegrator integrator = IntentIntegrator.forSupportFragment(this);
            integrator.setPrompt("Scan Patient QR");
            integrator.setBeepEnabled(true);
            integrator.setOrientationLocked(true);
            integrator.initiateScan();
        });

        // Open Records Directly (Manual navigation)
        cardRecords.setOnClickListener(v -> openRecordsFragment());

        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if (result != null && result.getContents() != null) {
            String scannedText = result.getContents();

            // Extract Patient UID from QR string (Example: "patient_uid:XYZ123")
            if (scannedText.startsWith("patient_uid:")) {
                String patientUid = scannedText.replace("patient_uid:", "");
                // SUCCESS: Open the OTP Verification screen
                openOtpVerificationFragment(patientUid);
            } else {
                Toast.makeText(requireContext(), "Invalid Patient QR Code", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openOtpVerificationFragment(String patientUid) {
        // 1. Hide the Dashboard and show the container
        rootView.findViewById(R.id.top_bar).setVisibility(View.GONE);
        rootView.findViewById(R.id.dashboard_scroll).setVisibility(View.GONE);

        View container = rootView.findViewById(R.id.doctor_fragment_container);
        container.setVisibility(View.VISIBLE);

        // 2. Prepare data to pass to the OTP screen
        Bundle bundle = new Bundle();
        bundle.putString("PATIENT_UID", patientUid);

        // 3. Launch the OTP fragment
        fragment_otp_verification otpFragment = new fragment_otp_verification();
        otpFragment.setArguments(bundle);

        // Use childFragmentManager if doctor_fragment_container is inside this fragment's layout
        getChildFragmentManager().beginTransaction()
                .replace(R.id.doctor_fragment_container, otpFragment)
                .addToBackStack(null)
                .commit();
    }

    private void openRecordsFragment() {
        rootView.findViewById(R.id.top_bar).setVisibility(View.GONE);
        rootView.findViewById(R.id.dashboard_scroll).setVisibility(View.GONE);
        rootView.findViewById(R.id.doctor_fragment_container).setVisibility(View.VISIBLE);

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.doctor_fragment_container, new PatientRecordsFragment())
                .addToBackStack(null)
                .commit();
    }
}