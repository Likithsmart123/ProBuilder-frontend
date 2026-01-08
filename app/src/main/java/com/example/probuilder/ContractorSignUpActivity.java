package com.example.probuilder;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;

import java.util.HashMap;
import java.util.Map;

public class ContractorSignUpActivity extends AppCompatActivity {

    TextInputEditText etName, etEmail, etPhone, etPassword, etConfirmPassword;
    Button btnCreateAccount;
    TextView tvLogin;

    // Using the new Python backend URL
    String SIGNUP_URL = "http://10.0.2.2:5000/signup";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contractor_signup);

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnCreateAccount = findViewById(R.id.btnCreateAccount);
        tvLogin = findViewById(R.id.tvLogin);

        btnCreateAccount.setOnClickListener(v -> signupUser());

        tvLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, ContractorLoginActivity.class));
            finish(); // Ensure this activity is closed
        });
    }

    private void signupUser() {
        // Disable the button to prevent multiple clicks
        btnCreateAccount.setEnabled(false);

        // Null-safe way to get text to prevent crashes
        String name = etName.getText() != null ? etName.getText().toString().trim() : "";
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String phone = etPhone.getText() != null ? etPhone.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";
        String confirmPassword = etConfirmPassword.getText() != null ? etConfirmPassword.getText().toString().trim() : "";

        if (name.isEmpty() || email.isEmpty() || phone.isEmpty()
                || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            btnCreateAccount.setEnabled(true); // Re-enable button
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            btnCreateAccount.setEnabled(true); // Re-enable button
            return;
        }

        StringRequest request = new StringRequest(Request.Method.POST, SIGNUP_URL,
                response -> {

                    response = response.trim();
                    Log.d("SignupResponse", "Server response: " + response);

                    switch (response) {
                        case "success":
                            Toast.makeText(this, "Account created. Please login.", Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(this, ContractorLoginActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                            break;

                        case "exists":
                            Toast.makeText(this, "Email already registered", Toast.LENGTH_SHORT).show();
                            btnCreateAccount.setEnabled(true); // Re-enable button
                            break;

                        case "empty":
                            Toast.makeText(this, "Missing fields sent to server", Toast.LENGTH_SHORT).show();
                            btnCreateAccount.setEnabled(true); // Re-enable button
                            break;

                        default:
                            Toast.makeText(this, "Signup failed: " + response, Toast.LENGTH_SHORT).show();
                            btnCreateAccount.setEnabled(true); // Re-enable button
                            break;
                    }
                },
                error -> {
                    Log.e("SignupError", "Volley error: " + error);
                    Toast.makeText(this, "Network Error: " + error, Toast.LENGTH_LONG).show();
                    btnCreateAccount.setEnabled(true); // Re-enable button
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("name", name);
                params.put("email", email);
                params.put("phone", phone);
                params.put("password", password);
                return params;
            }
        };

        // Set a longer timeout to prevent TimeoutError
        request.setRetryPolicy(new DefaultRetryPolicy(
                30000, // 30 seconds
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        Volley.newRequestQueue(this).add(request);
    }
}