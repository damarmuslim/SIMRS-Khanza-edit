-- Migrasi Penambahan Kolom Canvas Coretan Status Lokalis
-- Modul: Penilaian Medis IGD (RM 03-2)
ALTER TABLE `penilaian_medis_igd` ADD COLUMN `lokalis_canvas_gambar` LONGTEXT NULL AFTER `ket_lokalis`;
