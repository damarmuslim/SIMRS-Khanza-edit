-- phpMyAdmin SQL Dump
-- version 5.1.1
-- https://www.phpmyadmin.net/
--
-- Host: localhost
-- Generation Time: Feb 12, 2026 at 03:25 PM
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
-- Table structure for table `loket_admisi_wira`
--

CREATE TABLE `loket_admisi_wira` (
  `id` int(11) NOT NULL,
  `nama_loket` varchar(100) NOT NULL,
  `keterangan` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `loket_admisi_wira`
--

INSERT INTO `loket_admisi_wira` (`id`, `nama_loket`, `keterangan`) VALUES
(1, 'Loket 1', 'Pendaftaran Rawat Jalan'),
(2, 'Loket 2', 'Pendaftaran Rawat Jalan'),
(3, 'Loket 3', 'Pendaftaran Rawat Jalan'),
(4, 'Loket 4', 'Pendaftaran Rawat Jalan'),
(5, 'Loket 5', 'Pendaftaran Rawat Jalan'),
(6, 'Loket 6', 'Pasien Asuransi Swasta');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `loket_admisi_wira`
--
ALTER TABLE `loket_admisi_wira`
  ADD PRIMARY KEY (`id`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `loket_admisi_wira`
--
ALTER TABLE `loket_admisi_wira`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
