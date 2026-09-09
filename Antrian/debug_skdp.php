<?php
session_start();
if (!isset($_SESSION['user_id'])) {
    http_response_code(401);
    exit('Unauthorized');
}
include 'koneksi2.php';

echo "<h3>Sample data from skdp_bpjs:</h3>";
try {
    $stmt = $pdo_simrs->query("SELECT * FROM skdp_bpjs LIMIT 5");
    $data = $stmt->fetchAll(PDO::FETCH_ASSOC);
    echo "<pre>";
    print_r($data);
    echo "</pre>";
} catch (Exception $e) {
    echo "Error: " . $e->getMessage() . "<br>";
}
?>
