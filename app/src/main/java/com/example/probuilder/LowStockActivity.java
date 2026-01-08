package com.example.probuilder;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class LowStockActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_low_stock);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        RecyclerView rvStock = findViewById(R.id.rvStockAlerts);
        rvStock.setLayoutManager(new LinearLayoutManager(this));

        List<StockAlert> alertList = new ArrayList<>();
        alertList.add(new StockAlert("Cement Bags", 15, 50, "Critical", R.drawable.img_cement));
        alertList.add(new StockAlert("Steel Rods (10mm)", 35, 40, "Low", R.drawable.img_steel));
        alertList.add(new StockAlert("Bricks (Red)", 500, 2000, "Critical", R.drawable.img_bricks));
        alertList.add(new StockAlert("Sand (River)", 2, 10, "Critical", R.drawable.img_sand));
        alertList.add(new StockAlert("Paint (White)", 5, 20, "Low", R.drawable.img_paint));
        alertList.add(new StockAlert("Tiles (Floor)", 20, 100, "Critical", R.drawable.ic_supplier_management)); // Fallback icon for now
        alertList.add(new StockAlert("Plumbing Pipes", 12, 30, "Low", R.drawable.ic_supplier_management));

        StockAlertAdapter adapter = new StockAlertAdapter(alertList);
        rvStock.setAdapter(adapter);
    }

    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }
}