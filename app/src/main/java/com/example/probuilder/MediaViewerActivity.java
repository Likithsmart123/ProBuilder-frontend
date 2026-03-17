package com.example.probuilder;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.VideoView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

public class MediaViewerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_viewer);

        ImageView ivFullImage = findViewById(R.id.ivFullImage);
        VideoView videoView = findViewById(R.id.videoView);
        ProgressBar progressBar = findViewById(R.id.progressBar);
        ImageButton btnClose = findViewById(R.id.btnClose);

        String url = getIntent().getStringExtra("MEDIA_URL");
        String type = getIntent().getStringExtra("MEDIA_TYPE"); // "image" or "video"

        if (url == null) {
            Toast.makeText(this, "Error: No media URL provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        btnClose.setOnClickListener(v -> finish());

        if ("video".equalsIgnoreCase(type)) {
            // Show VideoView, Hide Image
            ivFullImage.setVisibility(View.GONE);
            videoView.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.VISIBLE);

            Uri uri = Uri.parse(url);
            videoView.setVideoURI(uri);

            MediaController mediaController = new MediaController(this);
            videoView.setMediaController(mediaController);
            mediaController.setAnchorView(videoView);

            videoView.setOnPreparedListener(mp -> {
                progressBar.setVisibility(View.GONE);
                videoView.start();
            });

            videoView.setOnErrorListener((mp, what, extra) -> {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Error playing video", Toast.LENGTH_SHORT).show();
                return true;
            });

        } else {
            // Show Image, Hide Video
            videoView.setVisibility(View.GONE);
            ivFullImage.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.VISIBLE); // Glide handles loading but showing progress is good practice if possible, 
                                                     // but Glide callbacks are cleaner. For simplicity, let's just let Glide show placeholder.
            // Actually, let's hide explicit progress bar and use Glide's placeholder/error which we can't easily map to this detached progress bar without a listener.
            // Let's us a listener.
            
            Glide.with(this)
                .load(url)
                .listener(new com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable>() {
                    @Override
                    public boolean onLoadFailed(com.bumptech.glide.load.engine.GlideException e, Object model, com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, boolean isFirstResource) {
                         progressBar.setVisibility(View.GONE);
                         Toast.makeText(MediaViewerActivity.this, "Failed to load image", Toast.LENGTH_SHORT).show();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(android.graphics.drawable.Drawable resource, Object model, com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, com.bumptech.glide.load.DataSource dataSource, boolean isFirstResource) {
                        progressBar.setVisibility(View.GONE);
                        return false;
                    }
                })
                .into(ivFullImage);
        }
    }
}
