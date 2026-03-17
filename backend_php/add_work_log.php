<?php
include 'db.php';

header('Content-Type: application/text'); // Return plain text "success" or error

$project_id = isset($_POST['project_id']) ? intval($_POST['project_id']) : 0;
// DEBUG: Check if files are arriving
error_log("FILES: " . print_r($_FILES, true));

$date = isset($_POST['date']) ? $_POST['date'] : '';
$summary = isset($_POST['summary']) ? $_POST['summary'] : '';
$worker_count = isset($_POST['worker_count']) ? intval($_POST['worker_count']) : 0;
$materials_used = isset($_POST['materials']) ? $_POST['materials'] : '';

// Optional Progress Update
$stage_id = isset($_POST['stage_id']) ? intval($_POST['stage_id']) : 0;
$progress = isset($_POST['progress']) ? intval($_POST['progress']) : 0;

if ($project_id <= 0 || empty($date) || empty($summary)) {
    echo "Missing required fields";
    exit();
}

// 1. Insert Work Log
// Ensure table exists (Basic check, usually run once manually)
$conn->query("CREATE TABLE IF NOT EXISTS work_logs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    project_id INT,
    log_date TEXT,
    summary TEXT,
    worker_count INT,
    materials_used TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
)");

$stmt = $conn->prepare("INSERT INTO work_logs (project_id, log_date, summary, worker_count, materials_used) VALUES (?, ?, ?, ?, ?)");
$stmt->bind_param("issis", $project_id, $date, $summary, $worker_count, $materials_used);

if (!$stmt->execute()) {
    echo "Error saving log: " . $stmt->error;
    $stmt->close();
    $conn->close();
    exit();
}
$stmt->close();

// 1.1 Deduct Stock from Materials Inventory (Strict ID-based)
$materials_list = json_decode($materials_used, true);
if (json_last_error() === JSON_ERROR_NONE && is_array($materials_list)) {
    $stmt_stock = $conn->prepare("UPDATE materials SET current_stock = current_stock - ? WHERE id = ?");
    foreach ($materials_list as $item) {
        // Expecting {"material_id": 1, "used_quantity": 50}
        $mat_id = isset($item['material_id']) ? intval($item['material_id']) : 0;
        $qty_used = isset($item['used_quantity']) ? floatval($item['used_quantity']) : 0;
        
        if ($mat_id > 0 && $qty_used > 0) {
            $stmt_stock->bind_param("di", $qty_used, $mat_id);
            $stmt_stock->execute();
        }
    }
    $stmt_stock->close();
}

// 2. Handle Media Uploads
// Ensure project_media table exists
$conn->query("CREATE TABLE IF NOT EXISTS project_media (
    id INT AUTO_INCREMENT PRIMARY KEY,
    project_id INT,
    file_path TEXT,
    media_type ENUM('image', 'video'),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
)");

$upload_dir = "uploads/";
if (!is_dir($upload_dir)) {
    mkdir($upload_dir, 0777, true);
}

if (!empty($_FILES['media']['name'][0])) {
    $total = count($_FILES['media']['name']);
    
    for ($i = 0; $i < $total; $i++) {
        $tmpFilePath = $_FILES['media']['tmp_name'][$i];
        
        if ($tmpFilePath != "") {
            $fileName = basename($_FILES['media']['name'][$i]);
            $targetFilePath = $upload_dir . time() . "_" . $fileName;
            
            // Determine type
            $ext = strtolower(pathinfo($fileName, PATHINFO_EXTENSION));
            $type = in_array($ext, ['mp4', 'mov', 'avi']) ? 'video' : 'image';

            if (move_uploaded_file($tmpFilePath, $targetFilePath)) {
                // Insert into DB
                $stmt_media = $conn->prepare("INSERT INTO project_media (project_id, file_path, media_type) VALUES (?, ?, ?)");
                $stmt_media->bind_param("iss", $project_id, $targetFilePath, $type);
                $stmt_media->execute();
                $stmt_media->close();
            }
        }
    }
}

// 3. Update Project Progress (if requested)
if ($stage_id > 0) {
    // Stage Definitions (Must match get_project_progress.php)
    $stage_defs = [
        1 => ["name" => "Foundation", "weight" => 25],
        2 => ["name" => "Structure", "weight" => 35],
        3 => ["name" => "Roofing", "weight" => 25],
        4 => ["name" => "Plumbing", "weight" => 15]
    ];

    if (isset($stage_defs[$stage_id])) {
        // Calculate new overall progress
        // Logic: Previous stages are 100%, Current stage is $progress%, Future stages are 0%
        
        $new_overall_progress = 0;
        
        foreach ($stage_defs as $id => $def) {
            if ($id < $stage_id) {
                // Previous stages must be considered 100% complete if we are updating a later stage
                $new_overall_progress += $def['weight'];
            } elseif ($id == $stage_id) {
                // Current stage contribution
                $contribution = ($progress / 100) * $def['weight'];
                $new_overall_progress += $contribution;
            } else {
                // Future stages contribute 0
            }
        }
        
        $new_overall_progress = intval($new_overall_progress);
        if ($new_overall_progress > 100) $new_overall_progress = 100;

        // Update Project
        $update_sql = "UPDATE projects SET progress = $new_overall_progress WHERE id = $project_id";
        if (!$conn->query($update_sql)) {
            // Log warning but don't fail the request
        }
    }
}

echo "success";
$conn->close();
?>
