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

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class AllQuotationsActivity extends AppCompatActivity {

    private RecyclerView rvQuotations;
    private QuotationAdapter adapter;
    private ProgressBar progressBar;
    private TextView tvNoQuotations;

    private static final String URL_GET_QUOTATIONS = "http://10.0.2.2:5000/quotations?contractor_id=1";

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
        progressBar.setVisibility(View.VISIBLE);
        tvNoQuotations.setVisibility(View.GONE);

        StringRequest request = new StringRequest(
                Request.Method.GET,
                URL_GET_QUOTATIONS,
                response -> {
                    progressBar.setVisibility(View.GONE);
                    try {
                        JSONObject root = new JSONObject(response);
                        JSONArray arr = root.getJSONArray("quotations");
                        List<Quotation> newQuotations = new ArrayList<>();

                        if (arr.length() == 0) {
                            tvNoQuotations.setVisibility(View.VISIBLE);
                        } else {
                            tvNoQuotations.setVisibility(View.GONE);
                        }

                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject q = arr.getJSONObject(i);

                            // CORRECT: Use a placeholder for status as it's not in the JSON
                            newQuotations.add(new Quotation(
                                    q.getInt("id"),
                                    q.getString("title"),
                                    q.getString("client_name"),
                                    q.getString("project_name"),
                                    q.getString("amount"),
                                    "Created", // Placeholder status
                                    q.getString("created_at")
                            ));
                        }

                        adapter.setQuotations(newQuotations);

                    } catch (Exception e) {
                        Log.e("QUOTATION_PARSE_ERROR", "Error parsing quotations: " + e.getMessage());
                        Toast.makeText(this, "Parsing error. Please check server response format.", Toast.LENGTH_LONG).show();
                    }
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    Log.e("QUOTATION_NETWORK_ERROR", "Network error: " + error.toString());
                    Toast.makeText(this, "Network error. Please check connection and URL.", Toast.LENGTH_LONG).show();
                }
        );

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);
    }

    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }
}
