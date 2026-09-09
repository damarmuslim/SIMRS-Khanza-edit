-- phpMyAdmin SQL Dump
-- version 5.1.1
-- https://www.phpmyadmin.net/
--
-- Host: localhost
-- Generation Time: Feb 17, 2026 at 08:14 AM
-- Server version: 10.4.20-MariaDB
-- PHP Version: 8.0.8

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `anjungan`
--

-- --------------------------------------------------------

--
-- Table structure for table `feature_control`
--

CREATE TABLE `feature_control` (
  `id` int(11) NOT NULL,
  `kode_fitur` varchar(50) NOT NULL,
  `nama_fitur` varchar(100) NOT NULL,
  `status` tinyint(1) NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Dumping data for table `feature_control`
--

INSERT INTO `feature_control` (`id`, `kode_fitur`, `nama_fitur`, `status`) VALUES
(1, 'admisi', 'Antrian Admisi', 1),
(2, 'daftar_poli', 'Daftar Poli', 1),
(3, 'cek_bpjs', 'Cek Kepesertaan BPJS', 1),
(4, 'checkin', 'Check-in JKN', 1),
(5, 'frista', 'Frista', 1),
(6, 'cari_ranap', 'Cari Pasien Ranap', 1),
(7, 'rujukan', 'SEP Rujukan FKTP', 0),
(8, 'sep_poli', 'SEP Poli', 0),
(9, 'kontrol_rajal', 'Surat Kontrol Rajal', 0),
(10, 'kontrol_ranap', 'Surat Kontrol Ranap', 0),
(11, 'antri_farmasi', 'Antri Farmasi', 1);

-- --------------------------------------------------------

--
-- Table structure for table `hak_akses`
--

CREATE TABLE `hak_akses` (
  `id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `menu` varchar(100) NOT NULL,
  `izin` tinyint(1) NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Dumping data for table `hak_akses`
--

INSERT INTO `hak_akses` (`id`, `user_id`, `menu`, `izin`) VALUES
(305, 1, 'anjungan', 1),
(306, 1, 'admisi', 1),
(307, 1, 'poliklinik', 1),
(308, 1, 'farmasi', 1),
(309, 1, 'casemix', 1),
(310, 1, 'bridging', 1),
(311, 1, 'pengguna', 1),
(312, 1, 'setting', 1),
(313, 4, 'anjungan', 1),
(314, 4, 'admisi', 1),
(315, 4, 'poliklinik', 1),
(316, 4, 'farmasi', 1),
(317, 4, 'casemix', 1),
(318, 4, 'bridging', 1),
(319, 4, 'pengguna', 1),
(320, 4, 'setting', 1);

-- --------------------------------------------------------

--
-- Table structure for table `menu_list`
--

CREATE TABLE `menu_list` (
  `id` int(11) NOT NULL,
  `kode` varchar(50) NOT NULL,
  `nama_menu` varchar(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Dumping data for table `menu_list`
--

INSERT INTO `menu_list` (`id`, `kode`, `nama_menu`) VALUES
(1, 'anjungan', 'Anjungan Pasien'),
(2, 'admisi', 'Admisi'),
(3, 'poliklinik', 'Poliklinik'),
(4, 'farmasi', 'Farmasi'),
(5, 'casemix', 'Casemix'),
(6, 'bridging', 'Bridging BPJS'),
(7, 'pengguna', 'Pengguna'),
(8, 'setting', 'Setting Sistem');

-- --------------------------------------------------------

--
-- Table structure for table `sep_simulasi`
--

CREATE TABLE `sep_simulasi` (
  `id` int(11) NOT NULL,
  `no_sep` varchar(50) DEFAULT NULL,
  `no_kartu` varchar(20) DEFAULT NULL,
  `no_rujukan` varchar(50) DEFAULT NULL,
  `tgl_sep` date DEFAULT NULL,
  `kode_dpjp` varchar(20) DEFAULT NULL,
  `data_json` text DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- --------------------------------------------------------

--
-- Table structure for table `setting_antrol`
--

CREATE TABLE `setting_antrol` (
  `id` int(11) NOT NULL,
  `cons_id` varchar(100) NOT NULL,
  `secret_key` varchar(255) NOT NULL,
  `user_key` varchar(100) NOT NULL,
  `base_url` varchar(255) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NULL DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- --------------------------------------------------------

--
-- Table structure for table `setting_frista`
--

CREATE TABLE `setting_frista` (
  `id` int(11) NOT NULL,
  `username` varchar(100) NOT NULL,
  `password` varchar(255) NOT NULL,
  `created_at` datetime DEFAULT current_timestamp(),
  `updated_at` datetime DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- --------------------------------------------------------

--
-- Table structure for table `setting_simrs`
--

CREATE TABLE `setting_simrs` (
  `id` int(11) NOT NULL,
  `nama_simrs` varchar(100) NOT NULL,
  `host` varchar(100) NOT NULL,
  `username` varchar(100) NOT NULL,
  `password` varchar(100) NOT NULL,
  `database_name` varchar(100) NOT NULL,
  `updated_at` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Dumping data for table `setting_simrs`
--

INSERT INTO `setting_simrs` (`id`, `nama_simrs`, `host`, `username`, `password`, `database_name`, `updated_at`) VALUES
(2, 'Simrs Khanza', 'localhost', 'root', '', 'khanzaaptonline', '2026-02-16 09:18:27');

-- --------------------------------------------------------

--
-- Table structure for table `setting_vclaim`
--

CREATE TABLE `setting_vclaim` (
  `id` int(11) NOT NULL,
  `cons_id` varchar(50) NOT NULL COMMENT 'Consumer ID dari BPJS',
  `secret_key` varchar(100) NOT NULL COMMENT 'Secret Key dari BPJS',
  `user_key` varchar(100) NOT NULL COMMENT 'User Key dari BPJS',
  `base_url` varchar(200) NOT NULL COMMENT 'Base URL VClaim (contoh: https://apijkn-dev.bpjs-kesehatan.go.id/vclaim-rest-dev)',
  `kd_ppk` varchar(20) NOT NULL COMMENT 'Kode PPK Rumah Sakit',
  `nm_ppk` varchar(200) NOT NULL COMMENT 'Nama PPK Rumah Sakit',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `id` int(11) NOT NULL,
  `nik` varchar(50) NOT NULL,
  `nama` varchar(150) NOT NULL,
  `email` varchar(150) NOT NULL,
  `password` varchar(255) NOT NULL,
  `hp` varchar(30) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NULL DEFAULT NULL ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`id`, `nik`, `nama`, `email`, `password`, `hp`, `created_at`, `updated_at`) VALUES
(1, '16210646', 'M Wira Satria Buana, S. Kom', 'wiramuhammad16@gmail.com', '$2y$10$.6zd37cqiqiRuRNdmnlHzODmuR.OgUMlJmVLixQM1usqIG1fhudMa', '082177846209', '2025-11-03 02:09:39', NULL);

-- --------------------------------------------------------

--
-- Table structure for table `visitor_log`
--

CREATE TABLE `visitor_log` (
  `id` int(11) NOT NULL,
  `nik` varchar(20) NOT NULL,
  `nama` varchar(100) NOT NULL,
  `nama_pasien_tuju` varchar(100) DEFAULT NULL,
  `no_rm_pasien_tuju` varchar(20) DEFAULT NULL,
  `waktu_kunjungan` timestamp NULL DEFAULT NULL,
  `waktu_daftar` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Indexes for dumped tables
--

--
-- Indexes for table `feature_control`
--
ALTER TABLE `feature_control`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `kode_fitur` (`kode_fitur`);

--
-- Indexes for table `hak_akses`
--
ALTER TABLE `hak_akses`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `menu_list`
--
ALTER TABLE `menu_list`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `kode` (`kode`);

--
-- Indexes for table `sep_simulasi`
--
ALTER TABLE `sep_simulasi`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `no_sep` (`no_sep`);

--
-- Indexes for table `setting_antrol`
--
ALTER TABLE `setting_antrol`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `setting_frista`
--
ALTER TABLE `setting_frista`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `setting_simrs`
--
ALTER TABLE `setting_simrs`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `setting_vclaim`
--
ALTER TABLE `setting_vclaim`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `nik` (`nik`),
  ADD UNIQUE KEY `email` (`email`);

--
-- Indexes for table `visitor_log`
--
ALTER TABLE `visitor_log`
  ADD PRIMARY KEY (`id`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `feature_control`
--
ALTER TABLE `feature_control`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=12;

--
-- AUTO_INCREMENT for table `hak_akses`
--
ALTER TABLE `hak_akses`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=321;

--
-- AUTO_INCREMENT for table `menu_list`
--
ALTER TABLE `menu_list`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=9;

--
-- AUTO_INCREMENT for table `sep_simulasi`
--
ALTER TABLE `sep_simulasi`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT for table `setting_antrol`
--
ALTER TABLE `setting_antrol`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT for table `setting_frista`
--
ALTER TABLE `setting_frista`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT for table `setting_simrs`
--
ALTER TABLE `setting_simrs`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT for table `setting_vclaim`
--
ALTER TABLE `setting_vclaim`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT for table `visitor_log`
--
ALTER TABLE `visitor_log`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=10;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
