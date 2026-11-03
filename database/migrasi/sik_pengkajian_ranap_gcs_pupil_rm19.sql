-- Migrasi additive-only untuk penyesuaian RMPenilaianAwalKeperawatanRanap.java
-- (Tahap 7: GCS dipecah E/M/V + Pupil Kanan/Kiri) dengan formulir fisik RM 19.
-- Field lama `pemeriksaan_gcs` (1 TextBox bebas "E,V,M") TETAP ADA & TIDAK
-- disentuh - disembunyikan saja di form input, diganti 3 field angka terpisah.
-- Pupil Kanan/Kiri (ukuran mm + reaksi cahaya) SEBELUMNYA TIDAK ADA SAMA SEKALI.
-- Aman dijalankan berulang. Kolom baru menempel di UJUNG tabel - urutan kolom
-- lama TIDAK BOLEH berubah (Sequel.menyimpantf()/mengedittf() posisional).

ALTER TABLE penilaian_awal_keperawatan_ranap
  ADD COLUMN IF NOT EXISTS gcs_e VARCHAR(5) DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS gcs_m VARCHAR(5) DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS gcs_v VARCHAR(5) DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS pupil_kanan_ukuran VARCHAR(10) DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS pupil_kanan_reaksi VARCHAR(100) DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS pupil_kiri_ukuran VARCHAR(10) DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS pupil_kiri_reaksi VARCHAR(100) DEFAULT NULL;
