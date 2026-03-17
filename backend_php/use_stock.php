<?php
error_reporting(E_ALL);
ini_set('display_errors', 1);
include("db.php");

$material_id = $_POST['material_id'] ?? '';
$qty = $_POST['quantity'] ?? '';

if ($material_id === '' || $qty === '') {
    echo "missing";
    exit();
}

// check current stock
$res = mysqli_query($conn,
    "SELECT current_stock FROM materials WHERE id='$material_id'"
);

$row = mysqli_fetch_assoc($res);

if ($row['current_stock'] < $qty) {
    echo "insufficient";
    exit();
}

mysqli_begin_transaction($conn);

// reduce stock
$u1 = mysqli_query($conn,
    "UPDATE materials
     SET current_stock = current_stock - $qty
     WHERE id = '$material_id'"
);

// log usage
$u2 = mysqli_query($conn,
    "INSERT INTO material_transactions (material_id, type, quantity)
     VALUES ('$material_id', 'USE', '$qty')"
);

if ($u1 && $u2) {
    mysqli_commit($conn);
    echo "success";
} else {
    mysqli_rollback($conn);
    echo "error";
}
