<?php
include 'db.php';

header('Content-Type: application/json');

$project_id = isset($_REQUEST['project_id']) ? intval($_REQUEST['project_id']) : 0;

if ($project_id <= 0) {
    echo json_encode(["error" => "Invalid Project ID"]);
    exit();
}

$response = array();

// 1. Fetch Project & Client Details
$sql = "SELECT p.id, p.project_name, p.location, p.start_date, p.end_date, p.status, p.estimated_cost, p.progress,
        c.name as client_name, c.phone as client_phone, c.email as client_email
        FROM projects p 
        LEFT JOIN clients c ON p.client_id = c.id 
        WHERE p.id = $project_id";

$result = $conn->query($sql);

if ($result && $result->num_rows > 0) {
    $project = $result->fetch_assoc();
    
    // Populate Response with Basic Info
    $project_data = array();
    $project_data['id'] = $project['id'];
    $project_data['name'] = $project['project_name'];
    $project_data['location'] = $project['location'] ? $project['location'] : "Location not set";
    $project_data['start_date'] = $project['start_date'] ? $project['start_date'] : "TBD";
    $project_data['status'] = $project['status'];
    $project_data['estimated_cost'] = floatval($project['estimated_cost']);
    $project_data['overall_progress'] = intval($project['progress']);
    
    $project_data['client_name'] = $project['client_name'] ? $project['client_name'] : "Unknown Client";
    $project_data['client_phone'] = $project['client_phone'] ? $project['client_phone'] : "N/A";
    $project_data['client_email'] = $project['client_email'] ? $project['client_email'] : "N/A";

    // 2. Calculate Financials (Total Paid)
    $sql_pay = "SELECT SUM(amount) as total_paid FROM payments WHERE project_id = $project_id";
    $res_pay = $conn->query($sql_pay);
    $paid = 0;
    if ($res_pay && $row_pay = $res_pay->fetch_assoc()) {
        $paid = floatval($row_pay['total_paid']);
    }
    $project_data['total_paid'] = $paid;
    $project_data['pending_amount'] = $project_data['estimated_cost'] - $paid;
    
    $response['project'] = $project_data;

    // 3. Generate Dynamic Stages
    // Since we don't have a specific `project_stages` table, we will simulate realistic stages based on the project progress.
    $stages = array();
    $overall_progress = intval($project['progress']); // e.g. 65

    // Define 4 standard stages
    $stage_defs = [
        ["id" => 1, "name" => "Foundation", "weight" => 25],
        ["id" => 2, "name" => "Structure", "weight" => 35],
        ["id" => 3, "name" => "Roofing", "weight" => 25],
        ["id" => 4, "name" => "Plumbing", "weight" => 15]
    ];

    $accumulated_progress = 0;
    
    foreach ($stage_defs as $def) {
        $id = $def['id'];
        $weight = $def['weight'];
        $stage_name = $def['name'];
        
        // Calculate how much of this stage is complete based on overall progress
        $current_stage_percent = 0;
        $status = "Pending";
        
        if ($overall_progress >= ($accumulated_progress + $weight)) {
            $current_stage_percent = 100;
            $status = "Completed";
            $accumulated_progress += $weight;
        } else if ($overall_progress > $accumulated_progress) {
            // Partially done
            $remaining_progress = $overall_progress - $accumulated_progress;
            $current_stage_percent = intval(($remaining_progress / $weight) * 100);
            $status = "In Progress";
            $accumulated_progress += $weight; // Assume we consumed what we had
        } else {
            $current_stage_percent = 0;
            $status = "Pending";
        }
        
        $stages[] = [
            "id" => $id,
            "stage_name" => $stage_name, // User requested "stage_name"
            "progress" => $current_stage_percent, // User requested "progress"
            "status" => $status
        ];
    }
    
    // Fetch Media
    $media = [];
    $media_sql = "SELECT file_path, media_type FROM project_media WHERE project_id = $project_id ORDER BY created_at DESC";
    $media_result = $conn->query($media_sql);
    if ($media_result) {
        while($row = $media_result->fetch_assoc()) {
            $media[] = $row;
        }
    }

    $response['stages'] = $stages;
    $response['media'] = $media;

} else {
    $response['error'] = "Project not found";
}

echo json_encode($response);
$conn->close();
?>
