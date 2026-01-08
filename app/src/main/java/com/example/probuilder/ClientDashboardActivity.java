package com.example.probuilder;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;

public class ClientDashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_dashboard);

        // Welcome Message
        TextView tvWelcomeMessage = findViewById(R.id.tvWelcomeMessage);
        String name = getIntent().getStringExtra("USER_NAME");
        if (name != null && !name.isEmpty()) {
            tvWelcomeMessage.setText("Welcome, " + name);
        }

        // Profile Icon
        ImageView ivProfile = findViewById(R.id.ivProfile);
        ivProfile.setOnClickListener(v -> startActivity(new Intent(this, ClientProfileActivity.class)));

        // Navigation Cards
        MaterialCardView cardProjects = findViewById(R.id.cardProjects);
        MaterialCardView cardQuotations = findViewById(R.id.cardQuotations);
        MaterialCardView cardCostBreakdown = findViewById(R.id.cardCostBreakdown);

        cardProjects.setOnClickListener(v -> startActivity(new Intent(this, ClientProjectListActivity.class)));
        cardQuotations.setOnClickListener(v -> startActivity(new Intent(this, ClientQuotationListActivity.class)));
        cardCostBreakdown.setOnClickListener(v -> startActivity(new Intent(this, ClientCostBreakdownActivity.class)));
        
        // Mock Stats (In a real app, fetch from network)
        TextView tvActiveProjectsCount = findViewById(R.id.tvActiveProjectsCount);
        TextView tvTotalQuotationsCount = findViewById(R.id.tvTotalQuotationsCount);
        
        tvActiveProjectsCount.setText("2"); // Mock
        tvTotalQuotationsCount.setText("2"); // Mock
    }
}
