package com.example.probuilder;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ChatInfoActivity extends AppCompatActivity {

    private TextView tvContactName, tvContactSubtitle, tvPhone, tvEmail, tvMediaCount, tvNoMedia;
    private ImageView ivProfilePicLarge;
    private LinearLayout layoutMediaGrid, btnDeleteChat;

    private int conversationId;
    private int otherUserId;
    private String otherUserName;
    private String otherUserType;
    private int currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_info);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        // Views
        tvContactName    = findViewById(R.id.tvContactName);
        tvContactSubtitle = findViewById(R.id.tvContactSubtitle);
        tvPhone          = findViewById(R.id.tvPhone);
        tvEmail          = findViewById(R.id.tvEmail);
        tvMediaCount     = findViewById(R.id.tvMediaCount);
        tvNoMedia        = findViewById(R.id.tvNoMedia);
        ivProfilePicLarge = findViewById(R.id.ivProfilePicLarge);
        layoutMediaGrid  = findViewById(R.id.layoutMediaGrid);
        btnDeleteChat    = findViewById(R.id.btnDeleteChat);

        // Get intent data
        conversationId = getIntent().getIntExtra("CONVERSATION_ID", -1);
        otherUserId    = getIntent().getIntExtra("OTHER_USER_ID", -1);
        otherUserName  = getIntent().getStringExtra("OTHER_USER_NAME");
        otherUserType  = getIntent().getStringExtra("OTHER_USER_TYPE");

        // Get current user ID from session
        SharedPreferences prefs = getSharedPreferences("AUTH", Context.MODE_PRIVATE);
        String role = prefs.getString("role", "");
        currentUserId = role.equals("contractor")
                ? prefs.getInt("contractor_id", -1)
                : prefs.getInt("client_id", -1);

        if (otherUserName != null) {
            tvContactName.setText(otherUserName);
        }

        // If other user is a client show "ProBuilder Client" else "Contractor"
        if ("client".equals(otherUserType)) {
            tvContactSubtitle.setText("ProBuilder Client");
        } else {
            tvContactSubtitle.setText("ProBuilder Contractor");
        }

        // Delete chat button
        btnDeleteChat.setOnClickListener(v -> confirmDeleteChat());

        // Load data from server
        if (conversationId != -1 && otherUserId != -1 && otherUserType != null) {
            fetchChatInfo();
        }
    }

    private void fetchChatInfo() {
        String url = Constants.BASE_URL + "get_chat_info.php"
                + "?conversation_id=" + conversationId
                + "&other_user_id=" + otherUserId
                + "&other_user_type=" + otherUserType;

        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        if (json.getString("status").equals("success")) {
                            // Contact info
                            JSONObject contact = json.getJSONObject("contact");
                            String name  = contact.optString("name", otherUserName);
                            String phone = contact.optString("phone", "Not available");
                            String email = contact.optString("email", "Not available");

                            tvContactName.setText(name);
                            tvPhone.setText(phone.isEmpty() ? "Not available" : phone);
                            tvEmail.setText(email.isEmpty() ? "Not available" : email);

                            // Shared media
                            JSONArray media = json.getJSONArray("media");
                            tvMediaCount.setText(String.valueOf(media.length()));

                            if (media.length() == 0) {
                                tvNoMedia.setVisibility(View.VISIBLE);
                                layoutMediaGrid.setVisibility(View.GONE);
                            } else {
                                tvNoMedia.setVisibility(View.GONE);
                                layoutMediaGrid.setVisibility(View.VISIBLE);
                                populateMediaGrid(media);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(this, "Failed to load info", Toast.LENGTH_SHORT).show());

        Volley.newRequestQueue(this).add(request);
    }

    private void populateMediaGrid(JSONArray media) {
        layoutMediaGrid.removeAllViews();
        int sizePx = (int) (96 * getResources().getDisplayMetrics().density);
        int marginPx = (int) (4 * getResources().getDisplayMetrics().density);
        int cornerPx = (int) (8 * getResources().getDisplayMetrics().density);

        for (int i = 0; i < media.length(); i++) {
            try {
                JSONObject item = media.getJSONObject(i);
                String fileUrl = item.optString("file_url", "");
                if (fileUrl.isEmpty()) continue;

                ImageView imgView = new ImageView(this);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(sizePx, sizePx);
                params.setMargins(marginPx, 0, marginPx, 0);
                imgView.setLayoutParams(params);
                imgView.setScaleType(ImageView.ScaleType.CENTER_CROP);

                // Rounded corners via background
                android.graphics.drawable.GradientDrawable bg = new android.graphics.drawable.GradientDrawable();
                bg.setCornerRadius(cornerPx);
                bg.setColor(0xFFE0E0E0);
                imgView.setBackground(bg);
                imgView.setClipToOutline(true);

                Glide.with(this).load(fileUrl).centerCrop().into(imgView);

                final String url = fileUrl;
                imgView.setOnClickListener(v -> {
                    Intent intent = new Intent(this, MediaViewerActivity.class);
                    intent.putExtra("MEDIA_URL", url);
                    intent.putExtra("MEDIA_TYPE", "image");
                    startActivity(intent);
                });

                layoutMediaGrid.addView(imgView);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void confirmDeleteChat() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Conversation")
                .setMessage("Are you sure you want to delete this entire conversation? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> deleteConversation())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteConversation() {
        String url = Constants.BASE_URL + "delete_conversation.php";

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        if (json.getString("status").equals("success")) {
                            Toast.makeText(this, "Conversation deleted", Toast.LENGTH_SHORT).show();
                            // Go back to chat list
                            Intent intent = new Intent(this, ChatListActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(this, "Failed to delete", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(this, "Network error", Toast.LENGTH_SHORT).show()) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("conversation_id", String.valueOf(conversationId));
                params.put("user_id", String.valueOf(currentUserId));
                return params;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }
}
