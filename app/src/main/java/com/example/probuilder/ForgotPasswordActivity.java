package com.example.probuilder;

import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText etEmailRaw;
    private Button btnSendResetLink;

    // Assumed endpoint based on ResetPasswordActivity's URL structure
    private static final String URL = "http://10.0.2.2:5000/forgot_password";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        etEmailRaw = findViewById(R.id.etEmailRaw);
        btnSendResetLink = findViewById(R.id.btnSendResetLink);

        btnSendResetLink.setOnClickListener(v -> sendResetLink());
    }

    private void sendResetLink() {
        String email = etEmailRaw.getText().toString().trim();

        if (email.isEmpty()) {
            Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSendResetLink.setEnabled(false);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL,
                response -> {
                    btnSendResetLink.setEnabled(true);
                    if (response.trim().equals("success")) {
                        Toast.makeText(ForgotPasswordActivity.this, "Reset link sent to your email.", Toast.LENGTH_LONG).show();
                        finish(); // Close activity on success
                    } else {
                        Toast.makeText(ForgotPasswordActivity.this, "Failed: " + response, Toast.LENGTH_LONG).show();
                    }
                },
                error -> {
                    btnSendResetLink.setEnabled(true);
                    Toast.makeText(ForgotPasswordActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("email", email);
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }
}