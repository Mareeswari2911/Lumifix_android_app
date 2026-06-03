package com.example.imageenhancer;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity {

    private TextView tvWelcome, tvPsnr, tvSsim;
    private MaterialButton btnPickImage, btnEnhance, btnDownload, btnHistory, btnLogout;
    private ImageView ivPreview, ivOriginal, ivEnhanced;
    private LinearLayout layoutPlaceholder, layoutLoading, layoutResult;

    private Uri selectedImageUri = null;
    private String enhancedBase64 = null;
    private String originalBase64 = null;

    private SessionManager session;
    private ApiService apiService;

    private final ActivityResultLauncher<String> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    ivPreview.setImageURI(uri);
                    ivPreview.setVisibility(View.VISIBLE);
                    layoutPlaceholder.setVisibility(View.GONE);
                    btnEnhance.setEnabled(true);
                    layoutResult.setVisibility(View.GONE);
                    enhancedBase64 = null;
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        session = new SessionManager(this);
        if (!session.isLoggedIn()) { goLogin(); return; }
        setContentView(R.layout.activity_home);

        tvWelcome         = findViewById(R.id.tvWelcome);
        btnPickImage      = findViewById(R.id.btnPickImage);
        btnEnhance        = findViewById(R.id.btnEnhance);
        btnDownload       = findViewById(R.id.btnDownload);
        btnHistory        = findViewById(R.id.btnHistory);
        btnLogout         = findViewById(R.id.btnLogout);
        ivPreview         = findViewById(R.id.ivPreview);
        ivOriginal        = findViewById(R.id.ivOriginal);
        ivEnhanced        = findViewById(R.id.ivEnhanced);
        layoutPlaceholder = findViewById(R.id.layoutPlaceholder);
        layoutLoading     = findViewById(R.id.layoutLoading);
        layoutResult      = findViewById(R.id.layoutResult);
        tvPsnr            = findViewById(R.id.tvPsnr);
        tvSsim            = findViewById(R.id.tvSsim);

        apiService = ApiClient.getApiService();

        String name = session.getName();
        if (name != null && !name.isEmpty()) tvWelcome.setText("Welcome, " + name + "!");

        btnPickImage.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
        btnEnhance.setOnClickListener(v -> enhanceImage());
        btnDownload.setOnClickListener(v -> downloadEnhancedImage());
        btnHistory.setOnClickListener(v -> startActivity(new Intent(this, HistoryActivity.class)));
        btnLogout.setOnClickListener(v -> logout());
    }

    private void enhanceImage() {
        if (selectedImageUri == null) { Toast.makeText(this, "Please select an image first", Toast.LENGTH_SHORT).show(); return; }
        layoutLoading.setVisibility(View.VISIBLE);
        layoutResult.setVisibility(View.GONE);
        btnEnhance.setEnabled(false);
        btnEnhance.setText("Enhancing...");
        try {
            InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
            byte[] imageBytes = toByteArray(inputStream);
            String mimeType = getContentResolver().getType(selectedImageUri);
            if (mimeType == null) mimeType = "image/jpeg";
            RequestBody requestFile = RequestBody.create(MediaType.parse(mimeType), imageBytes);
            MultipartBody.Part filePart = MultipartBody.Part.createFormData("file", "image.jpg", requestFile);
            RequestBody userIdBody = RequestBody.create(MediaType.parse("text/plain"), session.getUserId());
            apiService.enhanceImage(filePart, userIdBody).enqueue(new Callback<EnhanceResponse>() {
                @Override
                public void onResponse(Call<EnhanceResponse> call, Response<EnhanceResponse> response) {
                    layoutLoading.setVisibility(View.GONE);
                    btnEnhance.setEnabled(true);
                    btnEnhance.setText("Enhance Image");
                    if (response.isSuccessful() && response.body() != null && response.body().image != null) {
                        ImageRecord img = response.body().image;
                        originalBase64 = img.originalImage;
                        enhancedBase64 = img.enhancedImage;
                        Bitmap origBitmap = base64ToBitmap(originalBase64);
                        if (origBitmap != null) ivOriginal.setImageBitmap(origBitmap);
                        Bitmap enhBitmap = base64ToBitmap(enhancedBase64);
                        if (enhBitmap != null) { ivEnhanced.setImageBitmap(enhBitmap); ivPreview.setImageBitmap(enhBitmap); }
                        tvPsnr.setText(String.format("%.2f dB", img.psnr));
                        tvSsim.setText(String.format("%.3f", img.ssim));
                        layoutResult.setVisibility(View.VISIBLE);
                    } else {
                        Toast.makeText(HomeActivity.this, "Enhancement failed", Toast.LENGTH_LONG).show();
                    }
                }
                @Override
                public void onFailure(Call<EnhanceResponse> call, Throwable t) {
                    layoutLoading.setVisibility(View.GONE);
                    btnEnhance.setEnabled(true);
                    btnEnhance.setText("Enhance Image");
                    Toast.makeText(HomeActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        } catch (Exception e) {
            layoutLoading.setVisibility(View.GONE);
            btnEnhance.setEnabled(true);
            btnEnhance.setText("Enhance Image");
            Toast.makeText(this, "Failed to read image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void downloadEnhancedImage() {
        if (enhancedBase64 == null) return;
        Bitmap bitmap = base64ToBitmap(enhancedBase64);
        if (bitmap == null) { Toast.makeText(this, "No enhanced image to save", Toast.LENGTH_SHORT).show(); return; }
        String fileName = "enhanced_" + System.currentTimeMillis() + ".png";
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
                values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/ImageEnhancer");
                Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                if (uri != null) {
                    OutputStream os = getContentResolver().openOutputStream(uri);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
                    if (os != null) os.close();
                }
            } else {
                File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "ImageEnhancer");
                if (!dir.exists()) dir.mkdirs();
                File file = new File(dir, fileName);
                FileOutputStream fos = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.close();
                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
            }
            Toast.makeText(this, "Saved to Pictures/ImageEnhancer", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Toast.makeText(this, "Save failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private Bitmap base64ToBitmap(String dataUri) {
        try {
            String base64 = dataUri.contains(",") ? dataUri.split(",")[1] : dataUri;
            byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } catch (Exception e) { return null; }
    }

    private byte[] toByteArray(InputStream is) throws IOException {
        java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[16384];
        while ((nRead = is.read(data, 0, data.length)) != -1) buffer.write(data, 0, nRead);
        buffer.flush();
        return buffer.toByteArray();
    }

    private void logout() {
        session.clearSession();
        Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
        goLogin();
    }

    private void goLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}