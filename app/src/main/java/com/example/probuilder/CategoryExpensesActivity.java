package com.example.probuilder;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CategoryExpensesActivity extends AppCompatActivity {

    private TextView tvCategoryTitle, tvTotalAmount;
    private RecyclerView rvExpenses;
    private CategoryExpenseAdapter adapter;
    private List<JSONObject> expenseList = new ArrayList<>();
    private String category;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_expenses);

        category = getIntent().getStringExtra("category");
        double total = getIntent().getDoubleExtra("total", 0);

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        tvCategoryTitle = findViewById(R.id.tvCategoryTitle);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        
        tvCategoryTitle.setText(category != null ? category : "Expenses");
        tvTotalAmount.setText(String.format(Locale.getDefault(), "Total: ₹ %,.0f", total));

        rvExpenses = findViewById(R.id.rvExpenses);
        rvExpenses.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CategoryExpenseAdapter(expenseList);
        rvExpenses.setAdapter(adapter);

        loadExpenses();
    }

    private void loadExpenses() {
        if (category == null) return;
        
        String url = Constants.BASE_URL + "get_client_expenses_by_category.php?category=" + category;
        Log.d("CatExpense", "Calling: " + url);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        if (!"success".equalsIgnoreCase(response.optString("status"))) {
                            Toast.makeText(this, response.optString("message"), Toast.LENGTH_SHORT).show();
                            return;
                        }

                        JSONArray expenses = response.optJSONArray("expenses");
                        expenseList.clear();
                        
                        if (expenses != null) {
                            for (int i = 0; i < expenses.length(); i++) {
                                expenseList.add(expenses.getJSONObject(i));
                            }
                        }
                        
                        if (expenseList.isEmpty()) {
                            Toast.makeText(this, "No expenses added under " + category + " yet", Toast.LENGTH_SHORT).show();
                        }
                        
                        adapter.notifyDataSetChanged();

                    } catch (Exception e) {
                        Log.e("CatExpense", "Error", e);
                    }
                },
                error -> {
                    Log.e("CatExpense", "Volley Error", error);
                    Toast.makeText(this, "Failed to load expenses", Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            public java.util.Map<String, String> getHeaders() {
                java.util.Map<String, String> headers = new java.util.HashMap<>();
                headers.put("Authorization", new SessionManager(CategoryExpensesActivity.this).getApiToken());
                return headers;
            }
        };

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }
}
