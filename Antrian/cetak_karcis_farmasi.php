<?php
session_start();
include 'koneksi2.php';
date_default_timezone_set('Asia/Jakarta');

if (!isset($_SESSION['user_id'])) {
    die('Unauthorized');
}

$no_rawat = $_GET['no_rawat'] ?? '';
if (empty($no_rawat)) {
    die('No rawat tidak ditemukan');
}

// Ambil data resep + nomor antrian dari antrian_wira
try {
    $tgl      = date('Y-m-d');
    $tglStart = $tgl . ' 00:00:00';
    $tglEnd   = $tgl . ' 23:59:59';

    $stmt = $pdo_simrs->prepare("
        SELECT ro.no_resep, ro.tgl_peresepan, ro.jam_peresepan,
               r.no_rkm_medis, p.nm_pasien, pl.nm_poli,
               CASE
                   WHEN EXISTS (SELECT 1 FROM resep_dokter_racikan rr WHERE rr.no_resep = ro.no_resep)
                   THEN 'Racikan'
                   ELSE 'Non Racikan'
               END AS jenis_resep
        FROM resep_obat ro
        LEFT JOIN reg_periksa r  ON ro.no_rawat   = r.no_rawat
        LEFT JOIN pasien p       ON r.no_rkm_medis = p.no_rkm_medis
        LEFT JOIN poliklinik pl  ON r.kd_poli      = pl.kd_poli
        WHERE ro.no_rawat = ?
        ORDER BY ro.tgl_peresepan DESC, ro.jam_peresepan DESC
        LIMIT 1
    ");
    $stmt->execute([$no_rawat]);
    $resep = $stmt->fetch(PDO::FETCH_ASSOC);

    if (!$resep) {
        die('Data resep tidak ditemukan');
    }

    // Ambil nomor antrian dari antrian_wira (kiosk) — bukan dari no_resep
    $stmtAW = $pdo_simrs->prepare("
        SELECT nomor FROM antrian_wira
        WHERE jenis        = 'FARMASI'
          AND no_rkm_medis = ?
          AND created_at BETWEEN ? AND ?
        ORDER BY id ASC LIMIT 1
    ");
    $stmtAW->execute([$resep['no_rkm_medis'], $tglStart, $tglEnd]);
    $no_antrian_farmasi = $stmtAW->fetchColumn() ?: '-';

} catch (PDOException $e) {
    die('Error: ' . $e->getMessage());
}

// Ambil nama RS
try {
    $rs     = $pdo_simrs->query("SELECT nama_instansi, alamat_instansi, kontak FROM setting LIMIT 1")->fetch(PDO::FETCH_ASSOC);
    $namaRS = $rs['nama_instansi']    ?? 'RSUD DEMANG SEPULAU RAYA';
    $alamat = $rs['alamat_instansi']  ?? '';
    $kontak = $rs['kontak']           ?? '';
} catch (PDOException $e) {
    $namaRS = 'RSUD DEMANG SEPULAU RAYA';
    $alamat = '';
    $kontak = '';
}

$isRacikan = $resep['jenis_resep'] === 'Racikan';
?>
<!DOCTYPE html>
<html lang="id">
<head>
<meta charset="utf-8">
<title>Karcis Farmasi</title>
<style>
/* ======================================================
   OPTIMASI PRINTER THERMAL 58mm
   - Tidak ada background berwarna (ikut jadi abu/buram)
   - Tidak ada border tebal
   - Font bold agar tinta minimal tetap terbaca jelas
   - Semua ukuran dalam mm agar konsisten di semua browser
   ====================================================== */

@page {
    size: 58mm auto;   /* 58mm = lebar kertas POS 58 */
    margin: 2mm 3mm;
}

* {
    margin: 0;
    padding: 0;
    box-sizing: border-box;
}

html, body {
    width: 58mm;
    background: #fff;
    color: #000;
}

body {
    font-family: 'Courier New', Courier, monospace;
    font-size: 8pt;
    line-height: 1.4;
    -webkit-print-color-adjust: exact;
    print-color-adjust: exact;
}

/* Pemisah — hanya garis bawah, tipis */
.divider {
    border: none;
    border-bottom: 1px dashed #000;
    margin: 3mm 0;
}

/* Header RS */
.rs-name {
    font-size: 9pt;
    font-weight: 900;
    text-align: center;
    text-transform: uppercase;
    letter-spacing: 0.3px;
    line-height: 1.3;
}

.rs-sub {
    font-size: 7pt;
    text-align: center;
    font-weight: bold;
    line-height: 1.4;
}

/* Label section */
.section-label {
    font-size: 8pt;
    font-weight: 900;
    text-align: center;
    text-transform: uppercase;
    letter-spacing: 1px;
    margin: 1mm 0;
}

/* NOMOR BESAR — border kotak hitam, teks tebal */
.nomor-box {
    border: 2px solid #000;
    text-align: center;
    padding: 2mm 0;
    margin: 2mm 0;
}

.nomor-teks {
    font-size: 32pt;       /* Besar & jelas di 58mm */
    font-weight: 900;
    letter-spacing: 3px;
    line-height: 1;
    color: #000;
}

/* Badge jenis resep — teks saja, no background */
.jenis-badge {
    font-size: 8pt;
    font-weight: 900;
    text-align: center;
    text-transform: uppercase;
    letter-spacing: 1px;
    border: 1.5px solid #000;
    display: inline-block;
    padding: 1mm 4mm;
    margin: 1mm 0;
}

/* Info pasien */
.info-row {
    display: flex;
    font-size: 7.5pt;
    font-weight: bold;
    margin: 0.8mm 0;
    line-height: 1.3;
}

.info-label {
    width: 18mm;
    flex-shrink: 0;
    font-weight: 900;
}

.info-value {
    flex: 1;
    word-break: break-word;
}

/* Footer */
.footer-text {
    font-size: 7pt;
    font-weight: bold;
    text-align: center;
    line-height: 1.5;
}

.racikan-warning {
    font-size: 7pt;
    font-weight: 900;
    text-align: center;
    border: 1px solid #000;
    padding: 1mm;
    margin-top: 2mm;
}

.waktu-cetak {
    font-size: 6.5pt;
    font-weight: bold;
    text-align: center;
    margin-top: 2mm;
    color: #333;
}

/* Sembunyikan saat cetak hal yang tidak perlu */
@media print {
    .no-print { display: none !important; }
}
</style>
</head>
<body onload="window.print()">

<!-- HEADER RS -->
<div class="rs-name"><?= htmlspecialchars($namaRS) ?></div>
<?php if ($alamat): ?>
<div class="rs-sub"><?= htmlspecialchars(mb_strimwidth($alamat, 0, 60, '...')) ?></div>
<?php endif; ?>
<?php if ($kontak): ?>
<div class="rs-sub">Telp: <?= htmlspecialchars($kontak) ?></div>
<?php endif; ?>

<div class="divider"></div>

<!-- LABEL SECTION -->
<div class="section-label">ANTRIAN FARMASI</div>
<div class="section-label" style="font-size:7pt;font-weight:bold;">
    <?= date('d/m/Y') ?> | <?= date('H:i') ?> WIB
</div>

<div class="divider"></div>

<!-- NOMOR ANTRIAN — kotak besar, jelas -->
<div class="nomor-box">
    <div style="font-size:7pt;font-weight:900;letter-spacing:1px;margin-bottom:1mm;">
        NOMOR ANTRIAN ANDA
    </div>
    <div class="nomor-teks"><?= htmlspecialchars($no_antrian_farmasi) ?></div>
</div>

<!-- JENIS RESEP -->
<div style="text-align:center;margin:1mm 0;">
    <span class="jenis-badge">
        <?= $isRacikan ? '★ RACIKAN ★' : '■ NON RACIKAN ■' ?>
    </span>
</div>

<div class="divider"></div>

<!-- INFO PASIEN -->
<div class="info-row">
    <div class="info-label">No. RM</div>
    <div class="info-value">: <?= htmlspecialchars($resep['no_rkm_medis']) ?></div>
</div>
<div class="info-row">
    <div class="info-label">Nama</div>
    <div class="info-value">: <?= htmlspecialchars($resep['nm_pasien']) ?></div>
</div>
<div class="info-row">
    <div class="info-label">No. Resep</div>
    <div class="info-value">: <?= htmlspecialchars($resep['no_resep']) ?></div>
</div>
<div class="info-row">
    <div class="info-label">Dari Poli</div>
    <div class="info-value">: <?= htmlspecialchars($resep['nm_poli'] ?? '-') ?></div>
</div>

<div class="divider"></div>

<!-- FOOTER -->
<div class="footer-text">
    Mohon tunggu panggilan nomor Anda<br>
    di layar display antrian farmasi
</div>

<?php if ($isRacikan): ?>
<div class="racikan-warning">
    ⚠ Resep Racikan: estimasi 15-60 menit
</div>
<?php endif; ?>

<div class="waktu-cetak">Dicetak: <?= date('d/m/Y H:i:s') ?></div>

</body>
</html>
