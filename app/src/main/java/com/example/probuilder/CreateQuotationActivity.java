package com.example.probuilder;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreateQuotationActivity extends AppCompatActivity {

    Spinner spinnerClient, spinnerProject;
    EditText etQuotationTitle, etTotalAmount, etDescription;
    Button btnCreateQuotation;
    ImageView btnBack;

    List<String> clientNames = new ArrayList<>();
    List<Integer> clientIds = new ArrayList<>();

    List<String> projectNames = new ArrayList<>();
    List<Integer> projectIds = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_quotation);

        spinnerClient = findViewById(R.id.spinnerClient);
        spinnerProject = findViewById(R.id.spinnerProject);
        etQuotationTitle = findViewById(R.id.etQuotationTitle);
        etTotalAmount = findViewById(R.id.etTotalAmount);
        etDescription = findViewById(R.id.etDescription);
        btnCreateQuotation = findViewById(R.id.btnCreateQuotation);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());
        
        loadClients();
        loadProjects();

        btnCreateQuotation.setOnClickListener(v -> submitQuotation());
    }

    private void loadClients() {
        String url = "http://10.0.2.2:5000/clients?contractor_id=1";

        StringRequest request = new StringRequest(
            Request.Method.GET,
            url,
            response -> {
                try {
                    JSONObject json = new JSONObject(response);
                    JSONArray array = json.getJSONArray("clients");

                    clientNames.clear();
                    clientIds.clear();

                    clientNames.add("Select Client");
                    clientIds.add(-1);

                    for (int i = 0; i < array.length(); i++) {
                        JSONObject obj = array.getJSONObject(i);
                        clientNames.add(obj.getString("client_name"));
                        clientIds.add(obj.getInt("id"));
                    }

                    ArrayAdapter<String> adapter =
                            new ArrayAdapter<>(this,
                                    android.R.layout.simple_spinner_item,
                                    clientNames);

                    adapter.setDropDownViewResource(
                            android.R.layout.simple_spinner_dropdown_item);

                    spinnerClient.setAdapter(adapter);

                } catch (Exception e) {
                    Toast.makeText(this, "Client parsing error", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            },
            error -> Toast.makeText(this, "Failed to load clients", Toast.LENGTH_SHORT).show()
        );

        Volley.newRequestQueue(this).add(request);
    }

    private void loadProjects() {
        String url = "http://10.0.2.2:5000/projects?contractor_id=1";

        StringRequest request = new StringRequest(
            Request.Method.GET,
            url,
            response -> {
                try {
                    JSONObject json = new JSONObject(response);
                    JSONArray array = json.getJSONArray("projects");

                    projectNames.clear();
                    projectIds.clear();

                    projectNames.add("Select Project");
                    projectIds.add(-1);

                    for (int i = 0; i < array.length(); i++) {
                        JSONObject obj = array.getJSONObject(i);
                        projectNames.add(obj.getString("project_name"));
                        projectIds.add(obj.getInt("id"));
                    }

                    ArrayAdapter<String> adapter =
                            new ArrayAdapter<>(this,
                                    android.R.layout.simple_spinner_item,
                                    projectNames);

                    adapter.setDropDownViewResource(
                            android.R.layout.simple_spinner_dropdown_item);

                    spinnerProject.setAdapter(adapter);

                } catch (Exception e) {
                    Toast.makeText(this, "Project parsing error", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            },
            error -> Toast.makeText(this, "Failed to load projects", Toast.LENGTH_SHORT).show()
        );

        Volley.newRequestQueue(this).add(request);
    }

    private void submitQuotation() {
        int clientPos = spinnerClient.getSelectedItemPosition();
        int projectPos = spinnerProject.getSelectedItemPosition();

        if (clientPos <= 0 || projectPos <= 0) {
            Toast.makeText(this, "Please select client and project", Toast.LENGTH_SHORT).show();
            return;
        }

        int selectedClientId = clientIds.get(clientPos);
        int selectedProjectId = projectIds.get(projectPos);

        String url = "http://10.0.2.2:5000/add-quotation";

        StringRequest request = new StringRequest(
            Request.Method.POST,
            url,
            response -> {
                Log.d("ADD_QUOTATION", response);

                if (response.contains("success")) {
                    Toast.makeText(this, "Quotation created", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(this, "Failed: " + response, Toast.LENGTH_LONG).show();
                }
            },
            error -> {
                Log.e("ADD_QUOTATION_ERROR", error.toString());
                Toast.makeText(this, "Network error", Toast.LENGTH_LONG).show();
            }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("contractor_id", "1");
                params.put("client_id", String.valueOf(selectedClientId));
                params.put("project_id", String.valueOf(selectedProjectId));
                params.put("title", etQuotationTitle.getText().toString().trim());
                params.put("description", etDescription.getText().toString().trim());
                params.put("amount", etTotalAmount.getText().toString().trim());
                return params;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }
}
