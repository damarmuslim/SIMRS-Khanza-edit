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
-- Database: `khanzaaptonline`
--

-- --------------------------------------------------------

--
-- Table structure for table `simpan_antrian_farmasi_wira`
--

CREATE TABLE `simpan_antrian_farmasi_wira` (
  `id` int(11) NOT NULL,
  `no_resep` varchar(20) NOT NULL,
  `no_rawat` varchar(20) NOT NULL DEFAULT '',
  `no_rkm_medis` varchar(20) NOT NULL DEFAULT '',
  `no_antrian` varchar(20) NOT NULL,
  `nm_pasien` varchar(100) NOT NULL,
  `nm_poli` varchar(100) NOT NULL DEFAULT '',
  `nm_dokter` varchar(100) NOT NULL DEFAULT '',
  `jenis_resep` varchar(20) NOT NULL DEFAULT '',
  `tgl_panggil` date NOT NULL,
  `jml_panggil` tinyint(4) NOT NULL DEFAULT 1,
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Dumping data for table `simpan_antrian_farmasi_wira`
--

INSERT INTO `simpan_antrian_farmasi_wira` (`id`, `no_resep`, `no_rawat`, `no_rkm_medis`, `no_antrian`, `nm_pasien`, `nm_poli`, `nm_dokter`, `jenis_resep`, `tgl_panggil`, `jml_panggil`, `updated_at`) VALUES
(1, '202602170001', '2026/02/17/000001', '000056', 'F0001', 'M Wira Satria Buana', 'Poliklinik Bedah', 'dr. Faizal Hakim, SpB', 'Non Racikan', '2026-02-17', 5, '2026-02-17 06:58:39');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `simpan_antrian_farmasi_wira`
--
ALTER TABLE `simpan_antrian_farmasi_wira`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uk_no_resep_tgl` (`no_resep`,`tgl_panggil`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `simpan_antrian_farmasi_wira`
--
ALTER TABLE `simpan_antrian_farmasi_wira`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
