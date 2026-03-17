package com.example.probuilder;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreateQuotationActivity extends AppCompatActivity {

    android.widget.TextView tvSelectedClient;
    android.widget.FrameLayout flClientSelector;
    Spinner spinnerProject;
    EditText etQuotationTitle, etTotalAmount, etDescription;
    Button btnCreateQuotation;
    ImageView btnBack;

    String contractorId; // Changed to String as per user snippet usage
    String selectedClientId = "";
    String selectedProjectId = "";
    private ClientItem selectedClient = null;

    // Constants
    private static final String GET_CLIENTS_URL = Constants.BASE_URL + "get_clients.php";
    private static final String GET_PROJECTS_URL = Constants.BASE_URL + "get_projects.php";
    private static final String ADD_QUOTATION_URL = Constants.BASE_URL + "add_quotation.php";

    // Added members for lazy loading
    private ArrayAdapter<ProjectItem> projectAdapter;
    private List<ProjectItem> projectList = new ArrayList<>();
    private boolean isProjectsLoaded = false;
    
    private List<ClientItem> clientList;
    private ArrayAdapter<ClientItem> dialogAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_quotation);

        android.content.SharedPreferences prefs = getSharedPreferences("ProBuilderPrefs", MODE_PRIVATE);
        int cid = prefs.getInt("contractor_id", -1);
        contractorId = String.valueOf(cid);

        if (cid == -1 || cid == 0) {
            Toast.makeText(this, "Session expired. Please Login again to load data.", Toast.LENGTH_LONG).show();
            // Redirect to Login to refresh session
            Intent intent = new Intent(this, ContractorLoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        tvSelectedClient = findViewById(R.id.tvSelectedClient);
        flClientSelector = findViewById(R.id.flClientSelector);
        spinnerProject = findViewById(R.id.spinnerProject);
        etQuotationTitle = findViewById(R.id.etQuotationTitle);
        etTotalAmount = findViewById(R.id.etTotalAmount);
        etDescription = findViewById(R.id.etDescription);
        btnCreateQuotation = findViewById(R.id.btnCreateQuotation);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        flClientSelector.setOnClickListener(v -> {
            if (clientList != null && !clientList.isEmpty()) {
                showClientSearchDialog();
            } else {
                Toast.makeText(this, "No clients available", Toast.LENGTH_SHORT).show();
            }
        });

        loadClients();
        
        // Setup Project Spinner for Lazy Load
        ProjectItem defaultItem = new ProjectItem();
        defaultItem.id = -1;
        defaultItem.title = "Select Project";
        projectList.add(defaultItem);

        projectAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                projectList
        );
        spinnerProject.setAdapter(projectAdapter);

        // spinnerProject.setOnTouchListener((v, event) -> {
        //    if (event.getAction() == android.view.MotionEvent.ACTION_UP && !isProjectsLoaded) {
        //         loadProjects();
        //    }
        //    return false;
        // });

        spinnerProject.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                 ProjectItem item = (ProjectItem) parent.getSelectedItem();
                 if (item != null && item.id != -1) {
                     selectedProjectId = String.valueOf(item.id);
                 } else {
                     selectedProjectId = "";
                 }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedProjectId = "";
            }
        });


        btnCreateQuotation.setOnClickListener(v -> submitQuotation());
    }

    private void loadClients() {
        clientList = new ArrayList<>();
        selectedClient = null;
        tvSelectedClient.setText(R.string.label_select_client);

        String url = GET_CLIENTS_URL + "?contractor_id=" + contractorId;
        StringRequest request = new StringRequest(
                Request.Method.GET,
                url,
                response -> {
                    try {
                        Log.d("CLIENT_RESPONSE", response);

                        org.json.JSONObject root = new org.json.JSONObject(response.trim());
                        JSONArray arr = root.optJSONArray("clients");
                        if (arr == null) arr = new JSONArray();

                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject obj = arr.getJSONObject(i);
                            String id = obj.getString("client_id"); // Updated key
                            String name = obj.getString("name"); // Updated key
                            String phone = obj.optString("phone");
                            clientList.add(new ClientItem(id, name, phone));
                        }
                    } catch (Exception e) {
                        Log.e("CLIENT_PARSE_ERROR", response);
                    }
                },
                error -> Log.e("CLIENT_NET_ERROR", error.toString())
        );

        Volley.newRequestQueue(this).add(request);
    }

    private void loadProjects() {
        if (selectedClientId.isEmpty()) {
            projectList.clear();
            ProjectItem defaultItem = new ProjectItem();
            defaultItem.id = -1;
            defaultItem.title = "Select Client First";
            projectList.add(defaultItem);
            if (projectAdapter != null) projectAdapter.notifyDataSetChanged();
            return;
        }

        Toast.makeText(this, "Loading projects...", Toast.LENGTH_SHORT).show();
        String url = Constants.BASE_URL + "get_projects_v2.php?client_id=" + selectedClientId;
        
        // STEP 2 — ALWAYS READ TOKEN LIKE THIS (EVERY API)
        android.content.SharedPreferences prefs = getSharedPreferences("AUTH", MODE_PRIVATE);
        String token = prefs.getString("token", "");
        
        // STEP 4 — ADD ONE LOG (DEBUG ONLY)
        Log.d("AUTH_DEBUG", "Using token = " + token);

        if (token.isEmpty()) {
             Toast.makeText(this, "Not logged in (Token empty)", Toast.LENGTH_SHORT).show();
             return;
        }

        AuthJsonRequest request = new AuthJsonRequest(
                this,
                Request.Method.GET,
                url,
                null,
                response -> {
                    Log.d("PROJECT_RESPONSE", response.toString());

                    JSONArray projects = response.optJSONArray("projects");
                    if (projects == null) {
                        Log.e("PROJECT_RESPONSE", "No projects array");
                        return;
                    }

                    try {
                        projectList.clear();
                         
                        // Default
                        ProjectItem defaultItem = new ProjectItem();
                        defaultItem.id = -1;
                        defaultItem.title = "Select Project";
                        projectList.add(defaultItem);

                        for (int i = 0; i < projects.length(); i++) {
                            JSONObject obj = projects.getJSONObject(i);

                            ProjectItem p = new ProjectItem();
                            p.id = obj.getInt("project_id");
                            p.title = obj.getString("title");

                            projectList.add(p);
                        }

                        projectAdapter.notifyDataSetChanged();
                        isProjectsLoaded = true;

                    } catch (Exception e) {
                        Log.e("PROJECT_PARSE_ERROR", "Error parsing projects", e);
                    }
                },
                error -> {
                    Log.e("PROJECT_NET_ERROR", "Volley error", error);
                    Toast.makeText(this, "Failed to load projects", Toast.LENGTH_SHORT).show();
                }
        );

        request.setShouldCache(false);
        MyVolley.get(this).add(request);
    }

    private void submitQuotation() {
        // VALIDATION
        if (selectedProjectId.isEmpty()) {
            Toast.makeText(this, "Select project", Toast.LENGTH_SHORT).show();
            return;
        }

        /* 
           NOTE: We ignore selectedClientId for submission because create_quotation.php
           automatically derives the client_id from the project_id. 
           (One project belongs to exactly one client).
        */
        
        String title = etQuotationTitle.getText().toString().trim();
        if (title.isEmpty()) {
             etQuotationTitle.setError("Required");
             return;
        }

        String amountStr = etTotalAmount.getText().toString().trim();
        if (amountStr.isEmpty()) {
             etTotalAmount.setError("Required");
             return;
        }

        // URL
        String url = Constants.BASE_URL + "create_quotation.php";
        
        // JSON BODY
        JSONObject params = new JSONObject();
        try {
            // STEP 3 - BACKEND INSERTS EXACT PROJECT ID
            params.put("project_id", Integer.parseInt(selectedProjectId));
            params.put("title", title);
            params.put("description", etDescription.getText().toString().trim());
            params.put("amount", Double.parseDouble(amountStr));
            
            // Contractor ID -> From Token
            // Client ID -> From Project (Backend Lookup)

        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        Log.d("QUOTATION_PAYLOAD", params.toString());

        AuthJsonRequest request = new AuthJsonRequest(
            this,
            Request.Method.POST,
            url,
            params,
            response -> {
                Log.d("ADD_QUOTATION", response.toString());
                String status = response.optString("status");
                
                if ("success".equals(status)) {
                    Toast.makeText(this, "Quotation created successfully", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    String msg = response.optString("error", "Failed to create");
                    Toast.makeText(this, "Error: " + msg, Toast.LENGTH_LONG).show();
                }
            },
            error -> {
                Log.e("ADD_QUOTATION_ERROR", "Volley Error: " + error.toString());
                Toast.makeText(this, "Network error", Toast.LENGTH_LONG).show();
            }
        );

        MyVolley.get(this).add(request);
    }

    private void showClientSearchDialog() {
        android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.setContentView(R.layout.dialog_searchable_spinner);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        android.widget.EditText etSearch = dialog.findViewById(R.id.etSearchClient);
        android.widget.ListView listView = dialog.findViewById(R.id.lvClients);

        dialogAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, clientList);
        listView.setAdapter(dialogAdapter);

        etSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                dialogAdapter.getFilter().filter(s);
            }
            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        listView.setOnItemClickListener((parent, view, position, id) -> {
            selectedClient = dialogAdapter.getItem(position);
            selectedClientId = selectedClient.getId();
            tvSelectedClient.setText(selectedClient.getDisplayString());
            
            // RELOAD PROJECTS FOR THIS CLIENT
            if (!selectedClientId.isEmpty()) {
                selectedProjectId = ""; // Reset project selection
                isProjectsLoaded = false; // Force reload
                loadProjects(); 
            } else {
                // Clear projects if no client selected
                projectList.clear();
                ProjectItem defaultItem = new ProjectItem();
                defaultItem.id = -1;
                defaultItem.title = "Select Client First";
                projectList.add(defaultItem);
                if (projectAdapter != null) projectAdapter.notifyDataSetChanged();
            }

            dialog.dismiss();
        });

        dialog.show();
    }

    // Helper class for Spinner
    private static class ClientItem {
        private String id;
        private String name;
        private String phone;
        private String displayString;

        public ClientItem(String id, String name, String phone) {
            this.id = id;
            this.name = name;
            this.phone = phone != null ? phone : "";
            this.displayString = name + (this.phone.isEmpty() ? "" : " - " + this.phone);
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getDisplayString() {
            return displayString;
        }

        @Override
        public String toString() {
            return displayString;
        }
    }
}
