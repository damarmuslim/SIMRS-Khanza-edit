<?php
    // [RM 04-1/04-2] Gerbang masuk ke Kamera2 (Petugas Penerima). Tidak perlu
    // meneruskan no_rawat/tanggal_masuk lewat parameter URL spt pola pulangaps
    // - webapp ini sudah punya tabel antrian persisten
    // (antripersetujuantransferantarruang) yg dibaca ulang langsung oleh
    // kamera2.php, jadi cukup verifikasi kredensial saja di sini.
    include_once "conf/command.php";
    require_once('../conf/conf.php');

    $cari    = trim(isset($_GET['iyem']))?trim($_GET['iyem']):NULL;
    $cari    = json_decode(encrypt_decrypt($cari,"d"),true);
    $url     = "index.php?act=Home";
    if (isset($cari["usere"])) {
        if(($cari["usere"]==USERHYBRIDWEB)&&($cari["passwordte"]==PASHYBRIDWEB)){
            session_start();
            $_SESSION['ses_admin_transferruang']="admin";
            $url = "index.php?act=Kamera2";
        }
    }
    header("Location:".$url);
?>
