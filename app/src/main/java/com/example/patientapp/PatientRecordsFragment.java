package com.example.patientapp;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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

    private TextView tvAiSummary;

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
        tvAiSummary = rootView.findViewById(R.id.tvAiSummary);

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


        aiSummaryButton.setOnClickListener(v -> callGenerateSummary());

        loadFoldersFragment();

        return rootView;
    }

    private void animateSummaryText(String fullText) {

        tvAiSummary.setText("");

        if (fullText == null) return;

        tvAiSummary.setAlpha(0f);
        tvAiSummary.animate().alpha(1f).setDuration(300).start();

        String[] sentences = fullText.split("(?<=\\.)");

        Handler handler = new Handler(Looper.getMainLooper());
        int delay = 500;

        for (int i = 0; i < sentences.length; i++) {

            final int index = i;

            handler.postDelayed(() -> {
                tvAiSummary.append(sentences[index] + " ");
            }, delay * i);
        }
    }
    private void callGenerateSummary() {

        tvAiSummary.setText("Checking summary status...");
        aiSummaryButton.setEnabled(false);

        DatabaseReference summaryRef = FirebaseDatabase.getInstance()
                .getReference("aiSummaries");

        summaryRef.orderByChild("patientId")
                .equalTo(patientUid)
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        DatabaseReference userRef = FirebaseDatabase.getInstance()
                                .getReference("users")
                                .child(patientUid);

                        userRef.addListenerForSingleValueEvent(new ValueEventListener() {

                            @Override
                            public void onDataChange(@NonNull DataSnapshot userSnap) {

                                Long lastMedicalUpdate =
                                        userSnap.child("lastMedicalUpdateAt")
                                                .getValue(Long.class);

                                if (snapshot.exists()) {

                                    DataSnapshot summarySnap =
                                            snapshot.getChildren().iterator().next();

                                    String textSummary =
                                            summarySnap.child("textSummary")
                                                    .getValue(String.class);

                                    String imageSummary =
                                            summarySnap.child("imageSummary")
                                                    .getValue(String.class);

                                    Long generatedAt =
                                            summarySnap.child("generatedAt")
                                                    .getValue(Long.class);

                                    // Combine summaries safely
                                    String finalSummary =
                                            buildFinalSummary(textSummary, imageSummary);

                                    if (generatedAt != null &&
                                            lastMedicalUpdate != null &&
                                            generatedAt >= lastMedicalUpdate) {

                                        // ✅ Summary is fresh
                                        animateSummaryText(finalSummary);
                                        aiSummaryButton.setEnabled(true);
                                        return;
                                    }
                                }

                                // ❌ No summary OR outdated → regenerate
                                generateNewSummary();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                aiSummaryButton.setEnabled(true);
                                tvAiSummary.setText("Error checking user data.");
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        aiSummaryButton.setEnabled(true);
                        tvAiSummary.setText("Error checking summary.");
                    }
                });
    }

    private void generateNewSummary() {

        tvAiSummary.setText("Generating AI Summary...");
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

                            String textSummary = response.body().getTextSummary();
                            String imageSummary = response.body().getImageSummary();

                            String finalSummary =
                                    buildFinalSummary(textSummary, imageSummary);

                            if (finalSummary.trim().isEmpty()) {
                                tvAiSummary.setText("No summary generated.");
                                return;
                            }

                            animateSummaryText(finalSummary);

                        } else {
                            tvAiSummary.setText("Server Error.");
                        }
                    }

                    @Override
                    public void onFailure(Call<SummaryResponse> call, Throwable t) {
                        aiSummaryButton.setEnabled(true);
                        tvAiSummary.setText("Network Error.");
                    }
                });
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

    private String buildFinalSummary(String textSummary, String imageSummary) {

        StringBuilder builder = new StringBuilder();

        if (textSummary != null && !textSummary.trim().isEmpty()) {
            builder.append("📄 CLINICAL SUMMARY\n\n")
                    .append(textSummary.trim())
                    .append("\n\n");
        }

        if (imageSummary != null && !imageSummary.trim().isEmpty()) {
            builder.append("🩻 RADIOLOGY / IMAGE FINDINGS\n\n")
                    .append(imageSummary.trim());
        }

        return builder.toString();
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
