package com.example.probuilder;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import java.util.Collections;

public class MaterialDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_material_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        MaterialResponse data = (MaterialResponse) getIntent().getSerializableExtra("data");
        if (data == null) {
            finish();
            return;
        }

        bindData(data);
    }

    private void bindData(MaterialResponse data) {
        TextView tvMaterial = findViewById(R.id.tvDetailMaterial);
        TextView tvCurrent = findViewById(R.id.tvDetailCurrentPrice);
        TextView tvPredicted = findViewById(R.id.tvDetailPredicted);
        TextView tvConfidence = findViewById(R.id.tvDetailConfidence);
        android.widget.ImageView tvTrendIcon = findViewById(R.id.tvDetailTrendIcon);
        TextView tvTrendText = findViewById(R.id.tvDetailTrendText);
        TrendChartView chartView = findViewById(R.id.trendChart);

        tvMaterial.setText(data.material.toUpperCase());
        tvCurrent.setText("₹ " + data.current_price);
        tvPredicted.setText("₹ " + data.predicted_price);
        tvConfidence.setText(data.confidence + "%");

        int color = Color.GRAY;
        String trendText = "Stable trend";
        int iconRes = R.drawable.ic_remove;

        switch (data.trend.toLowerCase()) {
            case "increase":
                color = Color.GREEN;
                trendText = "Increasing trend";
                iconRes = R.drawable.ic_trending_up;
                break;
            case "decrease":
                color = Color.RED;
                trendText = "Decreasing trend";
                iconRes = R.drawable.ic_trending_down;
                break;
        }

        tvTrendIcon.setImageResource(iconRes);
        tvTrendIcon.setColorFilter(color);
        tvTrendText.setText(trendText);
        
        // Populate chart
        chartView.generateTrendData(data.trend, data.current_price, color);
    }
}
