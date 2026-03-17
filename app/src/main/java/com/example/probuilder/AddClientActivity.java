package com.example.probuilder;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class AddClientActivity extends AppCompatActivity {

    private TextInputEditText etName, etEmail, etPhone, etAddress;
    private MaterialButton btnSave;
    private static final String ADD_CLIENT_URL = Constants.BASE_URL + "add_client.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_client);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        etName = findViewById(R.id.etClientName);
        etEmail = findViewById(R.id.etClientEmail);
        etPhone = findViewById(R.id.etClientPhone);
        etAddress = findViewById(R.id.etClientAddress);
        btnSave = findViewById(R.id.btnSaveClient);

        btnSave.setOnClickListener(v -> saveClient());
    }

    private void saveClient() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String address = etAddress.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        android.content.SharedPreferences sp = getSharedPreferences("ProBuilderPrefs", MODE_PRIVATE);
        // Fallback to UserSession if not found (handling legacy prefs)
        int contractorId = sp.getInt("contractor_id", -1);
        if (contractorId == -1) {
             android.content.SharedPreferences sp2 = getSharedPreferences("UserSession", MODE_PRIVATE);
             contractorId = sp2.getInt("contractor_id", -1);
        }

        if (contractorId == -1) {
            Toast.makeText(this, "Session Expired. Please Login.", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSave.setEnabled(false);
        final int finalCid = contractorId;

        StringRequest request = new StringRequest(Request.Method.POST, ADD_CLIENT_URL,
                response -> {
                    btnSave.setEnabled(true);
                    Log.d("AddClient", "Response: " + response);
                    
                    try {
                        JSONObject json = new JSONObject(response);
                        if (json.optString("status").equals("success")) {
                            Toast.makeText(this, "Client Added Successfully", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            String msg = json.optString("message", "Failed to add client");
                            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        // Fallback if plain text
                        if (response.toLowerCase().contains("success")) {
                             Toast.makeText(this, "Client Added Successfully", Toast.LENGTH_SHORT).show();
                             finish();
                        } else {
                             Toast.makeText(this, "Error: " + response, Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                error -> {
                    btnSave.setEnabled(true);
                    Log.e("AddClient", "Error: " + error.toString());
                    Toast.makeText(this, "Network Error", Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("contractor_id", String.valueOf(finalCid));
                params.put("name", name);
                params.put("email", email);
                params.put("phone", phone);
                params.put("address", address);
                return params;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }
}
