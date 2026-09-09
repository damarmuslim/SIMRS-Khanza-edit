<?php
include 'koneksi2.php';
date_default_timezone_set('Asia/Jakarta');

$hari_ini = strtoupper(date('l'));
$mapHari  = [
    'MONDAY'=>'SENIN','TUESDAY'=>'SELASA','WEDNESDAY'=>'RABU',
    'THURSDAY'=>'KAMIS','FRIDAY'=>'JUMAT','SATURDAY'=>'SABTU','SUNDAY'=>'MINGGU'
];
$hari_indo = $mapHari[$hari_ini] ?? 'SENIN';

$sql = "
    SELECT j.kd_dokter, d.nm_dokter, p.nm_poli,
           j.hari_kerja, j.jam_mulai, j.jam_selesai, j.kuota
    FROM jadwal j
    INNER JOIN dokter     d ON j.kd_dokter = d.kd_dokter
    INNER JOIN poliklinik p ON j.kd_poli   = p.kd_poli
    WHERE j.hari_kerja = '$hari_indo'
    ORDER BY p.nm_poli, d.nm_dokter
";
$jadwal    = $pdo_simrs->query($sql)->fetchAll(PDO::FETCH_ASSOC);

$jumlah_pasien = [];
$tgl = date('Y-m-d');
$sq  = $pdo_simrs->prepare("SELECT kd_dokter, COUNT(no_rawat) AS total FROM reg_periksa WHERE tgl_registrasi=? GROUP BY kd_dokter");
$sq->execute([$tgl]);
foreach ($sq->fetchAll(PDO::FETCH_ASSOC) as $r) {
    $jumlah_pasien[$r['kd_dokter']] = (int)$r['total'];
}

// Untuk layar TV, idealnya 8 kartu per halaman (2 baris x 4 kolom)
$items_per_page = 8;
$pages     = array_chunk($jadwal, $items_per_page);
$totalPage = count($pages);

// Fungsi untuk membuat inisial nama dokter
function getInitials($name) {
    $name = str_replace(['dr. ', 'Dr. ', 'drg. ', 'msc', 'Sp.', 'M.Kes', ',', '.'], '', $name);
    $words = explode(' ', trim($name));
    if (count($words) >= 2) {
        return strtoupper(substr($words[0], 0, 1) . substr($words[1], 0, 1));
    }
    return strtoupper(substr($name, 0, 2));
}

// Konfigurasi warna avatar berdasarkan huruf awal
function getAvatarColor($initial) {
    $colors = [
        'A'=>'#f87171', 'B'=>'#fb923c', 'C'=>'#fbbf24', 'D'=>'#a3e635', 'E'=>'#34d399',
        'F'=>'#2dd4bf', 'G'=>'#38bdf8', 'H'=>'#818cf8', 'I'=>'#c084fc', 'J'=>'#e879f9',
        'K'=>'#f43f5e', 'L'=>'#f97316', 'M'=>'#eab308', 'N'=>'#84cc16', 'O'=>'#10b981',
        'P'=>'#06b6d4', 'Q'=>'#0ea5e9', 'R'=>'#6366f1', 'S'=>'#a855f7', 'T'=>'#d946ef',
        'U'=>'#ef4444', 'V'=>'#f59e0b', 'W'=>'#22c55e', 'X'=>'#0ea5e9', 'Y'=>'#8b5cf6', 'Z'=>'#ec4899'
    ];
    $char = substr($initial, 0, 1);
    return $colors[$char] ?? '#64748b';
}
?>
<!DOCTYPE html>
<html lang="id">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width,initial-scale=1">
<title>Jadwal Dokter — Rawat Jalan</title>
<link rel="preconnect" href="https://fonts.googleapis.com">
<link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
<link href="https://fonts.googleapis.com/css2?family=Outfit:wght@300;400;500;600;700;800&display=swap" rel="stylesheet">
<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
<style>
:root {
  --bg-dark: #0f172a;
  --glass-bg: rgba(30, 41, 59, 0.6);
  --glass-border: rgba(255, 255, 255, 0.08);
  --glass-shadow: 0 8px 32px 0 rgba(0, 0, 0, 0.37);
  --accent-1: #38bdf8; /* Light Blue */
  --accent-2: #818cf8; /* Indigo */
  --accent-3: #f472b6; /* Pink */
  --text-main: #f8fafc;
  --text-muted: #94a3b8;
  --success: #10b981;
  --warning: #f59e0b;
  --danger: #ef4444;
}

* { box-sizing: border-box; margin: 0; padding: 0; }

body {
  font-family: 'Outfit', sans-serif;
  background-color: var(--bg-dark);
  color: var(--text-main);
  height: 100vh;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  position: relative;
}

/* Animated Gradient Background */
.bg-animation {
  position: fixed;
  top: -50%; left: -50%; width: 200%; height: 200%;
  background: radial-gradient(circle at 50% 50%, rgba(56, 189, 248, 0.15) 0%, transparent 40%),
              radial-gradient(circle at 80% 20%, rgba(129, 140, 248, 0.15) 0%, transparent 40%),
              radial-gradient(circle at 20% 80%, rgba(244, 114, 182, 0.1) 0%, transparent 40%);
  animation: rotateBg 30s linear infinite;
  z-index: -1;
  pointer-events: none;
}
@keyframes rotateBg {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* Header */
.header {
  height: 90px;
  padding: 0 40px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: rgba(15, 23, 42, 0.7);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-bottom: 1px solid var(--glass-border);
  box-shadow: 0 4px 30px rgba(0, 0, 0, 0.1);
  z-index: 10;
}

.header-left { display: flex; align-items: center; gap: 20px; }
.logo-box {
  width: 54px; height: 54px;
  border-radius: 16px;
  background: linear-gradient(135deg, var(--accent-1), var(--accent-2));
  display: flex; align-items: center; justify-content: center;
  font-size: 28px; color: white;
  box-shadow: 0 0 20px rgba(56, 189, 248, 0.4);
}
.header-titles h1 {
  font-size: 26px; font-weight: 800; letter-spacing: 0.5px;
  background: linear-gradient(to right, #fff, var(--accent-1));
  -webkit-background-clip: text; -webkit-text-fill-color: transparent;
  margin-bottom: 2px;
}
.header-titles p { font-size: 15px; color: var(--text-muted); font-weight: 500; }

.header-right { display: flex; align-items: center; gap: 24px; }
.live-badge {
  display: flex; align-items: center; gap: 8px;
  padding: 8px 16px; border-radius: 30px;
  background: rgba(16, 185, 129, 0.1); border: 1px solid rgba(16, 185, 129, 0.3);
  color: var(--success); font-weight: 600; font-size: 14px; letter-spacing: 1px;
}
.pulse-dot {
  width: 8px; height: 8px; border-radius: 50%;
  background: var(--success); box-shadow: 0 0 10px var(--success);
  animation: pulse 1.5s infinite;
}
@keyframes pulse { 0%, 100% { opacity: 1; transform: scale(1); } 50% { opacity: 0.5; transform: scale(1.5); } }
.clock { font-size: 32px; font-weight: 700; letter-spacing: 2px; color: white; display:flex; align-items:center; gap:10px; }
.clock i { color: var(--accent-1); font-size: 26px; }

/* Main Content Area */
.main-stage {
  flex: 1; position: relative; overflow: hidden; padding: 25px 40px;
}

.slide-wrapper {
  position: absolute; inset: 25px 40px;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  grid-auto-rows: calc((100vh - 90px - 70px - 50px - 40px) / 2); /* Calculate ideal height for 2 rows */
  gap: 25px;
  opacity: 0; transform: scale(0.95) translateY(20px);
  transition: all 0.6s cubic-bezier(0.25, 1, 0.5, 1);
  pointer-events: none;
}
@media (max-width: 1400px) {
    .slide-wrapper { grid-template-columns: repeat(3, 1fr); grid-auto-rows: calc((100vh - 90px - 70px - 50px - 40px) / 2); }
}

.slide-wrapper.active {
  opacity: 1; transform: scale(1) translateY(0); pointer-events: auto;
}
.slide-wrapper.exit {
  opacity: 0; transform: scale(1.05) translateY(-20px);
}

/* Card Styling */
.card {
  background: var(--glass-bg);
  backdrop-filter: blur(16px); -webkit-backdrop-filter: blur(16px);
  border: 1px solid var(--glass-border);
  border-radius: 20px;
  padding: 24px;
  display: flex; flex-direction: column;
  box-shadow: var(--glass-shadow);
  position: relative; overflow: hidden;
  transition: transform 0.3s ease, border-color 0.3s ease;
}
.card::before {
  content: ''; position: absolute; top: 0; left: 0; width: 4px; height: 100%;
  background: linear-gradient(to bottom, var(--accent-1), var(--accent-2));
  border-radius: 4px 0 0 4px;
}
.card:hover { transform: translateY(-5px); border-color: rgba(255,255,255,0.2); }

.card-header { display: flex; align-items: center; gap: 16px; margin-bottom: 20px; }

.avatar {
  width: 60px; height: 60px; flex-shrink: 0;
  border-radius: 16px;
  display: flex; align-items: center; justify-content: center;
  font-size: 22px; font-weight: 700; color: white;
  box-shadow: 0 4px 15px rgba(0,0,0,0.3);
  position: relative;
}
/* Glowing ring around avatar */
.avatar::after {
  content:''; position: absolute; inset: -3px; border-radius: 19px;
  background: inherit; filter: blur(8px); opacity: 0.5; z-index: -1;
}

.doc-info { flex: 1; overflow: hidden; }
.poli-name {
  font-size: 13px; font-weight: 600; color: var(--accent-1); 
  text-transform: uppercase; letter-spacing: 1px; margin-bottom: 4px;
  display: flex; align-items: center; gap: 6px;
}
.doc-name {
  font-size: 18px; font-weight: 700; line-height: 1.3;
  white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
}

/* Stats Area */
.stats-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 15px; margin-bottom: 15px; flex: 1; }
.stat-box {
  background: rgba(0, 0, 0, 0.2); border-radius: 12px; padding: 12px 14px;
  display: flex; flex-direction: column; justify-content: center;
  border: 1px solid rgba(255,255,255,0.03);
}
.s-label { font-size: 12px; color: var(--text-muted); font-weight: 500; margin-bottom: 4px; display:flex; align-items:center; gap:6px; }
.s-value { font-size: 20px; font-weight: 700; }
.s-value.time { font-size: 16px; color: white; }

/* Progress Bar */
.prog-container { width: 100%; margin-top: auto; }
.prog-labels { display: flex; justify-content: space-between; font-size: 13px; margin-bottom: 8px; font-weight: 600; }
.prog-bar {
  height: 8px; background: rgba(0,0,0,0.3); border-radius: 10px; overflow: hidden;
  box-shadow: inset 0 1px 3px rgba(0,0,0,0.5); position: relative;
}
.prog-fill {
  height: 100%; border-radius: 10px; transition: width 1s ease-in-out;
  position: relative; overflow: hidden;
}
.prog-fill::after {
  content:''; position: absolute; top:0; left:0; right:0; bottom:0;
  background: linear-gradient(90deg, transparent, rgba(255,255,255,0.4), transparent);
  animation: shimmer 2s infinite;
}
@keyframes shimmer { 0% { transform: translateX(-100%); } 100% { transform: translateX(100%); } }

/* Footer */
.footer {
  height: 70px; background: rgba(15, 23, 42, 0.85);
  backdrop-filter: blur(20px); -webkit-backdrop-filter: blur(20px);
  border-top: 1px solid var(--glass-border);
  display: flex; align-items: center; padding: 0 40px; justify-content: space-between;
  z-index: 10; position: relative;
}
/* Progress line at very bottom */
.footer-progress {
  position: absolute; bottom: 0; left: 0; height: 3px; background: linear-gradient(90deg, var(--accent-1), var(--accent-3)); width: 0%;
}

.running-text {
  flex: 1; overflow: hidden; display: flex; align-items: center;
  font-size: 15px; font-weight: 500; color: #cbd5e1; margin-right: 30px;
}
.rt-track { display: flex; gap: 50px; white-space: nowrap; animation: scrollText 30s linear infinite; }
.rt-item { display: flex; align-items: center; gap: 10px; }
.rt-item i { color: var(--accent-1); }
@keyframes scrollText { 0% { transform: translateX(0); } 100% { transform: translateX(-50%); } }

.pagination { display: flex; align-items: center; gap: 15px; }
.page-info { font-size: 14px; font-weight: 600; color: var(--text-muted); letter-spacing: 1px; }
.dots { display: flex; gap: 8px; }
.dot { width: 8px; height: 8px; border-radius: 50%; background: rgba(255,255,255,0.2); transition: all 0.3s; }
.dot.active { width: 24px; border-radius: 4px; background: var(--accent-1); box-shadow: 0 0 10px var(--accent-1); }

/* Empty State */
.empty-state {
  position: absolute; inset: 0; display: flex; flex-direction: column; align-items: center; justify-content: center; text-align: center;
}
.empty-icon { font-size: 80px; color: rgba(255,255,255,0.1); margin-bottom: 20px; }
.empty-state h2 { font-size: 28px; font-weight: 700; color: var(--text-main); margin-bottom: 10px; }
.empty-state p { font-size: 16px; color: var(--text-muted); }
</style>
</head>
<body>

<div class="bg-animation"></div>

<header class="header">
  <div class="header-left">
    <div class="logo-box"><i class="bi bi-hospital"></i></div>
    <div class="header-titles">
      <h1>Jadwal Praktik Dokter</h1>
      <p><?= ucfirst(strtolower($hari_indo)) ?>, <?= date('d F Y') ?> • Rawat Jalan</p>
    </div>
  </div>
  <div class="header-right">
    <div class="live-badge"><div class="pulse-dot"></div> SINKRONISASI SIMRS</div>
    <div class="clock"><i class="bi bi-clock"></i> <span id="timeDisplay">00:00:00</span></div>
  </div>
</header>

<main class="main-stage">
<?php if (empty($jadwal)): ?>
  <div class="empty-state">
    <i class="bi bi-calendar-x empty-icon"></i>
    <h2>Belum Ada Jadwal</h2>
    <p>Tidak ada jadwal praktik dokter yang tersedia untuk hari <?= $hari_indo ?>.</p>
  </div>
<?php else:
  foreach ($pages as $index => $page):
?>
  <div class="slide-wrapper <?= $index === 0 ? 'active' : '' ?>" id="slide-<?= $index ?>">
    <?php foreach ($page as $j):
      $pasien = $jumlah_pasien[$j['kd_dokter']] ?? 0;
      $kuota  = max(1, (int)$j['kuota']);
      $sisa   = max(0, $kuota - $pasien);
      $pct    = min(100, round(($pasien / $kuota) * 100));
      
      $colorFill = 'var(--success)';
      if($pct >= 90) $colorFill = 'var(--danger)';
      elseif($pct >= 70) $colorFill = 'var(--warning)';
      elseif($pct >= 40) $colorFill = 'var(--accent-1)';
      
      $initials = getInitials($j['nm_dokter']);
      $avatarColor = getAvatarColor($initials);
    ?>
    <div class="card">
      <div class="card-header">
        <div class="avatar" style="background: <?= $avatarColor ?>;"><?= $initials ?></div>
        <div class="doc-info">
          <div class="poli-name"><i class="bi bi-diagram-3"></i> <?= htmlspecialchars($j['nm_poli']) ?></div>
          <div class="doc-name"><?= htmlspecialchars($j['nm_dokter']) ?></div>
        </div>
      </div>
      
      <div class="stats-grid">
        <div class="stat-box">
          <span class="s-label"><i class="bi bi-clock-history"></i> Jam Praktik</span>
          <span class="s-value time"><?= substr($j['jam_mulai'],0,5).' - '.substr($j['jam_selesai'],0,5) ?></span>
        </div>
        <div class="stat-box">
            <?php if($sisa > 0): ?>
                <span class="s-label"><i class="bi bi-ticket-detailed"></i> Sisa Kuota</span>
                <span class="s-value" style="color: var(--success);"><?= $sisa ?> <span style="font-size:12px;color:var(--text-muted);font-weight:500;">/ <?= $kuota ?></span></span>
            <?php else: ?>
                <span class="s-label"><i class="bi bi-x-circle"></i> Status</span>
                <span class="s-value" style="color: var(--danger); font-size:16px;">PENUH</span>
            <?php endif; ?>
        </div>
      </div>
      
      <div class="prog-container">
        <div class="prog-labels">
          <span style="color: var(--text-muted);">Keterisian</span>
          <span style="color: <?= $colorFill ?>;"><?= $pasien ?> Terdaftar (<?= $pct ?>%)</span>
        </div>
        <div class="prog-bar">
          <div class="prog-fill" style="width: <?= $pct ?>%; background: <?= $colorFill ?>; box-shadow: 0 0 10px <?= $colorFill ?>;"></div>
        </div>
      </div>
    </div>
    <?php endforeach; ?>
  </div>
<?php endforeach; endif; ?>
</main>

<footer class="footer">
  <div class="running-text">
    <div class="rt-track">
      <?php for($i=0; $i<3; $i++): ?>
      <div class="rt-item"><i class="bi bi-info-circle-fill"></i> Data jadwal dan kuota diperbarui otomatis secara real-time dari sistem SIMRS.</div>
      <div class="rt-item"><i class="bi bi-suit-heart-fill" style="color: var(--accent-3);"></i> Melayani dengan Sepenuh Hati. Semoga lekas sembuh.</div>
      <div class="rt-item"><i class="bi bi-exclamation-triangle-fill" style="color: var(--warning);"></i> Harap datang 15 menit lebih awal sebelum jam praktik dokter.</div>
      <?php endfor; ?>
    </div>
  </div>
  
  <?php if ($totalPage > 1): ?>
  <div class="pagination">
    <span class="page-info">HAL <span id="pageNum">1</span> DARI <?= $totalPage ?></span>
    <div class="dots">
      <?php for($i=0; $i<$totalPage; $i++): ?>
        <div class="dot <?= $i===0 ? 'active' : '' ?>" id="dot-<?= $i ?>"></div>
      <?php endfor; ?>
    </div>
  </div>
  <?php endif; ?>
  
  <div class="footer-progress" id="footerProgress"></div>
</footer>

<script>
// Jam Digital
function updateClock() {
  const now = new Date();
  document.getElementById('timeDisplay').textContent = 
    String(now.getHours()).padStart(2, '0') + ':' + 
    String(now.getMinutes()).padStart(2, '0') + ':' + 
    String(now.getSeconds()).padStart(2, '0');
}
setInterval(updateClock, 1000);
updateClock();

// Slider Otomatis
const slides = document.querySelectorAll('.slide-wrapper');
const totalPages = <?= $totalPage ?>;
const duration = 12000; // 12 detik per halaman
let current = 0;
let progressTimer;

function showSlide(index) {
  if(totalPages <= 1) return;
  
  // Sembunyikan yang lama
  slides[current].classList.remove('active');
  slides[current].classList.add('exit');
  
  const prev = current;
  setTimeout(() => slides[prev].classList.remove('exit'), 600);
  
  // Tampilkan yang baru
  current = index;
  slides[current].classList.add('active');
  
  // Update UI Pagination
  document.getElementById('pageNum').textContent = current + 1;
  document.querySelectorAll('.dot').forEach(d => d.classList.remove('active'));
  document.getElementById('dot-' + current).classList.add('active');
  
  startProgressBar();
}

function startProgressBar() {
  const pbar = document.getElementById('footerProgress');
  pbar.style.transition = 'none';
  pbar.style.width = '0%';
  
  clearTimeout(progressTimer);
  
  requestAnimationFrame(() => {
    pbar.style.transition = `width ${duration}ms linear`;
    pbar.style.width = '100%';
  });
  
  progressTimer = setTimeout(() => {
    showSlide((current + 1) % totalPages);
  }, duration);
}

if(totalPages > 1) {
  startProgressBar();
}

// Refresh whole page every 5 minutes to get fresh database data
setTimeout(() => location.reload(), 5 * 60 * 1000);
</script>

</body>
</html>