package com.example.probuilder;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ClientRegistrationActivity extends AppCompatActivity {

    private TextInputEditText etInviteToken, etName, etEmail, etPhone, etPassword;
    private Button btnRegister;
    private com.google.android.material.textfield.TextInputLayout tilPassword;
    private static final String URL = Constants.BASE_URL + "register_client.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_registration);

        etInviteToken = findViewById(R.id.etInviteToken);
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etRegEmail);
        etPhone = findViewById(R.id.etPhone);
        etPassword = findViewById(R.id.etRegPassword);
        tilPassword = findViewById(R.id.tilPassword);
        btnRegister = findViewById(R.id.btnRegister);
        android.widget.ImageView btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());
        btnRegister.setOnClickListener(v -> registerClient());

        setupErrorClearing();
    }

    private void setupErrorClearing() {
        etPassword.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                tilPassword.setError(null);
            }
            @Override public void afterTextChanged(android.text.Editable s) {}
        });
    }

    private void registerClient() {
        String tokenInput = etInviteToken.getText().toString().trim();
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (tokenInput.isEmpty() || name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        tilPassword.setError(null);

        // Strict Password Validation
        if (password.length() < 8) {
            tilPassword.setError("Password must be at least 8 characters");
            return;
        }
        if (!password.matches(".*[0-9].*")) {
            tilPassword.setError("Password must contain at least one number");
            return;
        }
        if (!password.matches(".*[a-zA-Z].*")) {
            tilPassword.setError("Password must contain at least one letter");
            return;
        }
        if (!password.matches(".*[@#$%^&+=!].*")) {
            tilPassword.setError("Password must contain special characters (@#$%^&+=!)");
            return;
        }

        // NO contractor checks here - Client is anonymous
        
        // Logic to extract token if user pasted full URL
        // Example: https://app.com/register?token=XYZ
        String finalToken = tokenInput;
        if (tokenInput.contains("token=")) {
            String[] parts = tokenInput.split("token=");
            if (parts.length > 1) {
                finalToken = parts[1].split("&")[0]; // Take value after token= and before &
            }
        }
        
        // Log for debugging
        Log.d("ClientReg", "Token: " + finalToken);

        btnRegister.setEnabled(false);
        String finalTokenForRequest = finalToken;

        StringRequest request = new StringRequest(Request.Method.POST, URL,
                response -> {
                    btnRegister.setEnabled(true);
                    android.util.Log.d("CLIENT_LOGIN_RAW", response);
                    Log.d("ClientReg", "Response: " + response);
                    try {
                        // Response might be JSON now
                        JSONObject json = new JSONObject(response);
                        if (json.optString("status").equals("success")) {
                            Toast.makeText(this, "Registration Successful!", Toast.LENGTH_LONG).show();
                            
                            // Auto Login Logic
                            int clientId = json.getInt("client_id");
                            int projectId = json.optInt("project_id", -1); // Retrieve project_id
                            
                            // Name is not returned by API, use local variable
                            String clientName = name; 

                            Intent intent = new Intent(ClientRegistrationActivity.this, ClientDashboardActivity.class);
                            intent.putExtra("USER_NAME", clientName);
                            
                            // Save Session (CLEAN NEW FILE: client_session)
                            android.content.SharedPreferences prefs = getSharedPreferences("client_session", MODE_PRIVATE);
                            prefs.edit()
                                    .putInt("client_id", clientId)
                                    .putInt("project_id", projectId)
                                    .putString("client_name", clientName)
                                    .putString("client_token", finalTokenForRequest)
                                    .apply();

                            // VERIFY IMMEDIATELY
                            int savedId = prefs.getInt("client_id", 0);
                            Log.d("CLIENT_SESSION", "Saved client_id = " + savedId);

                            startActivity(intent);
                            finishAffinity(); // Clear stack
                        } else {
                            // Check for error messages
                            String msg = json.optString("message", "Registration Failed");
                            if ("invalid_token".equals(msg)) msg = "Invalid or Expired Invite Token";
                            else if ("token_used".equals(msg)) msg = "Invite Token Already Used";
                            else if ("email_exists".equals(msg)) msg = "Email already registered";
                            
                            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        // Fallback for plain text response just in case
                         if (response.contains("success")) {
                             Toast.makeText(this, "Registered! Please Login.", Toast.LENGTH_LONG).show();
                             finish();
                         } else {
                             Toast.makeText(this, "Error: " + response, Toast.LENGTH_LONG).show();
                         }
                    }
                },
                error -> {
                    btnRegister.setEnabled(true);
                    String body = "";
                    int statusCode = 0;
                    if (error.networkResponse != null) {
                        statusCode = error.networkResponse.statusCode;
                        try {
                            body = new String(error.networkResponse.data, "UTF-8");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    Log.e("ClientReg", "Network Error: " + statusCode + " " + body);
                    Toast.makeText(this, "Error: " + body, Toast.LENGTH_LONG).show();
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("api_token", finalTokenForRequest);
                params.put("name", name);
                params.put("email", email);
                params.put("phone", phone);
                params.put("password", password);
                return params;
            }
        };
        
        Volley.newRequestQueue(this).add(request);
    }
}
