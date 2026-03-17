package com.example.probuilder;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;

    private final Context context;
    private final List<Message> messageList;
    private final int currentUserId;
    private final String currentUserType;

    // Callback for "Reply" action
    public interface OnReplyListener {
        void onReply(Message message);
    }

    private OnReplyListener replyListener;

    public void setOnReplyListener(OnReplyListener listener) {
        this.replyListener = listener;
    }

    public ChatAdapter(Context context, List<Message> messageList) {
        this.context = context;
        this.messageList = messageList;

        SharedPreferences prefs = context.getSharedPreferences("AUTH", Context.MODE_PRIVATE);
        String role = prefs.getString("role", "");
        if (role.equals("contractor")) {
            this.currentUserId = prefs.getInt("contractor_id", -1);
            this.currentUserType = "contractor";
        } else if (role.equals("client")) {
            this.currentUserId = prefs.getInt("client_id", -1);
            this.currentUserType = "client";
        } else {
            this.currentUserId = -1;
            this.currentUserType = "";
        }
    }

    @Override
    public int getItemViewType(int position) {
        Message msg = messageList.get(position);
        boolean isSentByMe = (msg.getSenderId() == currentUserId) && (currentUserType.equals(msg.getSenderType()));
        return isSentByMe ? VIEW_TYPE_SENT : VIEW_TYPE_RECEIVED;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SENT) {
            View v = LayoutInflater.from(context).inflate(R.layout.item_message_sent, parent, false);
            return new SentHolder(v);
        } else {
            View v = LayoutInflater.from(context).inflate(R.layout.item_message_received, parent, false);
            return new ReceivedHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message msg = messageList.get(position);
        String timeStr = formatTime(msg.getCreatedAt());
        
        boolean hasNoText = msg.getMessage() == null || msg.getMessage().trim().isEmpty();
        boolean hasNoImage = msg.getFileUrl() == null || msg.getFileUrl().trim().isEmpty();
        boolean isDeleted = "deleted".equals(msg.getMessageType()) || (hasNoText && hasNoImage);

        if (holder instanceof SentHolder) {
            SentHolder h = (SentHolder) holder;
            bindCommon(h.tvMessage, h.ivMessageImage, h.layoutReply,
                    h.tvReplyName, h.tvReplyText, h.ivStar, msg, isDeleted, "You");
            h.tvTime.setText(timeStr);
            // Read receipt ticks
            if (msg.getIsRead() == 1) {
                h.ivReadStatus.setColorFilter(Color.parseColor("#34B7F1")); // Blue ticks
            } else {
                h.ivReadStatus.setColorFilter(Color.parseColor("#888888")); // Gray ticks
            }
            // Long press context menu
            h.bubbleContainer.setOnLongClickListener(v -> {
                showContextMenu(msg, true);
                return true;
            });
        } else if (holder instanceof ReceivedHolder) {
            ReceivedHolder h = (ReceivedHolder) holder;
            bindCommon(h.tvMessage, h.ivMessageImage, h.layoutReply,
                    h.tvReplyName, h.tvReplyText, h.ivStar, msg, isDeleted, "Them");
            h.tvTime.setText(timeStr);
            h.bubbleContainer.setOnLongClickListener(v -> {
                showContextMenu(msg, false);
                return true;
            });
        }
    }

    private void bindCommon(TextView tvMsg, ImageView ivImg, LinearLayout layoutReply,
                            TextView tvReplyName, TextView tvReplyText, ImageView ivStar,
                            Message msg, boolean isDeleted, String replyLabel) {
        // Deleted message
        if (isDeleted) {
            tvMsg.setVisibility(View.VISIBLE);
            tvMsg.setText("🚫 This message was deleted");
            tvMsg.setTextColor(Color.parseColor("#888888"));
            tvMsg.setTextSize(13f);
            tvMsg.setTypeface(null, android.graphics.Typeface.ITALIC);
            ivImg.setVisibility(View.GONE);
            layoutReply.setVisibility(View.GONE);
            ivStar.setVisibility(View.GONE);
            return;
        }

        // Normal text
        if (msg.getMessage() != null && !msg.getMessage().isEmpty()) {
            tvMsg.setVisibility(View.VISIBLE);
            tvMsg.setText(msg.getMessage());
            tvMsg.setTextColor(Color.parseColor("#1A1A1A"));
            tvMsg.setTextSize(15f);
            tvMsg.setTypeface(null, android.graphics.Typeface.NORMAL);
        } else {
            tvMsg.setVisibility(View.GONE);
        }

        // Image
        if (msg.getFileUrl() != null && !msg.getFileUrl().isEmpty()
                && "image".equals(msg.getMessageType())) {
            ivImg.setVisibility(View.VISIBLE);
            
            String imageUrl = msg.getFileUrl();
            if (!imageUrl.startsWith("http")) {
                imageUrl = Constants.BASE_URL + imageUrl;
            }
            
            Glide.with(context).load(imageUrl).centerCrop().into(ivImg);
            
            final String finalImageUrl = imageUrl;
            ivImg.setOnClickListener(v -> {
                android.content.Intent intent = new android.content.Intent(context, MediaViewerActivity.class);
                intent.putExtra("MEDIA_URL", finalImageUrl);
                intent.putExtra("MEDIA_TYPE", "image");
                context.startActivity(intent);
            });
        } else {
            ivImg.setVisibility(View.GONE);
            ivImg.setOnClickListener(null);
        }

        // Reply preview
        if (msg.getReplyToMessageId() > 0 && msg.getReplyToText() != null) {
            layoutReply.setVisibility(View.VISIBLE);
            tvReplyName.setText(replyLabel);
            String replyPreview = "image".equals(msg.getReplyToType())
                    ? "📷 Photo" : msg.getReplyToText();
            tvReplyText.setText(replyPreview);
        } else {
            layoutReply.setVisibility(View.GONE);
        }

        // Star
        ivStar.setVisibility(msg.isStarred() ? View.VISIBLE : View.GONE);
    }

    private void showContextMenu(Message msg, boolean isSender) {
        String[] allOptions;
        if (isSender) {
            allOptions = new String[]{"Reply", "Copy", "Star", "Forward", "Delete for Me",
                    "Delete for Everyone"};
        } else {
            allOptions = new String[]{"Reply", "Copy", "Star", "Forward", "Delete for Me"};
        }

        new AlertDialog.Builder(context)
                .setItems(allOptions, (dialog, which) -> {
                    switch (which) {
                        case 0: // Reply
                            if (replyListener != null) replyListener.onReply(msg);
                            break;
                        case 1: // Copy
                            if (msg.getMessage() != null && !msg.getMessage().isEmpty()) {
                                ClipboardManager clipboard = (ClipboardManager)
                                        context.getSystemService(Context.CLIPBOARD_SERVICE);
                                ClipData clip = ClipData.newPlainText("message", msg.getMessage());
                                clipboard.setPrimaryClip(clip);
                                Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show();
                            }
                            break;
                        case 2: // Star
                            msg.setStarred(!msg.isStarred());
                            notifyDataSetChanged();
                            Toast.makeText(context, msg.isStarred() ? "Message starred" : "Star removed",
                                    Toast.LENGTH_SHORT).show();
                            break;
                        case 3: // Forward
                            showForwardDialog(msg);
                            break;
                        case 4: // Delete for me
                            deleteMessage(msg, "me");
                            break;
                        case 5: // Delete for everyone (sender only)
                            if (isSender) {
                                new AlertDialog.Builder(context)
                                        .setTitle("Delete for Everyone?")
                                        .setMessage("This message will be deleted for all users.")
                                        .setPositiveButton("Delete", (d, w) -> deleteMessage(msg, "everyone"))
                                        .setNegativeButton("Cancel", null)
                                        .show();
                            }
                            break;
                    }
                })
                .show();
    }

    private void deleteMessage(Message msg, String deleteFor) {
        StringRequest request = new StringRequest(Request.Method.POST,
                Constants.BASE_URL + "delete_message.php",
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        if (json.getString("status").equals("success")) {
                            // WhatsApp style: both 'me' and 'everyone' show deleted placeholder
                            msg.setMessage("");
                            msg.setMessageType("deleted");
                            notifyDataSetChanged();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(context, "Failed to delete", Toast.LENGTH_SHORT).show()) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("message_id", String.valueOf(msg.getId()));
                params.put("sender_id", String.valueOf(currentUserId));
                params.put("delete_for", deleteFor);
                return params;
            }
        };
        Volley.newRequestQueue(context).add(request);
    }

    private void showForwardDialog(Message msg) {
        if (msg.getMessage() == null || msg.getMessage().isEmpty()) {
            Toast.makeText(context, "Cannot forward a deleted message", Toast.LENGTH_SHORT).show();
            return;
        }
        SharedPreferences prefs = context.getSharedPreferences("AUTH", Context.MODE_PRIVATE);
        String role  = prefs.getString("role", "");
        int userId   = role.equals("contractor") ? prefs.getInt("contractor_id", -1) : prefs.getInt("client_id", -1);
        String userType = role.equals("contractor") ? "contractor" : "client";

        String url = Constants.BASE_URL + "get_conversations.php?user_id=" + userId + "&user_type=" + userType;

        StringRequest req = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        org.json.JSONObject json = new org.json.JSONObject(response);
                        org.json.JSONArray arr = json.getJSONArray("conversations");

                        String[] names = new String[arr.length()];
                        int[]   convIds = new int[arr.length()];

                        for (int i = 0; i < arr.length(); i++) {
                            org.json.JSONObject c = arr.getJSONObject(i);
                            names[i]   = c.optString("other_user_name", "Chat " + (i + 1));
                            convIds[i] = c.optInt("conversation_id", -1);
                        }

                        if (names.length == 0) {
                            Toast.makeText(context, "No other conversations to forward to", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        new AlertDialog.Builder(context)
                                .setTitle("Forward to…")
                                .setItems(names, (dialog, which) -> {
                                    int targetConversationId = convIds[which];
                                    if (targetConversationId != -1) {
                                        forwardMessage(msg.getMessage(), targetConversationId, userId);
                                    }
                                })
                                .setNegativeButton("Cancel", null)
                                .show();

                    } catch (Exception e) {
                        Toast.makeText(context, "Failed to load chats", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(context, "Network error", Toast.LENGTH_SHORT).show());

        Volley.newRequestQueue(context).add(req);
    }

    private void forwardMessage(String text, int targetConversationId, int senderId) {
        StringRequest req = new StringRequest(Request.Method.POST,
                Constants.BASE_URL + "send_message.php",
                response -> {
                    try {
                        org.json.JSONObject json = new org.json.JSONObject(response);
                        if (json.getString("status").equals("success")) {
                            Toast.makeText(context, "✓ Message forwarded", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, "Failed to forward", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(context, "Network error", Toast.LENGTH_SHORT).show()) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("conversation_id", String.valueOf(targetConversationId));
                params.put("sender_id", String.valueOf(senderId));
                params.put("message", text);
                params.put("message_type", "text");
                return params;
            }
        };
        Volley.newRequestQueue(context).add(req);
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public Message getMessage(int position) {
        if (position >= 0 && position < messageList.size()) {
            return messageList.get(position);
        }
        return null;
    }

    private String formatTime(String dbTime) {
        if (dbTime == null || dbTime.isEmpty()) return "";

        // Strip fractional seconds if present: "2026-03-06 10:26:41.000000" → "2026-03-06 10:26:41"
        String cleaned = dbTime.replaceAll("\\.\\d+$", "").trim();

        String[] patterns = {
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd HH:mm",
            "HH:mm:ss",
            "HH:mm",
            "yyyy-MM-dd'T'HH:mm:ss",
        };

        SimpleDateFormat outFmt = new SimpleDateFormat("h:mm a", Locale.ENGLISH);
        for (String pattern : patterns) {
            try {
                SimpleDateFormat inFmt = new SimpleDateFormat(pattern, Locale.ENGLISH);
                Date date = inFmt.parse(cleaned);
                if (date != null) return outFmt.format(date);
            } catch (ParseException ignored) {}
        }

        // If already formatted (e.g. "10:28 AM"), return as-is
        return cleaned;
    }

    // ── View Holders ────────────────────────────────────────────────────────────

    static class SentHolder extends RecyclerView.ViewHolder {
        TextView tvMessage, tvTime, tvReplyName, tvReplyText;
        ImageView ivMessageImage, ivReadStatus, ivStar;
        LinearLayout layoutReply, bubbleContainer;

        SentHolder(View v) {
            super(v);
            tvMessage      = v.findViewById(R.id.tvMessage);
            tvTime         = v.findViewById(R.id.tvTime);
            ivMessageImage = v.findViewById(R.id.ivMessageImage);
            ivReadStatus   = v.findViewById(R.id.ivReadStatus);
            ivStar         = v.findViewById(R.id.ivStar);
            layoutReply    = v.findViewById(R.id.layoutReply);
            tvReplyName    = v.findViewById(R.id.tvReplyName);
            tvReplyText    = v.findViewById(R.id.tvReplyText);
            bubbleContainer = v.findViewById(R.id.bubbleContainer);
        }
    }

    static class ReceivedHolder extends RecyclerView.ViewHolder {
        TextView tvMessage, tvTime, tvReplyName, tvReplyText;
        ImageView ivMessageImage, ivStar;
        LinearLayout layoutReply, bubbleContainer;

        ReceivedHolder(View v) {
            super(v);
            tvMessage      = v.findViewById(R.id.tvMessage);
            tvTime         = v.findViewById(R.id.tvTime);
            ivMessageImage = v.findViewById(R.id.ivMessageImage);
            ivStar         = v.findViewById(R.id.ivStar);
            layoutReply    = v.findViewById(R.id.layoutReply);
            tvReplyName    = v.findViewById(R.id.tvReplyName);
            tvReplyText    = v.findViewById(R.id.tvReplyText);
            bubbleContainer = v.findViewById(R.id.bubbleContainer);
        }
    }
}
