-- =====================================================================
-- Hak akses form RM 17-01 "Ringkasan Pasien Masuk Keluar"
--
-- WAJIB TANPA KLAUSA `AFTER`.
-- Kode di DlgUser.java membaca hak akses berdasarkan POSISI kolom (indeks
-- JTable), bukan berdasarkan nama. Menyisipkan kolom di tengah akan
-- MENGGESER seluruh hak akses sesudahnya, sehingga user bisa mendapat izin
-- form yang SALAH - dampaknya keamanan, bukan sekadar data. Menambah di
-- akhir (perilaku bawaan ADD COLUMN) adalah satu-satunya cara yang aman.
--
-- Jalankan SETELAH `sik_ringkasan_pasien_masuk_keluar_rm17.sql`.
--
-- Kolom ini akan NULL untuk semua user lama -> getBoolean() -> false, jadi
-- TIDAK ADA yang bisa membuka form ini sampai admin mencentangnya di
-- Setting User. Itu perilaku yang benar, bukan bug. Akun "Admin Utama"
-- otomatis mendapat akses lewat pemberian hardcode di akses.java.
--
-- Disarankan mencadangkan tabel `user` lebih dulu sebelum menjalankan DDL
-- ini (pola yang sama sudah dipakai saat migrasi hak akses RM 20).
-- =====================================================================

ALTER TABLE `user`
  ADD COLUMN `ringkasan_pasien_masuk_keluar` ENUM('true','false') DEFAULT NULL;
