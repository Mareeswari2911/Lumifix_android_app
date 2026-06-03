package com.example.imageenhancer;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnLogin;
    private TextView tvSignup;
    private ProgressBar progressBar;
    private SessionManager session;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        session = new SessionManager(this);
        if (session.isLoggedIn()) { goHome(); return; }
        setContentView(R.layout.activity_login);
        etEmail     = findViewById(R.id.etEmail);
        etPassword  = findViewById(R.id.etPassword);
        btnLogin    = findViewById(R.id.btnLogin);
        tvSignup    = findViewById(R.id.tvSignup);
        progressBar = findViewById(R.id.progressBar);
        apiService  = ApiClient.getApiService();
        btnLogin.setOnClickListener(v -> attemptLogin());
        tvSignup.setOnClickListener(v -> startActivity(new Intent(this, SignupActivity.class)));
    }

    private void attemptLogin() {
        String email    = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString() : "";
        if (TextUtils.isEmpty(email)) { etEmail.setError("Email is required"); etEmail.requestFocus(); return; }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) { etEmail.setError("Enter a valid email"); etEmail.requestFocus(); return; }
        if (TextUtils.isEmpty(password)) { etPassword.setError("Password is required"); etPassword.requestFocus(); return; }
        setLoading(true);
        apiService.login(new LoginRequest(email, password)).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse body = response.body();
                    if (body.token == null || body.token.isEmpty()) { Toast.makeText(LoginActivity.this, "Login failed", Toast.LENGTH_SHORT).show(); return; }
                    session.saveSession(body.token, body.userId != null ? body.userId : "", body.role != null ? body.role : "user", body.name != null ? body.name : "");
                    Toast.makeText(LoginActivity.this, "Welcome back!", Toast.LENGTH_SHORT).show();
                    goHome();
                } else {
                    Toast.makeText(LoginActivity.this, response.code() == 400 ? "Invalid email or password" : "Login failed", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                setLoading(false);
                Toast.makeText(LoginActivity.this, "Connection error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void goHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!loading);
        btnLogin.setText(loading ? "Logging in..." : "Login");
    }
}