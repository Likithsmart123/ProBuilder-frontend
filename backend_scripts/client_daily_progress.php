<?php
ini_set('display_errors', 0);
error_reporting(0);
header('Content-Type: application/json');

require 'db.php';

$headers = getallheaders();
$token = $headers['Authorization'] ?? '';

if (!$token) {
    echo json_encode([]);
    exit;
}

// Validate Token
$stmt = $conn->prepare("SELECT id FROM clients WHERE api_token = ?");
$stmt->bind_param("s", $token);
$stmt->execute();
$result = $stmt->get_result();

if ($result->num_rows === 0) {
    echo json_encode([]);
    exit;
}
$stmt->close();

$project_id = $_POST['project_id'] ?? $_GET['project_id'] ?? null;

if (!$project_id) {
    echo json_encode([]);
    exit;
}

$sql = "SELECT progress_date, description, progress_percent
        FROM daily_progress
        WHERE project_id = ?
        ORDER BY progress_date DESC";

$stmt = $conn->prepare($sql);
if ($stmt) {
    $stmt->bind_param("i", $project_id);
    $stmt->execute();
    $result = $stmt->get_result();
    
    $response = [];
    while ($row = $result->fetch_assoc()) {
        $response[] = $row;
    }
    echo json_encode($response);
    $stmt->close();
} else {
    echo json_encode([]);
}
exit;
?>
