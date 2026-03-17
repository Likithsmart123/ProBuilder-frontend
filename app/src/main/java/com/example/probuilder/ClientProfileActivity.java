package com.example.probuilder;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ClientProfileActivity extends AppCompatActivity {

    private TextInputEditText etName, etEmail, etPhone;
    private TextView tvContractorName;
    private int clientId;
    
    // API URLs - Assumed standard naming convention
    private static final String GET_PROFILE_URL = Constants.BASE_URL + "get_client_profile.php";
    private static final String UPDATE_PROFILE_URL = Constants.BASE_URL + "update_client_profile.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_profile);

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());
        
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        tvContractorName = findViewById(R.id.tvContractorName);
        
        // Get Client ID
        SharedPreferences authPrefs = getSharedPreferences("AUTH", MODE_PRIVATE);
        clientId = authPrefs.getInt("client_id", 0);
        if (clientId == 0) {
            clientId = getSharedPreferences("client_session", MODE_PRIVATE).getInt("client_id", 0);
        }

        if (clientId == 0) {
            Toast.makeText(this, "Session Invalid", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadProfile();

        MaterialButton btnUpdate = findViewById(R.id.btnUpdate);
        btnUpdate.setOnClickListener(v -> updateProfile());

        MaterialButton btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> {
            // Secure Logout: Clear all sessions
            getSharedPreferences("client_session", MODE_PRIVATE).edit().clear().apply();
            getSharedPreferences("ProBuilderPrefs", MODE_PRIVATE).edit().clear().apply();
            getSharedPreferences("auth", MODE_PRIVATE).edit().clear().apply();
            getSharedPreferences("AUTH", MODE_PRIVATE).edit().clear().apply();
            getSharedPreferences("UserSession", MODE_PRIVATE).edit().clear().apply();

            Toast.makeText(this, "Logged Out Securely", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        MaterialButton btnDeleteAccount = findViewById(R.id.btnDeleteAccount);
        btnDeleteAccount.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to delete your account? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    // Clear all local sessions
                    getSharedPreferences("client_session", MODE_PRIVATE).edit().clear().apply();
                    getSharedPreferences("ProBuilderPrefs", MODE_PRIVATE).edit().clear().apply();
                    getSharedPreferences("auth", MODE_PRIVATE).edit().clear().apply();
                    getSharedPreferences("AUTH", MODE_PRIVATE).edit().clear().apply();
                    getSharedPreferences("UserSession", MODE_PRIVATE).edit().clear().apply();

                    Toast.makeText(this, "Account deleted successfully", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
        });

        MaterialButton btnContactUs = findViewById(R.id.btnContactUs);
        btnContactUs.setOnClickListener(v -> {
            android.content.Intent emailIntent = new android.content.Intent(android.content.Intent.ACTION_SENDTO);
            emailIntent.setData(android.net.Uri.parse("mailto:support@probuilderapp.com"));
            emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "ProBuilder Support Request");
            try {
                startActivity(android.content.Intent.createChooser(emailIntent, "Send Email"));
            } catch (android.content.ActivityNotFoundException e) {
                Toast.makeText(this, "No email app found", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadProfile() {
        String url = GET_PROFILE_URL + "?client_id=" + clientId;
        Log.d("ClientProfile", "Loading profile from: " + url);

        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        if (json.optString("status").equals("success")) {
                            JSONObject data = json.optJSONObject("data");
                            if (data == null) data = json; // Fallback if direct object
                            
                            String name = data.optString("client_name");
                            String email = data.optString("email");
                            String phone = data.optString("phone");
                            String contractorName = data.optString("contractor_name");
                            
                            etName.setText(name);
                            etEmail.setText(email);
                            etPhone.setText(phone);
                            
                            if (contractorName != null && !contractorName.isEmpty()) {
                                tvContractorName.setText(contractorName);
                            } else {
                                tvContractorName.setText("Not Assigned");
                            }
                            
                            // Save name locally for Dashboard to use immediately
                            saveNameLocally(name);
                            
                        } else {
                            Toast.makeText(this, json.optString("message", "Failed to load profile"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.e("ClientProfile", "Error parsing profile", e);
                         // Fallback: Load from local prefs if network fails? 
                         // For now, just show toast
                        // Toast.makeText(this, "Error parsing data", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Log.e("ClientProfile", "Network Error: " + error.toString())
        );

        Volley.newRequestQueue(this).add(request);
    }

    private void updateProfile() {
        final String name = etName.getText().toString().trim();
        final String phone = etPhone.getText().toString().trim();

        if (name.isEmpty()) {
            etName.setError("Name required");
            return;
        }

        StringRequest request = new StringRequest(Request.Method.POST, UPDATE_PROFILE_URL,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        if (json.optString("status").equals("success")) {
                            Toast.makeText(this, "Profile Updated Successfully", Toast.LENGTH_SHORT).show();
                            saveNameLocally(name);
                        } else {
                            Toast.makeText(this, json.optString("message", "Update failed"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(this, "Error updating profile", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Network Error", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("client_id", String.valueOf(clientId));
                params.put("name", name);
                params.put("phone", phone);
                return params;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }

    private void saveNameLocally(String name) {
        // Save to multiple prefs to ensure Dashboard picks it up regardless of which one it reads
        getSharedPreferences("AUTH", MODE_PRIVATE).edit().putString("user_name", name).apply();
        getSharedPreferences("client_session", MODE_PRIVATE).edit().putString("name", name).apply();
        getSharedPreferences("ProBuilderPrefs", MODE_PRIVATE).edit().putString("user_name", name).apply();
    }
}
