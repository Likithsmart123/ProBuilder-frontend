package com.example.probuilder;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import android.view.View;
import android.util.Log;


public class ContractorDashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contractor_dashboard);

        // Welcome Message
        TextView tvWelcomeMessage = findViewById(R.id.tvWelcomeMessage);
        String name = getIntent().getStringExtra("USER_NAME");
        if (name != null && !name.isEmpty()) {
            tvWelcomeMessage.setText("Welcome back, " + name + "!");
        }

        // Header Buttons
        ImageView ivProfile = findViewById(R.id.ivProfile);
        ivProfile.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));

        ImageView ivChat = findViewById(R.id.ivChat);
        if (ivChat != null) {
            ivChat.setOnClickListener(v -> startActivity(new Intent(this, ChatListActivity.class)));
        }



        // Quick Actions
        MaterialCardView cardPaymentManagement = findViewById(R.id.cardPaymentManagement);
        MaterialCardView cardCreateProject = findViewById(R.id.cardCreateProject);
        MaterialCardView cardMaterialManagement = findViewById(R.id.cardMaterialManagement);
        MaterialCardView cardCreateQuotation = findViewById(R.id.cardCreateQuotation);
        MaterialCardView cardPriceHike = findViewById(R.id.cardPriceHike);

        MaterialCardView cardAddClient = findViewById(R.id.cardAddClient);

        // Metric Card Clicks
        MaterialCardView cardTotalClients = findViewById(R.id.cardTotalClients);
        MaterialCardView cardActiveProjects = findViewById(R.id.cardActiveProjects);
        MaterialCardView cardAllQuotations = findViewById(R.id.cardAllQuotations);
        MaterialCardView cardTotalExpenses = findViewById(R.id.cardTotalExpenses);

        // Low Stock
        TextView tvViewAllStock = findViewById(R.id.tvViewAllStock);

        // Set OnClick Listeners
        cardTotalClients.setOnClickListener(v -> startActivity(new Intent(this, ClientsActivity.class)));
        cardActiveProjects.setOnClickListener(v -> startActivity(new Intent(this, ProjectsActivity.class)));
        cardAllQuotations.setOnClickListener(v -> startActivity(new Intent(this, AllQuotationsActivity.class)));
        tvViewAllStock.setOnClickListener(v -> startActivity(new Intent(this, LowStockActivity.class)));
        cardCreateProject.setOnClickListener(v -> startActivity(new Intent(this, CreateProjectActivity.class)));
        cardCreateQuotation.setOnClickListener(v -> startActivity(new Intent(this, CreateQuotationActivity.class)));
        cardMaterialManagement.setOnClickListener(v -> startActivity(new Intent(this, MaterialInventoryActivity.class)));
        cardPriceHike.setOnClickListener(v -> startActivity(new Intent(this, MaterialCalculatorActivity.class)));
        cardPaymentManagement.setOnClickListener(v -> startActivity(new Intent(this, PaymentManagementActivity.class)));
        cardAddClient.setOnClickListener(v -> {
            startActivity(new Intent(this, InviteClientActivity.class));
        });
        cardTotalExpenses.setOnClickListener(v -> startActivity(new Intent(this, ExpenseSummaryActivity.class)));


        // Initialize Dashboard Stock Alerts
        RecyclerView rvDashboardStock = findViewById(R.id.rvDashboardStockAlerts);
        rvDashboardStock.setLayoutManager(new LinearLayoutManager(this));
        
        loadDashboardStockAlerts(rvDashboardStock);
    }

    @Override
    protected void onResume() {
        super.onResume();
        
        // Refresh Name
        android.content.SharedPreferences sp = getSharedPreferences("ProBuilderPrefs", MODE_PRIVATE);
        String name = sp.getString("user_name", "");
        TextView tvWelcomeMessage = findViewById(R.id.tvWelcomeMessage);
        if (!name.isEmpty() && tvWelcomeMessage != null) {
            tvWelcomeMessage.setText("Welcome back, " + name + "!");
        } else if (name.isEmpty() && tvWelcomeMessage != null) {
             // Fallback if name is missing in prefs but maybe in intent?
             String intentName = getIntent().getStringExtra("USER_NAME");
             if(intentName != null && !intentName.isEmpty()) {
                  tvWelcomeMessage.setText("Welcome back, " + intentName + "!");
             }
        }

        RecyclerView rvDashboardStock = findViewById(R.id.rvDashboardStockAlerts);
        if(rvDashboardStock != null) {
             loadDashboardStockAlerts(rvDashboardStock);
        }
        
        fetchUnreadMessageCount();
    }

    private void fetchUnreadMessageCount() {
        TextView tvUnreadBadge = findViewById(R.id.tvUnreadBadge);
        if (tvUnreadBadge == null) return;

        android.content.SharedPreferences sp = getSharedPreferences("ProBuilderPrefs", MODE_PRIVATE);
        int contractorId = sp.getInt("contractor_id", -1);
        if (contractorId == -1) return;

        String url = Constants.BASE_URL + "get_unread_message_count.php?user_id=" + contractorId + "&user_type=contractor";

        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        if (json.optString("status").equals("success")) {
                            int count = json.optInt("unread_count", 0);
                            if (count > 0) {
                                tvUnreadBadge.setVisibility(View.VISIBLE);
                                tvUnreadBadge.setText(count > 99 ? "99+" : String.valueOf(count));
                            } else {
                                tvUnreadBadge.setVisibility(View.GONE);
                            }
                        }
                    } catch (Exception e) {
                        Log.e("Dashboard", "Unread Count Error: " + e.getMessage());
                    }
                },
                error -> Log.e("Dashboard", "Unread Count Network Error: " + error.toString())
        );

        Volley.newRequestQueue(this).add(request);
    }

    private void loadDashboardStockAlerts(RecyclerView rvDashboardStock) {
        TextView tvNoDashboardAlerts = findViewById(R.id.tvNoDashboardAlerts);
        
        android.content.SharedPreferences sp = getSharedPreferences("ProBuilderPrefs", MODE_PRIVATE);
        String contractorId = String.valueOf(sp.getInt("contractor_id", -1));

        if ("-1".equals(contractorId)) {
             tvNoDashboardAlerts.setVisibility(View.VISIBLE);
             tvNoDashboardAlerts.setText("Log in to view alerts");
             return;
        }

        String url = Constants.BASE_URL + "get_materials.php?contractor_id=" + contractorId;
        Log.d("Dashboard", "Fetching Stock URL: " + url);

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
                            // Robust parsing
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
                            rvDashboardStock.setVisibility(View.GONE);
                            tvNoDashboardAlerts.setVisibility(View.VISIBLE);
                             tvNoDashboardAlerts.setText("No low stock alerts");
                        } else {
                            rvDashboardStock.setVisibility(View.VISIBLE);
                            tvNoDashboardAlerts.setVisibility(View.GONE);
                            StockAlertAdapter adapter = new StockAlertAdapter(alertList);
                            rvDashboardStock.setAdapter(adapter);
                        }

                    } catch (Exception e) {
                        Log.e("Dashboard", "Parsing Error: " + e.getMessage());
                    }
                },
                error -> Log.e("Dashboard", "Network Error: " + error.toString())
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
        return R.drawable.ic_supplier_management; 
    }
}
