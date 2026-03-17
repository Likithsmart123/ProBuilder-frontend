package com.example.probuilder;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class WorkLogImageAdapter
        extends RecyclerView.Adapter<WorkLogImageAdapter.Holder> {

    private List<String> images;

    public WorkLogImageAdapter(List<String> images) {
        this.images = images;
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        ImageView iv = new ImageView(parent.getContext());
        iv.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
        iv.setPadding(0, 0, 0, 0); // Removed padding for full bleed
        return new Holder(iv);
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        Glide.with(holder.imageView.getContext())
                .load(images.get(position))
                .placeholder(android.R.drawable.ic_menu_gallery) // Use built-in or existing resource
                .into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        ImageView imageView;
        Holder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView;
        }
    }
}
