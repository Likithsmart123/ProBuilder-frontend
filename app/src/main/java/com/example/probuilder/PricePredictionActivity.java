package com.example.probuilder;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class PricePredictionActivity extends AppCompatActivity {

    private PricePredictionAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_price_prediction);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        RecyclerView rvPricePredictions = findViewById(R.id.rvPricePredictions);
        rvPricePredictions.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PricePredictionAdapter(this);
        rvPricePredictions.setAdapter(adapter);

        loadStaticData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.price_prediction_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_refresh) {
            Toast.makeText(this, "Refreshing data...", Toast.LENGTH_SHORT).show();
            // TODO: Add backend call to refresh data here
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadStaticData() {
        List<PricePrediction> predictions = new ArrayList<>();
        predictions.add(new PricePrediction("Cement", "per bag", "Likely to Increase", 180.00, 200.00, -10.0, "Cement prices show a downward trend. You may benefit from waiting for further sales."));
        predictions.add(new PricePrediction("Steel Rods", "per kg", "Likely to Decrease", 95.00, 91.00, 4.4, "Steel prices are increasing. Consider purchasing for upcoming projects soon."));
        predictions.add(new PricePrediction("Sand", "per ton", "Stable", 1500.00, 1500.00, 0.0, "Sand prices are stable. Purchase as per your project's deadline."));
        predictions.add(new PricePrediction("Bricks", "per 1000 pcs", "Likely to Increase", 6800.00, 6500.00, 4.6, "Brick prices are rising gradually. Consider advance buying."));
        predictions.add(new PricePrediction("Paint", "per liter", "Stable", 280.00, 285.00, -1.8, "Paint prices are relatively stable with minor fluctuations. Purchase as needed."));
        predictions.add(new PricePrediction("Tiles", "per sq ft", "Likely to Increase", 45.00, 42.00, 7.1, "Tile prices are increasing due to high demand. Early procurement is advised."));

        adapter.setPredictions(predictions);
    }
}
