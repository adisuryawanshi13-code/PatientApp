package com.example.patientapp;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;

public class Home extends Fragment {

    private View homeLayout, eventsLayout;
    private LinearLayout eventsList;
    private TextView tvGreeting;
    private TextView tvEmpty;

    FirebaseAuth auth;
    DatabaseReference userRef;

    private final ActivityResultLauncher<Intent> folderLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                            String name = result.getData().getStringExtra("f_name");
                            String date = result.getData().getStringExtra("f_date");
                            String hospital = result.getData().getStringExtra("f_hospital");
                            String category = result.getData().getStringExtra("f_category");
                            String desc = result.getData().getStringExtra("f_desc");
                            addFolderCard(name, date, hospital, category, desc);
                        }
                    });

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // 🔥 MISSING LINE (THIS WAS THE BUG)
        tvGreeting = view.findViewById(R.id.tvGreeting);

        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            startActivity(new Intent(requireContext(), RegisterActivity.class));
            requireActivity().finish();
            return view;
        }

        String uid = currentUser.getUid();

        userRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(uid);

        loadUserName();

        ImageView qrImageView = view.findViewById(R.id.patient_qr_code);
        generatePatientQR(qrImageView, "patient_uid:" + uid);

        return view;
    }


    // ================= Existing logic below untouched =================

    private void loadUserName() {
        userRef.child("fullName")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (!isAdded() || tvGreeting == null) return;

                        if (snapshot.exists()) {
                            String fullName = snapshot.getValue(String.class);
                            tvGreeting.setText("Hello, " + fullName);

                        } else {
                            tvGreeting.setText("Hello");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        if (isAdded() && tvGreeting != null) {
                            tvGreeting.setText("Hello");
                        }
                    }
                });
    }


    private void addFolderCard(String name, String date, String hospital, String category, String desc) {
        if (tvEmpty != null) tvEmpty.setVisibility(View.GONE);

        CardView card = new CardView(requireContext());
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2);
        p.setMargins(0, 0, 0, 24);
        card.setLayoutParams(p);
        card.setRadius(32f);
        card.setCardElevation(6f);
        card.setCardBackgroundColor(Color.WHITE);

        LinearLayout cardContent = new LinearLayout(requireContext());
        cardContent.setOrientation(LinearLayout.VERTICAL);
        cardContent.setPadding(40, 40, 40, 40);

        TextView tTitle = new TextView(requireContext());
        tTitle.setText("📂 " + name);
        tTitle.setTextSize(18);
        tTitle.setTypeface(null, Typeface.BOLD);

        TextView tSub = new TextView(requireContext());
        tSub.setText("Date: " + date + " | " + category);
        tSub.setTextSize(14);

        cardContent.addView(tTitle);
        cardContent.addView(tSub);
        card.addView(cardContent);

        card.setOnClickListener(v ->
                showFolderDetails(name, date, hospital, category, desc)
        );

        eventsList.addView(card);
    }

    private void showFolderDetails(String name, String date, String hospital, String category, String desc) {
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(createDetailView(name, date, hospital, category, desc, dialog));
        dialog.show();
    }

    private View createDetailView(String name, String date, String hospital, String category, String desc, Dialog dialog) {
        LinearLayout root = new LinearLayout(requireContext());
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(60, 60, 60, 60);

        TextView title = new TextView(requireContext());
        title.setText(name);
        title.setTextSize(22);
        title.setTypeface(null, Typeface.BOLD);

        root.addView(title);
        root.addView(createDetailRow("Visit Date", date));
        root.addView(createDetailRow("Hospital", hospital));
        root.addView(createDetailRow("Category", category));

        TextView dText = new TextView(requireContext());
        dText.setText(desc);
        root.addView(dText);

        Button close = new Button(requireContext());
        close.setText("Close");
        close.setOnClickListener(v -> dialog.dismiss());
        root.addView(close);

        return root;
    }

    private View createDetailRow(String label, String value) {
        LinearLayout row = new LinearLayout(requireContext());
        TextView lbl = new TextView(requireContext());
        lbl.setText(label + ": ");
        lbl.setTypeface(null, Typeface.BOLD);

        TextView val = new TextView(requireContext());
        val.setText(value);

        row.addView(lbl);
        row.addView(val);
        return row;
    }

    private void generatePatientQR(ImageView view, String data) {
        try {
            BarcodeEncoder encoder = new BarcodeEncoder();
            Bitmap bitmap = encoder.encodeBitmap(data, BarcodeFormat.QR_CODE, 400, 400);
            view.setImageBitmap(bitmap);
        } catch (Exception ignored) {}
    }
}