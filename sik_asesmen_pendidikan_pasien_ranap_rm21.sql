-- =====================================================================
-- RM 21  ASESMEN & PERENCANAAN PENDIDIKAN PASIEN  (diisi Perawat / Bidan)
-- RSUD Demang Sepulau Raya
--
-- Migrasi ADDITIVE: hanya membuat SATU tabel baru, tidak mengubah atau
-- menghapus apa pun yang sudah ada.
--
-- KENAPA TABEL BARU, BUKAN NUMPANG `edukasi_pasien_keluarga_rj`
-- Tabel itu melayani form RM 02-1 "Edukasi Pasien & Keluarga Terintegrasi
-- RAWAT JALAN" dan PK-nya `no_rawat` saja - satu record per episode. Kalau
-- RM 21 numpang di sana, kedua form akan saling menimpa baris yang sama.
-- Alasan yang persis sama membuat RM 20 dulu dipisah dari
-- `penilaian_medis_ranap`. Konsekuensi yang disadari & disetujui: kedua form
-- TIDAK berbagi data.
--
-- Form RM 02-1 sendiri TIDAK disentuh sama sekali - ia tetap melayani rawat
-- jalan & IGD seperti sebelumnya (memang hanya bisa dibuka dari DlgRawatJalan,
-- DlgIGD, DlgKasirRalan, DlgReg, dan menu utama; tidak ada pintu masuk dari
-- rawat inap). RM 21 dipakai KHUSUS RAWAT INAP - keputusan user.
--
-- CATATAN ISI
-- Identitas pasien TIDAK disimpan di sini, diambil lewat JOIN saat form dibuka
-- dan saat mencetak - sama seperti RM 17-01.
--
-- Kolom "Pemberian Edukasi" pada kertas (Dokter / Farmasi / Perawat-Bidan /
-- Ahli Gizi / Terapis) TIDAK dibuatkan kolom: nilainya sudah TERCETAK TETAP
-- pada tiap baris di formulir fisik, jadi tidak ada yang perlu disimpan.
--
-- Seluruh isian Section B diisi MANUAL oleh perawat (keputusan user), tidak
-- dipetakan otomatis dari data registrasi. Selain karena pemetaannya tidak
-- pasti (mis. "Akademi" di kertas vs D1/D2/D3 di `pasien`.`pnd`), asesmen
-- edukasi memang harus mencerminkan yang dinilai perawat langsung terhadap
-- pasien - bukan data pendaftaran yang bisa sudah basi.
-- =====================================================================

CREATE TABLE IF NOT EXISTS `asesmen_pendidikan_pasien_ranap` (
  `no_rawat` VARCHAR(17) NOT NULL,
  `tanggal`  DATE DEFAULT NULL,
  `jam`      TIME DEFAULT NULL,

  -- =================================================================
  -- A. ASESMEN KEBUTUHAN PENDIDIKAN
  -- Delapan baris tetap sesuai kertas. Tiap baris punya PENANDA kebutuhan
  -- dan SASARAN-nya sendiri: pada formulir fisik kolom "Sasaran (P/K/P&K)"
  -- memang diisi per baris, bukan sekali untuk seluruh lembar - berbeda dari
  -- form rawat jalan yang hanya punya satu sasaran global.
  -- =================================================================
  `butuh_diagnosa`                  ENUM('Ya','Tidak') DEFAULT NULL,
  `sasaran_diagnosa`                ENUM('Pasien','Keluarga','Pasien & Keluarga') DEFAULT NULL,
  `butuh_obat`                      ENUM('Ya','Tidak') DEFAULT NULL,
  `sasaran_obat`                    ENUM('Pasien','Keluarga','Pasien & Keluarga') DEFAULT NULL,
  `butuh_alat_medis`                ENUM('Ya','Tidak') DEFAULT NULL,
  `sasaran_alat_medis`              ENUM('Pasien','Keluarga','Pasien & Keluarga') DEFAULT NULL,
  `butuh_diet_nutrisi`              ENUM('Ya','Tidak') DEFAULT NULL,
  `sasaran_diet_nutrisi`            ENUM('Pasien','Keluarga','Pasien & Keluarga') DEFAULT NULL,
  `butuh_rehabilitasi`              ENUM('Ya','Tidak') DEFAULT NULL,
  `sasaran_rehabilitasi`            ENUM('Pasien','Keluarga','Pasien & Keluarga') DEFAULT NULL,
  `butuh_manajemen_nyeri`           ENUM('Ya','Tidak') DEFAULT NULL,
  `sasaran_manajemen_nyeri`         ENUM('Pasien','Keluarga','Pasien & Keluarga') DEFAULT NULL,
  `butuh_ppi_cuci_tangan`           ENUM('Ya','Tidak') DEFAULT NULL,
  `sasaran_ppi_cuci_tangan`         ENUM('Pasien','Keluarga','Pasien & Keluarga') DEFAULT NULL,
  -- "Pemenuhan kebutuhan kesehatan berkelanjutan". Dikonfirmasi user sebagai
  -- konsep TERSENDIRI, bukan padanan `kebutuhan_rujukan_edukasi` milik form
  -- rawat jalan - karena lembar ini konteksnya rawat inap.
  `butuh_kesehatan_berkelanjutan`   ENUM('Ya','Tidak') DEFAULT NULL,
  `sasaran_kesehatan_berkelanjutan` ENUM('Pasien','Keluarga','Pasien & Keluarga') DEFAULT NULL,

  -- Baris "Lain-lain" pada kertas hanya punya kotak centang dan garis isian;
  -- kolom Pemberian Edukasi & Sasaran-nya dibiarkan kosong, jadi tidak ada
  -- kolom sasaran untuk baris ini.
  `butuh_lain_lain`                 ENUM('Ya','Tidak') DEFAULT NULL,
  `ket_lain_lain`                   VARCHAR(200) DEFAULT NULL,

  -- =================================================================
  -- B. PERENCANAAN PEMBERIAN EDUKASI
  -- =================================================================
  `keyakinan_nilai`          VARCHAR(300) DEFAULT NULL,
  `budaya`                   VARCHAR(300) DEFAULT NULL,

  `kemampuan_membaca`        ENUM('Tidak','Ya') DEFAULT NULL,

  -- Tujuh pilihan mengikuti PERSIS kertas. Sengaja TIDAK memakai daftar nilai
  -- `pasien`.`pnd` (yang punya 14 nilai berbeda): kertas menyebut "Akademi"
  -- sementara data registrasi memakai D1/D2/D3, dan padanannya belum
  -- dipastikan. Diisi manual - keputusan user.
  `tingkat_pendidikan`       ENUM('Tidak Sekolah','SD','SMP','SMA','Akademi','Sarjana','Lain-lain') DEFAULT NULL,
  `ket_tingkat_pendidikan`   VARCHAR(60) DEFAULT NULL,

  -- Bahasa sehari-hari. NULL berarti kotaknya tidak dicentang; kalau dicentang,
  -- nilainya sekaligus menyatakan Aktif atau Pasif seperti di kertas.
  `bahasa_indonesia`         ENUM('Aktif','Pasif') DEFAULT NULL,
  `bahasa_inggris`           ENUM('Aktif','Pasif') DEFAULT NULL,
  `bahasa_daerah1`           VARCHAR(60) DEFAULT NULL,
  `kemampuan_daerah1`        ENUM('Aktif','Pasif') DEFAULT NULL,
  `bahasa_daerah2`           VARCHAR(60) DEFAULT NULL,
  `kemampuan_daerah2`        ENUM('Aktif','Pasif') DEFAULT NULL,

  `penerjemah`               ENUM('Tidak','Ya') DEFAULT NULL,
  `ket_penerjemah`           VARCHAR(100) DEFAULT NULL,

  -- Empat hambatan. Masing-masing punya kolom keterangan sendiri karena kertas
  -- meminta "Ya, sebutkan : ....." - form rawat jalan yang ada hanya menyimpan
  -- penanda Ya/Tidak tanpa tempat menuliskan sebutannya.
  `hambatan_emosional`       ENUM('Tidak','Ya') DEFAULT NULL,
  `ket_hambatan_emosional`   VARCHAR(150) DEFAULT NULL,
  `hambatan_motivasi`        ENUM('Tidak','Ya') DEFAULT NULL,
  `ket_hambatan_motivasi`    VARCHAR(150) DEFAULT NULL,
  `keterbatasan_fisik`       ENUM('Tidak','Ya') DEFAULT NULL,
  `ket_keterbatasan_fisik`   VARCHAR(150) DEFAULT NULL,
  `keterbatasan_kognitif`    ENUM('Tidak','Ya') DEFAULT NULL,
  `ket_keterbatasan_kognitif` VARCHAR(150) DEFAULT NULL,

  `kesediaan_menerima`       ENUM('Tidak','Ya') DEFAULT NULL,

  -- Perawat / Bidan yang mengisi. Tanda tangannya dipasang sebagai QR pada
  -- cetakan (pola RM 03-x / RM 20 / RM 17-01).
  `nip`                      VARCHAR(20) DEFAULT NULL,

  PRIMARY KEY (`no_rawat`),
  KEY `idx_app_nip` (`nip`),
  CONSTRAINT `fk_app_no_rawat` FOREIGN KEY (`no_rawat`)
      REFERENCES `reg_periksa` (`no_rawat`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
