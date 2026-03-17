package com.example.probuilder;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ChatListActivity extends AppCompatActivity {

    private RecyclerView rvChatList;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView tvEmptyState;
    private Toolbar toolbar;

    private ChatListAdapter adapter;
    private List<Conversation> conversationList;
    private RequestQueue requestQueue;

    private int currentUserId;
    private String currentUserType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);

        rvChatList = findViewById(R.id.rvChatList);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

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
        }

        if (currentUserId == -1) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        conversationList = new ArrayList<>();
        adapter = new ChatListAdapter(this, conversationList);
        rvChatList.setLayoutManager(new LinearLayoutManager(this));
        rvChatList.setAdapter(adapter);

        requestQueue = Volley.newRequestQueue(this);

        EditText etSearch = findViewById(R.id.etSearch);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        swipeRefreshLayout.setOnRefreshListener(this::fetchConversations);
        
        View fabNewChat = findViewById(R.id.fabNewChat);
        if (currentUserType.equals("contractor")) {
            fabNewChat.setVisibility(View.VISIBLE);
            fabNewChat.setOnClickListener(v -> showClientSelectionDialog());
        } else {
            fabNewChat.setVisibility(View.GONE);
        }
    }

    private void showClientSelectionDialog() {
        com.google.android.material.bottomsheet.BottomSheetDialog dialog = new com.google.android.material.bottomsheet.BottomSheetDialog(this);
        View sheetView = getLayoutInflater().inflate(R.layout.dialog_select_client, null);
        dialog.setContentView(sheetView);

        RecyclerView rvClientList = sheetView.findViewById(R.id.rvClientList);
        rvClientList.setLayoutManager(new LinearLayoutManager(this));
        
        EditText etSearchClient = sheetView.findViewById(R.id.etSearchClient);

        List<Client> clientList = new ArrayList<>();
        ClientSelectAdapter clientAdapter = new ClientSelectAdapter(this, clientList, client -> {
            dialog.dismiss();
            
            // Check if project exists, default to generalized project ID 0 or handle logic
            // For now, allow chat to happen via generic project ID 0 since it isn't project bound
            Intent chatIntent = new Intent(this, ChatActivity.class);
            chatIntent.putExtra("PROJECT_ID", 0);
            chatIntent.putExtra("OTHER_USER_ID", client.clientId);
            chatIntent.putExtra("OTHER_USER_NAME", client.name);
            startActivity(chatIntent);
        });
        rvClientList.setAdapter(clientAdapter);
        
        etSearchClient.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                clientAdapter.filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Fetch Clients
        String url = Constants.BASE_URL + "get_clients.php?contractor_id=" + currentUserId;
        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.getString("status").equals("success")) {
                            JSONArray data = jsonObject.getJSONArray("clients");
                            for (int i = 0; i < data.length(); i++) {
                                JSONObject obj = data.getJSONObject(i);
                                Client client = new Client();
                                client.clientId = obj.getInt("client_id");
                                client.name = obj.getString("name");
                                client.email = obj.optString("email", "");
                                client.phone = obj.optString("phone", "");
                                clientList.add(client);
                            }
                            clientAdapter.updateData(clientList);
                            clientAdapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(this, "No clients found", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(this, "Network error", Toast.LENGTH_SHORT).show());
        
        requestQueue.add(request);
        dialog.show();
    }

    private void filter(String text) {
        List<Conversation> filteredList = new ArrayList<>();
        for (Conversation item : conversationList) {
            String otherName = item.getOtherUserName() != null ? item.getOtherUserName().toLowerCase() : "";
            String messageStr = item.getLastMessage() != null ? item.getLastMessage().toLowerCase() : "";
            
            if (otherName.contains(text.toLowerCase()) || messageStr.contains(text.toLowerCase())) {
                filteredList.add(item);
            }
        }
        adapter.filterList(filteredList);
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchConversations();
    }

    private void fetchConversations() {
        swipeRefreshLayout.setRefreshing(true);
        String url = Constants.BASE_URL + "get_conversations.php?user_id=" + currentUserId + "&user_type=" + currentUserType;

        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    swipeRefreshLayout.setRefreshing(false);
                    Log.d("RAW_CHAT_LIST_JSON", response);
                    conversationList.clear();
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.getString("status").equals("success")) {
                            JSONArray data = jsonObject.getJSONArray("data");
                            for (int i = 0; i < data.length(); i++) {
                                JSONObject convObj = data.getJSONObject(i);
                                
                                String otherBusiness = convObj.has("business_name") && !convObj.isNull("business_name") 
                                        ? convObj.getString("business_name") : "";
                                        
                                Conversation conv = new Conversation(
                                        convObj.getInt("conversation_id"),
                                        convObj.getInt("other_user_id"),
                                        convObj.optString("other_user_name", "User"),
                                        otherBusiness,
                                        convObj.optString("last_message"),
                                        convObj.optString("last_message_time"),
                                        convObj.optInt("unread_count", 0),
                                        convObj.optInt("is_read", 1),
                                        convObj.optInt("last_sender_id", 0)
                                );
                                conversationList.add(conv);
                            }
                            
                            adapter.notifyDataSetChanged();
                            
                            if (conversationList.isEmpty()) {
                                tvEmptyState.setVisibility(View.VISIBLE);
                                rvChatList.setVisibility(View.GONE);
                            } else {
                                tvEmptyState.setVisibility(View.GONE);
                                rvChatList.setVisibility(View.VISIBLE);
                            }
                        } else {
                            Toast.makeText(this, "Failed to load chats", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Log.e("ChatListJSONError", "JSON Parse error: " + response, e);
                        Toast.makeText(this, "Data Error", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    swipeRefreshLayout.setRefreshing(false);
                    Log.e("ChatListAPI", "Network Error", error);
                    Toast.makeText(this, "Network Error", Toast.LENGTH_SHORT).show();
                });

        requestQueue.add(request);
    }
}
