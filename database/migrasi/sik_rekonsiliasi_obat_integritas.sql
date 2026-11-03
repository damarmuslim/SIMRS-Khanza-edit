-- Migrasi aditif rekonsiliasi obat. Tidak mengubah tabel/urutan kolom lama.
-- Jalankan bersama JAR yang menggunakan RekonsiliasiObatRepository.
CREATE TABLE IF NOT EXISTS rekonsiliasi_obat_tte (
  no_rekonsiliasi VARCHAR(20) NOT NULL PRIMARY KEY,
  nip VARCHAR(20) NOT NULL,
  nama_validator VARCHAR(150) NOT NULL,
  tanggal_validasi DATETIME NOT NULL,
  hash_dokumen CHAR(64) NOT NULL,
  snapshot_dokumen LONGTEXT NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Sengaja tanpa FK cascade: jejak audit harus bertahan setelah dokumen dihapus.
CREATE TABLE IF NOT EXISTS rekonsiliasi_obat_audit (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  no_rekonsiliasi VARCHAR(20) NOT NULL,
  waktu DATETIME NOT NULL,
  aksi VARCHAR(20) NOT NULL,
  nip VARCHAR(20) NOT NULL,
  alasan VARCHAR(500) NOT NULL,
  hash_dokumen CHAR(64) NOT NULL,
  snapshot_dokumen LONGTEXT NOT NULL,
  KEY idx_rekon_waktu (no_rekonsiliasi,waktu)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
