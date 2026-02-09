package com.example.patientapp;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class HealthFolderFragment extends Fragment {

    // UI
    private EditText etFolderName, etDate, etHospital, etDescription;
    private Spinner spCategory;
    private Button btnCreateFolder;

    // Firebase
    private FirebaseAuth auth;
    private DatabaseReference folderRef;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {

        View view = inflater.inflate(R.layout.fragment_health_folder, container, false);

        // Bind UI
        etFolderName = view.findViewById(R.id.etFolderName);
        etDate = view.findViewById(R.id.etDate);
        etHospital = view.findViewById(R.id.etHospital);
        etDescription = view.findViewById(R.id.etDescription);
        spCategory = view.findViewById(R.id.spCategory);
        btnCreateFolder = view.findViewById(R.id.btnCreateFolder);
        ImageView btnBack = view.findViewById(R.id.btnBack);

        // Firebase
        auth = FirebaseAuth.getInstance();
        folderRef = FirebaseDatabase.getInstance().getReference("folder");

        // Spinner data
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                new String[]{"Checkup", "Prescription", "Lab Report", "Scan", "Other"}
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategory.setAdapter(adapter);

        // Date picker
        etDate.setOnClickListener(v -> openDatePicker());

        // Back
        btnBack.setOnClickListener(v -> requireActivity().onBackPressed());

        // Create folder
        btnCreateFolder.setOnClickListener(v -> createFolder());

        return view;
    }

    private void openDatePicker() {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(
                requireContext(),
                (view, y, m, d) -> etDate.setText(d + "/" + (m + 1) + "/" + y),
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private void createFolder() {

        String folderName = etFolderName.getText().toString().trim();
        String date = etDate.getText().toString().trim();
        String hospital = etHospital.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String category = spCategory.getSelectedItem().toString();

        if (TextUtils.isEmpty(folderName)) {
            etFolderName.setError("Required");
            return;
        }

        if (TextUtils.isEmpty(date)) {
            etDate.setError("Required");
            return;
        }

        if (auth.getCurrentUser() == null) {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        String folderId = folderRef.child(userId).push().getKey();

        if (folderId == null) {
            Toast.makeText(getContext(), "Failed to create folder", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("folderId", folderId);
        data.put("folderName", folderName);
        data.put("date", date);
        data.put("hospital", hospital);
        data.put("category", category);
        data.put("description", description);
        data.put("createdAt", System.currentTimeMillis());

        folderRef.child(userId).child(folderId)
                .setValue(data)
                .addOnSuccessListener(unused -> {

                    Toast.makeText(
                            getContext(),
                            "Health folder created",
                            Toast.LENGTH_SHORT
                    ).show();

                    // ✅ OPEN FOLDERS LIST FRAGMENT
                    requireActivity()
                            .getSupportFragmentManager()
                            .beginTransaction()
                            .replace(
                                    R.id.health_folder_fragment_container,
                                    new FoldersListFragment()
                            )
                            .addToBackStack(null)
                            .commit();
                });
    }
}
