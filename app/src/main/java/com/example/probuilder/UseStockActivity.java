package com.example.probuilder;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UseStockActivity extends AppCompatActivity {

    private android.widget.Spinner spinnerMaterial;
    private android.widget.Spinner spinnerProject;
    private TextInputEditText etQuantity;
    private TextInputEditText etSpecifications;
    private Button btnSaveUsage;

    private Map<String, Integer> materialNameToIdMap = new HashMap<>();
    private Map<String, Integer> projectNameToIdMap = new HashMap<>();
    private Integer selectedMaterialId = null;
    private Integer selectedProjectId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_use_stock);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        // 3.1 Spinner binding
        spinnerMaterial = findViewById(R.id.spinnerMaterial);
        spinnerProject = findViewById(R.id.spinnerProject);
        etQuantity = findViewById(R.id.etQuantity);
        etSpecifications = findViewById(R.id.etSpecifications);
        btnSaveUsage = findViewById(R.id.btnSaveUsage);

        // 3.2 This call in onCreate()
        loadMaterials();
        // loadProjects(); // REMOVED auto-load

        // Initialize Project Spinner for Lazy Load
        projectNames.add("Select Project");
        projectAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, projectNames);
        projectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProject.setAdapter(projectAdapter);

        // If launched from ProjectDetailActivity, auto-load projects
        if (getIntent().hasExtra("project_id")) {
            loadProjects();
        }

        spinnerMaterial.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                String selectedName = (String) parent.getItemAtPosition(position);
                selectedMaterialId = materialNameToIdMap.get(selectedName);
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                selectedMaterialId = null;
            }
        });

        // Lazy Load Touch Listener
        spinnerProject.setOnTouchListener((v, event) -> {
             if (event.getAction() == android.view.MotionEvent.ACTION_UP && !isProjectsLoaded) {
                 loadProjects();
             }
             return false;
        });

        spinnerProject.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                String selectedName = (String) parent.getItemAtPosition(position);
                selectedProjectId = projectNameToIdMap.get(selectedName);
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                selectedProjectId = null;
            }
        });

        btnSaveUsage.setOnClickListener(v -> saveUsage());
    }

    // Members for Lazy Loading
    private List<String> projectNames = new ArrayList<>();
    private ArrayAdapter<String> projectAdapter;
    private boolean isProjectsLoaded = false;

    private void loadMaterials() {
        android.content.SharedPreferences sp = getSharedPreferences("ProBuilderPrefs", MODE_PRIVATE);
        // Correct session for token
        android.content.SharedPreferences session = getSharedPreferences("contractor_session", MODE_PRIVATE);
        String token = session.getString("api_token", "");
        int contractorId = sp.getInt("contractor_id", -1);

        Log.d("MATERIAL_API", "Sending token = " + token);
        Log.d("MATERIAL_API", "Calling get_materials API");

        String url = Constants.BASE_URL + "get_materials.php?contractor_id=" + contractorId;
        Log.d("UseStock", "Fetching Materials: " + url);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        Log.d("MATERIAL_API", "Response received. Length: " + response.length());
                        JSONArray jsonArray = new JSONArray(response);
                        Log.d("MATERIAL_API", "Materials count = " + jsonArray.length());
                        
                        List<String> materialNames = new ArrayList<>();
                        materialNameToIdMap.clear();

                        materialNames.add("Select Material");

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject obj = jsonArray.getJSONObject(i);
                            String id = obj.getString("id"); // php might return string
                            String name = obj.getString("material_name");
                            // We can also show current stock in the name if desired
                            int currentStock = obj.optInt("current_stock", 0);
                            String unit = obj.optString("unit", "");
                            
                            String displayName = name + " (Stock: " + currentStock + " " + unit + ")";
                            
                            materialNameToIdMap.put(displayName, Integer.parseInt(id));
                            materialNames.add(displayName);
                        }

                        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, materialNames);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerMaterial.setAdapter(adapter);

                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e("MATERIAL_API", "Parsing Error", e);
                        Log.e("UseStock", "Parsing Error", e);
                        Toast.makeText(UseStockActivity.this, "Error parsing materials", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("MATERIAL_API", "API failed", error);
                    if (error.networkResponse != null) {
                         Log.d("MATERIAL_API", "Response code = " + error.networkResponse.statusCode);
                    }
                    Log.e("UseStock", "Network Error: " + error.toString());
                    Toast.makeText(UseStockActivity.this, "Error loading materials", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                // Correct Prefs file
                String token = getSharedPreferences("contractor_session", MODE_PRIVATE).getString("api_token", "");
                if (!token.isEmpty()) {
                    headers.put("Authorization", token);
                }
                return headers;
            }
        };

        Volley.newRequestQueue(this).add(stringRequest);
    }

    private void loadProjects() {
        Toast.makeText(this, "Loading projects...", Toast.LENGTH_SHORT).show();
        String url = Constants.BASE_URL + "get_projects_v2.php";

        // STEP 2 — ALWAYS READ TOKEN LIKE THIS
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
                    Log.d("USE_STOCK_PROJECT", response.toString());
                    try {
                        JSONArray projects = response.optJSONArray("projects");
                        if (projects == null) {
                             // Do nothing if empty, already has "Select Project"
                             return;
                        }

                        projectNames.clear();
                        projectNameToIdMap.clear();

                        projectNames.add("Select Project");

                        for (int i = 0; i < projects.length(); i++) {
                            JSONObject obj = projects.getJSONObject(i);
                            int id = obj.getInt("project_id");
                            String title = obj.getString("title");

                            projectNameToIdMap.put(title, id);
                            projectNames.add(title);
                        }

                        if (projectNames.size() == 1) {
                             Toast.makeText(this, "No projects found.", Toast.LENGTH_SHORT).show();
                        }

                        projectAdapter.notifyDataSetChanged();
                        isProjectsLoaded = true;

                        // Pre-select project if launched from ProjectDetailActivity
                        if (getIntent().hasExtra("project_id")) {
                            int passedProjectId = getIntent().getIntExtra("project_id", -1);
                            if (passedProjectId > 0) {
                                for (int idx = 0; idx < projectNames.size(); idx++) {
                                    String pName = projectNames.get(idx);
                                    Integer pId = projectNameToIdMap.get(pName);
                                    if (pId != null && pId == passedProjectId) {
                                        spinnerProject.setSelection(idx);
                                        selectedProjectId = pId;
                                        break;
                                    }
                                }
                            }
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e("PROJECT_PARSE_EXCEPTION", "Error parsing projects", e);
                    }
                },
                error -> {
                    Log.e("PROJECT_NETWORK_ERROR", error.toString());
                    Toast.makeText(UseStockActivity.this, "Error loading projects.", Toast.LENGTH_SHORT).show();
                }
        );

        request.setShouldCache(false);
        MyVolley.get(this).add(request);
    }

    private void saveUsage() {
        String quantityStr = etQuantity.getText().toString().trim();
        String specificationsStr = etSpecifications != null && etSpecifications.getText() != null ? etSpecifications.getText().toString().trim() : "";

        if (selectedMaterialId == null || selectedProjectId == null || quantityStr.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSaveUsage.setEnabled(false);
        String url = Constants.BASE_URL + "use_stock.php";

        android.content.SharedPreferences session = getSharedPreferences("contractor_session", MODE_PRIVATE);
        String token = session.getString("api_token", "");
        Log.d("USE_STOCK", "TOKEN = " + token);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    btnSaveUsage.setEnabled(true);
                     Log.d("USE_STOCK", "Response: " + response);
                    String res = response.trim();
                    try {
                        JSONObject obj = new JSONObject(res);
                        if (obj.optString("status").equals("success")) {
                            Toast.makeText(this, "Stock usage recorded!", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(this, obj.optString("message", "Failed"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        // Fallback for plain text response if any
                        if (res.equals("success")) {
                             Toast.makeText(this, "Stock usage recorded!", Toast.LENGTH_SHORT).show();
                             finish();
                        } else if (res.equals("insufficient")) {
                             Toast.makeText(this, "Insufficient stock!", Toast.LENGTH_LONG).show();
                        } else {
                             Toast.makeText(this, "Failed: " + res, Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                error -> {
                    btnSaveUsage.setEnabled(true);
                    Log.e("USE_STOCK", "Error", error);
                    Toast.makeText(this, "Network Error", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                if (!token.isEmpty()) {
                     headers.put("Authorization", token);
                }
                return headers;
            }

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("material_id", String.valueOf(selectedMaterialId));
                // CORRECT PARAM NAME: used_quantity
                params.put("used_quantity", quantityStr);
                params.put("project_id", String.valueOf(selectedProjectId));
                params.put("specifications", specificationsStr);
                return params;
            }
        };

        Volley.newRequestQueue(this).add(stringRequest);
    }
}
