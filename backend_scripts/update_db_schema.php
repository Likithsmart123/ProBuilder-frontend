<?php
ini_set('display_errors', 0); // Hide errors to keeping output clean (optional, but good practice here)
error_reporting(E_ALL); 
require 'db.php';

// Check if column exists
$checkColumn = "SHOW COLUMNS FROM `clients` LIKE 'api_token'";
$result = mysqli_query($conn, $checkColumn);

if (mysqli_num_rows($result) == 0) {
    // Column doesn't exist, add it
    $sql = "ALTER TABLE `clients` ADD `api_token` VARCHAR(64) UNIQUE DEFAULT NULL";
    if (mysqli_query($conn, $sql)) {
        echo "Successfully added 'api_token' column to 'clients' table.";
    } else {
        echo "Error adding column: " . mysqli_error($conn);
    }
} else {
    echo "Column 'api_token' already exists.";
}

mysqli_close($conn);
?>
