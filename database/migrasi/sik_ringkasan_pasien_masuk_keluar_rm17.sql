-- =====================================================================
-- RM 17-01  RINGKASAN PASIEN MASUK KELUAR (RM PASIEN RAWAT INAP)
-- RSUD Demang Sepulau Raya
--
-- Migrasi ADDITIVE: hanya membuat SATU tabel baru, tidak mengubah atau
-- menghapus apa pun yang sudah ada.
--
-- PRINSIP ISI TABEL INI
-- Tabel ini SENGAJA tidak menyimpan ulang identitas & data registrasi
-- pasien (nama, alamat, tanggal lahir, agama, pekerjaan, pendidikan,
-- tanggal/jam masuk, ruang, tanggal/jam keluar). Semua itu diambil lewat
-- JOIN saat form dibuka dan saat mencetak, supaya lembar RM 17-01 selalu
-- mengikuti master dan tidak bisa melenceng dari data pendaftaran.
--
-- Yang disimpan di sini hanya dua macam:
--   1. Data yang TIDAK ADA sumbernya di mana pun (nama suami/istri/ayah,
--      masuk melalui, dikirim oleh, kasus polisi, komplikasi, ke klinik,
--      dan seluruh halaman Sebab Kematian).
--   2. Data yang sumbernya ada tapi kosakatanya BERBEDA dari kertas
--      sehingga butuh keputusan manusia - terutama Keadaan Keluar: kertas
--      punya 5 pilihan (Sembuh/Berobat Jalan/Pulang Paksa/Pindah RS/
--      Rujuk) sedangkan sistem punya tiga sumber dengan kosakata berbeda
--      (resume_pasien_ranap.keadaan 4 nilai, resume_pasien_ranap.cara_keluar
--      4 nilai, kamar_inap.stts_pulang 15 nilai) dan tidak satu pun cocok
--      persis. Sistem hanya MENYARANKAN, petugas yang menentukan.
--
-- Baris "ALAMAT / JALAN / RT-RW / NO. RUMAH" di kertas TIDAK dibuatkan
-- kolom (keputusan user): form pendaftaran memang tidak pernah memisahkan
-- RT/RW dan nomor rumah, alamat ditulis sebagai satu teks bebas yang di
-- praktiknya sudah memuat RT/RW. Alamat dicetak apa adanya dari registrasi.
-- =====================================================================

CREATE TABLE IF NOT EXISTS `ringkasan_pasien_masuk_keluar` (
  `no_rawat` VARCHAR(17) NOT NULL,

  -- ---------- Identitas keluarga (kertas: NAMA LENGKAP SUAMI/ISTRI/AYAH/IBU)
  -- Nama IBU tidak diikutkan: sudah ada di `pasien`.`nm_ibu`, diambil lewat JOIN.
  `nama_suami`               VARCHAR(60)  DEFAULT NULL,
  `nama_istri`               VARCHAR(60)  DEFAULT NULL,
  `nama_ayah`                VARCHAR(60)  DEFAULT NULL,

  -- ---------- Kertas: MASUK MELALUI / DIKIRM OLEH / KASUS POLOSI
  -- `masuk_melalui` disarankan otomatis dari reg_periksa.kd_poli ('IGDK' = IGD).
  -- `dikirim_oleh` TIDAK bisa disarankan otomatis: rujuk_masuk.kategori_rujuk
  -- ternyata kategori medis (Bedah/Non-Bedah/Kebidanan/Anak), bukan jenis
  -- pengirim, dan rujuk_masuk.perujuk hanya nama bebas.
  `masuk_melalui`            ENUM('IGD','Poliklinik') DEFAULT NULL,
  `dikirim_oleh`             ENUM('RS Lain','Puskesmas','Para Medis','Polri','Dokter/Dokter Gizi','Kemauan Sendiri') DEFAULT NULL,
  `kasus_polisi`             ENUM('Ya','Tidak') DEFAULT NULL,

  -- ---------- Kertas: DIAGNOSE SEMENTARA / ALERGI TERHADAP
  `diagnose_sementara`       VARCHAR(200) DEFAULT NULL,
  `alergi_terhadap`          VARCHAR(200) DEFAULT NULL,

  -- ---------- Kertas: DIAGNOSE AKHIR | KOMPLIKASI | DIAGNOSE LAIN | KODE ICD
  -- Hanya `komplikasi` yang benar-benar tidak ada sumbernya; tiga lainnya
  -- disarankan dari resume_pasien_ranap tapi tetap boleh dikoreksi petugas.
  `diagnose_akhir`           VARCHAR(200) DEFAULT NULL,
  `komplikasi`               VARCHAR(200) DEFAULT NULL,
  `diagnose_lain`            VARCHAR(200) DEFAULT NULL,
  `kode_icd`                 VARCHAR(50)  DEFAULT NULL,

  -- ---------- Kertas: TINDAKAN (4 baris tetap) + TANGGAL + DOKTER
  -- Label barisnya sudah dicetak di kertas (1 Operasi ke 1, 2 Operasi ke 2,
  -- 3 Operasi Khusus, 4 Tindakan Lain) sehingga tidak perlu disimpan.
  -- Kolom dokter menyimpan KODE dokter (dipilih lewat DlgCariDokter), bukan
  -- nama bebas - namanya di-JOIN saat cetak.
  `tindakan_1`               VARCHAR(150) DEFAULT NULL,
  `tindakan_1_tanggal`       DATE         DEFAULT NULL,
  `tindakan_1_dokter`        VARCHAR(20)  DEFAULT NULL,
  `tindakan_2`               VARCHAR(150) DEFAULT NULL,
  `tindakan_2_tanggal`       DATE         DEFAULT NULL,
  `tindakan_2_dokter`        VARCHAR(20)  DEFAULT NULL,
  `tindakan_3`               VARCHAR(150) DEFAULT NULL,
  `tindakan_3_tanggal`       DATE         DEFAULT NULL,
  `tindakan_3_dokter`        VARCHAR(20)  DEFAULT NULL,
  `tindakan_4`               VARCHAR(150) DEFAULT NULL,
  `tindakan_4_tanggal`       DATE         DEFAULT NULL,
  `tindakan_4_dokter`        VARCHAR(20)  DEFAULT NULL,

  -- ---------- Kertas: blok MENINGGAL (TGL/BLN/TH/JAM)
  -- Terpisah dari tanggal/jam KELUAR di kamar_inap: jam kematian belum tentu
  -- sama dengan jam pasien dikeluarkan secara administratif.
  `meninggal_tanggal`        DATE         DEFAULT NULL,
  `meninggal_jam`            TIME         DEFAULT NULL,

  -- ---------- Kertas: KEADAAN KELUAR / CARA KELUAR
  `keadaan_keluar`           ENUM('Sembuh','Berobat Jalan','Pulang Paksa','Pindah RS','Rujuk') DEFAULT NULL,
  `rujuk_ke`                 VARCHAR(100) DEFAULT NULL,
  `cara_keluar_meninggal`    ENUM('Sebelum 48 Jam','Sesudah 48 Jam') DEFAULT NULL,

  -- ---------- Kertas: KUNJUNGAN ULANG TANGGAL / KE KLINIK
  `kunjungan_ulang_tanggal`  DATE         DEFAULT NULL,
  `ke_klinik`                VARCHAR(60)  DEFAULT NULL,

  -- ---------- Kertas: TANDA TANGAN DOKTER YANG MERAWAT + NIP
  -- Tanda tangan dipasang sebagai QR (pola RM 03-x / RM 20).
  -- NIP diambil dari pegawai.nik dan DICETAK APA ADANYA - keputusan user:
  -- di produksi isi kolom itu beragam (sebagian NIP untuk dokter PNS,
  -- sebagian kode pegawai), dan untuk sementara ditampilkan apa adanya
  -- sampai user memastikan aturannya. Tidak disimpan di sini karena itu
  -- properti dokter, bukan properti lembar ini.
  `kd_dokter_merawat`        VARCHAR(20)  DEFAULT NULL,

  -- =================================================================
  -- HALAMAN 2 : SEBAB KEMATIAN
  -- Seluruh bagian ini tidak punya padanan APA PUN di skema lama.
  -- Hanya diisi bila pasien meninggal; di form, section ini muncul
  -- mengikuti status pulang.
  -- =================================================================

  -- Rantai sebab a -> b -> c, masing-masing dengan "Lamanya
  -- (kira-kira, mulai sakit hingga meninggal dunia)".
  `sk_penyakit_a`            VARCHAR(200) DEFAULT NULL,
  `sk_lama_a`                VARCHAR(60)  DEFAULT NULL,
  `sk_penyakit_b`            VARCHAR(200) DEFAULT NULL,
  `sk_lama_b`                VARCHAR(60)  DEFAULT NULL,
  `sk_penyakit_c`            VARCHAR(200) DEFAULT NULL,
  `sk_lama_c`                VARCHAR(60)  DEFAULT NULL,

  -- "Penyakit-penyakit yang berarti dan mempengaruhi pula kematian itu,
  --  tetapi tidak ada hubungan penyakit tersebut dalam 1.a.b.c"
  `sk_penyakit_lain`         VARCHAR(200) DEFAULT NULL,
  `sk_lama_lain`             VARCHAR(60)  DEFAULT NULL,

  -- I. MATI KARENA RUDA PAKSA (Violent death)
  `rp_macam`                 VARCHAR(150) DEFAULT NULL,
  `rp_cara_kejadian`         VARCHAR(150) DEFAULT NULL,
  `rp_sifat_jenis`           VARCHAR(150) DEFAULT NULL,

  -- II. KELAHIRAN MATI (Still birth)
  `km_janin_lahir_mati`      ENUM('Ya','Tidak') DEFAULT NULL,
  `km_sebab`                 VARCHAR(150) DEFAULT NULL,

  -- III. PERSALINAN, KEHAMILAN
  `pk_persalinan`            ENUM('Ya','Tidak') DEFAULT NULL,
  `pk_kehamilan`             ENUM('Ya','Tidak') DEFAULT NULL,

  -- IV. OPERASI
  `op_dilakukan`             ENUM('Ya','Tidak') DEFAULT NULL,
  `op_jam`                   VARCHAR(30)  DEFAULT NULL,

  -- Penutup: "Gunung Sugih, ....... 20 ....." + Pemberi Keterangan sebab kematian
  `sk_tanggal`               DATE         DEFAULT NULL,
  `kd_pemberi_keterangan`    VARCHAR(20)  DEFAULT NULL,

  PRIMARY KEY (`no_rawat`),
  KEY `idx_rmk_dokter_merawat` (`kd_dokter_merawat`),
  KEY `idx_rmk_pemberi_ket` (`kd_pemberi_keterangan`),
  CONSTRAINT `fk_rmk_no_rawat` FOREIGN KEY (`no_rawat`)
      REFERENCES `reg_periksa` (`no_rawat`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
