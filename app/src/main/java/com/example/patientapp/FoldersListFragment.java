package com.example.patientapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FoldersListFragment extends Fragment {

    private RecyclerView recyclerFolders;
    private FolderAdapter adapter;
    private List<FolderModel> folderList;

    private Button createFolder;

    private FirebaseAuth auth;
    private String targetUid;

    private DatabaseReference folderRef;

    private View rootView;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {

        rootView = inflater.inflate(R.layout.fragment_folders_list, container, false);

        recyclerFolders = rootView.findViewById(R.id.recyclerFolders);
        recyclerFolders.setLayoutManager(new LinearLayoutManager(requireContext()));

        createFolder = rootView.findViewById(R.id.createFolder);

        folderList = new ArrayList<>();

        auth = FirebaseAuth.getInstance();

        // -----------------------------
        // Get TARGET UID
        // -----------------------------
        if (getArguments() != null) {
            targetUid = getArguments().getString("TARGET_UID");
        }

        if (targetUid == null) {
            targetUid = auth.getCurrentUser().getUid();
        }

        // -----------------------------
        // Folder Click Logic (Reusable)
        // -----------------------------
        adapter = new FolderAdapter(folderList, folder -> {

            Bundle bundle = new Bundle();
            bundle.putString("FOLDER_ID", folder.folderId);
            bundle.putString("TARGET_UID", targetUid);

            FolderDetailsFragment fragment = new FolderDetailsFragment();
            fragment.setArguments(bundle);

            // If this fragment is inside another fragment
            if (getParentFragment() != null) {

                getParentFragment()
                        .getChildFragmentManager()
                        .beginTransaction()
                        .replace(R.id.records_container, fragment)
                        .addToBackStack(null)
                        .commit();

            } else {
                // Normal patient flow
                requireActivity()
                        .getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit();
            }
        });

        recyclerFolders.setAdapter(adapter);

        folderRef = FirebaseDatabase
                .getInstance()
                .getReference("folder")
                .child(targetUid);

        loadFolders();

        // -----------------------------
        // Hide create button if doctor
        // -----------------------------
        if (getParentFragment() != null) {
            createFolder.setVisibility(View.GONE);
        } else {
            createFolder.setOnClickListener(v -> openHealthFolderFragment());
        }

        return rootView;
    }

    private void openHealthFolderFragment() {

        View container = rootView.findViewById(R.id.folder_fragment_container);
        View recycler = rootView.findViewById(R.id.recyclerFolders);
        View button = rootView.findViewById(R.id.createFolder);

        container.setVisibility(View.VISIBLE);
        recycler.setVisibility(View.GONE);
        button.setVisibility(View.GONE);

        getChildFragmentManager()
                .beginTransaction()
                .replace(
                        R.id.folder_fragment_container,
                        new HealthFolderFragment()
                )
                .addToBackStack(null)
                .commit();
    }

    private void loadFolders() {

        folderRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                folderList.clear();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    FolderModel model = ds.getValue(FolderModel.class);
                    if (model != null) {
                        folderList.add(model);
                    }
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(
                        requireContext(),
                        "Failed to load folders",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }
}
