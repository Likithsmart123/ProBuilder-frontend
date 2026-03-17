<?php
ini_set('display_errors', 0);
error_reporting(0);
header('Content-Type: application/json');

require 'db.php';

$headers = getallheaders();
$token = $headers['Authorization'] ?? '';

if (!$token) {
    echo json_encode(["status" => "error", "message" => "Unauthorized"]);
    exit;
}

// Validate Token
$stmt = $conn->prepare("SELECT id FROM clients WHERE api_token = ?");
$stmt->bind_param("s", $token);
$stmt->execute();
$result = $stmt->get_result();

if ($result->num_rows === 0) {
    echo json_encode(["status" => "error", "message" => "Unauthorized"]);
    exit;
}

$client = $result->fetch_assoc();
$client_id = $client['id'];
$stmt->close();

// Get Project ID from POST or GET
$project_id = $_POST['project_id'] ?? $_GET['project_id'] ?? null;

if (!$project_id) {
    echo json_encode(["status" => "error", "message" => "No project specified"]);
    exit;
}

// Fetch Project Details (Ensure it belongs to the client)
$sql = "SELECT project_name, location, budget, status, overall_progress, start_date, end_date
        FROM projects
        WHERE id = ? AND client_id = ?";

$stmt = $conn->prepare($sql);
if ($stmt) {
    $stmt->bind_param("ii", $project_id, $client_id);
    $stmt->execute();
    $result = $stmt->get_result();
    $project = $result->fetch_assoc();
    
    if ($project) {
        echo json_encode($project);
    } else {
        echo json_encode(["status" => "error", "message" => "Project not found or access denied"]);
    }
    $stmt->close();
} else {
    echo json_encode(["status" => "error", "message" => "Database error"]);
}
exit;
?>
