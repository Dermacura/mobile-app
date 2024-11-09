// ApiService.java
package com.thesis.dermocura.retrof;

import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {

    @POST("phpmailer/send_password_reset_code.php")
    Call<JsonObject> sendPasswordResetCode(@Body JsonObject email);

    @POST("phpmailer/verify_password_reset_code.php")
    Call<JsonObject> verifyPasswordResetCode(@Body JsonObject data);

    @POST("phpmailer/update_password.php")
    Call<JsonObject> updatePassword(@Body JsonObject data);

    @POST("phpmailer/verification_code.php")
    Call<JsonObject> sendVerificationCode(@Body JsonObject email);
}
