package com.example.probuilder;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonArrayRequest;
import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ClientProjectDetailActivity extends AppCompatActivity {

    private TextView tvProjectName, tvLocation, tvBudget, tvStatus, tvProgress, tvNoDailyProgress, tvStartDate;
    private ProgressBar progressBar;
    private RecyclerView rvDailyProgress;
    
    private DailyProgressAdapter progressAdapter;
    private List<DailyProgress> progressList = new ArrayList<>();
    
    // Kept for potential future use if we re-add tabs, but for now focusing on "Perfect Status + Scrolling"
    // private RecyclerView rvQuotations; 
    // private ClientQuotationAdapter quotationAdapter;
    // private List<Quotation> quotationList = new ArrayList<>();

    private int projectId;
    private int contractorId = -1;
    private String contractorName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_project_detail);

        projectId = getIntent().getIntExtra("project_id", -1);
        
        if (projectId <= 0) {
            Toast.makeText(this, "Invalid project", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupToolbar();
        initializeViews();
        loadAllData();
    }

    private void setupToolbar() {
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false); // Title handled by CollapsingToolbar
        }
        toolbar.setNavigationOnClickListener(v -> finish());
        
        ImageView ivChatProjectClient = findViewById(R.id.ivChatProjectClient);
        ivChatProjectClient.setOnClickListener(v -> {
            if (contractorId != -1) {
                android.content.Intent chatIntent = new android.content.Intent(ClientProjectDetailActivity.this, ChatActivity.class);
                chatIntent.putExtra("PROJECT_ID", projectId);
                chatIntent.putExtra("OTHER_USER_ID", contractorId);
                chatIntent.putExtra("OTHER_USER_NAME", contractorName);
                startActivity(chatIntent);
            } else {
                Toast.makeText(this, "Contractor details not loaded yet", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initializeViews() {
        tvProjectName = findViewById(R.id.tvProjectName);
        tvLocation = findViewById(R.id.tvLocation);
        tvBudget = findViewById(R.id.tvBudget);
        tvStatus = findViewById(R.id.tvStatus);
        tvProgress = findViewById(R.id.tvProgress);
        progressBar = findViewById(R.id.progressBar);
        tvNoDailyProgress = findViewById(R.id.tvNoDailyProgress);
        tvStartDate = findViewById(R.id.tvStartDate);

        rvDailyProgress = findViewById(R.id.rvDailyProgress);
        rvDailyProgress.setLayoutManager(new LinearLayoutManager(this));
        
        // Initialize Adapter with Click Listener
        progressAdapter = new DailyProgressAdapter(progressList, this::showWorkLogDetails);
        rvDailyProgress.setAdapter(progressAdapter);
    }

    private void loadAllData() {
        loadProjectDetails();
        loadDailyProgress();
        // loadQuotations(); // Can be re-enabled if designed into the new scroll view
    }

    private Map<String, String> getAuthHeaders() {
        Map<String, String> headers = new java.util.HashMap<>();
        SessionManager sessionManager = new SessionManager(this);
        String token = sessionManager.getApiToken();
        
        android.util.Log.d("AUTH_DEBUG", "Token from SessionManager = " + (token != null ? token : "NULL"));
        
        if (token != null && !token.isEmpty()) {
            headers.put("Authorization", token);
        }
        return headers;
    }

    private void loadProjectDetails() {
        SessionManager sessionManager = new SessionManager(this);
        String token = sessionManager.getApiToken();
        String url = Constants.BASE_URL + "client_project.php?project_id=" + projectId + "&token=" + (token != null ? token : "");
        
        android.util.Log.d("PROJECT_URL", url);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        // 1. Log the RAW response first (for sanity)
                        android.util.Log.d("BS_RESPONSE", response.toString());
                        
                        // 2. Extract the "project" object
                        JSONObject project = response.optJSONObject("project");
                        
                        if (project == null) {
                            android.util.Log.e("PROJECT_DETAIL", "No 'project' object in response!");
                            return;
                        }

                        // 3. Log the "project" object (MANDATORY per instruction)
                        android.util.Log.d("PROJECT_JSON", project.toString());

                        // 4. Parse fields using OPT (Safe)
                        String name = project.optString("project_name", "Unknown Project");
                        String location = project.optString("location", "Unknown Location");
                        int progress = project.optInt("overall_progress", 0);
                        
                        double budgetVal = project.optDouble("budget", 0);
                        String budgetStr = budgetVal <= 0 ? "Pending" : "₹ " + budgetVal;

                        String statusStr = project.optString("status", "Pending");
                        
                        String startDate = project.optString("start_date", "Not started");
                        if (startDate.equalsIgnoreCase("null")) startDate = "Not started";

                        String endDate = project.optString("end_date", "Not decided");
                        if (endDate.equalsIgnoreCase("null")) endDate = "Not decided";

                        // Extract contractor details for chat
                        contractorId = project.optInt("contractor_id", -1);
                        contractorName = project.optString("contractor_name", "Contractor");

                        // 5. Bind to UI
                        tvProjectName.setText(name);
                        
                        com.google.android.material.appbar.CollapsingToolbarLayout ctl = findViewById(R.id.collapsingToolbar);
                        if(ctl != null) ctl.setTitle(name);

                        tvLocation.setText(location);
                        
                        tvBudget.setText(budgetStr);
                        
                        tvStatus.setText("STATUS: " + statusStr.toUpperCase());
                        
                        tvStartDate.setText(startDate);
                        TextView tvEndDate = findViewById(R.id.tvEndDate);
                        tvEndDate.setText(endDate);

                        progressBar.setProgress(progress);
                        tvProgress.setText(progress + "%");
                        
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    android.util.Log.e("PROJECT_DETAIL", "Auth error loading project details: " + error.toString());
                    if (error instanceof com.android.volley.AuthFailureError) {
                         android.util.Log.e("AUTH_DEBUG", "AuthFailureError: 401/403");
                    }
                }
        ) {
            @Override
            public java.util.Map<String, String> getHeaders() {
                return getAuthHeaders();
            }
        };
        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void loadDailyProgress() {
        String url = Constants.BASE_URL + "get_work_logs.php?project_id=" + projectId;

        com.android.volley.toolbox.StringRequest request = new com.android.volley.toolbox.StringRequest(
                Request.Method.GET, 
                url,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);

                        // Safe parsing for status
                        if (!"success".equalsIgnoreCase(json.optString("status"))) return;

                        org.json.JSONArray logs = json.optJSONArray("logs");
                        List<DailyProgress> list = new ArrayList<>();

                        if (logs != null) {
                            for (int i = 0; i < logs.length(); i++) {
                                JSONObject obj = logs.optJSONObject(i);
                                if (obj == null) continue;
                                
                                int id = obj.optInt("id", 0);
                                String d = obj.optString("work_date", "");
                                String desc = obj.optString("summary", "");
                                int p = obj.optInt("progress_update", 0);
                                
                                java.util.List<String> images = new java.util.ArrayList<>();
                                org.json.JSONArray imgArr = obj.optJSONArray("images");
                                if (imgArr != null) {
                                    for (int j = 0; j < imgArr.length(); j++) {
                                        String imgUrl = imgArr.optString(j);
                                        if (!imgUrl.isEmpty() && !imgUrl.startsWith("http")) {
                                            imgUrl = Constants.BASE_URL + imgUrl;
                                        }
                                        images.add(imgUrl);
                                    }
                                }
                                list.add(new DailyProgress(id, d, desc, p, images));
                            }
                        }

                        if (list.isEmpty()) {
                            rvDailyProgress.setVisibility(android.view.View.GONE);
                            tvNoDailyProgress.setVisibility(android.view.View.VISIBLE);
                        } else {
                            rvDailyProgress.setVisibility(android.view.View.VISIBLE);
                            tvNoDailyProgress.setVisibility(android.view.View.GONE);
                            progressList.clear();
                            progressList.addAll(list);
                            progressAdapter.notifyDataSetChanged();
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        android.util.Log.e("WORK_LOGS", "Error parsing work logs: " + e.toString());
                    }
                },
                error -> { 
                     android.util.Log.e("WORK_LOGS", "Failed to load work logs: " + error.toString());
                }
        ) {
            @Override
            public java.util.Map<String, String> getHeaders() {
                return getAuthHeaders();
            }
        };
        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void showWorkLogDetails(DailyProgress log) {
        BottomSheetDialog dialog = new BottomSheetDialog(this, R.style.BottomSheetDialogTheme);
        android.view.View sheetView = getLayoutInflater().inflate(R.layout.dialog_work_log_detail, null);
        dialog.setContentView(sheetView);

        TextView tvDate = sheetView.findViewById(R.id.tvDetailDate);
        TextView tvProgress = sheetView.findViewById(R.id.tvDetailProgress);
        TextView tvSummary = sheetView.findViewById(R.id.tvDetailSummary);
        androidx.viewpager2.widget.ViewPager2 vpImages = sheetView.findViewById(R.id.vpImages);
        TextView tvNoImages = sheetView.findViewById(R.id.tvNoImages);

        tvDate.setText(log.getWorkDate());
        tvProgress.setText("+" + log.getProgressUpdate() + "% Progress");
        tvSummary.setText(log.getSummary());

        // Setup Images
        if (log.getImages() != null && !log.getImages().isEmpty()) {
            vpImages.setVisibility(android.view.View.VISIBLE);
            tvNoImages.setVisibility(android.view.View.GONE);
            vpImages.setAdapter(new WorkLogImageAdapter(log.getImages())); // Reusing existing Adapter for ViewPager
        } else {
            vpImages.setVisibility(android.view.View.GONE);
            tvNoImages.setVisibility(android.view.View.VISIBLE);
        }
        
        // TODO: Load specific materials for this log if API supports it. 
        // Currently `DailyProgress` model doesn't store materials. 
        // Would need a separate API call `get_work_log_details.php?id=...` to populate "Materials Used" section accurately.
        // For now, hiding material section or showing empty state.
        
        dialog.show();
    }
}
