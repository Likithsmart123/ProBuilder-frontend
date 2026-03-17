package com.example.probuilder;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
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

public class OtpVerificationActivity extends AppCompatActivity {

    private String email;
    private EditText otp1, otp2, otp3, otp4, otp5, otp6;
    private Button btnVerify, btnResend;
    private TextView tvTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verification);

        email = getIntent().getStringExtra("email");

        otp1 = findViewById(R.id.otpBox1);
        otp2 = findViewById(R.id.otpBox2);
        otp3 = findViewById(R.id.otpBox3);
        otp4 = findViewById(R.id.otpBox4);
        otp5 = findViewById(R.id.otpBox5);
        otp6 = findViewById(R.id.otpBox6);

        btnVerify = findViewById(R.id.btnVerifyOtp);
        btnResend = findViewById(R.id.btnResendOtp);
        tvTimer = findViewById(R.id.tvTimer);
        android.widget.ImageView btnBack = findViewById(R.id.btnBack);

        TextView tvEmail = findViewById(R.id.tvEmailLabel);
        tvEmail.setText("Enter the 6-digit code sent to " + email);

        setupOtpInputs();

        btnVerify.setOnClickListener(v -> verifyOtp());
        btnResend.setOnClickListener(v -> resendOtp());
        btnBack.setOnClickListener(v -> finish());

        startTimer();
    }

    private void setupOtpInputs() {
        otp1.addTextChangedListener(new OtpTextWatcher(otp1, otp2));
        otp2.addTextChangedListener(new OtpTextWatcher(otp2, otp3));
        otp3.addTextChangedListener(new OtpTextWatcher(otp3, otp4));
        otp4.addTextChangedListener(new OtpTextWatcher(otp4, otp5));
        otp5.addTextChangedListener(new OtpTextWatcher(otp5, otp6));
        otp6.addTextChangedListener(new OtpTextWatcher(otp6, null));

        otp2.setOnKeyListener(new OtpKeyListener(otp1));
        otp3.setOnKeyListener(new OtpKeyListener(otp2));
        otp4.setOnKeyListener(new OtpKeyListener(otp3));
        otp5.setOnKeyListener(new OtpKeyListener(otp4));
        otp6.setOnKeyListener(new OtpKeyListener(otp5));
    }

    private class OtpTextWatcher implements android.text.TextWatcher {
        private final EditText current, next;

        public OtpTextWatcher(EditText current, EditText next) {
            this.current = current;
            this.next = next;
        }

        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
        @Override
        public void afterTextChanged(android.text.Editable s) {
            if (s.length() == 1 && next != null) {
                next.requestFocus();
            }
        }
    }

    private class OtpKeyListener implements android.view.View.OnKeyListener {
        private final EditText previous;

        public OtpKeyListener(EditText previous) {
            this.previous = previous;
        }

        @Override
        public boolean onKey(android.view.View v, int keyCode, android.view.KeyEvent event) {
            if (keyCode == android.view.KeyEvent.KEYCODE_DEL && event.getAction() == android.view.KeyEvent.ACTION_DOWN) {
                EditText current = (EditText) v;
                if (current.getText().length() == 0 && previous != null) {
                    previous.requestFocus();
                    previous.setText(""); // Clear previous on backspace for better UX
                    return true;
                }
            }
            return false;
        }
    }

    private void startTimer() {
        btnResend.setEnabled(false);
        new CountDownTimer(60000, 1000) {
            public void onTick(long millisUntilFinished) {
                tvTimer.setText("Resend in " + millisUntilFinished / 1000 + "s");
            }

            public void onFinish() {
                tvTimer.setText("Did not receive code?");
                btnResend.setEnabled(true);
            }
        }.start();
    }

    private void verifyOtp() {
        String otp = otp1.getText().toString().trim() +
                     otp2.getText().toString().trim() +
                     otp3.getText().toString().trim() +
                     otp4.getText().toString().trim() +
                     otp5.getText().toString().trim() +
                     otp6.getText().toString().trim();

        if (otp.length() != 6) {
            Toast.makeText(this, "Please enter all 6 digits", Toast.LENGTH_SHORT).show();
            return;
        }
        btnVerify.setEnabled(false);

        String url = Constants.BASE_URL + "verify_otp.php";

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        if (json.optString("status").equals("success")) {
                            Toast.makeText(this, "Verified!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(this, ResetPasswordActivity.class);
                            intent.putExtra("email", email);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(this, json.optString("message"), Toast.LENGTH_SHORT).show();
                            btnVerify.setEnabled(true);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        btnVerify.setEnabled(true);
                    }
                },
                error -> {
                    Toast.makeText(this, "Network Error", Toast.LENGTH_SHORT).show();
                    btnVerify.setEnabled(true);
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("email", email);
                params.put("otp", otp);
                return params;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }

    private void resendOtp() {
        btnResend.setEnabled(false);
        String url = Constants.BASE_URL + "request_otp.php";

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    Toast.makeText(this, "OTP Resent!", Toast.LENGTH_SHORT).show();
                    startTimer();
                },
                error -> {
                    Toast.makeText(this, "Failed to resend", Toast.LENGTH_SHORT).show();
                    btnResend.setEnabled(true);
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("email", email);
                return params;
            }
        };
        Volley.newRequestQueue(this).add(request);
    }
}
