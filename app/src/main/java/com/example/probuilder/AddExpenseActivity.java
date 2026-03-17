package com.example.probuilder;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;



import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.Locale;

public class AddExpenseActivity extends AppCompatActivity {

    private LinearLayout btnCatMaterials, btnCatLabor, btnCatEquipment, btnCatTransport, btnCatUtilities, btnCatOther;
    private TextInputEditText etDate, etAmount, etDescription, etInvoice, etTitle;
    private AutoCompleteTextView actvProject;
    private MaterialButton btnSave, btnCancel;

    private String selectedCategory = "Materials"; // Default
    private Integer selectedProjectId = null;
    private Map<String, Integer> projectNameToIdMap = new HashMap<>();

    private static final String ADD_EXPENSE_URL = Constants.BASE_URL + "add_expenses.php"; // Updated to plural

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_expense);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        // Initialize Views
        btnCatMaterials = findViewById(R.id.btnCatMaterials);
        btnCatLabor = findViewById(R.id.btnCatLabor);
        btnCatEquipment = findViewById(R.id.btnCatEquipment);
        btnCatTransport = findViewById(R.id.btnCatTransport);
        btnCatUtilities = findViewById(R.id.btnCatUtilities);
        btnCatOther = findViewById(R.id.btnCatOther);

        etTitle = findViewById(R.id.etTitle); // NEW
        etDate = findViewById(R.id.etDate);
        etAmount = findViewById(R.id.etAmount);
        etDescription = findViewById(R.id.etDescription);
        etInvoice = findViewById(R.id.etInvoice);
        actvProject = findViewById(R.id.actvProject);

        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);

        // Setup Category Selection
        setupCategoryListeners();
        updateCategorySelection(btnCatMaterials); // Select Materials by default

        // Date Picker
        etDate.setOnClickListener(v -> showDatePicker());

        // Load projects for the dropdown
        loadProjects();

        actvProject.setOnItemClickListener((parent, view, position, id) -> {
            String selectedName = (String) parent.getItemAtPosition(position);
            Integer pId = projectNameToIdMap.get(selectedName);
            if (pId != null && pId == -1) {
                selectedProjectId = null; // "Select Project" chosen
            } else {
                selectedProjectId = pId;
            }
        });

        actvProject.setOnClickListener(v -> actvProject.showDropDown());
        actvProject.setOnFocusChangeListener((v, hasFocus) -> {
           if(hasFocus) actvProject.showDropDown();
        });

        // Buttons
        btnSave.setOnClickListener(v -> saveExpense());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void setupCategoryListeners() {
        View.OnClickListener listener = v -> {
            updateCategorySelection((LinearLayout) v);
            if (v.getId() == R.id.btnCatMaterials) selectedCategory = "Materials";
            else if (v.getId() == R.id.btnCatLabor) selectedCategory = "Labor";
            else if (v.getId() == R.id.btnCatEquipment) selectedCategory = "Equipment";
            else if (v.getId() == R.id.btnCatTransport) selectedCategory = "Transportation";
            else if (v.getId() == R.id.btnCatUtilities) selectedCategory = "Utilities";
            else if (v.getId() == R.id.btnCatOther) selectedCategory = "Other";
        };

        btnCatMaterials.setOnClickListener(listener);
        btnCatLabor.setOnClickListener(listener);
        btnCatEquipment.setOnClickListener(listener);
        btnCatTransport.setOnClickListener(listener);
        btnCatUtilities.setOnClickListener(listener);
        btnCatOther.setOnClickListener(listener);
    }

    private void updateCategorySelection(LinearLayout selected) {
        // Reset all
        resetCategoryStyle(btnCatMaterials);
        resetCategoryStyle(btnCatLabor);
        resetCategoryStyle(btnCatEquipment);
        resetCategoryStyle(btnCatTransport);
        resetCategoryStyle(btnCatUtilities);
        resetCategoryStyle(btnCatOther);

        // Highlight selected
        selected.setBackgroundResource(R.drawable.rounded_square_bg_light_blue);
    }

    private void resetCategoryStyle(LinearLayout layout) {
        layout.setBackgroundResource(R.drawable.rounded_square);
    }

    private void showDatePicker() {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            // Ensure yyyy-MM-dd format (zero-padded)
            String dateFormatted = String.format(Locale.getDefault(), "%d-%02d-%02d", year, month + 1, dayOfMonth);
            etDate.setText(dateFormatted);
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private String getToken() {
        return getSharedPreferences("contractor_session", MODE_PRIVATE).getString("api_token", "");
    }

    private void loadProjects() {
        String url = Constants.BASE_URL + "get_projects_v2.php";
        
        Log.d("ADD_EXPENSE_DEBUG", "Using token = " + getToken());
        Log.d("ADD_EXPENSE_DEBUG", "Calling = " + url);

        com.android.volley.toolbox.JsonObjectRequest request = new com.android.volley.toolbox.JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                         if (!"success".equalsIgnoreCase(response.optString("status"))) {
                            Toast.makeText(this, response.optString("message", "Failed to load projects"), Toast.LENGTH_SHORT).show();
                            return;
                        }

                        org.json.JSONArray projects = response.getJSONArray("projects");
                        List<String> projectNames = new ArrayList<>();
                        projectNameToIdMap.clear();

                        projectNames.add("Select Project");
                        projectNameToIdMap.put("Select Project", -1);

                        for (int i = 0; i < projects.length(); i++) {
                            org.json.JSONObject obj = projects.getJSONObject(i);
                            String title = obj.getString("title");
                            int id = obj.getInt("project_id"); // Ensure get_projects_v2 returns project_id
                            
                            projectNames.add(title);
                            projectNameToIdMap.put(title, id);
                        }

                        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, projectNames);
                        actvProject.setAdapter(adapter);

                        // Preselect Logic
                        if (getIntent().hasExtra("project_id")) {
                            int passedId = getIntent().getIntExtra("project_id", -1);
                            String passedName = getIntent().getStringExtra("project_name"); 
                            
                            if (passedName == null && passedId != -1) {
                                for (Map.Entry<String, Integer> entry : projectNameToIdMap.entrySet()) {
                                    if (entry.getValue() == passedId) {
                                        passedName = entry.getKey();
                                        break;
                                    }
                                }
                            }
                            
                            if (passedName != null && projectNameToIdMap.containsKey(passedName)) {
                                actvProject.setText(passedName, false);
                                selectedProjectId = projectNameToIdMap.get(passedName);
                            }
                        }

                    } catch (Exception e) {
                        Log.e("ADD_EXPENSE", "Parsing Error", e);
                    }
                },
                error -> {
                    Log.e("ADD_EXPENSE", "Error", error);
                    Toast.makeText(this, "Failed to load projects: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", getToken());
                return headers;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }

    private void saveExpense() {
        if (selectedCategory.isEmpty()) {
            Toast.makeText(this, "Select category", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedProjectId == null) {
            Toast.makeText(this, "Please select a project", Toast.LENGTH_SHORT).show();
            return;
        }

        String selectedDate = etDate.getText().toString().trim();
        if (selectedDate.isEmpty() || selectedDate.equals("Select Date")) {
             Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show();
             return;
        }

        String title = etTitle.getText().toString().trim();
        if(title.isEmpty()){
            Toast.makeText(this, "Enter Title (e.g. Cement)", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if(etAmount.getText().toString().trim().isEmpty()){
            Toast.makeText(this, "Enter Amount", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String description = etDescription.getText().toString().trim();
        // Description is optional now since we have Title
        // if(description.isEmpty()){ ... } 

        StringRequest request = new StringRequest(
                Request.Method.POST,
                ADD_EXPENSE_URL,
                response -> {
                    String res = response.trim();
                    Log.d("EXPENSE_RESPONSE", res);

                    if (res.equals("success")) {
                        Toast.makeText(this, "Expense added", Toast.LENGTH_SHORT).show();
                        finish();
                    } else if (res.equals("missing")) {
                        Toast.makeText(this, "Fill all details (Server)", Toast.LENGTH_SHORT).show();
                    } else {
                        String msg = res;
                        if (res.startsWith("error|")) {
                            msg = res.substring(6);
                        }
                        Toast.makeText(this, "Server error: " + msg, Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    String errorMsg = "Network Error";
                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        try {
                            String body = new String(error.networkResponse.data, "UTF-8");
                            Log.e("EXPENSE_ERROR_BODY", body);
                            errorMsg = "Server Error: " + error.networkResponse.statusCode;
                        } catch (Exception e) {
                            Log.e("EXPENSE_ERROR_BODY", "Error reading body", e);
                        }
                    } else if (error instanceof com.android.volley.TimeoutError) {
                        errorMsg = "Connection Timed Out";
                    } else if (error instanceof com.android.volley.NoConnectionError) {
                         errorMsg = "No Connection to Server";
                    } else if (error instanceof com.android.volley.AuthFailureError) {
                        errorMsg = "Auth Failed";
                    } else if (error instanceof com.android.volley.ServerError) {
                        errorMsg = "Server Error";
                    } else if (error instanceof com.android.volley.NetworkError) {
                        errorMsg = "Network Error";
                    } else if (error instanceof com.android.volley.ParseError) {
                        errorMsg = "Parse Error";
                    }
                    
                    Log.e("EXPENSE_ERROR", error.toString());
                    Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("project_id", String.valueOf(selectedProjectId));
                params.put("category", selectedCategory);
                params.put("title", title); 
                params.put("description", description);
                params.put("invoice_no", etInvoice.getText().toString().trim());
                params.put("amount", etAmount.getText().toString().trim());
                params.put("expense_date", selectedDate);
                
                Log.d("EXPENSE_PARAMS", params.toString());
                return params;
            }
        };

        // STEP 4 — FIX ANDROID VOLLEY TIMEOUT
        request.setRetryPolicy(
            new com.android.volley.DefaultRetryPolicy(
                15000, 
                com.android.volley.DefaultRetryPolicy.DEFAULT_MAX_RETRIES, 
                com.android.volley.DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
            )
        );

        Volley.newRequestQueue(this).add(request);
    }
}
