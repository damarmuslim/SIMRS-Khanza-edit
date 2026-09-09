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
-- Table structure for table `antrian_wira`
--

CREATE TABLE `antrian_wira` (
  `id` int(11) NOT NULL,
  `jenis` varchar(50) NOT NULL,
  `nomor` varchar(10) NOT NULL,
  `no_rkm_medis` varchar(20) DEFAULT NULL,
  `loket_id` int(11) DEFAULT NULL,
  `status` varchar(50) DEFAULT 'Menunggu',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `waktu_panggil` timestamp NULL DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Dumping data for table `antrian_wira`
--

INSERT INTO `antrian_wira` (`id`, `jenis`, `nomor`, `no_rkm_medis`, `loket_id`, `status`, `created_at`, `waktu_panggil`) VALUES
(1, 'ADMISI', 'A1', '123', 1, 'Selesai', '2026-02-13 14:18:51', '2026-02-13 14:19:31'),
(2, 'ADMISI', 'A2', '121212', 4, 'Selesai', '2026-02-13 14:18:55', '2026-02-13 14:19:52'),
(3, 'ADMISI', 'A3', NULL, NULL, 'Menunggu', '2026-02-13 14:18:59', NULL),
(4, 'ADMISI', 'A001', '12233', 1, 'Selesai', '2026-02-16 14:49:49', '2026-02-16 14:50:25'),
(5, 'ADMISI', 'A002', '898990', 5, 'Selesai', '2026-02-16 14:49:54', '2026-02-16 14:50:46'),
(6, 'ADMISI', 'A003', '', 1, 'Dipanggil', '2026-02-16 14:49:59', '2026-02-16 14:51:21'),
(7, 'ADMISI', 'A004', NULL, NULL, 'Menunggu', '2026-02-16 15:26:51', NULL),
(8, 'ADMISI', 'A001', NULL, NULL, 'Menunggu', '2026-02-17 04:59:30', NULL),
(9, 'ADMISI', 'A002', NULL, NULL, 'Menunggu', '2026-02-17 04:59:34', NULL),
(10, 'ADMISI', 'A003', NULL, NULL, 'Menunggu', '2026-02-17 04:59:39', NULL);

--
-- Indexes for dumped tables
--

--
-- Indexes for table `antrian_wira`
--
ALTER TABLE `antrian_wira`
  ADD PRIMARY KEY (`id`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `antrian_wira`
--
ALTER TABLE `antrian_wira`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
