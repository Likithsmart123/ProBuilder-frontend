package com.example.probuilder;

import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

public class ClientProjectDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_project_detail);

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());
        
        // Retrieve data
        String name = getIntent().getStringExtra("PROJECT_NAME");
        String location = getIntent().getStringExtra("PROJECT_LOCATION");
        String start = getIntent().getStringExtra("PROJECT_START");
        String end = getIntent().getStringExtra("PROJECT_END");
        String status = getIntent().getStringExtra("PROJECT_STATUS");
        int progress = getIntent().getIntExtra("PROJECT_PROGRESS", 0);

        // Populate Views
        ((android.widget.TextView) findViewById(R.id.tvProjectName)).setText(name);
        ((android.widget.TextView) findViewById(R.id.tvLocation)).setText("Location: " + location);
        ((android.widget.TextView) findViewById(R.id.tvStartDate)).setText(start);
        ((android.widget.TextView) findViewById(R.id.tvEndDate)).setText(end);
        ((android.widget.TextView) findViewById(R.id.tvStatus)).setText("Status: " + status);
        
        android.widget.ProgressBar progressBar = findViewById(R.id.progressBar);
        progressBar.setProgress(progress);
        
        ((android.widget.TextView) findViewById(R.id.tvStatus)).setTextColor(
            status.equalsIgnoreCase("Started") ? android.graphics.Color.parseColor("#2E7D32") : android.graphics.Color.parseColor("#1E6FE3")
        );
    }
}
