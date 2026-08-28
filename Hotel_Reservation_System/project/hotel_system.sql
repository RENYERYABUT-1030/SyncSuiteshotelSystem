-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Aug 28, 2026 at 12:55 PM
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
-- Table structure for table `archived_bookings`
--

CREATE TABLE `archived_bookings` (
  `archive_id` int(11) NOT NULL,
  `original_booking_id` int(11) NOT NULL,
  `customer_id` int(11) NOT NULL,
  `room_id` int(11) NOT NULL,
  `check_in_date` date DEFAULT NULL,
  `check_out_date` date DEFAULT NULL,
  `total_amount` decimal(10,2) DEFAULT NULL,
  `payment_method` varchar(50) DEFAULT NULL,
  `status` varchar(30) DEFAULT NULL,
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
  `log_id` int(11) NOT NULL,
  `timestamp` timestamp NOT NULL DEFAULT current_timestamp(),
  `user_type` varchar(30) DEFAULT NULL,
  `user_name` varchar(100) DEFAULT NULL,
  `action` varchar(100) DEFAULT NULL,
  `details` varchar(500) DEFAULT NULL
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
  `total_amount` decimal(10,2) NOT NULL,
  `payment_method` varchar(50) DEFAULT NULL,
  `status` varchar(30) NOT NULL DEFAULT 'Reserved',
  `adults` int(11) DEFAULT 1,
  `seniors` int(11) DEFAULT 0,
  `kids` int(11) DEFAULT 0,
  `senior_discount` decimal(10,2) DEFAULT 0.00,
  `extra_guest_charge` decimal(10,2) DEFAULT 0.00,
  `reschedule_charge` decimal(10,2) DEFAULT 0.00,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `bookings`
--

INSERT INTO `bookings` (`booking_id`, `customer_id`, `room_id`, `check_in_date`, `check_out_date`, `total_amount`, `payment_method`, `status`, `adults`, `seniors`, `kids`, `senior_discount`, `extra_guest_charge`, `reschedule_charge`, `created_at`) VALUES
(1, 1, 1, '2026-08-28', '2026-09-01', 14400.00, 'GCash/QR Scan', 'Reserved', 1, 1, 3, 1600.00, 6000.00, 0.00, '2026-08-28 08:29:30');

-- --------------------------------------------------------

--
-- Table structure for table `customers`
--

CREATE TABLE `customers` (
  `customer_id` int(11) NOT NULL,
  `first_name` varchar(100) NOT NULL,
  `last_name` varchar(100) NOT NULL,
  `phone_number` varchar(20) DEFAULT NULL,
  `email` varchar(150) DEFAULT NULL,
  `total_visits` int(11) DEFAULT 0,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `customers`
--

INSERT INTO `customers` (`customer_id`, `first_name`, `last_name`, `phone_number`, `email`, `total_visits`, `created_at`) VALUES
(1, 'RENYER', 'YABUT', '09887474444', 'yabutrenyer30@gmail.com', 1, '2026-08-28 08:29:30');

-- --------------------------------------------------------

--
-- Table structure for table `notification_log`
--

CREATE TABLE `notification_log` (
  `notification_id` int(11) NOT NULL,
  `booking_id` int(11) DEFAULT NULL,
  `notification_type` varchar(30) DEFAULT NULL,
  `recipient` varchar(150) DEFAULT NULL,
  `subject` varchar(255) DEFAULT NULL,
  `content` text DEFAULT NULL,
  `status` varchar(30) DEFAULT 'Sent',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `emailed_to` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `notification_log`
--

INSERT INTO `notification_log` (`notification_id`, `booking_id`, `notification_type`, `recipient`, `subject`, `content`, `status`, `created_at`, `emailed_to`) VALUES
(1, 1, 'SMS', '09811741641', 'Receipt RCP-1787905789460', 'Sync Suites Hotel\nReceipt: RCP-1787905789460\nCustomer: RENYER YABUT\nAmount: ₱14,400.00\nThank you for your stay!', 'Sent', '2026-08-28 08:30:42', NULL),
(2, 1, 'SMS', '09811741641', 'Receipt RCP-1787905789460', 'Sync Suites Hotel\nReceipt: RCP-1787905789460\nCustomer: RENYER YABUT\nAmount: ₱14,400.00\nThank you for your stay!', 'Sent', '2026-08-28 09:14:41', NULL),
(3, 1, 'Email', 'ryabut.1632@umak.edu.ph', 'Receipt RCP-1787905789460', 'Thank you for choosing Sync Suites Hotel! Your receipt is attached.', 'Sent', '2026-08-28 10:21:25', NULL),
(4, 1, 'Email', 'yabutrenyer30@gmail.com', 'Receipt RCP-1787905789460', 'Thank you for choosing Sync Suites Hotel! Your receipt is attached.', 'Sent', '2026-08-28 10:24:42', NULL),
(5, 1, 'Email', 'yabutrenyer30@gmail.com', 'Receipt RCP-1787905789460', 'Thank you for choosing Sync Suites Hotel! Your receipt is attached.', 'Sent', '2026-08-28 10:26:00', NULL),
(6, 1, 'Email', 'yabutrenyer30@gmail.com', 'Receipt RCP-1787905789460', 'Thank you for choosing Sync Suites Hotel! Your receipt is attached.', 'Sent', '2026-08-28 10:27:41', NULL),
(7, 1, 'Email', 'yabutrenyer30@gmail.com', 'Receipt RCP-1787905789460', 'Thank you for choosing Sync Suites Hotel! Your receipt is attached.', 'Sent', '2026-08-28 10:33:51', NULL),
(8, 1, 'Email', 'yabutrenyer30@gmail.com', 'Receipt RCP-1787905789460', 'Thank you for choosing Sync Suites Hotel! Your receipt is attached.', 'Sent', '2026-08-28 10:38:50', NULL),
(9, 1, 'Email', 'yabutrenyer30@gmail.com', 'Receipt RCP-1787905789460', 'Thank you for choosing Sync Suites Hotel! Your receipt is attached.', 'Sent', '2026-08-28 10:40:45', NULL),
(10, 1, 'Email', 'yabutrenyer30@gmail.com', 'Receipt RCP-1787905789460', 'Thank you for choosing Sync Suites Hotel! Your receipt is attached.', 'Sent', '2026-08-28 10:44:41', NULL);

-- --------------------------------------------------------

--
-- Table structure for table `payment_transactions`
--

CREATE TABLE `payment_transactions` (
  `transaction_id` int(11) NOT NULL,
  `booking_id` int(11) NOT NULL,
  `customer_id` int(11) NOT NULL,
  `amount` decimal(10,2) NOT NULL,
  `payment_method` varchar(50) DEFAULT NULL,
  `qr_reference` varchar(100) DEFAULT NULL,
  `transaction_status` varchar(30) DEFAULT 'Pending',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `payment_transactions`
--

INSERT INTO `payment_transactions` (`transaction_id`, `booking_id`, `customer_id`, `amount`, `payment_method`, `qr_reference`, `transaction_status`, `created_at`) VALUES
(1, 1, 1, 14400.00, 'QR PH', 'QRPH-1787906000796-1', 'Pending', '2026-08-28 08:33:20');

-- --------------------------------------------------------

--
-- Table structure for table `receipts`
--

CREATE TABLE `receipts` (
  `receipt_id` int(11) NOT NULL,
  `booking_id` int(11) NOT NULL,
  `customer_id` int(11) NOT NULL,
  `receipt_number` varchar(50) NOT NULL,
  `receipt_data` text DEFAULT NULL,
  `total_amount` decimal(10,2) DEFAULT NULL,
  `printed_by` varchar(100) DEFAULT NULL,
  `printed_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `emailed_to` varchar(255) DEFAULT NULL,
  `emailed_at` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `receipts`
--

INSERT INTO `receipts` (`receipt_id`, `booking_id`, `customer_id`, `receipt_number`, `receipt_data`, `total_amount`, `printed_by`, `printed_at`, `emailed_to`, `emailed_at`) VALUES
(1, 1, 1, 'RCP-1787905789460', '╔══════════════════════════════════════════╗\n║         SYNC SUITES HOTEL                ║\n║      Official Payment Receipt            ║\n╚══════════════════════════════════════════╝\n\nReceipt No: RCP-1787905789460\nDate: 2026-08-28 16:29:49\nBooking ID: 1\n\n----------------------------------------\nCUSTOMER INFORMATION\n----------------------------------------\nName:    RENYER YABUT\nPhone:   09887474444\nEmail:   yabutrenyer30@gmail.com\n\n----------------------------------------\nBOOKING DETAILS\n----------------------------------------\nRoom:        101 (Standard Room)\nRate/Day:    ₱2,500.00\nCheck-In:    2026-08-28\nCheck-Out:   2026-09-01\nStatus:      Reserved\nPayment:     GCash/QR Scan\n\n----------------------------------------\nGUEST BREAKDOWN\n----------------------------------------\nAdults:      1\nSeniors/PWD: 1 (20% discount applied)\nKids:        3 (Free)\n\n----------------------------------------\nCHARGES\n----------------------------------------\nBase Amount:              ₱ 10,000.00\nExtra Guest Charge:       ₱  6,000.00\nSenior/PWD Discount:      -₱ 1,600.00\n----------------------------------------\nTOTAL AMOUNT:             ₱ 14,400.00\n----------------------------------------\n\nThank you for choosing Sync Suites Hotel!\nFor inquiries: info@syncsuites.com\nPhone: +63 912 345 6789\n\nTHIS IS AN OFFICIAL RECEIPT\nKeep this for your records.\n', 14400.00, 'System', '2026-08-28 08:29:49', 'yabutrenyer30@gmail.com', '2026-08-28 18:44:41');

-- --------------------------------------------------------

--
-- Table structure for table `rooms`
--

CREATE TABLE `rooms` (
  `room_id` int(11) NOT NULL,
  `room_number` varchar(10) NOT NULL,
  `room_type_id` int(11) NOT NULL,
  `floor_number` int(11) NOT NULL,
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
(4, '201', 2, 2, 1, 0),
(5, '202', 2, 2, 1, 0),
(6, '301', 3, 3, 1, 0),
(7, '401', 4, 4, 1, 0);

-- --------------------------------------------------------

--
-- Table structure for table `room_types`
--

CREATE TABLE `room_types` (
  `room_type_id` int(11) NOT NULL,
  `type_name` varchar(100) NOT NULL,
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
  `setting_key` varchar(100) NOT NULL,
  `setting_value` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `system_settings`
--

INSERT INTO `system_settings` (`setting_key`, `setting_value`) VALUES
('smtp_host', 'smtp.gmail.com'),
('smtp_password', 'fokczhcuwtlgsfyo'),
('smtp_port', '587'),
('smtp_username', 'syncsuiteshotel@gmail.com');

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `user_id` int(11) NOT NULL,
  `username` varchar(50) NOT NULL,
  `password_hash` varchar(255) NOT NULL,
  `email` varchar(150) DEFAULT NULL,
  `user_type` enum('admin','customer') NOT NULL,
  `customer_id` int(11) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`user_id`, `username`, `password_hash`, `email`, `user_type`, `customer_id`, `created_at`) VALUES
(1, 'admin', '$2a$12$OmJnOescwdO/7GVWa6KPE.s5UhcksyWCyG8iWjNC8jBHAXo9vUlEi', NULL, 'admin', NULL, '2026-08-28 05:50:53');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `archived_bookings`
--
ALTER TABLE `archived_bookings`
  ADD PRIMARY KEY (`archive_id`);

--
-- Indexes for table `audit_log`
--
ALTER TABLE `audit_log`
  ADD PRIMARY KEY (`log_id`);

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
  ADD PRIMARY KEY (`notification_id`),
  ADD KEY `booking_id` (`booking_id`);

--
-- Indexes for table `payment_transactions`
--
ALTER TABLE `payment_transactions`
  ADD PRIMARY KEY (`transaction_id`),
  ADD KEY `booking_id` (`booking_id`),
  ADD KEY `customer_id` (`customer_id`);

--
-- Indexes for table `receipts`
--
ALTER TABLE `receipts`
  ADD PRIMARY KEY (`receipt_id`),
  ADD KEY `booking_id` (`booking_id`),
  ADD KEY `customer_id` (`customer_id`);

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
  ADD PRIMARY KEY (`setting_key`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`user_id`),
  ADD UNIQUE KEY `username` (`username`),
  ADD KEY `customer_id` (`customer_id`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `archived_bookings`
--
ALTER TABLE `archived_bookings`
  MODIFY `archive_id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `audit_log`
--
ALTER TABLE `audit_log`
  MODIFY `log_id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `bookings`
--
ALTER TABLE `bookings`
  MODIFY `booking_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT for table `customers`
--
ALTER TABLE `customers`
  MODIFY `customer_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT for table `notification_log`
--
ALTER TABLE `notification_log`
  MODIFY `notification_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;

--
-- AUTO_INCREMENT for table `payment_transactions`
--
ALTER TABLE `payment_transactions`
  MODIFY `transaction_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT for table `receipts`
--
ALTER TABLE `receipts`
  MODIFY `receipt_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT for table `rooms`
--
ALTER TABLE `rooms`
  MODIFY `room_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;

--
-- AUTO_INCREMENT for table `room_types`
--
ALTER TABLE `room_types`
  MODIFY `room_type_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `user_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

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
-- Constraints for table `notification_log`
--
ALTER TABLE `notification_log`
  ADD CONSTRAINT `notification_log_ibfk_1` FOREIGN KEY (`booking_id`) REFERENCES `bookings` (`booking_id`);

--
-- Constraints for table `payment_transactions`
--
ALTER TABLE `payment_transactions`
  ADD CONSTRAINT `payment_transactions_ibfk_1` FOREIGN KEY (`booking_id`) REFERENCES `bookings` (`booking_id`),
  ADD CONSTRAINT `payment_transactions_ibfk_2` FOREIGN KEY (`customer_id`) REFERENCES `customers` (`customer_id`);

--
-- Constraints for table `receipts`
--
ALTER TABLE `receipts`
  ADD CONSTRAINT `receipts_ibfk_1` FOREIGN KEY (`booking_id`) REFERENCES `bookings` (`booking_id`),
  ADD CONSTRAINT `receipts_ibfk_2` FOREIGN KEY (`customer_id`) REFERENCES `customers` (`customer_id`);

--
-- Constraints for table `rooms`
--
ALTER TABLE `rooms`
  ADD CONSTRAINT `rooms_ibfk_1` FOREIGN KEY (`room_type_id`) REFERENCES `room_types` (`room_type_id`);

--
-- Constraints for table `users`
--
ALTER TABLE `users`
  ADD CONSTRAINT `users_ibfk_1` FOREIGN KEY (`customer_id`) REFERENCES `customers` (`customer_id`) ON DELETE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
