<?php
session_start();
if (!isset($_SESSION['user_id'])) {
    http_response_code(401);
    exit('Unauthorized');
}
include 'koneksi2.php';

$tables = ['bridging_surat_kontrol_bpjs', 'skdp_bpjs', 'pasien'];

foreach ($tables as $table) {
    echo "<h3>Columns in $table:</h3>";
    try {
        $stmt = $pdo_simrs->query("SHOW COLUMNS FROM $table");
        while($row = $stmt->fetch(PDO::FETCH_ASSOC)) {
            echo $row['Field'] . " (" . $row['Type'] . ")<br>";
        }
    } catch (Exception $e) {
        echo "Table $table not found or error: " . $e->getMessage() . "<br>";
    }
}
?>
