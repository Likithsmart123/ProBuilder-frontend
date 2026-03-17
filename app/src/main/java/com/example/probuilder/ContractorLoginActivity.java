package com.example.probuilder;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class ContractorLoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contractor_login);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        ImageView btnBack = findViewById(R.id.btnBack); // Added btnBack declaration

        btnBack.setOnClickListener(v -> finish()); // Added OnClickListener for btnBack
        // The loginUser method is ONLY called when the user clicks the button.
        btnLogin.setOnClickListener(v -> loginUser());


        TextView tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvForgotPassword.setOnClickListener(
                v -> startActivity(new Intent(ContractorLoginActivity.this, ForgotPasswordActivity.class)));
    }

    private void loginUser() {
        // Disable the button to prevent multiple clicks
        btnLogin.setEnabled(false);

        // STEP 2 — INPUT TRIMMING (NON-NEGOTIABLE)
        // Using strict trimming as requested
        final String email = etEmail.getText().toString().trim();
        final String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter both email and password", Toast.LENGTH_SHORT).show();
            btnLogin.setEnabled(true);
            return;
        }

        // STEP 1 — LOGIN URL (MUST MATCH TEST)
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constants.BASE_URL + "login.php",
                response -> {
                    // STEP 4 — RESPONSE HANDLING (PLAIN TEXT)
                    try {
                        // Try Parsing as JSON
                        org.json.JSONObject obj = new org.json.JSONObject(response);
                        if (obj.optString("status").equals("success")) {
                            String token = obj.getString("token");
                            String name = obj.optString("name", "");
                            int contractorId = obj.getInt("contractor_id");

                            // SAVE TO AUTH PREFS (FINAL FIX)
                            getSharedPreferences("AUTH", MODE_PRIVATE).edit()
                                    .putString("token", token)
                                    .putString("role", "contractor")
                                    .putInt("contractor_id", contractorId) // Keeping ID just in case
                                    .apply();

                            // Legacy Session (Keeping for untouched parts of app briefly)
                            getSharedPreferences("contractor_session", MODE_PRIVATE).edit()
                                    .putString("api_token", token)
                                    .putInt("contractor_id", contractorId)
                                    .putString("name", name)
                                    .apply();

                            // Legacy Sync (Optional, if other legacy activities read ProBuilderPrefs)
                            getSharedPreferences("ProBuilderPrefs", MODE_PRIVATE).edit()
                                    .putInt("contractor_id", contractorId)
                                    .putString("user_name", name)
                                    .apply();

                            Toast.makeText(this, "Login success", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(this, ContractorDashboardActivity.class));
                            finish();
                        } else {
                            String msg = obj.optString("message", "Login failed");
                            if ("invalid".equals(response.trim()))
                                msg = "Invalid credentials"; // Fallback
                            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                            btnLogin.setEnabled(true);
                        }
                    } catch (org.json.JSONException e) {
                        // Fallback for Legacy "success|id|name" if server revert/cache issue
                        String res = response.trim();
                        if (res.startsWith("success")) {
                            String[] parts = res.split("\\|");
                            if (parts.length >= 3) {
                                // ... handle legacy if really needed, but better to force JSON ...
                                // For now, assuming JSON update works
                            }
                        }
                        Log.e("LoginActivity", "JSON Parse error: " + response, e);
                        Toast.makeText(this, "Login Failed: " + response, Toast.LENGTH_SHORT).show();
                        btnLogin.setEnabled(true);
                    }
                },
                error -> {
                    String errorMsg = "Network Error";
                    if (error.getMessage() != null) {
                        errorMsg += ": " + error.getMessage();
                    }
                    if (error instanceof NoConnectionError) {
                        errorMsg = "Cannot connect to server. Check your internet connection or the server address.";
                    } else if (error.networkResponse != null) {
                        errorMsg = "HTTP " + error.networkResponse.statusCode + " Error";
                    }
                    Toast.makeText(ContractorLoginActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                    Log.e("LoginError", "Volley Error: " + error);
                    btnLogin.setEnabled(true);
                }) {
            @Override
            protected Map<String, String> getParams() {
                // STEP 3 — PARAM NAMES (MUST MATCH PHP)
                Map<String, String> params = new HashMap<>();
                params.put("email", email);
                params.put("password", password);
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }
}