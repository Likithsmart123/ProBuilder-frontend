package com.example.probuilder;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.card.MaterialCardView;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MaterialCalculatorActivity extends AppCompatActivity {

    AutoCompleteTextView actvMaterialName;
    EditText etPricePerUnit, etQuantity;
    Button btnCalculate;
    TextView tvResult, tvTotalCost;
    ProgressBar progressBar;
    MaterialCardView cardResult;

    String url = Constants.BASE_URL + "calculate_material_cost.php";
    String getMaterialsUrl = Constants.BASE_URL + "get_materials.php";
    
    // Store fetched materials to populate adapter
    java.util.List<String> materialNameList = new java.util.ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_material_calculator);

        ImageView ivBack = findViewById(R.id.ivBack);
        ivBack.setOnClickListener(v -> finish());

        actvMaterialName = findViewById(R.id.actvMaterialName);
        etPricePerUnit  = findViewById(R.id.etPricePerUnit);
        etQuantity      = findViewById(R.id.etQuantity);
        btnCalculate    = findViewById(R.id.btnCalculate);
        tvResult        = findViewById(R.id.tvResult);
        progressBar     = findViewById(R.id.progressBar);
        cardResult      = findViewById(R.id.cardResult);
        tvTotalCost     = findViewById(R.id.tvTotalCost);

        btnCalculate.setOnClickListener(v -> calculateCost());
        
        // Load real contractor_id
        android.content.SharedPreferences sp = getSharedPreferences("ProBuilderPrefs", MODE_PRIVATE);
        int contractorId = sp.getInt("contractor_id", -1);
        if (contractorId > 0) {
            loadMaterials(contractorId);
        }
    }
    
    private void loadMaterials(int contractorId) {
        String urlWithParams = getMaterialsUrl + "?contractor_id=" + contractorId;
        StringRequest request = new StringRequest(Request.Method.GET, urlWithParams,
                response -> {
                    try {
                        org.json.JSONArray arr = new org.json.JSONArray(response);
                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject obj = arr.getJSONObject(i);
                            materialNameList.add(obj.getString("material_name"));
                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                this, android.R.layout.simple_dropdown_item_1line, materialNameList);
                        actvMaterialName.setAdapter(adapter);
                    } catch (Exception e) {
                        Toast.makeText(this, "Error loading materials list", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Failed to load materials", Toast.LENGTH_SHORT).show()) {
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

    private void calculateCost() {
        final String materialName = actvMaterialName.getText().toString().trim();
        final String pricePerUnit = etPricePerUnit.getText().toString().trim();
        final String quantity = etQuantity.getText().toString().trim();

        if (materialName.isEmpty()) {
            Toast.makeText(this, "Please enter material name", Toast.LENGTH_SHORT).show();
            return;
        }

        if (pricePerUnit.isEmpty()) {
            Toast.makeText(this, "Please enter price per unit", Toast.LENGTH_SHORT).show();
            return;
        }

        if (quantity.isEmpty()) {
            Toast.makeText(this, "Please enter quantity", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnCalculate.setEnabled(false);
        cardResult.setVisibility(View.GONE);
        tvResult.setVisibility(View.GONE);

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    progressBar.setVisibility(View.GONE);
                    btnCalculate.setEnabled(true);
                    try {
                        JSONObject obj = new JSONObject(response);

                        if (obj.has("error")) {
                            tvResult.setText("Server: " + obj.getString("error"));
                            tvResult.setVisibility(View.VISIBLE);
                            return;
                        }

                        double totalCost = obj.getDouble("total_cost");
                        tvTotalCost.setText("Total Cost : ₹" + formatNum(totalCost));

                        cardResult.setVisibility(View.VISIBLE);
                        tvResult.setVisibility(View.GONE);

                    } catch (Exception e) {
                        tvResult.setText("Error parsing response.");
                        tvResult.setVisibility(View.VISIBLE);
                        cardResult.setVisibility(View.GONE);
                    }
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    btnCalculate.setEnabled(true);
                    tvResult.setText("Network error.");
                    tvResult.setVisibility(View.VISIBLE);
                    cardResult.setVisibility(View.GONE);
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("material_name", materialName);
                params.put("price_per_unit", pricePerUnit);
                params.put("quantity", quantity);
                return params;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }

    private String formatNum(double value) {
        if (value == Math.floor(value)) {
            return String.valueOf((long) value);
        }
        return String.format("%.2f", value);
    }
}
