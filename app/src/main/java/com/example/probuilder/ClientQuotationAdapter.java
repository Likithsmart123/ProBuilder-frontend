package com.example.probuilder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Locale;

public class ClientQuotationAdapter extends RecyclerView.Adapter<ClientQuotationAdapter.ViewHolder> {

    private final List<Quotation> quotations;
    private final OnQuotationClickListener listener;

    public interface OnQuotationClickListener {
        void onQuotationClick(Quotation quotation);
    }

    public ClientQuotationAdapter(List<Quotation> quotations, OnQuotationClickListener listener) {
        this.quotations = quotations;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_client_quotation, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Quotation quotation = quotations.get(position);
        holder.tvQuotationTitle.setText("Quotation #" + quotation.getId()); // Using ID as title for now
        holder.tvDate.setText(quotation.getCreatedAt());
        holder.tvProjectLink.setText("Project: " + quotation.getProjectName());
        holder.tvAmount.setText("₹ " + quotation.getAmount());
        
        holder.itemView.setOnClickListener(v -> listener.onQuotationClick(quotation));
    }

    @Override
    public int getItemCount() {
        return quotations.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvQuotationTitle, tvDate, tvProjectLink, tvAmount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvQuotationTitle = itemView.findViewById(R.id.tvQuotationTitle);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvProjectLink = itemView.findViewById(R.id.tvProjectLink);
            tvAmount = itemView.findViewById(R.id.tvAmount);
        }
    }
}
