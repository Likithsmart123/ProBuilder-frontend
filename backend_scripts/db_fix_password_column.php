<?php
// Fix for Password Hash Truncation
// This script checks the 'password_hash' column in 'clients' table and resizes it to VARCHAR(255)

header('Content-Type: application/json');

// Define connection manually to avoid path issues if db.php isn't found relative to this script
$servername = "localhost";
$username = "root";
$password = "";
$dbname = "probuilder_db"; // Assuming this is the DB name based on context, will default to try including db.php first

// Try to include db.php if available in the same directory (when copied to htdocs)
if (file_exists('db.php')) {
    require 'db.php';
} elseif (file_exists('../db.php')) {
    require '../db.php';
} else {
    // Fallback connection
    $conn = new mysqli($servername, $username, $password, $dbname);
    if ($conn->connect_error) {
        die(json_encode(["status" => "error", "message" => "Connection failed: " . $conn->connect_error]));
    }
}

$table = 'clients';
$column = 'password_hash';

echo "Checking schema for table '$table'...\n";

// 1. Check current column type
$checkSql = "SHOW COLUMNS FROM $table LIKE '$column'";
$result = $conn->query($checkSql);

if ($result && $row = $result->fetch_assoc()) {
    $currentType = $row['Type'];
    echo "Current type: $currentType\n";
    
    // Check if it's already VARCHAR(255)
    if (strpos($currentType, 'varchar(255)') !== false) {
        echo "Column is already VARCHAR(255). No changes needed.\n";
    } else {
        // 2. Modify Column
        echo "Resizing '$column' to VARCHAR(255)...\n";
        $alterSql = "ALTER TABLE $table MODIFY $column VARCHAR(255)";
        if ($conn->query($alterSql) === TRUE) {
            echo "SUCCESS: Column '$column' resized to VARCHAR(255).\n";
        } else {
            echo "ERROR: Failed to resize column: " . $conn->error . "\n";
        }
    }
} else {
    echo "ERROR: Column '$column' not found in table '$table'.\n";
}

$conn->close();
?>
