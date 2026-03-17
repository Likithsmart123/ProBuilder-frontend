package com.example.probuilder;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_screen);

        CardView cardContractor = findViewById(R.id.cardContractor);
        CardView cardClient = findViewById(R.id.cardClient);

        cardContractor.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, ContractorLoginActivity.class)));

        cardClient.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, ClientLoginActivity.class)));
    }
}