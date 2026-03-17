package com.example.probuilder;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.google.android.material.button.MaterialButton;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class ClientProjectAdapter extends RecyclerView.Adapter<ClientProjectAdapter.ProjectVH> {

    private final List<ClientProject> projects = new ArrayList<>();
    private final OnProjectClickListener listener;

    public interface OnProjectClickListener {
        void onProjectClick(ClientProject project);
    }

    public ClientProjectAdapter(OnProjectClickListener listener) {
        this.listener = listener;
    }

    public void setProjects(List<ClientProject> newProjects) {
        projects.clear();
        if (newProjects != null) {
            projects.addAll(newProjects);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProjectVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_client_project, parent, false);
        return new ProjectVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ProjectVH holder, int position) {
        ClientProject project = projects.get(position);
        holder.bind(project, listener);
    }

    @Override
    public int getItemCount() {
        return projects.size();
    }

    static class ProjectVH extends RecyclerView.ViewHolder {
        TextView tvTitle, tvLocation, tvStatus, tvProgressText, tvLastActivity;
        ProgressBar progressBar;
        MaterialButton btnView;

        public ProjectVH(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvProjectTitle);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvStatus = itemView.findViewById(R.id.tvStatusBadge);
            tvProgressText = itemView.findViewById(R.id.tvProgressText);
            tvLastActivity = itemView.findViewById(R.id.tvLastActivity);
            progressBar = itemView.findViewById(R.id.progressBar);
            btnView = itemView.findViewById(R.id.btnViewProject);
        }

        public void bind(ClientProject p, OnProjectClickListener listener) {
            tvTitle.setText(p.title);
            tvLocation.setText(p.location);
            tvStatus.setText(p.status);
            tvProgressText.setText(p.overallProgress + "%");
            progressBar.setProgress(p.overallProgress); // Use setProgress(int)
            tvLastActivity.setText("Last update: " + p.lastActivityDate);

            // Status Styling
            switch (p.status.toLowerCase()) {
                case "completed":
                    tvStatus.setTextColor(Color.parseColor("#4CAF50")); // Green
                    break;
                case "planning":
                    tvStatus.setTextColor(Color.parseColor("#2196F3")); // Blue
                    break;
                case "delayed":
                    tvStatus.setTextColor(Color.parseColor("#F44336")); // Red
                    break;
                default:
                    tvStatus.setTextColor(Color.parseColor("#FF9800")); // Orange
                    break;
            }

            btnView.setOnClickListener(v -> listener.onProjectClick(p));
            // Make whole card clickable too for better UX? User said "View Project" button.
            // Let's attach listener to both for safety/usability, but the button is explicit.
            itemView.setOnClickListener(v -> listener.onProjectClick(p));
        }
    }
}
