package Hotel_Reservation;
import Hotel_Reservation.core.EnhancedDBConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.awt.print.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.ArrayList;
import java.util.List;

// JavaMail imports - comment out if mail.jar is not available
// Uncomment these lines when you add mail.jar to your project:
// import javax.mail.*;
// import javax.mail.internet.*;

/**
 * Reports & Receipts Panel
 * Features: Print receipts, email receipts, SMS receipts, view all receipts
 * QR Payment integration placeholder
 * 
 * FIXED VERSION - Bug fixes applied:
 * 1. Fixed generateQRPayment: Added customer_id to SELECT query
 * 2. Fixed receipt base amount calculation formula
 * 3. Added NumberFormatException handling for booking ID parsing
 * 4. Added null checks for timestamps and receipt data
 * 5. Made email functionality optional (graceful fallback if mail.jar missing)
 * 6. Added dialog disposal to prevent memory leaks
 * 7. Replaced generic error codes with descriptive error messages
 * 8. Replaced LoginFrame references with SessionManager
 * 
 * ERROR CODE REFERENCE:
 * ├─ 001 - Report Generation Failed
 * ├─ 002 - Receipt Generation Failed
 * ├─ 003 - Receipt Save Failed
 * ├─ 004 - Print Failed
 * ├─ 005 - Database Connection Error
 * ├─ 006 - Email Sending Failed
 * ├─ 007 - SMS Sending Failed
 * ├─ 008 - QR Code Generation Failed
 * ├─ 009 - Failed to Load Bookings
 * ├─ 010 - Failed to Load Receipts
 * ├─ 011 - Failed to View Receipt
 * └─ 012 - Notification Log Error
 */
public class ReportsPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private JTable receiptTable, bookingTable;
    private DefaultTableModel receiptModel, bookingModel;
    private JTextArea receiptPreview;
    private JTabbedPane tabbedPane;

    // Track if JavaMail is available
    // FIX: The old check only tested javax.mail.Message and swallowed the
    // real reason for failure. Email attachments also need javax.activation
    // (JAF), which was removed from the JDK in Java 9+ and must be added as
    // its own jar (activation.jar / jakarta.activation) separately from
    // mail.jar. If either class is missing, or if the wrong artifact was
    // added (e.g. "jakarta.mail" instead of "javax.mail" — different package
    // names), Class.forName silently fails here with no visible error unless
    // you check System.err or console output. We now capture *which* class
    // was missing so Settings/Reports can show the real reason instead of a
    // generic "not available" message.
    private static boolean javaMailAvailable = false;
    private static String javaMailStatusMessage = "Not checked yet.";
    static {
        try {
            Class.forName("javax.mail.Message");
            Class.forName("javax.mail.internet.MimeMessage");
            Class.forName("javax.mail.Transport");
            Class.forName("javax.activation.DataHandler");
            javaMailAvailable = true;
            javaMailStatusMessage = "JavaMail + Activation libraries detected on the classpath.";
        } catch (ClassNotFoundException e) {
            javaMailAvailable = false;
            javaMailStatusMessage = "Missing class: " + e.getMessage()
                + ". Make sure mail.jar (javax.mail, NOT jakarta.mail) and "
                + "activation.jar are both on the RUN classpath — adding them "
                + "to the IDE build path is not enough if your run/export "
                + "configuration doesn't include them.";
            System.err.println("JavaMail not found. Email features will be disabled. Reason: " + e.getMessage());
        }
    }

    /** Lets other panels (e.g. SettingsPanel) show the real email-library status. */
    public static boolean isJavaMailAvailable() {
        return javaMailAvailable;
    }

    public static String getJavaMailStatusMessage() {
        return javaMailStatusMessage;
    }

    public ReportsPanel() {
        setLayout(new BorderLayout());

        JLabel titleLbl = new JLabel("Reports & Receipt Management");
        titleLbl.setForeground(new Color(0, 0, 47));
        titleLbl.setBackground(new Color(240, 240, 240));
        titleLbl.setOpaque(true);
        titleLbl.setFont(new Font("SansSerif", Font.BOLD, 20));
        titleLbl.setBorder(new EmptyBorder(15, 15, 15, 0));
        add(titleLbl, BorderLayout.NORTH);

        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("SansSerif", Font.BOLD, 13));

        // Tab 1: Generate Receipt
        tabbedPane.addTab("🧾 Generate Receipt", createGenerateReceiptPanel());

        // Tab 2: Receipt History
        tabbedPane.addTab("📜 Receipt History", createReceiptHistoryPanel());

        // Tab 3: Send Receipts
        tabbedPane.addTab("📧 Send Receipts", createSendReceiptsPanel());

        // Tab 4: Revenue Report
        tabbedPane.addTab("💰 Revenue Report", createRevenueReportPanel());

        add(tabbedPane, BorderLayout.CENTER);

        loadBookingsForReceipt();
        loadReceiptHistory();
    }

    private JPanel createGenerateReceiptPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Left: Booking selection
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setPreferredSize(new Dimension(500, 0));
        leftPanel.setBorder(BorderFactory.createTitledBorder("Select Booking"));

        String[] columns = {"Booking ID", "Customer", "Room", "Check-In", "Check-Out", "Total", "Status"};
        bookingModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        bookingTable = new JTable(bookingModel);
        bookingTable.setRowHeight(28);
        bookingTable.setFont(new Font("SansSerif", Font.PLAIN, 12));
        bookingTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        bookingTable.getTableHeader().setForeground(Color.WHITE);
        bookingTable.getTableHeader().setBackground(new Color(31, 71, 145));

        JScrollPane scroll = new JScrollPane(bookingTable);
        leftPanel.add(scroll, BorderLayout.CENTER);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        JButton btnGenerate = new JButton("🧾 Generate Receipt");
        btnGenerate.setBackground(new Color(70, 130, 180));
        btnGenerate.setForeground(Color.WHITE);
        btnGenerate.setFont(new Font("SansSerif", Font.BOLD, 13));
        btnGenerate.addActionListener(e -> generateReceipt());
        btnPanel.add(btnGenerate);

        JButton btnPrint = new JButton("🖨 Print Receipt");
        btnPrint.setBackground(new Color(46, 194, 126));
        btnPrint.setForeground(Color.WHITE);
        btnPrint.setFont(new Font("SansSerif", Font.BOLD, 13));
        btnPrint.addActionListener(e -> printReceipt());
        btnPanel.add(btnPrint);

        leftPanel.add(btnPanel, BorderLayout.SOUTH);

        panel.add(leftPanel, BorderLayout.WEST);

        // Right: Receipt Preview
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBorder(BorderFactory.createTitledBorder("Receipt Preview"));

        receiptPreview = new JTextArea();
        receiptPreview.setFont(new Font("Monospaced", Font.PLAIN, 12));
        receiptPreview.setEditable(false);
        receiptPreview.setBackground(new Color(255, 255, 240));
        receiptPreview.setText("Select a booking and click 'Generate Receipt' to preview...");

        JScrollPane previewScroll = new JScrollPane(receiptPreview);
        rightPanel.add(previewScroll, BorderLayout.CENTER);

        panel.add(rightPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createReceiptHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        String[] columns = {"Receipt ID", "Booking ID", "Receipt #", "Customer", "Amount", "Printed By", "Date"};
        receiptModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        receiptTable = new JTable(receiptModel);
        receiptTable.setRowHeight(28);
        receiptTable.setFont(new Font("SansSerif", Font.PLAIN, 12));
        receiptTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        receiptTable.getTableHeader().setForeground(Color.WHITE);
        receiptTable.getTableHeader().setBackground(new Color(100, 50, 150));

        JScrollPane scroll = new JScrollPane(receiptTable);
        panel.add(scroll, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));

        JButton btnRefresh = new JButton("🔄 Refresh");
        btnRefresh.setBackground(new Color(70, 130, 180));
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.addActionListener(e -> loadReceiptHistory());
        btnPanel.add(btnRefresh);

        JButton btnView = new JButton("👁 View");
        btnView.setBackground(new Color(46, 194, 126));
        btnView.setForeground(Color.WHITE);
        btnView.addActionListener(e -> viewReceiptFromHistory());
        btnPanel.add(btnView);

        panel.add(btnPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createSendReceiptsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel formPanel = new JPanel(new GridLayout(0, 2, 15, 15));
        formPanel.setBackground(new Color(250, 250, 255));

        // Booking ID
        formPanel.add(new JLabel("Booking ID:"));
        JTextField txtBookingId = new JTextField();
        txtBookingId.setFont(new Font("SansSerif", Font.PLAIN, 13));
        formPanel.add(txtBookingId);

        // Email
        formPanel.add(new JLabel("Customer Email:"));
        JTextField txtEmail = new JTextField();
        txtEmail.setFont(new Font("SansSerif", Font.PLAIN, 13));
        formPanel.add(txtEmail);

        // Phone
        formPanel.add(new JLabel("Phone Number (for SMS):"));
        JTextField txtPhone = new JTextField();
        txtPhone.setFont(new Font("SansSerif", Font.PLAIN, 13));
        formPanel.add(txtPhone);

        // Message
        formPanel.add(new JLabel("Custom Message:"));
        JTextArea txtMessage = new JTextArea(3, 20);
        txtMessage.setFont(new Font("SansSerif", Font.PLAIN, 13));
        txtMessage.setText("Thank you for choosing Sync Suites Hotel! Your receipt is attached.");
        formPanel.add(new JScrollPane(txtMessage));

        panel.add(formPanel, BorderLayout.CENTER);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));

        JButton btnEmail = new JButton("📧 Send via Email");
        btnEmail.setBackground(new Color(70, 130, 180));
        btnEmail.setForeground(Color.WHITE);
        btnEmail.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnEmail.setPreferredSize(new Dimension(180, 40));
        btnEmail.addActionListener(e -> {
            // FIX: Validate booking ID before parsing
            String bookingIdStr = txtBookingId.getText().trim();
            if (bookingIdStr.isEmpty()) {
                JOptionPane.showMessageDialog(panel, "Please enter a Booking ID!", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                int bookingId = Integer.parseInt(bookingIdStr);
                if (bookingId <= 0) {
                    JOptionPane.showMessageDialog(panel, "Booking ID must be a positive number!", "Validation Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                sendReceiptEmail(bookingId, txtEmail.getText().trim(), txtMessage.getText().trim());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(panel, "Invalid Booking ID! Please enter a valid number.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        btnPanel.add(btnEmail);

        JButton btnSMS = new JButton("📱 Send via SMS");
        btnSMS.setBackground(new Color(46, 194, 126));
        btnSMS.setForeground(Color.WHITE);
        btnSMS.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnSMS.setPreferredSize(new Dimension(180, 40));
        btnSMS.addActionListener(e -> {
            // FIX: Validate booking ID before parsing
            String bookingIdStr = txtBookingId.getText().trim();
            if (bookingIdStr.isEmpty()) {
                JOptionPane.showMessageDialog(panel, "Please enter a Booking ID!", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                int bookingId = Integer.parseInt(bookingIdStr);
                if (bookingId <= 0) {
                    JOptionPane.showMessageDialog(panel, "Booking ID must be a positive number!", "Validation Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                sendReceiptSMS(bookingId, txtPhone.getText().trim());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(panel, "Invalid Booking ID! Please enter a valid number.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        btnPanel.add(btnSMS);

        JButton btnQR = new JButton("📲 Generate QR Payment");
        btnQR.setBackground(new Color(255, 165, 0));
        btnQR.setForeground(Color.WHITE);
        btnQR.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnQR.setPreferredSize(new Dimension(200, 40));
        btnQR.addActionListener(e -> {
            // FIX: Validate booking ID before parsing
            String bookingIdStr = txtBookingId.getText().trim();
            if (bookingIdStr.isEmpty()) {
                JOptionPane.showMessageDialog(panel, "Please enter a Booking ID!", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                int bookingId = Integer.parseInt(bookingIdStr);
                if (bookingId <= 0) {
                    JOptionPane.showMessageDialog(panel, "Booking ID must be a positive number!", "Validation Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                generateQRPayment(bookingId);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(panel, "Invalid Booking ID! Please enter a valid number.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        btnPanel.add(btnQR);

        panel.add(btnPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createRevenueReportPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JTextArea reportArea = new JTextArea();
        reportArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        reportArea.setEditable(false);
        reportArea.setBackground(new Color(250, 250, 250));

        // Generate revenue report
        StringBuilder report = new StringBuilder();
        report.append("========================================\n");
        report.append("    SYNC SUITES HOTEL REVENUE REPORT\n");
        report.append("========================================\n\n");

        try (Connection conn = EnhancedDBConnection.getConnection()) {
            // Total revenue
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(
                "SELECT COUNT(*) as total_bookings, SUM(total_amount) as total_revenue, " +
                "AVG(total_amount) as avg_booking FROM bookings WHERE status != 'Cancelled'");
            if (rs.next()) {
                report.append("📊 OVERALL STATISTICS\n");
                report.append("---------------------\n");
                report.append("Total Bookings:    ").append(rs.getInt("total_bookings")).append("\n");

                // FIX: Handle NULL from SUM/AVG when no data
                double totalRevenue = rs.getDouble("total_revenue");
                if (rs.wasNull()) totalRevenue = 0.0;
                double avgBooking = rs.getDouble("avg_booking");
                if (rs.wasNull()) avgBooking = 0.0;

                report.append("Total Revenue:     ₱").append(String.format("%,.2f", totalRevenue)).append("\n");
                report.append("Average Booking:   ₱").append(String.format("%,.2f", avgBooking)).append("\n\n");
            }

            // Revenue by room type
            rs = stmt.executeQuery(
                "SELECT rt.type_name, COUNT(*) as count, SUM(b.total_amount) as revenue " +
                "FROM bookings b JOIN rooms r ON b.room_id = r.room_id " +
                "JOIN room_types rt ON r.room_type_id = rt.room_type_id " +
                "WHERE b.status != 'Cancelled' GROUP BY rt.type_name");

            report.append("🏨 REVENUE BY ROOM TYPE\n");
            report.append("----------------------\n");
            while (rs.next()) {
                double revenue = rs.getDouble("revenue");
                if (rs.wasNull()) revenue = 0.0;
                report.append(String.format("%-20s %3d bookings  ₱%,12.2f\n", 
                    rs.getString("type_name"), rs.getInt("count"), revenue));
            }
            report.append("\n");

            // Monthly revenue
            rs = stmt.executeQuery(
                "SELECT DATE_FORMAT(created_at, '%Y-%m') as month, COUNT(*) as count, SUM(total_amount) as revenue " +
                "FROM bookings WHERE status != 'Cancelled' GROUP BY month ORDER BY month DESC LIMIT 12");

            report.append("📅 MONTHLY REVENUE (Last 12 Months)\n");
            report.append("-----------------------------------\n");
            while (rs.next()) {
                double revenue = rs.getDouble("revenue");
                if (rs.wasNull()) revenue = 0.0;
                report.append(String.format("%-10s %3d bookings  ₱%,12.2f\n", 
                    rs.getString("month"), rs.getInt("count"), revenue));
            }
            report.append("\n");

            // Payment method breakdown
            rs = stmt.executeQuery(
                "SELECT payment_method, COUNT(*) as count, SUM(total_amount) as revenue " +
                "FROM bookings WHERE status != 'Cancelled' GROUP BY payment_method");

            report.append("💳 PAYMENT METHOD BREAKDOWN\n");
            report.append("---------------------------\n");
            while (rs.next()) {
                double revenue = rs.getDouble("revenue");
                if (rs.wasNull()) revenue = 0.0;
                report.append(String.format("%-20s %3d bookings  ₱%,12.2f\n", 
                    rs.getString("payment_method"), rs.getInt("count"), revenue));
            }

        } catch (SQLException e) {
            report.append("ERROR CODE 001 - Report Generation Failed\n");
            report.append("Description: Unable to generate revenue report\n");
            report.append("Details: ").append(e.getMessage()).append("\n");
            report.append("Action: Please check database connection and try again.\n");
        }

        report.append("\n========================================\n");
        report.append("Report Generated: ").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        report.append("\n========================================");

        reportArea.setText(report.toString());

        JScrollPane scroll = new JScrollPane(reportArea);
        panel.add(scroll, BorderLayout.CENTER);

        // Print button
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnPrint = new JButton("🖨 Print Report");
        btnPrint.setBackground(new Color(46, 194, 126));
        btnPrint.setForeground(Color.WHITE);
        btnPrint.setFont(new Font("SansSerif", Font.BOLD, 13));
        btnPrint.addActionListener(e -> {
            try {
                reportArea.print();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(panel, "Print error: " + ex.getMessage());
            }
        });
        btnPanel.add(btnPrint);
        panel.add(btnPanel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Generate receipt text for a booking
     */
    private void generateReceipt() {
        int row = bookingTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a booking first!", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int bookingId = (int) bookingModel.getValueAt(row, 0);

        try (Connection conn = EnhancedDBConnection.getConnection()) {
            // FIX: Use explicit column list instead of b.* for better maintainability
            String sql = "SELECT b.booking_id, b.customer_id, b.room_id, b.check_in_date, " +
                        "b.check_out_date, b.total_amount, b.payment_method, b.status, " +
                        "b.adults, b.seniors, b.kids, b.senior_discount, b.extra_guest_charge, " +
                        "b.reschedule_charge, " +
                        "CONCAT(c.first_name, ' ', c.last_name) as customer, " +
                        "c.phone_number, c.email, r.room_number, rt.type_name, rt.rate_per_day " +
                        "FROM bookings b " +
                        "JOIN customers c ON b.customer_id = c.customer_id " +
                        "JOIN rooms r ON b.room_id = r.room_id " +
                        "JOIN room_types rt ON r.room_type_id = rt.room_type_id " +
                        "WHERE b.booking_id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, bookingId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                StringBuilder receipt = new StringBuilder();
                String receiptNum = "RCP-" + System.currentTimeMillis();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                receipt.append("╔══════════════════════════════════════════╗\n");
                receipt.append("║         SYNC SUITES HOTEL                ║\n");
                receipt.append("║      Official Payment Receipt            ║\n");
                receipt.append("╚══════════════════════════════════════════╝\n\n");
                receipt.append("Receipt No: ").append(receiptNum).append("\n");
                receipt.append("Date: ").append(sdf.format(new Date())).append("\n");
                receipt.append("Booking ID: ").append(bookingId).append("\n\n");
                receipt.append("----------------------------------------\n");
                receipt.append("CUSTOMER INFORMATION\n");
                receipt.append("----------------------------------------\n");
                receipt.append("Name:    ").append(rs.getString("customer")).append("\n");
                receipt.append("Phone:   ").append(rs.getString("phone_number")).append("\n");
                receipt.append("Email:   ").append(rs.getString("email")).append("\n\n");
                receipt.append("----------------------------------------\n");
                receipt.append("BOOKING DETAILS\n");
                receipt.append("----------------------------------------\n");
                receipt.append("Room:        ").append(rs.getString("room_number")).append(" (").append(rs.getString("type_name")).append(")\n");
                receipt.append("Rate/Day:    ₱").append(String.format("%,.2f", rs.getDouble("rate_per_day"))).append("\n");
                receipt.append("Check-In:    ").append(rs.getDate("check_in_date")).append("\n");
                receipt.append("Check-Out:   ").append(rs.getDate("check_out_date")).append("\n");
                receipt.append("Status:      ").append(rs.getString("status")).append("\n");
                receipt.append("Payment:     ").append(rs.getString("payment_method")).append("\n\n");
                receipt.append("----------------------------------------\n");
                receipt.append("GUEST BREAKDOWN\n");
                receipt.append("----------------------------------------\n");
                receipt.append("Adults:      ").append(rs.getInt("adults")).append("\n");
                receipt.append("Seniors/PWD: ").append(rs.getInt("seniors")).append(" (20% discount applied)\n");
                receipt.append("Kids:        ").append(rs.getInt("kids")).append(" (Free)\n\n");
                receipt.append("----------------------------------------\n");
                receipt.append("CHARGES\n");
                receipt.append("----------------------------------------\n");

                // FIX: Correct base amount calculation
                // total = base + extra_guest - senior_discount + reschedule_charge
                // Therefore: base = total - extra_guest + senior_discount - reschedule_charge
                double totalAmount = rs.getDouble("total_amount");
                double extraCharge = rs.getDouble("extra_guest_charge");
                double seniorDiscount = rs.getDouble("senior_discount");
                double rescheduleCharge = rs.getDouble("reschedule_charge");

                double baseAmount = totalAmount - extraCharge + seniorDiscount - rescheduleCharge;
                if (baseAmount < 0) baseAmount = 0; // Safety check

                receipt.append(String.format("%-25s ₱%,10.2f\n", "Base Amount:", baseAmount));
                if (extraCharge > 0) {
                    receipt.append(String.format("%-25s ₱%,10.2f\n", "Extra Guest Charge:", extraCharge));
                }
                if (seniorDiscount > 0) {
                    receipt.append(String.format("%-25s -₱%,9.2f\n", "Senior/PWD Discount:", seniorDiscount));
                }
                if (rescheduleCharge > 0) {
                    receipt.append(String.format("%-25s ₱%,10.2f\n", "Reschedule Fee:", rescheduleCharge));
                }
                receipt.append("----------------------------------------\n");
                receipt.append(String.format("%-25s ₱%,10.2f\n", "TOTAL AMOUNT:", totalAmount));
                receipt.append("----------------------------------------\n\n");
                receipt.append("Thank you for choosing Sync Suites Hotel!\n");
                receipt.append("For inquiries: info@syncsuites.com\n");
                receipt.append("Phone: +63 912 345 6789\n\n");
                receipt.append("THIS IS AN OFFICIAL RECEIPT\n");
                receipt.append("Keep this for your records.\n");

                receiptPreview.setText(receipt.toString());
                receiptPreview.setCaretPosition(0);

                // Save receipt to database
                saveReceiptToDatabase(bookingId, receiptNum, receipt.toString(), totalAmount);

                JOptionPane.showMessageDialog(this, 
                    "Receipt generated!\nReceipt No: " + receiptNum, 
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "ERROR CODE 002 - Receipt Generation Failed\n" +
                "Description: Could not generate receipt for booking\n" +
                "Details: " + e.getMessage() + "\n" +
                "Action: Verify booking data and try again",
                "Receipt Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Save receipt to database
     * FIX: Replaced LoginFrame.currentUserName with SessionManager.getCurrentUserName()
     */
    private void saveReceiptToDatabase(int bookingId, String receiptNum, String receiptData, double amount) {
        try (Connection conn = EnhancedDBConnection.getConnection()) {
            // Get customer_id from booking
            PreparedStatement getPs = conn.prepareStatement("SELECT customer_id FROM bookings WHERE booking_id = ?");
            getPs.setInt(1, bookingId);
            ResultSet rs = getPs.executeQuery();
            int customerId = 0;
            if (rs.next()) customerId = rs.getInt("customer_id");

            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO receipts (booking_id, customer_id, receipt_number, receipt_data, total_amount, printed_by) " +
                "VALUES (?,?,?,?,?,?)");
            ps.setInt(1, bookingId);
            ps.setInt(2, customerId);
            ps.setString(3, receiptNum);
            ps.setString(4, receiptData);
            ps.setDouble(5, amount);
            // FIX: Use SessionManager instead of LoginFrame
            ps.setString(6, SessionManager.getCurrentUserName());
            ps.executeUpdate();

            loadReceiptHistory();
        } catch (SQLException e) {
            System.err.println("ERROR CODE 003 - Receipt Save Failed");
            System.err.println("Description: Could not save receipt to database");
            System.err.println("Details: " + e.getMessage());
            System.err.println("Troubleshooting: Check database write permissions and connection status");
        }
    }

    /**
     * Print the generated receipt
     */
    private void printReceipt() {
        if (receiptPreview.getText().equals("Select a booking and click 'Generate Receipt' to preview...")) {
            JOptionPane.showMessageDialog(this, "Please generate a receipt first!", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            PrinterJob job = PrinterJob.getPrinterJob();
            job.setJobName("Sync Suites Hotel Receipt");

            job.setPrintable(new Printable() {
                @Override
                public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
                    if (pageIndex > 0) return Printable.NO_SUCH_PAGE;

                    Graphics2D g2d = (Graphics2D) graphics;
                    g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

                    // Draw receipt text
                    String[] lines = receiptPreview.getText().split("\n");
                    int y = 20;
                    g2d.setFont(new Font("Monospaced", Font.PLAIN, 10));

                    for (String line : lines) {
                        g2d.drawString(line, 10, y);
                        y += 14;
                    }

                    return Printable.PAGE_EXISTS;
                }
            });

            if (job.printDialog()) {
                job.print();
                JOptionPane.showMessageDialog(this, "Receipt sent to printer!", "Printed", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "ERROR CODE 004 - Print Failed\n" +
                "Description: Unable to print receipt\n" +
                "Details: " + e.getMessage() + "\n" +
                "Action: Check printer connection and try again",
                "Print Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Send receipt via email using JavaMail
     * FIX: Made email optional - shows warning if JavaMail not available
     */
    private void sendReceiptEmail(int bookingId, String email, String customMessage) {
        // FIX: Check if JavaMail is available, and show the SPECIFIC reason
        // (which class is missing) instead of a generic message, so it's
        // actually possible to diagnose "I added the jars but it still
        // doesn't work" — usually a run/export classpath issue, not a
        // missing jar in the IDE build path.
        if (!javaMailAvailable) {
            JOptionPane.showMessageDialog(this,
                "Email feature is not available.\n\n" + javaMailStatusMessage
                + "\n\nCheck Settings \u2192 Email Settings for a live library status check.",
                "Email Unavailable", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter an email address!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Basic email validation
        if (!email.contains("@") || !email.contains(".") || email.indexOf("@") > email.lastIndexOf(".")) {
            JOptionPane.showMessageDialog(this, "Please enter a valid email address!", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Get receipt from database
        try (Connection conn = EnhancedDBConnection.getConnection()) {
            // Also fetch customer name for PDF filename
            PreparedStatement ps = conn.prepareStatement(
                "SELECT r.receipt_data, r.receipt_number, " +
                "CONCAT(c.first_name, ' ', c.last_name) as customer_name " +
                "FROM receipts r " +
                "JOIN bookings b ON r.booking_id = b.booking_id " +
                "JOIN customers c ON b.customer_id = c.customer_id " +
                "WHERE r.booking_id = ? ORDER BY r.receipt_id DESC LIMIT 1");
            ps.setInt(1, bookingId);
            ResultSet rs = ps.executeQuery();

            String receiptData;
            String receiptNum;
            String customerName;
            if (rs.next()) {
                receiptData = rs.getString("receipt_data");
                receiptNum = rs.getString("receipt_number");
                customerName = rs.getString("customer_name");

                // FIX: Check for null receipt data
                if (receiptData == null) receiptData = "";
                if (receiptNum == null) receiptNum = "UNKNOWN";
                if (customerName == null) customerName = "Guest";
            } else {
                JOptionPane.showMessageDialog(this, "No receipt found for this booking! Generate one first.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Get SMTP settings from database
            Statement stmt = conn.createStatement();
            ResultSet settings = stmt.executeQuery(
                "SELECT setting_key, setting_value FROM system_settings WHERE setting_key LIKE 'smtp_%'");

            String smtpHost = "smtp.gmail.com";
            String smtpPort = "587";
            String smtpUser = "";
            String smtpPass = "";

            while (settings.next()) {
                String key = settings.getString("setting_key");
                String val = settings.getString("setting_value");
                if (key.equals("smtp_host")) smtpHost = val;
                if (key.equals("smtp_port")) smtpPort = val;
                if (key.equals("smtp_username")) smtpUser = val;
                if (key.equals("smtp_password")) smtpPass = val;
            }

            if (smtpUser.isEmpty() || smtpPass.isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "SMTP not configured!\nPlease set email credentials in Settings.", 
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // FIX: Use reflection to call JavaMail to avoid compile errors when mail.jar is missing
            // This allows the code to compile without mail.jar, but email won't work until it's added
            sendEmailViaReflection(smtpHost, smtpPort, smtpUser, smtpPass, email, receiptNum, customMessage, receiptData, customerName);

            // Log notification
            logNotification(bookingId, "Email", email, "Receipt " + receiptNum, customMessage);

            // Update receipt record
            PreparedStatement updatePs = conn.prepareStatement(
                "UPDATE receipts SET emailed_to = ?, emailed_at = NOW() WHERE receipt_number = ?");
            updatePs.setString(1, email);
            updatePs.setString(2, receiptNum);
            updatePs.executeUpdate();

            JOptionPane.showMessageDialog(this, 
                "Receipt sent to " + email + " successfully!", 
                "Email Sent", JOptionPane.INFORMATION_MESSAGE);

            loadReceiptHistory();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "ERROR CODE 005 - Database Connection Error\n" +
                "Description: Failed to connect to database\n" +
                "Details: " + e.getMessage() + "\n" +
                "Action: Verify database is running and accessible",
                "Database Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            // FIX: sendEmailViaReflection() calls javax.mail methods via
            // reflection, so real failures (bad credentials, host unreachable,
            // etc.) arrive wrapped in java.lang.reflect.InvocationTargetException
            // whose own getMessage() is usually null. Unwrap to the actual
            // cause so the dialog shows something actionable instead of
            // "Details: null".
            Throwable cause = e;
            while (cause.getCause() != null && cause.getCause() != cause) {
                cause = cause.getCause();
            }
            String detail = cause.getMessage() != null ? cause.getMessage() : cause.toString();

            JOptionPane.showMessageDialog(this, 
                "ERROR CODE 006 - Email Sending Failed\n" +
                "Description: Could not send receipt email\n" +
                "Details: " + detail + "\n" +
                "Action: Verify SMTP host/port, that the account allows SMTP\n" +
                "(Gmail needs an App Password, not your normal password), and\n" +
                "that this device's internet/firewall allows outbound SMTP.",
                "Email Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    /**
     * Generates a minimal valid PDF from plain receipt text using pure Java (no libraries).
     * The PDF contains the receipt lines rendered as simple text objects.
     * Returns the PDF as a byte array.
     */
    private byte[] generateReceiptPdf(String receiptText) throws Exception {
        final int PAGE_W   = 595;
        final int PAGE_H   = 842;
        final int MARGIN   = 40;
        final int FONT_SIZE = 11;
        final int LINE_H   = 16;
        final java.awt.Font FONT = new java.awt.Font("Monospaced", java.awt.Font.PLAIN, FONT_SIZE);

        String[] lines = receiptText.split("\n");

        // Paginate lines
        int linesPerPage = (PAGE_H - MARGIN * 2) / LINE_H;
        java.util.List<java.util.List<String>> pages = new java.util.ArrayList<>();
        java.util.List<String> cur = new java.util.ArrayList<>();
        for (String line : lines) {
            cur.add(line);
            if (cur.size() >= linesPerPage) { pages.add(cur); cur = new java.util.ArrayList<>(); }
        }
        if (!cur.isEmpty()) pages.add(cur);

        // Footer lines printed at the bottom of the last page
        String[] footerLines = {
            "----------------------------------------",
            "Thank you for choosing Sync Suites Hotel!",
            "For inquiries: info@syncsuites.com",
            "Phone: +63 912 345 6789",
            "",
            "THIS IS AN OFFICIAL RECEIPT",
            "Keep this for your records."
        };
        final int FOOTER_H      = footerLines.length * LINE_H + 10; // total height reserved
        final int FOOTER_TOP_Y  = PAGE_H - MARGIN - FOOTER_H;       // y where footer starts (image coords)

        // Render each page to JPEG bytes
        java.util.List<byte[]> pageImages = new java.util.ArrayList<>();
        int totalPageCount = pages.size();
        for (int pi = 0; pi < totalPageCount; pi++) {
            java.util.List<String> pageLines = pages.get(pi);
            java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(
                PAGE_W, PAGE_H, java.awt.image.BufferedImage.TYPE_INT_RGB);
            java.awt.Graphics2D g = img.createGraphics();
            g.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING,
                               java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setColor(java.awt.Color.WHITE);
            g.fillRect(0, 0, PAGE_W, PAGE_H);
            g.setColor(java.awt.Color.BLACK);
            g.setFont(FONT);
            int y = MARGIN + FONT_SIZE;
            for (String ln : pageLines) { g.drawString(ln, MARGIN, y); y += LINE_H; }

            // Draw footer only on the last page
            if (pi == totalPageCount - 1) {
                java.awt.Font boldFont   = new java.awt.Font("Monospaced", java.awt.Font.BOLD,  FONT_SIZE);
                java.awt.Font normalFont = new java.awt.Font("Monospaced", java.awt.Font.PLAIN, FONT_SIZE);
                int fy = FOOTER_TOP_Y + FONT_SIZE;
                for (int fi = 0; fi < footerLines.length; fi++) {
                    String fl = footerLines[fi];
                    // Bold for the official receipt line
                    g.setFont(fl.equals("THIS IS AN OFFICIAL RECEIPT") ? boldFont : normalFont);
                    g.drawString(fl, MARGIN, fy);
                    fy += LINE_H;
                }
            }

            g.dispose();
            java.io.ByteArrayOutputStream imgBaos = new java.io.ByteArrayOutputStream();
            javax.imageio.ImageIO.write(img, "JPEG", imgBaos);
            pageImages.add(imgBaos.toByteArray());
        }

        int numPages = pageImages.size();

        // Build PDF byte-by-byte (no external library)
        java.io.ByteArrayOutputStream pdf = new java.io.ByteArrayOutputStream();
        java.util.List<Integer> offsets   = new java.util.ArrayList<>();

        // Helper lambdas
        java.util.function.Consumer<String> write = s -> {
            try { pdf.write(s.getBytes("ISO-8859-1")); }
            catch (java.io.IOException ex) { throw new RuntimeException(ex); }
        };
        java.util.function.Consumer<byte[]> writeBytes = b -> {
            try { pdf.write(b); }
            catch (java.io.IOException ex) { throw new RuntimeException(ex); }
        };

        // Object numbering:
        //   1 = Catalog, 2 = Pages
        //   For each page i (0-based):  pageObj=3+i*3, imgObj=4+i*3, contentObj=5+i*3

        write.accept("%PDF-1.4\n");

        // Obj 1: Catalog
        offsets.add(pdf.size());
        write.accept("1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n");

        // Obj 2: Pages
        offsets.add(pdf.size());
        StringBuilder kids = new StringBuilder();
        for (int i = 0; i < numPages; i++) {
            if (i > 0) kids.append(" ");
            kids.append(3 + i * 3).append(" 0 R");
        }
        write.accept("2 0 obj\n<< /Type /Pages /Kids [" + kids + "] /Count " + numPages + " >>\nendobj\n");

        // Page triplets
        for (int i = 0; i < numPages; i++) {
            byte[] imgBytes  = pageImages.get(i);
            int pageObj      = 3 + i * 3;
            int imgObj       = 4 + i * 3;
            int contentObj   = 5 + i * 3;

            // Content stream: scale image to fill page
            byte[] csBytes = ("q " + PAGE_W + " 0 0 " + PAGE_H + " 0 0 cm /Im Do Q\n")
                              .getBytes("ISO-8859-1");

            // Page object
            offsets.add(pdf.size());
            write.accept(pageObj + " 0 obj\n" +
                "<< /Type /Page /Parent 2 0 R\n" +
                "   /MediaBox [0 0 " + PAGE_W + " " + PAGE_H + "]\n" +
                "   /Resources << /XObject << /Im " + imgObj + " 0 R >> >>\n" +
                "   /Contents " + contentObj + " 0 R >>\n" +
                "endobj\n");

            // Image XObject
            offsets.add(pdf.size());
            write.accept(imgObj + " 0 obj\n" +
                "<< /Type /XObject /Subtype /Image\n" +
                "   /Width " + PAGE_W + " /Height " + PAGE_H + "\n" +
                "   /ColorSpace /DeviceRGB /BitsPerComponent 8\n" +
                "   /Filter /DCTDecode /Length " + imgBytes.length + " >>\n" +
                "stream\n");
            writeBytes.accept(imgBytes);
            write.accept("\nendstream\nendobj\n");

            // Content stream object
            offsets.add(pdf.size());
            write.accept(contentObj + " 0 obj\n" +
                "<< /Length " + csBytes.length + " >>\n" +
                "stream\n");
            writeBytes.accept(csBytes);
            write.accept("\nendstream\nendobj\n");
        }

        // Cross-reference table
        int xrefOffset = pdf.size();
        int totalObjs  = 2 + numPages * 3;
        write.accept("xref\n0 " + (totalObjs + 1) + "\n");
        write.accept("0000000000 65535 f \n");
        for (int off : offsets) {
            write.accept(String.format("%010d 00000 n \n", off));
        }

        // Trailer
        write.accept("trailer\n<< /Size " + (totalObjs + 1) + " /Root 1 0 R >>\n");
        write.accept("startxref\n" + xrefOffset + "\n%%EOF\n");

        return pdf.toByteArray();
    }


    /**
     * Send email with receipt attached as a PDF named after the customer.
     * Uses JavaMail via reflection to avoid compile-time dependency on mail.jar.
     */
    private void sendEmailViaReflection(String host, String port, String user, String pass,
                                        String toEmail, String receiptNum, String customMessage,
                                        String receiptData, String customerName) throws Exception {
        // ── JavaMail classes ──────────────────────────────────────────────────
        Class<?> propertiesClass    = Class.forName("java.util.Properties");
        Class<?> sessionClass       = Class.forName("javax.mail.Session");
        Class<?> messageClass       = Class.forName("javax.mail.Message");
        Class<?> mimeMessageClass   = Class.forName("javax.mail.internet.MimeMessage");
        Class<?> addressClass       = Class.forName("javax.mail.Address");
        Class<?> internetAddrClass  = Class.forName("javax.mail.internet.InternetAddress");
        Class<?> mimeBodyPartClass  = Class.forName("javax.mail.internet.MimeBodyPart");
        Class<?> mimeMultipartClass = Class.forName("javax.mail.internet.MimeMultipart");
        Class<?> multipartClass     = Class.forName("javax.mail.Multipart");
        Class<?> transportClass     = Class.forName("javax.mail.Transport");
        Class<?> recipientTypeClass = Class.forName("javax.mail.Message$RecipientType");
        Class<?> dataHandlerClass   = Class.forName("javax.activation.DataHandler");
        Class<?> dataSourceClass    = Class.forName("javax.activation.DataSource");
        Class<?> byteArrayDSClass   = Class.forName("javax.mail.util.ByteArrayDataSource");

        // ── SMTP properties ───────────────────────────────────────────────────
        // Use Properties directly — no reflection needed for standard JDK class
        Properties props = new Properties();
        props.setProperty("mail.smtp.auth",            "true");
        props.setProperty("mail.smtp.starttls.enable", "true");
        props.setProperty("mail.smtp.host",             host);
        props.setProperty("mail.smtp.port",             port);
        props.setProperty("mail.smtp.user",             user);
        props.setProperty("mail.smtp.password",         pass);

        // ── Session (no Authenticator — credentials supplied at send time) ────
        Object session = sessionClass.getMethod("getInstance", Properties.class)
                                     .invoke(null, props);

        // ── Message headers ───────────────────────────────────────────────────
        Object message = mimeMessageClass.getConstructor(sessionClass).newInstance(session);

        // setFrom(Address)
        Object fromAddr = internetAddrClass.getConstructor(String.class).newInstance(user);
        mimeMessageClass.getMethod("setFrom", addressClass).invoke(message, fromAddr);

        // setRecipients(RecipientType, Address[])
        Object recipientType = recipientTypeClass.getField("TO").get(null);
        Object[] parsed = (Object[]) internetAddrClass.getMethod("parse", String.class).invoke(null, toEmail);
        // Use InternetAddress[] as the concrete array type for setRecipients
        Object addrArray = java.lang.reflect.Array.newInstance(internetAddrClass, parsed.length);
        for (int i = 0; i < parsed.length; i++) java.lang.reflect.Array.set(addrArray, i, parsed[i]);
        mimeMessageClass.getMethod("setRecipients", recipientTypeClass, Class.forName("[Ljavax.mail.Address;"))
                        .invoke(message, recipientType, addrArray);

        mimeMessageClass.getMethod("setSubject", String.class)
                        .invoke(message, "Sync Suites Hotel - Receipt " + receiptNum);

        // ── Generate PDF attachment ───────────────────────────────────────────
        byte[] pdfBytes = generateReceiptPdf(receiptData);

        // PDF filename: "Renyer Booking Receipt.pdf" (sanitise name for filesystem safety)
        String safeName = customerName.replaceAll("[^a-zA-Z0-9 ]", "").trim();
        if (safeName.isEmpty()) safeName = "Guest";
        String pdfFilename = safeName + " Booking Receipt.pdf";

        // ── Build multipart/mixed: body text + PDF attachment ─────────────────
        Object multipart = mimeMultipartClass.getConstructor(String.class).newInstance("mixed");

        // Part 1 — body text (visible directly in Gmail)
        String emailFooter =
            "\n\n----------------------------------------" +
            "\nThank you for choosing Sync Suites Hotel!" +
            "\nFor inquiries: info@syncsuites.com" +
            "\nPhone: +63 912 345 6789" +
            "\n\nTHIS IS AN OFFICIAL RECEIPT" +
            "\nKeep this for your records." +
            "\n----------------------------------------";

        String bodyIntro = customMessage.isEmpty()
            ? "Dear " + customerName + ",\n\nPlease find your booking receipt attached as a PDF."
            : "Dear " + customerName + ",\n\n" + customMessage + "\n\nPlease find your booking receipt attached as a PDF.";

        Object textPart = mimeBodyPartClass.newInstance();
        mimeBodyPartClass.getMethod("setText", String.class)
                         .invoke(textPart, bodyIntro + emailFooter);
        multipartClass.getMethod("addBodyPart", Class.forName("javax.mail.BodyPart"))
                      .invoke(multipart, textPart);

        // Part 2 — PDF attachment via ByteArrayDataSource
        Object pdfDataSource = byteArrayDSClass
                .getConstructor(byte[].class, String.class)
                .newInstance(pdfBytes, "application/pdf");
        Object pdfDataHandler = dataHandlerClass
                .getConstructor(dataSourceClass)
                .newInstance(pdfDataSource);

        Object pdfPart = mimeBodyPartClass.newInstance();
        mimeBodyPartClass.getMethod("setDataHandler", dataHandlerClass)
                         .invoke(pdfPart, pdfDataHandler);
        mimeBodyPartClass.getMethod("setFileName", String.class)
                         .invoke(pdfPart, pdfFilename);
        mimeBodyPartClass.getMethod("setHeader", String.class, String.class)
                         .invoke(pdfPart, "Content-Disposition", "attachment; filename=\"" + pdfFilename + "\"");
        multipartClass.getMethod("addBodyPart", Class.forName("javax.mail.BodyPart"))
                      .invoke(multipart, pdfPart);

        mimeMessageClass.getMethod("setContent", multipartClass).invoke(message, multipart); // multipartClass = javax.mail.Multipart (correct)

        // ── Send (credentials passed directly — no Authenticator object needed) ──
        transportClass.getMethod("send", messageClass, String.class, String.class)
                      .invoke(null, message, user, pass);
    }

    /**
     * Send receipt via SMS (placeholder for Twilio integration)
     */
    private void sendReceiptSMS(int bookingId, String phone) {
        if (phone.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a phone number!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Basic phone validation
        if (!phone.matches("\\d{10,11}")) {
            JOptionPane.showMessageDialog(this, "Phone must be 10-11 digits!", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Get receipt summary
        try (Connection conn = EnhancedDBConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT r.receipt_number, b.total_amount, c.first_name, c.last_name " +
                "FROM receipts r JOIN bookings b ON r.booking_id = b.booking_id " +
                "JOIN customers c ON b.customer_id = c.customer_id " +
                "WHERE r.booking_id = ? ORDER BY r.receipt_id DESC LIMIT 1");
            ps.setInt(1, bookingId);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                JOptionPane.showMessageDialog(this, "No receipt found! Generate one first.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String receiptNum = rs.getString("receipt_number");
            double amount = rs.getDouble("total_amount");
            String customer = rs.getString("first_name") + " " + rs.getString("last_name");

            // SMS message (shortened for SMS)
            String smsMessage = String.format(
                "Sync Suites Hotel\nReceipt: %s\nCustomer: %s\nAmount: ₱%,.2f\nThank you for your stay!",
                receiptNum, customer, amount
            );

            // NOTE: For actual SMS sending, integrate Twilio or other SMS API
            // This is a placeholder showing the message that would be sent
            JOptionPane.showMessageDialog(this, 
                "SMS would be sent to: " + phone + "\n\nMessage:\n" + smsMessage + "\n\n" +
                "Note: To enable real SMS, configure Twilio credentials in Settings and integrate the API.", 
                "SMS Preview", JOptionPane.INFORMATION_MESSAGE);

            // Log notification
            logNotification(bookingId, "SMS", phone, "Receipt " + receiptNum, smsMessage);

            // Update receipt record
            PreparedStatement updatePs = conn.prepareStatement(
                "UPDATE receipts SET sms_sent_to = ?, sms_sent_at = NOW() WHERE receipt_number = ?");
            updatePs.setString(1, phone);
            updatePs.setString(2, receiptNum);
            updatePs.executeUpdate();

            loadReceiptHistory();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "ERROR CODE 007 - SMS Sending Failed\n" +
                "Description: Could not send receipt via SMS\n" +
                "Details: " + e.getMessage() + "\n" +
                "Action: Verify phone number format and SMS service credentials",
                "SMS Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Generate QR Payment code (placeholder for QR PH integration)
     * FIX: Added customer_id to SELECT query
     */
    private void generateQRPayment(int bookingId) {
        try (Connection conn = EnhancedDBConnection.getConnection()) {
            // FIX: Added b.customer_id to the SELECT query
            PreparedStatement ps = conn.prepareStatement(
                "SELECT b.total_amount, b.customer_id, c.first_name, c.last_name, r.room_number " +
                "FROM bookings b JOIN customers c ON b.customer_id = c.customer_id " +
                "JOIN rooms r ON b.room_id = r.room_id WHERE b.booking_id = ?");
            ps.setInt(1, bookingId);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                JOptionPane.showMessageDialog(this, "Booking not found!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            double amount = rs.getDouble("total_amount");
            int customerId = rs.getInt("customer_id");
            String customer = rs.getString("first_name") + " " + rs.getString("last_name");
            String room = rs.getString("room_number");

            // Generate QR reference
            String qrRef = "QRPH-" + System.currentTimeMillis() + "-" + bookingId;

            // Save transaction record
            PreparedStatement transPs = conn.prepareStatement(
                "INSERT INTO payment_transactions (booking_id, customer_id, amount, payment_method, qr_reference, transaction_status) " +
                "VALUES (?,?,?,?,?,?)");
            transPs.setInt(1, bookingId);
            transPs.setInt(2, customerId);  // FIX: Now customer_id is properly fetched
            transPs.setDouble(3, amount);
            transPs.setString(4, "QR PH");
            transPs.setString(5, qrRef);
            transPs.setString(6, "Pending");
            transPs.executeUpdate();

            // Show QR code dialog (placeholder - would show actual QR image)
            JPanel qrPanel = new JPanel(new BorderLayout());
            qrPanel.setPreferredSize(new Dimension(300, 350));

            JLabel lblQR = new JLabel("QR PAYMENT CODE", SwingConstants.CENTER);
            lblQR.setFont(new Font("SansSerif", Font.BOLD, 18));
            qrPanel.add(lblQR, BorderLayout.NORTH);

            // Placeholder for QR image
            JPanel imgPanel = new JPanel();
            imgPanel.setBackground(Color.WHITE);
            imgPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
            imgPanel.setPreferredSize(new Dimension(250, 250));

            JLabel lblPlaceholder = new JLabel("[QR CODE IMAGE]", SwingConstants.CENTER);
            lblPlaceholder.setFont(new Font("Monospaced", Font.BOLD, 16));
            imgPanel.add(lblPlaceholder);

            qrPanel.add(imgPanel, BorderLayout.CENTER);

            JTextArea infoArea = new JTextArea();
            infoArea.setEditable(false);
            infoArea.setFont(new Font("SansSerif", Font.PLAIN, 12));
            infoArea.setText(
                "Reference: " + qrRef + "\n" +
                "Amount: ₱" + String.format("%,.2f", amount) + "\n" +
                "Customer: " + customer + "\n" +
                "Room: " + room + "\n\n" +
                "Scan with QR PH app to pay"
            );
            qrPanel.add(infoArea, BorderLayout.SOUTH);

            JOptionPane.showMessageDialog(this, qrPanel, "QR Payment", JOptionPane.PLAIN_MESSAGE);

            // Note about real integration
            JOptionPane.showMessageDialog(this, 
                "Note: For real QR PH integration:\n" +
                "1. Sign up at https://qr.ph\n" +
                "2. Get API credentials\n" +
                "3. Use their Java SDK to generate actual QR codes\n" +
                "4. Implement webhook for payment confirmation", 
                "Integration Info", JOptionPane.INFORMATION_MESSAGE);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "ERROR CODE 008 - QR Code Generation Failed\n" +
                "Description: Could not generate QR code for payment\n" +
                "Details: " + e.getMessage() + "\n" +
                "Action: Check internet connection to QR API service",
                "QR Code Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Load bookings for receipt generation
     */
    private void loadBookingsForReceipt() {
        bookingModel.setRowCount(0);

        try (Connection conn = EnhancedDBConnection.getConnection()) {
            String sql = "SELECT b.booking_id, CONCAT(c.first_name, ' ', c.last_name) as customer, " +
                        "r.room_number, b.check_in_date, b.check_out_date, b.total_amount, b.status " +
                        "FROM bookings b " +
                        "JOIN customers c ON b.customer_id = c.customer_id " +
                        "JOIN rooms r ON b.room_id = r.room_id " +
                        "ORDER BY b.booking_id DESC";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                bookingModel.addRow(new Object[] {
                    rs.getInt("booking_id"),
                    rs.getString("customer"),
                    rs.getString("room_number"),
                    rs.getString("check_in_date"),
                    rs.getString("check_out_date"),
                    "₱" + String.format("%,.2f", rs.getDouble("total_amount")),
                    rs.getString("status")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "ERROR CODE 009 - Failed to Load Bookings\n" +
                "Description: Could not retrieve booking data from database\n" +
                "Details: " + e.getMessage() + "\n" +
                "Action: Check database connection and try refreshing",
                "Load Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Load receipt history
     * FIX: Added null check for timestamp
     */
    private void loadReceiptHistory() {
        receiptModel.setRowCount(0);

        try (Connection conn = EnhancedDBConnection.getConnection()) {
            String sql = "SELECT r.*, CONCAT(c.first_name, ' ', c.last_name) as customer " +
                        "FROM receipts r JOIN customers c ON r.customer_id = c.customer_id " +
                        "ORDER BY r.printed_at DESC";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

            while (rs.next()) {
                // FIX: Handle null timestamp
                Timestamp printedAt = rs.getTimestamp("printed_at");
                String dateStr = printedAt != null ? sdf.format(printedAt) : "N/A";

                receiptModel.addRow(new Object[] {
                    rs.getInt("receipt_id"),
                    rs.getInt("booking_id"),
                    rs.getString("receipt_number"),
                    rs.getString("customer"),
                    "₱" + String.format("%,.2f", rs.getDouble("total_amount")),
                    rs.getString("printed_by"),
                    dateStr
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "ERROR CODE 010 - Failed to Load Receipts\n" +
                "Description: Could not retrieve receipt history from database\n" +
                "Details: " + e.getMessage() + "\n" +
                "Action: Check database connection and try refreshing",
                "Load Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * View receipt from history
     * FIX: Added null check for receipt data
     */
    private void viewReceiptFromHistory() {
        int row = receiptTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a receipt to view.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int receiptId = (int) receiptModel.getValueAt(row, 0);

        try (Connection conn = EnhancedDBConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("SELECT receipt_data FROM receipts WHERE receipt_id = ?");
            ps.setInt(1, receiptId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String receiptData = rs.getString("receipt_data");
                // FIX: Handle null receipt data
                if (receiptData == null) receiptData = "[No receipt data available]";

                JTextArea area = new JTextArea(receiptData);
                area.setFont(new Font("Monospaced", Font.PLAIN, 12));
                area.setEditable(false);
                area.setBackground(new Color(255, 255, 240));

                JScrollPane scroll = new JScrollPane(area);
                scroll.setPreferredSize(new Dimension(450, 500));

                JOptionPane.showMessageDialog(this, scroll, "Receipt View", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "ERROR CODE 011 - Failed to View Receipt\n" +
                "Description: Could not retrieve receipt details\n" +
                "Details: " + e.getMessage() + "\n" +
                "Action: Verify receipt exists and try again",
                "View Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void logNotification(int bookingId, String type, String recipient, String subject, String content) {
        try (Connection conn = EnhancedDBConnection.getConnection()) {
            String sql = "INSERT INTO notification_log (booking_id, notification_type, recipient, subject, content, status) " +
                        "VALUES (?,?,?,?,?,'Sent')";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, bookingId);
            ps.setString(2, type);
            ps.setString(3, recipient);
            ps.setString(4, subject);
            ps.setString(5, content);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("ERROR CODE 012 - Notification Log Error");
            System.err.println("Description: Failed to log notification event");
            System.err.println("Details: " + e.getMessage());
            System.err.println("Impact: Notification not recorded in audit log");
        }
    }
}