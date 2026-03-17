package com.example.probuilder;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class ProjectMediaAdapter extends RecyclerView.Adapter<ProjectMediaAdapter.MediaViewHolder> {

    private Context context;
    private List<MediaItem> mediaList;

    public ProjectMediaAdapter(Context context, List<MediaItem> mediaList) {
        this.context = context;
        this.mediaList = mediaList;
    }

    @NonNull
    @Override
    public MediaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_media_image, parent, false);
        return new MediaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MediaViewHolder holder, int position) {
        MediaItem item = mediaList.get(position);
        String relativePath = item.path;
        
        // STEP 1: Build FULL URL
        String fullUrl = Constants.BASE_URL + relativePath;

        // STEP 3: LOG THE URL
        Log.d("IMAGE_URL", fullUrl);

        // STEP 2: Load with Glide
        Glide.with(context)
             .load(fullUrl)
             .placeholder(R.drawable.ic_image_placeholder) // Ensure this resource exists or use a default one like android.R.drawable.ic_menu_gallery
             .error(R.drawable.ic_image_placeholder) // Using same for error for now if they don't have separate error drawable
             .into(holder.ivMedia);

        if ("video".equals(item.type)) {
            holder.ivPlayIcon.setVisibility(View.VISIBLE);
            holder.ivPlayIcon.setImageResource(R.drawable.ic_play_arrow);
        } else {
            holder.ivPlayIcon.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(context, MediaViewerActivity.class);
            intent.putExtra("MEDIA_URL", fullUrl);
            intent.putExtra("MEDIA_TYPE", item.type);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return mediaList.size();
    }

    static class MediaViewHolder extends RecyclerView.ViewHolder {
        ImageView ivMedia, ivPlayIcon;

        MediaViewHolder(@NonNull View itemView) {
            super(itemView);
            ivMedia = itemView.findViewById(R.id.ivMedia);
            ivPlayIcon = itemView.findViewById(R.id.ivPlayIcon);
        }
    }
    
    public static class MediaItem {
        public String path;
        public String type;
        
        public MediaItem(String path, String type) {
            this.path = path;
            this.type = type;
        }
    }
}
