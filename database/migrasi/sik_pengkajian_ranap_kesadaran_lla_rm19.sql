-- Migrasi additive-only untuk penyesuaian RMPenilaianAwalKeperawatanRanap.java
-- (Tahap 3: Tanda Vital - LLA + Tingkat Kesadaran skala klinis penuh) dengan
-- formulir fisik RM 19 "PENGKAJIAN AWAL KEPERAWATAN RAWAT INAP".
-- Aman dijalankan berulang (IF NOT EXISTS). Kolom baru menempel di UJUNG tabel -
-- Sequel.menyimpantf()/mengedittf() INSERT/UPDATE posisional, urutan kolom lama
-- (termasuk migrasi2 RM19 sebelumnya) TIDAK BOLEH berubah.
--
-- Field lama `pemeriksaan_keadaan_umum` (ENUM Baik/Sedang/Buruk, dipakai combo
-- KeadaanMentalUmum) TETAP ADA & TIDAK disentuh - disembunyikan saja di form
-- input (data lama tidak hilang), diganti kolom baru tingkat_kesadaran dgn skala
-- klinis penuh sesuai formulir fisik.

ALTER TABLE penilaian_awal_keperawatan_ranap
  ADD COLUMN IF NOT EXISTS tingkat_kesadaran ENUM('Compos Mentis','Apatis','Somnolen/Letargi','Delirium','Stupor','Semikomatosa','Coma') DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS lla VARCHAR(5) DEFAULT NULL;
