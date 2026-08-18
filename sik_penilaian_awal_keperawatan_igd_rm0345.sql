-- ============================================================
-- Migrasi: Penilaian Awal Keperawatan IGD (RM 03-4 / 03-5 / 03-6)
-- RSUD Demang Sepulau Raya - SIMRS Khanza
-- Formulir target: RM 03-4 (Penilaian Awal Keperawatan Gawat Darurat),
-- RM 03-5 (Skrining Gizi + Asesmen Nyeri), RM 03-6 (FLACC + PQRST +
-- Diagnosis/Intervensi Keperawatan + Tindakan) + lanjutan (Pemberian
-- Obat/Infus + Kondisi Pasien Saat Pindah/Pulang dari IGD).
--
-- STATUS: File ini untuk diimport manual oleh pengguna (mis. lewat
-- phpMyAdmin di XAMPP). TIDAK dijalankan otomatis oleh Claude Code
-- kecuali diminta eksplisit.
--
-- Prinsip desain:
--  - Additive only: tidak ada DROP/CHANGE pada kolom/tabel existing.
--  - Semua kolom baru masuk ke tabel `penilaian_awal_keperawatan_igd`
--    yang sudah ada (tetap 1:1 dengan no_rawat), KECUALI "Pemberian
--    Obat/Infus" yang butuh tabel anak baru (tabel berulang, banyak
--    baris per no_rawat).
--  - Semua statement pakai IF NOT EXISTS supaya aman dijalankan ulang.
--  - Field lama yang datanya SUDAH ADA di data registrasi pasien
--    (Agama, Bahasa, Cacat Fisik - dikonfirmasi user) TIDAK dibuatkan
--    kolom baru, tetap reuse widget lama yang sudah baca dari sana.
--  - Field lama yang wording/strukturnya SUDAH BEDA dari formulir fisik
--    terbaru dipensiunkan dari UI aktif (TETAP ADA di skema, data lama
--    tetap terbaca), diganti kolom baru dengan wording sesuai foto:
--      psikologis, jiwa, perilaku, dilaporkan, sebutkan
--      tinggal_dengan, ket_tinggal
--      berjalan_a, berjalan_b, berjalan_c, hasil
--      nyeri
--      provokes, ket_provokes, quality, ket_quality, menyebar, durasi
--    (Lihat juga catatan yang sama di RMPenilaianAwalKeperawatanIGD.java
--    dekat deklarasi widget lama, supaya tidak sengaja dipakai lagi.)
--  - Field "II. PEMERIKSAAN FISIK" lama (tekanan, pupil, neurosensorik,
--    integumen, turgor, edema, mukosa, perdarahan, intoksikasi, bab,
--    bak, dst) TIDAK disentuh - tidak ada foto formulir fisik utk
--    section ini di sesi ini, jadi dibiarkan aktif apa adanya.
-- ============================================================

-- ---------------------------------------------------------------------
-- KELOMPOK A: Riwayat Alergi
-- ---------------------------------------------------------------------
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `alergi_ada`      ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `alergi_sebutkan` VARCHAR(200) NOT NULL DEFAULT '';

-- ---------------------------------------------------------------------
-- KELOMPOK B: Keadaan Umum (Tanda Vital) - form ini sebelumnya sama
-- sekali tidak punya kolom tanda vital sendiri.
-- ---------------------------------------------------------------------
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `td`   VARCHAR(8) NOT NULL DEFAULT '';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `nadi` VARCHAR(5) NOT NULL DEFAULT '';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `suhu` VARCHAR(5) NOT NULL DEFAULT '';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `spo`  VARCHAR(5) NOT NULL DEFAULT '';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `rr`   VARCHAR(5) NOT NULL DEFAULT '';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `bb`   VARCHAR(5) NOT NULL DEFAULT '';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `tb`   VARCHAR(5) NOT NULL DEFAULT '';

-- ---------------------------------------------------------------------
-- KELOMPOK C: Asesmen Psikologis (checkbox multi, ganti dari dropdown
-- tunggal `psikologis` lama - wording & struktur beda dari formulir).
-- ---------------------------------------------------------------------
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `psi_tenang`                  ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `psi_cemas`                   ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `psi_takut`                   ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `psi_sedih`                   ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `psi_marah`                   ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `psi_bunuh_diri`              ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `psi_masalah_prilaku`         ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `psi_masalah_prilaku_sebutkan` VARCHAR(200) NOT NULL DEFAULT '';

-- ---------------------------------------------------------------------
-- KELOMPOK D: Status Sosial dan Ekonomis. `status_pernikahan` dan
-- `pendidikan_pasien` SENGAJA kolom baru terpisah dari widget lama
-- `StatusPernikahan`/`PendidikanPasien` yang read-only dari data
-- registrasi pasien (`pasien.stts_nikah`/`pasien.pnd`) - permintaan
-- eksplisit user, widget lama TETAP ADA sbg referensi read-only.
-- `tempat_tinggal` ganti `tinggal_dengan` lama (opsi beda).
-- ---------------------------------------------------------------------
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `status_pernikahan`     ENUM('Belum Menikah','Menikah','Cerai','Lainnya') NULL DEFAULT NULL;
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `ket_status_pernikahan` VARCHAR(100) NOT NULL DEFAULT '';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `pekerjaan_kep`         VARCHAR(100) NOT NULL DEFAULT '';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `pendidikan_pasien_kep` VARCHAR(50) NOT NULL DEFAULT '';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `tempat_tinggal`        ENUM('Rumah Sendiri','Rumah Keluarganya','Lainnya') NULL DEFAULT NULL;
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `ket_tempat_tinggal`    VARCHAR(100) NOT NULL DEFAULT '';

-- ---------------------------------------------------------------------
-- KELOMPOK E: Skrining Geriatri - section baru, tidak ada padanan lama.
-- ---------------------------------------------------------------------
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `geri_60_multi_penyakit`   ENUM('Ya','Tidak') NULL DEFAULT NULL;
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `geri_60_penurunan_fungsi`  ENUM('Ya','Tidak') NULL DEFAULT NULL;
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `geri_70_1_penyakit`        ENUM('Ya','Tidak') NULL DEFAULT NULL;

-- ---------------------------------------------------------------------
-- KELOMPOK F: Skrining Resiko Jatuh (Get Up and Go) - ganti dari
-- `berjalan_a/b/c`+`hasil` lama (3 sub-kriteria + hasil 3-tingkat)
-- ke struktur formulir baru (2 kriteria A/B + hasil biner).
-- ---------------------------------------------------------------------
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `jatuh_cara_berjalan`   ENUM('Ya','Tidak') NULL DEFAULT NULL;
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `jatuh_menopang_duduk`   ENUM('Ya','Tidak') NULL DEFAULT NULL;
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `jatuh_hasil`            ENUM('Tidak Beresiko','Beresiko') NULL DEFAULT NULL;

-- ---------------------------------------------------------------------
-- KELOMPOK G: Pengkajian Spiritual, Kepercayaan, Fungsional dan Budaya.
-- Agama/Bahasa/Cacat Tubuh REUSE widget lama dari registrasi pasien
-- (dikonfirmasi user, TIDAK bikin kolom baru) - kelompok ini cuma utk
-- detail yang belum ada representasinya sama sekali.
-- ---------------------------------------------------------------------
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `wajib_ibadah`         ENUM('Baligh','Belum Baligh','Halangan Lain') NULL DEFAULT NULL;
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `ket_wajib_ibadah`     VARCHAR(100) NOT NULL DEFAULT '';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `thaharoh`              ENUM('Berwudhu','Tayamum') NULL DEFAULT NULL;
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `sholat`                 ENUM('Berdiri','Duduk','Berbaring') NULL DEFAULT NULL;
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `nilai_kepercayaan`     ENUM('Tidak','Ya') NULL DEFAULT NULL;
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `ket_nilai_kepercayaan` VARCHAR(100) NOT NULL DEFAULT '';

-- Revisi (permintaan user, sesi lanjutan): Agama diubah dari reuse
-- readonly registrasi jadi input manual (kolom baru, independen dari
-- pasien.agama - dinamai agama_manual supaya tidak bentrok nama kolom
-- dgn hasil JOIN pasien.agama di getData()/isRawat()). Cacat Tubuh/
-- Disabilitas diubah dari reuse readonly jadi checkbox manual sesuai
-- foto fisik (Tidak/Tuna Netra/Tuna Daksa/Tuna Rungu/Tuna Wicara).
-- Bahasa Sehari-hari DIHAPUS dari UI (tak ada di foto fisik formulir) -
-- tidak pernah punya kolom sendiri di tabel ini, jadi tidak ada yg perlu
-- diretensi.
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `agama_manual`      VARCHAR(50) NOT NULL DEFAULT '';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `cacat_tidak`       ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `cacat_tuna_netra`  ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `cacat_tuna_daksa`  ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `cacat_tuna_rungu`  ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `cacat_tuna_wicara` ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';

-- ---------------------------------------------------------------------
-- KELOMPOK H: Skrining Gizi Dewasa (Malnutrition Screening Tool / MST)
-- - section baru sama sekali, RM 03-5.
-- ---------------------------------------------------------------------
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `mst_penurunan_bb`              ENUM('Tidak Ada','Tidak Yakin','Ya') NULL DEFAULT NULL;
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `mst_penurunan_bb_kg`           ENUM('1-5 kg','6-10 kg','11-15 kg','>15 kg') NULL DEFAULT NULL;
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `mst_asupan_berkurang`          ENUM('Ya','Tidak') NULL DEFAULT NULL;
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `mst_total_skor`                VARCHAR(5) NOT NULL DEFAULT '';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `mst_diagnosis_khusus`          ENUM('Ya','Tidak') NULL DEFAULT NULL;
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `mst_diagnosis_khusus_sebutkan` VARCHAR(100) NOT NULL DEFAULT '';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `mst_kesimpulan`                ENUM('Risiko','Tidak Risiko') NULL DEFAULT NULL;

-- ---------------------------------------------------------------------
-- KELOMPOK I: Skrining Gizi Anak (Usia 0-14 tahun) - section baru.
-- ---------------------------------------------------------------------
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `gizi_anak_kurus`             ENUM('Ya','Tidak') NULL DEFAULT NULL;
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `gizi_anak_turun_bb`          ENUM('Ya','Tidak') NULL DEFAULT NULL;
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `gizi_anak_kondisi_khusus`    ENUM('Ya','Tidak') NULL DEFAULT NULL;
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `gizi_anak_risiko_malnutrisi` ENUM('Ya','Tidak') NULL DEFAULT NULL;
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `gizi_anak_total_skor`        VARCHAR(5) NOT NULL DEFAULT '';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `gizi_anak_kesimpulan`        ENUM('Risiko Rendah','Risiko Sedang','Risiko Tinggi') NULL DEFAULT NULL;

-- ---------------------------------------------------------------------
-- KELOMPOK J: Asesmen Nyeri (bagian atas) - ganti `nyeri` lama
-- (3-opsi: Tidak Ada Nyeri/Nyeri Akut/Nyeri Kronis) ke Ya/Tidak biner
-- sesuai formulir. `lokasi`/`skala_nyeri` existing tetap dipakai apa
-- adanya (sudah cocok).
-- ---------------------------------------------------------------------
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `nyeri_ada` ENUM('Ya','Tidak') NULL DEFAULT NULL;

-- ---------------------------------------------------------------------
-- KELOMPOK K: FLACC (Face, Legs, Activity, Cry, Consolability) - utk
-- bayi/anak <3 tahun, section baru sama sekali, RM 03-6.
-- ---------------------------------------------------------------------
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `flacc_wajah`     ENUM('0','1','2') NULL DEFAULT NULL;
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `flacc_kaki`      ENUM('0','1','2') NULL DEFAULT NULL;
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `flacc_aktivitas` ENUM('0','1','2') NULL DEFAULT NULL;
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `flacc_menangis`  ENUM('0','1','2') NULL DEFAULT NULL;
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `flacc_bersuara`  ENUM('0','1','2') NULL DEFAULT NULL;
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `flacc_total`     VARCHAR(5) NOT NULL DEFAULT '';

-- ---------------------------------------------------------------------
-- KELOMPOK L: PQRST - ganti total dari `provokes`/`quality`/`menyebar`/
-- `durasi` lama (wording klinis lama, tidak cocok dgn checkbox formulir
-- terbaru). `nyeri_hilang`/`ket_nyeri`/`pada_dokter`/`ket_dokter` TIDAK
-- disentuh (tidak ada bukti foto utk field ini, dibiarkan aktif).
-- ---------------------------------------------------------------------
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `pqrst_prov_cahaya`    ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `pqrst_prov_gelap`     ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `pqrst_prov_gerakan`   ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `pqrst_prov_berbaring` ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `pqrst_qual_tumpul`    ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `pqrst_qual_ditusuk`   ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `pqrst_qual_berdenyut` ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `pqrst_qual_tajam`     ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `pqrst_qual_panas`     ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `pqrst_radiation`      ENUM('Tidak','Ya') NULL DEFAULT NULL;
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `pqrst_radiation_ke`   VARCHAR(100) NOT NULL DEFAULT '';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `pqrst_severity`       ENUM('Tidak Nyeri','Ringan','Sedang','Berat') NULL DEFAULT NULL;
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `pqrst_time`           ENUM('Terus Menerus','Hilang Timbul','<30 Menit','>30 Menit') NULL DEFAULT NULL;

-- ---------------------------------------------------------------------
-- KELOMPOK M: II. Intervensi Keperawatan - checklist flat 14 item +
-- lainnya, KOLOM BOOLEAN LANGSUNG (bukan tabel master+anak, keputusan
-- user) - menggantikan sistem `master_rencana_keperawatan_igd` lama yg
-- isinya cuma data uji ("tes 1"/"tes 2") & strukturnya hierarkis
-- (rencana tergantung masalah). Tabel lama TETAP ADA (additive-only,
-- tidak dihapus), cuma tidak dipakai UI baru.
-- ---------------------------------------------------------------------
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `interv_manajemen_jalan_nafas`      ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `interv_suctioning_jalan_nafas`     ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `interv_manajemen_asam_basa`        ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `interv_manajemen_syok`             ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `interv_manajemen_nyeri`            ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `interv_manajemen_cairan`           ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `interv_monitoring_cairan`          ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `interv_manajemen_lingkungan`       ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `interv_pendidikan_kesehatan`       ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `interv_fever_treatment`            ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `interv_manajemen_sensasi_perifer`  ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `interv_monitoring_kardiovaskuler`  ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `interv_pencegahan_penekanan_luka`  ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `interv_lainnya`                    VARCHAR(200) NOT NULL DEFAULT '';

-- ---------------------------------------------------------------------
-- KELOMPOK N: IV. Kardiopulmonal
-- ---------------------------------------------------------------------
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `kardio_pulse_oximetri`     ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `kardio_intubasi`            ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `kardio_rjp`                 ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `kardio_rjp_ket`             VARCHAR(100) NOT NULL DEFAULT '';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `kardio_kardioversi_dc_shock` ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';

-- ---------------------------------------------------------------------
-- KELOMPOK O: V. Tindakan Penunjang
-- ---------------------------------------------------------------------
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `penunjang_cek_lab`      ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `penunjang_cek_lab_ket`  VARCHAR(100) NOT NULL DEFAULT '';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `penunjang_rontgen`      ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `penunjang_rontgen_ket`  VARCHAR(100) NOT NULL DEFAULT '';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `penunjang_ct_scan`      ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `penunjang_ct_scan_ket`  VARCHAR(100) NOT NULL DEFAULT '';

-- ---------------------------------------------------------------------
-- KELOMPOK P: VI. Tindakan Kolaborasi
-- ---------------------------------------------------------------------
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `kolab_o2`                      ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `kolab_o2_ket`                  VARCHAR(100) NOT NULL DEFAULT '';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `kolab_ekg_monitor`             ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `kolab_nebulizer`               ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `kolab_pasang_infus`            ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `kolab_pasang_infus_ket`        VARCHAR(100) NOT NULL DEFAULT '';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `kolab_injeksi_syringe_pump`    ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `kolab_transfusi_darah`         ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `kolab_transfusi_darah_ket`     VARCHAR(100) NOT NULL DEFAULT '';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `kolab_ngt`                     ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `kolab_ngt_ukuran`              VARCHAR(50) NOT NULL DEFAULT '';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `kolab_bilas_lambung`           ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `kolab_bilas_lambung_ket`       VARCHAR(100) NOT NULL DEFAULT '';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `kolab_dower_catheter`          ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `kolab_dower_catheter_ukuran`   VARCHAR(50) NOT NULL DEFAULT '';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `kolab_restrain_pengaman`       ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';

-- ---------------------------------------------------------------------
-- KELOMPOK Q: VII. Trauma / Bedah
-- ---------------------------------------------------------------------
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `trauma_cervical_collar`                ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `trauma_immobilisasi_tulang_belakang`   ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `trauma_gips`                           ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `trauma_perawatan_luka`                 ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `trauma_jahit_luka`                     ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `trauma_balutan_spalk`                  ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `trauma_selang_dada_wsd`                ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `trauma_lainnya`                        VARCHAR(200) NOT NULL DEFAULT '';

-- ---------------------------------------------------------------------
-- KELOMPOK R: VIII. THT / Mata
-- ---------------------------------------------------------------------
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `tht_irigasi_mata_telinga`    ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `tht_ambil_korpus_alienum`    ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';

-- ---------------------------------------------------------------------
-- KELOMPOK S: IX. Tindakan Obgyn
-- ---------------------------------------------------------------------
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `obgyn_membantu_partus`      ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `obgyn_periksa_ginekologi`   ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `obgyn_doppler_ctg_usg`      ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `obgyn_pasang_tampon`        ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';

-- ---------------------------------------------------------------------
-- KELOMPOK T: Kondisi Pasien Saat Pindah/Pulang dari IGD - section baru
-- sama sekali (halaman lanjutan RM 03-6). Tanda tangan Perawat/Bidan +
-- Dokter pakai QR code digital (dikonfirmasi user, pola sama RM 03-1/
-- 03-2) - jadi cukup simpan kode dokter/perawat yg bertanggung jawab,
-- tidak perlu kolom gambar tanda tangan.
-- ---------------------------------------------------------------------
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `kp_pukul`                  TIME NULL DEFAULT NULL;
-- kp_pukul/kp_dipulangkan_pukul/kp_meninggal_pukul diubah dari TIME ke
-- DATETIME (permintaan user) - tanggalnya bisa beda dari kolom "tanggal"
-- (tanggal kunjungan), mis. pasien masuk/pindah/meninggal lewat tengah
-- malam. MODIFY COLUMN terpisah (bukan ubah ADD COLUMN di atas) supaya
-- instalasi yang sudah pernah jalankan migrasi ini (kolomnya sudah ada
-- sbg TIME, ADD COLUMN IF NOT EXISTS akan di-skip) tetap ikut ter-upgrade.
ALTER TABLE `penilaian_awal_keperawatan_igd` MODIFY COLUMN `kp_pukul`                  DATETIME NULL DEFAULT NULL;
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `kp_gcs_e`                  VARCHAR(5) NOT NULL DEFAULT '';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `kp_gcs_v`                  VARCHAR(5) NOT NULL DEFAULT '';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `kp_gcs_m`                  VARCHAR(5) NOT NULL DEFAULT '';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `kp_td`                     VARCHAR(8) NOT NULL DEFAULT '';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `kp_nadi`                   VARCHAR(5) NOT NULL DEFAULT '';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `kp_nadi_reguler`           ENUM('Reguler','Ireguler') NULL DEFAULT NULL;
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `kp_suhu`                   VARCHAR(5) NOT NULL DEFAULT '';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `kp_pernafasan`              VARCHAR(5) NOT NULL DEFAULT '';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `kp_spo`                    VARCHAR(5) NOT NULL DEFAULT '';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `kp_masuk_rs`               ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `kp_ruang`                  VARCHAR(100) NOT NULL DEFAULT '';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `kp_petugas_penerima`       VARCHAR(100) NOT NULL DEFAULT '';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `kp_pendamping`             ENUM('Dokter','Perawat') NULL DEFAULT NULL;
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `kp_tindakan_lanjutan`      ENUM('Kamar Operasi','Kamar Bersalin','HCU','Kamar Jenazah','Lainnya') NULL DEFAULT NULL;
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `kp_tindakan_lanjutan_ket`  VARCHAR(100) NOT NULL DEFAULT '';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `kp_dirujuk`                ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `kp_dirujuk_ket`            VARCHAR(150) NOT NULL DEFAULT '';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `kp_dirujuk_alasan`         VARCHAR(200) NOT NULL DEFAULT '';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `kp_dipulangkan`            ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `kp_dipulangkan_pukul`      TIME NULL DEFAULT NULL;
ALTER TABLE `penilaian_awal_keperawatan_igd` MODIFY COLUMN `kp_dipulangkan_pukul`      DATETIME NULL DEFAULT NULL;
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `kp_meninggal`              ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `kp_meninggal_pukul`        TIME NULL DEFAULT NULL;
ALTER TABLE `penilaian_awal_keperawatan_igd` MODIFY COLUMN `kp_meninggal_pukul`        DATETIME NULL DEFAULT NULL;
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `kp_doa`                    ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `kp_edukasi_makan_obat`     ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `kp_edukasi_luka`           ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `kp_edukasi_diet`           ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `kp_edukasi_diet_ket`       VARCHAR(100) NOT NULL DEFAULT '';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `kp_edukasi_lain`           ENUM('Ya','Tidak') NOT NULL DEFAULT 'Tidak';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `kp_edukasi_lain_ket`       VARCHAR(100) NOT NULL DEFAULT '';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `kp_kd_dokter`              VARCHAR(20) NOT NULL DEFAULT '';
ALTER TABLE `penilaian_awal_keperawatan_igd` ADD COLUMN IF NOT EXISTS `kp_kd_perawat`             VARCHAR(20) NOT NULL DEFAULT '';

-- ---------------------------------------------------------------------
-- KELOMPOK U: Pemberian Obat / Infus - TABEL ANAK BARU (repeating,
-- banyak baris per no_rawat). Pola disalin dari `intervensi_nyeri_
-- farmakologi` (lihat RMDataIntervensiNyeriFarmakologi.java) - beda
-- dari analognya, formulir minta "Diperiksa Oleh" DAN "Diberikan Oleh"
-- terpisah (analog cuma punya 1 `nip`).
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `penilaian_awal_keperawatan_igd_obat` (
  `no_rawat` VARCHAR(17) NOT NULL,
  `tanggal` DATE NOT NULL,
  `pukul` TIME NOT NULL,
  `nama_obat` VARCHAR(150) NOT NULL DEFAULT '',
  `dosis` VARCHAR(50) NOT NULL DEFAULT '',
  `rute` VARCHAR(50) NOT NULL DEFAULT '',
  `nip_diperiksa` VARCHAR(20) NOT NULL DEFAULT '',
  `nip_diberikan` VARCHAR(20) NOT NULL DEFAULT '',
  PRIMARY KEY (`no_rawat`,`tanggal`,`pukul`,`nama_obat`),
  KEY `nip_diperiksa` (`nip_diperiksa`),
  KEY `nip_diberikan` (`nip_diberikan`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

-- ---------------------------------------------------------------------
-- KELOMPOK V: DEFAULT utk kolom lama yang dipensiunkan dari UI aktif
-- (lihat catatan di kepala file) - semula NOT NULL TANPA default, INSERT
-- baris baru (kolom bernama, tidak menyebut kolom pensiun) akan gagal
-- kalau tidak diberi DEFAULT. ALTER COLUMN SET DEFAULT TIDAK mengubah
-- tipe data/constraint/data existing - additive & aman.
-- ---------------------------------------------------------------------
ALTER TABLE `penilaian_awal_keperawatan_igd` ALTER COLUMN `psikologis`    SET DEFAULT 'Tidak Ada Masalah';
ALTER TABLE `penilaian_awal_keperawatan_igd` ALTER COLUMN `jiwa`          SET DEFAULT 'Tidak';
ALTER TABLE `penilaian_awal_keperawatan_igd` ALTER COLUMN `perilaku`      SET DEFAULT '-';
ALTER TABLE `penilaian_awal_keperawatan_igd` ALTER COLUMN `hubungan`      SET DEFAULT 'Harmonis';
ALTER TABLE `penilaian_awal_keperawatan_igd` ALTER COLUMN `tinggal_dengan` SET DEFAULT 'Sendiri';
ALTER TABLE `penilaian_awal_keperawatan_igd` ALTER COLUMN `budaya`        SET DEFAULT 'Tidak Ada';
ALTER TABLE `penilaian_awal_keperawatan_igd` ALTER COLUMN `ket_budaya`    SET DEFAULT '';
ALTER TABLE `penilaian_awal_keperawatan_igd` ALTER COLUMN `pendidikan_pj` SET DEFAULT '-';
ALTER TABLE `penilaian_awal_keperawatan_igd` ALTER COLUMN `edukasi`       SET DEFAULT 'Pasien';
ALTER TABLE `penilaian_awal_keperawatan_igd` ALTER COLUMN `ket_edukasi`   SET DEFAULT '';
ALTER TABLE `penilaian_awal_keperawatan_igd` ALTER COLUMN `kemampuan`     SET DEFAULT 'Mandiri';
ALTER TABLE `penilaian_awal_keperawatan_igd` ALTER COLUMN `aktifitas`     SET DEFAULT 'Duduk';
ALTER TABLE `penilaian_awal_keperawatan_igd` ALTER COLUMN `alat_bantu`    SET DEFAULT 'Tidak';
ALTER TABLE `penilaian_awal_keperawatan_igd` ALTER COLUMN `nyeri`         SET DEFAULT 'Tidak Ada Nyeri';
ALTER TABLE `penilaian_awal_keperawatan_igd` ALTER COLUMN `provokes`      SET DEFAULT 'Lain-lain';
ALTER TABLE `penilaian_awal_keperawatan_igd` ALTER COLUMN `ket_provokes`  SET DEFAULT '';
ALTER TABLE `penilaian_awal_keperawatan_igd` ALTER COLUMN `quality`       SET DEFAULT 'Lain-lain';
ALTER TABLE `penilaian_awal_keperawatan_igd` ALTER COLUMN `ket_quality`   SET DEFAULT '';
ALTER TABLE `penilaian_awal_keperawatan_igd` ALTER COLUMN `lokasi`        SET DEFAULT '';
ALTER TABLE `penilaian_awal_keperawatan_igd` ALTER COLUMN `menyebar`      SET DEFAULT 'Tidak';
ALTER TABLE `penilaian_awal_keperawatan_igd` ALTER COLUMN `skala_nyeri`   SET DEFAULT '0';
ALTER TABLE `penilaian_awal_keperawatan_igd` ALTER COLUMN `durasi`        SET DEFAULT '';
ALTER TABLE `penilaian_awal_keperawatan_igd` ALTER COLUMN `nyeri_hilang`  SET DEFAULT 'Istirahat';
ALTER TABLE `penilaian_awal_keperawatan_igd` ALTER COLUMN `pada_dokter`   SET DEFAULT 'Tidak';
ALTER TABLE `penilaian_awal_keperawatan_igd` ALTER COLUMN `berjalan_a`    SET DEFAULT 'Tidak';
ALTER TABLE `penilaian_awal_keperawatan_igd` ALTER COLUMN `berjalan_b`    SET DEFAULT 'Tidak';
ALTER TABLE `penilaian_awal_keperawatan_igd` ALTER COLUMN `berjalan_c`    SET DEFAULT 'Tidak';
ALTER TABLE `penilaian_awal_keperawatan_igd` ALTER COLUMN `hasil`         SET DEFAULT 'Tidak beresiko (tidak ditemukan a dan b)';
ALTER TABLE `penilaian_awal_keperawatan_igd` ALTER COLUMN `lapor`         SET DEFAULT 'Tidak';
ALTER TABLE `penilaian_awal_keperawatan_igd` ALTER COLUMN `rencana`       SET DEFAULT '';

-- ---------------------------------------------------------------------
-- KELOMPOK W: Tambahan 15 kode baru ke master "I. Diagnosis Keperawatan"
-- sesuai wording formulir fisik RM 03-6 (ADDITIVE - 14 kode lama 001-014
-- TETAP ADA utk data historis, TIDAK dihapus/ditimpa). Kode baru pakai
-- prefix "D" supaya tidak tabrakan dgn kode numerik lama.
-- ---------------------------------------------------------------------
INSERT IGNORE INTO `master_masalah_keperawatan_igd` (`kode_masalah`,`nama_masalah`) VALUES
('D01','Bersihan Jalan Nafas Tidak Efektif'),
('D02','Pola Nafas Tidak Efektif'),
('D03','Perfusi Perifer Tidak Efektif'),
('D04','Hipovolemia'),
('D05','Hipervolemia'),
('D06','Nyeri'),
('D07','Risiko Alergi'),
('D08','Hipertermi'),
('D09','Kerusakan Integritas Kulit'),
('D10','Risiko Syok'),
('D11','Risiko Jatuh'),
('D12','Gangguan Eliminasi Urin'),
('D13','Konstipasi'),
('D14','Defisit Nutrisi'),
('D15','Lainnya');

-- Verifikasi struktur tabel setelah migrasi
DESCRIBE `penilaian_awal_keperawatan_igd`;
DESCRIBE `penilaian_awal_keperawatan_igd_obat`;
