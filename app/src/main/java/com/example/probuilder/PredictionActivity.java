package com.example.probuilder;

import android.graphics.Color;
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

public class PredictionActivity extends AppCompatActivity {

    Spinner spMaterial;
    EditText etQuantity;
    Button btnPredict;
    TextView tvResult;
    TextView tvMaterialName, tvQuantity, tvPredictedPrice, tvTotalCost;
    ProgressBar progressBar;
    MaterialCardView cardResult;

    // College server API URL
    String url = Constants.BASE_URL + "predict_material_price.php";

    // Index 0 = prompt, 1=Cement, 2=Sand, 3=Steel Rods
    int[] materialIds = {0, 1, 2, 3};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prediction);

        // Back button
        ImageView ivBack = findViewById(R.id.ivBack);
        ivBack.setOnClickListener(v -> finish());

        spMaterial      = findViewById(R.id.spMaterial);
        etQuantity      = findViewById(R.id.etQuantity);
        btnPredict      = findViewById(R.id.btnPredict);
        tvResult        = findViewById(R.id.tvResult);
        progressBar     = findViewById(R.id.progressBar);
        cardResult      = findViewById(R.id.cardResult);
        tvMaterialName  = findViewById(R.id.tvMaterialName);
        tvQuantity      = findViewById(R.id.tvQuantity);
        tvPredictedPrice = findViewById(R.id.tvPredictedPrice);
        tvTotalCost     = findViewById(R.id.tvTotalCost);

        // Spinner with "Select Material" prompt
        String[] materials = {"Select Material", "Cement", "Sand", "Steel Rods"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, materials) {
            @Override
            public boolean isEnabled(int position) {
                return position != 0; // disable prompt item
            }
            @Override
            public View getDropDownView(int position, View convertView, android.view.ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                tv.setTextColor(position == 0 ? Color.GRAY : Color.BLACK);
                return view;
            }
        };
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spMaterial.setAdapter(adapter);
        spMaterial.setSelection(0);

        btnPredict.setOnClickListener(v -> predictPrice());
    }

    private void predictPrice() {
        int position = spMaterial.getSelectedItemPosition();
        if (position == 0) {
            Toast.makeText(this, "Please select a material", Toast.LENGTH_SHORT).show();
            return;
        }

        final int materialId  = materialIds[position];
        final String quantity = etQuantity.getText().toString().trim();

        if (quantity.isEmpty()) {
            Toast.makeText(this, "Please enter quantity", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading, hide previous result
        progressBar.setVisibility(View.VISIBLE);
        btnPredict.setEnabled(false);
        cardResult.setVisibility(View.GONE);
        tvResult.setVisibility(View.GONE);

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    progressBar.setVisibility(View.GONE);
                    btnPredict.setEnabled(true);
                    try {
                        JSONObject obj = new JSONObject(response);

                        // Check for server-side error
                        if (obj.has("error")) {
                            tvResult.setText("Server: " + obj.getString("error"));
                            tvResult.setVisibility(View.VISIBLE);
                            return;
                        }

                        // Parse the fields the college API actually sends
                        String material       = obj.getString("material");
                        double predictedPrice = obj.getDouble("predicted_price");
                        String unit           = obj.getString("unit");
                        String qty            = obj.getString("quantity");
                        double totalCost      = obj.getDouble("total_cost");

                        // Populate result card
                        tvMaterialName.setText(material);
                        tvQuantity.setText(qty + " " + unit);
                        tvPredictedPrice.setText("₹" + formatNum(predictedPrice) + " / " + unit);
                        tvTotalCost.setText("₹" + formatNum(totalCost));

                        // Show result card
                        cardResult.setVisibility(View.VISIBLE);
                        tvResult.setVisibility(View.GONE);

                    } catch (Exception e) {
                        tvResult.setText("Error parsing response. Check server data.");
                        tvResult.setVisibility(View.VISIBLE);
                        cardResult.setVisibility(View.GONE);
                    }
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    btnPredict.setEnabled(true);
                    tvResult.setText("Network error. Check connection.");
                    tvResult.setVisibility(View.VISIBLE);
                    cardResult.setVisibility(View.GONE);
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("material_id", String.valueOf(materialId));
                params.put("quantity", quantity);
                return params;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }

    /** Format: show integer if no decimals, else 2 decimal places */
    private String formatNum(double value) {
        if (value == Math.floor(value)) {
            return String.valueOf((long) value);
        }
        return String.format("%.2f", value);
    }
}
