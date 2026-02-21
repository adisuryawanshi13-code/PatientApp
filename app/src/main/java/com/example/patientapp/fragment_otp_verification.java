package com.example.patientapp;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class fragment_otp_verification extends Fragment {

    private String patientUid;
    private EditText otp1, otp2, otp3, otp4;

    public fragment_otp_verification() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_otp_verification, container, false);

        if (getArguments() != null) {
            patientUid = getArguments().getString("PATIENT_UID");
        }

        otp1 = view.findViewById(R.id.otp1);
        otp2 = view.findViewById(R.id.otp2);
        otp3 = view.findViewById(R.id.otp3);
        otp4 = view.findViewById(R.id.otp4);

        setupOtpInputs();

        view.findViewById(R.id.btnSubmit).setOnClickListener(v -> {
            // Verify OTP logic here (Static check for demo: 1234)
            String enteredOtp = otp1.getText().toString() + otp2.getText().toString() +
                    otp3.getText().toString() + otp4.getText().toString();

            if (enteredOtp.length() == 4) {
                openPatientRecords();
            } else {
                Toast.makeText(requireContext(), "Enter complete OTP", Toast.LENGTH_SHORT).show();
            }
        });

        view.findViewById(R.id.btnBack).setOnClickListener(v -> getParentFragmentManager().popBackStack());

        return view;
    }

    private void setupOtpInputs() {
        // Simple logic to move focus to next box automatically
        otp1.addTextChangedListener(new GenericTextWatcher(otp1, otp2));
        otp2.addTextChangedListener(new GenericTextWatcher(otp2, otp3));
        otp3.addTextChangedListener(new GenericTextWatcher(otp3, otp4));
        otp4.addTextChangedListener(new GenericTextWatcher(otp4, null));
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

    private class GenericTextWatcher implements TextWatcher {
        private final View currentView;
        private final View nextView;

        public GenericTextWatcher(View currentView, View nextView) {
            this.currentView = currentView;
            this.nextView = nextView;
        }

        @Override public void afterTextChanged(Editable s) {
            if (s.length() == 1 && nextView != null) nextView.requestFocus();
        }
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
    }
}