<?php
/**
 * get_current_call.php
 * Endpoint ringan untuk polling display antrian poli.
 * Mengembalikan JSON panggilan terbaru hari ini.
 *
 * Query params:
 *   ?poli=   filter kd_poli (opsional)
 *   ?dokter= filter kd_dokter (opsional)
 *   ?since=  timestamp unix — hanya kembalikan data jika ada panggilan SETELAH waktu ini
 */
include 'koneksi2.php';
date_default_timezone_set('Asia/Jakarta');
header('Content-Type: application/json');
header('Cache-Control: no-store, no-cache');

$tgl    = date('Y-m-d');
$poli   = trim($_GET['poli']   ?? '');
$dokter = trim($_GET['dokter'] ?? '');
$since  = intval($_GET['since'] ?? 0);

try {
    // Gunakan kolom tgl_panggil langsung (bukan DATE()) agar index bisa dipakai
    $sql = "
        SELECT
            s.no_rawat, s.no_rkm_medis, s.no_antrian,
            s.nm_pasien, s.nm_poli, s.nm_dokter,
            s.kd_poli, s.kd_dokter, s.jml_panggil,
            UNIX_TIMESTAMP(s.updated_at) AS ts
        FROM simpan_antrian_poli_wira s
        WHERE s.tgl_panggil = ?
    ";
    $params = [$tgl];

    if (!empty($poli)) {
        $sql .= " AND s.kd_poli = ?";
        $params[] = $poli;
    }
    if (!empty($dokter)) {
        $sql .= " AND s.kd_dokter = ?";
        $params[] = $dokter;
    }

    $sql .= " ORDER BY s.updated_at DESC LIMIT 1";

    $stmt = $pdo_simrs->prepare($sql);
    $stmt->execute($params);
    $row = $stmt->fetch(PDO::FETCH_ASSOC);

    if (!$row) {
        echo json_encode(['has_call' => false, 'ts' => 0]);
        exit;
    }

    $ts = (int)$row['ts'];

    // Jika data tidak berubah sejak request terakhir, balas ringkas
    if ($since > 0 && $ts <= $since) {
        echo json_encode(['has_call' => true, 'changed' => false, 'ts' => $ts]);
        exit;
    }

    echo json_encode([
        'has_call'        => true,
        'changed'         => true,
        'ts'              => $ts,
        'no_antrian'      => $row['no_antrian'],
        'no_rawat'        => $row['no_rawat'],
        'nm_pasien'       => $row['nm_pasien'],
        'nm_pasien_suara' => normalizeNamaPasien($row['nm_pasien']),
        'nm_poli'         => $row['nm_poli'],
        'nm_dokter'       => $row['nm_dokter'],
        'kd_poli'         => $row['kd_poli'],
        'kd_dokter'       => $row['kd_dokter'],
        'jml_panggil'     => (int)$row['jml_panggil'],
    ]);

} catch (PDOException $e) {
    echo json_encode(['has_call' => false, 'error' => $e->getMessage()]);
}
?>
