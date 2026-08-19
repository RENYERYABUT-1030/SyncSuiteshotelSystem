package Hotel_Reservation;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;

public class MaintenanceModePanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private JTable activeTable, archiveTable;
    private DefaultTableModel activeModel, archiveModel;
    private JTabbedPane tabbedPane;
    private JTextField txtSearch;

    public MaintenanceModePanel() {
        setLayout(new BorderLayout());

        JLabel titleLbl = new JLabel("Maintenance Mode - Archive Management");
        titleLbl.setForeground(new Color(0, 0, 47));
        titleLbl.setBackground(new Color(240, 240, 240));
        titleLbl.setOpaque(true);
        titleLbl.setFont(new Font("SansSerif", Font.BOLD, 20));
        titleLbl.setBorder(new EmptyBorder(15, 15, 15, 0));
        add(titleLbl, BorderLayout.NORTH);

        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("SansSerif", Font.BOLD, 13));

        tabbedPane.addTab("Active Records", createActiveRecordsPanel());
        tabbedPane.addTab("Archived Records", createArchivedRecordsPanel());

        add(tabbedPane, BorderLayout.CENTER);

        loadActiveRecords();
        loadArchivedRecords();
    }

    private JPanel createActiveRecordsPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        searchPanel.setBackground(new Color(240, 240, 240));

        txtSearch = new JTextField(20);
        txtSearch.setFont(new Font("SansSerif", Font.PLAIN, 13));
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(txtSearch);

        JButton btnSearch = new JButton("Search");
        btnSearch.setBackground(new Color(70, 130, 180));
        btnSearch.setForeground(Color.WHITE);
        btnSearch.addActionListener(e -> loadActiveRecords());
        searchPanel.add(btnSearch);

        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.setBackground(new Color(46, 194, 126));
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.addActionListener(e -> loadActiveRecords());
        searchPanel.add(btnRefresh);

        panel.add(searchPanel, BorderLayout.NORTH);

        String[] columns = {"Booking ID", "Customer", "Room", "Type", "Check-In", "Check-Out", "Total", "Status", "Created"};
        activeModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };

        activeTable = new JTable(activeModel);
        activeTable.setRowHeight(28);
        activeTable.setFont(new Font("SansSerif", Font.PLAIN, 12));
        activeTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        activeTable.getTableHeader().setForeground(Color.WHITE);
        activeTable.getTableHeader().setBackground(new Color(31, 71, 145));

        JScrollPane scroll = new JScrollPane(activeTable);
        panel.add(scroll, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));

        JButton btnArchive = new JButton("Archive Selected");
        btnArchive.setBackground(new Color(255, 165, 0));
        btnArchive.setForeground(Color.WHITE);
        btnArchive.addActionListener(e -> archiveSelectedRecord());
        btnPanel.add(btnArchive);

        JButton btnArchiveOld = new JButton("Archive Old (>1 Year)");
        btnArchiveOld.setBackground(new Color(224, 27, 36));
        btnArchiveOld.setForeground(Color.WHITE);
        btnArchiveOld.addActionListener(e -> archiveOldRecords());
        btnPanel.add(btnArchiveOld);

        JButton btnDelete = new JButton("Permanent Delete");
        btnDelete.setBackground(new Color(100, 100, 100));
        btnDelete.setForeground(Color.WHITE);
        btnDelete.addActionListener(e -> permanentlyDeleteRecord());
        btnPanel.add(btnDelete);

        panel.add(btnPanel, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createArchivedRecordsPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        String[] columns = {"Archive ID", "Orig. Booking ID", "Customer ID", "Room ID", "Check-In", "Check-Out", "Total", "Status", "Archived By", "Reason", "Archived Date"};
        archiveModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };

        archiveTable = new JTable(archiveModel);
        archiveTable.setRowHeight(28);
        archiveTable.setFont(new Font("SansSerif", Font.PLAIN, 12));
        archiveTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        archiveTable.getTableHeader().setForeground(Color.WHITE);
        archiveTable.getTableHeader().setBackground(new Color(100, 50, 150));

        JScrollPane scroll = new JScrollPane(archiveTable);
        panel.add(scroll, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));

        JButton btnRestore = new JButton("Restore Selected");
        btnRestore.setBackground(new Color(46, 194, 126));
        btnRestore.setForeground(Color.WHITE);
        btnRestore.addActionListener(e -> restoreArchivedRecord());
        btnPanel.add(btnRestore);

        JButton btnView = new JButton("View Details");
        btnView.setBackground(new Color(70, 130, 180));
        btnView.setForeground(Color.WHITE);
        btnView.addActionListener(e -> viewArchiveDetails());
        btnPanel.add(btnView);

        panel.add(btnPanel, BorderLayout.SOUTH);
        return panel;
    }

    private void loadActiveRecords() {
        activeModel.setRowCount(0);
        String search = txtSearch.getText().trim();

        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT b.booking_id, CONCAT(c.first_name, ' ', c.last_name) AS customer, " +
                        "r.room_number, rt.type_name, b.check_in_date, b.check_out_date, " +
                        "b.total_amount, b.status, b.created_at " +
                        "FROM bookings b " +
                        "JOIN customers c ON b.customer_id = c.customer_id " +
                        "JOIN rooms r ON b.room_id = r.room_id " +
                        "JOIN room_types rt ON r.room_type_id = rt.room_type_id " +
                        "WHERE 1=1 ";

            if (!search.isEmpty()) {
                sql += "AND (c.first_name LIKE ? OR c.last_name LIKE ? OR r.room_number LIKE ?) ";
            }
            sql += "ORDER BY b.created_at DESC";

            PreparedStatement ps = conn.prepareStatement(sql);
            if (!search.isEmpty()) {
                String pattern = "%" + search + "%";
                ps.setString(1, pattern);
                ps.setString(2, pattern);
                ps.setString(3, pattern);
            }

            ResultSet rs = ps.executeQuery();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

            while (rs.next()) {
                Timestamp created = rs.getTimestamp("created_at");
                String createdStr = created != null ? sdf.format(created) : "N/A";
                activeModel.addRow(new Object[] {
                    rs.getInt("booking_id"), rs.getString("customer"), rs.getString("room_number"),
                    rs.getString("type_name"), rs.getString("check_in_date"), rs.getString("check_out_date"),
                    "P" + String.format("%,.2f", rs.getDouble("total_amount")),
                    rs.getString("status"), createdStr
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading records: " + e.getMessage());
        }
    }

    private void loadArchivedRecords() {
        archiveModel.setRowCount(0);
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT * FROM archived_bookings ORDER BY archived_at DESC";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

            while (rs.next()) {
                archiveModel.addRow(new Object[] {
                    rs.getInt("archive_id"), rs.getInt("original_booking_id"),
                    rs.getInt("customer_id"), rs.getInt("room_id"),
                    rs.getString("check_in_date"), rs.getString("check_out_date"),
                    "P" + String.format("%,.2f", rs.getDouble("total_amount")),
                    rs.getString("status"), rs.getString("archived_by"),
                    rs.getString("archive_reason"),
                    sdf.format(rs.getTimestamp("archived_at"))
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading archived records: " + e.getMessage());
        }
    }

    private void archiveSelectedRecord() {
        int row = activeTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a record to archive.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int bookingId = (int) activeModel.getValueAt(row, 0);
        String customerName = (String) activeModel.getValueAt(row, 1);

        String reason = JOptionPane.showInputDialog(this, 
            "Enter reason for archiving Booking #" + bookingId + " (" + customerName + "):",
            "Archive Record", JOptionPane.QUESTION_MESSAGE);

        if (reason == null || reason.trim().isEmpty()) return;

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            PreparedStatement getPs = conn.prepareStatement("SELECT * FROM bookings WHERE booking_id = ?");
            getPs.setInt(1, bookingId);
            ResultSet rs = getPs.executeQuery();

            if (rs.next()) {
                PreparedStatement archivePs = conn.prepareStatement(
                    "INSERT INTO archived_bookings (original_booking_id, customer_id, room_id, " +
                    "check_in_date, check_out_date, total_amount, payment_method, status, " +
                    "adults, seniors, kids, senior_discount, extra_guest_charge, reschedule_charge, " +
                    "archived_by, archive_reason) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");

                archivePs.setInt(1, bookingId);
                archivePs.setInt(2, rs.getInt("customer_id"));
                archivePs.setInt(3, rs.getInt("room_id"));
                archivePs.setDate(4, rs.getDate("check_in_date"));
                archivePs.setDate(5, rs.getDate("check_out_date"));
                archivePs.setDouble(6, rs.getDouble("total_amount"));
                archivePs.setString(7, rs.getString("payment_method"));
                archivePs.setString(8, rs.getString("status"));
                archivePs.setInt(9, rs.getInt("adults"));
                archivePs.setInt(10, rs.getInt("seniors"));
                archivePs.setInt(11, rs.getInt("kids"));
                archivePs.setDouble(12, rs.getDouble("senior_discount"));
                archivePs.setDouble(13, rs.getDouble("extra_guest_charge"));
                archivePs.setDouble(14, rs.getDouble("reschedule_charge"));
                archivePs.setString(15, LoginFrame.currentUserName);
                archivePs.setString(16, reason);
                archivePs.executeUpdate();

                PreparedStatement delPs = conn.prepareStatement("DELETE FROM bookings WHERE booking_id = ?");
                delPs.setInt(1, bookingId);
                delPs.executeUpdate();

                PreparedStatement roomPs = conn.prepareStatement(
                    "UPDATE rooms SET is_available = 1 WHERE room_id = ? AND " +
                    "NOT EXISTS (SELECT 1 FROM bookings WHERE room_id = ? AND status IN ('Reserved', 'Checked In'))");
                roomPs.setInt(1, rs.getInt("room_id"));
                roomPs.setInt(2, rs.getInt("room_id"));
                roomPs.executeUpdate();

                conn.commit();
                JOptionPane.showMessageDialog(this, "Booking #" + bookingId + " archived!", "Archived", JOptionPane.INFORMATION_MESSAGE);
                loadActiveRecords();
                loadArchivedRecords();
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Archive failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void archiveOldRecords() {
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Archive all bookings older than 1 year?",
            "Confirm Bulk Archive", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            PreparedStatement findPs = conn.prepareStatement(
                "SELECT * FROM bookings WHERE check_out_date < DATE_SUB(CURDATE(), INTERVAL 1 YEAR) " +
                "AND status IN ('Checked Out', 'Cancelled')");
            ResultSet rs = findPs.executeQuery();

            int count = 0;
            while (rs.next()) {
                PreparedStatement archivePs = conn.prepareStatement(
                    "INSERT INTO archived_bookings (original_booking_id, customer_id, room_id, " +
                    "check_in_date, check_out_date, total_amount, payment_method, status, " +
                    "adults, seniors, kids, senior_discount, extra_guest_charge, reschedule_charge, " +
                    "archived_by, archive_reason) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
                archivePs.setInt(1, rs.getInt("booking_id"));
                archivePs.setInt(2, rs.getInt("customer_id"));
                archivePs.setInt(3, rs.getInt("room_id"));
                archivePs.setDate(4, rs.getDate("check_in_date"));
                archivePs.setDate(5, rs.getDate("check_out_date"));
                archivePs.setDouble(6, rs.getDouble("total_amount"));
                archivePs.setString(7, rs.getString("payment_method"));
                archivePs.setString(8, rs.getString("status"));
                archivePs.setInt(9, rs.getInt("adults"));
                archivePs.setInt(10, rs.getInt("seniors"));
                archivePs.setInt(11, rs.getInt("kids"));
                archivePs.setDouble(12, rs.getDouble("senior_discount"));
                archivePs.setDouble(13, rs.getDouble("extra_guest_charge"));
                archivePs.setDouble(14, rs.getDouble("reschedule_charge"));
                archivePs.setString(15, LoginFrame.currentUserName);
                archivePs.setString(16, "Auto-archive: Older than 1 year");
                archivePs.executeUpdate();

                PreparedStatement delPs = conn.prepareStatement("DELETE FROM bookings WHERE booking_id = ?");
                delPs.setInt(1, rs.getInt("booking_id"));
                delPs.executeUpdate();
                count++;
            }
            conn.commit();
            JOptionPane.showMessageDialog(this, count + " records archived!", "Done", JOptionPane.INFORMATION_MESSAGE);
            loadActiveRecords();
            loadArchivedRecords();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Bulk archive failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void restoreArchivedRecord() {
        int row = archiveTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select an archived record.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int archiveId = (int) archiveModel.getValueAt(row, 0);
        int originalBookingId = (int) archiveModel.getValueAt(row, 1);

        int confirm = JOptionPane.showConfirmDialog(this, 
            "Restore archived record #" + archiveId + "?", "Confirm Restore", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            PreparedStatement checkPs = conn.prepareStatement("SELECT booking_id FROM bookings WHERE booking_id = ?");
            checkPs.setInt(1, originalBookingId);
            if (checkPs.executeQuery().next()) {
                conn.rollback();
                JOptionPane.showMessageDialog(this, "Original booking ID already exists!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            PreparedStatement getPs = conn.prepareStatement("SELECT * FROM archived_bookings WHERE archive_id = ?");
            getPs.setInt(1, archiveId);
            ResultSet rs = getPs.executeQuery();

            if (rs.next()) {
                PreparedStatement restorePs = conn.prepareStatement(
                    "INSERT INTO bookings (booking_id, customer_id, room_id, check_in_date, check_out_date, " +
                    "total_amount, payment_method, status, adults, seniors, kids, " +
                    "senior_discount, extra_guest_charge, reschedule_charge) " +
                    "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
                restorePs.setInt(1, originalBookingId);
                restorePs.setInt(2, rs.getInt("customer_id"));
                restorePs.setInt(3, rs.getInt("room_id"));
                restorePs.setDate(4, rs.getDate("check_in_date"));
                restorePs.setDate(5, rs.getDate("check_out_date"));
                restorePs.setDouble(6, rs.getDouble("total_amount"));
                restorePs.setString(7, rs.getString("payment_method"));
                restorePs.setString(8, rs.getString("status"));
                restorePs.setInt(9, rs.getInt("adults"));
                restorePs.setInt(10, rs.getInt("seniors"));
                restorePs.setInt(11, rs.getInt("kids"));
                restorePs.setDouble(12, rs.getDouble("senior_discount"));
                restorePs.setDouble(13, rs.getDouble("extra_guest_charge"));
                restorePs.setDouble(14, rs.getDouble("reschedule_charge"));
                restorePs.executeUpdate();

                PreparedStatement delPs = conn.prepareStatement("DELETE FROM archived_bookings WHERE archive_id = ?");
                delPs.setInt(1, archiveId);
                delPs.executeUpdate();

                conn.commit();
                JOptionPane.showMessageDialog(this, "Record restored!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadActiveRecords();
                loadArchivedRecords();
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Restore failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void viewArchiveDetails() {
        int row = archiveTable.getSelectedRow();
        if (row == -1) return;
        int archiveId = (int) archiveModel.getValueAt(row, 0);

        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT ab.*, c.first_name, c.last_name, r.room_number, rt.type_name " +
                        "FROM archived_bookings ab " +
                        "JOIN customers c ON ab.customer_id = c.customer_id " +
                        "JOIN rooms r ON ab.room_id = r.room_id " +
                        "JOIN room_types rt ON r.room_type_id = rt.room_type_id " +
                        "WHERE ab.archive_id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, archiveId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                StringBuilder details = new StringBuilder();
                details.append("ARCHIVED BOOKING DETAILS\n\n");
                details.append("Archive ID: ").append(rs.getInt("archive_id")).append("\n");
                details.append("Original Booking ID: ").append(rs.getInt("original_booking_id")).append("\n");
                details.append("Customer: ").append(rs.getString("first_name")).append(" ").append(rs.getString("last_name")).append("\n");
                details.append("Room: ").append(rs.getString("room_number")).append(" (").append(rs.getString("type_name")).append(")\n");
                details.append("Check-In: ").append(rs.getDate("check_in_date")).append("\n");
                details.append("Check-Out: ").append(rs.getDate("check_out_date")).append("\n");
                details.append("Total: P").append(String.format("%,.2f", rs.getDouble("total_amount"))).append("\n");
                details.append("Status: ").append(rs.getString("status")).append("\n");
                details.append("Guests: ").append(rs.getInt("adults")).append(" Adults, ")
                       .append(rs.getInt("seniors")).append(" Seniors, ")
                       .append(rs.getInt("kids")).append(" Kids\n");
                details.append("\nArchived By: ").append(rs.getString("archived_by")).append("\n");
                details.append("Reason: ").append(rs.getString("archive_reason")).append("\n");
                details.append("Archived At: ").append(rs.getTimestamp("archived_at")).append("\n");

                JTextArea textArea = new JTextArea(details.toString());
                textArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
                textArea.setEditable(false);
                textArea.setBackground(new Color(250, 250, 250));

                JScrollPane scroll = new JScrollPane(textArea);
                scroll.setPreferredSize(new Dimension(450, 400));
                JOptionPane.showMessageDialog(this, scroll, "Archive Details", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void permanentlyDeleteRecord() {
        int row = activeTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a record.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int bookingId = (int) activeModel.getValueAt(row, 0);

        String confirm = JOptionPane.showInputDialog(this, 
            "PERMANENT DELETE - Booking #" + bookingId + "\nType 'DELETE' to confirm:",
            "PERMANENT DELETE", JOptionPane.WARNING_MESSAGE);

        if (!"DELETE".equals(confirm)) {
            JOptionPane.showMessageDialog(this, "Deletion cancelled.", "Cancelled", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            PreparedStatement getPs = conn.prepareStatement("SELECT room_id FROM bookings WHERE booking_id = ?");
            getPs.setInt(1, bookingId);
            ResultSet rs = getPs.executeQuery();
            int roomId = -1;
            if (rs.next()) roomId = rs.getInt("room_id");

            PreparedStatement delReceipts = conn.prepareStatement("DELETE FROM receipts WHERE booking_id = ?");
            delReceipts.setInt(1, bookingId);
            delReceipts.executeUpdate();

            PreparedStatement delPayments = conn.prepareStatement("DELETE FROM payment_transactions WHERE booking_id = ?");
            delPayments.setInt(1, bookingId);
            delPayments.executeUpdate();

            PreparedStatement delNotif = conn.prepareStatement("DELETE FROM notification_log WHERE booking_id = ?");
            delNotif.setInt(1, bookingId);
            delNotif.executeUpdate();

            PreparedStatement delBooking = conn.prepareStatement("DELETE FROM bookings WHERE booking_id = ?");
            delBooking.setInt(1, bookingId);
            delBooking.executeUpdate();

            if (roomId != -1) {
                PreparedStatement roomPs = conn.prepareStatement(
                    "UPDATE rooms SET is_available = 1 WHERE room_id = ? AND " +
                    "NOT EXISTS (SELECT 1 FROM bookings WHERE room_id = ? AND status IN ('Reserved', 'Checked In'))");
                roomPs.setInt(1, roomId);
                roomPs.setInt(2, roomId);
                roomPs.executeUpdate();
            }

            conn.commit();
            JOptionPane.showMessageDialog(this, "Booking #" + bookingId + " permanently deleted!", "Deleted", JOptionPane.INFORMATION_MESSAGE);
            loadActiveRecords();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Delete failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}