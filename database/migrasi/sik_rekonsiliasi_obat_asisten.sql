-- Seluruh perubahan aditif; data klinis dan tabel lama tidak diubah.
CREATE TABLE IF NOT EXISTS rekonsiliasi_obat_draf (
 nip VARCHAR(20) NOT NULL,
 no_rawat VARCHAR(17) NOT NULL,
 konteks VARCHAR(40) NOT NULL,
 versi BIGINT NOT NULL,
 diperbarui DATETIME NOT NULL,
 payload LONGTEXT NOT NULL,
 PRIMARY KEY(nip,no_rawat,konteks)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS rekonsiliasi_obat_sumber (
 no_rekonsiliasi VARCHAR(20) NOT NULL,
 hash_baris CHAR(64) NOT NULL,
 kolom INT NOT NULL,
 jenis VARCHAR(30) NOT NULL,
 referensi VARCHAR(150) NOT NULL,
 waktu_sumber VARCHAR(50) NOT NULL,
 penulis VARCHAR(150) NOT NULL,
 nilai TEXT NULL,
 ditinjau TINYINT NOT NULL DEFAULT 0,
 PRIMARY KEY(no_rekonsiliasi,hash_baris,kolom)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS rekonsiliasi_obat_favorit (
 nip VARCHAR(20) NOT NULL,
 kode_brng VARCHAR(15) NOT NULL,
 PRIMARY KEY(nip,kode_brng)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS rekonsiliasi_obat_alias (
 kode_brng VARCHAR(15) NOT NULL,
 alias VARCHAR(100) NOT NULL,
 PRIMARY KEY(kode_brng,alias)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
