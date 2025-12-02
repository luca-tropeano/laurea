package com.example.examapplication;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GoogleCustomSearchApi {

    @GET("customsearch/v1")
    Call<SearchResponse> search(
            @Query("key") String apiKey,
            @Query("cx") String searchEngineId,
            @Query("q") String query
    );
}
