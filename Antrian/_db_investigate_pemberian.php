<?php
session_start();
if (!isset($_SESSION['user_id'])) {
    http_response_code(401);
    exit('Unauthorized');
}
include 'koneksi2.php';

$tables = [];
$stmt = $pdo_simrs->query("SHOW TABLES LIKE '%pemberian%'");
while($row = $stmt->fetch()) {
    $tables[] = $row[0];
}

echo "Tables found: " . implode(", ", $tables) . "\n\n";

foreach ($tables as $t) {
    echo "--- Columns of $t ---\n";
    $stmt2 = $pdo_simrs->query("DESC $t");
    while($row = $stmt2->fetch(PDO::FETCH_ASSOC)) {
        echo $row['Field'] . " (" . $row['Type'] . ")\n";
    }
    echo "\n";
}
