-- =====================================================================
-- MIGRASI: Penyesuaian Kolom Penunjang Penilaian Awal Keperawatan IGD
-- Tanggal : 2026-10-28
-- Tujuan  : Menambahkan kolom penunjang CT Scan dan Pemeriksaan Lainnya
--           dengan tipe TEXT agar dapat menampung hasil/permintaan tanpa truncate.
-- =====================================================================

ALTER TABLE `penilaian_awal_keperawatan_igd`
  MODIFY COLUMN `penunjang_cek_lab_ket` TEXT,
  MODIFY COLUMN `penunjang_rontgen_ket` TEXT,
  MODIFY COLUMN `penunjang_ct_scan_ket` TEXT,
  ADD COLUMN IF NOT EXISTS `penunjang_lain` ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak',
  ADD COLUMN IF NOT EXISTS `penunjang_lain_ket` TEXT;
