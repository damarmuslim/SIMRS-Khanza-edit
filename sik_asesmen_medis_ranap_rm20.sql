-- =====================================================================
-- RM 20 "ASESMEN MEDIS RAWAT INAP" (RSUD Demang Sepulau Raya)
-- Tabel data untuk form BARU rekammedis/RMAsesmenMedisRanap.java
-- =====================================================================
--
-- Tabel ini SENGAJA TERPISAH dari `penilaian_medis_ranap` (form lama
-- "Awal Medis Ranap Umum" / RMPenilaianAwalMedisRanapDewasa.java).
-- Alasannya bukan gaya, tapi teknis: `penilaian_medis_ranap` PK-nya
-- `no_rawat` saja = 1 record per episode rawat. Kalau RM 20 numpang di
-- tabel itu, kedua form akan berebut baris yang SAMA - dokter isi RM 20,
-- lalu form lama dibuka, getData() menimpa, Simpan -> data RM 20 hilang.
--
-- Keputusan user (Skenario A): form lama TETAP UTUH, tidak ada satupun
-- kolom/menu/form yang disembunyikan atau diganti. RM 20 murni tambahan.
--
-- Konsekuensi yang disadari: kedua form TIDAK berbagi data.
--
-- Struktur mengikuti lembar fisik RM 20 (1 halaman):
--   Tanggal/Jam -> I. ANAMNESA -> PENGKAJIAN MEDIS / II. PEMERIKSAAN
--   MEDIS -> DIAGNOSA KERJA -> RENCANA DAN TERAPI -> 2 tanda tangan.
--
-- CATATAN untuk sesi berikutnya: form ini memakai INSERT BERNAMA KOLOM
-- (koneksi.prepareStatement langsung), BUKAN Sequel.menyimpantf() yang
-- posisional. Jadi penambahan kolom di kemudian hari TIDAK wajib
-- menempel di ujung tabel - beda dari `penilaian_medis_ranap` dan
-- `penilaian_awal_keperawatan_ranap` yang posisional.
-- =====================================================================

CREATE TABLE IF NOT EXISTS `asesmen_medis_ranap` (

  -- ---------- Header ----------
  `no_rawat` varchar(17) NOT NULL,
  -- Tanggal + Jam pada kertas ditampung satu kolom DATETIME.
  `tanggal` datetime NOT NULL,
  -- Kertas punya DUA tanda tangan dokter yang berbeda perannya.
  -- Form lama hanya punya satu `kd_dokter` - ini penambahan nyata.
  `kd_dokter_periksa` varchar(20) NOT NULL,
  `kd_dokter_dpjp` varchar(20) NOT NULL,

  -- ---------- I. ANAMNESA ----------
  `keluhan_utama` text NOT NULL,
  -- "Keluhan Tambahan" belum pernah ada di form lama.
  `keluhan_tambahan` text NOT NULL,
  `alergi` varchar(200) NOT NULL DEFAULT '',
  -- Kertas: "RIWAYAT PERJALANAN PENYAKIT RIWAYAT PENGOBATAN
  -- ( Sendiri / Keluarga )" - satu blok naratif + penanda sumbernya.
  -- Di form lama informasi ini terpecah jadi 4 kolom terpisah
  -- (rps/rpd/rpk/rpo); di sini sengaja mengikuti kertas.
  `sumber_riwayat` enum('Sendiri','Keluarga') DEFAULT NULL,
  `riwayat_penyakit_pengobatan` text NOT NULL,

  -- ---------- II. PEMERIKSAAN MEDIS ----------
  -- Kertas menyediakan titik-titik bebas, tapi dibuat dropdown baku
  -- (keputusan user) supaya konsisten dgn form lain & bisa dianalisis.
  -- Nilai enum-nya disamakan PERSIS dengan `penilaian_medis_ranap`.
  `keadaan_umum` enum('Sehat','Sakit Ringan','Sakit Sedang','Sakit Berat') DEFAULT NULL,
  `kesadaran_mental` enum('Compos Mentis','Apatis','Somnolen','Sopor','Koma') DEFAULT NULL,
  -- Kertas menulis "GSC : E ..... M ...... V ......" - "GSC" itu typo
  -- lazim dari GCS, dan urutannya E-M-V (form lama label-nya "GCS(E,V,M)"
  -- dan cuma 1 textbox gabungan). Di sini dipecah 3 dan urutannya
  -- mengikuti kertas.
  `gcs_e` varchar(2) NOT NULL DEFAULT '',
  `gcs_m` varchar(2) NOT NULL DEFAULT '',
  `gcs_v` varchar(2) NOT NULL DEFAULT '',
  -- Kertas: "Tensi : ...../.....mmHg" - dua kotak, jadi dipecah dua kolom
  -- (form lama menyatukannya jadi satu varchar(8)).
  `td_sistol` varchar(4) NOT NULL DEFAULT '',
  `td_diastol` varchar(4) NOT NULL DEFAULT '',
  `nadi` varchar(5) NOT NULL DEFAULT '',
  `rr` varchar(5) NOT NULL DEFAULT '',
  `suhu` varchar(5) NOT NULL DEFAULT '',
  `tb` varchar(5) NOT NULL DEFAULT '',
  `bb` varchar(5) NOT NULL DEFAULT '',
  -- `umur` & `imt` DIHITUNG otomatis oleh aplikasi, tapi TETAP DISIMPAN
  -- sebagai snapshot saat asesmen (permintaan user: jejak audit).
  -- Disimpan sbg teks, bukan angka, karena umur berformat "36 Th 2 Bl".
  `umur` varchar(30) NOT NULL DEFAULT '',
  `imt` varchar(8) NOT NULL DEFAULT '',
  -- Kertas: "Status pediatric : Gizi Kurang / Baik / Lebih".
  -- Nullable: untuk pasien anak (<18 th) aplikasi SENGAJA tidak mengisi
  -- otomatis karena ambang IMT dewasa salah secara klinis untuk anak -
  -- dokter yang memutuskan. Lihat catatan di RMAsesmenMedisRanap.java.
  `status_pediatrik` enum('Gizi Kurang','Baik','Gizi Lebih') DEFAULT NULL,

  -- ---------- DIAGNOSA KERJA ----------
  -- Kertas menyediakan 3 baris bernomor (form lama cuma 1 field bebas).
  `diagnosa_kerja_1` varchar(200) NOT NULL DEFAULT '',
  `diagnosa_kerja_2` varchar(200) NOT NULL DEFAULT '',
  `diagnosa_kerja_3` varchar(200) NOT NULL DEFAULT '',

  -- ---------- RENCANA DAN TERAPI ----------
  -- Kertas: satu kotak besar "RENCANA DAN TERAPI (pemeriksaan Penunjang
  -- & Pengobatannya)". Di form lama ini terpecah jadi lab/rad/penunjang/
  -- tata; di sini sengaja digabung mengikuti kertas.
  `rencana_terapi` text NOT NULL,

  PRIMARY KEY (`no_rawat`),
  KEY `kd_dokter_periksa` (`kd_dokter_periksa`),
  KEY `kd_dokter_dpjp` (`kd_dokter_dpjp`),
  CONSTRAINT `asesmen_medis_ranap_ibfk_1` FOREIGN KEY (`no_rawat`) REFERENCES `reg_periksa` (`no_rawat`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `asesmen_medis_ranap_ibfk_2` FOREIGN KEY (`kd_dokter_periksa`) REFERENCES `dokter` (`kd_dokter`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `asesmen_medis_ranap_ibfk_3` FOREIGN KEY (`kd_dokter_dpjp`) REFERENCES `dokter` (`kd_dokter`) ON DELETE CASCADE ON UPDATE CASCADE

) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci ROW_FORMAT=DYNAMIC;
