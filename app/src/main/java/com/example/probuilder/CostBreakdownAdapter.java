package com.example.probuilder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Locale;

public class CostBreakdownAdapter extends RecyclerView.Adapter<CostBreakdownAdapter.ViewHolder> {

    private final List<CostItem> items;

    public CostBreakdownAdapter(List<CostItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cost_breakdown, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CostItem item = items.get(position);
        holder.tvItemName.setText(item.getName());
        holder.tvItemAmount.setText(String.format(Locale.getDefault(), "₹ %,.0f", item.getAmount()));
        holder.pbItemUsage.setProgress(item.getPercentage());

        holder.itemView.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(v.getContext(), CategoryExpensesActivity.class);
            intent.putExtra("category", item.getName());
            intent.putExtra("total", item.getAmount());
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvItemName, tvItemAmount;
        ProgressBar pbItemUsage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvItemName = itemView.findViewById(R.id.tvItemName);
            tvItemAmount = itemView.findViewById(R.id.tvItemAmount);
            pbItemUsage = itemView.findViewById(R.id.pbItemUsage);
        }
    }
}
