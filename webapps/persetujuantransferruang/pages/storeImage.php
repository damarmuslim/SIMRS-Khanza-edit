<?php
    // [RM 04-1/04-2] Peran 1/4: PETUGAS PENGIRIM -> tabel bukti_transfer_petugas_pengirim.
    // Setelah sukses, lanjut ke login2.php (act=Kamera2, Petugas Penerima).
    require_once('../../conf/conf.php');
    $norawat       = validTeks4($_POST["norawat"],20);
    $tanggal_masuk = validTeks5($_POST["tanggal_masuk"],20);
    $jenis_bukti   = ($_POST["jenis_bukti"]==="Tanda Tangan") ? "Tanda Tangan" : "Foto";
    $fileName      = str_replace("/","",$norawat).str_replace(":","",str_replace("-","",str_replace(" ","",$tanggal_masuk)))."PG.jpeg";
    if(file_exists(host()."/webapps/persetujuantransferruang/pages/upload/".$fileName)){
        @unlink(host()."/webapps/persetujuantransferruang/pages/upload/".$fileName);
    }

    $img            = $_POST["image"];
    $folderPath     = "upload/";
    $image_parts    = explode(";base64,", $img);
    $image_type_aux = explode("image/", $image_parts[0]);
    $image_type     = $image_type_aux[1];
    $image_base64   = base64_decode($image_parts[1]);
    $file           = $folderPath.$fileName;
    file_put_contents($file, $image_base64);

    $urlLanjut = '../login2.php?iyem='.encrypt_decrypt('{"usere":"'.USERHYBRIDWEB.'","passwordte":"'.PASHYBRIDWEB.'"}','e');
    $htmlHead  = "<head><title>SIMKES Khanza</title><link rel='stylesheet' href='../css/bootstrap.min.css'/><style>#results { padding:0px; background:#EEFFEE; width:490; height:390 }</style></head>";

    try {
        Tambah3("bukti_transfer_petugas_pengirim","'".$norawat."','".$tanggal_masuk."','pages/upload/$fileName','".$jenis_bukti."'");
        echo $htmlHead."<body><center>Bukti Petugas Pengirim tersimpan. Lanjutkan ke peran berikutnya (Petugas Penerima) ..!! <br><a href='".$urlLanjut."' class='btn btn-secondary'>Lanjut</a></center></body>";
    } catch (mysqli_sql_exception $e) {
        if($e->getCode() == 1062){
            echo $htmlHead."<body><center>Gagal, kemungkinan bukti Petugas Pengirim sudah pernah diambil sebelumnya ..!! <br><a href='".$urlLanjut."' class='btn btn-secondary'>Lanjut</a></center></body>";
        } else {
            echo $htmlHead."<body><center>Gagal, silahkan ulangi ..!! <br><a href='javascript:history.back()' class='btn btn-secondary'>Kembali</a></center></body>";
        }
    }
?>
