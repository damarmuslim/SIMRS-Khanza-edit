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

// Pemetaan Task Name
function getTaskName($taskid) {
    switch ($taskid) {
        case '1': return '1. (Rencana) Mulai waktu tunggu admisi';
        case '2': return '2. (Rencana) Akhir waktu tunggu admisi/mulai waktu layan admisi';
        case '3': return '3. (Rencana) Akhir waktu layan admisi/mulai waktu tunggu poli';
        case '4': return '4. (Rencana) Akhir waktu tunggu poli/mulai waktu layan poli';
        case '5': return '5. (Rencana) Akhir waktu layan poli/mulai waktu tunggu farmasi';
        case '6': return '6. (Rencana) Akhir waktu tunggu farmasi/mulai waktu layan farmasi membuat obat';
        case '7': return '7. (Rencana) Akhir waktu layan farmasi membuat obat/selesai';
        case '99': return '99. Tidak Hadir/Batal';
        default: return 'Task ID Tidak Dikenal';
    }
}

// Susun Query
$sql = "SELECT 
            t.no_rawat, rm.norm, p.nm_pasien, rm.nohp, rm.nomorkartu, rm.nik, 
            rm.tanggalperiksa, pl.nm_poli, d.nm_dokter, 
            t.waktu AS waktu_rs, 
            UNIX_TIMESTAMP(t.waktu)*1000 AS waktu_task,
            t.taskid
        FROM referensi_mobilejkn_bpjs_taskid t
        INNER JOIN referensi_mobilejkn_bpjs rm ON t.no_rawat = rm.no_rawat
        LEFT JOIN pasien p ON rm.norm = p.no_rkm_medis
        LEFT JOIN poliklinik pl ON rm.kodepoli = pl.kd_poli
        LEFT JOIN dokter d ON rm.kodedokter = d.kd_dokter
        WHERE DATE(t.waktu) BETWEEN :tgl_mulai AND :tgl_akhir";

$params = [
    ':tgl_mulai' => $searchTglMulai,
    ':tgl_akhir' => $searchTglAkhir
];

if (!empty($keyword)) {
    $sql .= " AND (t.no_rawat LIKE :keyword OR rm.norm LIKE :keyword OR p.nm_pasien LIKE :keyword OR rm.nomorkartu LIKE :keyword OR rm.nik LIKE :keyword)";
    $params[':keyword'] = '%' . $keyword . '%';
}

$sql .= " ORDER BY t.waktu DESC";

try {
    $stmt = $pdo_simrs->prepare($sql);
    $stmt->execute($params);
    $data_task = $stmt->fetchAll(PDO::FETCH_ASSOC);
} catch (PDOException $e) {
    $error_msg = "Terjadi kesalahan kueri: " . $e->getMessage();
}

?>
<!doctype html>
<html lang="id">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width,initial-scale=1">
<title>Log Task ID Mobile JKN - MediFix</title>
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
.filter-section { background: white; padding: 20px; border-radius: 12px; box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1); margin-bottom: 24px; border-left: 4px solid #0ea5e9; }
.result-section { background: white; border-radius: 12px; box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1); overflow: hidden; padding: 20px;}
.table th { font-size: 13px; white-space: nowrap; background-color: #f1f5f9; font-weight: 600; color: #475569;}
.table td { font-size: 13px; vertical-align: middle; white-space: nowrap;}
.task-badge { font-size: 11px; padding: 4px 8px; border-radius: 6px; font-weight: 600; background-color: #e0f2fe; color: #0369a1; border: 1px solid #bae6fd; }
.text-primary-custom { color: #0ea5e9 !important; font-weight: 500; }
.header-title-box { background-color: #f1f5f9; padding: 6px 12px; border-radius: 6px; font-size: 14px; font-weight: 600; color: #334155; margin-bottom: 15px; display: inline-block; border-left: 3px solid #0ea5e9;}
</style>
</head>
<body>

<div class="topbar">
    <a href="bridging_dashboard.php" class="back-btn"><i class="bi bi-arrow-left"></i> Kembali ke Dashboard Bridging</a>
</div>

<div class="container-fluid px-4 mt-4">
    <div class="d-flex align-items-center mb-4">
        <h3 class="fw-bold m-0"><i class="bi bi-journal-check me-2 text-primary"></i>Log Task ID Mobile JKN BPJS</h3>
    </div>
    
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
                    <input type="text" class="form-control" name="keyword" placeholder="No. RM / No. Rawat / Nama / NIK" value="<?= htmlspecialchars($keyword) ?>">
                </div>
                <div class="col-md-2">
                    <button type="submit" class="btn btn-primary w-100"><i class="bi bi-filter-circle me-1"></i> Tampilkan</button>
                </div>
            </div>
        </form>
    </div>

    <div class="result-section">
        <div class="header-title-box">
            ::[ Task ID Mobile JKN ]::
        </div>
        <div class="table-responsive">
            <table id="tabelTaskID" class="table table-hover table-bordered table-striped w-100">
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
                        <th>Waktu RS</th>
                        <th>Waktu</th>
                        <th>Task Name</th>
                        <th>Task ID</th>
                    </tr>
                </thead>
                <tbody>
                    <?php if (isset($data_task) && count($data_task) > 0): ?>
                        <?php foreach($data_task as $row): ?>
                            <tr>
                                <td class="text-primary-custom"><?= htmlspecialchars($row['no_rawat']) ?></td>
                                <td class="text-primary-custom"><?= htmlspecialchars($row['norm']) ?></td>
                                <td class="text-primary-custom"><?= htmlspecialchars($row['nm_pasien']) ?></td>
                                <td class="text-primary-custom"><?= htmlspecialchars($row['nohp']) ?></td>
                                <td class="text-primary-custom"><?= htmlspecialchars($row['nomorkartu']) ?></td>
                                <td class="text-primary-custom"><?= htmlspecialchars($row['nik']) ?></td>
                                <td class="text-primary-custom"><?= htmlspecialchars($row['tanggalperiksa']) ?></td>
                                <td><?= htmlspecialchars($row['nm_poli']) ?></td>
                                <td><?= htmlspecialchars($row['nm_dokter']) ?></td>
                                <td class="text-primary-custom"><?= htmlspecialchars($row['waktu_rs']) ?></td>
                                <td class="text-primary-custom"><?= htmlspecialchars($row['waktu_task']) ?></td>
                                <td><span class="task-badge"><?= htmlspecialchars(getTaskName($row['taskid'])) ?></span></td>
                                <td class="text-center fw-bold"><?= htmlspecialchars($row['taskid']) ?></td>
                            </tr>
                        <?php endforeach; ?>
                    <?php else: ?>
                        <!-- Let DataTables handle empty state, or we can just leave it to DataTables fallback -->
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
        $('#tabelTaskID').DataTable({
            language: {
                url: '//cdn.datatables.net/plug-ins/1.13.7/i18n/id.json',
            },
            ordering: false,
            pageLength: 25,
            scrollX: true,
            searching: false // Matikan search datatable bawaan krn pakai filter custom PHP
        });
    });
</script>
</body>
</html>
