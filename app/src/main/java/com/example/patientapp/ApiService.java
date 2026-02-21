package com.example.patientapp;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {

    @POST("/api/ai-summary")
    Call<SummaryResponse> generateSummary(@Body SummaryRequest request);
}
