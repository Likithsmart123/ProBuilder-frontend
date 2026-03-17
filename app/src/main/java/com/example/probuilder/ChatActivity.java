package com.example.probuilder;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView chatRecycler;
    private EditText etMessage;
    private ImageView btnSend, btnBack, btnQuickActions, btnAttach;
    private TextView tvChatName, tvOnlineStatus;
    private LinearLayout layoutQuickActions;
    private TextView btnActionUpdate, btnActionPhoto, btnActionPayment;
    private ImageView ivProfilePic;

    private ChatAdapter chatAdapter;
    private List<Message> messageList;

    private int currentUserId;
    private String currentUserType;
    private int conversationId = -1;
    private int otherUserId;
    private String otherUserName;
    private int projectId;

    private Handler handler;
    private Runnable pollingRunnable;
    private static final int POLLING_INTERVAL = 2000; // 2 seconds

    private RequestQueue requestQueue;
    private int lastMsgId = 0;
    private String pendingMessageText = null;
    private String pendingMessageType = null;
    private String pendingMessageFileUrl = null;

    // Reply state
    private Message replyingToMessage = null;
    private LinearLayout layoutReplyBar;
    private TextView tvReplyBarName, tvReplyBarText;
    private ImageView btnCancelReply;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Init views
        chatRecycler = findViewById(R.id.chatRecycler);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        btnBack = findViewById(R.id.btnBack);
        btnQuickActions = findViewById(R.id.btnQuickActions);
        btnAttach = findViewById(R.id.btnAttach);
        tvChatName = findViewById(R.id.tvChatName);
        tvOnlineStatus = findViewById(R.id.tvOnlineStatus);
        layoutQuickActions = findViewById(R.id.layoutQuickActions);
        btnActionUpdate = findViewById(R.id.btnActionUpdate);
        btnActionPhoto = findViewById(R.id.btnActionPhoto);
        btnActionPayment = findViewById(R.id.btnActionPayment);
        ivProfilePic = findViewById(R.id.ivProfilePic);

        requestQueue = Volley.newRequestQueue(this);
        messageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(this, messageList);

        // Reply bar views
        layoutReplyBar  = findViewById(R.id.layoutReplyBar);
        tvReplyBarName  = findViewById(R.id.tvReplyBarName);
        tvReplyBarText  = findViewById(R.id.tvReplyBarText);
        btnCancelReply  = findViewById(R.id.btnCancelReply);
        btnCancelReply.setOnClickListener(v -> clearReply());

        // Reply listener from adapter long-press
        chatAdapter.setOnReplyListener(msg -> {
            replyingToMessage = msg;
            layoutReplyBar.setVisibility(View.VISIBLE);
            tvReplyBarName.setText(msg.getSenderId() == currentUserId ? "You" : otherUserName);
            String preview = "image".equals(msg.getMessageType()) ? "📷 Photo" : msg.getMessage();
            tvReplyBarText.setText(preview);
            etMessage.requestFocus();
        });
        
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true); // Start from bottom
        chatRecycler.setLayoutManager(layoutManager);
        chatRecycler.setAdapter(chatAdapter);

        // Attach Swipe-to-Reply
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new SwipeToReplyCallback(this, chatAdapter, msg -> {
            replyingToMessage = msg;
            layoutReplyBar.setVisibility(View.VISIBLE);
            tvReplyBarName.setText(msg.getSenderId() == currentUserId ? "You" : otherUserName);
            String preview = "image".equals(msg.getMessageType()) ? "📷 Photo" : msg.getMessage();
            tvReplyBarText.setText(preview);
            etMessage.requestFocus();
        }));
        itemTouchHelper.attachToRecyclerView(chatRecycler);

        // Get from Intent or SharedPreferences
        SharedPreferences prefs = getSharedPreferences("AUTH", Context.MODE_PRIVATE);
        String role = prefs.getString("role", "");
        if (role.equals("contractor")) {
            currentUserId = prefs.getInt("contractor_id", -1);
            currentUserType = "contractor";
        } else if (role.equals("client")) {
            currentUserId = prefs.getInt("client_id", -1);
            currentUserType = "client";
        } else {
            currentUserId = -1;
            currentUserType = "";
        } // 'contractor' or 'client'

        conversationId = getIntent().getIntExtra("CONVERSATION_ID", -1);
        otherUserId = getIntent().getIntExtra("OTHER_USER_ID", -1);
        otherUserName = getIntent().getStringExtra("OTHER_USER_NAME");
        projectId = getIntent().getIntExtra("PROJECT_ID", -1);

        if (otherUserName != null) {
            tvChatName.setText(otherUserName);
        }

        btnBack.setOnClickListener(v -> finish());

        // Clicking the name or profile pic opens the WhatsApp-style contact info screen
        View.OnClickListener openInfo = v -> {
            String type = currentUserType.equals("contractor") ? "client" : "contractor";
            Intent infoIntent = new Intent(this, ChatInfoActivity.class);
            infoIntent.putExtra("CONVERSATION_ID", conversationId);
            infoIntent.putExtra("OTHER_USER_ID", otherUserId);
            infoIntent.putExtra("OTHER_USER_NAME", otherUserName);
            infoIntent.putExtra("OTHER_USER_TYPE", type);
            startActivity(infoIntent);
        };
        tvChatName.setOnClickListener(openInfo);
        ivProfilePic.setOnClickListener(openInfo);
        
        btnAttach.setOnClickListener(v -> openImagePicker());

        btnQuickActions.setOnClickListener(v -> {
            if (layoutQuickActions.getVisibility() == View.VISIBLE) {
                layoutQuickActions.setVisibility(View.GONE);
            } else {
                layoutQuickActions.setVisibility(View.VISIBLE);
            }
        });

        btnSend.setOnClickListener(v -> {
            String text = etMessage.getText().toString().trim();
            if (!text.isEmpty()) {
                sendMessage(text, "text", null);
            }
        });

        // Quick Actions logic
        btnActionUpdate.setOnClickListener(v -> {
            etMessage.setText("Project Update: ");
            etMessage.setSelection(etMessage.getText().length());
            layoutQuickActions.setVisibility(View.GONE);
        });
        
        btnActionPayment.setOnClickListener(v -> {
            etMessage.setText("Payment Details: ");
            etMessage.setSelection(etMessage.getText().length());
            layoutQuickActions.setVisibility(View.GONE);
        });
        
        btnActionPhoto.setOnClickListener(v -> {
            openImagePicker();
            layoutQuickActions.setVisibility(View.GONE);
        });

        // Setup polling handler BEFORE using it in TextWatcher
        handler = new Handler(Looper.getMainLooper());
        
        // Typing Status logic
        Runnable typingTimeoutRunnable = () -> updateOwnStatus(false);
        
        etMessage.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateOwnStatus(true);
                handler.removeCallbacks(typingTimeoutRunnable);
                handler.postDelayed(typingTimeoutRunnable, 1500); // 1.5 seconds typing timeout
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        pollingRunnable = new Runnable() {
            @Override
            public void run() {
                if (conversationId != -1) {
                    loadMessages();
                    pollStatus();
                    markAsRead();
                }
                handler.postDelayed(this, POLLING_INTERVAL);
            }
        };

        if (conversationId == -1 && otherUserId != -1 && projectId != -1) {
            // Need to get or create conversation first (e.g., opened from Project Detail)
            initConversation();
        }
    }
    
    // Image Upload Logic
    private final androidx.activity.result.ActivityResultLauncher<android.content.Intent> imagePickerLauncher = registerForActivityResult(
            new androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    android.net.Uri selectedImageUri = result.getData().getData();
                    if (selectedImageUri != null) {
                        uploadImage(selectedImageUri);
                    }
                }
            }
    );

    private void openImagePicker() {
        android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }
    
    private void uploadImage(android.net.Uri imageUri) {
        Toast.makeText(this, "Uploading image...", Toast.LENGTH_SHORT).show();
        
        // Convert URI to Base64
        try {
            java.io.InputStream inputStream = getContentResolver().openInputStream(imageUri);
            android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeStream(inputStream);
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 70, baos);
            byte[] imageBytes = baos.toByteArray();
            String base64Image = android.util.Base64.encodeToString(imageBytes, android.util.Base64.DEFAULT);
            
            // Send to server using Multipart Request to avoid base64 corruption
            String url = Constants.BASE_URL + "send_message.php";
            
            VolleyMultipartRequest request = new VolleyMultipartRequest(Request.Method.POST, url,
                    response -> {
                        try {
                            String responseString = new String(response.data);
                            JSONObject jsonObject = new JSONObject(responseString);
                            if (jsonObject.getString("status").equals("success")) {
                                loadMessages(); // Refresh chat
                            } else {
                                Toast.makeText(this, "Upload failed: " + jsonObject.optString("message"), Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    },
                    error -> Toast.makeText(this, "Upload Network error", Toast.LENGTH_SHORT).show()) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("conversation_id", String.valueOf(conversationId));
                    params.put("sender_id", String.valueOf(currentUserId));
                    params.put("sender_type", currentUserType);
                    params.put("message_type", "image");
                    return params;
                }
                
                @Override
                protected Map<String, DataPart> getByteData() {
                    Map<String, DataPart> params = new HashMap<>();
                    long imagename = System.currentTimeMillis();
                    params.put("file", new DataPart(imagename + ".jpg", imageBytes, "image/jpeg"));
                    return params;
                }
            };
            requestQueue.add(request);
            
        } catch (java.io.IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to read image", Toast.LENGTH_SHORT).show();
        }
    }

    private void initConversation() {
        String url = Constants.BASE_URL + "get_or_create_conversation.php";
        
        int contractorId = currentUserType.equals("contractor") ? currentUserId : otherUserId;
        int clientId = currentUserType.equals("client") ? currentUserId : otherUserId;

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.getString("status").equals("success")) {
                            conversationId = jsonObject.getInt("conversation_id");

                            // Immediately load existing messages (don't wait for next poll cycle)
                            lastMsgId = 0;
                            loadMessages();

                            // Send any pending message now that we have the ID
                            if (pendingMessageText != null || pendingMessageFileUrl != null) {
                                sendMessage(pendingMessageText, pendingMessageType, pendingMessageFileUrl);
                                // Clear pending
                                pendingMessageText = null;
                                pendingMessageType = null;
                                pendingMessageFileUrl = null;
                            }
                        } else {
                            Toast.makeText(this, "Failed to load chat", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Log.e("ChatAPI", "Error initializing conversation", error)) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("contractor_id", String.valueOf(contractorId));
                params.put("client_id", String.valueOf(clientId));
                params.put("project_id", String.valueOf(projectId));
                return params;
            }
        };
        requestQueue.add(request);
    }

    private void loadMessages() {
        if (conversationId == -1) return;

        String url = Constants.BASE_URL + "get_messages.php?conversation_id=" + conversationId + "&last_msg_id=" + lastMsgId + "&user_id=" + currentUserId;

        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.getString("status").equals("success")) {
                            JSONArray data = jsonObject.getJSONArray("data");
                            boolean hasNewMessages = false;
                            
                            for (int i = 0; i < data.length(); i++) {
                                JSONObject msgObj = data.getJSONObject(i);
                                int msgId = msgObj.getInt("id");
                                
                                if (msgId > lastMsgId) {
                                    Message msg = new Message(
                                            msgId,
                                            msgObj.getInt("conversation_id"),
                                            msgObj.getInt("sender_id"),
                                            msgObj.optString("sender_type", ""),
                                            msgObj.optString("message"),
                                            msgObj.optString("message_type"),
                                            msgObj.optString("file_url"),
                                            msgObj.getInt("is_read"),
                                            msgObj.getString("created_at")
                                    );
                                    // Reply fields
                                    int replyId = msgObj.optInt("reply_to_message_id", 0);
                                    if (replyId > 0) {
                                        msg.setReplyToMessageId(replyId);
                                        msg.setReplyToText(msgObj.optString("reply_to_text", ""));
                                        msg.setReplyToType(msgObj.optString("reply_to_type", "text"));
                                    }
                                    messageList.add(msg);
                                    lastMsgId = msgId;
                                    hasNewMessages = true;
                                }
                            }

                            // Sync deleted messages
                            if (jsonObject.has("deleted_ids")) {
                                JSONArray deletedIds = jsonObject.getJSONArray("deleted_ids");
                                for (int i = 0; i < deletedIds.length(); i++) {
                                    int delId = deletedIds.getInt(i);
                                    for (Message msg : messageList) {
                                        if (msg.getId() == delId && !"deleted".equals(msg.getMessageType())) {
                                            msg.setMessage("");
                                            msg.setMessageType("deleted");
                                            hasNewMessages = true; // Trigger UI refresh
                                            break;
                                        }
                                    }
                                }
                            }

                            // Sync read receipts
                            if (jsonObject.has("last_read_id")) {
                                int lastReadId = jsonObject.getInt("last_read_id");
                                for (Message msg : messageList) {
                                    if (msg.getSenderId() == currentUserId && msg.getId() <= lastReadId && msg.getIsRead() == 0) {
                                        msg.setIsRead(1);
                                        hasNewMessages = true; // Trigger UI refresh for blue ticks
                                    }
                                }
                            }

                            if (hasNewMessages) {
                                chatAdapter.notifyDataSetChanged();
                                chatRecycler.scrollToPosition(messageList.size() - 1);
                            }
                        }
                    } catch (JSONException e) {
                        Log.e("ChatAPI", "JSON Parse Error: " + response);
                    }
                },
                error -> Log.e("ChatAPI", "Load Error", error));
        
        requestQueue.add(request);
    }

    private void sendMessage(String text, String type, String fileUrl) {
        final String msgText = (text == null) ? "" : text;
        final String msgType = (type != null) ? type : "text";

        if (msgText.isEmpty() && fileUrl == null) return;

        if (conversationId == -1) {
            pendingMessageText = msgText;
            pendingMessageType = msgType;
            pendingMessageFileUrl = fileUrl;
            initConversation();
            return;
        }

        // Clear input immediately
        etMessage.setText("");

        // Capture reply context before clearing
        final Message replyMsg = replyingToMessage;
        clearReply();

        // Optimistic UI: add message to list right away (like WhatsApp)
        String now = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                java.util.Locale.ENGLISH).format(new java.util.Date());
        Message optimisticMsg = new Message(
                Integer.MAX_VALUE, conversationId, currentUserId, currentUserType,
                msgText, msgType, fileUrl, 0, now);
        if (replyMsg != null) {
            optimisticMsg.setReplyToMessageId(replyMsg.getId());
            optimisticMsg.setReplyToText(replyMsg.getMessage());
            optimisticMsg.setReplyToType(replyMsg.getMessageType());
        }
        messageList.add(optimisticMsg);
        chatAdapter.notifyItemInserted(messageList.size() - 1);
        chatRecycler.scrollToPosition(messageList.size() - 1);

        String url = Constants.BASE_URL + "send_message.php";

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (!jsonObject.getString("status").equals("success")) {
                            // Roll back optimistic message on failure
                            messageList.remove(optimisticMsg);
                            chatAdapter.notifyDataSetChanged();
                            Toast.makeText(this, "Failed to send", Toast.LENGTH_SHORT).show();
                        } else {
                            // Update the temporary ID with the real server-assigned ID
                            int realId = jsonObject.optInt("message_id", Integer.MAX_VALUE);
                            optimisticMsg.setId(realId);
                            if (realId > lastMsgId) lastMsgId = realId;
                            chatAdapter.notifyDataSetChanged();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    // Roll back on network error
                    messageList.remove(optimisticMsg);
                    chatAdapter.notifyDataSetChanged();
                    Toast.makeText(this, "Network error. Try again.", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("conversation_id", String.valueOf(conversationId));
                params.put("sender_id", String.valueOf(currentUserId));
                params.put("sender_type", currentUserType);
                params.put("message", msgText);
                params.put("message_type", msgType);
                if (fileUrl != null) params.put("file_url", fileUrl);
                if (replyMsg != null) params.put("reply_to_message_id", String.valueOf(replyMsg.getId()));
                return params;
            }
        };
        requestQueue.add(request);
    }

    private void clearReply() {
        replyingToMessage = null;
        layoutReplyBar.setVisibility(View.GONE);
        tvReplyBarText.setText("");
    }

    private void pollStatus() {
        if (conversationId == -1 || otherUserId == -1) return;

        String otherType = currentUserType.equals("contractor") ? "client" : "contractor";
        String url = Constants.BASE_URL + "get_chat_status.php?conversation_id=" + conversationId + "&other_user_id=" + otherUserId + "&other_user_type=" + otherType;

        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.getString("status").equals("success")) {
                            boolean isOnline = jsonObject.getBoolean("is_online");
                            boolean isTyping = jsonObject.getBoolean("is_typing");
                            
                            if (isTyping) {
                                tvOnlineStatus.setText("typing...");
                                tvOnlineStatus.setTextColor(Color.parseColor("#4CAF50"));
                            } else if (isOnline) {
                                tvOnlineStatus.setText("Online");
                                tvOnlineStatus.setTextColor(Color.parseColor("#4CAF50"));
                            } else {
                                tvOnlineStatus.setText("Offline");
                                tvOnlineStatus.setTextColor(Color.parseColor("#DDDDDD"));
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Log.e("ChatAPI", "Status Polling Error", error));
        
        requestQueue.add(request);
        
        // Also update our own status
        updateOwnStatus(false);
    }
    
    private void updateOwnStatus(boolean isTyping) {
        String url = Constants.BASE_URL + "update_status.php";
        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {}, error -> {}) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("user_id", String.valueOf(currentUserId));
                params.put("user_type", currentUserType);
                if (conversationId != -1) {
                    params.put("conversation_id", String.valueOf(conversationId));
                    params.put("is_typing", String.valueOf(isTyping ? 1 : 0));
                }
                return params;
            }
        };
        requestQueue.add(request);
    }

    private void markAsRead() {
        if (conversationId == -1) return;
        String url = Constants.BASE_URL + "update_read_status.php";
        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {}, error -> {}) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("conversation_id", String.valueOf(conversationId));
                params.put("user_id", String.valueOf(currentUserId));
                return params;
            }
        };
        requestQueue.add(request);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reset so we load ALL messages fresh each time the screen is opened
        messageList.clear();
        lastMsgId = 0;
        chatAdapter.notifyDataSetChanged();
        handler.post(pollingRunnable); // Start polling
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(pollingRunnable); // Stop polling
    }
}
