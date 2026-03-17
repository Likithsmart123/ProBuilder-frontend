<?php
ini_set('display_errors', 0);
error_reporting(0);
header('Content-Type: application/json');

require 'db.php';

// Standardized Auth Header Retrieval
$headers = null;
if (function_exists('getallheaders')) {
    $headers = getallheaders();
}
$token = '';

// 1. Try to get token from headers
if ($headers && isset($headers['Authorization'])) {
    $token = $headers['Authorization'];
} 
// 2. Try case-insensitive header lookup if primary failed
if (!$token && $headers) {
    $headers_lower = array_change_key_case($headers, CASE_LOWER);
    if (isset($headers_lower['authorization'])) {
        $token = $headers_lower['authorization'];
    }
}
// 3. Fallback to $_SERVER
if (!$token && isset($_SERVER['HTTP_AUTHORIZATION'])) {
    $token = $_SERVER['HTTP_AUTHORIZATION'];
}

if (!$token) {
    http_response_code(401); // Set HTTP 401 code
    echo json_encode(["status" => "error", "message" => "Unauthorized: Missing Token"]);
    exit;
}

// Validate Token
$stmt = $conn->prepare("SELECT id FROM clients WHERE api_token = ?");
$stmt->bind_param("s", $token);
$stmt->execute();
$result = $stmt->get_result();

if ($result->num_rows === 0) {
    http_response_code(401);
    echo json_encode(["status" => "error", "message" => "Unauthorized: Invalid Token"]);
    exit;
}

$client = $result->fetch_assoc();
$client_id = $client['id'];
$stmt->close();

// Fetch Quotations
$sql = "SELECT q.id, q.title, q.description, q.amount, q.status, q.created_at, p.project_name
        FROM quotations q
        JOIN projects p ON q.project_id = p.id
        WHERE p.client_id = ?
        ORDER BY q.created_at DESC";

$stmt = $conn->prepare($sql);
if ($stmt) {
    $stmt->bind_param("i", $client_id);
    $stmt->execute();
    $result = $stmt->get_result();
    
    $quotations = [];
    while ($row = $result->fetch_assoc()) {
        $quotations[] = $row;
    }
    
    // Return Object with "quotations" key as expected by Android JsonObjectRequest
    echo json_encode([
        "status" => "success",
        "quotations" => $quotations
    ]);
    
    $stmt->close();
} else {
    echo json_encode(["status" => "error", "message" => "Database error"]);
}
exit;
?>
