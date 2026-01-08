package com.example.probuilder;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class ClientProjectListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_project_list);

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        RecyclerView rvProjects = findViewById(R.id.rvProjects);
        rvProjects.setLayoutManager(new LinearLayoutManager(this));

        // Mock Data
        List<Project> projects = new ArrayList<>();
        projects.add(new Project(1, "Villa Construction", "Whitefield", "Client A", "9999999999", "Jan 1, 2026", "Dec 31, 2026", "Started"));
        projects.add(new Project(2, "Office Renovation", "Indiranagar", "Client A", "9999999999", "Feb 15, 2026", "Aug 15, 2026", "Planning"));

        ClientProjectAdapter adapter = new ClientProjectAdapter(projects, project -> {
            Intent intent = new Intent(this, ClientProjectDetailActivity.class);
            intent.putExtra("PROJECT_NAME", project.getName());
            intent.putExtra("PROJECT_LOCATION", project.getLocation());
            intent.putExtra("PROJECT_START", project.getStartDate());
            intent.putExtra("PROJECT_END", project.getEndDate());
            intent.putExtra("PROJECT_STATUS", project.getStatus());
            intent.putExtra("PROJECT_PROGRESS", project.getProgress());
            startActivity(intent);
        });
        rvProjects.setAdapter(adapter);
    }
}
