<?php
ini_set('display_errors', 0);
ini_set('log_errors', 1);
ini_set('error_log', __DIR__ . '/php_error.log');
error_reporting(E_ALL);
header('Content-Type: application/json');

require 'db.php';

// Log raw POST data for debugging
file_put_contents(__DIR__ . "/post_debug.txt", print_r($_POST, true));

// Retrieve params - supporting both JSON (if sent) and POST (standard)
$inputJSON = file_get_contents('php://input');
$input = json_decode($inputJSON, TRUE);

$email = null;
if (isset($_POST['email'])) {
    $email = $_POST['email'];
} elseif (isset($input['email'])) {
    $email = $input['email'];
}

$password = null;
if (isset($_POST['password'])) {
    $password = $_POST['password'];
} elseif (isset($input['password'])) {
    $password = $input['password'];
}

if (!$email || !$password) {
    echo json_encode(array("status" => "error", "message" => "Missing credentials"));
    exit;
}

// CORRECT QUERY: Check 'password_hash' column
$sql = "SELECT id, password_hash FROM clients WHERE email = ?";
$stmt = $conn->prepare($sql);

if ($stmt) {
    $stmt->bind_param("s", $email);
    $stmt->execute();
    $result = $stmt->get_result();
    
    if ($user = $result->fetch_assoc()) {
        // CORRECT VERIFICATION: Use password_verify with hash
        if (password_verify($password, $user['password_hash'])) {
            
            // Generate Token
            $token = "";
            if (function_exists('random_bytes')) {
                try {
                    $token = bin2hex(random_bytes(32));
                } catch (Exception $e) {
                    $token = bin2hex(openssl_random_pseudo_bytes(32));
                }
            } elseif (function_exists('openssl_random_pseudo_bytes')) {
                $token = bin2hex(openssl_random_pseudo_bytes(32));
            } else {
                $token = md5(uniqid(rand(), true));
            }
            
            // Save Token in DB
            $updateSql = "UPDATE clients SET api_token = ? WHERE id = ?";
            $updateStmt = $conn->prepare($updateSql);
            $updateStmt->bind_param("si", $token, $user['id']);
            $updateStmt->execute();
            $updateStmt->close();
            
            echo json_encode(array(
                "status" => "success", 
                "message" => "Login successful",
                "client_id" => $user['id'],
                "token" => $token,
                "role" => "client"
            ));
        } else {
            echo json_encode(array("status" => "error", "message" => "Invalid password"));
        }
    } else {
        echo json_encode(array("status" => "error", "message" => "Client not found"));
    }
    $stmt->close();
} else {
    // Log DB error for internal debugging
    error_log("Database error: " . $conn->error);
    echo json_encode(array("status" => "error", "message" => "Database error"));
}
exit;
?>
