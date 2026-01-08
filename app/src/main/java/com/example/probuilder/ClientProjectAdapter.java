package com.example.probuilder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ClientProjectAdapter extends RecyclerView.Adapter<ClientProjectAdapter.ViewHolder> {

    private final List<Project> projects;
    private final OnProjectClickListener listener;

    public interface OnProjectClickListener {
        void onProjectClick(Project project);
    }

    public ClientProjectAdapter(List<Project> projects, OnProjectClickListener listener) {
        this.projects = projects;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_client_project, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Project project = projects.get(position);
        holder.tvProjectName.setText(project.getName());
        holder.tvContractorName.setText("Contractor: " + project.getClientName()); // Should ideally be contractor name
        
        // Mock status logic since Project model might not have status enum yet
        holder.tvStatusBadge.setText("On Schedule");
        holder.tvStatusBadge.setTextColor(android.graphics.Color.parseColor("#2E7D32")); // Green
        
        // Mock progress
        holder.progressBar.setProgress(65);
        
        holder.tvEndDate.setText("Planned End: " + project.getEndDate());
        
        holder.itemView.setOnClickListener(v -> listener.onProjectClick(project));
    }

    @Override
    public int getItemCount() {
        return projects.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvProjectName, tvContractorName, tvStatusBadge, tvEndDate;
        ProgressBar progressBar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProjectName = itemView.findViewById(R.id.tvProjectName);
            tvContractorName = itemView.findViewById(R.id.tvContractorName);
            tvStatusBadge = itemView.findViewById(R.id.tvStatusBadge);
            tvEndDate = itemView.findViewById(R.id.tvEndDate);
            progressBar = itemView.findViewById(R.id.progressBar);
        }
    }
}
