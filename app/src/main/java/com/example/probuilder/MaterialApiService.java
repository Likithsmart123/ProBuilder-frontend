package com.example.probuilder;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface MaterialApiService {

    @GET("/api/material-prediction")
    Call<MaterialResponse> getMaterialPrediction(
            @Query("material") String material
    );
}
