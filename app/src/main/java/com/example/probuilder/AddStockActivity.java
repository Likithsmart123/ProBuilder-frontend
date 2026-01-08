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

public class AddStockActivity extends AppCompatActivity {

    private AutoCompleteTextView actvMaterial;
    private TextInputEditText etQuantity;
    private Button btnSave;

    private Map<String, Integer> materialNameToIdMap = new HashMap<>();
    private Integer selectedMaterialId = null;

    private static final String MATERIALS_URL = "http://10.0.2.2:5000/materials?contractor_id=1";
    private static final String ADD_STOCK_URL = "http://10.0.2.2:5000/add-stock";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_stock);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        actvMaterial = findViewById(R.id.actvMaterial);
        etQuantity = findViewById(R.id.etQuantity);
        btnSave = findViewById(R.id.btnSave);

        loadMaterials();

        actvMaterial.setOnItemClickListener((parent, view, position, id) -> {
            String selectedName = (String) parent.getItemAtPosition(position);
            selectedMaterialId = materialNameToIdMap.get(selectedName);
        });

        btnSave.setOnClickListener(v -> saveStock());
    }

    private void loadMaterials() {
        // CORRECTED: Using static data for UI verification as requested.
        List<String> materialNames = new ArrayList<>();
        materialNames.add("Cement");
        materialNames.add("Sand");
        materialNames.add("Bricks");
        materialNames.add("Steel Rods");
        materialNames.add("Paint");

        materialNameToIdMap.clear();
        materialNameToIdMap.put("Cement", 1);
        materialNameToIdMap.put("Sand", 2);
        materialNameToIdMap.put("Bricks", 3);
        materialNameToIdMap.put("Steel Rods", 4);
        materialNameToIdMap.put("Paint", 5);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, materialNames);
        actvMaterial.setAdapter(adapter);
        
        /* --- Backend code is temporarily disabled ---
        StringRequest request = new StringRequest(Request.Method.GET, MATERIALS_URL,
                response -> {
                    try {
                        JSONObject root = new JSONObject(response);
                        JSONArray materialsArray = root.getJSONArray("materials");
                        List<String> backendMaterialNames = new ArrayList<>();
                        materialNameToIdMap.clear();

                        for (int i = 0; i < materialsArray.length(); i++) {
                            JSONObject obj = materialsArray.getJSONObject(i);
                            String name = obj.getString("name");
                            int id = obj.getInt("id");
                            backendMaterialNames.add(name);
                            materialNameToIdMap.put(name, id);
                        }

                        ArrayAdapter<String> backendAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, backendMaterialNames);
                        actvMaterial.setAdapter(backendAdapter);

                    } catch (Exception e) {
                        Log.e("AddStockActivity", "JSON Parsing Error: " + e.getMessage());
                        Toast.makeText(this, "Error parsing materials", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("AddStockActivity", "Volley Error: " + error.toString());
                    Toast.makeText(this, "Failed to load materials", Toast.LENGTH_SHORT).show();
                });

        Volley.newRequestQueue(this).add(request);
        */
    }

    private void saveStock() {
        final String quantityStr = etQuantity.getText().toString().trim();

        if (selectedMaterialId == null) {
            Toast.makeText(this, "Please select a material", Toast.LENGTH_SHORT).show();
            return;
        }

        if (quantityStr.isEmpty()) {
            etQuantity.setError("Quantity is required");
            etQuantity.requestFocus();
            return;
        }

        btnSave.setEnabled(false); // Disable button

        // Since we are using static data, we will just show a success message.
        Toast.makeText(AddStockActivity.this, "Stock added successfully! (Static)", Toast.LENGTH_SHORT).show();
        finish();

        /* --- Backend code is temporarily disabled ---
        StringRequest stringRequest = new StringRequest(Request.Method.POST, ADD_STOCK_URL,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        if ("success".equals(jsonResponse.getString("status"))) {
                            Toast.makeText(AddStockActivity.this, "Stock added successfully!", Toast.LENGTH_SHORT).show();
                            finish(); // Go back to the inventory screen
                        } else {
                            Toast.makeText(AddStockActivity.this, "Failed: " + jsonResponse.getString("message"), Toast.LENGTH_LONG).show();
                            btnSave.setEnabled(true);
                        }
                    } catch (Exception e) {
                         Toast.makeText(AddStockActivity.this, "Error parsing response.", Toast.LENGTH_SHORT).show();
                         btnSave.setEnabled(true);
                    }
                },
                error -> {
                    Toast.makeText(AddStockActivity.this, "Network Error: " + error.toString(), Toast.LENGTH_LONG).show();
                    btnSave.setEnabled(true);
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("material_id", String.valueOf(selectedMaterialId));
                params.put("quantity", quantityStr);
                params.put("contractor_id", "1"); // Assuming contractor_id is needed
                return params;
            }
        };

        Volley.newRequestQueue(this).add(stringRequest);
        */
    }
}
