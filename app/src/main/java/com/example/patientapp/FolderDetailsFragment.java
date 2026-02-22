package com.example.patientapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.*;

public class FolderDetailsFragment extends Fragment {

    private TextView folderName;
    private ExtendedFloatingActionButton btnUpload;

    private FirebaseAuth auth;
    private DatabaseReference folderRef;
    private DatabaseReference filesRef;

    private String folderId;
    private Uri selectedFileUri;
    private String selectedCategory = "PDF";

    private static final String CLOUD_NAME = "dwwdy3bk2";
    private static final String UPLOAD_PRESET = "android_pdf_upload";

    private ProgressDialog progressDialog;

    private String currentCategory = "REPORT"; // default

    private String targetUid;




    private RecyclerView rvRecords;
    private MedicalFileAdapter adapter;
    private List<MedicalFileModel> fileList = new ArrayList<>();


    // -------------------- ON CREATE --------------------

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {

        View view = inflater.inflate(R.layout.fragment_folder_details, container, false);

        progressDialog = new ProgressDialog(requireContext());
        progressDialog.setMessage("Uploading file...");
        progressDialog.setCancelable(false);


        folderName = view.findViewById(R.id.folderName);
        btnUpload = view.findViewById(R.id.btnUpload);

        rvRecords = view.findViewById(R.id.rvRecords);
        rvRecords.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new MedicalFileAdapter(fileList);
        rvRecords.setAdapter(adapter);


        auth = FirebaseAuth.getInstance();

        if (getArguments() != null) {
            folderId = getArguments().getString("FOLDER_ID");
            targetUid = getArguments().getString("TARGET_UID");
        }

        if (folderId == null) {
            Toast.makeText(requireContext(), "Invalid folder", Toast.LENGTH_SHORT).show();
            return view;
        }

        // If opened normally (patient flow)
        if (targetUid == null) {
            targetUid = auth.getCurrentUser().getUid();
        }



        filesRef = FirebaseDatabase.getInstance()
                .getReference("folderFiles")
                .child(targetUid)
                .child(folderId);


        loadFiles();

        folderRef = FirebaseDatabase.getInstance()
                .getReference("folder")
                .child(targetUid)
                .child(folderId);

        loadFolderDetails();

        // If doctor viewing patient, hide upload
        if (!targetUid.equals(auth.getCurrentUser().getUid())) {
            btnUpload.setVisibility(View.GONE);
        }


        TabLayout tabLayout = view.findViewById(R.id.tabLayout);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                switch (tab.getPosition()) {
                    case 0:
                        loadPrescriptions();
                        return; // IMPORTANT: stop loadFiles()
                    case 1:
                        currentCategory = "XRAY";
                        break;
                    case 2:
                        currentCategory = "REPORT";
                        break;
                    case 3:
                        currentCategory = "DOCUMENT";
                        break;
                }

                loadFiles(); // reload with new filter
            }

            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });


        btnUpload.setOnClickListener(v -> showUploadOptions());

        return view;
    }

    // -------------------- LOAD FOLDER NAME --------------------

    private void loadFolderDetails() {

        folderRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists()) {
                    String name = snapshot.child("folderName").getValue(String.class);
                    if (name != null) {
                        folderName.setText(name);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(requireContext(),
                        "Failed to load folder",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    // -------------------- BOTTOM SHEET --------------------

    private void showUploadOptions() {

        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());

        View sheetView = getLayoutInflater()
                .inflate(R.layout.fragment_bottom_sheet_upload_options, null);

        dialog.setContentView(sheetView);

        sheetView.findViewById(R.id.prescription)
                .setOnClickListener(v -> {

                    dialog.dismiss();

                    Bundle bundle = new Bundle();
                    bundle.putString("patientId", targetUid);   // get this from current screen
                    bundle.putString("folderId", folderId);     // current folder

                    add_new_prescription fragment =
                            new add_new_prescription();

                    fragment.setArguments(bundle);

                    fragment.show(getParentFragmentManager(),
                            "Prescription");
                });

        sheetView.findViewById(R.id.optionXray)
                .setOnClickListener(v -> {
                    selectedCategory = "XRAY";
                    dialog.dismiss();
                    openFilePicker();
                });

        sheetView.findViewById(R.id.optionLabReport)
                .setOnClickListener(v -> {
                    selectedCategory = "REPORT";
                    dialog.dismiss();
                    openFilePicker();
                });

        sheetView.findViewById(R.id.documents)
                .setOnClickListener(v -> {
                    selectedCategory = "DOCUMENT";
                    dialog.dismiss();
                    openFilePicker();
                });

        dialog.show();
    }

    // -------------------- FILE PICKER --------------------

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*"); // allow pdf or image
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        picker.launch(intent);
    }

    private final ActivityResultLauncher<Intent> picker =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK &&
                                result.getData() != null) {

                            Uri uri = result.getData().getData();
                            if (uri != null) {

                                selectedFileUri = uri;

                                requireContext().getContentResolver()
                                        .takePersistableUriPermission(
                                                uri,
                                                Intent.FLAG_GRANT_READ_URI_PERMISSION
                                        );

                                progressDialog.show();

                                new Thread(this::uploadToCloudinary).start();
                            }
                        }
                    }
            );

    // -------------------- CLOUDINARY UPLOAD --------------------

    private void uploadToCloudinary() {

        try {

            InputStream inputStream = requireContext()
                    .getContentResolver()
                    .openInputStream(selectedFileUri);

            ByteArrayOutputStream bos = new ByteArrayOutputStream();

            byte[] buffer = new byte[4096];
            int read;

            while ((read = inputStream.read(buffer)) != -1) {
                bos.write(buffer, 0, read);
            }

            byte[] fileBytes = bos.toByteArray();

            OkHttpClient client = new OkHttpClient();

            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart(
                            "file",
                            "upload_file",
                            RequestBody.create(fileBytes,
                                    MediaType.parse("application/pdf"))

                    )
                    .addFormDataPart("upload_preset", UPLOAD_PRESET)
                    .build();

            String uploadUrl =
                    "https://api.cloudinary.com/v1_1/" + CLOUD_NAME + "/auto/upload";

            Request request = new Request.Builder()
                    .url(uploadUrl)
                    .post(requestBody)
                    .build();

            Response response = client.newCall(request).execute();

            if (!response.isSuccessful()) {
                throw new Exception("Upload failed");
            }

            String body = response.body().string();
            JSONObject json = new JSONObject(body);

            String fileUrl = json.getString("secure_url");

            saveFileMetadata(fileUrl);

        } catch (Exception e) {

            requireActivity().runOnUiThread(() -> {
                progressDialog.dismiss();
                Toast.makeText(requireContext(),
                        "Upload failed",
                        Toast.LENGTH_SHORT).show();
            });

        }
    }


    private void saveFileMetadata(String fileUrl) {

        String fileId = filesRef.push().getKey();

        Map<String, Object> fileData = new HashMap<>();
        fileData.put("fileUrl", fileUrl);
        fileData.put("category", selectedCategory);
        fileData.put("uploadedAt", System.currentTimeMillis());

        filesRef.child(fileId).setValue(fileData);

        requireActivity().runOnUiThread(() -> {
            progressDialog.dismiss();
            Toast.makeText(requireContext(),
                    "Upload successful",
                    Toast.LENGTH_SHORT).show();
        });

    }

    private void loadFiles() {

        if (filesRef == null) return;

        filesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                fileList.clear();

                for (DataSnapshot ds : snapshot.getChildren()) {

                    MedicalFileModel model =
                            ds.getValue(MedicalFileModel.class);

                    if (model != null &&
                            currentCategory.equals(model.category)) {

                        fileList.add(model);
                    }
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(requireContext(),
                        "Failed to load files",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadPrescriptions() {

        DatabaseReference prescriptionRef = FirebaseDatabase.getInstance()
                .getReference("prescriptions")
                .child(targetUid);

        prescriptionRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                fileList.clear();

                for (DataSnapshot ds : snapshot.getChildren()) {

                    String folder = ds.child("folderId").getValue(String.class);

                    if (folder != null && folder.equals(folderId)) {

                        MedicalFileModel model = new MedicalFileModel();
                        model.category = "PRESCRIPTION";
                        model.fileUrl = "";
                        model.displayName = "Doctor Prescription";
                        model.subtitle = "Tap to view medicines";
                        model.id = ds.getKey();
                        model.patientId = targetUid;

                        fileList.add(model);
                    }
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(requireContext(),
                        "Failed to load prescriptions",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void openPdfExternally(String url) {

        try {

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.parse(url), "application/pdf");
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

            Intent chooser =
                    Intent.createChooser(intent, "Open PDF using");

            startActivity(chooser);

        }
        catch (Exception e) {

            // fallback → open in browser
            Intent browserIntent =
                    new Intent(Intent.ACTION_VIEW, Uri.parse(url));

            startActivity(browserIntent);
        }
    }

}
