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

    @Override
    public void onBindViewHolder(@NonNull ProjectViewHolder holder, int position) {
        Project project = projectList.get(position);
        holder.tvProjectName.setText(project.getName());
        holder.tvClientName.setText(project.getClientName());
        String dates = project.getStartDate() + " - " + project.getEndDate();
        holder.tvProjectDates.setText(dates);
        holder.tvProjectStatus.setText(project.getStatus());
        
        // Dummy progress for now, can be calculated later
        if ("Completed".equalsIgnoreCase(project.getStatus())) {
            holder.projectProgressBar.setProgress(100);
        } else {
            holder.projectProgressBar.setProgress(50);
        }
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
                if (item.getName().toLowerCase().contains(text) || item.getClientName().toLowerCase().contains(text)) {
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