package com.example.probuilder;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ScheduleTrackerAdapter extends RecyclerView.Adapter<ScheduleTrackerAdapter.ScheduleViewHolder> {

    private final List<ProjectSchedule> scheduleList = new ArrayList<>();
    private final Context context;

    public ScheduleTrackerAdapter(Context context) {
        this.context = context;
    }

    public void setSchedules(List<ProjectSchedule> schedules) {
        this.scheduleList.clear();
        this.scheduleList.addAll(schedules);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ScheduleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_schedule_project, parent, false);
        return new ScheduleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ScheduleViewHolder holder, int position) {
        ProjectSchedule schedule = scheduleList.get(position);

        holder.tvProjectName.setText(schedule.getProjectName());
        holder.tvClientName.setText(schedule.getClientName());
        holder.tvStatus.setText(schedule.getStatus());
        holder.tvProgressPercent.setText(String.format(Locale.getDefault(), "%d%%", schedule.getProgress()));
        holder.pbProgress.setProgress(schedule.getProgress());
        holder.tvPlannedStart.setText(schedule.getPlannedStart());
        holder.tvPlannedEnd.setText(schedule.getPlannedEnd());
        holder.tvExpectedEnd.setText(schedule.getExpectedCompletion());
        holder.tvDaysElapsed.setText(schedule.getDaysElapsed());
        holder.tvProgressRate.setText(schedule.getProgressRate());
        holder.tvCurrentStage.setText(schedule.getCurrentStage());

        // Status Styling
        if ("On Schedule".equalsIgnoreCase(schedule.getStatus())) {
            holder.tvStatus.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#4CAF50")));
            holder.tvExpectedEnd.setTextColor(Color.parseColor("#4CAF50"));
            holder.tvProgressRate.setTextColor(Color.parseColor("#4CAF50"));
            holder.tvStatusMessage.setVisibility(View.GONE);
        } else if ("Minor Delay".equalsIgnoreCase(schedule.getStatus())) {
            holder.tvStatus.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFC107")));
            holder.tvExpectedEnd.setTextColor(Color.parseColor("#F57F17"));
            holder.tvProgressRate.setTextColor(Color.parseColor("#F57F17"));

            holder.tvStatusMessage.setVisibility(View.VISIBLE);
            holder.tvStatusMessage.setText(schedule.getAnalysisMessage());
            holder.tvStatusMessage.setTextColor(Color.parseColor("#F57F17"));
            holder.tvStatusMessage.setBackgroundColor(Color.parseColor("#FFF8E1"));
        } else { // At Risk
            holder.tvStatus.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#F44336")));
            holder.tvExpectedEnd.setTextColor(Color.parseColor("#D32F2F"));
            holder.tvProgressRate.setTextColor(Color.parseColor("#D32F2F"));

            holder.tvStatusMessage.setVisibility(View.VISIBLE);
            holder.tvStatusMessage.setText(schedule.getAnalysisMessage());
            holder.tvStatusMessage.setTextColor(Color.parseColor("#D32F2F"));
            holder.tvStatusMessage.setBackgroundColor(Color.parseColor("#FFEBEE"));
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ScheduleDetailActivity.class);
            intent.putExtra("PROJECT_DETAIL", schedule);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return scheduleList.size();
    }

    public static class ScheduleViewHolder extends RecyclerView.ViewHolder {
        TextView tvProjectName, tvClientName, tvStatus, tvProgressPercent, tvCurrentStage;
        ProgressBar pbProgress;
        TextView tvPlannedStart, tvPlannedEnd, tvExpectedEnd;
        TextView tvDaysElapsed, tvProgressRate;
        TextView tvStatusMessage;

        public ScheduleViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProjectName = itemView.findViewById(R.id.tvProjectName);
            tvClientName = itemView.findViewById(R.id.tvClientName);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvProgressPercent = itemView.findViewById(R.id.tvProgressPercent);
            pbProgress = itemView.findViewById(R.id.pbProgress);
            tvPlannedStart = itemView.findViewById(R.id.tvPlannedStart);
            tvPlannedEnd = itemView.findViewById(R.id.tvPlannedEnd);
            tvExpectedEnd = itemView.findViewById(R.id.tvExpectedEnd);
            tvDaysElapsed = itemView.findViewById(R.id.tvDaysElapsed);
            tvProgressRate = itemView.findViewById(R.id.tvProgressRate);
            tvStatusMessage = itemView.findViewById(R.id.tvStatusMessage);
            tvCurrentStage = itemView.findViewById(R.id.tvCurrentStage);
        }
    }
}
