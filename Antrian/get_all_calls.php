<?php
/**
 * get_all_calls.php
 * Digunakan oleh display_poli.php untuk polling live semua panggilan per dokter
 *
 * Dulu: 1 query dokter + 2 query per dokter (N+1), dipanggil tiap 2 detik oleh
 * setiap layar TV yang terbuka -> full table scan reg_periksa berulang-ulang.
 * Sekarang: rata query per dokter, ikut pola "1 query bukan N query" yang
 * sudah dipakai display_poli.php untuk render awal.
 */
include 'koneksi2.php';
date_default_timezone_set('Asia/Jakarta');
header('Content-Type: application/json');

$today  = date('Y-m-d');
$poli   = $_GET['poli']   ?? '';
$dokter = $_GET['dokter'] ?? '';

$excluded_poli = ['IGDK','PL013','PL014','PL015','PL016','PL017','U0022','U0030'];
$excluded_list = "'" . implode("','", $excluded_poli) . "'";

try {
    // Ambil semua dokter yang ada antrian hari ini
    $sqlDokter = "
        SELECT DISTINCT r.kd_dokter, r.kd_poli, d.nm_dokter, p.nm_poli
        FROM reg_periksa r
        LEFT JOIN dokter d ON r.kd_dokter = d.kd_dokter
        LEFT JOIN poliklinik p ON r.kd_poli = p.kd_poli
        WHERE r.tgl_registrasi = ?
          AND r.kd_poli NOT IN ($excluded_list)
    ";
    $params = [$today];
    if (!empty($poli))   { $sqlDokter .= " AND r.kd_poli = ?";   $params[] = $poli; }
    if (!empty($dokter)) { $sqlDokter .= " AND r.kd_dokter = ?"; $params[] = $dokter; }

    $stmtD = $pdo_simrs->prepare($sqlDokter);
    $stmtD->execute($params);
    $daftar = $stmtD->fetchAll(PDO::FETCH_ASSOC);

    // ── Ambil SEMUA pasien hari ini sekaligus (1 query, bukan N query) ──
    $sqlAllPasien = "
        SELECT r.no_reg, r.no_rawat, r.kd_poli, r.kd_dokter, r.stts, ps.nm_pasien
        FROM reg_periksa r
        LEFT JOIN pasien ps ON r.no_rkm_medis = ps.no_rkm_medis
        WHERE r.tgl_registrasi = ?
    ";
    $pasienParams = [$today];
    if (!empty($poli))   { $sqlAllPasien .= " AND r.kd_poli = ?";   $pasienParams[] = $poli; }
    if (!empty($dokter)) { $sqlAllPasien .= " AND r.kd_dokter = ?"; $pasienParams[] = $dokter; }
    $sqlAllPasien .= " ORDER BY r.kd_poli, r.kd_dokter, r.no_reg+0 ASC";
    $stmtAllPasien = $pdo_simrs->prepare($sqlAllPasien);
    $stmtAllPasien->execute($pasienParams);
    $allPasienRaw = $stmtAllPasien->fetchAll(PDO::FETCH_ASSOC);

    $pasienMap = [];
    foreach ($allPasienRaw as $row) {
        $pasienMap[$row['kd_poli'] . '|' . $row['kd_dokter']][] = $row;
    }

    // ── Ambil SEMUA panggilan terakhir sekaligus (1 query) ──
    $stmtAllCall = $pdo_simrs->prepare("
        SELECT kd_poli, kd_dokter, no_antrian, nm_pasien, no_rawat, jml_panggil, updated_at
        FROM simpan_antrian_poli_wira
        WHERE tgl_panggil = ?
        ORDER BY updated_at DESC
    ");
    $stmtAllCall->execute([$today]);
    $allCallRaw = $stmtAllCall->fetchAll(PDO::FETCH_ASSOC);

    $callMap = [];
    foreach ($allCallRaw as $row) {
        $key = $row['kd_poli'] . '|' . $row['kd_dokter'];
        if (!isset($callMap[$key])) {
            $callMap[$key] = $row;
        }
    }

    $result = [];
    foreach ($daftar as $d) {
        $kd_d = $d['kd_dokter'];
        $kd_p = $d['kd_poli'];
        $key  = $kd_p . '|' . $kd_d;

        $call        = $callMap[$key]   ?? null;
        $pasien_rows = $pasienMap[$key] ?? [];

        $total    = count($pasien_rows);
        $sudah    = count(array_filter($pasien_rows, fn($s) => $s['stts'] === 'Sudah'));
        $menunggu = count(array_filter($pasien_rows, fn($s) => in_array($s['stts'], ['Menunggu','Belum'])));

        $result[] = [
            'kd_dokter'  => $kd_d,
            'kd_poli'    => $kd_p,
            'nm_dokter'  => $d['nm_dokter'] ?? '',
            'nm_poli'    => $d['nm_poli']   ?? '',
            'no_antrian' => $call['no_antrian'] ?? null,
            'nm_pasien'  => $call['nm_pasien']  ?? null,
            'nm_pasien_suara' => normalizeNamaPasien($call['nm_pasien']  ?? null),
            'no_rawat'   => $call['no_rawat']   ?? null,
            'jml_panggil'=> $call['jml_panggil']?? 0,
            'total'      => $total,
            'sudah'      => $sudah,
            'menunggu'   => $menunggu,
            'pasien'     => $pasien_rows
        ];
    }

    echo json_encode($result);

} catch (PDOException $e) {
    http_response_code(500);
    error_log('get_all_calls.php: ' . $e->getMessage());
    echo json_encode(['error' => 'Gagal mengambil data antrian']);
}