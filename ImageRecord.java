package com.example.imageenhancer;

import com.google.gson.annotations.SerializedName;

public class ImageRecord {
    @SerializedName("_id")
    public String id;

    @SerializedName("userId")
    public String userId;

    @SerializedName("originalImage")
    public String originalImage;

    @SerializedName("enhancedImage")
    public String enhancedImage;

    @SerializedName("psnr")
    public double psnr;

    @SerializedName("ssim")
    public double ssim;

    @SerializedName("createdAt")
    public String createdAt;
}