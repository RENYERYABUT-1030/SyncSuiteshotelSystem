package Hotel_Reservation;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Enhanced Manage Booking Panel
 * Bug fixes: SQL injection prevention, transaction safety, date validation
 * Features: Check-in/out, extend stay, upgrade room, reschedule, cancel, filter, print receipt
 */
public class ManageBookingPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private JTable table;
    private DefaultTableModel tableModel;

    // Filters
    private String filterStatus = "All";
    private String filterPayment = "All";
    private String filterRoomType = "All";

    public ManageBookingPanel() {
        setLayout(new BorderLayout());

        // Header
        JLabel titleLbl = new JLabel("Manage Bookings");
        titleLbl.setForeground(new Color(0, 0, 47));
        titleLbl.setBackground(new Color(240, 240, 240));
        titleLbl.setOpaque(true);
        titleLbl.setFont(new Font("SansSerif", Font.BOLD, 20));
        titleLbl.setBorder(new EmptyBorder(15, 15, 15, 0));
        add(titleLbl, BorderLayout.NORTH);

        // Table Setup
        String[] columns = {"Booking ID", "Customer Name", "Room No.", "Room Type", "Check-In", "Check-Out", "Total (₱)", "Payment", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        table = new JTable(tableModel);
        table.setRowHeight(28);
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setBackground(new Color(31, 71, 145));

        JScrollPane scrollPane = new JScrollPane();
        add(scrollPane, BorderLayout.CENTER);
        scrollPane.setViewportView(table);

        // Status column color renderer
        table.getColumnModel().getColumn(8).setCellRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setFont(new Font("SansSerif", Font.BOLD, 13));

                if (value != null) {
                    String status = value.toString();
                    switch (status) {
                        case "Reserved": c.setForeground(new Color(0, 128, 255)); break;
                        case "Checked In": c.setForeground(new Color(46, 194, 126)); break;
                        case "Checked Out": c.setForeground(new Color(255, 165, 0)); break;
                        case "Cancelled": c.setForeground(new Color(224, 27, 36)); break;
                        case "Rescheduled": c.setForeground(new Color(148, 0, 211)); break;
                        default: c.setForeground(Color.BLACK); break;
                    }
                }

                if (isSelected) {
                    c.setBackground(table.getSelectionBackground());
                } else {
                    c.setBackground(table.getBackground());
                }
                return c;
            }
        });

        // Action Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 10));
        add(buttonPanel, BorderLayout.SOUTH);

        JButton btnRefresh = new JButton("🔄 Refresh");
        JButton btnCheckIn = new JButton("✓ Check In");
        JButton btnCheckOut = new JButton("✗ Check Out");
        JButton btnExtend = new JButton("📅 Extend Stay");
        JButton btnUpgrade = new JButton("🏨 Upgrade Room");
        JButton btnReschedule = new JButton("📆 Reschedule");
        JButton btnCancel = new JButton("❌ Cancel");
        JButton btnReceipt = new JButton("🧾 Receipt");
        JButton btnFilter = new JButton("🔍 Filter");

        // Styling
        btnFilter.setBackground(new Color(70, 130, 180)); btnFilter.setForeground(Color.WHITE);
        btnCheckIn.setBackground(new Color(46, 194, 126)); btnCheckIn.setForeground(Color.WHITE);
        btnCheckOut.setBackground(new Color(255, 165, 0)); btnCheckOut.setForeground(Color.WHITE);
        btnExtend.setBackground(new Color(148, 0, 211)); btnExtend.setForeground(Color.WHITE);
        btnUpgrade.setBackground(new Color(32, 178, 170)); btnUpgrade.setForeground(Color.WHITE);
        btnReschedule.setBackground(new Color(0, 128, 255)); btnReschedule.setForeground(Color.WHITE);
        btnCancel.setBackground(new Color(224, 27, 36)); btnCancel.setForeground(Color.WHITE);
        btnReceipt.setBackground(new Color(100, 50, 150)); btnReceipt.setForeground(Color.WHITE);

        for (JButton btn : new JButton[]{btnRefresh, btnCheckIn, btnCheckOut, btnExtend, btnUpgrade, btnReschedule, btnCancel, btnReceipt, btnFilter}) {
            btn.setFont(new Font("SansSerif", Font.BOLD, 12));
            btn.setFocusPainted(false);
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            buttonPanel.add(btn);
        }

        // Listeners
        btnRefresh.addActionListener(e -> loadBookings());
        btnCheckIn.addActionListener(e -> checkInBooking());
        btnCheckOut.addActionListener(e -> checkOutBooking());
        btnExtend.addActionListener(e -> extendStayBooking());
        btnUpgrade.addActionListener(e -> upgradeRoomBooking());
        btnReschedule.addActionListener(e -> rescheduleBooking());
        btnCancel.addActionListener(e -> cancelBooking());
        btnReceipt.addActionListener(e -> printReceiptForBooking());
        btnFilter.addActionListener(e -> showFilterDialog());

        loadBookings();
    }

    /**
     * Load bookings with auto-update rules and filters
     * BUG FIX: Proper SQL with prepared statements, no string concatenation
     */
    private void loadBookings() {
        tableModel.setRowCount(0);

        try (Connection conn = DBConnection.getConnection()) {
            // Auto Check-Out guests if their date has passed
            try (Statement autoStmt = conn.createStatement()) {
                autoStmt.executeUpdate(
                    "UPDATE bookings SET status = 'Checked Out' " +
                    "WHERE check_out_date < DATE(CURDATE()) AND status = 'Checked In'"
                );
            }

            // Free rooms ONLY if they have NO active bookings
            try (Statement autoFreeStmt = conn.createStatement()) {
                autoFreeStmt.executeUpdate(
                    "UPDATE rooms r SET r.is_available = 1 " +
                    "WHERE r.is_available = 0 AND r.is_maintenance = 0 " +
                    "AND NOT EXISTS (" +
                    "    SELECT 1 FROM bookings b " +
                    "    WHERE b.room_id = r.room_id " +
                    "    AND b.status IN ('Reserved', 'Checked In')" +
                    ")"
                );
            }

            // Build query with prepared statements
            String sql = "SELECT b.booking_id, CONCAT(c.first_name, ' ', c.last_name) AS customer, " +
                        "r.room_number, rt.type_name, b.check_in_date, b.check_out_date, " +
                        "b.total_amount, b.payment_method, b.status " +
                        "FROM bookings b " +
                        "JOIN customers c ON b.customer_id = c.customer_id " +
                        "JOIN rooms r ON b.room_id = r.room_id " +
                        "JOIN room_types rt ON r.room_type_id = rt.room_type_id " +
                        "WHERE 1=1 ";

            if (!filterStatus.equals("All")) {
                sql += "AND b.status = ? ";
            }
            if (!filterPayment.equals("All")) {
                sql += "AND b.payment_method = ? ";
            }
            if (!filterRoomType.equals("All")) {
                sql += "AND rt.type_name = ? ";
            }
            sql += "ORDER BY b.check_in_date DESC";

            PreparedStatement ps = conn.prepareStatement(sql);
            int index = 1;

            if (!filterStatus.equals("All")) {
                ps.setString(index++, filterStatus);
            }
            if (!filterPayment.equals("All")) {
                ps.setString(index++, filterPayment);
            }
            if (!filterRoomType.equals("All")) {
                ps.setString(index++, filterRoomType);
            }

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                tableModel.addRow(new Object[] {
                    rs.getInt("booking_id"), rs.getString("customer"), rs.getString("room_number"),
                    rs.getString("type_name"), rs.getString("check_in_date"), rs.getString("check_out_date"),
                    "₱" + String.format("%,.2f", rs.getDouble("total_amount")),
                    rs.getString("payment_method"), rs.getString("status")
                });
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading bookings: " + e.getMessage());
        }
    }

    /**
     * Check In - with validation
     */
    private void checkInBooking() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a booking.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!tableModel.getValueAt(row, 8).equals("Reserved")) {
            JOptionPane.showMessageDialog(this, "Only 'Reserved' bookings can be Checked In.");
            return;
        }

        int bookingId = (int) tableModel.getValueAt(row, 0);
        String customer = (String) tableModel.getValueAt(row, 1);
        String room = (String) tableModel.getValueAt(row, 2);

        int confirm = JOptionPane.showConfirmDialog(this,
            "Check in customer " + customer + " to Room " + room + "?",
            "Confirm Check-In", JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) return;

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            PreparedStatement ps = conn.prepareStatement(
                "UPDATE bookings SET status = 'Checked In' WHERE booking_id = ?");
            ps.setInt(1, bookingId);
            ps.executeUpdate();

            // Ensure room is marked unavailable
            PreparedStatement roomPs = conn.prepareStatement(
                "UPDATE rooms SET is_available = 0 WHERE room_id = (SELECT room_id FROM bookings WHERE booking_id = ?)");
            roomPs.setInt(1, bookingId);
            roomPs.executeUpdate();

            conn.commit();
            JOptionPane.showMessageDialog(this, "Check-in successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadBookings();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    /**
     * Check Out - with validation and room freeing
     */
    private void checkOutBooking() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a booking.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!tableModel.getValueAt(row, 8).equals("Checked In")) {
            JOptionPane.showMessageDialog(this, "Only 'Checked In' guests can Check Out.");
            return;
        }

        int bookingId = (int) tableModel.getValueAt(row, 0);
        String customer = (String) tableModel.getValueAt(row, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
            "Check out customer " + customer + "?",
            "Confirm Check-Out", JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) return;

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            // Update booking status
            PreparedStatement ps = conn.prepareStatement(
                "UPDATE bookings SET status = 'Checked Out' WHERE booking_id = ?");
            ps.setInt(1, bookingId);
            ps.executeUpdate();

            // Free the room
            PreparedStatement roomPs = conn.prepareStatement(
                "UPDATE rooms r JOIN bookings b ON b.room_id = r.room_id SET r.is_available = 1 WHERE b.booking_id = ? AND r.is_maintenance = 0");
            roomPs.setInt(1, bookingId);
            roomPs.executeUpdate();

            conn.commit();
            JOptionPane.showMessageDialog(this, "Check-out successful! Room is now available.", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadBookings();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    /**
     * Extend Stay - with proper date validation and cost calculation
     */
    private void extendStayBooking() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a booking.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String status = (String) tableModel.getValueAt(row, 8);
        if (!status.equals("Checked In")) {
            JOptionPane.showMessageDialog(this, "Only 'Checked In' guests can extend their stay.", "Cannot Extend", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int bookingId = (int) tableModel.getValueAt(row, 0);
            String currentTotalStr = ((String) tableModel.getValueAt(row, 6)).replace("₱", "").replace(",", "");
            double currentTotal = Double.parseDouble(currentTotalStr);
            String roomType = (String) tableModel.getValueAt(row, 3);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date oldCheckOut = sdf.parse((String) tableModel.getValueAt(row, 5));

            SpinnerDateModel extendModel = new SpinnerDateModel(oldCheckOut, oldCheckOut, null, Calendar.DAY_OF_MONTH);
            JSpinner extendSpinner = new JSpinner(extendModel);
            extendSpinner.setEditor(new JSpinner.DateEditor(extendSpinner, "yyyy-MM-dd"));

            if (JOptionPane.showConfirmDialog(this,
                new Object[]{"Select New Check-Out Date:", extendSpinner},
                "Extend Stay", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {

                Date newCheckOut = (Date) extendSpinner.getValue();
                long extraDays = (newCheckOut.getTime() - oldCheckOut.getTime()) / (1000 * 60 * 60 * 24);

                if (extraDays <= 0) {
                    JOptionPane.showMessageDialog(this, "New date must be after current check-out date.", "Invalid Date", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // Max extend 14 days
                if (extraDays > 14) {
                    JOptionPane.showMessageDialog(this, "Maximum extension is 14 days.", "Limit Exceeded", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                double extraCost = extraDays * getRateForType(roomType);
                double newTotal = currentTotal + extraCost;

                int confirm = JOptionPane.showConfirmDialog(this,
                    "Extend by " + extraDays + " day(s)\nExtra cost: ₱" + String.format("%,.2f", extraCost) + "\nNew Total: ₱" + String.format("%,.2f", newTotal) + "\n\nProceed?",
                    "Confirm Extension", JOptionPane.YES_NO_OPTION);

                if (confirm != JOptionPane.YES_OPTION) return;

                try (Connection conn = DBConnection.getConnection()) {
                    conn.setAutoCommit(false);

                    // Check room availability for extended dates
                    PreparedStatement checkPs = conn.prepareStatement(
                        "SELECT room_id FROM bookings WHERE booking_id = ?");
                    checkPs.setInt(1, bookingId);
                    ResultSet rs = checkPs.executeQuery();
                    int roomId = 0;
                    if (rs.next()) roomId = rs.getInt("room_id");

                    // Check if room is available for extended period
                    PreparedStatement availPs = conn.prepareStatement(
                        "SELECT COUNT(*) FROM bookings WHERE room_id = ? AND booking_id != ? " +
                        "AND status IN ('Reserved', 'Checked In') " +
                        "AND check_in_date < ? AND check_out_date > ?");
                    availPs.setInt(1, roomId);
                    availPs.setInt(2, bookingId);
                    availPs.setDate(3, new java.sql.Date(newCheckOut.getTime()));
                    availPs.setDate(4, new java.sql.Date(oldCheckOut.getTime()));
                    ResultSet availRs = availPs.executeQuery();
                    if (availRs.next() && availRs.getInt(1) > 0) {
                        conn.rollback();
                        JOptionPane.showMessageDialog(this, "Room is not available for the extended dates.", "Unavailable", JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    PreparedStatement ps = conn.prepareStatement(
                        "UPDATE bookings SET check_out_date = ?, total_amount = ? WHERE booking_id = ?");
                    ps.setString(1, sdf.format(newCheckOut));
                    ps.setDouble(2, newTotal);
                    ps.setInt(3, bookingId);
                    ps.executeUpdate();

                    conn.commit();
                    JOptionPane.showMessageDialog(this, "Stay extended! Extra cost: ₱" + String.format("%,.2f", extraCost), "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadBookings();
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Upgrade Room - with availability check and price difference calculation
     */
    private void upgradeRoomBooking() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a booking first.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String status = (String) tableModel.getValueAt(row, 8);
        if (!status.equals("Reserved") && !status.equals("Checked In")) {
            JOptionPane.showMessageDialog(this, "Cannot upgrade this booking.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int bookingId = (int) tableModel.getValueAt(row, 0);
            String oldRoomNum = (String) tableModel.getValueAt(row, 2);
            String oldRoomType = (String) tableModel.getValueAt(row, 3);
            String currentTotalStr = ((String) tableModel.getValueAt(row, 6)).replace("₱", "").replace(",", "");
            double currentTotal = Double.parseDouble(currentTotalStr);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date in = sdf.parse((String) tableModel.getValueAt(row, 4));
            Date out = sdf.parse((String) tableModel.getValueAt(row, 5));
            long days = (out.getTime() - in.getTime()) / (1000 * 60 * 60 * 24);

            // Create upgrade dialog
            JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
            String[] roomTypes = {"Standard Room", "Junior Suite", "Executive Suite", "Presidential Suite"};
            JComboBox<String> roomTypeCombo = new JComboBox<>(roomTypes);
            JComboBox<String> availableRoomCombo = new JComboBox<>();

            panel.add(new JLabel("Room Type:"));
            panel.add(roomTypeCombo);
            panel.add(new JLabel("Available Room:"));
            panel.add(availableRoomCombo);

            java.util.List<Integer> roomIds = new java.util.ArrayList<>();
            java.util.List<Double> roomRates = new java.util.ArrayList<>();

            // Load available rooms dynamically
            ActionListener updateRoomsAction = e -> {
                availableRoomCombo.removeAllItems();
                roomIds.clear();
                roomRates.clear();

                int typeId = roomTypeCombo.getSelectedIndex() + 1;

                try (Connection conn = DBConnection.getConnection()) {
                    PreparedStatement ps = conn.prepareStatement(
                        "SELECT r.room_id, r.room_number, rt.rate_per_day " +
                        "FROM rooms r JOIN room_types rt ON r.room_type_id = rt.room_type_id " +
                        "WHERE r.room_type_id = ? AND r.is_available = 1 AND r.is_maintenance = 0");
                    ps.setInt(1, typeId);
                    ResultSet rs = ps.executeQuery();

                    boolean found = false;
                    while (rs.next()) {
                        availableRoomCombo.addItem("Room " + rs.getString("room_number"));
                        roomIds.add(rs.getInt("room_id"));
                        roomRates.add(rs.getDouble("rate_per_day"));
                        found = true;
                    }
                    if (!found) availableRoomCombo.addItem("No rooms available");
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            };

            roomTypeCombo.addActionListener(updateRoomsAction);
            updateRoomsAction.actionPerformed(null);

            if (JOptionPane.showConfirmDialog(this, panel, "Upgrade Room", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                String selectedRoom = (String) availableRoomCombo.getSelectedItem();
                if (selectedRoom == null || selectedRoom.equals("No rooms available")) {
                    JOptionPane.showMessageDialog(this, "Invalid room selection.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                int selectedIndex = availableRoomCombo.getSelectedIndex();
                int newRoomId = roomIds.get(selectedIndex);
                double newRate = roomRates.get(selectedIndex);

                double oldRate = getRateForType(oldRoomType);
                double diff = (newRate - oldRate) * days;
                double newTotal = currentTotal + diff;

                if (JOptionPane.showConfirmDialog(this,
                    "Price Difference: ₱" + String.format("%,.2f", diff) + "\nNew Total: ₱" + String.format("%,.2f", newTotal) + "\nProceed?",
                    "Confirm Upgrade", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {

                    try (Connection conn = DBConnection.getConnection()) {
                        conn.setAutoCommit(false);

                        // Update booking
                        PreparedStatement ps = conn.prepareStatement(
                            "UPDATE bookings SET room_id = ?, total_amount = ? WHERE booking_id = ?");
                        ps.setInt(1, newRoomId);
                        ps.setDouble(2, newTotal);
                        ps.setInt(3, bookingId);
                        ps.executeUpdate();

                        // Free old room
                        PreparedStatement freeOld = conn.prepareStatement(
                            "UPDATE rooms SET is_available = 1 WHERE room_number = ?");
                        freeOld.setString(1, oldRoomNum);
                        freeOld.executeUpdate();

                        // Occupy new room
                        PreparedStatement occupyNew = conn.prepareStatement(
                            "UPDATE rooms SET is_available = 0 WHERE room_id = ?");
                        occupyNew.setInt(1, newRoomId);
                        occupyNew.executeUpdate();

                        conn.commit();
                        JOptionPane.showMessageDialog(this, "Room upgraded successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                        loadBookings();
                    }
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Reschedule - with fee calculation and validation
     */
    private void rescheduleBooking() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a booking.");
            return;
        }
        if (!tableModel.getValueAt(row, 8).equals("Reserved")) {
            JOptionPane.showMessageDialog(this, "Only 'Reserved' bookings can be rescheduled.");
            return;
        }

        int bookingId = (int) tableModel.getValueAt(row, 0);
        double dailyRate = getRateForType((String) tableModel.getValueAt(row, 3));

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date originalCheckIn = sdf.parse((String) tableModel.getValueAt(row, 4));
            Date originalCheckOut = sdf.parse((String) tableModel.getValueAt(row, 5));

            SpinnerDateModel checkInModel = new SpinnerDateModel(originalCheckIn, originalCheckIn, null, Calendar.DAY_OF_MONTH);
            SpinnerDateModel checkOutModel = new SpinnerDateModel(originalCheckOut, originalCheckIn, null, Calendar.DAY_OF_MONTH);

            JSpinner checkInSpinner = new JSpinner(checkInModel);
            JSpinner checkOutSpinner = new JSpinner(checkOutModel);
            checkInSpinner.setEditor(new JSpinner.DateEditor(checkInSpinner, "yyyy-MM-dd"));
            checkOutSpinner.setEditor(new JSpinner.DateEditor(checkOutSpinner, "yyyy-MM-dd"));

            JPanel datePanel = new JPanel(new GridLayout(2, 2, 10, 10));
            datePanel.add(new JLabel("New Check-In Date:")); datePanel.add(checkInSpinner);
            datePanel.add(new JLabel("New Check-Out Date:")); datePanel.add(checkOutSpinner);

            if (JOptionPane.showConfirmDialog(this, datePanel, "Pick New Dates", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                Date in = (Date) checkInSpinner.getValue();
                Date out = (Date) checkOutSpinner.getValue();

                // Strip time
                Calendar calIn = Calendar.getInstance(); calIn.setTime(in);
                calIn.set(Calendar.HOUR_OF_DAY, 0); calIn.set(Calendar.MINUTE, 0); calIn.set(Calendar.SECOND, 0); calIn.set(Calendar.MILLISECOND, 0);
                Calendar calOut = Calendar.getInstance(); calOut.setTime(out);
                calOut.set(Calendar.HOUR_OF_DAY, 0); calOut.set(Calendar.MINUTE, 0); calOut.set(Calendar.SECOND, 0); calOut.set(Calendar.MILLISECOND, 0);

                long days = (calOut.getTimeInMillis() - calIn.getTimeInMillis()) / (1000 * 60 * 60 * 24);
                if (days <= 0) {
                    JOptionPane.showMessageDialog(this, "Check-out must be after check-in.");
                    return;
                }

                double fee = dailyRate * 0.15;
                double newTotal = (dailyRate * days) + fee;

                int confirm = JOptionPane.showConfirmDialog(this,
                    String.format("Reschedule Fee: ₱%,.2f\nNew Total: ₱%,.2f\nConfirm?", fee, newTotal),
                    "Confirm", JOptionPane.YES_NO_OPTION);

                if (confirm == JOptionPane.YES_OPTION) {
                    try (Connection conn = DBConnection.getConnection()) {
                        PreparedStatement ps = conn.prepareStatement(
                            "UPDATE bookings SET check_in_date=?, check_out_date=?, total_amount=?, reschedule_charge=?, status='Reserved' WHERE booking_id=?");
                        ps.setDate(1, new java.sql.Date(calIn.getTimeInMillis()));
                        ps.setDate(2, new java.sql.Date(calOut.getTimeInMillis()));
                        ps.setDouble(3, newTotal);
                        ps.setDouble(4, fee);
                        ps.setInt(5, bookingId);
                        ps.executeUpdate();
                        JOptionPane.showMessageDialog(this, "Booking rescheduled!", "Success", JOptionPane.INFORMATION_MESSAGE);
                        loadBookings();
                    }
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Cancel Booking - with room freeing
     */
    private void cancelBooking() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a booking.");
            return;
        }
        if (!tableModel.getValueAt(row, 8).equals("Reserved")) {
            JOptionPane.showMessageDialog(this, "Only 'Reserved' bookings can be cancelled.");
            return;
        }

        int bookingId = (int) tableModel.getValueAt(row, 0);
        String customer = (String) tableModel.getValueAt(row, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
            "Cancel booking for " + customer + "?\nThis will free the room.",
            "Confirm Cancellation", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) return;

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            PreparedStatement ps = conn.prepareStatement(
                "UPDATE bookings SET status = 'Cancelled' WHERE booking_id = ?");
            ps.setInt(1, bookingId);
            ps.executeUpdate();

            PreparedStatement roomPs = conn.prepareStatement(
                "UPDATE rooms r JOIN bookings b ON b.room_id = r.room_id SET r.is_available = 1 WHERE b.booking_id = ? AND r.is_maintenance = 0");
            roomPs.setInt(1, bookingId);
            roomPs.executeUpdate();

            conn.commit();
            JOptionPane.showMessageDialog(this, "Booking cancelled! Room is now available.", "Cancelled", JOptionPane.INFORMATION_MESSAGE);
            loadBookings();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    /**
     * Print receipt for selected booking
     */
    private void printReceiptForBooking() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a booking.");
            return;
        }

        int bookingId = (int) tableModel.getValueAt(row, 0);

        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT b.*, CONCAT(c.first_name, ' ', c.last_name) as customer, " +
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
                receipt.append("CUSTOMER\n");
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
                receipt.append("GUESTS\n");
                receipt.append("----------------------------------------\n");
                receipt.append("Adults:      ").append(rs.getInt("adults")).append("\n");
                receipt.append("Seniors/PWD: ").append(rs.getInt("seniors")).append("\n");
                receipt.append("Kids:        ").append(rs.getInt("kids")).append("\n\n");
                receipt.append("----------------------------------------\n");
                receipt.append("CHARGES\n");
                receipt.append("----------------------------------------\n");
                if (rs.getDouble("extra_guest_charge") > 0) {
                    receipt.append(String.format("%-25s ₱%,10.2f\n", "Extra Guest:", rs.getDouble("extra_guest_charge")));
                }
                if (rs.getDouble("senior_discount") > 0) {
                    receipt.append(String.format("%-25s -₱%,9.2f\n", "Senior Discount:", rs.getDouble("senior_discount")));
                }
                if (rs.getDouble("reschedule_charge") > 0) {
                    receipt.append(String.format("%-25s ₱%,10.2f\n", "Reschedule Fee:", rs.getDouble("reschedule_charge")));
                }
                receipt.append("----------------------------------------\n");
                receipt.append(String.format("%-25s ₱%,10.2f\n", "TOTAL:", rs.getDouble("total_amount")));
                receipt.append("----------------------------------------\n\n");
                receipt.append("Thank you for choosing Sync Suites Hotel!\n");
                receipt.append("THIS IS AN OFFICIAL RECEIPT\n");

                // Show receipt dialog
                JTextArea area = new JTextArea(receipt.toString());
                area.setFont(new Font("Monospaced", Font.PLAIN, 12));
                area.setEditable(false);
                area.setBackground(new Color(255, 255, 240));

                JScrollPane scroll = new JScrollPane(area);
                scroll.setPreferredSize(new Dimension(450, 500));

                Object[] options = {"Print", "Save to Records", "Close"};
                int choice = JOptionPane.showOptionDialog(this, scroll, "Receipt",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[1]);

                if (choice == 0) { // Print
                    try {
                        area.print();
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(this, "Print error: " + ex.getMessage());
                    }
                } else if (choice == 1) { // Save
                    // Save to receipts table
                    PreparedStatement savePs = conn.prepareStatement(
                        "INSERT INTO receipts (booking_id, customer_id, receipt_number, receipt_data, total_amount, printed_by) " +
                        "VALUES (?,?,?,?,?,?)");
                    savePs.setInt(1, bookingId);
                    savePs.setInt(2, rs.getInt("customer_id"));
                    savePs.setString(3, receiptNum);
                    savePs.setString(4, receipt.toString());
                    savePs.setDouble(5, rs.getDouble("total_amount"));
                    savePs.setString(6, LoginFrame.currentUserName);
                    savePs.executeUpdate();

                    JOptionPane.showMessageDialog(this, "Receipt saved to records!", "Saved", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    /**
     * Get rate for room type
     */
    private double getRateForType(String typeName) {
        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("SELECT rate_per_day FROM room_types WHERE type_name = ?");
            ps.setString(1, typeName);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble("rate_per_day");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    /**
     * Show filter dialog
     */
    private void showFilterDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Filter Bookings", true);
        dialog.setSize(350, 220);
        dialog.setLocationRelativeTo(this);

        JPanel form = new JPanel(new GridLayout(3, 2, 10, 10));
        form.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        String[] statusList = {"All", "Reserved", "Checked In", "Checked Out", "Cancelled"};
        JComboBox<String> cboStatus = new JComboBox<>(statusList);
        cboStatus.setSelectedItem(filterStatus);

        String[] paymentList = {"All", "Cash", "GCash/QR Scan", "QR PH"};
        JComboBox<String> cboPayment = new JComboBox<>(paymentList);
        cboPayment.setSelectedItem(filterPayment);

        String[] typeList = {"All", "Standard Room", "Junior Suite", "Executive Suite", "Presidential Suite"};
        JComboBox<String> cboType = new JComboBox<>(typeList);
        cboType.setSelectedItem(filterRoomType);

        form.add(new JLabel("Status:")); form.add(cboStatus);
        form.add(new JLabel("Payment:")); form.add(cboPayment);
        form.add(new JLabel("Room Type:")); form.add(cboType);

        JPanel buttons = new JPanel();
        JButton btnApply = new JButton("Apply");
        JButton btnClear = new JButton("Clear");
        btnApply.setBackground(new Color(46, 194, 126)); btnApply.setForeground(Color.WHITE);
        btnClear.setBackground(new Color(128, 128, 128)); btnClear.setForeground(Color.WHITE);
        buttons.add(btnApply);
        buttons.add(btnClear);

        dialog.getContentPane().add(form, BorderLayout.CENTER);
        dialog.getContentPane().add(buttons, BorderLayout.SOUTH);

        btnApply.addActionListener(e -> {
            filterStatus = (String) cboStatus.getSelectedItem();
            filterPayment = (String) cboPayment.getSelectedItem();
            filterRoomType = (String) cboType.getSelectedItem();
            loadBookings();
            dialog.dispose();
        });

        btnClear.addActionListener(e -> {
            filterStatus = "All";
            filterPayment = "All";
            filterRoomType = "All";
            loadBookings();
            dialog.dispose();
        });

        dialog.setVisible(true);
    }
}