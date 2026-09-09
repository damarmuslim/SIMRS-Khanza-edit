<?php
include 'koneksi2.php';
date_default_timezone_set('Asia/Jakarta');

$today  = date('Y-m-d');
$poli   = $_GET['poli']   ?? '';
$dokter = $_GET['dokter'] ?? '';

// === FUNGSI MAPPING HURUF ANTRIAN ===
function generateKodeAntrian($kd_poli, $nm_poli) {
    $mapping = [
        'U0001' => 'K', // Kandungan
        'U0002' => 'A', // Anak
        'U0003' => 'P', // Penyakit Dalam / Paru
        'U0004' => 'B', // Bedah
        'U0005' => 'M', // Mata
        'U0006' => 'K', // Kulit & Kelamin
        'U0007' => 'S', // Syaraf
        'U0008' => 'R', // Radiologi
        'U0009' => 'U', // Umum
        'U0010' => 'G', // Gigi & Mulut
        'U0011' => 'T', // THT
        'U0012' => 'J', // Jantung
        'U0052' => 'G', // Ginjal
        'U0053' => 'F', // Fisioterapi
        'IGDK'  => 'I', // IGD
        'MCU'   => 'C', // MCU
        'OBG'   => 'O', // Obstetri
        'INT'   => 'D', // Dalam (Penyakit Dalam)
    ];

    $kd = trim($kd_poli);
    if (isset($mapping[$kd])) return $mapping[$kd];
    $nama = trim(str_ireplace(['Poliklinik ', 'Poli '], '', $nm_poli));
    if (!empty($nama)) return strtoupper(substr($nama, 0, 1));
    return strtoupper(substr($kd, 0, 1));
}

// === POLI YANG DISEMBUNYIKAN ===
$excluded_poli = ['IGDK','PL013','PL014','PL015','PL016','PL017','U0022','U0030'];
$excluded_list = "'" . implode("','", $excluded_poli) . "'";

try {
    $sqlDokter = "
        SELECT DISTINCT
            r.kd_dokter, r.kd_poli,
            d.nm_dokter, p.nm_poli,
            NULL AS jam_mulai, NULL AS jam_selesai
        FROM reg_periksa r
        LEFT JOIN dokter     d ON r.kd_dokter = d.kd_dokter
        LEFT JOIN poliklinik p ON r.kd_poli   = p.kd_poli
        WHERE r.tgl_registrasi = ?
          AND r.kd_poli NOT IN ($excluded_list)
    ";
    $paramsDokter = [$today];
    if (!empty($poli))   { $sqlDokter .= " AND r.kd_poli = ?";   $paramsDokter[] = $poli; }
    if (!empty($dokter)) { $sqlDokter .= " AND r.kd_dokter = ?"; $paramsDokter[] = $dokter; }
    $sqlDokter .= " ORDER BY p.nm_poli, d.nm_dokter";

    $stmtDokter = $pdo_simrs->prepare($sqlDokter);
    $stmtDokter->execute($paramsDokter);
    $daftar_dokter = $stmtDokter->fetchAll(PDO::FETCH_ASSOC);

    // ── Ambil SEMUA pasien hari ini sekaligus (1 query, bukan N query) ──
    $sqlAllPasien = "
        SELECT r.no_reg, r.no_rawat, r.kd_poli, r.kd_dokter, r.stts, ps.nm_pasien
        FROM reg_periksa r
        LEFT JOIN pasien ps ON r.no_rkm_medis = ps.no_rkm_medis
        WHERE r.tgl_registrasi = ?
    ";
    $pasienParams = [$today];
    if (!empty($poli))   { $sqlAllPasien .= " AND r.kd_poli = ?";   $pasienParams[] = $poli; }
    if (!empty($dokter)) { $sqlAllPasien .= " AND r.kd_dokter = ?"; $pasienParams[] = $dokter; }
    $sqlAllPasien .= " ORDER BY r.kd_poli, r.kd_dokter, r.no_reg+0 ASC";
    $stmtAllPasien = $pdo_simrs->prepare($sqlAllPasien);
    $stmtAllPasien->execute($pasienParams);
    $allPasienRaw  = $stmtAllPasien->fetchAll(PDO::FETCH_ASSOC);

    // Kelompokkan per kd_poli+kd_dokter
    $pasienMap = [];
    foreach ($allPasienRaw as $row) {
        $pasienMap[$row['kd_poli'] . '|' . $row['kd_dokter']][] = $row;
    }

    // ── Ambil SEMUA panggilan terakhir sekaligus (1 query) ──
    $sqlAllCall = "
        SELECT kd_poli, kd_dokter, no_antrian, nm_pasien, no_rawat, jml_panggil, updated_at
        FROM simpan_antrian_poli_wira
        WHERE tgl_panggil = ?
        ORDER BY updated_at DESC
    ";
    $stmtAllCall = $pdo_simrs->prepare($sqlAllCall);
    $stmtAllCall->execute([$today]);
    $allCallRaw  = $stmtAllCall->fetchAll(PDO::FETCH_ASSOC);

    // Ambil entry terbaru per poli+dokter
    $callMap = [];
    foreach ($allCallRaw as $row) {
        $key = $row['kd_poli'] . '|' . $row['kd_dokter'];
        if (!isset($callMap[$key])) {
            $callMap[$key] = $row;
        }
    }

    // ── Bangun dokter_data dari data yang sudah di-cache ──
    $dokter_data = [];
    foreach ($daftar_dokter as $dok) {
        $kd_d        = $dok['kd_dokter'];
        $kd_p        = $dok['kd_poli'];
        $key         = $kd_p . '|' . $kd_d;
        $pasien_list = $pasienMap[$key] ?? [];
        $call_raw    = $callMap[$key]   ?? null;

        $current_call = null;
        if ($call_raw) {
            $current_call = $call_raw;
            $current_call['nm_pasien_suara'] = normalizeNamaPasien($call_raw['nm_pasien']);
        }

        $total    = count($pasien_list);
        $sudah    = count(array_filter($pasien_list, fn($p) => $p['stts'] === 'Sudah'));
        $menunggu = count(array_filter($pasien_list, fn($p) => in_array($p['stts'], ['Menunggu','Belum'])));

        $dokter_data[] = [
            'kd_dokter'    => $kd_d,
            'kd_poli'      => $kd_p,
            'nm_dokter'    => $dok['nm_dokter'],
            'nm_poli'      => $dok['nm_poli'],
            'jam_mulai'    => $dok['jam_mulai'],
            'jam_selesai'  => $dok['jam_selesai'],
            'pasien'       => $pasien_list,
            'current_call' => $current_call,
            'total'        => $total,
            'sudah'        => $sudah,
            'menunggu'     => $menunggu,
        ];
    }

    $globalTotal    = array_sum(array_column($dokter_data, 'total'));
    $globalSudah    = array_sum(array_column($dokter_data, 'sudah'));
    $globalMenunggu = array_sum(array_column($dokter_data, 'menunggu'));

} catch (PDOException $e) {
    die("Gagal mengambil data: " . $e->getMessage());
}
?>
<!doctype html>
<html lang="id">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width,initial-scale=1">
<title>Display Antrian Poliklinik</title>
<link href="https://fonts.googleapis.com/css2?family=Archivo+Black&family=DM+Sans:wght@400;500;600;700;800&display=swap" rel="stylesheet">
<link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">
<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css">
<style>
* { margin:0; padding:0; box-sizing:border-box; }

:root {
    --primary:  #00d4aa;
    --secondary:#0088ff;
    --success:  #00e676;
    --danger:   #ff5252;
    --warning:  #fbbf24;
    --dark:     #0a1929;
    --card-bg:  rgba(255,255,255,0.98);
    --shadow:   rgba(10,25,41,0.10);
}

html, body {
    height: 100vh;
    overflow: hidden;
    font-family: 'DM Sans', sans-serif;
    background: linear-gradient(160deg, #0a1929 0%, #132f4c 50%, #1e4976 100%);
    position: relative;
}

#audioUnlockOverlay {
    position: fixed; inset: 0; background: rgba(10,25,41,0.95);
    z-index: 9999; display: flex; flex-direction: column;
    align-items: center; justify-content: center; color: #fff;
    cursor: pointer; backdrop-filter: blur(5px);
}
#audioUnlockOverlay:hover i { transform: scale(1.1); color: #fff; }
#audioUnlockOverlay i { transition: 0.3s; font-size: 6rem; color: var(--primary); margin-bottom: 20px; text-shadow: 0 0 30px rgba(0,212,170,.6); }
#audioUnlockOverlay h1 { font-family: 'Archivo Black', sans-serif; font-size: 3rem; margin: 0; letter-spacing: -1px; }
#audioUnlockOverlay p { font-size: 1.2rem; opacity: 0.7; margin-top: 10px; font-weight: 500; }

body::before {
    content:''; position:absolute; top:-50%; right:-20%;
    width:80%; height:80%;
    background:radial-gradient(circle,rgba(0,212,170,.13) 0%,transparent 70%);
    border-radius:50%; animation:bgPulse 18s ease-in-out infinite; pointer-events:none;
}
body::after {
    content:''; position:absolute; bottom:-30%; left:-15%;
    width:60%; height:60%;
    background:radial-gradient(circle,rgba(0,136,255,.10) 0%,transparent 70%);
    border-radius:50%; animation:bgPulse 22s ease-in-out infinite reverse; pointer-events:none;
}
@keyframes bgPulse{0%,100%{transform:scale(1);opacity:1}50%{transform:scale(1.12);opacity:.7}}

/* ===== HEADER ===== */
.header {
    position:relative; z-index:10;
    background:rgba(10,25,41,.95);
    backdrop-filter:blur(20px);
    border-bottom:3px solid var(--primary);
    padding:1.2vh 3vw;
    display:grid; grid-template-columns:auto 1fr auto;
    gap:2vw; align-items:center;
    box-shadow:0 4px 30px rgba(0,212,170,.2);
}

.brand-section { display:flex; align-items:center; gap:1.2vw; }
.brand-icon {
    width:4.5vw; height:4.5vw;
    min-width:52px; min-height:52px; max-width:72px; max-height:72px;
    background:linear-gradient(135deg,var(--primary),#00aa88);
    border-radius:1vw; display:flex; align-items:center; justify-content:center;
    box-shadow:0 8px 24px rgba(0,212,170,.4);
}
.brand-icon i { font-size:2.4vw; color:#fff; }
.brand-text h1 {
    font-family:'Archivo Black',sans-serif;
    font-size:2vw; color:#fff; margin:0; line-height:1;
    text-transform:uppercase; letter-spacing:-.02em;
    background:linear-gradient(135deg,#fff 0%,var(--primary) 100%);
    -webkit-background-clip:text; -webkit-text-fill-color:transparent;
}
.brand-text p { font-size:.9vw; color:rgba(255,255,255,.7); margin:.3vh 0 0; font-weight:600; letter-spacing:.05em; }

.header-stats { display:flex; gap:1.2vw; justify-content:center; }
.header-stat-item {
    display:flex; align-items:center; gap:.8vw;
    padding:.9vh 1.3vw;
    background:rgba(255,255,255,.08);
    border-radius:.8vw; border:1px solid rgba(255,255,255,.1);
}
.header-stat-icon {
    width:2.4vw; height:2.4vw; min-width:32px; min-height:32px;
    border-radius:.5vw; display:flex; align-items:center; justify-content:center;
}
.header-stat-icon.total   { background:linear-gradient(135deg,var(--secondary),#0066cc); }
.header-stat-icon.avail   { background:linear-gradient(135deg,var(--success),#00c853); }
.header-stat-icon.occ     { background:linear-gradient(135deg,var(--danger),#d32f2f); }
.header-stat-icon.waiting { background:linear-gradient(135deg,var(--warning),#e09000); }
.header-stat-icon i { font-size:1.2vw; color:#fff; }
.header-stat-label { font-size:.72vw; color:rgba(255,255,255,.6); font-weight:700; text-transform:uppercase; letter-spacing:.05em; }
.header-stat-value { font-family:'Archivo Black',sans-serif; font-size:1.8vw; color:#fff; line-height:1; }

/* Jam + WIB */
.header-info { text-align:right; }
.live-time-wrap { display:flex; align-items:flex-end; justify-content:flex-end; gap:.5vw; line-height:1; }
.live-time {
    font-family:'Archivo Black',sans-serif;
    font-size:2.8vw; color:var(--primary);
    letter-spacing:-.02em;
    text-shadow:0 0 30px rgba(0,212,170,.6);
}
.live-tz {
    font-family:'Archivo Black',sans-serif;
    font-size:1vw; color:var(--primary);
    opacity:.75; margin-bottom:.35vw;
    letter-spacing:.05em;
}
.live-date { font-size:.9vw; color:rgba(255,255,255,.8); font-weight:600; margin-top:.3vh; }

/* ===== MAIN ===== */
.main-content {
    position:relative; z-index:1;
    padding:1.5vh 2.5vw 0;
    height:calc(100vh - 11vh - 10vh - 3.5vh);
    overflow:hidden;
}
.page-wrapper { height:100%; position:relative; }

/* Opacity-based paging — TIDAK pakai display:none agar tidak kedip */
.page-slide {
    position:absolute; inset:0; height:100%;
    display:flex; flex-direction:column;
    opacity:0; visibility:hidden;
    transition:opacity .7s ease, visibility .7s ease;
    pointer-events:none;
}
.page-slide.active {
    opacity:1; visibility:visible; pointer-events:auto; position:relative;
}

.dokter-grid {
    display:grid; gap:1.4vw;
    width:100%; height:100%;
    grid-template-columns:repeat(var(--cols,3),1fr);
    grid-auto-rows:1fr;
}

/* ===== KARTU DOKTER ===== */
.dokter-card {
    background:var(--card-bg); border-radius:1.2vw; overflow:hidden;
    display:flex; flex-direction:column;
    box-shadow:0 4px 20px var(--shadow);
    border:2px solid transparent;
    height:100%; min-height:0;
}
.dokter-card.has-call {
    border-color:var(--warning);
    animation:cardGlow 2s ease-in-out infinite;
}
@keyframes cardGlow{
    0%,100%{box-shadow:0 4px 20px var(--shadow)}
    50%{box-shadow:0 6px 35px rgba(251,191,36,.5)}
}

.card-head {
    padding:1.2vh 1.4vw 1vh;
    background:linear-gradient(135deg,#0a1929,#132f4c);
    display:flex; align-items:flex-start; gap:.9vw; flex-shrink:0;
}
.dokter-avatar {
    width:3.2vw; height:3.2vw; min-width:40px; min-height:40px; max-width:54px; max-height:54px;
    background:linear-gradient(135deg,var(--primary),#0088ff);
    border-radius:50%; display:flex; align-items:center; justify-content:center; flex-shrink:0;
}
.dokter-avatar i { font-size:1.6vw; color:#fff; }
.card-head-info { flex:1; min-width:0; }
.nm-dokter {
    font-family:'Archivo Black',sans-serif;
    font-size:1.15vw; color:#fff; line-height:1.2;
    white-space:nowrap; overflow:hidden; text-overflow:ellipsis;
}
.nm-poli { font-size:.88vw; color:var(--primary); font-weight:600; margin-top:.3vh; }

.now-serving {
    background:linear-gradient(135deg,#fbbf24,#f59e0b);
    padding:1vh 1.4vw; display:flex; align-items:center; gap:1.4vw; flex-shrink:0;
}
.ns-left { display:flex; flex-direction:column; flex-shrink:0; }
.ns-label { font-size:.78vw; font-weight:700; color:#78350f; text-transform:uppercase; letter-spacing:.05em; margin-bottom:.1vh; }
.ns-number {
    font-family:'Archivo Black',sans-serif; font-size:2.8vw; color:#1c1400; line-height:1;
    animation:numPulse 2s ease-in-out infinite;
}
@keyframes numPulse{0%,100%{transform:scale(1)}50%{transform:scale(1.05)}}
.ns-right { flex:1; min-width:0; }
.ns-name { font-size:1.1vw; font-weight:800; color:#78350f; white-space:nowrap; overflow:hidden; text-overflow:ellipsis; }
.ns-info { font-size:.78vw; color:#92400e; margin-top:.2vh; }
.ns-empty {
    background:rgba(10,25,41,.06); padding:.9vh 1.4vw;
    display:flex; align-items:center; gap:.5vw;
    font-size:.85vw; color:rgba(0,0,0,.4); font-weight:600; flex-shrink:0;
}

.card-stats { display:flex; border-bottom:1px solid rgba(0,0,0,.07); flex-shrink:0; }
.cs-item { flex:1; text-align:center; padding:.8vh .5vw; border-right:1px solid rgba(0,0,0,.07); }
.cs-item:last-child { border-right:none; }
.cs-val { font-family:'Archivo Black',sans-serif; font-size:1.6vw; line-height:1; }
.cs-val.total  { color:#0088ff; }
.cs-val.sudah  { color:#00c853; }
.cs-val.tunggu { color:#ff5252; }
.cs-label { font-size:.7vw; font-weight:700; color:rgba(0,0,0,.45); text-transform:uppercase; margin-top:.3vh; }

/* Daftar pasien — auto-scroll via RAF */
.pasien-list { flex:1; overflow:hidden; padding:.3vh 0; min-height:0; position:relative; }
.pasien-inner { display:flex; flex-direction:column; }

.pasien-row {
    display:grid; grid-template-columns:5.8vw 1fr 5.5vw;
    gap:.6vw; align-items:center; padding:.7vh 1.2vw;
    border-bottom:1px solid rgba(0,0,0,.05);
}
.pasien-row:last-child { border-bottom:none; }
.pasien-row.active { background:rgba(251,191,36,.18); }
.pasien-row.done   { opacity:.45; background:rgba(0,200,83,.06); }
.pr-no { font-family:'Archivo Black',sans-serif; font-size:.85vw; color:var(--dark); white-space:nowrap; }
.pr-no.active { color:#b45309; }
.pr-nama { font-size:.88vw; font-weight:600; color:#1a2e44; white-space:nowrap; overflow:hidden; text-overflow:ellipsis; }
.pr-status { font-size:.75vw; font-weight:700; text-align:right; text-transform:uppercase; letter-spacing:.03em; }
.pr-status.waiting { color:#ff5252; }
.pr-status.done    { color:#00c853; }
.pr-status.active  { color:#d97706; }

/* Fade bawah — isyarat masih ada data */
.pasien-list::after {
    content:''; position:absolute; bottom:0; left:0; right:0; height:2.5vh;
    background:linear-gradient(to bottom,transparent,rgba(255,255,255,.95));
    pointer-events:none; z-index:2;
}
.pasien-list.no-overflow::after { display:none; }
.no-pasien { text-align:center; padding:2vh 1vw; color:rgba(0,0,0,.35); font-size:.92vw; font-weight:600; }

/* ===== PAGE DOTS ===== */
.page-dots {
    position:fixed; bottom:11vh; left:50%; transform:translateX(-50%);
    display:flex; gap:.6vw; z-index:20; height:3vh; align-items:center;
}
.page-dot {
    width:.7vw; height:.7vw; min-width:8px; min-height:8px;
    border-radius:50%; background:rgba(255,255,255,.35);
    transition:background .3s, transform .3s;
}
.page-dot.active { background:var(--primary); transform:scale(1.4); }

/* ===== FOOTER 2 BARIS ===== */
.footer {
    position:fixed; bottom:0; left:0; right:0; z-index:10;
    background:rgba(10,25,41,.97);
    backdrop-filter:blur(20px);
    border-top:3px solid var(--primary);
    overflow:hidden;
    box-shadow:0 -4px 30px rgba(0,212,170,.2);
}

/* Baris 1 — ringkasan dokter */
.marquee-row-1 {
    border-bottom:1px solid rgba(255,255,255,.08);
    padding:.55vh 0; overflow:hidden;
}
/* Baris 2 — copyright */
.footer-copy {
    border-top:1px solid rgba(255,255,255,.06);
    padding:.4vh 2vw;
    display:flex; align-items:center; justify-content:space-between;
    background:rgba(0,0,0,.35);
}
.footer-copy-left {
    font-size:.68vw; color:rgba(255,255,255,.4); font-weight:500;
    display:flex; align-items:center; gap:.5vw;
}
.footer-copy-left i { color:var(--primary); font-size:.7vw; }
.footer-copy-right {
    font-size:.68vw; color:rgba(255,255,255,.35); font-weight:500;
    letter-spacing:.03em;
}
.footer-copy-right span { color:var(--primary); font-weight:700; }

/* Marquee konten */
.marquee-content {
    display:inline-flex; white-space:nowrap;
    font-size:.95vw; font-weight:600; color:#fff;
}
.marquee-content.row1 { animation:mq1 140s linear infinite; }
@keyframes mq1{0%{transform:translateX(0)}100%{transform:translateX(-50%)}}

/* Baris 1 — item per dokter */
.mq-item { display:inline-flex; align-items:center; gap:.5vw; padding:0 2vw 0 0; }
.mq-item i { color:var(--primary); font-size:.85vw; }
.mq-dokter  { color:#fff; font-weight:800; }
.mq-poli    { color:var(--primary); font-weight:600; }
.mq-num     { color:var(--warning); font-family:'Archivo Black',sans-serif; }
.mq-tunggu  { color:#ff5252; font-weight:800; }
.mq-selesai { color:var(--success); font-weight:800; }
.mq-sep     { color:rgba(255,255,255,.2); margin:0 .15vw; }

/* sync dot */
.sync-dot {
    width:9px; height:9px; border-radius:50%;
    background:#10b981; display:inline-block; margin-left:8px;
    animation:syncBlink 2s ease-in-out infinite; vertical-align:middle;
}
@keyframes syncBlink{0%,100%{opacity:1}50%{opacity:.3}}

.btn-fullscreen {
    position: fixed;
    top: 18px;
    right: 18px;
    z-index: 10000;
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

<div id="audioUnlockOverlay">
    <i class="bi bi-play-circle-fill"></i>
    <h1>KLIK UNTUK MEMULAI</h1>
    <p>Mengaktifkan Tampilan & Suara Panggilan</p>
</div>

<!-- Button Fullscreen -->
<a class="btn-fullscreen" onclick="toggleFullscreen()" title="Toggle Fullscreen">
    <i class="fa fa-arrows-alt" id="fullscreenIcon"></i>
</a>

<!-- ===== HEADER ===== -->
<div class="header">
    <div class="brand-section">
        <div class="brand-icon"><i class="bi bi-display-fill"></i></div>
        <div class="brand-text">
            <h1>Display Antrian Poliklinik <span class="sync-dot" id="syncDot"></span></h1>
            <p>
                <?php if (!empty($poli) && !empty($dokter_data)): ?>
                    <?= htmlspecialchars($dokter_data[0]['nm_poli'] ?? '') ?>
                <?php else: ?>
                    Semua Poliklinik &mdash; RSUD DEMANG SEPULAU RAYA
                <?php endif; ?>
            </p>
        </div>
    </div>

    <div class="header-stats">
        <div class="header-stat-item">
            <div class="header-stat-icon total"><i class="bi bi-person-lines-fill"></i></div>
            <div>
                <div class="header-stat-label">Total Pasien</div>
                <div class="header-stat-value" id="globalTotal"><?= $globalTotal ?></div>
            </div>
        </div>
        <div class="header-stat-item">
            <div class="header-stat-icon avail"><i class="bi bi-check-circle-fill"></i></div>
            <div>
                <div class="header-stat-label">Selesai</div>
                <div class="header-stat-value" id="globalSudah"><?= $globalSudah ?></div>
            </div>
        </div>
        <div class="header-stat-item">
            <div class="header-stat-icon waiting"><i class="bi bi-clock-history"></i></div>
            <div>
                <div class="header-stat-label">Menunggu</div>
                <div class="header-stat-value" id="globalMenunggu"><?= $globalMenunggu ?></div>
            </div>
        </div>
        <div class="header-stat-item">
            <div class="header-stat-icon occ"><i class="bi bi-person-badge-fill"></i></div>
            <div>
                <div class="header-stat-label">Dokter Aktif</div>
                <div class="header-stat-value"><?= count($dokter_data) ?></div>
            </div>
        </div>
    </div>

    <!-- Jam + WIB -->
    <div class="header-info">
        <div class="live-time-wrap">
            <div class="live-time" id="liveTime">00:00:00</div>
            <div class="live-tz">WIB</div>
        </div>
        <div class="live-date" id="liveDate">&mdash;</div>
    </div>
</div>

<!-- ===== MAIN ===== -->
<div class="main-content">
    <div class="page-wrapper" id="pageWrapper">
        <?php
        $jml = count($dokter_data);
        if      ($jml <= 1) $cols = 1;
        elseif  ($jml <= 2) $cols = 2;
        else                $cols = 3;

        $per_page = 6;
        $pages    = array_chunk($dokter_data, $per_page);
        ?>

        <?php foreach ($pages as $pi => $page_dokter): ?>
        <div class="page-slide <?= $pi === 0 ? 'active' : '' ?>" id="slide-<?= $pi ?>">
            <div class="dokter-grid" style="--cols:<?= $cols ?>">
            <?php foreach ($page_dokter as $dok):
                $cc       = $dok['current_call'];
                $has_call = !empty($cc);
            ?>
                <div class="dokter-card <?= $has_call ? 'has-call' : '' ?>"
                     data-kd-dokter="<?= htmlspecialchars($dok['kd_dokter']) ?>"
                     data-kd-poli="<?= htmlspecialchars($dok['kd_poli']) ?>">

                    <div class="card-head">
                        <div class="dokter-avatar"><i class="bi bi-person-badge-fill"></i></div>
                        <div class="card-head-info">
                            <div class="nm-dokter"><?= htmlspecialchars($dok['nm_dokter']) ?></div>
                            <div class="nm-poli"><i class="bi bi-geo-alt-fill"></i> <?= htmlspecialchars($dok['nm_poli']) ?></div>
                        </div>
                    </div>

                    <?php if ($has_call): ?>
                    <div class="now-serving">
                        <div class="ns-left">
                            <div class="ns-label"><i class="bi bi-megaphone-fill"></i> Sedang Dilayani</div>
                            <div class="ns-number"><?= htmlspecialchars($cc['no_antrian']) ?></div>
                        </div>
                        <div class="ns-right">
                            <div class="ns-name"><?= htmlspecialchars($cc['nm_pasien']) ?></div>
                            <?php if ($cc['jml_panggil'] > 1): ?>
                            <div class="ns-info"><i class="bi bi-arrow-repeat"></i> Dipanggil <?= $cc['jml_panggil'] ?>x</div>
                            <?php endif; ?>
                        </div>
                    </div>
                    <?php else: ?>
                    <div class="ns-empty"><i class="bi bi-hourglass-split"></i> Menunggu Panggilan...</div>
                    <?php endif; ?>

                    <div class="card-stats">
                        <div class="cs-item"><div class="cs-val total"><?= $dok['total'] ?></div><div class="cs-label">Total</div></div>
                        <div class="cs-item"><div class="cs-val tunggu"><?= $dok['menunggu'] ?></div><div class="cs-label">Menunggu</div></div>
                        <div class="cs-item"><div class="cs-val sudah"><?= $dok['sudah'] ?></div><div class="cs-label">Selesai</div></div>
                    </div>

                    <?php if (empty($dok['pasien'])): ?>
                    <div class="pasien-list no-overflow">
                        <div class="pasien-inner">
                            <div class="no-pasien"><i class="bi bi-inbox"></i> Belum ada pasien</div>
                        </div>
                    </div>
                    <?php else: ?>
                    <div class="pasien-list">
                        <div class="pasien-inner">
                        <?php foreach ($dok['pasien'] as $ps):
                            $hurufAwal = generateKodeAntrian($dok['kd_poli'], $dok['nm_poli']);
                            $no_ant    = $hurufAwal . '-' . $ps['no_reg'];
                            $is_active= $cc && $cc['no_rawat'] === $ps['no_rawat'];
                            $is_done  = $ps['stts'] === 'Sudah';
                            $rowCls   = $is_active ? 'active' : ($is_done ? 'done' : '');
                            $stLabel  = $is_active ? 'Dipanggil' : ($is_done ? 'Selesai' : 'Menunggu');
                            $stCls    = $is_active ? 'active' : ($is_done ? 'done' : 'waiting');
                        ?>
                        <div class="pasien-row <?= $rowCls ?>" data-no-rawat="<?= htmlspecialchars($ps['no_rawat']) ?>">
                            <div class="pr-no <?= $is_active ? 'active' : '' ?>"><?= htmlspecialchars($no_ant) ?></div>
                            <div class="pr-nama"><?= htmlspecialchars($ps['nm_pasien']) ?></div>
                            <div class="pr-status <?= $stCls ?>"><?= $stLabel ?></div>
                        </div>
                        <?php endforeach; ?>
                        </div>
                    </div>
                    <?php endif; ?>

                </div>
            <?php endforeach; ?>
            </div>
        </div>
        <?php endforeach; ?>
    </div>
</div>

<!-- Page Dots -->
<div class="page-dots" id="pageDots">
    <?php foreach ($pages as $pi => $_): ?>
    <div class="page-dot <?= $pi === 0 ? 'active' : '' ?>"></div>
    <?php endforeach; ?>
</div>

<!-- ===== FOOTER 2 BARIS + COPYRIGHT ===== -->
<div class="footer">

    <!-- Baris 1: Ringkasan per dokter -->
    <div class="marquee-row-1">
        <div class="marquee-content row1" id="marqueeRow1">
            <?php
            $r1 = '';
            foreach ($dokter_data as $dok) {
                $calling = $dok['current_call']
                    ? '<span class="mq-num">&#9654; No.'.htmlspecialchars($dok['current_call']['no_antrian']).'</span>'
                      .' <span style="color:rgba(255,255,255,.85);font-weight:600">'.htmlspecialchars($dok['current_call']['nm_pasien']).'</span>'
                    : '<span style="color:rgba(255,255,255,.3)">Menunggu&hellip;</span>';
                $r1 .= "<span class='mq-item'>"
                    ."<i class='bi bi-hospital-fill'></i>"
                    ."<span class='mq-dokter'>".htmlspecialchars($dok['nm_dokter'])."</span>"
                    ."<span class='mq-sep'>|</span>"
                    ."<span class='mq-poli'>".htmlspecialchars($dok['nm_poli'])."</span>"
                    ."<span class='mq-sep'>&middot;</span>"
                    ."Dilayani: $calling"
                    ."<span class='mq-sep'>&middot;</span>"
                    ."Menunggu: <span class='mq-tunggu'>{$dok['menunggu']}</span>"
                    ."<span class='mq-sep'>&middot;</span>"
                    ."Selesai: <span class='mq-selesai'>{$dok['sudah']}</span>"
                    ."<span class='mq-sep' style='margin-left:.8vw;opacity:.3'>&boxv;&boxv;</span>"
                    ."</span>";
            }
            echo $r1.$r1;
            ?>
        </div>
    </div>

    <!-- Baris 2: Copyright -->
    <div class="footer-copy">
        <div class="footer-copy-left">
            <i class="bi bi-shield-check-fill"></i>
            &copy; <?= date('Y') ?> <span style="color:var(--primary);font-weight:700">MediFix</span>
            &nbsp;&mdash;&nbsp;
            Anjungan Pasien Mandiri &amp; Sistem Antrian
            &nbsp;&nbsp;<span style="color:rgba(255,255,255,.2)">|</span>&nbsp;&nbsp;
            <i class="bi bi-person-fill"></i>
            <span style="color:var(--primary);font-weight:700">IT RSUD DEMANG SEPULAU RAYA</span>
        </div>
        <div class="footer-copy-right">
            Powered by <span>MediFix</span>
            &nbsp;&middot;&nbsp; v1.0
        </div>
    </div>
</div>

<script>
/* ===== CLOCK ===== */
var HARI  = ['Minggu','Senin','Selasa','Rabu','Kamis','Jumat','Sabtu'];
var BULAN = ['Januari','Februari','Maret','April','Mei','Juni',
             'Juli','Agustus','September','Oktober','November','Desember'];
function pad2(n){ return String(n).padStart(2,'0'); }
function updateClock(){
    var now = new Date();
    document.getElementById('liveTime').textContent =
        pad2(now.getHours())+':'+pad2(now.getMinutes())+':'+pad2(now.getSeconds());
    document.getElementById('liveDate').textContent =
        HARI[now.getDay()]+', '+now.getDate()+' '+BULAN[now.getMonth()]+' '+now.getFullYear();
}
updateClock();
setInterval(updateClock, 1000);

/* ===== PAGING — opacity, tidak kedip ===== */
var slides    = document.querySelectorAll('.page-slide');
var dots      = document.querySelectorAll('.page-dot');
var curPage   = 0;
var pageTimer = null;
var POLI      = '<?= addslashes($poli) ?>';
var DOKTER    = '<?= addslashes($dokter) ?>';

function calcPageDuration(slideEl){
    var maxRows = 0;
    slideEl.querySelectorAll('.pasien-list').forEach(function(list){
        var n = list.querySelectorAll('.pasien-row').length;
        if(n > maxRows) maxRows = n;
    });
    /* min 10 dtk, +0.7 dtk per baris, max 30 dtk */
    return Math.min(30000, Math.max(10000, maxRows * 700 + 6000));
}

function showSlide(idx){
    slides.forEach(function(s){ s.classList.remove('active'); });
    dots.forEach(function(d){   d.classList.remove('active'); });
    slides[idx].classList.add('active');
    if(dots[idx]) dots[idx].classList.add('active');
    curPage = idx;
    if(slides.length > 1){
        if(pageTimer) clearTimeout(pageTimer);
        pageTimer = setTimeout(function(){ showSlide((curPage+1) % slides.length); },
                               calcPageDuration(slides[idx]));
    }
}
if(slides.length > 1) showSlide(0);

/* ===== AUTO-SCROLL PASIEN — requestAnimationFrame ===== */
var SPEED_DN     = 22;
var SPEED_UP     = 250;
var PAUSE_TOP_MS = 3000;
var PAUSE_BOT_MS = 2500;
var scrollStates = [];

function buildScrollStates(){
    var oldMap = {};
    scrollStates.forEach(function(st){
        if(st.list.dataset.scrollKey)
            oldMap[st.list.dataset.scrollKey] = {pos:st.pos, phase:st.phase, pauseEnd:st.pauseEnd};
    });
    scrollStates = [];

    document.querySelectorAll('.pasien-list').forEach(function(list){
        var inner = list.querySelector('.pasien-inner');
        if(!inner) return;
        inner.style.animation = inner.style.transition = 'none';

        var maxScroll = Math.max(0, inner.scrollHeight - list.clientHeight);
        if(maxScroll <= 0){ list.classList.add('no-overflow'); inner.style.transform='translateY(0)'; return; }
        list.classList.remove('no-overflow');

        var card = list.closest('.dokter-card');
        var key  = card ? card.dataset.kdDokter+'_'+card.dataset.kdPoli : Math.random();
        list.dataset.scrollKey = key;
        var old  = oldMap[key];

        scrollStates.push({
            list:list, inner:inner, maxScroll:maxScroll,
            pos      : old ? Math.min(old.pos, maxScroll) : 0,
            phase    : old ? old.phase : 'PAUSE_TOP',
            pauseEnd : old ? old.pauseEnd : performance.now()+PAUSE_TOP_MS,
            lastTime : performance.now()
        });
    });
}

function rafLoop(now){
    scrollStates.forEach(function(st){
        var dt = Math.min((now-st.lastTime)/1000, 0.1);
        st.lastTime = now;
        switch(st.phase){
            case 'PAUSE_TOP':
                if(now>=st.pauseEnd) st.phase='SCROLL_DN'; break;
            case 'SCROLL_DN':
                st.pos += SPEED_DN*dt;
                if(st.pos>=st.maxScroll){ st.pos=st.maxScroll; st.phase='PAUSE_BOT'; st.pauseEnd=now+PAUSE_BOT_MS; }
                st.inner.style.transform='translateY(-'+st.pos.toFixed(1)+'px)'; break;
            case 'PAUSE_BOT':
                if(now>=st.pauseEnd) st.phase='SCROLL_UP'; break;
            case 'SCROLL_UP':
                st.pos -= SPEED_UP*dt;
                if(st.pos<=0){ st.pos=0; st.phase='PAUSE_TOP'; st.pauseEnd=now+PAUSE_TOP_MS; }
                st.inner.style.transform='translateY(-'+st.pos.toFixed(1)+'px)'; break;
        }
    });
    requestAnimationFrame(rafLoop);
}

window.addEventListener('load', function(){
    setTimeout(function(){ buildScrollStates(); requestAnimationFrame(rafLoop); }, 500);
});
window.addEventListener('resize', function(){ setTimeout(buildScrollStates,200); });

/* ===== POLLING LIVE — soft update tanpa reload ===== */
var lastCalls = {};
<?php foreach ($dokter_data as $dok): ?>
lastCalls['<?= addslashes($dok['kd_dokter'].'_'.$dok['kd_poli']) ?>'] = {
    no_antrian: '<?= addslashes($dok['current_call']['no_antrian'] ?? '') ?>',
    jml_panggil: <?= (int)($dok['current_call']['jml_panggil'] ?? 0) ?>
};
<?php endforeach; ?>

/* ===== SUARA TTS ===== */
var bellAudio = new Audio('sound/opening.mp3');
bellAudio.preload = 'auto';
var soundQueue = [];
var isSpeaking = false;

function angkaToSuara(n) {
    n = parseInt(n, 10);
    if (isNaN(n)) return '';
    var satuan  = ['','satu','dua','tiga','empat','lima','enam','tujuh','delapan','sembilan'];
    var belasan = ['sepuluh','sebelas','dua belas','tiga belas','empat belas','lima belas',
                   'enam belas','tujuh belas','delapan belas','sembilan belas'];
    if (n === 0) return 'nol';
    if (n < 10)  return satuan[n];
    if (n < 20)  return belasan[n - 10];
    if (n < 100) { var p=Math.floor(n/10),s=n%10; return satuan[p] + ' puluh' + (s>0 ? ' ' + satuan[s] : ''); }
    if (n < 200) { var s2=n-100; return 'seratus' + (s2>0 ? ' ' + angkaToSuara(s2) : ''); }
    if (n < 1000){ var r=Math.floor(n/100),s3=n%100; return satuan[r] + ' ratus' + (s3>0 ? ' ' + angkaToSuara(s3) : ''); }
    if (n < 2000){ var s4=n-1000; return 'seribu' + (s4>0 ? ' ' + angkaToSuara(s4) : ''); }
    var rb=Math.floor(n/1000),s5=n%1000;
    return satuan[rb] + ' ribu' + (s5>0 ? ' ' + angkaToSuara(s5) : '');
}

function speak(text, callback) {
    var u = new SpeechSynthesisUtterance(text);
    u.lang = 'id-ID'; u.pitch = 1.0; u.rate = 0.9; u.volume = 1.0;
    var voices = window.speechSynthesis.getVoices();
    var idv = voices.find(function(v){ return v.lang.indexOf('id') >= 0; });
    if (idv) u.voice = idv;
    u.onend = function(){ if(callback) callback(); };
    window.speechSynthesis.speak(u);
}

function playCallSound(noAntrian, nmPasien, nmPoli) {
    soundQueue.push({noAntrian: noAntrian, nmPasien: nmPasien, nmPoli: nmPoli});
    if (!isSpeaking) processQueue();
}

function processQueue() {
    if (soundQueue.length === 0) { isSpeaking = false; return; }
    isSpeaking = true;
    var item = soundQueue.shift();
    var parts = item.noAntrian.split('-');
    var kodePoli = parts[0] || '';
    var rawNomor = parts.slice(1).join('-') || '';
    var nomorKata = angkaToSuara(rawNomor);
    var namaTitleCase = (item.nmPasien || '').toLowerCase().replace(/\b\w/g, function(c){ return c.toUpperCase(); });
    
    var kalimat = 'Nomor antrian ' + kodePoli.split('').join(' ') + ' ' + nomorKata
        + '. Atas nama, ' + namaTitleCase
        + '. Silakan menuju ' + item.nmPoli + '.';

    window.speechSynthesis.cancel();
    bellAudio.currentTime = 0;
    bellAudio.play()
        .then(function(){ bellAudio.onended = function(){ speak(kalimat, processQueue); }; })
        .catch(function(){ speak(kalimat, processQueue); });
}

/* Aktifkan audio pada klik overlay (diperlukan browser) */
document.getElementById('audioUnlockOverlay').addEventListener('click', function() {
    this.style.opacity = '0';
    this.style.transition = 'opacity 0.5s';
    setTimeout(() => this.style.display = 'none', 500);

    bellAudio.volume = 0;
    bellAudio.play().then(function(){ bellAudio.pause(); bellAudio.volume = 1; }).catch(function(){});
    
    // Unlock TTS engine
    var u = new SpeechSynthesisUtterance('');
    u.volume = 0;
    window.speechSynthesis.speak(u);
}, {once:true});

function pollCalls(){
    fetch('get_all_calls.php?poli='+encodeURIComponent(POLI)+'&dokter='+encodeURIComponent(DOKTER)+'&_t='+Date.now())
    .then(function(r){ return r.json(); })
    .then(function(data){
        document.getElementById('syncDot').style.background='#10b981';
        var tot=0, sdh=0, tgu=0;
        var r1Parts=[];

        data.forEach(function(item){
            tot+=(item.total||0); sdh+=(item.sudah||0); tgu+=(item.menunggu||0);

            /* Deteksi panggilan baru atau panggil ulang */
            var callKey = item.kd_dokter + '_' + item.kd_poli;
            var prev = lastCalls[callKey] || {no_antrian: '', jml_panggil: 0};
            var isNewCall = item.no_antrian && (
                item.no_antrian !== prev.no_antrian ||
                (item.jml_panggil || 0) > prev.jml_panggil
            );
            if (isNewCall) {
                playCallSound(item.no_antrian, item.nm_pasien_suara || item.nm_pasien || '', item.nm_poli || '');
            }
            lastCalls[callKey] = {
                no_antrian: item.no_antrian || '',
                jml_panggil: item.jml_panggil || 0
            };

            var card=document.querySelector(
                '.dokter-card[data-kd-dokter="'+item.kd_dokter+'"][data-kd-poli="'+item.kd_poli+'"]');
            if(!card) return;

            card.classList.toggle('has-call',!!item.no_antrian);

            var nsEl=card.querySelector('.now-serving,.ns-empty');
            if(nsEl){
                if(item.no_antrian){
                    nsEl.outerHTML='<div class="now-serving">'
                        +'<div class="ns-left">'
                            +'<div class="ns-label"><i class="bi bi-megaphone-fill"></i> Sedang Dilayani</div>'
                            +'<div class="ns-number">'+item.no_antrian+'</div>'
                        +'</div>'
                        +'<div class="ns-right">'
                            +'<div class="ns-name">'+(item.nm_pasien||'')+'</div>'
                        +'</div>'
                    +'</div>';
                } else {
                    nsEl.outerHTML='<div class="ns-empty"><i class="bi bi-hourglass-split"></i> Menunggu Panggilan...</div>';
                }
            }

            var vals=card.querySelectorAll('.cs-val');
            if(vals.length>=3){ vals[0].textContent=item.total||0; vals[1].textContent=item.menunggu||0; vals[2].textContent=item.sudah||0; }

            /* Re-render Daftar Pasien */
            var listWrapper = card.querySelector('.pasien-list');
            var innerList = card.querySelector('.pasien-inner');
            
            if (listWrapper && innerList) {
                if (!item.pasien || item.pasien.length === 0) {
                    listWrapper.classList.add('no-overflow');
                    innerList.innerHTML = '<div class="no-pasien"><i class="bi bi-inbox"></i> Belum ada pasien</div>';
                } else {
                    var pasienHtml = '';
                    item.pasien.forEach(function(ps) {
                        var kdPoli = item.kd_poli || '';
                        var nmPoli = item.nm_poli || '';
                        
                        // Mapping manual sederhana untuk JS (sama dengan PHP generateKodeAntrian)
                        function getKodeJS(kd, nm) {
                            var map = { 'U0001':'K','U0002':'A','U0003':'P','U0004':'B','U0005':'M','U0006':'K','U0007':'S','U0008':'R','U0009':'U',
                                        'U0010':'G','U0011':'T','U0012':'J','U0052':'G','U0053':'F','IGDK':'I','MCU':'C','OBG':'O','INT':'D' };
                            if(map[kd]) return map[kd];
                            var n = nm.replace(/Poliklinik |Poli /gi, '').trim();
                            return (n ? n[0] : (kd ? kd[0] : '?')).toUpperCase();
                        }
                        
                        var huruf = getKodeJS(kdPoli, nmPoli);
                        var noAnt  = huruf + '-' + ps.no_reg;
                        var isActive = !!(item.no_rawat && ps.no_rawat === item.no_rawat);
                        var isDone   = ps.stts === 'Sudah';
                        
                        var rowCls = isActive ? 'active' : (isDone ? 'done' : '');
                        var stLabel = isActive ? 'Dipanggil' : (isDone ? 'Selesai' : 'Menunggu');
                        var stCls = isActive ? 'active' : (isDone ? 'done' : 'waiting');
                        
                        pasienHtml += '<div class="pasien-row ' + rowCls + '" data-no-rawat="' + ps.no_rawat + '">'
                            + '<div class="pr-no ' + (isActive ? 'active' : '') + '">' + noAnt + '</div>'
                            + '<div class="pr-nama">' + ps.nm_pasien + '</div>'
                            + '<div class="pr-status ' + stCls + '">' + stLabel + '</div>'
                            + '</div>';
                    });
                    innerList.innerHTML = pasienHtml;
                }
            }

            /* Baris 1 marquee */
            var calling = item.no_antrian
                ? '<span class="mq-num">&#9654; No.'+item.no_antrian+'</span> <span style="color:rgba(255,255,255,.85);font-weight:600">'+(item.nm_pasien||'')+'</span>'
                : '<span style="color:rgba(255,255,255,.3)">Menunggu&hellip;</span>';
            r1Parts.push('<span class="mq-item">'
                +'<i class="bi bi-hospital-fill"></i>'
                +'<span class="mq-dokter">'+(item.nm_dokter||'')+'</span>'
                +'<span class="mq-sep">|</span>'
                +'<span class="mq-poli">'+(item.nm_poli||'')+'</span>'
                +'<span class="mq-sep">&middot;</span>'
                +'Dilayani: '+calling
                +'<span class="mq-sep">&middot;</span>'
                +'Menunggu: <span class="mq-tunggu">'+(item.menunggu||0)+'</span>'
                +'<span class="mq-sep">&middot;</span>'
                +'Selesai: <span class="mq-selesai">'+(item.sudah||0)+'</span>'
                +'<span class="mq-sep" style="margin-left:.8vw;opacity:.3">&boxv;&boxv;</span>'
                +'</span>');
        });

        var elTot=document.getElementById('globalTotal');
        var elSdh=document.getElementById('globalSudah');
        var elTgu=document.getElementById('globalMenunggu');
        if(elTot) elTot.textContent=tot;
        if(elSdh) elSdh.textContent=sdh;
        if(elTgu) elTgu.textContent=tgu;

        var mr1=document.getElementById('marqueeRow1');
        if(mr1 && r1Parts.length){ var h=r1Parts.join(''); mr1.innerHTML=h+h; }

        /* RE-INITIALIZE SCROLLING */
        buildScrollStates();
    })
    .catch(function(){ document.getElementById('syncDot').style.background='#ef4444'; });
}

pollCalls();
setInterval(pollCalls, 2000);
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

document.addEventListener('click', function triggerAuto() {
    if (!document.fullscreenElement) {
        document.documentElement.requestFullscreen().catch(e => console.warn(e));
    }
    document.removeEventListener('click', triggerAuto);
}, { once: true });

// Hide redundant button if inside Kiosk Wrapper (iframe)
if (window.self !== window.top) {
    const fsBtn = document.querySelector('.btn-fullscreen');
    if (fsBtn) fsBtn.style.display = 'none';
}
</script>
</body>
</html>