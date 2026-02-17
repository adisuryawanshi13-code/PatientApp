package com.example.patientapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class PatientRecordsFragment extends Fragment {

    private TextView patientName, height, bloodGroup;
    private DatabaseReference userRef;
    private String patientUid;

    public PatientRecordsFragment() {}

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {

        View rootView = inflater.inflate(
                R.layout.fragment_patient_records,
                container,
                false
        );

        // -----------------------------
        // Back Button
        // -----------------------------
        rootView.findViewById(R.id.btn_back_records)
                .setOnClickListener(v -> {
                    if (getActivity() != null) {
                        getActivity().findViewById(R.id.doctor_fragment_container)
                                .setVisibility(View.GONE);
                        getActivity().getSupportFragmentManager()
                                .popBackStack();
                    }
                });

        patientName = rootView.findViewById(R.id.patientName);
        height = rootView.findViewById(R.id.height);
        bloodGroup = rootView.findViewById(R.id.patientBloodGroup);

        // -----------------------------
        // Get PATIENT UID
        // -----------------------------
        Bundle bundle = getArguments();
        if (bundle != null && bundle.containsKey("PATIENT_UID")) {
            patientUid = bundle.getString("PATIENT_UID");
        }

        if (patientUid == null) return rootView;

        // -----------------------------
        // Load Patient Info
        // -----------------------------
        userRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(patientUid);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists()) {

                    String fullName = snapshot.child("fullName").getValue(String.class);
                    String bloodGrp = snapshot.child("bloodGroup").getValue(String.class);
                    Long heightCm = snapshot.child("heightCm").getValue(Long.class);

                    if (fullName != null)
                        patientName.setText(fullName);

                    if (bloodGrp != null)
                        bloodGroup.setText(bloodGrp);

                    if (heightCm != null)
                        height.setText(heightCm + " cm");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        // -----------------------------
        // Load FoldersListFragment inside
        // -----------------------------
        loadFoldersFragment();

        return rootView;
    }

    private void loadFoldersFragment() {

        Bundle bundle = new Bundle();
        bundle.putString("TARGET_UID", patientUid);

        FoldersListFragment fragment = new FoldersListFragment();
        fragment.setArguments(bundle);

        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.records_container, fragment)
                .addToBackStack(null)
                .commit();

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        View topBar = requireActivity().findViewById(R.id.top_bar);
        if (topBar != null) {
            topBar.setVisibility(View.VISIBLE);
        }
    }
}
