<?php
    // [RM 32] Menyimpan gambar tanda tangan pasien/keluarga untuk SATU baris
    // catatan perawat/bidan.
    //
    // Yang disimpan ke database hanyalah PATH berkasnya, bukan gambarnya -
    // pola yang sama dengan bukti_transfer_* milik RM 04. Path ditulis relatif
    // ("pages/upload/xxx.jpeg") supaya aplikasi desktop bisa merangkainya jadi
    // URL penuh saat mencetak, tanpa perlu tahu di mana webapp dipasang.
    require_once('../../conf/conf.php');

    $norawat = validTeks4($_POST["norawat"],20);
    $tanggal = validTeks5($_POST["tanggal"],20);
    $jam     = validTeks5($_POST["jam"],20);

    $htmlHead = "<head><title>SIMKES Khanza</title>"
              . "<meta name='viewport' content='width=device-width, initial-scale=1'/>"
              . "<link rel='stylesheet' href='../css/bootstrap.min.css'/></head>";
    $kembali  = "<br/><br/><a href='../index.php?act=Ttd' class='btn btn-secondary'>Kembali</a>";

    if ($norawat=="" || $tanggal=="" || $jam=="") {
        exit($htmlHead."<body><center>Data baris catatan tidak lengkap, silahkan perawat mengirim ulang.".$kembali."</center></body>");
    }

    $img = isset($_POST["image"]) ? $_POST["image"] : "";
    if (strpos($img, ";base64,") === false) {
        exit($htmlHead."<body><center>Tanda tangan kosong atau gagal terkirim, silahkan ulangi.".$kembali."</center></body>");
    }

    // Nama berkas dirakit dari KETIGA bagian kunci catatan, bukan dari no_rawat
    // saja: satu pasien punya banyak baris catatan, dan kalau namanya hanya
    // berdasarkan no_rawat maka tanda tangan baris berikutnya akan MENIMPA
    // tanda tangan baris sebelumnya.
    $fileName = str_replace("/","",$norawat)
              . str_replace("-","",$tanggal)
              . str_replace(":","",$jam)
              . ".jpeg";

    $folderPath = __DIR__ . DIRECTORY_SEPARATOR . "upload" . DIRECTORY_SEPARATOR;
    if (!is_dir($folderPath)) {
        @mkdir($folderPath, 0777, true);
    }
    if (file_exists($folderPath.$fileName)) {
        // Tanda tangan ulang menggantikan yang lama - pasien memang boleh
        // memperbaiki tanda tangannya sendiri.
        @unlink($folderPath.$fileName);
    }

    $image_parts  = explode(";base64,", $img);
    $image_base64 = base64_decode($image_parts[1]);
    if ($image_base64 === false || strlen($image_base64) < 100) {
        exit($htmlHead."<body><center>Gambar tanda tangan tidak terbaca, silahkan ulangi.".$kembali."</center></body>");
    }
    file_put_contents($folderPath.$fileName, $image_base64);

    $pathDb = "pages/upload/".$fileName;

    try {
        Ubah2("catatan_keperawatan_ranap",
              "ttd_pasien='".$pathDb."' "
            . "where no_rawat='".$norawat."' and tanggal='".$tanggal."' and jam='".$jam."'");

        // Antrian dikosongkan supaya tablet tidak menampilkan permintaan yang
        // sudah selesai, dan supaya tanda tangan berikutnya tidak salah sasaran.
        HapusAll("antricatatanperawatbidan");

        echo $htmlHead."<body><center><br/><h5>Tanda tangan tersimpan. Terima kasih.</h5>"
           . "<p class='text-muted'>Silahkan serahkan kembali perangkat ini kepada petugas.</p>"
           . "<a href='../index.php?act=Ttd' class='btn btn-secondary'>Selesai</a></center></body>";
    } catch (mysqli_sql_exception $e) {
        echo $htmlHead."<body><center>Gagal menyimpan tanda tangan, silahkan ulangi.".$kembali."</center></body>";
    }
?>
