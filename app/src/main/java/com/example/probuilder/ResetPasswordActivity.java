package com.example.probuilder;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ResetPasswordActivity extends AppCompatActivity {

    private String email;
    private EditText etPass, etConfirm;
    private Button btnReset;

    private com.google.android.material.textfield.TextInputLayout tilNewPass, tilConfirmPass; // Declare TextInputLayouts

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password_otp);

        email = getIntent().getStringExtra("email");
        
        android.widget.ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        tilNewPass = findViewById(R.id.tilNewPass); // Initialize TILs
        tilConfirmPass = findViewById(R.id.tilConfirmPass);
        etPass = findViewById(R.id.etNewPassword);
        etConfirm = findViewById(R.id.etConfirmPassword);
        btnReset = findViewById(R.id.btnSubmitReset);

        btnReset.setOnClickListener(v -> submitReset());

        // Add TextWatchers to clear errors
        android.text.TextWatcher clearErrorWatcher = new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tilNewPass.setError(null);
                tilConfirmPass.setError(null);
            }
            @Override
            public void afterTextChanged(android.text.Editable s) {}
        };
        etPass.addTextChangedListener(clearErrorWatcher);
        etConfirm.addTextChangedListener(clearErrorWatcher);

        etConfirm.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {
                submitReset();
                return true;
            }
            return false;
        });
    }

    private void submitReset() {
        String pass = etPass.getText().toString().trim();
        String confirm = etConfirm.getText().toString().trim();

        // Clear previous errors first
        tilNewPass.setError(null);
        tilConfirmPass.setError(null);

        if (pass.isEmpty()) {
            tilNewPass.setError("Password cannot be empty");
            return;
        }
        if (!pass.equals(confirm)) {
            tilConfirmPass.setError("Passwords do not match");
            return;
        }

        // Strict Password Validation
        if (pass.length() < 8) {
            tilNewPass.setError("Password must be at least 8 characters");
            return;
        }
        if (!pass.matches(".*[0-9].*")) {
            tilNewPass.setError("Password must contain at least one number");
            return;
        }
        if (!pass.matches(".*[a-z].*")) {
            tilNewPass.setError("Password must contain at least one lowercase letter");
            return;
        }
        if (!pass.matches(".*[A-Z].*")) {
            tilNewPass.setError("Password must contain at least one uppercase letter");
            return;
        }
        if (!pass.matches(".*[@#$%^&+=!\\-_?].*")) {
            tilNewPass.setError("Password must contain at least one special character");
            return;
        }

        btnReset.setEnabled(false);

        String url = Constants.BASE_URL + "reset_password.php";

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        if (json.optString("status").equals("success")) {
                            Toast.makeText(this, "Password Updated! Please Login.", Toast.LENGTH_LONG).show();
                            
                            // Go back to Login (Clear backstack)
                            Intent intent = new Intent(this, LoginActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            String msg = json.optString("message");
                            if (msg.toLowerCase().contains("match")) {
                                tilConfirmPass.setError(msg);
                            } else {
                                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                            }
                            btnReset.setEnabled(true);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        btnReset.setEnabled(true);
                    }
                },
                error -> {
                    Toast.makeText(this, "Network Error", Toast.LENGTH_SHORT).show();
                    btnReset.setEnabled(true);
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("email", email);
                params.put("new_password", pass);
                return params;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }
}