-- ============================================================
-- Migrasi: Penilaian Awal Medis IGD - Pengkajian Awal Medis
-- Gawat Darurat (RM 03-2)
-- RSUD Demang Sepulau Raya - SIMRS Khanza
-- Formulir target: RM 03-2
--
-- STATUS: File ini untuk diimport manual oleh pengguna (mis. lewat
-- phpMyAdmin di XAMPP). TIDAK dijalankan otomatis oleh Claude Code.
--
-- Prinsip desain:
--  - Additive only: tidak ada DROP/CHANGE pada kolom/tabel existing.
--  - Semua kolom baru masuk ke tabel `penilaian_medis_igd` yang sudah
--    ada (tetap 1:1 dengan no_rawat, tidak perlu tabel anak baru).
--  - Semua statement pakai IF NOT EXISTS supaya aman dijalankan ulang.
--  - Desain form baru MENGGANTIKAN (bukan berdampingan dengan) 10 kolom
--    lama di bawah ini dari sisi UI/cetakan aktif - kolom-kolom lama
--    TETAP ADA di skema (data lama tetap terbaca), hanya TIDAK LAGI
--    DITULIS oleh form baru:
--      kepala, mata, gigi, leher, thoraks, abdomen, genital,
--      ekstremitas, gcs, diagnosis
--    (Lihat juga catatan yang sama di RMPenilaianAwalMedisIGD.java
--    dekat deklarasi widget lama, supaya tidak sengaja dipakai lagi.)
-- ============================================================

-- ---------------------------------------------------------------------
-- KELOMPOK A: Intervensi Pre Hospital
-- ---------------------------------------------------------------------
ALTER TABLE `penilaian_medis_igd` ADD COLUMN IF NOT EXISTS `pre_cervical_collar` ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';
ALTER TABLE `penilaian_medis_igd` ADD COLUMN IF NOT EXISTS `pre_defibrilasi`     ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';
ALTER TABLE `penilaian_medis_igd` ADD COLUMN IF NOT EXISTS `pre_vtp`             ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';
ALTER TABLE `penilaian_medis_igd` ADD COLUMN IF NOT EXISTS `pre_balut_bidai`     ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';
ALTER TABLE `penilaian_medis_igd` ADD COLUMN IF NOT EXISTS `pre_ngt`             ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';
ALTER TABLE `penilaian_medis_igd` ADD COLUMN IF NOT EXISTS `pre_obat_ket`        VARCHAR(200) NOT NULL DEFAULT '';
ALTER TABLE `penilaian_medis_igd` ADD COLUMN IF NOT EXISTS `pre_rjp`             ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';
ALTER TABLE `penilaian_medis_igd` ADD COLUMN IF NOT EXISTS `pre_intubasi`        ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';
ALTER TABLE `penilaian_medis_igd` ADD COLUMN IF NOT EXISTS `pre_dekompresi_wsd`  ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';
ALTER TABLE `penilaian_medis_igd` ADD COLUMN IF NOT EXISTS `pre_kateter_urin`    ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';
ALTER TABLE `penilaian_medis_igd` ADD COLUMN IF NOT EXISTS `pre_infus`           ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';
ALTER TABLE `penilaian_medis_igd` ADD COLUMN IF NOT EXISTS `pre_tidak_ada`       ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';

-- ---------------------------------------------------------------------
-- KELOMPOK B: Survei Primer - Jalan Napas
-- ---------------------------------------------------------------------
ALTER TABLE `penilaian_medis_igd` ADD COLUMN IF NOT EXISTS `jn_paten`                 ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';
ALTER TABLE `penilaian_medis_igd` ADD COLUMN IF NOT EXISTS `jn_obstruksi_partial`     ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';
ALTER TABLE `penilaian_medis_igd` ADD COLUMN IF NOT EXISTS `jn_stridor`               ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';
ALTER TABLE `penilaian_medis_igd` ADD COLUMN IF NOT EXISTS `jn_snoring`               ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';
ALTER TABLE `penilaian_medis_igd` ADD COLUMN IF NOT EXISTS `jn_gurgling`              ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';
ALTER TABLE `penilaian_medis_igd` ADD COLUMN IF NOT EXISTS `jn_wheezing`              ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';
ALTER TABLE `penilaian_medis_igd` ADD COLUMN IF NOT EXISTS `jn_obstruksi_total`       ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';
ALTER TABLE `penilaian_medis_igd` ADD COLUMN IF NOT EXISTS `jn_trauma_jalan_napas`    ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';
ALTER TABLE `penilaian_medis_igd` ADD COLUMN IF NOT EXISTS `jn_fasial_leher_inhalasi` ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';
ALTER TABLE `penilaian_medis_igd` ADD COLUMN IF NOT EXISTS `jn_risiko_aspirasi`       ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';
ALTER TABLE `penilaian_medis_igd` ADD COLUMN IF NOT EXISTS `jn_benda_asing`           ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';
ALTER TABLE `penilaian_medis_igd` ADD COLUMN IF NOT EXISTS `jn_kesimpulan`            ENUM('Aman','Mengancam Jiwa') NULL DEFAULT NULL;

-- ---------------------------------------------------------------------
-- KELOMPOK C: Survei Primer - Pernapasan
-- ---------------------------------------------------------------------
ALTER TABLE `penilaian_medis_igd` ADD COLUMN IF NOT EXISTS `nap_pola`             ENUM('Spontan','Tidak Spontan') NULL DEFAULT NULL;
ALTER TABLE `penilaian_medis_igd` ADD COLUMN IF NOT EXISTS `nap_reguler`          ENUM('Reguler','Irreguler') NULL DEFAULT NULL;
ALTER TABLE `penilaian_medis_igd` ADD COLUMN IF NOT EXISTS `nap_gerakan_dada`     ENUM('Simetris','Asimetris') NULL DEFAULT NULL;
ALTER TABLE `penilaian_medis_igd` ADD COLUMN IF NOT EXISTS `nap_jejas_kanan`      ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';
ALTER TABLE `penilaian_medis_igd` ADD COLUMN IF NOT EXISTS `nap_jejas_kiri`       ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';
ALTER TABLE `penilaian_medis_igd` ADD COLUMN IF NOT EXISTS `nap_tipe_normal`         ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';
ALTER TABLE `penilaian_medis_igd` ADD COLUMN IF NOT EXISTS `nap_tipe_takipneu`       ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';
ALTER TABLE `penilaian_medis_igd` ADD COLUMN IF NOT EXISTS `nap_tipe_kussmaul`       ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';
ALTER TABLE `penilaian_medis_igd` ADD COLUMN IF NOT EXISTS `nap_tipe_hiperventilasi` ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';
ALTER TABLE `penilaian_medis_igd` ADD COLUMN IF NOT EXISTS `nap_tipe_biot`           ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';
ALTER TABLE `penilaian_medis_igd` ADD COLUMN IF NOT EXISTS `nap_tipe_cheyne_stoke`   ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';
ALTER TABLE `penilaian_medis_igd` ADD COLUMN IF NOT EXISTS `nap_tipe_apneustic`      ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';
ALTER TABLE `penilaian_medis_igd` ADD COLUMN IF NOT EXISTS `nap_tipe_retraktif`      ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';
ALTER TABLE `penilaian_medis_igd` ADD COLUMN IF NOT EXISTS `nap_tipe_flare`          ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';
ALTER TABLE `penilaian_medis_igd` ADD COLUMN IF NOT EXISTS `nap_kesimpulan`       ENUM('Aman','Mengancam Jiwa') NULL DEFAULT NULL;

-- ---------------------------------------------------------------------
-- KELOMPOK D: Survei Primer - Sirkulasi
-- ---------------------------------------------------------------------
ALTER TABLE `penilaian_medis_igd` ADD COLUMN IF NOT EXISTS `sir_nadi`           ENUM('Reguler','Irreguler') NULL DEFAULT NULL;
ALTER TABLE `penilaian_medis_igd` ADD COLUMN IF NOT EXISTS `sir_nadi_kekuatan`  ENUM('Kuat','Lemah') NULL DEFAULT NULL;
ALTER TABLE `penilaian_medis_igd` ADD COLUMN IF NOT EXISTS `sir_kulit_warna`    ENUM('Normal','Pucat') NULL DEFAULT NULL;
ALTER TABLE `penilaian_medis_igd` ADD COLUMN IF NOT EXISTS `sir_kulit_kondisi`  ENUM('Jaundice','Cyanosis') NULL DEFAULT NULL;
ALTER TABLE `penilaian_medis_igd` ADD COLUMN IF NOT EXISTS `sir_berkeringat`    ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';
ALTER TABLE `penilaian_medis_igd` ADD COLUMN IF NOT EXISTS `sir_akral`          ENUM('Hangat','Dingin') NULL DEFAULT NULL;
ALTER TABLE `penilaian_medis_igd` ADD COLUMN IF NOT EXISTS `sir_akral_kondisi`  ENUM('Kering','Basah') NULL DEFAULT NULL;
ALTER TABLE `penilaian_medis_igd` ADD COLUMN IF NOT EXISTS `sir_crt`            ENUM('<2 detik','>2 detik') NULL DEFAULT NULL;
ALTER TABLE `penilaian_medis_igd` ADD COLUMN IF NOT EXISTS `sir_kesimpulan`     ENUM('Aman','Mengancam Jiwa') NULL DEFAULT NULL;

-- ---------------------------------------------------------------------
-- KELOMPOK E: Kategori Pasien (toggle Bayi Baru Lahir) + Skor APGAR +
-- Segitiga PAT. kategori_pasien mengontrol tampil/sembunyi panel APGAR/PAT
-- di UI (pola sama seperti perbaruiVisibilitasMekanismeTrauma() di
-- RMTriaseIGD.java). Kolom APGAR/PAT sengaja teks bebas dulu (VARCHAR) -
-- tipe final menyusul setelah dikonfirmasi ke dokter.
-- ---------------------------------------------------------------------
ALTER TABLE `penilaian_medis_igd` ADD COLUMN IF NOT EXISTS `kategori_pasien`             ENUM('Umum','Bayi Baru Lahir') NOT NULL DEFAULT 'Umum';
ALTER TABLE `penilaian_medis_igd` ADD COLUMN IF NOT EXISTS `apgar_cukup_bulan`           VARCHAR(100) NOT NULL DEFAULT '';
ALTER TABLE `penilaian_medis_igd` ADD COLUMN IF NOT EXISTS `apgar_cairan_amnion_jernih`  VARCHAR(100) NOT NULL DEFAULT '';
ALTER TABLE `penilaian_medis_igd` ADD COLUMN IF NOT EXISTS `apgar_pernapasan_menangis`   VARCHAR(100) NOT NULL DEFAULT '';
ALTER TABLE `penilaian_medis_igd` ADD COLUMN IF NOT EXISTS `apgar_tonus_otot`            VARCHAR(100) NOT NULL DEFAULT '';
ALTER TABLE `penilaian_medis_igd` ADD COLUMN IF NOT EXISTS `pat_penampilan`              VARCHAR(100) NOT NULL DEFAULT '';
ALTER TABLE `penilaian_medis_igd` ADD COLUMN IF NOT EXISTS `pat_usaha_napas`             VARCHAR(100) NOT NULL DEFAULT '';
ALTER TABLE `penilaian_medis_igd` ADD COLUMN IF NOT EXISTS `pat_perfusi_kulit`           VARCHAR(100) NOT NULL DEFAULT '';

-- ---------------------------------------------------------------------
-- KELOMPOK F: Disabilitas. gcs_e/v/m menggantikan kolom `gcs` lama dari
-- sisi UI aktif (kolom `gcs` lama TETAP ADA, lihat catatan di atas).
-- Kolom `kesadaran` existing dipakai ulang apa adanya, tidak ada kolom
-- baru untuk itu di sini.
-- ---------------------------------------------------------------------
ALTER TABLE `penilaian_medis_igd` ADD COLUMN IF NOT EXISTS `gcs_e`                VARCHAR(5) NOT NULL DEFAULT '';
ALTER TABLE `penilaian_medis_igd` ADD COLUMN IF NOT EXISTS `gcs_v`                VARCHAR(5) NOT NULL DEFAULT '';
ALTER TABLE `penilaian_medis_igd` ADD COLUMN IF NOT EXISTS `gcs_m`                VARCHAR(5) NOT NULL DEFAULT '';
ALTER TABLE `penilaian_medis_igd` ADD COLUMN IF NOT EXISTS `pupil`                ENUM('Isokor','Anisokor') NULL DEFAULT NULL;
ALTER TABLE `penilaian_medis_igd` ADD COLUMN IF NOT EXISTS `diameter_pupil_kiri`  VARCHAR(10) NOT NULL DEFAULT '';
ALTER TABLE `penilaian_medis_igd` ADD COLUMN IF NOT EXISTS `diameter_pupil_kanan` VARCHAR(10) NOT NULL DEFAULT '';
ALTER TABLE `penilaian_medis_igd` ADD COLUMN IF NOT EXISTS `refleks_cahaya_kiri`  ENUM('Positif','Negatif') NULL DEFAULT NULL;
ALTER TABLE `penilaian_medis_igd` ADD COLUMN IF NOT EXISTS `refleks_cahaya_kanan` ENUM('Positif','Negatif') NULL DEFAULT NULL;
ALTER TABLE `penilaian_medis_igd` ADD COLUMN IF NOT EXISTS `meningeal_signs`     VARCHAR(200) NOT NULL DEFAULT '';
ALTER TABLE `penilaian_medis_igd` ADD COLUMN IF NOT EXISTS `lateralisasi`        ENUM('Kanan','Kiri','Tidak Ada') NULL DEFAULT NULL;

-- ---------------------------------------------------------------------
-- KELOMPOK G: Eksposur
-- ---------------------------------------------------------------------
ALTER TABLE `penilaian_medis_igd` ADD COLUMN IF NOT EXISTS `eks_deformitas`        ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';
ALTER TABLE `penilaian_medis_igd` ADD COLUMN IF NOT EXISTS `eks_ekskoriasi`        ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';
ALTER TABLE `penilaian_medis_igd` ADD COLUMN IF NOT EXISTS `eks_contusio`          ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';
ALTER TABLE `penilaian_medis_igd` ADD COLUMN IF NOT EXISTS `eks_abrasi`            ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';
ALTER TABLE `penilaian_medis_igd` ADD COLUMN IF NOT EXISTS `eks_penetrasi`         ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';
ALTER TABLE `penilaian_medis_igd` ADD COLUMN IF NOT EXISTS `eks_burn`              ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';
ALTER TABLE `penilaian_medis_igd` ADD COLUMN IF NOT EXISTS `eks_tenderness`        ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';
ALTER TABLE `penilaian_medis_igd` ADD COLUMN IF NOT EXISTS `eks_laserasi`          ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';
ALTER TABLE `penilaian_medis_igd` ADD COLUMN IF NOT EXISTS `eks_swelling`          ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';
ALTER TABLE `penilaian_medis_igd` ADD COLUMN IF NOT EXISTS `eks_tidak_tampak_jelas` ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';

-- ---------------------------------------------------------------------
-- KELOMPOK H: Kepala Leher (halaman 2). Menggantikan kolom `kepala`/`mata`
-- lama dari sisi UI aktif (kolom lama TETAP ADA, lihat catatan di atas).
-- Diagram anatomi ($P{lokalis}) SUDAH ADA & berfungsi, tidak perlu kolom
-- baru untuk itu.
-- ---------------------------------------------------------------------
ALTER TABLE `penilaian_medis_igd` ADD COLUMN IF NOT EXISTS `konjungtiva`  ENUM('Pucat','Pink') NULL DEFAULT NULL;
ALTER TABLE `penilaian_medis_igd` ADD COLUMN IF NOT EXISTS `sklera`       ENUM('Ikterik','Tidak Ikterik') NULL DEFAULT NULL;
ALTER TABLE `penilaian_medis_igd` ADD COLUMN IF NOT EXISTS `bibir_lidah`  ENUM('Sianosis','Tidak Sianosis') NULL DEFAULT NULL;
ALTER TABLE `penilaian_medis_igd` ADD COLUMN IF NOT EXISTS `mukosa_mulut` ENUM('Kering','Basah') NULL DEFAULT NULL;

-- ---------------------------------------------------------------------
-- KELOMPOK I: Leher rinci. Menggantikan kolom `leher` lama dari sisi UI
-- aktif (kolom lama TETAP ADA, lihat catatan di atas).
-- ---------------------------------------------------------------------
ALTER TABLE `penilaian_medis_igd` ADD COLUMN IF NOT EXISTS `deviasi_trakea` ENUM('Kanan','Kiri','Tidak Ada') NULL DEFAULT NULL;
ALTER TABLE `penilaian_medis_igd` ADD COLUMN IF NOT EXISTS `jvp`            ENUM('Meningkat','Tidak') NULL DEFAULT NULL;
ALTER TABLE `penilaian_medis_igd` ADD COLUMN IF NOT EXISTS `lnn`            ENUM('Teraba','Tidak') NULL DEFAULT NULL;
ALTER TABLE `penilaian_medis_igd` ADD COLUMN IF NOT EXISTS `tiroid`         ENUM('Teraba','Tidak') NULL DEFAULT NULL;

-- ---------------------------------------------------------------------
-- KELOMPOK J: Thorax. Menggantikan kolom `thoraks` lama dari sisi UI
-- aktif (kolom lama TETAP ADA, lihat catatan di atas).
-- ---------------------------------------------------------------------
ALTER TABLE `penilaian_medis_igd` ADD COLUMN IF NOT EXISTS `jantung` VARCHAR(500) NOT NULL DEFAULT '';
ALTER TABLE `penilaian_medis_igd` ADD COLUMN IF NOT EXISTS `paru`    VARCHAR(500) NOT NULL DEFAULT '';

-- ---------------------------------------------------------------------
-- KELOMPOK K: Abdomen & Pelvis. Menggantikan kolom `abdomen` lama dari
-- sisi UI aktif (kolom lama TETAP ADA, lihat catatan di atas).
-- `ekstremitas_pelvis` SENGAJA field BARU & TERPISAH dari kolom
-- `ekstremitas` lama (belum dikonfirmasi ke dokter apakah konsepnya
-- sama - lihat PROGRESS.md/percakapan sesi ini).
-- ---------------------------------------------------------------------
ALTER TABLE `penilaian_medis_igd` ADD COLUMN IF NOT EXISTS `punggung_pinggang` VARCHAR(500) NOT NULL DEFAULT '';
ALTER TABLE `penilaian_medis_igd` ADD COLUMN IF NOT EXISTS `ekstremitas_pelvis` VARCHAR(500) NOT NULL DEFAULT '';

-- ---------------------------------------------------------------------
-- KELOMPOK L: Pemeriksaan Penunjang tambahan. Laboratorium/X-Ray/EKG
-- SENGAJA TIDAK dipasangkan checkbox baru - pakai kolom teks `lab`/`rad`/
-- `ekg` existing apa adanya (permintaan user, jangan duplikasi field yang
-- sudah ada).
-- CT Scan/USG (checkbox+keterangan) DIHAPUS - user memutuskan cukup pakai
-- Pemeriksaan Penunjang bawaan, tidak perlu modalitas terpisah.
-- `pemeriksaan_lainnya` dibuat sama persis gaya dgn `lab`/`rad`/`ekg`
-- existing (TEXT polos, tanpa kolom checkbox) supaya seragam.
-- ---------------------------------------------------------------------
ALTER TABLE `penilaian_medis_igd` ADD COLUMN IF NOT EXISTS `pemeriksaan_lainnya` TEXT NOT NULL;

-- ---------------------------------------------------------------------
-- KELOMPOK M: Diagnosis (pecah jadi Kerja/Banding). Menggantikan kolom
-- `diagnosis` lama dari sisi UI aktif (kolom lama TETAP ADA, lihat
-- catatan di atas).
-- ---------------------------------------------------------------------
ALTER TABLE `penilaian_medis_igd` ADD COLUMN IF NOT EXISTS `diagnosa_kerja_cek`   ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';
ALTER TABLE `penilaian_medis_igd` ADD COLUMN IF NOT EXISTS `diagnosa_kerja`       VARCHAR(500) NOT NULL DEFAULT '';
ALTER TABLE `penilaian_medis_igd` ADD COLUMN IF NOT EXISTS `diagnosa_banding_cek` ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';
ALTER TABLE `penilaian_medis_igd` ADD COLUMN IF NOT EXISTS `diagnosa_banding`     VARCHAR(500) NOT NULL DEFAULT '';

-- ---------------------------------------------------------------------
-- KELOMPOK N: Konsul dr. Spesialis. Dokter & Unit/Poli konsul dipilih lewat
-- dialog pencarian (pola sama seperti reg_periksa.kd_dokter/kd_poli di
-- Registrasi) - disimpan sebagai KODE (bukan nama bebas), nama ditampilkan
-- via JOIN ke tabel dokter/poliklinik saat dibutuhkan.
-- Catatan: 8 kolom checkbox per-spesialisasi (konsul_penyakit_dalam..
-- konsul_tht) DIPENSIUNKAN dari UI aktif (redundan - spesialisasi sudah
-- tersirat dari dokter/poli konsul yg dipilih) - kolom TETAP ADA di skema
-- (data lama tetap terbaca), hanya TIDAK LAGI DITULIS oleh form.
-- ---------------------------------------------------------------------
ALTER TABLE `penilaian_medis_igd` ADD COLUMN IF NOT EXISTS `konsul_penyakit_dalam` ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';
ALTER TABLE `penilaian_medis_igd` ADD COLUMN IF NOT EXISTS `konsul_anak`           ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';
ALTER TABLE `penilaian_medis_igd` ADD COLUMN IF NOT EXISTS `konsul_bedah`          ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';
ALTER TABLE `penilaian_medis_igd` ADD COLUMN IF NOT EXISTS `konsul_syaraf`         ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';
ALTER TABLE `penilaian_medis_igd` ADD COLUMN IF NOT EXISTS `konsul_obgyn`          ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';
ALTER TABLE `penilaian_medis_igd` ADD COLUMN IF NOT EXISTS `konsul_orthopedi`      ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';
ALTER TABLE `penilaian_medis_igd` ADD COLUMN IF NOT EXISTS `konsul_mata`           ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';
ALTER TABLE `penilaian_medis_igd` ADD COLUMN IF NOT EXISTS `konsul_tht`            ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';
ALTER TABLE `penilaian_medis_igd` ADD COLUMN IF NOT EXISTS `konsul_kd_dokter`      VARCHAR(20) NOT NULL DEFAULT '';
ALTER TABLE `penilaian_medis_igd` ADD COLUMN IF NOT EXISTS `konsul_kd_poli`        VARCHAR(20) NOT NULL DEFAULT '';

-- ---------------------------------------------------------------------
-- KELOMPOK O: Edukasi
-- ---------------------------------------------------------------------
ALTER TABLE `penilaian_medis_igd` ADD COLUMN IF NOT EXISTS `edukasi_diberikan`     ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';
ALTER TABLE `penilaian_medis_igd` ADD COLUMN IF NOT EXISTS `edukasi_tidak_bisa`    ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';
ALTER TABLE `penilaian_medis_igd` ADD COLUMN IF NOT EXISTS `edukasi_tidak_bisa_ket` VARCHAR(300) NOT NULL DEFAULT '';

-- ---------------------------------------------------------------------
-- KELOMPOK P: Kondisi Saat Pulang. Catatan: berpotensi tumpang tindih
-- konsep dengan resume_pasien.kondisi_pulang dan reg_periksa.stts (menu
-- Status di DlgIGD.java) - disengaja, tidak disinkronkan (di luar scope
-- permintaan user saat ini).
-- ---------------------------------------------------------------------
ALTER TABLE `penilaian_medis_igd` ADD COLUMN IF NOT EXISTS `kondisi_pulang`     ENUM('Membaik','Memburuk','Tetap','Meninggal','Death On Arrival') NULL DEFAULT NULL;
ALTER TABLE `penilaian_medis_igd` ADD COLUMN IF NOT EXISTS `kondisi_pulang_jam` TIME NULL DEFAULT NULL;

-- ---------------------------------------------------------------------
-- KELOMPOK Q: Tanda Vital Terakhir (saat pulang - SET KEDUA, terpisah
-- dari tanda vital awal `td`/`nadi`/`rr`/`suhu` existing yang tetap
-- dipakai untuk tanda vital awal masuk).
-- ---------------------------------------------------------------------
ALTER TABLE `penilaian_medis_igd` ADD COLUMN IF NOT EXISTS `td_akhir`   VARCHAR(8) NOT NULL DEFAULT '';
ALTER TABLE `penilaian_medis_igd` ADD COLUMN IF NOT EXISTS `nadi_akhir` VARCHAR(5) NOT NULL DEFAULT '';
ALTER TABLE `penilaian_medis_igd` ADD COLUMN IF NOT EXISTS `rr_akhir`   VARCHAR(5) NOT NULL DEFAULT '';
ALTER TABLE `penilaian_medis_igd` ADD COLUMN IF NOT EXISTS `suhu_akhir` VARCHAR(5) NOT NULL DEFAULT '';

-- ---------------------------------------------------------------------
-- KELOMPOK R: DEFAULT utk 10 kolom lama yang dipensiunkan dari UI aktif
-- (lihat catatan di kepala file). Kolom-kolom ini semula NOT NULL TANPA
-- default - form baru sudah tidak lagi mengisinya, jadi INSERT baris baru
-- akan gagal kalau tidak diberi DEFAULT. ALTER COLUMN SET DEFAULT TIDAK
-- mengubah tipe data/constraint/data existing - cuma menambah nilai
-- fallback utk kolom yg tidak disebut saat INSERT, aman & additive.
-- ---------------------------------------------------------------------
ALTER TABLE `penilaian_medis_igd` ALTER COLUMN `kepala`      SET DEFAULT 'Tidak Diperiksa';
ALTER TABLE `penilaian_medis_igd` ALTER COLUMN `mata`        SET DEFAULT 'Tidak Diperiksa';
ALTER TABLE `penilaian_medis_igd` ALTER COLUMN `gigi`        SET DEFAULT 'Tidak Diperiksa';
ALTER TABLE `penilaian_medis_igd` ALTER COLUMN `leher`       SET DEFAULT 'Tidak Diperiksa';
ALTER TABLE `penilaian_medis_igd` ALTER COLUMN `thoraks`     SET DEFAULT 'Tidak Diperiksa';
ALTER TABLE `penilaian_medis_igd` ALTER COLUMN `abdomen`     SET DEFAULT 'Tidak Diperiksa';
ALTER TABLE `penilaian_medis_igd` ALTER COLUMN `genital`     SET DEFAULT 'Tidak Diperiksa';
ALTER TABLE `penilaian_medis_igd` ALTER COLUMN `ekstremitas` SET DEFAULT 'Tidak Diperiksa';
ALTER TABLE `penilaian_medis_igd` ALTER COLUMN `gcs`         SET DEFAULT '';
ALTER TABLE `penilaian_medis_igd` ALTER COLUMN `diagnosis`   SET DEFAULT '';

-- Verifikasi struktur tabel setelah migrasi
DESCRIBE `penilaian_medis_igd`;
