package com.example.patientapp;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;

public class HomePage extends AppCompatActivity {

    private View homeLayout, eventsLayout;
    private LinearLayout eventsList;
    private TextView tvGreeting;

    private TextView tvEmpty;

    FirebaseAuth auth;
    DatabaseReference userRef;


    private final ActivityResultLauncher<Intent> folderLauncher = registerForActivityResult(
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        homeLayout = findViewById(R.id.home_layout);
        eventsLayout = findViewById(R.id.events_layout);
        eventsList = findViewById(R.id.events_list_container);

        tvGreeting = findViewById(R.id.tvGreeting);
        tvEmpty = findViewById(R.id.tv_empty);

        auth = FirebaseAuth.getInstance();

        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            // User not logged in → redirect to login
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        String uid = currentUser.getUid();


        userRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(uid);

        loadUserName();

        ImageView qrImageView = findViewById(R.id.patient_qr_code);
        generatePatientQR(qrImageView, "Patient: Itachi | ABHA: 12-3456-7890-1234");

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.nav_home) {
                    homeLayout.setVisibility(View.VISIBLE);
                    eventsLayout.setVisibility(View.GONE);
                    return true;
                } else if (itemId == R.id.nav_events) {
                    homeLayout.setVisibility(View.GONE);
                    eventsLayout.setVisibility(View.VISIBLE);
                    return true;
                } else if (itemId == R.id.nav_files) {
                    Intent intent = new Intent(HomePage.this, HealthFolderActivity.class);
                    folderLauncher.launch(intent);
                    return true;
                } else if (itemId == R.id.nav_profile) {
                    // Start the new Profile Activity
                    Intent intent = new Intent(HomePage.this, ProfileActivity.class);
                    startActivity(intent);
                    return true;
                }
                return true;
            }
        });
    }

    private void loadUserName() {

        userRef.child("fullName")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (snapshot.exists()) {
                            String fullName = snapshot.getValue(String.class);
                            tvGreeting.setText("Hello, " + fullName);
                        } else {
                            tvGreeting.setText("Hello");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        tvGreeting.setText("Hello");
                    }
                });
    }



    private void addFolderCard(String name, String date, String hospital, String category, String desc) {
        if (tvEmpty != null) tvEmpty.setVisibility(View.GONE);
        CardView card = new CardView(this);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2);
        p.setMargins(0, 0, 0, 24);
        card.setLayoutParams(p);
        card.setRadius(32f);
        card.setCardElevation(6f);
        card.setClickable(true);
        card.setFocusable(true);
        card.setCardBackgroundColor(Color.WHITE);
        LinearLayout cardContent = new LinearLayout(this);
        cardContent.setOrientation(LinearLayout.VERTICAL);
        cardContent.setPadding(40, 40, 40, 40);
        TextView tTitle = new TextView(this);
        tTitle.setText("📂 " + name);
        tTitle.setTextSize(18);
        tTitle.setTextColor(Color.parseColor("#111827"));
        tTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        TextView tSub = new TextView(this);
        tSub.setText("Date: " + date + " | " + category);
        tSub.setTextSize(14);
        tSub.setTextColor(Color.parseColor("#6B7280"));
        cardContent.addView(tTitle);
        cardContent.addView(tSub);
        card.addView(cardContent);
        card.setOnClickListener(v -> showFolderDetails(name, date, hospital, category, desc));
        eventsList.addView(card);
    }

    private void showFolderDetails(String name, String date, String hospital, String category, String desc) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(createDetailView(name, date, hospital, category, desc, dialog));
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        dialog.show();
    }

    private View createDetailView(String name, String date, String hospital, String category, String desc, Dialog dialog) {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.WHITE);
        root.setPadding(60, 60, 60, 60);
        TextView title = new TextView(this);
        title.setText(name);
        title.setTextSize(22);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setPadding(0, 0, 0, 40);
        root.addView(title);
        root.addView(createDetailRow("Visit Date", date));
        root.addView(createDetailRow("Hospital", hospital));
        root.addView(createDetailRow("Category", category));
        TextView dText = new TextView(this);
        dText.setText(desc);
        dText.setPadding(0, 40, 0, 40);
        root.addView(dText);
        Button close = new Button(this);
        close.setText("Close");
        close.setOnClickListener(v -> dialog.dismiss());
        root.addView(close);
        return root;
    }

    private View createDetailRow(String label, String value) {
        LinearLayout row = new LinearLayout(this);
        TextView lbl = new TextView(this);
        lbl.setText(label + ": ");
        lbl.setTypeface(null, android.graphics.Typeface.BOLD);
        TextView val = new TextView(this);
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
        } catch (Exception e) {}
    }
}