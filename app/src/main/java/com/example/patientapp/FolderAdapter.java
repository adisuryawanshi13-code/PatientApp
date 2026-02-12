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
    private final OnFolderClickListener listener;

    public FolderAdapter(List<FolderModel> folderList,
                         OnFolderClickListener listener) {
        this.folderList = folderList;
        this.listener = listener;
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

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onFolderClick(folder);
            }
        });
    }

    @Override
    public int getItemCount() {
        return folderList.size();
    }

    public interface OnFolderClickListener {
        void onFolderClick(FolderModel folder);
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
