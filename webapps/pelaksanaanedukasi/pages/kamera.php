<?php
    if(strpos($_SERVER['REQUEST_URI'],"pages")){
        exit(header("Location:../index.php"));
    }
    
    $namars        = getOne("select setting.nama_instansi from setting");
    $tanggal       = "";
    $norawat       = "";
    
    $_sql          = "select * from antripelaksanaanedukasi" ;  
    $hasil         = bukaquery2($_sql);
    while ($data = mysqli_fetch_array ($hasil)){
        $tanggal  = $data['tanggal'];
        $norawat  = $data['no_rawat'];
    }
    
    
    $no_rkm_medis = "";
    $nm_pasien    = "";
    $jk           = "";
    $umur         = "";
    $tgl_lahir    = "";
    $alamat       = "";
    $no_tlp       = "";
    
    $_sql2  = "select reg_periksa.no_rawat,pasien.no_rkm_medis,pasien.nm_pasien,if(pasien.jk='L','LAKI-LAKI','PEREMPUAN') as jk,
               pasien.umur,DATE_FORMAT(pasien.tgl_lahir,'%d-%m-%Y') as tgl_lahir,concat(pasien.alamat,', ',kelurahan.nm_kel,', ',kecamatan.nm_kec,', ',kabupaten.nm_kab) as alamat, 
               pasien.no_tlp from reg_periksa inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis 
               inner join kelurahan on pasien.kd_kel=kelurahan.kd_kel
               inner join kecamatan on pasien.kd_kec=kecamatan.kd_kec 
               inner join kabupaten on pasien.kd_kab=kabupaten.kd_kab
               where reg_periksa.no_rawat='".$norawat."'" ;  
    $hasil2 = bukaquery2($_sql2);
    while ($data2  = mysqli_fetch_array ($hasil2)){
        $no_rkm_medis = $data2['no_rkm_medis'];
        $nm_pasien    = $data2['nm_pasien'];
        $jk           = $data2['jk'];
        $umur         = $data2['umur'];
        $tgl_lahir    = $data2['tgl_lahir'];
        $alamat       = $data2['alamat'];
        $no_tlp       = $data2['no_tlp'];
    }
    
    $diberikan_pada             = "";
    $keterangan_diberikan_pada  = "";
    $materi_edukasi             = "";
    $tanggaledukasi             = "";
    $_sql2  = "select DATE_FORMAT(pelaksanaan_informasi_edukasi.tanggal,'%d-%m-%Y') as tanggaledukasi,pelaksanaan_informasi_edukasi.keterangan_diberikan_pada,pelaksanaan_informasi_edukasi.diberikan_pada,pelaksanaan_informasi_edukasi.materi_edukasi ".
              "from pelaksanaan_informasi_edukasi where pelaksanaan_informasi_edukasi.tanggal='$tanggal' and pelaksanaan_informasi_edukasi.no_rawat='$norawat'" ;  
    $hasil2 = bukaquery2($_sql2);
    while ($data2  = mysqli_fetch_array ($hasil2)){
        $tanggaledukasi            = $data2['tanggaledukasi'];
        $diberikan_pada            = $data2['diberikan_pada'];
        $keterangan_diberikan_pada = $data2['keterangan_diberikan_pada'];
        $materi_edukasi            = $data2['materi_edukasi'];
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
        <h5 class="text-dark"><center><button class="btn btn-secondary" onclick="window.location.reload();">Refresh</button><br/><br/>PENGAMBILAN BUKTI PELAKSANAAN INFORMASI & EDUKASI <br>NO.RAWAT <?=$norawat;?></center></h5>
        <h7 class="text-dark"><center>Tanggal <?=$tanggaledukasi;?> - Peran: <b>PASIEN/KELUARGA</b> (1/2)</center></h7><br/>
        <form method="POST" action="pages/storeImage.php" onsubmit="return validasiIsi();" enctype=multipart/form-data>
            <input type="hidden" name="tanggal" value="<?=$tanggal;?>">
            <input type="hidden" name="norawat" value="<?=$norawat;?>">
            <h7 class="text-dark">
                Saya yang dibawah ini :
            </h7>
            <table class="default" width="98%" border="0" align="center" cellpadding="3px" cellspacing="0px">
                <tr class="text-dark">
                    <td width="25%">Penerima Edukasi/Informasi</td>
                    <td width="70%">: <?=$keterangan_diberikan_pada;?></td>
                </tr>
                <tr class="text-dark">
                    <td width="25%">Hubungan Dengan Pasien</td>
                    <td width="75%">: <?=$diberikan_pada;?></td>
                </tr>
            </table>
            <br/>
            <h7 class="text-dark">
                Dari pasien <?=$namars?> dengan :
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
            <h7 class="text-dark">
                Bahwa saya telah mendapatkan materi informasi & edukasi "<?=$materi_edukasi?>" di <?=$namars?>. Demikian pernyataan ini dibuat dalam keadaan penuh kesadaran untuk digunakan sebagaimana mestinya
            </h7>
            <br/>
            <br/>
            <h7 class="text-dark"><center>Yang Membuat Pernyataan</center></h7>
            <input type="hidden" name="jenis_bukti" id="TxtJenisBukti" value="Tanda Tangan">
            <h7 class="text-dark"><center>Bukti Pelaksanaan - pilih Tanda Tangan atau Foto</center></h7>
            <div class="row">
                <div class="col-md-12 text-center">
                    <button type="button" class="btn btn-outline-primary" onclick="modeTtd()">Tanda Tangan</button>
                    <button type="button" class="btn btn-outline-primary" onclick="modeFoto()">Ambil Foto</button>
                    <br/><br/>
                </div>
            </div>
            <div class="row" id="blokTtd">
                <div class="col-md-12 text-center">
                    <canvas id="ttdCanvas" width="490" height="390"></canvas><br/>
                    <button type="button" class="btn btn-secondary" onclick="clearCanvas()">Hapus</button>
                    <button type="button" class="btn btn-warning" onclick="take_signature()">Gunakan Tanda Tangan Ini</button>
                </div>
            </div>
            <div class="row" id="blokFoto" style="display:none;">
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

