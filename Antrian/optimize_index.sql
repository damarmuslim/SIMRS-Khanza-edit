-- ============================================================
-- OPTIMASI INDEX UNTUK SISTEM ANTRIAN MEDIFAX
-- Jalankan file ini sekali di phpMyAdmin atau MySQL CLI
-- pada database SIMRS Khanza (bukan database anjungan)
-- ============================================================

-- Index pada antrian_wira untuk query polling get_antrian.php
-- Mencakup: filter tanggal + jenis + status + ORDER BY waktu_panggil
ALTER TABLE antrian_wira
    ADD INDEX IF NOT EXISTS idx_antrian_polling
        (created_at, jenis, status, waktu_panggil);

-- Index tambahan untuk WHERE status = 'Dipanggil' + ORDER BY waktu_panggil
ALTER TABLE antrian_wira
    ADD INDEX IF NOT EXISTS idx_antrian_dipanggil
        (status, jenis, waktu_panggil);

-- Index pada simpan_antrian_poli_wira untuk query get_current_call.php
ALTER TABLE simpan_antrian_poli_wira
    ADD INDEX IF NOT EXISTS idx_poli_polling
        (tgl_panggil, kd_poli, kd_dokter, updated_at);

-- Index pada simpan_antrian_farmasi_wira untuk query get_current_call_farmasi.php
ALTER TABLE simpan_antrian_farmasi_wira
    ADD INDEX IF NOT EXISTS idx_farmasi_polling
        (tgl_panggil, jenis_resep, updated_at);
SHOW INDEX FROM antrian_wira;
SHOW INDEX FROM simpan_antrian_poli_wira;

-- Verifikasi index yang ada
SHOW INDEX FROM antrian_wira;
SHOW INDEX FROM simpan_antrian_poli_wira;
SHOW INDEX FROM simpan_antrian_farmasi_wira;
