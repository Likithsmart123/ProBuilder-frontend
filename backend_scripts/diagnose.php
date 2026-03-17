<?php
ini_set('display_errors', 1);
error_reporting(E_ALL);
header('Content-Type: text/plain');

echo "--- Diagnostic Start ---\n";
echo "PHP Version: " . phpversion() . "\n";

echo "Checking JSON extension... ";
if (function_exists('json_decode')) {
    echo "OK\n";
} else {
    echo "MISSING\n";
}

echo "Checking MySQLi extension... ";
if (class_exists('mysqli')) {
    echo "OK\n";
} else {
    echo "MISSING\n";
}

echo "Checking Random Bytes... ";
if (function_exists('random_bytes')) {
    echo "OK\n";
} else {
    echo "MISSING (This will cause 500 in login)\n";
}

echo "Checking Null Coalescing Operator (??)... ";
try {
   eval('$a = null ?? 1;');
   echo "OK\n";
} catch (Throwable $t) {
   echo "ERROR (PHP < 7.0?)\n";
}

echo "Checking Database Connection...\n";
if (file_exists('db.php')) {
    require 'db.php';
    if ($conn) {
        echo "DB Connection: SUCCESS\n";
    } else {
        echo "DB Connection: FAILED\n";
    }
} else {
    echo "db.php NOT FOUND in current directory.\n";
}

echo "--- Diagnostic End ---\n";
?>
