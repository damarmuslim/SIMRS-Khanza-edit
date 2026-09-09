<?php
/**
 * PILIH JADWAL & BUAT SURAT KONTROL - MediFix
 */

error_reporting(E_ALL);
ini_set('display_errors', 1);

session_start();
include 'koneksi.php';
include 'koneksi2.php';
include 'vclaim_helper.php'; // enkripsi, compress, callVClaim

date_default_timezone_set('Asia/Jakarta');

$noSEP      = $_GET['noSEP']      ?? '';
$tglSEP     = $_GET['tglSEP']     ?? '';
$poliOrigin = $_GET['poliOrigin'] ?? '';

if (empty($noSEP)) {
    header("Location: surat_kontrol_rajal.php");
    exit;
}

// === AMBIL SETTING VCLAIM ===
$stmt       = $pdo->query("SELECT * FROM setting_vclaim LIMIT 1");
$setting    = $stmt->fetch(PDO::FETCH_ASSOC);
$cons_id    = trim($setting['cons_id']);
$secret_key = trim($setting['secret_key']);
$user_key   = trim($setting['user_key']);
$base_url   = rtrim(trim($setting['base_url']), '/');

// === AJAX HANDLER (Get Ref Poli & Dokter) ===
if (isset($_GET['action'])) {
    header('Content-Type: application/json');
    
    if ($_GET['action'] === 'getPoli') {
        $jnsKontrol = $_GET['jnsKontrol'] ?: '2';
        $noSep = $_GET['noSep'];
        $tgl = $_GET['tgl'];
        $res = callVClaim("/RencanaKontrol/ListPoli/tglRencanaKontrol/{$tgl}/nomor/{$noSep}/jnsKontrol/{$jnsKontrol}");
        echo json_encode($res);
        exit;
    }
    
    if ($_GET['action'] === 'getDokter') {
        $jnsKontrol = $_GET['jnsKontrol'] ?: '2';
        $kdPoli = $_GET['kdPoli'];
        $tgl = $_GET['tgl'];
        $res = callVClaim("/RencanaKontrol/ListDokter/tglRencanaKontrol/{$tgl}/kdPoli/{$kdPoli}/jnsKontrol/{$jnsKontrol}");
        echo json_encode($res);
        exit;
    }
}

// === PROSES SIMPAN SURAT KONTROL ===
$message = '';
$resSucc = null;

if ($_SERVER['REQUEST_METHOD'] === 'POST' && isset($_POST['buatSurat'])) {
    $requestData = [
        "noSEP" => $_POST['noSEP'],
        "kodeDokter" => $_POST['kodeDokter'],
        "poliKontrol" => $_POST['poliKontrol'],
        "tglRencanaKontrol" => $_POST['tglRencanaKontrol'],
        "user" => "MediFix Anjungan"
    ];
    
    $res = callVClaim("/RencanaKontrol/insert", "POST", $requestData);
    
    if (isset($res['metaData']['code']) && $res['metaData']['code'] == 200) {
        $resSucc = $res['response'];
        $message = "✔ Surat Kontrol Berhasil Dibuat!";
    } else {
        $message = "✖ Gagal: " . ($res['metaData']['message'] ?? "Error tidak diketahui");
    }
}

?>
<!DOCTYPE html>
<html lang="id">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width,initial-scale=1">
    <title>Jadwal Surat Kontrol - MediFix</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons/font/bootstrap-icons.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Poppins:wght@400;500;600;700&display=swap" rel="stylesheet">
    <style>
        body { font-family: 'Poppins', sans-serif; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); min-height:100vh; padding:40px 20px; }
        .main-container { max-width: 800px; margin: 0 auto; background: #fff; border-radius: 20px; box-shadow: 0 15px 50px rgba(0,0,0,0.3); overflow: hidden; }
        .header-section { background: linear-gradient(135deg, #f59e0b, #d97706); color: white; padding: 25px; text-align: center; }
        .content-section { padding: 30px; }
        .info-sep { background: #f1f5f9; border-radius: 12px; padding: 20px; margin-bottom: 25px; border-left: 5px solid #f59e0b; }
        .btn-submit { background: linear-gradient(135deg, #10b981, #059669); border:none; padding:15px; font-weight:700; font-size:1.1rem; }
    </style>
</head>
<body>

<div class="main-container">
    <div class="header-section">
        <h3><i class="bi bi-calendar-check-fill"></i> Atur Jadwal Kontrol</h3>
    </div>

    <div class="content-section">
        
        <?php if ($message): ?>
            <div class="alert alert-<?= $resSucc ? 'success' : 'danger' ?> alert-dismissible fade show" role="alert">
                <i class="bi bi-<?= $resSucc ? 'check-circle-fill' : 'exclamation-triangle-fill' ?>"></i> <?= $message ?>
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>
            
            <?php if ($resSucc): ?>
                <div class="card border-success mt-3 mb-4">
                    <div class="card-body text-center">
                        <h1 class="display-5 fw-bold text-success"><?= $resSucc['noSuratKontrol'] ?></h1>
                        <p class="mb-1 text-muted">Nomor Surat Kontrol Anda</p>
                        <hr>
                        <div class="row text-start mt-3">
                            <div class="col-6"><small>Poli Kontrol:</small><br><strong><?= $resSucc['namaPoliKontrol'] ?></strong></div>
                            <div class="col-6"><small>Tanggal Kontrol:</small><br><strong><?= $resSucc['tglRencanaKontrol'] ?></strong></div>
                        </div>
                        <div class="mt-4">
                            <button onclick="window.print()" class="btn btn-primary btn-lg px-5">
                                <i class="bi bi-printer"></i> Cetak Surat Kontrol
                            </button>
                        </div>
                    </div>
                </div>
            <?php endif; ?>
        <?php endif; ?>

        <?php if (!$resSucc): ?>
        <div class="info-sep">
            <div class="row">
                <div class="col-md-7">
                    <label class="text-muted small">Nomor SEP Rujukan:</label>
                    <div class="fw-bold fs-5"><?= htmlspecialchars($noSEP) ?></div>
                </div>
                <div class="col-md-5 text-md-end">
                    <label class="text-muted small">Asal Poli:</label>
                    <div class="fw-bold"><?= htmlspecialchars($poliOrigin) ?></div>
                </div>
            </div>
        </div>

        <form method="POST" id="formKontrol">
            <input type="hidden" name="noSEP" value="<?= htmlspecialchars($noSEP) ?>">
            
            <div class="mb-3">
                <label class="form-label fw-bold"><i class="bi bi-calendar-plus"></i> Tanggal Rencana Kontrol (H+1 atau lebih)</label>
                <input type="date" name="tglRencanaKontrol" id="tglKontrol" class="form-control form-control-lg" 
                       min="<?= date('Y-m-d', strtotime('+1 day')) ?>" required>
            </div>

            <div class="mb-3">
                <label class="form-label fw-bold"><i class="bi bi-hospital"></i> Poli Tujuan Kontrol</label>
                <select name="poliKontrol" id="poliKontrol" class="form-select form-select-lg" required disabled>
                    <option value="">-- Pilih Tanggal Terlebih Dahulu --</option>
                </select>
                <div id="loaderPoli" class="spinner-border spinner-border-sm text-primary d-none mt-1"></div>
            </div>

            <div class="mb-4">
                <label class="form-label fw-bold"><i class="bi bi-person-badge-fill"></i> Dokter Spesialis</label>
                <select name="kodeDokter" id="kodeDokter" class="form-select form-select-lg" required disabled>
                    <option value="">-- Pilih Poli Terlebih Dahulu --</option>
                </select>
                <div id="loaderDokter" class="spinner-border spinner-border-sm text-primary d-none mt-1"></div>
            </div>

            <button type="submit" name="buatSurat" id="btnSubmit" class="btn btn-success btn-lg w-100 btn-submit" disabled>
                <i class="bi bi-send-check"></i> Buat Surat Kontrol
            </button>
        </form>
        <?php endif; ?>

        <div class="mt-4 text-center">
            <a href="surat_kontrol_rajal.php" class="btn btn-link text-decoration-none">
                <i class="bi bi-arrow-left"></i> Kembali ke List SEP
            </a>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script>
const noSep = "<?= $noSEP ?>";
const tglEl = document.getElementById('tglKontrol');
const poliEl = document.getElementById('poliKontrol');
const dokterEl = document.getElementById('kodeDokter');
const btnEl = document.getElementById('btnSubmit');

tglEl.addEventListener('change', async function() {
    const tgl = this.value;
    if(!tgl) return;

    poliEl.disabled = true;
    poliEl.innerHTML = '<option>Loading poli...</option>';
    document.getElementById('loaderPoli').classList.remove('d-none');

    try {
        const resp = await fetch(`?action=getPoli&noSep=${noSep}&tgl=${tgl}&jnsKontrol=2`);
        const data = await resp.json();

        if(data.metaData.code == 200) {
            poliEl.innerHTML = '<option value="">-- Pilih Poli --</option>';
            data.response.list.forEach(p => {
                poliEl.innerHTML += `<option value="${p.kodePoli}">${p.namaPoli} (Kuota: ${p.kapasitas})</option>`;
            });
            poliEl.disabled = false;
        } else {
            poliEl.innerHTML = `<option value="">Error: ${data.metaData.message}</option>`;
        }
    } catch(e) {
        poliEl.innerHTML = '<option value="">Gagal koneksi server</option>';
    } finally {
        document.getElementById('loaderPoli').classList.add('d-none');
    }
});

poliEl.addEventListener('change', async function() {
    const kdPoli = this.value;
    const tgl = tglEl.value;
    if(!kdPoli) return;

    dokterEl.disabled = true;
    dokterEl.innerHTML = '<option>Loading dokter...</option>';
    document.getElementById('loaderDokter').classList.remove('d-none');

    try {
        const resp = await fetch(`?action=getDokter&kdPoli=${kdPoli}&tgl=${tgl}&jnsKontrol=2`);
        const data = await resp.json();

        if(data.metaData.code == 200) {
            dokterEl.innerHTML = '<option value="">-- Pilih Dokter --</option>';
            data.response.list.forEach(d => {
                dokterEl.innerHTML += `<option value="${d.kodeDokter}">${d.namaDokter} (Jadwal: ${d.jadwal})</option>`;
            });
            dokterEl.disabled = false;
        } else {
            dokterEl.innerHTML = `<option value="">Error: ${data.metaData.message}</option>`;
        }
    } catch(e) {
        dokterEl.innerHTML = '<option value="">Gagal koneksi server</option>';
    } finally {
        document.getElementById('loaderDokter').classList.add('d-none');
    }
});

dokterEl.addEventListener('change', function() {
    btnEl.disabled = this.value === "";
});
</script>
</body>
</html>
