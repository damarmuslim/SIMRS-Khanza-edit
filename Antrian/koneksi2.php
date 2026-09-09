<?php
include 'koneksi.php'; // koneksi utama

try {
    // Ambil konfigurasi SIMRS
    $stmt = $pdo->query("SELECT * FROM setting_simrs LIMIT 1");
    $cfg  = $stmt->fetch(PDO::FETCH_ASSOC);

    if (!$cfg) {
        die("<b style='color:red'>⚠ Konfigurasi SIMRS belum diatur. Silakan buka menu Setting SIMRS terlebih dahulu.</b>");
    }

    $host2 = $cfg['host'];
    $user2 = $cfg['username'];
    $pass2 = $cfg['password'];
    $db2   = $cfg['database_name'];

    // Buat koneksi SIMRS dengan persistent connection
    // Mengurangi overhead buka/tutup koneksi pada setiap request polling
    $pdo_simrs = new PDO(
        "mysql:host=$host2;dbname=$db2;charset=utf8",
        $user2,
        $pass2,
        [
            PDO::ATTR_PERSISTENT => true,
            PDO::ATTR_EMULATE_PREPARES => false,
        ]
    );

    $pdo_simrs->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);

    /**
     * Menormalisasi nama pasien (mengganti singkatan gelar/panggilan)
     * Agar suara TTS lebih enak didengar.
     */
    function normalizeNamaPasien($nama) {
        if (empty($nama)) return $nama;
        $awal    = array("AN.","TN.","BY.","NY.","NN.","BN","Bin","BT","Binti", "AN", "TN", "BY", "NY", "NN");
        $replace = array("Anak","Tuan","Bayi","Nyonya","Nona","Bin","Bin","Binti","Binti", "Anak", "Tuan", "Bayi", "Nyonya", "Nona");
        return str_replace($awal, $replace, $nama);
    }

} catch (PDOException $e) {
    error_log("koneksi2.php: koneksi SIMRS gagal: " . $e->getMessage());
    die("<b style='color:red'>❌ Koneksi SIMRS gagal. Hubungi admin sistem.</b>");
}
?>
