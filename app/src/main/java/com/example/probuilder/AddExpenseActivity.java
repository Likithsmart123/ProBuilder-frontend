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

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddExpenseActivity extends AppCompatActivity {

    private LinearLayout btnCatMaterials, btnCatLabor, btnCatEquipment, btnCatTransport, btnCatUtilities, btnCatOther;
    private TextInputEditText etDate, etAmount, etDescription, etInvoice;
    private AutoCompleteTextView actvProject;
    private MaterialButton btnSave, btnCancel;

    private String selectedCategory = "Materials"; // Default
    private Integer selectedProjectId = null;
    private Map<String, Integer> projectNameToIdMap = new HashMap<>();

    private static final String ADD_EXPENSE_URL = "http://10.0.2.2:5000/add-expense";

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

        etDate = findViewById(R.id.etDate); // Corrected ID
        etAmount = findViewById(R.id.etAmount);
        etDescription = findViewById(R.id.etDescription);
        etInvoice = findViewById(R.id.etInvoice);
        actvProject = findViewById(R.id.actvProject); // Corrected View Type

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
            selectedProjectId = projectNameToIdMap.get(selectedName);
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
            etDate.setText(year + "-" + (month + 1) + "-" + dayOfMonth); // Set text on correct view
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void loadProjects() {
        String url = "http://10.0.2.2:5000/projects?contractor_id=1";
        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        JSONArray array = json.getJSONArray("projects");
                        List<String> projectNames = new ArrayList<>();
                        projectNameToIdMap.clear();

                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);
                            String name = obj.getString("project_name");
                            int id = obj.getInt("id");
                            projectNames.add(name);
                            projectNameToIdMap.put(name, id);
                        }

                        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, projectNames);
                        actvProject.setAdapter(adapter);

                    } catch (Exception e) {
                        Log.e("AddExpenseActivity", "Project JSON Parsing Error: " + e.getMessage());
                    }
                },
                error -> Log.e("AddExpenseActivity", "Project Volley Error: " + error.toString()));

        Volley.newRequestQueue(this).add(request);
    }

    private void saveExpense() {
        String amountStr = etAmount.getText().toString();
        if (amountStr.isEmpty()) {
            Toast.makeText(this, "Please enter an amount", Toast.LENGTH_SHORT).show();
            return;
        }

        String date = etDate.getText().toString();
        if (date.isEmpty() || date.equals("Select Date")) {
            Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedProjectId == null) {
            Toast.makeText(this, "Please select a project", Toast.LENGTH_SHORT).show();
            return;
        }

        final String description = etDescription.getText().toString().trim();
        final String invoice = etInvoice.getText().toString().trim();

        btnSave.setEnabled(false);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, ADD_EXPENSE_URL,
                response -> {
                    if (response.trim().equalsIgnoreCase("success")) {
                        Toast.makeText(this, "Expense Saved Successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Error: " + response, Toast.LENGTH_LONG).show();
                        btnSave.setEnabled(true);
                    }
                },
                error -> {
                    Toast.makeText(this, "Network Error: " + error.toString(), Toast.LENGTH_LONG).show();
                    btnSave.setEnabled(true);
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("contractor_id", "1"); // Placeholder
                params.put("project_id", String.valueOf(selectedProjectId));
                params.put("category", selectedCategory);
                params.put("amount", amountStr);
                params.put("expense_date", date);
                params.put("description", description);
                params.put("invoice_number", invoice);
                return params;
            }
        };

        Volley.newRequestQueue(this).add(stringRequest);
    }
}
