<?php
session_start();
if (!isset($_SESSION['user_id'])) {
    http_response_code(401);
    exit('Unauthorized');
}
include 'koneksi2.php';

try {
    echo "<h3>Tables with 'jkn' or 'mobile' in their name:</h3>";
    $stmt = $pdo_simrs->query("SHOW TABLES LIKE '%jkn%'");
    // Note: I need to check the actual DB name from koneksi2.php to be sure about 'Tables_in_...'
    // But SHOW TABLES usually works without the 'In' part if we're already connected.
    $stmt = $pdo_simrs->query("SHOW TABLES LIKE '%jkn%'");
    while($row = $stmt->fetch(PDO::FETCH_NUM)) {
        echo $row[0] . "<br>";
    }
    $stmt = $pdo_simrs->query("SHOW TABLES LIKE '%mobile%'");
    while($row = $stmt->fetch(PDO::FETCH_NUM)) {
        echo $row[0] . "<br>";
    }

    echo "<h3>Columns in reg_periksa related to status or JKN:</h3>";
    $stmt = $pdo_simrs->query("SHOW COLUMNS FROM reg_periksa");
    while($row = $stmt->fetch(PDO::FETCH_ASSOC)) {
        if (stripos($row['Field'], 'status') !== false || stripos($row['Field'], 'jkn') !== false || stripos($row['Field'], 'booking') !== false) {
            echo $row['Field'] . " (" . $row['Type'] . ")<br>";
        }
    }
} catch (Exception $e) {
    echo "Error: " . $e->getMessage();
}
?>
