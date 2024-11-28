package com.example.coen_390_app;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface AIService {
    @Headers({
            "Content-Type: application/json",
            "Authorization: Bearer "// Replace with your API key, (Removed for security concerns)
    })
    @POST("v1/chat/completions")
    Call<AIResponse> getResponse(@Body AIRequest request);
}


