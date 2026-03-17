package com.example.probuilder;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.button.MaterialButton;

public class ClientDetailsActivity extends AppCompatActivity {

    private int clientId;
    private String name, email, phone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_details);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // Get Data
        clientId = getIntent().getIntExtra("CLIENT_ID", -1);
        name = getIntent().getStringExtra("NAME");
        email = getIntent().getStringExtra("EMAIL");
        phone = getIntent().getStringExtra("PHONE");

        // Init Views
        TextView tvInitial = findViewById(R.id.tvProfileInitial);
        TextView tvName = findViewById(R.id.tvClientName);
        TextView tvEmail = findViewById(R.id.tvClientEmail);
        TextView tvPhone = findViewById(R.id.tvClientPhone);
        MaterialButton btnProjects = findViewById(R.id.btnViewProjects);

        // Set Data
        tvName.setText(name != null ? name : "Unknown");
        tvEmail.setText(email != null && !email.isEmpty() ? email : "No email");
        tvPhone.setText(phone != null && !phone.isEmpty() ? phone : "No phone");

        if (name != null && !name.isEmpty()) {
            tvInitial.setText(String.valueOf(name.charAt(0)).toUpperCase());
        }

        // Action
        btnProjects.setOnClickListener(v -> {
            Intent intent = new Intent(ClientDetailsActivity.this, ProjectsActivity.class);
            intent.putExtra("CLIENT_ID", clientId);
            startActivity(intent);
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
