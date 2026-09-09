<?php
session_start();
if (!isset($_SESSION['user_id'])) {
    http_response_code(401);
    exit('Unauthorized');
}
include 'koneksi.php'; // Ambil pdo untuk cek setting
include 'koneksi2.php';

try {
    // 1. Cek Setting yang Terdaftar di Database 'anjungan'
    echo "<h3>1. Konfigurasi di Database 'anjungan'</h3>";
    $stmt_cfg = $pdo->query("SELECT * FROM setting_simrs LIMIT 1");
    $cfg = $stmt_cfg->fetch(PDO::FETCH_ASSOC);
    if ($cfg) {
        echo "Host di Setting: <b>" . $cfg['host'] . "</b><br>";
        echo "Database di Setting: <b>" . $cfg['database_name'] . "</b><br>";
        echo "User di Setting: <b>" . $cfg['username'] . "</b><br>";
    } else {
        echo "<b style='color:red'>Data setting_simrs tidak ditemukan!</b><br>";
    }

    // 2. Cek Koneksi SIMRS Aktif
    echo "<h3>2. Diagnosis Koneksi SIMRS Saat Ini</h3>";
    echo "Status Koneksi SIMRS: <b style='color:green'>Terhubung</b><br>";
    
    // Hitung data di referensi_mobilejkn_bpjs
    $stmt = $pdo_simrs->query("SELECT COUNT(*) FROM referensi_mobilejkn_bpjs");
    $count = $stmt->fetchColumn();
    echo "Jumlah data di tabel <code>referensi_mobilejkn_bpjs</code>: <b>" . $count . "</b><br>";
    
    // Tampilkan 5 data terakhir
    echo "<h4>5 Antrean Terakhir:</h4>";
    $stmt2 = $pdo_simrs->query("SELECT nobooking, tanggalperiksa, status, validasi FROM referensi_mobilejkn_bpjs ORDER BY validasi DESC LIMIT 5");
    $rows = $stmt2->fetchAll(PDO::FETCH_ASSOC);
    
    echo "<table border='1' cellpadding='5' style='font-size:12px;'>";
    echo "<tr><th>No. Booking</th><th>Tgl Periksa</th><th>Status</th><th>Validasi (Time)</th></tr>";
    foreach($rows as $r) {
        echo "<tr><td>{$r['nobooking']}</td><td>{$r['tanggalperiksa']}</td><td>{$r['status']}</td><td>{$r['validasi']}</td></tr>";
    }
    echo "</table>";

} catch (Exception $e) {
    echo "<h3>Diagnosis Gagal</h3>";
    echo "<b style='color:red'>Error:</b> " . $e->getMessage();
}
?>
