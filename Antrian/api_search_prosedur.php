<?php
session_start();
include 'koneksi2.php';

$q = $_GET['q'] ?? '';
if (strlen($q) < 3) {
    echo json_encode([]);
    exit;
}

$stmt = $pdo_simrs->prepare("SELECT kode, deskripsi_panjang FROM icd9 
                             WHERE kode LIKE ? OR deskripsi_panjang LIKE ? 
                             LIMIT 20");
$stmt->execute(["%$q%", "%$q%"]);
$results = $stmt->fetchAll();

header('Content-Type: application/json');
echo json_encode($results);
?>
