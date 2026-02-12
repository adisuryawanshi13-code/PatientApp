package com.example.patientapp;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DatabaseError;


public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    FirebaseAuth auth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottom_navigation);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        if (savedInstanceState == null) {
            loadHomeBasedOnRole();
        }

        bottomNavigationView.setOnItemSelectedListener(item -> {

            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                loadHomeBasedOnRole();
                return true;

            } else if (itemId == R.id.nav_files) {
                selectedFragment = new FoldersListFragment();

            } else if (itemId == R.id.nav_events) {
                selectedFragment = new EventsFragment();
            }

            if (selectedFragment != null) {
                loadFragment(selectedFragment);
                return true;
            }

            return false;
        });
    }

    private void loadHomeBasedOnRole() {

        if (auth.getCurrentUser() == null) {
            loadFragment(new Home());
            return;
        }

        String uid = auth.getCurrentUser().getUid();

        DatabaseReference usersRef =
                FirebaseDatabase.getInstance().getReference("users");

        usersRef.child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (snapshot.exists()) {
                            String role = snapshot.child("role").getValue(String.class);

                            if ("DOCTOR".equalsIgnoreCase(role)) {
                                loadFragment(new DoctorHomePageFragment());
                            } else {
                                loadFragment(new Home());
                            }
                        } else {
                            loadFragment(new Home());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        loadFragment(new Home());
                    }
                });
    }


    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}
