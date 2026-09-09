<?php
session_start();
if (!isset($_SESSION['user_id'])) {
    http_response_code(401);
    exit('Unauthorized');
}
include 'koneksi2.php';

$no_rawat = '2026/03/12/000001';

try {
    // Check specific patient from reg_periksa
    $no_rawat = '2026/03/12/000001';
    echo "<h3>Details for No Rawat: $no_rawat</h3>";
    $stmtReg = $pdo_simrs->prepare("SELECT r.*, ps.nm_pasien FROM reg_periksa r JOIN pasien ps ON r.no_rkm_medis = ps.no_rkm_medis WHERE r.no_rawat = ?");
    $stmtReg->execute([$no_rawat]);
    $reg = $stmtReg->fetch(PDO::FETCH_ASSOC);
    if ($reg) {
        echo "<pre>";
        print_r($reg);
        echo "</pre>";
    } else {
        echo "Registration not found.";
    }

    // List ALL records for inspection
    echo "<h3>All Records in referensi_mobilejkn_bpjs</h3>";
    $stmtAll = $pdo_simrs->query("SELECT * FROM referensi_mobilejkn_bpjs");
    $allRows = $stmtAll->fetchAll(PDO::FETCH_ASSOC);
    
    if ($allRows) {
        echo "<table border='1' cellpadding='5' style='font-size:11px;'>";
        echo "<tr><th>No. Booking</th><th>No. Rawat</th><th>No. RM (Norm)</th><th>Nama Pasien (from table)</th><th>Tgl Periksa</th><th>Status</th></tr>";
        foreach ($allRows as $r) {
            echo "<tr>
                <td>{$r['nobooking']}</td>
                <td>{$r['no_rawat']}</td>
                <td>{$r['norm']}</td>
                <td>" . ($r['nm_pasien'] ?? '-') . "</td>
                <td>{$r['tanggalperiksa']}</td>
                <td>{$r['status']}</td>
            </tr>";
        }
        echo "</table>";
    } else {
        echo "Table is empty.";
    }
} catch (Exception $e) {
    echo "Error: " . $e->getMessage();
}
?>
