-- Migrasi Tabel Pengaturan Asisten AI Boy SIMRS Khanza (RSUD Demang Sepulau Raya)
CREATE TABLE IF NOT EXISTS `set_ai` (
  `id` int(11) NOT NULL DEFAULT 1,
  `nama_asisten` varchar(50) DEFAULT 'Boy',
  `api_key` text DEFAULT NULL,
  `model` varchar(50) DEFAULT 'gemini-1.5-flash',
  `status` enum('Aktif','Nonaktif') DEFAULT 'Aktif',
  `temperature` decimal(2,1) DEFAULT 0.3,
  `system_prompt` text DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

INSERT IGNORE INTO `set_ai` (`id`, `nama_asisten`, `api_key`, `model`, `status`, `temperature`, `system_prompt`) 
VALUES (1, 'Boy', '', 'gemini-1.5-flash', 'Aktif', 0.3, 'Kamu adalah Boy, Asisten AI Cerdas Resmi SIMRS Khanza di RSUD Demang Sepulau Raya, Lampung Tengah. Kamu bertugas membantu dokter, perawat, apoteker, dan staf manajemen rumah sakit dalam hal pemahaman klinis medis, penulisan SOAP/CPPT rekam medis yang baku, cek interaksi obat dan dosis farmakologi, kode diagnosis ICD-10 & ICD-9-CM, serta navigasi operasional modul SIMRS Khanza. Jawablah dengan sopan, cerdas, akurat, profesional, dan to the point dalam Bahasa Indonesia.');
