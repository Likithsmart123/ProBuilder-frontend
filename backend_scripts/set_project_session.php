<?php
ini_set('display_errors', 0);
error_reporting(0);
header('Content-Type: application/json');

session_start();
require 'db.php';

if (!isset($_SESSION['client_id'])) {
    echo json_encode(["status" => "error", "message" => "Unauthorized"]);
    exit;
}

$client_id = $_SESSION['client_id'];
$project_id = $_POST['project_id'] ?? null;

if (!$project_id) {
    echo json_encode(["status" => "error", "message" => "Missing Project ID"]);
    exit;
}

// Verify this project actually belongs to this client
$sql = "SELECT id FROM projects WHERE id = ? AND client_id = ?";
$stmt = $conn->prepare($sql);

if ($stmt) {
    $stmt->bind_param("ii", $project_id, $client_id);
    $stmt->execute();
    $result = $stmt->get_result();
    
    if ($result->num_rows > 0) {
        $_SESSION['project_id'] = $project_id;
        echo json_encode(["status" => "success"]);
    } else {
        echo json_encode(["status" => "error", "message" => "Access Denied"]);
    }
    $stmt->close();
} else {
    echo json_encode(["status" => "error", "message" => "Database error"]);
}
exit;
?>
