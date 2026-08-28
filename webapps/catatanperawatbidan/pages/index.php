<?php
    // [RM 32] Halaman pendaratan ketika sesi BELUM terbentuk.
    //
    // Halaman ini SENGAJA tidak melempar balik ke ?act=Ttd. formProtek()
    // melempar pengunjung yang belum login dari 'Ttd' ke 'Home', dan 'Home'
    // adalah halaman ini - kalau di sini dilempar balik ke 'Ttd', keduanya
    // akan saling melempar tanpa henti.
    if(strpos($_SERVER['REQUEST_URI'],"pages")){
        exit(header("Location:../index.php"));
    }
?>
<div class="container" style="max-width:640px; padding-top:20px;">
    <div class="text-center">
        <h5 class="text-dark">TANDA TANGAN PASIEN / KELUARGA</h5>
        <h6 class="text-muted">Catatan Perawat / Bidan (RM 32)</h6>
    </div>
    <div class="alert alert-secondary text-center" style="margin-top:15px;">
        Halaman ini belum dibuka lewat aplikasi.<br/><br/>
        Silahkan petugas membuka halaman tanda tangan dari
        <b>SIMRS Khanza &rarr; Catatan Keperawatan Rawat Inap</b>,
        lalu perangkat ini cukup ditinggal terbuka di halaman tanda tangan.
    </div>
</div>
