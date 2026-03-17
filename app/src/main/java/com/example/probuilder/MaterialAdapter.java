package com.example.probuilder;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class MaterialAdapter extends RecyclerView.Adapter<MaterialAdapter.ViewHolder> {

    public interface OnDeleteListener {
        void onDelete(Material material, int position);
    }

    private Context context;
    private List<Material> materials = new ArrayList<>();
    private OnDeleteListener deleteListener;

    public MaterialAdapter(Context context) {
        this.context = context;
    }

    public void setOnDeleteListener(OnDeleteListener listener) {
        this.deleteListener = listener;
    }

    public void setMaterials(List<Material> materials) {
        this.materials = materials;
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        materials.remove(position);
        notifyItemRemoved(position);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_material, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Material material = materials.get(position);
        holder.bind(material, position);
    }

    @Override
    public int getItemCount() {
        return materials.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvStock, tvStatus, tvMinStock;
        View statusBorder;
        ImageView ivImage, ivDelete;

        ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvMaterialName);
            tvStock = itemView.findViewById(R.id.tvCurrentStock);
            tvMinStock = itemView.findViewById(R.id.tvMinStock);
            tvStatus = itemView.findViewById(R.id.tvStatusLabel);
            statusBorder = itemView.findViewById(R.id.status_border);
            ivImage = itemView.findViewById(R.id.ivMaterialImage);
            ivDelete = itemView.findViewById(R.id.ivDeleteMaterial);
        }

        void bind(Material material, int position) {
            tvName.setText(material.getName());
            tvStock.setText(material.getCurrentStock() + " " + material.getUnit());
            tvMinStock.setText("Min: " + material.getMinStock() + " " + material.getUnit());

            String status = material.getStatus();
            tvStatus.setText(status);
            tvStatus.setTextColor(Color.WHITE);

            if ("Critical".equals(status)) {
                tvStatus.setBackgroundResource(R.drawable.status_critical_background);
                statusBorder.setBackgroundColor(Color.parseColor("#E53935"));
            } else if ("Low".equals(status)) {
                tvStatus.setBackgroundResource(R.drawable.status_low_background);
                statusBorder.setBackgroundColor(Color.parseColor("#FFA500"));
            } else {
                tvStatus.setBackgroundResource(R.drawable.status_good_background);
                statusBorder.setBackgroundColor(Color.parseColor("#4CAF50"));
            }

            String nameLower = material.getName().toLowerCase();
            if (nameLower.contains("cement")) {
                ivImage.setImageResource(R.drawable.img_cement);
            } else if (nameLower.contains("steel") || nameLower.contains("rod") || nameLower.contains("bar")) {
                ivImage.setImageResource(R.drawable.img_steel);
            } else if (nameLower.contains("sand")) {
                ivImage.setImageResource(R.drawable.img_sand);
            } else if (nameLower.contains("brick")) {
                ivImage.setImageResource(R.drawable.img_bricks);
            } else if (nameLower.contains("paint")) {
                ivImage.setImageResource(R.drawable.img_paint);
            } else {
                ivImage.setImageResource(R.drawable.ic_construction);
            }

            // Wire delete icon
            ivDelete.setOnClickListener(v -> {
                if (deleteListener != null) {
                    deleteListener.onDelete(material, getAdapterPosition());
                }
            });
        }
    }
}
