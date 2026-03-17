package com.example.probuilder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Locale;
import android.widget.ImageView;
import android.widget.Toast;
import android.app.AlertDialog;
import android.content.Context;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

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

    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Payment payment);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @Override
    public void onBindViewHolder(@NonNull PaymentViewHolder holder, int position) {
        Payment payment = paymentList.get(position);
        
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(payment);
            }
        });
        
        // "Paid by: Client"
        holder.tvClientName.setText("Paid by: " + payment.getClientName());
        
        // "Project: Name"
        holder.tvProjectName.setText("Project: " + payment.getProjectName());
        
        // Amount
        holder.tvAmount.setText(String.format(Locale.getDefault(), "₹ %,.0f", payment.getAmount()));
        
        // Date: "Date: 21 Jan 2026"
        holder.tvDate.setText("Date: " + payment.getDate());
        
        // Mode: "Mode: UPI"
        holder.tvPaymentMethod.setText("Mode: " + payment.getPaymentMethod());

        // Note
        if (payment.getNotes() != null && !payment.getNotes().isEmpty()) {
            holder.tvNote.setVisibility(View.VISIBLE);
            holder.tvNote.setText("Note: " + payment.getNotes());
        } else {
            holder.tvNote.setVisibility(View.GONE);
        }

        ImageView ivDeletePayment = holder.itemView.findViewById(R.id.ivDeletePayment);
        if (ivDeletePayment != null) {
            ivDeletePayment.setOnClickListener(v -> {
                new AlertDialog.Builder(v.getContext())
                        .setTitle("Delete Payment")
                        .setMessage("Are you sure you want to delete this payment?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            deletePayment(v.getContext(), payment.getId(), position);
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });
        }
    }

    private void deletePayment(Context context, int id, int position) {
        String url = Constants.BASE_URL + "delete_payment.php";
        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        if (json.optString("status").equals("success")) {
                            Toast.makeText(context, "Payment deleted", Toast.LENGTH_SHORT).show();
                            if (position >= 0 && position < paymentList.size()) {
                                paymentList.remove(position);
                                notifyItemRemoved(position);
                                notifyItemRangeChanged(position, paymentList.size());
                            }
                        } else {
                            Toast.makeText(context, "Failed: " + json.optString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(context, "Error deleting payment", Toast.LENGTH_SHORT).show()) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("payment_id", String.valueOf(id));
                return params;
            }
        };
        VolleySingleton.getInstance(context).addToRequestQueue(request);
    }

    @Override
    public int getItemCount() {
        return paymentList.size();
    }

    public static class PaymentViewHolder extends RecyclerView.ViewHolder {
        TextView tvClientName, tvProjectName, tvAmount, tvDate, tvPaymentMethod, tvNote;

        public PaymentViewHolder(@NonNull View itemView) {
            super(itemView);
            // Re-map IDs based on new layout
            tvClientName = itemView.findViewById(R.id.tvClientName);
            tvProjectName = itemView.findViewById(R.id.tvProjectName);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvPaymentMethod = itemView.findViewById(R.id.tvPaymentMethod);
            tvNote = itemView.findViewById(R.id.tvNote);
        }
    }
}
