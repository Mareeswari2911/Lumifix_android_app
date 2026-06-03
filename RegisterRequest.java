package com.example.imageenhancer;

import com.google.gson.annotations.SerializedName;

public class RegisterRequest {
    @SerializedName("name")
    public String name;

    @SerializedName("email")
    public String email;

    @SerializedName("password")
    public String password;

    public RegisterRequest(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }
}