-- Migrasi tabel Master Template Pemeriksaan Fisik Dokter (Head-to-Toe)
-- Penilaian Awal Medis IGD & Poliklinik
CREATE TABLE IF NOT EXISTS `template_pemeriksaan_fisik_dokter` (
  `no_template` INT AUTO_INCREMENT PRIMARY KEY,
  `kd_dokter` VARCHAR(20) NOT NULL DEFAULT '-',
  `nama_template` VARCHAR(100) NOT NULL,
  `pemeriksaan_fisik` TEXT NOT NULL,
  INDEX(`kd_dokter`)
) ENGINE=InnoDB;
