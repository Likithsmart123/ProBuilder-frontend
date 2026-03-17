package com.example.probuilder;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class LowStockActivity extends AppCompatActivity {

    private RecyclerView rvStock;
    private TextView tvNoAlerts;
    private StockAlertAdapter adapter;
    private static final String GET_MATERIALS_URL = Constants.BASE_URL + "get_materials.php";
    private String contractorId = "1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_low_stock);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        rvStock = findViewById(R.id.rvStockAlerts);
        tvNoAlerts = findViewById(R.id.tvNoAlerts);
        rvStock.setLayoutManager(new LinearLayoutManager(this));

        loadMaterials();
    }

    private void loadMaterials() {
        android.content.SharedPreferences sp = getSharedPreferences("ProBuilderPrefs", MODE_PRIVATE);
        contractorId = String.valueOf(sp.getInt("contractor_id", -1));
        
        if ("-1".equals(contractorId)) {
            Toast.makeText(this, "Error: User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = GET_MATERIALS_URL + "?contractor_id=" + contractorId;
        Log.d("LowStockActivity", "Fetching URL: " + url);

        StringRequest request = new StringRequest(
                Request.Method.GET,
                url,
                response -> {
                    try {
                        JSONArray arr = new JSONArray(response);
                        List<StockAlert> alertList = new ArrayList<>();

                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject obj = arr.getJSONObject(i);
                            String name = obj.getString("material_name");
                            // Robust parsing for numbers
                            int currentStock = obj.optInt("current_stock", 0);
                            int minStock = obj.optInt("min_stock", 0);

                            if (currentStock <= minStock) {
                                String status;
                                if (currentStock <= minStock * 0.5) {
                                    status = "Critical";
                                } else {
                                    status = "Low";
                                }
                                
                                int iconResId = getIconForMaterial(name);
                                
                                alertList.add(new StockAlert(name, currentStock, minStock, status, iconResId));
                            }
                        }

                        if (alertList.isEmpty()) {
                            rvStock.setVisibility(View.GONE);
                            tvNoAlerts.setVisibility(View.VISIBLE);
                        } else {
                            rvStock.setVisibility(View.VISIBLE);
                            tvNoAlerts.setVisibility(View.GONE);
                            adapter = new StockAlertAdapter(alertList);
                            rvStock.setAdapter(adapter);
                        }

                    } catch (Exception e) {
                        Log.e("LowStockActivity", "Parsing Error: " + e.getMessage());
                        Toast.makeText(this, "Error loading alerts", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("LowStockActivity", "Network Error: " + error.toString());
                    Toast.makeText(this, "Network Error", Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                String token = getSharedPreferences("contractor_session", MODE_PRIVATE).getString("api_token", "");
                if (!token.isEmpty()) {
                     headers.put("Authorization", token);
                }
                return headers;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }

    private int getIconForMaterial(String name) {
        String lowerName = name.toLowerCase();
        if (lowerName.contains("cement")) return R.drawable.img_cement;
        if (lowerName.contains("steel")) return R.drawable.img_steel;
        if (lowerName.contains("sand")) return R.drawable.img_sand;
        if (lowerName.contains("brick")) return R.drawable.img_bricks;
        if (lowerName.contains("paint")) return R.drawable.img_paint;
        return R.drawable.ic_supplier_management; // Default
    }

    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }
}