-- =====================================================================
-- RM 30-1A  EARLY WARNING SCORING SYSTEM (DEWASA)
-- RSUD Demang Sepulau Raya
--
-- Menyesuaikan tabel `pemantauan_pews_dewasa` yang SUDAH ADA (dipakai form
-- RMPemantauanEWSD) dengan kertas RM 30-1A. Tidak ada tabel baru: modulnya
-- sudah tersambung dari DlgRawatInap, DlgKamarInap, DlgIGD, dan DlgKasirRalan,
-- dan membuat tabel kedua akan memecah data pasien ke dua tempat.
--
-- ================== APA YANG BERBEDA DENGAN KERTAS ==================
-- Skor bawaan di sistem BUKAN skor yang sama dengan kertas RSUD. Kertasnya
-- persis NEWS2; yang di sistem varian lama dengan tingkat tambahan "7".
--
--   Laju respirasi : sistem 7 pita s/d '>= 35'  ->  kertas 5 pita, maks skor 3
--   Saturasi       : sistem '>= 95'/'<= 92'     ->  kertas '>= 96'/'<= 91'
--   TD sistolik    : sistem 7 pita              ->  kertas 12 pita
--   Nadi           : sistem 7 pita              ->  kertas 11 pita
--   Kesadaran      : sistem Nyeri/Verbal+Unrespon -> kertas C/V/P/U
--   Temperatur     : sistem 5 pita              ->  kertas 6 pita
--   Skor maksimum  : sistem sampai 7 (biru)     ->  kertas maksimum 3
--
-- Tingkat skor 7 DIHAPUS dari pilihan (keputusan user): kertas maksimum 3, dan
-- kondisi henti napas/jantung jadi baris protokol tersendiri, bukan angka.
-- Nilai ENUM lamanya TIDAK dihapus - lihat alasan di bawah.
--
-- CACAT YANG IKUT DIPERBAIKI: pita saturasi lama TUMPANG TINDIH. '>= 95' dan
-- '94 - 95' sama-sama memuat 95; '<= 92' dan '92 - 93' sama-sama memuat 92.
-- Angka 95 bisa dinilai 0 atau 1 tergantung petugas memilih yang mana. Pita
-- kertas ('>= 96' / '94 - 95' / '92 - 93' / '<= 91') tidak tumpang tindih.
--
-- CACAT PADA KERTAS: pita laju pernapasan tertulis '9 - 10' lalu langsung
-- '12 - 20', sehingga angka 11 tidak masuk pita mana pun. Atas keputusan user
-- dipakai '9 - 11' sesuai standar, jadi seluruh rentang tertutup.
--
-- ============ NILAI ENUM DITAMBAH DI UJUNG, TIDAK PERNAH DISISIPKAN ============
-- MySQL menyimpan ENUM sebagai NOMOR URUT, bukan teksnya. Menambah nilai di
-- TENGAH atau mengubah urutannya akan diam-diam memetakan ulang baris yang
-- sudah ada ke nilai yang salah - baris lama tiba-tiba berubah artinya tanpa
-- error apa pun. Karena itu semua nilai baru DITEMPEL DI UJUNG dan nilai lama
-- dibiarkan utuh, walaupun sebagiannya tidak akan ditawarkan lagi oleh form.
-- Ini juga yang membuat migrasi ini tetap aditif: nol nilai dihapus, nol baris
-- lama berubah arti. JANGAN merapikan daftar ini dengan mengurutkannya.
--
-- ================== ⚠ PENYIMPANAN POSISIONAL ==================
-- RMPemantauanEWSD menyimpan lewat
--     Sequel.menyimpantf("pemantauan_pews_dewasa","?,?,?,...",19,nilai)
-- yang merakit `insert into ... values(...)` TANPA NAMA KOLOM. Tiga kolom baru
-- di bawah menempel di UJUNG tabel, jadi jumlah tanda tanya dan urutan nilai
-- di Java WAJIB ikut berubah dari 19 menjadi 22, dengan urutan persis:
--     ... , nip, aliran_oksigen, alat_oksigen, gula_darah
-- Kalau migrasi ini naik ke produksi tanpa Java-nya, SELURUH penyimpanan EWS
-- dewasa gagal. Naikkan BERSAMAAN.
-- =====================================================================

-- ---------- 1. Laju pernapasan : tambah '<= 8' dan '>= 25' ----------
-- '9 - 11', '12 - 20', '21 - 24' sudah ada dan dipakai ulang apa adanya.
ALTER TABLE `pemantauan_pews_dewasa`
  MODIFY COLUMN `parameter_laju_respirasi`
    ENUM('<= 5','6 - 8','9 - 11','12 - 20','21 - 24','25 - 34','>= 35',
         '<= 8','>= 25') DEFAULT NULL;

-- ---------- 2. Saturasi oksigen : tambah '>= 96' dan '<= 91' ----------
ALTER TABLE `pemantauan_pews_dewasa`
  MODIFY COLUMN `parameter_saturasi_oksigen`
    ENUM('>= 95','94 - 95','92 - 93','<= 92',
         '>= 96','<= 91') DEFAULT NULL;

-- ---------- 3. Tekanan darah sistolik : 12 pita ----------
-- '>= 220', '101 - 110', '91 - 100', '<= 70' sudah ada.
ALTER TABLE `pemantauan_pews_dewasa`
  MODIFY COLUMN `parameter_tekanan_darah_sistolik`
    ENUM('>= 220','181 - 220','111 - 180','101 - 110','91 - 100','71 - 90','<= 70',
         '201 - 219','181 - 200','161 - 180','141 - 160','121 - 140','111 - 120',
         '81 - 90','71 - 80') DEFAULT NULL;

-- ---------- 4. Nadi : 11 pita ----------
-- '41 - 50' dan '<= 40' sudah ada.
ALTER TABLE `pemantauan_pews_dewasa`
  MODIFY COLUMN `parameter_laju_jantung`
    ENUM('>= 140','131 - 140','111 - 130','91 - 110','51 - 90','41 - 50','<= 40',
         '>= 131','121 - 130','111 - 120','101 - 110','91 - 100','81 - 90',
         '71 - 80','61 - 70','51 - 60') DEFAULT NULL;

-- ---------- 5. Kesadaran : tambah 'C/V/P/U' ----------
-- Kertas menggabungkan Confusion/Voice/Pain/Unresponsive jadi satu baris
-- bernilai 3, menggantikan pasangan lama Nyeri/Verbal (3) + Unrespon (7).
ALTER TABLE `pemantauan_pews_dewasa`
  MODIFY COLUMN `parameter_kesadaran`
    ENUM('Sadar','Nyeri/Verbal','Unrespon',
         'C/V/P/U') DEFAULT NULL;

-- ---------- 6. Temperatur : 6 pita ----------
-- Pita lama '36.1 - 38' dipecah jadi '36.1 - 37.0' dan '37.1 - 38.0'; keduanya
-- tetap bernilai 0, jadi pemecahan ini hanya soal bentuk kertas, bukan skor.
ALTER TABLE `pemantauan_pews_dewasa`
  MODIFY COLUMN `parameter_temperatur`
    ENUM('<= 35','35.1 - 36','36.1 - 38','38.1 - 39','>= 39',
         '>= 39.1','38.1 - 39.0','37.1 - 38.0','36.1 - 37.0','35.1 - 36.0',
         '<= 35.0') DEFAULT NULL;

-- ---------- 7. Kolom baru : menempel di UJUNG ----------
-- Kertas mencatat aliran oksigen dalam L/menit dan alat yang dipakai. Keduanya
-- DOKUMENTASI, bukan penentu skor: yang menentukan skor 2 adalah pasien sedang
-- memakai oksigen atau tidak, yang sudah ditangani parameter_suplemen_oksigen.
ALTER TABLE `pemantauan_pews_dewasa`
  ADD COLUMN IF NOT EXISTS `aliran_oksigen` VARCHAR(10) DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS `alat_oksigen` ENUM('RM','NRM','NK','Tanpa Alat') DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS `gula_darah` VARCHAR(10) DEFAULT NULL;

-- CATATAN: baris "Inisial" pada kertas TIDAK dibuatkan kolom. Satu baris tabel
-- ini tepatnya satu kolom waktu di kertas, dan tiap baris sudah menyimpan `nip`
-- pengisinya - jadi inisialnya diturunkan dari `nip` saat mencetak. Menyimpannya
-- lagi hanya menciptakan salinan yang bisa berbeda dengan `nip`-nya sendiri.


-- ---------- 8. Tabel Konfirmasi & Validasi TTE EWS Dewasa ----------
-- Tabel pendamping non-destruktif untuk mencatat validasi Tanda Tangan Elektronik
-- (TTE) perawat/petugas dan dokter tanpa mengubah 22 kolom posisional `pemantauan_pews_dewasa`.
CREATE TABLE IF NOT EXISTS `pemantauan_pews_dewasa_konfirmasi` (
  `no_rawat` varchar(17) NOT NULL,
  `tanggal` datetime NOT NULL,
  `tgl_validasi_petugas` datetime DEFAULT NULL,
  `nip_petugas` varchar(20) DEFAULT NULL,
  `status_petugas` varchar(30) DEFAULT 'Belum Validasi',
  `tgl_validasi_dokter` datetime DEFAULT NULL,
  `kd_dokter` varchar(20) DEFAULT NULL,
  `status_dokter` varchar(30) DEFAULT 'Belum Validasi',
  `hash_tte` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`no_rawat`, `tanggal`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
