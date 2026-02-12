package com.example.patientapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.*;

import androidx.annotation.NonNull;
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

        // Firebase
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

        setupSpinners();

        // 🔥 Anonymous Auth (no login flow)
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            auth.signInAnonymously()
                    .addOnSuccessListener(authResult -> checkIfUserRegistered())
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Authentication failed", Toast.LENGTH_SHORT).show()
                    );
        } else {
            checkIfUserRegistered();
        }

        btnComplete.setOnClickListener(v -> saveUserData());
    }

    // ✅ If already registered → redirect by role
    private void checkIfUserRegistered() {
        String uid = auth.getCurrentUser().getUid();

        usersRef.child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String role = snapshot.child("role").getValue(String.class);
                            redirectToHomeBasedOnRole(role);
                        }
                        // else → stay on register screen
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) { }
                });
    }

    // ✅ Role-based navigation
    private void redirectToHomeBasedOnRole(String role) {
        Intent intent;

        if ("PATIENT".equalsIgnoreCase(role)) {
            intent = new Intent(this, MainActivity.class);
        } else if ("DOCTOR".equalsIgnoreCase(role)
                || "VOLUNTEER".equalsIgnoreCase(role)) {
            intent = new Intent(this, MainActivity.class);
        } else {
            intent = new Intent(this, MainActivity.class); // fallback
        }

        startActivity(intent);
        finish();
    }

    // Spinner setup
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

    // ✅ Save user and redirect by role
    private void saveUserData() {
        FirebaseUser firebaseUser = auth.getCurrentUser();
        if (firebaseUser == null) return;

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

        // Role selection
        String role = "PATIENT";
        int checkedId = toggleRole.getCheckedButtonId();
        if (checkedId == R.id.btnDoctor) role = "DOCTOR";
        else if (checkedId == R.id.btnVolunteer) role = "VOLUNTEER";

        // Chronic conditions
        List<String> conditions = new ArrayList<>();
        for (int i = 0; i < chipGroupConditions.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupConditions.getChildAt(i);
            if (chip.isChecked()) {
                conditions.add(chip.getText().toString());
            }
        }

        String uid = firebaseUser.getUid();

        Map<String, Object> user = new HashMap<>();
        user.put("fullName", fullName);
        user.put("phone", phone);
        user.put("role", role);
        user.put("gender", spinnerGender.getSelectedItem().toString());
        user.put("bloodGroup", spinnerBloodGroup.getSelectedItem().toString());
        user.put("heightCm",
                etHeight.getText().toString().isEmpty() ? 0 :
                        Integer.parseInt(etHeight.getText().toString()));
        user.put("chronicConditions", conditions);

        final String finalRole = role;

        usersRef.child(uid)
                .setValue(user)
                .addOnSuccessListener(aVoid -> redirectToHomeBasedOnRole(finalRole))
                .addOnFailureListener(e -> {
                    Log.e("DB_ERROR", e.getMessage());
                    Toast.makeText(this, "Save failed", Toast.LENGTH_SHORT).show();
                });

    }
}
