package com.example.probuilder;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.textfield.TextInputEditText;

public class ProfileActivity extends AppCompatActivity {

    private TextInputEditText etName, etEmail, etPhone, etRole, etJoinedDate;
    private TextView tvProfileName, tvProfileEmail;

    private int userId;
    private static final String GET_PROFILE_URL = Constants.BASE_URL + "get_profile.php";
    private static final String UPDATE_PROFILE_URL = Constants.BASE_URL + "update_profile.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etRole = findViewById(R.id.etRole);
        etJoinedDate = findViewById(R.id.etJoinedDate);
        
        tvProfileName = findViewById(R.id.tvProfileName);
        tvProfileEmail = findViewById(R.id.tvProfileEmail);

        // Get User ID from SharedPreferences
        android.content.SharedPreferences sp = getSharedPreferences("ProBuilderPrefs", MODE_PRIVATE);
        userId = sp.getInt("contractor_id", -1);

        if (userId == -1) {
            Toast.makeText(this, "Session Invalid", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadProfile();

        Button btnUpdateProfile = findViewById(R.id.btnUpdateProfile);
        btnUpdateProfile.setOnClickListener(v -> updateProfile());

        Button btnContactUs = findViewById(R.id.btnContactUs);
        btnContactUs.setOnClickListener(v -> {
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
            emailIntent.setData(Uri.parse("mailto:support@probuilderapp.com"));
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "ProBuilder Support Request");
            try {
                startActivity(Intent.createChooser(emailIntent, "Send Email"));
            } catch (android.content.ActivityNotFoundException e) {
                Toast.makeText(ProfileActivity.this, "No email app found", Toast.LENGTH_SHORT).show();
            }
        });

        Button btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> {
            // Clear user session/data here
            sp.edit().clear().apply();
            Toast.makeText(ProfileActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
            
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void loadProfile() {
        String url = GET_PROFILE_URL + "?user_id=" + userId;
        
        com.android.volley.toolbox.StringRequest request = new com.android.volley.toolbox.StringRequest(
                com.android.volley.Request.Method.GET, 
                url,
                response -> {
                    try {
                        org.json.JSONObject json = new org.json.JSONObject(response);
                        if (json.optString("status").equals("success")) {
                            org.json.JSONObject profile = json.getJSONObject("profile");
                            
                            String name = profile.optString("name");
                            String email = profile.optString("email");
                            String phone = profile.optString("phone");
                            String role = profile.optString("role");
                            String joinedDate = profile.optString("created_at");
                            
                            etName.setText(name);
                            etEmail.setText(email);
                            etPhone.setText(phone);
                            etRole.setText(role);
                            etJoinedDate.setText(joinedDate);
                            
                            tvProfileName.setText(name);
                            tvProfileEmail.setText(email);
                            
                            // Read-only fields
                            etEmail.setEnabled(false);
                            etRole.setEnabled(false);
                            etJoinedDate.setEnabled(false);
                            
                        } else {
                            Toast.makeText(this, json.optString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(this, "Error parsing profile", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Network Error", Toast.LENGTH_SHORT).show()
        );
        
        com.android.volley.toolbox.Volley.newRequestQueue(this).add(request);
    }

    private void updateProfile() {
        final String name = etName.getText().toString().trim();
        final String phone = etPhone.getText().toString().trim();

        if (name.isEmpty()) {
            etName.setError("Name required");
            return;
        }

        com.android.volley.toolbox.StringRequest request = new com.android.volley.toolbox.StringRequest(
                com.android.volley.Request.Method.POST,
                UPDATE_PROFILE_URL,
                response -> {
                    try {
                        org.json.JSONObject json = new org.json.JSONObject(response);
                        if (json.optString("status").equals("success")) {
                            Toast.makeText(this, "Profile Updated", Toast.LENGTH_SHORT).show();
                            tvProfileName.setText(name);
                            
                            // Update SharedPreferences so Dashboard can update
                            getSharedPreferences("ProBuilderPrefs", MODE_PRIVATE)
                                .edit()
                                .putString("user_name", name)
                                .apply();
                                
                        } else {
                            Toast.makeText(this, json.optString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(this, "Error updating profile", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Network Error", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected java.util.Map<String, String> getParams() {
                java.util.Map<String, String> params = new java.util.HashMap<>();
                params.put("user_id", String.valueOf(userId));
                params.put("name", name);
                params.put("phone", phone);
                return params;
            }
        };

        com.android.volley.toolbox.Volley.newRequestQueue(this).add(request);
    }
}
