-- =====================================================================
-- KONSOLIDASI RM 20-1 "ASESMEN MEDIS RAWAT INAP" (RSUD Demang Sepulau Raya)
-- Migrasi aditif untuk menggabungkan seluruh isian fisik RM 20-1 (Hal 1 & 2)
-- =====================================================================
-- Menambahkan kolom Anamnesis lengkap, TTV, GCS, Antropometri, Penunjang,
-- Status Ekonomi, dan string Base64 kanvas anatomi tubuh ke tabel
-- `asesmen_medis_ranap_rm20_1`.
--
-- Sifat: 100% ADITIF (hanya ALTER TABLE ADD COLUMN), tidak mengubah atau
-- menghapus kolom yang sudah ada. Tabel lama `penilaian_medis_ranap`
-- sama sekali TIDAK diubah strukturnya.
-- =====================================================================

ALTER TABLE `asesmen_medis_ranap_rm20_1`
  ADD COLUMN `asal_masuk` enum('IGD','Poli Rawat Jalan') DEFAULT 'IGD',
  ADD COLUMN `cara_masuk` enum('Berjalan','Kursi Roda','Brankar') DEFAULT 'Brankar',
  ADD COLUMN `keluhan_utama` text,
  ADD COLUMN `riwayat_penyakit` text,
  ADD COLUMN `riwayat_pengobatan` text,
  ADD COLUMN `alergi_obat` varchar(200) NOT NULL DEFAULT '',
  ADD COLUMN `alergi_makanan` varchar(200) NOT NULL DEFAULT '',
  ADD COLUMN `riwayat_lain` text,
  ADD COLUMN `status_ekonomi` enum('Baik','Sedang','Kurang') DEFAULT 'Sedang',
  ADD COLUMN `keadaan_umum` enum('Sehat','Sakit Ringan','Sakit Sedang','Sakit Berat') DEFAULT 'Sakit Sedang',
  ADD COLUMN `kesadaran` enum('Compos Mentis','Apatis','Somnolen','Sopor','Koma') DEFAULT 'Compos Mentis',
  ADD COLUMN `gcs_e` varchar(2) NOT NULL DEFAULT '',
  ADD COLUMN `gcs_m` varchar(2) NOT NULL DEFAULT '',
  ADD COLUMN `gcs_v` varchar(2) NOT NULL DEFAULT '',
  ADD COLUMN `td_sistol` varchar(4) NOT NULL DEFAULT '',
  ADD COLUMN `td_diastol` varchar(4) NOT NULL DEFAULT '',
  ADD COLUMN `nadi` varchar(5) NOT NULL DEFAULT '',
  ADD COLUMN `rr` varchar(5) NOT NULL DEFAULT '',
  ADD COLUMN `suhu` varchar(5) NOT NULL DEFAULT '',
  ADD COLUMN `spo2` varchar(5) NOT NULL DEFAULT '',
  ADD COLUMN `tb` varchar(5) NOT NULL DEFAULT '',
  ADD COLUMN `bb` varchar(5) NOT NULL DEFAULT '',
  ADD COLUMN `imt` varchar(8) NOT NULL DEFAULT '',
  ADD COLUMN `status_pediatrik` enum('Gizi Kurang','Baik','Gizi Lebih') DEFAULT NULL,
  ADD COLUMN `pemeriksaan_penunjang` text,
  ADD COLUMN `lokalis_canvas` longtext;
