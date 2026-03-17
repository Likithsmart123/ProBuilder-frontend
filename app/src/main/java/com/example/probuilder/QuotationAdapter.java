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
import java.util.Locale;

import android.widget.ImageView;
import android.widget.Toast;
import android.app.AlertDialog;
import android.content.Context;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

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

        // DEBUG: Visual check to see if item is inflated
        holder.itemView.setBackgroundColor(0x30FF0000);

        holder.tvQuotationTitle.setText(quotation.getTitle());
        holder.tvClientName.setText(quotation.getClientName());
        holder.tvProjectName.setText(quotation.getProjectName());

        try {
            double amount = Double.parseDouble(quotation.getAmount());
            holder.tvQuotationAmount.setText(String.format(Locale.getDefault(), "₹%.2f", amount));
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
        
        holder.itemView.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(v.getContext(), QuotationDetailActivity.class);
            intent.putExtra("quotation", quotation);
            v.getContext().startActivity(intent);
        });

        ImageView ivDeleteQuotation = holder.itemView.findViewById(R.id.ivDeleteQuotation);
        if (ivDeleteQuotation != null) {
            ivDeleteQuotation.setOnClickListener(v -> {
                new AlertDialog.Builder(v.getContext())
                        .setTitle("Delete Quotation")
                        .setMessage("Are you sure you want to delete this quotation?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            deleteQuotation(v.getContext(), quotation.getId(), position);
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });
        }
    }

    private void deleteQuotation(Context context, String id, int position) {
        String url = Constants.BASE_URL + "delete_quotation.php";
        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        if (json.optString("status").equals("success")) {
                            Toast.makeText(context, "Quotation deleted", Toast.LENGTH_SHORT).show();
                            if (position >= 0 && position < quotationList.size()) {
                                quotationList.remove(position);
                                notifyItemRemoved(position);
                                notifyItemRangeChanged(position, quotationList.size());
                            }
                        } else {
                            Toast.makeText(context, "Failed: " + json.optString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(context, "Error deleting quotation", Toast.LENGTH_SHORT).show()) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("quotation_id", id);
                return params;
            }
        };
        VolleySingleton.getInstance(context).addToRequestQueue(request);
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