package com.example.probuilder;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class PaymentManagementActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_management);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        RecyclerView rvPayments = findViewById(R.id.rvPayments);
        rvPayments.setLayoutManager(new LinearLayoutManager(this));

        // Create 3 mock payment records as requested
        List<Payment> payments = new ArrayList<>();
        payments.add(new Payment("Rakesh Kumar", "Villa Construction - Whitefield", 50000.00, "Jan 05, 2026", "Received", "Bank Transfer"));
        payments.add(new Payment("TechGuru Solutions", "Office Renovation - Indiranagar", 25000.00, "Jan 02, 2026", "Pending", "Cheque"));
        payments.add(new Payment("Priya Sharma", "Apartment Interiors - HSR Layout", 10000.00, "Dec 28, 2025", "Received", "UPI"));

        PaymentAdapter adapter = new PaymentAdapter(payments);
        rvPayments.setAdapter(adapter);
    }
}
