<?php
session_start();
if (!isset($_SESSION['user_id'])) {
    header('Location: login.php');
    exit;
}
include 'koneksi2.php';

$no_rawat = $_GET['no_rawat'] ?? '';
if (empty($no_rawat)) {
    header('Location: data_casemix.php');
    exit;
}

// Ambil data pasien & SEP
$stmt = $pdo_simrs->prepare("SELECT rp.no_rawat, p.no_rkm_medis, p.nm_pasien, p.tgl_lahir, p.jk, bs.no_sep, bs.diagawal as diagnosa_sep, pj.png_jawab
                             FROM reg_periksa rp
                             JOIN pasien p ON rp.no_rkm_medis = p.no_rkm_medis
                             LEFT JOIN bridging_sep bs ON rp.no_rawat = bs.no_rawat
                             JOIN penjab pj ON rp.kd_pj = pj.kd_pj
                             WHERE rp.no_rawat = ?");
$stmt->execute([$no_rawat]);
$pasien = $stmt->fetch();

if (!$pasien) {
    die("Data pasien tidak ditemukan.");
}

// Ambil Diagnosa Terpasang (ICD-10)
$stmt_diag = $pdo_simrs->prepare("SELECT dp.kd_penyakit, p.nm_penyakit, dp.prioritas, dp.status 
                                  FROM diagnosa_pasien dp 
                                  JOIN penyakit p ON dp.kd_penyakit = p.kd_penyakit 
                                  WHERE dp.no_rawat = ? ORDER BY dp.prioritas ASC");
$stmt_diag->execute([$no_rawat]);
$diagnosa = $stmt_diag->fetchAll();

// Ambil Prosedur Terpasang (ICD-9)
$stmt_pro = $pdo_simrs->prepare("SELECT pp.kode, p.deskripsi_panjang, pp.prioritas 
                                 FROM prosedur_pasien pp 
                                 JOIN icd9 p ON pp.kode = p.kode 
                                 WHERE pp.no_rawat = ? ORDER BY pp.prioritas ASC");
$stmt_pro->execute([$no_rawat]);
$prosedur = $stmt_pro->fetchAll();
?>
<!doctype html>
<html lang="id">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width,initial-scale=1">
    <title>Koding Diagnosa - MediFix</title>
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
        .card-custom { background: #fff; border-radius: 16px; border: 1px solid var(--slate-200); box-shadow: 0 4px 6px -1px rgba(0,0,0,0.1); margin-bottom: 24px; }
        .card-header-custom { padding: 20px 24px; border-bottom: 1px solid var(--slate-200); background: #fff; border-radius: 16px 16px 0 0; display: flex; align-items: center; justify-content: space-between; }
        .patient-info-box { background: linear-gradient(135deg, var(--purple-600), #7e22ce); color: #fff; padding: 24px; border-radius: 16px; margin-bottom: 24px; }
        .info-label { opacity: 0.8; font-size: 12px; font-weight: 600; text-transform: uppercase; letter-spacing: 0.05em; margin-bottom: 4px; }
        .info-value { font-size: 16px; font-weight: 700; }
        .btn-add { background: var(--purple-600); color: #fff; font-weight: 600; border-radius: 8px; transition: 0.2s; }
        .btn-add:hover { background: #7e22ce; color: #fff; transform: translateY(-1px); }
        .table thead th { background: var(--slate-100); color: #64748b; font-weight: 700; text-transform: uppercase; font-size: 11px; letter-spacing: 0.05em; padding: 12px 16px; border: none; }
    </style>
</head>
<body>
<div class="topbar">
    <a href="data_casemix.php" class="back-btn"><i class="bi bi-arrow-left"></i> Kembali ke Data Casemix</a>
    <div class="badge bg-purple-subtle text-purple py-2 px-3 rounded-pill fw-bold" style="background:#f3e8ff; color:#9333ea;">Modul Koding ICD-10/9</div>
</div>

<div class="container mt-4 pb-5">
    <!-- Patient Profile -->
    <div class="patient-info-box">
        <div class="row align-items-center">
            <div class="col-md-auto mb-3 mb-md-0">
                <div class="bg-white text-purple d-flex align-items-center justify-content-center rounded-circle" style="width: 64px; height: 64px;">
                    <i class="bi bi-person-fill fs-2"></i>
                </div>
            </div>
            <div class="col-md">
                <div class="row">
                    <div class="col-md-3">
                        <div class="info-label">No. Rawat</div>
                        <div class="info-value"><?= $pasien['no_rawat'] ?></div>
                    </div>
                    <div class="col-md-4">
                        <div class="info-label">Nama Pasien / No. RM</div>
                        <div class="info-value"><?= $pasien['nm_pasien'] ?> (<?= $pasien['no_rkm_medis'] ?>)</div>
                    </div>
                    <div class="col-md-2">
                        <div class="info-label">Tgl. Lahir / JK</div>
                        <div class="info-value"><?= $pasien['tgl_lahir'] ?> / <?= $pasien['jk'] ?></div>
                    </div>
                    <div class="col-md-3">
                        <div class="info-label">Asuransi / SEP</div>
                        <div class="info-value"><?= $pasien['png_jawab'] ?> / <span class="badge bg-white text-purple"><?= $pasien['no_sep'] ?: '-' ?></span></div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <div class="row">
        <!-- Diagnosa ICD-10 -->
        <div class="col-lg-6">
            <div class="card-custom">
                <div class="card-header-custom">
                    <h5 class="mb-0 fw-bold"><i class="bi bi-activity me-2 text-primary"></i> Diagnosa (ICD-10)</h5>
                    <button class="btn btn-sm btn-add" data-bs-toggle="modal" data-bs-target="#modalDiagnosa"><i class="bi bi-plus-lg"></i> Tambah</button>
                </div>
                <div class="table-responsive">
                    <table class="table table-hover mb-0">
                        <thead>
                            <tr>
                                <th class="ps-4">Kode</th>
                                <th>Nama Penyakit</th>
                                <th>Prioritas</th>
                                <th class="text-center pe-4">Aksi</th>
                            </tr>
                        </thead>
                        <tbody>
                            <?php if (empty($diagnosa)): ?>
                                <tr><td colspan="4" class="text-center py-4 text-muted">Belum ada diagnosa terinput.</td></tr>
                            <?php else: ?>
                                <?php foreach ($diagnosa as $dg): ?>
                                    <tr>
                                        <td class="ps-4 fw-bold text-primary"><?= $dg['kd_penyakit'] ?></td>
                                        <td><?= $dg['nm_penyakit'] ?></td>
                                        <td><span class="badge <?= $dg['prioritas'] == 1 ? 'bg-danger' : 'bg-secondary' ?>"><?= $dg['prioritas'] == 1 ? 'Primer' : 'Sekunder ' . ($dg['prioritas']-1) ?></span></td>
                                        <td class="text-center pe-4">
                                            <button class="btn btn-sm text-danger"><i class="bi bi-trash"></i></button>
                                        </td>
                                    </tr>
                                <?php endforeach; ?>
                            <?php endif; ?>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>

        <!-- Prosedur ICD-9 -->
        <div class="col-lg-6">
            <div class="card-custom">
                <div class="card-header-custom">
                    <h5 class="mb-0 fw-bold"><i class="bi bi-scissors me-2 text-success"></i> Prosedur (ICD-9)</h5>
                    <button class="btn btn-sm btn-add" data-bs-toggle="modal" data-bs-target="#modalProsedur"><i class="bi bi-plus-lg"></i> Tambah</button>
                </div>
                <div class="table-responsive">
                    <table class="table table-hover mb-0">
                        <thead>
                            <tr>
                                <th class="ps-4">Kode</th>
                                <th>Deskripsi Prosedur</th>
                                <th class="text-center pe-4">Aksi</th>
                            </tr>
                        </thead>
                        <tbody>
                            <?php if (empty($prosedur)): ?>
                                <tr><td colspan="3" class="text-center py-4 text-muted">Belum ada prosedur terinput.</td></tr>
                            <?php else: ?>
                                <?php foreach ($prosedur as $pr): ?>
                                    <tr>
                                        <td class="ps-4 fw-bold text-success"><?= $pr['kode'] ?></td>
                                        <td><?= $pr['deskripsi_panjang'] ?></td>
                                        <td class="text-center pe-4">
                                            <button class="btn btn-sm text-danger"><i class="bi bi-trash"></i></button>
                                        </td>
                                    </tr>
                                <?php endforeach; ?>
                            <?php endif; ?>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>
</div>

<!-- Modal Diagnosa (ICD-10) -->
<div class="modal fade" id="modalDiagnosa" tabindex="-1">
    <div class="modal-dialog modal-lg">
        <div class="modal-content rounded-4 border-0">
            <div class="modal-header border-0 pb-0">
                <h5 class="modal-title fw-bold">Input ICD-10</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <div class="modal-body p-4">
                <div class="input-group mb-3">
                    <input type="text" id="searchDiagnosa" class="form-control" placeholder="Cari Kode atau Nama Penyakit...">
                    <button class="btn btn-primary" onclick="cariDiagnosa()"><i class="bi bi-search"></i></button>
                </div>
                <div id="hasilCariDiagnosa" class="list-group">
                    <!-- Hasil cari muncul disini -->
                    <div class="text-center text-muted py-3">Gunakan pencarian untuk menemukan ICD-10.</div>
                </div>
            </div>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<script>
async function cariDiagnosa() {
    const query = document.getElementById('searchDiagnosa').value;
    const container = document.getElementById('hasilCariDiagnosa');
    
    if(query.length < 3) {
        alert('Masukkan minimal 3 karakter untuk mencari');
        return;
    }
    
    container.innerHTML = '<div class="text-center py-3"><div class="spinner-border text-primary" role="status"></div></div>';
    
    try {
        const response = await fetch(`api_search_diagnosa.php?q=${encodeURIComponent(query)}`);
        const data = await response.json();
        
        if(data.length === 0) {
            container.innerHTML = '<div class="text-center text-muted py-3">Tidak ada hasil ditemukan.</div>';
            return;
        }
        
        let html = '';
        data.forEach(item => {
            html += `
                <a href="#" class="list-group-item list-group-item-action d-flex justify-content-between align-items-center" onclick="pilihDiagnosa('${item.kd_penyakit}', '${item.nm_penyakit.replace(/'/g, "\\'")}')">
                    <div>
                        <span class="fw-bold text-primary">${item.kd_penyakit}</span> - ${item.nm_penyakit}
                    </div>
                    <i class="bi bi-plus-circle text-primary"></i>
                </a>
            `;
        });
        container.innerHTML = html;
        
    } catch (error) {
        container.innerHTML = '<div class="alert alert-danger">Terjadi kesalahan saat mencari data.</div>';
    }
}

async function cariProsedur() {
    const query = document.getElementById('searchProsedur').value;
    const container = document.getElementById('hasilCariProsedur');
    
    if(query.length < 3) {
        alert('Masukkan minimal 3 karakter untuk mencari');
        return;
    }
    
    container.innerHTML = '<div class="text-center py-3"><div class="spinner-border text-success" role="status"></div></div>';
    
    try {
        const response = await fetch(`api_search_prosedur.php?q=${encodeURIComponent(query)}`);
        const data = await response.json();
        
        if(data.length === 0) {
            container.innerHTML = '<div class="text-center text-muted py-3">Tidak ada hasil ditemukan.</div>';
            return;
        }
        
        let html = '';
        data.forEach(item => {
            html += `
                <a href="#" class="list-group-item list-group-item-action d-flex justify-content-between align-items-center" onclick="simpanKoding('${item.kode}', 'prosedur')">
                    <div>
                        <span class="fw-bold text-success">${item.kode}</span> - ${item.deskripsi_panjang}
                    </div>
                    <i class="bi bi-plus-circle text-success"></i>
                </a>
            `;
        });
        container.innerHTML = html;
    } catch (error) {
        container.innerHTML = '<div class="alert alert-danger">Terjadi kesalahan saat mencari data.</div>';
    }
}

function pilihDiagnosa(kode, nama) {
    if(confirm(`Tambahkan diagnosa ${kode} - ${nama}?`)) {
        simpanKoding(kode, 'diagnosa');
    }
}

async function simpanKoding(kode, type) {
    const formData = new FormData();
    formData.append('no_rawat', '<?= $no_rawat ?>');
    formData.append('kode', kode);
    formData.append('type', type);

    try {
        const response = await fetch('api_save_koding.php', {
            method: 'POST',
            body: formData
        });
        const result = await response.json();
        
        if(result.status === 'success') {
            location.reload();
        } else {
            alert('Gagal menyimpan: ' + result.message);
        }
    } catch (error) {
        alert('Terjadi kesalahan jaringan.');
    }
}

async function hapusKoding(kode, type) {
    if(!confirm(`Hapus ${type} ${kode}?`)) return;

    const formData = new FormData();
    formData.append('no_rawat', '<?= $no_rawat ?>');
    formData.append('kode', kode);
    formData.append('type', type);
    formData.append('action', 'delete');

    try {
        const response = await fetch('api_save_koding.php', {
            method: 'POST',
            body: formData
        });
        const result = await response.json();
        
        if(result.status === 'success') {
            location.reload();
        } else {
            alert('Gagal menghapus: ' + result.message);
        }
    } catch (error) {
        alert('Terjadi kesalahan jaringan.');
    }
}
</script>
</body>
</html>
