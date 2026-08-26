-- Migrasi additive-only untuk penyesuaian RMPenilaianAwalKeperawatanRanap.java
-- (RM19, poin 42) - 3 permintaan user, hanya 1 yg butuh kolom baru:
--   1. "Bahasa Isyarat" dipindah dari kelompok "b. Bahasa Sehari-hari" ke
--      "c. Dibutuhkan Penerjemah" - TIDAK ADA kolom baru, kolom lama
--      `edu_bahasa_isyarat` dipakai apa adanya (cuma posisi widget yg pindah).
--   2. "10. Perencanaan Pulang / Discharge Planning" ditambah 1 baris yg NAMA
--      KOMPONENnya diketik manual (di bawah baris "Dirujuk ke kelompok"),
--      lengkap dgn Ya/Tidak + keterangan -> 4 kolom baru di bawah ini.
--   3. Tab "Rencana Keperawatan" & "Rencana Keperawatan Lainnya" disembunyikan
--      - TIDAK ADA perubahan skema. Kolom `rencana` & tabel detail rencana
--      TIDAK disentuh sama sekali, data lama tetap utuh dan tinggal
--      ditampilkan lagi kalau suatu saat dibutuhkan.
--
-- Aman dijalankan berulang. Kolom baru menempel di UJUNG tabel - urutan kolom
-- lama TIDAK BOLEH berubah (Sequel.menyimpantf()/mengedittf() posisional).
-- URUTAN kolom di file ini HARUS sama persis dgn urutan di ganti()/simpan().
-- File ini WAJIB diimport SESUDAH sik_pengkajian_ranap_nyeri_fungsional_rm19.sql.

ALTER TABLE penilaian_awal_keperawatan_ranap
  ADD COLUMN IF NOT EXISTS discharge_manual_komponen VARCHAR(200) DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS discharge_manual_ya ENUM('Ya','Tidak') DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS discharge_manual_tidak ENUM('Ya','Tidak') DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS discharge_manual_ket VARCHAR(200) DEFAULT NULL;
