package com.example.patientapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Random;

public class fragment_otp_verification extends Fragment {

    private String patientUid;
    private String patientPhoneNumber = ""; // To store the fetched number
    private EditText otp1, otp2, otp3, otp4;
    private Button btnSendOtp, btnSubmit;
    private LinearLayout otpContainer;
    private View resendContainer;
    private TextView tvSubtitle;

    private DatabaseReference patientRef;
    private String generatedOtp = "";
    private static final int SMS_PERMISSION_CODE = 101;

    public fragment_otp_verification() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_otp_verification, container, false);

        // 1. Initialize Views and Firebase
        if (getArguments() != null) {
            patientUid = getArguments().getString("PATIENT_UID");
            patientRef = FirebaseDatabase.getInstance().getReference("users").child(patientUid);
        }

        tvSubtitle = view.findViewById(R.id.tvSubtitle);
        btnSendOtp = view.findViewById(R.id.btnSendOtp);
        btnSubmit = view.findViewById(R.id.btnSubmit);
        otpContainer = view.findViewById(R.id.otpContainer);
        resendContainer = view.findViewById(R.id.resend_container);

        otp1 = view.findViewById(R.id.otp1);
        otp2 = view.findViewById(R.id.otp2);
        otp3 = view.findViewById(R.id.otp3);
        otp4 = view.findViewById(R.id.otp4);

        setupOtpFocusForwarding();

        // 2. Fetch Patient Data (To get the phone number)
        fetchPatientData();

        // 3. Send OTP Logic
        btnSendOtp.setOnClickListener(v -> {
            checkPermissionAndSendOtp();
        });

        // 4. Verify OTP Logic
        btnSubmit.setOnClickListener(v -> {
            verifyOtp();
        });

        view.findViewById(R.id.btnBack).setOnClickListener(v -> getParentFragmentManager().popBackStack());

        return view;
    }

    private void fetchPatientData() {
        patientRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    patientPhoneNumber = snapshot.child("phone").getValue(String.class);
                    if (patientPhoneNumber != null && patientPhoneNumber.length() > 4) {
                        String maskedPhone = "******" + patientPhoneNumber.substring(patientPhoneNumber.length() - 4);
                        tvSubtitle.setText("The OTP will be sent to the patient's registered number: " + maskedPhone);
                    }
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void checkPermissionAndSendOtp() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED) {
            generateAndSendOtp();
        } else {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_CODE);
        }
    }

    private void generateAndSendOtp() {
        if (patientPhoneNumber == null || patientPhoneNumber.isEmpty()) {
            Toast.makeText(requireContext(), "Patient phone number not found!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Generate a 4-digit random OTP
        int randomOtp = new Random().nextInt(9000) + 1000;
        generatedOtp = String.valueOf(randomOtp);

        // A. Save OTP to Firebase for verification
        patientRef.child("currentOtp").setValue(generatedOtp).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // B. Send the actual SMS
                sendSmsMessage(patientPhoneNumber, "Your PatientApp verification code is: " + generatedOtp);

                Toast.makeText(requireContext(), "OTP sent to " + patientPhoneNumber, Toast.LENGTH_SHORT).show();

                // Show verification UI
                btnSendOtp.setVisibility(View.GONE);
                otpContainer.setVisibility(View.VISIBLE);
                btnSubmit.setVisibility(View.VISIBLE);
                resendContainer.setVisibility(View.VISIBLE);
                tvSubtitle.setText("Enter the 4-digit code sent to the patient.");
                otp1.requestFocus();
            } else {
                Toast.makeText(requireContext(), "Failed to generate OTP. Try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendSmsMessage(String phoneNumber, String message) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);
        } catch (Exception e) {
            Toast.makeText(requireContext(), "SMS failed to send. Check permissions.", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private void verifyOtp() {
        String enteredCode = otp1.getText().toString() + otp2.getText().toString() +
                otp3.getText().toString() + otp4.getText().toString();

        patientRef.child("currentOtp").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String dbOtp = snapshot.getValue(String.class);
                if (dbOtp != null && dbOtp.equals(enteredCode)) {
                    patientRef.child("currentOtp").removeValue();
                    openPatientRecords();
                } else {
                    Toast.makeText(requireContext(), "Incorrect OTP. Access Denied.", Toast.LENGTH_SHORT).show();
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void openPatientRecords() {
        Bundle bundle = new Bundle();
        bundle.putString("PATIENT_UID", patientUid);
        PatientRecordsFragment fragment = new PatientRecordsFragment();
        fragment.setArguments(bundle);

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.doctor_fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void setupOtpFocusForwarding() {
        otp1.addTextChangedListener(new OtpTextWatcher(otp1, otp2));
        otp2.addTextChangedListener(new OtpTextWatcher(otp2, otp3));
        otp3.addTextChangedListener(new OtpTextWatcher(otp3, otp4));
        otp4.addTextChangedListener(new OtpTextWatcher(otp4, null));
    }

    private class OtpTextWatcher implements TextWatcher {
        private final View currentView, nextView;
        public OtpTextWatcher(View currentView, View nextView) {
            this.currentView = currentView;
            this.nextView = nextView;
        }
        @Override public void afterTextChanged(Editable s) {
            if (s.length() == 1 && nextView != null) nextView.requestFocus();
        }
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == SMS_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                generateAndSendOtp();
            } else {
                Toast.makeText(requireContext(), "SMS Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}