package com.example.probuilder;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class QuotationDetailActivity extends AppCompatActivity {

    private Quotation quotation;

    private TextView tvTitle, tvClient, tvClientContact, tvProject, tvProjectLocation, tvAmount, tvDate, tvDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quotation_detail);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        quotation = (Quotation) getIntent().getSerializableExtra("quotation");

        tvTitle = findViewById(R.id.tvDetailTitle);
        tvClient = findViewById(R.id.tvDetailClient);
        tvClientContact = findViewById(R.id.tvDetailClientContact);
        tvProject = findViewById(R.id.tvDetailProject);
        tvProjectLocation = findViewById(R.id.tvDetailProjectLocation);
        tvAmount = findViewById(R.id.tvDetailAmount);
        tvDate = findViewById(R.id.tvDetailDate);
        tvDescription = findViewById(R.id.tvDetailDescription);
        Button btnDownload = findViewById(R.id.btnDownloadPdf);

        if (quotation != null) {
            tvTitle.setText(quotation.getTitle());
            tvClient.setText(quotation.getClientName());
            
            // Initial set from intent data
            String email = quotation.getClientEmail();
            String phone = quotation.getClientPhone();
            
            // Clean formatting
            String contactInfo = "";
            if (email != null && !email.isEmpty() && !email.equals("N/A")) contactInfo += email;
            if (contactInfo.length() > 0 && phone != null && !phone.isEmpty() && !phone.equals("N/A")) contactInfo += " | ";
            if (phone != null && !phone.isEmpty() && !phone.equals("N/A")) contactInfo += phone;
            
            tvClientContact.setText(contactInfo);
            
            tvProject.setText(quotation.getProjectName());
            tvProjectLocation.setText("Location: " + quotation.getProjectLocation());
            
            tvAmount.setText("₹ " + quotation.getAmount());
            tvDate.setText(quotation.getCreatedAt());
            tvDescription.setText(quotation.getDescription());

            // Add the requested sanity check log
            Log.d("QUOTATION_DETAIL", "Client name = [" + quotation.getClientName() + "]");

            // Fetch fresh details from API
            loadQuotationDetails(quotation.getId());
        }

        btnDownload.setOnClickListener(v -> checkPermissionAndGeneratePdf());
    }

    private void loadQuotationDetails(String quotationId) {
        String url = Constants.BASE_URL + "get_quotations.php?id=" + quotationId;
        Log.e("QUOTATION_DETAIL_DEBUG", "Calling URL = " + url);

        com.android.volley.toolbox.StringRequest request = new com.android.volley.toolbox.StringRequest(
                com.android.volley.Request.Method.GET,
                url,
                response -> {
                    Log.e("QUOTATION_DETAIL_DEBUG", "RAW RESPONSE = " + response);
                    try {
                        org.json.JSONObject obj = new org.json.JSONObject(response);
                        
                        // Map fields using class members
                        if (tvTitle != null) tvTitle.setText(obj.optString("title", "N/A"));
                        if (tvClient != null) tvClient.setText(obj.optString("client_name", "N/A"));
                        
                        String phone = obj.optString("client_phone", "");
                        String email = obj.optString("client_email", "");
                        
                        // Clean formatting for API response too
                        String contactInfo = "";
                        if (email != null && !email.isEmpty() && !email.equals("N/A")) contactInfo += email;
                        if (contactInfo.length() > 0 && phone != null && !phone.isEmpty() && !phone.equals("N/A")) contactInfo += " | ";
                        if (phone != null && !phone.isEmpty() && !phone.equals("N/A")) contactInfo += phone;

                        if (tvClientContact != null) tvClientContact.setText(contactInfo);
                        
                        if (tvProject != null) tvProject.setText(obj.optString("project_name", "N/A"));
                        if (tvProjectLocation != null) tvProjectLocation.setText("Location: " + obj.optString("project_location", "N/A"));
                        if (tvAmount != null) tvAmount.setText("₹ " + obj.optString("amount", "0"));
                        if (tvDate != null) tvDate.setText(obj.optString("created_at", "N/A"));
                        if (tvDescription != null) tvDescription.setText(obj.optString("description", ""));

                    } catch (Exception e) {
                         Log.e("QUOTATION_DETAIL_DEBUG", "Parse error", e);
                    }
                },
                error -> Log.e("QUOTATION_DETAIL_DEBUG", "Volley error", error)
        );

        com.android.volley.toolbox.Volley.newRequestQueue(this).add(request);
    }

    private void checkPermissionAndGeneratePdf() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            // For older Android versions, we need explicit write permission
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return;
            }
        }
        generatePdf();
    }

    private void generatePdf() {
        PdfDocument pdfDocument = new PdfDocument();
        Paint paint = new Paint();
        Paint titlePaint = new Paint();

        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create(); // A4 size
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        titlePaint.setTextSize(24);
        paint.setTextSize(14);
        paint.setColor(getResources().getColor(android.R.color.black));

        int y = 50;

        // HEADER
        canvas.drawText("QUOTATION", 220, y, titlePaint);
        y += 40;
        
        paint.setTextSize(12);
        canvas.drawText("Date: " + quotation.getCreatedAt(), 400, y, paint);
        y += 30;

        // CLIENT DETAILS
        paint.setFakeBoldText(true);
        canvas.drawText("Client:", 50, y, paint);
        paint.setFakeBoldText(false);
        canvas.drawText(quotation.getClientName(), 120, y, paint);
        y += 20;

        paint.setFakeBoldText(true);
        canvas.drawText("Project:", 50, y, paint);
        paint.setFakeBoldText(false);
        canvas.drawText(quotation.getProjectName(), 120, y, paint);
        y += 40;

        // TITLE
        paint.setTextSize(16);
        paint.setFakeBoldText(true);
        canvas.drawText(quotation.getTitle(), 50, y, paint);
        y += 30;

        // DRAW LINE
        paint.setStrokeWidth(1);
        canvas.drawLine(50, y, 545, y, paint);
        y += 20;

        // DESCRIPTION
        paint.setTextSize(14);
        paint.setFakeBoldText(false);
        String description = quotation.getDescription();
        
        // Simple text wrapping for description
        int x = 50;
        String[] words = description.split(" ");
        StringBuilder line = new StringBuilder();
        for (String word : words) {
            if (paint.measureText(line + word) < 480) {
                line.append(word).append(" ");
            } else {
                canvas.drawText(line.toString(), x, y, paint);
                y += 20;
                line = new StringBuilder(word + " ");
            }
        }
        canvas.drawText(line.toString(), x, y, paint);
        y += 40;

        // TOTAL LINE
        canvas.drawLine(50, y, 545, y, paint);
        y += 30;
        
        paint.setTextSize(18);
        paint.setFakeBoldText(true);
        canvas.drawText("Total Amount:", 250, y, paint);
        canvas.drawText("₹ " + quotation.getAmount(), 400, y, paint);

        pdfDocument.finishPage(page);

        // SAVE FILE
        String fileName = "Quotation_" + quotation.getId() + "_" + System.currentTimeMillis() + ".pdf";
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName);

        try {
            pdfDocument.writeTo(new FileOutputStream(file));
            Toast.makeText(this, "PDF Downloaded: " + fileName, Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error saving PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        pdfDocument.close();
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            generatePdf();
        } else {
            Toast.makeText(this, "Permission denied to save PDF", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }
}
