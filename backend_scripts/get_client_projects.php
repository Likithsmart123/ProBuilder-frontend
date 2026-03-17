<?php
ini_set('display_errors', 0);
ini_set('log_errors', 1);
ini_set('error_log', __DIR__ . '/php_error.log');
error_reporting(E_ALL);
header('Content-Type: application/json');

require 'db.php';

// Helper function to get headers (robust)
function get_request_headers() {
    $headers = array();
    foreach ($_SERVER as $key => $value) {
        if (substr($key, 0, 5) <> 'HTTP_') {
            continue;
        }
        $header = str_replace(' ', '-', ucwords(str_replace('_', ' ', strtolower(substr($key, 5)))));
        $headers[$header] = $value;
    }
    // Add Authorization specifically if missing (sometimes Apache eats it)
    if (!isset($headers['Authorization']) && isset($_SERVER['Authorization'])) {
        $headers['Authorization'] = $_SERVER['Authorization'];
    }
    return $headers;
}

// Fetch headers safe way
if (function_exists('getallheaders')) {
    $request_headers = getallheaders();
} else {
    $request_headers = get_request_headers();
}

// Case-insensitive token lookup
$token = '';
if (isset($request_headers['Authorization'])) {
    $token = $request_headers['Authorization'];
} elseif (isset($request_headers['authorization'])) {
    $token = $request_headers['authorization'];
}

// Log for debugging
file_put_contents(__DIR__ . "/project_debug.txt", "Token received: " . $token . "\n", FILE_APPEND);

if (!$token) {
    echo json_encode(array("status" => "error", "message" => "Unauthorized - No Token"));
    exit;
}

// Validate Token and Get Client ID
$stmt = $conn->prepare("SELECT id FROM clients WHERE api_token = ?");
$stmt->bind_param("s", $token);
$stmt->execute();
$result = $stmt->get_result();

if ($result->num_rows === 0) {
    echo json_encode(array("status" => "error", "message" => "Unauthorized - Invalid Token"));
    exit;
}

$client = $result->fetch_assoc();
$client_id = $client['id'];
$stmt->close();

// Fetch Projects
$sql = "SELECT id, project_name, location, status, overall_progress, start_date, end_date
        FROM projects
        WHERE client_id = ?
        ORDER BY created_at DESC";

$stmt = $conn->prepare($sql);
if ($stmt) {
    $stmt->bind_param("i", $client_id);
    $stmt->execute();
    $result = $stmt->get_result();
    
    $response = array();
    while ($row = $result->fetch_assoc()) {
        $response[] = $row;
    }
    echo json_encode($response);
    $stmt->close();
} else {
    // Log real DB error
    error_log("DB Error in get_client_projects: " . $conn->error);
    echo json_encode(array("status" => "error", "message" => "Database error"));
}
exit;
?>
