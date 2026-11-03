-- Migrasi aditif RM19; tidak mengubah/menghapus kolom atau data historis.
CREATE TABLE IF NOT EXISTS rm19_integritas (
 no_rawat VARCHAR(17) NOT NULL PRIMARY KEY,
 hash_dokumen CHAR(64) NULL,
 versi BIGINT NOT NULL DEFAULT 0,
 diperbarui DATETIME NULL,
 CONSTRAINT fk_rm19_integritas_header FOREIGN KEY(no_rawat)
 REFERENCES penilaian_awal_keperawatan_ranap(no_rawat) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
INSERT IGNORE INTO rm19_integritas(no_rawat)
 SELECT no_rawat FROM penilaian_awal_keperawatan_ranap;
-- Menjaga perlindungan juga untuk INSERT dari klien lama selama transisi.
CREATE TRIGGER IF NOT EXISTS rm19_perlindungan_baru
 AFTER INSERT ON penilaian_awal_keperawatan_ranap FOR EACH ROW
 INSERT IGNORE INTO rm19_integritas(no_rawat) VALUES(NEW.no_rawat);
CREATE TABLE IF NOT EXISTS rm19_pengesahan (
 no_rawat VARCHAR(17) NOT NULL,
 peran VARCHAR(20) NOT NULL,
 nip VARCHAR(20) NOT NULL,
 nama VARCHAR(150) NOT NULL,
 waktu DATETIME NOT NULL,
 hash_dokumen CHAR(64) NOT NULL,
 PRIMARY KEY(no_rawat,peran),
 CONSTRAINT fk_rm19_pengesahan_header FOREIGN KEY(no_rawat)
 REFERENCES penilaian_awal_keperawatan_ranap(no_rawat) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
CREATE TABLE IF NOT EXISTS rm19_audit (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 no_rawat VARCHAR(17) NOT NULL,
 waktu DATETIME NOT NULL,
 aksi VARCHAR(30) NOT NULL,
 aktor VARCHAR(30) NOT NULL,
 peran VARCHAR(20) NULL,
 penandatangan VARCHAR(20) NULL,
 hash_lama CHAR(64) NULL,
 hash_baru CHAR(64) NULL,
 sebelum LONGTEXT NULL,
 sesudah LONGTEXT NULL,
 INDEX idx_rm19_audit_rawat(no_rawat,waktu)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
ALTER TABLE rm19_audit ADD COLUMN IF NOT EXISTS peran VARCHAR(20) NULL;
ALTER TABLE rm19_audit ADD COLUMN IF NOT EXISTS penandatangan VARCHAR(20) NULL;
CREATE TABLE IF NOT EXISTS rm19_draf (
 aktor VARCHAR(30) NOT NULL,
 no_rawat VARCHAR(17) NOT NULL,
 hash_dasar VARCHAR(64) NOT NULL DEFAULT '',
 versi BIGINT NOT NULL,
 payload LONGTEXT NOT NULL,
 diperbarui DATETIME NOT NULL,
 PRIMARY KEY(aktor,no_rawat)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
