package com.example.probuilder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.android.material.chip.Chip;
import com.google.android.material.imageview.ShapeableImageView;
import java.util.List;

public class DailyProgressAdapter extends RecyclerView.Adapter<DailyProgressAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(DailyProgress progress);
    }

    private List<DailyProgress> progressList;
    private OnItemClickListener listener;

    public DailyProgressAdapter(List<DailyProgress> progressList, OnItemClickListener listener) {
        this.progressList = progressList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_daily_progress_modern, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DailyProgress progress = progressList.get(position);
        
        holder.tvDate.setText(progress.getWorkDate());
        holder.tvSummary.setText(progress.getSummary());
        holder.chipProgress.setText("+" + progress.getProgressUpdate() + "%");
        
        // Show stage name if available (DailyProgress might need this field, defaulting to static or summary for now)
        // Since DailyProgress model might not have stage name yet, we will just use summary for now or leave stage text generic
        // Assuming user wants "Extraordinary UI", we will bind data carefully.
        holder.tvStageName.setText("Daily Update"); // Placeholder or update model if needed

        // Thumbnail logic
        if (progress.getImages() != null && !progress.getImages().isEmpty()) {
            holder.ivThumbnail.setVisibility(View.VISIBLE);
            String fullUrl = progress.getImages().get(0); // API returns full URL
            Glide.with(holder.itemView.getContext())
                .load(fullUrl)
                .placeholder(R.drawable.ic_image_placeholder) // Use specific placeholder
                .error(R.drawable.ic_broken_image) // Use specific error image
                .into(holder.ivThumbnail);
        } else {
            holder.ivThumbnail.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(progress);
        });
    }

    @Override
    public int getItemCount() {
        return progressList == null ? 0 : progressList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvSummary, tvStageName;
        Chip chipProgress;
        ShapeableImageView ivThumbnail;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvSummary = itemView.findViewById(R.id.tvSummary);
            tvStageName = itemView.findViewById(R.id.tvStageName);
            chipProgress = itemView.findViewById(R.id.chipProgress);
            ivThumbnail = itemView.findViewById(R.id.ivThumbnail);
        }
    }
}
