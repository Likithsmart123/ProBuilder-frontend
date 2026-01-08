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

    private AutoCompleteTextView actvMaterial, actvProject;
    private TextInputEditText etQuantity;
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

        actvMaterial = findViewById(R.id.actvMaterial);
        actvProject = findViewById(R.id.actvProject);
        etQuantity = findViewById(R.id.etQuantity);
        btnSaveUsage = findViewById(R.id.btnSaveUsage);

        loadMaterials();
        loadProjects();

        actvMaterial.setOnItemClickListener((parent, view, position, id) -> {
            String selectedName = (String) parent.getItemAtPosition(position);
            selectedMaterialId = materialNameToIdMap.get(selectedName);
        });

        actvProject.setOnItemClickListener((parent, view, position, id) -> {
            String selectedName = (String) parent.getItemAtPosition(position);
            selectedProjectId = projectNameToIdMap.get(selectedName);
        });

        btnSaveUsage.setOnClickListener(v -> saveUsage());
    }

    private void loadMaterials() {
        // CORRECTED: Using static data for UI verification.
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
    }

    private void loadProjects() {
        // CORRECTED: Using static data for UI verification.
        List<String> projectNames = new ArrayList<>();
        projectNames.add("Villa Renovation");
        projectNames.add("Downtown Office Build");
        projectNames.add("Suburban Mall Construction");

        projectNameToIdMap.clear();
        projectNameToIdMap.put("Villa Renovation", 1);
        projectNameToIdMap.put("Downtown Office Build", 2);
        projectNameToIdMap.put("Suburban Mall Construction", 3);
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, projectNames);
        actvProject.setAdapter(adapter);
    }

    private void saveUsage() {
        String quantityStr = etQuantity.getText().toString().trim();

        if (selectedMaterialId == null || selectedProjectId == null || quantityStr.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Since we are using static data, we will just show a success message.
        Toast.makeText(this, "Stock usage saved! (Static)", Toast.LENGTH_SHORT).show();
        finish(); 
    }
}
