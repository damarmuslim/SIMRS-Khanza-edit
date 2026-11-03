-- ============================================================
-- Migrasi: Transfer Pasien Antar Ruangan (RM 04-1 / RM 04-2)
-- RSUD Demang Sepulau Raya - SIMRS Khanza
-- Formulir target: RM 04-1 (halaman 1) + RM 04-2 (halaman 2)
--
-- STATUS: File ini untuk diimport manual oleh pengguna (mis. lewat
-- phpMyAdmin / mysql.exe di XAMPP). TIDAK dijalankan otomatis oleh
-- Claude Code.
--
-- Prinsip desain:
--  - Additive only: tidak ada DROP/CHANGE pada kolom/tabel existing.
--  - Semua kolom baru masuk ke tabel `transfer_pasien_antar_ruang`
--    yang sudah ada (tetap 1:1 dengan no_rawat+tanggal_masuk).
--  - Konvensi tipe kolom MENGIKUTI gaya tabel ini sendiri (semua
--    kolom existing di `transfer_pasien_antar_ruang` nullable,
--    `DEFAULT NULL`, TIDAK ada NOT NULL DEFAULT '' seperti di
--    migrasi RM 03-2/03-4-05-06 - jadi kolom baru di sini ikut gaya
--    tabel ini, bukan gaya migrasi lain).
--  - Semua statement pakai IF NOT EXISTS supaya aman dijalankan ulang.
--  - Desain form baru MENGGANTIKAN (bukan berdampingan dengan) 15
--    kolom lama di bawah ini dari sisi UI/cetakan aktif - kolom-kolom
--    lama TETAP ADA di skema (data lama tetap terbaca), hanya TIDAK
--    LAGI DITULIS oleh form baru:
--      keadaan_umum_sebelum_transfer, td_sebelum_transfer,
--      nadi_sebelum_transfer, rr_sebelum_transfer, suhu_sebelum_transfer,
--      keluhan_utama_sebelum_transfer, keadaan_umum_sesudah_transfer,
--      td_sesudah_transfer, nadi_sesudah_transfer, rr_sesudah_transfer,
--      suhu_sesudah_transfer, keluhan_utama_sesudah_transfer,
--      peralatan_yang_menyertai, keterangan_peralatan_yang_menyertai,
--      pemeriksaan_penunjang_yang_dilakukan
--    (Lihat juga catatan yang sama di RMTransferPasienAntarRuang.java
--    dekat deklarasi widget lama, supaya tidak sengaja dipakai lagi.)
-- ============================================================

-- ---------------------------------------------------------------------
-- KELOMPOK A: Keluarga / kontak
-- ---------------------------------------------------------------------
ALTER TABLE `transfer_pasien_antar_ruang` ADD COLUMN IF NOT EXISTS `no_tlp_menyetujui`      VARCHAR(15) DEFAULT NULL;
ALTER TABLE `transfer_pasien_antar_ruang` ADD COLUMN IF NOT EXISTS `sudah_dijelaskan_diagnosa` ENUM('Ya','Tidak') DEFAULT NULL;

-- ---------------------------------------------------------------------
-- KELOMPOK B: Kelas ruangan asal/tujuan. Nilai mengikuti persis enum
-- `kamar.kelas` yang sudah ada (sik.sql), bukan set baru.
-- ---------------------------------------------------------------------
ALTER TABLE `transfer_pasien_antar_ruang` ADD COLUMN IF NOT EXISTS `kelas_asal_ruang`         ENUM('Kelas 1','Kelas 2','Kelas 3','Kelas Utama','Kelas VIP','Kelas VVIP') DEFAULT NULL;
ALTER TABLE `transfer_pasien_antar_ruang` ADD COLUMN IF NOT EXISTS `kelas_ruang_selanjutnya`   ENUM('Kelas 1','Kelas 2','Kelas 3','Kelas Utama','Kelas VIP','Kelas VVIP') DEFAULT NULL;

-- ---------------------------------------------------------------------
-- KELOMPOK C: Perluasan enum `metode_pemindahan_pasien` (additive -
-- nilai lama TETAP ADA supaya data lama tidak kehilangan pilihan
-- terpilihnya di ComboBox; nilai baru mengikuti wording formulir fisik
-- "Cara Pindah": Jalan / Kursi Roda / Strecher / Lain-lain).
-- ---------------------------------------------------------------------
ALTER TABLE `transfer_pasien_antar_ruang`
  MODIFY COLUMN `metode_pemindahan_pasien`
  ENUM('Kursi Roda','Tempat Tidur','Brankar','Jalan Sendiri','-','Jalan','Strecher','Lain-lain') DEFAULT NULL;

-- ---------------------------------------------------------------------
-- KELOMPOK D: Dokter yang merawat, asal & tujuan. KETIGA slot (bukan cuma
-- slot 1) = kode dokter, dipilih lewat DlgCariDokter - tidak ada input
-- nama manual sama sekali (permintaan eksplisit user).
-- ---------------------------------------------------------------------
ALTER TABLE `transfer_pasien_antar_ruang` ADD COLUMN IF NOT EXISTS `kd_dokter_merawat_asal_1`    VARCHAR(20) DEFAULT NULL;
ALTER TABLE `transfer_pasien_antar_ruang` ADD COLUMN IF NOT EXISTS `kd_dokter_merawat_asal_2`    VARCHAR(20) DEFAULT NULL;
ALTER TABLE `transfer_pasien_antar_ruang` ADD COLUMN IF NOT EXISTS `kd_dokter_merawat_asal_3`    VARCHAR(20) DEFAULT NULL;
ALTER TABLE `transfer_pasien_antar_ruang` ADD COLUMN IF NOT EXISTS `kd_dokter_merawat_tujuan_1`   VARCHAR(20) DEFAULT NULL;
ALTER TABLE `transfer_pasien_antar_ruang` ADD COLUMN IF NOT EXISTS `kd_dokter_merawat_tujuan_2`   VARCHAR(20) DEFAULT NULL;
ALTER TABLE `transfer_pasien_antar_ruang` ADD COLUMN IF NOT EXISTS `kd_dokter_merawat_tujuan_3`   VARCHAR(20) DEFAULT NULL;

-- ---------------------------------------------------------------------
-- KELOMPOK E: Kondisi Pasien - observasi TUNGGAL (bukan sebelum/sesudah
-- seperti 12 kolom lama yang dipensiunkan di atas).
-- ---------------------------------------------------------------------
ALTER TABLE `transfer_pasien_antar_ruang` ADD COLUMN IF NOT EXISTS `kesadaran_transfer`     ENUM('Compos Mentis','Gelisah','Delirium','Koma') DEFAULT NULL;
ALTER TABLE `transfer_pasien_antar_ruang` ADD COLUMN IF NOT EXISTS `td_transfer`             VARCHAR(7) DEFAULT NULL;
ALTER TABLE `transfer_pasien_antar_ruang` ADD COLUMN IF NOT EXISTS `hr_transfer`             VARCHAR(5) DEFAULT NULL;
ALTER TABLE `transfer_pasien_antar_ruang` ADD COLUMN IF NOT EXISTS `rr_transfer`             VARCHAR(5) DEFAULT NULL;
ALTER TABLE `transfer_pasien_antar_ruang` ADD COLUMN IF NOT EXISTS `suhu_transfer`           VARCHAR(5) DEFAULT NULL;
ALTER TABLE `transfer_pasien_antar_ruang` ADD COLUMN IF NOT EXISTS `gcs_transfer`            VARCHAR(10) DEFAULT NULL;
ALTER TABLE `transfer_pasien_antar_ruang` ADD COLUMN IF NOT EXISTS `spo2_transfer`           VARCHAR(5) DEFAULT NULL;
ALTER TABLE `transfer_pasien_antar_ruang` ADD COLUMN IF NOT EXISTS `tingkat_nyeri_transfer`  ENUM('0','1','2','3','4','5','6','7','8','9','10') DEFAULT NULL;
ALTER TABLE `transfer_pasien_antar_ruang` ADD COLUMN IF NOT EXISTS `observasi_terakhir_pukul` DATETIME DEFAULT NULL;

-- ---------------------------------------------------------------------
-- KELOMPOK F: Therapy Oksigen. Menggantikan sebagian konsep
-- `peralatan_yang_menyertai` lama (kolom lama dipensiunkan, lihat catatan
-- header) - konsep "Oksigen Portable" lama pecah jadi jenis rinci sesuai
-- formulir fisik.
-- ---------------------------------------------------------------------
ALTER TABLE `transfer_pasien_antar_ruang` ADD COLUMN IF NOT EXISTS `oksigen_status`                  ENUM('Ya','Tidak') DEFAULT NULL;
ALTER TABLE `transfer_pasien_antar_ruang` ADD COLUMN IF NOT EXISTS `oksigen_nasal_kanule`             ENUM('Ya','Tidak') DEFAULT NULL;
ALTER TABLE `transfer_pasien_antar_ruang` ADD COLUMN IF NOT EXISTS `oksigen_nasal_kateter`            ENUM('Ya','Tidak') DEFAULT NULL;
ALTER TABLE `transfer_pasien_antar_ruang` ADD COLUMN IF NOT EXISTS `oksigen_face_mask`                ENUM('Ya','Tidak') DEFAULT NULL;
ALTER TABLE `transfer_pasien_antar_ruang` ADD COLUMN IF NOT EXISTS `oksigen_sungkup_rebreathing`      ENUM('Ya','Tidak') DEFAULT NULL;
ALTER TABLE `transfer_pasien_antar_ruang` ADD COLUMN IF NOT EXISTS `oksigen_sungkup_non_rebreathing`  ENUM('Ya','Tidak') DEFAULT NULL;
ALTER TABLE `transfer_pasien_antar_ruang` ADD COLUMN IF NOT EXISTS `oksigen_tracheostomy`             ENUM('Ya','Tidak') DEFAULT NULL;
ALTER TABLE `transfer_pasien_antar_ruang` ADD COLUMN IF NOT EXISTS `oksigen_ett`                      ENUM('Ya','Tidak') DEFAULT NULL;

-- ---------------------------------------------------------------------
-- KELOMPOK G: "DATA SERAH TERIMA" - Line Invasif
-- ---------------------------------------------------------------------
ALTER TABLE `transfer_pasien_antar_ruang` ADD COLUMN IF NOT EXISTS `line_invasif`          ENUM('Tidak Ada Line Invasive','CVP/PICC','IV Line') DEFAULT NULL;
ALTER TABLE `transfer_pasien_antar_ruang` ADD COLUMN IF NOT EXISTS `line_invasif_tanggal`  DATETIME DEFAULT NULL;

-- ---------------------------------------------------------------------
-- KELOMPOK H: Kewaspadaan / Precaution
-- ---------------------------------------------------------------------
ALTER TABLE `transfer_pasien_antar_ruang` ADD COLUMN IF NOT EXISTS `kewaspadaan_standar`   ENUM('Ya','Tidak') DEFAULT NULL;
ALTER TABLE `transfer_pasien_antar_ruang` ADD COLUMN IF NOT EXISTS `kewaspadaan_kontak`    ENUM('Ya','Tidak') DEFAULT NULL;
ALTER TABLE `transfer_pasien_antar_ruang` ADD COLUMN IF NOT EXISTS `kewaspadaan_airborne`  ENUM('Ya','Tidak') DEFAULT NULL;
ALTER TABLE `transfer_pasien_antar_ruang` ADD COLUMN IF NOT EXISTS `kewaspadaan_droplet`   ENUM('Ya','Tidak') DEFAULT NULL;

-- ---------------------------------------------------------------------
-- KELOMPOK I: Nutrisi. `nutrisi_ngt` turut menggantikan konsep "NGT" dari
-- `peralatan_yang_menyertai` lama.
-- ---------------------------------------------------------------------
ALTER TABLE `transfer_pasien_antar_ruang` ADD COLUMN IF NOT EXISTS `nutrisi_puasa`                ENUM('Ya','Tidak') DEFAULT NULL;
ALTER TABLE `transfer_pasien_antar_ruang` ADD COLUMN IF NOT EXISTS `nutrisi_oral`                 ENUM('Ya','Tidak') DEFAULT NULL;
ALTER TABLE `transfer_pasien_antar_ruang` ADD COLUMN IF NOT EXISTS `nutrisi_ngt`                  ENUM('Ya','Tidak') DEFAULT NULL;
ALTER TABLE `transfer_pasien_antar_ruang` ADD COLUMN IF NOT EXISTS `nutrisi_batasan_cairan_cc`    VARCHAR(10) DEFAULT NULL;
ALTER TABLE `transfer_pasien_antar_ruang` ADD COLUMN IF NOT EXISTS `nutrisi_diet_khusus`          ENUM('Ya','Tidak') DEFAULT NULL;
ALTER TABLE `transfer_pasien_antar_ruang` ADD COLUMN IF NOT EXISTS `nutrisi_diet_khusus_jelaskan` VARCHAR(100) DEFAULT NULL;

-- ---------------------------------------------------------------------
-- KELOMPOK J: Eliminasi BAB/BAK. `eliminasi_bak`='Kateter Urine' turut
-- menggantikan konsep "Kateter Urin" dari `peralatan_yang_menyertai` lama.
-- ---------------------------------------------------------------------
ALTER TABLE `transfer_pasien_antar_ruang` ADD COLUMN IF NOT EXISTS `eliminasi_bab`                        ENUM('Normal','Diare/Konstipasi','Ileustomy/Colostomy','Inkontinensia Alvi') DEFAULT NULL;
ALTER TABLE `transfer_pasien_antar_ruang` ADD COLUMN IF NOT EXISTS `eliminasi_bak`                        ENUM('Normal','Inkontinensia Urine','Kateter Urine') DEFAULT NULL;
ALTER TABLE `transfer_pasien_antar_ruang` ADD COLUMN IF NOT EXISTS `eliminasi_bak_kateter_no`             VARCHAR(20) DEFAULT NULL;
ALTER TABLE `transfer_pasien_antar_ruang` ADD COLUMN IF NOT EXISTS `eliminasi_bak_kateter_tanggal_pasang` DATETIME DEFAULT NULL;

-- ---------------------------------------------------------------------
-- KELOMPOK K: Halaman 2 (RM 04-2) - Aktifitas / Mobilisasi / Drainase
-- ---------------------------------------------------------------------
ALTER TABLE `transfer_pasien_antar_ruang` ADD COLUMN IF NOT EXISTS `aktifitas`            ENUM('Mandiri','Dibantu Sebagian','Dibantu Penuh') DEFAULT NULL;
ALTER TABLE `transfer_pasien_antar_ruang` ADD COLUMN IF NOT EXISTS `mobilisasi`           ENUM('Tirah Baring','Duduk','Jalan') DEFAULT NULL;
ALTER TABLE `transfer_pasien_antar_ruang` ADD COLUMN IF NOT EXISTS `drainase_wsd`         ENUM('Ya','Tidak') DEFAULT NULL;
ALTER TABLE `transfer_pasien_antar_ruang` ADD COLUMN IF NOT EXISTS `drainase_drain_luka`  ENUM('Ya','Tidak') DEFAULT NULL;
ALTER TABLE `transfer_pasien_antar_ruang` ADD COLUMN IF NOT EXISTS `drainase_stoma`       ENUM('Ya','Tidak') DEFAULT NULL;
ALTER TABLE `transfer_pasien_antar_ruang` ADD COLUMN IF NOT EXISTS `drainase_lain`        ENUM('Ya','Tidak') DEFAULT NULL;
ALTER TABLE `transfer_pasien_antar_ruang` ADD COLUMN IF NOT EXISTS `drainase_lain_ket`    VARCHAR(100) DEFAULT NULL;

-- ---------------------------------------------------------------------
-- KELOMPOK L: Integritas Kulit/Luka - 4 kelompok Ukuran/Lokasi/Kondisi
-- berulang + Tidak Ada Luka + Lain-lain.
-- ---------------------------------------------------------------------
ALTER TABLE `transfer_pasien_antar_ruang` ADD COLUMN IF NOT EXISTS `luka_tidak_ada`         ENUM('Ya','Tidak') DEFAULT NULL;

ALTER TABLE `transfer_pasien_antar_ruang` ADD COLUMN IF NOT EXISTS `luka_operasi_cek`       ENUM('Ya','Tidak') DEFAULT NULL;
ALTER TABLE `transfer_pasien_antar_ruang` ADD COLUMN IF NOT EXISTS `luka_operasi_ukuran`    VARCHAR(30) DEFAULT NULL;
ALTER TABLE `transfer_pasien_antar_ruang` ADD COLUMN IF NOT EXISTS `luka_operasi_lokasi`    VARCHAR(50) DEFAULT NULL;
ALTER TABLE `transfer_pasien_antar_ruang` ADD COLUMN IF NOT EXISTS `luka_operasi_kondisi`   VARCHAR(50) DEFAULT NULL;

ALTER TABLE `transfer_pasien_antar_ruang` ADD COLUMN IF NOT EXISTS `luka_dekubitus_cek`     ENUM('Ya','Tidak') DEFAULT NULL;
ALTER TABLE `transfer_pasien_antar_ruang` ADD COLUMN IF NOT EXISTS `luka_dekubitus_ukuran`  VARCHAR(30) DEFAULT NULL;
ALTER TABLE `transfer_pasien_antar_ruang` ADD COLUMN IF NOT EXISTS `luka_dekubitus_lokasi`  VARCHAR(50) DEFAULT NULL;
ALTER TABLE `transfer_pasien_antar_ruang` ADD COLUMN IF NOT EXISTS `luka_dekubitus_kondisi` VARCHAR(50) DEFAULT NULL;

ALTER TABLE `transfer_pasien_antar_ruang` ADD COLUMN IF NOT EXISTS `luka_skin_lost_cek`     ENUM('Ya','Tidak') DEFAULT NULL;
ALTER TABLE `transfer_pasien_antar_ruang` ADD COLUMN IF NOT EXISTS `luka_skin_lost_ukuran`  VARCHAR(30) DEFAULT NULL;
ALTER TABLE `transfer_pasien_antar_ruang` ADD COLUMN IF NOT EXISTS `luka_skin_lost_lokasi`  VARCHAR(50) DEFAULT NULL;
ALTER TABLE `transfer_pasien_antar_ruang` ADD COLUMN IF NOT EXISTS `luka_skin_lost_kondisi` VARCHAR(50) DEFAULT NULL;

ALTER TABLE `transfer_pasien_antar_ruang` ADD COLUMN IF NOT EXISTS `luka_gangren_cek`       ENUM('Ya','Tidak') DEFAULT NULL;
ALTER TABLE `transfer_pasien_antar_ruang` ADD COLUMN IF NOT EXISTS `luka_gangren_ukuran`    VARCHAR(30) DEFAULT NULL;
ALTER TABLE `transfer_pasien_antar_ruang` ADD COLUMN IF NOT EXISTS `luka_gangren_lokasi`    VARCHAR(50) DEFAULT NULL;
ALTER TABLE `transfer_pasien_antar_ruang` ADD COLUMN IF NOT EXISTS `luka_gangren_kondisi`   VARCHAR(50) DEFAULT NULL;

ALTER TABLE `transfer_pasien_antar_ruang` ADD COLUMN IF NOT EXISTS `luka_lain_cek`          ENUM('Ya','Tidak') DEFAULT NULL;
ALTER TABLE `transfer_pasien_antar_ruang` ADD COLUMN IF NOT EXISTS `luka_lain_ket`          VARCHAR(100) DEFAULT NULL;

-- ---------------------------------------------------------------------
-- KELOMPOK M: Hasil Investigasi Abnormal, Tindakan/Kebutuhan Khusus,
-- Riwayat Alergi, Alat Bantu Yang Digunakan.
-- ---------------------------------------------------------------------
ALTER TABLE `transfer_pasien_antar_ruang` ADD COLUMN IF NOT EXISTS `hasil_investigasi_abnormal`     VARCHAR(500) DEFAULT NULL;

ALTER TABLE `transfer_pasien_antar_ruang` ADD COLUMN IF NOT EXISTS `tindakan_resiko_jatuh`          ENUM('Ya','Tidak') DEFAULT NULL;
ALTER TABLE `transfer_pasien_antar_ruang` ADD COLUMN IF NOT EXISTS `tindakan_pemasangan_restrain`   ENUM('Ya','Tidak') DEFAULT NULL;
ALTER TABLE `transfer_pasien_antar_ruang` ADD COLUMN IF NOT EXISTS `tindakan_perawatan_luka`        ENUM('Ya','Tidak') DEFAULT NULL;
ALTER TABLE `transfer_pasien_antar_ruang` ADD COLUMN IF NOT EXISTS `tindakan_hygiene`               ENUM('Ya','Tidak') DEFAULT NULL;
ALTER TABLE `transfer_pasien_antar_ruang` ADD COLUMN IF NOT EXISTS `riwayat_alergi`                 ENUM('Ya','Tidak') DEFAULT NULL;
ALTER TABLE `transfer_pasien_antar_ruang` ADD COLUMN IF NOT EXISTS `jenis_alergi`                   VARCHAR(100) DEFAULT NULL;
ALTER TABLE `transfer_pasien_antar_ruang` ADD COLUMN IF NOT EXISTS `reaksi_alergi`                  VARCHAR(100) DEFAULT NULL;

ALTER TABLE `transfer_pasien_antar_ruang` ADD COLUMN IF NOT EXISTS `alat_bantu_tanpa`               ENUM('Ya','Tidak') DEFAULT NULL;
ALTER TABLE `transfer_pasien_antar_ruang` ADD COLUMN IF NOT EXISTS `alat_bantu_dengar`              ENUM('Ya','Tidak') DEFAULT NULL;
ALTER TABLE `transfer_pasien_antar_ruang` ADD COLUMN IF NOT EXISTS `alat_bantu_kaca_mata`           ENUM('Ya','Tidak') DEFAULT NULL;
ALTER TABLE `transfer_pasien_antar_ruang` ADD COLUMN IF NOT EXISTS `alat_bantu_gigi_palsu`          ENUM('Ya','Tidak') DEFAULT NULL;
ALTER TABLE `transfer_pasien_antar_ruang` ADD COLUMN IF NOT EXISTS `alat_bantu_tongkat_kruk`        ENUM('Ya','Tidak') DEFAULT NULL;
ALTER TABLE `transfer_pasien_antar_ruang` ADD COLUMN IF NOT EXISTS `alat_bantu_lain`                ENUM('Ya','Tidak') DEFAULT NULL;
ALTER TABLE `transfer_pasien_antar_ruang` ADD COLUMN IF NOT EXISTS `alat_bantu_lain_ket`            VARCHAR(100) DEFAULT NULL;

-- ---------------------------------------------------------------------
-- KELOMPOK N: Sisa Halaman 2 - Tindakan Yang Sudah Dilakukan, Pemeriksaan
-- Penunjang (pola 4-baris cek+ket, menggantikan `pemeriksaan_penunjang_
-- yang_dilakukan` lama - lihat catatan header), Rencana Tindak Lanjut,
-- Masalah Utama Keperawatan, Dokumen Yang Diserahterimakan.
-- ---------------------------------------------------------------------
ALTER TABLE `transfer_pasien_antar_ruang` ADD COLUMN IF NOT EXISTS `tindakan_yang_sudah_dilakukan` VARCHAR(800) DEFAULT NULL;

ALTER TABLE `transfer_pasien_antar_ruang` ADD COLUMN IF NOT EXISTS `penunjang_laboratorium_cek`    ENUM('Ya','Tidak') DEFAULT NULL;
ALTER TABLE `transfer_pasien_antar_ruang` ADD COLUMN IF NOT EXISTS `penunjang_laboratorium_ket`    VARCHAR(200) DEFAULT NULL;
ALTER TABLE `transfer_pasien_antar_ruang` ADD COLUMN IF NOT EXISTS `penunjang_radiologi_cek`       ENUM('Ya','Tidak') DEFAULT NULL;
ALTER TABLE `transfer_pasien_antar_ruang` ADD COLUMN IF NOT EXISTS `penunjang_radiologi_ket`       VARCHAR(200) DEFAULT NULL;
ALTER TABLE `transfer_pasien_antar_ruang` ADD COLUMN IF NOT EXISTS `penunjang_ct_scan_cek`         ENUM('Ya','Tidak') DEFAULT NULL;
ALTER TABLE `transfer_pasien_antar_ruang` ADD COLUMN IF NOT EXISTS `penunjang_ct_scan_ket`         VARCHAR(200) DEFAULT NULL;
ALTER TABLE `transfer_pasien_antar_ruang` ADD COLUMN IF NOT EXISTS `penunjang_lain_cek`            ENUM('Ya','Tidak') DEFAULT NULL;
ALTER TABLE `transfer_pasien_antar_ruang` ADD COLUMN IF NOT EXISTS `penunjang_lain_ket`            VARCHAR(200) DEFAULT NULL;

ALTER TABLE `transfer_pasien_antar_ruang` ADD COLUMN IF NOT EXISTS `rencana_tindak_lanjut`         VARCHAR(800) DEFAULT NULL;
ALTER TABLE `transfer_pasien_antar_ruang` ADD COLUMN IF NOT EXISTS `masalah_utama_keperawatan`     VARCHAR(500) DEFAULT NULL;
ALTER TABLE `transfer_pasien_antar_ruang` ADD COLUMN IF NOT EXISTS `dokumen_yang_diserahterimakan` VARCHAR(500) DEFAULT NULL;

-- ============================================================
-- KELOMPOK O: Bukti tanda tangan/foto - 4 penandatangan
-- (Petugas Pengirim, Petugas Penerima, Dokter Yang Merawat,
-- Pasien/Keluarga). Setiap tabel = 1 bukti per transfer
-- (no_rawat+tanggal_masuk), diisi oleh webapp
-- persetujuantransferruang/ lewat alur 4 halaman kamera.php.
--
-- `bukti_persetujuan_transfer_pasien_antar_ruang` (Pasien/Keluarga)
-- SUDAH ADA dan sudah punya data produksi - TIDAK dibuat ulang,
-- cuma ditambahkan kolom `jenis_bukti` (additive) supaya tahu
-- apakah bukti berupa Foto (webcam, cara lama) atau Tanda Tangan
-- (kanvas, cara baru). Baris lama otomatis DEFAULT 'Foto' (benar,
-- karena semua baris lama memang hasil webcam).
-- ============================================================
ALTER TABLE `bukti_persetujuan_transfer_pasien_antar_ruang`
  ADD COLUMN IF NOT EXISTS `jenis_bukti` ENUM('Foto','Tanda Tangan') NOT NULL DEFAULT 'Foto' AFTER `photo`;

CREATE TABLE IF NOT EXISTS `bukti_transfer_petugas_pengirim` (
  `no_rawat` varchar(17) NOT NULL,
  `tanggal_masuk` datetime NOT NULL,
  `photo` varchar(500) DEFAULT NULL,
  `jenis_bukti` enum('Foto','Tanda Tangan') NOT NULL DEFAULT 'Foto',
  PRIMARY KEY (`no_rawat`,`tanggal_masuk`),
  CONSTRAINT `bukti_transfer_petugas_pengirim_ibfk_1` FOREIGN KEY (`no_rawat`) REFERENCES `reg_periksa` (`no_rawat`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

CREATE TABLE IF NOT EXISTS `bukti_transfer_petugas_penerima` (
  `no_rawat` varchar(17) NOT NULL,
  `tanggal_masuk` datetime NOT NULL,
  `photo` varchar(500) DEFAULT NULL,
  `jenis_bukti` enum('Foto','Tanda Tangan') NOT NULL DEFAULT 'Foto',
  PRIMARY KEY (`no_rawat`,`tanggal_masuk`),
  CONSTRAINT `bukti_transfer_petugas_penerima_ibfk_1` FOREIGN KEY (`no_rawat`) REFERENCES `reg_periksa` (`no_rawat`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

CREATE TABLE IF NOT EXISTS `bukti_transfer_dokter_merawat` (
  `no_rawat` varchar(17) NOT NULL,
  `tanggal_masuk` datetime NOT NULL,
  `photo` varchar(500) DEFAULT NULL,
  `jenis_bukti` enum('Foto','Tanda Tangan') NOT NULL DEFAULT 'Foto',
  PRIMARY KEY (`no_rawat`,`tanggal_masuk`),
  CONSTRAINT `bukti_transfer_dokter_merawat_ibfk_1` FOREIGN KEY (`no_rawat`) REFERENCES `reg_periksa` (`no_rawat`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

-- `antripersetujuantransferantarruang` (tabel antrian 1-baris) SENGAJA
-- TIDAK diubah - peran (pengirim/penerima/dokter/pasien) ditentukan lewat
-- alur halaman webapp (kamera.php -> kamera2.php -> kamera3.php ->
-- kamera4.php), bukan lewat kolom database.
