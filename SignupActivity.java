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

public class SignupActivity extends AppCompatActivity {

    private TextInputEditText etName, etEmail, etPassword;
    private MaterialButton btnSignup;
    private TextView tvLogin;
    private ProgressBar progressBar;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        etName      = findViewById(R.id.etName);
        etEmail     = findViewById(R.id.etEmail);
        etPassword  = findViewById(R.id.etPassword);
        btnSignup   = findViewById(R.id.btnSignup);
        tvLogin     = findViewById(R.id.tvLogin);
        progressBar = findViewById(R.id.progressBar);
        apiService  = ApiClient.getApiService();
        btnSignup.setOnClickListener(v -> attemptSignup());
        tvLogin.setOnClickListener(v -> { startActivity(new Intent(this, LoginActivity.class)); finish(); });
    }

    private void attemptSignup() {
        String name     = etName.getText()     != null ? etName.getText().toString().trim()  : "";
        String email    = etEmail.getText()    != null ? etEmail.getText().toString().trim()  : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString()      : "";
        if (TextUtils.isEmpty(name))     { etName.setError("Name is required");     etName.requestFocus();     return; }
        if (TextUtils.isEmpty(email))    { etEmail.setError("Email is required");   etEmail.requestFocus();    return; }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) { etEmail.setError("Enter a valid email"); etEmail.requestFocus(); return; }
        if (TextUtils.isEmpty(password)) { etPassword.setError("Password is required"); etPassword.requestFocus(); return; }
        if (password.length() < 6)      { etPassword.setError("Min 6 characters"); etPassword.requestFocus(); return; }
        setLoading(true);
        apiService.register(new RegisterRequest(name, email, password)).enqueue(new Callback<RegisterResponse>() {
            @Override
            public void onResponse(Call<RegisterResponse> call, Response<RegisterResponse> response) {
                setLoading(false);
                if (response.isSuccessful()) {
                    Toast.makeText(SignupActivity.this, "Account created! Please login.", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(SignupActivity.this, response.code() == 400 ? "Email already registered." : "Signup failed", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<RegisterResponse> call, Throwable t) {
                setLoading(false);
                Toast.makeText(SignupActivity.this, "Connection error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnSignup.setEnabled(!loading);
        btnSignup.setText(loading ? "Creating account..." : "Create Account");
    }
}