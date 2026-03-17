package com.example.probuilder;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.chip.Chip;

public class ScheduleDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        ProjectSchedule project = (ProjectSchedule) getIntent().getSerializableExtra("PROJECT_DETAIL");

        if (project == null) {
            finish(); // Cannot show details without data
            return;
        }

        TextView tvProjectName = findViewById(R.id.tvProjectName);
        TextView tvClientName = findViewById(R.id.tvClientName);
        Chip chipStatus = findViewById(R.id.chipStatus);
        ProgressBar pbProgress = findViewById(R.id.pbProgress);
        TextView tvProgressPercent = findViewById(R.id.tvProgressPercent);
        TextView tvStatusMessage = findViewById(R.id.tvStatusMessage);
        TextView tvPlannedStart = findViewById(R.id.tvPlannedStart);
        TextView tvPlannedEnd = findViewById(R.id.tvPlannedEnd);
        TextView tvExpectedEnd = findViewById(R.id.tvExpectedEnd);

        tvProjectName.setText(project.getProjectName());
        tvClientName.setText("For " + project.getClientName());
        chipStatus.setText(project.getStatus());
        pbProgress.setProgress(project.getProgress());
        tvProgressPercent.setText(project.getProgress() + "%");
        tvPlannedStart.setText(project.getPlannedStart());
        tvPlannedEnd.setText(project.getPlannedEnd());
        tvExpectedEnd.setText(project.getExpectedCompletion());

        // Status Styling
        if ("On Schedule".equalsIgnoreCase(project.getStatus())) {
            chipStatus.setChipBackgroundColor(ColorStateList.valueOf(Color.parseColor("#4CAF50")));
            tvStatusMessage.setVisibility(View.GONE);
        } else if ("Minor Delay".equalsIgnoreCase(project.getStatus())) {
            chipStatus.setChipBackgroundColor(ColorStateList.valueOf(Color.parseColor("#FFC107")));
            tvStatusMessage.setVisibility(View.VISIBLE);
            tvStatusMessage.setText(project.getAnalysisMessage());
            tvStatusMessage.setBackgroundColor(Color.parseColor("#FFF8E1"));
            tvStatusMessage.setTextColor(Color.parseColor("#F57F17"));
        } else { // At Risk
            chipStatus.setChipBackgroundColor(ColorStateList.valueOf(Color.parseColor("#F44336")));
            tvStatusMessage.setVisibility(View.VISIBLE);
            tvStatusMessage.setText(project.getAnalysisMessage());
            tvStatusMessage.setBackgroundColor(Color.parseColor("#FFEBEE"));
            tvStatusMessage.setTextColor(Color.parseColor("#D32F2F"));
        }
    }
}
