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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InviteClientActivity extends AppCompatActivity {

    private Button btnSelectProject;
    private Button btnGenerateLink;
    private List<ProjectItem> projectList = new ArrayList<>();
    private ProjectItem selectedProject = null;

    private static final String TAG = "InviteClientActivity";

    private boolean isProjectsLoaded = false;

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
        toolbar.setNavigationOnClickListener(v -> finish());

        btnSelectProject = findViewById(R.id.btnSelectProject);
        btnGenerateLink = findViewById(R.id.btnGenerateLink);

        btnSelectProject.setOnClickListener(v -> {
            if (!isProjectsLoaded) {
                Toast.makeText(this, "Loading projects...", Toast.LENGTH_SHORT).show();
                loadProjects();
            } else {
                showProjectSearchDialog();
            }
        });

        // Load immediately
        loadProjects();

        btnGenerateLink.setOnClickListener(v -> generateInvite());
    }


    private void loadProjects() {
        Toast.makeText(this, "Loading projects...", Toast.LENGTH_SHORT).show();
        String url = Constants.BASE_URL + "get_projects_v2.php";
        
        // STEP 2 — ALWAYS READ TOKEN LIKE THIS (EVERY API)
        android.content.SharedPreferences prefs = getSharedPreferences("AUTH", MODE_PRIVATE);
        String token = prefs.getString("token", "");
        
        // STEP 4 — ADD ONE LOG (DEBUG ONLY)
        Log.d("AUTH_DEBUG", "Using token = " + token);

        if (token.isEmpty()) {
             Toast.makeText(this, "Not logged in (Token empty)", Toast.LENGTH_SHORT).show();
             return;
        }

        AuthJsonRequest request = new AuthJsonRequest(
                this,
                Request.Method.GET,
                url,
                null,
                response -> {
                    Log.d("PROJECT_RESPONSE", response.toString());

                    JSONArray projects = response.optJSONArray("projects");
                    if (projects == null) {
                        Log.e("PROJECT_RESPONSE", "No projects array");
                        return;
                    }

                    try {
                        projectList.clear();

                        for (int i = 0; i < projects.length(); i++) {
                            JSONObject obj = projects.getJSONObject(i);

                            ProjectItem p = new ProjectItem();
                            p.id = obj.getInt("project_id");
                            p.title = obj.getString("title");
                            if (obj.has("client_name") && !obj.isNull("client_name")) {
                                p.title += " - " + obj.getString("client_name");
                            } else if (obj.has("location") && !obj.isNull("location")) {
                                p.title += " (" + obj.getString("location") + ")";
                            }

                            projectList.add(p);
                        }

                        isProjectsLoaded = true; // Mark as loaded
                        
                    } catch (Exception e) {
                        Log.e("PROJECT_PARSE_ERROR", "Error parsing projects", e);
                    }
                },
                error -> {
                    Log.e("PROJECT_NET_ERROR", "Volley error", error);
                    Toast.makeText(this, "Failed to load projects", Toast.LENGTH_SHORT).show();
                }
        );

        request.setShouldCache(false);
        MyVolley.get(this).add(request);
    }

    private void showProjectSearchDialog() {
        if (projectList.isEmpty()) {
            Toast.makeText(this, "No projects found", Toast.LENGTH_SHORT).show();
            return;
        }

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Select Project");

        android.view.View dialogView = getLayoutInflater().inflate(R.layout.dialog_client_search, null);
        android.widget.EditText searchInput = dialogView.findViewById(R.id.etClientSearch);
        searchInput.setHint("Search project...");
        
        android.widget.ListView listView = dialogView.findViewById(R.id.lvClients);
        android.widget.ArrayAdapter<ProjectItem> dialogAdapter = new android.widget.ArrayAdapter<>(this, android.R.layout.simple_list_item_1, projectList);
        listView.setAdapter(dialogAdapter);

        builder.setView(dialogView);
        builder.setNegativeButton("Cancel", null);
        
        android.app.AlertDialog dialog = builder.create();

        searchInput.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                dialogAdapter.getFilter().filter(s);
            }
            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        listView.setOnItemClickListener((parent, view, position, id) -> {
            selectedProject = dialogAdapter.getItem(position);
            btnSelectProject.setText(selectedProject.title);
            dialog.dismiss();
        });

        dialog.show();
    }
    
    private void generateInvite() {
        if (selectedProject == null) {
            Toast.makeText(this, "Select a project", Toast.LENGTH_SHORT).show();
            return;
        }

        int selectedProjectId = selectedProject.id;
        btnGenerateLink.setEnabled(false);

        android.content.SharedPreferences sp = getSharedPreferences("UserSession", MODE_PRIVATE);
        int contractorId = sp.getInt("contractor_id", -1);
        if (contractorId == -1) {
             sp = getSharedPreferences("ProBuilderPrefs", MODE_PRIVATE);
             contractorId = sp.getInt("contractor_id", -1);
        }

        if (contractorId == -1) {
             Toast.makeText(this, "Session expired", Toast.LENGTH_SHORT).show();
             btnGenerateLink.setEnabled(true);
             return;
        }
        
        final String cidStr = String.valueOf(contractorId);

        String url = Constants.BASE_URL + "create_invite.php";

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    btnGenerateLink.setEnabled(true);
                    response = response.trim();
                    Log.d("INVITE_RESPONSE", response);

                    if (response.startsWith("success")) {
                        // Extract Token
                        String[] parts = response.split("\\|");
                        String token = (parts.length > 1) ? parts[1] : "error";

                        // Launch Success Screen
                        android.content.Intent intent = new android.content.Intent(InviteClientActivity.this, InviteSuccessActivity.class);
                        String link = Constants.BASE_URL + "client_register.php?token=" + token;
                        intent.putExtra("INVITE_LINK", link);
                        startActivity(intent);
                        finish(); 
                    } else {
                        Toast.makeText(this, "Error: " + response, Toast.LENGTH_LONG).show();
                    }
                },
                error -> {
                    btnGenerateLink.setEnabled(true);
                    Toast.makeText(this, "Network error", Toast.LENGTH_LONG).show();
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("contractor_id", cidStr);
                params.put("project_id", String.valueOf(selectedProjectId));
                return params;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }
}