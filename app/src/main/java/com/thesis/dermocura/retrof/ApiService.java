package com.thesis.dermocura.retrof;

import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {
    @POST("phpmailer/verification_code.php") // Your endpoint
    Call<JsonObject> sendVerificationCode(@Body JsonObject email);
}
