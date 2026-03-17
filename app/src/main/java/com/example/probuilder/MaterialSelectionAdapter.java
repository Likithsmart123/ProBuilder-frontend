package com.example.probuilder;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MaterialSelectionAdapter extends RecyclerView.Adapter<MaterialSelectionAdapter.ViewHolder> {

    private Context context;
    private List<Material> materialList;

    public MaterialSelectionAdapter(Context context, List<Material> materialList) {
        this.context = context;
        this.materialList = materialList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_material_selection, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Material material = materialList.get(position);

        holder.tvMaterialName.setText(material.getName());
        holder.tvAvailable.setText("Available: " + material.getCurrentStock() + " " + material.getUnit());
        holder.tvUnit.setText(material.getUnit());
        holder.cbSelect.setChecked(material.isSelected());

        // Remove listener before setting text to avoid infinite loop
        if (holder.qtyWatcher != null) {
            holder.etUsedQty.removeTextChangedListener(holder.qtyWatcher);
        }

        if (material.getUsedQty() > 0) {
            holder.etUsedQty.setText(String.valueOf(material.getUsedQty()));
        } else {
            holder.etUsedQty.setText("");
        }

        holder.etUsedQty.setEnabled(material.isSelected());

        // Checkbox listener
        holder.cbSelect.setOnCheckedChangeListener((buttonView, isChecked) -> {
            material.setSelected(isChecked);
            holder.etUsedQty.setEnabled(isChecked);
            if (!isChecked) {
                material.setUsedQty(0);
                holder.etUsedQty.setText("");
            }
        });

        // Quantity input listener
        holder.qtyWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    try {
                        double val = Double.parseDouble(s.toString());
                        material.setUsedQty(val);
                    } catch (NumberFormatException e) {
                        material.setUsedQty(0);
                    }
                } else {
                    material.setUsedQty(0);
                }
            }
        };
        holder.etUsedQty.addTextChangedListener(holder.qtyWatcher);
    }

    @Override
    public int getItemCount() {
        return materialList.size();
    }

    // Helper to get selected items
    public List<Material> getAllMaterials() {
        return materialList;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMaterialName, tvAvailable, tvUnit;
        CheckBox cbSelect;
        EditText etUsedQty;
        TextWatcher qtyWatcher;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMaterialName = itemView.findViewById(R.id.tvMaterialName);
            tvAvailable = itemView.findViewById(R.id.tvAvailable);
            tvUnit = itemView.findViewById(R.id.tvUnit);
            cbSelect = itemView.findViewById(R.id.cbSelect);
            etUsedQty = itemView.findViewById(R.id.etUsedQty);
        }
    }
}
