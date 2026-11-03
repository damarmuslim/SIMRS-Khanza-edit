-- =====================================================================
-- SBAR dan TBAK Rawat Inap
--
-- Tabel diambil dari implementasi DlgRawatInap RS pembanding. Migrasi ini
-- hanya menambah tabel baru dan tidak mengubah tabel/data yang sudah ada.
-- Jalankan bersama versi aplikasi yang memuat form DlgSBAR dan DlgTBAK.
-- =====================================================================

CREATE TABLE IF NOT EXISTS `pemeriksaan_ranap_sbar` (
  `no_rawat` varchar(17) NOT NULL,
  `tgl_perawatan` date NOT NULL,
  `jam_rawat` time NOT NULL,
  `situation` varchar(2000) DEFAULT NULL,
  `background` varchar(2000) DEFAULT NULL,
  `assesment` varchar(2000) NOT NULL,
  `recommendation` varchar(2000) NOT NULL,
  `nip` varchar(20) NOT NULL,
  `kd_dokter` varchar(20) NOT NULL,
  PRIMARY KEY (`no_rawat`,`tgl_perawatan`,`jam_rawat`),
  KEY `nip` (`nip`),
  KEY `kd_dokter` (`kd_dokter`),
  CONSTRAINT `pemeriksaan_ranap_sbar_ibfk_1` FOREIGN KEY (`no_rawat`) REFERENCES `reg_periksa` (`no_rawat`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `pemeriksaan_ranap_sbar_ibfk_2` FOREIGN KEY (`nip`) REFERENCES `pegawai` (`nik`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `pemeriksaan_ranap_sbar_ibfk_3` FOREIGN KEY (`kd_dokter`) REFERENCES `dokter` (`kd_dokter`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE IF NOT EXISTS `pemeriksaan_ranap_tbak` (
  `no_rawat` varchar(17) NOT NULL,
  `tgl_perawatan` date NOT NULL,
  `jam_rawat` time NOT NULL,
  `situation` varchar(2000) DEFAULT NULL,
  `background` varchar(2000) DEFAULT NULL,
  `assesment` varchar(2000) NOT NULL,
  `recommendation` varchar(2000) NOT NULL,
  `nip` varchar(20) NOT NULL,
  `kddokter` varchar(20) NOT NULL,
  PRIMARY KEY (`no_rawat`,`tgl_perawatan`,`jam_rawat`),
  KEY `nip` (`nip`),
  KEY `kddokter` (`kddokter`),
  CONSTRAINT `pemeriksaan_ranap_tbak_ibfk_1` FOREIGN KEY (`no_rawat`) REFERENCES `reg_periksa` (`no_rawat`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `pemeriksaan_ranap_tbak_ibfk_2` FOREIGN KEY (`nip`) REFERENCES `pegawai` (`nik`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `pemeriksaan_ranap_tbak_ibfk_3` FOREIGN KEY (`kddokter`) REFERENCES `dokter` (`kd_dokter`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE IF NOT EXISTS `validasi_pemeriksaan_sbar` (
  `no_rawat` varchar(17) NOT NULL,
  `tgl_perawatan` date NOT NULL,
  `jam_rawat` time NOT NULL,
  `situation` text NOT NULL,
  `background` text NOT NULL,
  `assesment` text NOT NULL,
  `recommendation` text NOT NULL,
  `nik` varchar(20) NOT NULL,
  `nik_validator` varchar(20) NOT NULL,
  `tgl_validasi` date NOT NULL,
  `jam_validasi` time NOT NULL,
  `status_validasi` enum('-','Validasi','Tidak Di Validasi') NOT NULL,
  PRIMARY KEY (`no_rawat`,`tgl_perawatan`,`jam_rawat`),
  KEY `nik` (`nik`),
  CONSTRAINT `validasi_pemeriksaan_sbar_ibfk_1` FOREIGN KEY (`no_rawat`) REFERENCES `reg_periksa` (`no_rawat`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `validasi_pemeriksaan_sbar_ibfk_2` FOREIGN KEY (`nik`) REFERENCES `pegawai` (`nik`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE IF NOT EXISTS `validasi_pemeriksaan_tbak` (
  `no_rawat` varchar(17) NOT NULL,
  `tgl_perawatan` date NOT NULL,
  `jam_rawat` time NOT NULL,
  `situation` text NOT NULL,
  `background` text NOT NULL,
  `assesment` text NOT NULL,
  `recommendation` text NOT NULL,
  `nik` varchar(20) NOT NULL,
  `nik_validator` varchar(20) NOT NULL,
  `tgl_validasi` date NOT NULL,
  `jam_validasi` time NOT NULL,
  `status_validasi` enum('-','Validasi','Tidak Di Validasi') NOT NULL,
  PRIMARY KEY (`no_rawat`,`tgl_perawatan`,`jam_rawat`),
  KEY `nik` (`nik`),
  CONSTRAINT `validasi_pemeriksaan_tbak_ibfk_1` FOREIGN KEY (`no_rawat`) REFERENCES `reg_periksa` (`no_rawat`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `validasi_pemeriksaan_tbak_ibfk_2` FOREIGN KEY (`nik`) REFERENCES `pegawai` (`nik`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
