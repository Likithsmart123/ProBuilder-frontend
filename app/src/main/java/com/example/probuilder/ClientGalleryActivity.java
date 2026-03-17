package com.example.probuilder;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClientGalleryActivity extends AppCompatActivity {

    private RecyclerView rvGallery;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private GalleryAdapter adapter;
    private List<MediaItem> mediaList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_gallery);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        rvGallery = findViewById(R.id.rvGallery);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);

        rvGallery.setLayoutManager(new GridLayoutManager(this, 3));
        adapter = new GalleryAdapter(mediaList);
        rvGallery.setAdapter(adapter);

        fetchGallery();
    }

    private void fetchGallery() {
        progressBar.setVisibility(View.VISIBLE);
        String url = Constants.BASE_URL + "get_client_gallery.php";

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    progressBar.setVisibility(View.GONE);
                    android.util.Log.e("GALLERY_DEBUG", "Response: " + response);
                    try {
                        JSONObject json = new JSONObject(response);
                        if ("success".equals(json.optString("status"))) {
                            JSONArray data = json.getJSONArray("data");
                            android.util.Log.e("GALLERY_DEBUG", "Data count: " + data.length());
                            mediaList.clear();
                            for (int i = 0; i < data.length(); i++) {
                                JSONObject obj = data.getJSONObject(i);
                                String fileUrl = obj.getString("url");
                                if (!fileUrl.isEmpty() && !fileUrl.startsWith("http")) {
                                    fileUrl = Constants.BASE_URL + fileUrl;
                                }
                                String type = obj.optString("media_type", "photo");
                                String projectName = obj.optString("project_name", "");
                                
                                mediaList.add(new MediaItem(fileUrl, type, projectName));
                            }
                            
                            android.util.Log.e("GALLERY_DEBUG", "List size: " + mediaList.size());

                            if (mediaList.isEmpty()) {
                                tvEmpty.setVisibility(View.VISIBLE);
                                rvGallery.setVisibility(View.GONE);
                            } else {
                                tvEmpty.setVisibility(View.GONE);
                                rvGallery.setVisibility(View.VISIBLE);
                                adapter.notifyDataSetChanged();
                            }
                        } else {
                            android.util.Log.e("GALLERY_DEBUG", "Error status: " + json.optString("message"));
                            Toast.makeText(this, "Error: " + json.optString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        android.util.Log.e("GALLERY_DEBUG", "Parse error: " + e.getMessage());
                        Toast.makeText(this, "Parse error", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    if (error.networkResponse != null && error.networkResponse.data != null) {
                         String errorMsg = new String(error.networkResponse.data);
                         android.util.Log.e("GALLERY_DEBUG", "Error Body: " + errorMsg);
                         android.util.Log.e("GALLERY_DEBUG", "Status Code: " + error.networkResponse.statusCode);
                    } else {
                        android.util.Log.e("GALLERY_DEBUG", "Volley Error: " + error.getMessage());
                    }
                    Toast.makeText(this, "Network Error", Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                
                // Get Client ID (Standard way from ClientDashboard)
                // We should probably check multiple prefs or passing via intent would be cleaner,
                // but for consistency with Dashboard, we read prefs again or expect intent.
                // Dashboard didn't pass client_id in intent usually, it reads from Prefs.
                // Let's read from Prefs as per Dashboard logic.
                
                android.content.SharedPreferences authPrefs = getSharedPreferences("AUTH", MODE_PRIVATE);
                int clientId = authPrefs.getInt("client_id", 0);
                
                if (clientId == 0) {
                    android.content.SharedPreferences sessionPrefs = getSharedPreferences("client_session", MODE_PRIVATE);
                    clientId = sessionPrefs.getInt("client_id", 0);
                }
                
                if (clientId == 0) {
                     SessionManager sm = new SessionManager(ClientGalleryActivity.this);
                     clientId = sm.getClientId();
                }

                params.put("client_id", String.valueOf(clientId));
                return params;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }

    // --- Inner Classes ---

    private static class MediaItem {
        String url;
        String type; // "photo" or "video"
        String projectName;

        MediaItem(String url, String type, String projectName) {
            this.url = url;
            this.type = type;
            this.projectName = projectName;
        }
    }

    private class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.ViewHolder> {
        private List<MediaItem> list;

        GalleryAdapter(List<MediaItem> list) {
            this.list = list;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_gallery_media, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            MediaItem item = list.get(position);

            Glide.with(ClientGalleryActivity.this)
                    .load(item.url)
                    .centerCrop()
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .into(holder.ivThumbnail);

            holder.ivPlayIcon.setVisibility("video".equalsIgnoreCase(item.type) ? View.VISIBLE : View.GONE);
            holder.tvProjectName.setText(item.projectName);

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(ClientGalleryActivity.this, MediaViewerActivity.class);
                intent.putExtra("MEDIA_URL", item.url);
                intent.putExtra("MEDIA_TYPE", item.type);
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView ivThumbnail, ivPlayIcon;
            TextView tvProjectName;

            ViewHolder(View itemView) {
                super(itemView);
                ivThumbnail = itemView.findViewById(R.id.ivThumbnail);
                ivPlayIcon = itemView.findViewById(R.id.ivPlayIcon);
                tvProjectName = itemView.findViewById(R.id.tvProjectName);
            }
        }
    }
}
