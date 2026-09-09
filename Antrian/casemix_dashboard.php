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
<title>Dashboard Casemix - MediFix</title>
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
<link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.1/font/bootstrap-icons.css" rel="stylesheet">
<link href="https://fonts.googleapis.com/css2?family=Outfit:wght@300;400;500;600;700;800&family=Plus+Jakarta+Sans:ital,wght@0,400;0,500;0,600;0,700;1,400&display=swap" rel="stylesheet">
<style>
:root {
  --purple-50:  #faf5ff; --purple-100: #f3e8ff; --purple-500: #a855f7; --purple-600: #9333ea; --purple-700: #7e22ce;
  --fuchsia-50: #fdf4ff; --fuchsia-100:#fae8ff; --fuchsia-500:#d946ef; --fuchsia-600:#c026d3; --fuchsia-700:#a21caf;
  --blue-50:    #eff6ff; --blue-100:   #dbeafe; --blue-500:   #3b82f6; --blue-600:   #2563eb; --blue-700:   #1d4ed8;
  --slate-50:   #f8fafc; --slate-100:  #f1f5f9; --slate-200:  #e2e8f0; --slate-800:  #1e293b; --slate-900:  #0f172a;
}
body { background: var(--slate-50); font-family: 'Plus Jakarta Sans', sans-serif; color: var(--slate-800); }
.topbar { background: #fff; box-shadow: 0 1px 3px rgba(0,0,0,0.05); padding: 12px 24px; display: flex; align-items: center; justify-content: space-between; position: sticky; top: 0; z-index: 50; }
.back-btn { display: flex; align-items: center; gap: 8px; font-weight: 600; color: var(--slate-800); text-decoration: none; padding: 8px 16px; border-radius: 8px; transition: 0.2s; }
.back-btn:hover { background: var(--slate-100); color: var(--purple-600); }
.hero-banner { background: linear-gradient(135deg, var(--purple-700), var(--fuchsia-600)); padding: 48px 24px; border-radius: 20px; color: #fff; margin: 24px 0; position: relative; overflow: hidden; box-shadow: 0 10px 25px rgba(147, 51, 234, 0.2); }
.hero-banner::after { content: ''; position: absolute; right: 0; bottom: 0; width: 300px; height: 300px; background: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="rgba(255,255,255,0.05)"><path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-6h2v6zm0-8h-2V7h2v2z"/></svg>') no-repeat center bottom / contain; }
.hero-title { font-family: 'Outfit', sans-serif; font-size: 32px; font-weight: 800; margin-bottom: 8px; }
.hero-subtitle { font-size: 16px; opacity: 0.9; font-weight: 500; }
.grid-menu { display: grid; grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); gap: 20px; }
.card-menu { background: #fff; border: 1px solid var(--slate-200); border-radius: 16px; padding: 24px; transition: 0.3s; text-decoration: none; display: flex; align-items: flex-start; gap: 16px; overflow: hidden; position: relative; }
.card-menu:hover { transform: translateY(-4px); box-shadow: 0 12px 24px rgba(0,0,0,0.06); border-color: var(--purple-500); }
.card-menu::before { content:''; position: absolute; left: 0; top: 0; bottom: 0; width: 4px; background: var(--purple-500); opacity: 0; transition: 0.3s; }
.card-menu:hover::before { opacity: 1; }
.ico-box { width: 56px; height: 56px; border-radius: 12px; display: flex; align-items: center; justify-content: center; font-size: 26px; flex-shrink: 0; }
.m-title { font-size: 18px; font-weight: 700; color: var(--slate-800); margin-bottom: 4px; font-family: 'Outfit', sans-serif;}
.m-desc { font-size: 14px; color: #64748b; margin: 0; }
.ic-purple { background: var(--purple-50); color: var(--purple-600); }
.ic-fuchsia { background: var(--fuchsia-50); color: var(--fuchsia-600); }
.ic-blue { background: var(--blue-50); color: var(--blue-600); }
</style>
</head>
<body>

<div class="topbar">
    <a href="dashboard.php" class="back-btn"><i class="bi bi-arrow-left"></i> Kembali ke Dashboard Utama</a>
    <div class="d-flex align-items-center gap-3">
        <span class="fw-medium text-secondary"><i class="bi bi-person-fill me-1"></i> <?= htmlspecialchars($nama) ?></span>
        <span class="badge bg-purple-subtle text-purple py-2 px-3 rounded-pill fw-bold" style="background:#f3e8ff; color:#9333ea;"><i class="bi bi-bar-chart-fill me-1"></i> Casemix INA-CBG</span>
    </div>
</div>

<div class="container pb-5">
    <div class="hero-banner">
        <div class="position-relative z-1">
            <h1 class="hero-title">Dashboard Casemix</h1>
            <p class="hero-subtitle">Sistem Pengelolaan Klaim, Koding Diagnosa, dan Tarif INA-CBG BPJS Kesehatan.</p>
        </div>
    </div>

    <div class="mb-4">
        <h4 class="fw-bold mb-3" style="font-family: 'Outfit', sans-serif; color: var(--slate-900);">Modul Rekam Medis & Klaim</h4>
        <div class="grid-menu">
            <a href="data_casemix.php" class="card-menu">
                <div class="ico-box ic-purple"><i class="bi bi-folder-check"></i></div>
                <div>
                    <h5 class="m-title">Data Casemix</h5>
                    <p class="m-desc">Manajemen arsip klaim, validasi koding diagnosa (ICD-10/9), dan integrasi INA-CBG.</p>
                </div>
            </a>
            <a href="data_casemix.php" class="card-menu">
                <div class="ico-box ic-fuchsia"><i class="bi bi-clipboard2-pulse"></i></div>
                <div>
                    <h5 class="m-title">Koding Diagnosa</h5>
                    <p class="m-desc">Proses koding morbiditas dan mortalitas untuk pembentukan grup tarif (grouper).</p>
                </div>
            </a>
            <a href="pengajuan_klaim.php" class="card-menu">
                <div class="ico-box ic-blue"><i class="bi bi-receipt"></i></div>
                <div>
                    <h5 class="m-title">Pengajuan Klaim</h5>
                    <p class="m-desc">Monitoring status pengajuan tagihan/klaim pelayanan pasien ke pihak asuransi (BPJS).</p>
                </div>
            </a>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
