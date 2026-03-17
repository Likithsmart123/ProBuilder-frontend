package com.example.probuilder;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.example.probuilder.VolleySingleton;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProjectDetailActivity extends AppCompatActivity {

    private int projectId;
    private String projectTitle;
    private String projectLocation = "";

    // UI Components
    private TextView tvProjectName, tvStatus, tvProgressPercent;
    private ProgressBar pbOverallProgress;
    
    // Overview Tab Views
    private TextView tvClientName, tvClientPhone, tvClientEmail;
    private TextView tvAddress, tvStartDate, tvCost, tvPaid, tvPending;
    private TextView budgetWarning;
    
    private int clientId = -1;
    private double currentBudget = 0;
    private boolean isFirstLoad = true;
    private String clientName = "";
    
    // Layout Containers for Tabs
    private View layoutOverview;
    private FrameLayout layoutPhotos, layoutMaterials;
    private LinearLayout layoutExpenses;
    private LinearLayout containerStages;
    
    // Chips
    private ChipGroup chipGroupTabs;
    
    // Refresh Layouts
    private SwipeRefreshLayout srlOverview, srlPhotos, srlExpenses, srlMaterials;
    private Button btnCompleteProject;
    private android.widget.ImageView ivDeleteProject;

    @Override
    protected void onResume() {
        super.onResume();
        // Reload data when returning from AddExpense / UseStock / WorkLog screens
        // Skip very first call since onCreate already calls loadProjectDetails()
        if (isFirstLoad) {
            isFirstLoad = false;
            return;
        }
        loadProjectDetails();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_detail);

        // Get Intent Data
        Intent intent = getIntent();
        projectId = intent.getIntExtra("PROJECT_ID", -1);
        projectTitle = intent.getStringExtra("PROJECT_NAME");

        if (projectId == -1) {
            Toast.makeText(this, "Invalid Project ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupToolbar();
        initializeViews();
        setupTabs();
        setupBottomNavigation();
        
        // Initial Load
        loadProjectDetails();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(projectTitle != null ? projectTitle : "Project Details");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
        
        android.widget.ImageView ivChatProject = findViewById(R.id.ivChatProject);
        ivChatProject.setOnClickListener(v -> {
            if (clientId != -1) {
                Intent chatIntent = new Intent(ProjectDetailActivity.this, ChatActivity.class);
                chatIntent.putExtra("PROJECT_ID", projectId);
                chatIntent.putExtra("OTHER_USER_ID", clientId);
                chatIntent.putExtra("OTHER_USER_NAME", clientName);
                startActivity(chatIntent);
            } else {
                Toast.makeText(this, "Client details not loaded yet", Toast.LENGTH_SHORT).show();
            }
        });

        ivDeleteProject = findViewById(R.id.ivDeleteProject);
        ivDeleteProject.setOnClickListener(v -> confirmDeleteProject());
    }

    private void initializeViews() {
        // Header
        tvProjectName = findViewById(R.id.tvProjectNameHeader);
        tvStatus = findViewById(R.id.tvProjectStatus);
        tvProgressPercent = findViewById(R.id.tvOverallProgressPercent);
        pbOverallProgress = findViewById(R.id.pbOverallProgress);
        
        if (projectTitle != null) tvProjectName.setText(projectTitle);

        // Overview Tab
        tvClientName = findViewById(R.id.tvOverviewClientName);
        tvClientPhone = findViewById(R.id.tvOverviewClientPhone);
        tvClientEmail = findViewById(R.id.tvOverviewClientEmail);
        tvAddress = findViewById(R.id.tvOverviewAddress);
        tvStartDate = findViewById(R.id.tvOverviewStartDate);
        tvCost = findViewById(R.id.tvOverviewCost);
        tvPaid = findViewById(R.id.tvOverviewPaid);
        tvPending = findViewById(R.id.tvOverviewPending);
        budgetWarning = findViewById(R.id.budgetWarning);

        // Layouts
        layoutOverview = findViewById(R.id.layoutOverview);
        layoutPhotos = findViewById(R.id.layoutPhotos);
        layoutExpenses = findViewById(R.id.layoutExpenses); // Is LinearLayout in XML but cast to View/Layout
        containerStages = findViewById(R.id.containerStages);
        layoutMaterials = findViewById(R.id.layoutMaterials);
        // Note: Check XML types. layoutOverview is ScrollView. layoutExpenses is LinearLayout. layoutPhotos is FrameLayout. layoutMaterials is FrameLayout.
        // We will refer to them as View for visibility toggling.

        // Chips
        chipGroupTabs = findViewById(R.id.chipGroupTabs);
        
        // Refresh
        srlOverview = findViewById(R.id.srlOverview);
        srlOverview.setOnRefreshListener(this::loadProjectDetails);
        
        btnCompleteProject = findViewById(R.id.btnCompleteProject);
        if(btnCompleteProject != null) {
            btnCompleteProject.setOnClickListener(v -> markProjectAsCompleted());
        }
        
        // Others (Placeholders - implement logic later)
        srlPhotos = findViewById(R.id.srlPhotos);
        srlPhotos.setOnRefreshListener(this::fetchPhotos);
        
        srlExpenses = findViewById(R.id.srlExpenses);
        srlExpenses.setOnRefreshListener(() -> fetchExpenses(currentBudget));
        
        srlMaterials = findViewById(R.id.srlMaterials);
        srlMaterials.setOnRefreshListener(this::fetchMaterials);
    }

    private void setupTabs() {
        chipGroupTabs.setOnCheckedChangeListener((group, checkedId) -> {
            // Hide all first
            layoutOverview.setVisibility(View.GONE);
            if (layoutPhotos != null) layoutPhotos.setVisibility(View.GONE);
            if (layoutExpenses != null) layoutExpenses.setVisibility(View.GONE);
            if (layoutMaterials != null) layoutMaterials.setVisibility(View.GONE);

            if (checkedId == R.id.chipOverview) {
                layoutOverview.setVisibility(View.VISIBLE);
            } else if (checkedId == R.id.chipPhotos) {
                if (layoutPhotos != null) layoutPhotos.setVisibility(View.VISIBLE);
            } else if (checkedId == R.id.chipExpenses) {
                if (layoutExpenses != null) layoutExpenses.setVisibility(View.VISIBLE);
            } else if (checkedId == R.id.chipMaterials) {
                if (layoutMaterials != null) layoutMaterials.setVisibility(View.VISIBLE);
            }
        });
    }
    
    private void setupBottomNavigation() {
        Button btnWorkLog = findViewById(R.id.btnWorkLog);
        Button btnNavExpense = findViewById(R.id.btnNavExpense);
        Button btnNavMaterials = findViewById(R.id.btnNavMaterials);

        btnNavExpense.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddExpenseActivity.class);
            intent.putExtra("project_id", projectId);
            intent.putExtra("project_name", projectTitle);
            startActivity(intent);
        });
        
        btnNavMaterials.setOnClickListener(v -> {
            Intent intent = new Intent(this, UseStockActivity.class);
            intent.putExtra("project_id", projectId);
            intent.putExtra("project_name", projectTitle);
            startActivity(intent);
        });
        
        btnWorkLog.setOnClickListener(v -> {
            Intent intent = new Intent(this, WorkLogActivity.class);
            intent.putExtra("PROJECT_ID", projectId);
            startActivity(intent);
        });
    }

    private void loadProjectDetails() {
        srlOverview.setRefreshing(true);
        
        String url = Constants.BASE_URL + "get_project_details.php?project_id=" + projectId;

        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    srlOverview.setRefreshing(false);
                    Log.d("DEBUG_PROJECT_JSON", response); // Log loaded JSON
                    try {
                        JSONObject json = new JSONObject(response);
                        if (json.optString("status").equals("success")) {
                            JSONObject project = json.getJSONObject("project");
                            
                            // 1. Basic Info
                            String name = project.optString("project_name", projectTitle);
                            String status = project.optString("status", "Ongoing");
                            int progress = project.optInt("overall_progress", 0);
                            String location = project.optString("location", "");
                            projectLocation = location;
                            String start = project.optString("start_date", "N/A");
                            
                            tvProjectName.setText(name);
                            if (getSupportActionBar() != null) getSupportActionBar().setTitle(name);
                            
                            tvStatus.setText(status);
                            pbOverallProgress.setProgress(progress);
                            tvProgressPercent.setText(progress + "% Complete");

                            if (status.equalsIgnoreCase("Completed")) {
                                tvStatus.setTextColor(android.graphics.Color.parseColor("#4CAF50")); // Green
                                if(btnCompleteProject != null) btnCompleteProject.setVisibility(View.GONE);
                            } else {
                                tvStatus.setTextColor(android.graphics.Color.parseColor("#E0E0E0"));
                                if (progress >= 100 && btnCompleteProject != null) {
                                    btnCompleteProject.setVisibility(View.VISIBLE);
                                } else if (btnCompleteProject != null) {
                                    btnCompleteProject.setVisibility(View.GONE);
                                }
                            }
                            
                            tvAddress.setText(location);
                            tvStartDate.setText("Start Date: " + start);

                            // 2. Client Info
                            // 2. Client Info
                            JSONObject client = project.optJSONObject("client");
                            
                            // Get Intent Extras as Fallback
                            String intentPhone = getIntent().getStringExtra("CLIENT_PHONE");
                            String intentEmail = getIntent().getStringExtra("CLIENT_EMAIL");
                            
                            if (client != null) {
                                clientId = client.optInt("id", client.optInt("client_id", -1));
                                clientName = client.optString("name", "N/A");
                                tvClientName.setText(clientName);
                                
                                String phone = client.optString("phone");
                                if (isInvalid(phone)) phone = client.optString("client_phone");
                                if (isInvalid(phone)) phone = client.optString("mobile");
                                if (isInvalid(phone)) phone = client.optString("contact_no");
                                if (isInvalid(phone)) phone = intentPhone; // Fallback
                                
                                tvClientPhone.setText(phone != null && !phone.isEmpty() ? phone : "N/A");
                                
                                String email = client.optString("email");
                                if (isInvalid(email)) email = client.optString("client_email");
                                if (isInvalid(email)) email = client.optString("mail");
                                if (isInvalid(email)) email = intentEmail; // Fallback
                                
                                tvClientEmail.setText(email != null && !email.isEmpty() ? email : "N/A");
                            } else {
                                // If client object is completely missing
                                tvClientName.setText("N/A");
                                tvClientPhone.setText(intentPhone != null ? intentPhone : "N/A");
                                tvClientEmail.setText(intentEmail != null ? intentEmail : "N/A");
                            }
                            
                            // 3. Financials
                            double budget = project.optDouble("budget", 0);
                            currentBudget = budget;
                            String budgetStr = String.format("Estimated Cost: ₹ %,.2f", budget);
                            tvCost.setText(budgetStr);
                            
                            // Check for budget warning
                            if (budget > 0 && progress < 100) { 
                                // Logic placeholder: if expenses > budget
                                budgetWarning.setVisibility(View.GONE);
                            }
                            
                            // 4. Stage Progress
                            if (containerStages != null) {
                                containerStages.removeAllViews();
                                
                                // Try multiple keys for stages
                                JSONArray stages = project.optJSONArray("stages");
                                if (stages == null) stages = project.optJSONArray("project_stages");
                                if (stages == null) stages = project.optJSONArray("milestones");
                                if (stages == null) stages = project.optJSONArray("phases");
                                
                                if (stages != null && stages.length() > 0) {
                                    for (int i = 0; i < stages.length(); i++) {
                                        JSONObject stage = stages.optJSONObject(i);
                                        if (stage != null) {
                                            String stageName = stage.optString("stage_name", "Unknown Stage");
                                            if (stageName.equals("Unknown Stage")) stageName = stage.optString("name", "Stage " + (i+1));
                                            
                                            Double progressVal = stage.optDouble("completed_percentage", -1);
                                            if (progressVal == -1) progressVal = stage.optDouble("progress", 0);
                                            
                                            // Inflate layout
                                            View stageView = getLayoutInflater().inflate(R.layout.item_stage_progress, containerStages, false);
                                            TextView tvStageName = stageView.findViewById(R.id.tvStageName);
                                            TextView tvStagePercent = stageView.findViewById(R.id.tvStagePercent);
                                            ProgressBar pbStage = stageView.findViewById(R.id.pbStageProgress);
                                            
                                            tvStageName.setText(stageName);
                                            tvStagePercent.setText((int)Math.round(progressVal) + "%");
                                            pbStage.setProgress((int)Math.round(progressVal));
                                            
                                            containerStages.addView(stageView);
                                        }
                                    }
                                } else {
                                    // Show "No stages defined" or similar if needed, or just leave empty
                                    // Default Stages Fallback as per user request for "Perfect" loading
                                    String[] defaultStages = {"Foundation", "Structure", "Roofing", "Plumbing"};
                                    for (String stageName : defaultStages) {
                                        View stageView = getLayoutInflater().inflate(R.layout.item_stage_progress, containerStages, false);
                                        TextView tvStageName = stageView.findViewById(R.id.tvStageName);
                                        TextView tvStagePercent = stageView.findViewById(R.id.tvStagePercent);
                                        ProgressBar pbStage = stageView.findViewById(R.id.pbStageProgress);
                                        
                                        tvStageName.setText(stageName);
                                        tvStagePercent.setText("0%");
                                        pbStage.setProgress(0);
                                        
                                        containerStages.addView(stageView);
                                    }
                                }
                            }
                            
                            // Load Payments for Total Paid / Pending
                            fetchPayments(budget);
                            
                            // Load Expenses for the expenses tab
                            fetchExpenses(budget);
                            
                            // Load Photos
                            fetchPhotos();
                            
                            // Load Materials
                            fetchMaterials();
                            
                        } else {
                            Toast.makeText(this, "Failed to load details: " + json.optString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e("ProjectDetailErr", "Error parsing: " + e.getMessage());
                        // Toast.makeText(this, "Error parsing details", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    srlOverview.setRefreshing(false);
                    Log.e("ProjectDetail", "Error loading details", error);
                    // Toast.makeText(this, "Network Error", Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                // Use Contractor Auth Token
                android.content.SharedPreferences prefs = getSharedPreferences("AUTH", MODE_PRIVATE);
                String token = prefs.getString("token", "");
                
                if (token != null && !token.isEmpty()) {
                    headers.put("Authorization", token);
                }
                return headers;
            }
        };
        
        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }
    
    private void fetchPayments(double budget) {
        String url = Constants.BASE_URL + "get_project_payments.php?project_id=" + projectId;
        
        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        double totalPaid = json.optDouble("total_received", 0);
                        
                        Log.d("FetchPayments", "Total received: " + totalPaid);
                        
                        // Update Financials UI from payments
                        String paidStr = String.format("Total Paid: ₹ %,.2f", totalPaid);
                        double remaining = budget - totalPaid;
                        String pendingStr = String.format("Pending Amount: ₹ %,.2f", remaining);
                        
                        tvPaid.setText(paidStr);
                        tvPending.setText(pendingStr);
                        
                        if (remaining < 0) {
                            tvPending.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                            budgetWarning.setVisibility(View.VISIBLE);
                            budgetWarning.setText("Budget Exceeded by ₹ " + String.format("%,.2f", Math.abs(remaining)));
                        } else {
                            tvPending.setTextColor(android.graphics.Color.BLACK);
                            budgetWarning.setVisibility(View.GONE);
                        }
                    } catch (Exception e) {
                        Log.e("FetchPayments", "Parse error: " + e.getMessage());
                        e.printStackTrace();
                    }
                },
                error -> Log.e("FetchPayments", "Error: " + error.toString())
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                android.content.SharedPreferences prefs = getSharedPreferences("AUTH", MODE_PRIVATE);
                String token = prefs.getString("token", "");
                if (token != null && !token.isEmpty()) headers.put("Authorization", token);
                return headers;
            }
        };
        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }
    
    private void fetchExpenses(double budget) {
        srlExpenses.setRefreshing(true);
        String url = Constants.BASE_URL + "get_expenses.php?project_id=" + projectId;
        
        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    srlExpenses.setRefreshing(false);
                    try {
                        JSONObject json = new JSONObject(response);
                        double totalExpenses = 0;
                        List<Expense> expenseList = new ArrayList<>();
                        
                        if (json.optString("status").equals("success")) {
                            JSONArray expenses = json.optJSONArray("expenses");
                            if (expenses != null) {
                                for(int i=0; i<expenses.length(); i++) {
                                    JSONObject obj = expenses.getJSONObject(i);
                                    Expense e = new Expense(
                                        obj.optString("expense_id", obj.optString("id")),
                                        obj.optString("category"),
                                        obj.optDouble("amount"),
                                        obj.optString("expense_date", obj.optString("date")),
                                        String.valueOf(projectId), 
                                        projectTitle,
                                        obj.optString("title", obj.optString("description")),
                                        obj.optString("invoice_no", obj.optString("invoice_number")),
                                        projectLocation // Use project location
                                    );
                                    expenseList.add(e);
                                    totalExpenses += e.getAmount();
                                }
                            }
                        }
                        
                        // Update expenses total on the Expenses tab
                        TextView tvExpensesTotal = findViewById(R.id.tvExpensesTotal);
                        if (tvExpensesTotal != null) {
                            tvExpensesTotal.setText(String.format("₹ %,.2f", totalExpenses));
                        }
                        
                        // Setup Expense Recycler if list is not empty
                        RecyclerView rvExpenses = findViewById(R.id.rvExpenses);
                        if(rvExpenses != null && !expenseList.isEmpty()) {
                            rvExpenses.setLayoutManager(new LinearLayoutManager(this));
                            rvExpenses.setAdapter(new ProjectExpenseAdapter(expenseList));
                            
                            if (findViewById(R.id.tvNoExpenses) != null) 
                                findViewById(R.id.tvNoExpenses).setVisibility(View.GONE);
                        } else {
                             if (findViewById(R.id.tvNoExpenses) != null) 
                                findViewById(R.id.tvNoExpenses).setVisibility(View.VISIBLE);
                        }
                        
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e("FetchExpenses", "Parse error: " + e.getMessage());
                    }
                },
                error -> {
                    srlExpenses.setRefreshing(false);
                    Log.e("FetchExpenses", "Error: " + error.toString());
                }
        ) {
             @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                android.content.SharedPreferences prefs = getSharedPreferences("AUTH", MODE_PRIVATE);
                String token = prefs.getString("token", "");
                if (token != null && !token.isEmpty()) headers.put("Authorization", token);
                return headers;
            }
        };
        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }
    
    private void fetchPhotos() {
        srlPhotos.setRefreshing(true);
        String url = Constants.BASE_URL + "get_project_media.php?project_id=" + projectId;
        
        StringRequest request = new StringRequest(Request.Method.GET, url,
            response -> {
                srlPhotos.setRefreshing(false);
                Log.d("FetchPhotos", "Response: " + response);
                try {
                    JSONObject json = new JSONObject(response);
                    if (json.optString("status").equals("success")) {
                        JSONArray media = json.optJSONArray("media");
                        List<PhotoItem> photoList = new ArrayList<>();
                        if (media != null) {
                            for (int i = 0; i < media.length(); i++) {
                                JSONObject obj = media.getJSONObject(i);
                                String photoUrl = obj.optString("url");
                                if (photoUrl.isEmpty()) photoUrl = obj.optString("file_path");
                                
                                if (!photoUrl.isEmpty()) {
                                    if (!photoUrl.startsWith("http")) {
                                        // The DB stores paths like "uploads/..."
                                        // Constants.BASE_URL is "http://14.139.187.229:8081/oct/spic_730/probuilder/"
                                        photoUrl = Constants.BASE_URL + photoUrl;
                                    }
                                    photoList.add(new PhotoItem(photoUrl, obj.optString("media_type")));
                                }
                            }
                        }
                        
                        Log.d("FetchPhotos", "Photos found: " + photoList.size());
                        
                        RecyclerView rvPhotos = findViewById(R.id.rvPhotos);
                        TextView tvNoPhotos = findViewById(R.id.tvNoPhotos);
                        
                        if (!photoList.isEmpty()) {
                            if (rvPhotos != null) {
                                rvPhotos.setVisibility(View.VISIBLE);
                                rvPhotos.setLayoutManager(new androidx.recyclerview.widget.GridLayoutManager(this, 3));
                                PhotosAdapter adapter = new PhotosAdapter(this);
                                adapter.setPhotos(photoList);
                                rvPhotos.setAdapter(adapter);
                                
                                adapter.setOnPhotoClickListener(item -> {
                                    Intent intent = new Intent(ProjectDetailActivity.this, MediaViewerActivity.class);
                                    intent.putExtra("MEDIA_URL", item.url);
                                    intent.putExtra("MEDIA_TYPE", item.mediaType);
                                    startActivity(intent);
                                });
                            }
                            if (tvNoPhotos != null) tvNoPhotos.setVisibility(View.GONE);
                        } else {
                            if (rvPhotos != null) rvPhotos.setVisibility(View.GONE);
                            if (tvNoPhotos != null) tvNoPhotos.setVisibility(View.VISIBLE);
                        }
                    } else {
                        Log.e("FetchPhotos", "Status not success: " + json.optString("message"));
                    }
                } catch (Exception e) {
                    Log.e("FetchPhotos", "Parse error: " + e.getMessage());
                    e.printStackTrace();
                }
            },
            error -> {
                srlPhotos.setRefreshing(false);
                Log.e("FetchPhotos", "Network Error: " + error.toString());
            }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                android.content.SharedPreferences prefs = getSharedPreferences("AUTH", MODE_PRIVATE);
                String token = prefs.getString("token", "");
                if (token != null && !token.isEmpty()) headers.put("Authorization", token);
                return headers;
            }
        };
        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }
    
    private void fetchMaterials() {
        srlMaterials.setRefreshing(true);
        String url = Constants.BASE_URL + "get_project_materials.php?project_id=" + projectId;
        
        StringRequest request = new StringRequest(Request.Method.GET, url,
            response -> {
                srlMaterials.setRefreshing(false);
                Log.d("FetchMaterials", "Response: " + response);
                try {
                    JSONObject json = new JSONObject(response);
                     List<ProjectMaterialAdapter.ProjectMaterial> materialList = new ArrayList<>();
                     
                    if (json.optString("status").equals("success")) {
                        JSONArray materials = json.optJSONArray("materials");
                        if (materials != null) {
                            for (int i = 0; i < materials.length(); i++) {
                                JSONObject obj = materials.getJSONObject(i);
                                materialList.add(new ProjectMaterialAdapter.ProjectMaterial(
                                    obj.optInt("material_id", obj.optInt("id")),
                                    obj.optString("material_name"),
                                    obj.optString("unit", "Units"),
                                    obj.optDouble("used_quantity", 0),
                                    obj.optInt("remaining_quantity", 0),
                                    obj.optString("specifications", "")
                                ));
                            }
                        }
                        
                        Log.d("FetchMaterials", "Materials found: " + materialList.size());
                        
                        RecyclerView rvMaterials = findViewById(R.id.rvMaterials);
                        TextView tvNoMaterials = findViewById(R.id.tvNoMaterials);
                        
                        if (!materialList.isEmpty()) {
                            if (rvMaterials != null) {
                                rvMaterials.setVisibility(View.VISIBLE);
                                rvMaterials.setLayoutManager(new LinearLayoutManager(this));
                                rvMaterials.setAdapter(new ProjectMaterialAdapter(materialList, projectId));
                            }
                            if (tvNoMaterials != null) tvNoMaterials.setVisibility(View.GONE);
                        } else {
                            if (rvMaterials != null) rvMaterials.setVisibility(View.GONE);
                            if (tvNoMaterials != null) tvNoMaterials.setVisibility(View.VISIBLE);
                        }
                    } else {
                        Log.e("FetchMaterials", "Status not success: " + json.optString("message"));
                    }
                } catch (Exception e) {
                    Log.e("FetchMaterials", "Parse error: " + e.getMessage());
                    e.printStackTrace();
                }
            },
            error -> {
                srlMaterials.setRefreshing(false);
                Log.e("FetchMaterials", "Error: " + error.toString());
            }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                android.content.SharedPreferences prefs = getSharedPreferences("AUTH", MODE_PRIVATE);
                String token = prefs.getString("token", "");
                if (token != null && !token.isEmpty()) headers.put("Authorization", token);
                return headers;
            }
        };
        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private boolean isInvalid(String s) {
        return s == null || s.isEmpty() || s.equalsIgnoreCase("null") || s.equalsIgnoreCase("N/A");
    }

    private void markProjectAsCompleted() {
        new AlertDialog.Builder(this)
                .setTitle("Mark as Completed")
                .setMessage("Are you sure you want to mark this project as completed? This action cannot be undone.")
                .setPositiveButton("Yes, Mark Completed", (dialog, which) -> {
                    String url = Constants.BASE_URL + "complete_project.php";
                    StringRequest request = new StringRequest(Request.Method.POST, url,
                            response -> {
                                try {
                                    JSONObject json = new JSONObject(response);
                                    if(json.optString("status").equals("success")) {
                                        Toast.makeText(this, "Project marked as Completed", Toast.LENGTH_SHORT).show();
                                        tvStatus.setText("Completed");
                                        tvStatus.setTextColor(android.graphics.Color.parseColor("#4CAF50")); // Green
                                        btnCompleteProject.setVisibility(View.GONE);
                                        // Update local variable just in case
                                        // If any logic needs to be refreshed, we can call loadProjectDetails()
                                    } else {
                                        Toast.makeText(this, "Failed: " + json.optString("message"), Toast.LENGTH_SHORT).show();
                                    }
                                } catch(Exception e) {
                                    e.printStackTrace();
                                }
                            },
                            error -> Toast.makeText(this, "Network Error", Toast.LENGTH_SHORT).show()
                    ) {
                        @Override
                        protected Map<String, String> getParams() {
                            Map<String, String> params = new HashMap<>();
                            params.put("project_id", String.valueOf(projectId));
                            return params;
                        }
                    };
                    VolleySingleton.getInstance(this).addToRequestQueue(request);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void confirmDeleteProject() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Project")
                .setMessage("Are you sure you want to permanently delete \"" + projectTitle + "\"? This will also remove all payments and invites linked to this project. This action cannot be undone.")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Delete", (dialog, which) -> deleteProject())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteProject() {
        String url = Constants.BASE_URL + "delete_project.php";

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        if (json.optString("status").equals("success")) {
                            Toast.makeText(this, "Project deleted successfully", Toast.LENGTH_SHORT).show();
                            // Go back to the projects list
                            finish();
                        } else {
                            Toast.makeText(this,
                                    "Delete failed: " + json.optString("message", "Unknown error"),
                                    Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error processing response", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("DeleteProject", "Network error: " + error.toString());
                    Toast.makeText(this, "Network error. Please try again.", Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("project_id", String.valueOf(projectId));
                return params;
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                android.content.SharedPreferences prefs = getSharedPreferences("AUTH", MODE_PRIVATE);
                String token = prefs.getString("token", "");
                if (token != null && !token.isEmpty()) headers.put("Authorization", token);
                return headers;
            }
        };

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }
}
