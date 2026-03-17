package com.example.probuilder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

public class ProjectExpenseAdapter extends RecyclerView.Adapter<ProjectExpenseAdapter.ExpenseViewHolder> {

    private List<Expense> expenseList;

    public ProjectExpenseAdapter(List<Expense> expenseList) {
        this.expenseList = expenseList;
    }

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_expense, parent, false);
        return new ExpenseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        Expense expense = expenseList.get(position);
        holder.bind(expense);
    }

    @Override
    public int getItemCount() {
        return expenseList.size();
    }

    static class ExpenseViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategoryBadge, tvAmount, tvTitle, tvDescription, tvDate, tvInvoice, tvProject;
        android.widget.LinearLayout llInvoice;

        public ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategoryBadge = itemView.findViewById(R.id.tvCategoryBadge);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            // Fix: Bind tvTitle to enable dynamic titles instead of hardcoded "Cement Purchase"
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvInvoice = itemView.findViewById(R.id.tvInvoice);
            tvProject = itemView.findViewById(R.id.tvProject);
            // Added llInvoice binding
            llInvoice = itemView.findViewById(R.id.llInvoice);
        }

        public void bind(Expense expense) {
            tvAmount.setText("₹ " + String.format(Locale.getDefault(), "%,.0f", expense.getAmount()));
            
            // Fix: Use description as the main title and hide the secondary description to avoid duplication
            if (tvTitle != null) {
                tvTitle.setText(expense.getDescription());
            }
            // Hide original description view as we promoted it to Title
            tvDescription.setVisibility(View.GONE);

            tvDate.setText(expense.getDate());
            
            // Invoice Logic
            if (expense.getInvoiceNumber() != null && !expense.getInvoiceNumber().isEmpty()) {
                tvInvoice.setText(expense.getInvoiceNumber());
                if (llInvoice != null) llInvoice.setVisibility(View.VISIBLE);
            } else {
                if (llInvoice != null) llInvoice.setVisibility(View.GONE);
            }

            tvCategoryBadge.setText(expense.getCategory());
            
            // Show location instead of project name which is redundant in details view
            if (expense.getLocation() != null && !expense.getLocation().isEmpty()) {
                 tvProject.setText(expense.getLocation());
                 tvProject.setVisibility(View.VISIBLE);
            } else {
                 tvProject.setVisibility(View.GONE);
            }
        }
    }
}
