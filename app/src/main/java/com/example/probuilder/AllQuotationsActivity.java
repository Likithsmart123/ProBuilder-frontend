package com.example.probuilder;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;



import java.util.ArrayList;
import java.util.List;

public class AllQuotationsActivity extends AppCompatActivity {

    private RecyclerView rvQuotations;
    private QuotationAdapter adapter;
    private ProgressBar progressBar;
    private TextView tvNoQuotations;

    private static final String URL_GET_QUOTATIONS = Constants.BASE_URL + "get_quotations.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_quotations);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        rvQuotations = findViewById(R.id.rvQuotations);
        progressBar = findViewById(R.id.progressBar);
        tvNoQuotations = findViewById(R.id.tvNoQuotations);

        findViewById(R.id.fabCreateQuotation).setOnClickListener(v -> {
            startActivity(new Intent(AllQuotationsActivity.this, CreateQuotationActivity.class));
        });

        rvQuotations.setLayoutManager(new LinearLayoutManager(this));
        adapter = new QuotationAdapter();
        rvQuotations.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadQuotations(); // Always refresh when returning
    }

    private void loadQuotations() {
        // ✅ CORRECT API FOR CONTRACTOR
        String url = Constants.BASE_URL + "get_contractor_quotations.php";

        Log.d("QUOTATION_DEBUG", "Calling URL: " + url);
        
        progressBar.setVisibility(View.VISIBLE);
        tvNoQuotations.setVisibility(View.GONE);

        // ✅ CORRECT ANDROID REQUEST (Using unified AuthJsonRequest)
        AuthJsonRequest request = new AuthJsonRequest(
                this,
                Request.Method.GET,
                url,
                null,
                response -> {
                    progressBar.setVisibility(View.GONE);
                    Log.d("QUOTATION_DEBUG", "Response: " + response.toString());
                    
                    try {
                        // Response structure: { "status": "success", "quotations": [...] }
                        if (!response.optString("status").equals("success")) {
                            Toast.makeText(this, response.optString("error", "Failed"), Toast.LENGTH_SHORT).show();
                            return;
                        }

                        org.json.JSONArray arr = response.getJSONArray("quotations");
                        List<Quotation> newQuotations = new ArrayList<>();

                        for (int i = 0; i < arr.length(); i++) {
                            org.json.JSONObject o = arr.getJSONObject(i);

                            String id = String.valueOf(o.getInt("quotation_id")); // API returns quotation_id
                            String title = o.getString("title");
                            String projectName = o.optString("project_title", "Unknown Project");
                            String clientName = o.optString("client_name", "Unknown Client");
                            String amount = String.valueOf(o.getDouble("amount"));
                            String status = o.getString("status");
                            String createdAt = o.getString("created_at");

                            String description = o.optString("description", "");
                            String clientEmail = o.optString("client_email", "");
                            String clientPhone = o.optString("client_phone", "");
                            
                            // Defaults/Empty for fields not in this specific API but needed by model
                            String projectLocation = ""; 
                            String projectStart = "";
                            String projectEnd = "";

                            newQuotations.add(new Quotation(
                                id, title, clientName, projectName, amount, status, createdAt,
                                description, clientEmail, clientPhone, projectLocation, projectStart, projectEnd
                            ));
                        }

                        adapter.setQuotations(newQuotations);
                        tvNoQuotations.setVisibility(newQuotations.isEmpty() ? View.VISIBLE : View.GONE);

                    } catch (Exception e) {
                        Log.e("QUOTATION_ERR", "Parse error", e);
                        Toast.makeText(this, "Data Error", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    Log.e("QUOTATION_ERROR", error.toString());
                    Toast.makeText(this, "Network Error", Toast.LENGTH_SHORT).show();
                }
        );

        Volley.newRequestQueue(this).add(request);
    }

    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }
}
