<?php
error_reporting(0);
include("db.php");

$contractor_id = $_POST['contractor_id'] ?? '';
$project_id    = $_POST['project_id'] ?? '';
$client_id     = $_POST['client_id'] ?? '';
$title         = $_POST['title'] ?? '';
$description   = $_POST['description'] ?? '';
$amount        = $_POST['amount'] ?? '';

if ($contractor_id === '' || $project_id === '' || $client_id === '' || $amount === '') {
    echo "error|Missing fields";
    exit();
}

$sql = "INSERT INTO quotations (contractor_id, project_id, client_id, title, description, amount, created_at) 
        VALUES ('$contractor_id', '$project_id', '$client_id', '$title', '$description', '$amount', NOW())";

if (mysqli_query($conn, $sql)) {
    echo "success";
} else {
    echo "error|Database error: " . mysqli_error($conn);
}
?>
