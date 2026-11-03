-- Migrasi additive-only untuk penyesuaian RMEdukasiPasienKeluargaRawatJalan.java
-- & RMPelaksanaanInformasiEdukasi.java dengan formulir fisik RM 02-1
-- "FORMULIR EDUKASI PASIEN DAN KELUARGA" (RSUD Demang Sepulau Raya).
-- Aman dijalankan berulang (IF NOT EXISTS). Kolom baru sengaja ditambahkan
-- TANPA posisi eksplisit (AFTER ...) supaya menempel di UJUNG tabel -
-- kedua tabel disimpan lewat Sequel.menyimpantf()/mengedittf() yang
-- INSERT/UPDATE posisional, jadi urutan kolom lama TIDAK BOLEH berubah.

-- =====================================================================
-- Bagian A - Assessment Kebutuhan Edukasi (RMEdukasiPasienKeluargaRawatJalan.java)
-- Field lama (bicara/cara_belajar/penyakitnya_merupakan/dst) TETAP ADA di
-- tabel, cuma disembunyikan di form input (data lama tidak hilang) -
-- diganti field baru di bawah ini sesuai RM 02-1.
-- =====================================================================
ALTER TABLE edukasi_pasien_keluarga_rj
  ADD COLUMN IF NOT EXISTS tinggal_bersama_anak ENUM('Ya','Tidak') DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS tinggal_bersama_orang_tua ENUM('Ya','Tidak') DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS tinggal_bersama_sendiri ENUM('Ya','Tidak') DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS tinggal_bersama_suami_istri ENUM('Ya','Tidak') DEFAULT NULL,

  ADD COLUMN IF NOT EXISTS hambatan_ada ENUM('Ada','Tidak') DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS hambatan_gangguan_pendengaran ENUM('Ya','Tidak') DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS hambatan_gangguan_penglihatan ENUM('Ya','Tidak') DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS hambatan_gangguan_bicara ENUM('Ya','Tidak') DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS hambatan_gangguan_emosi ENUM('Ya','Tidak') DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS hambatan_motivasi_kurang ENUM('Ya','Tidak') DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS hambatan_memori_hilang ENUM('Ya','Tidak') DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS hambatan_fisik_lemah ENUM('Ya','Tidak') DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS hambatan_alkoholik ENUM('Ya','Tidak') DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS hambatan_perokok ENUM('Ya','Tidak') DEFAULT NULL,

  ADD COLUMN IF NOT EXISTS kepada_fisiologis_mampu_belajar ENUM('Ya','Tidak') DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS kepada_pasien ENUM('Ya','Tidak') DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS kepada_keluarga ENUM('Ya','Tidak') DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS keterangan_kepada_keluarga VARCHAR(50) DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS kepada_asing ENUM('Ya','Tidak') DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS keterangan_kepada_asing VARCHAR(50) DEFAULT NULL,

  ADD COLUMN IF NOT EXISTS kemampuan_bahasa ENUM('Indonesia','Asing') DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS keterangan_kemampuan_bahasa VARCHAR(50) DEFAULT NULL,

  ADD COLUMN IF NOT EXISTS baca_tulis ENUM('Bisa','Tidak Bisa') DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS kepercayaan_lainnya ENUM('Ada','Tidak Ada') DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS cara_edukasi ENUM('Lisan','Tulisan','Prosedur Pemeriksaan Penunjang') DEFAULT NULL,

  ADD COLUMN IF NOT EXISTS kebutuhan_hak_partisipasi ENUM('Ya','Tidak') DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS kebutuhan_informed_consent ENUM('Ya','Tidak') DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS kebutuhan_alat_medis_aman ENUM('Ya','Tidak') DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS kebutuhan_cuci_tangan ENUM('Ya','Tidak') DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS kebutuhan_rujukan_edukasi ENUM('Ya','Tidak') DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS kebutuhan_diet_nutrisi ENUM('Ya','Tidak') DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS kebutuhan_manajemen_nyeri ENUM('Ya','Tidak') DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS kebutuhan_bahaya_merokok ENUM('Ya','Tidak') DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS kebutuhan_kondisi_kesehatan_diagnosis ENUM('Ya','Tidak') DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS kebutuhan_penggunaan_obat ENUM('Ya','Tidak') DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS kebutuhan_teknik_rehabilitasi ENUM('Ya','Tidak') DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS kebutuhan_lain_lain ENUM('Ya','Tidak') DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS keterangan_kebutuhan_lain_lain VARCHAR(50) DEFAULT NULL;

-- =====================================================================
-- Bagian B - Edukasi / Pelaksanaan (RMPelaksanaanInformasiEdukasi.java)
-- hasil_verifikasi TIDAK diubah (dikonfirmasi user - formulir fisik yg
-- salah cetak menggabung "Sudah mengerti"/"Re-Edukasi Re-Demonstrasi"
-- jadi 1 opsi, sistem yg 3 opsi terpisah tetap dipertahankan).
-- =====================================================================
ALTER TABLE pelaksanaan_informasi_edukasi
  ADD COLUMN IF NOT EXISTS kode_leaflet VARCHAR(50) DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS tanggal_re_edukasi DATE DEFAULT NULL;

-- =====================================================================
-- [KOREKSI] "Secara Fisiologis Mampu Belajar" dipindah dari kelompok
-- "Edukasi Diberikan Kepada" ke kelompok "Hambatan" (sesuai koreksi user
-- stlh cek ulang foto formulir fisik). Kolom LAMA
-- `kepada_fisiologis_mampu_belajar`/`kepada_asing`/`keterangan_kepada_asing`
-- TIDAK dihapus/direname (migrasi ini dijalankan tak lama stlh migrasi
-- pertama, tapi tetap ikuti aturan additive-only sesi ini) - cuma TIDAK
-- dipakai lagi oleh form (Java berhenti mengisinya, selalu kirim
-- null/"Tidak"). "Asing" TIDAK ada di "Edukasi Diberikan Kepada" (cuma ada
-- di "Kemampuan Bahasa", kolom `kemampuan_bahasa` sudah ada, tidak perlu
-- kolom baru) - grup "Edukasi Diberikan Kepada" jadi cuma 2 pilihan:
-- Pasien/Keluarga.
-- =====================================================================
ALTER TABLE edukasi_pasien_keluarga_rj
  ADD COLUMN IF NOT EXISTS hambatan_fisiologis_mampu_belajar ENUM('Ya','Tidak') DEFAULT NULL;
