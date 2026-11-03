-- Migrasi tabel Master Template TTV Dokter
-- Modul: Penilaian Medis IGD & Rawat Jalan
CREATE TABLE IF NOT EXISTS `template_ttv_dokter` (
  `no_template` INT AUTO_INCREMENT PRIMARY KEY,
  `kd_dokter` VARCHAR(20) NOT NULL DEFAULT '-',
  `nama_template` VARCHAR(100) NOT NULL,
  `tensi` VARCHAR(8) NOT NULL DEFAULT '',
  `nadi` VARCHAR(5) NOT NULL DEFAULT '',
  `respirasi` VARCHAR(5) NOT NULL DEFAULT '',
  `suhu` VARCHAR(5) NOT NULL DEFAULT '',
  `spo2` VARCHAR(5) NOT NULL DEFAULT '',
  `gcs` VARCHAR(10) NOT NULL DEFAULT '',
  `kesadaran` VARCHAR(50) NOT NULL DEFAULT 'Compos Mentis',
  INDEX(`kd_dokter`)
) ENGINE=InnoDB;
