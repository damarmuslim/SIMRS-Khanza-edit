<?php
session_start();
if (!isset($_SESSION['user_id'])) {
    http_response_code(401);
    exit('Unauthorized');
}
include 'koneksi2.php';

$tables = [];
$stmt = $pdo_simrs->query("SHOW TABLES LIKE '%resep%'");
while($row = $stmt->fetch()) {
    $tables[] = $row[0];
}

echo "Tables found: " . implode(", ", $tables) . "\n\n";

foreach (['resep_obat', 'telaah_resep', 'verifikasi_resep'] as $t) {
    if (in_array($t, $tables)) {
        echo "--- Columns of $t ---\n";
        $stmt2 = $pdo_simrs->query("DESC $t");
        while($row = $stmt2->fetch(PDO::FETCH_ASSOC)) {
            echo $row['Field'] . " (" . $row['Type'] . ")\n";
        }
        echo "\n";
    } else {
        echo "$t not found\n\n";
    }
}
