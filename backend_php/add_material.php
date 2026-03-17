<?php
include 'db.php';

$contractor_id = $_POST['contractor_id'] ?? '';
$material_name = $_POST['material_name'] ?? '';
$unit = $_POST['unit'] ?? '';
$min_stock = $_POST['min_stock'] ?? '';

if (empty($contractor_id) || empty($material_name) || empty($unit) || empty($min_stock)) {
    echo "missing_params";
    exit;
}

// Check if material already exists
$check_sql = "SELECT id FROM materials WHERE name = '$material_name'";
$check_result = mysqli_query($conn, $check_sql);

if (mysqli_num_rows($check_result) > 0) {
    echo "exists";
} else {
    // Insert new material
    $insert_sql = "INSERT INTO materials (name, unit, min_stock, current_stock, contractor_id) VALUES ('$material_name', '$unit', '$min_stock', 0, '$contractor_id')"; // Initial stock 0
    if (mysqli_query($conn, $insert_sql)) {
        echo "success";
    } else {
        echo "error";
    }
}

mysqli_close($conn);
?>
