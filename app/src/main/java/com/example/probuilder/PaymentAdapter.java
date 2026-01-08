package com.example.probuilder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Locale;

public class PaymentAdapter extends RecyclerView.Adapter<PaymentAdapter.PaymentViewHolder> {

    private final List<Payment> paymentList;

    public PaymentAdapter(List<Payment> paymentList) {
        this.paymentList = paymentList;
    }

    @NonNull
    @Override
    public PaymentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_payment, parent, false);
        return new PaymentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PaymentViewHolder holder, int position) {
        Payment payment = paymentList.get(position);
        holder.tvClientName.setText(payment.getClientName());
        holder.tvProjectName.setText(payment.getProjectName());
        holder.tvAmount.setText(String.format(Locale.getDefault(), "₹ %,.2f", payment.getAmount()));
        holder.tvDate.setText(payment.getDate());
        holder.tvStatus.setText(payment.getStatus());
        holder.tvPaymentMethod.setText(payment.getPaymentMethod());
        
        // Simple color coding for status
        if ("Pending".equalsIgnoreCase(payment.getStatus())) {
            holder.tvStatus.setTextColor(android.graphics.Color.parseColor("#F57F17")); // Orange
            holder.tvStatus.setBackgroundResource(R.drawable.stock_alert_low_background); // Reusing suitable background
        } else {
             holder.tvStatus.setTextColor(android.graphics.Color.parseColor("#1E6FE3")); // Blue
             holder.tvStatus.setBackgroundResource(R.drawable.rounded_square_bg_light_blue);
        }
    }

    @Override
    public int getItemCount() {
        return paymentList.size();
    }

    public static class PaymentViewHolder extends RecyclerView.ViewHolder {
        TextView tvClientName, tvProjectName, tvAmount, tvDate, tvStatus, tvPaymentMethod;

        public PaymentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvClientName = itemView.findViewById(R.id.tvClientName);
            tvProjectName = itemView.findViewById(R.id.tvProjectName);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvPaymentMethod = itemView.findViewById(R.id.tvPaymentMethod);
        }
    }
}
