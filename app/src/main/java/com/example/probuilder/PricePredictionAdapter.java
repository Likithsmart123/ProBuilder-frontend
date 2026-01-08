package com.example.probuilder;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class PricePredictionAdapter extends RecyclerView.Adapter<PricePredictionAdapter.PredictionViewHolder> {

    private List<PricePrediction> predictionList = new ArrayList<>();
    private Context context;

    public PricePredictionAdapter(Context context) {
        this.context = context;
    }

    public void setPredictions(List<PricePrediction> predictions) {
        this.predictionList.clear();
        this.predictionList.addAll(predictions);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PredictionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_price_prediction, parent, false);
        return new PredictionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PredictionViewHolder holder, int position) {
        PricePrediction prediction = predictionList.get(position);

        holder.tvMaterialName.setText(prediction.getMaterialName());
        holder.tvUnit.setText(prediction.getUnit());
        holder.tvStatus.setText(prediction.getStatus());
        holder.tvCurrentPrice.setText(String.format("₹%.2f", prediction.getCurrentAvgPrice()));
        holder.tvPreviousPrice.setText(String.format("₹%.2f", prediction.getPreviousPrice()));
        holder.tvRecommendation.setText(prediction.getRecommendation());

        double change = prediction.getChangePercentage();
        String changeText = String.format("%.1f%%", Math.abs(change));

        if (change > 0) {
            holder.tvChange.setText("+" + changeText);
            holder.tvChange.setTextColor(Color.RED);
            holder.tvStatus.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_trending_up, 0, 0, 0);
        } else if (change < 0) {
            holder.tvChange.setText("-" + changeText);
            holder.tvChange.setTextColor(Color.parseColor("#43A047")); // Green
            holder.tvStatus.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_trending_down, 0, 0, 0);
        } else {
            holder.tvChange.setText("0.0%");
            holder.tvChange.setTextColor(Color.GRAY);
            holder.tvStatus.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0); // No icon
        }
    }

    @Override
    public int getItemCount() {
        return predictionList.size();
    }

    static class PredictionViewHolder extends RecyclerView.ViewHolder {
        TextView tvMaterialName, tvUnit, tvStatus, tvCurrentPrice, tvPreviousPrice, tvChange, tvRecommendation;

        public PredictionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMaterialName = itemView.findViewById(R.id.tvMaterialName);
            tvUnit = itemView.findViewById(R.id.tvUnit);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvCurrentPrice = itemView.findViewById(R.id.tvCurrentPrice);
            tvPreviousPrice = itemView.findViewById(R.id.tvPreviousPrice);
            tvChange = itemView.findViewById(R.id.tvChange);
            tvRecommendation = itemView.findViewById(R.id.tvRecommendation);
        }
    }
}