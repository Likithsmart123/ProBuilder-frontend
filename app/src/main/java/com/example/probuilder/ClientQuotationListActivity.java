package com.example.probuilder;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class ClientQuotationListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_quotation_list);

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        RecyclerView rvQuotations = findViewById(R.id.rvQuotations);
        rvQuotations.setLayoutManager(new LinearLayoutManager(this));

        // Mock Data
        List<Quotation> quotations = new ArrayList<>();
        // Assuming Quotation constructor matches (id, clientId, projectId, clientName, projectName, date, totalAmount, status)
        // Quotation(int id, String title, String clientName, String projectName, String amount, String status, String createdAt)
        quotations.add(new Quotation(101, "Foundation Estimation", "Client A", "Villa Construction", "450,000.00", "Sent", "2026-01-05"));
        quotations.add(new Quotation(102, "Interior Design", "Client A", "Office Renovation", "120,000.00", "Sent", "2026-01-02"));

        ClientQuotationAdapter adapter = new ClientQuotationAdapter(quotations, quotation -> {
             Intent intent = new Intent(this, ClientQuotationDetailActivity.class);
             intent.putExtra("QUOTE_TITLE", quotation.getTitle());
             intent.putExtra("QUOTE_PROJECT", quotation.getProjectName());
             intent.putExtra("QUOTE_AMOUNT", quotation.getAmount());
             intent.putExtra("QUOTE_DATE", quotation.getCreatedAt());
             intent.putExtra("QUOTE_ID", quotation.getId());
             startActivity(intent);
        });
        rvQuotations.setAdapter(adapter);
    }
}
