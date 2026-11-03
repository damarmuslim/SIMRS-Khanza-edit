-- Migrasi additive-only untuk penyesuaian RMPenilaianAwalKeperawatanRanap.java
-- (Tahap 8: restrukturisasi urutan section sesuai formulir fisik - "1. Riwayat
-- Alergi" dipindah ke paling atas, "2. Alasan Masuk RS" digabung jadi 1 panel
-- berisi Anamnesis+Nama Keluarga+Hubungan+Keluhan Utama+Riwayat Kesehatan
-- Sekarang). Field "Keluhan Utama" SEBELUMNYA TIDAK ADA SAMA SEKALI - field
-- lain di section ini (Anamnesis/RPS) reuse kolom lama yg sudah ada
-- (informasi/ket_informasi/rps), TIDAK perlu kolom baru.
-- Aman dijalankan berulang. Kolom baru menempel di UJUNG tabel - urutan kolom
-- lama TIDAK BOLEH berubah (Sequel.menyimpantf()/mengedittf() posisional).

ALTER TABLE penilaian_awal_keperawatan_ranap
  ADD COLUMN IF NOT EXISTS keluhan_utama VARCHAR(200) DEFAULT NULL;
