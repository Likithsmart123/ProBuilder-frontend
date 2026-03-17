package com.example.probuilder;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 2500; // 2.5 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        TextView sloganTextView = findViewById(R.id.sloganTextView);
        String sloganText = "Build. Track. Deliver.";
        SpannableString spannableSlogan = new SpannableString(sloganText);

        // Set the deep blue color for the entire string
        spannableSlogan.setSpan(new ForegroundColorSpan(Color.parseColor("#0B3C5D")),
                0, sloganText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Find the dots and color them yellow to match the helmet
        int firstDot = sloganText.indexOf('.');
        int secondDot = sloganText.indexOf('.', firstDot + 1);

        if (firstDot != -1) {
            spannableSlogan.setSpan(new ForegroundColorSpan(Color.parseColor("#FFC107")),
                    firstDot, firstDot + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        if (secondDot != -1) {
            spannableSlogan.setSpan(new ForegroundColorSpan(Color.parseColor("#FFC107")),
                    secondDot, secondDot + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        sloganTextView.setText(spannableSlogan);

        // Handler to start the LoginActivity and close this SplashActivity after some seconds.
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, SubscriptionActivity.class);
            startActivity(intent);
            finish();
        }, SPLASH_DELAY);
    }
}
