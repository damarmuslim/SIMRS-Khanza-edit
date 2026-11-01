-- Timestamp Pengkaji 1 tetap memakai kolom lama `tanggal`.
-- Timestamp Perawat Penanggung Jawab disimpan terpisah agar kedua petugas
-- mempunyai tanggal dan jam masing-masing.
--
-- Kolom nullable dan ditempel di ujung tabel untuk menjaga urutan seluruh
-- kolom lama yang dipakai INSERT posisional RMPenilaianAwalKeperawatanRanap.

ALTER TABLE penilaian_awal_keperawatan_ranap
  ADD COLUMN IF NOT EXISTS tanggal_penanggung_jawab DATETIME DEFAULT NULL;
