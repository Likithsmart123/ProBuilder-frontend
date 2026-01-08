package com.example.probuilder;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;

import java.util.HashMap;
import java.util.Map;

public class InviteClientActivity extends AppCompatActivity {

    private TextInputEditText etClientName, etClientEmail, etClientPhone;
    private Button btnCreateClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_client);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        etClientName = findViewById(R.id.etClientName);
        etClientEmail = findViewById(R.id.etClientEmail);
        etClientPhone = findViewById(R.id.etClientPhone);
        btnCreateClient = findViewById(R.id.btnCreateClient);

        btnCreateClient.setOnClickListener(v -> addClient());
    }

    private void addClient() {
        String name = etClientName.getText() != null ? etClientName.getText().toString().trim() : "";
        String email = etClientEmail.getText() != null ? etClientEmail.getText().toString().trim() : "";
        String phone = etClientPhone.getText() != null ? etClientPhone.getText().toString().trim() : "";

        if (name.isEmpty() || email.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "All fields required", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("ADD_CLIENT_DEBUG", name + " | " + email + " | " + phone);

        String url = "http://10.0.2.2:5000/invite-client";

        StringRequest request = new StringRequest(
                Request.Method.POST,
                url,
                response -> {
                    Log.d("ADD_CLIENT_RESPONSE", response);
                    if (response.trim().equals("success")) {
                        Toast.makeText(this, "Client added successfully", Toast.LENGTH_SHORT).show();
                        
                        // Launch Success Screen to allow copying/sharing link
                        android.content.Intent intent = new android.content.Intent(InviteClientActivity.this, InviteSuccessActivity.class);
                        // Generate a mock unique link since backend helper doesn't return one yet
                        String mockLink = "https://probuilder.com/invite/" + System.currentTimeMillis(); 
                        intent.putExtra("INVITE_LINK", mockLink);
                        startActivity(intent);
                        
                        finish(); // Close this activity so back button from Success screen goes to Lists
                    } else {
                        Toast.makeText(this, response, Toast.LENGTH_LONG).show();
                    }
                },
                error -> {
                    Toast.makeText(this, "Network error", Toast.LENGTH_LONG).show();
                    error.printStackTrace();
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("name", name);
                params.put("email", email);
                params.put("phone", phone);
                params.put("contractor_id", "1"); // logged-in contractor
                return params;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }

    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }
}