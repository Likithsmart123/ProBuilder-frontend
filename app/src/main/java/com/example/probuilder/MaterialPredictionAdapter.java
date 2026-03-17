package com.example.probuilder;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class MaterialPredictionAdapter extends RecyclerView.Adapter<MaterialPredictionAdapter.ViewHolder> {

    private List<MaterialResponse> materials = new ArrayList<>();
    private Context context;

    public void setMaterials(List<MaterialResponse> materials) {
        this.materials = materials;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_material_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(materials.get(position));
    }

    @Override
    public int getItemCount() {
        return materials.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtMaterial, txtCurrent, txtPredicted, txtConfidence;
        ImageView trendIcon, imgIcon;

        ViewHolder(View itemView) {
            super(itemView);
            txtMaterial = itemView.findViewById(R.id.txtMaterial);
            txtCurrent = itemView.findViewById(R.id.txtCurrent);
            txtPredicted = itemView.findViewById(R.id.txtPredictedVal);
            txtConfidence = itemView.findViewById(R.id.txtConfidence);
            trendIcon = itemView.findViewById(R.id.trendIcon);
            imgIcon = itemView.findViewById(R.id.imgMaterialIcon);

            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    Intent intent = new Intent(context, MaterialDetailActivity.class);
                    intent.putExtra("data", materials.get(pos));
                    context.startActivity(intent);
                }
            });
        }

        void bind(MaterialResponse m) {
            txtMaterial.setText(m.material.toUpperCase());
            txtCurrent.setText("₹ " + m.current_price);
            txtPredicted.setText("₹ " + m.predicted_price);
            txtConfidence.setText(m.confidence + "%");

            // Icon Logic
            String nameLower = m.material.toLowerCase();
            if (nameLower.contains("cement")) {
                imgIcon.setImageResource(R.drawable.img_cement);
            } else if (nameLower.contains("steel")) {
                imgIcon.setImageResource(R.drawable.img_steel);
            } else if (nameLower.contains("sand")) {
                imgIcon.setImageResource(R.drawable.img_sand);
            } else {
                 imgIcon.setImageResource(R.drawable.ic_supplier_management);
            }

            switch (m.trend) {
                case "increase":
                    trendIcon.setImageResource(R.drawable.ic_trending_up);
                    trendIcon.setColorFilter(Color.GREEN);
                    break;
                case "decrease":
                    trendIcon.setImageResource(R.drawable.ic_trending_down);
                    trendIcon.setColorFilter(Color.RED);
                    break;
                default:
                    trendIcon.setImageResource(R.drawable.ic_remove); // Flat line
                    trendIcon.setColorFilter(Color.GRAY);
            }

            // Confidence display rules
            int badgeColor;
            if (m.confidence >= 85) {
                badgeColor = Color.GREEN;
            } else if (m.confidence >= 60) {
                badgeColor = Color.parseColor("#FFA500"); // Orange
            } else {
                badgeColor = Color.RED;
            }

            GradientDrawable background = new GradientDrawable();
            background.setShape(GradientDrawable.RECTANGLE);
            background.setCornerRadius(50); // Pill shape
            background.setColor(badgeColor);
            txtConfidence.setBackground(background);
        }
    }
}
