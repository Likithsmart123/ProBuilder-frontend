package com.example.probuilder;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.util.Log;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AutoCompleteTextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.Spinner;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Locale;

public class WorkLogActivity extends AppCompatActivity {

    private int projectId;
    private TextView tvDate;
    private EditText etWorkSummary, etWorkerCount;
    private Button btnSaveLog;
    // Materials
    private Spinner spMaterialSelect;
    private EditText etMaterialQty;
    private Button btnAddMaterialChip;
    private ArrayAdapter<Material> materialAdapter;

    private ChipGroup chipGroupMaterials;
    private List<Material> availableMaterials = new ArrayList<>();
    private List<Material> selectedMaterials = new ArrayList<>();
    private int contractorId;
    
    // Media
    private RecyclerView rvMedia;
    private List<Uri> mediaUris = new ArrayList<>();
    private MediaAdapter mediaAdapter;

    // Helper for file reading
    private byte[] getFileDataFromDrawable(Uri uri) {
        try {
            java.io.InputStream inputStream = getContentResolver().openInputStream(uri);
            java.io.ByteArrayOutputStream byteBuffer = new java.io.ByteArrayOutputStream();
            int bufferSize = 1024;
            byte[] buffer = new byte[bufferSize];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                byteBuffer.write(buffer, 0, len);
            }
            return byteBuffer.toByteArray();
        } catch (Exception e) {
            Log.e("WorkLog", "File read error", e);
            return null;
        }
    }
    
    // Helper to get name
    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (android.database.Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                   int index = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME);
                   if(index >= 0) result = cursor.getString(index);
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    } 

    // Progress Elements
    private Spinner stageSpinner;
    private SeekBar progressSeekBar;
    private TextView progressText;
    private List<Stage> stageList = new ArrayList<>();
    private ArrayAdapter<Stage> stageAdapter;
    private Stage selectedStage = null;

    private final ActivityResultLauncher<Intent> pickMediaLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    if (data.getClipData() != null) {
                        for (int i = 0; i < data.getClipData().getItemCount(); i++) {
                            mediaUris.add(data.getClipData().getItemAt(i).getUri());
                        }
                    } else if (data.getData() != null) {
                        mediaUris.add(data.getData());
                    }
                    if (mediaAdapter != null) mediaAdapter.notifyDataSetChanged();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work_log);
        Log.d("WORK_LOG", "WorkLogActivity Started");

        projectId = getIntent().getIntExtra("PROJECT_ID", -1);
        
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        tvDate = findViewById(R.id.tvDate);
        etWorkSummary = findViewById(R.id.etWorkSummary);
        etWorkerCount = findViewById(R.id.etWorkerCount);
        
        // Materials UI
        
        // Materials UI - NEW
        spMaterialSelect = findViewById(R.id.spMaterialSelect);
        etMaterialQty = findViewById(R.id.etMaterialQty);
        btnAddMaterialChip = findViewById(R.id.btnAddMaterialChip);
        chipGroupMaterials = findViewById(R.id.chipGroupMaterials);
        
        // RESTORE MISSING INITS
        btnSaveLog = findViewById(R.id.btnSaveLog);
        rvMedia = findViewById(R.id.rvMedia);
        
        // Init Adapter
        materialAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, availableMaterials);
        spMaterialSelect.setAdapter(materialAdapter);

        btnAddMaterialChip.setOnClickListener(v -> addMaterialChip());

        // Progress UI
        stageSpinner = findViewById(R.id.stageSpinner);
        progressSeekBar = findViewById(R.id.progressSeekBar);
        progressText = findViewById(R.id.progressText);

        // Date Picker
        tvDate.setOnClickListener(v -> showDatePicker());

        // Media Buttons
        findViewById(R.id.btnAddPhoto).setOnClickListener(v -> pickMedia(true));
        findViewById(R.id.btnAddVideo).setOnClickListener(v -> pickMedia(false));

        // Media RecyclerView
        mediaAdapter = new MediaAdapter(this, mediaUris);
        rvMedia.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvMedia.setAdapter(mediaAdapter);
        
        btnSaveLog.setOnClickListener(v -> saveWorkLog());

        setupStageSpinner();

        loadStages();
        loadMaterials();
    }
    
    // STEP 3: POPULATE MATERIAL LIST CORRECTLY
    private void loadMaterials() {
        Log.d("WORK_LOG_MATERIAL", "loadMaterials() called");
        android.content.SharedPreferences sp = getSharedPreferences("ProBuilderPrefs", MODE_PRIVATE);
        contractorId = sp.getInt("contractor_id", -1);
        if (contractorId == -1) {
            Log.e("WORK_LOG", "Contractor ID not found in prefs. Cannot load materials.");
            Toast.makeText(this, "Error: User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = Constants.BASE_URL + "get_materials_stock.php?contractor_id=" + contractorId;
        Log.d("WORK_LOG_MATERIAL", "Calling URL: " + url);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
            response -> {
                Log.d("WORK_LOG_MATERIAL", "RESPONSE: " + response);
                Log.d("MATERIAL_API", response); // STEP 4: VERIFY API CALL
                try {
                    availableMaterials.clear();
                    // Add Default Prompt
                    availableMaterials.add(new Material(-1, "Select Material", 0, ""));
                    
                    JSONArray array = new JSONArray(response);
                    Log.d("COUNT_DEBUG", "API size = " + array.length());
                    java.util.Set<String> added = new java.util.HashSet<>();

                    for (int i = 0; i < array.length(); i++) {
                        JSONObject obj = array.getJSONObject(i);
                        // CORRECT MAPPING
                        int id = obj.getInt("id");                  // DB ID
                        String name = obj.getString("material_name");
                        double qty = obj.getDouble("quantity");   // Strict Step 3
                        String unit = obj.getString("unit");
                        
                        String key = name + "_" + unit;
                        Log.d("COUNT_DEBUG", name + " | " + unit);

                        if (added.add(key)) {
                            Log.d("WORK_LOG_MATERIAL", "Adding material: " + name);
                            Log.d("CHIP_DEBUG", "Added material: " + name); // STEP 6: VERIFY PARSING
                            availableMaterials.add(new Material(id, name, qty, unit));
                        }
                    }
                    materialAdapter.notifyDataSetChanged();
                    Log.d("COUNT_DEBUG", "Adapter size = " + availableMaterials.size());
                } catch (Exception e) {
                    Log.e("MATERIAL_PARSE", "Error parsing materials", e);
                }
            },
            error -> {
                Log.e("WORK_LOG_MATERIAL", "Error loading materials: " + error.toString());
                Log.e("MATERIAL_API", error.toString());
            }
        );
        Volley.newRequestQueue(this).add(stringRequest);
    }

    private void addMaterialChip() {
        Log.d("WORK_LOG_MATERIAL", "Add Material Chip clicked");
        Material selected = (Material) spMaterialSelect.getSelectedItem();
        String qtyStr = etMaterialQty.getText().toString().trim();
        
        if (selected == null || selected.getId() == -1) {
             Toast.makeText(this, "Select a valid material", Toast.LENGTH_SHORT).show();
             return;
        }
        
        if (qtyStr.isEmpty()) {
            Toast.makeText(this, "Enter quantity", Toast.LENGTH_SHORT).show();
            return;
        }
        
        double qty = 0;
        try {
            qty = Double.parseDouble(qtyStr);
        } catch (NumberFormatException e) {
             Toast.makeText(this, "Invalid quantity", Toast.LENGTH_SHORT).show();
             return;
        }
        
        if (qty <= 0) {
             Toast.makeText(this, "Quantity must be > 0", Toast.LENGTH_SHORT).show();
             return;
        }
        
        // CLONE OBJECT for usage (so we don't mutate the adapter's object directly if user adds same material twice, though logic below might need care for duplicates)
        // Simplest: use the same object but check duplicates in list
        if (selectedMaterials.contains(selected)) {
            Toast.makeText(this, "Material already added", Toast.LENGTH_SHORT).show();
            return;
        }

        // Set used qty on a fresh object or the same one?
        // Since the adapter holds references, we should ideally clone.
        // But for simplicity/task speed: we can just use a separate List<Material> for selected items 
        // that are DIFFERENT instances or just set it here if unique.
        // Let's create a NEW Material instance for the selected list to avoid messing up the adapter's available qty display if we were to modify it.
        Material entry = new Material(selected.getId(), selected.getName(), selected.getQuantity(), selected.getUnit());
        entry.setUsedQty(qty);
        
        selectedMaterials.add(entry);



        Chip chip = new Chip(this);
        String chipText = String.format(Locale.getDefault(), "%s (Used: %.1f %s)", entry.getName(), qty, entry.getUnit());
        chip.setText(chipText);
        chip.setTag(entry); 
        chip.setCloseIconVisible(true);
        chip.setOnCloseIconClickListener(v -> {
            chipGroupMaterials.removeView(chip);
            selectedMaterials.remove((Material) chip.getTag());
        });
        chipGroupMaterials.addView(chip);
        
        etMaterialQty.setText("");
    }

    private void setupStageSpinner() {
        stageAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, stageList);
        stageSpinner.setAdapter(stageAdapter);

        stageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Stage s = (Stage) parent.getItemAtPosition(position);

                if (s.id == -1) { // Default prompt
                    selectedStage = null;
                    progressSeekBar.setEnabled(false);
                    progressSeekBar.setProgress(0);
                    progressText.setText("Progress: --");
                } else {
                    selectedStage = s;
                    progressSeekBar.setEnabled(true);
                    progressSeekBar.setProgress(s.currentProgress);
                    progressText.setText("Progress: " + s.currentProgress + "%");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        progressSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && selectedStage != null) {
                    if (progress < selectedStage.currentProgress) {
                        // STRICT RULE: Prevent backward progress
                        seekBar.setProgress(selectedStage.currentProgress);
                        Toast.makeText(WorkLogActivity.this, "Progress cannot decrease", Toast.LENGTH_SHORT).show();
                    } else {
                        progressText.setText("Progress: " + progress + "%");
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                 if (selectedStage != null && seekBar.getProgress() < selectedStage.currentProgress) {
                    seekBar.setProgress(selectedStage.currentProgress);
                 }
            }
        });
    }

    private void loadStages() {
        String url = Constants.BASE_URL + "get_project_progress.php?project_id=" + projectId;
        
        stageList.clear();
        // Add default option as a dummy Stage
        stageList.add(new Stage(-1, "Select Stage (Optional)", 0));

        StringRequest request = new StringRequest(Request.Method.GET, url,
            response -> {
                try {
                    JSONObject root = new JSONObject(response);
                    JSONArray stages = root.optJSONArray("stages");
                    if (stages != null) {
                        for (int i = 0; i < stages.length(); i++) {
                            JSONObject s = stages.getJSONObject(i);
                            int id = s.optInt("id");
                            String name = s.optString("stage_name");
                            int progress = s.optInt("progress");
                            
                            stageList.add(new Stage(id, name, progress));
                        }
                        stageAdapter.notifyDataSetChanged();
                    }
                } catch (Exception e) {
                    Log.e("WorkLog", "Stage parse error", e);
                }
            },
            error -> Toast.makeText(this, "Failed to load stages", Toast.LENGTH_SHORT).show()
        );
        Volley.newRequestQueue(this).add(request);
    }

    private void showDatePicker() {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            tvDate.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void pickMedia(boolean isImage) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType(isImage ? "image/*" : "video/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        pickMediaLauncher.launch(Intent.createChooser(intent, "Select Media"));
    }

    private void saveWorkLog() {
        String date = tvDate.getText().toString();
        String summary = etWorkSummary.getText().toString();
        String workerCount = etWorkerCount.getText().toString();

        if (date.contains("Select") || summary.isEmpty()) {
            Toast.makeText(this, "Please fill required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // VALIDATE AND COLLECT MATERIALS
        JSONArray materialApiArray = new JSONArray();
        
        // STEP 5: ADD ONE LOG
        for (Material m : selectedMaterials) {
             Log.d("MATERIAL_SEND", "id=" + m.getId() + ", name=" + m.getName() + ", used=" + m.getUsedQty());
             try {
                // STEP 4: STRICT PAYLOAD - NO NAMES
                JSONObject o = new JSONObject();
                o.put("material_id", m.getId());     // REAL DB ID
                o.put("used_quantity", m.getUsedQty()); // USER INPUT
                materialApiArray.put(o);
             } catch (Exception e) { Log.e("WorkLog", "JSON error", e); }
        }
        
        final String materialsJson = materialApiArray.toString();

        // FIX 2: GET REAL ID AND VALIDATE
        int stageId = (selectedStage != null) ? selectedStage.id : -1;
        
        Log.d("WORK_LOG_API", "project_id=" + projectId + ", stage_id=" + stageId);

        // FIX 1: BLOCK IF INVALID
        if (stageId <= 0) {
            Toast.makeText(this, "Please select a valid stage", Toast.LENGTH_SHORT).show();
            return;
        }

        int deltaProgress = 0;
        if (selectedStage != null && progressSeekBar.isEnabled()) {
            int current = selectedStage.currentProgress;
            int newProgress = progressSeekBar.getProgress();
            deltaProgress = newProgress - current;

            if (deltaProgress <= 0) {
                 Toast.makeText(this, "Progress must be at least 1%", Toast.LENGTH_SHORT).show();
                 return;
            }
        }

        final int finalDelta = deltaProgress;

        String url = Constants.BASE_URL + "add_work_log.php";

        VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(Request.Method.POST, url,
                response -> {
                    try {
                        String resultResponse = new String(response.data);
                        JSONObject obj = new JSONObject(resultResponse);
                        String status = obj.getString("status");

                        if ("success".equals(status)) {
                             int workLogId = obj.optInt("work_log_id", -1);
                             showMessage("Work log saved" + (workLogId != -1 ? " (ID: " + workLogId + ")" : ""));
                             
                             showMessage("Work log saved" + (workLogId != -1 ? " (ID: " + workLogId + ")" : ""));
                             
                             Log.d("WORK_LOG", "Finishing WorkLogActivity");
                             setResult(RESULT_OK);
                             finish();
                        } else {
                             String msg = obj.optString("message", "Unknown error");
                             showMessage(msg);
                        }
                    } catch (Exception e) {
                        Log.e("WorkLog", "JSON Parse Error", e);
                        showMessage("Invalid server response");
                    }
                },
                error -> {
                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        String errorBody = new String(error.networkResponse.data);
                        Log.e("WorkLog", "API ERROR: " + errorBody);
                    } else {
                        Log.e("WorkLog", "Network Error", error);
                    }
                    if (!isFinishing() && !isDestroyed()) {
                        Toast.makeText(WorkLogActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("project_id", String.valueOf(projectId));
                params.put("work_date", date); // Updated key
                params.put("summary", summary);
                params.put("worker_count", workerCount.isEmpty() ? "0" : workerCount);
                params.put("materials_used", materialsJson); // Updated key
                
                if (selectedStage != null && progressSeekBar.isEnabled()) {
                    params.put("stage_id", String.valueOf(selectedStage.id));
                    params.put("progress_update", String.valueOf(finalDelta)); // SEND DELTA
                }
                
                return params;
            }

            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                for (int i = 0; i < mediaUris.size(); i++) {
                    Uri uri = mediaUris.get(i);
                    byte[] fileData = getFileDataFromDrawable(uri);
                    if (fileData != null) {
                        String fileName = getFileName(uri);
                        // FORCE MIME TYPE to Ensure PHP detects it as an image
                        params.put("media[" + i + "]", new VolleyMultipartRequest.DataPart(fileName, fileData, "image/jpeg"));
                    }
                }
                return params;
            }
        };

        Volley.newRequestQueue(this).add(multipartRequest);
    }

    private void showMessage(String message) {
        if (!isFinishing() && !isDestroyed()) {
             Toast.makeText(WorkLogActivity.this, message, Toast.LENGTH_SHORT).show();
        }
    }

    private static class Stage {
        int id;
        String name;
        int currentProgress;

        Stage(int id, String name, int currentProgress) {
            this.id = id;
            this.name = name;
            this.currentProgress = currentProgress;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
