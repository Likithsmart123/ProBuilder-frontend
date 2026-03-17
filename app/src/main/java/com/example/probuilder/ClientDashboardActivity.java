package com.example.probuilder;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class ClientDashboardActivity extends AppCompatActivity {

    private TextView tvActiveProjectsCount, tvTotalQuotationsCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_dashboard);

        // Header Profile Icon
        android.widget.ImageView ivProfile = findViewById(R.id.ivProfile);
        ivProfile.setOnClickListener(v -> startActivity(new Intent(this, ClientProfileActivity.class)));

        android.widget.ImageView ivChat = findViewById(R.id.ivChat);
        if (ivChat != null) {
            ivChat.setOnClickListener(v -> startActivity(new Intent(this, ChatListActivity.class)));
        }

        TextView tvWelcomeMessage = findViewById(R.id.tvWelcomeMessage);
        String name = getIntent().getStringExtra("USER_NAME");
        if (name != null && !name.isEmpty()) {
            tvWelcomeMessage.setText("Welcome, " + name + "!");
        }

        // Stats
        tvActiveProjectsCount = findViewById(R.id.tvActiveProjectsCount);
        tvTotalQuotationsCount = findViewById(R.id.tvTotalQuotationsCount);
        
        // Navigation Cards
        com.google.android.material.card.MaterialCardView cardProjects = findViewById(R.id.cardProjects);
        com.google.android.material.card.MaterialCardView cardQuotations = findViewById(R.id.cardQuotations);
        com.google.android.material.card.MaterialCardView cardCostBreakdown = findViewById(R.id.cardCostBreakdown);

        cardProjects.setOnClickListener(v -> startActivity(new Intent(this, ClientProjectListActivity.class)));
        cardQuotations.setOnClickListener(v -> startActivity(new Intent(this, ClientQuotationListActivity.class)));
        cardCostBreakdown.setOnClickListener(v -> startActivity(new Intent(this, ClientCostBreakdownActivity.class)));
        
        com.google.android.material.card.MaterialCardView cardGallery = findViewById(R.id.cardGallery);
        cardGallery.setOnClickListener(v -> startActivity(new Intent(this, ClientGalleryActivity.class)));

        fetchDashboardStats();
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchDashboardStats();
        
        // Refresh Name
        String name = getSharedPreferences("AUTH", MODE_PRIVATE).getString("user_name", "");
        if (name.isEmpty()) {
             name = getSharedPreferences("client_session", MODE_PRIVATE).getString("name", "");
        }
        
        TextView tvWelcomeMessage = findViewById(R.id.tvWelcomeMessage);
        
        if (!name.isEmpty() && tvWelcomeMessage != null) {
            tvWelcomeMessage.setText("Welcome, " + name + "!");
        }
        
        fetchUnreadMessageCount();
    }

    private void fetchUnreadMessageCount() {
        TextView tvUnreadBadge = findViewById(R.id.tvUnreadBadge);
        if (tvUnreadBadge == null) return;

        android.content.SharedPreferences authPrefs = getSharedPreferences("AUTH", MODE_PRIVATE);
        int clientId = authPrefs.getInt("client_id", 0);
        
        if (clientId == 0) {
            android.content.SharedPreferences sessionPrefs = getSharedPreferences("client_session", MODE_PRIVATE);
            clientId = sessionPrefs.getInt("client_id", 0);
        }
        
        if (clientId == 0) {
            SessionManager sm = new SessionManager(this);
            clientId = sm.getClientId();
        }
        
        if (clientId == 0) return;

        String url = Constants.BASE_URL + "get_unread_message_count.php?user_id=" + clientId + "&user_type=client";

        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        if (json.optString("status").equals("success")) {
                            int count = json.optInt("unread_count", 0);
                            if (count > 0) {
                                tvUnreadBadge.setVisibility(android.view.View.VISIBLE);
                                tvUnreadBadge.setText(count > 99 ? "99+" : String.valueOf(count));
                            } else {
                                tvUnreadBadge.setVisibility(android.view.View.GONE);
                            }
                        }
                    } catch (Exception e) {
                        android.util.Log.e("Dashboard", "Unread Count Error: " + e.getMessage());
                    }
                },
                error -> android.util.Log.e("Dashboard", "Unread Count Network Error: " + error.toString())
        );

        Volley.newRequestQueue(this).add(request);
    }

    private void fetchDashboardStats() {
        String url = Constants.BASE_URL + "get_client_dashboard.php";

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        if (tvActiveProjectsCount != null) {
                            tvActiveProjectsCount.setText(String.valueOf(json.optInt("active_projects", 0)));
                        }
                        if (tvTotalQuotationsCount != null) {
                            tvTotalQuotationsCount.setText(String.valueOf(json.optInt("total_quotations", 0)));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    // Silent fail
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                
                // 1. Try AUTH (New Standard)
                android.content.SharedPreferences authPrefs = getSharedPreferences("AUTH", MODE_PRIVATE);
                int clientId = authPrefs.getInt("client_id", 0);
                
                // 2. Fallback to client_session (Legacy)
                if (clientId == 0) {
                    android.content.SharedPreferences sessionPrefs = getSharedPreferences("client_session", MODE_PRIVATE);
                    clientId = sessionPrefs.getInt("client_id", 0);
                }
                
                // 3. Fallback to SessionManager (Legacy Wrapper)
                if (clientId == 0) {
                    SessionManager sm = new SessionManager(ClientDashboardActivity.this);
                    clientId = sm.getClientId();
                }

                android.util.Log.e("DASHBOARD_DEBUG", "Sending client_id = " + clientId);
                
                params.put("client_id", String.valueOf(clientId));
                return params;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }
}
