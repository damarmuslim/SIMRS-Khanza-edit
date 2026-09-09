<?php
session_start();
date_default_timezone_set('Asia/Jakarta');
include 'koneksi2.php';

$nama = $_SESSION['nama'] ?? 'Pengguna';
$qr_path = 'qrcode/JKN/qr-code.jpg';
$msg = "";

// Ambil data pasien berdasarkan input nama atau nobooking
$patient = null;
if (isset($_POST['search']) && !empty($_POST['search_name'])) {
    $search = "%".$_POST['search_name']."%";
    $stmt = $pdo_simrs->prepare("SELECT * FROM referensi_mobilejkn_bpjs WHERE nomorkartu LIKE :search OR nobooking LIKE :search OR nik LIKE :search OR norm LIKE :search LIMIT 1");
    $stmt->execute([':search' => $search]);
    $patient = $stmt->fetch(PDO::FETCH_ASSOC);
}

// Proses Check-In
if (isset($_POST['checkin']) && !empty($_POST['nobooking'])) {
    $nobooking = $_POST['nobooking'];

    try {
        // 1. Update status di referensi_mobilejkn_bpjs
        $upd = $pdo_simrs->prepare("UPDATE referensi_mobilejkn_bpjs 
                                    SET status='Checkin', validasi=NOW() 
                                    WHERE nobooking=:nobooking");
        $upd->execute([':nobooking' => $nobooking]);

        // 2. Hapus data di referensi_mobilejkn_bpjs_batal
        $del = $pdo_simrs->prepare("DELETE FROM referensi_mobilejkn_bpjs_batal WHERE nobooking=:nobooking");
        $del->execute([':nobooking' => $nobooking]);

        $msg = "<div class='alert alert-success text-center'>
                    <i class='bi bi-check-circle'></i> Check-In berhasil!<br>
                    Nobooking: <strong>$nobooking</strong>
                </div>";
        $patient = null; // reset input setelah checkin
    } catch (PDOException $e) {
        $msg = "<div class='alert alert-danger text-center'>Terjadi kesalahan: " . $e->getMessage() . "</div>";
    }
}
?>
<!doctype html>
<html lang="id">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>Check-In JKN | MediFix</title>
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
<link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css" rel="stylesheet">
<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css">
<link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@300;400;500;600;700&display=swap" rel="stylesheet">
<style>
:root {
    --primary-color: #0d47a1;
    --accent-color: #00acc1;
    --success-color: #2e7d32;
    --error-color: #c62828;
    --glass-bg: rgba(255, 255, 255, 0.85);
    --glass-border: rgba(255, 255, 255, 0.4);
}

body {
    background: radial-gradient(circle at top left, #e3f2fd, #bbdefb);
    background-attachment: fixed;
    font-family: 'Plus Jakarta Sans', sans-serif;
    min-height: 100vh;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 20px;
    overflow-x: hidden;
}

/* Decorative Background Elements */
body::before, body::after {
    content: '';
    position: fixed;
    width: 300px;
    height: 300px;
    border-radius: 50%;
    z-index: -1;
    filter: blur(80px);
}

body::before {
    background: rgba(13, 71, 161, 0.15);
    top: -50px;
    right: -50px;
}

body::after {
    background: rgba(0, 172, 193, 0.15);
    bottom: -50px;
    left: -50px;
}

.main-card {
    background: var(--glass-bg);
    backdrop-filter: blur(20px);
    -webkit-backdrop-filter: blur(20px);
    border: 1px solid var(--glass-border);
    border-radius: 30px;
    box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.1);
    padding: 2.5rem;
    width: 100%;
    max-width: 550px;
    transition: transform 0.3s ease;
}

.brand-header {
    text-align: center;
    margin-bottom: 2rem;
}

.brand-icon {
    width: 70px;
    height: 70px;
    background: linear-gradient(135deg, var(--primary-color), var(--accent-color));
    color: white;
    border-radius: 20px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 32px;
    margin: 0 auto 1.5rem;
    box-shadow: 0 10px 20px rgba(13, 71, 161, 0.3);
}

.brand-header h3 {
    font-weight: 700;
    color: #1a237e;
    letter-spacing: -0.5px;
}

.brand-header p {
    color: #546e7a;
    font-size: 0.95rem;
}

.qr-preview {
    background: white;
    padding: 15px;
    border-radius: 20px;
    box-shadow: 0 5px 15px rgba(0,0,0,0.05);
    margin-bottom: 2rem;
    display: inline-block;
    transition: transform 0.3s ease;
}

.qr-preview:hover {
    transform: scale(1.02);
}

.qr-preview img {
    max-width: 220px;
    border-radius: 12px;
}

.form-floating > .form-control {
    border-radius: 15px;
    border: 1px solid #e0e0e0;
    background: rgba(255,255,255,0.7);
}

.form-floating > .form-control:focus {
    box-shadow: 0 0 0 4px rgba(13, 71, 161, 0.1);
    border-color: var(--primary-color);
}

.btn-premium {
    border-radius: 15px;
    padding: 12px 24px;
    font-weight: 600;
    transition: all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 10px;
    text-transform: uppercase;
    letter-spacing: 0.5px;
    font-size: 0.9rem;
}

.btn-search {
    background: linear-gradient(135deg, var(--primary-color), #1565c0);
    border: none;
    color: white;
    width: 100%;
}

.btn-search:hover {
    box-shadow: 0 8px 20px rgba(13, 71, 161, 0.3);
    transform: translateY(-2px);
    color: white;
}

.btn-checkin {
    background: linear-gradient(135deg, var(--success-color), #43a047);
    border: none;
    color: white;
}

.btn-checkin:hover {
    box-shadow: 0 8px 20px rgba(46, 125, 50, 0.3);
    transform: translateY(-2px);
    color: white;
}

.btn-exit {
    background: #eceff1;
    border: none;
    color: #455a64;
}

.btn-exit:hover {
    background: #cfd8dc;
    color: #263238;
}

/* Patient Data Card */
.patient-card {
    background: #f8faff;
    border: 1px solid #e3f2fd;
    border-radius: 20px;
    padding: 1.5rem;
    margin-top: 1.5rem;
    text-align: left;
    position: relative;
    overflow: hidden;
}

.patient-card::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    width: 4px;
    height: 100%;
    background: var(--accent-color);
}

.patient-info-item {
    margin-bottom: 0.75rem;
}

.patient-info-label {
    font-size: 0.75rem;
    color: #90a4ae;
    text-transform: uppercase;
    letter-spacing: 0.5px;
    font-weight: 600;
}

.patient-info-value {
    font-size: 1rem;
    color: #263238;
    font-weight: 600;
}

.alert {
    border-radius: 18px;
    border: none;
    font-weight: 500;
}

.alert-success {
    background: #e8f5e9;
    color: #1b5e20;
}

/* Animations */
@keyframes fadeIn {
    from { opacity: 0; transform: translateY(20px); }
    to { opacity: 1; transform: translateY(0); }
}

.animate-fade-in {
    animation: fadeIn 0.6s ease forwards;
}

@media (max-width: 576px) {
    .main-card {
        padding: 1.5rem;
    }
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

<div class="main-card animate-fade-in">
    <div class="brand-header">
        <div class="brand-icon">
            <i class="bi bi-shield-check"></i>
        </div>
        <h3>Check-In JKN</h3>
        <p>Verifikasi kedatangan melalui aplikasi Mobile JKN</p>
    </div>

    <?= $msg ?>

    <div class="text-center">
        <?php if (file_exists($qr_path)): ?>
            <div class="qr-preview">
                <img src="<?= htmlspecialchars($qr_path) ?>" alt="QR Code JKN">
            </div>
        <?php endif; ?>
    </div>

    <!-- Form cari pasien -->
    <form method="POST">
        <div class="form-floating mb-3">
            <input type="text" name="search_name" class="form-control" id="searchName" placeholder="Nama / Booking / NIK" required autocomplete="off">
            <label for="searchName">Ketik Nama / Nobooking / NIK</label>
        </div>
        <button type="submit" name="search" class="btn btn-premium btn-search">
            <i class="bi bi-search"></i> Cari Data Pasien
        </button>
    </form>

    <?php if($patient): ?>
        <div class="patient-card animate-fade-in">
            <div class="row g-3">
                <div class="col-6">
                    <div class="patient-info-item">
                        <div class="patient-info-label">Nama Pasien</div>
                        <div class="patient-info-value"><?= htmlspecialchars($patient['norm']) ?></div>
                    </div>
                </div>
                <div class="col-6">
                    <div class="patient-info-item">
                        <div class="patient-info-label">No. Booking</div>
                        <div class="patient-info-value"><?= htmlspecialchars($patient['nobooking']) ?></div>
                    </div>
                </div>
                <div class="col-6">
                    <div class="patient-info-item">
                        <div class="patient-info-label">Poliklinik</div>
                        <div class="patient-info-value text-primary"><?= htmlspecialchars($patient['kodepoli']) ?></div>
                    </div>
                </div>
                <div class="col-6">
                    <div class="patient-info-item">
                        <div class="patient-info-label">Dokter</div>
                        <div class="patient-info-value"><?= htmlspecialchars($patient['kodedokter']) ?></div>
                    </div>
                </div>
                <div class="col-12">
                    <div class="patient-info-item">
                        <div class="patient-info-label">Kunjungan</div>
                        <div class="patient-info-value"><?= htmlspecialchars($patient['jeniskunjungan']) ?></div>
                    </div>
                </div>
            </div>
        </div>

        <form method="POST" class="mt-4 d-grid gap-3">
            <input type="hidden" name="nobooking" value="<?= htmlspecialchars($patient['nobooking']) ?>">
            <button type="submit" name="checkin" class="btn btn-premium btn-checkin">
                <i class="bi bi-person-check-fill"></i> Konfirmasi Check-In
            </button>
            <a href="anjungan.php" class="btn btn-premium btn-exit">
                <i class="bi bi-arrow-left"></i> Kembali ke Menu
            </a>
        </form>
    <?php endif; ?>

    <?php if (!$patient && !isset($_POST['search'])): ?>
        <div class="mt-4 text-center">
            <a href="anjungan.php" class="btn btn-premium btn-exit w-100">
                <i class="bi bi-house"></i> Home Anjungan
            </a>
        </div>
    <?php endif; ?>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>

<script>
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
