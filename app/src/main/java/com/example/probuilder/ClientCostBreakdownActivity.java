package com.example.probuilder;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ClientCostBreakdownActivity extends AppCompatActivity {

    private TextView tvTotalBudget, tvSpentAmount, tvRemainingAmount, tvUtilizationPercentage;
    private ProgressBar pbBudget;
    private RecyclerView rvCostItems;
    private CostBreakdownAdapter adapter;
    private List<CostItem> costItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_cost_breakdown);

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // Views
        tvTotalBudget = findViewById(R.id.tvTotalBudget);
        tvSpentAmount = findViewById(R.id.tvSpentAmount);
        tvRemainingAmount = findViewById(R.id.tvRemainingAmount);
        pbBudget = findViewById(R.id.pbBudget);
        tvUtilizationPercentage = findViewById(R.id.tvUtilizationPercentage);
        
        rvCostItems = findViewById(R.id.rvCostItems);
        rvCostItems.setLayoutManager(new LinearLayoutManager(this));
        
        adapter = new CostBreakdownAdapter(costItems);
        rvCostItems.setAdapter(adapter);

        loadCostData();
    }

    private void loadCostData() {
        String url = Constants.BASE_URL + "get_client_cost_breakdown_v2.php";
        
        Log.d("URL_DEBUG", "URL = [" + url + "]");

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        if (!"success".equalsIgnoreCase(response.optString("status"))) {
                             Toast.makeText(this, response.optString("message", "Error loading data"), Toast.LENGTH_SHORT).show();
                             return;
                        }

                        // 1. Parse Totals
                        double totalBudget = response.optDouble("budget", 0);
                        double totalSpent = response.optDouble("spent", 0);
                        double remaining = response.optDouble("remaining", 0);

                        tvTotalBudget.setText(String.format(Locale.getDefault(), "₹ %,.0f", totalBudget));
                        tvSpentAmount.setText(String.format(Locale.getDefault(), "₹ %,.0f", totalSpent));
                        tvRemainingAmount.setText(String.format(Locale.getDefault(), "₹ %,.0f", remaining));

                        int progress = 0;
                        if (totalBudget > 0) {
                            progress = (int) ((totalSpent / totalBudget) * 100);
                        }
                        pbBudget.setProgress(progress);
                        tvUtilizationPercentage.setText(progress + "% Used");

                        // 2. Parse Breakdown
                        costItems.clear();
                        JSONArray breakdown = response.getJSONArray("breakdown");
                        for (int i = 0; i < breakdown.length(); i++) {
                            JSONObject obj = breakdown.getJSONObject(i);
                            String category = obj.optString("category", "Unknown");
                            double amount = obj.optDouble("total", 0);
                            
                            // Context for item percentage is Total Spent (to show distribution of expenses)
                            costItems.add(new CostItem(category, amount, totalSpent));
                        }
                        adapter.notifyDataSetChanged();

                    } catch (JSONException e) {
                        Log.e("ClientCost", "JSON Error: " + e.getMessage());
                        Toast.makeText(this, "Error parsing cost data", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("ClientCost", "Volley Error: " + error.toString());
                    Toast.makeText(this, "Failed to load cost breakdown", Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            public java.util.Map<String, String> getHeaders() {
                java.util.Map<String, String> headers = new java.util.HashMap<>();
                headers.put("Authorization", new SessionManager(ClientCostBreakdownActivity.this).getApiToken());
                return headers;
            }
        };

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }
}
