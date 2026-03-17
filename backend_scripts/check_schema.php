<?php
header('Content-Type: text/plain');
require '../backend_php/db.php';

function describeTable($conn, $tableName) {
    echo "--- Table: $tableName ---\n";
    $result = $conn->query("DESCRIBE $tableName");
    if ($result) {
        while ($row = $result->fetch_assoc()) {
            echo $row['Field'] . " (" . $row['Type'] . ")\n";
        }
    } else {
        echo "Error: " . $conn->error . "\n";
    }
    echo "\n";
}

describeTable($conn, 'projects');
describeTable($conn, 'clients');
?>
