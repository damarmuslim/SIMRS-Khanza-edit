-- =====================================================================
-- RM 01  RESUME RAWAT JALAN  - RSUD Demang Sepulau Raya
--
-- Migrasi ADDITIVE: hanya membuat SATU tabel baru, tidak mengubah atau
-- menghapus apa pun yang sudah ada.
--
-- KUNCINYA `no_rkm_medis`, BUKAN `no_rawat` - INI YANG MEMBEDAKANNYA
-- Keputusan user: satu lembar untuk satu PASIEN, menghimpun seluruh kunjungan
-- rawat jalannya. Itulah sebabnya kolom DPJP ada di TIAP BARIS pada kertas -
-- tiap baris bisa dokter yang berbeda. Berbeda dari RM 32 dan RM 33 yang
-- keduanya berkunci `no_rawat`.
--
-- TABEL INI HANYA MENYIMPAN BLOK KEPALA
-- Tujuh kolom grid pada kertas (Tanggal/Jam, DPJP, Diagnosa, Uraian Klinis,
-- Rencana Penatalaksanaan, Catatan Penting, Nama & Paraf) TIDAK dibuatkan
-- kolom sama sekali. Semuanya sudah diisi dokter lewat DlgRawatJalan setiap
-- kunjungan dan tersimpan di `pemeriksaan_ralan`:
--     tgl_perawatan + jam_rawat     -> Tanggal / Jam
--     keluhan + pemeriksaan + penilaian -> Uraian Klinis  (S, O, A)
--     rtl + instruksi               -> Rencana Penatalaksanaan
--     evaluasi                      -> Catatan Penting
--     nip                           -> Nama & Paraf
-- ditambah `reg_periksa.kd_dokter` -> DPJP dan `diagnosa_pasien` -> Diagnosa.
-- Menyalinnya ke sini berarti dua sumber kebenaran untuk catatan klinis yang
-- sama - kalau dokter memperbaiki SOAP-nya, salinan di sini jadi basi tanpa
-- ada yang tahu. Grid dirakit saat mencetak.
--
-- TANPA MIGRASI HAK AKSES
-- Mengikuti RM 21 dan RM 33: siapa pun boleh mengisi, tidak ada kolom yang
-- ditambahkan ke tabel `user`.
-- =====================================================================

CREATE TABLE IF NOT EXISTS `resume_rawat_jalan` (
  `no_rkm_medis` VARCHAR(15) NOT NULL,
  `tanggal`      DATE DEFAULT NULL,
  `jam`          TIME DEFAULT NULL,

  -- ---------------------------------------------------------------
  -- ALERGI OBAT - tiga baris, persis seperti kertas.
  -- Diketik MANUAL (keputusan user). `pemeriksaan_ralan`.`alergi` memang ada,
  -- tapi per kunjungan, hanya varchar(80) satu baris, dan pada data yang ada
  -- TERISI 0 DARI 66 BARIS - jadi tidak ada yang bisa diambil dari sana.
  -- ---------------------------------------------------------------
  `alergi1` VARCHAR(150) DEFAULT NULL,
  `alergi2` VARCHAR(150) DEFAULT NULL,
  `alergi3` VARCHAR(150) DEFAULT NULL,

  -- ---------------------------------------------------------------
  -- RIWAYAT KESEHATAN PASIEN - tujuh baris, masing-masing dengan tahunnya.
  -- DIUSULKAN OTOMATIS dari diagnosa lampau pasien (reg_periksa +
  -- diagnosa_pasien + penyakit, disaring unik per penyakit+tahun), tapi
  -- TETAP DISIMPAN sebagai teks di sini dan tetap bisa diedit petugas -
  -- bukan sekadar dihitung saat mencetak. Alasannya: lembar ini
  -- ditandatangani, jadi yang tercetak harus yang disetujui petugas, dan
  -- tidak boleh berubah sendiri kalau kelak diagnosa lama dikoreksi.
  -- ---------------------------------------------------------------
  `riwayat1` VARCHAR(200) DEFAULT NULL, `tahun1` VARCHAR(4) DEFAULT NULL,
  `riwayat2` VARCHAR(200) DEFAULT NULL, `tahun2` VARCHAR(4) DEFAULT NULL,
  `riwayat3` VARCHAR(200) DEFAULT NULL, `tahun3` VARCHAR(4) DEFAULT NULL,
  `riwayat4` VARCHAR(200) DEFAULT NULL, `tahun4` VARCHAR(4) DEFAULT NULL,
  `riwayat5` VARCHAR(200) DEFAULT NULL, `tahun5` VARCHAR(4) DEFAULT NULL,
  `riwayat6` VARCHAR(200) DEFAULT NULL, `tahun6` VARCHAR(4) DEFAULT NULL,
  `riwayat7` VARCHAR(200) DEFAULT NULL, `tahun7` VARCHAR(4) DEFAULT NULL,

  -- ---------------------------------------------------------------
  -- PERHATIAN KHUSUS - lima penanda + isian bebas, diketik MANUAL.
  -- Hasil asesmennya sebenarnya ada di sistem, tapi TERSEBAR di 8+ tabel
  -- (risiko jatuh punya lima tabel terpisah: anak, dewasa, geriatri, lansia,
  -- psikiatri, plus neonatus; kekerasan di penilaian_korban_kekerasan dan
  -- skrining_kekerasan_pada_perempuan; DNR di surat_penolakan_resusitasi;
  -- disabilitas hanya `pasien`.`cacat_fisik` bertipe angka). DlgRawatJalan
  -- cuma punya TOMBOL yang membuka asesmen-asesmen itu, tidak menyimpan
  -- hasilnya. Keputusan user: manual.
  -- ---------------------------------------------------------------
  `khusus_lansia`           ENUM('Ya','Tidak') DEFAULT NULL,
  `khusus_disabilitas`      ENUM('Ya','Tidak') DEFAULT NULL,
  `khusus_resiko_kekerasan` ENUM('Ya','Tidak') DEFAULT NULL,
  `khusus_resiko_jatuh`     ENUM('Ya','Tidak') DEFAULT NULL,
  `khusus_dnr`              ENUM('Ya','Tidak') DEFAULT NULL,
  `khusus_lainnya`          VARCHAR(150) DEFAULT NULL,

  -- Petugas yang mengisi. Tanda tangannya dipasang sebagai QR pada cetakan.
  `nip` VARCHAR(20) DEFAULT NULL,

  PRIMARY KEY (`no_rkm_medis`),
  KEY `idx_rrj_nip` (`nip`),
  CONSTRAINT `fk_rrj_no_rkm_medis` FOREIGN KEY (`no_rkm_medis`)
      REFERENCES `pasien` (`no_rkm_medis`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
