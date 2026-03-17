<?php
include 'db.php';

header('Content-Type: application/json');

$contractor_id = isset($_POST['contractor_id']) ? trim($_POST['contractor_id']) : '';

if (!$contractor_id) {
    echo json_encode([]);
    exit();
}

// Ensure the contractor_id is safe to use in query
$contractor_id = mysqli_real_escape_string($conn, $contractor_id);

$sql = "SELECT id, project_name FROM projects WHERE contractor_id = '$contractor_id'";
$result = $conn->query($sql);

$projects = array();

if ($result) {
    while ($row = $result->fetch_assoc()) {
        $projects[] = $row;
    }
}

echo json_encode($projects);
$conn->close();
?>
