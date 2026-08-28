<?php
    // [RM 32] Router webapp tanda tangan pasien/keluarga untuk lembar
    // CATATAN PERAWAT/BIDAN. Mengikuti pola webapp persetujuan yang sudah ada
    // (persetujuantransferruang dkk), disederhanakan: hanya SATU peran
    // (pasien/keluarga) dan hanya canvas tanda tangan - tanpa webcam.
    if(strpos($_SERVER['REQUEST_URI'],"conf")){
        exit(header("Location:../index.php"));
    }

    function title(){
        $judul = "SIMKES Khanza";
        $judul = preg_replace("[^A-Za-z0-9_\-\./,|]"," ",$judul);
        $judul = str_replace(array('.','-','/',',')," ",$judul);
        $judul = trim($judul);
        echo "$judul";
    }

    // Nama sesi SENGAJA berbeda dari webapp lain supaya login di satu webapp
    // tidak diam-diam memberi akses ke webapp yang lain.
    function cekSessiAdmin() {
        return isset($_SESSION['ses_admin_catatanperawatbidan']);
    }

    function cekUser() {
        return isset($_SESSION['ses_admin_catatanperawatbidan']);
    }

    function adminAktif() {
        if (cekSessiAdmin()) {
            return $_SESSION['ses_admin_catatanperawatbidan'];
        }
    }

    function isGuest() {
        return !cekSessiAdmin();
    }

    function formProtek() {
        $aksi = isset($_GET['act']) ? $_GET['act'] : NULL;
        if (!cekUser()) {
            $form = array('Ttd');
            foreach ($form as $page) {
                if ($aksi == $page) {
                    echo "<META HTTP-EQUIV = 'Refresh' Content = '0; URL = ?act=Home'>";
                    exit;
                }
            }
        }
    }

    function actionPages() {
        $aksi = isset($_REQUEST['act']) ? $_REQUEST['act'] : NULL;
        formProtek();
        switch ($aksi) {
            case 'Home' : include_once('pages/index.php'); break;
            case 'Ttd'  : include_once('pages/ttd.php');   break;
            default     : include_once('pages/index.php');
        }
    }
?>
