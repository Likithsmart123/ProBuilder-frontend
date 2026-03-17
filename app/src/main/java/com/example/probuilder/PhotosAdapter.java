package com.example.probuilder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class PhotosAdapter extends RecyclerView.Adapter<PhotosAdapter.PhotoVH> {

    private final List<PhotoItem> photos = new ArrayList<>();
    private final Context context;
    private OnPhotoClickListener listener;

    public interface OnPhotoClickListener {
        void onPhotoClick(PhotoItem item);
    }

    public void setOnPhotoClickListener(OnPhotoClickListener listener) {
        this.listener = listener;
    }

    public PhotosAdapter(Context context) {
        this.context = context;
        setHasStableIds(true);
    }

    public void setPhotos(List<PhotoItem> newPhotos) {
        photos.clear();
        if (newPhotos != null) photos.addAll(newPhotos);
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        return photos.get(position).url.hashCode();
    }

    @NonNull
    @Override
    public PhotoVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_photo, parent, false);
        return new PhotoVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoVH holder, int position) {
        PhotoItem item = photos.get(position);

        Glide.with(context)
                .load(item.url)
                .placeholder(R.drawable.ic_image_placeholder)
                .error(R.drawable.ic_launcher_background)
                .centerCrop()
                .into(holder.image);
        
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onPhotoClick(item);
        });
    }

    @Override
    public int getItemCount() {
        return photos.size();
    }

    static class PhotoVH extends RecyclerView.ViewHolder {
        ImageView image;

        PhotoVH(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.photoImage);
        }
    }
}
