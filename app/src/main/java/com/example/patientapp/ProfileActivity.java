package com.example.patientapp;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // 1. Back Navigation
        findViewById(R.id.btn_back_profile).setOnClickListener(v -> finish());

        // 2. Generate Cyan Profile QR
        ImageView qrImg = findViewById(R.id.profile_qr_code);
        generateDetailedQR(qrImg, "ABHA-ID: 12-3456-7890-1234 | Patient: Itachi");

        // 3. Share Button Logic
        findViewById(R.id.btn_share_card).setOnClickListener(v ->
                Toast.makeText(this, "Generating Shareable Card...", Toast.LENGTH_SHORT).show()
        );
    }

    private void generateDetailedQR(ImageView view, String data) {
        try {
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            // Larger QR for profile page
            Bitmap bitmap = barcodeEncoder.encodeBitmap(data, BarcodeFormat.QR_CODE, 500, 500);
            view.setImageBitmap(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}