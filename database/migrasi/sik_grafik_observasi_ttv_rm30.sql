-- =========================================================================
-- MIGRASI BASIS DATA ADITIF: GRAFIK OBSERVASI TANDA-TANDA VITAL, 
-- KESEIMBANGAN CAIRAN & PEMBERIAN OBAT (RM 30)
-- RSUD DEMANG SEPULAU RAYA
-- =========================================================================

-- 1. TABEL UTAMA / HEADER PERIODE LEMBAR OBSERVASI RM 30 (5 HARI RAWAT)
CREATE TABLE IF NOT EXISTS `grafik_observasi_ttv_rm30` (
    `no_rawat` varchar(17) NOT NULL,
    `periode_ke` int(3) NOT NULL DEFAULT 1,
    `tgl_mulai` date NOT NULL,
    `tgl_akhir` date NOT NULL,
    `hari_rawat_ke` varchar(25) DEFAULT 'Hari 1 s/d 5',
    `kd_dokter` varchar(20) DEFAULT NULL,
    `nip` varchar(20) DEFAULT NULL,
    `catatan_klinis` text,
    `tgl_input` datetime DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`no_rawat`, `periode_ke`),
    KEY `idx_rm30_tgl` (`tgl_mulai`),
    KEY `idx_rm30_dokter` (`kd_dokter`),
    KEY `idx_rm30_petugas` (`nip`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- 2. TABEL DATA NUMERIK SLOT OBSERVASI (5 HARI x 4 WAKTU: P, S1, S2, M = 20 SLOT)
CREATE TABLE IF NOT EXISTS `grafik_observasi_ttv_rm30_slot` (
    `no_rawat` varchar(17) NOT NULL,
    `periode_ke` int(3) NOT NULL DEFAULT 1,
    `slot_index` tinyint(2) NOT NULL,
    `hari_ke` tinyint(1) NOT NULL,
    `waktu_shift` enum('P','S1','S2','M') NOT NULL,
    `tgl_observasi` date DEFAULT NULL,
    `jam_observasi` time DEFAULT NULL,
    `nadi` int(3) DEFAULT NULL,
    `pernapasan` int(3) DEFAULT NULL,
    `suhu` decimal(4,1) DEFAULT NULL,
    `tensi_sistol` int(3) DEFAULT NULL,
    `tensi_diastol` int(3) DEFAULT NULL,
    `berat_badan` decimal(5,1) DEFAULT NULL,
    `cairan_masuk` int(5) DEFAULT NULL,
    `cairan_keluar` int(5) DEFAULT NULL,
    `balance_cairan` int(6) DEFAULT NULL,
    `nip_petugas` varchar(20) DEFAULT NULL,
    PRIMARY KEY (`no_rawat`, `periode_ke`, `slot_index`),
    KEY `idx_rm30_slot_petugas` (`nip_petugas`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- 3. TABEL MATRIKS PEMBERIAN OBAT & PARAF PETUGAS (MEDICATION ADMINISTRATION RECORD)
CREATE TABLE IF NOT EXISTS `grafik_observasi_ttv_rm30_obat` (
    `no_rawat` varchar(17) NOT NULL,
    `periode_ke` int(3) NOT NULL DEFAULT 1,
    `no_urut` tinyint(3) NOT NULL,
    `nama_obat` varchar(200) NOT NULL,
    `dosis` varchar(100) DEFAULT '',
    `rute_frekuensi` varchar(100) DEFAULT '',
    `paraf_0` varchar(15) DEFAULT '',
    `paraf_1` varchar(15) DEFAULT '',
    `paraf_2` varchar(15) DEFAULT '',
    `paraf_3` varchar(15) DEFAULT '',
    `paraf_4` varchar(15) DEFAULT '',
    `paraf_5` varchar(15) DEFAULT '',
    `paraf_6` varchar(15) DEFAULT '',
    `paraf_7` varchar(15) DEFAULT '',
    `paraf_8` varchar(15) DEFAULT '',
    `paraf_9` varchar(15) DEFAULT '',
    `paraf_10` varchar(15) DEFAULT '',
    `paraf_11` varchar(15) DEFAULT '',
    `paraf_12` varchar(15) DEFAULT '',
    `paraf_13` varchar(15) DEFAULT '',
    `paraf_14` varchar(15) DEFAULT '',
    `paraf_15` varchar(15) DEFAULT '',
    `paraf_16` varchar(15) DEFAULT '',
    `paraf_17` varchar(15) DEFAULT '',
    `paraf_18` varchar(15) DEFAULT '',
    `paraf_19` varchar(15) DEFAULT '',
    PRIMARY KEY (`no_rawat`, `periode_ke`, `no_urut`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- 4. TABEL PENGESAHAN & VALIDASI TTE (DOKTER DPJP / KEPALA RUANG)
CREATE TABLE IF NOT EXISTS `grafik_observasi_ttv_rm30_konfirmasi` (
    `no_rawat` varchar(17) NOT NULL,
    `periode_ke` int(3) NOT NULL DEFAULT 1,
    `tgl_validasi` datetime DEFAULT NULL,
    `status_tte` varchar(30) DEFAULT 'Belum Validasi',
    `kd_dokter` varchar(20) DEFAULT NULL,
    `nip_petugas` varchar(20) DEFAULT NULL,
    `hash_dokumen` varchar(255) DEFAULT NULL,
    PRIMARY KEY (`no_rawat`, `periode_ke`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
