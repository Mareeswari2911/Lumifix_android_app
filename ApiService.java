package com.example.imageenhancer;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.*;


public interface ApiService {



    @POST("api/auth/register")
    @Headers("Content-Type: application/json")
    Call<RegisterResponse> register(@Body RegisterRequest body);

    @POST("api/auth/login")
    @Headers("Content-Type: application/json")
    Call<LoginResponse> login(@Body LoginRequest body);



    @Multipart
    @POST("api/enhance")
    Call<EnhanceResponse> enhanceImage(
            @Part MultipartBody.Part file,
            @Part("userId") RequestBody userId
    );



    @GET("api/image/history/{userId}")
    Call<java.util.List<ImageRecord>> getHistory(@Path("userId") String userId);
}