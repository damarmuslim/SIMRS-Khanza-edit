-- Migrasi untuk penyesuaian RMPenilaianAwalKeperawatanRanap.java (Tahap 5:
-- Status Sosial-Ekonomi - Status Pernikahan + Kerabat Terdekat, keduanya blm
-- ada sama sekali; + perluasan opsi Status Psikologi) dgn formulir fisik RM 19.
-- Aman dijalankan berulang.

-- Kolom baru (additive, menempel di ujung tabel - urutan kolom lama, termasuk
-- migrasi2 RM19 sebelumnya, TIDAK BOLEH berubah krn Sequel.menyimpantf()
-- INSERT posisional).
ALTER TABLE penilaian_awal_keperawatan_ranap
  ADD COLUMN IF NOT EXISTS status_pernikahan ENUM('Single','Menikah','Janda','Duda') DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS kerabat_nama VARCHAR(50) DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS kerabat_hubungan VARCHAR(50) DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS kerabat_telp VARCHAR(20) DEFAULT NULL;

-- Perluasan ENUM riwayat_psiko_kondisi_psiko (kolom LAMA, widget KondisiPsikologis
-- TIDAK diganti/disembunyikan - cuma nambah opsi safety-relevant yg blm ada di
-- kode tapi ada di formulir fisik: Acuh Tak Acuh/Sedih/Kecenderungan Bunuh Diri).
-- MODIFY COLUMN backward-compatible: 9 opsi lama tetap valid, data lama tidak
-- terpengaruh, cuma menambah nilai yg diperbolehkan.
ALTER TABLE penilaian_awal_keperawatan_ranap
  MODIFY COLUMN riwayat_psiko_kondisi_psiko ENUM('Tidak Ada Masalah','Marah','Takut','Depresi','Cepat Lelah','Cemas','Gelisah','Sulit Tidur','Acuh Tak Acuh','Sedih','Kecenderungan Bunuh Diri','Lain-lain') NOT NULL;
