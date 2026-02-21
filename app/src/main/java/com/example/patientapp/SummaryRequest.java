package com.example.patientapp;

public class SummaryRequest {

    private String patientId;

    public SummaryRequest(String patientId) {
        this.patientId = patientId;
    }

    public String getPatientId() {
        return patientId;
    }
}
