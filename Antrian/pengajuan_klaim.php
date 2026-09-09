<?php
session_start();
if (!isset($_SESSION['user_id'])) {
    header('Location: login.php');
    exit;
}
include 'koneksi2.php';

$nama = $_SESSION['nama'] ?? 'Pengguna';
$tgl1 = $_GET['tgl1'] ?? date('Y-m-d');
$tgl2 = $_GET['tgl2'] ?? date('Y-m-d');

// Query monitoring klaim: Pasien yang sudah ada SEP dan minimal sudah ada diagnosa primer
$query = "SELECT rp.no_rawat, p.no_rkm_medis, p.nm_pasien, bs.no_sep, pj.png_jawab,
                 (SELECT kd_penyakit FROM diagnosa_pasien WHERE no_rawat = rp.no_rawat AND prioritas = 1 LIMIT 1) as diag_primer
          FROM reg_periksa rp
          JOIN pasien p ON rp.no_rkm_medis = p.no_rkm_medis
          JOIN bridging_sep bs ON rp.no_rawat = bs.no_rawat
          JOIN penjab pj ON rp.kd_pj = pj.kd_pj
          WHERE rp.tgl_registrasi BETWEEN :tgl1 AND :tgl2
          ORDER BY rp.tgl_registrasi DESC";

$stmt = $pdo_simrs->prepare($query);
$stmt->bindValue(':tgl1', $tgl1);
$stmt->bindValue(':tgl2', $tgl2);
$stmt->execute();
$data_klaim = $stmt->fetchAll();
?>
<!doctype html>
<html lang="id">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width,initial-scale=1">
    <title>Pengajuan Klaim - MediFix</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.1/font/bootstrap-icons.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Outfit:wght@400;500;600;700&family=Plus+Jakarta+Sans:wght@400;500;600;700&display=swap" rel="stylesheet">
    <style>
        :root { --purple-600: #9333ea; --purple-50: #f3e8ff; --slate-200: #e2e8f0; --slate-800: #1e293b; }
        body { background: #f8fafc; font-family: 'Plus Jakarta Sans', sans-serif; color: var(--slate-800); }
        .topbar { background: #fff; box-shadow: 0 1px 3px rgba(0,0,0,0.05); padding: 12px 24px; position: sticky; top: 0; z-index: 50; display: flex; align-items: center; justify-content: space-between;}
        .back-btn { text-decoration: none; color: var(--slate-800); font-weight: 600; display: flex; align-items: center; gap: 8px; }
        .card-klaim { background: #fff; border-radius: 16px; border: 1px solid var(--slate-200); box-shadow: 0 4px 6px -1px rgba(0,0,0,0.1); overflow: hidden; }
        .status-ready { background: #dcfce7; color: #166534; border: 1px solid #bbf7d0; }
        .status-pending { background: #fef9c3; color: #854d0e; border: 1px solid #fef08a; }
    </style>
</head>
<body>
<div class="topbar">
    <a href="casemix_dashboard.php" class="back-btn"><i class="bi bi-arrow-left"></i> Kembali ke Dashboard Casemix</a>
    <div class="badge bg-purple-subtle text-purple py-2 px-3 rounded-pill fw-bold" style="background:#f3e8ff; color:#9333ea;">Monitoring Klaim</div>
</div>

<div class="container mt-4 pb-5">
    <div class="d-flex align-items-center mb-4 gap-3">
        <div class="bg-primary-subtle rounded-4 p-3 text-primary" style="background:#eff6ff; color:#3b82f6;"><i class="bi bi-receipt fs-3"></i></div>
        <div>
            <h3 class="fw-bold mb-0" style="font-family: 'Outfit', sans-serif;">Pengajuan Klaim</h3>
            <p class="text-secondary mb-0">Monitoring status kesiapan berkas klaim INA-CBG.</p>
        </div>
    </div>

    <div class="card-klaim">
        <div class="p-4 border-bottom">
            <form action="" method="GET" class="row g-3 align-items-end">
                <div class="col-md-3">
                    <label class="form-label small fw-bold">Dari Tanggal</label>
                    <input type="date" name="tgl1" class="form-control" value="<?= $tgl1 ?>">
                </div>
                <div class="col-md-3">
                    <label class="form-label small fw-bold">Sd. Tanggal</label>
                    <input type="date" name="tgl2" class="form-control" value="<?= $tgl2 ?>">
                </div>
                <div class="col-md-2">
                    <button type="submit" class="btn btn-primary w-100"><i class="bi bi-search"></i> Filter</button>
                </div>
            </form>
        </div>
        <div class="table-responsive">
            <table class="table table-hover mb-0">
                <thead class="table-light">
                    <tr>
                        <th class="ps-4">No. Rawat / SEP</th>
                        <th>Nama Pasien</th>
                        <th>Diagnosa Utama</th>
                        <th>Cara Bayar</th>
                        <th class="text-center">Status Berkas</th>
                        <th class="pe-4 text-center">Aksi</th>
                    </tr>
                </thead>
                <tbody>
                    <?php if(empty($data_klaim)): ?>
                        <tr><td colspan="6" class="text-center py-5 text-muted">Tidak ada data klaim pada periode ini.</td></tr>
                    <?php else: ?>
                        <?php foreach($data_klaim as $row): ?>
                        <tr>
                            <td class="ps-4">
                                <div class="fw-bold"><?= $row['no_rawat'] ?></div>
                                <div class="small text-secondary"><?= $row['no_sep'] ?></div>
                            </td>
                            <td><?= $row['nm_pasien'] ?></td>
                            <td><?= $row['diag_primer'] ?: '<span class="text-danger small italic">Belum Coded</span>' ?></td>
                            <td><?= $row['png_jawab'] ?></td>
                            <td class="text-center">
                                <?php if($row['diag_primer']): ?>
                                    <span class="badge status-ready py-2 px-3 rounded-pill">SIAP KLAIM</span>
                                <?php else: ?>
                                    <span class="badge status-pending py-2 px-3 rounded-pill">PENDING KODING</span>
                                <?php endif; ?>
                            </td>
                            <td class="pe-4 text-center">
                                <a href="koding_diagnosa.php?no_rawat=<?= urlencode($row['no_rawat']) ?>" class="btn btn-sm btn-outline-primary">
                                    <i class="bi bi-pencil-square"></i> Edit
                                </a>
                            </td>
                        </tr>
                        <?php endforeach; ?>
                    <?php endif; ?>
                </tbody>
            </table>
        </div>
    </div>
</div>
</body>
</html>
