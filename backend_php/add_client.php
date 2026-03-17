<?php
include 'db.php';

header('Content-Type: application/json');

$contractor_id = $_POST['contractor_id'] ?? null;
$name = $_POST['name'] ?? null;
$email = $_POST['email'] ?? null;
$phone = $_POST['phone'] ?? null;
$address = $_POST['address'] ?? null;

if (!$contractor_id || !$name || !$email || !$phone) {
    echo json_encode(["status" => "error", "message" => "Missing required fields"]);
    exit();
}

$stmt = $conn->prepare("INSERT INTO clients (contractor_id, client_name, email, phone) VALUES (?, ?, ?, ?)");
if (!$stmt) {
    echo json_encode(["status" => "error", "message" => "Database error: " . $conn->error]);
    exit();
}

$stmt->bind_param("isss", $contractor_id, $name, $email, $phone);

if ($stmt->execute()) {
    echo json_encode(["status" => "success", "message" => "Client created successfully", "client_id" => $stmt->insert_id]);
} else {
    echo json_encode(["status" => "error", "message" => "Failed to create client: " . $stmt->error]);
}

$stmt->close();
$conn->close();
?>
