-- ============================================================
-- Migrasi: Triase IGD PACS (Patient's Acuity Categorization Scale)
-- RSUD Demang Sepulau Raya - SIMRS Khanza
-- Formulir target: RM 03-1
--
-- STATUS: File ini untuk diimport manual oleh pengguna (mis. lewat
-- phpMyAdmin di XAMPP). TIDAK dijalankan otomatis oleh Claude Code.
--
-- Prinsip desain:
--  - Additive only: tidak ada DROP/CHANGE pada kolom/tabel existing.
--  - Kolom baru di data_triase_igd nullable/default aman untuk baris lama.
--  - Mekanisme Trauma & PACS dipisah jadi tabel baru (1:1 no_rawat),
--    pola sama seperti data_triase_igdprimer/data_triase_igdsekunder
--    yang sudah ada di sik.sql.
--  - PACS BERDAMPINGAN dengan model Skala 1-5 lama, bukan menggantikan.
--  - Semua statement pakai IF NOT EXISTS supaya aman dijalankan ulang.
-- ============================================================

-- ---------------------------------------------------------------------
-- KELOMPOK A: kolom tambahan di data_triase_igd (existing, tanpa DROP)
-- ---------------------------------------------------------------------
ALTER TABLE `data_triase_igd` ADD COLUMN IF NOT EXISTS `kontak_awal_tanggal`   DATE NULL AFTER `tgl_kunjungan`;
ALTER TABLE `data_triase_igd` ADD COLUMN IF NOT EXISTS `kontak_awal_pukul`     TIME NULL AFTER `kontak_awal_tanggal`;
ALTER TABLE `data_triase_igd` ADD COLUMN IF NOT EXISTS `terpasang_infus`       ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak' AFTER `keterangan_kedatangan`;
ALTER TABLE `data_triase_igd` ADD COLUMN IF NOT EXISTS `terpasang_ngt`         ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak' AFTER `terpasang_infus`;
ALTER TABLE `data_triase_igd` ADD COLUMN IF NOT EXISTS `terpasang_kateter_urin` ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak' AFTER `terpasang_ngt`;
ALTER TABLE `data_triase_igd` ADD COLUMN IF NOT EXISTS `nama_pengantar`        VARCHAR(100) NULL AFTER `terpasang_kateter_urin`;
ALTER TABLE `data_triase_igd` ADD COLUMN IF NOT EXISTS `telp_pengantar`        VARCHAR(20) NULL AFTER `nama_pengantar`;
ALTER TABLE `data_triase_igd` ADD COLUMN IF NOT EXISTS `kesadaran`             ENUM('Sadar Penuh','Respon Suara','Respon Nyeri','Tidak Respon') NULL AFTER `nyeri`;
ALTER TABLE `data_triase_igd` ADD COLUMN IF NOT EXISTS `nyeri_ada`             ENUM('Ya','Tidak') NULL AFTER `kesadaran`;
ALTER TABLE `data_triase_igd` ADD COLUMN IF NOT EXISTS `berat_badan`           VARCHAR(6) NULL AFTER `nyeri_ada`;
ALTER TABLE `data_triase_igd` ADD COLUMN IF NOT EXISTS `tinggi_badan`          VARCHAR(8) NULL AFTER `berat_badan`;

-- ---------------------------------------------------------------------
-- KELOMPOK B: tabel baru data_triase_mekanisme_trauma (1:1 no_rawat)
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `data_triase_mekanisme_trauma` (
  `no_rawat` varchar(17) NOT NULL,
  `kkl_tunggal` enum('Ya','Tidak') NOT NULL DEFAULT 'Tidak',
  `kkl_dengan_lawan` enum('Ya','Tidak') NOT NULL DEFAULT 'Tidak',
  `kkl_lawan` varchar(100) DEFAULT NULL,
  `kkl_tempat_kejadian` varchar(150) DEFAULT NULL,
  `kkl_tanggal_kejadian` date DEFAULT NULL,
  `kkl_pukul_kejadian` time DEFAULT NULL,
  `jatuh_ketinggian` enum('Ya','Tidak') NOT NULL DEFAULT 'Tidak',
  `jatuh_ketinggian_keterangan` varchar(200) DEFAULT NULL,
  `luka_bakar` enum('Ya','Tidak') NOT NULL DEFAULT 'Tidak',
  `luka_bakar_keterangan` varchar(200) DEFAULT NULL,
  `trauma_fisik` enum('Ya','Tidak') NOT NULL DEFAULT 'Tidak',
  `trauma_fisik_keterangan` varchar(200) DEFAULT NULL,
  `trauma_zat_kimia` enum('Ya','Tidak') NOT NULL DEFAULT 'Tidak',
  `trauma_zat_kimia_keterangan` varchar(200) DEFAULT NULL,
  `trauma_zat_lainnya` enum('Ya','Tidak') NOT NULL DEFAULT 'Tidak',
  `trauma_zat_lainnya_keterangan` varchar(200) DEFAULT NULL,
  PRIMARY KEY (`no_rawat`),
  CONSTRAINT `data_triase_mekanisme_trauma_ibfk_1` FOREIGN KEY (`no_rawat`) REFERENCES `data_triase_igd` (`no_rawat`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

-- ---------------------------------------------------------------------
-- KELOMPOK C: tabel baru data_triase_pacs (1:1 no_rawat) - berdampingan
-- dengan data_triase_igdprimer/igdsekunder (model Skala 1-5), bukan
-- pengganti.
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `data_triase_pacs` (
  `no_rawat` varchar(17) NOT NULL,
  `skor_kesadaran` tinyint(1) NOT NULL DEFAULT 0,
  `skor_tekanan_darah_sistolik` tinyint(1) NOT NULL DEFAULT 0,
  `skor_nadi` tinyint(1) NOT NULL DEFAULT 0,
  `skor_respirasi` tinyint(1) NOT NULL DEFAULT 0,
  `skor_temperatur` tinyint(1) NOT NULL DEFAULT 0,
  `skor_saturasi_o2` tinyint(1) NOT NULL DEFAULT 0,
  `total_skor` tinyint(2) NOT NULL DEFAULT 0,
  `level_pacs` enum('PACS 1','PACS 2','PACS 3','PACS 4') DEFAULT NULL,
  `keputusan_pacs` enum('Ruang Resusitasi','Ruang Non Resusitasi','Poli Klinik/Klinik Umum 24 Jam','Death On Arrival') DEFAULT NULL,
  `keputusan_pukul` time DEFAULT NULL,
  `tanggal_triase` datetime NOT NULL,
  `nik` varchar(20) NOT NULL,
  `keluhan_utama` varchar(400) DEFAULT NULL,
  PRIMARY KEY (`no_rawat`),
  KEY `level_pacs` (`level_pacs`),
  KEY `nik` (`nik`),
  CONSTRAINT `data_triase_pacs_ibfk_1` FOREIGN KEY (`no_rawat`) REFERENCES `data_triase_igd` (`no_rawat`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `data_triase_pacs_ibfk_2` FOREIGN KEY (`nik`) REFERENCES `pegawai` (`nik`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

-- Tambahan (kalau tabel di atas sudah pernah dibuat dari versi file ini
-- sebelumnya, sebelum kolom keluhan_utama ada): aman dijalankan ulang.
ALTER TABLE `data_triase_pacs` ADD COLUMN IF NOT EXISTS `keluhan_utama` VARCHAR(400) DEFAULT NULL;

-- Tambahan: Catatan Khusus (kolom di formulir fisik RM 03-1, dekat baris
-- Total skor EWS) - aman dijalankan ulang.
ALTER TABLE `data_triase_pacs` ADD COLUMN IF NOT EXISTS `catatan_khusus` VARCHAR(400) DEFAULT NULL;

-- Verifikasi struktur tabel setelah migrasi
DESCRIBE `data_triase_igd`;
DESCRIBE `data_triase_mekanisme_trauma`;
DESCRIBE `data_triase_pacs`;
