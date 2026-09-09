<?php
session_start();
include 'koneksi.php';
include 'koneksi2.php';
require_once __DIR__ . '/vendor/autoload.php';
use LZCompressor\LZString;

if (!isset($_SESSION['user_id'])) {
    header('Location: login.php');
    exit;
}

$nama = $_SESSION['nama'] ?? 'Pengguna';
date_default_timezone_set('Asia/Jakarta');

// === FUNGSI DEKRIPSI & KOMPRESI ( Khanza Method ) ===
function stringDecrypt($key, $string) {
    if (empty($string)) return "";
    $encrypt_method = 'AES-256-CBC';
    $key_hash = hex2bin(hash('sha256', $key));
    $iv = substr(hex2bin(hash('sha256', $key)), 0, 16);
    return openssl_decrypt(base64_decode($string), $encrypt_method, $key_hash, OPENSSL_RAW_DATA, $iv);
}

function decompress($string) {
    return LZCompressor\LZString::decompressFromEncodedURIComponent($string);
}

// === AMBIL SETTING VCLAIM ===
$stmt = $pdo->query("SELECT * FROM setting_vclaim LIMIT 1");
$setting = $stmt->fetch(PDO::FETCH_ASSOC);

if (!$setting) {
    die("Konfigurasi V-Claim belum diatur. Silakan hubungi IT.");
}

$cons_id = trim($setting['cons_id']);
$secret_key = trim($setting['secret_key']);
$user_key = trim($setting['user_key']);
$base_url = trim($setting['base_url']);

// === LOGIKA BRIDGING REQUEST ===
function callVClaim($endpoint) {
    global $cons_id, $secret_key, $user_key, $base_url;
    
    date_default_timezone_set('UTC');
    $tStamp = strval(time());
    date_default_timezone_set('Asia/Jakarta');
    
    $signature = base64_encode(hash_hmac('sha256', $cons_id . "&" . $tStamp, $secret_key, true));
    $key = $cons_id . $secret_key . $tStamp;
    
    $headers = [
        "X-cons-id: $cons_id",
        "X-timestamp: $tStamp",
        "X-signature: $signature",
        "user_key: $user_key",
        "Content-Type: application/json"
    ];

    $ch = curl_init($base_url . $endpoint);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);
    curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);
    curl_setopt($ch, CURLOPT_TIMEOUT, 20);

    $response = curl_exec($ch);
    $httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
    curl_close($ch);

    $result = json_decode($response, true);
    if (isset($result['response']) && is_string($result['response']) && !empty($result['response'])) {
        $decrypted = stringDecrypt($key, $result['response']);
        $decompressed = decompress($decrypted);
        $result['response'] = json_decode($decompressed, true);
    }

    return $result;
}

// === PROSES PENCARIAN ===
$results = [];
$error = '';
$tab = $_GET['tab'] ?? 'poli';
$search = $_GET['search'] ?? '';
$jenis_faskes = $_GET['jenis'] ?? '1'; // 1 = FKTP, 2 = FKTL

if (!empty($search)) {
    if ($tab === 'poli') {
        $res = callVClaim("/referensi/poli/" . urlencode($search));
        if (isset($res['metaData']['code']) && $res['metaData']['code'] == 200) {
            $results = $res['response']['poli'] ?? [];
        } else {
            $error = "BPJS: " . ($res['metaData']['message'] ?? "Gagal mengambil data referensi poliklinik.");
        }
    } else if ($tab === 'faskes') {
        $res = callVClaim("/referensi/faskes/" . urlencode($search) . "/" . $jenis_faskes);
        if (isset($res['metaData']['code']) && $res['metaData']['code'] == 200) {
            $results = $res['response']['faskes'] ?? [];
        } else {
            $error = "BPJS: " . ($res['metaData']['message'] ?? "Gagal mengambil data referensi faskes.");
        }
    } else if ($tab === 'dokter') {
        $res = callVClaim("/referensi/dokter/" . urlencode($search));
        if (isset($res['metaData']['code']) && $res['metaData']['code'] == 200) {
            $results = $res['response']['list'] ?? [];
        } else {
            $error = "BPJS: " . ($res['metaData']['message'] ?? "Gagal mengambil data referensi dokter.");
        }
    }
}
?>
<!doctype html>
<html lang="id">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width,initial-scale=1">
<title>Referensi Poli & Faskes BPJS - MediFix</title>
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
<link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.1/font/bootstrap-icons.css" rel="stylesheet">
<link href="https://fonts.googleapis.com/css2?family=Outfit:wght@400;500;600;700&family=Plus+Jakarta+Sans:wght@400;500;600;700&display=swap" rel="stylesheet">
<style>
:root {
  --slate-50: #f8fafc; --slate-100: #f1f5f9; --slate-200: #e2e8f0; --slate-700: #334155; --slate-800: #1e293b; --slate-900: #0f172a;
  --sky-500: #0ea5e9; --sky-600: #0284c7; --teal-500: #14b8a6;
}
body { background: var(--slate-50); font-family: 'Plus Jakarta Sans', sans-serif; color: var(--slate-800); }
.topbar { background: #fff; box-shadow: 0 1px 3px rgba(0,0,0,0.05); padding: 12px 24px; position: sticky; top: 0; z-index: 50; display: flex; justify-content: space-between; align-items: center;}
.back-btn { text-decoration: none; color: #1e293b; font-weight: 600; display: flex; align-items: center; gap: 8px; }
.back-btn:hover { color: var(--sky-600); }

.card-ref { background: #fff; border-radius: 16px; border: 1px solid var(--slate-200); box-shadow: 0 4px 6px -1px rgba(0,0,0,0.05); overflow: hidden; }
.card-header-ref { background: var(--slate-50); border-bottom: 1px solid var(--slate-200); padding: 20px 24px; }
.nav-pills-custom .nav-link { border-radius: 8px; padding: 10px 20px; font-weight: 600; color: var(--slate-700); transition: 0.2s; }
.nav-pills-custom .nav-link.active { background: var(--sky-500); color: #fff; }
.nav-pills-custom .nav-link:not(.active):hover { background: var(--slate-100); }

.search-box { position: relative; }
.search-box .bi-search { position: absolute; left: 16px; top: 50%; transform: translateY(-50%); color: #94a3b8; }
.search-input { padding-left: 44px; border-radius: 12px; border: 1.5px solid var(--slate-200); height: 48px; font-weight: 500; }
.search-input:focus { border-color: var(--sky-500); box-shadow: 0 0 0 4px rgba(14, 165, 233, 0.1); }

.table-glass { border-collapse: separate; border-spacing: 0; }
.table-glass thead th { background: var(--slate-100); border-bottom: none; color: var(--slate-700); font-weight: 700; padding: 16px 24px; text-transform: uppercase; font-size: 12px; letter-spacing: 0.05em; }
.table-glass tbody td { padding: 16px 24px; vertical-align: middle; border-bottom: 1px solid var(--slate-100); }
.table-glass tbody tr:last-child td { border-bottom: none; }
.table-glass tbody tr:hover { background: var(--slate-50); }

.badge-code { background: var(--slate-100); color: var(--slate-800); font-family: monospace; font-weight: 700; padding: 4px 8px; border-radius: 4px; font-size: 14px; }
</style>
</head>
<body>

<div class="topbar">
    <a href="bridging_vclaim.php" class="back-btn"><i class="bi bi-arrow-left"></i> Kembali ke Dashboard</a>
    <div class="fw-bold text-sky-600" style="font-family: 'Outfit'; font-size: 20px;">MediFix <span class="text-secondary fw-normal">| Referensi V-Claim</span></div>
</div>

<div class="container mt-4 pb-5">
    <div class="row justify-content-center">
        <div class="col-lg-10">
            <div class="card-ref">
                <div class="card-header-ref">
                    <div class="d-flex flex-column flex-md-row justify-content-between align-items-md-center gap-3">
                        <h4 class="fw-bold mb-0" style="font-family: 'Outfit';">Cari Data Referensi BPJS</h4>
                        <ul class="nav nav-pills nav-pills-custom">
                            <li class="nav-item">
                                <a class="nav-link <?= $tab === 'poli' ? 'active' : '' ?>" href="?tab=poli&search=<?= urlencode($search) ?>">
                                    <i class="bi bi-diagram-3 me-2"></i> Poliklinik
                                </a>
                            </li>
                            <li class="nav-item">
                                <a class="nav-link <?= $tab === 'faskes' ? 'active' : '' ?>" href="?tab=faskes&search=<?= urlencode($search) ?>&jenis=<?= $jenis_faskes ?>">
                                    <i class="bi bi-hospital me-2"></i> Faskes / RS
                                </a>
                            </li>
                            <li class="nav-item">
                                <a class="nav-link <?= $tab === 'dokter' ? 'active' : '' ?>" href="?tab=dokter&search=<?= urlencode($search) ?>">
                                    <i class="bi bi-person-vcard me-2"></i> Dokter
                                </a>
                            </li>
                        </ul>
                    </div>
                </div>
                <div class="p-4">
                    <form action="" method="GET" class="row g-3">
                        <input type="hidden" name="tab" value="<?= htmlspecialchars($tab) ?>">
                        <div class="col-md-7">
                            <div class="search-box">
                                <i class="bi bi-search"></i>
                                <input type="text" name="search" class="form-control search-input" 
                                       placeholder="Masukkan nama <?= $tab === 'poli' ? 'poli' : ($tab === 'dokter' ? 'dokter' : 'faskes') ?> (min. 3 karakter)..." 
                                       value="<?= htmlspecialchars($search) ?>" required minlength="3">
                            </div>
                        </div>
                        <?php if ($tab === 'faskes'): ?>
                        <div class="col-md-3">
                            <select name="jenis" class="form-select search-input">
                                <option value="1" <?= $jenis_faskes === '1' ? 'selected' : '' ?>>Faskes 1 (Puskesmas/Klinik)</option>
                                <option value="2" <?= $jenis_faskes === '2' ? 'selected' : '' ?>>Faskes 2 (Rumah Sakit)</option>
                            </select>
                        </div>
                        <?php endif; ?>
                        <div class="col-md-2">
                            <button type="submit" class="btn btn-primary w-100 h-100 shadow-sm" style="border-radius: 12px; font-weight: 600;">
                                Cari Data
                            </button>
                        </div>
                    </form>

                    <?php if ($error): ?>
                        <div class="alert alert-danger mt-4 border-0 rounded-4 d-flex align-items-center" role="alert">
                            <i class="bi bi-exclamation-circle-fill fs-4 me-3"></i>
                            <div><?= $error ?></div>
                        </div>
                    <?php endif; ?>

                    <?php if (!empty($search)): ?>
                        <div class="mt-4">
                            <div class="d-flex justify-content-between align-items-center mb-3">
                                <p class="text-secondary mb-0">Hasil pencarian untuk: <strong>"<?= htmlspecialchars($search) ?>"</strong></p>
                                <span class="badge bg-light text-secondary border"><?= count($results) ?> Data Ditemukan</span>
                            </div>

                            <div class="table-responsive">
                                <table class="table table-glass w-100">
                                    <thead>
                                        <?php if ($tab === 'poli'): ?>
                                            <tr>
                                                <th>Kode Poli</th>
                                                <th>Nama Poliklinik</th>
                                            </tr>
                                        <?php elseif ($tab === 'dokter'): ?>
                                            <tr>
                                                <th>Kode Dokter</th>
                                                <th>Nama Dokter Spesialis</th>
                                            </tr>
                                        <?php else: ?>
                                            <tr>
                                                <th>Kode Faskes</th>
                                                <th>Nama Fasilitas Kesehatan</th>
                                            </tr>
                                        <?php endif; ?>
                                    </thead>
                                    <tbody>
                                        <?php foreach ($results as $item): ?>
                                            <tr>
                                                <td><span class="badge-code"><?= $item['kode'] ?></span></td>
                                                <td class="fw-semibold"><?= $item['nama'] ?></td>
                                            </tr>
                                        <?php endforeach; ?>

                                        <?php if (empty($results) && !$error): ?>
                                            <tr>
                                                <td colspan="2" class="text-center py-5">
                                                    <i class="bi bi-inbox text-secondary opacity-25" style="font-size: 64px;"></i>
                                                    <p class="text-secondary mt-3">Tidak ada data yang ditemukan.</p>
                                                </td>
                                            </tr>
                                        <?php endif; ?>
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    <?php else: ?>
                        <div class="text-center py-5">
                            <div class="opacity-25"><i class="bi bi-search" style="font-size: 80px;"></i></div>
                            <h5 class="mt-3 text-secondary">Silakan masukkan kata kunci pencarian</h5>
                            <p class="text-muted small">Minimal 3 karakter untuk mendapatkan hasil yang akurat.</p>
                        </div>
                    <?php endif; ?>
                </div>
            </div>
            
            <div class="mt-4 d-flex align-items-center gap-2 text-secondary small">
                <i class="bi bi-info-circle"></i>
                <span>Data bersumber langsung dari server BPJS V-Claim 2.0 melalui API Bridging.</span>
            </div>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
