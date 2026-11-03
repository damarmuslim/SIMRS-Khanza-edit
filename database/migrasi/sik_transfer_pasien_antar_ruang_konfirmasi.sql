-- =========================================================================
-- MIGRASI BASIS DATA ADITIF: SISTEM KONFIRMASI & TTE TRANSFER PASIEN ANTAR RUANGAN (RM 04)
-- RSUD DEMANG SEPULAU RAYA
-- =========================================================================

CREATE TABLE IF NOT EXISTS `transfer_pasien_antar_ruang_konfirmasi` (
    `no_rawat` varchar(17) NOT NULL,
    `tanggal_masuk` datetime NOT NULL,
    `tgl_validasi_menyerahkan` datetime DEFAULT NULL,
    `nip_menyerahkan` varchar(20) DEFAULT NULL,
    `status_menyerahkan` varchar(30) DEFAULT 'Belum Validasi',
    `tgl_validasi_menerima` datetime DEFAULT NULL,
    `nip_menerima` varchar(20) DEFAULT NULL,
    `status_menerima` varchar(30) DEFAULT 'Belum Validasi',
    `tgl_validasi_dokter` datetime DEFAULT NULL,
    `kd_dokter` varchar(20) DEFAULT NULL,
    `status_dokter` varchar(30) DEFAULT 'Belum Validasi',
    PRIMARY KEY (`no_rawat`, `tanggal_masuk`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
