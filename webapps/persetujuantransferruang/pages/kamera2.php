<?php
    // [RM 04-1/04-2] Peran 2/4: PETUGAS PENERIMA. Setelah Simpan, alur
    // dilanjutkan ke login3.php -> Kamera3 (Dokter Yang Merawat). Bukti
    // disimpan ke tabel bukti_transfer_petugas_penerima.
    if(strpos($_SERVER['REQUEST_URI'],"pages")){
        exit(header("Location:../index.php"));
    }

    $namars        = getOne("select setting.nama_instansi from setting");
    $tanggal_masuk = "";
    $norawat       = "";

    $_sql          = "select * from antripersetujuantransferantarruang" ;
    $hasil         = bukaquery2($_sql);
    while ($data = mysqli_fetch_array ($hasil)){
        $tanggal_masuk  = $data['tanggal_masuk'];
        $norawat        = $data['no_rawat'];
    }

    $no_rkm_medis = "";
    $nm_pasien    = "";
    $jk           = "";
    $tgl_lahir    = "";

    $_sql2  = "select reg_periksa.no_rawat,pasien.no_rkm_medis,pasien.nm_pasien,if(pasien.jk='L','LAKI-LAKI','PEREMPUAN') as jk,
               DATE_FORMAT(pasien.tgl_lahir,'%d-%m-%Y') as tgl_lahir from reg_periksa inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis
               where reg_periksa.no_rawat='".$norawat."'" ;
    $hasil2 = bukaquery2($_sql2);
    while ($data2  = mysqli_fetch_array ($hasil2)){
        $no_rkm_medis = $data2['no_rkm_medis'];
        $nm_pasien    = $data2['nm_pasien'];
        $jk           = $data2['jk'];
        $tgl_lahir    = $data2['tgl_lahir'];
    }

    $tanggal = "";
    $nm_peran = "";
    $_sql3  = "select DATE_FORMAT(transfer_pasien_antar_ruang.tanggal_pindah,'%d-%m-%Y') as tanggal,petugas.nama as nm_peran
               from transfer_pasien_antar_ruang inner join petugas on transfer_pasien_antar_ruang.nip_menerima=petugas.nip
               where transfer_pasien_antar_ruang.tanggal_masuk='$tanggal_masuk' and transfer_pasien_antar_ruang.no_rawat='$norawat'" ;
    $hasil3 = bukaquery2($_sql3);
    while ($data3  = mysqli_fetch_array ($hasil3)){
        $tanggal  = $data3['tanggal'];
        $nm_peran = $data3['nm_peran'];
    }
?>

<!DOCTYPE html>
<html>
<head>
    <title>SIMKES Khanza</title>
    <script src="js/jquery.min.js"></script>
    <script src="js/webcam.min.js"></script>
    <link rel="stylesheet" href="css/bootstrap.min.css" />
    <style type="text/css">
        #results { padding: 0px; background:#EEFFEE; width: 490; height: 390 }
        #ttdCanvas { border:1px solid #999; background:#fff; touch-action:none; }
    </style>
</head>
<body>
    <div class="container">
        <h5 class="text-dark"><center><button class="btn btn-secondary" onclick="window.location.reload();">Refresh</button><br/><br/>BUKTI SERAH TERIMA TRANSFER PASIEN <br>NO.RAWAT <?=$norawat;?></center></h5>
        <h7 class="text-dark"><center>Tanggal <?=$tanggal;?> - Peran: <b>PETUGAS PENERIMA</b></center></h7><br/>
        <form method="POST" action="pages/storeImage2.php" onsubmit="return validasiIsi();" enctype=multipart/form-data>
            <input type="hidden" name="tanggal_masuk" value="<?=$tanggal_masuk;?>">
            <input type="hidden" name="norawat" value="<?=$norawat;?>">
            <input type="hidden" name="jenis_bukti" id="TxtJenisBukti" value="Foto">
            <h7 class="text-dark">
                Saya menyatakan turut serta dalam proses serah terima transfer pasien ini sebagai <b>Petugas Penerima</b>, untuk pasien:
            </h7>
            <table class="default" width="98%" border="0" align="center" cellpadding="3px" cellspacing="0px">
                <tr class="text-dark">
                    <td width="25%">Nama Pasien</td>
                    <td width="70%">: <?=$nm_pasien;?></td>
                </tr>
                <tr class="text-dark">
                    <td width="25%">Nomor Rekam Medis</td>
                    <td width="75%">: <?=$no_rkm_medis;?></td>
                </tr>
                <tr class="text-dark">
                    <td width="25%">Jenis Kelamin</td>
                    <td width="75%">: <?=$jk;?></td>
                </tr>
                <tr class="text-dark">
                    <td width="25%">Tanggal Lahir</td>
                    <td width="75%">: <?=$tgl_lahir;?></td>
                </tr>
            </table>
            <br/>
            <h5 class="text-dark"><center>Ditandatangani oleh : <b><?=($nm_peran==""?"-":$nm_peran);?></b></center></h5>
            <h7 class="text-dark"><center>Bukti Petugas Penerima - pilih Foto atau Tanda Tangan</center></h7>
            <div class="row">
                <div class="col-md-12 text-center">
                    <button type="button" class="btn btn-outline-primary" onclick="modeFoto()">Ambil Foto</button>
                    <button type="button" class="btn btn-outline-primary" onclick="modeTtd()">Tanda Tangan</button>
                    <br/><br/>
                </div>
            </div>
            <div class="row" id="blokFoto">
                <div class="col-md-6">
                    <div id="my_camera"></div>
                    <input type="hidden" name="image" class="image-tag" onkeydown="setDefault(this, document.getElementById('MsgIsi1'));" id="TxtIsi1">
                </div>
                <div class="col-md-6">
                    <div id="results"><h7 class="text-success"><center>Gambar akan diambil jika anda sudah mengeklik ya</center></h7></div>
                    <span id="MsgIsi1" style="color:#CC0000; font-size:10px;"></span>
                </div>
                <div class="col-md-12 text-center">
                    <br>
                    <input type="button" class="btn btn-warning" value="Ya, Saya sebagai pembuat pernyataan" onClick="take_snapshot()">
                </div>
            </div>
            <div class="row" id="blokTtd" style="display:none;">
                <div class="col-md-12 text-center">
                    <canvas id="ttdCanvas" width="490" height="390"></canvas><br/>
                    <button type="button" class="btn btn-secondary" onclick="clearCanvas()">Hapus</button>
                    <button type="button" class="btn btn-warning" onclick="take_signature()">Gunakan Tanda Tangan Ini</button>
                </div>
            </div>
            <div class="col-md-12 text-center">
                <br>
                <button class="btn btn-danger">Simpan</button>
            </div>
        </form>
    </div>

    <script language="JavaScript">
        Webcam.set({
            width: 490,
            height: 390,
            image_format: 'jpeg',
            jpeg_quality: 90
        });
        Webcam.attach( '#my_camera' );

        function take_snapshot() {
            document.getElementById('TxtJenisBukti').value = 'Foto';
            Webcam.snap( function(data_uri) {
                $(".image-tag").val(data_uri);
                document.getElementById('results').innerHTML = '<img src="'+data_uri+'"/>';
            } );
        }

        function modeFoto(){
            document.getElementById('blokFoto').style.display = '';
            document.getElementById('blokTtd').style.display = 'none';
            document.getElementById('TxtJenisBukti').value = 'Foto';
        }
        function modeTtd(){
            document.getElementById('blokFoto').style.display = 'none';
            document.getElementById('blokTtd').style.display = '';
            document.getElementById('TxtJenisBukti').value = 'Tanda Tangan';
        }

        var canvas = document.getElementById('ttdCanvas');
        var ctx = canvas.getContext('2d');
        ctx.fillStyle = '#fff'; ctx.fillRect(0,0,canvas.width,canvas.height);
        ctx.strokeStyle = '#000'; ctx.lineWidth = 2; ctx.lineJoin = 'round'; ctx.lineCap = 'round';
        var drawing = false;
        function posCanvas(e){
            var r = canvas.getBoundingClientRect();
            var p = e.touches ? e.touches[0] : e;
            return { x: p.clientX - r.left, y: p.clientY - r.top };
        }
        function startDraw(e){ drawing = true; var p = posCanvas(e); ctx.beginPath(); ctx.moveTo(p.x,p.y); e.preventDefault(); }
        function moveDraw(e){ if(!drawing) return; var p = posCanvas(e); ctx.lineTo(p.x,p.y); ctx.stroke(); e.preventDefault(); }
        function endDraw(){ drawing = false; }
        canvas.addEventListener('mousedown', startDraw); canvas.addEventListener('mousemove', moveDraw);
        canvas.addEventListener('mouseup', endDraw); canvas.addEventListener('mouseleave', endDraw);
        canvas.addEventListener('touchstart', startDraw); canvas.addEventListener('touchmove', moveDraw); canvas.addEventListener('touchend', endDraw);
        function clearCanvas(){ ctx.fillStyle = '#fff'; ctx.fillRect(0,0,canvas.width,canvas.height); }
        function take_signature() {
            $(".image-tag").val(canvas.toDataURL('image/jpeg', 0.92));
            document.getElementById('TxtJenisBukti').value = 'Tanda Tangan';
        }
    </script>
</body>
</html>
