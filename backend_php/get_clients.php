<?php
include 'db.php';

$contractor_id = $_POST['contractor_id'] ?? $_GET['contractor_id'] ?? null;

if (!$contractor_id) {
    echo json_encode([]); // Return empty JSON array on missing ID
    exit();
}

$sql = "SELECT id, client_name AS name, email, phone FROM clients WHERE contractor_id = '$contractor_id'";
$result = $conn->query($sql);

$clients = array();

if ($result) {
    while ($row = $result->fetch_assoc()) {
        $clients[] = $row;
    }
}

echo json_encode($clients);
$conn->close();
?>
