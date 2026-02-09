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
        requireActivity()
                .getOnBackPressedDispatcher()
                .addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {

                        FragmentManager childFm = getChildFragmentManager();

                        if (childFm.getBackStackEntryCount() > 0) {
                            // Pop HealthFolderFragment
                            childFm.popBackStack();
                        } else {
                            // Let activity handle back (switch tab / exit)
                            setEnabled(false);
                            requireActivity().onBackPressed();
                        }
                    }
                });


        recyclerFolders = rootView.findViewById(R.id.recyclerFolders);
        recyclerFolders.setLayoutManager(new LinearLayoutManager(requireContext()));

        createFolder = rootView.findViewById(R.id.createFolder);

        folderList = new ArrayList<>();
        adapter = new FolderAdapter(folderList);
        recyclerFolders.setAdapter(adapter);

        auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() == null) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return rootView;
        }

        String userId = auth.getCurrentUser().getUid();

        folderRef = FirebaseDatabase
                .getInstance()
                .getReference("folder")
                .child(userId);

        loadFolders();

        createFolder.setOnClickListener(v -> openHealthFolderFragment());

        setupBackStackListener();

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

    private void setupBackStackListener() {

        getChildFragmentManager().addOnBackStackChangedListener(() -> {

            if (getChildFragmentManager().getBackStackEntryCount() == 0) {

                View container = rootView.findViewById(R.id.folder_fragment_container);
                View recycler = rootView.findViewById(R.id.recyclerFolders);
                View button = rootView.findViewById(R.id.createFolder);

                container.setVisibility(View.GONE);
                recycler.setVisibility(View.VISIBLE);
                button.setVisibility(View.VISIBLE);
            }
        });
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
