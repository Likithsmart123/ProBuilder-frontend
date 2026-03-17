package com.example.probuilder;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MediaAdapter extends RecyclerView.Adapter<MediaAdapter.MediaViewHolder> {

    private final Context context;
    private final List<Uri> mediaUris;

    public MediaAdapter(Context context, List<Uri> mediaUris) {
        this.context = context;
        this.mediaUris = mediaUris;
    }

    @NonNull
    @Override
    public MediaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_media_thumbnail, parent, false);
        return new MediaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MediaViewHolder holder, int position) {
        Uri uri = mediaUris.get(position);
        
        // Use native setImageURI for local files
        String mimeType = context.getContentResolver().getType(uri);
        if (mimeType != null && mimeType.startsWith("video")) {
            holder.ivThumbnail.setImageResource(android.R.drawable.ic_media_play); 
        } else {
            holder.ivThumbnail.setImageURI(uri);
        }
    }

    @Override
    public int getItemCount() {
        return mediaUris.size();
    }

    public static class MediaViewHolder extends RecyclerView.ViewHolder {
        ImageView ivThumbnail;

        public MediaViewHolder(@NonNull View itemView) {
            super(itemView);
            ivThumbnail = itemView.findViewById(R.id.ivThumbnail);
        }
    }
}
