package com.example.probuilder;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClientProjectListActivity extends AppCompatActivity {

    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView rvProjects;
    private TextView tvEmpty;
    private ClientProjectAdapter adapter;
    private ClientProjectsViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_project_list);

        viewModel = new ViewModelProvider(this).get(ClientProjectsViewModel.class);

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());
        
        swipeRefresh = findViewById(R.id.swipeRefresh);
        rvProjects = findViewById(R.id.rvProjects);
        // Assuming there is a placeholder TextView in layout. If not, I should probably add one or handle visibility. 
        // For now, I'll try finding it, if null, I'll ignore.
        // Actually, let's just focus on RV.
        
        rvProjects.setLayoutManager(new LinearLayoutManager(this));
        
        adapter = new ClientProjectAdapter(this::onProjectClicked);
        rvProjects.setAdapter(adapter);
        
        // Observe ViewModel
        viewModel.getProjects().observe(this, projects -> {
            if (projects != null) {
                adapter.setProjects(projects);
            }
        });
        
        viewModel.getIsLoading().observe(this, loading -> {
            swipeRefresh.setRefreshing(loading);
        });
        
        viewModel.getError().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });

        swipeRefresh.setOnRefreshListener(() -> fetchProjects(true));

        if (!viewModel.hasProjects()) {
            fetchProjects(false);
        }
    }

    private void fetchProjects(boolean forceRefresh) {
        android.content.SharedPreferences prefs = getSharedPreferences("client_session", MODE_PRIVATE);
        int clientId = prefs.getInt("client_id", 0);

        if (clientId <= 0) {
            Toast.makeText(this, "Session expired", Toast.LENGTH_SHORT).show();
            return;
        }

        viewModel.setLoading(true);

        String url = Constants.BASE_URL + "get_client_projects.php?client_id=" + clientId;

        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject root = new JSONObject(response);
                        JSONArray array = root.optJSONArray("projects");
                        List<ClientProject> list = new ArrayList<>();
                        
                        if (array != null) {
                            java.util.Set<Integer> seenIds = new java.util.HashSet<>();
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject obj = array.getJSONObject(i);
                                int pId = obj.getInt("project_id");
                                
                                if (!seenIds.contains(pId)) {
                                    seenIds.add(pId);
                                    list.add(new ClientProject(
                                        pId,
                                        obj.getString("project_name"),
                                        obj.getString("location"),
                                        obj.getString("status"),
                                        obj.getInt("overall_progress"),
                                        obj.getString("last_activity_date"),
                                        obj.getBoolean("has_photos")
                                    ));
                                } else {
                                    Log.w("CLIENT_PROJECTS", "Duplicate project ignored: " + pId);
                                }
                            }
                        }
                        viewModel.setProjects(list);
                        
                    } catch (Exception e) {
                        Log.e("CLIENT_PROJECTS", "Parse error", e);
                        viewModel.setError("Failed to load projects");
                    }
                },
                error -> {
                    Log.e("CLIENT_PROJECTS", "Network error", error);
                    viewModel.setError("Network error");
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                String token = prefs.getString("client_token", "");
                if (!token.isEmpty()) headers.put("Authorization", token);
                return headers;
            }
        };

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void onProjectClicked(ClientProject project) {
        Intent intent = new Intent(this, ClientProjectDetailActivity.class);
        intent.putExtra("project_id", project.projectId);
        startActivity(intent);
    }
}
