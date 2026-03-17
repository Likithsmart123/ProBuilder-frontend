<?php
include 'db.php';

// Allow any origin for testing; restrict in production
header("Access-Control-Allow-Origin: *");
header("Content-Type: text/plain"); // Returning plain text as per Android logic

$contractor_id = $_POST['contractor_id'] ?? null;
$project_id = $_POST['project_id'] ?? null;

if (!$contractor_id || !$project_id) {
    echo "error|Missing parameters";
    exit();
}

// Verify Project Ownership
$checkStmt = $conn->prepare("SELECT id FROM projects WHERE id = ? AND contractor_id = ?");
$checkStmt->bind_param("ii", $project_id, $contractor_id);
$checkStmt->execute();
$checkStmt->store_result();

if ($checkStmt->num_rows == 0) {
    echo "error|Invalid Project";
    $checkStmt->close();
    exit();
}
$checkStmt->close();

// Generate Token
$token = bin2hex(random_bytes(16));
$invite_link = "client_register.php?token=" . $token;

// Save to DB
$stmt = $conn->prepare("INSERT INTO project_invites (project_id, token, status) VALUES (?, ?, 'pending')");
if (!$stmt) {
    echo "error|Database Error";
    exit();
}

$stmt->bind_param("is", $project_id, $token);

if ($stmt->execute()) {
    echo "success|" . $token;
} else {
    echo "error|Failed to save invite";
}

$stmt->close();
$conn->close();
?>
