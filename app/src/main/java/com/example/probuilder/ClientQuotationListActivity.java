package com.example.probuilder;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout; // Added SwipeRefreshLayout if needed, or just normal load
import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class ClientQuotationListActivity extends AppCompatActivity {

    private RecyclerView rvQuotations;
    private ClientQuotationAdapter adapter;
    private List<Quotation> quotationList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_quotation_list);

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        rvQuotations = findViewById(R.id.rvQuotations);
        rvQuotations.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ClientQuotationAdapter(quotationList, quotation -> {
             Intent intent = new Intent(this, ClientQuotationDetailActivity.class);
             intent.putExtra("QUOTE_ID", quotation.getId());
             intent.putExtra("QUOTE_TITLE", quotation.getTitle());
             intent.putExtra("QUOTE_PROJECT", quotation.getProjectName());
             intent.putExtra("QUOTE_AMOUNT", quotation.getAmount());
             intent.putExtra("QUOTE_DATE", quotation.getCreatedAt());
             intent.putExtra("QUOTE_DESC", quotation.getDescription());
             startActivity(intent);
        });
        rvQuotations.setAdapter(adapter);

        loadQuotations();
    }

    private void loadQuotations() {
        String url = Constants.BASE_URL + "get_client_quotations_all.php"; // Ensure this file exists on server

        com.android.volley.toolbox.JsonObjectRequest request = new com.android.volley.toolbox.JsonObjectRequest(Request.Method.GET, url, null,
            response -> {
                quotationList.clear();
                try {
                    org.json.JSONArray quotations = response.getJSONArray("quotations");

                    if (quotations.length() == 0) {
                        adapter.notifyDataSetChanged();
                        return;
                    }

                    for (int i = 0; i < quotations.length(); i++) {
                        JSONObject q = quotations.getJSONObject(i);

                        int id = q.getInt("id");
                        String title = q.getString("title");
                        String project = q.optString("project_title", "Unknown Project");
                        String contractor = q.optString("contractor_name", "");
                        String amount = q.getString("amount");
                        String date = q.getString("created_at");
                        String status = q.optString("status", "created");

                        // Adapt to Quotation constructor: 
                        // id, title, clientName/contractorName, projectName, amount, status, date ...
                        quotationList.add(new Quotation(
                            String.valueOf(id),
                            title,
                            contractor, // Display Contractor Name in the "Client" field for Client view
                            project, 
                            amount,
                            status,
                            date,
                            q.optString("description", ""), "", "", "", "", ""
                        ));
                    }
                    adapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    Log.e("ClientQuotations", "JSON Error: " + e.getMessage());
                    e.printStackTrace();
                }
            },
            error -> {
                Log.e("ClientQuotations", "Volley Error", error);
                Toast.makeText(this, "Failed to load quotations", Toast.LENGTH_SHORT).show();
            }
        ) {
            @Override
            public java.util.Map<String, String> getHeaders() {
                java.util.Map<String, String> headers = new java.util.HashMap<>();
                
                // FIXED: Use correct prefs "AUTH" and key "token"
                android.content.SharedPreferences prefs = getSharedPreferences("AUTH", MODE_PRIVATE);
                String token = prefs.getString("token", "");
                
                // STEP 4 LOG
                android.util.Log.d("CLIENT_AUTH", "Sending Authorization header = " + token);
                
                if (!token.isEmpty()) {
                    headers.put("Authorization", token);
                }
                return headers;
            }
        };
        
        // STEP 3 LOG (Before sending)
        android.content.SharedPreferences prefs = getSharedPreferences("AUTH", MODE_PRIVATE);
        String savedToken = prefs.getString("token", "NULL");
        android.util.Log.d("CLIENT_AUTH", "Using token for client API = " + savedToken); // This log actually runs before request typically due to queueing, but good for verification
        
        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }
}
