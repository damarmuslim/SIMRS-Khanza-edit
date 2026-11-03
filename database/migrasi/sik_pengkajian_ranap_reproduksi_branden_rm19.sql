-- Migrasi additive-only untuk penyesuaian RMPenilaianAwalKeperawatanRanap.java
-- (Tahap 4: Status Reproduksi + Skala Branden) dengan formulir fisik RM 19
-- "PENGKAJIAN AWAL KEPERAWATAN RAWAT INAP". Kedua sub-bagian ini SEBELUMNYA
-- TIDAK ADA sama sekali di form/tabel.
-- Aman dijalankan berulang (IF NOT EXISTS). Kolom baru menempel di UJUNG tabel -
-- Sequel.menyimpantf()/mengedittf() INSERT/UPDATE posisional, urutan kolom lama
-- (termasuk migrasi2 RM19 sebelumnya) TIDAK BOLEH berubah.

ALTER TABLE penilaian_awal_keperawatan_ranap
  ADD COLUMN IF NOT EXISTS reproduksi_menstruasi ENUM('Teratur','Tidak Teratur','Belum Menstruasi','Menopause') DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS reproduksi_hamil ENUM('Tidak','Ya') DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS reproduksi_hpht VARCHAR(20) DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS reproduksi_papsmear ENUM('Tidak Teratur','Pernah') DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS reproduksi_papsmear_tanggal VARCHAR(20) DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS reproduksi_mamografi ENUM('Tidak Teratur','Pernah') DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS reproduksi_mamografi_tanggal VARCHAR(20) DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS reproduksi_sadari ENUM('Tidak Teratur','Pernah') DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS reproduksi_sadari_tanggal VARCHAR(20) DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS reproduksi_screening_prostat ENUM('Tidak Teratur','Pernah') DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS reproduksi_screening_prostat_tanggal VARCHAR(20) DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS skala_branden VARCHAR(5) DEFAULT NULL;
