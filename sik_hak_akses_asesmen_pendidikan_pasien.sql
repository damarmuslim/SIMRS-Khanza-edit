-- =====================================================================
-- Hak akses form RM 21 "Asesmen & Perencanaan Pendidikan Pasien"
--
-- WAJIB TANPA KLAUSA `AFTER`.
-- DlgUser.java membaca hak akses berdasarkan POSISI kolom (indeks JTable),
-- bukan berdasarkan nama. Menyisipkan kolom di tengah akan MENGGESER seluruh
-- hak akses sesudahnya sehingga user bisa mendapat izin form yang SALAH -
-- dampaknya keamanan, bukan sekadar data, dan gagalnya DIAM tanpa pesan error.
-- Menambah di akhir (perilaku bawaan ADD COLUMN) satu-satunya cara yang aman.
--
-- Jalankan SETELAH `sik_asesmen_pendidikan_pasien_ranap_rm21.sql`.
--
-- Kolom ini akan NULL untuk semua user lama -> getBoolean() -> false, jadi
-- TIDAK ADA yang bisa membuka form ini sampai admin mencentangnya di Setting
-- User. Itu perilaku yang benar, bukan bug. Akun "Admin Utama" otomatis
-- mendapat akses lewat pemberian hardcode di akses.java.
--
-- Cadangkan tabel `user` lebih dulu sebelum menjalankan DDL ini (pola yang
-- sudah dipakai saat migrasi hak akses RM 20 dan RM 17-01).
-- =====================================================================

ALTER TABLE `user`
  ADD COLUMN `asesmen_pendidikan_pasien` ENUM('true','false') DEFAULT NULL;
