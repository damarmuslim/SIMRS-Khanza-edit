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
<title>Dashboard V-Claim BPJS - MediFix</title>
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
<link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.1/font/bootstrap-icons.css" rel="stylesheet">
<link href="https://fonts.googleapis.com/css2?family=Outfit:wght@300;400;500;600;700;800&family=Plus+Jakarta+Sans:ital,wght@0,400;0,500;0,600;0,700;1,400&display=swap" rel="stylesheet">
<style>
:root {
  --teal-50:  #f0fdfa; --teal-100: #ccfbf1; --teal-500: #14b8a6; --teal-600: #0d9488; --teal-700: #0f766e;
  --sky-50:   #f0f9ff; --sky-100:  #e0f2fe; --sky-500:  #0ea5e9; --sky-600:  #0284c7; --sky-700:  #0369a1;
  --indigo-50:#eef2ff; --indigo-100:#e0e7ff; --indigo-500:#6366f1; --indigo-600:#4f46e5;
  --slate-50: #f8fafc; --slate-100: #f1f5f9; --slate-200: #e2e8f0; --slate-800: #1e293b; --slate-900: #0f172a;
}
body { background: var(--slate-50); font-family: 'Plus Jakarta Sans', sans-serif; color: var(--slate-800); }
.topbar { background: #fff; box-shadow: 0 1px 3px rgba(0,0,0,0.05); padding: 12px 24px; display: flex; align-items: center; justify-content: space-between; position: sticky; top: 0; z-index: 50; }
.back-btn { display: flex; align-items: center; gap: 8px; font-weight: 600; color: var(--slate-800); text-decoration: none; padding: 8px 16px; border-radius: 8px; transition: 0.2s; }
.back-btn:hover { background: var(--slate-100); color: var(--sky-600); }
.hero-banner { background: linear-gradient(135deg, var(--sky-600), var(--teal-600)); padding: 48px 24px; border-radius: 20px; color: #fff; margin: 24px 0; position: relative; overflow: hidden; box-shadow: 0 10px 25px rgba(2, 132, 199, 0.2); }
.hero-banner::after { content: ''; position: absolute; right: 0; bottom: 0; width: 300px; height: 300px; background: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="rgba(255,255,255,0.05)"><path d="M19 3H5a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V5a2 2 0 0 0-2-2m-5 14H7v-2h7v2m3-4H7v-2h10v2m0-4H7V7h10v2z"/></svg>') no-repeat center bottom / contain; }
.hero-title { font-family: 'Outfit', sans-serif; font-size: 32px; font-weight: 800; margin-bottom: 8px; }
.hero-subtitle { font-size: 16px; opacity: 0.9; font-weight: 500; }
.grid-menu { display: grid; grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); gap: 20px; }
.card-menu { background: #fff; border: 1px solid var(--slate-200); border-radius: 16px; padding: 24px; transition: 0.3s; text-decoration: none; display: flex; align-items: flex-start; gap: 16px; overflow: hidden; position: relative; }
.card-menu:hover { transform: translateY(-4px); box-shadow: 0 12px 24px rgba(0,0,0,0.06); border-color: var(--sky-500); }
.card-menu::before { content:''; position: absolute; left: 0; top: 0; bottom: 0; width: 4px; background: var(--sky-500); opacity: 0; transition: 0.3s; }
.card-menu:hover::before { opacity: 1; }
.ico-box { width: 56px; height: 56px; border-radius: 12px; display: flex; align-items: center; justify-content: center; font-size: 26px; flex-shrink: 0; }
.m-title { font-size: 18px; font-weight: 700; color: var(--slate-800); margin-bottom: 4px; font-family: 'Outfit', sans-serif;}
.m-desc { font-size: 14px; color: #64748b; margin: 0; }
.ic-sky { background: var(--sky-50); color: var(--sky-600); }
.ic-teal { background: var(--teal-50); color: var(--teal-600); }
.ic-indigo { background: var(--indigo-50); color: var(--indigo-600); }
</style>
</head>
<body>

<div class="topbar">
    <a href="dashboard.php" class="back-btn"><i class="bi bi-arrow-left"></i> Kembali ke Dashboard Utama</a>
    <div class="d-flex align-items-center gap-3">
        <span class="fw-medium text-secondary"><i class="bi bi-person-fill me-1"></i> <?= htmlspecialchars($nama) ?></span>
        <span class="badge bg-success-subtle text-success py-2 px-3 rounded-pill fw-bold"><i class="bi bi-shield-check me-1"></i> V-Claim 2.0 Ready</span>
    </div>
</div>

<div class="container pb-5">
    <div class="hero-banner">
        <div class="position-relative z-1">
            <h1 class="hero-title">Dashboard V-Claim BPJS</h1>
            <p class="hero-subtitle">Integrasi Manajemen Data SEP & Surat Kontrol Rawat Jalan/Inap BPJS Kesehatan.</p>
        </div>
    </div>

    <div class="mb-4">
        <h4 class="fw-bold mb-3" style="font-family: 'Outfit', sans-serif; color: var(--slate-900);">Modul Registrasi & SEP</h4>
        <div class="grid-menu">
            <a href="surat_kontrol_rajal.php" class="card-menu">
                <div class="ico-box ic-sky"><i class="bi bi-file-earmark-medical"></i></div>
                <div>
                    <h5 class="m-title">Surat Kontrol Rawat Jalan</h5>
                    <p class="m-desc">Penerbitan surat kontrol (SPRI) khusus pasien pasca-rawat jalan rujukan BPJS.</p>
                </div>
            </a>
            <a href="surat_kontrol_ranap.php" class="card-menu">
                <div class="ico-box ic-indigo"><i class="bi bi-hospital"></i></div>
                <div>
                    <h5 class="m-title">Surat Kontrol Rawat Inap</h5>
                    <p class="m-desc">Penerbitan surat kontrol pertama (pasca-ranap) ke poliklinik tujuan.</p>
                </div>
            </a>
            <a href="data_sep_bpjs.php" class="card-menu">
                <div class="ico-box ic-teal"><i class="bi bi-collection"></i></div>
                <div>
                    <h5 class="m-title">Data Riwayat SEP</h5>
                    <p class="m-desc">Cari dan kelola riwayat Surat Eligibilitas Peserta (SEP) yang telah terbit.</p>
                </div>
            </a>
        </div>
    </div>
    
    <div class="mb-3">
        <h4 class="fw-bold mb-3" style="font-family: 'Outfit', sans-serif; color: var(--slate-900);">Modul Referensi BPJS</h4>
        <div class="grid-menu">
            <a href="referensi_poli.php" class="card-menu">
                <div class="ico-box bg-light text-secondary"><i class="bi bi-diagram-3"></i></div>
                <div>
                    <h5 class="m-title">Referensi Poli & Faskes</h5>
                    <p class="m-desc">Lihat sinkronisasi relasi data poliklinik dan faskes rujukan V-Claim.</p>
                </div>
            </a>
            <a href="referensi_dokter.php" class="card-menu">
                <div class="ico-box bg-light text-secondary"><i class="bi bi-person-vcard"></i></div>
                <div>
                    <h5 class="m-title">Referensi Dokter JKN</h5>
                    <p class="m-desc">Lihat sinkronisasi data jadwal dan referensi dokter spesialis V-Claim.</p>
                </div>
            </a>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
