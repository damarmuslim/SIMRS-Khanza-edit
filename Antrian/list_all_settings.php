<?php
session_start();
if (!isset($_SESSION['user_id'])) {
    http_response_code(401);
    exit('Unauthorized');
}
include 'koneksi.php';
try {
    $stmt = $pdo->query("SELECT * FROM setting_simrs");
    $rows = $stmt->fetchAll(PDO::FETCH_ASSOC);
    echo "<h3>Daftar Semua Setting SIMRS di Lokal:</h3>";
    echo "<table border='1' cellpadding='5'>";
    echo "<tr><th>ID</th><th>Nama SIMRS</th><th>Host</th><th>DB Name</th></tr>";
    foreach($rows as $r) {
        echo "<tr><td>{$r['id']}</td><td>{$r['nama_simrs']}</td><td>{$r['host']}</td><td>{$r['database_name']}</td></tr>";
    }
    echo "</table>";
} catch (Exception $e) {
    echo "Error: " . $e->getMessage();
}
?>
