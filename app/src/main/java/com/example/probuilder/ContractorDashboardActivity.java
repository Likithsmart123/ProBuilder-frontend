package com.example.probuilder;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;

public class ContractorDashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contractor_dashboard);

        // Welcome Message
        TextView tvWelcomeMessage = findViewById(R.id.tvWelcomeMessage);
        String name = getIntent().getStringExtra("USER_NAME");
        if (name != null && !name.isEmpty()) {
            tvWelcomeMessage.setText("Welcome back, " + name + "!");
        }

        // Header Buttons
        ImageView ivProfile = findViewById(R.id.ivProfile);
        ivProfile.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));

        // Quick Actions
        MaterialCardView cardPaymentManagement = findViewById(R.id.cardPaymentManagement);
        MaterialCardView cardCreateProject = findViewById(R.id.cardCreateProject);
        MaterialCardView cardMaterialManagement = findViewById(R.id.cardMaterialManagement);
        MaterialCardView cardCreateQuotation = findViewById(R.id.cardCreateQuotation);
        MaterialCardView cardPriceHike = findViewById(R.id.cardPriceHike);
        MaterialCardView cardSupplierTracker = findViewById(R.id.cardSupplierTracker); // Assuming this ID exists or was meant to be used

        // Metric Card Clicks
        MaterialCardView cardTotalClients = findViewById(R.id.cardTotalClients);
        MaterialCardView cardActiveProjects = findViewById(R.id.cardActiveProjects);
        MaterialCardView cardAllQuotations = findViewById(R.id.cardAllQuotations);
        MaterialCardView cardTotalExpenses = findViewById(R.id.cardTotalExpenses);

        // Low Stock
        TextView tvViewAllStock = findViewById(R.id.tvViewAllStock);

        // Set OnClick Listeners
        cardTotalClients.setOnClickListener(v -> startActivity(new Intent(this, ClientsActivity.class)));
        cardActiveProjects.setOnClickListener(v -> startActivity(new Intent(this, ProjectsActivity.class)));
        cardAllQuotations.setOnClickListener(v -> startActivity(new Intent(this, AllQuotationsActivity.class)));
        tvViewAllStock.setOnClickListener(v -> startActivity(new Intent(this, MaterialInventoryActivity.class)));
        cardCreateProject.setOnClickListener(v -> startActivity(new Intent(this, CreateProjectActivity.class)));
        cardCreateQuotation.setOnClickListener(v -> startActivity(new Intent(this, CreateQuotationActivity.class)));
        cardMaterialManagement.setOnClickListener(v -> startActivity(new Intent(this, MaterialInventoryActivity.class)));
        cardPriceHike.setOnClickListener(v -> startActivity(new Intent(this, PricePredictionActivity.class)));
        cardPaymentManagement.setOnClickListener(v -> startActivity(new Intent(this, PaymentManagementActivity.class)));
        cardSupplierTracker.setOnClickListener(v -> startActivity(new Intent(this, SupplierTrackerActivity.class)));
        cardTotalExpenses.setOnClickListener(v -> startActivity(new Intent(this, ExpenseTrackingActivity.class)));
    }
}
