-- Migrasi additive-only untuk penyesuaian RMPenilaianAwalKeperawatanRanap.java
-- (Tahap 1: Riwayat Alergi + Riwayat Kesehatan) dengan formulir fisik RM 19
-- "PENGKAJIAN AWAL KEPERAWATAN RAWAT INAP" (RSUD Demang Sepulau Raya).
-- Aman dijalankan berulang (IF NOT EXISTS). Kolom baru sengaja ditambahkan
-- TANPA posisi eksplisit (AFTER ...) supaya menempel di UJUNG tabel -
-- tabel disimpan lewat Sequel.menyimpantf()/mengedittf() yang INSERT/UPDATE
-- posisional, jadi urutan kolom lama TIDAK BOLEH berubah.
--
-- Field lama (riwayat_alergi/rpd/rpk) TETAP ADA di tabel & TIDAK disentuh -
-- cuma disembunyikan di form input (data riwayat lama tidak hilang), diganti
-- checklist baru di bawah ini sesuai RM 19.

-- =====================================================================
-- 1. Riwayat Alergi / Reaksi
-- =====================================================================
ALTER TABLE penilaian_awal_keperawatan_ranap
  ADD COLUMN IF NOT EXISTS alergi_tidak_ada ENUM('Ya','Tidak') DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS alergi_obat ENUM('Ya','Tidak') DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS alergi_obat_sebutkan VARCHAR(50) DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS alergi_obat_reaksi VARCHAR(100) DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS alergi_makanan ENUM('Ya','Tidak') DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS alergi_makanan_sebutkan VARCHAR(50) DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS alergi_makanan_reaksi VARCHAR(100) DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS alergi_lainnya ENUM('Ya','Tidak') DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS alergi_lainnya_sebutkan VARCHAR(50) DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS alergi_lainnya_reaksi VARCHAR(100) DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS alergi_tidak_diketahui ENUM('Ya','Tidak') DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS alergi_tidak_diketahui_reaksi VARCHAR(100) DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS alergi_tanda_dipasang ENUM('Ya','Tidak') DEFAULT NULL,

-- =====================================================================
-- 3a. Riwayat Kesehatan yang Lalu (RPD)
-- =====================================================================
  ADD COLUMN IF NOT EXISTS rpd_hipertensi ENUM('Ya','Tidak') DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS rpd_dm ENUM('Ya','Tidak') DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS rpd_jantung ENUM('Ya','Tidak') DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS rpd_hepatitis ENUM('Ya','Tidak') DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS rpd_operasi ENUM('Ya','Tidak') DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS rpd_anemia ENUM('Ya','Tidak') DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS rpd_asthma ENUM('Ya','Tidak') DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS rpd_stroke ENUM('Ya','Tidak') DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS rpd_paru ENUM('Ya','Tidak') DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS rpd_lainlain ENUM('Ya','Tidak') DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS rpd_lainlain_sebutkan VARCHAR(50) DEFAULT NULL,

-- =====================================================================
-- 3b. Riwayat Kesehatan Keluarga (RPK)
-- =====================================================================
  ADD COLUMN IF NOT EXISTS rpk_hipertensi ENUM('Ya','Tidak') DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS rpk_dm ENUM('Ya','Tidak') DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS rpk_jantung ENUM('Ya','Tidak') DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS rpk_thalasemia ENUM('Ya','Tidak') DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS rpk_lainlain ENUM('Ya','Tidak') DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS rpk_lainlain_sebutkan VARCHAR(50) DEFAULT NULL;
