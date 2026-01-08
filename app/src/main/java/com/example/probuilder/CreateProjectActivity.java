package com.example.probuilder;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class CreateProjectActivity extends AppCompatActivity {

    private TextInputEditText etProjectName, etProjectLocation, etClientName, etClientPhone, etStartDate, etEndDate;
    private Button btnAddProject;
    private static final String ADD_PROJECT_URL = "http://10.0.2.2:5000/add-project";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_project);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        etProjectName = findViewById(R.id.etProjectName);
        etProjectLocation = findViewById(R.id.etProjectLocation);
        etClientName = findViewById(R.id.etClientName);
        etClientPhone = findViewById(R.id.etClientPhone);
        etStartDate = findViewById(R.id.etStartDate);
        etEndDate = findViewById(R.id.etEndDate);
        btnAddProject = findViewById(R.id.btnAddProject);

        etStartDate.setOnClickListener(v -> showDatePickerDialog(etStartDate));
        etEndDate.setOnClickListener(v -> showDatePickerDialog(etEndDate));

        btnAddProject.setOnClickListener(v -> addProject());
    }

    private void showDatePickerDialog(TextInputEditText dateField) {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year1, monthOfYear, dayOfMonth) -> {
                    String selectedDate = year1 + "-" + (monthOfYear + 1) + "-" + dayOfMonth;
                    dateField.setText(selectedDate);
                }, year, month, day);
        datePickerDialog.show();
    }

    private void addProject() {
        final String name = etProjectName.getText() != null ? etProjectName.getText().toString().trim() : "";
        final String location = etProjectLocation.getText() != null ? etProjectLocation.getText().toString().trim() : "";
        final String clientName = etClientName.getText() != null ? etClientName.getText().toString().trim() : "";
        final String clientPhone = etClientPhone.getText() != null ? etClientPhone.getText().toString().trim() : "";
        final String startDate = etStartDate.getText() != null ? etStartDate.getText().toString().trim() : "";
        final String endDate = etEndDate.getText() != null ? etEndDate.getText().toString().trim() : "";

        if (name.isEmpty() || location.isEmpty() || clientName.isEmpty() || clientPhone.isEmpty() || startDate.isEmpty() || endDate.isEmpty()) {
            Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        btnAddProject.setEnabled(false);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, ADD_PROJECT_URL,
                response -> {
                    if (response.trim().equalsIgnoreCase("success")) {
                        Toast.makeText(CreateProjectActivity.this, "Project Added Successfully", Toast.LENGTH_SHORT).show();
                        finish(); // CORRECT: Just finish the activity
                    } else {
                        Toast.makeText(CreateProjectActivity.this, "Error: " + response, Toast.LENGTH_LONG).show();
                        btnAddProject.setEnabled(true);
                    }
                },
                error -> {
                    Toast.makeText(CreateProjectActivity.this, "Network Error: " + error.toString(), Toast.LENGTH_LONG).show();
                    Log.e("CreateProject", "Volley Error: " + error.toString());
                    btnAddProject.setEnabled(true);
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("project_name", name);
                params.put("location", location);
                params.put("client_name", clientName);
                params.put("client_phone", clientPhone);
                params.put("start_date", startDate);
                params.put("end_date", endDate);
                params.put("contractor_id", "1"); // Placeholder for logged-in contractor
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }
}