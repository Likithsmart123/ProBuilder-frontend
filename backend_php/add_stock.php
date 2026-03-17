<?php
error_reporting(E_ALL);
ini_set('display_errors', 1);

include 'db.php';

$material_id = $_POST['material_id'] ?? '';
$quantity    = $_POST['quantity'] ?? '';

if ($material_id === '' || $quantity === '') {
    echo "missing";
    exit();
}

if (!is_numeric($quantity) || $quantity <= 0) {
    echo "invalid_quantity";
    exit();
}

// begin transaction
mysqli_begin_transaction($conn);

// update stock
$u1 = mysqli_query($conn,
    "UPDATE materials
     SET current_stock = current_stock + $quantity
     WHERE id = '$material_id'"
);

// log transaction
$u2 = mysqli_query($conn,
    "INSERT INTO material_transactions (material_id, type, quantity)
     VALUES ('$material_id', 'ADD', '$quantity')"
);

if ($u1 && $u2) {
    mysqli_commit($conn);
    echo "success";
} else {
    mysqli_rollback($conn);
    echo "error";
}

$conn->close();
