-- Migrasi PERBAIKAN BATAS BARIS InnoDB untuk tabel penilaian_awal_keperawatan_ranap.
--
-- MASALAH: tabel ini SUDAH melewati batas keras InnoDB (max row size 8126 byte
-- utk innodb_page_size=16384) pada kondisi 442 kolom - BUKAN gara-gara migrasi
-- RM19 yg baru. Buktinya: `ALTER TABLE ... ADD COLUMN` apapun ditolak dgn
-- ERROR 1118, bahkan `CREATE TABLE x LIKE penilaian_awal_keperawatan_ranap`
-- pun gagal. ROW_FORMAT sudah `Dynamic`, jadi perbaikan lazim "ganti ke DYNAMIC"
-- TIDAK berlaku di sini.
--
-- SEBAB: worst-case baris ~8.7KB (130 VARCHAR = 8224 char latin1 + 289 ENUM +
-- 19 tinyint + dll). Di ROW_FORMAT=Dynamic, hanya kolom variable-length >40 byte
-- yg bisa didorong off-page (pointer 20 byte); VARCHAR pendek & semua ENUM
-- WAJIB inline.
--
-- SOLUSI: ubah setiap VARCHAR panjang (>40 char) jadi TEXT, sehingga cuma
-- menyisakan pointer 20 byte di baris. Sesudah ini worst-case turun ke ~3.4KB
-- dari 8126 - lega utk penambahan kolom berikutnya.
--
-- AMAN utk codebase ini krn:
--   (a) URUTAN kolom TIDAK berubah - kritis, `Sequel.menyimpantf()` INSERT-nya
--       POSISIONAL murni tanpa nama kolom;
--   (b) `Sequel` mengirim & membaca semuanya sbg String, jadi TEXT berperilaku
--       identik dari sisi Java;
--   (c) TIDAK ada satupun kolom di bawah ini yg terindeks (indeks tabel ini cuma
--       no_rawat[PK], nip1, nip2, kd_dokter - semuanya kolom pendek, tidak ikut
--       dikonversi).
--
-- CATATAN: `innodb_strict_mode=OFF` BUKAN pengganti migrasi ini - dgn strict off
-- skema memang bisa dibuat, TAPI baris GAGAL disimpan saat field panjang terisi
-- banyak (sudah diuji & terbukti gagal). Migrasi ini memperbaiki sebabnya.
--
-- Dijalankan SEBELUM 5 migrasi RM19 (pernafasan_kemih -> neurosensori ->
-- psikososial -> nyeri_fungsional -> discharge_manual).

ALTER TABLE penilaian_awal_keperawatan_ranap
  MODIFY COLUMN `rps` TEXT NOT NULL,
  MODIFY COLUMN `rpd` TEXT NOT NULL,
  MODIFY COLUMN `rpk` TEXT NOT NULL,
  MODIFY COLUMN `rpo` TEXT NOT NULL,
  MODIFY COLUMN `pemeriksaan_susunan_kepala_keterangan` TEXT NOT NULL,
  MODIFY COLUMN `pemeriksaan_susunan_wajah_keterangan` TEXT NOT NULL,
  MODIFY COLUMN `pemeriksaan_susunan_kejang_keterangan` TEXT NOT NULL,
  MODIFY COLUMN `pemeriksaan_kardiovaskuler_sirkulasi_keterangan` TEXT NOT NULL,
  MODIFY COLUMN `pemeriksaan_respirasi_jenis_pernafasan_keterangan` TEXT NOT NULL,
  MODIFY COLUMN `pemeriksaan_gastrointestinal_mulut_keterangan` TEXT NOT NULL,
  MODIFY COLUMN `pemeriksaan_gastrointestinal_gigi_keterangan` TEXT NOT NULL,
  MODIFY COLUMN `pemeriksaan_gastrointestinal_lidah_keterangan` TEXT NOT NULL,
  MODIFY COLUMN `pemeriksaan_gastrointestinal_tenggorokan_keterangan` TEXT NOT NULL,
  MODIFY COLUMN `pemeriksaan_gastrointestinal_abdomen_keterangan` TEXT NOT NULL,
  MODIFY COLUMN `pemeriksaan_neurologi_pengelihatan_keterangan` TEXT NOT NULL,
  MODIFY COLUMN `pemeriksaan_neurologi_bicara_keterangan` TEXT NOT NULL,
  MODIFY COLUMN `pemeriksaan_muskuloskletal_nyeri_sendi_keterangan` TEXT NOT NULL,
  MODIFY COLUMN `pemeriksaan_muskuloskletal_oedema_keterangan` TEXT NOT NULL,
  MODIFY COLUMN `pemeriksaan_muskuloskletal_fraktur_keterangan` TEXT NOT NULL,
  MODIFY COLUMN `penilaian_nyeri_ket_penyebab` TEXT NOT NULL,
  MODIFY COLUMN `penilaian_nyeri_ket_kualitas` TEXT NOT NULL,
  MODIFY COLUMN `penilaian_nyeri_lokasi` TEXT NOT NULL,
  MODIFY COLUMN `penilaian_nyeri_ket_hilang` TEXT NOT NULL,
  MODIFY COLUMN `skrining_gizi_ket_diagnosa_khusus` TEXT NULL DEFAULT NULL,
  MODIFY COLUMN `rencana` TEXT NULL DEFAULT NULL,
  MODIFY COLUMN `alergi_obat_sebutkan` TEXT NULL DEFAULT NULL,
  MODIFY COLUMN `alergi_obat_reaksi` TEXT NULL DEFAULT NULL,
  MODIFY COLUMN `alergi_makanan_sebutkan` TEXT NULL DEFAULT NULL,
  MODIFY COLUMN `alergi_makanan_reaksi` TEXT NULL DEFAULT NULL,
  MODIFY COLUMN `alergi_lainnya_sebutkan` TEXT NULL DEFAULT NULL,
  MODIFY COLUMN `alergi_lainnya_reaksi` TEXT NULL DEFAULT NULL,
  MODIFY COLUMN `alergi_tidak_diketahui_reaksi` TEXT NULL DEFAULT NULL,
  MODIFY COLUMN `rpd_lainlain_sebutkan` TEXT NULL DEFAULT NULL,
  MODIFY COLUMN `rpk_lainlain_sebutkan` TEXT NULL DEFAULT NULL,
  MODIFY COLUMN `informasi_nama_keluarga` TEXT NULL DEFAULT NULL,
  MODIFY COLUMN `informasi_hubungan_keluarga` TEXT NULL DEFAULT NULL,
  MODIFY COLUMN `kerabat_nama` TEXT NULL DEFAULT NULL,
  MODIFY COLUMN `kerabat_hubungan` TEXT NULL DEFAULT NULL,
  MODIFY COLUMN `edu_bicara_jelaskan` TEXT NULL DEFAULT NULL,
  MODIFY COLUMN `edu_bahasa_daerah_ket` TEXT NULL DEFAULT NULL,
  MODIFY COLUMN `edu_bahasa_asing_ket` TEXT NULL DEFAULT NULL,
  MODIFY COLUMN `edu_penerjemah_bahasa` TEXT NULL DEFAULT NULL,
  MODIFY COLUMN `edu_kebutuhan_lainlain_ket` TEXT NULL DEFAULT NULL,
  MODIFY COLUMN `discharge_homecare_ket` TEXT NULL DEFAULT NULL,
  MODIFY COLUMN `discharge_perawatanluka_ket` TEXT NULL DEFAULT NULL,
  MODIFY COLUMN `discharge_alatbantu_ket` TEXT NULL DEFAULT NULL,
  MODIFY COLUMN `discharge_pemesananalat_ket` TEXT NULL DEFAULT NULL,
  MODIFY COLUMN `discharge_timterapis_ket` TEXT NULL DEFAULT NULL,
  MODIFY COLUMN `discharge_gizi_ket` TEXT NULL DEFAULT NULL,
  MODIFY COLUMN `discharge_kelompok_ket` TEXT NULL DEFAULT NULL,
  MODIFY COLUMN `data_penunjang1` TEXT NULL DEFAULT NULL,
  MODIFY COLUMN `data_penunjang2` TEXT NULL DEFAULT NULL,
  MODIFY COLUMN `data_penunjang3` TEXT NULL DEFAULT NULL,
  MODIFY COLUMN `data_penunjang4` TEXT NULL DEFAULT NULL,
  MODIFY COLUMN `pupil_kanan_reaksi` TEXT NULL DEFAULT NULL,
  MODIFY COLUMN `pupil_kiri_reaksi` TEXT NULL DEFAULT NULL,
  MODIFY COLUMN `keluhan_utama` TEXT NULL DEFAULT NULL,
  MODIFY COLUMN `airway_rtt_ukuran` TEXT NULL DEFAULT NULL,
  MODIFY COLUMN `o2_liter_menit` TEXT NULL DEFAULT NULL,
  MODIFY COLUMN `o2_saturasi` TEXT NULL DEFAULT NULL,
  MODIFY COLUMN `cardio_lain` TEXT NULL DEFAULT NULL,
  MODIFY COLUMN `gi_pembatasan_makanan_ket` TEXT NULL DEFAULT NULL,
  MODIFY COLUMN `gi_muntah_kali` TEXT NULL DEFAULT NULL,
  MODIFY COLUMN `gi_diare_x` TEXT NULL DEFAULT NULL,
  MODIFY COLUMN `gi_ivfd_tts` TEXT NULL DEFAULT NULL,
  MODIFY COLUMN `kemih_lain` TEXT NULL DEFAULT NULL,
  MODIFY COLUMN `penglihatan_lain` TEXT NULL DEFAULT NULL,
  MODIFY COLUMN `kekuatanotot_tremor_di` TEXT NULL DEFAULT NULL,
  MODIFY COLUMN `hemiplegi_di` TEXT NULL DEFAULT NULL,
  MODIFY COLUMN `parase_di` TEXT NULL DEFAULT NULL,
  MODIFY COLUMN `warnakulit_lain` TEXT NULL DEFAULT NULL,
  MODIFY COLUMN `integritaskulit_melepuh_di` TEXT NULL DEFAULT NULL
;
