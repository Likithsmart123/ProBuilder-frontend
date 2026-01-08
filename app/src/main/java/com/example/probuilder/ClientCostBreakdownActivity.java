package com.example.probuilder;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ClientCostBreakdownActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_cost_breakdown);

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // Mock Data
        double totalBudget = 5000000.00;
        double spentAmount = 1500000.00;
        double remainingAmount = totalBudget - spentAmount;

        TextView tvTotalBudget = findViewById(R.id.tvTotalBudget);
        TextView tvSpentAmount = findViewById(R.id.tvSpentAmount);
        TextView tvRemainingAmount = findViewById(R.id.tvRemainingAmount);
        ProgressBar pbBudget = findViewById(R.id.pbBudget);
        TextView tvUtilizationPercentage = findViewById(R.id.tvUtilizationPercentage);

        tvTotalBudget.setText(String.format(Locale.getDefault(), "₹ %,.0f", totalBudget));
        tvSpentAmount.setText(String.format(Locale.getDefault(), "₹ %,.0f", spentAmount));
        tvRemainingAmount.setText(String.format(Locale.getDefault(), "₹ %,.0f", remainingAmount));

        int progress = (int) ((spentAmount / totalBudget) * 100);
        pbBudget.setProgress(progress);
        tvUtilizationPercentage.setText(progress + "% Used");

        // Itemized Breakdown
        RecyclerView rvCostItems = findViewById(R.id.rvCostItems);
        rvCostItems.setLayoutManager(new LinearLayoutManager(this));

        List<CostItem> items = new ArrayList<>();
        // Mock Item Data
        items.add(new CostItem("Cement", 450000, spentAmount));
        items.add(new CostItem("Steel", 520000, spentAmount));
        items.add(new CostItem("Sand", 200000, spentAmount));
        items.add(new CostItem("Bricks", 150000, spentAmount));
        items.add(new CostItem("Labor", 180000, spentAmount));
        
        CostBreakdownAdapter adapter = new CostBreakdownAdapter(items);
        rvCostItems.setAdapter(adapter);
    }
}
