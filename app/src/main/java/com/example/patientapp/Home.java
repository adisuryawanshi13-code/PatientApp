package com.example.patientapp;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;

public class Home extends Fragment {

    private LinearLayout eventsList;
    private TextView tvGreeting;
    private TextView tvEmpty;
    private ImageView healthRecord;
    private CardView cardLabReports; // Variable for Lab Reports card

    FirebaseAuth auth;
    DatabaseReference userRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // 1. Initialize Views
        tvGreeting = view.findViewById(R.id.tvGreeting);
        healthRecord = view.findViewById(R.id.healthRecord);
        cardLabReports = view.findViewById(R.id.card_lab_reports); // Link to Lab Reports Card

        // 2. Lab Reports Click Listener (Updated to match your filename)
        cardLabReports.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new fragment_health_analytics())
                    .addToBackStack(null)
                    .commit();
        });

        // 3. Health Records Click Listener
        healthRecord.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new FoldersListFragment())
                    .addToBackStack(null)
                    .commit();
        });

        // Firebase Logic
        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            startActivity(new Intent(requireContext(), RegisterActivity.class));
            requireActivity().finish();
            return view;
        }

        String uid = currentUser.getUid();
        userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);
        loadUserName();

        ImageView qrImageView = view.findViewById(R.id.patient_qr_code);
        generatePatientQR(qrImageView, "patient_uid:" + uid);

        return view;
    }

    private void loadUserName() {
        userRef.child("fullName").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded() || tvGreeting == null) return;
                if (snapshot.exists()) {
                    String fullName = snapshot.getValue(String.class);
                    tvGreeting.setText("Hello, " + fullName);
                } else {
                    tvGreeting.setText("Hello");
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void generatePatientQR(ImageView view, String data) {
        try {
            BarcodeEncoder encoder = new BarcodeEncoder();
            Bitmap bitmap = encoder.encodeBitmap(data, BarcodeFormat.QR_CODE, 400, 400);
            view.setImageBitmap(bitmap);
        } catch (Exception ignored) {}
    }
}