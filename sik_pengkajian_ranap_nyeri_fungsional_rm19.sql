-- Migrasi additive-only untuk penyesuaian RMPenilaianAwalKeperawatanRanap.java
-- (RM19-3, poin 41) - 3 permintaan user:
--   1. "6. PENAPISAN / SKRINING NYERI" disesuaikan dgn foto: P/Q/R/S/T jadi 5
--      isian bebas (field lama Penyebab/Kualitas/Lokasi/Menyebar/Skala Nyeri/
--      Durasi berupa combo terstruktur DISEMBUNYIKAN, kolom lamanya
--      `penilaian_nyeri*` TIDAK disentuh sama sekali - data lama tetap aman),
--      plus "a. Keluhan" & "b. Nyeri hilang, bila".
--   2. Gambar skala nyeri jadi KANVAS yg bisa dicoret pakai mouse - hasil
--      coretan disimpan di `nyeri_canvas_gambar` sbg PNG base64 (LONGTEXT).
--      Yg disimpan HANYA layer coretannya (PNG transparan), bukan gambar latar
--      skala nyeri - latar selalu diambil dari resource aplikasi
--      (/picture/nyeri.png), jadi ukurannya kecil (ratusan byte s/d bbrp KB)
--      dan latar bisa diganti kapan saja tanpa merusak data lama. Pasien yg
--      tidak ditandai disimpan sbg string kosong, bukan PNG blank.
--   3. "VII. PENGKAJIAN RESIKO JATUH" (Skala Morse + Sydney) DISEMBUNYIKAN,
--      digantikan "7. STATUS FUNGSIONAL" (Aktifitas total + Berjalan). Kolom
--      lama `penilaian_jatuhmorse_*` & `penilaian_jatuhsydney_*` (+ total-nya)
--      TIDAK dihapus & wiring-nya TIDAK disentuh - data lama tetap utuh.
--
-- CATATAN PENTING (bug yg diperbaiki bareng migrasi ini): 37 kolom poin 39
-- (psiko/sosek/kepribadian) sebelumnya ditulis di TENGAH daftar value
-- simpan()/ganti() (urutan ke-244..280), padahal scr FISIK kolomnya menempel di
-- UJUNG tabel. Krn simpan() memakai Sequel.menyimpantf() yg INSERT-nya
-- POSISIONAL murni (tanpa nama kolom), nilainya akan masuk ke kolom yg SALAH
-- begitu migrasi psikososial diimport. Sudah diperbaiki di sisi Java (urutan
-- value dipindah ke ujung, cocok dgn urutan fisik) - TIDAK ada perubahan skema
-- yg diperlukan utk perbaikan itu. ganti() sendiri aman krn SET-nya pakai nama
-- kolom, itu sebabnya bug ini tidak terlihat sblm migrasi diimport.
--
-- Aman dijalankan berulang. Kolom baru menempel di UJUNG tabel - urutan kolom
-- lama TIDAK BOLEH berubah (Sequel.menyimpantf()/mengedittf() posisional).
-- URUTAN kolom di file ini HARUS sama persis dgn urutan di ganti()/simpan().

ALTER TABLE penilaian_awal_keperawatan_ranap
  ADD COLUMN IF NOT EXISTS nyeri_canvas_gambar LONGTEXT DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS nyeri_p VARCHAR(200) DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS nyeri_q VARCHAR(200) DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS nyeri_r VARCHAR(200) DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS nyeri_s VARCHAR(200) DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS nyeri_t VARCHAR(200) DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS keluhan_nyeri_kronik ENUM('Ya','Tidak') DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS keluhan_nyeri_akut ENUM('Ya','Tidak') DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS keluhan_tidak_ada_nyeri ENUM('Ya','Tidak') DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS nyerihilang_minum_obat ENUM('Ya','Tidak') DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS nyerihilang_istirahat ENUM('Ya','Tidak') DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS nyerihilang_berubah_posisi ENUM('Ya','Tidak') DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS nyerihilang_mendengar_musik ENUM('Ya','Tidak') DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS nyerihilang_lainlain ENUM('Ya','Tidak') DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS nyerihilang_lainlain_ket VARCHAR(200) DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS fungsional_mandiri ENUM('Ya','Tidak') DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS fungsional_bantuan_minimal ENUM('Ya','Tidak') DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS fungsional_bantuan_sebagian ENUM('Ya','Tidak') DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS fungsional_ketergantungan ENUM('Ya','Tidak') DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS fungsional_berjalan_normal ENUM('Ya','Tidak') DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS fungsional_berjalan_perlu_bantuan ENUM('Ya','Tidak') DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS fungsional_berjalan_sering_jatuh ENUM('Ya','Tidak') DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS fungsional_berjalan_kelumpuhan ENUM('Ya','Tidak') DEFAULT NULL;
