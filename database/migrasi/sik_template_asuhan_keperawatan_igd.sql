-- Migrasi tabel Master Template Asuhan Keperawatan IGD
-- Modul: Penilaian Awal Keperawatan IGD (RM 03-4/05/06)
CREATE TABLE IF NOT EXISTS `template_asuhan_keperawatan_igd` (
  `no_template` INT AUTO_INCREMENT PRIMARY KEY,
  `nip` VARCHAR(20) NOT NULL DEFAULT '-',
  `nama_template` VARCHAR(100) NOT NULL,
  `keluhan_utama` TEXT NOT NULL,
  `rpd` TEXT NULL,
  `rpo` TEXT NULL,
  `rencana_keperawatan` TEXT NULL,
  INDEX(`nip`)
) ENGINE=InnoDB;

-- Template Standar IGD (Umum)
INSERT INTO `template_asuhan_keperawatan_igd` (`nip`, `nama_template`, `keluhan_utama`, `rpd`, `rpo`, `rencana_keperawatan`) VALUES
('-', 'Sesak Napas Akut / Asma', 'Pasien datang dengan keluhan sesak napas berat sejak ... jam SMRS. Pasien tampak bernapas cepat dan dangkal, menggunakan otot bantu pernapasan. Batuk (+), mengi (+).', 'Riwayat asma / alergi dingin (+)', 'Salbutamol inhaler / nebulizer', 'Posisikan semi-fowler/fowler. Berikan terapi oksigen sesuai instruksi. Observasi tanda vital dan SpO2 berkala. Kolaborasi pemberian bronkodilator/nebulizer.'),
('-', 'Nyeri Dada Khas (Suspek ACS)', 'Pasien mengeluh nyeri dada substernal menjalar ke lengan kiri dan leher sejak ... jam SMRS. Nyeri terasa seperti tertimpa beban berat, disertai keringat dingin dan mual.', 'Riwayat hipertensi / dislipidemia (+)', 'Obat antihipertensi / aspirin', 'Tirah baring total. Pasang monitor EKG dan saturasi O2. Berikan oksigenasi. Kolaborasi perekaman EKG 12 lead segera dan pemberian terapi nitrat/antiplatelet.'),
('-', 'Kecelakaan Lalu Lintas (Trauma)', 'Pasien datang rujukan/diantar keluarga post KLL ... jam SMRS. Mekanisme kejadian: ..., helm (+/-). Terdapat luka lecet dan nyeri gerak pada ...', 'Tidak ada riwayat penyakit kronis sebelumnya', 'Tidak ada riwayat konsumsi obat rutin', 'Lakukan imobilisasi servikal dan ekstremitas cedera. Rawat luka aseptik dan pasang balut tekan bila ada perdarahan aktif. Pantau GCS dan tanda vital. Kolaborasi pemeriksaan radiologi.'),
('-', 'Demam Tinggi / Kejang Demam Anak', 'Pasien anak datang dengan demam tinggi sejak ... hari SMRS. Suhu naik mendadak disertai kejang tonik-klonik selama ... menit, setelah kejang pasien menangis/sadar.', 'Riwayat kejang demam sebelumnya (+/-)', 'Parasetamol sirup', 'Baringkan miring aman, bebaskan jalan napas. Kompres hangat pada aksila dan lipat paha. Pasang akses IV bila dehidrasi/kejang berulang. Kolaborasi antipiretik/antikonvulsan.'),
('-', 'Gastroenteritis Akut / Dehidrasi', 'Pasien mengeluh buang air besar cair frekuensi ... kali sejak ... hari SMRS, konsistensi cair tanpa/dengan lendir/darah. Disertai mual dan muntah ... kali, nafsu makan/minum menurun, badan terasa lemas.', 'Riwayat maag / dispepsia (+/-)', 'Antasida / obat warung', 'Kaji derajat dehidrasi (turgor kulit, mukosa mulut, mata cekung). Berikan rehidrasi oralit/cairan IV sesuai protokol. Monitor balance cairan dan frekuensi BAB/muntah.'),
('-', 'Penurunan Kesadaran / Suspek Stroke', 'Pasien dibawa keluarga karena penurunan kesadaran mendadak sejak ... jam SMRS. Sebelumnya mengeluh pusing dan kelemahan anggota gerak sisi ... Bicara pelo (+), mulut mencong (+).', 'Riwayat hipertensi tidak terkontrol / DM (+)', 'Amlodipine / Metformin tidak rutin', 'Pertahankan jalan napas paten, posisikan kepala elevasi 30 derajat. Pasang NGT bila disfagia. Pasang kateter urin untuk pemantauan haluaran. Kolaborasi CT scan kepala segera.');
