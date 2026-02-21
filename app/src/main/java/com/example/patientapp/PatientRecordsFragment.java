package com.example.patientapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PatientRecordsFragment extends Fragment {

    private TextView patientName, height, bloodGroup;
    private DatabaseReference userRef;
    private String patientUid;
    private Button aiSummaryButton;

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
        aiSummaryButton = rootView.findViewById(R.id.aisummarybutton);

        // -----------------------------
        // Get PATIENT UID
        // -----------------------------
        Bundle bundle = getArguments();
        if (bundle != null && bundle.containsKey("PATIENT_UID")) {
            patientUid = bundle.getString("PATIENT_UID");
        }

        if (patientUid == null) {
            Toast.makeText(getContext(), "Patient ID missing", Toast.LENGTH_LONG).show();
            return rootView;
        }

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
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(),
                        "Failed to load patient data",
                        Toast.LENGTH_SHORT).show();
            }
        });

        // -----------------------------
        // AI SUMMARY BUTTON CLICK
        // -----------------------------
        aiSummaryButton.setOnClickListener(v -> callGenerateSummary());

        // -----------------------------
        // Load Folders Fragment
        // -----------------------------
        loadFoldersFragment();

        return rootView;
    }

    // =============================
    // CALL EXPRESS BACKEND
    // =============================
    private void callGenerateSummary() {

        Toast.makeText(getContext(),
                "Generating AI Summary...",
                Toast.LENGTH_SHORT).show();

        aiSummaryButton.setEnabled(false);

        ApiService apiService =
                RetrofitClient.getInstance().create(ApiService.class);

        SummaryRequest request = new SummaryRequest(patientUid);

        apiService.generateSummary(request)
                .enqueue(new Callback<SummaryResponse>() {

                    @Override
                    public void onResponse(Call<SummaryResponse> call,
                                           Response<SummaryResponse> response) {

                        aiSummaryButton.setEnabled(true);

                        if (response.isSuccessful() && response.body() != null) {

                            String summary = response.body().getSummary();

                            if (summary == null || summary.isEmpty()) {
                                Toast.makeText(getContext(),
                                        "Empty summary received",
                                        Toast.LENGTH_LONG).show();
                                return;
                            }

                            showSummaryDialog(summary);

                        } else {
                            Toast.makeText(getContext(),
                                    "Server Error: " + response.code(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<SummaryResponse> call, Throwable t) {

                        aiSummaryButton.setEnabled(true);

                        Toast.makeText(getContext(),
                                "Network Error: " + t.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    // =============================
    // SHOW SUMMARY DIALOG
    // =============================
    private void showSummaryDialog(String summaryText) {

        AlertDialog.Builder builder =
                new AlertDialog.Builder(requireContext());

        builder.setTitle("AI Medical Summary");

        builder.setMessage(summaryText);

        builder.setPositiveButton("Close",
                (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    // =============================
    // LOAD FOLDERS FRAGMENT
    // =============================
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
