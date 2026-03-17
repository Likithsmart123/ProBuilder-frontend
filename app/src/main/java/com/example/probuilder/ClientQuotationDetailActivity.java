package com.example.probuilder;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;
import com.google.android.material.button.MaterialButton;
import androidx.appcompat.app.AppCompatActivity;

public class ClientQuotationDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_quotation_detail);

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());
        
        // Retrieve Data
        String title = getIntent().getStringExtra("QUOTE_TITLE");
        String project = getIntent().getStringExtra("QUOTE_PROJECT");
        String amount = getIntent().getStringExtra("QUOTE_AMOUNT");
        String date = getIntent().getStringExtra("QUOTE_DATE");
        String id = getIntent().getStringExtra("QUOTE_ID");
        String desc = getIntent().getStringExtra("QUOTE_DESC");

        // Populate Views
        ((android.widget.TextView) findViewById(R.id.tvQuotationTitle)).setText(title);
        ((android.widget.TextView) findViewById(R.id.tvProjectName)).setText(project);
        ((android.widget.TextView) findViewById(R.id.tvQuotationDate)).setText("Issued: " + date);
        ((android.widget.TextView) findViewById(R.id.tvTotalAmount)).setText("₹ " + amount);
        
        ((android.widget.TextView) findViewById(R.id.tvQuotationDescription)).setText(
            desc != null && !desc.isEmpty() ? desc : "No description provided."
        );

        MaterialButton btnDownload = findViewById(R.id.btnDownload);
        btnDownload.setOnClickListener(v -> 
            Toast.makeText(this, "Downloading PDF for Quotation #" + id + "...", Toast.LENGTH_SHORT).show()
        );
    }
}
