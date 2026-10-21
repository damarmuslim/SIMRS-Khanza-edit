-- Migrasi additive-only untuk penyesuaian RMRekonsiliasiObat.java dengan
-- formulir fisik RM 03-06A "REKONSILIASI OBAT" (RSUD Demang Sepulau Raya).
-- Aman dijalankan berulang (IF NOT EXISTS). Kolom baru sengaja ditambahkan
-- TANPA posisi eksplisit (AFTER ...) supaya menempel di UJUNG tabel -
-- rekonsiliasi_obat disimpan lewat Sequel.menyimpantf() yang INSERT
-- posisional tanpa nama kolom (lihat BtnSimpanActionPerformed di
-- RMRekonsiliasiObat.java), jadi urutan 8 kolom lama TIDAK BOLEH berubah.

ALTER TABLE rekonsiliasi_obat
  ADD COLUMN IF NOT EXISTS penggunaan_obat_sebelum_admisi ENUM('Ya','Tidak') DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS sumber_informasi ENUM('Status Pasien di Rekam Medis','Pasien/Keluarga Pasien','Lain-lain') DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS sumber_informasi_lain VARCHAR(100) DEFAULT NULL;

-- Perluasan enum ADDITIF - nilai lama 'Lanjut'/'Stop' (data lama) TETAP
-- valid, 2 nilai baru ditambahkan sesuai kolom "Tindak Lanjut Oleh DPJP"
-- pada formulir fisik.
ALTER TABLE rekonsiliasi_obat_detail_obat
  MODIFY COLUMN tindak_lanjut ENUM('Lanjut','Stop','Lanjut aturan pakai sama','Lanjut aturan pakai berubah','Baru') DEFAULT NULL;
