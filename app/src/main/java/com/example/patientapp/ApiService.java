package com.example.patientapp;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {

    @POST("/generate-summary")
    Call<SummaryResponse> generateSummary(@Body SummaryRequest request);
}
