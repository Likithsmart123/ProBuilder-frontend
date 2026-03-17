package com.example.probuilder;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.probuilder.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ExpenseSummaryActivity extends AppCompatActivity {

    private RecyclerView rvProjectSummaries;
    private TextView tvGrandTotal;
    private SummaryAdapter adapter;
    private static final String GET_SUMMARY_URL = Constants.BASE_URL + "get_all_project_expenses.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_summary);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("All Project Expenses");
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        tvGrandTotal = findViewById(R.id.tvGrandTotal);
        rvProjectSummaries = findViewById(R.id.rvProjectSummaries);
        rvProjectSummaries.setLayoutManager(new LinearLayoutManager(this));

        com.google.android.material.floatingactionbutton.FloatingActionButton fab = findViewById(R.id.fabAddExpenseSummary);
        fab.setOnClickListener(v -> startActivity(new Intent(ExpenseSummaryActivity.this, AddExpenseActivity.class)));

        fetchSummary();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        fetchSummary(); // Refresh when returning from detail view
    }

    private void fetchSummary() {
        com.android.volley.toolbox.JsonObjectRequest request = new com.android.volley.toolbox.JsonObjectRequest(
                Request.Method.GET, 
                GET_SUMMARY_URL, 
                null,
                response -> {
                    Log.d("EXPENSE_SUMMARY", response.toString());
                    try {
                        if (!"success".equalsIgnoreCase(response.optString("status"))) {
                            Toast.makeText(this, "Failed to load expenses", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // 1. Get Grand Total Directly
                        double grandTotal = response.optDouble("total_expense", 0.0);
                        tvGrandTotal.setText("₹ " + String.format(Locale.getDefault(), "%,.0f", grandTotal));

                        // 2. Parse Project List
                        JSONArray array = response.optJSONArray("projects");
                        List<ProjectExpenseSummary> summaries = new ArrayList<>();

                        if (array != null) {
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject obj = array.getJSONObject(i);
                                String id = obj.optString("project_id");
                                String name = obj.optString("project_title");
                                double total = obj.optDouble("total", 0.0);
                                
                                summaries.add(new ProjectExpenseSummary(id, name, total));
                            }
                        }

                        if (summaries.isEmpty()) {
                            Toast.makeText(this, "No expenses recorded yet", Toast.LENGTH_SHORT).show();
                        }

                        adapter = new SummaryAdapter(summaries);
                        rvProjectSummaries.setAdapter(adapter);

                    } catch (Exception e) {
                        Log.e("ExpenseSummary", "Parsing error", e);
                        Toast.makeText(this, "Error parsing summary", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("ExpenseSummary", "Volley Error", error);
                    Toast.makeText(this, "Network Error", Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            public java.util.Map<String, String> getHeaders() {
                java.util.Map<String, String> headers = new java.util.HashMap<>();
                String token = getSharedPreferences("contractor_session", MODE_PRIVATE).getString("api_token", "");
                if (!token.isEmpty()) {
                    headers.put("Authorization", token);
                }
                return headers;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }

    // Model Class
    private static class ProjectExpenseSummary {
        String projectId;
        String projectName;
        double totalSpent;

        public ProjectExpenseSummary(String projectId, String projectName, double totalSpent) {
            this.projectId = projectId;
            this.projectName = projectName;
            this.totalSpent = totalSpent;
        }
    }

    // Adapter Class
    private class SummaryAdapter extends RecyclerView.Adapter<SummaryAdapter.ViewHolder> {
        private List<ProjectExpenseSummary> list;

        public SummaryAdapter(List<ProjectExpenseSummary> list) {
            this.list = list;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_project_expense_summary, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ProjectExpenseSummary item = list.get(position);
            holder.tvName.setText(item.projectName);
            holder.tvTotal.setText("₹ " + String.format(Locale.getDefault(), "%,.0f", item.totalSpent));
            
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(ExpenseSummaryActivity.this, ExpenseTrackingActivity.class);
                try {
                    int pId = Integer.parseInt(item.projectId.trim());
                    intent.putExtra("project_id", pId);
                } catch (NumberFormatException e) {
                    Log.e("ExpenseSummary", "Invalid Project ID: " + item.projectId);
                    Toast.makeText(ExpenseSummaryActivity.this, "Invalid Project ID", Toast.LENGTH_SHORT).show();
                    return;
                }
                intent.putExtra("project_name", item.projectName);
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvTotal;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvProjectNameSummary);
                tvTotal = itemView.findViewById(R.id.tvProjectTotal);
            }
        }
    }
}
