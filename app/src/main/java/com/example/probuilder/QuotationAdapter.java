package com.example.probuilder;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class QuotationAdapter extends RecyclerView.Adapter<QuotationAdapter.ViewHolder> {

    private List<Quotation> quotationList = new ArrayList<>();

    // Default constructor
    public QuotationAdapter() {
    }

    public void setQuotations(List<Quotation> newQuotations) {
        this.quotationList.clear();
        this.quotationList.addAll(newQuotations);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_quotation, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Quotation quotation = quotationList.get(position);

        holder.tvQuotationTitle.setText(quotation.getTitle());
        holder.tvClientName.setText(quotation.getClientName());
        holder.tvProjectName.setText(quotation.getProjectName());

        try {
            double amount = Double.parseDouble(quotation.getAmount());
            holder.tvQuotationAmount.setText(String.format("₹%.2f", amount));
        } catch (NumberFormatException e) {
            holder.tvQuotationAmount.setText("₹--.--"); // Placeholder for invalid amount
        }

        holder.tvCreatedDate.setText(quotation.getCreatedAt());

        String status = quotation.getStatus();
        holder.tvQuotationStatus.setText(status);

        // Styling based on status
        if ("Accepted".equalsIgnoreCase(status)) {
            holder.tvQuotationStatus.setTextColor(Color.parseColor("#2E7D32")); // Green
        } else if ("Rejected".equalsIgnoreCase(status)) {
            holder.tvQuotationStatus.setTextColor(Color.parseColor("#C62828")); // Red
        } else { // "Created" or other statuses
            holder.tvQuotationStatus.setTextColor(Color.parseColor("#F9A825")); // Yellow/Orange
        }
    }

    @Override
    public int getItemCount() {
        return quotationList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvQuotationTitle, tvClientName, tvQuotationAmount, tvProjectName, tvQuotationStatus, tvCreatedDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvQuotationTitle = itemView.findViewById(R.id.tvQuotationTitle);
            tvClientName = itemView.findViewById(R.id.tvClientName);
            tvQuotationAmount = itemView.findViewById(R.id.tvQuotationAmount);
            tvProjectName = itemView.findViewById(R.id.tvProjectName);
            tvQuotationStatus = itemView.findViewById(R.id.tvQuotationStatus);
            tvCreatedDate = itemView.findViewById(R.id.tvCreatedDate);
        }
    }
}