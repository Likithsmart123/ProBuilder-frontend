package com.example.probuilder;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.ArrayList;
import java.util.List;

public class MaterialPredictionActivity extends AppCompatActivity {

    private MaterialPredictionAdapter adapter;
    private List<MaterialResponse> materialList = new ArrayList<>();
    private final String[] materialsToFetch = {"cement", "steel", "sand"};
    private int completedRequests = 0;
    
    // Auto Refresh
    private Handler handler;
    private Runnable refreshRunnable;
    private static final long REFRESH_INTERVAL = 30000; // 30 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_material_prediction);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Market Insights"); // Premium Title
        }
        toolbar.setNavigationOnClickListener(v -> finish());
        
        // Custom background for premium feel in code if XML not enough (but XML handles it)

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MaterialPredictionAdapter();
        recyclerView.setAdapter(adapter);

        handler = new Handler(Looper.getMainLooper());
        refreshRunnable = new Runnable() {
            @Override
            public void run() {
                loadMaterialData();
                handler.postDelayed(this, REFRESH_INTERVAL);
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadMaterialData(); // Load immediately
        handler.postDelayed(refreshRunnable, REFRESH_INTERVAL); // Schedule next
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(refreshRunnable); // Stop refreshing
    }

    private void loadMaterialData() {
        MaterialApiService api = ApiClient.getClient().create(MaterialApiService.class);
        materialList.clear();
        completedRequests = 0;

        for (String materialName : materialsToFetch) {
            api.getMaterialPrediction(materialName).enqueue(new Callback<MaterialResponse>() {
                @Override
                public void onResponse(Call<MaterialResponse> call, Response<MaterialResponse> response) {
                    completedRequests++;
                    if (response.isSuccessful() && response.body() != null) {
                        materialList.add(response.body());
                    }
                    checkCompletion();
                }

                @Override
                public void onFailure(Call<MaterialResponse> call, Throwable t) {
                    completedRequests++;
                    // Silent fail on auto-refresh to avoid spamming toasts
                    // t.printStackTrace(); 
                    checkCompletion();
                }
            });
        }
    }

    private void checkCompletion() {
        if (completedRequests == materialsToFetch.length) {
            if (!materialList.isEmpty()) {
                // Fix: Sort the list to match the original order of 'materialsToFetch'
                List<MaterialResponse> sortedList = new ArrayList<>();
                for (String name : materialsToFetch) {
                    for (MaterialResponse response : materialList) {
                        if (response.material.equalsIgnoreCase(name)) { // Case-insensitive match 
                            sortedList.add(response);
                            break; 
                        }
                    }
                }
                adapter.setMaterials(sortedList); 
            }
        }
    }
}
