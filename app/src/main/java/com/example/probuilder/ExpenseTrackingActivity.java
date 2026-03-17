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
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ExpenseTrackingActivity extends AppCompatActivity {

    private RecyclerView rvExpenses;
    private TextView tvTotalExpenseAmount;
    private FloatingActionButton fabAddExpense;
    private ExpenseAdapter adapter;
    private List<Expense> expenses;
    private int contractorId;
    private int projectId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_tracking);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        rvExpenses = findViewById(R.id.rvExpenses);
        tvTotalExpenseAmount = findViewById(R.id.tvTotalExpenseAmount);
        fabAddExpense = findViewById(R.id.fabAddExpense);

        projectId = getIntent().getIntExtra("project_id", -1);
        String projectName = getIntent().getStringExtra("project_name");
        if (projectName != null && getSupportActionBar() != null) {
            getSupportActionBar().setTitle(projectName);
        }

        expenses = new ArrayList<>();
        adapter = new ExpenseAdapter(expenses);
        rvExpenses.setLayoutManager(new LinearLayoutManager(this));
        rvExpenses.setAdapter(adapter);

        fabAddExpense.setOnClickListener(v -> {
            Intent intent = new Intent(ExpenseTrackingActivity.this, AddExpenseActivity.class);
            if (projectId != -1) {
                intent.putExtra("project_id", projectId);
            }
            startActivity(intent);
        });

        fetchExpenses();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        fetchExpenses();
    }

    private void fetchExpenses() {
        if (projectId == -1) {
             Toast.makeText(this, "Project ID Missing", Toast.LENGTH_SHORT).show();
             return;
        }

        String url = Constants.BASE_URL + "get_project_expenses.php?project_id=" + projectId;

        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    Log.d("EXPENSE_LIST_RESPONSE", response);
                    try {
                        expenses.clear();
                        JSONObject root = new JSONObject(response);
                        
                        if (!"success".equalsIgnoreCase(root.optString("status"))) {
                           Toast.makeText(this, root.optString("message", "Failed to load"), Toast.LENGTH_SHORT).show();
                           return;
                        }

                        // 1. Set Total
                        double total = root.optDouble("total", 0.0);
                        tvTotalExpenseAmount.setText("₹ " + formatAmount(total));
                        
                        // 2. Parse Expenses
                        JSONArray array = root.optJSONArray("expenses");
                        if (array != null) {
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject obj = array.getJSONObject(i);
                                
                                String title = obj.optString("title", ""); 
                                String category = obj.optString("category", "General");
                                double amount = obj.optDouble("amount", 0);
                                String date = obj.optString("expense_date"); 
                                String description = obj.optString("description");
                                String invoice = obj.optString("invoice_no");

                                // We map 'title' to constructor's 'projectName' param for now, or 'description'
                                // Expense(id, category, amount, date, projectId, projectName, description, invoiceNumber, location)
                                Expense expense = new Expense(
                                    "", // ID not returned/needed for display
                                    category, 
                                    amount, 
                                    date, 
                                    String.valueOf(projectId), 
                                    title, // Using title as "ProjectName" field in model temporarily to bind to title view
                                    description, 
                                    invoice, 
                                    ""
                                );
                                expenses.add(expense);
                            }
                        }
                        
                        if (expenses.isEmpty()) {
                            Toast.makeText(this, "No expenses logged", Toast.LENGTH_SHORT).show();
                        }

                        adapter.notifyDataSetChanged();

                    } catch (Exception e) {
                        Log.e("ExpenseTracking", "Parsing Error", e);
                    }
                },
                error -> {
                    Log.e("ExpenseTracking", "Network Error", error);
                }
        ) {
            @Override
            public java.util.Map<String, String> getHeaders() {
                 java.util.Map<String, String> headers = new java.util.HashMap<>();
                 String token = getSharedPreferences("contractor_session", MODE_PRIVATE).getString("api_token", "");
                 if (!token.isEmpty()) headers.put("Authorization", token);
                 return headers;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }

    private String formatAmount(double amount) {
        return String.format(Locale.getDefault(), "%,.0f", amount);
    }

    private class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {
        private List<Expense> list;

        public ExpenseAdapter(List<Expense> list) {
            this.list = list;
        }

        @NonNull
        @Override
        public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_expense, parent, false);
            return new ExpenseViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
            Expense expense = list.get(position);
            
            // 1. Amount
            holder.tvAmount.setText("₹ " + formatAmount(expense.getAmount()));
            
            // 2. Title & Category Logic (Smart Deduplication)
            String rawTitle = expense.getProjectName(); // Mapped 'title' to this field
            String category = expense.getCategory();

            boolean titleMissing = (rawTitle == null || rawTitle.isEmpty() || rawTitle.equalsIgnoreCase("null"));
            
            if (titleMissing) {
                // FALLBACK: Use Category as Title, but HIDE the Badge to avoid "Materials Materials"
                holder.tvTitle.setText(category);
                holder.tvTitle.setVisibility(View.VISIBLE);
                holder.tvCategoryBadge.setVisibility(View.GONE); // Hide duplicate badge
            } else {
                // NORMAL: Show Specific Title AND Category Badge
                holder.tvTitle.setText(rawTitle);
                holder.tvTitle.setVisibility(View.VISIBLE);
                holder.tvCategoryBadge.setText(category);
                holder.tvCategoryBadge.setVisibility(View.VISIBLE);
            }

            // 4. Date
            holder.tvDate.setText(expense.getDate()); 

            // 5. Description (Hide if empty AND Hide if duplicate of Title)
            String desc = expense.getDescription();
            // Determine what is currently shown as title
            String displayedTitle = titleMissing ? category : rawTitle;

            boolean descEmpty = (desc == null || desc.isEmpty() || desc.equals("null"));
            boolean descDuplicate = !descEmpty && desc.trim().equalsIgnoreCase(displayedTitle.trim());

            if (descEmpty || descDuplicate) {
                holder.tvDescription.setVisibility(View.GONE);
            } else {
                holder.tvDescription.setVisibility(View.VISIBLE);
                holder.tvDescription.setText(desc);
            }

            // 6. Invoice (Hide if empty)
            String inv = expense.getInvoiceNumber();
            if (inv == null || inv.isEmpty() || inv.equals("null")) {
                holder.tvInvoice.setVisibility(View.GONE);
            } else {
                holder.tvInvoice.setVisibility(View.VISIBLE);
                holder.tvInvoice.setText("Invoice: " + inv);
            }

            // 7. Hide Confusing Location View
            holder.llProject.setVisibility(View.GONE);
        }

        @Override
        public int getItemCount() { return list.size(); }

        class ExpenseViewHolder extends RecyclerView.ViewHolder {
            TextView tvCategoryBadge, tvAmount, tvDescription, tvDate, tvInvoice, tvProject, tvTitle;
            View llProject; // Use View for generic handling

            public ExpenseViewHolder(@NonNull View itemView) {
                super(itemView);
                tvCategoryBadge = itemView.findViewById(R.id.tvCategoryBadge);
                tvAmount = itemView.findViewById(R.id.tvAmount);
                tvDescription = itemView.findViewById(R.id.tvDescription);
                tvDate = itemView.findViewById(R.id.tvDate);
                tvInvoice = itemView.findViewById(R.id.tvInvoice);
                tvProject = itemView.findViewById(R.id.tvProject);
                llProject = itemView.findViewById(R.id.llProject);
                tvTitle = itemView.findViewById(R.id.tvTitle);
            }
        }
    }
}
