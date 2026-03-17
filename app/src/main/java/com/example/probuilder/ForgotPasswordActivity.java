package com.example.probuilder;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText etEmail;
    private Button btnSendOtp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        etEmail = findViewById(R.id.etEmailRaw);
        btnSendOtp = findViewById(R.id.btnResetPassword); // Reusing existing ID, rename text in layout if needed
        android.widget.ImageView btnBack = findViewById(R.id.btnBack);

        btnSendOtp.setText("Send OTP");

        btnBack.setOnClickListener(v -> finish());
        btnSendOtp.setOnClickListener(v -> requestOtp());
    }

    private void requestOtp() {
        btnSendOtp.setEnabled(false);
        String email = etEmail.getText().toString().trim();

        if (email.isEmpty()) {
            Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show();
            btnSendOtp.setEnabled(true);
            return;
        }

        String url = Constants.BASE_URL + "request_otp.php";

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        Log.d("OTP_DEBUG", "Response: " + response);
                        JSONObject json = new JSONObject(response);
                        if (json.optString("status").equals("success")) {
                            Toast.makeText(this, "OTP Sent!", Toast.LENGTH_SHORT).show();
                            
                            // Navigate to OTP Verification
                            Intent intent = new Intent(ForgotPasswordActivity.this, OtpVerificationActivity.class);
                            intent.putExtra("email", email);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(this, json.optString("message", "Failed to send OTP"), Toast.LENGTH_SHORT).show();
                            btnSendOtp.setEnabled(true);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "JSON Error: " + response, Toast.LENGTH_SHORT).show();
                        btnSendOtp.setEnabled(true);
                    }
                },
                error -> {
                    Toast.makeText(this, "Network Error", Toast.LENGTH_SHORT).show();
                    btnSendOtp.setEnabled(true);
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("email", email);
                return params;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(30000, 1, 1.0f));
        Volley.newRequestQueue(this).add(request);
    }
}
