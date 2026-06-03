package com.example.imageenhancer;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {
    @SerializedName("token")
    public String token;

    @SerializedName("userId")
    public String userId;

    @SerializedName("role")
    public String role;

    @SerializedName("name")
    public String name;

    @SerializedName("msg")
    public String msg;
}