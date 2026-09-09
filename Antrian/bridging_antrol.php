<?php
session_start();
if (!isset($_SESSION['user_id'])) {
    header('Location: login.php');
    exit;
}

$nama = $_SESSION['nama'] ?? 'Pengguna';
date_default_timezone_set('Asia/Jakarta');
?>
<!doctype html>
<html lang="id">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width,initial-scale=1">
<title>Dashboard Antrol BPJS - MediFix</title>
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
<link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.1/font/bootstrap-icons.css" rel="stylesheet">
<link href="https://fonts.googleapis.com/css2?family=Outfit:wght@300;400;500;600;700;800&family=Plus+Jakarta+Sans:ital,wght@0,400;0,500;0,600;0,700;1,400&display=swap" rel="stylesheet">
<style>
:root {
  --orange-50: #fff7ed; --orange-100: #ffedd5; --orange-500: #f97316; --orange-600: #ea580c;
  --rose-50:   #fff1f2; --rose-100:   #ffe4e6; --rose-500:   #f43f5e; --rose-600:   #e11d48;
  --amber-50:  #fffbeb; --amber-100:  #fef3c7; --amber-500:  #f59e0b; --amber-600:  #d97706;
  --slate-50:  #f8fafc; --slate-100:  #f1f5f9; --slate-200:  #e2e8f0; --slate-800:  #1e293b; --slate-900: #0f172a;
}
body { background: var(--slate-50); font-family: 'Plus Jakarta Sans', sans-serif; color: var(--slate-800); }
.topbar { background: #fff; box-shadow: 0 1px 3px rgba(0,0,0,0.05); padding: 12px 24px; display: flex; align-items: center; justify-content: space-between; position: sticky; top: 0; z-index: 50; }
.back-btn { display: flex; align-items: center; gap: 8px; font-weight: 600; color: var(--slate-800); text-decoration: none; padding: 8px 16px; border-radius: 8px; transition: 0.2s; }
.back-btn:hover { background: var(--slate-100); color: var(--rose-600); }
.hero-banner { background: linear-gradient(135deg, var(--rose-600), var(--orange-600)); padding: 48px 24px; border-radius: 20px; color: #fff; margin: 24px 0; position: relative; overflow: hidden; box-shadow: 0 10px 25px rgba(244, 63, 94, 0.2); }
.hero-banner::after { content: ''; position: absolute; right: 0; bottom: 0; width: 300px; height: 300px; background: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="rgba(255,255,255,0.05)"><path d="M16 11c1.66 0 2.99-1.34 2.99-3S17.66 5 16 5c-1.66 0-3 1.34-3 3s1.34 3 3 3zm-8 0c1.66 0 2.99-1.34 2.99-3S9.66 5 8 5C6.34 5 5 6.34 5 8s1.34 3 3 3zm0 2c-2.33 0-7 1.17-7 3.5V19h14v-2.5c0-2.33-4.67-3.5-7-3.5zm8 0c-.29 0-.62.02-.97.05 1.16.84 1.97 1.97 1.97 3.45V19h6v-2.5c0-2.33-4.67-3.5-7-3.5z"/></svg>') no-repeat center bottom / contain; }
.hero-title { font-family: 'Outfit', sans-serif; font-size: 32px; font-weight: 800; margin-bottom: 8px; }
.hero-subtitle { font-size: 16px; opacity: 0.9; font-weight: 500; }
.grid-menu { display: grid; grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); gap: 20px; }
.card-menu { background: #fff; border: 1px solid var(--slate-200); border-radius: 16px; padding: 24px; transition: 0.3s; text-decoration: none; display: flex; align-items: flex-start; gap: 16px; overflow: hidden; position: relative; }
.card-menu:hover { transform: translateY(-4px); box-shadow: 0 12px 24px rgba(0,0,0,0.06); border-color: var(--rose-500); }
.card-menu::before { content:''; position: absolute; left: 0; top: 0; bottom: 0; width: 4px; background: var(--rose-500); opacity: 0; transition: 0.3s; }
.card-menu:hover::before { opacity: 1; }
.ico-box { width: 56px; height: 56px; border-radius: 12px; display: flex; align-items: center; justify-content: center; font-size: 26px; flex-shrink: 0; }
.m-title { font-size: 18px; font-weight: 700; color: var(--slate-800); margin-bottom: 4px; font-family: 'Outfit', sans-serif;}
.m-desc { font-size: 14px; color: #64748b; margin: 0; }
.ic-rose { background: var(--rose-50); color: var(--rose-600); }
.ic-orange { background: var(--orange-50); color: var(--orange-600); }
.ic-amber { background: var(--amber-50); color: var(--amber-600); }
</style>
</head>
<body>

<div class="topbar">
    <a href="dashboard.php" class="back-btn"><i class="bi bi-arrow-left"></i> Kembali ke Dashboard Utama</a>
    <div class="d-flex align-items-center gap-3">
        <span class="fw-medium text-secondary"><i class="bi bi-person-fill me-1"></i> <?= htmlspecialchars($nama) ?></span>
        <span class="badge bg-danger-subtle text-danger py-2 px-3 rounded-pill fw-bold"><i class="bi bi-broadcast me-1"></i> Antrian Online JKN</span>
    </div>
</div>

<div class="container pb-5">
    <div class="hero-banner">
        <div class="position-relative z-1">
            <h1 class="hero-title">Dashboard Antrol BPJS</h1>
            <p class="hero-subtitle">Sistem Antrean Online terintegrasi dengan Mobile JKN BPJS Kesehatan.</p>
        </div>
    </div>

    <div class="mb-4">
        <h4 class="fw-bold mb-3" style="font-family: 'Outfit', sans-serif; color: var(--slate-900);">Monitoring Waktu Tunggu</h4>
        <div class="grid-menu">
            <a href="log_task_id.php" class="card-menu">
                <div class="ico-box ic-rose"><i class="bi bi-clock-history"></i></div>
                <div>
                    <h5 class="m-title">Log Task ID</h5>
                    <p class="m-desc">Pantau pengiriman waktu tunggu pelayanan (waktu layan) masing-masing unit/poli.</p>
                </div>
            </a>
            <a href="dashboard_waktu_pertanggal.php" class="card-menu">
                <div class="ico-box ic-orange"><i class="bi bi-calendar-check"></i></div>
                <div>
                    <h5 class="m-title">Laporan Pertanggal</h5>
                    <p class="m-desc">Statistik pencapaian antrean online harian berdasarkan rentang tanggal BPJS.</p>
                </div>
            </a>
            <a href="cek_status_antrean.php" class="card-menu">
                <div class="ico-box ic-amber"><i class="bi bi-search"></i></div>
                <div>
                    <h5 class="m-title">Cek Status Antrean</h5>
                    <p class="m-desc">Pencarian data antrean spesifik pasien Mobile JKN yang sudah bridging hari ini.</p>
                </div>
            </a>
        </div>
    </div>
    
    <div class="mb-3">
        <h4 class="fw-bold mb-3" style="font-family: 'Outfit', sans-serif; color: var(--slate-900);">Sistem Pendaftaran</h4>
        <div class="grid-menu">
            <a href="update_jadwal_dokter.php" class="card-menu">
                <div class="ico-box bg-light text-secondary"><i class="bi bi-calendar-event"></i></div>
                <div>
                    <h5 class="m-title">Update Jadwal Dokter</h5>
                    <p class="m-desc">Kirim dan perbarui jadwal HFIS dokter ke sistem JKN secara berkala.</p>
                </div>
            </a>
            <a href="daftar_batal_fingerprint.php" class="card-menu">
                <div class="ico-box bg-light text-secondary"><i class="bi bi-journal-text"></i></div>
                <div>
                    <h5 class="m-title">Daftar Batal/Fingerprint</h5>
                    <p class="m-desc">Lihat pasien BPJS yang batal periksa atau validasi antrean biometrik (Fingerprint).</p>
                </div>
            </a>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
