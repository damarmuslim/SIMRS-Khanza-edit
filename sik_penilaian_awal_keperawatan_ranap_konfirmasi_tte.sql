-- =========================================================================
-- MIGRASI BASIS DATA ADITIF: SISTEM KONFIRMASI & TTE PENGKAJIAN AWAL KEPERAWATAN RANAP (RM 15 / RM 19)
-- RSUD DEMANG SEPULAU RAYA
-- =========================================================================

-- 1. Tabel Kredensial Global PIN TTE Dokter & Petugas (Reusable)
CREATE TABLE IF NOT EXISTS `dokter_pin_tte` (
    `kd_dokter` varchar(20) NOT NULL,
    `pin_tte` varchar(64) NOT NULL,
    `tgl_update` datetime NOT NULL,
    PRIMARY KEY (`kd_dokter`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- 2. Tabel Status Validasi TTE Pengkajian Awal Keperawatan Ranap
CREATE TABLE IF NOT EXISTS `penilaian_awal_keperawatan_ranap_konfirmasi` (
    `no_rawat` varchar(17) NOT NULL,
    `tgl_validasi_periksa` datetime DEFAULT NULL,
    `kd_dokter_periksa` varchar(20) DEFAULT NULL,
    `status_periksa` varchar(30) DEFAULT 'Belum Validasi',
    `tgl_validasi_dpjp` datetime DEFAULT NULL,
    `kd_dokter_dpjp` varchar(20) DEFAULT NULL,
    `status_dpjp` varchar(30) DEFAULT 'Belum Validasi',
    PRIMARY KEY (`no_rawat`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

