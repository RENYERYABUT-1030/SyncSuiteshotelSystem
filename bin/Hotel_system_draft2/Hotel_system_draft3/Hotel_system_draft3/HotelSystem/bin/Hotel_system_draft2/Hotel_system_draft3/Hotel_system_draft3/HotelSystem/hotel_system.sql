-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Jun 16, 2026 at 07:13 PM
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

--
-- Dumping data for table `archived_bookings`
--

INSERT INTO `archived_bookings` (`archive_id`, `original_booking_id`, `customer_id`, `room_id`, `check_in_date`, `check_out_date`, `total_amount`, `payment_method`, `status`, `adults`, `seniors`, `kids`, `senior_discount`, `extra_guest_charge`, `reschedule_charge`, `archived_by`, `archive_reason`, `archived_at`) VALUES
(1, 41, 30, 22, '2026-05-18', '2026-05-23', 32000.00, 'Cash', 'Cancelled', 2, 0, 0, 0.00, 0.00, 0.00, 'admin', 'secret', '2026-06-16 16:02:23');

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
(39, 28, 9, '2026-05-18', '2026-05-19', 3920.00, 0.00, 'Cash', 'Checked Out', 2, 1, 0, 700.00, 0.00, '2026-05-18 01:59:22'),
(40, 29, 17, '2026-05-18', '2026-05-21', 26400.00, 0.00, 'Cash', 'Checked Out', 4, 0, 0, 0.00, 0.00, '2026-05-18 02:00:17'),
(42, 31, 2, '2026-05-18', '2026-05-21', 7200.00, 0.00, 'GCash/QR Scan', 'Reserved', 2, 0, 0, 0.00, 0.00, '2026-05-18 02:04:45'),
(43, 32, 1, '2026-06-17', '2026-06-20', 12420.00, 0.00, 'Cash', 'Checked In', 3, 2, 1, 1080.00, 6000.00, '2026-06-16 16:01:40'),
(44, 28, 22, '2026-06-17', '2026-06-21', 27733.33, 0.00, 'Cash', 'Checked In', 1, 2, 2, 4266.67, 0.00, '2026-06-16 16:39:34');

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
(28, 'SEAN', 'UTRERA', '09811741631', 'seantur@gmail.com', 2, '2026-05-18 01:59:22'),
(29, 'DWIGTH', 'RAMOS', '09223344556', 'dwigrmos@gmail.com', 1, '2026-05-18 02:00:17'),
(30, 'ARJAY', 'ESPIJON', '09876765366', 'arj@gmail.com', 0, '2026-05-18 02:02:14'),
(31, 'BOGARD', 'SENTENO', '09827366646', 'bograd@gmail.com', 0, '2026-05-18 02:04:45'),
(32, 'HIRO', 'ZENN', '09811741641', 'hirozen@gmail.com', 1, '2026-06-17 00:01:40');

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
(1, 43, NULL, 'SMS', '09811741641', 'Receipt RCP-1781625907428', 'Sync Suites Hotel\nReceipt: RCP-1781625907428\nCustomer: HIRO ZENN\nAmount: ₱12,420.00\nThank you for your stay!', 'Sent', NULL, '2026-06-16 16:07:14'),
(2, 43, NULL, 'SMS', '09811741641', 'Receipt RCP-1781625907428', 'Sync Suites Hotel\nReceipt: RCP-1781625907428\nCustomer: HIRO ZENN\nAmount: ₱12,420.00\nThank you for your stay!', 'Sent', NULL, '2026-06-16 16:07:25'),
(3, 44, NULL, 'SMS', '09811741641', 'Receipt RCP-1781628068475', 'Sync Suites Hotel\nReceipt: RCP-1781628068475\nCustomer: SEAN UTRERA\nAmount: ₱27,733.33\nThank you for your stay!', 'Sent', NULL, '2026-06-16 16:41:25'),
(4, 44, NULL, 'SMS', '09811741641', 'Receipt RCP-1781628068475', 'Sync Suites Hotel\nReceipt: RCP-1781628068475\nCustomer: SEAN UTRERA\nAmount: ₱27,733.33\nThank you for your stay!', 'Sent', NULL, '2026-06-16 16:41:46'),
(5, 44, NULL, 'SMS', '09811741641', 'Receipt RCP-1781628068475', 'Sync Suites Hotel\nReceipt: RCP-1781628068475\nCustomer: SEAN UTRERA\nAmount: ₱27,733.33\nThank you for your stay!', 'Sent', NULL, '2026-06-16 16:41:55'),
(6, 44, NULL, 'SMS', '09811741641', 'Receipt RCP-1781628068475', 'Sync Suites Hotel\nReceipt: RCP-1781628068475\nCustomer: SEAN UTRERA\nAmount: ₱27,733.33\nThank you for your stay!', 'Sent', NULL, '2026-06-16 16:42:10'),
(7, 44, NULL, 'SMS', '09811741641', 'Receipt RCP-1781628068475', 'Sync Suites Hotel\nReceipt: RCP-1781628068475\nCustomer: SEAN UTRERA\nAmount: ₱27,733.33\nThank you for your stay!', 'Sent', NULL, '2026-06-16 16:47:17'),
(8, 44, NULL, 'SMS', '09811741641', 'Receipt RCP-1781629555497', 'Sync Suites Hotel\nReceipt: RCP-1781629555497\nCustomer: SEAN UTRERA\nAmount: ₱27,733.33\nThank you for your stay!', 'Sent', NULL, '2026-06-16 17:06:38'),
(9, 44, NULL, 'SMS', '09811741641', 'Receipt RCP-1781629555497', 'Sync Suites Hotel\nReceipt: RCP-1781629555497\nCustomer: SEAN UTRERA\nAmount: ₱27,733.33\nThank you for your stay!', 'Sent', NULL, '2026-06-16 17:06:47'),
(10, 44, NULL, 'SMS', '09811741641', 'Receipt RCP-1781629555497', 'Sync Suites Hotel\nReceipt: RCP-1781629555497\nCustomer: SEAN UTRERA\nAmount: ₱27,733.33\nThank you for your stay!', 'Sent', NULL, '2026-06-16 17:06:54');

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
(1, 43, 32, 12420.00, 'QR PH', 'QRPH-1781626002812-43', NULL, 'Pending', NULL, '2026-06-16 16:06:42'),
(2, 44, 28, 27733.33, 'QR PH', 'QRPH-1781628034445-44', NULL, 'Pending', NULL, '2026-06-16 16:40:34'),
(3, 44, 28, 27733.33, 'QR PH', 'QRPH-1781628107700-44', NULL, 'Pending', NULL, '2026-06-16 16:41:47'),
(4, 44, 28, 27733.33, 'QR PH', 'QRPH-1781629603830-44', NULL, 'Pending', NULL, '2026-06-16 17:06:43');

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
(1, 42, 31, 'RCP-1781625756784', '╔══════════════════════════════════════════╗\n║         SYNC SUITES HOTEL                ║\n║      Official Payment Receipt            ║\n╚══════════════════════════════════════════╝\n\nReceipt No: RCP-1781625756784\nDate: 2026-06-17 00:02:36\nBooking ID: 42\n\n----------------------------------------\nCUSTOMER INFORMATION\n----------------------------------------\nName:    BOGARD SENTENO\nPhone:   09827366646\nEmail:   bograd@gmail.com\n\n----------------------------------------\nBOOKING DETAILS\n----------------------------------------\nRoom:        102 (Standard Room)\nRate/Day:    ₱2,500.00\nCheck-In:    2026-05-18\nCheck-Out:   2026-05-21\nStatus:      Reserved\nPayment:     GCash/QR Scan\n\n----------------------------------------\nGUEST BREAKDOWN\n----------------------------------------\nAdults:      2\nSeniors/PWD: 0 (20% discount applied)\nKids:        0 (Free)\n\n----------------------------------------\nCHARGES\n----------------------------------------\nBase Amount:              ₱  7,200.00\n----------------------------------------\nTOTAL AMOUNT:             ₱  7,200.00\n----------------------------------------\n\nThank you for choosing Sync Suites Hotel!\nFor inquiries: info@syncsuites.com\nPhone: +63 912 345 6789\n\nTHIS IS AN OFFICIAL RECEIPT\nKeep this for your records.\n', 7200.00, 'admin', '2026-06-16 16:02:36', NULL, NULL, NULL, NULL),
(2, 43, 32, 'RCP-1781625907428', '╔══════════════════════════════════════════╗\n║         SYNC SUITES HOTEL                ║\n║      Official Payment Receipt            ║\n╚══════════════════════════════════════════╝\n\nReceipt No: RCP-1781625907428\nDate: 2026-06-17 00:05:07\nBooking ID: 43\n\n----------------------------------------\nCUSTOMER\n----------------------------------------\nName:    HIRO ZENN\nPhone:   09811741641\nEmail:   hirozen@gmail.com\n\n----------------------------------------\nBOOKING DETAILS\n----------------------------------------\nRoom:        101 (Standard Room)\nRate/Day:    ₱2,500.00\nCheck-In:    2026-06-17\nCheck-Out:   2026-06-20\nStatus:      Reserved\nPayment:     Cash\n\n----------------------------------------\nGUESTS\n----------------------------------------\nAdults:      3\nSeniors/PWD: 2\nKids:        1\n\n----------------------------------------\nCHARGES\n----------------------------------------\nExtra Guest:              ₱  6,000.00\nSenior Discount:          -₱ 1,080.00\n----------------------------------------\nTOTAL:                    ₱ 12,420.00\n----------------------------------------\n\nThank you for choosing Sync Suites Hotel!\nTHIS IS AN OFFICIAL RECEIPT\n', 12420.00, 'admin', '2026-06-16 16:05:11', NULL, NULL, '09811741641', '2026-06-16 16:07:25'),
(3, 39, 28, 'RCP-1781628048981', '╔══════════════════════════════════════════╗\n║         SYNC SUITES HOTEL                ║\n║      Official Payment Receipt            ║\n╚══════════════════════════════════════════╝\n\nReceipt No: RCP-1781628048981\nDate: 2026-06-17 00:40:48\nBooking ID: 39\n\n----------------------------------------\nCUSTOMER INFORMATION\n----------------------------------------\nName:    SEAN UTRERA\nPhone:   09811741631\nEmail:   seantur@gmail.com\n\n----------------------------------------\nBOOKING DETAILS\n----------------------------------------\nRoom:        201 (Junior Suite)\nRate/Day:    ₱3,500.00\nCheck-In:    2026-05-18\nCheck-Out:   2026-05-19\nStatus:      Checked Out\nPayment:     Cash\n\n----------------------------------------\nGUEST BREAKDOWN\n----------------------------------------\nAdults:      2\nSeniors/PWD: 1 (20% discount applied)\nKids:        0 (Free)\n\n----------------------------------------\nCHARGES\n----------------------------------------\nBase Amount:              ₱  4,620.00\nSenior/PWD Discount:      -₱   700.00\n----------------------------------------\nTOTAL AMOUNT:             ₱  3,920.00\n----------------------------------------\n\nThank you for choosing Sync Suites Hotel!\nFor inquiries: info@syncsuites.com\nPhone: +63 912 345 6789\n\nTHIS IS AN OFFICIAL RECEIPT\nKeep this for your records.\n', 3920.00, 'admin', '2026-06-16 16:40:48', NULL, NULL, NULL, NULL),
(4, 44, 28, 'RCP-1781628068475', '╔══════════════════════════════════════════╗\n║         SYNC SUITES HOTEL                ║\n║      Official Payment Receipt            ║\n╚══════════════════════════════════════════╝\n\nReceipt No: RCP-1781628068475\nDate: 2026-06-17 00:41:08\nBooking ID: 44\n\n----------------------------------------\nCUSTOMER\n----------------------------------------\nName:    SEAN UTRERA\nPhone:   09811741631\nEmail:   seantur@gmail.com\n\n----------------------------------------\nBOOKING DETAILS\n----------------------------------------\nRoom:        401 (Presidential Suite)\nRate/Day:    ₱8,000.00\nCheck-In:    2026-06-17\nCheck-Out:   2026-06-21\nStatus:      Checked In\nPayment:     Cash\n\n----------------------------------------\nGUESTS\n----------------------------------------\nAdults:      1\nSeniors/PWD: 2\nKids:        2\n\n----------------------------------------\nCHARGES\n----------------------------------------\nSenior Discount:          -₱ 4,266.67\n----------------------------------------\nTOTAL:                    ₱ 27,733.33\n----------------------------------------\n\nThank you for choosing Sync Suites Hotel!\nTHIS IS AN OFFICIAL RECEIPT\n', 27733.33, 'admin', '2026-06-16 16:41:10', NULL, NULL, '09811741641', '2026-06-16 16:47:17'),
(5, 44, 28, 'RCP-1781629548353', '╔══════════════════════════════════════════╗\n║         SYNC SUITES HOTEL                ║\n║      Official Payment Receipt            ║\n╚══════════════════════════════════════════╝\n\nReceipt No: RCP-1781629548353\nDate: 2026-06-17 01:05:48\nBooking ID: 44\n\n----------------------------------------\nCUSTOMER INFORMATION\n----------------------------------------\nName:    SEAN UTRERA\nPhone:   09811741631\nEmail:   seantur@gmail.com\n\n----------------------------------------\nBOOKING DETAILS\n----------------------------------------\nRoom:        401 (Presidential Suite)\nRate/Day:    ₱8,000.00\nCheck-In:    2026-06-17\nCheck-Out:   2026-06-21\nStatus:      Checked In\nPayment:     Cash\n\n----------------------------------------\nGUEST BREAKDOWN\n----------------------------------------\nAdults:      1\nSeniors/PWD: 2 (20% discount applied)\nKids:        2 (Free)\n\n----------------------------------------\nCHARGES\n----------------------------------------\nBase Amount:              ₱ 32,000.00\nSenior/PWD Discount:      -₱ 4,266.67\n----------------------------------------\nTOTAL AMOUNT:             ₱ 27,733.33\n----------------------------------------\n\nThank you for choosing Sync Suites Hotel!\nFor inquiries: info@syncsuites.com\nPhone: +63 912 345 6789\n\nTHIS IS AN OFFICIAL RECEIPT\nKeep this for your records.\n', 27733.33, 'admin', '2026-06-16 17:05:48', NULL, NULL, NULL, NULL),
(6, 43, 32, 'RCP-1781629551410', '╔══════════════════════════════════════════╗\n║         SYNC SUITES HOTEL                ║\n║      Official Payment Receipt            ║\n╚══════════════════════════════════════════╝\n\nReceipt No: RCP-1781629551410\nDate: 2026-06-17 01:05:51\nBooking ID: 43\n\n----------------------------------------\nCUSTOMER INFORMATION\n----------------------------------------\nName:    HIRO ZENN\nPhone:   09811741641\nEmail:   hirozen@gmail.com\n\n----------------------------------------\nBOOKING DETAILS\n----------------------------------------\nRoom:        101 (Standard Room)\nRate/Day:    ₱2,500.00\nCheck-In:    2026-06-17\nCheck-Out:   2026-06-20\nStatus:      Checked In\nPayment:     Cash\n\n----------------------------------------\nGUEST BREAKDOWN\n----------------------------------------\nAdults:      3\nSeniors/PWD: 2 (20% discount applied)\nKids:        1 (Free)\n\n----------------------------------------\nCHARGES\n----------------------------------------\nBase Amount:              ₱  7,500.00\nExtra Guest Charge:       ₱  6,000.00\nSenior/PWD Discount:      -₱ 1,080.00\n----------------------------------------\nTOTAL AMOUNT:             ₱ 12,420.00\n----------------------------------------\n\nThank you for choosing Sync Suites Hotel!\nFor inquiries: info@syncsuites.com\nPhone: +63 912 345 6789\n\nTHIS IS AN OFFICIAL RECEIPT\nKeep this for your records.\n', 12420.00, 'admin', '2026-06-16 17:05:51', NULL, NULL, NULL, NULL),
(7, 44, 28, 'RCP-1781629555497', '╔══════════════════════════════════════════╗\n║         SYNC SUITES HOTEL                ║\n║      Official Payment Receipt            ║\n╚══════════════════════════════════════════╝\n\nReceipt No: RCP-1781629555497\nDate: 2026-06-17 01:05:55\nBooking ID: 44\n\n----------------------------------------\nCUSTOMER INFORMATION\n----------------------------------------\nName:    SEAN UTRERA\nPhone:   09811741631\nEmail:   seantur@gmail.com\n\n----------------------------------------\nBOOKING DETAILS\n----------------------------------------\nRoom:        401 (Presidential Suite)\nRate/Day:    ₱8,000.00\nCheck-In:    2026-06-17\nCheck-Out:   2026-06-21\nStatus:      Checked In\nPayment:     Cash\n\n----------------------------------------\nGUEST BREAKDOWN\n----------------------------------------\nAdults:      1\nSeniors/PWD: 2 (20% discount applied)\nKids:        2 (Free)\n\n----------------------------------------\nCHARGES\n----------------------------------------\nBase Amount:              ₱ 32,000.00\nSenior/PWD Discount:      -₱ 4,266.67\n----------------------------------------\nTOTAL AMOUNT:             ₱ 27,733.33\n----------------------------------------\n\nThank you for choosing Sync Suites Hotel!\nFor inquiries: info@syncsuites.com\nPhone: +63 912 345 6789\n\nTHIS IS AN OFFICIAL RECEIPT\nKeep this for your records.\n', 27733.33, 'admin', '2026-06-16 17:05:55', NULL, NULL, '09811741641', '2026-06-16 17:06:54');

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
(1, '101', 1, 1, 0, 0),
(2, '102', 1, 1, 0, 0),
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
(22, '401', 4, 4, 0, 0),
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
(2, 'hotel_address', '123 Main Street, City, Philippines', 'Hotel address', '2026-06-16 14:58:50'),
(3, 'hotel_phone', '+63 912 345 6789', 'Hotel contact number', '2026-06-16 14:58:50'),
(4, 'hotel_email', 'info@syncsuites.com', 'Hotel email', '2026-06-16 14:58:50'),
(5, 'senior_discount_rate', '0.20', 'Senior/PWD discount percentage', '2026-06-16 14:58:50'),
(6, 'max_advance_booking_days', '90', 'Maximum days allowed for advance booking', '2026-06-16 14:58:50'),
(7, 'check_in_time', '14:00', 'Standard check-in time', '2026-06-16 14:58:50'),
(8, 'check_out_time', '12:00', 'Standard check-out time', '2026-06-16 14:58:50'),
(9, 'smtp_host', 'yrenyer45@gmail.com', 'SMTP server for emails', '2026-06-16 16:44:05'),
(10, 'smtp_port', '587', 'SMTP port', '2026-06-16 14:58:50'),
(11, 'smtp_username', '', 'SMTP username', '2026-06-16 14:58:50'),
(12, 'smtp_password', '', 'SMTP password', '2026-06-16 14:58:50'),
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
  MODIFY `archive_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT for table `audit_log`
--
ALTER TABLE `audit_log`
  MODIFY `audit_id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `bookings`
--
ALTER TABLE `bookings`
  MODIFY `booking_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=45;

--
-- AUTO_INCREMENT for table `customers`
--
ALTER TABLE `customers`
  MODIFY `customer_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=33;

--
-- AUTO_INCREMENT for table `notification_log`
--
ALTER TABLE `notification_log`
  MODIFY `log_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;

--
-- AUTO_INCREMENT for table `payment_transactions`
--
ALTER TABLE `payment_transactions`
  MODIFY `transaction_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT for table `receipts`
--
ALTER TABLE `receipts`
  MODIFY `receipt_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;

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
  MODIFY `user_id` int(11) NOT NULL AUTO_INCREMENT;

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
