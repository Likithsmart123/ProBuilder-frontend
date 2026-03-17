package com.example.probuilder;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddStockActivity extends AppCompatActivity {

    private Spinner spinnerMaterial;
    private TextInputEditText etQuantity;
    private Button btnSave;

    private List<String> materialNames = new ArrayList<>();
    private List<String> materialIds = new ArrayList<>();
    private String selectedMaterialId = "";

    // Loaded from SharedPreferences on startup
    private String contractorId = "-1";

    private static final String GET_MATERIALS_URL = Constants.BASE_URL + "get_materials.php";
    private static final String ADD_STOCK_URL = Constants.BASE_URL + "add_stock.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_stock);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        spinnerMaterial = findViewById(R.id.spinnerMaterial);
        etQuantity = findViewById(R.id.etQuantity);
        btnSave = findViewById(R.id.btnSave);

        // Load real contractor_id from SharedPreferences
        android.content.SharedPreferences sp = getSharedPreferences("ProBuilderPrefs", MODE_PRIVATE);
        contractorId = String.valueOf(sp.getInt("contractor_id", -1));

        loadMaterials();

        spinnerMaterial.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < materialIds.size()) {
                    selectedMaterialId = materialIds.get(position);
                    if (selectedMaterialId.isEmpty()) {
                        Log.d("MATERIAL_SELECT", "No material selected");
                    } else {
                        Log.d("MATERIAL_SELECT", "Selected ID: " + selectedMaterialId);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedMaterialId = "";
            }
        });

        btnSave.setOnClickListener(v -> saveStock());
    }

    private void loadMaterials() {
        if ("-1".equals(contractorId)) {
            Toast.makeText(this, "Error: User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }
        // Change to GET with contractor_id so materials are filtered per-contractor
        StringRequest request = new StringRequest(
                Request.Method.GET,
                GET_MATERIALS_URL + "?contractor_id=" + contractorId,
                response -> {
                    try {
                        Log.d("MATERIAL_RAW", response);

                        JSONArray arr = new JSONArray(response);

                        materialNames.clear();
                        materialIds.clear();

                        // 🔹 DEFAULT OPTION
                        materialNames.add("Select Material");
                        materialIds.add("");

                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject obj = arr.getJSONObject(i);
                            materialIds.add(obj.getString("id"));
                            materialNames.add(obj.getString("material_name"));
                        }

                        ArrayAdapter<String> adapter =
                                new ArrayAdapter<>(
                                        this,
                                        android.R.layout.simple_spinner_item,
                                        materialNames
                                );

                        adapter.setDropDownViewResource(
                                android.R.layout.simple_spinner_dropdown_item
                        );

                        spinnerMaterial.setAdapter(adapter);

                    } catch (Exception e) {
                        Log.e("MATERIAL_PARSE_ERROR", e.toString());
                        Toast.makeText(this, "Error parsing materials", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("MATERIAL_NET_ERROR", error.toString());
                    Toast.makeText(this, "Network Error", Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                String token = getSharedPreferences("contractor_session", MODE_PRIVATE).getString("api_token", "");
                if (!token.isEmpty()) {
                    headers.put("Authorization", token);
                }
                return headers;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }

    private void saveStock() {
        final String quantityStr = etQuantity.getText().toString().trim();

        if (selectedMaterialId.isEmpty()) {
            Toast.makeText(this, "Please select a material", Toast.LENGTH_SHORT).show();
            return;
        }

        if (quantityStr.isEmpty()) {
            etQuantity.setError("Quantity is required");
            etQuantity.requestFocus();
            return;
        }

        btnSave.setEnabled(false); // Disable button

        StringRequest stringRequest = new StringRequest(Request.Method.POST, ADD_STOCK_URL,
                response -> {
                    String res = response.trim();
                    try {
                        if (res.equals("success")) {
                            Toast.makeText(AddStockActivity.this, "Stock added successfully!", Toast.LENGTH_SHORT).show();
                            finish(); // Go back to the inventory screen
                        } else {
                            String msg = "Failed to add stock";
                            if (res.startsWith("error|")) {
                                msg = res.substring(6);
                            }
                            Toast.makeText(AddStockActivity.this, "Failed: " + msg, Toast.LENGTH_LONG).show();
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
                params.put("material_id", selectedMaterialId);
                params.put("quantity", quantityStr);
                // params.put("contractor_id", "1"); // Not needed for simple increment if logic is in PHP
                return params;
            }
        };

        Volley.newRequestQueue(this).add(stringRequest);
    }
}
