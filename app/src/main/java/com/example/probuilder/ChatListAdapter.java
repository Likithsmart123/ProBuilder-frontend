package com.example.probuilder;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ConvViewHolder> {

    private Context context;
    private List<Conversation> conversationList;

    public ChatListAdapter(Context context, List<Conversation> conversationList) {
        this.context = context;
        this.conversationList = conversationList;
    }

    @NonNull
    @Override
    public ConvViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_conversation, parent, false);
        return new ConvViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ConvViewHolder holder, int position) {
        Conversation conv = conversationList.get(position);

        holder.tvUserName.setText(conv.getOtherUserName());
        
        if (conv.getLastMessage().isEmpty()) {
            holder.tvLastMessage.setText("Attachment/Start of conversation");
            holder.tvLastMessage.setTypeface(null, Typeface.ITALIC);
        } else {
            holder.tvLastMessage.setText(conv.getLastMessage());
            holder.tvLastMessage.setTypeface(null, Typeface.NORMAL);
        }
        
        holder.tvLastMessageTime.setText(formatTime(conv.getLastMessageTime()));

        if (conv.getUnreadCount() > 0) {
            holder.tvUnreadCount.setVisibility(View.VISIBLE);
            holder.tvUnreadCount.setText(String.valueOf(conv.getUnreadCount()));
            holder.tvLastMessage.setTypeface(null, Typeface.BOLD);
            holder.tvLastMessage.setTextColor(Color.parseColor("#333333"));
            holder.tvLastMessageTime.setTextColor(Color.parseColor("#00897B"));
        } else {
            holder.tvUnreadCount.setVisibility(View.GONE);
            holder.tvLastMessage.setTypeface(null, Typeface.NORMAL);
            holder.tvLastMessage.setTextColor(Color.parseColor("#666666"));
            holder.tvLastMessageTime.setTextColor(Color.parseColor("#888888"));
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("CONVERSATION_ID", conv.getConversationId());
            intent.putExtra("OTHER_USER_ID", conv.getOtherUserId());
            intent.putExtra("OTHER_USER_NAME", conv.getOtherUserName());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return conversationList.size();
    }

    private String formatTime(String dbTime) {
        if (dbTime == null || dbTime.isEmpty()) return "";
        try {
            SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date date = dbFormat.parse(dbTime);
            SimpleDateFormat uiFormat = new SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault());
            return uiFormat.format(date);
        } catch (ParseException e) {
            return dbTime;
        }
    }

    public void filterList(List<Conversation> filteredList) {
        this.conversationList = filteredList;
        notifyDataSetChanged();
    }

    static class ConvViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserName, tvLastMessage, tvLastMessageTime, tvUnreadCount;
        ImageView ivUserImage;

        ConvViewHolder(View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvLastMessage = itemView.findViewById(R.id.tvLastMessage);
            tvLastMessageTime = itemView.findViewById(R.id.tvLastMessageTime);
            tvUnreadCount = itemView.findViewById(R.id.tvUnreadCount);
            ivUserImage = itemView.findViewById(R.id.ivUserImage);
        }
    }
}
