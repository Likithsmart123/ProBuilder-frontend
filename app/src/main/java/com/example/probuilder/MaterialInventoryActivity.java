package com.example.probuilder;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONObject;



import androidx.appcompat.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MaterialInventoryActivity extends AppCompatActivity {

    private RecyclerView rvMaterials;
    private MaterialAdapter adapter;
    private static final String ADD_MATERIAL_URL = Constants.BASE_URL + "add_material.php";
    private static final String DELETE_MATERIAL_URL = Constants.BASE_URL + "delete_material.php";
    private String contractorId = "-1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_material_inventory);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Material Management");
        toolbar.setNavigationOnClickListener(v -> finish());
        
        // Set Status Bar Color
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            android.view.Window window = getWindow();
            window.addFlags(android.view.WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(android.graphics.Color.parseColor("#1E6FE3"));
        }

        rvMaterials = findViewById(R.id.rvMaterials);
        rvMaterials.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MaterialAdapter(this);
        adapter.setOnDeleteListener((material, position) -> confirmDeleteMaterial(material, position));
        rvMaterials.setAdapter(adapter);

        // CORRECTED: Set click listeners for Add Stock and Use Stock
        MaterialButton btnAddStock = findViewById(R.id.btnAddStock);
        btnAddStock.setOnClickListener(v -> startActivity(new Intent(this, AddStockActivity.class)));

        MaterialButton btnUseStock = findViewById(R.id.btnUseStock);
        btnUseStock.setOnClickListener(v -> startActivity(new Intent(this, UseStockActivity.class)));

        FloatingActionButton fabAddNewMaterial = findViewById(R.id.fabAddNewMaterial);
        fabAddNewMaterial.setOnClickListener(v -> showAddMaterialDialog());

        // Load static data for UI verification
        // Load materials from backend
        // loadMaterials(); // Moved to onResume
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Always refresh contractor_id from SharedPrefs before loading
        android.content.SharedPreferences sp = getSharedPreferences("ProBuilderPrefs", MODE_PRIVATE);
        contractorId = String.valueOf(sp.getInt("contractor_id", -1));
        loadMaterials();
    }

    
    private void showAddMaterialDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_add_material, null);
        builder.setView(view);

        EditText etName = view.findViewById(R.id.etMaterialName);

        Spinner spinnerUnit = view.findViewById(R.id.spinnerUnit);
        EditText etMinStock = view.findViewById(R.id.etMinStock);

        // Predefined units
        String[] units = {"bags", "sqft", "kg", "tons", "nos"};
        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, units);
        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUnit.setAdapter(unitAdapter);

        builder.setTitle("Add Material");

        builder.setPositiveButton("Save", (dialog, which) -> {
            String name = etName.getText().toString().trim();
            String unit = spinnerUnit.getSelectedItem().toString();
            String minStock = etMinStock.getText().toString().trim();

            if (name.isEmpty() || minStock.isEmpty()) {
                Toast.makeText(this, "Fill all details", Toast.LENGTH_SHORT).show();
                return;
            }

            // Numeric check for unit is no longer needed as we use a spinner with valid text options.

            addMaterialToServer(name, unit, minStock);
        });

        builder.setNegativeButton("Cancel", null);

        builder.show();
    }

    private void addMaterialToServer(String name, String unit, String minStock) {
        StringRequest request = new StringRequest(
                Request.Method.POST,
                ADD_MATERIAL_URL,
                response -> {
                    if (response.trim().equals("success")) {
                        Toast.makeText(this, "Material added", Toast.LENGTH_SHORT).show();
                        loadMaterials(); // refresh material list
                    } else if (response.trim().equals("exists")) {
                        Toast.makeText(this, "Material already exists", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Server error: " + response, Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Network error", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("contractor_id", contractorId);
                params.put("material_name", name);
                params.put("unit", unit);
                params.put("min_stock", minStock);
                return params;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }

    private void confirmDeleteMaterial(Material material, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Material")
                .setMessage("Are you sure you want to delete \"" + material.getName() + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> deleteMaterial(material, position))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteMaterial(Material material, int position) {
        StringRequest request = new StringRequest(
                Request.Method.POST,
                DELETE_MATERIAL_URL,
                response -> {
                    try {
                        org.json.JSONObject json = new org.json.JSONObject(response.trim());
                        String status = json.optString("status");
                        String message = json.optString("message");
                        if ("success".equals(status)) {
                            adapter.removeItem(position);
                            Toast.makeText(this, "Material deleted", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Network error while deleting", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("material_id", String.valueOf(material.getId()));
                return params;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }

    private void loadStaticData() {
        List<Material> staticMaterials = new ArrayList<>();
        staticMaterials.add(new Material(1, "Cement Bags", 15, 50, "bags"));
        staticMaterials.add(new Material(2, "Steel Rods", 35, 40, "kg"));
        staticMaterials.add(new Material(3, "Sand", 8, 5, "tons"));
        adapter.setMaterials(staticMaterials);
    }

    // Valid Phase: loadMaterials
    private void loadMaterials() {
        if ("-1".equals(contractorId)) {
            Toast.makeText(this, "Error: User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }
        String url = Constants.BASE_URL + "get_materials.php?contractor_id=" + contractorId;

        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONArray arr = new JSONArray(response);
                        List<Material> materials = new ArrayList<>();

                        if (arr.length() > 0) {
                            for (int i = 0; i < arr.length(); i++) {
                                JSONObject obj = arr.getJSONObject(i);
                                int id = obj.getInt("id");
                                String name = obj.getString("material_name");
                                int currentStock = obj.getInt("current_stock");
                                int minStock = obj.getInt("min_stock");
                                String unit = obj.getString("unit");
                                
                                materials.add(new Material(id, name, currentStock, minStock, unit));
                            }
                        }
                        adapter.setMaterials(materials);

                    } catch (Exception e) {
                        Log.e("MaterialInventory", "Parsing Error: " + e.getMessage());
                        Toast.makeText(this, "Error parsing material data", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("MaterialInventory", "Volley Error: " + error.toString());
                    Toast.makeText(this, "Failed to load materials", Toast.LENGTH_SHORT).show();
                }) {
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
}
