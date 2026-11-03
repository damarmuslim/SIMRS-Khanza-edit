-- =========================================================================
-- MIGRASI BASIS DATA ADITIF: SISTEM OTENTIKASI PIN TTE DOKTER & KONFIRMASI RM 20
-- RSUD DEMANG SEPULAU RAYA
-- =========================================================================

-- 1. Tabel Global Kredensial PIN TTE Dokter (Reusable untuk seluruh modul SIMRS)
CREATE TABLE IF NOT EXISTS dokter_pin_tte (
    kd_dokter varchar(20) NOT NULL,
    pin_tte varchar(64) NOT NULL,
    tgl_update datetime NOT NULL,
    PRIMARY KEY (kd_dokter)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- 2. Tabel Status Validasi TTE 2 Dokter (Asesmen Medis Rawat Inap RM 20)
CREATE TABLE IF NOT EXISTS asesmen_medis_ranap_konfirmasi (
    no_rawat varchar(17) NOT NULL,
    tgl_validasi_periksa datetime DEFAULT NULL,
    kd_dokter_periksa varchar(20) DEFAULT NULL,
    status_periksa varchar(30) DEFAULT 'Belum Validasi',
    tgl_validasi_dpjp datetime DEFAULT NULL,
    kd_dokter_dpjp varchar(20) DEFAULT NULL,
    status_dpjp varchar(30) DEFAULT 'Belum Validasi',
    PRIMARY KEY (no_rawat)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
