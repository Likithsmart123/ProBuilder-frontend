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
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ActiveProjectsActivity extends AppCompatActivity {

    private ProjectAdapter projectAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_active_projects);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        RecyclerView rvProjects = findViewById(R.id.rvActiveProjects);
        rvProjects.setLayoutManager(new LinearLayoutManager(this));

        projectAdapter = new ProjectAdapter(); 
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
        fabAddProject.setOnClickListener(v -> startActivity(new Intent(this, CreateProjectActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProjects();
    }

    private void loadProjects() {
        int contractorId = getSharedPreferences("AUTH", MODE_PRIVATE).getInt("contractor_id", -1);
        String url = Constants.ACTIVE_PROJECTS_URL + "projects?contractor_id=" + contractorId;

        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        List<Project> newProjects = new ArrayList<>();
                        JSONObject jsonObject = new JSONObject(response);
                        JSONArray jsonArray = jsonObject.getJSONArray("projects");

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject obj = jsonArray.getJSONObject(i);
                            // Parse nested client object
                            JSONObject clientObj = obj.optJSONObject("client");
                            String clientName = "Unknown";
                            String clientPhone = "";
                            
                            if (clientObj != null) {
                                clientName = clientObj.optString("name", "Unknown");
                                clientPhone = clientObj.optString("phone", "");
                            }

                            Project project = new Project();
                            project.projectId = obj.getInt("id");
                            project.title = obj.optString("title", "Unknown Project");
                            project.status = obj.optString("status", "Pending");
                            project.overallProgress = obj.optInt("overall_progress", 0);
                            project.startDate = obj.optString("start_date", "");
                            project.endDate = obj.optString("end_date", "");
                            
                            project.client = new Client();
                            project.client.name = clientName;
                            project.client.phone = clientPhone;
                            
                            newProjects.add(project);
                        }
                        projectAdapter.setProjects(newProjects);

                    } catch (Exception e) {
                        Log.e("ProjectsActivity", "JSON Parsing Error: " + e.getMessage());
                        Toast.makeText(this, "Error parsing project data.", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("ProjectsActivity", "Volley Error: " + error.toString());
                    Toast.makeText(this, "Failed to load projects from server.", Toast.LENGTH_SHORT).show();
                });

        Volley.newRequestQueue(this).add(request);
    }

    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }
}
