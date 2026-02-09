package com.example.patientapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
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

    private FirebaseAuth auth;
    private DatabaseReference folderRef;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {

        View view = inflater.inflate(R.layout.fragment_folders_list, container, false);

        recyclerFolders = view.findViewById(R.id.recyclerFolders);
        recyclerFolders.setLayoutManager(new LinearLayoutManager(getContext()));

        folderList = new ArrayList<>();
        adapter = new FolderAdapter(folderList);
        recyclerFolders.setAdapter(adapter);

        auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() == null) {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return view;
        }

        String userId = auth.getCurrentUser().getUid();

        folderRef = FirebaseDatabase
                .getInstance()
                .getReference("folder")
                .child(userId);

        loadFolders();

        return view;
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
                        getContext(),
                        "Failed to load folders",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }
}
