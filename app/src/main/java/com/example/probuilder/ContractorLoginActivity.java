package com.example.probuilder;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class ContractorLoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private static final String URL = "http://10.0.2.2:5000/login";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contractor_login);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);

        // The loginUser method is ONLY called when the user clicks the button.
        btnLogin.setOnClickListener(v -> loginUser());

        TextView tvSignUp = findViewById(R.id.tvSignUp);
        tvSignUp.setOnClickListener(v -> startActivity(new Intent(ContractorLoginActivity.this, ContractorSignUpActivity.class)));

        TextView tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvForgotPassword.setOnClickListener(v -> startActivity(new Intent(ContractorLoginActivity.this, ForgotPasswordActivity.class)));
    }

    private void loginUser() {
        // Disable the button to prevent multiple clicks
        btnLogin.setEnabled(false);

        // CORRECTED: Null-safe way to get text to prevent crashes
        final String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        final String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter both email and password", Toast.LENGTH_SHORT).show();
            btnLogin.setEnabled(true); // Re-enable button
            return;
        }

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL,
                response -> {
                    String trimmedResponse = response.trim();
                    Log.d("LoginResponse", "Server Response: " + trimmedResponse);

                    if (trimmedResponse.equals("success")) {
                        Toast.makeText(ContractorLoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(ContractorLoginActivity.this, ContractorDashboardActivity.class));
                        finish();
                    } else {
                        // Check if response is HTML
                        if (trimmedResponse.toLowerCase().startsWith("<!doctype html>") || trimmedResponse.toLowerCase().contains("<html>")) {
                            Toast.makeText(ContractorLoginActivity.this, "Server Error: Invalid HTML response received. Check API URL.", Toast.LENGTH_LONG).show();
                            Log.e("LoginResponse", "HTML Response received: " + trimmedResponse);
                        } else {
                            Toast.makeText(ContractorLoginActivity.this, "Login Failed: " + trimmedResponse, Toast.LENGTH_LONG).show();
                        }
                        btnLogin.setEnabled(true); // Re-enable button on failure
                    }
                },
                error -> {
                    String message = "Network Error";
                    if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                        message = "Cannot connect to server. Check internet or server status.";
                    } else if (error instanceof AuthFailureError) {
                        message = "Authentication Failure.";
                    } else if (error instanceof ServerError) {
                        message = "Server Error. Please try again.";
                        if (error.networkResponse != null && error.networkResponse.data != null) {
                            String errorData = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                            Log.e("LoginError", "Server Error Data: " + errorData);
                            
                            if (errorData.toLowerCase().startsWith("<!doctype html>") || errorData.toLowerCase().contains("<html>")) {
                                message = "Server Error: Invalid HTML response (Check Server Logs/URL).";
                                // Try to extract title
                                int titleStart = errorData.toLowerCase().indexOf("<title>");
                                int titleEnd = errorData.toLowerCase().indexOf("</title>");
                                if (titleStart != -1 && titleEnd != -1) {
                                    message = "Server Error: " + errorData.substring(titleStart + 7, titleEnd);
                                }
                            } else {
                                message = "Server Error: " + errorData;
                            }
                        }
                    } else if (error instanceof NetworkError) {
                         message = "Network Error. Check your connection.";
                    } else if (error instanceof ParseError) {
                        message = "Parsing Error.";
                    }

                    Toast.makeText(ContractorLoginActivity.this, message, Toast.LENGTH_LONG).show();
                    Log.e("LoginError", "Volley Error: " + error);
                    if (error.networkResponse != null) {
                        Log.e("LoginError", "Status Code: " + error.networkResponse.statusCode);
                    }
                    btnLogin.setEnabled(true); // Re-enable button on error
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("email", email);
                params.put("password", password);
                params.put("role", "contractor");
                Log.d("LoginParams", "Parameters: " + params);
                return params;
            }
        };

        // Set a longer timeout
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                30000, // 30 seconds
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }
}