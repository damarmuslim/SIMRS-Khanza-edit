-- Migrasi additive-only untuk penyesuaian RMPenilaianAwalKeperawatanRanap.java
-- (RM19-1/2, lanjutan poin 37) - 6 perbaikan sesuai foto formulir fisik:
--   1. "Screening Prostat" ditambah judul "Pria" (visual saja, tidak ada kolom baru).
--   2. Pupil Kanan/Kiri dipindah dari blok "Status Reproduksi" ke "f. Status
--      Neurosensori" (di bawah Konsentrasi) - kolom lama `pupil_kanan_ukuran` dst
--      TIDAK diubah sama sekali, cuma posisi widget di form yang pindah.
--   3. "Kekuatan Otot" ditambah field input nilai (sebelum "Tremor di").
--   4. "g. Fungsi Ekstremitas" ditambah field input "Lain-lain".
--   5. "Skala Branden" dipindah dari "Status Reproduksi" ke "h. Kulit dan Kelamin"
--      - kolom lama `skala_branden` TIDAK diubah, cuma posisi widget yang pindah.
--   6. "Integritas Kulit" ditambah checkbox "Kemerahan" + field keterangan.
-- Aman dijalankan berulang. Kolom baru menempel di UJUNG tabel - urutan kolom
-- lama TIDAK BOLEH berubah (Sequel.menyimpantf()/mengedittf() posisional).

ALTER TABLE penilaian_awal_keperawatan_ranap
  ADD COLUMN IF NOT EXISTS kekuatanotot_nilai VARCHAR(50) DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS ekstremitas_lain VARCHAR(100) DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS integritaskulit_kemerahan ENUM('Ya','Tidak') DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS integritaskulit_kemerahan_di VARCHAR(100) DEFAULT NULL;
