package com.example.probuilder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class AddStockAdapter extends RecyclerView.Adapter<AddStockAdapter.AddStockViewHolder> {

    private List<Material> materialList = new ArrayList<>();
    // You can add a listener here to pass quantity changes back to the activity

    public void setMaterials(List<Material> materials) {
        this.materialList.clear();
        this.materialList.addAll(materials);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AddStockViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_add_stock, parent, false);
        return new AddStockViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AddStockViewHolder holder, int position) {
        Material material = materialList.get(position);
        holder.tvMaterialName.setText(material.getName());
        // Assuming you might add a category field to your Material model later
        holder.tvCategory.setText("Construction"); 
        holder.tvCurrentStock.setText("Current: " + material.getCurrentStock() + " " + material.getUnit());
        holder.tvAddStockLabel.setText("Add Stock (" + material.getUnit() + ")");

        // Logic for the stepper
        holder.btnIncrease.setOnClickListener(v -> {
            int currentQuantity = Integer.parseInt(holder.etQuantityToAdd.getText().toString());
            holder.etQuantityToAdd.setText(String.valueOf(currentQuantity + 1));
        });

        holder.btnDecrease.setOnClickListener(v -> {
            int currentQuantity = Integer.parseInt(holder.etQuantityToAdd.getText().toString());
            if (currentQuantity > 0) {
                holder.etQuantityToAdd.setText(String.valueOf(currentQuantity - 1));
            }
        });
    }

    @Override
    public int getItemCount() {
        return materialList.size();
    }

    static class AddStockViewHolder extends RecyclerView.ViewHolder {
        TextView tvMaterialName, tvCategory, tvCurrentStock, tvAddStockLabel;
        MaterialButton btnDecrease, btnIncrease;
        EditText etQuantityToAdd;

        public AddStockViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMaterialName = itemView.findViewById(R.id.tvMaterialName);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvCurrentStock = itemView.findViewById(R.id.tvCurrentStock);
            tvAddStockLabel = itemView.findViewById(R.id.tvAddStockLabel);
            btnDecrease = itemView.findViewById(R.id.btnDecrease);
            btnIncrease = itemView.findViewById(R.id.btnIncrease);
            etQuantityToAdd = itemView.findViewById(R.id.etQuantityToAdd);
        }
    }
}
