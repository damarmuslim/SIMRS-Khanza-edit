-- Diagnosis kerja tunggal dan diagnosis pembanding jamak berbasis master ICD-10.
-- Kolom nama lama tetap dipakai sebagai snapshot agar laporan yang ada tetap
-- kompatibel. Migrasi ini aditif dan aman dijalankan ulang.

ALTER TABLE `penilaian_medis_igd`
  ADD COLUMN IF NOT EXISTS `diagnosa_kerja_kd` VARCHAR(15) NULL DEFAULT NULL;

CREATE TABLE IF NOT EXISTS `penilaian_medis_igd_diagnosa_banding` (
  `no_rawat` VARCHAR(17) NOT NULL,
  `kd_penyakit` VARCHAR(15) NOT NULL,
  `urutan` SMALLINT UNSIGNED NOT NULL DEFAULT 1,
  PRIMARY KEY (`no_rawat`,`kd_penyakit`),
  KEY `kd_penyakit` (`kd_penyakit`),
  CONSTRAINT `pmigd_banding_rawat_fk`
    FOREIGN KEY (`no_rawat`) REFERENCES `penilaian_medis_igd` (`no_rawat`)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `pmigd_banding_icd10_fk`
    FOREIGN KEY (`kd_penyakit`) REFERENCES `penyakit` (`kd_penyakit`)
    ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
