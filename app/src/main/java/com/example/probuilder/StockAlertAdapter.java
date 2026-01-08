package com.example.probuilder;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.probuilder.R;
import java.util.List;

public class StockAlertAdapter extends RecyclerView.Adapter<StockAlertAdapter.StockViewHolder> {

    private List<StockAlert> stockList;

    public StockAlertAdapter(List<StockAlert> stockList) {
        this.stockList = stockList;
    }

    @NonNull
    @Override
    public StockViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_stock_alert, parent, false);
        return new StockViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StockViewHolder holder, int position) {
        StockAlert alert = stockList.get(position);
        holder.tvItemName.setText(alert.getItemName());
        holder.tvStockDetails.setText("Current: " + alert.getCurrentStock() + " | Min: " + alert.getMinStock());
        holder.tvStatus.setText(alert.getStatus());
        holder.ivStockImage.setImageResource(alert.getImageResId());

        if ("Critical".equalsIgnoreCase(alert.getStatus())) {
            holder.tvStatus.setBackgroundResource(R.drawable.status_critical_background);
            holder.tvStatus.setTextColor(Color.WHITE);
        } else {
            holder.tvStatus.setBackgroundResource(R.drawable.status_low_background);
            holder.tvStatus.setTextColor(Color.BLACK);
        }
    }

    @Override
    public int getItemCount() {
        return stockList.size();
    }

    public static class StockViewHolder extends RecyclerView.ViewHolder {
        TextView tvItemName, tvStockDetails, tvStatus;
        com.google.android.material.imageview.ShapeableImageView ivStockImage;

        public StockViewHolder(@NonNull View itemView) {
            super(itemView);
            tvItemName = itemView.findViewById(R.id.tvItemName);
            tvStockDetails = itemView.findViewById(R.id.tvStockDetails);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            ivStockImage = itemView.findViewById(R.id.ivStockImage);
        }
    }
}
