-- Menambahkan kolom canvas coretan status lokalis ke tabel penilaian_medis_igd
ALTER TABLE penilaian_medis_igd ADD COLUMN lokalis_canvas_gambar LONGTEXT NULL AFTER ket_lokalis;
