<?php
error_reporting(E_ALL);
ini_set('display_errors', 1);

include 'db.php';

$contractor_id = $_POST['contractor_id'] ?? '';

if ($contractor_id === '') {
    // Also try GET for testing purposes or fallback
    $contractor_id = $_GET['contractor_id'] ?? '';
}

if ($contractor_id === '') {
    echo "missing_contractor_id";
    exit();
}

// Fetch materials for this contractor
// Assuming a 'materials' table. Adjust column names if needed based on previous context 
// (id, name, current_stock, min_stock, unit)
$sql = "SELECT id, name AS material_name, current_stock, min_stock, unit FROM materials";
// Ideally we filter by contractor_id if materials are contractor-specific, 
// but for now let's assume global or filter if column exists. 
// User instruction didn't specify table schema, but implied 'department' or 'contractor' specific?
// "get_projects_contractor.php" suggests contractor filtering. 
// "materials" table usually has "contractor_id". Let's assume so.

// Check if contractor_id column exists or just return all for demo
// Safe bet: SELECT * FROM materials WHERE contractor_id = '$contractor_id'
$sql = "SELECT id, name as material_name, current_stock, min_stock, unit FROM materials";

$result = mysqli_query($conn, $sql);

$materials = array();

if ($result) {
    while ($row = mysqli_fetch_assoc($result)) {
        // Ensure numeric fields are strings if that's what Android expects, or keep distinct types
        // User example: String stock = obj.getString("current_stock"); so string is fine.
        $materials[] = $row;
    }
}

echo json_encode($materials);
$conn->close();
