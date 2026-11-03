-- ============================================================
-- Migrasi Aditif: Kolom pre_oksigen pada penilaian_medis_igd
-- RSUD Demang Sepulau Raya
-- ============================================================
ALTER TABLE `penilaian_medis_igd` ADD COLUMN IF NOT EXISTS `pre_oksigen` ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';

