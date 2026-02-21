package com.example.patientapp;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.common.InputImage;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class DoctorHomePageFragment extends Fragment {

    private static final int GALLERY_REQUEST_CODE = 200;

    private Button btnScanQr;
    private MaterialButton btnImportGallery;
    private TextView doctorName;

    private FirebaseAuth auth;
    private DatabaseReference userRef;
    private View rootView;

    public DoctorHomePageFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_doctor_home_page, container, false);

        btnScanQr = rootView.findViewById(R.id.btnScanQr);
        btnImportGallery = rootView.findViewById(R.id.btnImportGallery);
        doctorName = rootView.findViewById(R.id.doctorName);

        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        loadDoctorName(currentUser);

        // =============================
        // CAMERA SCAN
        // =============================
        btnScanQr.setOnClickListener(v -> {
            IntentIntegrator integrator = IntentIntegrator.forSupportFragment(this);
            integrator.setCaptureActivity(CaptureActivityPortrait.class);
            integrator.setPrompt("Scan Patient QR");
            integrator.setBeepEnabled(true);
            integrator.setOrientationLocked(true);
            integrator.initiateScan();
        });

        // =============================
        // GALLERY IMPORT
        // =============================
        btnImportGallery.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, GALLERY_REQUEST_CODE);
        });


        return rootView;
    }

    // =============================
    // OPEN MEDICAL CAMPS
    // =============================

    private void openMedicalCampsFragment() {

        rootView.findViewById(R.id.top_bar).setVisibility(View.GONE);
        rootView.findViewById(R.id.dashboard_scroll).setVisibility(View.GONE);
        rootView.findViewById(R.id.doctor_fragment_container).setVisibility(View.VISIBLE);

        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.doctor_fragment_container,
                        new fragment_medical_camps())
                .addToBackStack(null)
                .commit();
    }

    // =============================
    // HANDLE RESULTS
    // =============================

    @Override
    public void onActivityResult(int requestCode,
                                 int resultCode,
                                 @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_REQUEST_CODE
                && resultCode == Activity.RESULT_OK
                && data != null) {

            try {
                Uri imageUri = data.getData();
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(
                        requireActivity().getContentResolver(),
                        imageUri
                );
                scanQrFromBitmap(bitmap);

            } catch (Exception e) {
                Toast.makeText(requireContext(),
                        "Failed to read image",
                        Toast.LENGTH_SHORT).show();
            }
            return;
        }

        IntentResult result =
                IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if (result != null && result.getContents() != null) {

            String scannedText = result.getContents();

            if (scannedText.startsWith("patient_uid:")) {
                String patientUid =
                        scannedText.replace("patient_uid:", "");
                openOtpVerificationFragment(patientUid);
            } else {
                Toast.makeText(requireContext(),
                        "Invalid Patient QR",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    // =============================
    // ML KIT QR SCAN
    // =============================

    private void scanQrFromBitmap(Bitmap bitmap) {

        BarcodeScannerOptions options =
                new BarcodeScannerOptions.Builder()
                        .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                        .build();

        BarcodeScanner scanner =
                BarcodeScanning.getClient(options);

        InputImage image =
                InputImage.fromBitmap(bitmap, 0);

        scanner.process(image)
                .addOnSuccessListener(barcodes -> {

                    if (barcodes == null || barcodes.isEmpty()) {
                        Toast.makeText(requireContext(),
                                "No QR found in image",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Barcode barcode = barcodes.get(0);
                    String scannedText = barcode.getRawValue();

                    if (scannedText != null &&
                            scannedText.startsWith("patient_uid:")) {

                        String patientUid =
                                scannedText.replace("patient_uid:", "");

                        openOtpVerificationFragment(patientUid);

                    } else {
                        Toast.makeText(requireContext(),
                                "Invalid Patient QR",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(),
                                "QR Scan Failed",
                                Toast.LENGTH_SHORT).show()
                );
    }

    // =============================
    // LOAD DOCTOR NAME
    // =============================

    private void loadDoctorName(FirebaseUser currentUser) {

        if (currentUser == null) return;

        String uid = currentUser.getUid();

        userRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(uid);

        userRef.addListenerForSingleValueEvent(
                new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (snapshot.exists()) {

                            String fullName =
                                    snapshot.child("fullName")
                                            .getValue(String.class);

                            if (fullName != null) {
                                doctorName.setText("Dr. " + fullName);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                        if (isAdded()) {
                            Toast.makeText(requireContext(),
                                    "Failed to load name",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    private void openOtpVerificationFragment(String patientUid) {

        rootView.findViewById(R.id.top_bar).setVisibility(View.GONE);
        rootView.findViewById(R.id.dashboard_scroll).setVisibility(View.GONE);
        rootView.findViewById(R.id.doctor_fragment_container).setVisibility(View.VISIBLE);

        Bundle bundle = new Bundle();
        bundle.putString("PATIENT_UID", patientUid);

        fragment_otp_verification otpFragment =
                new fragment_otp_verification();

        otpFragment.setArguments(bundle);

        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.doctor_fragment_container,
                        otpFragment)
                .addToBackStack(null)
                .commit();
    }

    // =============================
    // OPEN RECORDS
    // =============================

    private void openRecordsFragment() {

        rootView.findViewById(R.id.top_bar).setVisibility(View.GONE);
        rootView.findViewById(R.id.dashboard_scroll).setVisibility(View.GONE);
        rootView.findViewById(R.id.doctor_fragment_container).setVisibility(View.VISIBLE);

        requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.doctor_fragment_container,
                        new PatientRecordsFragment())
                .addToBackStack(null)
                .commit();
    }
}