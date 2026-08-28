-- ═══════════════════════════════════════════════════════════════
-- Sync Suites Hotel Reservation System - Database Schema
-- Built to match exactly what LoginFrame.java, NewBookingPanel.java,
-- ManageBookingPanel.java, MaintenanceModePanel.java, ReportsPanel.java,
-- CustomerPortalFrame.java, RoomAvailabilityPanel.java, and AuditLogPanel.java
-- actually read/write.
-- ═══════════════════════════════════════════════════════════════

CREATE DATABASE IF NOT EXISTS hotel_system;
USE hotel_system;

-- ───────────────────────────────────────────────────────────────
-- CUSTOMERS
-- ───────────────────────────────────────────────────────────────
CREATE TABLE customers (
    customer_id     INT AUTO_INCREMENT PRIMARY KEY,
    first_name      VARCHAR(100) NOT NULL,
    last_name       VARCHAR(100) NOT NULL,
    phone_number    VARCHAR(20),
    email           VARCHAR(150),
    total_visits    INT DEFAULT 0,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ───────────────────────────────────────────────────────────────
-- USERS (login accounts — admin and customer)
-- Matches LoginFrame.java's query exactly:
--   SELECT user_id, username, password_hash, user_type, customer_id FROM users WHERE username = ?
-- ───────────────────────────────────────────────────────────────
CREATE TABLE users (
    user_id         INT AUTO_INCREMENT PRIMARY KEY,
    username        VARCHAR(50) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    email           VARCHAR(150),
    user_type       ENUM('admin', 'customer') NOT NULL,
    customer_id     INT NULL,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id) ON DELETE CASCADE
);

-- ───────────────────────────────────────────────────────────────
-- ROOM TYPES
-- ───────────────────────────────────────────────────────────────
CREATE TABLE room_types (
    room_type_id    INT AUTO_INCREMENT PRIMARY KEY,
    type_name       VARCHAR(100) NOT NULL,
    rate_per_day    DECIMAL(10,2) NOT NULL
);

-- ───────────────────────────────────────────────────────────────
-- ROOMS
-- ───────────────────────────────────────────────────────────────
CREATE TABLE rooms (
    room_id         INT AUTO_INCREMENT PRIMARY KEY,
    room_number     VARCHAR(10) NOT NULL UNIQUE,
    room_type_id    INT NOT NULL,
    floor_number    INT NOT NULL,
    is_available    BOOLEAN DEFAULT TRUE,
    is_maintenance  BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (room_type_id) REFERENCES room_types(room_type_id)
);

-- ───────────────────────────────────────────────────────────────
-- BOOKINGS
-- ───────────────────────────────────────────────────────────────
CREATE TABLE bookings (
    booking_id          INT AUTO_INCREMENT PRIMARY KEY,
    customer_id         INT NOT NULL,
    room_id             INT NOT NULL,
    check_in_date       DATE NOT NULL,
    check_out_date      DATE NOT NULL,
    total_amount        DECIMAL(10,2) NOT NULL,
    payment_method      VARCHAR(50),
    status              VARCHAR(30) NOT NULL DEFAULT 'Reserved',
    adults              INT DEFAULT 1,
    seniors             INT DEFAULT 0,
    kids                INT DEFAULT 0,
    senior_discount     DECIMAL(10,2) DEFAULT 0.00,
    extra_guest_charge  DECIMAL(10,2) DEFAULT 0.00,
    reschedule_charge   DECIMAL(10,2) DEFAULT 0.00,
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id),
    FOREIGN KEY (room_id) REFERENCES rooms(room_id)
);

-- ───────────────────────────────────────────────────────────────
-- ARCHIVED BOOKINGS (moved here when maintenance forces cancellation)
-- ───────────────────────────────────────────────────────────────
CREATE TABLE archived_bookings (
    archive_id            INT AUTO_INCREMENT PRIMARY KEY,
    original_booking_id   INT NOT NULL,
    customer_id           INT NOT NULL,
    room_id               INT NOT NULL,
    check_in_date         DATE,
    check_out_date        DATE,
    total_amount          DECIMAL(10,2),
    payment_method        VARCHAR(50),
    status                VARCHAR(30),
    adults                INT,
    seniors               INT,
    kids                  INT,
    senior_discount       DECIMAL(10,2),
    extra_guest_charge    DECIMAL(10,2),
    reschedule_charge     DECIMAL(10,2),
    archived_by           VARCHAR(100),
    archive_reason        VARCHAR(255),
    archived_at           TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ───────────────────────────────────────────────────────────────
-- RECEIPTS
-- ───────────────────────────────────────────────────────────────
CREATE TABLE receipts (
    receipt_id       INT AUTO_INCREMENT PRIMARY KEY,
    booking_id       INT NOT NULL,
    customer_id      INT NOT NULL,
    receipt_number   VARCHAR(50) NOT NULL,
    receipt_data     TEXT,
    total_amount     DECIMAL(10,2),
    printed_by       VARCHAR(100),
    printed_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (booking_id) REFERENCES bookings(booking_id),
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id)
);

-- ───────────────────────────────────────────────────────────────
-- PAYMENT TRANSACTIONS
-- ───────────────────────────────────────────────────────────────
CREATE TABLE payment_transactions (
    transaction_id      INT AUTO_INCREMENT PRIMARY KEY,
    booking_id           INT NOT NULL,
    customer_id           INT NOT NULL,
    amount                DECIMAL(10,2) NOT NULL,
    payment_method        VARCHAR(50),
    qr_reference          VARCHAR(100),
    transaction_status    VARCHAR(30) DEFAULT 'Pending',
    created_at            TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (booking_id) REFERENCES bookings(booking_id),
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id)
);

-- ───────────────────────────────────────────────────────────────
-- NOTIFICATION LOG (SMS/email confirmations)
-- ───────────────────────────────────────────────────────────────
CREATE TABLE notification_log (
    notification_id    INT AUTO_INCREMENT PRIMARY KEY,
    booking_id          INT,
    notification_type   VARCHAR(30),
    recipient            VARCHAR(150),
    subject               VARCHAR(255),
    content               TEXT,
    status                VARCHAR(30) DEFAULT 'Sent',
    created_at            TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (booking_id) REFERENCES bookings(booking_id)
);

-- ───────────────────────────────────────────────────────────────
-- AUDIT LOG
-- ───────────────────────────────────────────────────────────────
CREATE TABLE audit_log (
    log_id       INT AUTO_INCREMENT PRIMARY KEY,
    timestamp    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    user_type    VARCHAR(30),
    user_name    VARCHAR(100),
    action       VARCHAR(100),
    details      VARCHAR(500)
);

-- ───────────────────────────────────────────────────────────────
-- SYSTEM SETTINGS
-- ───────────────────────────────────────────────────────────────
CREATE TABLE system_settings (
    setting_key     VARCHAR(100) PRIMARY KEY,
    setting_value   VARCHAR(255)
);

-- ═══════════════════════════════════════════════════════════════
-- SEED DATA
-- ═══════════════════════════════════════════════════════════════

-- Room types (matches the prices already hardcoded in NewBookingPanel.java / CustomerPortalFrame.java)
INSERT INTO room_types (type_name, rate_per_day) VALUES
    ('Standard Room', 2500.00),
    ('Junior Suite', 3500.00),
    ('Executive Suite', 5000.00),
    ('Presidential Suite', 8000.00);

-- A few sample rooms
INSERT INTO rooms (room_number, room_type_id, floor_number) VALUES
    ('101', 1, 1), ('102', 1, 1), ('103', 1, 1),
    ('201', 2, 2), ('202', 2, 2),
    ('301', 3, 3),
    ('401', 4, 4);

-- ═══════════════════════════════════════════════════════════════
-- ADMIN ACCOUNT
--
-- IMPORTANT: You need a REAL BCrypt hash here - I cannot generate one for
-- you from where I'm running (no internet access, no BCrypt library
-- available to me), so do NOT trust a hash pasted in by an AI unless you
-- generated it yourself. Instead:
--
--   1. Run HashGenerator.java once in your project (right-click -> Run As
--      -> Java Application in Eclipse, or click Run in VS Code)
--   2. It will print a real hash to the console
--   3. Copy that hash and paste it below, replacing REPLACE_WITH_REAL_HASH
--   4. Run this INSERT statement
--   5. You can delete HashGenerator.java afterward
--
-- Default login this generates: username = admin, password = Admin123!
-- (change the password inside HashGenerator.java before running it if
-- you want a different one)
-- ═══════════════════════════════════════════════════════════════
INSERT INTO users (username, password_hash, user_type, customer_id) VALUES
    ('admin', 'REPLACE_WITH_REAL_HASH', 'admin', NULL);
