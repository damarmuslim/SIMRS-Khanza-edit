-- Migrasi additive-only untuk penyesuaian RMPenilaianAwalKeperawatanRanap.java
-- (pengelompokan ulang field Pernafasan/Bunyi Paru & tambahan Perkemihan
-- pada RM19-1/2) agar sesuai formulir fisik:
--   1. Ngorok dipindah ke kelompok Breathing (Fungsi Pernafasan).
--   2. Orthopnea dipindah ke kelompok Bunyi Paru.
--   3. Breathing & Bunyi Paru masing-masing ditambah field "Lain-lain"
--      (checkbox + keterangan bebas), meniru pola RTT pada Airway.
--   4. Saturasi O2 (field %) digabung ke baris "Penggunaan otot bantu nafas".
--   5. Kelompok Fungsi Perkemihan ditambah checkbox "Poliuri".
-- Kolom lama `bunyiparu_ngorok` & `breathing_orthopnea` TETAP DIPAKAI APA
-- ADANYA (hanya variabel Java yang di-rename ke kelompok yang benar) -
-- TIDAK ADA rename kolom DB untuk menghindari risiko pada data yang sudah
-- terimport. Aman dijalankan berulang. Kolom baru menempel di UJUNG tabel -
-- urutan kolom lama TIDAK BOLEH berubah (Sequel.menyimpantf()/mengedittf()
-- posisional).

ALTER TABLE penilaian_awal_keperawatan_ranap
  ADD COLUMN IF NOT EXISTS breathing_lain ENUM('Ya','Tidak') DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS breathing_lain_ket VARCHAR(100) DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS bunyiparu_lain ENUM('Ya','Tidak') DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS bunyiparu_lain_ket VARCHAR(100) DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS kemih_poliuri ENUM('Ya','Tidak') DEFAULT NULL;
