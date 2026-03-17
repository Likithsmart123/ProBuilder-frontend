package com.example.probuilder;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

public class PaymentManagementActivity extends AppCompatActivity {

    private RecyclerView rvPayments;
    private PaymentAdapter adapter;
    private List<Payment> paymentList = new ArrayList<>();
    private com.google.android.material.floatingactionbutton.FloatingActionButton fabAddPayment;
    private int projectId = 0; // 0 = All projects
    private int contractorId = 1; 
    private String apiToken = "";

    // Member variables for Header text
    private TextView tvProjectNameHeader, tvClientNameHeader, tvTotalReceived;
    
    // Client data
    private final ArrayList<String> clientNames = new ArrayList<>();
    private final ArrayList<Integer> clientIds = new ArrayList<>();
    private final ArrayList<String> clientPhones = new ArrayList<>();
    // Track selected client for the searchable dialog
    private int selectedClientId = 0;
    private String selectedClientName = "Select Client";

    // Project data - STRICT MODEL
    // Removed inner Project class to use com.example.probuilder.Project

    private List<Project> projectList = new ArrayList<>();
    private android.widget.ArrayAdapter<Project> projectAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_management);

        android.content.SharedPreferences prefs = getSharedPreferences("AUTH", MODE_PRIVATE);
        contractorId = prefs.getInt("contractor_id", 0);
        apiToken = prefs.getString("token", "");

        if (apiToken.isEmpty()) {
             android.content.SharedPreferences legacyPrefs = getSharedPreferences("contractor_session", MODE_PRIVATE);
             apiToken = legacyPrefs.getString("api_token", "");
             contractorId = legacyPrefs.getInt("contractor_id", 0);
        }

        if (apiToken.isEmpty()) {
            android.widget.Toast.makeText(this, "Session Expired. Please Logout and Login.", android.widget.Toast.LENGTH_LONG).show();
            android.util.Log.e("PAYMENT_DEBUG", "No API Token found in AUTH or contractor_session");
        } else {
            android.util.Log.d("PAYMENT_DEBUG", "Token found: " + apiToken.substring(0, Math.min(10, apiToken.length())) + "...");
        }

        if (contractorId == 0) contractorId = 1; // Fallback

        if (getIntent().hasExtra("PROJECT_ID")) {
            projectId = getIntent().getIntExtra("PROJECT_ID", 0);
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        rvPayments = findViewById(R.id.rvPayments);
        tvTotalReceived = findViewById(R.id.tvTotalReceived);
        tvProjectNameHeader = findViewById(R.id.tvProjectNameHeader);
        tvClientNameHeader = findViewById(R.id.tvClientNameHeader);

        rvPayments.setLayoutManager(new LinearLayoutManager(this));
        
        adapter = new PaymentAdapter(paymentList);
        adapter.setOnItemClickListener(payment -> {
            new android.app.AlertDialog.Builder(this)
                .setTitle("Payment Details")
                .setMessage("Amount: ₹ " + payment.getAmount() + "\n" +
                            "Client: " + payment.getClientName() + "\n" +
                            "Project: " + payment.getProjectName() + "\n" + 
                            "Date: " + payment.getDate() + "\n" +
                            "Mode: " + payment.getPaymentMethod() + "\n" +
                            "Notes: " + (payment.getNotes() != null ? payment.getNotes() : "N/A"))
                .setPositiveButton("Close", null)
                .show();
        });
        rvPayments.setAdapter(adapter);

        fabAddPayment = findViewById(R.id.fabAddPayment);
        fabAddPayment.setOnClickListener(v -> showAddPaymentDialog());
        
        loadPayments();
    }

    private void loadClients(Runnable onLoaded) {
        android.util.Log.e("CLIENT_DEBUG", "loadClients() CALLED");

        String url = Constants.BASE_URL + "get_clients.php?contractor_id=" + contractorId;

        com.android.volley.toolbox.StringRequest request = new com.android.volley.toolbox.StringRequest(
            com.android.volley.Request.Method.GET,
            url,
            response -> {
                try {
                    android.util.Log.e("CLIENT_DEBUG", "RAW RESPONSE = " + response);

                    org.json.JSONObject root = new org.json.JSONObject(response);
                    org.json.JSONArray array = root.optJSONArray("clients");
                    if (array == null) array = new org.json.JSONArray();

                    clientNames.clear();
                    clientIds.clear();
                    clientPhones.clear();

                    for (int i = 0; i < array.length(); i++) {
                        org.json.JSONObject obj = array.getJSONObject(i);
                        clientIds.add(obj.getInt("client_id"));
                        clientNames.add(obj.getString("name"));
                        clientPhones.add(obj.optString("phone", ""));
                    }

                    android.util.Log.e("CLIENT_DEBUG", "Loaded " + clientNames.size() + " clients");

                    runOnUiThread(() -> {
                        if (onLoaded != null) onLoaded.run();
                    });

                } catch (Exception e) {
                    android.util.Log.e("CLIENT_DEBUG", "Parse error", e);
                }
            },
            error -> android.util.Log.e("CLIENT_DEBUG", "Volley error", error)
        ) {
            @Override
            public java.util.Map<String, String> getHeaders() {
                java.util.Map<String, String> headers = new java.util.HashMap<>();
                headers.put("Authorization", apiToken);
                return headers;
            }
        };

        com.android.volley.toolbox.Volley.newRequestQueue(this).add(request);
    }

    /**
     * Shows a searchable client picker dialog.
     * Filters clients in real-time as user types.
     */
    private void showClientSearchDialog(android.widget.Button btnSelectClient,
                                         List<ProjectItem> dialogProjectList,
                                         Runnable onClientSelected) {
        android.view.View dialogView = getLayoutInflater().inflate(R.layout.dialog_client_search, null);
        android.widget.EditText etSearch = dialogView.findViewById(R.id.etClientSearch);
        android.widget.ListView lvClients = dialogView.findViewById(R.id.lvClients);

        // Build display strings: "Name · Phone" or just "Name"
        ArrayList<String> displayList = new ArrayList<>();
        for (int i = 0; i < clientNames.size(); i++) {
            String phone = clientPhones.size() > i ? clientPhones.get(i) : "";
            displayList.add(phone.isEmpty() ? clientNames.get(i) : clientNames.get(i) + "  ·  " + phone);
        }

        // Mutable filtered copies
        final ArrayList<String> filteredDisplay = new ArrayList<>(displayList);
        final ArrayList<Integer> filteredIds = new ArrayList<>(clientIds);
        final ArrayList<String> filteredNames = new ArrayList<>(clientNames);

        android.widget.ArrayAdapter<String> listAdapter = new android.widget.ArrayAdapter<String>(
                this, android.R.layout.simple_list_item_1, filteredDisplay) {
            @Override
            public android.view.View getView(int position, android.view.View convertView, android.view.ViewGroup parent) {
                android.view.View v = super.getView(position, convertView, parent);
                android.widget.TextView tv = (android.widget.TextView) v.findViewById(android.R.id.text1);
                tv.setPadding(32, 24, 32, 24);
                tv.setTextSize(14);
                tv.setTextColor(android.graphics.Color.parseColor("#212121"));
                return v;
            }
        };
        lvClients.setAdapter(listAdapter);

        android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(this)
                .setTitle("Select Client")
                .setView(dialogView)
                .setNegativeButton("Cancel", null)
                .create();

        // Real-time search filtering
        etSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().toLowerCase().trim();
                filteredDisplay.clear();
                filteredIds.clear();
                filteredNames.clear();
                for (int i = 0; i < clientNames.size(); i++) {
                    String phone = clientPhones.size() > i ? clientPhones.get(i) : "";
                    if (clientNames.get(i).toLowerCase().contains(query)
                            || phone.contains(query)) {
                        filteredNames.add(clientNames.get(i));
                        filteredIds.add(clientIds.get(i));
                        filteredDisplay.add(phone.isEmpty()
                                ? clientNames.get(i)
                                : clientNames.get(i) + "  ·  " + phone);
                    }
                }
                listAdapter.notifyDataSetChanged();
            }
            @Override public void afterTextChanged(android.text.Editable s) {}
        });

        // On client item tap
        lvClients.setOnItemClickListener((parent, view, position, id) -> {
            selectedClientId = filteredIds.get(position);
            selectedClientName = filteredNames.get(position);
            btnSelectClient.setText(selectedClientName);
            btnSelectClient.setTextColor(android.graphics.Color.parseColor("#212121"));
            dialog.dismiss();
            if (onClientSelected != null) onClientSelected.run();
        });

        dialog.show();
        // Auto-focus keyboard on search box
        etSearch.requestFocus();
        android.view.inputmethod.InputMethodManager imm =
                (android.view.inputmethod.InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) imm.showSoftInput(etSearch, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
    }

    private void showAddPaymentDialog() {
        // Reset selections each time dialog opens
        selectedClientId = 0;
        selectedClientName = "Select Client";

        // Track selected project within this dialog
        final int[] selectedProjectId = {0};
        final String[] selectedProjectTitle = {"Select Project"};

        com.google.android.material.bottomsheet.BottomSheetDialog dialog = new com.google.android.material.bottomsheet.BottomSheetDialog(this);
        android.view.View view = getLayoutInflater().inflate(R.layout.bottom_sheet_add_payment, null);
        dialog.setContentView(view);

        android.widget.Button btnSelectClient = view.findViewById(R.id.btnSelectClient);
        android.widget.Button btnSelectProject = view.findViewById(R.id.btnSelectProject);
        android.widget.EditText etAmount = view.findViewById(R.id.etAmount);
        android.widget.Spinner spinnerMode = view.findViewById(R.id.spinnerMode);
        android.widget.EditText etNotes = view.findViewById(R.id.etNotes);
        android.widget.Button btnSave = view.findViewById(R.id.btnSavePayment);

        // Project list populated after a client is chosen
        final List<ProjectItem> dialogProjectList = new ArrayList<>();
        btnSelectProject.setEnabled(false); // disabled until a client is picked

        String[] modes = {"Cash", "UPI", "Bank Transfer"};
        android.widget.ArrayAdapter<String> modeAdapter = new android.widget.ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, modes);
        modeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMode.setAdapter(modeAdapter);

        // When a client is picked from the search dialog → load projects → enable project button
        Runnable onClientSelected = () -> {
            btnSelectProject.setText("Loading projects…");
            btnSelectProject.setEnabled(false);
            selectedProjectId[0] = 0;
            selectedProjectTitle[0] = "Select Project";
            dialogProjectList.clear();

            loadProjectsByClientForButton(selectedClientId, dialogProjectList, () -> {
                runOnUiThread(() -> {
                    btnSelectProject.setEnabled(true);
                    btnSelectProject.setText("Select Project");
                    btnSelectProject.setTextColor(android.graphics.Color.parseColor("#555555"));
                });
            });
        };

        // Wire up client button
        Runnable wireClientBtn = () -> btnSelectClient.setOnClickListener(v ->
                showClientSearchDialog(btnSelectClient, dialogProjectList, onClientSelected));

        loadClients(wireClientBtn::run);
        if (!clientNames.isEmpty()) wireClientBtn.run();

        // Wire up project button
        btnSelectProject.setOnClickListener(v -> {
            if (selectedClientId == 0) {
                android.widget.Toast.makeText(this, "Please select a client first", android.widget.Toast.LENGTH_SHORT).show();
                return;
            }
            showProjectPickerDialog(btnSelectProject, dialogProjectList, selectedProjectId, selectedProjectTitle);
        });

        btnSave.setOnClickListener(b -> {
            if (selectedClientId == 0) {
                android.widget.Toast.makeText(this, "Please select a client", android.widget.Toast.LENGTH_SHORT).show();
                return;
            }
            if (selectedProjectId[0] == 0) {
                android.widget.Toast.makeText(this, "Please select a project", android.widget.Toast.LENGTH_SHORT).show();
                return;
            }
            if (DialogUtils.isEmpty(etAmount)) {
                etAmount.setError("Required");
                return;
            }
            String amount = etAmount.getText().toString();
            String mode = spinnerMode.getSelectedItem().toString();
            String notes = etNotes.getText().toString();
            addPayment(selectedProjectId[0], selectedClientId, amount, mode, notes, dialog);
        });

        dialog.show();
    }

    /** Shows a simple project picker dialog (list only, no search needed — projects are few). */
    private void showProjectPickerDialog(android.widget.Button btnSelectProject,
                                          List<ProjectItem> projectList,
                                          int[] selectedProjectId,
                                          String[] selectedProjectTitle) {
        if (projectList.isEmpty()) {
            android.widget.Toast.makeText(this, "No projects found for this client", android.widget.Toast.LENGTH_SHORT).show();
            return;
        }

        String[] titles = new String[projectList.size()];
        for (int i = 0; i < projectList.size(); i++) titles[i] = projectList.get(i).title;

        new android.app.AlertDialog.Builder(this)
                .setTitle("Select Project")
                .setItems(titles, (d, which) -> {
                    ProjectItem picked = projectList.get(which);
                    selectedProjectId[0] = picked.id;
                    selectedProjectTitle[0] = picked.title;
                    btnSelectProject.setText(picked.title);
                    btnSelectProject.setTextColor(android.graphics.Color.parseColor("#212121"));
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /** Variant of loadProjectsByClient that fires a Runnable callback on completion instead of updating an adapter. */
    private void loadProjectsByClientForButton(int clientId, List<ProjectItem> targetList, Runnable onDone) {
        String url = Constants.BASE_URL + "get_projects_by_client.php?client_id=" + clientId;
        com.android.volley.toolbox.StringRequest request = new com.android.volley.toolbox.StringRequest(
            com.android.volley.Request.Method.GET, url,
            response -> {
                try {
                    org.json.JSONObject root = new org.json.JSONObject(response);
                    org.json.JSONArray arr = root.optJSONArray("projects");
                    if (arr == null) arr = new org.json.JSONArray();
                    targetList.clear();
                    for (int i = 0; i < arr.length(); i++) {
                        org.json.JSONObject o = arr.getJSONObject(i);
                        ProjectItem p = new ProjectItem();
                        p.id = o.optInt("project_id", o.optInt("id"));
                        p.title = o.getString("title");
                        targetList.add(p);
                    }
                    runOnUiThread(() -> { if (onDone != null) onDone.run(); });
                } catch (Exception e) {
                    android.util.Log.e("PROJECT_DEBUG", "Parse error", e);
                }
            },
            error -> android.util.Log.e("PROJECT_DEBUG", "Volley error", error)
        ) {
            @Override
            public java.util.Map<String, String> getHeaders() {
                java.util.Map<String, String> headers = new java.util.HashMap<>();
                headers.put("Authorization", apiToken);
                return headers;
            }
        };
        com.android.volley.toolbox.Volley.newRequestQueue(this).add(request);
    }

    private void loadProjectsByClient(int clientId, List<ProjectItem> targetList, android.widget.ArrayAdapter<ProjectItem> targetAdapter) {
        android.util.Log.e("PROJECT_DEBUG", "Selected clientId = " + clientId);

        String url = Constants.BASE_URL + "get_projects_by_client.php?client_id=" + clientId;
        android.util.Log.e("PROJECT_DEBUG", "Calling URL = " + url);

        com.android.volley.toolbox.StringRequest request = new com.android.volley.toolbox.StringRequest(
            com.android.volley.Request.Method.GET,
            url,
            response -> {
                android.util.Log.e("PROJECT_DEBUG", "Raw response = " + response);

                try {
                    org.json.JSONObject root = new org.json.JSONObject(response);
                    org.json.JSONArray arr = root.optJSONArray("projects");
                    if (arr == null) arr = new org.json.JSONArray();
                    
                    targetList.clear();
                    // Add Default
                    ProjectItem dummy = new ProjectItem();
                    dummy.id = 0;
                    dummy.title = "Select Project";
                    targetList.add(dummy);

                    for (int i = 0; i < arr.length(); i++) {
                        org.json.JSONObject o = arr.getJSONObject(i);

                        // Use ProjectItem model
                        ProjectItem p = new ProjectItem();
                        p.id = o.optInt("project_id", o.optInt("id"));
                        p.title = o.getString("title");
                        
                        targetList.add(p);
                    }
                    
                    targetAdapter.notifyDataSetChanged();
                    android.util.Log.e("PROJECT_DEBUG", "Projects in spinner = " + targetList.size());

                } catch (Exception e) {
                    android.util.Log.e("PROJECT_DEBUG", "Parse error", e);
                }
            },
            error -> android.util.Log.e("PROJECT_DEBUG", "Volley error", error)
        ) {
            @Override
            public java.util.Map<String, String> getHeaders() {
                java.util.Map<String, String> headers = new java.util.HashMap<>();
                headers.put("Authorization", apiToken);
                return headers;
            }
        };

        com.android.volley.toolbox.Volley.newRequestQueue(this).add(request);
    }

    private void loadPayments() {
        // Fetch All payments if projectId=0
        String url = Constants.BASE_URL + "get_project_payments.php" + (projectId > 0 ? "?project_id=" + projectId : "");
        com.android.volley.toolbox.StringRequest request = new com.android.volley.toolbox.StringRequest(
                com.android.volley.Request.Method.GET,
                url,
                response -> {
                    android.util.Log.e("PAYMENT_DEBUG", "Raw Payment Response: " + response);
                    try {
                        org.json.JSONObject root = new org.json.JSONObject(response);
                        double total = root.optDouble("total_received", 0);
                        if (tvTotalReceived != null) {
                            tvTotalReceived.setText(String.format(java.util.Locale.getDefault(), "₹ %,.0f", total));
                        }
                        String pName = root.optString("project_name", "All Projects");
                        String cName = root.optString("client_name", "All Clients");
                        if (tvProjectNameHeader != null) tvProjectNameHeader.setText(pName);
                        if (tvClientNameHeader != null) tvClientNameHeader.setText(cName);

                        org.json.JSONArray arr = root.optJSONArray("payments");
                        if (arr == null) return; 

                        paymentList.clear();
                        for (int i = 0; i < arr.length(); i++) {
                            org.json.JSONObject obj = arr.getJSONObject(i);
                            paymentList.add(new Payment(
                                obj.optInt("id", 0),
                                obj.optString("client_name", "N/A"), 
                                obj.optString("project_name", "N/A"),
                                obj.optDouble("amount"),
                                obj.optString("payment_date"),
                                "Received",
                                obj.optString("payment_mode", obj.optString("payment_method")),
                                obj.optString("notes")
                            ));
                        }
                        adapter.notifyDataSetChanged();
                    } catch (Exception e) { e.printStackTrace(); }
                },
                error -> android.widget.Toast.makeText(this, "Error loading", android.widget.Toast.LENGTH_SHORT).show()
        ) {
            @Override
            public java.util.Map<String, String> getHeaders() {
                java.util.Map<String, String> headers = new java.util.HashMap<>();
                headers.put("Authorization", apiToken);
                return headers;
            }
        };
        com.android.volley.toolbox.Volley.newRequestQueue(this).add(request);
    }

    private void addPayment(int projectId, int clientId, String amount, String mode, String notes, com.google.android.material.bottomsheet.BottomSheetDialog dialog) {
        String url = Constants.BASE_URL + "add_project_payment.php";
        com.android.volley.toolbox.StringRequest req = new com.android.volley.toolbox.StringRequest(
            com.android.volley.Request.Method.POST,
            url,
            res -> {
                dialog.dismiss();
                loadPayments(); 
                android.widget.Toast.makeText(this, "Payment Saved", android.widget.Toast.LENGTH_SHORT).show();
            },
            err -> android.widget.Toast.makeText(this, "Error saving payment", android.widget.Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected java.util.Map<String, String> getParams() {
                java.util.Map<String, String> map = new java.util.HashMap<>();
                map.put("project_id", String.valueOf(projectId));
                map.put("client_id", String.valueOf(clientId)); 
                map.put("amount", amount);
                map.put("payment_mode", mode);
                map.put("notes", notes);
                map.put("notes", notes);
                return map;
            }

            @Override
            public java.util.Map<String, String> getHeaders() {
                java.util.Map<String, String> headers = new java.util.HashMap<>();
                headers.put("Authorization", apiToken);
                return headers;
            }
        };
        com.android.volley.toolbox.Volley.newRequestQueue(this).add(req);
    }

    static class DialogUtils {
        static boolean isEmpty(android.widget.EditText et) {
            return android.text.TextUtils.isEmpty(et.getText().toString().trim());
        }
    }
}
