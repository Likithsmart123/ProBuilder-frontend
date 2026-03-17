package com.example.probuilder;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;



import java.util.ArrayList;
import java.util.List;

public class ProjectsActivity extends AppCompatActivity {

    private ProjectAdapter projectAdapter;
    private List<Project> projectList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_projects);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        RecyclerView rvProjects = findViewById(R.id.rvProjects);
        rvProjects.setLayoutManager(new LinearLayoutManager(this));

        projectAdapter = new ProjectAdapter(); 
        projectAdapter.setOnItemClickListener(project -> {
            Intent intent = new Intent(ProjectsActivity.this, ProjectDetailActivity.class);
            intent.putExtra("PROJECT_ID", project.projectId);
            intent.putExtra("PROJECT_NAME", project.title);
            if (project.client != null) {
                intent.putExtra("CLIENT_PHONE", project.client.phone);
                intent.putExtra("CLIENT_EMAIL", project.client.email);
            }
            // Pass other basic details if available to show immediately
            // intent.putExtra("PROJECT_LOCATION", project.location); // Field removed
            startActivity(intent);
        });
        rvProjects.setAdapter(projectAdapter);

        EditText etSearch = findViewById(R.id.etSearchProjects);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                projectAdapter.filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        FloatingActionButton fabAddProject = findViewById(R.id.fabAddProject);
        fabAddProject.setOnClickListener(v -> {
            startActivity(new Intent(this, CreateProjectActivity.class));
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProjects(); 
    }

    private void loadProjects() {
        ProgressBar progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(android.view.View.VISIBLE);

        android.content.SharedPreferences sp = getSharedPreferences("UserSession", MODE_PRIVATE);
        int contractorId = sp.getInt("contractor_id", -1);
        
        if (contractorId == -1) {
             sp = getSharedPreferences("ProBuilderPrefs", MODE_PRIVATE); // Fallback
             contractorId = sp.getInt("contractor_id", -1);
        }

        if (contractorId == -1) {
            Toast.makeText(this, "Session invalid", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = Constants.BASE_URL + "get_projects.php"; // Corrected URL
        
        // Append client_id if present
        if (getIntent().hasExtra("CLIENT_ID")) {
            int clientId = getIntent().getIntExtra("CLIENT_ID", -1);
            if (clientId != -1) {
                url += "?client_id=" + clientId;
            }
        }
        
        Log.d("ProjectsActivity", "Calling URL: " + url);

        AuthJsonRequest request = new AuthJsonRequest(
                this,
                Request.Method.GET,
                url,
                null,
                response -> {
                    progressBar.setVisibility(android.view.View.GONE);
                    Log.d("PROJECT_LIST_RAW", response.toString()); // MANDATORY LOG

                    try {
                        org.json.JSONObject jsonRoot = response;
                        
                        if (jsonRoot.optString("status").equals("success")) {
                            org.json.JSONArray jsonArray = jsonRoot.getJSONArray("projects");
                            List<Project> newProjects = new ArrayList<>();
                            java.util.Set<Integer> seenIds = new java.util.HashSet<>();

                            for (int i = 0; i < jsonArray.length(); i++) {
                                org.json.JSONObject obj = jsonArray.getJSONObject(i);
                                int pId = obj.getInt("project_id");

                                if (seenIds.contains(pId)) {
                                    Log.w("PROJECTS_DEBUG", "Duplicate project ignored: " + pId);
                                    continue;
                                }
                                seenIds.add(pId);

                                Project project = new Project();
                                project.projectId = pId;
                                project.title = obj.getString("title");
                                project.status = obj.getString("status");
                                project.overallProgress = obj.optInt("overall_progress", 0);
                                
                                // Dates
                                project.startDate = obj.optString("start_date", "");
                                project.endDate = obj.optString("end_date", "");

                                // Client Logic
                                if (!obj.isNull("client")) {
                                    org.json.JSONObject clientObj = obj.getJSONObject("client");
                                    project.client = new Client();
                                    project.client.clientId = clientObj.optInt("id", -1);
                                    project.client.name = clientObj.optString("name", "Unknown");
                                    project.client.phone = clientObj.optString("phone", "");
                                    project.client.email = clientObj.optString("email", "");
                                    
                                    project.clientName = project.client.name; // Flattened for Adapter
                                } else {
                                    project.client = null;
                                    project.clientName = "No client assigned";
                                }

                                newProjects.add(project);
                            }

                            if (newProjects.isEmpty()) {
                                Toast.makeText(this, "No projects found", Toast.LENGTH_SHORT).show();
                            }
                            projectAdapter.setProjects(newProjects);
                        } else {
                            Toast.makeText(this, jsonRoot.optString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Response parse error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                },

                error -> {
                    progressBar.setVisibility(android.view.View.GONE);
                    Log.e("ProjectsActivity", error.toString());
                    Toast.makeText(this, "Failed to load from server", Toast.LENGTH_SHORT).show();
                }
        );

        request.setShouldCache(false);
        MyVolley.get(this).add(request);
    }

    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }
}
