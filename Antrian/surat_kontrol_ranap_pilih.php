<?php
/**
 * PILIH JADWAL & BUAT SURAT KONTROL RANAP - MediFix
 * jnsKontrol = 1 (Rawat Inap)
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
$jnsKontrol = $_GET['jnsKontrol'] ?? '1'; // Default Ranap

if (empty($noSEP)) {
    header("Location: surat_kontrol_ranap.php");
    exit;
}

// === AMBIL SETTING VCLAIM ===
$stmt       = $pdo->query("SELECT * FROM setting_vclaim LIMIT 1");
$setting    = $stmt->fetch(PDO::FETCH_ASSOC);
$cons_id    = trim($setting['cons_id']);
$secret_key = trim($setting['secret_key']);
$user_key   = trim($setting['user_key']);
$base_url   = rtrim(trim($setting['base_url']), '/');

// === AJAX HANDLER ===
if (isset($_GET['action'])) {
    header('Content-Type: application/json');
    $tgl = $_GET['tgl'] ?? '';
    
    if ($_GET['action'] === 'getPoli') {
        echo json_encode(callVClaim("/RencanaKontrol/ListPoli/tglRencanaKontrol/{$tgl}/nomor/{$noSEP}/jnsKontrol/{$jnsKontrol}"));
        exit;
    }
    
    if ($_GET['action'] === 'getDokter') {
        $kdPoli = $_GET['kdPoli'] ?? '';
        echo json_encode(callVClaim("/RencanaKontrol/ListDokter/tglRencanaKontrol/{$tgl}/kdPoli/{$kdPoli}/jnsKontrol/{$jnsKontrol}"));
        exit;
    }
}

// === PROSES SUBMIT SURAT KONTROL ===
$message = '';
$resSucc = null;

if ($_SERVER['REQUEST_METHOD'] === 'POST' && isset($_POST['submitRanap'])) {
    $requestData = [
        "noSEP" => $_POST['noSEP'],
        "kodeDokter" => $_POST['kodeDokter'],
        "poliKontrol" => $_POST['poliKontrol'],
        "tglRencanaKontrol" => $_POST['tglRencanaKontrol'],
        "user" => "MediFix Anjungan RI"
    ];
    
    $res = callVClaim("/RencanaKontrol/insert", "POST", $requestData);
    
    if (isset($res['metaData']['code']) && $res['metaData']['code'] == 200) {
        $resSucc = $res['response'];
        $message = "✔ Surat Kontrol Rawat Inap Berhasil Dibuat!";
    } else {
        $message = "✖ Gagal: " . ($res['metaData']['message'] ?? "Error BPJS tidak diketahui");
    }
}

?>
<!DOCTYPE html>
<html lang="id">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Jadwal Kontrol Ranap - MediFix</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons/font/bootstrap-icons.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Poppins:wght@400;500;600;700&display=swap" rel="stylesheet">
    <style>
        body { font-family: 'Poppins', sans-serif; background: linear-gradient(135deg, #6366f1 0%, #a855f7 100%); min-height:100vh; padding:40px 20px; }
        .main-container { max-width: 800px; margin: 0 auto; background: #fff; border-radius: 20px; box-shadow: 0 15px 50px rgba(0,0,0,0.3); overflow: hidden; }
        .header-section { background: linear-gradient(135deg, #14b8a6, #0d9488); color: white; padding: 25px; text-align: center; }
        .content-section { padding: 30px; }
        .info-sep { background: #f0fdfa; border-radius: 12px; padding: 20px; margin-bottom: 25px; border-left: 5px solid #14b8a6; }
    </style>
</head>
<body>

<div class="main-container">
    <div class="header-section">
        <h3><i class="bi bi-calendar-event-fill"></i> Atur Kontrol Pasca Inap</h3>
    </div>

    <div class="content-section">
        
        <?php if ($message): ?>
            <div class="alert alert-<?= $resSucc ? 'success' : 'danger' ?> fade show" role="alert">
                <i class="bi bi-<?= $resSucc ? 'check-circle-fill' : 'exclamation-triangle' ?>"></i> <?= $message ?>
            </div>
            
            <?php if ($resSucc): ?>
                <div class="card border-success text-center mb-4">
                    <div class="card-body">
                        <h4 class="text-success mb-3">NOMOR SURAT KONTROL:</h4>
                        <h1 class="display-3 fw-bold"><?= $resSucc['noSuratKontrol'] ?></h1>
                        <hr>
                        <p>Poli: <strong><?= $resSucc['namaPoliKontrol'] ?></strong> | Tgl: <strong><?= $resSucc['tglRencanaKontrol'] ?></strong></p>
                        <button onclick="window.print()" class="btn btn-primary btn-lg px-5 mt-3"><i class="bi bi-printer"></i> Cetak Surat</button>
                    </div>
                </div>
            <?php endif; ?>
        <?php endif; ?>

        <?php if (!$resSucc): ?>
        <div class="info-sep">
            <div class="row align-items-center">
                <div class="col-8">
                    <small class="text-muted d-block">SEP Rawat Inap Sebagai Dasar:</small>
                    <span class="fs-4 fw-bold text-teal" style="color:#0d9488;"><?= htmlspecialchars($noSEP) ?></span>
                </div>
                <div class="col-4 text-end">
                    <span class="badge bg-teal" style="background:#14b8a6;">JENIS: RAWAT INAP</span>
                </div>
            </div>
        </div>

        <form method="POST">
            <input type="hidden" name="noSEP" value="<?= htmlspecialchars($noSEP) ?>">
            
            <div class="mb-4">
                <label class="form-label fw-bold">1. Pilih Tanggal Rencana Kontrol</label>
                <input type="date" name="tglRencanaKontrol" id="tglKontrol" class="form-control form-control-lg" 
                       min="<?= date('Y-m-d', strtotime('+1 day')) ?>" required>
            </div>

            <div class="mb-4">
                <label class="form-label fw-bold">2. Pilih Poli Tujuan</label>
                <select name="poliKontrol" id="poliKontrol" class="form-select form-select-lg" required disabled>
                    <option value="">-- Pilih Poli --</option>
                </select>
                <div id="loaderPoli" class="spinner-border spinner-border-sm text-teal d-none mt-1"></div>
            </div>

            <div class="mb-4">
                <label class="form-label fw-bold">3. Pilih Dokter Pengganti / Spesialis</label>
                <select name="kodeDokter" id="kodeDokter" class="form-select form-select-lg" required disabled>
                    <option value="">-- Pilih Dokter --</option>
                </select>
                <div id="loaderDokter" class="spinner-border spinner-border-sm text-teal d-none mt-1"></div>
            </div>

            <button type="submit" name="submitRanap" id="btnSubmit" class="btn btn-lg w-100 text-white" 
                    style="background: linear-gradient(135deg, #14b8a6, #0d9488); font-weight:700; padding:18px;" disabled>
                BERIKAN SURAT KONTROL RAJAL
            </button>
        </form>
        <?php endif; ?>

        <div class="mt-4 text-center">
            <a href="surat_kontrol_ranap.php" class="btn btn-link link-secondary text-decoration-none">
                <i class="bi bi-arrow-left"></i> Kembali ke Riwayat
            </a>
        </div>
    </div>
</div>

<script>
const noSep = "<?= $noSEP ?>";
const jnsKontrol = "<?= $jnsKontrol ?>";
const tglEl = document.getElementById('tglKontrol');
const poliEl = document.getElementById('poliKontrol');
const dokterEl = document.getElementById('kodeDokter');
const btnEl = document.getElementById('btnSubmit');

tglEl.addEventListener('change', async function() {
    const tgl = this.value;
    if(!tgl) return;
    poliEl.disabled = true;
    poliEl.innerHTML = '<option>Proses...</option>';
    document.getElementById('loaderPoli').classList.remove('d-none');
    try {
        const resp = await fetch(`?action=getPoli&noSep=${noSep}&tgl=${tgl}&jnsKontrol=${jnsKontrol}`);
        const data = await resp.json();
        if(data.metaData.code == 200) {
            poliEl.innerHTML = '<option value="">-- Pilih Poli --</option>';
            data.response.list.forEach(p => poliEl.innerHTML += `<option value="${p.kodePoli}">${p.namaPoli} (Kuota: ${p.kapasitas})</option>`);
            poliEl.disabled = false;
        } else {
            poliEl.innerHTML = `<option value="">${data.metaData.message}</option>`;
        }
    } catch(e) { } finally { document.getElementById('loaderPoli').classList.add('d-none'); }
});

poliEl.addEventListener('change', async function() {
    const kdPoli = this.value;
    const tgl = tglEl.value;
    if(!kdPoli) return;
    dokterEl.disabled = true;
    dokterEl.innerHTML = '<option>Proses...</option>';
    document.getElementById('loaderDokter').classList.remove('d-none');
    try {
        const resp = await fetch(`?action=getDokter&kdPoli=${kdPoli}&tgl=${tgl}&jnsKontrol=${jnsKontrol}`);
        const data = await resp.json();
        if(data.metaData.code == 200) {
            dokterEl.innerHTML = '<option value="">-- Pilih Dokter --</option>';
            data.response.list.forEach(d => dokterEl.innerHTML += `<option value="${d.kodeDokter}">${d.namaDokter} (Jadwal: ${d.jadwal})</option>`);
            dokterEl.disabled = false;
        }
    } catch(e) { } finally { document.getElementById('loaderDokter').classList.add('d-none'); }
});

dokterEl.addEventListener('change', function() { btnEl.disabled = this.value === ""; });
</script>
</body>
</html>
