<?php
    // [RM 32] Pintu masuk halaman tanda tangan. Dibuka sekali di tablet
    // ruangan, sesudah itu tablet cukup menekan Refresh setiap kali perawat
    // mengirim baris catatan baru dari aplikasi desktop.
    include_once "conf/command.php";
    require_once('../conf/conf.php');

    $cari = trim(isset($_GET['iyem'])) ? trim($_GET['iyem']) : NULL;
    $cari = json_decode(encrypt_decrypt($cari,"d"), true);
    $url  = "index.php?act=Home";
    if (isset($cari["usere"])) {
        if (($cari["usere"]==USERHYBRIDWEB) && ($cari["passwordte"]==PASHYBRIDWEB)) {
            session_start();
            $_SESSION['ses_admin_catatanperawatbidan'] = "admin";
            $url = "index.php?act=Ttd";
        }
    }
    header("Location:".$url);
?>
