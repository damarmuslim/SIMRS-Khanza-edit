<?php
session_start();
if (!isset($_SESSION['user_id'])) {
    http_response_code(401);
    exit('Unauthorized');
}
include 'koneksi2.php';

echo "--- TABLES LIKE %resep% ---\n";
$stmt = $pdo_simrs->query("SHOW TABLES LIKE '%resep%'");
while($row = $stmt->fetch()) {
    echo $row[0] . "\n";
}

echo "\n--- COLUMNS OF resep_obat ---\n";
$stmt = $pdo_simrs->query("DESC resep_obat");
while($row = $stmt->fetch(PDO::FETCH_ASSOC)) {
    echo $row['Field'] . " (" . $row['Type'] . ")\n";
}

echo "\n--- COLUMNS OF telaah_resep (if exists) ---\n";
try {
    $stmt = $pdo_simrs->query("DESC telaah_resep");
    while($row = $stmt->fetch(PDO::FETCH_ASSOC)) {
        echo $row['Field'] . " (" . $row['Type'] . ")\n";
    }
} catch (Exception $e) { echo "telaah_resep not found\n"; }

echo "\n--- COLUMNS OF verifikasi_resep (if exists) ---\n";
try {
    $stmt = $pdo_simrs->query("DESC verifikasi_resep");
    while($row = $stmt->fetch(PDO::FETCH_ASSOC)) {
        echo $row['Field'] . " (" . $row['Type'] . ")\n";
    }
} catch (Exception $e) { echo "verifikasi_resep not found\n"; }
