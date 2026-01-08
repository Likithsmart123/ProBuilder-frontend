package com.example.probuilder;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ClientsActivity extends AppCompatActivity {

    private ClientAdapter clientAdapter;
    private static final String CLIENTS_URL = "http://10.0.2.2:5000/clients?contractor_id=1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clients);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        RecyclerView rvClients = findViewById(R.id.rvClients);
        rvClients.setLayoutManager(new LinearLayoutManager(this));

        clientAdapter = new ClientAdapter();
        rvClients.setAdapter(clientAdapter);

        EditText etSearch = findViewById(R.id.etSearch);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                clientAdapter.filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        FloatingActionButton fabAddClient = findViewById(R.id.fabAddClient);
        fabAddClient.setOnClickListener(v -> startActivity(new Intent(ClientsActivity.this, InviteClientActivity.class)));

        // Initial load
        loadClients();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadClients();
    }

    private void loadClients() {
        Log.d("ClientsActivity", "Loading clients from: " + CLIENTS_URL);
        // Use StringRequest to handle flexible response types (Object or Array)
        com.android.volley.toolbox.StringRequest request = new com.android.volley.toolbox.StringRequest(
                Request.Method.GET,
                CLIENTS_URL,
                response -> {
                    Log.d("ClientsActivity", "Raw Response: " + response);
                    List<Client> clientList = new ArrayList<>();
                    try {
                        JSONArray array;
                        // Determine if response is Object (wrapped) or Array
                        if (response.trim().startsWith("{")) {
                            JSONObject root = new JSONObject(response);
                            if (root.has("clients")) {
                                array = root.getJSONArray("clients");
                            } else {
                                // Handle case where it might be a single object or different key
                                Log.e("ClientsActivity", "JSON Object found but no 'clients' key");
                                array = new JSONArray(); 
                            }
                        } else {
                            // Assume it's a direct array
                            array = new JSONArray(response);
                        }

                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);
                            // Robust key fetching: check standard keys and "client_" prefixed keys
                            int id = obj.optInt("id", obj.optInt("client_id", -1));
                            String name = obj.optString("name", obj.optString("client_name", "Unknown"));
                            String email = obj.optString("email", obj.optString("client_email", ""));
                            String phone = obj.optString("phone", obj.optString("client_phone", ""));
                            int isUsed = obj.optInt("is_used", 0);
                            
                            clientList.add(new Client(id, name, email, phone, isUsed));
                        }
                        clientAdapter.setClients(clientList);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e("ClientsActivity", "JSON Parsing error: " + e.getMessage(), e);
                        Toast.makeText(this, "Data parsing error. Check logs.", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("ClientsActivity", "Error loading clients", error);
                    String errorMsg = error.getMessage() != null ? error.getMessage() : error.toString();
                    if (error.networkResponse != null) {
                        errorMsg += " Status: " + error.networkResponse.statusCode;
                    }
                    Toast.makeText(this, "Load failed: " + errorMsg, Toast.LENGTH_LONG).show();
                }
        );

        Volley.newRequestQueue(this).add(request);
    }

    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }
}