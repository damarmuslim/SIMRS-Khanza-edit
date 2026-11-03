-- =====================================================================
-- RM 33  RESUME KEPERAWATAN / KEBIDANAN  (diisi Perawat / Bidan)
-- RSUD Demang Sepulau Raya
--
-- Migrasi ADDITIVE: hanya membuat SATU tabel baru, tidak mengubah atau
-- menghapus apa pun yang sudah ada.
--
-- KENAPA TABEL BARU, BUKAN NUMPANG `resume_pasien_ranap`
-- Tabel itu adalah resume MEDIS: ditandatangani DOKTER (kolom `kd_dokter`),
-- isinya diagnosa & prosedur ber-kode ICD, obat pulang, cara keluar, dan
-- keadaan waktu pulang. RM 33 adalah resume KEPERAWATAN - ditandatangani
-- perawat/bidan, isinya diagnosa & tindakan keperawatan. Dua profesi, dua
-- tanda tangan, dua lembar fisik yang terpisah. Selain itu PK
-- `resume_pasien_ranap` adalah `no_rawat` saja, jadi kalau numpang, kedua
-- resume akan saling menimpa baris yang sama.
--
-- Yang lain sudah dicek dan memang tidak ada padanannya:
--   - `resume_pasien`                            -> resume medis RAWAT JALAN
--   - `penilaian_awal_keperawatan_ranap_masalah` -> masalah keperawatan, tapi
--     diambil saat MASUK (asesmen awal) dan berupa kode 3 digit ke tabel
--     master, bukan lima baris teks bebas saat PULANG
-- Tidak ada satu pun tabel resume keperawatan di database.
--
-- TIDAK ADA MIGRASI HAK AKSES untuk form ini.
-- Keputusan eksplisit user: siapa pun boleh mengisinya, tidak perlu
-- pembatasan. Jadi tidak ada kolom hak akses yang ditambahkan ke tabel `user`
-- dan menunya selalu tersedia - sama seperti RM 21, berbeda dari RM 20 dan
-- RM 17-01 yang keduanya memakai hak akses.
--
-- YANG TIDAK DISIMPAN DI SINI (sengaja)
-- Tiga isian di kepala kertas - TANGGAL MASUK, TANGGAL KELUAR, dan RUANGAN -
-- TIDAK dibuatkan kolom. Ketiganya sudah ada di `kamar_inap` dan diambil
-- lewat JOIN saat form dibuka dan saat mencetak. Menyalinnya ke sini justru
-- berbahaya: kalau pasien pindah kamar atau tanggal pulangnya dikoreksi,
-- salinan di lembar ini jadi basi tanpa ada yang tahu. Pola yang sama dipakai
-- untuk Ruangan di modul Asuhan Gizi.
--
-- Identitas pasien (No.RM, nama, tgl lahir, L/P) juga tidak disimpan - lewat
-- JOIN ke `reg_periksa`/`pasien`, sama seperti RM 17-01 dan RM 21.
--
-- KENAPA LIMA BARIS TETAP, BUKAN TABEL ANAK
-- Formulir fisik mencetak tepat lima baris bernomor untuk masing-masing dari
-- empat blok, jadi lima adalah batas yang sudah ditetapkan oleh kertasnya -
-- bukan batasan yang dibuat-buat di sini. Tabel anak akan memberi jumlah baris
-- tak terbatas yang tidak akan muat saat dicetak. Kalau nanti petugas ternyata
-- butuh lebih dari lima, ini perlu diubah jadi tabel anak DAN tata letak
-- cetakannya dirancang ulang - keduanya sekaligus, bukan salah satu.
-- =====================================================================

CREATE TABLE IF NOT EXISTS `resume_keperawatan_ranap` (
  `no_rawat` VARCHAR(17) NOT NULL,
  `tanggal`  DATE DEFAULT NULL,
  `jam`      TIME DEFAULT NULL,

  -- Terisi otomatis dari `kamar_inap`.`diagnosa_akhir` (mundur ke
  -- `diagnosa_awal` kalau belum diisi), TAPI tetap bisa diedit perawat.
  -- Disimpan - tidak seperti tanggal/ruangan - karena yang ditulis perawat di
  -- lembar ini adalah diagnosa medis SEBAGAIMANA DIPAHAMI saat resume dibuat,
  -- dan itu bagian dari isi dokumen yang ditandatanganinya.
  `diagnosa_medis`  VARCHAR(200) DEFAULT NULL,
  `keluhan_utama`   TEXT DEFAULT NULL,

  -- =================================================================
  -- Empat blok, masing-masing lima baris bernomor - persis seperti kertas.
  -- Semua teks bebas: keputusan user, mengikuti kertas.
  -- =================================================================

  -- DIAGNOSA KEPERAWATAN / KEBIDANAN YANG TIMBUL
  `dx_timbul1` VARCHAR(200) DEFAULT NULL,
  `dx_timbul2` VARCHAR(200) DEFAULT NULL,
  `dx_timbul3` VARCHAR(200) DEFAULT NULL,
  `dx_timbul4` VARCHAR(200) DEFAULT NULL,
  `dx_timbul5` VARCHAR(200) DEFAULT NULL,

  -- TINDAKAN KEPERAWATAN / KEBIDANAN
  `tindakan1` VARCHAR(200) DEFAULT NULL,
  `tindakan2` VARCHAR(200) DEFAULT NULL,
  `tindakan3` VARCHAR(200) DEFAULT NULL,
  `tindakan4` VARCHAR(200) DEFAULT NULL,
  `tindakan5` VARCHAR(200) DEFAULT NULL,

  -- DIAGNOSA KEPERAWATAN / KEBIDANAN YANG SUDAH TERATASI
  `dx_teratasi1` VARCHAR(200) DEFAULT NULL,
  `dx_teratasi2` VARCHAR(200) DEFAULT NULL,
  `dx_teratasi3` VARCHAR(200) DEFAULT NULL,
  `dx_teratasi4` VARCHAR(200) DEFAULT NULL,
  `dx_teratasi5` VARCHAR(200) DEFAULT NULL,

  -- RENCANA TINDAK LANJUT / NASIHAT WAKTU PULANG
  `rencana1` VARCHAR(200) DEFAULT NULL,
  `rencana2` VARCHAR(200) DEFAULT NULL,
  `rencana3` VARCHAR(200) DEFAULT NULL,
  `rencana4` VARCHAR(200) DEFAULT NULL,
  `rencana5` VARCHAR(200) DEFAULT NULL,

  -- Perawat / Bidan yang mengisi. Tanda tangannya dipasang sebagai QR pada
  -- cetakan (pola RM 03-x / RM 20 / RM 17-01 / RM 21).
  `nip` VARCHAR(20) DEFAULT NULL,

  PRIMARY KEY (`no_rawat`),
  KEY `idx_rkr_nip` (`nip`),
  CONSTRAINT `fk_rkr_no_rawat` FOREIGN KEY (`no_rawat`)
      REFERENCES `reg_periksa` (`no_rawat`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
