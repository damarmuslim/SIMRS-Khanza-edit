-- =====================================================================
-- RM 20-1 "ASESMEN MEDIS RAWAT INAP (LANJUTAN)" - RSUD Demang Sepulau Raya
-- Tabel data untuk form rekammedis/RMAsesmenMedisRanap20_1.java
-- =====================================================================
--
-- Merupakan lembar lanjutan dari RM 20 (Asesmen Medis Rawat Inap).
-- Berfokus pada:
--   I.  Pemeriksaan Fisik a-n (14 regio sistematis + keterangan)
--   II. Asesmen & Program (Diagnosa Medis/Banding, Advis Terapi & Rencana Tindakan)
--
-- Tabel ini SENGAJA DIBUAT TERPISAH mengikuti pola isolasi modul:
--   - `asesmen_medis_ranap` (RM 20): Anamnesa & TTV
--   - `asesmen_medis_ranap_rm20_1` (RM 20-1): Pemeriksaan Fisik & Asesmen Program
-- Keduanya berelasi langsung ke `reg_periksa` (`no_rawat`).
--
-- Migrasi ini 100% ADDITIVE: tidak mengubah atau menghapus tabel lain.
-- =====================================================================

CREATE TABLE IF NOT EXISTS `asesmen_medis_ranap_rm20_1` (
  `no_rawat` varchar(17) NOT NULL,
  `tanggal` datetime NOT NULL,
  `kd_dokter_periksa` varchar(20) NOT NULL,
  `kd_dokter_dpjp` varchar(20) NOT NULL,
  `anamnesis` enum('Autoanamnesis','Aloanamnesis') NOT NULL DEFAULT 'Autoanamnesis',
  `hubungan` varchar(100) NOT NULL DEFAULT '',

  -- ---------- I. PEMERIKSAAN FISIK a-n ----------
  `fisik_kepala` enum('Normal','Tidak Normal') NOT NULL DEFAULT 'Normal',
  `fisik_kepala_ket` varchar(255) NOT NULL DEFAULT '',
  `fisik_mata` enum('Normal','Tidak Normal') NOT NULL DEFAULT 'Normal',
  `fisik_mata_ket` varchar(255) NOT NULL DEFAULT '',
  `fisik_tht` enum('Normal','Tidak Normal') NOT NULL DEFAULT 'Normal',
  `fisik_tht_ket` varchar(255) NOT NULL DEFAULT '',
  `fisik_leher` enum('Normal','Tidak Normal') NOT NULL DEFAULT 'Normal',
  `fisik_leher_ket` varchar(255) NOT NULL DEFAULT '',
  `fisik_mulut` enum('Normal','Tidak Normal') NOT NULL DEFAULT 'Normal',
  `fisik_mulut_ket` varchar(255) NOT NULL DEFAULT '',
  `fisik_jantung` enum('Normal','Tidak Normal') NOT NULL DEFAULT 'Normal',
  `fisik_jantung_ket` varchar(255) NOT NULL DEFAULT '',
  `fisik_thoraks` enum('Normal','Tidak Normal') NOT NULL DEFAULT 'Normal',
  `fisik_thoraks_ket` varchar(255) NOT NULL DEFAULT '',
  `fisik_abdomen` enum('Normal','Tidak Normal') NOT NULL DEFAULT 'Normal',
  `fisik_abdomen_ket` varchar(255) NOT NULL DEFAULT '',
  `fisik_kulit` enum('Normal','Tidak Normal') NOT NULL DEFAULT 'Normal',
  `fisik_kulit_ket` varchar(255) NOT NULL DEFAULT '',
  `fisik_tulang` enum('Normal','Tidak Normal') NOT NULL DEFAULT 'Normal',
  `fisik_tulang_ket` varchar(255) NOT NULL DEFAULT '',
  `fisik_saraf` enum('Normal','Tidak Normal') NOT NULL DEFAULT 'Normal',
  `fisik_saraf_ket` varchar(255) NOT NULL DEFAULT '',
  `fisik_genitalia` enum('Normal','Tidak Normal') NOT NULL DEFAULT 'Normal',
  `fisik_genitalia_ket` varchar(255) NOT NULL DEFAULT '',
  `fisik_lokasi` enum('Normal','Tidak Normal') NOT NULL DEFAULT 'Normal',
  `fisik_lokasi_ket` varchar(255) NOT NULL DEFAULT '',
  `fisik_lain` enum('Normal','Tidak Normal') NOT NULL DEFAULT 'Normal',
  `fisik_lain_ket` varchar(255) NOT NULL DEFAULT '',

  -- ---------- II. ASESMEN & PROGRAM ----------
  `diagnosa_medis` text NOT NULL,
  `diagnosa_banding` text NOT NULL,
  `advis_terapi` text NOT NULL,
  `rencana_tindakan` text NOT NULL,

  PRIMARY KEY (`no_rawat`),
  KEY `kd_dokter_periksa` (`kd_dokter_periksa`),
  KEY `kd_dokter_dpjp` (`kd_dokter_dpjp`),
  CONSTRAINT `asesmen_medis_ranap_rm20_1_ibfk_1` FOREIGN KEY (`no_rawat`) REFERENCES `reg_periksa` (`no_rawat`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `asesmen_medis_ranap_rm20_1_ibfk_2` FOREIGN KEY (`kd_dokter_periksa`) REFERENCES `dokter` (`kd_dokter`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `asesmen_medis_ranap_rm20_1_ibfk_3` FOREIGN KEY (`kd_dokter_dpjp`) REFERENCES `dokter` (`kd_dokter`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci ROW_FORMAT=DYNAMIC;
