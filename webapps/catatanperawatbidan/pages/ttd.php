<?php
    // [RM 32] Halaman tanda tangan pasien/keluarga untuk SATU baris catatan
    // perawat/bidan.
    //
    // Baris mana yang ditandatangani diambil dari tabel antrian
    // `antricatatanperawatbidan` yang diisi aplikasi desktop - pola yang sama
    // dengan ~20 webapp persetujuan lain di sistem ini. Tabel itu berisi TIGA
    // kolom, bukan cuma no_rawat, karena PK catatan adalah
    // (tanggal, jam, no_rawat): tanpa jam, tanda tangan bisa menempel ke baris
    // catatan yang salah pada pasien yang sama.
    if(strpos($_SERVER['REQUEST_URI'],"pages")){
        exit(header("Location:../index.php"));
    }

    $norawat = "";
    $tanggal = "";
    $jam     = "";
    $hasil   = bukaquery2("select * from antricatatanperawatbidan");
    while ($data = mysqli_fetch_array($hasil)) {
        $norawat = $data['no_rawat'];
        $tanggal = $data['tanggal'];
        $jam     = $data['jam'];
    }

    $no_rkm_medis = "";
    $nm_pasien    = "";
    $jk           = "";
    $tgl_lahir    = "";
    $ruangan      = "";
    if ($norawat != "") {
        $sql2 = "select pasien.no_rkm_medis,pasien.nm_pasien,
                 if(pasien.jk='L','LAKI-LAKI','PEREMPUAN') as jk,
                 DATE_FORMAT(pasien.tgl_lahir,'%d-%m-%Y') as tgl_lahir,
                 ifnull((select bangsal.nm_bangsal from kamar_inap ki
                         inner join kamar on ki.kd_kamar=kamar.kd_kamar
                         inner join bangsal on kamar.kd_bangsal=bangsal.kd_bangsal
                         where ki.no_rawat=reg_periksa.no_rawat
                         order by ki.tgl_masuk desc,ki.jam_masuk desc limit 1),'') as ruangan
                 from reg_periksa inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis
                 where reg_periksa.no_rawat='".$norawat."'";
        $h2 = bukaquery2($sql2);
        while ($d2 = mysqli_fetch_array($h2)) {
            $no_rkm_medis = $d2['no_rkm_medis'];
            $nm_pasien    = $d2['nm_pasien'];
            $jk           = $d2['jk'];
            $tgl_lahir    = $d2['tgl_lahir'];
            $ruangan      = $d2['ruangan'];
        }
    }

    // Isi catatan DITAMPILKAN sebelum tanda tangan. Meminta orang
    // menandatangani sesuatu yang tidak bisa dia baca bukan persetujuan.
    $uraian    = "";
    $alkes     = "";
    $nm_perawat = "";
    $sudah_ttd = "";
    if ($norawat != "") {
        $sql3 = "select catatan_keperawatan_ranap.uraian,
                 ifnull(catatan_keperawatan_ranap.alkes_pengobatan_diet,'') as alkes_pengobatan_diet,
                 ifnull(catatan_keperawatan_ranap.ttd_pasien,'') as ttd_pasien,
                 petugas.nama
                 from catatan_keperawatan_ranap
                 inner join petugas on catatan_keperawatan_ranap.nip=petugas.nip
                 where catatan_keperawatan_ranap.no_rawat='".$norawat."'
                   and catatan_keperawatan_ranap.tanggal='".$tanggal."'
                   and catatan_keperawatan_ranap.jam='".$jam."'";
        $h3 = bukaquery2($sql3);
        while ($d3 = mysqli_fetch_array($h3)) {
            $uraian     = $d3['uraian'];
            $alkes      = $d3['alkes_pengobatan_diet'];
            $nm_perawat = $d3['nama'];
            $sudah_ttd  = $d3['ttd_pasien'];
        }
    }
?>
<style type="text/css">
    #ttdCanvas { border:2px dashed #999; background:#fff; touch-action:none; max-width:100%; }
    .kotak-info { background:#f4f8f4; border:1px solid #cfe0cf; padding:10px; border-radius:6px; }
</style>

<div class="container" style="max-width:640px; padding-top:10px;">
    <div class="text-center">
        <button class="btn btn-secondary btn-sm" onclick="window.location.reload();">Refresh</button>
        <h5 class="text-dark" style="margin-top:10px;">TANDA TANGAN PASIEN / KELUARGA</h5>
        <h6 class="text-muted">Catatan Perawat / Bidan (RM 32)</h6>
    </div>

<?php if ($norawat == "") { ?>
    <div class="alert alert-warning text-center" style="margin-top:15px;">
        Belum ada permintaan tanda tangan.<br/>
        Silahkan perawat menekan tombol <b>Minta TTD Pasien</b> di aplikasi,
        lalu tekan <b>Refresh</b> di halaman ini.
    </div>
<?php } else if ($uraian == "") { ?>
    <div class="alert alert-danger text-center" style="margin-top:15px;">
        Baris catatan yang diminta tidak ditemukan.<br/>
        Kemungkinan sudah dihapus atau jam-nya diubah setelah permintaan dikirim.
        Silahkan perawat mengirim ulang.
    </div>
<?php } else { ?>
    <div class="kotak-info" style="margin-top:12px;">
        <b><?=htmlspecialchars($nm_pasien);?></b> (<?=htmlspecialchars($no_rkm_medis);?>)<br/>
        <?=htmlspecialchars($jk);?> &middot; <?=htmlspecialchars($tgl_lahir);?>
        <?php if($ruangan!=""){ ?>&middot; Ruang <?=htmlspecialchars($ruangan);?><?php } ?><br/>
        <span class="text-muted">Catatan tanggal <?=htmlspecialchars($tanggal);?> jam <?=htmlspecialchars($jam);?></span>
    </div>

    <div style="margin-top:12px;">
        <?php if($alkes!=""){ ?>
        <div><b>Penggunaan ALKES / Pengobatan / Diet :</b><br/><?=nl2br(htmlspecialchars($alkes));?></div>
        <?php } ?>
        <div style="margin-top:8px;"><b>Catatan :</b><br/><?=nl2br(htmlspecialchars($uraian));?></div>
        <div class="text-muted" style="margin-top:8px;">Ditulis oleh : <?=htmlspecialchars($nm_perawat);?></div>
    </div>

    <?php if($sudah_ttd!=""){ ?>
    <div class="alert alert-info text-center" style="margin-top:12px;">
        Baris ini <b>sudah pernah ditandatangani</b>.
        Menandatangani lagi akan menggantikan tanda tangan sebelumnya.
    </div>
    <?php } ?>

    <form method="POST" action="pages/storeTtd.php" onsubmit="return validasiIsi();">
        <input type="hidden" name="norawat" value="<?=htmlspecialchars($norawat);?>">
        <input type="hidden" name="tanggal" value="<?=htmlspecialchars($tanggal);?>">
        <input type="hidden" name="jam"     value="<?=htmlspecialchars($jam);?>">
        <input type="hidden" name="image"   class="image-tag" value="">

        <p style="margin-top:14px; margin-bottom:4px;"><b>Silahkan tanda tangan di kotak bawah ini :</b></p>
        <canvas id="ttdCanvas" width="600" height="260"></canvas>
        <div style="margin-top:8px;">
            <button type="button" class="btn btn-outline-secondary" onclick="clearCanvas()">Hapus &amp; Ulangi</button>
            <button type="submit" class="btn btn-primary float-right">Simpan Tanda Tangan</button>
        </div>
    </form>
<?php } ?>
</div>

<script type="text/javascript">
    var canvas = document.getElementById('ttdCanvas');
    if (canvas) {
        var ctx = canvas.getContext('2d');
        var menggambar = false;
        var adaGoresan = false;

        function bersih(){ ctx.fillStyle = '#fff'; ctx.fillRect(0,0,canvas.width,canvas.height); }
        bersih();
        ctx.strokeStyle = '#000';
        ctx.lineWidth = 2.5;
        ctx.lineCap = 'round';
        ctx.lineJoin = 'round';

        // Canvas ditampilkan lebih kecil dari ukuran aslinya di layar HP, jadi
        // koordinat sentuhan harus diskalakan - kalau tidak, goresan muncul
        // meleset dari jari.
        function titik(e){
            var r = canvas.getBoundingClientRect();
            var sx = canvas.width / r.width;
            var sy = canvas.height / r.height;
            var src = (e.touches && e.touches.length) ? e.touches[0] : e;
            return { x:(src.clientX - r.left) * sx, y:(src.clientY - r.top) * sy };
        }
        function mulai(e){ e.preventDefault(); menggambar = true; var p = titik(e); ctx.beginPath(); ctx.moveTo(p.x,p.y); }
        function gerak(e){ if(!menggambar) return; e.preventDefault(); var p = titik(e); ctx.lineTo(p.x,p.y); ctx.stroke(); adaGoresan = true; }
        function selesai(e){ menggambar = false; }

        canvas.addEventListener('mousedown', mulai);
        canvas.addEventListener('mousemove', gerak);
        canvas.addEventListener('mouseup', selesai);
        canvas.addEventListener('mouseleave', selesai);
        canvas.addEventListener('touchstart', mulai, {passive:false});
        canvas.addEventListener('touchmove', gerak, {passive:false});
        canvas.addEventListener('touchend', selesai);

        window.clearCanvas = function(){ bersih(); adaGoresan = false; };

        // Menolak kanvas kosong: tanpa ini, menekan Simpan tanpa menggores akan
        // menyimpan gambar putih polos sebagai "tanda tangan".
        window.validasiIsi = function(){
            if(!adaGoresan){
                alert('Tanda tangan masih kosong. Silahkan tanda tangan dulu.');
                return false;
            }
            document.querySelector('.image-tag').value = canvas.toDataURL('image/jpeg', 0.92);
            return true;
        };
    }
</script>
