<?php
session_start();
if (!isset($_SESSION['user_id'])) {
    header('Location: login.php');
    exit;
}
include 'koneksi2.php';

$nama = $_SESSION['nama'] ?? 'Pengguna';
$search = $_GET['search'] ?? '';
$tgl1 = $_GET['tgl1'] ?? date('Y-m-d');
$tgl2 = $_GET['tgl2'] ?? date('Y-m-d');

// Query untuk mengambil data registrasi pasien yang memiliki SEP (bridging_sep)
$query = "SELECT rp.no_rawat, rp.no_reg, rp.tgl_registrasi, p.no_rkm_medis, p.nm_pasien, 
                 bs.no_sep, pj.png_jawab, d.nm_dokter, pol.nm_poli
          FROM reg_periksa rp
          JOIN pasien p ON rp.no_rkm_medis = p.no_rkm_medis
          LEFT JOIN bridging_sep bs ON rp.no_rawat = bs.no_rawat
          JOIN penjab pj ON rp.kd_pj = pj.kd_pj
          JOIN dokter d ON rp.kd_dokter = d.kd_dokter
          JOIN poliklinik pol ON rp.kd_poli = pol.kd_poli
          WHERE rp.tgl_registrasi BETWEEN :tgl1 AND :tgl2";

if (!empty($search)) {
    $query .= " AND (p.no_rkm_medis LIKE :search OR p.nm_pasien LIKE :search OR bs.no_sep LIKE :search OR rp.no_rawat LIKE :search)";
}

$query .= " ORDER BY rp.tgl_registrasi DESC, rp.jam_reg DESC LIMIT 100";

$stmt = $pdo_simrs->prepare($query);
$stmt->bindValue(':tgl1', $tgl1);
$stmt->bindValue(':tgl2', $tgl2);
if (!empty($search)) {
    $stmt->bindValue(':search', "%$search%");
}
$stmt->execute();
$data_pasien = $stmt->fetchAll();
?>
<!doctype html>
<html lang="id">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width,initial-scale=1">
    <title>Data Casemix - MediFix</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.1/font/bootstrap-icons.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Outfit:wght@400;500;600;700&family=Plus+Jakarta+Sans:wght@400;500;600;700&display=swap" rel="stylesheet">
    <style>
        :root {
            --purple-600: #9333ea;
            --purple-50: #f3e8ff;
            --slate-100: #f1f5f9;
            --slate-200: #e2e8f0;
            --slate-800: #1e293b;
        }
        body { background: #f8fafc; font-family: 'Plus Jakarta Sans', sans-serif; color: var(--slate-800); }
        .topbar { background: #fff; box-shadow: 0 1px 3px rgba(0,0,0,0.05); padding: 12px 24px; position: sticky; top: 0; z-index: 50; display: flex; align-items: center; justify-content: space-between;}
        .back-btn { text-decoration: none; color: var(--slate-800); font-weight: 600; display: flex; align-items: center; gap: 8px; transition: 0.2s; }
        .back-btn:hover { color: var(--purple-600); }
        .card-data { background: #fff; border-radius: 16px; border: 1px solid var(--slate-200); box-shadow: 0 4px 6px -1px rgba(0,0,0,0.1); overflow: hidden; }
        .table-header { padding: 24px; border-bottom: 1px solid var(--slate-200); background: #fff; }
        .table thead th { background: var(--slate-100); color: #64748b; font-weight: 700; text-transform: uppercase; font-size: 11px; letter-spacing: 0.05em; padding: 12px 16px; border: none; }
        .table tbody td { padding: 16px; vertical-align: middle; border-bottom: 1px solid var(--slate-100); font-size: 14px; }
        .badge-sep { background: var(--purple-50); color: var(--purple-600); font-weight: 700; border: 1px solid #d8b4fe; }
        .btn-action { border-radius: 8px; font-weight: 600; font-size: 13px; padding: 8px 12px; transition: 0.2s; }
        .btn-koding { background: var(--purple-600); color: #fff; border: none; }
        .btn-koding:hover { background: #7e22ce; color: #fff; transform: translateY(-1px); }
        .search-box { border-radius: 10px; border: 1px solid var(--slate-200); padding: 8px 16px; font-size: 14px; }
        .search-box:focus { border-color: var(--purple-600); box-shadow: 0 0 0 3px rgba(147, 51, 234, 0.1); outline: none; }
    </style>
</head>
<body>
<div class="topbar">
    <a href="casemix_dashboard.php" class="back-btn"><i class="bi bi-arrow-left"></i> Kembali ke Dashboard Casemix</a>
    <div class="d-flex align-items-center gap-3">
        <span class="text-secondary small fw-medium">Kasir: <strong><?= htmlspecialchars($nama) ?></strong></span>
        <div class="badge bg-purple-subtle text-purple py-2 px-3 rounded-pill fw-bold" style="background:#f3e8ff; color:#9333ea;">Integrasi INA-CBG</div>
    </div>
</div>

<div class="container mt-4 pb-5">
    <div class="d-flex align-items-center mb-4 gap-3">
        <div class="bg-purple-subtle rounded-4 p-3 text-purple" style="background:#f3e8ff; color:#9333ea;"><i class="bi bi-folder-check fs-3"></i></div>
        <div>
            <h3 class="fw-bold mb-0" style="font-family: 'Outfit', sans-serif;">Manajemen Data Casemix</h3>
            <p class="text-secondary mb-0">Kelola koding diagnosa dan prosedur untuk klaim BPJS Kesehatan.</p>
        </div>
    </div>
    
    <div class="card-data">
        <div class="table-header">
            <form action="" method="GET" class="row g-3 align-items-center">
                <div class="col-md-3">
                    <label class="form-label small fw-bold text-secondary">Mulai Tanggal</label>
                    <input type="date" name="tgl1" class="form-control search-box" value="<?= $tgl1 ?>">
                </div>
                <div class="col-md-3">
                    <label class="form-label small fw-bold text-secondary">Sampai Tanggal</label>
                    <input type="date" name="tgl2" class="form-control search-box" value="<?= $tgl2 ?>">
                </div>
                <div class="col-md-4">
                    <label class="form-label small fw-bold text-secondary">Cari Pasien</label>
                    <div class="input-group">
                        <span class="input-group-text bg-white border-end-0" style="border-radius: 10px 0 0 10px;"><i class="bi bi-search text-muted"></i></span>
                        <input type="text" name="search" class="form-control border-start-0 search-box" placeholder="No. RM / Nama / No. SEP..." value="<?= htmlspecialchars($search) ?>" style="border-radius: 0 10px 10px 0;">
                    </div>
                </div>
                <div class="col-md-2 d-flex align-items-end">
                    <button type="submit" class="btn btn-koding w-100 py-2" style="border-radius: 10px;"><i class="bi bi-filter me-1"></i> Terapkan</button>
                </div>
            </form>
        </div>
        
        <div class="table-responsive">
            <table class="table table-hover mb-0">
                <thead>
                    <tr>
                        <th class="ps-4">No. Rawat</th>
                        <th>No. RM & Nama Pasien</th>
                        <th>Dokter & Poli</th>
                        <th>No. SEP</th>
                        <th>Cara Bayar</th>
                        <th class="text-center pe-4">Aksi</th>
                    </tr>
                </thead>
                <tbody>
                    <?php if (empty($data_pasien)): ?>
                    <tr>
                        <td colspan="6" class="text-center py-5">
                            <i class="bi bi-inbox text-secondary display-4 d-block mb-3"></i>
                            <p class="text-muted mb-0">Tidak ada data pasien ditemukan untuk kriteria ini.</p>
                        </td>
                    </tr>
                    <?php else: ?>
                    <?php foreach ($data_pasien as $row): ?>
                    <tr>
                        <td class="ps-4 fw-medium text-purple"><?= $row['no_rawat'] ?></td>
                        <td>
                            <div class="fw-bold"><?= $row['no_rkm_medis'] ?></div>
                            <div class="small text-secondary"><?= $row['nm_pasien'] ?></div>
                        </td>
                        <td>
                            <div class="fw-medium"><?= $row['nm_dokter'] ?></div>
                            <div class="small text-muted"><?= $row['nm_poli'] ?></div>
                        </td>
                        <td>
                            <?php if ($row['no_sep']): ?>
                                <span class="badge badge-sep py-2 px-3 rounded-pill"><?= $row['no_sep'] ?></span>
                            <?php else: ?>
                                <span class="badge bg-danger-subtle text-danger py-2 px-3 rounded-pill"><i class="bi bi-x-circle me-1"></i> Belum Ada SEP</span>
                            <?php endif; ?>
                        </td>
                        <td><span class="badge bg-light text-dark border py-2 px-3 rounded-pill"><?= $row['png_jawab'] ?></span></td>
                        <td class="text-center pe-4">
                            <a href="koding_diagnosa.php?no_rawat=<?= urlencode($row['no_rawat']) ?>" class="btn btn-action btn-koding">
                                <i class="bi bi-clipboard-pulse me-1"></i> Koding
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

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
