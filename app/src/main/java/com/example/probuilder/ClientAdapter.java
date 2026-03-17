package com.example.probuilder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ClientAdapter extends RecyclerView.Adapter<ClientAdapter.ClientViewHolder> {

    private List<Client> clientList = new ArrayList<>();
    private List<Client> clientListFull = new ArrayList<>();

    // Default constructor
    public ClientAdapter() {}

    public void setClients(List<Client> newList) {
        clientList.clear();
        clientList.addAll(newList);
        clientListFull.clear(); // Also update the full list used for filtering
        clientListFull.addAll(newList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ClientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_client, parent, false);
        return new ClientViewHolder(view);
    }

    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Client client);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @Override
    public void onBindViewHolder(@NonNull ClientViewHolder holder, int position) {
        Client client = clientList.get(position);
        holder.tvClientName.setText(client.name);
        holder.tvClientEmail.setText(client.email);
        holder.tvClientPhone.setText(client.phone);
        
        if (client.name != null && !client.name.isEmpty()) {
            holder.tvClientInitial.setText(String.valueOf(client.name.charAt(0)));
        } else {
            holder.tvClientInitial.setText("?");
        }
        
        // Active projects removed from model, hiding or setting default
        holder.tvActiveProjects.setText("");

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(client);
            }
        });
    }

    @Override
    public int getItemCount() {
        return clientList.size();
    }

    public void filter(String text) {
        clientList.clear();
        if (text.isEmpty()) {
            clientList.addAll(clientListFull);
        } else {
            text = text.toLowerCase();
            for (Client item : clientListFull) {
                if (item.name != null && item.name.toLowerCase().contains(text)) {
                    clientList.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    public static class ClientViewHolder extends RecyclerView.ViewHolder {
        TextView tvClientName, tvActiveProjects, tvClientPhone, tvClientEmail, tvClientInitial;

        public ClientViewHolder(@NonNull View itemView) {
            super(itemView);
            tvClientName = itemView.findViewById(R.id.tvClientName);
            tvActiveProjects = itemView.findViewById(R.id.tvActiveProjects);
            tvClientPhone = itemView.findViewById(R.id.tvClientPhone);
            tvClientEmail = itemView.findViewById(R.id.tvClientEmail);
            tvClientInitial = itemView.findViewById(R.id.tvClientInitial);
        }
    }
}