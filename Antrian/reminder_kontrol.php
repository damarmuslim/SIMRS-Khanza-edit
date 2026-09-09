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

// Cek izin akses poliklinik
if (!isset($_SESSION['akses']['poliklinik']) || $_SESSION['akses']['poliklinik'] != 1) {
    header("Location: forbidden.php");
    exit;
}

// Filter Tanggal Rencana Kontrol (Default besok)
$tgl_rencana = $_GET['tgl_rencana'] ?? date('Y-m-d', strtotime('+1 day'));
$search      = $_GET['search'] ?? '';

// Query Data Surat Kontrol (Union BPJS Bridging & Lokal SKDP)
try {
    $sql = "SELECT * FROM (
                SELECT 
                    sk.no_surat, 
                    sk.tgl_rencana as tgl_datang, 
                    sk.nm_dokter_bpjs as nm_dokter, 
                    sk.nm_poli_bpjs as nm_poli,
                    sep.nomr,
                    p.nm_pasien,
                    p.no_tlp,
                    'BPJS' as sumber
                FROM bridging_surat_kontrol_bpjs sk
                INNER JOIN bridging_sep sep ON sk.no_sep = sep.no_sep
                INNER JOIN pasien p ON sep.nomr = p.no_rkm_medis
                
                UNION ALL
                
                SELECT 
                    s.no_antrian as no_surat, 
                    DATE(s.tanggal_datang) as tgl_datang, 
                    d.nm_dokter, 
                    COALESCE(sp.nm_sps, 'Poliklinik') as nm_poli,
                    s.no_rkm_medis as nomr,
                    p.nm_pasien,
                    p.no_tlp,
                    'Lokal' as sumber
                FROM skdp_bpjs s
                INNER JOIN pasien p ON s.no_rkm_medis = p.no_rkm_medis
                INNER JOIN dokter d ON s.kd_dokter = d.kd_dokter
                LEFT JOIN spesialis sp ON d.kd_sps = sp.kd_sps
            ) AS gabungan
            WHERE tgl_datang = :tgl_rencana";
    
    if (!empty($search)) {
        $sql .= " AND (nm_pasien LIKE :search OR nomr LIKE :search OR no_surat LIKE :search)";
    }
    
    $sql .= " ORDER BY nm_poli ASC, nm_pasien ASC";
    
    $stmt = $pdo_simrs->prepare($sql);
    $params = ['tgl_rencana' => $tgl_rencana];
    
    if (!empty($search)) {
        $params['search'] = "%$search%";
    }
    
    $stmt->execute($params);
    $data_kontrol = $stmt->fetchAll(PDO::FETCH_ASSOC);
} catch (Exception $e) {
    $error = "Gagal mengambil data: " . $e->getMessage();
}

// Ambil Semua Template WA
$stmtTemplates = $pdo->query("SELECT * FROM wa_templates ORDER BY judul ASC");
$allTemplates = $stmtTemplates->fetchAll(PDO::FETCH_ASSOC);

// Ambil Template Terpilih (Default ke template_id dari URL atau yang is_default=1)
$template_id = $_GET['template_id'] ?? null;
if (!$template_id) {
    foreach ($allTemplates as $t) {
        if ($t['is_default'] == 1) {
            $template_id = $t['id'];
            break;
        }
    }
}

// Jika masih null, ambil yang pertama
if (!$template_id && !empty($allTemplates)) {
    $template_id = $allTemplates[0]['id'];
}

// Ambil Konten Template Terpilih
$currentTemplate = null;
foreach ($allTemplates as $t) {
    if ($t['id'] == $template_id) {
        $currentTemplate = $t;
        break;
    }
}

// Ambil Nama RS dari setting_wa (Global)
$stmtRS = $pdo->query("SELECT nama_rs FROM setting_wa LIMIT 1");
$settingRS = $stmtRS->fetch(PDO::FETCH_ASSOC);
$namaRS = $settingRS['nama_rs'] ?? "MediFix Hospital";

// ==== SIMPAN / TAMBAH / HAPUS TEMPLATE (POST) ====
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    if (isset($_POST['save_template'])) {
        try {
            $judul_save = strip_tags($_POST['judul_template']);
            $template_save = strip_tags($_POST['template_pesan']);
            $tid = $_POST['template_id'];
            
            $stmtUpdate = $pdo->prepare("UPDATE wa_templates SET judul=?, template_pesan=? WHERE id=?");
            $stmtUpdate->execute([$judul_save, $template_save, $tid]);
            
            // Update Nama RS (Global)
            $stmtRSUpdate = $pdo->prepare("UPDATE setting_wa SET nama_rs=? LIMIT 1");
            $stmtRSUpdate->execute([strip_tags($_POST['nama_rs'])]);
            
            header("Location: reminder_kontrol.php?tgl_rencana=$tgl_rencana&template_id=$tid&success=1");
            exit;
        } catch (Exception $e) {
            $error = "Gagal simpan template: " . $e->getMessage();
        }
    }
    
    if (isset($_POST['add_template'])) {
        try {
            $judul = "Template Baru " . date('His');
            $isi = "Halo {nama_pasien}, ini pesan baru dari {nama_rs}.";
            $stmtAdd = $pdo->prepare("INSERT INTO wa_templates (judul, template_pesan) VALUES (?, ?)");
            $stmtAdd->execute([$judul, $isi]);
            $newId = $pdo->lastInsertId();
            header("Location: reminder_kontrol.php?tgl_rencana=$tgl_rencana&template_id=$newId&success=2");
            exit;
        } catch (Exception $e) {
            $error = "Gagal menambah template: " . $e->getMessage();
        }
    }

    if (isset($_POST['delete_template'])) {
        try {
            $tid = $_POST['template_id'];
            // Jangan hapus jika tinggal 1
            $count = $pdo->query("SELECT COUNT(*) FROM wa_templates")->fetchColumn();
            if ($count > 1) {
                $stmtDel = $pdo->prepare("DELETE FROM wa_templates WHERE id=?");
                $stmtDel->execute([$tid]);
                header("Location: reminder_kontrol.php?tgl_rencana=$tgl_rencana&success=3");
            } else {
                $error = "Minimal harus ada 1 template!";
            }
            exit;
        } catch (Exception $e) {
            $error = "Gagal menghapus template: " . $e->getMessage();
        }
    }
}

if (isset($_GET['success'])) {
    if ($_GET['success'] == 1) $success_msg = "Template berhasil diperbarui!";
    if ($_GET['success'] == 2) $success_msg = "Template baru berhasil ditambahkan!";
    if ($_GET['success'] == 3) $success_msg = "Template berhasil dihapus!";
}

/**
 * Format nomor WhatsApp agar diawali dengan 62
 */
function formatWA($no) {
    $no = preg_replace('/[^0-9]/', '', $no);
    if (empty($no)) return "";
    if (str_starts_with($no, '0')) {
        $no = '62' . substr($no, 1);
    } elseif (str_starts_with($no, '8')) {
        $no = '62' . $no;
    }
    return $no;
}

/**
 * Template Pesan WhatsApp
 */
function getPesanWA($row) {
    global $currentTemplate, $namaRS;
    $tgl = date('d-m-Y', strtotime($row['tgl_datang']));
    
    $template = $currentTemplate['template_pesan'] ?? "Halo {nama_pasien}, jadwal kontrol Anda {tgl_kontrol}.";
    
    $pesan = str_replace(
        ['{nama_pasien}', '{tgl_kontrol}', '{nama_poli}', '{nama_dokter}', '{no_surat}', '{nama_rs}'],
        [$row['nm_pasien'], $tgl, $row['nm_poli'], $row['nm_dokter'], $row['no_surat'], $namaRS],
        $template
    );
    
    return urlencode($pesan);
}

?>
<!doctype html>
<html lang="id">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width,initial-scale=1">
<title>Reminder Jadwal Kontrol - MediFix</title>
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
<link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.1/font/bootstrap-icons.css" rel="stylesheet">
<link href="https://fonts.googleapis.com/css2?family=Outfit:wght@300;400;500;600;700;800&family=Plus+Jakarta+Sans:ital,wght@0,400;0,500;0,600;0,700;1,400&display=swap" rel="stylesheet">
<style>
:root {
  --wa-green: #25D366;
  --wa-dark: #128C7E;
  --teal-50:  #f0fdfa; --teal-500: #14b8a6; --teal-600: #0d9488;
  --slate-50: #f8fafc; --slate-100: #f1f5f9; --slate-200: #e2e8f0; --slate-500: #64748b; --slate-800: #1e293b; --slate-900: #0f172a;
}
body { background: var(--slate-50); font-family: 'Plus Jakarta Sans', sans-serif; color: var(--slate-800); }
.topbar { background: #fff; box-shadow: 0 1px 3px rgba(0,0,0,0.05); padding: 12px 24px; display: flex; align-items: center; justify-content: space-between; position: sticky; top: 0; z-index: 50; }
.back-btn { display: flex; align-items: center; gap: 8px; font-weight: 600; color: var(--slate-800); text-decoration: none; padding: 8px 16px; border-radius: 8px; transition: 0.2s; }
.back-btn:hover { background: var(--slate-100); color: var(--teal-600); }

.page-header { background: #fff; border-bottom: 1px solid var(--slate-200); padding: 24px 0; margin-bottom: 24px; }
.page-title { font-family: 'Outfit', sans-serif; font-weight: 800; color: var(--slate-900); margin: 0; }

.card-container { background: #fff; border-radius: 16px; border: 1px solid var(--slate-200); box-shadow: 0 4px 6px -1px rgba(0,0,0,0.05); overflow: hidden; }
.filter-section { padding: 20px; background: var(--slate-50); border-bottom: 1px solid var(--slate-200); }

.table th { background: var(--slate-100); color: var(--slate-500); font-weight: 600; font-size: 13px; text-transform: uppercase; letter-spacing: 0.025em; border-top: none; padding: 12px 16px; }
.table td { padding: 16px; vertical-align: middle; font-size: 14px; border-bottom: 1px solid var(--slate-100); }
.table tr:hover { background: var(--slate-50); }

.btn-wa { 
    background: var(--wa-green); 
    color: #fff; 
    border: none; 
    font-weight: 700; 
    padding: 8px 16px; 
    border-radius: 10px; 
    transition: 0.2s; 
    text-decoration: none; 
    display: inline-flex; 
    align-items: center; 
    gap: 8px;
    box-shadow: 0 4px 12px rgba(37, 211, 102, 0.3);
}
.btn-wa:hover { background: var(--wa-dark); color: #fff; transform: translateY(-2px); box-shadow: 0 6px 15px rgba(37, 211, 102, 0.4); }
.btn-wa.disabled { background: #cbd5e1; box-shadow: none; cursor: not-allowed; }

.status-badge { font-size: 11px; font-weight: 800; text-transform: uppercase; padding: 4px 10px; border-radius: 50px; }
.bg-rencana { background: var(--teal-50); color: var(--teal-600); }

.form-control { border-radius: 10px; border: 1px solid var(--slate-200); padding: 12px 16px; }
.form-control:focus { border-color: var(--teal-500); box-shadow: 0 0 0 4px rgba(20, 184, 166, 0.1); }

/* Modal Preview */
.preview-wa {
    background: #e5ddd5;
    background-image: url("https://user-images.githubusercontent.com/15075759/28719144-86dc0f70-73b1-11e7-911d-60d70fcded21.png");
    border-radius: 10px;
    padding: 20px;
}
.chat-bubble {
    background: #fff;
    padding: 12px;
    border-radius: 8px;
    position: relative;
    box-shadow: 0 1px 2px rgba(0,0,0,0.1);
    font-size: 13px;
    white-space: pre-wrap;
}
.chat-bubble::before {
    content: "";
    position: absolute;
    left: -8px;
    top: 0;
    border-width: 8px 8px 8px 0;
    border-style: solid;
    border-color: #fff transparent transparent transparent;
}
.var-badge {
    display: inline-block;
    background: #f1f5f9;
    color: #475569;
    padding: 2px 8px;
    border-radius: 4px;
    font-size: 11px;
    font-family: monospace;
    cursor: pointer;
    margin: 2px;
    border: 1px solid #e2e8f0;
}
.var-badge:hover { background: #e2e8f0; }

/* Template List Sidebar */
.template-list {
    border-right: 1px solid var(--slate-200);
    max-height: 400px;
    overflow-y: auto;
}
.template-item {
    padding: 12px 16px;
    border-radius: 8px;
    cursor: pointer;
    transition: 0.2s;
    margin-bottom: 4px;
    border: 1px solid transparent;
    display: flex;
    justify-content: space-between;
    align-items: center;
}
.template-item:hover { background: var(--slate-100); }
.template-item.active { 
    background: var(--teal-50); 
    border-color: var(--teal-500); 
    color: var(--teal-600); 
    font-weight: 700;
}
.template-item .judul { font-size: 13px; }
.template-item .icon { font-size: 18px; color: var(--wa-green); }
</style>
</head>
<body>

<div class="topbar">
    <a href="dashboard.php" class="back-btn"><i class="bi bi-arrow-left"></i> Kembali ke Dashboard</a>
    <div class="d-flex align-items-center gap-3">
        <span class="fw-medium text-secondary"><i class="bi bi-person-fill me-1"></i> <?= htmlspecialchars($nama) ?></span>
    </div>
</div>

<div class="page-header">
    <div class="container">
        <div class="d-flex justify-content-between align-items-center">
            <div>
                <h2 class="page-title"><i class="bi bi-whatsapp text-success me-2"></i> Reminder Jadwal Kontrol</h2>
                <p class="text-secondary mb-0">Kirim pengingat jadwal kontrol pasien via WhatsApp.</p>
            </div>
            <div class="text-end d-flex gap-2">
                <button type="button" class="btn btn-outline-success rounded-3 fw-bold" data-bs-toggle="modal" data-bs-target="#modalTemplate">
                    <i class="bi bi-gear-fill me-1"></i> Atur Template WA
                </button>
                <span class="badge bg-rencana py-2 px-3 rounded-3 d-flex align-items-center">
                    <i class="bi bi-calendar-check me-1"></i> <?= date('d M Y', strtotime($tgl_rencana)) ?>
                </span>
            </div>
        </div>
    </div>
</div>

<div class="container pb-5">
    <div class="card-container">
        <div class="filter-section">
            <form method="GET" class="row g-3">
                <div class="col-md-3">
                    <label class="form-label small fw-bold text-secondary">Pilih Template Pesan</label>
                    <select name="template_id" class="form-select border-0 bg-white shadow-sm" onchange="this.form.submit()" style="border-radius: 10px; padding: 12px;">
                        <?php foreach($allTemplates as $t): ?>
                            <option value="<?= $t['id'] ?>" <?= $template_id == $t['id'] ? 'selected' : '' ?>>
                                <?= htmlspecialchars($t['judul']) ?>
                            </option>
                        <?php endforeach; ?>
                    </select>
                </div>
                <div class="col-md-3">
                    <label class="form-label small fw-bold text-secondary">Tanggal Rencana</label>
                    <input type="date" name="tgl_rencana" class="form-control border-0 shadow-sm" value="<?= htmlspecialchars($tgl_rencana) ?>">
                </div>
                <div class="col-md-4">
                    <label class="form-label small fw-bold text-secondary">Pencarian Pasien / RM / No. Surat</label>
                    <div class="input-group shadow-sm" style="border-radius: 10px; overflow: hidden;">
                        <span class="input-group-text bg-white border-0"><i class="bi bi-search"></i></span>
                        <input type="text" name="search" class="form-control border-0" placeholder="Ketik nama atau RM..." value="<?= htmlspecialchars($search) ?>">
                    </div>
                </div>
                <div class="col-md-2 d-flex align-items-end">
                    <button type="submit" class="btn btn-dark w-100 rounded-3 py-2 fw-bold shadow-sm">
                        <i class="bi bi-filter me-1"></i> Tampilkan
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
                        <th>Pasien & RM</th>
                        <th>No. Surat Kontrol</th>
                        <th>Poli Tujuan</th>
                        <th>Dokter Spesialis</th>
                        <th>No. WhatsApp</th>
                        <th class="text-center">Aksi Reminder</th>
                    </tr>
                </thead>
                <tbody>
                    <?php if (empty($data_kontrol)): ?>
                        <tr>
                            <td colspan="6" class="text-center py-5">
                                <i class="bi bi-chat-left-dots text-secondary" style="font-size: 48px;"></i>
                                <p class="text-secondary mt-2 mb-0">Tidak ada jadwal kontrol ditemukan untuk tanggal ini.</p>
                            </td>
                        </tr>
                    <?php else: ?>
                        <?php foreach($data_kontrol as $row): ?>
                        <tr>
                            <td>
                                <div class="fw-bold text-dark"><?= htmlspecialchars($row['nm_pasien']) ?></div>
                                <div class="small text-secondary">RM: <?= htmlspecialchars($row['nomr']) ?></div>
                            </td>
                            <td>
                                <span class="badge bg-light text-dark border fw-bold"><?= htmlspecialchars($row['no_surat']) ?></span>
                                <div class="mt-1"><span class="badge <?= $row['sumber'] == 'BPJS' ? 'bg-primary' : 'bg-info' ?> rounded-pill" style="font-size: 10px;"><?= $row['sumber'] ?></span></div>
                            </td>
                            <td>
                                <div class="fw-bold"><?= htmlspecialchars($row['nm_poli']) ?></div>
                            </td>
                            <td>
                                <div class="small text-secondary"><?= htmlspecialchars($row['nm_dokter']) ?></div>
                            </td>
                            <td>
                                <?php if (!empty($row['no_tlp'])): ?>
                                    <div class="fw-bold text-primary"><i class="bi bi-telephone me-1"></i> <?= htmlspecialchars($row['no_tlp']) ?></div>
                                <?php else: ?>
                                    <span class="text-danger small"><i class="bi bi-x-circle me-1"></i> Tidak ada No. HP</span>
                                <?php endif; ?>
                            </td>
                            <td class="text-center">
                                <?php if (!empty($row['no_tlp'])): ?>
                                    <a href="https://api.whatsapp.com/send?phone=<?= formatWA($row['no_tlp']) ?>&text=<?= getPesanWA($row) ?>" 
                                       target="_blank" class="btn-wa">
                                        <i class="bi bi-whatsapp"></i> Kirim WA
                                    </a>
                                <?php else: ?>
                                    <button class="btn-wa disabled" disabled>
                                        <i class="bi bi-whatsapp"></i> No. HP Kosong
                                    </button>
                                <?php endif; ?>
                            </td>
                        </tr>
                        <?php endforeach; ?>
                    <?php endif; ?>
                </tbody>
            </table>
        </div>
    </div>
    
    <div class="mt-4 p-3 bg-white border rounded-3">
        <h6 class="fw-bold"><i class="bi bi-info-circle-fill text-primary me-2"></i> Petunjuk Penggunaan</h6>
        <ul class="small text-secondary mb-0">
            <li>Gunakan filter tanggal untuk mencari jadwal kontrol di hari tertentu (Default: Besok).</li>
            <li>Klik tombol <strong>"Kirim WA"</strong> untuk membuka jendela WhatsApp dengan pesan otomatis.</li>
            <li>Pastikan nomor telepon pasien sudah terdaftar di SIMRS dengan format yang benar.</li>
            <li>Fitur ini menggunakan WhatsApp Web/Aplikasi untuk pengiriman pesan secara personal.</li>
        </ul>
    </div>
</div>

<!-- Modal Template WA -->
<div class="modal fade" id="modalTemplate" tabindex="-1" aria-hidden="true">
  <div class="modal-dialog modal-xl">
    <div class="modal-content" style="border-radius: 20px; overflow: hidden; border: none; box-shadow: 0 10px 40px rgba(0,0,0,0.2);">
      <div class="modal-header bg-dark text-white p-4 border-0">
        <h5 class="modal-title fw-bold d-flex align-items-center"><i class="bi bi-whatsapp me-2 fs-4 text-success"></i> Manajemen Template Pengingat</h5>
        <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
      </div>
      <div class="modal-body p-0 bg-white">
        <div class="row g-0">
            <!-- Sidebar: Daftar Template -->
            <div class="col-md-3 bg-light p-4 template-list border-end">
                <div class="d-flex justify-content-between align-items-center mb-3">
                    <h6 class="fw-bold text-secondary mb-0">Daftar Template</h6>
                    <form method="POST">
                        <button type="submit" name="add_template" class="btn btn-sm btn-teal rounded-circle" title="Tambah Template Baru" style="width:30px; height:30px; padding:0;">
                            <i class="bi bi-plus-lg"></i>
                        </button>
                    </form>
                </div>
                <?php foreach($allTemplates as $t): ?>
                    <a href="reminder_kontrol.php?tgl_rencana=<?= $tgl_rencana ?>&template_id=<?= $t['id'] ?>&open_modal=1" 
                       class="template-item text-decoration-none <?= $template_id == $t['id'] ? 'active' : '' ?>">
                        <div class="judul text-truncate me-2"><?= htmlspecialchars($t['judul']) ?></div>
                        <i class="bi bi-chat-text icon"></i>
                    </a>
                <?php endforeach; ?>
            </div>

            <!-- Content: Editor -->
            <div class="col-md-9 p-4">
                <form method="POST">
                    <input type="hidden" name="template_id" value="<?= $template_id ?>">
                    <div class="row">
                        <div class="col-md-7">
                            <div class="mb-3">
                                <label class="form-label fw-bold text-dark mb-1">Nama Rumah Sakit / Klinik (Global)</label>
                                <input type="text" name="nama_rs" class="form-control bg-light border-0 py-2 px-3 shadow-sm" value="<?= htmlspecialchars($namaRS) ?>" required>
                            </div>
                            <div class="mb-3">
                                <label class="form-label fw-bold text-dark mb-1">Nama / Judul Template Ini</label>
                                <input type="text" name="judul_template" class="form-control bg-light border-0 py-2 px-3 shadow-sm" value="<?= htmlspecialchars($currentTemplate['judul']) ?>" required>
                            </div>
                            <div class="mb-3">
                                <label class="form-label fw-bold text-dark mb-1">Isi Pesan WhatsApp</label>
                                <div class="p-2 border rounded-3 mb-2 bg-light shadow-sm">
                                    <div class="d-flex flex-wrap gap-1">
                                        <span class="var-badge" onclick="insertVar('{nama_pasien}')"><i class="bi bi-person"></i> Pasien</span>
                                        <span class="var-badge" onclick="insertVar('{tgl_kontrol}')"><i class="bi bi-calendar-event"></i> Tgl</span>
                                        <span class="var-badge" onclick="insertVar('{nama_poli}')"><i class="bi bi-hospital"></i> Poli</span>
                                        <span class="var-badge" onclick="insertVar('{nama_dokter}')"><i class="bi bi-person-badge"></i> Dokter</span>
                                        <span class="var-badge" onclick="insertVar('{no_surat}')"><i class="bi bi-file-earmark-text"></i> No.Surat</span>
                                        <span class="var-badge" onclick="insertVar('{nama_rs}')"><i class="bi bi-building"></i> RS</span>
                                    </div>
                                </div>
                                <textarea name="template_pesan" id="template_pesan" class="form-control bg-light border-0 py-3 px-4 shadow-sm" rows="6" required><?= htmlspecialchars($currentTemplate['template_pesan']) ?></textarea>
                            </div>
                            <div class="d-flex justify-content-between align-items-center mt-4 pt-3 border-top">
                                <button type="submit" name="delete_template" class="btn btn-sm btn-outline-danger border-0" onclick="return confirm('Yakin ingin menghapus template ini?')">
                                    <i class="bi bi-trash me-1"></i> Hapus Template Ini
                                </button>
                                <div class="d-flex gap-2">
                                    <button type="button" class="btn btn-light rounded-3 px-4" data-bs-dismiss="modal">Tutup</button>
                                    <button type="submit" name="save_template" class="btn btn-primary rounded-3 px-5 fw-bold shadow-sm">
                                        <i class="bi bi-check-lg me-1"></i> Simpan Perubahan
                                    </button>
                                </div>
                            </div>
                        </div>
                        <div class="col-md-5">
                            <label class="form-label fw-bold text-dark mb-1">Pratinjau Pesan</label>
                            <div class="preview-wa shadow-sm border" style="min-height: 350px;">
                                <div class="chat-bubble shadow-none" id="livePreview"></div>
                            </div>
                            <div class="mt-3 p-2 rounded-3 bg-light border small text-secondary">
                                Tips: <b>*teks*</b> = Tebal, <i>_teks_</i> = Miring.
                            </div>
                        </div>
                    </div>
                </form>
            </div>
        </div>
      </div>
    </div>
  </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
<script>
const templateArea = document.getElementById('template_pesan');
const livePreview = document.getElementById('livePreview');
const inputRS = document.querySelector('input[name="nama_rs"]');

function updatePreview() {
    let text = templateArea.value;
    const namaRS = inputRS.value;
    
    // Ganti variabel dengan data dummy untuk preview
    text = text.replace(/{nama_pasien}/g, '<b>Ibu Siti Aminah</b>');
    text = text.replace(/{tgl_kontrol}/g, '<b>' + new Date().toLocaleDateString('id-ID') + '</b>');
    text = text.replace(/{nama_poli}/g, '<b>Poliklinik Jantung</b>');
    text = text.replace(/{nama_dokter}/g, '<b>dr. Ahmad, Sp.JP</b>');
    text = text.replace(/{no_surat}/g, '<b>123/SKDP/04/2026</b>');
    text = text.replace(/{nama_rs}/g, '<b>' + (namaRS || 'MediFix Hospital') + '</b>');
    
    // Format bold WhatsApp (*text*) ke HTML (<b>text</b>)
    text = text.replace(/\*(.*?)\*/g, '<b>$1</b>');
    
    livePreview.innerHTML = text;
}

function insertVar(val) {
    const start = templateArea.selectionStart;
    const end = templateArea.selectionEnd;
    const text = templateArea.value;
    templateArea.value = text.substring(0, start) + val + text.substring(end);
    updatePreview();
    templateArea.focus();
}

function restoreDefault() {
    if(confirm('Yakin ingin mengembalikan template ke pengaturan awal?')) {
        inputRS.value = 'MediFix Hospital';
        templateArea.value = "Halo Bapak/Ibu *{nama_pasien}*,\n\nSalam sehat dari *{nama_rs}* 😊.\n\nKami ingin mengingatkan kembali jadwal kontrol Anda pada:\n\n📅 Hari/Tgl: *{tgl_kontrol}*\n🏥 Poliklinik: *{nama_poli}*\n👨‍⚕️ Dokter: *{nama_dokter}*\n\nMohon datang 30 menit sebelum jam pelayanan dimulai ya. Sampai jumpa! 🙏";
        updatePreview();
    }
}

templateArea.addEventListener('input', updatePreview);
inputRS.addEventListener('input', updatePreview);

// Initial preview
updatePreview();

// Buka modal otomatis jika ada parameter open_modal
const urlParams = new URLSearchParams(window.location.search);
if (urlParams.has('open_modal')) {
    const myModal = new bootstrap.Modal(document.getElementById('modalTemplate'));
    myModal.show();
}

<?php if (isset($success_msg)): ?>
Swal.fire({
    icon: 'success',
    title: 'Berhasil!',
    text: '<?= $success_msg ?>',
    timer: 2000,
    showConfirmButton: false,
    customClass: { popup: 'rounded-4' }
});
<?php endif; ?>
</script>
</body>
</html>
