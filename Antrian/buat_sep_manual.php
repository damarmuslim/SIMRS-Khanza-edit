<?php
/**
 * BUAT SEP MANUAL
 * Form input manual untuk membuat SEP (Surat Eligibilitas Peserta) BPJS
 * tanpa bergantung pada no_rawat dari sistem SIMRS
 * 
 * File: buat_sep_manual.php
 */

session_start();
error_reporting(E_ALL);
ini_set('display_errors', 1);
date_default_timezone_set('Asia/Jakarta');

include 'koneksi.php';  // koneksi DB anjungan (setting_vclaim)
include 'koneksi2.php'; // koneksi DB SIMRS (poliklinik, dokter, pasien)

if (!isset($_SESSION['user_id'])) {
    header('Location: login.php');
    exit;
}

// ===== FUNGSI TRUSTMARK VCLAIM =====
function stringEncrypt($key, $data) {
    $encrypt_method = 'AES-256-CBC';
    $key_hash = hex2bin(hash('sha256', $key));
    $iv = substr(hex2bin(hash('sha256', $key)), 0, 16);
    $encrypted = openssl_encrypt($data, $encrypt_method, $key_hash, OPENSSL_RAW_DATA, $iv);
    return base64_encode($encrypted);
}

function stringDecrypt($key, $string) {
    $encrypt_method = 'AES-256-CBC';
    $key_hash = hex2bin(hash('sha256', $key));
    $iv = substr(hex2bin(hash('sha256', $key)), 0, 16);
    return openssl_decrypt(base64_decode($string), $encrypt_method, $key_hash, OPENSSL_RAW_DATA, $iv);
}

// ===== CEK VENDOR LZString =====
$use_lz = false;
if (file_exists(__DIR__ . '/vendor/autoload.php')) {
    require_once __DIR__ . '/vendor/autoload.php';
    if (class_exists('LZCompressor\LZString')) {
        $use_lz = true;
    }
}

function compress($string) {
    global $use_lz;
    if ($use_lz) {
        return \LZCompressor\LZString::compressToEncodedURIComponent($string);
    }
    return $string;
}

function decompress($string) {
    global $use_lz;
    if ($use_lz) {
        return \LZCompressor\LZString::decompressFromEncodedURIComponent($string);
    }
    return $string;
}

// ===== AMBIL SETTING VCLAIM =====
$setting = null;
$settingError = '';
try {
    $stmt = $pdo->query("SELECT * FROM setting_vclaim LIMIT 1");
    $setting = $stmt->fetch(PDO::FETCH_ASSOC);
    if (!$setting) {
        $settingError = '⚠ Setting VClaim belum diset. <a href="setting_vclaim.php">Klik di sini untuk mengatur.</a>';
    }
} catch (PDOException $e) {
    $settingError = '⚠ Gagal membaca setting VClaim: ' . $e->getMessage();
}

// ===== AMBIL DATA POLIKLINIK =====
$daftarPoli = [];
try {
    $stmt = $pdo2->query("SELECT kd_poli, nm_poli FROM poliklinik ORDER BY nm_poli ASC");
    $daftarPoli = $stmt->fetchAll(PDO::FETCH_ASSOC);
} catch (PDOException $e) {
    // abaikan jika gagal
}

// ===== AMBIL DATA DOKTER =====
$daftarDokter = [];
try {
    $stmt = $pdo2->query("SELECT kd_dokter, nm_dokter FROM dokter ORDER BY nm_dokter ASC");
    $daftarDokter = $stmt->fetchAll(PDO::FETCH_ASSOC);
} catch (PDOException $e) {
    // abaikan jika gagal
}

// ===== INISIALISASI VARIABEL RESULT =====
$result    = null;
$noSepHasil = '';
$pesanError = '';
$pesanSukses = '';

// ===== PROSES FORM SUBMIT =====
if ($_SERVER['REQUEST_METHOD'] === 'POST' && isset($_POST['submit_sep'])) {

    if (!$setting) {
        $pesanError = 'Setting VClaim belum dikonfigurasi!';
    } else {
        $cons_id    = trim($setting['cons_id']);
        $secret_key = trim($setting['secret_key']);
        $user_key   = trim($setting['user_key']);
        $base_url   = rtrim(trim($setting['base_url']), '/');
        $kd_ppk     = trim($setting['kd_ppk']);

        // --- Ambil field form ---
        $noKartu      = trim($_POST['noKartu'] ?? '');
        $tglSep       = trim($_POST['tglSep'] ?? date('Y-m-d'));
        $jnsPelayanan = trim($_POST['jnsPelayanan'] ?? '2');
        $klsRawatHak  = trim($_POST['klsRawatHak'] ?? '1');
        $klsRawatNaik = trim($_POST['klsRawatNaik'] ?? '');
        $pembiayaan   = trim($_POST['pembiayaan'] ?? '');
        $penanggungJawab = trim($_POST['penanggungJawab'] ?? '');
        $noMR         = trim($_POST['noMR'] ?? '');
        $asalRujukan  = trim($_POST['asalRujukan'] ?? '2');
        $tglRujukan   = trim($_POST['tglRujukan'] ?? '');
        $noRujukan    = trim($_POST['noRujukan'] ?? '');
        $ppkRujukan   = trim($_POST['ppkRujukan'] ?? $kd_ppk);
        $catatan      = trim($_POST['catatan'] ?? '');
        $diagAwal     = trim($_POST['diagAwal'] ?? '');
        $poliTujuan   = trim($_POST['poliTujuan'] ?? '');
        $poliEksekutif = trim($_POST['poliEksekutif'] ?? '0');
        $cob          = trim($_POST['cob'] ?? '0');
        $katarak      = trim($_POST['katarak'] ?? '0');
        $lakaLantas   = trim($_POST['lakaLantas'] ?? '0');
        $noLP         = trim($_POST['noLP'] ?? '');
        $tglKejadian  = trim($_POST['tglKejadian'] ?? '');
        $keterangan   = trim($_POST['keterangan'] ?? '');
        $suplesi      = trim($_POST['suplesi'] ?? '0');
        $noSepSuplesi = trim($_POST['noSepSuplesi'] ?? '');
        $kdPropinsi   = trim($_POST['kdPropinsi'] ?? '');
        $kdKabupaten  = trim($_POST['kdKabupaten'] ?? '');
        $kdKecamatan  = trim($_POST['kdKecamatan'] ?? '');
        $tujuanKunj   = trim($_POST['tujuanKunj'] ?? '0');
        $flagProcedure = trim($_POST['flagProcedure'] ?? '');
        $kdPenunjang  = trim($_POST['kdPenunjang'] ?? '');
        $assesmentPel = trim($_POST['assesmentPel'] ?? '');
        $noSurat      = trim($_POST['noSurat'] ?? '');
        $kodeDPJP     = trim($_POST['kodeDPJP'] ?? '');
        $dpjpLayan    = trim($_POST['dpjpLayan'] ?? '');
        $noTelp       = trim($_POST['noTelp'] ?? '');
        $user         = $_SESSION['nama'] ?? 'SEP Manual';

        // Validasi wajib
        if (empty($noKartu) || empty($noMR) || empty($poliTujuan)) {
            $pesanError = '⚠ No. Kartu BPJS, No. Rekam Medis, dan Poli Tujuan wajib diisi!';
        } else {
            // === BUILD PAYLOAD SEP 2.0 ===
            $sepPayload = [
                "request" => [
                    "t_sep" => [
                        "noKartu"       => $noKartu,
                        "tglSep"        => $tglSep,
                        "ppkPelayanan"  => $kd_ppk,
                        "jnsPelayanan"  => $jnsPelayanan,
                        "klsRawat"      => [
                            "klsRawatHak"      => $klsRawatHak,
                            "klsRawatNaik"     => $klsRawatNaik,
                            "pembiayaan"       => $pembiayaan,
                            "penanggungJawab"  => $penanggungJawab,
                        ],
                        "noMR"          => $noMR,
                        "rujukan"       => [
                            "asalRujukan" => $asalRujukan,
                            "tglRujukan"  => $tglRujukan,
                            "noRujukan"   => $noRujukan,
                            "ppkRujukan"  => $ppkRujukan,
                        ],
                        "catatan"       => $catatan,
                        "diagAwal"      => $diagAwal,
                        "poli"          => [
                            "tujuan"    => $poliTujuan,
                            "eksekutif" => $poliEksekutif,
                        ],
                        "cob"           => ["cob" => $cob],
                        "katarak"       => ["katarak" => $katarak],
                        "jaminan"       => [
                            "lakaLantas" => $lakaLantas,
                            "noLP"       => $noLP,
                            "penjamin"   => [
                                "tglKejadian" => $tglKejadian,
                                "keterangan"  => $keterangan,
                                "suplesi"     => [
                                    "suplesi"      => $suplesi,
                                    "noSepSuplesi" => $noSepSuplesi,
                                    "lokasiLaka"   => [
                                        "kdPropinsi"  => $kdPropinsi,
                                        "kdKabupaten" => $kdKabupaten,
                                        "kdKecamatan" => $kdKecamatan,
                                    ],
                                ],
                            ],
                        ],
                        "tujuanKunj"    => $tujuanKunj,
                        "flagProcedure" => $flagProcedure,
                        "kdPenunjang"   => $kdPenunjang,
                        "assesmentPel"  => $assesmentPel,
                        "skdp"          => [
                            "noSurat"   => $noSurat,
                            "kodeDPJP"  => $kodeDPJP,
                        ],
                        "dpjpLayan"     => $dpjpLayan,
                        "noTelp"        => $noTelp,
                        "user"          => $user,
                    ]
                ]
            ];

            // === TRUSTMARK AUTH ===
            date_default_timezone_set('UTC');
            $utcTimestamp = strval(time());
            date_default_timezone_set('Asia/Jakarta');
            $signature = base64_encode(hash_hmac('sha256', $cons_id . "&" . $utcTimestamp, $secret_key, true));

            // === ENKRIPSI PAYLOAD ===
            $jsonString = json_encode($sepPayload);
            $encKey = $cons_id . $secret_key . $utcTimestamp;
            $compressed = compress($jsonString);
            $encrypted  = stringEncrypt($encKey, $compressed);

            $headers = [
                "X-cons-id: $cons_id",
                "X-timestamp: $utcTimestamp",
                "X-signature: $signature",
                "user_key: $user_key",
                "Content-Type: application/x-www-form-urlencoded",
            ];

            $endpoint = $base_url . "/SEP/2.0/insert";
            $ch = curl_init($endpoint);
            curl_setopt_array($ch, [
                CURLOPT_RETURNTRANSFER => true,
                CURLOPT_HTTPHEADER     => $headers,
                CURLOPT_POST           => true,
                CURLOPT_POSTFIELDS     => "request=" . urlencode($encrypted),
                CURLOPT_TIMEOUT        => 30,
                CURLOPT_SSL_VERIFYPEER => false,
                CURLOPT_SSL_VERIFYHOST => false,
            ]);

            $rawResponse = curl_exec($ch);
            $httpCode    = curl_getinfo($ch, CURLINFO_HTTP_CODE);
            $curlError   = curl_error($ch);
            curl_close($ch);

            if ($curlError) {
                $pesanError = "❌ Curl Error: $curlError";
            } else {
                $isHtml = (stripos($rawResponse, '<html') !== false);

                if (!$isHtml && !empty($rawResponse)) {
                    $jsonResp = json_decode($rawResponse, true);

                    // Decrypt jika response terenkripsi
                    if (isset($jsonResp['response']) && is_string($jsonResp['response']) && !empty($jsonResp['response'])) {
                        try {
                            $dec = stringDecrypt($encKey, $jsonResp['response']);
                            $decomp = decompress($dec);
                            if ($decomp) {
                                $jsonResp['response'] = json_decode($decomp, true);
                            }
                        } catch (Exception $e) {
                            // abaikan error decrypt
                        }
                    }

                    $result = $jsonResp;
                    $metaCode = $result['metaData']['code'] ?? $result['response']['metaData']['code'] ?? 0;
                    $metaMsg  = $result['metaData']['message'] ?? $result['response']['metaData']['message'] ?? 'Tidak ada pesan';

                    if ($metaCode == 200) {
                        $noSepHasil = $result['response']['sep']['noSep']
                            ?? $result['response']['response']['sep']['noSep']
                            ?? '-';
                        $pesanSukses = "✅ SEP berhasil dibuat! No. SEP: <strong>$noSepHasil</strong>";
                    } else {
                        $pesanError = "❌ Gagal membuat SEP. Code: $metaCode | Pesan: " . htmlspecialchars($metaMsg);
                    }
                } else {
                    $pesanError = "❌ Response tidak valid (HTTP $httpCode). Server mungkin mengembalikan HTML error.";
                }
            }
        }
    }
}

// ===== SETUP LAYOUT =====
$page_title = 'Buat SEP Manual - MediFix';

$extra_css = '
.section-title {
    font-size: 13px;
    font-weight: 700;
    color: #3b82f6;
    text-transform: uppercase;
    letter-spacing: 0.5px;
    border-bottom: 2px solid #e2e8f0;
    padding-bottom: 6px;
    margin-bottom: 15px;
    margin-top: 5px;
}

.form-control {
    border: 1.5px solid #e2e8f0;
    border-radius: 4px;
    font-size: 13px;
}
.form-control:focus {
    border-color: #3b82f6;
    box-shadow: 0 0 0 2px rgba(59,130,246,0.15);
}
.form-group label {
    font-weight: 600;
    font-size: 12px;
    color: #4a5568;
}
.required-star { color: #e53e3e; }

.result-box {
    border-radius: 8px;
    padding: 16px 20px;
    margin-bottom: 16px;
    border-left: 5px solid;
}
.result-box.success { background: #f0fdf4; border-color: #22c55e; color: #166534; }
.result-box.error   { background: #fef2f2; border-color: #ef4444; color: #991b1b; }
.result-box h4 { margin: 0 0 6px 0; font-size: 15px; }
.result-box .no-sep { font-size: 24px; font-weight: 800; letter-spacing: 1px; color: #15803d; }

.info-setting {
    background: #eff6ff;
    border-left: 4px solid #3b82f6;
    padding: 10px 15px;
    border-radius: 4px;
    font-size: 12px;
    color: #1e40af;
    margin-bottom: 16px;
}

.btn-kirim {
    background: linear-gradient(135deg, #3b82f6, #2563eb);
    color: white;
    border: none;
    padding: 10px 30px;
    font-weight: 700;
    border-radius: 5px;
    font-size: 14px;
    transition: all 0.2s ease;
}
.btn-kirim:hover {
    background: linear-gradient(135deg, #2563eb, #1d4ed8);
    color: white;
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(59,130,246,0.4);
}
.btn-reset {
    background: #f1f5f9;
    color: #475569;
    border: 1px solid #cbd5e1;
    padding: 10px 20px;
    font-weight: 600;
    border-radius: 5px;
    font-size: 14px;
}
.btn-reset:hover {
    background: #e2e8f0;
    color: #334155;
}

/* collapsible section */
.section-toggle { cursor: pointer; user-select: none; }
.section-toggle:hover { color: #2563eb; }
.section-toggle i.arrow { transition: transform 0.2s; }
.section-toggle.collapsed i.arrow { transform: rotate(-90deg); }
';

$extra_js = '
<script>
// Toggle collapsible sections
document.querySelectorAll(".section-toggle").forEach(function(el) {
    el.addEventListener("click", function() {
        var target = document.querySelector(this.dataset.target);
        if (target) {
            var visible = target.style.display !== "none";
            target.style.display = visible ? "none" : "block";
            this.classList.toggle("collapsed", visible);
        }
    });
});

// Toggle lakaLantas fields
function toggleLaka() {
    var val = document.getElementById("lakaLantas").value;
    var el  = document.getElementById("laka-fields");
    el.style.display = (val === "1") ? "block" : "none";
}

// Toggle suplesi fields
function toggleSuplesi() {
    var val = document.getElementById("suplesi").value;
    var el  = document.getElementById("suplesi-fields");
    el.style.display = (val === "1") ? "block" : "none";
}

// Auto-fill ppkRujukan dari setting
document.addEventListener("DOMContentLoaded", function() {
    toggleLaka();
    toggleSuplesi();
});
</script>
';

include 'includes/header.php';
include 'includes/sidebar.php';
?>

  <div class="content-wrapper">
    <section class="content-header">
      <h1><i class="fa fa-file-text-o"></i> Buat SEP Manual</h1>
      <ol class="breadcrumb">
        <li><a href="dashboard.php"><i class="fa fa-dashboard"></i> Home</a></li>
        <li><a href="#">BPJS</a></li>
        <li class="active">Buat SEP Manual</li>
      </ol>
    </section>

    <section class="content">

      <?php if ($settingError): ?>
        <div class="alert alert-warning">
          <i class="fa fa-exclamation-triangle"></i>
          <?= $settingError ?>
        </div>
      <?php endif; ?>

      <!-- ===== HASIL SEP ===== -->
      <?php if ($pesanSukses): ?>
        <div class="result-box success">
          <h4><i class="fa fa-check-circle"></i> SEP Berhasil Dibuat</h4>
          <div><?= $pesanSukses ?></div>
          <?php if ($noSepHasil && $noSepHasil !== '-'): ?>
            <div class="no-sep mt-2"><i class="fa fa-barcode"></i> <?= htmlspecialchars($noSepHasil) ?></div>
          <?php endif; ?>
          <?php if ($result): ?>
            <details class="mt-2" style="font-size:12px;">
              <summary style="cursor:pointer;color:#166534;">Lihat detail response</summary>
              <pre style="background:#f0fdf4;margin-top:8px;padding:10px;border-radius:4px;font-size:11px;"><?= json_encode($result, JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE) ?></pre>
            </details>
          <?php endif; ?>
        </div>
      <?php endif; ?>

      <?php if ($pesanError): ?>
        <div class="result-box error">
          <h4><i class="fa fa-times-circle"></i> Gagal Membuat SEP</h4>
          <div><?= $pesanError ?></div>
        </div>
      <?php endif; ?>

      <!-- ===== FORM ===== -->
      <form method="POST" id="formSepManual">

        <div class="row">

          <!-- =============================== -->
          <!-- KOLOM KIRI -->
          <!-- =============================== -->
          <div class="col-md-8">

            <!-- BOX: Data Peserta -->
            <div class="box box-primary">
              <div class="box-header with-border">
                <h3 class="box-title"><i class="fa fa-id-card-o"></i> Data Peserta</h3>
              </div>
              <div class="box-body">

                <?php if ($setting): ?>
                  <div class="info-setting">
                    <i class="fa fa-hospital-o"></i>
                    <strong>PPK Pelayanan:</strong> <?= htmlspecialchars($setting['kd_ppk']) ?> — <?= htmlspecialchars($setting['nm_ppk'] ?? '') ?>
                  </div>
                <?php endif; ?>

                <div class="row">
                  <div class="col-md-6">
                    <div class="form-group">
                      <label>No. Kartu BPJS <span class="required-star">*</span></label>
                      <input type="text" name="noKartu" class="form-control"
                             value="<?= htmlspecialchars($_POST['noKartu'] ?? '') ?>"
                             placeholder="13 digit No. Kartu BPJS" maxlength="13" required>
                    </div>
                  </div>
                  <div class="col-md-6">
                    <div class="form-group">
                      <label>No. Rekam Medis <span class="required-star">*</span></label>
                      <input type="text" name="noMR" class="form-control"
                             value="<?= htmlspecialchars($_POST['noMR'] ?? '') ?>"
                             placeholder="No. Rekam Medis pasien" required>
                    </div>
                  </div>
                </div>

                <div class="row">
                  <div class="col-md-4">
                    <div class="form-group">
                      <label>Tanggal SEP <span class="required-star">*</span></label>
                      <input type="date" name="tglSep" class="form-control"
                             value="<?= htmlspecialchars($_POST['tglSep'] ?? date('Y-m-d')) ?>" required>
                    </div>
                  </div>
                  <div class="col-md-4">
                    <div class="form-group">
                      <label>Jenis Pelayanan <span class="required-star">*</span></label>
                      <select name="jnsPelayanan" class="form-control">
                        <option value="1" <?= (($_POST['jnsPelayanan'] ?? '') == '1') ? 'selected' : '' ?>>1 - Rawat Inap</option>
                        <option value="2" <?= (($_POST['jnsPelayanan'] ?? '2') == '2') ? 'selected' : '' ?>>2 - Rawat Jalan</option>
                      </select>
                    </div>
                  </div>
                  <div class="col-md-4">
                    <div class="form-group">
                      <label>No. Telepon Peserta</label>
                      <input type="text" name="noTelp" class="form-control"
                             value="<?= htmlspecialchars($_POST['noTelp'] ?? '') ?>"
                             placeholder="08xxxxxxxxxx">
                    </div>
                  </div>
                </div>

              </div>
            </div>

            <!-- BOX: Kelas Rawat -->
            <div class="box box-info">
              <div class="box-header with-border">
                <h3 class="box-title"><i class="fa fa-bed"></i> Kelas Rawat</h3>
              </div>
              <div class="box-body">
                <div class="row">
                  <div class="col-md-3">
                    <div class="form-group">
                      <label>Kelas Hak <span class="required-star">*</span></label>
                      <select name="klsRawatHak" class="form-control">
                        <option value="1" <?= (($_POST['klsRawatHak'] ?? '1') == '1') ? 'selected' : '' ?>>1 - Kelas 1</option>
                        <option value="2" <?= (($_POST['klsRawatHak'] ?? '') == '2') ? 'selected' : '' ?>>2 - Kelas 2</option>
                        <option value="3" <?= (($_POST['klsRawatHak'] ?? '') == '3') ? 'selected' : '' ?>>3 - Kelas 3</option>
                      </select>
                    </div>
                  </div>
                  <div class="col-md-3">
                    <div class="form-group">
                      <label>Kelas Naik</label>
                      <select name="klsRawatNaik" class="form-control">
                        <option value="">- Tidak Naik -</option>
                        <option value="1" <?= (($_POST['klsRawatNaik'] ?? '') == '1') ? 'selected' : '' ?>>Kelas 1</option>
                        <option value="2" <?= (($_POST['klsRawatNaik'] ?? '') == '2') ? 'selected' : '' ?>>Kelas 2</option>
                        <option value="VIP" <?= (($_POST['klsRawatNaik'] ?? '') == 'VIP') ? 'selected' : '' ?>>VIP</option>
                      </select>
                    </div>
                  </div>
                  <div class="col-md-3">
                    <div class="form-group">
                      <label>Pembiayaan</label>
                      <select name="pembiayaan" class="form-control">
                        <option value="">- Pilih -</option>
                        <option value="1" <?= (($_POST['pembiayaan'] ?? '') == '1') ? 'selected' : '' ?>>1 - Pribadi</option>
                        <option value="2" <?= (($_POST['pembiayaan'] ?? '') == '2') ? 'selected' : '' ?>>2 - Pemberi Kerja</option>
                        <option value="3" <?= (($_POST['pembiayaan'] ?? '') == '3') ? 'selected' : '' ?>>3 - Asuransi Lain</option>
                      </select>
                    </div>
                  </div>
                  <div class="col-md-3">
                    <div class="form-group">
                      <label>Penanggung Jawab</label>
                      <input type="text" name="penanggungJawab" class="form-control"
                             value="<?= htmlspecialchars($_POST['penanggungJawab'] ?? '') ?>"
                             placeholder="(opsional)">
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <!-- BOX: Data Poli & Diagnosa -->
            <div class="box box-success">
              <div class="box-header with-border">
                <h3 class="box-title"><i class="fa fa-stethoscope"></i> Poli Tujuan & Diagnosa</h3>
              </div>
              <div class="box-body">
                <div class="row">
                  <div class="col-md-6">
                    <div class="form-group">
                      <label>Poli Tujuan <span class="required-star">*</span></label>
                      <select name="poliTujuan" class="form-control" required>
                        <option value="">-- Pilih Poli --</option>
                        <?php foreach ($daftarPoli as $poli): ?>
                          <option value="<?= htmlspecialchars($poli['kd_poli']) ?>"
                            <?= (($_POST['poliTujuan'] ?? '') == $poli['kd_poli']) ? 'selected' : '' ?>>
                            <?= htmlspecialchars($poli['kd_poli']) ?> - <?= htmlspecialchars($poli['nm_poli']) ?>
                          </option>
                        <?php endforeach; ?>
                      </select>
                    </div>
                  </div>
                  <div class="col-md-3">
                    <div class="form-group">
                      <label>Eksekutif</label>
                      <select name="poliEksekutif" class="form-control">
                        <option value="0" <?= (($_POST['poliEksekutif'] ?? '0') == '0') ? 'selected' : '' ?>>0 - Tidak</option>
                        <option value="1" <?= (($_POST['poliEksekutif'] ?? '') == '1') ? 'selected' : '' ?>>1 - Ya</option>
                      </select>
                    </div>
                  </div>
                  <div class="col-md-3">
                    <div class="form-group">
                      <label>Tujuan Kunjungan</label>
                      <select name="tujuanKunj" class="form-control">
                        <option value="0" <?= (($_POST['tujuanKunj'] ?? '0') == '0') ? 'selected' : '' ?>>0 - Normal</option>
                        <option value="1" <?= (($_POST['tujuanKunj'] ?? '') == '1') ? 'selected' : '' ?>>1 - Prosedur</option>
                        <option value="2" <?= (($_POST['tujuanKunj'] ?? '') == '2') ? 'selected' : '' ?>>2 - Konsul Dokter</option>
                      </select>
                    </div>
                  </div>
                </div>

                <div class="row">
                  <div class="col-md-4">
                    <div class="form-group">
                      <label>Diagnosa Awal (ICD-10)</label>
                      <input type="text" name="diagAwal" class="form-control"
                             value="<?= htmlspecialchars($_POST['diagAwal'] ?? '') ?>"
                             placeholder="Contoh: Z03.8">
                    </div>
                  </div>
                  <div class="col-md-4">
                    <div class="form-group">
                      <label>Kode DPJP</label>
                      <select name="kodeDPJP" class="form-control">
                        <option value="">-- Pilih Dokter --</option>
                        <?php foreach ($daftarDokter as $dok): ?>
                          <option value="<?= htmlspecialchars($dok['kd_dokter']) ?>"
                            <?= (($_POST['kodeDPJP'] ?? '') == $dok['kd_dokter']) ? 'selected' : '' ?>>
                            <?= htmlspecialchars($dok['kd_dokter']) ?> - <?= htmlspecialchars($dok['nm_dokter']) ?>
                          </option>
                        <?php endforeach; ?>
                      </select>
                    </div>
                  </div>
                  <div class="col-md-4">
                    <div class="form-group">
                      <label>DPJP Layanan</label>
                      <input type="text" name="dpjpLayan" class="form-control"
                             value="<?= htmlspecialchars($_POST['dpjpLayan'] ?? '') ?>"
                             placeholder="Kode DPJP Layanan">
                    </div>
                  </div>
                </div>

                <div class="row">
                  <div class="col-md-6">
                    <div class="form-group">
                      <label>No. Surat SKDP</label>
                      <input type="text" name="noSurat" class="form-control"
                             value="<?= htmlspecialchars($_POST['noSurat'] ?? '') ?>"
                             placeholder="No. Surat Kontrol / SKDP">
                    </div>
                  </div>
                  <div class="col-md-6">
                    <div class="form-group">
                      <label>Catatan</label>
                      <input type="text" name="catatan" class="form-control"
                             value="<?= htmlspecialchars($_POST['catatan'] ?? '') ?>"
                             placeholder="Catatan tambahan">
                    </div>
                  </div>
                </div>

              </div>
            </div>

            <!-- BOX: Data Rujukan -->
            <div class="box box-warning">
              <div class="box-header with-border">
                <h3 class="box-title"><i class="fa fa-share-square-o"></i> Data Rujukan</h3>
              </div>
              <div class="box-body">
                <div class="row">
                  <div class="col-md-3">
                    <div class="form-group">
                      <label>Asal Rujukan</label>
                      <select name="asalRujukan" class="form-control">
                        <option value="1" <?= (($_POST['asalRujukan'] ?? '') == '1') ? 'selected' : '' ?>>1 - Faskes 1</option>
                        <option value="2" <?= (($_POST['asalRujukan'] ?? '2') == '2') ? 'selected' : '' ?>>2 - Faskes 2</option>
                        <option value="3" <?= (($_POST['asalRujukan'] ?? '') == '3') ? 'selected' : '' ?>>3 - Faskes 3</option>
                      </select>
                    </div>
                  </div>
                  <div class="col-md-3">
                    <div class="form-group">
                      <label>Tanggal Rujukan</label>
                      <input type="date" name="tglRujukan" class="form-control"
                             value="<?= htmlspecialchars($_POST['tglRujukan'] ?? '') ?>">
                    </div>
                  </div>
                  <div class="col-md-6">
                    <div class="form-group">
                      <label>No. Rujukan</label>
                      <input type="text" name="noRujukan" class="form-control"
                             value="<?= htmlspecialchars($_POST['noRujukan'] ?? '') ?>"
                             placeholder="No. Surat Rujukan dari Faskes">
                    </div>
                  </div>
                </div>

                <div class="form-group">
                  <label>PPK Rujukan (Kode Faskes Perujuk)</label>
                  <input type="text" name="ppkRujukan" class="form-control"
                         value="<?= htmlspecialchars($_POST['ppkRujukan'] ?? ($setting['kd_ppk'] ?? '')) ?>"
                         placeholder="Kode Faskes yang merujuk">
                </div>

              </div>
            </div>

          </div><!-- /col-md-8 -->

          <!-- =============================== -->
          <!-- KOLOM KANAN -->
          <!-- =============================== -->
          <div class="col-md-4">

            <!-- BOX: Opsi Tambahan -->
            <div class="box box-default">
              <div class="box-header with-border">
                <h3 class="box-title"><i class="fa fa-sliders"></i> Opsi Tambahan</h3>
              </div>
              <div class="box-body">

                <div class="form-group">
                  <label>COB (Coordination of Benefit)</label>
                  <select name="cob" class="form-control">
                    <option value="0" <?= (($_POST['cob'] ?? '0') == '0') ? 'selected' : '' ?>>0 - Tidak</option>
                    <option value="1" <?= (($_POST['cob'] ?? '') == '1') ? 'selected' : '' ?>>1 - Ya</option>
                  </select>
                </div>

                <div class="form-group">
                  <label>Katarak</label>
                  <select name="katarak" class="form-control">
                    <option value="0" <?= (($_POST['katarak'] ?? '0') == '0') ? 'selected' : '' ?>>0 - Tidak</option>
                    <option value="1" <?= (($_POST['katarak'] ?? '') == '1') ? 'selected' : '' ?>>1 - Ya</option>
                  </select>
                </div>

                <div class="form-group">
                  <label>Flag Procedure</label>
                  <select name="flagProcedure" class="form-control">
                    <option value="" <?= (($_POST['flagProcedure'] ?? '') == '') ? 'selected' : '' ?>>- Pilih -</option>
                    <option value="0" <?= (($_POST['flagProcedure'] ?? '') == '0') ? 'selected' : '' ?>>0 - Tidak</option>
                    <option value="1" <?= (($_POST['flagProcedure'] ?? '') == '1') ? 'selected' : '' ?>>1 - Ya</option>
                  </select>
                </div>

                <div class="form-group">
                  <label>Kode Penunjang</label>
                  <input type="text" name="kdPenunjang" class="form-control"
                         value="<?= htmlspecialchars($_POST['kdPenunjang'] ?? '') ?>"
                         placeholder="(opsional)">
                </div>

                <div class="form-group">
                  <label>Assesment Pelayanan</label>
                  <input type="text" name="assesmentPel" class="form-control"
                         value="<?= htmlspecialchars($_POST['assesmentPel'] ?? '') ?>"
                         placeholder="(opsional)">
                </div>

              </div>
            </div>

            <!-- BOX: Jaminan Laka Lantas -->
            <div class="box box-danger">
              <div class="box-header with-border">
                <h3 class="box-title"><i class="fa fa-car"></i> Jaminan & Laka Lantas</h3>
              </div>
              <div class="box-body">

                <div class="form-group">
                  <label>Laka Lantas</label>
                  <select name="lakaLantas" id="lakaLantas" class="form-control" onchange="toggleLaka()">
                    <option value="0" <?= (($_POST['lakaLantas'] ?? '0') == '0') ? 'selected' : '' ?>>0 - Bukan Laka Lantas</option>
                    <option value="1" <?= (($_POST['lakaLantas'] ?? '') == '1') ? 'selected' : '' ?>>1 - Laka Lantas</option>
                  </select>
                </div>

                <div id="laka-fields" style="display:none;">
                  <div class="form-group">
                    <label>No. LP (Laporan Polisi)</label>
                    <input type="text" name="noLP" class="form-control"
                           value="<?= htmlspecialchars($_POST['noLP'] ?? '') ?>"
                           placeholder="No. LP">
                  </div>
                  <div class="form-group">
                    <label>Tanggal Kejadian</label>
                    <input type="date" name="tglKejadian" class="form-control"
                           value="<?= htmlspecialchars($_POST['tglKejadian'] ?? '') ?>">
                  </div>
                  <div class="form-group">
                    <label>Keterangan</label>
                    <input type="text" name="keterangan" class="form-control"
                           value="<?= htmlspecialchars($_POST['keterangan'] ?? '') ?>"
                           placeholder="Keterangan kejadian">
                  </div>

                  <div class="form-group">
                    <label>Suplesi</label>
                    <select name="suplesi" id="suplesi" class="form-control" onchange="toggleSuplesi()">
                      <option value="0" <?= (($_POST['suplesi'] ?? '0') == '0') ? 'selected' : '' ?>>0 - Tidak</option>
                      <option value="1" <?= (($_POST['suplesi'] ?? '') == '1') ? 'selected' : '' ?>>1 - Ya</option>
                    </select>
                  </div>

                  <div id="suplesi-fields" style="display:none;">
                    <div class="form-group">
                      <label>No. SEP Suplesi</label>
                      <input type="text" name="noSepSuplesi" class="form-control"
                             value="<?= htmlspecialchars($_POST['noSepSuplesi'] ?? '') ?>"
                             placeholder="No. SEP yang disuplesi">
                    </div>
                    <div class="form-group">
                      <label>Kd. Propinsi Laka</label>
                      <input type="text" name="kdPropinsi" class="form-control"
                             value="<?= htmlspecialchars($_POST['kdPropinsi'] ?? '') ?>"
                             placeholder="Kode Propinsi">
                    </div>
                    <div class="form-group">
                      <label>Kd. Kabupaten Laka</label>
                      <input type="text" name="kdKabupaten" class="form-control"
                             value="<?= htmlspecialchars($_POST['kdKabupaten'] ?? '') ?>"
                             placeholder="Kode Kabupaten">
                    </div>
                    <div class="form-group">
                      <label>Kd. Kecamatan Laka</label>
                      <input type="text" name="kdKecamatan" class="form-control"
                             value="<?= htmlspecialchars($_POST['kdKecamatan'] ?? '') ?>"
                             placeholder="Kode Kecamatan">
                    </div>
                  </div>
                </div>

              </div>
            </div>

            <!-- BOX: Tombol -->
            <div class="box box-solid">
              <div class="box-body">
                <button type="submit" name="submit_sep" class="btn btn-kirim btn-block"
                  <?= !$setting ? 'disabled title="Setting VClaim belum diset"' : '' ?>>
                  <i class="fa fa-send"></i> Kirim ke VClaim BPJS
                </button>
                <button type="reset" class="btn btn-reset btn-block" style="margin-top:8px;">
                  <i class="fa fa-refresh"></i> Reset Form
                </button>
                <a href="sep_poli.php" class="btn btn-default btn-block" style="margin-top:8px;">
                  <i class="fa fa-list"></i> Lihat Daftar SEP
                </a>
              </div>
            </div>

          </div><!-- /col-md-4 -->

        </div><!-- /row -->

      </form>

    </section>
  </div>

<?php
echo $extra_js;
include 'includes/footer.php';
?>
