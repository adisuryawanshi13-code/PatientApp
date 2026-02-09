package com.example.patientapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class FolderAdapter extends RecyclerView.Adapter<FolderAdapter.ViewHolder> {

    private final List<FolderModel> folderList;

    public FolderAdapter(List<FolderModel> folderList) {
        this.folderList = folderList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_folder, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position
    ) {
        FolderModel folder = folderList.get(position);

        holder.tvFolderName.setText(folder.folderName);
        holder.tvMeta.setText(folder.date + " • " + folder.hospital);
    }

    @Override
    public int getItemCount() {
        return folderList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvFolderName, tvMeta;
        ImageView imgIcon;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFolderName = itemView.findViewById(R.id.tvFolderName);
            tvMeta = itemView.findViewById(R.id.tvMeta);
            imgIcon = itemView.findViewById(R.id.imgIcon);
        }
    }
}
