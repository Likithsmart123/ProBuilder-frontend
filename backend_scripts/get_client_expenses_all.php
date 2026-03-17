<?php
ini_set('display_errors', 0);
error_reporting(0);
header('Content-Type: application/json');

require 'db.php';

$headers = getallheaders();
$token = $headers['Authorization'] ?? '';

if (!$token) {
    echo json_encode(["status" => "error", "message" => "Unauthorized"]);
    exit;
}

// Validate Token
$stmt = $conn->prepare("SELECT id FROM clients WHERE api_token = ?");
$stmt->bind_param("s", $token);
$stmt->execute();
$result = $stmt->get_result();

if ($result->num_rows === 0) {
    echo json_encode(["status" => "error", "message" => "Unauthorized"]);
    exit;
}

$client = $result->fetch_assoc();
$client_id = $client['id'];
$stmt->close();

// 1. Calculate Totals
$sqlTotals = "SELECT 
                SUM(p.budget) as total_budget,
                (SELECT SUM(e.amount) 
                 FROM expenses e 
                 JOIN projects p2 ON e.project_id = p2.id 
                 WHERE p2.client_id = ?) as total_spent
              FROM projects p
              WHERE p.client_id = ?";

$totals = ['total_budget' => 0, 'total_spent' => 0];

$stmt = $conn->prepare($sqlTotals);
if ($stmt) {
    $stmt->bind_param("ii", $client_id, $client_id);
    $stmt->execute();
    $result = $stmt->get_result();
    $fetched = $result->fetch_assoc();
    if($fetched) {
        $totals = $fetched;
    }
    $stmt->close();
}

$total_budget = $totals['total_budget'] ?? 0;
$total_spent = $totals['total_spent'] ?? 0;
$remaining = floatval($total_budget) - floatval($total_spent);

// 2. Breakdown
$sqlItems = "SELECT category, SUM(amount) as total_amount
             FROM expenses e
             JOIN projects p ON e.project_id = p.id
             WHERE p.client_id = ?
             GROUP BY category
             ORDER BY total_amount DESC";

$items = [];
$stmtItems = $conn->prepare($sqlItems);
if ($stmtItems) {
    $stmtItems->bind_param("i", $client_id);
    $stmtItems->execute();
    $resultItems = $stmtItems->get_result();
    while ($row = $resultItems->fetch_assoc()) {
        $items[] = $row;
    }
    $stmtItems->close();
}

echo json_encode([
    "total_budget" => $total_budget,
    "total_spent" => $total_spent,
    "remaining_amount" => $remaining,
    "breakdown" => $items
]);
exit;
?>
