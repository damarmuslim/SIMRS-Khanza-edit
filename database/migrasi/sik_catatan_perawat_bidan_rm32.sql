-- =====================================================================
-- RM 32  CATATAN PERAWAT / BIDAN  - RSUD Demang Sepulau Raya
--
-- Migrasi ADDITIVE: menambah dua kolom nullable pada tabel yang sudah ada
-- dan membuat satu tabel antrian baru. Tidak ada kolom yang diubah tipenya,
-- tidak ada yang dihapus, tidak ada data yang disentuh.
--
-- ==================== PERINGATAN KERAS ====================
-- MIGRASI INI WAJIB DIKIRIM BERSAMA PERUBAHAN JAVA-NYA.
--
-- RMDataCatatanKeperawatanRanap.simpan() memakai
--     Sequel.menyimpantf("catatan_keperawatan_ranap","?,?,?,?,?","Data",5,...)
-- yang merakit `insert into catatan_keperawatan_ranap values(?,?,?,?,?)`
-- TANPA NAMA KOLOM - jadi POSISIONAL. Kolom baru selalu menempel di UJUNG
-- tabel (ADD COLUMN tanpa AFTER), sehingga sesudah migrasi ini tabel punya
-- 7 kolom sementara kodenya masih mengirim 5 nilai: SELURUH PENYIMPANAN AKAN
-- GAGAL sampai Java-nya ikut diubah jadi "?,?,?,?,?,?,?" dengan 7 nilai.
--
-- Ini persis kejadian kolom `jam` di modul Asuhan Gizi. Kalau salah satu saja
-- yang dikirim ke produksi, perawat tidak bisa menyimpan catatan sama sekali.
-- ==========================================================
--
-- KENAPA MENUMPANG TABEL LAMA, BUKAN TABEL BARU
-- `catatan_keperawatan_ranap` sudah ada dan bentuknya SUDAH BENAR untuk lembar
-- ini: PK (tanggal, jam, no_rawat) yang artinya banyak baris per episode -
-- persis buku catatan berbaris di kertas. Sudah ada pula form
-- RMDataCatatanKeperawatanRanap, hak akses `catatan_keperawatan_ranap`, dan
-- laporan rptFormulirCatatanKeperawatanRanap. Membuat tabel baru akan
-- memecah catatan yang sama ke dua tempat.
--
-- `catatan_keperawatan_ralan` SENGAJA TIDAK DISENTUH: RM 32 punya isian
-- Ruangan dan Nomor bed, jadi khusus rawat inap.
-- `catatan_perawatan` juga bukan - itu ditandatangani DOKTER (`kd_dokter`).
--
-- YANG TIDAK DIBUATKAN KOLOM (sengaja)
-- RUANGAN dan NOMOR (bed) diambil dari `kamar_inap` lewat JOIN, tidak
-- disalin. Alasan sama seperti RM 33: pasien pindah kamar membuat salinan
-- basi tanpa ada yang tahu. Nomor bed dibaca dari `kamar_inap.kd_kamar`.
-- =====================================================================

-- ---------------------------------------------------------------------
-- 1. Kolom "Penggunaan ALKES / Pengobatan / Diet"
--    Kolom kedua pada lembar fisik, tidak punya padanan di skema lama.
-- ---------------------------------------------------------------------
ALTER TABLE `catatan_keperawatan_ranap`
    ADD COLUMN `alkes_pengobatan_diet` VARCHAR(300) DEFAULT NULL;

-- ---------------------------------------------------------------------
-- 2. Kolom tanda tangan pasien/keluarga
--    Berisi PATH berkas gambar hasil canvas tanda tangan di webapp
--    (mis. "pages/upload/2026072800000120260828143012.jpeg"), bukan gambarnya
--    sendiri - pola yang sama dengan bukti_transfer_* milik RM 04.
--
--    DIISI OLEH WEBAPP, BUKAN OLEH FORM DESKTOP. Karena itu simpan() di Java
--    mengirim NULL untuk kolom ini, dan ganti() TIDAK BOLEH menyertakannya di
--    klausa SET - kalau disertakan, mengedit teks catatan akan menghapus
--    tanda tangan yang sudah diberikan pasien.
-- ---------------------------------------------------------------------
ALTER TABLE `catatan_keperawatan_ranap`
    ADD COLUMN `ttd_pasien` VARCHAR(255) DEFAULT NULL;

-- ---------------------------------------------------------------------
-- 3. Tabel antrian tanda tangan
--    Pola rumah tangga SIMRS Khanza: satu tabel `antri<modul>` berisi SATU
--    baris yang menyatakan record mana yang sedang menunggu tanda tangan.
--    Aplikasi desktop menulis ke sini, lalu tablet di ruangan yang sudah
--    membuka halaman webapp menekan Refresh dan mengambilnya.
--
--    MyISAM dan tanpa PRIMARY KEY - mengikuti persis
--    `antripersetujuantransferantarruang` dan ~20 tabel antri lain yang sudah
--    ada. Ini tabel gores (scratch), bukan penyimpan data; isinya dihapus dan
--    ditulis ulang tiap kali ada permintaan tanda tangan baru.
--
--    Tiga kolom karena PK catatan adalah (tanggal, jam, no_rawat) - webapp
--    harus tahu BARIS MANA yang ditandatangani, bukan sekadar pasien mana.
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `antricatatanperawatbidan` (
  `no_rawat` VARCHAR(17) NOT NULL,
  `tanggal`  DATE DEFAULT NULL,
  `jam`      TIME DEFAULT NULL
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
