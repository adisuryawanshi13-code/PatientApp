package com.example.patientapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.util.*;

public class RegisterActivity extends AppCompatActivity {

    private EditText etFullName, etPhone, etHeight;
    private MaterialButtonToggleGroup toggleRole;
    private Spinner spinnerGender, spinnerBloodGroup;
    private ChipGroup chipGroupConditions;
    private Button btnComplete;

    private FirebaseAuth auth;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        auth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        // Views
        etFullName = findViewById(R.id.etFullName);
        etPhone = findViewById(R.id.etPhone);
        etHeight = findViewById(R.id.etHeight);
        toggleRole = findViewById(R.id.toggleGroup);
        spinnerGender = findViewById(R.id.spinnerGender);
        spinnerBloodGroup = findViewById(R.id.spinnerBloodGroup);
        chipGroupConditions = findViewById(R.id.chipGroup);
        btnComplete = findViewById(R.id.btnComplete);

        // ---------- FORCE AUTH (NO CRASH GUARANTEE) ----------
        if (auth.getCurrentUser() == null) {
            auth.signInAnonymously()
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Auth failed", Toast.LENGTH_SHORT).show();
                        finish();
                    });
        }

        // ---------- SPINNERS ----------
        setupSpinners();

        btnComplete.setOnClickListener(v -> saveUserData());
    }

    private void setupSpinners() {
        String[] genders = {"Select Gender", "Male", "Female", "Other"};
        String[] bloodGroups = {"Select Blood Group","A+","A-","B+","B-","AB+","AB-","O+","O-"};

        ArrayAdapter<String> gAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, genders);
        gAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(gAdapter);

        ArrayAdapter<String> bAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, bloodGroups);
        bAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBloodGroup.setAdapter(bAdapter);

        spinnerGender.setSelection(0);
        spinnerBloodGroup.setSelection(0);
    }

    private void saveUserData() {
        FirebaseUser firebaseUser = auth.getCurrentUser();
        if (firebaseUser == null) {
            Toast.makeText(this, "Auth error. Restart app.", Toast.LENGTH_SHORT).show();
            return;
        }

        String fullName = etFullName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        if (fullName.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (spinnerGender.getSelectedItemPosition() == 0 ||
                spinnerBloodGroup.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Select Gender & Blood Group", Toast.LENGTH_SHORT).show();
            return;
        }

        // Role
        String role = "Patient";
        int checkedId = toggleRole.getCheckedButtonId();
        if (checkedId == R.id.btnDoctor) role = "Doctor";
        else if (checkedId == R.id.btnVolunteer) role = "Volunteer";

        // Conditions
        List<String> conditions = new ArrayList<>();
        for (int i = 0; i < chipGroupConditions.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupConditions.getChildAt(i);
            if (chip.isChecked()) conditions.add(chip.getText().toString());
        }

        String uid = firebaseUser.getUid();

        Map<String, Object> user = new HashMap<>();
        user.put("fullName", fullName);
        user.put("phone", phone);
        user.put("role", role);
        user.put("gender", spinnerGender.getSelectedItem().toString());
        user.put("bloodGroup", spinnerBloodGroup.getSelectedItem().toString());
        user.put("heightCm", etHeight.getText().toString().isEmpty() ? 0 :
                Integer.parseInt(etHeight.getText().toString()));
        user.put("chronicConditions", conditions);
        user.put("profileCompleted", true);

        usersRef.child(uid)
                .setValue(user)
                .addOnSuccessListener(aVoid -> {
                    startActivity(new Intent(this, HomePage.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e("DB_ERROR", e.getMessage());
                    Toast.makeText(this, "Save failed", Toast.LENGTH_SHORT).show();
                });
    }
}
