-- =====================================================================
-- RM 01 - SNAPSHOT KUNJUNGAN DAN E-PARAF INTERNAL
--
-- Migrasi additive. Tabel sumber pemeriksaan_ralan tidak diubah.
-- Satu baris dibuat hanya saat catatan difinalisasi. Isinya menjadi snapshot
-- yang dapat dibandingkan dengan sumber melalui hash_dokumen SHA-256.
--
-- E-paraf ini adalah autentikasi INTERNAL SIMRS, bukan TTE tersertifikasi.
-- Password/PIN tidak pernah disimpan di tabel ini; validasi memakai password
-- akun SIMRS yang sedang login.
-- Aplikasi hanya mengizinkan akun DPJP pada reg_periksa untuk memberi e-paraf;
-- nip_pemeriksa tetap disimpan terpisah sebagai identitas pembuat catatan SOAP.
-- =====================================================================

CREATE TABLE IF NOT EXISTS `resume_rawat_jalan_kunjungan` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `no_rkm_medis` VARCHAR(15) NOT NULL,
  `no_rawat` VARCHAR(17) NOT NULL,
  `tgl_perawatan` DATE NOT NULL,
  `jam_rawat` TIME NOT NULL,
  `kd_dokter_dpjp` VARCHAR(20) DEFAULT NULL,
  `nm_dokter_dpjp` VARCHAR(50) DEFAULT NULL,
  `diagnosa` TEXT,
  `uraian_klinis` TEXT,
  `penatalaksanaan` TEXT,
  `catatan_penting` TEXT,
  `nip_pemeriksa` VARCHAR(20) NOT NULL,
  `nm_pemeriksa` VARCHAR(50) DEFAULT NULL,
  `versi` INT UNSIGNED NOT NULL DEFAULT 1,
  `jenis_catatan` VARCHAR(20) NOT NULL DEFAULT 'Final',
  `jenis_paraf` VARCHAR(30) NOT NULL DEFAULT 'Internal SIMRS',
  `status_paraf` VARCHAR(25) NOT NULL DEFAULT 'Ditandatangani',
  `ditandatangani_oleh` VARCHAR(20) NOT NULL,
  `waktu_paraf` DATETIME NOT NULL,
  `hash_dokumen` CHAR(64) NOT NULL,
  `jumlah_cetak` INT UNSIGNED NOT NULL DEFAULT 0,
  `cetak_terakhir` DATETIME DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_rrjk_sumber_versi` (`no_rawat`,`tgl_perawatan`,`jam_rawat`,`nip_pemeriksa`,`versi`),
  KEY `idx_rrjk_pasien` (`no_rkm_medis`,`tgl_perawatan`,`jam_rawat`),
  KEY `idx_rrjk_penanda_tangan` (`ditandatangani_oleh`,`waktu_paraf`),
  CONSTRAINT `fk_rrjk_no_rkm_medis` FOREIGN KEY (`no_rkm_medis`)
      REFERENCES `pasien` (`no_rkm_medis`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
