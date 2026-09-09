<?php
session_start();
if (!isset($_SESSION['user_id'])) {
    http_response_code(401);
    exit('Unauthorized');
}
include 'koneksi2.php';

echo "<h3>Sample data from bridging_surat_kontrol_bpjs:</h3>";
try {
    $stmt = $pdo_simrs->query("SELECT * FROM bridging_surat_kontrol_bpjs LIMIT 5");
    $data = $stmt->fetchAll(PDO::FETCH_ASSOC);
    echo "<pre>";
    print_r($data);
    echo "</pre>";
} catch (Exception $e) {
    echo "Error: " . $e->getMessage() . "<br>";
}

echo "<h3>Checking join bridging_surat_kontrol_bpjs with bridging_sep:</h3>";
try {
    $stmt = $pdo_simrs->query("
        SELECT sk.no_sep, sk.no_surat, sep.no_sep as sep_exists 
        FROM bridging_surat_kontrol_bpjs sk 
        LEFT JOIN bridging_sep sep ON sk.no_sep = sep.no_sep 
        LIMIT 5
    ");
    $data = $stmt->fetchAll(PDO::FETCH_ASSOC);
    echo "<pre>";
    print_r($data);
    echo "</pre>";
} catch (Exception $e) {
    echo "Error: " . $e->getMessage() . "<br>";
}
?>
