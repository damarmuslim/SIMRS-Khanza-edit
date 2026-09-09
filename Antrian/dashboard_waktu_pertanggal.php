<?php
date_default_timezone_set('Asia/Jakarta');
include 'koneksi.php';
use LZCompressor\LZString;
require_once __DIR__ . '/vendor/autoload.php';

/* ============================================================
   1. SETTING ANTROL
   ============================================================ */
try {
    $stmt = $pdo->query("SELECT * FROM setting_antrol LIMIT 1");
    $config = $stmt->fetch(PDO::FETCH_ASSOC);
    if (!$config) die("Setting Antrol belum diisi! Silakan ke menu Setting > Bridging Antrol.");
} catch (Exception $e) {
    die("Error DB: " . $e->getMessage());
}

$cons_id    = $config['cons_id'];
$secret_key = $config['secret_key'];
$user_key   = $config['user_key'];
$base_url   = rtrim($config['base_url'], '/');

/* ============================================================
   2. PARAMETER FILTER
   ============================================================ */
$tanggal = $_GET['tanggal'] ?? date('Y-m-d');
$waktu   = $_GET['waktu']   ?? 'rs';

/* ============================================================
   3. TIMESTAMP & SIGNATURE
   ============================================================ */
$tStamp    = strval(time());
$signature = base64_encode(hash_hmac('sha256', $cons_id . "&" . $tStamp, $secret_key, true));

/* ============================================================
   4. HEADER
   ============================================================ */
$headers = [
    "x-cons-id: $cons_id",
    "x-timestamp: $tStamp",
    "x-signature: $signature",
    "user_key: $user_key",
    "Accept: application/json"
];

/* ============================================================
   5. URL ENDPOINT (PERTANGGAL)
   ============================================================ */
$url = $base_url . "/dashboard/waktutunggu/tanggal/".urlencode($tanggal)."/waktu/".urlencode($waktu);

/* ============================================================
   6. CURL REQUEST
   ============================================================ */
$ch = curl_init($url);
curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);
curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);
curl_setopt($ch, CURLOPT_TIMEOUT, 15);
$response = curl_exec($ch);
curl_close($ch);

$data = json_decode($response, true);

/* ============================================================
   7. DEKRIPSI + DEKOMPRESI
   ============================================================ */
function stringDecrypt($key, $string){
    $key_hash = hex2bin(hash('sha256', $key));
    $iv = substr($key_hash, 0, 16);
    return openssl_decrypt(base64_decode($string), 'AES-256-CBC', $key_hash, OPENSSL_RAW_DATA, $iv);
}

function toMinutes($value){
    if(!is_numeric($value)) return '-';
    if($value == 0) return '0';
    return round($value/60, 2); // konversi detik ke menit
}

$finalData = [];
if ($data && isset($data['metaData']) && $data['metaData']['code'] == 200) {
    try {
        if(isset($data["response"]["list"])) {
            $finalData = $data["response"]["list"];
        } else {
            $keyDecrypt = $cons_id . $secret_key . $tStamp;
            $decrypted = stringDecrypt($keyDecrypt, $data["response"]);
            $decompressed = LZCompressor\LZString::decompressFromEncodedURIComponent($decrypted);
            $finalData = json_decode($decompressed, true);
        }
    } catch(Exception $e){
        $error_msg = "Error decrypt/decompress: ".$e->getMessage();
    }
} else {
    $error_msg = $data['metaData']['message'] ?? "Gagal mengambil data dari BPJS.";
}

// Integrasi dengan layout MediFix
$page_title = 'Dashboard Waktu Tunggu Pertanggal';
include 'includes/header.php';
include 'includes/sidebar.php';
?>

<style>
/* Dashboard Styling */
.dashboard-card { border-top: 3px solid #605ca8; }
.btn-search { background-color: #605ca8; color: white; border: none; }
.btn-search:hover { background-color: #555299; color: white; }
.table-dashboard thead th { 
    background-color: #f4f4f4; 
    text-align: center; 
    vertical-align: middle; 
    font-size: 11px;
    border: 1px solid #ddd;
}
.table-dashboard tbody td { text-align: center; font-size: 11px; }
.badge-task { font-size: 10px; padding: 3px 6px; }
</style>

<div class="content-wrapper">
    <section class="content-header">
        <h1>Dashboard Waktu Tunggu <small>Per Tanggal</small></h1>
        <ol class="breadcrumb">
            <li><a href="dashboard.php"><i class="fa fa-dashboard"></i> Home</a></li>
            <li class="active">Waktu Tunggu Pertanggal</li>
        </ol>
    </section>

    <section class="content">
        <div class="box box-primary dashboard-card">
            <div class="box-header with-border">
                <h3 class="box-title"><i class="fa fa-clock-o"></i> Filter Statistik</h3>
            </div>
            <div class="box-body">
                <form method="get" class="form-inline">
                    <div class="form-group">
                        <label>Pilih Tanggal: &nbsp;</label>
                        <input type="date" name="tanggal" class="form-control" value="<?= htmlspecialchars($tanggal) ?>" required>
                    </div>
                    <div class="form-group" style="margin-left: 15px;">
                        <label>Jenis Waktu: &nbsp;</label>
                        <select name="waktu" class="form-control">
                            <option value="rs" <?= ($waktu=='rs')?'selected':'' ?>>RS</option>
                            <option value="server" <?= ($waktu=='server')?'selected':'' ?>>Server</option>
                        </select>
                    </div>
                    <button type="submit" class="btn btn-search" style="margin-left: 10px;">
                        <i class="fa fa-search"></i> Tampilkan Data
                    </button>
                </form>
            </div>
        </div>

        <?php if(isset($error_msg)): ?>
            <div class="alert alert-warning">
                <i class="icon fa fa-warning"></i> <?= $error_msg ?>
            </div>
        <?php endif; ?>

        <div class="box">
            <div class="box-header">
                <h3 class="box-title">Data Waktu Tunggu Tanggal <?= date('d-m-Y', strtotime($tanggal)) ?></h3>
            </div>
            <div class="box-body">
                <div class="table-responsive">
                    <table class="table table-bordered table-hover table-dashboard">
                        <thead>
                            <tr>
                                <th rowspan="2" width="40">No</th>
                                <th rowspan="2">Poli</th>
                                <th rowspan="2" width="70">Antrean</th>
                                <th colspan="2">Task 1<br><small>Admisi</small></th>
                                <th colspan="2">Task 2<br><small>Pendaftaran</small></th>
                                <th colspan="2">Task 3<br><small>Poli (Tunggu)</small></th>
                                <th colspan="2">Task 4<br><small>Poli (Layanan)</small></th>
                                <th colspan="2">Task 5<br><small>Farmasi (Validasi)</small></th>
                                <th colspan="2">Task 6<br><small>Farmasi (Siap)</small></th>
                                <th colspan="2">Task 7<br><small>Selesai</small></th>
                            </tr>
                            <tr>
                                <th>Total</th><th>Avg</th>
                                <th>Total</th><th>Avg</th>
                                <th>Total</th><th>Avg</th>
                                <th>Total</th><th>Avg</th>
                                <th>Total</th><th>Avg</th>
                                <th>Total</th><th>Avg</th>
                                <th>Total</th><th>Avg</th>
                            </tr>
                        </thead>
                        <tbody>
                        <?php if(!empty($finalData)): ?>
                            <?php foreach($finalData as $i=>$row): ?>
                            <tr>
                                <td><?= $i+1 ?></td>
                                <td style="text-align: left;"><strong><?= $row['namapoli'] ?? '-' ?></strong><br><small><?= $row['kodepoli'] ?? '' ?></small></td>
                                <td><span class="badge bg-blue"><?= $row['jumlah_antrean'] ?? '0' ?></span></td>
                                <td><?= toMinutes($row['waktu_task1'] ?? 0) ?></td>
                                <td><strong><?= toMinutes($row['avg_waktu_task1'] ?? 0) ?></strong></td>
                                <td><?= toMinutes($row['waktu_task2'] ?? 0) ?></td>
                                <td><strong><?= toMinutes($row['avg_waktu_task2'] ?? 0) ?></strong></td>
                                <td><?= toMinutes($row['waktu_task3'] ?? 0) ?></td>
                                <td><strong><?= toMinutes($row['avg_waktu_task3'] ?? 0) ?></strong></td>
                                <td><?= toMinutes($row['waktu_task4'] ?? 0) ?></td>
                                <td><strong><?= toMinutes($row['avg_waktu_task4'] ?? 0) ?></strong></td>
                                <td><?= toMinutes($row['waktu_task5'] ?? 0) ?></td>
                                <td><strong><?= toMinutes($row['avg_waktu_task5'] ?? 0) ?></strong></td>
                                <td><?= toMinutes($row['waktu_task6'] ?? 0) ?></td>
                                <td><strong><?= toMinutes($row['avg_waktu_task6'] ?? 0) ?></strong></td>
                                <td><?= toMinutes($row['waktu_task7'] ?? 0) ?></td>
                                <td><strong><?= toMinutes($row['avg_waktu_task7'] ?? 0) ?></strong></td>
                            </tr>
                            <?php endforeach; ?>
                        <?php else: ?>
                            <tr><td colspan="17" class="text-center"><em>Data tidak ditemukan atau filter belum diterapkan.</em></td></tr>
                        <?php endif; ?>
                        </tbody>
                    </table>
                </div>
            </div>
            <div class="box-footer">
                <small class="text-muted">* Satuan waktu dalam menit. Avg = Rata-rata.</small>
            </div>
        </div>
    </section>
</div>

<?php include 'includes/footer.php'; ?>
