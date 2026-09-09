<?php
include 'koneksi2.php';
date_default_timezone_set('Asia/Jakarta');
header('Content-Type: application/json');
header('Cache-Control: no-store, no-cache');

// Gunakan range tanggal agar MySQL bisa manfaatkan index pada created_at
// (WHERE DATE(col) = ? tidak bisa pakai index, WHERE col BETWEEN ? AND ? bisa)
$tglStart = date('Y-m-d') . ' 00:00:00';
$tglEnd   = date('Y-m-d') . ' 23:59:59';

try {
    // 1 query gabungan untuk statistik total/menunggu/selesai (non-FARMASI)
    $stmtStat = $pdo_simrs->prepare("
        SELECT
            COUNT(*) AS total,
            SUM(CASE WHEN status = 'Menunggu' THEN 1 ELSE 0 END) AS menunggu,
            SUM(CASE WHEN status = 'Selesai'  THEN 1 ELSE 0 END) AS selesai
        FROM antrian_wira
        WHERE created_at BETWEEN ? AND ?
          AND jenis != 'FARMASI'
    ");
    $stmtStat->execute([$tglStart, $tglEnd]);
    $stat = $stmtStat->fetch(PDO::FETCH_ASSOC);

    // Query terpisah untuk data panggilan terakhir (butuh JOIN)
    $stmtCall = $pdo_simrs->prepare("
        SELECT a.nomor, a.waktu_panggil, a.no_rkm_medis,
               l.nama_loket, p.nm_pasien
        FROM antrian_wira a
        LEFT JOIN loket_admisi_wira l ON a.loket_id = l.id
        LEFT JOIN pasien p ON a.no_rkm_medis = p.no_rkm_medis
        WHERE a.created_at BETWEEN ? AND ?
          AND a.status = 'Dipanggil'
          AND a.jenis != 'FARMASI'
        ORDER BY a.waktu_panggil DESC
        LIMIT 1
    ");
    $stmtCall->execute([$tglStart, $tglEnd]);
    $current = $stmtCall->fetch(PDO::FETCH_ASSOC);

    echo json_encode([
        'nomor'           => $current['nomor']    ?? '',
        'loket'           => $current['nama_loket'] ?? '',
        'nm_pasien'       => $current['nm_pasien'] ?? '',
        'nm_pasien_suara' => normalizeNamaPasien($current['nm_pasien'] ?? ''),
        'total'           => (int)($stat['total']    ?? 0),
        'menunggu'        => (int)($stat['menunggu'] ?? 0),
        'selesai'         => (int)($stat['selesai']  ?? 0),
        'waktu_panggil'   => $current['waktu_panggil'] ?? '',
    ]);

} catch (PDOException $e) {
    echo json_encode([
        'nomor' => '', 'loket' => '', 'nm_pasien' => '',
        'nm_pasien_suara' => '', 'total' => 0, 'menunggu' => 0,
        'selesai' => 0, 'waktu_panggil' => '',
        'error' => $e->getMessage()
    ]);
}
?>
