<?php
session_start();
include 'koneksi2.php';
date_default_timezone_set('Asia/Jakarta');

if (!isset($_SESSION['user_id'])) {
    http_response_code(401);
    exit('Unauthorized');
}

$id    = $_POST['id'] ?? 0;
$loket = $_POST['loket_id'] ?? 1; // ✅ ganti ke loket_id agar sesuai dengan form
$rm    = $_POST['rm'] ?? '';

if ($id > 0) {
    $stmt = $pdo_simrs->prepare("
        UPDATE antrian_wira 
        SET status='Dipanggil', waktu_panggil=NOW(), loket_id=?, no_rkm_medis=? 
        WHERE id=?
    ");
    $stmt->execute([$loket, $rm, $id]);
    echo "Nomor antrian $id dipanggil ke loket $loket (RM: $rm)";
} else {
    echo "ID tidak valid.";
}
?>
