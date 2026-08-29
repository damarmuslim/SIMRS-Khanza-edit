-- =====================================================================
-- RM 02-2 / RM 02-3  ASESMEN AWAL KEPERAWATAN DI INSTALASI RAWAT JALAN
-- RSUD Demang Sepulau Raya
--
-- Migrasi ADDITIVE: hanya membuat SATU tabel baru.
--
-- KENAPA TABEL BARU
-- `penilaian_awal_keperawatan_ralan` (57 kolom) sudah ada dan sebagian isinya
-- cocok (TTV, keluhan_utama, rpd, rpk, alergi, nyeri, risiko jatuh, skrining
-- gizi). TAPI ia melayani form "Pengkajian Awal Keperawatan Rawat Jalan"
-- bawaan Khanza yang bentuknya lain (I. Keadaan Umum s.d. V. Riwayat
-- Psiko-Sosial) beserta 13 tabel turunan per kelompok pasien. Ia juga TIDAK
-- punya blok terbesar kertas ini: pemeriksaan fisik 20 regio, pola kebiasaan,
-- pertumbuhan balita, dan kesehatan reproduksi.
-- Menambah seratus kolom ke sana akan mengganggu modul yang sudah jalan.
--
-- SATU KOLOM PER REGIO, BUKAN PER KOTAK CENTANG - keputusan setelah dihitung
-- Kertas punya ~130 kotak centang hanya untuk Pemeriksaan Fisik. Kalau tiap
-- kotak dibuatkan kolom ENUM sendiri, tabelnya jadi ~235 kolom dan mendekati
-- batas ukuran baris InnoDB - masalah yang sudah pernah menabrak tabel
-- `penilaian_awal` RM 19 pada 442 kolom.
--
-- Karena itu tiap REGIO disimpan sebagai SATU teks berisi temuan yang
-- dicentang, dipisah koma:
--     mata = 'Simetris dan lengkap, Konjungtiva anemis, Ikterik'
-- Kotak centangnya tetap ada di FORM; penggabungan terjadi saat menyimpan.
-- Konsekuensi yang disadari: tidak bisa dihitung per temuan dengan rapi,
-- hanya bisa dicari dengan LIKE. Temuan pemeriksaan fisik memang dibaca
-- sebagai narasi dan hampir tidak pernah diagregasi.
--
-- TEKS PANJANG WAJIB TEXT, BUKAN VARCHAR - ketahuan saat import PERTAMA GAGAL
-- Versi pertama migrasi ini memakai VARCHAR(150..400) untuk tiap regio dan
-- DITOLAK MariaDB:
--     ERROR 1118 (42000): Row size too large (> 8126)
-- Jumlah kolomnya bukan penyebabnya - PANJANG VARCHAR-nya yang dijumlahkan.
-- Dua puluh regio yang masing-masing bisa menampung dua belas temuan sudah
-- menghabiskan lebih dari 5 KB sendirian, dan seluruh baris InnoDB dibatasi
-- 8126 byte apa pun jumlah kolomnya.
--
-- Karena itu SEMUA kolom yang isinya teks bebas panjang (>= 150 karakter pada
-- rancangan awal) dijadikan TEXT: TEXT disimpan DI LUAR baris dan hanya
-- menitipkan penunjuk 20 byte. Kolom pendek yang isinya memang terbatas
-- (angka, satuan, tanggal, kode) tetap VARCHAR supaya tidak boros.
--
-- ROW_FORMAT=DYNAMIC ditulis EKSPLISIT dan itu penting: pada ROW_FORMAT
-- COMPACT, tiap TEXT masih menitipkan 768 byte pertama di dalam baris, jadi
-- 50 kolom TEXT akan menabrak batas yang sama persis lagi. DYNAMIC hanya
-- menitipkan penunjuknya. Jangan menghapus klausa ini.
--
-- Obat yang sama sudah pernah dipakai di proyek ini: `penilaian_awal_
-- keperawatan_ranap` (RM 19) mentok di 442 kolom, 72 VARCHAR panjangnya
-- diubah jadi TEXT, dan tabelnya kini 515 kolom. Jadi pola ini bukan
-- percobaan - sudah terbukti sekali di tabel yang jauh lebih besar.
--
-- IDENTITAS TIDAK DISIMPAN
-- Nama, L/P, tanggal lahir, status nikah, agama, pendidikan, cara bayar,
-- alamat, dan telepon semuanya sudah ada di `pasien`/`reg_periksa` dan
-- diambil lewat JOIN.
--
-- DUA BLOK BERSYARAT - keputusan user
-- Blok BALITA hanya ditampilkan kalau umur pasien di bawah 5 tahun, dan blok
-- KESEHATAN REPRODUKSI hanya untuk pasien perempuan (prinsip yang sama,
-- diterapkan konsisten). Kolomnya tetap ada dan tetap tersimpan kalau diisi -
-- yang bersyarat hanya tampilannya, mengikuti pola Sebab Kematian di RM 17-01.
--
-- TIDAK ADA MIGRASI HAK AKSES - mengikuti RM 21/33/01/02-1.
-- =====================================================================

CREATE TABLE IF NOT EXISTS `asesmen_awal_keperawatan_ralan` (
  `no_rawat` VARCHAR(17) NOT NULL,
  `tanggal`  DATE DEFAULT NULL,
  `jam`      TIME DEFAULT NULL,
  `diagnosa_medis` TEXT DEFAULT NULL,

  -- ============ 2. RIWAYAT KESEHATAN ============
  `keluhan_utama`     TEXT DEFAULT NULL,
  `riwayat_kesehatan` TEXT DEFAULT NULL,

  -- ============ 3. PEMERIKSAAN FISIK ============
  `keadaan_umum` ENUM('Baik','Lemah','Buruk') DEFAULT NULL,
  `td_sistolik`  VARCHAR(5) DEFAULT NULL,
  `td_diastolik` VARCHAR(5) DEFAULT NULL,
  `vas`          VARCHAR(3) DEFAULT NULL,   -- Visual Analog Scale 0-10
  `hr`           VARCHAR(5) DEFAULT NULL,
  `rr`           VARCHAR(5) DEFAULT NULL,
  `suhu`         VARCHAR(5) DEFAULT NULL,

  -- Tiap regio = satu teks berisi temuan yang dicentang, dipisah koma.
  -- Semuanya TEXT, lihat catatan ukuran baris di atas.
  `kepala`      TEXT DEFAULT NULL,
  `rambut`      TEXT DEFAULT NULL,
  `mata`        TEXT DEFAULT NULL,
  `hidung`      TEXT DEFAULT NULL,
  `telinga`     TEXT DEFAULT NULL,
  `mulut`       TEXT DEFAULT NULL,
  `gigi`        TEXT DEFAULT NULL,
  `lidah`       TEXT DEFAULT NULL,
  `tenggorokan` TEXT DEFAULT NULL,
  `leher`       TEXT DEFAULT NULL,
  `integumen`   TEXT DEFAULT NULL,
  `payudara_ukuran_bentuk` TEXT DEFAULT NULL,
  `payudara_warna`         TEXT DEFAULT NULL,
  `payudara_kelainan`      TEXT DEFAULT NULL,
  `thorax_paru` TEXT DEFAULT NULL,
  `jantung`     TEXT DEFAULT NULL,
  `abdomen`     TEXT DEFAULT NULL,
  `genitalia`   TEXT DEFAULT NULL,
  `anus`        TEXT DEFAULT NULL,
  `muskuloskeletal` TEXT DEFAULT NULL,
  `crt` ENUM('<2 detik','>2 detik') DEFAULT NULL,
  -- Kekuatan otot: ekstremitas superior/inferior, dextra/sinistra.
  `otot_esd` VARCHAR(5) DEFAULT NULL,
  `otot_ess` VARCHAR(5) DEFAULT NULL,
  `otot_eid` VARCHAR(5) DEFAULT NULL,
  `otot_eis` VARCHAR(5) DEFAULT NULL,

  -- ============ 4. POLA KEBIASAAN PASIEN ============
  `nutrisi`   TEXT DEFAULT NULL,
  `eliminasi` TEXT DEFAULT NULL,
  `istirahat` TEXT DEFAULT NULL,
  `aktivitas` TEXT DEFAULT NULL,

  -- ============ 5. PERTUMBUHAN DAN PERKEMBANGAN (BALITA) ============
  -- Blok BERSYARAT: hanya ditampilkan untuk pasien di bawah 5 tahun.
  `balita_usia_kehamilan` VARCHAR(30) DEFAULT NULL,
  `balita_lahir_di`       VARCHAR(100) DEFAULT NULL,
  `balita_ditolong`       VARCHAR(100) DEFAULT NULL,
  `balita_anak_ke`        VARCHAR(10) DEFAULT NULL,
  `balita_bb_lahir`       VARCHAR(10) DEFAULT NULL,
  `balita_tb_lahir`       VARCHAR(10) DEFAULT NULL,
  `balita_bbtb_sekarang`  VARCHAR(50) DEFAULT NULL,
  `balita_kelainan`       TEXT DEFAULT NULL,
  `balita_mendapat`       TEXT DEFAULT NULL,
  `balita_imunisasi_dasar` TEXT DEFAULT NULL,
  `balita_imunisasi_ulang` TEXT DEFAULT NULL,
  `balita_membalikkan` VARCHAR(20) DEFAULT NULL,
  `balita_duduk`       VARCHAR(20) DEFAULT NULL,
  `balita_berdiri`     VARCHAR(20) DEFAULT NULL,
  `balita_berjalan`    VARCHAR(20) DEFAULT NULL,
  `balita_mengoceh`    VARCHAR(20) DEFAULT NULL,
  `balita_berbicara`   VARCHAR(20) DEFAULT NULL,

  -- ============ 6. KESEHATAN REPRODUKSI (WANITA / IBU HAMIL) ============
  -- Blok BERSYARAT: hanya ditampilkan untuk pasien perempuan.
  `repro_usia_menstruasi` VARCHAR(10) DEFAULT NULL,
  `repro_siklus`   TEXT DEFAULT NULL,
  `repro_interval` VARCHAR(20) DEFAULT NULL,
  `repro_jumlah`   VARCHAR(20) DEFAULT NULL,
  `repro_nyeri_skala` VARCHAR(10) DEFAULT NULL,
  `repro_bercak`   TEXT DEFAULT NULL,
  `repro_hpht`     VARCHAR(20) DEFAULT NULL,
  `repro_keluhan_hamil` TEXT DEFAULT NULL,
  `repro_obat`     TEXT DEFAULT NULL,
  `obs_g` VARCHAR(5) DEFAULT NULL,
  `obs_p` VARCHAR(5) DEFAULT NULL,
  `obs_o` VARCHAR(5) DEFAULT NULL,
  `obs_jumlah_anak`   VARCHAR(5) DEFAULT NULL,
  `obs_laki`          VARCHAR(5) DEFAULT NULL,
  `obs_perempuan`     VARCHAR(5) DEFAULT NULL,
  `obs_anak_terkecil` VARCHAR(30) DEFAULT NULL,
  `repro_kb`     TEXT DEFAULT NULL,
  `repro_pernah` TEXT DEFAULT NULL,
  `palpasi_tfu`        VARCHAR(30) DEFAULT NULL,
  `palpasi_auskultasi` VARCHAR(50) DEFAULT NULL,
  `palpasi_presentasi` TEXT DEFAULT NULL,
  `palpasi_masuk_pap`  TEXT DEFAULT NULL,
  `janin_pergerakan`     TEXT DEFAULT NULL,
  `janin_posisi`         TEXT DEFAULT NULL,
  `janin_umur_kehamilan` VARCHAR(30) DEFAULT NULL,
  `janin_masalah`        TEXT DEFAULT NULL,
  `repro_informasi_lain` TEXT DEFAULT NULL,
  `repro_dirujuk`        TEXT DEFAULT NULL,

  -- ============ 7. DATA PSIKOLOGIS SOSIOLOGIS DAN SPIRITUAL ============
  `psikologi`  TEXT DEFAULT NULL,
  `cemas`      TEXT DEFAULT NULL,
  `sosiologi`  TEXT DEFAULT NULL,
  `spiritual`  TEXT DEFAULT NULL,
  `kebutuhan_informasi` TEXT DEFAULT NULL,

  -- ============ 8. DATA PEMERIKSAAN DIAGNOSTIK ============
  `diag_laboratorium` TEXT DEFAULT NULL,
  `diag_rontgen`      TEXT DEFAULT NULL,
  `diag_ekg`          TEXT DEFAULT NULL,
  `diag_usg`          TEXT DEFAULT NULL,
  `diag_scanning`     TEXT DEFAULT NULL,
  `diag_terapi_medik` TEXT DEFAULT NULL,

  -- ============ 9. KONTROL YANG AKAN DATANG ============
  `kontrol_rawat_inap` TEXT DEFAULT NULL,
  `kontrol_tanggal`    DATE DEFAULT NULL,

  -- ============ 10. DIAGNOSA KEPERAWATAN ============
  -- Lima diagnosa tercetak tetap di kertas, jadi tiap-tiap dibuatkan penanda
  -- sendiri - berbeda dari blok pemeriksaan fisik yang temuannya banyak dan
  -- bebas. Lima kolom ENUM masih jauh dari membebani baris.
  `dx_oksigen`   ENUM('Ya','Tidak') DEFAULT NULL,
  `dx_nutrisi`   ENUM('Ya','Tidak') DEFAULT NULL,
  `dx_eliminasi` ENUM('Ya','Tidak') DEFAULT NULL,
  `dx_integritas_kulit` ENUM('Ya','Tidak') DEFAULT NULL,
  `dx_kurang_pengetahuan` ENUM('Ya','Tidak') DEFAULT NULL,

  -- Perawat yang mengkaji; tanda tangannya jadi QR pada cetakan.
  `nip` VARCHAR(20) DEFAULT NULL,

  PRIMARY KEY (`no_rawat`),
  KEY `idx_aakr_nip` (`nip`),
  CONSTRAINT `fk_aakr_no_rawat` FOREIGN KEY (`no_rawat`)
      REFERENCES `reg_periksa` (`no_rawat`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=latin1;
