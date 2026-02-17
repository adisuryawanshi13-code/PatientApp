package com.example.patientapp;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class MedicalFileAdapter
        extends RecyclerView.Adapter<MedicalFileAdapter.ViewHolder> {

    private final List<MedicalFileModel> fileList;

    public MedicalFileAdapter(List<MedicalFileModel> fileList) {
        this.fileList = fileList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_item_medical_record, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position) {

        MedicalFileModel file = fileList.get(position);

        holder.tvFileName.setText("Medical File");
        holder.tvFileSubtitle.setText("Uploaded recently");

        if ("XRAY".equals(file.category)) {
            holder.ivIconType.setImageResource(R.drawable.hand_bones);
        } else if ("REPORT".equals(file.category)) {
            holder.ivIconType.setImageResource(R.drawable.ic_pdf);
        } else if ("DOCUMENT".equals(file.category)) {
            holder.ivIconType.setImageResource(R.drawable.docs);
        }

        holder.btnDownload.setOnClickListener(v -> {
            String downloadUrl =
                    file.fileUrl.replace("/upload/", "/upload/fl_attachment/");

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(downloadUrl));
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return fileList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvFileName, tvFileSubtitle;
        ImageView ivIconType;
        FloatingActionButton btnDownload;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvFileName = itemView.findViewById(R.id.tvFileName);
            tvFileSubtitle = itemView.findViewById(R.id.tvFileSubtitle);
            ivIconType = itemView.findViewById(R.id.ivIconType);
            btnDownload = itemView.findViewById(R.id.btnDownload);
        }
    }
}

