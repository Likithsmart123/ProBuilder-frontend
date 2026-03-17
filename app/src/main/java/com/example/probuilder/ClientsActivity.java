package com.example.probuilder;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ClientsActivity extends AppCompatActivity {

    private RecyclerView rvClients;
    private ClientAdapter clientAdapter;
    private static final String GET_CLIENTS_URL  = Constants.BASE_URL + "get_clients.php";
    private static final String GET_PROJECTS_URL = Constants.BASE_URL + "get_projects.php";
    private int contractorId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clients);

        android.content.SharedPreferences prefs = getSharedPreferences("ProBuilderPrefs", MODE_PRIVATE);
        contractorId = prefs.getInt("contractor_id", -1);

        Log.d("CONTRACTOR_CHECK", "Contractor ID from SharedPrefs = " + contractorId);
        if (contractorId == -1) {
            Log.e("CONTRACTOR_CHECK", "contractor_id is -1 — Login never saved the value!");
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        rvClients = findViewById(R.id.rvClients);
        rvClients.setLayoutManager(new LinearLayoutManager(this));

        clientAdapter = new ClientAdapter();
        clientAdapter.setOnItemClickListener(client -> {
            Intent intent = new Intent(ClientsActivity.this, ClientDetailsActivity.class);
            intent.putExtra("CLIENT_ID", client.clientId);
            intent.putExtra("NAME", client.name);
            intent.putExtra("EMAIL", client.email);
            intent.putExtra("PHONE", client.phone);
            startActivity(intent);
        });
        rvClients.setAdapter(clientAdapter);

        EditText etSearch = findViewById(R.id.etSearch);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                clientAdapter.filter(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        FloatingActionButton fabAddClient = findViewById(R.id.fabAddClient);
        fabAddClient.setOnClickListener(v ->
                startActivity(new Intent(ClientsActivity.this, AddClientActivity.class)));

        loadClients();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadClients();
    }

    private void loadClients() {
        String url = GET_CLIENTS_URL + "?contractor_id=" + contractorId;
        Log.d("ClientsActivity", "Loading clients from: " + url);

        ProgressBar progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);

        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    progressBar.setVisibility(View.GONE);
                    Log.d("ClientsActivity", "Raw Response: " + response);
                    List<Client> clientList = new ArrayList<>();
                    try {
                        org.json.JSONObject root = new org.json.JSONObject(response);

                        if (!root.optString("status").equals("success")) {
                            Toast.makeText(this, "Failed to load clients: " + root.optString("message"), Toast.LENGTH_SHORT).show();
                            return;
                        }

                        org.json.JSONArray array = root.optJSONArray("clients");
                        if (array == null) {
                            clientAdapter.setClients(new ArrayList<>());
                            return;
                        }

                        for (int i = 0; i < array.length(); i++) {
                            org.json.JSONObject obj = array.getJSONObject(i);
                            Client client = new Client();
                            client.clientId = obj.optInt("client_id", -1);
                            client.name     = obj.optString("name", "Unknown");
                            client.email    = obj.optString("email", "");
                            client.phone    = obj.optString("phone", "");
                            clientList.add(client);
                        }

                        // ── Frontend-only filter: show only clients with at least one project ──
                        filterClientsByProjects(clientList);

                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e("ClientsActivity", "Parsing error: " + e.getMessage());
                        Toast.makeText(this, "Error parsing client data.", Toast.LENGTH_LONG).show();
                    }
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    String errorMsg = error.getMessage() != null ? error.getMessage() : "Unknown Network Error";
                    Log.e("ClientsActivity", "Volley Error: " + errorMsg);
                    if (error.networkResponse != null) {
                        Log.e("ClientsActivity", "Status Code: " + error.networkResponse.statusCode);
                    }
                    Toast.makeText(this, "Failed to load clients: " + errorMsg, Toast.LENGTH_LONG).show();
                });

        Volley.newRequestQueue(this).add(request);
    }

    /**
     * Calls get_projects.php (with the stored api_token as Authorization header),
     * collects the set of client IDs that appear in at least one project,
     * then filters the full client list to only those — purely frontend, no backend change.
     * Falls back to showing all clients if the projects call fails.
     */
    private void filterClientsByProjects(final List<Client> allClients) {
        final String apiToken = getSharedPreferences("contractor_session", MODE_PRIVATE)
                .getString("api_token", "");

        StringRequest projectsRequest = new StringRequest(Request.Method.GET, GET_PROJECTS_URL,
                response -> {
                    Set<Integer> clientIdsWithProjects = new HashSet<>();
                    try {
                        org.json.JSONObject root = new org.json.JSONObject(response);
                        org.json.JSONArray projects = root.optJSONArray("projects");
                        if (projects != null) {
                            for (int i = 0; i < projects.length(); i++) {
                                org.json.JSONObject project = projects.getJSONObject(i);
                                org.json.JSONObject clientObj = project.optJSONObject("client");
                                if (clientObj != null) {
                                    int cId = clientObj.optInt("id", -1);
                                    if (cId > 0) {
                                        clientIdsWithProjects.add(cId);
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        Log.e("ClientsActivity", "Project filter parse error: " + e.getMessage());
                        // Fallback: show all clients if parsing fails
                        clientAdapter.setClients(allClients);
                        return;
                    }

                    // Keep only clients whose ID appears in at least one project
                    List<Client> filtered = new ArrayList<>();
                    for (Client c : allClients) {
                        if (clientIdsWithProjects.contains(c.clientId)) {
                            filtered.add(c);
                        }
                    }

                    Log.d("ClientsActivity",
                            "Total clients: " + allClients.size()
                            + " | Clients with projects: " + filtered.size());

                    clientAdapter.setClients(filtered);
                },
                error -> {
                    // Fallback: if projects API fails, show all clients
                    Log.e("ClientsActivity", "Projects fetch failed (filter fallback): " + error.toString());
                    clientAdapter.setClients(allClients);
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", apiToken);
                return headers;
            }
        };

        Volley.newRequestQueue(this).add(projectsRequest);
    }

    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }
}