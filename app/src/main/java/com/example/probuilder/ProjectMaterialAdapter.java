package com.example.probuilder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import android.widget.ImageView;
import android.widget.Toast;
import android.app.AlertDialog;
import android.content.Context;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class ProjectMaterialAdapter extends RecyclerView.Adapter<ProjectMaterialAdapter.MaterialViewHolder> {

    private List<ProjectMaterial> materialList;
    private int projectId;

    public ProjectMaterialAdapter(List<ProjectMaterial> materialList, int projectId) {
        this.materialList = materialList;
        this.projectId = projectId;
    }

    @NonNull
    @Override
    public MaterialViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Reuse item_schedule_tracker maybe? Or create a simple layout.
        // Let's assume we need a simple item layout. Since I can't create multiple files in one step easily without being tedious,
        // and user didn't give me a layout, I'll reuse 'android.R.layout.simple_list_item_2' or
        // create a new one. Let's create a new one 'item_project_material_used.xml' in next step.
        // For now, I'll refer to R.layout.item_project_material_used
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_project_material_used, parent, false);
        return new MaterialViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MaterialViewHolder holder, int position) {
        ProjectMaterial material = materialList.get(position);
        holder.tvName.setText(material.getMaterialName());
        
        holder.tvQuantity.setText("Used: " + material.getUsedQuantity() + " " + material.getUnit());
        
        if (material.getSpecifications() != null && !material.getSpecifications().trim().isEmpty()) {
            holder.tvSpecifications.setVisibility(View.VISIBLE);
            holder.tvSpecifications.setText("Type/Size: " + material.getSpecifications());
        } else {
            holder.tvSpecifications.setVisibility(View.GONE);
            holder.tvSpecifications.setText("");
        }

        if (holder.tvRemaining != null) {
            holder.tvRemaining.setText("Remaining: " + material.getRemainingQuantity() + " " + material.getUnit());
        }
        
        // Image Logic based on StockAlertAdapter
        String nameLower = material.getMaterialName().toLowerCase();
        int imageResId = R.drawable.ic_widgets; // Default

        if (nameLower.contains("cement")) {
            imageResId = R.drawable.img_cement;
        } else if (nameLower.contains("brick")) {
            imageResId = R.drawable.img_bricks;
        } else if (nameLower.contains("sand")) {
            imageResId = R.drawable.img_sand;
        } else if (nameLower.contains("steel") || nameLower.contains("iron")) {
            imageResId = R.drawable.img_steel;
        } else if (nameLower.contains("paint")) {
            imageResId = R.drawable.img_paint;
        } else if (nameLower.contains("wood") || nameLower.contains("timber")) {
            imageResId = R.drawable.ic_construction; // Fallback
        } else if (nameLower.contains("tile") || nameLower.contains("marble")) {
            imageResId = R.drawable.ic_widgets; // Fallback
        } else if (nameLower.contains("electric")) {
            imageResId = R.drawable.ic_lightbulb;
        } else if (nameLower.contains("plumb") || nameLower.contains("pipe")) {
            imageResId = R.drawable.ic_widgets; // Fallback
        }
        
        holder.ivMaterial.setImageResource(imageResId);

        ImageView ivDeleteMaterial = holder.itemView.findViewById(R.id.ivDeleteMaterial);
        if (ivDeleteMaterial != null) {
            ivDeleteMaterial.setOnClickListener(v -> {
                new AlertDialog.Builder(v.getContext())
                        .setTitle("Delete Material")
                        .setMessage("Are you sure you want to delete this material usage? Stock will be restored.")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            deleteMaterial(v.getContext(), material.getMaterialId(), position);
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });
        }
    }

    private void deleteMaterial(Context context, int materialId, int position) {
        String url = Constants.BASE_URL + "delete_material.php";
        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        if (json.optString("status").equals("success")) {
                            Toast.makeText(context, "Material deleted and stock restored", Toast.LENGTH_SHORT).show();
                            if (position >= 0 && position < materialList.size()) {
                                materialList.remove(position);
                                notifyItemRemoved(position);
                                notifyItemRangeChanged(position, materialList.size());
                            }
                        } else {
                            Toast.makeText(context, "Failed: " + json.optString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(context, "Error deleting material", Toast.LENGTH_SHORT).show()) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("project_id", String.valueOf(projectId));
                params.put("material_id", String.valueOf(materialId));
                return params;
            }
        };
        VolleySingleton.getInstance(context).addToRequestQueue(request);
    }

    @Override
    public int getItemCount() {
        return materialList.size();
    }

    static class MaterialViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvQuantity, tvRemaining, tvSpecifications;
        android.widget.ImageView ivMaterial;

        public MaterialViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvMaterialName);
            tvQuantity = itemView.findViewById(R.id.tvMaterialQuantity);
            tvRemaining = itemView.findViewById(R.id.tvRemaining);
            tvSpecifications = itemView.findViewById(R.id.tvMaterialSpecifications);
            ivMaterial = itemView.findViewById(R.id.ivMaterialImage);
        }
    }

    public static class ProjectMaterial {
        private int materialId;
        private String materialName;
        private String unit;
        private double usedQuantity;
        private int remainingQuantity;
        private String specifications;

        public ProjectMaterial(int materialId, String materialName, String unit, double usedQuantity, int remainingQuantity, String specifications) {
            this.materialId = materialId;
            this.materialName = materialName;
            this.unit = unit;
            this.usedQuantity = usedQuantity;
            this.remainingQuantity = remainingQuantity;
            this.specifications = specifications;
        }
        
        // Constructor for compatibility where specifications isn't provided
        public ProjectMaterial(int materialId, String materialName, String unit, double usedQuantity, int remainingQuantity) {
            this(materialId, materialName, unit, usedQuantity, remainingQuantity, "");
        }

        public int getMaterialId() { return materialId; }
        public String getMaterialName() { return materialName; }
        public String getUnit() { return unit; }
        public double getUsedQuantity() { return usedQuantity; }
        public int getRemainingQuantity() { return remainingQuantity; }
        public String getSpecifications() { return specifications; }
    }
}
