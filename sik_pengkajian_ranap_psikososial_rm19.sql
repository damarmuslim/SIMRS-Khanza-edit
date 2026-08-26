-- Migrasi additive-only untuk penyesuaian RMPenilaianAwalKeperawatanRanap.java
-- (RM19-1/2) - restrukturisasi total "V. RIWAYAT PSIKOLOGIS - SOSIAL - EKONOMI -
-- BUDAYA - SPIRITUAL" (GEN lama) jadi "5. STATUS PSIKO-SOSIAL-EKONOMI DAN
-- SPIRITUAL" sesuai foto formulir fisik RM19-1/2:
--   a. Status Psikologi (checklist 8 item, ganti dropdown lama `KondisiPsikologis`)
--   b. Status Sosial-Ekonomi: Status Pernikahan (dipakai ulang, kolom lama
--      `status_pernikahan`), Hubungan dgn Keluarga, Tempat Tinggal (baru total),
--      Tinggal Bersama (baru total), Pekerjaan (baru total), Kebiasaan (baru
--      total, beda lokasi fisik dari Kebiasaan Merokok/Alkohol di "I. Riwayat
--      Kesehatan" yg sudah disembunyikan sebelumnya), Kerabat Terdekat (dipakai
--      ulang, kolom lama `kerabat_nama`/`kerabat_hubungan`/`kerabat_telp`)
--   c. Status Kepribadian (baru total - kebutuhan rohani/spiritual)
-- Field GEN lama (KondisiPsikologis/AdakahPerilaku/GangguanJiwa/
-- HubunganAnggotaKeluarga/Agama/TinggalDengan/PekerjaanPasien/CaraBayar/
-- NilaiKepercayaan/Bahasa/PendidikanPasien/PendidikanPJ/EdukasiPsikolgis) SEMUA
-- disembunyikan di Java, kolom DB lama (riwayat_psiko_*) TIDAK disentuh sama
-- sekali - data lama tetap aman. Aman dijalankan berulang. Kolom baru menempel
-- di UJUNG tabel - urutan kolom lama TIDAK BOLEH berubah
-- (Sequel.menyimpantf()/mengedittf() posisional).

ALTER TABLE penilaian_awal_keperawatan_ranap
  ADD COLUMN IF NOT EXISTS psiko_tak ENUM('Ya','Tidak') DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS psiko_marah ENUM('Ya','Tidak') DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS psiko_gelisah ENUM('Ya','Tidak') DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS psiko_sedih ENUM('Ya','Tidak') DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS psiko_cemas ENUM('Ya','Tidak') DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS psiko_takut ENUM('Ya','Tidak') DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS psiko_acuhtakacuh ENUM('Ya','Tidak') DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS psiko_bunuhdiri ENUM('Ya','Tidak') DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS sosek_hubkeluarga_baik ENUM('Ya','Tidak') DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS sosek_hubkeluarga_tidakbaik ENUM('Ya','Tidak') DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS sosek_tinggal_rumah ENUM('Ya','Tidak') DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS sosek_tinggal_asrama ENUM('Ya','Tidak') DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS sosek_tinggal_panti ENUM('Ya','Tidak') DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS sosek_tinggal_lainnya ENUM('Ya','Tidak') DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS sosek_tinggal_lainnya_ket VARCHAR(100) DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS sosek_bersama_suamiistri ENUM('Ya','Tidak') DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS sosek_bersama_anak ENUM('Ya','Tidak') DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS sosek_bersama_orangtua ENUM('Ya','Tidak') DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS sosek_bersama_lainnya ENUM('Ya','Tidak') DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS sosek_bersama_lainnya_ket VARCHAR(100) DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS sosek_kerja_bekerja ENUM('Ya','Tidak') DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS sosek_kerja_pns ENUM('Ya','Tidak') DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS sosek_kerja_swasta ENUM('Ya','Tidak') DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS sosek_kerja_tnipolri ENUM('Ya','Tidak') DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS sosek_kerja_tani_buruh ENUM('Ya','Tidak') DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS sosek_kebiasaan_merokok ENUM('Ya','Tidak') DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS sosek_kebiasaan_alkohol ENUM('Ya','Tidak') DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS sosek_kebiasaan_lainnya ENUM('Ya','Tidak') DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS sosek_kebiasaan_jenis_jumlah VARCHAR(100) DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS kepribadian_tidakadakeluhan ENUM('Ya','Tidak') DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS kepribadian_perlupendampingan ENUM('Ya','Tidak') DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS kepribadian_perludibantuibadah ENUM('Ya','Tidak') DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS kepribadian_berdoa ENUM('Ya','Tidak') DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS kepribadian_bacaliteratur ENUM('Ya','Tidak') DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS kepribadian_ganggu_praktik VARCHAR(255) DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS kepribadian_nilai_budaya VARCHAR(255) DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS kepribadian_lainlain VARCHAR(255) DEFAULT NULL;
