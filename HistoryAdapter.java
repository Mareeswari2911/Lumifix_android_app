package com.example.imageenhancer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private final List<ImageRecord> images;

    public HistoryAdapter(List<ImageRecord> images) {
        this.images = images;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ImageRecord img = images.get(position);
        holder.tvIndex.setText("Image " + (position + 1));
        holder.tvPsnr.setText(String.format("PSNR: %.2f dB", img.psnr));
        holder.tvSsim.setText(String.format("SSIM: %.3f", img.ssim));
        Bitmap orig = base64ToBitmap(img.originalImage);
        if (orig != null) holder.ivOriginal.setImageBitmap(orig);
        Bitmap enh = base64ToBitmap(img.enhancedImage);
        if (enh != null) holder.ivEnhanced.setImageBitmap(enh);
    }

    @Override
    public int getItemCount() { return images.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView  tvIndex, tvPsnr, tvSsim;
        ImageView ivOriginal, ivEnhanced;
        ViewHolder(View itemView) {
            super(itemView);
            tvIndex    = itemView.findViewById(R.id.tvIndex);
            tvPsnr     = itemView.findViewById(R.id.tvPsnr);
            tvSsim     = itemView.findViewById(R.id.tvSsim);
            ivOriginal = itemView.findViewById(R.id.ivOriginal);
            ivEnhanced = itemView.findViewById(R.id.ivEnhanced);
        }
    }

    private Bitmap base64ToBitmap(String dataUri) {
        if (dataUri == null) return null;
        try {
            String base64 = dataUri.contains(",") ? dataUri.split(",")[1] : dataUri;
            byte[] bytes  = Base64.decode(base64, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } catch (Exception e) { return null; }
    }
}