<?php
session_start();
include 'koneksi2.php';
date_default_timezone_set('Asia/Jakarta');

// Parameter Filter
$tgl_mulai = $_GET['tgl_mulai'] ?? date('Y-m-d');
$tgl_akhir = $_GET['tgl_akhir'] ?? date('Y-m-d');
$keyword = $_GET['keyword'] ?? '';

// Susun Query
$sql = "SELECT 
            rm.nobooking, rm.tanggalperiksa, rm.kodepoli, rm.kodedokter, rm.jampraktek, 
            rm.nik, rm.nomorkartu, rm.nohp, rm.norm, p.nm_pasien, 
            rm.jeniskunjungan, rm.nomorreferensi, rm.nomorantrean, 
            rm.estimasidilayani, rm.validasi, rm.status
        FROM referensi_mobilejkn_bpjs rm
        LEFT JOIN pasien p ON rm.norm = p.no_rkm_medis
        WHERE rm.tanggalperiksa BETWEEN :tgl_mulai AND :tgl_akhir";

$params = [
    ':tgl_mulai' => $tgl_mulai,
    ':tgl_akhir' => $tgl_akhir
];

if (!empty($keyword)) {
    $sql .= " AND (rm.nobooking LIKE :keyword OR rm.norm LIKE :keyword OR p.nm_pasien LIKE :keyword OR rm.nomorkartu LIKE :keyword OR rm.nik LIKE :keyword)";
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

// Ambil identitas rumah sakit
try {
    $stmtSet = $pdo_simrs->query("SELECT nama_instansi FROM setting LIMIT 1");
    $rs = $stmtSet->fetch(PDO::FETCH_ASSOC);
    $namaRS = $rs['nama_instansi'] ?? 'MediFix Hospital';
} catch (Exception $e) {
    $namaRS = 'MediFix Hospital';
}
?>
<!doctype html>
<html lang="id">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Cek Status Antrean Mobile JKN - <?= htmlspecialchars($namaRS) ?></title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.1/font/bootstrap-icons.css" rel="stylesheet">
<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css">
    <link href="https://fonts.googleapis.com/css2?family=Outfit:wght@400;500;600;700&display=swap" rel="stylesheet">
    <!-- DataTables CSS -->
    <link rel="stylesheet" type="text/css" href="https://cdn.datatables.net/1.13.7/css/dataTables.bootstrap5.min.css">
    <style>
        :root {
            --primary-gradient: linear-gradient(135deg, #0ea5e9, #0284c7);
            --bg-color: #f8fafc;
        }

        body { 
            background: var(--bg-color); 
            font-family: 'Outfit', sans-serif; 
            min-height: 100vh;
        }

        .top-banner {
            background: #fff;
            padding: 10px 20px;
            border-bottom: 2px solid #ef4444; /* Red top border like in screenshot */
            color: #1e293b;
            font-weight: 600;
            font-size: 0.9rem;
        }

        .page-content {
            padding: 20px;
        }

        .table-title {
            font-size: 1rem;
            color: #1e293b;
            margin-bottom: 5px;
            font-weight: 500;
        }

        .filter-card {
            background: white;
            border-radius: 12px;
            box-shadow: 0 1px 3px rgba(0,0,0,0.1);
            padding: 15px;
            margin-bottom: 20px;
        }

        .table-card {
            background: white;
            border-radius: 8px;
            box-shadow: 0 1px 3px rgba(0,0,0,0.1);
            padding: 0;
            overflow: hidden;
        }

        .table thead th {
            background-color: #f8fafc;
            color: #475569;
            font-size: 0.75rem;
            font-weight: 600;
            text-transform: capitalize;
            border-bottom: 2px solid #e2e8f0;
            white-space: nowrap;
            padding: 12px 8px;
            text-align: center;
        }

        .table tbody td {
            font-size: 0.75rem;
            padding: 10px 8px;
            color: #1e293b;
            vertical-align: middle;
            border-bottom: 1px solid #f1f5f9;
            white-space: nowrap;
            text-align: center;
        }

        .status-badge {
            font-size: 0.7rem;
            padding: 3px 8px;
            border-radius: 4px;
            font-weight: 600;
        }

        .status-checkin { background-color: #dcfce7; color: #166534; }
        .status-batal { background-color: #fee2e2; color: #991b1b; }
        .status-belum { background-color: #fef9c3; color: #854d0e; }
        .status-gagal { background-color: #e2e8f0; color: #475569; }

        .btn-back {
            text-decoration: none;
            color: #64748b;
            font-size: 0.9rem;
            display: inline-flex;
            align-items: center;
            gap: 5px;
        }

        .btn-back:hover { color: #0284c7; }

        /* Highlight matching screenshot row style (though not strictly necessary for functionality) */
        .table-hover tbody tr:hover {
            background-color: #f1f5f9;
        }

        .dataTables_wrapper .dataTables_length, 
        .dataTables_wrapper .dataTables_filter {
            padding: 15px;
            font-size: 0.85rem;
        }

        .dataTables_wrapper .dataTables_info,
        .dataTables_wrapper .dataTables_paginate {
            padding: 15px;
            font-size: 0.85rem;
        }
    
.btn-fullscreen {
    position: fixed;
    top: 18px;
    right: 18px;
    z-index: 10000;
    display: none;
    width: 38px;
    height: 38px;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    background: rgba(0, 0, 0, 0.45);
    backdrop-filter: blur(8px);
    -webkit-backdrop-filter: blur(8px);
    border-radius: 8px;
    border: none;
    color: #fff;
    transition: all 0.2s ease;
    box-shadow: 0 2px 8px rgba(0,0,0,0.2);
    text-decoration: none;
}
.btn-fullscreen:hover {
    background: rgba(0, 0, 0, 0.65);
    color: #fff;
    transform: scale(1.05);
    box-shadow: 0 4px 16px rgba(0,0,0,0.25);
}
.btn-fullscreen i {
    font-size: 0.95rem;
}
:fullscreen .btn-fullscreen,
:-webkit-full-screen .btn-fullscreen {
    display: none !important;
}
@keyframes fadeInToggle {
    from { opacity: 0; transform: translateY(5px); }
    to { opacity: 1; transform: translateY(0); }
}
}
</style>
</head>
<body>

<div class="top-banner">
    ::[ Antrean Per Tanggal Mobile JKN ]::
</div>

<div class="page-content">
    <div class="d-flex justify-content-between align-items-center mb-4">
        <div>
            <h4 class="fw-bold m-0"><?= htmlspecialchars($namaRS) ?></h4>
            <small class="text-muted">Monitoring Real-time Antrean Mobile JKN</small>
        </div>
        <a href="anjungan.php" class="btn-back">
            <i class="bi bi-house-fill"></i> Menu Utama
        </a>
    </div>

    <?php if (isset($error_msg)): ?>
        <div class="alert alert-danger rounded-3" role="alert">
            <i class="bi bi-exclamation-triangle-fill me-2"></i> <?= htmlspecialchars($error_msg) ?>
        </div>
    <?php endif; ?>

    <div class="filter-card">
        <form method="GET" class="row g-3 align-items-end">
            <div class="col-md-3">
                <label class="form-label small fw-bold">Tanggal Mulai</label>
                <input type="date" name="tgl_mulai" class="form-control form-control-sm" value="<?= htmlspecialchars($tgl_mulai) ?>">
            </div>
            <div class="col-md-3">
                <label class="form-label small fw-bold">Tanggal Akhir</label>
                <input type="date" name="tgl_akhir" class="form-control form-control-sm" value="<?= htmlspecialchars($tgl_akhir) ?>">
            </div>
            <div class="col-md-4">
                <label class="form-label small fw-bold">Pencarian</label>
                <input type="text" name="keyword" class="form-control form-control-sm" placeholder="Booking / RM / Nama / NIK" value="<?= htmlspecialchars($keyword) ?>">
            </div>
            <div class="col-md-2">
                <button type="submit" class="btn btn-primary btn-sm w-100 fw-bold">
                    <i class="bi bi-funnel-fill"></i> TAMPILKAN
                </button>
            </div>
        </form>
    </div>

    <div class="table-card">
        <div class="table-responsive">
            <table id="tabelAntrean" class="table table-hover w-100 m-0">
                <thead>
                    <tr>
                        <th>Kode Booking</th>
                        <th>Tanggal</th>
                        <th>Kode Poli</th>
                        <th>Kode Dokter</th>
                        <th>Jam Praktek</th>
                        <th>NIK</th>
                        <th>Noka</th>
                        <th>No. HP</th>
                        <th>RM</th>
                        <th>Jenis Kunjungan</th>
                        <th>No. Ref</th>
                        <th>Sumber Data</th>
                        <th>Peserta</th>
                        <th>No. Antrean</th>
                        <th>Estimasi Dilayani</th>
                        <th>Created Time</th>
                        <th>Status</th>
                    </tr>
                </thead>
                <tbody>
                    <?php if (!empty($data_mobilejkn)): ?>
                        <?php foreach($data_mobilejkn as $row): ?>
                            <?php 
                                $statusClass = 'status-belum';
                                if($row['status'] == 'Checkin') $statusClass = 'status-checkin';
                                elseif($row['status'] == 'Batal') $statusClass = 'status-batal';
                                elseif($row['status'] == 'Gagal') $statusClass = 'status-gagal';
                            ?>
                            <tr>
                                <td class="fw-bold"><?= htmlspecialchars($row['nobooking']) ?></td>
                                <td><?= htmlspecialchars($row['tanggalperiksa']) ?></td>
                                <td><?= htmlspecialchars($row['kodepoli']) ?></td>
                                <td><?= htmlspecialchars($row['kodedokter']) ?></td>
                                <td><?= htmlspecialchars($row['jampraktek']) ?></td>
                                <td><?= htmlspecialchars($row['nik']) ?></td>
                                <td><?= htmlspecialchars($row['nomorkartu']) ?></td>
                                <td><?= htmlspecialchars($row['nohp']) ?></td>
                                <td><?= htmlspecialchars($row['norm']) ?></td>
                                <td>
                                    <?php 
                                        $jk = $row['jeniskunjungan'];
                                        if (strpos($jk, '1') !== false) echo 'Rujukan FKTP';
                                        elseif (strpos($jk, '2') !== false) echo 'Internal';
                                        elseif (strpos($jk, '3') !== false) echo 'Kontrol';
                                        elseif (strpos($jk, '4') !== false) echo 'Rujukan RS';
                                        else echo htmlspecialchars($jk);
                                    ?>
                                </td>
                                <td><?= htmlspecialchars($row['nomorreferensi']) ?></td>
                                <td><span class="badge bg-info text-dark" style="font-size: 0.65rem;">Mobile JKN</span></td>
                                <td class="text-start fw-medium"><?= htmlspecialchars($row['nm_pasien'] ?? '-') ?></td>
                                <td class="fw-bold text-primary"><?= htmlspecialchars($row['nomorantrean']) ?></td>
                                <td><?= htmlspecialchars($row['estimasidilayani']) ?></td>
                                <td class="small"><?= htmlspecialchars($row['validasi']) ?></td>
                                <td>
                                    <span class="status-badge <?= $statusClass ?>">
                                        <?= htmlspecialchars($row['status']) ?>
                                    </span>
                                </td>
                            </tr>
                        <?php endforeach; ?>
                    <?php endif; ?>
                </tbody>
            </table>
        </div>
    </div>
</div>

<script src="https://code.jquery.com/jquery-3.7.0.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<script src="https://cdn.datatables.net/1.13.7/js/jquery.dataTables.min.js"></script>
<script src="https://cdn.datatables.net/1.13.7/js/dataTables.bootstrap5.min.js"></script>
<script>
    $(document).ready(function() {
        $('#tabelAntrean').DataTable({
            language: {
                url: '//cdn.datatables.net/plug-ins/1.13.7/i18n/id.json',
            },
            ordering: true,
            order: [[15, 'desc']], // Order by Created Time
            pageLength: 25,
            scrollX: true,
            dom: '<"d-flex justify-content-between align-items-center"lf>rt<"d-flex justify-content-between align-items-center"ip>'
        });
    });

function toggleFullscreen() {
    if (!document.fullscreenElement) {
        document.documentElement.requestFullscreen().catch(err => {
            console.error(`Error attempting to enable full-screen mode: ${err.message}`);
        });
    } else {
        if (document.exitFullscreen) {
            document.exitFullscreen();
        }
    }
}

document.addEventListener('fullscreenchange', () => {
    const icon = document.getElementById('fullscreenIcon');
    if (icon) {
        if (document.fullscreenElement) {
            icon.classList.replace('fa-arrows-alt', 'fa-compress');
        } else {
            icon.classList.replace('fa-compress', 'fa-arrows-alt');
        }
    }
});

// === AUTO FULLSCREEN: langsung fullscreen tanpa overlay ===
(function() {
    if (document.fullscreenElement || window.self !== window.top) return;

    // Coba langsung fullscreen saat load
    document.documentElement.requestFullscreen().catch(function() {
        // Browser butuh user gesture - tangkap interaksi pertama
        function autoFS() {
            if (!document.fullscreenElement) {
                document.documentElement.requestFullscreen().catch(function(){});
            }
            // Hapus semua listener setelah trigger
            document.removeEventListener('click', autoFS, true);
            document.removeEventListener('touchstart', autoFS, true);
            document.removeEventListener('keydown', autoFS, true);
        }
        // Gunakan capture phase agar jalan SEBELUM event handler lain,
        // tapi TANPA stopPropagation agar klik tetap sampai ke tombol
        document.addEventListener('click', autoFS, true);
        document.addEventListener('touchstart', autoFS, true);
        document.addEventListener('keydown', autoFS, true);
    });
})();

// Hide redundant button if inside Kiosk Wrapper (iframe)
if (window.self !== window.top) {
    const fsBtn = document.querySelector('.btn-fullscreen');
    if (fsBtn) fsBtn.style.display = 'none';
}
</script>

<!-- Button Fullscreen -->
<a class="btn-fullscreen" onclick="toggleFullscreen()" title="Toggle Fullscreen">
    <i class="fa fa-arrows-alt" id="fullscreenIcon"></i>
</a>
</body>
</html>
