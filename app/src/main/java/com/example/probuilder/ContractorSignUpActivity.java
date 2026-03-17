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
    String SIGNUP_URL = Constants.BASE_URL + "signup.php";

    private com.google.android.material.textfield.TextInputLayout tilPassword, tilConfirmPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contractor_signup);

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        tilPassword = findViewById(R.id.tilPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);
        btnCreateAccount = findViewById(R.id.btnCreateAccount);
        tvLogin = findViewById(R.id.tvLogin);
        android.widget.ImageView btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());
        btnCreateAccount.setOnClickListener(v -> signupUser());

        tvLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, ContractorLoginActivity.class));
            finish(); // Ensure this activity is closed
        });

        setupErrorClearing();
    }

    private void setupErrorClearing() {
        android.text.TextWatcher watcher = new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                tilPassword.setError(null);
                tilConfirmPassword.setError(null);
            }
            @Override public void afterTextChanged(android.text.Editable s) {}
        };
        etPassword.addTextChangedListener(watcher);
        etConfirmPassword.addTextChangedListener(watcher);
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

        tilPassword.setError(null);
        tilConfirmPassword.setError(null);

        if (name.isEmpty() || email.isEmpty() || phone.isEmpty()
                || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            btnCreateAccount.setEnabled(true); // Re-enable button
            return;
        }

        // Strict Password Validation
        if (password.length() < 8) {
            tilPassword.setError("Password must be at least 8 characters");
            btnCreateAccount.setEnabled(true);
            return;
        }
        if (!password.matches(".*[0-9].*")) {
            tilPassword.setError("Password must contain at least one number");
            btnCreateAccount.setEnabled(true);
            return;
        }
        if (!password.matches(".*[a-zA-Z].*")) {
            tilPassword.setError("Password must contain at least one letter");
            btnCreateAccount.setEnabled(true);
            return;
        }
        if (!password.matches(".*[@#$%^&+=!].*")) {
            tilPassword.setError("Password must contain special characters (@#$%^&+=!)");
            btnCreateAccount.setEnabled(true);
            return;
        }

        if (!password.equals(confirmPassword)) {
            tilConfirmPassword.setError("Passwords do not match");
            btnCreateAccount.setEnabled(true); // Re-enable button
            return;
        }

        StringRequest request = new StringRequest(
                Request.Method.POST,
                SIGNUP_URL,
                response -> {
                    String res = response.trim();
                    Log.d("SIGNUP_RESPONSE", res);

                    if (res.equals("success")) {
                        Toast.makeText(this, "Signup successful", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(ContractorSignUpActivity.this, ContractorLoginActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    } else if (res.equals("missing")) {
                        Toast.makeText(this, "Fill all details", Toast.LENGTH_SHORT).show();
                        btnCreateAccount.setEnabled(true);
                    } else if (res.equals("password_mismatch")) {
                        tilConfirmPassword.setError("Passwords do not match");
                        btnCreateAccount.setEnabled(true);
                    } else if (res.equals("email_exists")) {
                        Toast.makeText(this, "Email already exists", Toast.LENGTH_SHORT).show();
                        btnCreateAccount.setEnabled(true);
                    } else {
                        Toast.makeText(this, "Server error", Toast.LENGTH_SHORT).show();
                        btnCreateAccount.setEnabled(true);
                    }
                },
                error -> {
                    Log.e("SIGNUP_ERROR", error.toString());
                    Toast.makeText(this, "Network error", Toast.LENGTH_SHORT).show();
                    btnCreateAccount.setEnabled(true);
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                // Keys must match backend exactly as per user instruction
                params.put("name", name);
                params.put("email", email);
                params.put("phone", phone);
                params.put("password", password);
                params.put("confirm_password", confirmPassword);
                return params;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }
}