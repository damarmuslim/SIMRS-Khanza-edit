-- Migrasi aditif SATUSEHAT. Tidak mengubah tabel klinis/penyimpanan posisional.
-- Payload memuat data medis: batasi akses database sama seperti rekam medis.
CREATE TABLE IF NOT EXISTS satu_sehat_outbox (
    request_key CHAR(64) CHARACTER SET ascii NOT NULL PRIMARY KEY,
    endpoint VARCHAR(500) NOT NULL,
    metode VARCHAR(8) NOT NULL,
    resource_type VARCHAR(40) NOT NULL,
    no_rawat VARCHAR(40) NOT NULL DEFAULT '',
    payload MEDIUMTEXT NOT NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'MENUNGGU',
    percobaan INT NOT NULL DEFAULT 0,
    http_status INT DEFAULT NULL,
    response_body MEDIUMTEXT DEFAULT NULL,
    pesan VARCHAR(500) NOT NULL DEFAULT '',
    berikutnya DATETIME DEFAULT NULL,
    diperbarui TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    dibuat TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY antrean (status,berikutnya), KEY kunjungan (no_rawat)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS satu_sehat_checkpoint (
    nama VARCHAR(80) NOT NULL PRIMARY KEY,
    tanggal DATE NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS satu_sehat_mapping_alergi_terstruktur (
    istilah VARCHAR(190) NOT NULL PRIMARY KEY,
    kategori VARCHAR(30) NOT NULL,
    coding_system VARCHAR(150) NOT NULL,
    coding_code VARCHAR(60) NOT NULL,
    coding_display VARCHAR(250) NOT NULL,
    diverifikasi_oleh VARCHAR(40) NOT NULL,
    diperbarui TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
