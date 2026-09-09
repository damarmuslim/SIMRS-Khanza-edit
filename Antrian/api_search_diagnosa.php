<?php
session_start();
include 'koneksi2.php';

$q = $_GET['q'] ?? '';
if (strlen($q) < 3) {
    echo json_encode([]);
    exit;
}

$stmt = $pdo_simrs->prepare("SELECT kd_penyakit, nm_penyakit FROM penyakit 
                             WHERE kd_penyakit LIKE ? OR nm_penyakit LIKE ? 
                             LIMIT 20");
$stmt->execute(["%$q%", "%$q%"]);
$results = $stmt->fetchAll();

header('Content-Type: application/json');
echo json_encode($results);
?>
