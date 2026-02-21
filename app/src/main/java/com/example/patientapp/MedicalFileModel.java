package com.example.patientapp;

public class MedicalFileModel {

    public String fileUrl;
    public String category;
    public long uploadedAt;

    public String displayName; // NEW
    public String subtitle;
    public String id;
    public String patientId;

    public MedicalFileModel() {
        // Required for Firebase
    }
}
