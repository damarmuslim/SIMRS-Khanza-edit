<?php
session_start();
if (!isset($_SESSION['user_id'])) {
    header('Location: login.php');
    exit;
}

include 'koneksi.php';  // koneksi database anjungan
include 'koneksi2.php'; // koneksi database SIMRS

$nama = $_SESSION['nama'] ?? 'Pengguna';
date_default_timezone_set('Asia/Jakarta');

// Filter
$tgl_awal  = $_GET['tgl_awal'] ?? date('Y-m-d');
$tgl_akhir = $_GET['tgl_akhir'] ?? date('Y-m-d');
$search    = $_GET['search'] ?? '';

// Query Data SEP
try {
    $sql = "SELECT * FROM bridging_sep WHERE tglsep BETWEEN :tgl_awal AND :tgl_akhir";
    
    if (!empty($search)) {
        $sql .= " AND (no_sep LIKE :search OR no_rawat LIKE :search OR nomr LIKE :search OR nama_pasien LIKE :search OR no_kartu LIKE :search)";
    }
    
    $sql .= " ORDER BY tglsep DESC, no_sep DESC";
    
    $stmt = $pdo_simrs->prepare($sql);
    $params = [
        'tgl_awal'  => $tgl_awal,
        'tgl_akhir' => $tgl_akhir
    ];
    
    if (!empty($search)) {
        $params['search'] = "%$search%";
    }
    
    $stmt->execute($params);
    $data_sep = $stmt->fetchAll();
} catch (Exception $e) {
    $error = "Gagal mengambil data: " . $e->getMessage();
}
?>
<!doctype html>
<html lang="id">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width,initial-scale=1">
<title>Data Riwayat SEP - MediFix</title>
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
<link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.1/font/bootstrap-icons.css" rel="stylesheet">
<link href="https://fonts.googleapis.com/css2?family=Outfit:wght@300;400;500;600;700;800&family=Plus+Jakarta+Sans:ital,wght@0,400;0,500;0,600;0,700;1,400&display=swap" rel="stylesheet">
<style>
:root {
  --teal-50:  #f0fdfa; --teal-100: #ccfbf1; --teal-500: #14b8a6; --teal-600: #0d9488; --teal-700: #0f766e;
  --sky-50:   #f0f9ff; --sky-100:  #e0f2fe; --sky-500:  #0ea5e9; --sky-600:  #0284c7; --sky-700:  #0369a1;
  --indigo-50:#eef2ff; --indigo-100:#e0e7ff; --indigo-500:#6366f1; --indigo-600:#4f46e5;
  --slate-50: #f8fafc; --slate-100: #f1f5f9; --slate-200: #e2e8f0; --slate-500: #64748b; --slate-800: #1e293b; --slate-900: #0f172a;
}
body { background: var(--slate-50); font-family: 'Plus Jakarta Sans', sans-serif; color: var(--slate-800); }
.topbar { background: #fff; box-shadow: 0 1px 3px rgba(0,0,0,0.05); padding: 12px 24px; display: flex; align-items: center; justify-content: space-between; position: sticky; top: 0; z-index: 50; }
.back-btn { display: flex; align-items: center; gap: 8px; font-weight: 600; color: var(--slate-800); text-decoration: none; padding: 8px 16px; border-radius: 8px; transition: 0.2s; }
.back-btn:hover { background: var(--slate-100); color: var(--sky-600); }

.page-header { background: #fff; border-bottom: 1px solid var(--slate-200); padding: 24px 0; margin-bottom: 24px; }
.page-title { font-family: 'Outfit', sans-serif; font-weight: 800; color: var(--slate-900); margin: 0; }

.card-container { background: #fff; border-radius: 16px; border: 1px solid var(--slate-200); box-shadow: 0 4px 6px -1px rgba(0,0,0,0.05); overflow: hidden; }
.filter-section { padding: 20px; background: var(--slate-50); border-bottom: 1px solid var(--slate-200); }

.table th { background: var(--slate-100); color: var(--slate-500); font-weight: 600; font-size: 13px; text-transform: uppercase; letter-spacing: 0.025em; border-top: none; padding: 12px 16px; }
.table td { padding: 16px; vertical-align: middle; font-size: 14px; border-bottom: 1px solid var(--slate-100); }
.table tr:hover { background: var(--slate-50); }

.badge-sep { background: var(--teal-100); color: var(--teal-700); font-weight: 700; font-size: 12px; padding: 6px 10px; border-radius: 6px; font-family: 'Outfit', sans-serif; }
.btn-cetak { background: var(--sky-50); color: var(--sky-600); border: 1px solid var(--sky-100); font-weight: 600; padding: 6px 12px; border-radius: 8px; transition: 0.2s; text-decoration: none; font-size: 13px; }
.btn-cetak:hover { background: var(--sky-600); color: #fff; border-color: var(--sky-600); }

.form-control, .form-select { border-radius: 8px; border: 1px solid var(--slate-200); padding: 10px 14px; font-size: 14px; }
.form-control:focus, .form-select:focus { border-color: var(--sky-500); box-shadow: 0 0 0 3px rgba(14, 165, 233, 0.1); }
.btn-filter { background: var(--slate-900); color: #fff; border-radius: 8px; font-weight: 600; padding: 10px 24px; transition: 0.3s; gap: 8px; display: flex; align-items: center; }
.btn-filter:hover { background: #000; color: #fff; }
</style>
</head>
<body>

<div class="topbar">
    <a href="bridging_vclaim.php" class="back-btn"><i class="bi bi-arrow-left"></i> Kembali ke Dashboard V-Claim</a>
    <div class="d-flex align-items-center gap-3">
        <span class="fw-medium text-secondary"><i class="bi bi-person-fill me-1"></i> <?= htmlspecialchars($nama) ?></span>
    </div>
</div>

<div class="page-header">
    <div class="container">
        <div class="d-flex justify-content-between align-items-center">
            <div>
                <h2 class="page-title">Riwayat Data SEP</h2>
                <p class="text-secondary mb-0">Daftar Surat Eligibilitas Peserta (SEP) yang telah diterbitkan.</p>
            </div>
            <div class="ico-box ic-teal" style="width: 56px; height: 56px; border-radius: 12px; display: flex; align-items: center; justify-content: center; font-size: 26px; background: var(--teal-50); color: var(--teal-600);">
                <i class="bi bi-collection"></i>
            </div>
        </div>
    </div>
</div>

<div class="container pb-5">
    <div class="card-container">
        <div class="filter-section">
            <form method="GET" class="row g-3">
                <div class="col-md-3">
                    <label class="form-label small fw-bold text-secondary">Tanggal Awal</label>
                    <input type="date" name="tgl_awal" class="form-control" value="<?= htmlspecialchars($tgl_awal) ?>">
                </div>
                <div class="col-md-3">
                    <label class="form-label small fw-bold text-secondary">Tanggal Akhir</label>
                    <input type="date" name="tgl_akhir" class="form-control" value="<?= htmlspecialchars($tgl_akhir) ?>">
                </div>
                <div class="col-md-4">
                    <label class="form-label small fw-bold text-secondary">Pencarian</label>
                    <input type="text" name="search" class="form-control" placeholder="No. SEP / RM / Rawat / Nama..." value="<?= htmlspecialchars($search) ?>">
                </div>
                <div class="col-md-2 d-flex align-items-end">
                    <button type="submit" class="btn btn-filter w-100">
                        <i class="bi bi-search"></i> Filter Data
                    </button>
                </div>
            </form>
        </div>

        <?php if (isset($error)): ?>
            <div class="alert alert-danger m-3"><?= $error ?></div>
        <?php endif; ?>

        <div class="table-responsive">
            <table class="table mb-0">
                <thead>
                    <tr>
                        <th>No. SEP</th>
                        <th>Tgl. SEP</th>
                        <th>Pasien & No. RM</th>
                        <th>No. Rawat</th>
                        <th>Poli & Diagnosa</th>
                        <th class="text-center">Aksi</th>
                    </tr>
                </thead>
                <tbody>
                    <?php if (empty($data_sep)): ?>
                        <tr>
                            <td colspan="6" class="text-center py-5">
                                <i class="bi bi-inbox text-secondary" style="font-size: 48px;"></i>
                                <p class="text-secondary mt-2 mb-0">Data tidak ditemukan untuk periode/pencarian ini.</p>
                            </td>
                        </tr>
                    <?php else: ?>
                        <?php foreach($data_sep as $row): ?>
                        <tr>
                            <td>
                                <span class="badge-sep"><?= htmlspecialchars($row['no_sep']) ?></span>
                                <div class="mt-1 small text-secondary">BPJS: <?= htmlspecialchars($row['no_kartu']) ?></div>
                            </td>
                            <td>
                                <div class="fw-bold"><?= date('d/m/Y', strtotime($row['tglsep'])) ?></div>
                            </td>
                            <td>
                                <div class="fw-bold text-dark"><?= htmlspecialchars($row['nama_pasien']) ?></div>
                                <div class="small text-secondary">RM: <?= htmlspecialchars($row['nomr']) ?></div>
                            </td>
                            <td>
                                <span class="badge bg-light text-dark border"><?= htmlspecialchars($row['no_rawat']) ?></span>
                            </td>
                            <td>
                                <div class="fw-bold"><?= htmlspecialchars($row['nmpolitujuan']) ?></div>
                                <div class="small text-truncate" style="max-width: 250px;" title="<?= htmlspecialchars($row['nmdiagnosaawal']) ?>">
                                    <?= htmlspecialchars($row['diagawal']) ?> - <?= htmlspecialchars($row['nmdiagnosaawal']) ?>
                                </div>
                            </td>
                            <td class="text-center">
                                <a href="cetak_sep_poli.php?no_rawat=<?= urlencode($row['no_rawat']) ?>" class="btn-cetak" target="_blank">
                                    <i class="bi bi-printer me-1"></i> Cetak
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
