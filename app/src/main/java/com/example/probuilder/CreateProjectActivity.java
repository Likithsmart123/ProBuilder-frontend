package com.example.probuilder;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;



import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreateProjectActivity extends AppCompatActivity {

    private TextInputEditText etProjectName, etProjectLocation, etStartDate, etEndDate, etBudget;
    private android.widget.TextView tvSelectedClient;
    private android.widget.FrameLayout flClientSelector;
    private Button btnAddProject;
    
    // Updated endpoints
    private static final String CREATE_PROJECT_URL = Constants.BASE_URL + "create_project.php";
    private static final String GET_CLIENTS_URL = Constants.BASE_URL + "get_clients.php";

    private List<ClientItem> clientList;
    private ArrayAdapter<ClientItem> adapter;
    private ClientItem selectedClient = null;

    private final androidx.activity.result.ActivityResultLauncher<android.content.Intent> addClientLauncher = 
        registerForActivityResult(new androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult(), result -> {
            android.content.SharedPreferences sp = getSharedPreferences("ProBuilderPrefs", MODE_PRIVATE);
            int contractorId = sp.getInt("contractor_id", -1);
            if (contractorId != -1) {
                fetchClients(contractorId);
            }
        });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_project);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        etProjectName = findViewById(R.id.etProjectName);
        etProjectLocation = findViewById(R.id.etProjectLocation);
        etStartDate = findViewById(R.id.etStartDate);
        etEndDate = findViewById(R.id.etEndDate);
        etBudget = findViewById(R.id.etBudget); // New field
        tvSelectedClient = findViewById(R.id.tvSelectedClient);
        flClientSelector = findViewById(R.id.flClientSelector);
        btnAddProject = findViewById(R.id.btnAddProject);
        Button btnCancel = findViewById(R.id.btnCancel);
        com.google.android.material.button.MaterialButton btnAddNewClient = findViewById(R.id.btnAddNewClient);

        flClientSelector.setOnClickListener(v -> {
            if (clientList != null && !clientList.isEmpty()) {
                showClientSearchDialog();
            } else {
                Toast.makeText(this, "No clients available", Toast.LENGTH_SHORT).show();
            }
        });

        btnAddNewClient.setOnClickListener(v -> {
            addClientLauncher.launch(new android.content.Intent(CreateProjectActivity.this, AddClientActivity.class));
        });

        etStartDate.setOnClickListener(v -> showDatePickerDialog(etStartDate));
        etEndDate.setOnClickListener(v -> showDatePickerDialog(etEndDate));
        
        btnCancel.setOnClickListener(v -> finish());

        // Initialize SharedPreferences
        android.content.SharedPreferences sp = getSharedPreferences("ProBuilderPrefs", MODE_PRIVATE);
        // Get contractor ID (default to -1 or handle error)
        int contractorId = sp.getInt("contractor_id", -1);
        
        if (contractorId == -1) {
             Toast.makeText(this, "Session invalid. Please login.", Toast.LENGTH_SHORT).show();
             finish();
             return;
        }

        btnAddProject.setOnClickListener(v -> addProject(contractorId));

        fetchClients(contractorId);
    }

    private void fetchClients(int contractorId) {
        clientList = new ArrayList<>();
        selectedClient = null;
        tvSelectedClient.setText("Select Client");

        String url = GET_CLIENTS_URL + "?contractor_id=" + contractorId;
        
        Log.d("FetchClients", "Calling GET clients API: " + url);

        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        org.json.JSONObject root = new org.json.JSONObject(response);
                        org.json.JSONArray array = root.optJSONArray("clients");
                        
                        if (array == null) {
                             array = new org.json.JSONArray();
                        }
                        
                        for (int i = 0; i < array.length(); i++) {
                            org.json.JSONObject obj = array.getJSONObject(i);
                            // id is int in JSON, convert to String for ClientItem
                            String id = String.valueOf(obj.optInt("client_id")); // Updated key
                            String name = obj.optString("name"); // Updated key
                            String phone = obj.optString("phone");
                            
                            clientList.add(new ClientItem(id, name, phone));
                        }
                    } catch (Exception e) {
                        Log.e("FetchClients", "Parse Error: " + e.getMessage());
                        Toast.makeText(this, "Error parsing clients", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("FetchClients", "Volley Error: " + error.toString());
                    Toast.makeText(this, "Failed to load clients", Toast.LENGTH_SHORT).show();
                }
        );

        Volley.newRequestQueue(this).add(request);
    }

    private void showDatePickerDialog(TextInputEditText dateField) {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year1, monthOfYear, dayOfMonth) -> {
                    String selectedDate = year1 + "-" + (monthOfYear + 1) + "-" + dayOfMonth;
                    dateField.setText(selectedDate);
                }, year, month, day);
        datePickerDialog.show();
    }

    private void addProject(int contractorId) {
        hideKeyboard(); // Dismiss keyboard immediately

        final String name = etProjectName.getText() != null ? etProjectName.getText().toString().trim() : "";
        final String location = etProjectLocation.getText() != null ? etProjectLocation.getText().toString().trim() : "";
        final String startDate = etStartDate.getText() != null ? etStartDate.getText().toString().trim() : "";
        final String endDate = etEndDate.getText() != null ? etEndDate.getText().toString().trim() : "";
        final String budgetStr = etBudget.getText() != null ? etBudget.getText().toString().trim() : ""; // Get Budget

        if (selectedClient == null) {
            Toast.makeText(this, "Please select a client", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String clientIdStr = selectedClient.getId();
        int clientId = Integer.parseInt(clientIdStr);

        if (name.isEmpty() || location.isEmpty() || startDate.isEmpty() || endDate.isEmpty() || budgetStr.isEmpty()) {
            Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        
        double budget = 0;
        try {
            budget = Double.parseDouble(budgetStr);
        } catch (NumberFormatException e) {
            etBudget.setError("Invalid budget amount");
            return;
        }

        btnAddProject.setEnabled(false);
        
        StringRequest request = new StringRequest(Request.Method.POST, CREATE_PROJECT_URL,
                response -> {
                    Log.d("PROJECT_CREATE", response);
                    try {
                        org.json.JSONObject jsonResponse = new org.json.JSONObject(response);
                        String status = jsonResponse.optString("status");

                        if ("success".equals(status)) {
                            Toast.makeText(this, "Project added successfully", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            String msg = jsonResponse.optString("message", "Unknown error");
                            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                            btnAddProject.setEnabled(true);
                        }
                    } catch (org.json.JSONException e) {
                        Log.e("PROJECT_CREATE", "JSON Parse Error: " + e.getMessage());
                        Toast.makeText(this, "Server response error", Toast.LENGTH_SHORT).show();
                        btnAddProject.setEnabled(true);
                    }
                },
                error -> {
                    Log.e("PROJECT_CREATE", "Error: " + error.toString());
                    Toast.makeText(this, "Network / Server error", Toast.LENGTH_SHORT).show();
                    btnAddProject.setEnabled(true);
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("contractor_id", String.valueOf(contractorId));
                params.put("client_id", String.valueOf(clientId));
                params.put("title", name);
                params.put("location", location);
                params.put("start_date", startDate);
                params.put("end_date", endDate);
                params.put("budget", budgetStr);
                return params;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }

    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return true;
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

        ArrayAdapter<ClientItem> dialogAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, clientList);
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
            tvSelectedClient.setText(selectedClient.getDisplayString());
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

    private void hideKeyboard() {
        android.view.View view = this.getCurrentFocus();
        if (view != null) {
            android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}