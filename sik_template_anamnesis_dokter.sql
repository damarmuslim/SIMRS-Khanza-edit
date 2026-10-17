-- Migrasi tabel Master Template Anamnesis Dokter
-- Penilaian Awal Medis IGD & Rawat Jalan
CREATE TABLE IF NOT EXISTS `template_anamnesis_dokter` (
  `no_template` INT AUTO_INCREMENT PRIMARY KEY,
  `kd_dokter` VARCHAR(20) NOT NULL DEFAULT '-',
  `nama_template` VARCHAR(100) NOT NULL,
  `anamnesis` TEXT NOT NULL,
  INDEX(`kd_dokter`)
) ENGINE=InnoDB;
