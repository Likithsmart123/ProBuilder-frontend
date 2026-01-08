package com.example.probuilder;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class ExpenseTrackingActivity extends AppCompatActivity {

    private TextView tvTotalExpenseAmount;
    private RecyclerView rvExpenses;
    private ExpenseAdapter adapter;

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

        tvTotalExpenseAmount = findViewById(R.id.tvTotalExpenseAmount);
        rvExpenses = findViewById(R.id.rvExpenses);
        rvExpenses.setLayoutManager(new LinearLayoutManager(this));

        FloatingActionButton fabAddExpense = findViewById(R.id.fabAddExpense);
        fabAddExpense.setOnClickListener(v -> {
            startActivity(new Intent(ExpenseTrackingActivity.this, AddExpenseActivity.class));
        });

        loadData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData(); // Refresh on return
    }

    private void loadData() {
        List<Expense> expenses = ExpenseRepository.getInstance().getExpenses();
        adapter = new ExpenseAdapter(expenses);
        rvExpenses.setAdapter(adapter);

        // Calculate Total
        double totalAmount = 0;
        for (Expense expense : expenses) {
            totalAmount += expense.getAmount();
        }
        tvTotalExpenseAmount.setText("₹ " + formatAmount(totalAmount));
    }

    private String formatAmount(double amount) {
        return String.format("%,.0f", amount);
    }
    
    // Inner Adapter Class
    private class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {
        private List<Expense> expenses;

        public ExpenseAdapter(List<Expense> expenses) {
            this.expenses = expenses;
        }

        @NonNull
        @Override
        public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_expense, parent, false);
            return new ExpenseViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
            Expense expense = expenses.get(position);
            holder.tvAmount.setText("₹ " + formatAmount(expense.getAmount()));
            holder.tvDescription.setText(expense.getDescription());
            holder.tvDate.setText(expense.getDate());
            holder.tvProject.setText(expense.getProjectName());
            holder.tvInvoice.setText(expense.getInvoiceNumber());
            
            holder.tvCategoryBadge.setText(expense.getCategory());
            
            // Basic icon logic - assuming standard or generic icons if specific ones are missing
            // You can enhance this if you added specific drawables
        }

        @Override
        public int getItemCount() {
            return expenses.size();
        }

        class ExpenseViewHolder extends RecyclerView.ViewHolder {
            TextView tvCategoryBadge, tvAmount, tvDescription, tvDate, tvProject, tvInvoice;

            public ExpenseViewHolder(@NonNull View itemView) {
                super(itemView);
                tvCategoryBadge = itemView.findViewById(R.id.tvCategoryBadge);
                tvAmount = itemView.findViewById(R.id.tvAmount);
                tvDescription = itemView.findViewById(R.id.tvDescription);
                tvDate = itemView.findViewById(R.id.tvDate);
                tvProject = itemView.findViewById(R.id.tvProject);
                tvInvoice = itemView.findViewById(R.id.tvInvoice);
            }
        }
    }
}
