<?php
session_start();
include 'koneksi2.php';

if (!isset($_SESSION['user_id'])) {
    http_response_code(401);
    echo json_encode(['status' => 'error', 'message' => 'Unauthorized']);
    exit;
}

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    echo json_encode(['status' => 'error', 'message' => 'Invalid request method']);
    exit;
}

$no_rawat = $_POST['no_rawat'] ?? '';
$kode = $_POST['kode'] ?? '';
$type = $_POST['type'] ?? ''; // 'diagnosa' or 'prosedur'
$action = $_POST['action'] ?? 'save';

if (empty($no_rawat) || empty($kode) || empty($type)) {
    echo json_encode(['status' => 'error', 'message' => 'Missing required parameters']);
    exit;
}

try {
    if ($action === 'delete') {
        if ($type === 'diagnosa') {
            $stmt = $pdo_simrs->prepare("DELETE FROM diagnosa_pasien WHERE no_rawat = ? AND kd_penyakit = ?");
            $stmt->execute([$no_rawat, $kode]);
        } else if ($type === 'prosedur') {
            $stmt = $pdo_simrs->prepare("DELETE FROM prosedur_pasien WHERE no_rawat = ? AND kode = ?");
            $stmt->execute([$no_rawat, $kode]);
        }
    } else {
        if ($type === 'diagnosa') {
            // Cek prioritas terakhir
            $stmt = $pdo_simrs->prepare("SELECT MAX(prioritas) FROM diagnosa_pasien WHERE no_rawat = ?");
            $stmt->execute([$no_rawat]);
            $max_prio = $stmt->fetchColumn() ?: 0;
            $new_prio = $max_prio + 1;

            $stmt_save = $pdo_simrs->prepare("INSERT INTO diagnosa_pasien (no_rawat, kd_penyakit, status, prioritas) VALUES (?, ?, 'Jalan', ?)");
            $stmt_save->execute([$no_rawat, $kode, $new_prio]);
        } else if ($type === 'prosedur') {
            // Cek prioritas terakhir
            $stmt = $pdo_simrs->prepare("SELECT MAX(prioritas) FROM prosedur_pasien WHERE no_rawat = ?");
            $stmt->execute([$no_rawat]);
            $max_prio = $stmt->fetchColumn() ?: 0;
            $new_prio = $max_prio + 1;

            $stmt_save = $pdo_simrs->prepare("INSERT INTO prosedur_pasien (no_rawat, kode, status, prioritas) VALUES (?, ?, 'Jalan', ?)");
            $stmt_save->execute([$no_rawat, $kode, $new_prio]);
        }
    }

    echo json_encode(['status' => 'success', 'message' => 'Data berhasil disimpan']);
} catch (Exception $e) {
    echo json_encode(['status' => 'error', 'message' => $e->getMessage()]);
}
?>
