package com.example.patientapp;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import java.util.Calendar;

public class HealthFolderActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_health_folder);

        // Load fragment ONLY once
        if (savedInstanceState == null) {
            loadFragment(new FoldersListFragment());
        }
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(
                        R.id.health_folder_fragment_container,
                        fragment
                )
                .commit();
    }
}

