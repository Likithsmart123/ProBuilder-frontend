<?php
include 'db.php';

header('Content-Type: application/json');

$contractor_id = $_POST['contractor_id'] ?? null;
$client_id = $_POST['client_id'] ?? null;
$title = $_POST['title'] ?? null;
$location = $_POST['location'] ?? null;
$start_date = $_POST['start_date'] ?? null;
$end_date = $_POST['end_date'] ?? null;
$budget = $_POST['budget'] ?? 0;

if (!$contractor_id || !$client_id || !$title || !$location || !$start_date || !$end_date) {
    echo json_encode(["status" => "error", "message" => "Missing required fields"]);
    exit();
}

// Ensure client_id is valid
$checkClient = $conn->query("SELECT id FROM clients WHERE id = '$client_id' AND contractor_id = '$contractor_id'");
if ($checkClient->num_rows == 0) {
    echo json_encode(["status" => "error", "message" => "Invalid client"]);
    exit();
}

$stmt = $conn->prepare("INSERT INTO projects (contractor_id, client_id, title, location, start_date, end_date, budget, status) VALUES (?, ?, ?, ?, ?, ?, ?, 'active')");
if (!$stmt) {
    echo json_encode(["status" => "error", "message" => "Database error: " . $conn->error]);
    exit();
}

$stmt->bind_param("iissssd", $contractor_id, $client_id, $title, $location, $start_date, $end_date, $budget);

if ($stmt->execute()) {
    echo json_encode(["status" => "success", "message" => "Project created successfully", "project_id" => $stmt->insert_id]);
} else {
    echo json_encode(["status" => "error", "message" => "Failed to create project: " . $stmt->error]);
}

$stmt->close();
$conn->close();
?>
