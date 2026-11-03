-- =====================================================================
-- RM 02-1  ASESMEN MEDIS AWAL RAWAT JALAN  - RSUD Demang Sepulau Raya
--
-- Migrasi ADDITIVE: hanya membuat SATU tabel baru. Tidak mengubah atau
-- menghapus apa pun yang sudah ada.
--
-- KENAPA TABEL BARU - keputusan user, dan alasannya tepat
-- Tiga tabel yang sudah ada sempat dipertimbangkan dan semuanya ditolak:
--
--   1. `asesmen_pendidikan_pasien_ranap` (RM 21). Blok PERENCANAAN PEMBERIAN
--      EDUKASI di kertas RM 02-1 isinya SAMA PERSIS dengan Section B RM 21,
--      jadi menggoda untuk dipakai ulang. Tapi PK-nya `no_rawat` SAJA - satu
--      pasien yang punya RM 21 dan RM 02-1 akan SALING MENIMPA. Alasan yang
--      sama dulu membuat RM 21 dipisah dari `edukasi_pasien_keluarga_rj`.
--
--   2. `edukasi_pasien_keluarga_rj`. Ini memang tabel edukasi RAWAT JALAN,
--      tapi bentuknya berbeda jauh: 66 kolom dengan penamaan lain
--      (bicara, cara_belajar, kepada_*, hambatan_*), dan TIDAK punya
--      "tingkatan pendidikan" yang diminta kertas ini.
--
--   3. `penilaian_medis_ralan`. Bagian klinisnya memang cocok (keluhan_utama,
--      rps, rpd, rpk, alergi, keadaan, nadi, rr, suhu, td, bb, tb, diagnosis,
--      tata), TAPI tabel itu dipakai form "Pengkajian Awal Medis Rawat Jalan"
--      bawaan Khanza yang bentuknya lain (I. Riwayat Kesehatan s.d.
--      VII. Konsul/Rujuk) beserta 11 tabel turunan per spesialisasi.
--      Menambah ~40 kolom ke sana akan mengganggu modul yang sudah jalan.
--
-- Konsekuensi yang disadari: RM 02-1 TIDAK berbagi data dengan ketiganya.
-- Form-form itu tidak disentuh sama sekali.
--
-- TIDAK ADA MIGRASI HAK AKSES - mengikuti RM 21, RM 33, dan RM 01: siapa pun
-- boleh mengisi, tidak ada kolom yang ditambahkan ke tabel `user`.
--
-- YANG TIDAK DISIMPAN: identitas pasien diambil lewat JOIN ke
-- `reg_periksa`/`pasien`, dan poliklinik dari `reg_periksa.kd_poli`.
-- =====================================================================

CREATE TABLE IF NOT EXISTS `asesmen_medis_awal_ralan` (
  `no_rawat` VARCHAR(17) NOT NULL,
  `tanggal`  DATE DEFAULT NULL,
  `jam`      TIME DEFAULT NULL,

  -- ===================================================================
  -- PERENCANAAN PEMBERIAN EDUKASI (baris 1-7 pada kertas)
  -- Susunannya sengaja dibuat sama dengan RM 21 supaya kedua lembar
  -- konsisten dan mudah dibandingkan - hanya tabelnya yang terpisah.
  -- ===================================================================
  `keyakinan_nilai` TEXT DEFAULT NULL,
  `budaya`          TEXT DEFAULT NULL,

  `kemampuan_membaca` ENUM('Tidak','Ya') DEFAULT NULL,

  `tingkat_pendidikan` ENUM('Tidak Sekolah','SD','SMP','SMA','Akademi','Sarjana','Lain-lain') DEFAULT NULL,
  `ket_tingkat_pendidikan` VARCHAR(100) DEFAULT NULL,

  -- Bahasa: kolom diisi HANYA kalau bahasanya dicentang; NULL berarti
  -- bahasa itu tidak dipakai pasien.
  `bahasa_indonesia` ENUM('Aktif','Pasif') DEFAULT NULL,
  `bahasa_inggris`   ENUM('Aktif','Pasif') DEFAULT NULL,
  `bahasa_daerah1`   VARCHAR(50) DEFAULT NULL,
  `kemampuan_daerah1` ENUM('Aktif','Pasif') DEFAULT NULL,
  `bahasa_daerah2`   VARCHAR(50) DEFAULT NULL,
  `kemampuan_daerah2` ENUM('Aktif','Pasif') DEFAULT NULL,

  `penerjemah`     ENUM('Tidak','Ya') DEFAULT NULL,
  `ket_penerjemah` VARCHAR(150) DEFAULT NULL,

  `hambatan_emosional`     ENUM('Tidak','Ya') DEFAULT NULL,
  `ket_hambatan_emosional` VARCHAR(150) DEFAULT NULL,
  `hambatan_motivasi`      ENUM('Tidak','Ya') DEFAULT NULL,
  `ket_hambatan_motivasi`  VARCHAR(150) DEFAULT NULL,
  `keterbatasan_fisik`     ENUM('Tidak','Ya') DEFAULT NULL,
  `ket_keterbatasan_fisik` VARCHAR(150) DEFAULT NULL,
  `keterbatasan_kognitif`  ENUM('Tidak','Ya') DEFAULT NULL,
  `ket_keterbatasan_kognitif` VARCHAR(150) DEFAULT NULL,

  `kesediaan_menerima` ENUM('Tidak','Ya') DEFAULT NULL,

  -- ===================================================================
  -- KLINIK
  -- Diprai-isi dari poliklinik pasien, tapi tetap DISIMPAN karena dokter
  -- boleh mengoreksinya - dan lembar ini ditandatangani, jadi yang tercetak
  -- harus yang disetujui dokter, bukan yang berubah sendiri kalau kelak
  -- poli pendaftaran dikoreksi.
  -- ===================================================================
  `klinik` ENUM('Penyakit Dalam','Bedah','Kebidanan & Kandungan','Anak',
                'Penyakit Syaraf','Penyakit Paru','Mata','Kulit & Kelamin',
                'THT','Gigi','Rehabilitasi Medik','Lain-lain') DEFAULT NULL,
  `ket_klinik` VARCHAR(100) DEFAULT NULL,

  -- ===================================================================
  -- RIWAYAT PENYAKIT DAHULU / ALERGI / SAKIT PADA KELUARGA
  -- Kertas memakai kotak centang untuk tiga penyakit yang paling sering
  -- (DM, Jantung, Hipertensi) plus baris kosong. Bentuk itu ditiru supaya
  -- centangnya benar-benar tercetak, bukan diperas jadi satu teks bebas
  -- seperti `penilaian_medis_ralan`.`rpd`.
  -- ===================================================================
  `rpd_ada`        ENUM('Tidak ada','Ada') DEFAULT NULL,
  `rpd_dm`         ENUM('Ya','Tidak') DEFAULT NULL,
  `rpd_jantung`    ENUM('Ya','Tidak') DEFAULT NULL,
  `rpd_hipertensi` ENUM('Ya','Tidak') DEFAULT NULL,
  `rpd_lain1`      VARCHAR(100) DEFAULT NULL,
  `rpd_lain2`      VARCHAR(100) DEFAULT NULL,

  `alergi_ada` ENUM('Tidak ada','Ada') DEFAULT NULL,
  `alergi1`    VARCHAR(100) DEFAULT NULL,
  `alergi2`    VARCHAR(100) DEFAULT NULL,
  `alergi3`    VARCHAR(100) DEFAULT NULL,
  `alergi4`    VARCHAR(100) DEFAULT NULL,

  `rpk_ada`        ENUM('Tidak ada','Ada') DEFAULT NULL,
  `rpk_dm`         ENUM('Ya','Tidak') DEFAULT NULL,
  `rpk_jantung`    ENUM('Ya','Tidak') DEFAULT NULL,
  `rpk_hipertensi` ENUM('Ya','Tidak') DEFAULT NULL,
  `rpk_lain1`      VARCHAR(100) DEFAULT NULL,
  `rpk_lain2`      VARCHAR(100) DEFAULT NULL,

  -- ===================================================================
  -- PENGKAJIAN RISIKO PASIEN JATUH (Skala Jatuh Get Up And Go Test)
  -- Dua kriteria saja, sesuai kertas. Tingkat risikonya TIDAK dibuatkan
  -- kolom: ia turunan murni dari kedua jawaban ("Tidak beresiko bila tidak
  -- ditemukan a dan b; Beresiko bila ditemukan salah satu"), jadi menyimpan
  -- hasilnya berarti menyimpan sesuatu yang bisa desinkron dari dasarnya.
  --
  -- Tabel `penilaian_lanjutan_resiko_jatuh_*` yang sudah ada semuanya
  -- LANJUTAN, bukan skrining awal ini - jadi tidak bisa dipakai.
  -- ===================================================================
  `jatuh_cara_berjalan` ENUM('Ya','Tidak') DEFAULT NULL,
  `jatuh_menopang`      ENUM('Ya','Tidak') DEFAULT NULL,

  -- ===================================================================
  -- ANAMNESIS & PEMERIKSAAN FISIK
  -- ===================================================================
  `keluhan_utama` TEXT DEFAULT NULL,
  `rps`           TEXT DEFAULT NULL,

  `keadaan_umum` VARCHAR(150) DEFAULT NULL,
  `nadi`         VARCHAR(10) DEFAULT NULL,
  `nafas`        VARCHAR(10) DEFAULT NULL,
  `suhu`         VARCHAR(10) DEFAULT NULL,
  `tensi_sistolik`  VARCHAR(10) DEFAULT NULL,
  `tensi_diastolik` VARCHAR(10) DEFAULT NULL,
  `bb`     VARCHAR(10) DEFAULT NULL,
  `tinggi` VARCHAR(10) DEFAULT NULL,
  `status_gizi` ENUM('Buruk','Kurang','Baik','Lebih') DEFAULT NULL,

  -- ===================================================================
  -- DIAGNOSIS & RENCANA
  -- ===================================================================
  `diagnosis1` VARCHAR(200) DEFAULT NULL,
  `diagnosis2` VARCHAR(200) DEFAULT NULL,
  `rencana_terapi` TEXT DEFAULT NULL,

  -- DPJP yang menandatangani. Tanda tangannya dipasang sebagai QR pada
  -- cetakan, pola RM 20/21/33/01.
  `kd_dokter` VARCHAR(20) DEFAULT NULL,

  PRIMARY KEY (`no_rawat`),
  KEY `idx_amar_dokter` (`kd_dokter`),
  CONSTRAINT `fk_amar_no_rawat` FOREIGN KEY (`no_rawat`)
      REFERENCES `reg_periksa` (`no_rawat`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
