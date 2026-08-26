-- Migrasi additive-only untuk penyesuaian RMPenilaianAwalKeperawatanRanap.java
-- (Tahap 2: Alasan Masuk RS - Nama Keluarga & Hubungan saat Alloanamnesis)
-- dengan formulir fisik RM 19 "PENGKAJIAN AWAL KEPERAWATAN RAWAT INAP".
-- Aman dijalankan berulang (IF NOT EXISTS). Kolom baru sengaja ditambahkan
-- TANPA posisi eksplisit (AFTER ...) supaya menempel di UJUNG tabel -
-- tabel disimpan lewat Sequel.menyimpantf()/mengedittf() yang INSERT/UPDATE
-- posisional, jadi urutan kolom lama (termasuk 30 kolom dari migrasi
-- sik_pengkajian_ranap_riwayat_rm19.sql sebelumnya) TIDAK BOLEH berubah.

ALTER TABLE penilaian_awal_keperawatan_ranap
  ADD COLUMN IF NOT EXISTS informasi_nama_keluarga VARCHAR(50) DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS informasi_hubungan_keluarga VARCHAR(50) DEFAULT NULL;
