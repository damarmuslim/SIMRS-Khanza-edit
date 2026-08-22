-- [Peran ke-2: Pemberi Edukasi] Tabel bukti TTD/foto webapp baru, dipisah
-- dari bukti_pelaksanaan_informasi_edukasi yg SUDAH ADA (data lama di tabel
-- itu TIDAK disentuh/dipindah - tabel itu tetap dipakai apa adanya utk peran
-- "Pasien/Keluarga", krn isi pernyataan kamera.php sudah dari sudut pandang
-- pasien/keluarga sejak awal). Pola & skema disamakan PERSIS dgn
-- bukti_transfer_petugas_pengirim dkk (webapps/persetujuantransferruang).
CREATE TABLE IF NOT EXISTS `bukti_pelaksanaan_edukasi_pemberi` (
  `no_rawat` varchar(17) NOT NULL,
  `tanggal` datetime NOT NULL,
  `photo` varchar(500) DEFAULT NULL,
  `jenis_bukti` enum('Foto','Tanda Tangan') NOT NULL DEFAULT 'Foto',
  PRIMARY KEY (`no_rawat`,`tanggal`),
  CONSTRAINT `bukti_pelaksanaan_edukasi_pemberi_ibfk_1` FOREIGN KEY (`no_rawat`) REFERENCES `reg_periksa` (`no_rawat`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
