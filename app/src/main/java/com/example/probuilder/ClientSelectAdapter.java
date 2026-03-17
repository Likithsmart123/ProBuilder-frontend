package com.example.probuilder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class ClientSelectAdapter extends RecyclerView.Adapter<ClientSelectAdapter.ViewHolder> {
    private Context context;
    private List<Client> clientList;
    private List<Client> clientListFull;
    private OnClientClickListener listener;

    public interface OnClientClickListener {
        void onClientClick(Client client);
    }

    public ClientSelectAdapter(Context context, List<Client> clientList, OnClientClickListener listener) {
        this.context = context;
        this.clientList = clientList;
        this.clientListFull = new ArrayList<>(clientList);
        this.listener = listener;
    }

    public void updateData(List<Client> newClientList) {
        this.clientListFull = new ArrayList<>(newClientList);
    }

    public void filter(String text) {
        clientList.clear();
        if (text.isEmpty()) {
            clientList.addAll(clientListFull);
        } else {
            text = text.toLowerCase().trim();
            for (Client item : clientListFull) {
                if ((item.name != null && item.name.toLowerCase().contains(text)) ||
                    (item.email != null && item.email.toLowerCase().contains(text)) ||
                    (item.phone != null && item.phone.toLowerCase().contains(text))) {
                    clientList.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_client_select, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Client client = clientList.get(position);
        holder.tvClientName.setText(client.name);
        
        String detail = "";
        if (client.email != null && !client.email.isEmpty() && !client.email.equals("null")) {
            detail += client.email;
        }
        if (client.phone != null && !client.phone.isEmpty() && !client.phone.equals("null")) {
            if (!detail.isEmpty()) detail += " / ";
            detail += client.phone;
        }
        holder.tvClientEmailOrPhone.setText(detail.isEmpty() ? "No contact info" : detail);

        holder.itemView.setOnClickListener(v -> listener.onClientClick(client));
    }

    @Override
    public int getItemCount() {
        return clientList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvClientName, tvClientEmailOrPhone;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvClientName = itemView.findViewById(R.id.tvClientName);
            tvClientEmailOrPhone = itemView.findViewById(R.id.tvClientEmailOrPhone);
        }
    }
}
