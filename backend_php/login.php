<?php
error_reporting(0);
include("db.php");

$email    = $_POST['email'] ?? '';
$password = $_POST['password'] ?? '';

if ($email === '' || $password === '') {
    echo "missing";
    exit();
}

// 🔥 USE users TABLE, NOT contractors
$sql = "SELECT id, name, password FROM users WHERE email='$email' AND role='contractor' LIMIT 1";
$res = mysqli_query($conn, $sql);

if (!$res || mysqli_num_rows($res) === 0) {
    echo "invalid";
    exit();
}

$row = mysqli_fetch_assoc($res);

if (!password_verify($password, $row['password'])) {
    echo "invalid";
    exit();
}

// Return id and name for session handling
echo "success|" . $row['id'] . "|" . $row['name'];
