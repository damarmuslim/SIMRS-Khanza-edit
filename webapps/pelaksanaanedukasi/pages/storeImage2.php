<?php
    // Peran 2/2: PEMBERI EDUKASI -> tabel bukti_pelaksanaan_edukasi_pemberi
    // (tabel baru, terpisah dari bukti_pelaksanaan_informasi_edukasi milik
    // Peran 1). Setelah sukses, KEMBALI ke login.php (menutup putaran - 2
    // peran saja di sini, beda dari persetujuantransferruang yg 4 peran).
    // Nama file diberi akhiran "PE" (Pemberi Edukasi) spy tidak menimpa file
    // foto/ttd milik Peran 1 yg basis nama filenya sama (norawat+tanggal).
    require_once('../../conf/conf.php');
    $norawat     = validTeks4($_POST["norawat"],20);
    $tanggal     = validTeks5($_POST["tanggal"],20);
    $jenis_bukti = ($_POST["jenis_bukti"]==="Tanda Tangan") ? "Tanda Tangan" : "Foto";
    $fileName    = str_replace("/","",$norawat).str_replace(":","",str_replace("-","",str_replace(" ","",$tanggal)))."PE.jpeg";
    if(file_exists(host()."/webapps/pelaksanaanedukasi/pages/upload/".$fileName)){
        @unlink(host()."/webapps/pelaksanaanedukasi/pages/upload/".$fileName);
    }

    $img            = $_POST["image"];
    $folderPath     = "upload/";
    $image_parts    = explode(";base64,", $img);
    $image_type_aux = explode("image/", $image_parts[0]);
    $image_type     = $image_type_aux[1];
    $image_base64   = base64_decode($image_parts[1]);
    $file           = $folderPath.$fileName;
    file_put_contents($file, $image_base64);

    $urlKembali = '../login.php?iyem='.encrypt_decrypt('{"usere":"'.USERHYBRIDWEB.'","passwordte":"'.PASHYBRIDWEB.'"}','e');
    $htmlHead   = "<head><title>SIMKES Khanza</title><link rel='stylesheet' href='../css/bootstrap.min.css'/><style>#results { padding:0px; background:#EEFFEE; width:490; height:390 }</style></head>";

    try {
        Tambah3("bukti_pelaksanaan_edukasi_pemberi","'".$norawat."','".$tanggal."','pages/upload/$fileName','".$jenis_bukti."'");
        echo $htmlHead."<body><center>Bukti Pemberi Edukasi tersimpan. Proses Pengambilan Bukti Pelaksanaan Informasi & Edukasi Sudah Selesai ..!! <br><a href='".$urlKembali."' class='btn btn-secondary'>Kembali</a></center></body>";
    } catch (mysqli_sql_exception $e) {
        if($e->getCode() == 1062){
            echo $htmlHead."<body><center>Gagal, kemungkinan bukti Pemberi Edukasi sudah pernah diambil sebelumnya ..!! <br><a href='".$urlKembali."' class='btn btn-secondary'>Kembali</a></center></body>";
        } else {
            echo $htmlHead."<body><center>Gagal, silahkan ulangi ..!! <br><a href='javascript:history.back()' class='btn btn-secondary'>Kembali</a></center></body>";
        }
    }
?>
