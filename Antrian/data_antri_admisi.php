<?php
session_start();
include 'koneksi2.php';
date_default_timezone_set('Asia/Jakarta');

if (!isset($_SESSION['user_id'])) {
    header('Location: login.php');
    exit;
}
$nama = $_SESSION['nama'] ?? 'Pengguna';

$tglStart = date('Y-m-d') . ' 00:00:00';
$tglEnd   = date('Y-m-d') . ' 23:59:59';

// Cek jumlah antrian hari ini untuk reset AUTO_INCREMENT (versi ringan)
try {
    $check_today = $pdo_simrs->prepare("
        SELECT COUNT(*) FROM antrian_wira
        WHERE created_at BETWEEN ? AND ?
          AND jenis != 'FARMASI'
    ");
    $check_today->execute([$tglStart, $tglEnd]);
    if ($check_today->fetchColumn() == 0) {
        $pdo_simrs->exec("ALTER TABLE antrian_wira AUTO_INCREMENT = 1");
    }
} catch (PDOException $e) {
    // Abaikan error reset — tidak kritis
}

$limit = 20;
$page = isset($_GET['page']) && is_numeric($_GET['page']) ? (int)$_GET['page'] : 1;
$offset = ($page - 1) * $limit;

try {
    $count_stmt = $pdo_simrs->prepare("
        SELECT COUNT(*) FROM antrian_wira
        WHERE created_at BETWEEN ? AND ?
          AND jenis != 'FARMASI'
    ");
    $count_stmt->execute([$tglStart, $tglEnd]);
    $total = $count_stmt->fetchColumn();
    $total_pages = ceil($total / $limit);
} catch (PDOException $e) {
    die("Gagal menghitung data: " . $e->getMessage());
}

try {
    $stmt = $pdo_simrs->prepare("
        SELECT a.*, l.nama_loket
        FROM antrian_wira a
        LEFT JOIN loket_admisi_wira l ON a.loket_id = l.id
        WHERE a.created_at BETWEEN ? AND ?
          AND a.jenis != 'FARMASI'
        ORDER BY a.created_at ASC
        LIMIT $limit OFFSET $offset
    ");
    $stmt->execute([$tglStart, $tglEnd]);
    $antrian = $stmt->fetchAll(PDO::FETCH_ASSOC);

    $status_stmt = $pdo_simrs->prepare("
        SELECT
            SUM(CASE WHEN status='Menunggu'  THEN 1 ELSE 0 END) AS menunggu,
            SUM(CASE WHEN status='Dipanggil' THEN 1 ELSE 0 END) AS dipanggil,
            SUM(CASE WHEN status='Selesai'   THEN 1 ELSE 0 END) AS selesai
        FROM antrian_wira
        WHERE created_at BETWEEN ? AND ?
          AND jenis != 'FARMASI'
    ");
    $status_stmt->execute([$tglStart, $tglEnd]);
    $status_count = $status_stmt->fetch(PDO::FETCH_ASSOC);
    $menunggu  = $status_count['menunggu']  ?? 0;
    $dipanggil = $status_count['dipanggil'] ?? 0;
    $selesai   = $status_count['selesai']   ?? 0;

    $loket_stmt = $pdo_simrs->query("SELECT id, nama_loket FROM loket_admisi_wira ORDER BY id ASC");
    $daftar_loket = $loket_stmt->fetchAll(PDO::FETCH_ASSOC);
} catch (PDOException $e) {
    die("Gagal mengambil data antrian: " . $e->getMessage());
}

// Set page title dan extra CSS
$page_title = 'Data Antrian Admisi - MediFix';
$extra_css = '
/* Welcome Box - SAMA seperti dashboard.php */
.welcome-box {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 5px;
  padding: 25px;
  margin-bottom: 20px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
}

.welcome-box h3 {
  margin: 0 0 10px 0;
  font-size: 24px;
  font-weight: 700;
}

.welcome-box p {
  margin: 0;
  opacity: 0.9;
  font-size: 14px;
}

.welcome-box .date-time {
  margin-top: 10px;
  font-size: 13px;
  opacity: 0.8;
}

/* Stats Cards - Style seperti menu cards */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 20px;
}

.stat-card {
  background: #fff;
  border-radius: 5px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.12);
  transition: all 0.3s ease;
  border-top: 3px solid;
  overflow: hidden;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 15px rgba(0,0,0,0.2);
}

.stat-card-content {
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 15px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 30px;
  color: white;
}

.stat-info {
  flex: 1;
}

.stat-label {
  font-size: 13px;
  color: #666;
  margin-bottom: 5px;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #333;
}

/* Color variations */
.stat-total {
  border-top-color: #3c8dbc;
}
.stat-total .stat-icon {
  background: #3c8dbc;
}

.stat-menunggu {
  border-top-color: #f39c12;
}
.stat-menunggu .stat-icon {
  background: #f39c12;
}

.stat-dipanggil {
  border-top-color: #00c0ef;
}
.stat-dipanggil .stat-icon {
  background: #00c0ef;
}

.stat-selesai {
  border-top-color: #00a65a;
}
.stat-selesai .stat-icon {
  background: #00a65a;
}

@media (max-width: 992px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 576px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
}
';

$extra_js = '
function updateClock() {
    const now = new Date();
    const options = { weekday: "long", year: "numeric", month: "long", day: "numeric" };
    const tanggal = now.toLocaleDateString("id-ID", options);
    const waktu = now.toLocaleTimeString("id-ID");
    
    document.getElementById("tanggalSekarang").innerHTML = tanggal;
    document.getElementById("clockDisplay").innerHTML = waktu;
}

setInterval(updateClock, 1000);
updateClock();

function panggilAntrian(id, nomor) {
    const btn = document.getElementById("btn" + id);
    if (btn) {
        btn.disabled = true;
        btn.innerHTML = "<i class=\"fa fa-spinner fa-spin\"></i> Memanggil...";
    }

    const loketEl = document.getElementById("loket" + id);
    const loket = loketEl ? loketEl.value : "";

    fetch("panggil_antrian.php", {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: `id=${encodeURIComponent(id)}&loket_id=${encodeURIComponent(loket)}&ulang=0`
    })
    .then(res => res.text())
    .then(() => {
        // Suara diputar otomatis di halaman display_admisi.php
        setTimeout(() => location.reload(), 500);
    })
    .catch(err => {
        console.error("❌ Error:", err);
        if (btn) {
            btn.disabled = false;
            btn.innerHTML = "<i class=\"fa fa-phone\"></i> Panggil";
        }
    });
}

function simpanRM(id) {
    const btnSave = document.getElementById("btnSave" + id);
    if (btnSave) {
        btnSave.disabled = true;
        btnSave.innerHTML = "<i class=\"fa fa-spinner fa-spin\"></i> Selesai...";
    }

    fetch("simpan_rm.php", {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: `id=${encodeURIComponent(id)}&rm=`
    })
    .then(res => res.text())
    .then(() => {
        Swal.fire({
            icon: "success",
            title: "Berhasil",
            text: "Antrean telah diselesaikan!",
            timer: 1500,
            showConfirmButton: false
        }).then(() => {
            location.reload();
        });
    })
    .catch(err => {
        console.error("❌ Error:", err);
        Swal.fire({
            icon: "error",
            title: "Gagal",
            text: "Terjadi kesalahan saat menyelesaikan antrean."
        });
        if (btnSave) {
            btnSave.disabled = false;
            btnSave.innerHTML = "<i class=\"fa fa-check\"></i> Selesai";
        }
    });
}

function panggilUlang(id, nomor, loket) {
    const btn = document.getElementById("btnRecall" + id);
    if (btn) {
        btn.disabled = true;
        btn.innerHTML = "<i class=\"fa fa-spinner fa-spin\"></i> Memanggil...";
    }

    fetch("panggil_antrian.php", {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: `id=${encodeURIComponent(id)}&loket_id=${encodeURIComponent(loket)}&ulang=1`
    })
    .then(res => res.text())
    .then(() => {
        // Suara diputar otomatis di halaman display_admisi.php
        if (btn) {
            btn.disabled = false;
            btn.innerHTML = "<i class=\"fa fa-repeat\"></i> Ulang";
        }
    })
    .catch(err => {
        console.error("❌ Error:", err);
        if (btn) {
            btn.disabled = false;
            btn.innerHTML = "<i class=\"fa fa-repeat\"></i> Ulang";
        }
    });
}
';

// Include header
include 'includes/header.php';

// Include sidebar
include 'includes/sidebar.php';
?>

  <div class="content-wrapper">
    <section class="content-header">
      <h1>Data Antrian Admisi</h1>
      <ol class="breadcrumb">
        <li><a href="dashboard.php"><i class="fa fa-dashboard"></i> Home</a></li>
        <li><a href="#">Admisi</a></li>
        <li class="active">Data Antrian</li>
      </ol>
    </section>

    <section class="content">
      
     

      <!-- Stats Cards -->
      <div class="stats-grid">
        <div class="stat-card stat-total">
          <div class="stat-card-content">
            <div class="stat-icon">
              <i class="fa fa-list-ol"></i>
            </div>
            <div class="stat-info">
              <div class="stat-label">Total Antrian</div>
              <div class="stat-value"><?= $total ?></div>
            </div>
          </div>
        </div>

        <div class="stat-card stat-menunggu">
          <div class="stat-card-content">
            <div class="stat-icon">
              <i class="fa fa-hourglass-half"></i>
            </div>
            <div class="stat-info">
              <div class="stat-label">Menunggu</div>
              <div class="stat-value"><?= $menunggu ?></div>
            </div>
          </div>
        </div>

        <div class="stat-card stat-dipanggil">
          <div class="stat-card-content">
            <div class="stat-icon">
              <i class="fa fa-phone"></i>
            </div>
            <div class="stat-info">
              <div class="stat-label">Dipanggil</div>
              <div class="stat-value"><?= $dipanggil ?></div>
            </div>
          </div>
        </div>

        <div class="stat-card stat-selesai">
          <div class="stat-card-content">
            <div class="stat-icon">
              <i class="fa fa-check-circle"></i>
            </div>
            <div class="stat-info">
              <div class="stat-label">Selesai</div>
              <div class="stat-value"><?= $selesai ?></div>
            </div>
          </div>
        </div>
      </div>

      <!-- Table Section -->
      <div class="row">
        <div class="col-xs-12">
          <div class="box">
            <div class="box-header">
              <h3 class="box-title">Daftar Antrian (<?= count($antrian) ?> dari <?= $total ?>)</h3>
            </div>
            <div class="box-body">
              
              <?php if ($total > 0): ?>
              <div class="table-responsive">
                <table class="table table-bordered table-striped table-hover">
                  <thead style="background: #3c8dbc; color: white;">
                    <tr>
                      <th width="30">No</th>
                      <th width="80">No. Antrian</th>
                      <th width="100">No. RM</th>
                      <th width="80">Status</th>
                      <th width="120">Waktu Ambil</th>
                      <th width="100">Waktu Panggil</th>
                      <th width="120">Loket</th>
                      <th>Aksi</th>
                    </tr>
                  </thead>
                  <tbody>
                    <?php 
                    $no = $offset + 1; 
                    foreach ($antrian as $row): 
                        $statusClass = ['Menunggu'=>'warning','Dipanggil'=>'info','Selesai'=>'success'][$row['status']] ?? 'default';
                    ?>
                    <tr data-id="<?= $row['id']; ?>">
                      <td class="text-center"><?= $no++; ?></td>
                      <td class="text-center">
                        <strong style="color: #3c8dbc; font-size: 13px;">
                          <?= htmlspecialchars($row['nomor']); ?>
                        </strong>
                      </td>
                      <td class="text-center no-rm-cell" data-rm="<?= htmlspecialchars($row['no_rkm_medis'] ?? '') ?>">
                        <strong>
                          <?= !empty($row['no_rkm_medis']) ? htmlspecialchars($row['no_rkm_medis']) : '-' ?>
                        </strong>
                      </td>
                      <td class="text-center">
                        <span class="label label-<?= $statusClass ?>">
                          <?= htmlspecialchars($row['status']); ?>
                        </span>
                      </td>
                      <td class="text-center" style="font-size: 11px;">
                        <?= date('d-m-Y H:i', strtotime($row['created_at'])); ?>
                      </td>
                      <td class="text-center" style="font-size: 11px;">
                        <?= $row['waktu_panggil'] ? date('H:i:s', strtotime($row['waktu_panggil'])) : '-'; ?>
                      </td>
                      <td class="text-center">
                        <?php if (!empty($row['nama_loket'])): ?>
                          <span class="label label-primary"><?= htmlspecialchars($row['nama_loket']); ?></span>
                        <?php else: ?>
                          <select id="loket<?= $row['id']; ?>" class="form-control input-sm">
                            <?php foreach ($daftar_loket as $l): ?>
                              <option value="<?= $l['id']; ?>"><?= htmlspecialchars($l['nama_loket']); ?></option>
                            <?php endforeach; ?>
                          </select>
                        <?php endif; ?>
                      </td>
                      <td class="text-center">
                        <?php if ($row['status'] == 'Menunggu'): ?>
                          <button class="btn btn-warning btn-sm" id="btn<?= $row['id']; ?>" 
                                  onclick="panggilAntrian('<?= $row['id']; ?>','<?= $row['nomor']; ?>')">
                            <i class="fa fa-phone"></i> Panggil
                          </button>
                        <?php elseif ($row['status'] == 'Dipanggil'): ?>
                          <div class="btn-group">
                            <button class="btn btn-success btn-sm" id="btnSave<?= $row['id']; ?>" 
                                    onclick="simpanRM('<?= $row['id']; ?>')">
                              <i class="fa fa-check"></i> Selesai
                            </button>
                            <button class="btn btn-info btn-sm" id="btnRecall<?= $row['id']; ?>" 
                                    onclick="panggilUlang('<?= $row['id']; ?>','<?= $row['nomor']; ?>','<?= $row['loket_id']; ?>')">
                              <i class="fa fa-repeat"></i> Ulang
                            </button>
                          </div>
                        <?php else: ?>
                          <button class="btn btn-default btn-sm" disabled>
                            <i class="fa fa-check-circle"></i> Selesai
                          </button>
                        <?php endif; ?>
                      </td>
                    </tr>
                    <?php endforeach; ?>
                  </tbody>
                </table>
              </div>

              <?php if ($total_pages > 1): ?>
              <div class="box-footer clearfix">
                <ul class="pagination pagination-sm no-margin pull-right">
                  <li <?= ($page <= 1) ? 'class="disabled"' : '' ?>>
                    <a href="?page=<?= $page - 1 ?>">«</a>
                  </li>
                  <?php for ($i = 1; $i <= $total_pages; $i++): ?>
                    <li <?= ($i == $page) ? 'class="active"' : '' ?>>
                      <a href="?page=<?= $i ?>"><?= $i ?></a>
                    </li>
                  <?php endfor; ?>
                  <li <?= ($page >= $total_pages) ? 'class="disabled"' : '' ?>>
                    <a href="?page=<?= $page + 1 ?>">»</a>
                  </li>
                </ul>
              </div>
              <?php endif; ?>

              <?php else: ?>
              <div class="callout callout-info">
                <h4><i class="fa fa-info"></i> Informasi</h4>
                <p>Belum ada antrian admisi untuk hari ini</p>
              </div>
              <?php endif; ?>
              
            </div>
          </div>
        </div>
      </div>

    </section>
  </div>

<?php
// Include footer
include 'includes/footer.php';
?>