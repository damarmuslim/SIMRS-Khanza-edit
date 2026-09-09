<?php
session_start();
include 'koneksi.php';

if (!isset($_SESSION['user_id'])) {
    http_response_code(401);
    exit('Unauthorized');
}

try {
    $sql = "INSERT IGNORE INTO feature_control (kode_fitur, nama_fitur, status) VALUES 
            ('antri_farmasi', 'Antri Farmasi', 1),
            ('kontrol_rajal', 'Surat Kontrol Rajal', 0),
            ('kontrol_ranap', 'Surat Kontrol Ranap', 0)";
    $pdo->exec($sql);
    echo "✔ Fitur berhasil ditambahkan ke database.";
} catch (Exception $e) {
    echo "✖ Gagal: " . $e->getMessage();
}
?>
