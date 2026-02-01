package com.example.patientapp;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Calendar;

public class HealthFolderActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_health_folder);

        // 1. Linking all UI elements
        EditText etName = findViewById(R.id.et_folder_name);
        EditText etDate = findViewById(R.id.et_date);
        EditText etHospital = findViewById(R.id.et_hospital); // Matches the XML ID below
        EditText etDesc = findViewById(R.id.et_description);   // Matches the XML ID below
        Spinner spinner = findViewById(R.id.spinner_category);
        Button btnCreate = findViewById(R.id.btn_create_folder);
        ImageView back = findViewById(R.id.btn_back);

        // 2. Back Button Logic
        back.setOnClickListener(v -> finish());

        // 3. Date Picker Logic
        etDate.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(this, (view, year, month, day) -> {
                etDate.setText(day + "/" + (month + 1) + "/" + year);
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });

        // 4. Setup Category Spinner
        String[] items = {"General", "Pathology", "Radiology", "Surgery", "Cytopathology", "Other"};
        spinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items));

        // 5. Create Button Logic - Sends ALL data to MainActivity
        btnCreate.setOnClickListener(v -> {
            String name = etName.getText().toString();
            String date = etDate.getText().toString();
            String hospital = etHospital.getText().toString();
            String category = spinner.getSelectedItem().toString();
            String desc = etDesc.getText().toString();

            // Validation: Ensure Name and Date are provided at minimum
            if (name.isEmpty() || date.isEmpty()) {
                Toast.makeText(this, "Please provide Folder Name and Date", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent res = new Intent();
            res.putExtra("f_name", name);
            res.putExtra("f_date", date);
            res.putExtra("f_hospital", hospital);
            res.putExtra("f_category", category);
            res.putExtra("f_desc", desc);

            setResult(Activity.RESULT_OK, res);
            finish(); // Closes screen and sends data to the Canvas MainActivity
        });
    }
}