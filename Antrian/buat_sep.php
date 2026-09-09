<?php
session_start();
error_reporting(E_ALL);
ini_set('display_errors', 1);

include 'koneksi.php';   // $pdo — untuk setting_vclaim
include 'koneksi2.php';  // $pdo_simrs — untuk data SIMRS

require_once __DIR__ . '/vendor/autoload.php';
use LZCompressor\LZString;

date_default_timezone_set('Asia/Jakarta');

// === FUNGSI ENKRIPSI/KOMPRESI ===
if (!function_exists('vclaimEncrypt')) {
    function vclaimEncrypt($key, $data) {
        $method   = 'AES-256-CBC';
        $key_hash = hex2bin(hash('sha256', $key));
        $iv       = substr($key_hash, 0, 16);
        return base64_encode(openssl_encrypt($data, $method, $key_hash, OPENSSL_RAW_DATA, $iv));
    }
    function vclaimDecrypt($key, $string) {
        $method   = 'AES-256-CBC';
        $key_hash = hex2bin(hash('sha256', $key));
        $iv       = substr($key_hash, 0, 16);
        return openssl_decrypt(base64_decode($string), $method, $key_hash, OPENSSL_RAW_DATA, $iv);
    }
    function vclaimCompress($str)   { return LZString::compressToEncodedURIComponent($str); }
    function vclaimDecompress($str) { return LZString::decompressFromEncodedURIComponent($str); }
}

// === CEK INPUT ===
if (!isset($_GET['no_rawat'])) {
    die("<div style='font-family:sans-serif;padding:20px;color:red'>⚠ Parameter no_rawat diperlukan.</div>");
}
$no_rawat = trim($_GET['no_rawat']);

// === AMBIL SETTING VCLAIM ===
$stmt    = $pdo->query("SELECT * FROM setting_vclaim LIMIT 1");
$setting = $stmt->fetch(PDO::FETCH_ASSOC);
if (!$setting) {
    die("<div style='font-family:sans-serif;padding:20px;color:red'>⚠ Konfigurasi VClaim belum diatur. Silakan atur di menu Setting VClaim.</div>");
}

$cons_id    = trim($setting['cons_id']);
$secret_key = trim($setting['secret_key']);
$user_key   = trim($setting['user_key']);
$base_url   = rtrim(trim($setting['base_url']), '/');
$kd_ppk     = trim($setting['kd_ppk']);

// === AMBIL DATA PASIEN & REGISTRASI dari SIMRS ===
$stmt = $pdo_simrs->prepare("
    SELECT r.no_rawat, r.tgl_registrasi, r.kd_dokter, r.kd_poli,
           p.no_rkm_medis, p.nm_pasien, p.no_peserta, p.jk,
           p.tmp_lahir, p.tgl_lahir, p.no_tlp,
           pol.nm_poli, dok.nm_dokter
    FROM reg_periksa r
    JOIN pasien     p   ON p.no_rkm_medis = r.no_rkm_medis
    LEFT JOIN poliklinik pol ON pol.kd_poli   = r.kd_poli
    LEFT JOIN dokter     dok ON dok.kd_dokter = r.kd_dokter
    WHERE r.no_rawat = ?
    LIMIT 1
");
$stmt->execute([$no_rawat]);
$pasien = $stmt->fetch(PDO::FETCH_ASSOC);

if (!$pasien) {
    die("<div style='font-family:sans-serif;padding:20px;color:red'>⚠ Data pasien untuk no rawat <b>" . htmlspecialchars($no_rawat) . "</b> tidak ditemukan.</div>");
}

$noKartu = trim($pasien['no_peserta'] ?? '');

// === FUNGSI CALL VCLAIM ===
function callVClaimSEP($endpoint, $method = 'GET', $data = null) {
    global $cons_id, $secret_key, $user_key, $base_url;

    date_default_timezone_set('UTC');
    $tStamp   = strval(time());
    date_default_timezone_set('Asia/Jakarta');

    $signature = base64_encode(hash_hmac('sha256', $cons_id . "&" . $tStamp, $secret_key, true));
    $key       = $cons_id . $secret_key . $tStamp;

    $headers = [
        "X-cons-id: $cons_id",
        "X-timestamp: $tStamp",
        "X-signature: $signature",
        "user_key: $user_key",
    ];

    $ch = curl_init($base_url . $endpoint);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);
    curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);
    curl_setopt($ch, CURLOPT_TIMEOUT, 20);

    if ($method === 'POST' && $data !== null) {
        $compressed = vclaimCompress(json_encode($data));
        $encrypted  = vclaimEncrypt($key, $compressed);
        $headers[]  = "Content-Type: application/x-www-form-urlencoded";
        curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);
        curl_setopt($ch, CURLOPT_POST, true);
        curl_setopt($ch, CURLOPT_POSTFIELDS, "request=" . urlencode($encrypted));
    }

    $response = curl_exec($ch);
    $curlErr  = curl_error($ch);
    curl_close($ch);

    if ($curlErr) return ['error' => $curlErr];

    $result = json_decode($response, true);
    if (isset($result['response']) && is_string($result['response']) && !empty($result['response'])) {
        $decrypted    = vclaimDecrypt($key, $result['response']);
        $decompressed = vclaimDecompress($decrypted);
        $result['response'] = json_decode($decompressed, true);
    }
    return $result;
}

// === PROSES BUAT SEP ===
$result  = null;
$message = '';
$isError = false;

if ($_SERVER['REQUEST_METHOD'] === 'POST' && isset($_POST['buatSep'])) {
    if (empty($noKartu)) {
        $message = "⚠ Pasien tidak memiliki nomor kartu BPJS di database.";
        $isError = true;
    } else {
        $jnsPelayanan = trim($_POST['jns_pelayanan'] ?? '2'); // 2 = rawat jalan
        $klsHak       = trim($_POST['kls_hak'] ?? '3');
        $diagAwal     = trim($_POST['diag_awal'] ?? '');
        $catatan      = trim($_POST['catatan'] ?? '');
        $noRujukan    = trim($_POST['no_rujukan'] ?? '');
        $tglRujukan   = trim($_POST['tgl_rujukan'] ?? '');
        $ppkRujukan   = trim($_POST['ppk_rujukan'] ?? '');
        $asalRujukan  = !empty($noRujukan) ? '2' : '0'; // 2=PPK, 0=tanpa rujukan

        $payload = [
            "request" => [
                "t_sep" => [
                    "noKartu"      => $noKartu,
                    "tglSep"       => date('Y-m-d'),
                    "ppkPelayanan" => $kd_ppk,
                    "jnsPelayanan" => $jnsPelayanan,
                    "klsRawat" => [
                        "klsRawatHak"      => $klsHak,
                        "klsRawatNaik"     => "",
                        "pembiayaan"       => "",
                        "penanggungJawab"  => ""
                    ],
                    "noMR"   => $pasien['no_rkm_medis'],
                    "rujukan" => [
                        "asalRujukan" => $asalRujukan,
                        "tglRujukan"  => $tglRujukan,
                        "noRujukan"   => $noRujukan,
                        "ppkRujukan"  => $ppkRujukan
                    ],
                    "catatan"  => $catatan,
                    "diagAwal" => $diagAwal,
                    "poli" => [
                        "tujuan"    => $pasien['kd_poli'],
                        "eksekutif" => "0"
                    ],
                    "cob"     => ["cob" => "0"],
                    "katarak" => ["katarak" => "0"],
                    "jaminan" => [
                        "lakaLantas" => "0",
                        "noLP"       => "",
                        "penjamin"   => [
                            "tglKejadian" => "",
                            "keterangan"  => "",
                            "suplesi"     => [
                                "suplesi"       => "0",
                                "noSepSuplesi"  => "",
                                "lokasiLaka"    => [
                                    "kdPropinsi"   => "",
                                    "kdKabupaten"  => "",
                                    "kdKecamatan"  => ""
                                ]
                            ]
                        ]
                    ],
                    "tujuanKunj"    => "0",
                    "flagProcedure" => "0",
                    "kdPenunjang"   => "",
                    "assesmentPel"  => "",
                    "skdp" => [
                        "noSurat"   => "",
                        "kodeDPJP"  => $pasien['kd_dokter'] ?? ""
                    ],
                    "dpjpLayan" => $pasien['kd_dokter'] ?? "",
                    "noTelp"    => $pasien['no_tlp'] ?? "",
                    "user"      => "MediFix"
                ]
            ]
        ];

        $result = callVClaimSEP("/SEP/2.0/insert", "POST", $payload);

        if (isset($result['metaData']['code']) && $result['metaData']['code'] == 200) {
            $message = "✅ SEP berhasil dibuat!";
            $isError = false;
        } else {
            $message = "❌ Gagal: " . ($result['metaData']['message'] ?? ($result['error'] ?? "Error tidak diketahui"));
            $isError = true;
        }
    }
}
?>
<!DOCTYPE html>
<html lang="id">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Buat SEP — MediFix</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons/font/bootstrap-icons.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Poppins:wght@400;600;700&display=swap" rel="stylesheet">
    <style>
        body { font-family: 'Poppins', sans-serif; background: linear-gradient(135deg, #667eea, #764ba2); min-height: 100vh; padding: 30px 15px; }
        .card { border-radius: 16px; box-shadow: 0 10px 40px rgba(0,0,0,.2); border: none; }
        .card-header { background: linear-gradient(135deg, #667eea, #764ba2); color: #fff; border-radius: 16px 16px 0 0 !important; padding: 20px 25px; }
        .info-row { display: flex; gap: 10px; background: #f8fafc; border-radius: 10px; padding: 12px 15px; margin-bottom: 10px; align-items: center; }
        .info-label { font-size: 11px; font-weight: 700; color: #94a3b8; text-transform: uppercase; letter-spacing: .5px; }
        .info-value { font-size: 15px; font-weight: 700; color: #1e293b; }
        .sep-result { background: linear-gradient(135deg, #dcfce7, #bbf7d0); border: 2px solid #4ade80; border-radius: 14px; padding: 20px; text-align: center; }
        .sep-number { font-family: monospace; font-size: 1.8rem; font-weight: 900; color: #166534; letter-spacing: 2px; }
    </style>
</head>
<body>
<div class="container" style="max-width:750px">

    <div class="card">
        <div class="card-header">
            <h4 class="mb-0"><i class="bi bi-file-earmark-medical-fill"></i> Buat SEP — <?= htmlspecialchars($pasien['nm_pasien']) ?></h4>
        </div>
        <div class="card-body p-4">

            <?php if ($message): ?>
                <div class="alert alert-<?= $isError ? 'danger' : 'success' ?> d-flex align-items-center gap-2">
                    <i class="bi bi-<?= $isError ? 'exclamation-triangle-fill' : 'check-circle-fill' ?>"></i>
                    <div><?= $message ?></div>
                </div>
            <?php endif; ?>

            <?php if (!$isError && $result && isset($result['response']['sep'])): ?>
            <div class="sep-result mb-4">
                <div class="text-muted small mb-1">Nomor SEP:</div>
                <div class="sep-number"><?= htmlspecialchars($result['response']['sep']['noSep'] ?? '-') ?></div>
                <div class="mt-2 text-muted small">Tgl: <?= htmlspecialchars($result['response']['sep']['tglSep'] ?? '') ?></div>
                <button onclick="window.print()" class="btn btn-success mt-3 px-4">
                    <i class="bi bi-printer-fill"></i> Cetak SEP
                </button>
            </div>
            <?php endif; ?>

            <!-- Info Pasien -->
            <h6 class="fw-bold text-muted mb-3">DATA PASIEN</h6>
            <div class="row g-2 mb-4">
                <div class="col-md-6">
                    <div class="info-row">
                        <i class="bi bi-person-fill text-primary fs-5"></i>
                        <div>
                            <div class="info-label">Nama Pasien</div>
                            <div class="info-value"><?= htmlspecialchars($pasien['nm_pasien']) ?></div>
                        </div>
                    </div>
                </div>
                <div class="col-md-6">
                    <div class="info-row">
                        <i class="bi bi-credit-card-fill text-success fs-5"></i>
                        <div>
                            <div class="info-label">No. Kartu BPJS</div>
                            <div class="info-value"><?= $noKartu ?: '<span class="text-danger">Belum ada</span>' ?></div>
                        </div>
                    </div>
                </div>
                <div class="col-md-6">
                    <div class="info-row">
                        <i class="bi bi-hospital-fill text-info fs-5"></i>
                        <div>
                            <div class="info-label">Poli Tujuan</div>
                            <div class="info-value"><?= htmlspecialchars($pasien['nm_poli'] ?? $pasien['kd_poli']) ?></div>
                        </div>
                    </div>
                </div>
                <div class="col-md-6">
                    <div class="info-row">
                        <i class="bi bi-person-badge-fill text-warning fs-5"></i>
                        <div>
                            <div class="info-label">Dokter</div>
                            <div class="info-value"><?= htmlspecialchars($pasien['nm_dokter'] ?? $pasien['kd_dokter']) ?></div>
                        </div>
                    </div>
                </div>
            </div>

            <?php if (empty($noKartu)): ?>
            <div class="alert alert-warning">
                <i class="bi bi-exclamation-triangle-fill"></i>
                Pasien ini tidak memiliki nomor kartu BPJS di database. SEP tidak dapat dibuat.
            </div>
            <?php else: ?>
            <!-- Form Buat SEP -->
            <h6 class="fw-bold text-muted mb-3">PARAMETER SEP</h6>
            <form method="POST">
                <div class="row g-3 mb-3">
                    <div class="col-md-6">
                        <label class="form-label fw-bold">Jenis Pelayanan</label>
                        <select name="jns_pelayanan" class="form-select">
                            <option value="2" selected>Rawat Jalan</option>
                            <option value="1">Rawat Inap</option>
                        </select>
                    </div>
                    <div class="col-md-6">
                        <label class="form-label fw-bold">Kelas Hak</label>
                        <select name="kls_hak" class="form-select">
                            <option value="1">Kelas 1</option>
                            <option value="2">Kelas 2</option>
                            <option value="3" selected>Kelas 3</option>
                        </select>
                    </div>
                    <div class="col-md-12">
                        <label class="form-label fw-bold">Diagnosis Awal (ICD-10)</label>
                        <input type="text" name="diag_awal" class="form-control" placeholder="Contoh: A00 atau kosongkan jika belum ada">
                    </div>
                    <div class="col-md-4">
                        <label class="form-label fw-bold">No. Rujukan (jika ada)</label>
                        <input type="text" name="no_rujukan" class="form-control" placeholder="Nomor rujukan FKTP">
                    </div>
                    <div class="col-md-4">
                        <label class="form-label fw-bold">Tgl. Rujukan</label>
                        <input type="date" name="tgl_rujukan" class="form-control">
                    </div>
                    <div class="col-md-4">
                        <label class="form-label fw-bold">Kode PPK Rujukan</label>
                        <input type="text" name="ppk_rujukan" class="form-control" placeholder="Kode FKTP">
                    </div>
                    <div class="col-md-12">
                        <label class="form-label fw-bold">Catatan (opsional)</label>
                        <input type="text" name="catatan" class="form-control" placeholder="Catatan SEP">
                    </div>
                </div>

                <div class="d-flex gap-2">
                    <button type="submit" name="buatSep" class="btn btn-primary btn-lg flex-grow-1 fw-bold">
                        <i class="bi bi-send-check-fill"></i> Buat SEP ke BPJS
                    </button>
                    <a href="sep_poli.php" class="btn btn-outline-secondary btn-lg">
                        <i class="bi bi-arrow-left"></i> Kembali
                    </a>
                </div>
            </form>
            <?php endif; ?>

        </div>
    </div>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
