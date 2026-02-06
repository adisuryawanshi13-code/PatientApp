package com.example.patientapp;

import android.app.DatePickerDialog;
import android.os.Bundle;
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

import java.util.Calendar;

public class HealthFolderFragment extends Fragment {

    private EditText etFolderName, etDate, etHospital, etDescription;
    private Spinner spCategory;
    private Button btnCreateFolder;
    private ImageView btnBack;

    public HealthFolderFragment() {}

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {

        View view = inflater.inflate(
                R.layout.fragment_health_folder,
                container,
                false
        );

        // Bind views
        etFolderName = view.findViewById(R.id.etFolderName);
        etDate = view.findViewById(R.id.etDate);
        etHospital = view.findViewById(R.id.etHospital);
        etDescription = view.findViewById(R.id.etDescription);
        spCategory = view.findViewById(R.id.spCategory);
        btnCreateFolder = view.findViewById(R.id.btnCreateFolder);
        btnBack = view.findViewById(R.id.btnBack);

        setupCategorySpinner();
        setupDatePicker();

        btnCreateFolder.setOnClickListener(v -> {
            // TEMP: just show data
            Toast.makeText(
                    requireContext(),
                    "Health Folder Created (TEMP)",
                    Toast.LENGTH_SHORT
            ).show();
        });

        btnBack.setOnClickListener(v -> {
            requireActivity().onBackPressed();
        });

        return view;
    }

    // -----------------------------
    // Spinner setup
    // -----------------------------
    private void setupCategorySpinner() {

        String[] categories = {
                "Dental",
                "Cardiology",
                "Neurology",
                "Orthopedic",
                "General",
                "Others"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                categories
        );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spCategory.setAdapter(adapter);
    }

    // -----------------------------
    // Date Picker
    // -----------------------------
    private void setupDatePicker() {

        etDate.setOnClickListener(v -> {

            Calendar calendar = Calendar.getInstance();

            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog dialog = new DatePickerDialog(
                    requireContext(),
                    (view, y, m, d) -> {
                        String selectedDate =
                                d + "/" + (m + 1) + "/" + y;
                        etDate.setText(selectedDate);
                    },
                    year,
                    month,
                    day
            );

            dialog.show();
        });
    }
}
