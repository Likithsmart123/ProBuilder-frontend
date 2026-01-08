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

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MaterialInventoryActivity extends AppCompatActivity {

    private RecyclerView rvMaterials;
    private MaterialAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_material_inventory);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Material Management");
        toolbar.setNavigationOnClickListener(v -> finish());

        rvMaterials = findViewById(R.id.rvMaterials);
        rvMaterials.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MaterialAdapter(this);
        rvMaterials.setAdapter(adapter);

        // CORRECTED: Set click listeners for Add Stock and Use Stock
        MaterialButton btnAddStock = findViewById(R.id.btnAddStock);
        btnAddStock.setOnClickListener(v -> startActivity(new Intent(this, AddStockActivity.class)));

        MaterialButton btnUseStock = findViewById(R.id.btnUseStock);
        btnUseStock.setOnClickListener(v -> startActivity(new Intent(this, UseStockActivity.class)));

        // TODO: Add listener for fabAddNewMaterial

        // Load static data for UI verification
        loadStaticData();
    }

    private void loadStaticData() {
        List<Material> staticMaterials = new ArrayList<>();
        staticMaterials.add(new Material(1, "Cement Bags", 15, 50, "bags"));
        staticMaterials.add(new Material(2, "Steel Rods", 35, 40, "kg"));
        staticMaterials.add(new Material(3, "Sand", 8, 5, "tons"));
        adapter.setMaterials(staticMaterials);
    }

    // The backend loading is temporarily disabled for UI verification.
    private void loadMaterials() {
        String url = "http://10.0.2.2:5000/materials?contractor_id=1"; 

        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject root = new JSONObject(response);
                        JSONArray materialsArray = root.getJSONArray("materials");
                        List<Material> materials = new ArrayList<>();

                        for (int i = 0; i < materialsArray.length(); i++) {
                            JSONObject obj = materialsArray.getJSONObject(i);
                            materials.add(new Material(
                                    obj.getInt("id"),
                                    obj.getString("name"),
                                    obj.getInt("current_stock"),
                                    obj.getInt("min_stock"),
                                    obj.getString("unit")
                            ));
                        }
                        adapter.setMaterials(materials);

                    } catch (Exception e) {
                        Log.e("MaterialInventory", "JSON Parsing Error: " + e.getMessage());
                        Toast.makeText(this, "Error parsing material data", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("MaterialInventory", "Volley Error: " + error.toString());
                    Toast.makeText(this, "Failed to load materials", Toast.LENGTH_SHORT).show();
                });

        Volley.newRequestQueue(this).add(request);
    }
}
