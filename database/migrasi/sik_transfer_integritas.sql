-- RM04: pendamping aditif. Terapkan bersama JAR dan report baru.
CREATE TABLE IF NOT EXISTS transfer_rm04_integritas (
 no_rawat VARCHAR(17) NOT NULL, tanggal_masuk DATETIME NOT NULL,
 hash_dokumen CHAR(64) NULL, versi BIGINT NOT NULL DEFAULT 0,
 tambahan LONGTEXT NULL, diperbarui DATETIME NULL,
 PRIMARY KEY(no_rawat,tanggal_masuk),
 CONSTRAINT fk_transfer_rm04_header FOREIGN KEY(no_rawat,tanggal_masuk)
 REFERENCES transfer_pasien_antar_ruang(no_rawat,tanggal_masuk) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
INSERT IGNORE INTO transfer_rm04_integritas(no_rawat,tanggal_masuk)
 SELECT no_rawat,tanggal_masuk FROM transfer_pasien_antar_ruang;
CREATE TRIGGER IF NOT EXISTS transfer_rm04_perlindungan AFTER INSERT ON transfer_pasien_antar_ruang
 FOR EACH ROW INSERT IGNORE INTO transfer_rm04_integritas(no_rawat,tanggal_masuk) VALUES(NEW.no_rawat,NEW.tanggal_masuk);
CREATE TABLE IF NOT EXISTS transfer_rm04_pengesahan (
 no_rawat VARCHAR(17) NOT NULL, tanggal_masuk DATETIME NOT NULL, peran VARCHAR(20) NOT NULL,
 nip VARCHAR(20) NOT NULL, nama VARCHAR(150) NOT NULL, waktu DATETIME NOT NULL, hash_dokumen CHAR(64) NOT NULL,
 PRIMARY KEY(no_rawat,tanggal_masuk,peran),
 CONSTRAINT fk_transfer_rm04_sah FOREIGN KEY(no_rawat,tanggal_masuk)
 REFERENCES transfer_pasien_antar_ruang(no_rawat,tanggal_masuk) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
ALTER TABLE transfer_rm04_pengesahan ADD COLUMN IF NOT EXISTS ceklist LONGTEXT NULL;
CREATE TABLE IF NOT EXISTS transfer_rm04_audit (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY, no_rawat VARCHAR(17) NOT NULL, tanggal_masuk DATETIME NOT NULL,
 waktu DATETIME NOT NULL, aksi VARCHAR(40) NOT NULL, aktor VARCHAR(30) NOT NULL,
 peran VARCHAR(20) NULL, penandatangan VARCHAR(20) NULL, hash_lama CHAR(64) NULL, hash_baru CHAR(64) NULL,
 sebelum LONGTEXT NULL, sesudah LONGTEXT NULL, INDEX idx_transfer_rm04_audit(no_rawat,tanggal_masuk,waktu)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS transfer_rm04_draf (
 aktor VARCHAR(30) NOT NULL, no_rawat VARCHAR(17) NOT NULL, konteks VARCHAR(80) NOT NULL,
 versi BIGINT NOT NULL, payload LONGTEXT NOT NULL, diperbarui DATETIME NOT NULL,
 PRIMARY KEY(aktor,no_rawat,konteks)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
