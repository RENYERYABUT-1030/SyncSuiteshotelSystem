-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Aug 24, 2026 at 03:46 PM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `hotel_system`
--

-- --------------------------------------------------------

--
-- Table structure for table `admins`
--

CREATE TABLE `admins` (
  `adminID` int(11) NOT NULL,
  `username` varchar(50) NOT NULL,
  `password` varchar(50) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `admins`
--

INSERT INTO `admins` (`adminID`, `username`, `password`) VALUES
(1, 'admin', 'admin123');

-- --------------------------------------------------------

--
-- Table structure for table `archived_bookings`
--

CREATE TABLE `archived_bookings` (
  `archive_id` int(11) NOT NULL,
  `original_booking_id` int(11) NOT NULL,
  `customer_id` int(11) NOT NULL,
  `room_id` int(11) NOT NULL,
  `check_in_date` date NOT NULL,
  `check_out_date` date NOT NULL,
  `total_amount` decimal(10,2) NOT NULL,
  `payment_method` varchar(50) DEFAULT NULL,
  `status` varchar(50) DEFAULT NULL,
  `adults` int(11) DEFAULT NULL,
  `seniors` int(11) DEFAULT NULL,
  `kids` int(11) DEFAULT NULL,
  `senior_discount` decimal(10,2) DEFAULT NULL,
  `extra_guest_charge` decimal(10,2) DEFAULT NULL,
  `reschedule_charge` decimal(10,2) DEFAULT NULL,
  `archived_by` varchar(100) DEFAULT NULL,
  `archive_reason` varchar(255) DEFAULT NULL,
  `archived_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `audit_log`
--

CREATE TABLE `audit_log` (
  `audit_id` int(11) NOT NULL,
  `user_type` enum('admin','customer') NOT NULL,
  `user_id` int(11) NOT NULL,
  `action` varchar(100) NOT NULL,
  `table_affected` varchar(50) DEFAULT NULL,
  `record_id` int(11) DEFAULT NULL,
  `old_values` text DEFAULT NULL,
  `new_values` text DEFAULT NULL,
  `ip_address` varchar(45) DEFAULT NULL,
  `action_timestamp` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `audit_log`
--

INSERT INTO `audit_log` (`audit_id`, `user_type`, `user_id`, `action`, `table_affected`, `record_id`, `old_values`, `new_values`, `ip_address`, `action_timestamp`) VALUES
(1, 'admin', 1, 'LOGIN', NULL, NULL, NULL, NULL, '192.168.1.127', '2026-08-19 07:32:19'),
(2, 'admin', 1, 'LOGOUT', NULL, NULL, NULL, NULL, '192.168.1.127', '2026-08-19 07:32:47'),
(3, 'admin', 1, 'LOGIN', NULL, NULL, NULL, NULL, '192.168.1.127', '2026-08-19 07:50:18'),
(4, 'admin', 1, 'LOGOUT', NULL, NULL, NULL, NULL, '192.168.1.127', '2026-08-19 07:50:23');

-- --------------------------------------------------------

--
-- Table structure for table `bookings`
--

CREATE TABLE `bookings` (
  `booking_id` int(11) NOT NULL,
  `customer_id` int(11) NOT NULL,
  `room_id` int(11) NOT NULL,
  `check_in_date` date NOT NULL,
  `check_out_date` date NOT NULL,
  `total_amount` decimal(10,2) DEFAULT NULL,
  `reschedule_charge` decimal(10,2) DEFAULT 0.00,
  `payment_method` enum('Cash','GCash/QR Scan','QR PH') NOT NULL DEFAULT 'Cash',
  `status` enum('Reserved','Checked In','Checked Out','Cancelled','Rescheduled') NOT NULL DEFAULT 'Reserved',
  `adults` int(11) DEFAULT 1,
  `seniors` int(11) DEFAULT 0,
  `kids` int(11) DEFAULT 0,
  `senior_discount` decimal(10,2) DEFAULT 0.00,
  `extra_guest_charge` decimal(10,2) DEFAULT 0.00,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `bookings`
--

INSERT INTO `bookings` (`booking_id`, `customer_id`, `room_id`, `check_in_date`, `check_out_date`, `total_amount`, `reschedule_charge`, `payment_method`, `status`, `adults`, `seniors`, `kids`, `senior_discount`, `extra_guest_charge`, `created_at`) VALUES
(48, 36, 9, '2026-06-17', '2026-06-18', 3033.33, 0.00, 'Cash', 'Checked Out', 1, 2, 0, 466.67, 0.00, '2026-06-17 13:09:23'),
(49, 37, 1, '2026-06-17', '2026-06-18', 3466.67, 0.00, 'Cash', 'Reserved', 1, 2, 2, 533.33, 1500.00, '2026-06-17 13:24:02'),
(50, 38, 2, '2026-06-17', '2026-06-18', 2500.00, 0.00, 'Cash', 'Checked Out', 1, 0, 0, 0.00, 0.00, '2026-06-17 13:35:01'),
(51, 28, 3, '2026-06-17', '2026-06-19', 5000.00, 0.00, 'Cash', 'Checked Out', 1, 0, 0, 0.00, 0.00, '2026-06-17 15:09:57');

-- --------------------------------------------------------

--
-- Table structure for table `customers`
--

CREATE TABLE `customers` (
  `customer_id` int(11) NOT NULL,
  `first_name` varchar(50) NOT NULL,
  `last_name` varchar(50) NOT NULL,
  `phone_number` varchar(30) NOT NULL,
  `email` varchar(50) NOT NULL,
  `total_visits` int(11) DEFAULT 0,
  `created_at` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `customers`
--

INSERT INTO `customers` (`customer_id`, `first_name`, `last_name`, `phone_number`, `email`, `total_visits`, `created_at`) VALUES
(28, 'SEAN', 'UTRERA', '09811741631', 'seantur@gmail.com', 3, '2026-05-18 01:59:22'),
(29, 'DWIGTH', 'RAMOS', '09223344556', 'dwigrmos@gmail.com', 1, '2026-05-18 02:00:17'),
(30, 'ARJAY', 'ESPIJON', '09876765366', 'arj@gmail.com', 1, '2026-05-18 02:02:14'),
(31, 'BOGARD', 'SENTENO', '09827366646', 'bograd@gmail.com', 0, '2026-05-18 02:04:45'),
(32, 'HIRO', 'ZENN', '09811741641', 'hirozen@gmail.com', 1, '2026-06-17 00:01:40'),
(33, 'ADRIAN', 'BAGUHIN', '09811741641', 'yabutrenyer30@gmail.com', 1, '2026-06-17 01:39:04'),
(34, 'RYZNE', 'YABTU', '09811741641', 'yrenyer45@gmail. com', 0, '2026-06-17 03:37:47'),
(35, 'RENDEL', 'CEDENO', '09811741641', 'nocumrendel@gmail.com', 1, '2026-06-17 20:54:29'),
(36, 'RENDEL', 'CEDENO', '09676767676', 'nocumrendel@gmail.com', 1, '2026-06-17 21:09:23'),
(37, 'RENYER', 'YABUT', '09811741641', 'yrenyer45@gmail.com', 1, '2026-06-17 21:24:02'),
(38, 'RENYER', 'YABUT', '09811741641', 'yabutrenyer30@gmail.com', 1, '2026-06-17 21:33:49'),
(39, 'RYZEN', 'TUBAY', '09811741641', 'yabutrenyer30@gmail.com', 0, '2026-08-24 21:21:10');

-- --------------------------------------------------------

--
-- Table structure for table `notification_log`
--

CREATE TABLE `notification_log` (
  `log_id` int(11) NOT NULL,
  `booking_id` int(11) DEFAULT NULL,
  `customer_id` int(11) DEFAULT NULL,
  `notification_type` enum('Email','SMS','Receipt') NOT NULL,
  `recipient` varchar(100) DEFAULT NULL,
  `subject` varchar(255) DEFAULT NULL,
  `content` text DEFAULT NULL,
  `status` enum('Sent','Failed','Pending') DEFAULT 'Pending',
  `error_message` text DEFAULT NULL,
  `sent_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `notification_log`
--

INSERT INTO `notification_log` (`log_id`, `booking_id`, `customer_id`, `notification_type`, `recipient`, `subject`, `content`, `status`, `error_message`, `sent_at`) VALUES
(37, 48, NULL, 'Email', 'fromytlang@gmail.com', 'Receipt RCP-1781701851924', 'Thank you for choosing Sync Suites Hotel! Your receipt is attached.', 'Sent', NULL, '2026-06-17 13:11:52'),
(38, 50, NULL, 'SMS', '09811741641', 'Receipt RCP-1781703515222', 'Sync Suites Hotel\nReceipt: RCP-1781703515222\nCustomer: RENYER YABUT\nAmount: ₱2,500.00\nThank you for your stay!', 'Sent', NULL, '2026-06-17 13:40:20'),
(39, 50, NULL, 'Email', 'fromytlang@gmail.com', 'Receipt RCP-1781703515222', 'Thank you for choosing Sync Suites Hotel! Your receipt is attached.', 'Sent', NULL, '2026-06-17 13:40:42'),
(40, 51, NULL, 'Email', 'sutrera.7543@umak.edu.ph', 'Receipt RCP-1781709014132', 'Thank you for choosing Sync Suites Hotel! Your receipt is attached.', 'Sent', NULL, '2026-06-17 15:10:50'),
(41, 49, NULL, 'Email', 'daniel12utrera@gmail.com', 'Receipt RCP-1781712180627', 'Thank you for choosing Sync Suites Hotel! Your receipt is attached.', 'Sent', NULL, '2026-08-12 14:11:48');

-- --------------------------------------------------------

--
-- Table structure for table `payment_transactions`
--

CREATE TABLE `payment_transactions` (
  `transaction_id` int(11) NOT NULL,
  `booking_id` int(11) DEFAULT NULL,
  `customer_id` int(11) DEFAULT NULL,
  `amount` decimal(10,2) NOT NULL,
  `payment_method` varchar(50) NOT NULL,
  `qr_reference` varchar(100) DEFAULT NULL,
  `qr_image_data` text DEFAULT NULL,
  `transaction_status` enum('Pending','Completed','Failed','Refunded') DEFAULT 'Pending',
  `paid_at` timestamp NULL DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `payment_transactions`
--

INSERT INTO `payment_transactions` (`transaction_id`, `booking_id`, `customer_id`, `amount`, `payment_method`, `qr_reference`, `qr_image_data`, `transaction_status`, `paid_at`, `created_at`) VALUES
(12, 50, 38, 2500.00, 'QR PH', 'QRPH-1781703586934-50', NULL, 'Pending', NULL, '2026-06-17 13:39:46'),
(13, 50, 38, 2500.00, 'QR PH', 'QRPH-1781703591740-50', NULL, 'Pending', NULL, '2026-06-17 13:39:51'),
(14, 49, 37, 3466.67, 'QR PH', 'QRPH-1786543961744-49', NULL, 'Pending', NULL, '2026-08-12 14:12:41');

-- --------------------------------------------------------

--
-- Table structure for table `receipts`
--

CREATE TABLE `receipts` (
  `receipt_id` int(11) NOT NULL,
  `booking_id` int(11) NOT NULL,
  `customer_id` int(11) NOT NULL,
  `receipt_number` varchar(50) NOT NULL,
  `receipt_data` text NOT NULL,
  `total_amount` decimal(10,2) NOT NULL,
  `printed_by` varchar(100) DEFAULT NULL,
  `printed_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `emailed_to` varchar(100) DEFAULT NULL,
  `emailed_at` timestamp NULL DEFAULT NULL,
  `sms_sent_to` varchar(20) DEFAULT NULL,
  `sms_sent_at` timestamp NULL DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `receipts`
--

INSERT INTO `receipts` (`receipt_id`, `booking_id`, `customer_id`, `receipt_number`, `receipt_data`, `total_amount`, `printed_by`, `printed_at`, `emailed_to`, `emailed_at`, `sms_sent_to`, `sms_sent_at`) VALUES
(26, 48, 36, 'RCP-1781701800490', '╔══════════════════════════════════════════╗\n║         SYNC SUITES HOTEL                ║\n║      Official Payment Receipt            ║\n╚══════════════════════════════════════════╝\n\nReceipt No: RCP-1781701800490\nDate: 2026-06-17 21:10:00\nBooking ID: 48\n\n----------------------------------------\nCUSTOMER\n----------------------------------------\nName:    RENDEL CEDENO\nPhone:   09676767676\nEmail:   nocumrendel@gmail.com\n\n----------------------------------------\nBOOKING DETAILS\n----------------------------------------\nRoom:        201 (Junior Suite)\nRate/Day:    ₱3,500.00\nCheck-In:    2026-06-17\nCheck-Out:   2026-06-18\nStatus:      Reserved\nPayment:     Cash\n\n----------------------------------------\nGUESTS\n----------------------------------------\nAdults:      1\nSeniors/PWD: 2\nKids:        0\n\n----------------------------------------\nCHARGES\n----------------------------------------\nSenior Discount:          -₱   466.67\n----------------------------------------\nTOTAL:                    ₱  3,033.33\n----------------------------------------\n\nThank you for choosing Sync Suites Hotel!\nTHIS IS AN OFFICIAL RECEIPT\n', 3033.33, 'admin', '2026-06-17 13:10:01', NULL, NULL, NULL, NULL),
(27, 48, 36, 'RCP-1781701828401', '╔══════════════════════════════════════════╗\n║         SYNC SUITES HOTEL                ║\n║      Official Payment Receipt            ║\n╚══════════════════════════════════════════╝\n\nReceipt No: RCP-1781701828401\nDate: 2026-06-17 21:10:28\nBooking ID: 48\n\n----------------------------------------\nCUSTOMER\n----------------------------------------\nName:    RENDEL CEDENO\nPhone:   09676767676\nEmail:   nocumrendel@gmail.com\n\n----------------------------------------\nBOOKING DETAILS\n----------------------------------------\nRoom:        201 (Junior Suite)\nRate/Day:    ₱3,500.00\nCheck-In:    2026-06-17\nCheck-Out:   2026-06-18\nStatus:      Reserved\nPayment:     Cash\n\n----------------------------------------\nGUESTS\n----------------------------------------\nAdults:      1\nSeniors/PWD: 2\nKids:        0\n\n----------------------------------------\nCHARGES\n----------------------------------------\nSenior Discount:          -₱   466.67\n----------------------------------------\nTOTAL:                    ₱  3,033.33\n----------------------------------------\n\nThank you for choosing Sync Suites Hotel!\nTHIS IS AN OFFICIAL RECEIPT\n', 3033.33, 'admin', '2026-06-17 13:10:29', NULL, NULL, NULL, NULL),
(28, 48, 36, 'RCP-1781701851924', '╔══════════════════════════════════════════╗\n║         SYNC SUITES HOTEL                ║\n║      Official Payment Receipt            ║\n╚══════════════════════════════════════════╝\n\nReceipt No: RCP-1781701851924\nDate: 2026-06-17 21:10:51\nBooking ID: 48\n\n----------------------------------------\nCUSTOMER\n----------------------------------------\nName:    RENDEL CEDENO\nPhone:   09676767676\nEmail:   nocumrendel@gmail.com\n\n----------------------------------------\nBOOKING DETAILS\n----------------------------------------\nRoom:        201 (Junior Suite)\nRate/Day:    ₱3,500.00\nCheck-In:    2026-06-17\nCheck-Out:   2026-06-18\nStatus:      Reserved\nPayment:     Cash\n\n----------------------------------------\nGUESTS\n----------------------------------------\nAdults:      1\nSeniors/PWD: 2\nKids:        0\n\n----------------------------------------\nCHARGES\n----------------------------------------\nSenior Discount:          -₱   466.67\n----------------------------------------\nTOTAL:                    ₱  3,033.33\n----------------------------------------\n\nThank you for choosing Sync Suites Hotel!\nTHIS IS AN OFFICIAL RECEIPT\n', 3033.33, 'admin', '2026-06-17 13:10:54', 'fromytlang@gmail.com', '2026-06-17 13:11:52', NULL, NULL),
(29, 49, 37, 'RCP-1781702665656', '╔══════════════════════════════════════════╗\n║         SYNC SUITES HOTEL                ║\n║      Official Payment Receipt            ║\n╚══════════════════════════════════════════╝\n\nReceipt No: RCP-1781702665656\nDate: 2026-06-17 21:24:25\nBooking ID: 49\n\n----------------------------------------\nCUSTOMER\n----------------------------------------\nName:    RENYER YABUT\nPhone:   09811741641\nEmail:   yrenyer45@gmail.com\n\n----------------------------------------\nBOOKING DETAILS\n----------------------------------------\nRoom:        101 (Standard Room)\nRate/Day:    ₱2,500.00\nCheck-In:    2026-06-17\nCheck-Out:   2026-06-18\nStatus:      Reserved\nPayment:     Cash\n\n----------------------------------------\nGUESTS\n----------------------------------------\nAdults:      1\nSeniors/PWD: 2\nKids:        2\n\n----------------------------------------\nCHARGES\n----------------------------------------\nExtra Guest:              ₱  1,500.00\nSenior Discount:          -₱   533.33\n----------------------------------------\nTOTAL:                    ₱  3,466.67\n----------------------------------------\n\nThank you for choosing Sync Suites Hotel!\nTHIS IS AN OFFICIAL RECEIPT\n', 3466.67, 'admin', '2026-06-17 13:24:27', NULL, NULL, NULL, NULL),
(30, 48, 36, 'RCP-1781702685833', '╔══════════════════════════════════════════╗\n║         SYNC SUITES HOTEL                ║\n║      Official Payment Receipt            ║\n╚══════════════════════════════════════════╝\n\nReceipt No: RCP-1781702685833\nDate: 2026-06-17 21:24:45\nBooking ID: 48\n\n----------------------------------------\nCUSTOMER INFORMATION\n----------------------------------------\nName:    RENDEL CEDENO\nPhone:   09676767676\nEmail:   nocumrendel@gmail.com\n\n----------------------------------------\nBOOKING DETAILS\n----------------------------------------\nRoom:        201 (Junior Suite)\nRate/Day:    ₱3,500.00\nCheck-In:    2026-06-17\nCheck-Out:   2026-06-18\nStatus:      Checked In\nPayment:     Cash\n\n----------------------------------------\nGUEST BREAKDOWN\n----------------------------------------\nAdults:      1\nSeniors/PWD: 2 (20% discount applied)\nKids:        0 (Free)\n\n----------------------------------------\nCHARGES\n----------------------------------------\nBase Amount:              ₱  3,500.00\nSenior/PWD Discount:      -₱   466.67\n----------------------------------------\nTOTAL AMOUNT:             ₱  3,033.33\n----------------------------------------\n\nThank you for choosing Sync Suites Hotel!\nFor inquiries: info@syncsuites.com\nPhone: +63 912 345 6789\n\nTHIS IS AN OFFICIAL RECEIPT\nKeep this for your records.\n', 3033.33, 'admin', '2026-06-17 13:24:45', NULL, NULL, NULL, NULL),
(31, 50, 38, 'RCP-1781703340344', '╔══════════════════════════════════════════╗\n║         SYNC SUITES HOTEL                ║\n║      Official Payment Receipt            ║\n╚══════════════════════════════════════════╝\n\nReceipt No: RCP-1781703340344\nDate: 2026-06-17 21:35:40\nBooking ID: 50\n\n----------------------------------------\nCUSTOMER\n----------------------------------------\nName:    RENYER YABUT\nPhone:   09811741641\nEmail:   yabutrenyer30@gmail.com\n\n----------------------------------------\nBOOKING DETAILS\n----------------------------------------\nRoom:        102 (Standard Room)\nRate/Day:    ₱2,500.00\nCheck-In:    2026-06-17\nCheck-Out:   2026-06-18\nStatus:      Reserved\nPayment:     Cash\n\n----------------------------------------\nGUESTS\n----------------------------------------\nAdults:      1\nSeniors/PWD: 0\nKids:        0\n\n----------------------------------------\nCHARGES\n----------------------------------------\n----------------------------------------\nTOTAL:                    ₱  2,500.00\n----------------------------------------\n\nThank you for choosing Sync Suites Hotel!\nTHIS IS AN OFFICIAL RECEIPT\n', 2500.00, 'admin', '2026-06-17 13:35:41', NULL, NULL, NULL, NULL),
(32, 50, 38, 'RCP-1781703379162', '╔══════════════════════════════════════════╗\n║         SYNC SUITES HOTEL                ║\n║      Official Payment Receipt            ║\n╚══════════════════════════════════════════╝\n\nReceipt No: RCP-1781703379162\nDate: 2026-06-17 21:36:19\nBooking ID: 50\n\n----------------------------------------\nCUSTOMER\n----------------------------------------\nName:    RENYER YABUT\nPhone:   09811741641\nEmail:   yabutrenyer30@gmail.com\n\n----------------------------------------\nBOOKING DETAILS\n----------------------------------------\nRoom:        102 (Standard Room)\nRate/Day:    ₱2,500.00\nCheck-In:    2026-06-17\nCheck-Out:   2026-06-18\nStatus:      Reserved\nPayment:     Cash\n\n----------------------------------------\nGUESTS\n----------------------------------------\nAdults:      1\nSeniors/PWD: 0\nKids:        0\n\n----------------------------------------\nCHARGES\n----------------------------------------\n----------------------------------------\nTOTAL:                    ₱  2,500.00\n----------------------------------------\n\nThank you for choosing Sync Suites Hotel!\nTHIS IS AN OFFICIAL RECEIPT\n', 2500.00, 'admin', '2026-06-17 13:36:20', NULL, NULL, NULL, NULL),
(33, 50, 38, 'RCP-1781703515222', '╔══════════════════════════════════════════╗\n║         SYNC SUITES HOTEL                ║\n║      Official Payment Receipt            ║\n╚══════════════════════════════════════════╝\n\nReceipt No: RCP-1781703515222\nDate: 2026-06-17 21:38:35\nBooking ID: 50\n\n----------------------------------------\nCUSTOMER\n----------------------------------------\nName:    RENYER YABUT\nPhone:   09811741641\nEmail:   yabutrenyer30@gmail.com\n\n----------------------------------------\nBOOKING DETAILS\n----------------------------------------\nRoom:        102 (Standard Room)\nRate/Day:    ₱2,500.00\nCheck-In:    2026-06-17\nCheck-Out:   2026-06-18\nStatus:      Checked In\nPayment:     Cash\n\n----------------------------------------\nGUESTS\n----------------------------------------\nAdults:      1\nSeniors/PWD: 0\nKids:        0\n\n----------------------------------------\nCHARGES\n----------------------------------------\n----------------------------------------\nTOTAL:                    ₱  2,500.00\n----------------------------------------\n\nThank you for choosing Sync Suites Hotel!\nTHIS IS AN OFFICIAL RECEIPT\n', 2500.00, 'admin', '2026-06-17 13:38:36', 'fromytlang@gmail.com', '2026-06-17 13:40:42', '09811741641', '2026-06-17 13:40:20'),
(34, 49, 37, 'RCP-1781703566153', '╔══════════════════════════════════════════╗\n║         SYNC SUITES HOTEL                ║\n║      Official Payment Receipt            ║\n╚══════════════════════════════════════════╝\n\nReceipt No: RCP-1781703566153\nDate: 2026-06-17 21:39:26\nBooking ID: 49\n\n----------------------------------------\nCUSTOMER INFORMATION\n----------------------------------------\nName:    RENYER YABUT\nPhone:   09811741641\nEmail:   yrenyer45@gmail.com\n\n----------------------------------------\nBOOKING DETAILS\n----------------------------------------\nRoom:        101 (Standard Room)\nRate/Day:    ₱2,500.00\nCheck-In:    2026-06-17\nCheck-Out:   2026-06-18\nStatus:      Reserved\nPayment:     Cash\n\n----------------------------------------\nGUEST BREAKDOWN\n----------------------------------------\nAdults:      1\nSeniors/PWD: 2 (20% discount applied)\nKids:        2 (Free)\n\n----------------------------------------\nCHARGES\n----------------------------------------\nBase Amount:              ₱  2,500.00\nExtra Guest Charge:       ₱  1,500.00\nSenior/PWD Discount:      -₱   533.33\n----------------------------------------\nTOTAL AMOUNT:             ₱  3,466.67\n----------------------------------------\n\nThank you for choosing Sync Suites Hotel!\nFor inquiries: info@syncsuites.com\nPhone: +63 912 345 6789\n\nTHIS IS AN OFFICIAL RECEIPT\nKeep this for your records.\n', 3466.67, 'admin', '2026-06-17 13:39:26', NULL, NULL, NULL, NULL),
(35, 51, 28, 'RCP-1781709014132', '╔══════════════════════════════════════════╗\n║         SYNC SUITES HOTEL                ║\n║      Official Payment Receipt            ║\n╚══════════════════════════════════════════╝\n\nReceipt No: RCP-1781709014132\nDate: 2026-06-17 23:10:14\nBooking ID: 51\n\n----------------------------------------\nCUSTOMER\n----------------------------------------\nName:    SEAN UTRERA\nPhone:   09811741631\nEmail:   seantur@gmail.com\n\n----------------------------------------\nBOOKING DETAILS\n----------------------------------------\nRoom:        103 (Standard Room)\nRate/Day:    ₱2,500.00\nCheck-In:    2026-06-17\nCheck-Out:   2026-06-19\nStatus:      Checked In\nPayment:     Cash\n\n----------------------------------------\nGUESTS\n----------------------------------------\nAdults:      1\nSeniors/PWD: 0\nKids:        0\n\n----------------------------------------\nCHARGES\n----------------------------------------\n----------------------------------------\nTOTAL:                    ₱  5,000.00\n----------------------------------------\n\nThank you for choosing Sync Suites Hotel!\nTHIS IS AN OFFICIAL RECEIPT\n', 5000.00, 'admin', '2026-06-17 15:10:15', 'sutrera.7543@umak.edu.ph', '2026-06-17 15:10:50', NULL, NULL),
(36, 50, 38, 'RCP-1781709332850', '╔══════════════════════════════════════════╗\n║         SYNC SUITES HOTEL                ║\n║      Official Payment Receipt            ║\n╚══════════════════════════════════════════╝\n\nReceipt No: RCP-1781709332850\nDate: 2026-06-17 23:15:32\nBooking ID: 50\n\n----------------------------------------\nCUSTOMER INFORMATION\n----------------------------------------\nName:    RENYER YABUT\nPhone:   09811741641\nEmail:   yabutrenyer30@gmail.com\n\n----------------------------------------\nBOOKING DETAILS\n----------------------------------------\nRoom:        102 (Standard Room)\nRate/Day:    ₱2,500.00\nCheck-In:    2026-06-17\nCheck-Out:   2026-06-18\nStatus:      Checked In\nPayment:     Cash\n\n----------------------------------------\nGUEST BREAKDOWN\n----------------------------------------\nAdults:      1\nSeniors/PWD: 0 (20% discount applied)\nKids:        0 (Free)\n\n----------------------------------------\nCHARGES\n----------------------------------------\nBase Amount:              ₱  2,500.00\n----------------------------------------\nTOTAL AMOUNT:             ₱  2,500.00\n----------------------------------------\n\nThank you for choosing Sync Suites Hotel!\nFor inquiries: info@syncsuites.com\nPhone: +63 912 345 6789\n\nTHIS IS AN OFFICIAL RECEIPT\nKeep this for your records.\n', 2500.00, 'admin', '2026-06-17 15:15:32', NULL, NULL, NULL, NULL),
(37, 51, 28, 'RCP-1781712173210', '╔══════════════════════════════════════════╗\n║         SYNC SUITES HOTEL                ║\n║      Official Payment Receipt            ║\n╚══════════════════════════════════════════╝\n\nReceipt No: RCP-1781712173210\nDate: 2026-06-18 00:02:53\nBooking ID: 51\n\n----------------------------------------\nCUSTOMER INFORMATION\n----------------------------------------\nName:    SEAN UTRERA\nPhone:   09811741631\nEmail:   seantur@gmail.com\n\n----------------------------------------\nBOOKING DETAILS\n----------------------------------------\nRoom:        103 (Standard Room)\nRate/Day:    ₱2,500.00\nCheck-In:    2026-06-17\nCheck-Out:   2026-06-19\nStatus:      Checked In\nPayment:     Cash\n\n----------------------------------------\nGUEST BREAKDOWN\n----------------------------------------\nAdults:      1\nSeniors/PWD: 0 (20% discount applied)\nKids:        0 (Free)\n\n----------------------------------------\nCHARGES\n----------------------------------------\nBase Amount:              ₱  5,000.00\n----------------------------------------\nTOTAL AMOUNT:             ₱  5,000.00\n----------------------------------------\n\nThank you for choosing Sync Suites Hotel!\nFor inquiries: info@syncsuites.com\nPhone: +63 912 345 6789\n\nTHIS IS AN OFFICIAL RECEIPT\nKeep this for your records.\n', 5000.00, 'admin', '2026-06-17 16:02:53', NULL, NULL, NULL, NULL),
(38, 50, 38, 'RCP-1781712176597', '╔══════════════════════════════════════════╗\n║         SYNC SUITES HOTEL                ║\n║      Official Payment Receipt            ║\n╚══════════════════════════════════════════╝\n\nReceipt No: RCP-1781712176597\nDate: 2026-06-18 00:02:56\nBooking ID: 50\n\n----------------------------------------\nCUSTOMER INFORMATION\n----------------------------------------\nName:    RENYER YABUT\nPhone:   09811741641\nEmail:   yabutrenyer30@gmail.com\n\n----------------------------------------\nBOOKING DETAILS\n----------------------------------------\nRoom:        102 (Standard Room)\nRate/Day:    ₱2,500.00\nCheck-In:    2026-06-17\nCheck-Out:   2026-06-18\nStatus:      Checked In\nPayment:     Cash\n\n----------------------------------------\nGUEST BREAKDOWN\n----------------------------------------\nAdults:      1\nSeniors/PWD: 0 (20% discount applied)\nKids:        0 (Free)\n\n----------------------------------------\nCHARGES\n----------------------------------------\nBase Amount:              ₱  2,500.00\n----------------------------------------\nTOTAL AMOUNT:             ₱  2,500.00\n----------------------------------------\n\nThank you for choosing Sync Suites Hotel!\nFor inquiries: info@syncsuites.com\nPhone: +63 912 345 6789\n\nTHIS IS AN OFFICIAL RECEIPT\nKeep this for your records.\n', 2500.00, 'admin', '2026-06-17 16:02:56', NULL, NULL, NULL, NULL),
(39, 49, 37, 'RCP-1781712180627', '╔══════════════════════════════════════════╗\n║         SYNC SUITES HOTEL                ║\n║      Official Payment Receipt            ║\n╚══════════════════════════════════════════╝\n\nReceipt No: RCP-1781712180627\nDate: 2026-06-18 00:03:00\nBooking ID: 49\n\n----------------------------------------\nCUSTOMER INFORMATION\n----------------------------------------\nName:    RENYER YABUT\nPhone:   09811741641\nEmail:   yrenyer45@gmail.com\n\n----------------------------------------\nBOOKING DETAILS\n----------------------------------------\nRoom:        101 (Standard Room)\nRate/Day:    ₱2,500.00\nCheck-In:    2026-06-17\nCheck-Out:   2026-06-18\nStatus:      Reserved\nPayment:     Cash\n\n----------------------------------------\nGUEST BREAKDOWN\n----------------------------------------\nAdults:      1\nSeniors/PWD: 2 (20% discount applied)\nKids:        2 (Free)\n\n----------------------------------------\nCHARGES\n----------------------------------------\nBase Amount:              ₱  2,500.00\nExtra Guest Charge:       ₱  1,500.00\nSenior/PWD Discount:      -₱   533.33\n----------------------------------------\nTOTAL AMOUNT:             ₱  3,466.67\n----------------------------------------\n\nThank you for choosing Sync Suites Hotel!\nFor inquiries: info@syncsuites.com\nPhone: +63 912 345 6789\n\nTHIS IS AN OFFICIAL RECEIPT\nKeep this for your records.\n', 3466.67, 'admin', '2026-06-17 16:03:00', 'daniel12utrera@gmail.com', '2026-08-12 14:11:48', NULL, NULL),
(40, 48, 36, 'RCP-1781712184644', '╔══════════════════════════════════════════╗\n║         SYNC SUITES HOTEL                ║\n║      Official Payment Receipt            ║\n╚══════════════════════════════════════════╝\n\nReceipt No: RCP-1781712184644\nDate: 2026-06-18 00:03:04\nBooking ID: 48\n\n----------------------------------------\nCUSTOMER INFORMATION\n----------------------------------------\nName:    RENDEL CEDENO\nPhone:   09676767676\nEmail:   nocumrendel@gmail.com\n\n----------------------------------------\nBOOKING DETAILS\n----------------------------------------\nRoom:        201 (Junior Suite)\nRate/Day:    ₱3,500.00\nCheck-In:    2026-06-17\nCheck-Out:   2026-06-18\nStatus:      Checked In\nPayment:     Cash\n\n----------------------------------------\nGUEST BREAKDOWN\n----------------------------------------\nAdults:      1\nSeniors/PWD: 2 (20% discount applied)\nKids:        0 (Free)\n\n----------------------------------------\nCHARGES\n----------------------------------------\nBase Amount:              ₱  3,500.00\nSenior/PWD Discount:      -₱   466.67\n----------------------------------------\nTOTAL AMOUNT:             ₱  3,033.33\n----------------------------------------\n\nThank you for choosing Sync Suites Hotel!\nFor inquiries: info@syncsuites.com\nPhone: +63 912 345 6789\n\nTHIS IS AN OFFICIAL RECEIPT\nKeep this for your records.\n', 3033.33, 'admin', '2026-06-17 16:03:04', NULL, NULL, NULL, NULL),
(41, 51, 28, 'RCP-1786543816092', '╔══════════════════════════════════════════╗\n║         SYNC SUITES HOTEL                ║\n║      Official Payment Receipt            ║\n╚══════════════════════════════════════════╝\n\nReceipt No: RCP-1786543816092\nDate: 2026-08-12 22:10:16\nBooking ID: 51\n\n----------------------------------------\nCUSTOMER INFORMATION\n----------------------------------------\nName:    SEAN UTRERA\nPhone:   09811741631\nEmail:   seantur@gmail.com\n\n----------------------------------------\nBOOKING DETAILS\n----------------------------------------\nRoom:        103 (Standard Room)\nRate/Day:    ₱2,500.00\nCheck-In:    2026-06-17\nCheck-Out:   2026-06-19\nStatus:      Checked Out\nPayment:     Cash\n\n----------------------------------------\nGUEST BREAKDOWN\n----------------------------------------\nAdults:      1\nSeniors/PWD: 0 (20% discount applied)\nKids:        0 (Free)\n\n----------------------------------------\nCHARGES\n----------------------------------------\nBase Amount:              ₱  5,000.00\n----------------------------------------\nTOTAL AMOUNT:             ₱  5,000.00\n----------------------------------------\n\nThank you for choosing Sync Suites Hotel!\nFor inquiries: info@syncsuites.com\nPhone: +63 912 345 6789\n\nTHIS IS AN OFFICIAL RECEIPT\nKeep this for your records.\n', 5000.00, 'admin', '2026-08-12 14:10:16', NULL, NULL, NULL, NULL);

-- --------------------------------------------------------

--
-- Table structure for table `rooms`
--

CREATE TABLE `rooms` (
  `room_id` int(11) NOT NULL,
  `room_number` varchar(10) NOT NULL,
  `room_type_id` int(11) NOT NULL,
  `floor_number` int(11) DEFAULT 1,
  `is_available` tinyint(1) DEFAULT 1,
  `is_maintenance` tinyint(1) DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `rooms`
--

INSERT INTO `rooms` (`room_id`, `room_number`, `room_type_id`, `floor_number`, `is_available`, `is_maintenance`) VALUES
(1, '101', 1, 1, 1, 0),
(2, '102', 1, 1, 1, 0),
(3, '103', 1, 1, 1, 0),
(4, '104', 1, 1, 1, 0),
(5, '105', 1, 1, 1, 0),
(6, '106', 1, 1, 1, 0),
(7, '107', 1, 1, 1, 0),
(8, '108', 1, 1, 1, 0),
(9, '201', 2, 2, 1, 0),
(10, '202', 2, 2, 1, 0),
(11, '203', 2, 2, 1, 0),
(12, '204', 2, 2, 1, 0),
(13, '205', 2, 2, 1, 0),
(14, '206', 2, 2, 1, 0),
(15, '207', 2, 2, 1, 0),
(16, '208', 2, 2, 1, 0),
(17, '301', 3, 3, 1, 0),
(18, '302', 3, 3, 1, 0),
(19, '303', 3, 3, 1, 0),
(20, '304', 3, 3, 1, 0),
(21, '305', 3, 3, 1, 0),
(22, '401', 4, 4, 1, 0),
(23, '402', 4, 4, 1, 0),
(24, '403', 4, 4, 1, 0),
(25, '404', 4, 4, 1, 0),
(26, '405', 4, 4, 1, 0);

-- --------------------------------------------------------

--
-- Table structure for table `room_types`
--

CREATE TABLE `room_types` (
  `room_type_id` int(11) NOT NULL,
  `type_name` varchar(50) NOT NULL,
  `rate_per_day` decimal(10,2) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `room_types`
--

INSERT INTO `room_types` (`room_type_id`, `type_name`, `rate_per_day`) VALUES
(1, 'Standard Room', 2500.00),
(2, 'Junior Suite', 3500.00),
(3, 'Executive Suite', 5000.00),
(4, 'Presidential Suite', 8000.00);

-- --------------------------------------------------------

--
-- Table structure for table `system_settings`
--

CREATE TABLE `system_settings` (
  `setting_id` int(11) NOT NULL,
  `setting_key` varchar(100) NOT NULL,
  `setting_value` text DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `system_settings`
--

INSERT INTO `system_settings` (`setting_id`, `setting_key`, `setting_value`, `description`, `updated_at`) VALUES
(1, 'hotel_name', 'Sync Suites Hotel', 'Hotel display name', '2026-06-16 14:58:50'),
(2, 'hotel_address', '123 Tomas Morato Ave Quezon CIty Manila Philipines', 'Hotel address', '2026-06-16 19:30:19'),
(3, 'hotel_phone', '+63 912 345 6789', 'Hotel contact number', '2026-06-16 14:58:50'),
(4, 'hotel_email', 'info@syncsuites.com', 'Hotel email', '2026-06-16 14:58:50'),
(5, 'senior_discount_rate', '0.20', 'Senior/PWD discount percentage', '2026-06-16 14:58:50'),
(6, 'max_advance_booking_days', '90', 'Maximum days allowed for advance booking', '2026-06-16 14:58:50'),
(7, 'check_in_time', '14:00', 'Standard check-in time', '2026-06-16 14:58:50'),
(8, 'check_out_time', '12:00', 'Standard check-out time', '2026-06-16 14:58:50'),
(9, 'smtp_host', 'smtp.gmail.com', 'SMTP server for emails', '2026-06-16 17:29:41'),
(10, 'smtp_port', '587', 'SMTP port', '2026-06-16 18:00:43'),
(11, 'smtp_username', 'syncsuiteshotel@gmail.com', 'SMTP username', '2026-06-17 12:27:41'),
(12, 'smtp_password', 'fdaynxshphfbretg', 'SMTP password', '2026-06-17 12:27:41'),
(13, 'twilio_sid', '', 'Twilio Account SID for SMS', '2026-06-16 14:58:50'),
(14, 'twilio_auth_token', '', 'Twilio Auth Token', '2026-06-16 14:58:50'),
(15, 'twilio_phone', '', 'Twilio phone number', '2026-06-16 14:58:50');

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `user_id` int(11) NOT NULL,
  `customer_id` int(11) DEFAULT NULL,
  `username` varchar(50) NOT NULL,
  `password` varchar(255) NOT NULL,
  `email` varchar(100) NOT NULL,
  `phone_number` varchar(20) DEFAULT NULL,
  `is_active` tinyint(1) DEFAULT 1,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `last_login` timestamp NULL DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`user_id`, `customer_id`, `username`, `password`, `email`, `phone_number`, `is_active`, `created_at`, `last_login`) VALUES
(1, 34, 'ren123', 'renyer', 'yrenyer45@gmail. com', '09811741641', 1, '2026-06-16 19:37:47', '2026-06-16 19:38:20'),
(2, 38, 'renyer', 'pogiako', 'yabutrenyer30@gmail.com', '09811741641', 1, '2026-06-17 13:33:49', '2026-06-18 16:55:35'),
(3, 39, 'ryzenbay', '12345678', 'yabutrenyer30@gmail.com', '09811741641', 1, '2026-08-24 13:21:10', '2026-08-24 13:21:29');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `admins`
--
ALTER TABLE `admins`
  ADD PRIMARY KEY (`adminID`);

--
-- Indexes for table `archived_bookings`
--
ALTER TABLE `archived_bookings`
  ADD PRIMARY KEY (`archive_id`);

--
-- Indexes for table `audit_log`
--
ALTER TABLE `audit_log`
  ADD PRIMARY KEY (`audit_id`);

--
-- Indexes for table `bookings`
--
ALTER TABLE `bookings`
  ADD PRIMARY KEY (`booking_id`),
  ADD KEY `customer_id` (`customer_id`),
  ADD KEY `room_id` (`room_id`);

--
-- Indexes for table `customers`
--
ALTER TABLE `customers`
  ADD PRIMARY KEY (`customer_id`);

--
-- Indexes for table `notification_log`
--
ALTER TABLE `notification_log`
  ADD PRIMARY KEY (`log_id`);

--
-- Indexes for table `payment_transactions`
--
ALTER TABLE `payment_transactions`
  ADD PRIMARY KEY (`transaction_id`),
  ADD KEY `booking_id` (`booking_id`);

--
-- Indexes for table `receipts`
--
ALTER TABLE `receipts`
  ADD PRIMARY KEY (`receipt_id`),
  ADD UNIQUE KEY `receipt_number` (`receipt_number`),
  ADD KEY `booking_id` (`booking_id`);

--
-- Indexes for table `rooms`
--
ALTER TABLE `rooms`
  ADD PRIMARY KEY (`room_id`),
  ADD UNIQUE KEY `room_number` (`room_number`),
  ADD KEY `room_type_id` (`room_type_id`);

--
-- Indexes for table `room_types`
--
ALTER TABLE `room_types`
  ADD PRIMARY KEY (`room_type_id`);

--
-- Indexes for table `system_settings`
--
ALTER TABLE `system_settings`
  ADD PRIMARY KEY (`setting_id`),
  ADD UNIQUE KEY `setting_key` (`setting_key`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`user_id`),
  ADD UNIQUE KEY `username` (`username`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `admins`
--
ALTER TABLE `admins`
  MODIFY `adminID` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT for table `archived_bookings`
--
ALTER TABLE `archived_bookings`
  MODIFY `archive_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT for table `audit_log`
--
ALTER TABLE `audit_log`
  MODIFY `audit_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT for table `bookings`
--
ALTER TABLE `bookings`
  MODIFY `booking_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=52;

--
-- AUTO_INCREMENT for table `customers`
--
ALTER TABLE `customers`
  MODIFY `customer_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=40;

--
-- AUTO_INCREMENT for table `notification_log`
--
ALTER TABLE `notification_log`
  MODIFY `log_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=42;

--
-- AUTO_INCREMENT for table `payment_transactions`
--
ALTER TABLE `payment_transactions`
  MODIFY `transaction_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=15;

--
-- AUTO_INCREMENT for table `receipts`
--
ALTER TABLE `receipts`
  MODIFY `receipt_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=42;

--
-- AUTO_INCREMENT for table `rooms`
--
ALTER TABLE `rooms`
  MODIFY `room_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=27;

--
-- AUTO_INCREMENT for table `room_types`
--
ALTER TABLE `room_types`
  MODIFY `room_type_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT for table `system_settings`
--
ALTER TABLE `system_settings`
  MODIFY `setting_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=16;

--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `user_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `bookings`
--
ALTER TABLE `bookings`
  ADD CONSTRAINT `bookings_ibfk_1` FOREIGN KEY (`customer_id`) REFERENCES `customers` (`customer_id`),
  ADD CONSTRAINT `bookings_ibfk_2` FOREIGN KEY (`room_id`) REFERENCES `rooms` (`room_id`);

--
-- Constraints for table `payment_transactions`
--
ALTER TABLE `payment_transactions`
  ADD CONSTRAINT `payment_transactions_ibfk_1` FOREIGN KEY (`booking_id`) REFERENCES `bookings` (`booking_id`);

--
-- Constraints for table `receipts`
--
ALTER TABLE `receipts`
  ADD CONSTRAINT `receipts_ibfk_1` FOREIGN KEY (`booking_id`) REFERENCES `bookings` (`booking_id`);

--
-- Constraints for table `rooms`
--
ALTER TABLE `rooms`
  ADD CONSTRAINT `rooms_ibfk_1` FOREIGN KEY (`room_type_id`) REFERENCES `room_types` (`room_type_id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
