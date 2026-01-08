package com.example.probuilder;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;

public class RoleSelectionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_role_selection);

        MaterialCardView cardContractor = findViewById(R.id.cardContractor);
        MaterialCardView cardClient = findViewById(R.id.cardClient);

        cardContractor.setOnClickListener(v -> {
            // Navigate to Contractor Login
            startActivity(new Intent(RoleSelectionActivity.this, ContractorLoginActivity.class));
        });

        cardClient.setOnClickListener(v -> {
            // Navigate to Client Login
            // Ensure ClientLoginActivity exists or handle gracefully
            try {
                startActivity(new Intent(RoleSelectionActivity.this, ClientLoginActivity.class));
            } catch (Exception e) {
                Toast.makeText(this, "Client Portal coming soon!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
