package com.example.probuilder;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;

import java.util.HashMap;
import java.util.Map;


import org.json.JSONObject;

public class ClientLoginActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword;
    private Button btnLogin;
    private static final String URL = Constants.BASE_URL + "client_login.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_login);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        android.widget.TextView tvRegisterLink = findViewById(R.id.tvRegisterLink);
        android.widget.TextView tvForgotPassword = findViewById(R.id.tvForgotPassword);
        android.widget.ImageView btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());
        btnLogin.setOnClickListener(v -> loginUser());
        tvRegisterLink.setOnClickListener(v -> startActivity(new Intent(this, ClientRegistrationActivity.class)));
        tvForgotPassword.setOnClickListener(v -> startActivity(new Intent(this, ForgotPasswordActivity.class)));
    }

    private void loginUser() {
        btnLogin.setEnabled(false);

        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter both email and password", Toast.LENGTH_SHORT).show();
            btnLogin.setEnabled(true);
            return;
        }

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL,
                response -> {
                    android.util.Log.d("CLIENT_LOGIN_RAW", response);
                    try {
                        JSONObject responseJson = new JSONObject(response);
                        String status = responseJson.optString("status");
                        String token = responseJson.optString("token");
                        int clientId = responseJson.optInt("client_id");

                        String role = responseJson.optString("role");
                        String name = responseJson.optString("name", ""); // Optimistic fetch

                        if (status.equals("success") && !token.isEmpty()) {
                            Toast.makeText(ClientLoginActivity.this, "Login Success", Toast.LENGTH_SHORT).show();

                            // 1. SAVE TO AUTH PREFS (FINAL FIX)
                            android.content.SharedPreferences prefs = getSharedPreferences("AUTH", MODE_PRIVATE);
                            prefs.edit()
                                    .putString("token", token)
                                    .putString("role", "client")
                                    .putInt("client_id", clientId)

                                    .putString("user_name", name) // Save for Dashboard
                                    .apply();

                            android.util.Log.d("CLIENT_AUTH", "Client token received = " + token);
                            android.util.Log.d("CLIENT_AUTH", "Client token saved = " + token);

                            String savedToken = prefs.getString("token", "NULL");
                            android.util.Log.d("CLIENT_AUTH", "Client token read back = " + savedToken);

                            // Use SessionManager (Legacy/Sync)
                            SessionManager sessionManager = new SessionManager(ClientLoginActivity.this);
                            sessionManager.clearSession(); // 1. Clear old
                            sessionManager.saveApiToken(token); // 2. Save new

                            sessionManager.saveClientId(clientId); // 3. Save ID
                            
                            if (!name.isEmpty()) {
                                 // Legacy session update
                                 getSharedPreferences("client_session", MODE_PRIVATE).edit()
                                      .putString("name", name)
                                      .apply();
                            }

                            // VERIFY IMMEDIATELY
                            int savedId = sessionManager.getClientId();
                            android.util.Log.d("SESSION_MGR", "Saved client_id = " + savedId);

                            if ("contractor".equals(role)) {
                                startActivity(new Intent(ClientLoginActivity.this, ContractorDashboardActivity.class));
                            } else {
                                startActivity(new Intent(ClientLoginActivity.this, ClientDashboardActivity.class));
                            }
                            finish();
                        } else {
                            String msg = responseJson.optString("message", "Login failed");
                            Toast.makeText(ClientLoginActivity.this, msg, Toast.LENGTH_SHORT).show();
                            btnLogin.setEnabled(true);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(ClientLoginActivity.this, "Response parse error", Toast.LENGTH_SHORT).show();
                        btnLogin.setEnabled(true);
                    }
                },
                error -> {
                    String message = "Network Error";
                    if (error instanceof com.android.volley.TimeoutError || error instanceof com.android.volley.NoConnectionError) {
                        message = "Cannot connect to server. Check internet or server status.";
                    } else if (error instanceof com.android.volley.AuthFailureError) {
                        message = "Authentication Failure.";
                    } else if (error instanceof com.android.volley.ServerError) {
                        message = "Server Error. Please try again.";
                        if (error.networkResponse != null && error.networkResponse.data != null) {
                            String errorData = new String(error.networkResponse.data, java.nio.charset.StandardCharsets.UTF_8);
                            if (errorData.toLowerCase().startsWith("<!doctype html>") || errorData.toLowerCase().contains("<html>")) {
                                message = "Server Error: Invalid HTML response.";
                            } else {
                                message = "Server Error: " + errorData;
                            }
                        }
                    } else if (error instanceof com.android.volley.NetworkError) {
                        message = "Network Error";
                        if (error.getMessage() != null) {
                            message += ": " + error.getMessage();
                        }
                    } else if (error instanceof com.android.volley.ParseError) {
                        message = "Parsing Error.";
                    }
                    if (message.equals("Network Error") && error.networkResponse != null) {
                        message = "HTTP " + error.networkResponse.statusCode + " Error";
                    }

                    Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                    btnLogin.setEnabled(true);
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("email", email);
                params.put("password", password);
                params.put("role", "client");
                return params;
            }
        };

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        VolleySingleton.getInstance(this).addToRequestQueue(stringRequest);
    }
}
