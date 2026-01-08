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
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONObject;

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

        projectAdapter = new ProjectAdapter(); // Simplified constructor
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
        String url = "http://10.0.2.2:5000/projects?contractor_id=1"; 

        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        List<Project> newProjects = new ArrayList<>();
                        JSONObject jsonObject = new JSONObject(response);
                        JSONArray jsonArray = jsonObject.getJSONArray("projects");

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject obj = jsonArray.getJSONObject(i);
                            Project project = new Project(
                                    obj.getInt("id"),
                                    obj.getString("project_name"),
                                    obj.getString("location"),
                                    obj.getString("client_name"),
                                    obj.getString("client_phone"),
                                    obj.getString("start_date"),
                                    obj.getString("end_date"),
                                    obj.getString("status")
                            );
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
