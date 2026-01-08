package com.example.probuilder;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class SupplierAdapter extends RecyclerView.Adapter<SupplierAdapter.SupplierViewHolder> {

    private final List<Supplier> supplierList;

    public SupplierAdapter(List<Supplier> supplierList) {
        this.supplierList = supplierList;
    }

    @NonNull
    @Override
    public SupplierViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_supplier, parent, false);
        return new SupplierViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SupplierViewHolder holder, int position) {
        Supplier supplier = supplierList.get(position);
        holder.tvSupplierName.setText(supplier.getName());
        holder.tvMaterialType.setText(supplier.getMaterialType());
        holder.tvContactNumber.setText(supplier.getContactNumber());
        holder.tvStatus.setText(supplier.getStatus());
        holder.tvRating.setText(String.valueOf(supplier.getRating()));

        // Status Styling
        if ("Active".equalsIgnoreCase(supplier.getStatus())) {
            holder.tvStatus.setTextColor(Color.parseColor("#1E6FE3")); // Blue
            holder.tvStatus.setBackgroundResource(R.drawable.rounded_square_bg_light_blue);
        } else {
            holder.tvStatus.setTextColor(Color.parseColor("#757575")); // Gray
            holder.tvStatus.setBackgroundResource(R.drawable.rounded_square_bg_light_gray);
        }
    }

    @Override
    public int getItemCount() {
        return supplierList.size();
    }

    public static class SupplierViewHolder extends RecyclerView.ViewHolder {
        TextView tvSupplierName, tvMaterialType, tvContactNumber, tvStatus, tvRating;

        public SupplierViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSupplierName = itemView.findViewById(R.id.tvSupplierName);
            tvMaterialType = itemView.findViewById(R.id.tvMaterialType);
            tvContactNumber = itemView.findViewById(R.id.tvContactNumber);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvRating = itemView.findViewById(R.id.tvRating);
        }
    }
}
