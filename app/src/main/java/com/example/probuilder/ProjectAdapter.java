package com.example.probuilder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ProjectAdapter extends RecyclerView.Adapter<ProjectAdapter.ProjectViewHolder> {

    private List<Project> projectList = new ArrayList<>();
    private List<Project> projectListFull = new ArrayList<>();

    // Constructor no longer needs a listener
    public ProjectAdapter() {
    }

    public void setProjects(List<Project> projects) {
        this.projectList.clear();
        this.projectList.addAll(projects);
        this.projectListFull.clear();
        this.projectListFull.addAll(projects);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_project, parent, false);
        return new ProjectViewHolder(view);
    }

    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Project project);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @Override
    public void onBindViewHolder(@NonNull ProjectViewHolder holder, int position) {
        Project project = projectList.get(position);
        holder.tvProjectName.setText(project.title);
        holder.tvClientName.setText(project.clientName);
        
        String startDate = project.startDate;
        String endDate = project.endDate;

        if (startDate != null && !startDate.isEmpty() && !startDate.equals("null") && 
            endDate != null && !endDate.isEmpty() && !endDate.equals("null")) {
            holder.tvProjectDates.setText(startDate + " - " + endDate);
        } else {
            holder.tvProjectDates.setText("Dates not set");
        }

        holder.tvProjectStatus.setText(project.status);
        
        // Use real progress from the model
        holder.projectProgressBar.setProgress(project.overallProgress);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(project);
            }
        });
    }

    @Override
    public int getItemCount() {
        return projectList.size();
    }

    public void filter(String text) {
        projectList.clear();
        if (text.isEmpty()) {
            projectList.addAll(projectListFull);
        } else {
            text = text.toLowerCase();
            for (Project item : projectListFull) {
                String clientName = item.client != null ? item.client.name : "";
                if (item.title.toLowerCase().contains(text) || clientName.toLowerCase().contains(text)) {
                    projectList.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    public static class ProjectViewHolder extends RecyclerView.ViewHolder {
        TextView tvProjectName, tvClientName, tvProjectDates, tvProjectStatus;
        ProgressBar projectProgressBar;

        public ProjectViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProjectName = itemView.findViewById(R.id.tvProjectName);
            tvClientName = itemView.findViewById(R.id.tvClientName);
            tvProjectDates = itemView.findViewById(R.id.tvProjectDates);
            tvProjectStatus = itemView.findViewById(R.id.tvProjectStatus);
            projectProgressBar = itemView.findViewById(R.id.projectProgressBar);
        }
    }
}