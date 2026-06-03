package com.example.imageenhancer;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HistoryActivity extends AppCompatActivity {

    private RecyclerView    recyclerView;
    private TextView        tvEmpty;
    private MaterialButton  btnBack;
    private View            progressBar;
    private SessionManager  session;
    private ApiService      apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        session      = new SessionManager(this);
        apiService   = ApiClient.getApiService();
        recyclerView = findViewById(R.id.recyclerHistory);
        tvEmpty      = findViewById(R.id.tvEmpty);
        btnBack      = findViewById(R.id.btnBack);
        progressBar  = findViewById(R.id.progressBar);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        btnBack.setOnClickListener(v -> finish());
        loadHistory();
    }

    private void loadHistory() {
        String userId = session.getUserId();
        if (userId == null || userId.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            tvEmpty.setText("No user session found");
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        apiService.getHistory(userId).enqueue(new Callback<List<ImageRecord>>() {
            @Override
            public void onResponse(Call<List<ImageRecord>> call, Response<List<ImageRecord>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    List<ImageRecord> images = response.body();
                    if (images.isEmpty()) {
                        tvEmpty.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    } else {
                        tvEmpty.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                        recyclerView.setAdapter(new HistoryAdapter(images));
                    }
                } else {
                    Toast.makeText(HistoryActivity.this, "Failed to load history", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<List<ImageRecord>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(HistoryActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}