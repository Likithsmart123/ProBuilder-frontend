package com.example.probuilder;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.List;

public class ScheduleTrackerActivity extends AppCompatActivity {

    private RecyclerView rvSchedule;
    private ScheduleTrackerAdapter adapter;
    private TextView tvOnScheduleCount, tvNeedsAttentionCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_schedule_tracker);

            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
            toolbar.setNavigationOnClickListener(v -> finish());

            tvOnScheduleCount = findViewById(R.id.tvOnScheduleCount);
            tvNeedsAttentionCount = findViewById(R.id.tvNeedsAttentionCount);
            rvSchedule = findViewById(R.id.rvSchedule);
            rvSchedule.setLayoutManager(new LinearLayoutManager(this));

            MaterialButton btnViewAlerts = findViewById(R.id.btnViewAlerts);
            btnViewAlerts.setOnClickListener(v -> Toast.makeText(this, "Opening Detailed Alerts...", Toast.LENGTH_SHORT).show());

            loadData();
        } catch (Exception e) {
            Toast.makeText(this, "Error initializing: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
            finish(); // Close if failed
        }
    }

    private void loadData() {
        List<ProjectSchedule> projects = ScheduleRepository.getInstance().getProjects();
        
        adapter = new ScheduleTrackerAdapter(this); // Pass context for clicks
        adapter.setSchedules(projects);
        rvSchedule.setAdapter(adapter);

        // Update counts
        int onSchedule = 0;
        int needsAttention = 0;
        for (ProjectSchedule p : projects) {
            if ("On Schedule".equalsIgnoreCase(p.getStatus())) {
                onSchedule++;
            } else {
                needsAttention++;
            }
        }
        tvOnScheduleCount.setText(String.valueOf(onSchedule));
        tvNeedsAttentionCount.setText(String.valueOf(needsAttention));
    }
}
