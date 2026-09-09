<?php
session_start();
if (!isset($_SESSION['user_id'])) {
    header('Location: login.php');
    exit;
}
$nama = $_SESSION['nama'] ?? 'Pengguna';

// Include koneksi ke SIMRS
require_once 'koneksi2.php';

// Filter parameter
$searchTglMulai = isset($_GET['tgl_mulai']) ? $_GET['tgl_mulai'] : date('Y-m-d');
$searchTglAkhir = isset($_GET['tgl_akhir']) ? $_GET['tgl_akhir'] : date('Y-m-d');
$keyword = isset($_GET['keyword']) ? trim($_GET['keyword']) : '';

// Susun Query
$sql = "SELECT 
            rm.no_rawat, rm.norm, p.nm_pasien, rm.nohp, rm.nomorkartu, rm.nik, 
            rm.tanggalperiksa, pl.nm_poli, d.nm_dokter, rm.jampraktek, 
            rm.jeniskunjungan, rm.nomorreferensi, rm.status, rm.validasi, rm.nobooking
        FROM referensi_mobilejkn_bpjs rm
        LEFT JOIN pasien p ON rm.norm = p.no_rkm_medis
        LEFT JOIN dokter d ON rm.kodedokter = d.kd_dokter
        LEFT JOIN poliklinik pl ON rm.kodepoli = pl.kd_poli
        WHERE rm.tanggalperiksa BETWEEN :tgl_mulai AND :tgl_akhir";

$params = [
    ':tgl_mulai' => $searchTglMulai,
    ':tgl_akhir' => $searchTglAkhir
];

if (!empty($keyword)) {
    $sql .= " AND (rm.no_rawat LIKE :keyword OR rm.norm LIKE :keyword OR p.nm_pasien LIKE :keyword OR rm.nomorkartu LIKE :keyword OR rm.nobooking LIKE :keyword)";
    $params[':keyword'] = '%' . $keyword . '%';
}

$sql .= " ORDER BY rm.validasi DESC";

try {
    $stmt = $pdo_simrs->prepare($sql);
    $stmt->execute($params);
    $data_mobilejkn = $stmt->fetchAll(PDO::FETCH_ASSOC);
} catch (PDOException $e) {
    $error_msg = "Terjadi kesalahan kueri: " . $e->getMessage();
}

?>
<!doctype html>
<html lang="id">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width,initial-scale=1">
<title>Referensi Pendaftaran Mobile JKN - MediFix</title>
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
<link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.1/font/bootstrap-icons.css" rel="stylesheet">
<link href="https://fonts.googleapis.com/css2?family=Outfit:wght@400;500;600;700&display=swap" rel="stylesheet">
<!-- DataTables CSS -->
<link rel="stylesheet" type="text/css" href="https://cdn.datatables.net/1.13.7/css/dataTables.bootstrap5.min.css">
<style>
body { background: #f8fafc; font-family: 'Outfit', sans-serif; }
.topbar { background: #fff; box-shadow: 0 1px 3px rgba(0,0,0,0.05); padding: 12px 24px; position: sticky; top: 0; z-index: 50; }
.back-btn { text-decoration: none; color: #1e293b; font-weight: 600; display: flex; align-items: center; gap: 8px; }
.back-btn:hover { color: #0ea5e9; }
.card-header-custom { background: linear-gradient(135deg, #0ea5e9, #0284c7); color: white; border-radius: 12px 12px 0 0 !important; }
.filter-section { background: white; padding: 20px; border-radius: 12px; box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1); margin-bottom: 24px; }
.result-section { background: white; border-radius: 12px; box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1); overflow: hidden; padding: 20px;}
.table th { font-size: 13px; white-space: nowrap; background-color: #f1f5f9; font-weight: 600; color: #475569;}
.table td { font-size: 13px; vertical-align: middle; white-space: nowrap;}
.status-badge { font-size: 11px; padding: 4px 8px; border-radius: 6px; font-weight: 600;}
.status-checkin { background-color: #dcfce7; color: #166534; }
.status-batal { background-color: #fee2e2; color: #991b1b; }
.status-belum { background-color: #fef9c3; color: #854d0e; }
.status-gagal { background-color: #e0e7ff; color: #3730a3; }
.text-danger { color: #dc3545 !important; } /* Match user screenshot logic for rawat, no rm, etc */
</style>
</head>
<body>

<div class="topbar">
    <a href="bridging_dashboard.php" class="back-btn"><i class="bi bi-arrow-left"></i> Kembali ke Dashboard Bridging</a>
</div>

<div class="container-fluid px-4 mt-4">
    <h3 class="fw-bold mb-4"><i class="bi bi-phone me-2 text-primary"></i>Referensi Pendaftaran Mobile JKN</h3>
    
    <?php if (isset($error_msg)): ?>
        <div class="alert alert-danger alert-dismissible fade show" role="alert">
            <i class="bi bi-exclamation-triangle-fill me-2"></i> <?= htmlspecialchars($error_msg) ?>
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
    <?php endif; ?>

    <div class="filter-section">
        <form method="GET" action="">
            <div class="row align-items-end g-3">
                <div class="col-md-3">
                    <label class="form-label fw-medium"><i class="bi bi-calendar me-1"></i> Tanggal Mulai</label>
                    <input type="date" class="form-control" name="tgl_mulai" value="<?= htmlspecialchars($searchTglMulai) ?>">
                </div>
                <div class="col-md-3">
                    <label class="form-label fw-medium"><i class="bi bi-calendar-check me-1"></i> Tanggal Akhir</label>
                    <input type="date" class="form-control" name="tgl_akhir" value="<?= htmlspecialchars($searchTglAkhir) ?>">
                </div>
                <div class="col-md-4">
                    <label class="form-label fw-medium"><i class="bi bi-search me-1"></i> Pencarian</label>
                    <input type="text" class="form-control" name="keyword" placeholder="No. RM / No. Rawat / Nama / No. Antrean" value="<?= htmlspecialchars($keyword) ?>">
                </div>
                <div class="col-md-2">
                    <button type="submit" class="btn btn-primary w-100"><i class="bi bi-filter-circle me-1"></i> Tampilkan</button>
                </div>
            </div>
        </form>
    </div>

    <div class="result-section">
        <div class="table-responsive">
            <table id="tabelMobileJKN" class="table table-hover table-bordered table-striped w-100">
                <thead>
                    <tr>
                        <th>No.Rawat</th>
                        <th>No.RM</th>
                        <th>Nama Pasien</th>
                        <th>No.HP</th>
                        <th>No.Kartu</th>
                        <th>NIK</th>
                        <th>Tanggal</th>
                        <th>Poliklinik</th>
                        <th>Dokter</th>
                        <th>Jam Praktek</th>
                        <th>Jenis Kunjungan</th>
                        <th>Nomor Referensi</th>
                        <th>Status</th>
                        <th>Validasi Checkin</th>
                        <th>No.Booking</th>
                    </tr>
                </thead>
                <tbody>
                    <?php if (isset($data_mobilejkn) && count($data_mobilejkn) > 0): ?>
                        <?php foreach($data_mobilejkn as $row): ?>
                            
                            <?php 
                                // Set badge format untuk Status
                                $statusClass = 'status-belum';
                                if($row['status'] == 'Checkin') $statusClass = 'status-checkin';
                                if($row['status'] == 'Batal') $statusClass = 'status-batal';
                                if($row['status'] == 'Gagal') $statusClass = 'status-gagal';
                            ?>

                            <tr>
                                <td class="text-danger"><?= htmlspecialchars($row['no_rawat']) ?></td>
                                <td class="text-danger"><?= htmlspecialchars($row['norm']) ?></td>
                                <td class="text-danger"><?= htmlspecialchars($row['nm_pasien']) ?></td>
                                <td class="text-danger"><?= htmlspecialchars($row['nohp']) ?></td>
                                <td class="text-danger"><?= htmlspecialchars($row['nomorkartu']) ?></td>
                                <td class="text-danger"><?= htmlspecialchars($row['nik']) ?></td>
                                <td class="text-danger"><?= htmlspecialchars($row['tanggalperiksa']) ?></td>
                                <td><?= htmlspecialchars($row['nm_poli']) ?></td>
                                <td><?= htmlspecialchars($row['nm_dokter']) ?></td>
                                <td class="text-danger"><?= htmlspecialchars($row['jampraktek']) ?></td>
                                <td class="text-danger"><?= htmlspecialchars($row['jeniskunjungan']) ?></td>
                                <td class="text-danger"><?= htmlspecialchars($row['nomorreferensi']) ?></td>
                                <td class="text-danger"><?= htmlspecialchars($row['status']) ?></td>
                                <td class="text-danger"><?= htmlspecialchars($row['validasi']) ?></td>
                                <td class="text-danger"><?= htmlspecialchars($row['nobooking']) ?></td>
                            </tr>
                        <?php endforeach; ?>
                    <?php endif; ?>
                </tbody>
            </table>
        </div>
    </div>
</div>

<!-- Scripts -->
<script src="https://code.jquery.com/jquery-3.7.0.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<script src="https://cdn.datatables.net/1.13.7/js/jquery.dataTables.min.js"></script>
<script src="https://cdn.datatables.net/1.13.7/js/dataTables.bootstrap5.min.js"></script>
<script>
    $(document).ready(function() {
        $('#tabelMobileJKN').DataTable({
            language: {
                url: '//cdn.datatables.net/plug-ins/1.13.7/i18n/id.json',
            },
            ordering: false,
            pageLength: 25,
            scrollX: true,
            searching: false // Matikan search datatable bawaan krn pakai filter custom
        });
    });
</script>
</body>
</html>
