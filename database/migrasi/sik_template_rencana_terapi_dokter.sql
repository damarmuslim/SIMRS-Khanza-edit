-- Migrasi tabel Master Template Rencana Terapi Dokter
-- Modul: Penilaian Medis IGD & Rawat Jalan
CREATE TABLE IF NOT EXISTS `template_rencana_terapi_dokter` (
  `no_template` INT AUTO_INCREMENT PRIMARY KEY,
  `kd_dokter` VARCHAR(20) NOT NULL DEFAULT '-',
  `nama_template` VARCHAR(100) NOT NULL,
  `rencana_terapi` TEXT NOT NULL,
  INDEX(`kd_dokter`)
) ENGINE=InnoDB;
