<?php
/**
 * SURAT KONTROL RAWAT JALAN - MediFix
 * Bridging BPJS V-Claim 2.0
 */

error_reporting(E_ALL);
ini_set('display_errors', 1);

session_start();
include 'koneksi.php';
include 'koneksi2.php';
include 'vclaim_helper.php'; // enkripsi, compress, callVClaim

date_default_timezone_set('Asia/Jakarta');

// === AMBIL SETTING VCLAIM ===
$stmt = $pdo->query("SELECT * FROM setting_vclaim LIMIT 1");
$setting = $stmt->fetch(PDO::FETCH_ASSOC);

if (!$setting) {
    die("Konfigurasi V-Claim belum diatur. Silakan hubungi IT.");
}

$cons_id    = trim($setting['cons_id']);
$secret_key = trim($setting['secret_key']);
$user_key   = trim($setting['user_key']);
$base_url   = rtrim(trim($setting['base_url']), '/');

// === PROSES PENCARIAN RIWAYAT SEP ===
$riwayatSEP = [];
$error = '';
$searchParam = '';

if ($_SERVER['REQUEST_METHOD'] === 'POST' && isset($_POST['cariPasien'])) {
    $searchParam = trim($_POST['searchParam']);
    
    if (empty($searchParam)) {
        $error = "Masukkan No. RM atau Nama Pasien!";
    } else {
        try {
            // 1. Cari No Kartu di DB Lokal (pasien)
            $stmt = $pdo_simrs->prepare("SELECT no_peserta, nm_pasien, no_rkm_medis FROM pasien WHERE no_rkm_medis = ? OR nm_pasien LIKE ?");
            $stmt->execute([$searchParam, "%$searchParam%"]);
            $pasiens = $stmt->fetchAll(PDO::FETCH_ASSOC);
            
            if (count($pasiens) > 0) {
                // Gunakan pasien pertama yang ditemukan
                $pasien = $pasiens[0];
                $noKartu = $pasien['no_peserta'];
                
                if (empty($noKartu)) {
                    $error = "Pasien tidak memiliki Nomor Kartu BPJS di database.";
                } else {
                    // 2. Tarik Riwayat dari VClaim (List Berdasarkan No Kartu)
                    // Endpoint: /Monitoring/HistoriPelayanan/NoKartu/{noKartu}/tglMulai/{tglMulai}/tglAkhir/{tglAkhir}
                    $tglAkhir = date('Y-m-d');
                    $tglMulai = date('Y-m-d', strtotime('-3 months'));
                    
                    $res = callVClaim("/Monitoring/HistoriPelayanan/NoKartu/{$noKartu}/tglMulai/{$tglMulai}/tglAkhir/{$tglAkhir}");
                    
                    if (isset($res['metaData']['code']) && $res['metaData']['code'] == 200) {
                        $riwayatSEP = $res['response']['histori'] ?? [];
                    } else {
                        $error = "BPJS: " . ($res['metaData']['message'] ?? "Gagal mengambil riwayat pelayanan.");
                    }
                }
            } else {
                $error = "Pasien tidak ditemukan di database.";
            }
        } catch (Exception $e) {
            $error = "System Error: " . $e->getMessage();
        }
    }
}

?>
<!DOCTYPE html>
<html lang="id">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Surat Kontrol Rawat Jalan - MediFix</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons/font/bootstrap-icons.css" rel="stylesheet">
<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css">
    <link href="https://fonts.googleapis.com/css2?family=Poppins:wght@400;500;600;700&display=swap" rel="stylesheet">
    <style>
        body {
            font-family: 'Poppins', sans-serif;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            min-height: 100vh;
            padding: 40px 20px;
        }
        .main-container {
            max-width: 1000px;
            margin: 0 auto;
            background: rgba(255, 255, 255, 0.98);
            border-radius: 20px;
            box-shadow: 0 15px 50px rgba(0,0,0,0.3);
            overflow: hidden;
        }
        .header-section {
            background: linear-gradient(135deg, #10b981, #059669);
            color: white;
            padding: 30px;
            text-align: center;
        }
        .content-section {
            padding: 30px;
        }
        .search-area {
            background: #f8fafc;
            border-radius: 15px;
            padding: 25px;
            margin-bottom: 30px;
            border: 2px solid #e2e8f0;
        }
        .benefit-card {
            border: 1px solid #dee2e6;
            border-radius: 12px;
            padding: 15px;
            margin-bottom: 15px;
            transition: all 0.3s;
            cursor: pointer;
        }
        .benefit-card:hover {
            border-color: #667eea;
            background: #f5f3ff;
            transform: translateY(-2px);
        }
        .sep-number {
            font-family: monospace;
            font-weight: 700;
            color: #4c1d95;
            font-size: 1.1rem;
        }
        .status-badge {
            font-size: 0.75rem;
            padding: 4px 8px;
            border-radius: 20px;
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

<div class="main-container">
    <div class="header-section">
        <h3><i class="bi bi-clipboard2-pulse-fill"></i> Pembuatan Surat Kontrol Rawat Jalan</h3>
        <p class="mb-0">Silakan masukkan data untuk pengecekan riwayat SEP</p>
    </div>

    <div class="content-section">
        
        <?php if ($error): ?>
            <div class="alert alert-danger alert-dismissible fade show" role="alert">
                <i class="bi bi-exclamation-triangle-fill"></i> <?= $error ?>
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        <?php endif; ?>

        <!-- Form Pencarian -->
        <div class="search-area">
            <form method="POST" class="row g-3">
                <div class="col-md-9">
                    <label class="form-label fw-bold">No. RM / Nama Pasien</label>
                    <div class="input-group input-group-lg">
                        <span class="input-group-text bg-white border-end-0"><i class="bi bi-search"></i></span>
                        <input type="text" name="searchParam" class="form-control border-start-0" 
                               placeholder="Contoh: 000123 atau Nama Pasien" 
                               value="<?= htmlspecialchars($searchParam) ?>" required>
                    </div>
                </div>
                <div class="col-md-3 d-flex align-items-end">
                    <button type="submit" name="cariPasien" class="btn btn-primary btn-lg w-100">
                        Cari Riwayat
                    </button>
                </div>
            </form>
        </div>

        <!-- Hasil Riwayat SEP -->
        <?php if (!empty($riwayatSEP)): ?>
            <h5 class="mb-3">Ditemukan <?= count($riwayatSEP) ?> Riwayat Pelayanan (3 Bulan Terakhir)</h5>
            <div class="riwayat-list">
                <?php foreach ($riwayatSEP as $sep): ?>
                    <div class="benefit-card" onclick="pilihSEP('<?= $sep['noSep'] ?>', '<?= $sep['tglSep'] ?>', '<?= $sep['poli'] ?>')">
                        <div class="row align-items-center">
                            <div class="col-md-8">
                                <div class="sep-number"><?= $sep['noSep'] ?></div>
                                <div class="text-muted small">
                                    <i class="bi bi-calendar-event"></i> <?= date('d F Y', strtotime($sep['tglSep'])) ?> 
                                    &bull; <i class="bi bi-hospital"></i> <?= $sep['poli'] ?>
                                </div>
                                <div class="mt-1 fw-bold"><?= $sep['diagnosa'] ?></div>
                            </div>
                            <div class="col-md-4 text-end">
                                <span class="badge bg-info status-badge"><?= $sep['jnsPelayanan'] == '1' ? 'Rawat Inap' : 'Rawat Jalan' ?></span>
                                <div class="mt-2">
                                    <button class="btn btn-outline-primary btn-sm">Gunakan Untuk Kontrol</button>
                                </div>
                            </div>
                        </div>
                    </div>
                <?php endforeach; ?>
            </div>
        <?php elseif($_SERVER['REQUEST_METHOD'] === 'POST' && !$error): ?>
            <div class="text-center py-5">
                <i class="bi bi-folder-x display-1 text-muted"></i>
                <p class="mt-3 text-muted">Tidak ada riwayat SEP yang ditemukan untuk pasien ini.</p>
            </div>
        <?php endif; ?>

        <hr>
        <div class="d-flex justify-content-between mt-4">
            <a href="anjungan.php" class="btn btn-secondary btn-lg">
                <i class="bi bi-arrow-left"></i> Kembali ke Menu
            </a>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script>
function pilihSEP(noSep, tglSep, poli) {
    // Akan dilanjutkan ke tahap pemilihan Poli & Dokter Kontrol
    window.location.href = `surat_kontrol_rajal_pilih.php?noSEP=${noSep}&tglSEP=${tglSep}&poliOrigin=${poli}`;
}

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
