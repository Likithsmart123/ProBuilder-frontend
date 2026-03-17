package com.example.probuilder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import org.json.JSONObject;
import java.util.List;
import java.util.Locale;

public class CategoryExpenseAdapter extends RecyclerView.Adapter<CategoryExpenseAdapter.ViewHolder> {

    private List<JSONObject> expenseList;

    public CategoryExpenseAdapter(List<JSONObject> expenseList) {
        this.expenseList = expenseList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category_expense, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        JSONObject expense = expenseList.get(position);
        
        String title = expense.optString("title", "Expense");
        String desc = expense.optString("description", "");
        String date = expense.optString("expense_date", "");
        String invoice = expense.optString("invoice_no", "");
        double amount = expense.optDouble("amount", 0);

        holder.tvTitle.setText(title);
        holder.tvDescription.setText(desc.isEmpty() ? "No description" : desc);
        holder.tvDate.setText(date);
        holder.tvInvoice.setText(invoice.isEmpty() ? "-" : invoice);
        holder.tvAmount.setText(String.format(Locale.getDefault(), "₹ %,.0f", amount));
        
        holder.tvInvoice.setVisibility(invoice.isEmpty() ? View.GONE : View.VISIBLE);
    }

    @Override
    public int getItemCount() {
        return expenseList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDescription, tvDate, tvInvoice, tvAmount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvInvoice = itemView.findViewById(R.id.tvInvoice);
            tvAmount = itemView.findViewById(R.id.tvAmount);
        }
    }
}
