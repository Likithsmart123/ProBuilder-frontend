package com.example.probuilder;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MaterialAdapter extends RecyclerView.Adapter<MaterialAdapter.MaterialViewHolder> {

    private List<Material> materialList = new ArrayList<>();
    private Context context;

    public MaterialAdapter(Context context) {
        this.context = context;
    }

    public void setMaterials(List<Material> materials) {
        this.materialList.clear();
        this.materialList.addAll(materials);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MaterialViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_material, parent, false);
        return new MaterialViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MaterialViewHolder holder, int position) {
        Material material = materialList.get(position);

        holder.tvMaterialName.setText(material.getName());
        holder.tvMinStock.setText("Min Stock: " + material.getMinStock() + " " + material.getUnit());
        holder.tvCurrentStock.setText("Current: " + material.getCurrentStock() + " " + material.getUnit());

        String status = material.getStatus();
        holder.tvStatusLabel.setText(status);

        int statusColor;
        int statusBackgroundColor;

        switch (status) {
            case "Critical":
                statusColor = Color.parseColor("#E53935"); // Red
                statusBackgroundColor = R.drawable.status_critical_background;
                holder.ivWarning.setVisibility(View.VISIBLE);
                break;
            case "Low":
                statusColor = Color.parseColor("#FDD835"); // Yellow
                statusBackgroundColor = R.drawable.status_low_background;
                holder.ivWarning.setVisibility(View.VISIBLE);
                break;
            default: // "Good"
                statusColor = Color.parseColor("#43A047"); // Green
                statusBackgroundColor = R.drawable.status_good_background;
                holder.ivWarning.setVisibility(View.GONE);
                break;
        }

        holder.statusBorder.setBackgroundColor(statusColor);
        holder.tvStatusLabel.setBackgroundResource(statusBackgroundColor);
         // The text color for Low status is black, others are white.
        if ("Low".equals(status)) {
            holder.tvStatusLabel.setTextColor(Color.BLACK);
        } else {
            holder.tvStatusLabel.setTextColor(Color.WHITE);
        }
    }

    @Override
    public int getItemCount() {
        return materialList.size();
    }

    public static class MaterialViewHolder extends RecyclerView.ViewHolder {
        TextView tvMaterialName, tvMinStock, tvCurrentStock, tvStatusLabel;
        View statusBorder;
        ImageView ivWarning;

        public MaterialViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMaterialName = itemView.findViewById(R.id.tvMaterialName);
            tvMinStock = itemView.findViewById(R.id.tvMinStock);
            tvCurrentStock = itemView.findViewById(R.id.tvCurrentStock);
            tvStatusLabel = itemView.findViewById(R.id.tvStatusLabel);
            statusBorder = itemView.findViewById(R.id.status_border);
            ivWarning = itemView.findViewById(R.id.ivWarning);
        }
    }
}
