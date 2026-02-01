package com.example.patientapp;

import android.app.DownloadManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "CLOUDINARY_DEBUG";

    // 🔴 REPLACE THESE
    private static final String CLOUD_NAME = "dwwdy3bk2";
    private static final String UPLOAD_PRESET = "android_pdf_upload";

    private Uri selectedFileUri;
    private String fileUrl = "";

    private DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // UI
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setPadding(40, 40, 40, 40);

        Button uploadBtn = new Button(this);
        uploadBtn.setText("Upload PDF");

        Button downloadBtn = new Button(this);
        downloadBtn.setText("Download PDF");

        layout.addView(uploadBtn);
        layout.addView(downloadBtn);
        setContentView(layout);

        dbRef = FirebaseDatabase.getInstance().getReference("files");

        uploadBtn.setOnClickListener(v -> pickPdf());
        downloadBtn.setOnClickListener(v -> downloadPdf());

        fetchLastFile();
    }

    // 📂 PICK FILE
    private void pickPdf() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/pdf");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        picker.launch(intent);
    }

    ActivityResultLauncher<Intent> picker =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            Uri uri = result.getData().getData();
                            if (uri != null) {
                                getContentResolver().takePersistableUriPermission(
                                        uri,
                                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                                );
                                selectedFileUri = uri;
                                new Thread(this::uploadToCloudinary).start();
                            }
                        }
                    }
            );

    // ☁️ UPLOAD TO CLOUDINARY
    private void uploadToCloudinary() {
        try {
            Log.d(TAG, "Upload started");

            InputStream inputStream = getContentResolver().openInputStream(selectedFileUri);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();

            byte[] buffer = new byte[4096];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                bos.write(buffer, 0, read);
            }

            byte[] fileBytes = bos.toByteArray();
            Log.d(TAG, "File size: " + fileBytes.length);

            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                    .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                    .build();

            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart(
                            "file",
                            "document.pdf",
                            RequestBody.create(fileBytes, MediaType.parse("application/pdf"))
                    )
                    .addFormDataPart("upload_preset", UPLOAD_PRESET)
                    .build();

            // ✅ FIXED ENDPOINT
            String uploadUrl =
                    "https://api.cloudinary.com/v1_1/" + CLOUD_NAME + "/image/upload";

            Request request = new Request.Builder()
                    .url(uploadUrl)
                    .post(requestBody)
                    .build();

            Response response = client.newCall(request).execute();
            String body = response.body().string();

            if (!response.isSuccessful()) {
                throw new Exception(body);
            }

            JSONObject json = new JSONObject(body);
            fileUrl = json.getString("secure_url");

            Log.d(TAG, "SUCCESS URL = " + fileUrl);

            dbRef.push().child("fileUrl").setValue(fileUrl);

            runOnUiThread(() ->
                    Toast.makeText(this, "Upload successful", Toast.LENGTH_LONG).show()
            );

        } catch (Exception e) {
            Log.e(TAG, "UPLOAD FAILED", e);
            runOnUiThread(() ->
                    Toast.makeText(this, "Upload failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
            );
        }
    }


    // ⬇️ DOWNLOAD / OPEN
    private void downloadPdf() {
        if (fileUrl == null || fileUrl.isEmpty()) {
            Toast.makeText(this, "No file found", Toast.LENGTH_SHORT).show();
            return;
        }

        // Force download instead of preview
        String downloadUrl = fileUrl.replace(
                "/upload/",
                "/upload/fl_attachment/"
        );

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(downloadUrl));
        startActivity(intent);
    }






    private void fetchLastFile() {
        dbRef.limitToLast(1).get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                for (DataSnapshot child : snapshot.getChildren()) {
                    fileUrl = child.child("fileUrl").getValue(String.class);
                    Log.d(TAG, "Fetched last file URL: " + fileUrl);
                }
            } else {
                Log.d(TAG, "No files found in database.");
            }
        });
    }
}
