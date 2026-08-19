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
 * Customer Portal Frame
 * Tabs: My Bookings | My Receipts | Book a Room (self-booking → status = 'Reserved')
 *
 * Self-booking flow:
 *  1. Customer picks a room type → we check how many rooms of that type are free for the dates
 *  2. Customer picks check-in / check-out dates → total amount auto-calculated
 *  3. On confirm → an available room of that type is auto-assigned in the background and the
 *     booking is inserted with status = 'Reserved' (admin sees it in Manage Booking)
 *  4. SMS confirmation sent via ServicesManager
 *
 *  NOTE: Customers never choose a specific room number. Room numbers are only handed out by
 *  staff at check-in (front desk), since physical key/keycard assignment happens on arrival.
 *  The system still picks *a* concrete room_id behind the scenes so availability stays accurate,
 *  but that detail is intentionally hidden from the customer-facing UI.
 */
public class CustomerPortalFrame extends JFrame {

    private int customerId;
    private JTabbedPane tabbedPane;
    private JLabel lblWelcome;

    // Self-booking fields (kept as instance vars so inner methods can reach them)
    private JComboBox<String> cbRoomType;
    private JLabel lblRoomAvailability;   // shows count only — customer does NOT pick a room number
    private JSpinner spCheckIn;
    private JSpinner spCheckOut;
    private JTextField txtSelfTotal;
    private JButton btnBook;

    // Room data mirrors NewBookingPanel
    private final String[] ROOM_TYPE_NAMES  = {"Standard Room", "Junior Suite", "Executive Suite", "Presidential Suite"};
    private final double[] BASE_PRICES       = {2500.0, 3500.0, 5000.0, 8000.0};

    public CustomerPortalFrame(int customerId) {
        this.customerId = customerId;
        initialize();
    }

    // ─────────────────────────────────────────────────────────────
    // INIT
    // ─────────────────────────────────────────────────────────────
    private void initialize() {
        setTitle("Sync Suites Hotel - Customer Portal");
        setSize(900, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setLayout(new BorderLayout());

        // ── Header ──────────────────────────────────────────────
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(39, 0, 110));
        headerPanel.setPreferredSize(new Dimension(900, 60));

        lblWelcome = new JLabel("  Welcome!");
        lblWelcome.setFont(new Font("SansSerif", Font.BOLD, 20));
        lblWelcome.setForeground(new Color(255, 215, 0));
        headerPanel.add(lblWelcome, BorderLayout.WEST);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBackground(new Color(39, 0, 110));

        JButton btnProfile = new JButton("👤 My Profile");
        btnProfile.setForeground(Color.WHITE);
        btnProfile.setBackground(new Color(70, 130, 180));
        btnProfile.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnProfile.addActionListener(e -> showProfileDialog());
        btnPanel.add(btnProfile);

        JButton btnLogout = new JButton("Logout");
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setBackground(new Color(224, 27, 36));
        btnLogout.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnLogout.addActionListener(e -> {
            LoginFrame.currentUserId  = -1;
            LoginFrame.currentUserType = "";
            LoginFrame.currentUserName = "";
            dispose();
            new LoginFrame();
        });
        btnPanel.add(btnLogout);

        headerPanel.add(btnPanel, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        loadCustomerInfo();

        // ── Tabs ─────────────────────────────────────────────────
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("SansSerif", Font.BOLD, 13));

        tabbedPane.addTab("📋 My Bookings",  createMyBookingsPanel());
        tabbedPane.addTab("🧾 My Receipts",  createMyReceiptsPanel());
        tabbedPane.addTab("🏨 Book a Room",  createSelfBookingPanel());   // ← NEW

        add(tabbedPane, BorderLayout.CENTER);
        setVisible(true);
    }

    // ─────────────────────────────────────────────────────────────
    // LOAD CUSTOMER NAME
    // ─────────────────────────────────────────────────────────────
    private void loadCustomerInfo() {
        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT first_name, last_name, total_visits FROM customers WHERE customer_id = ?");
            ps.setInt(1, customerId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String name   = rs.getString("first_name") + " " + rs.getString("last_name");
                int    visits = rs.getInt("total_visits");
                lblWelcome.setText("  Welcome, " + name + "! (Visits: " + visits + ")");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ─────────────────────────────────────────────────────────────
    // TAB 1 – MY BOOKINGS  (unchanged)
    // ─────────────────────────────────────────────────────────────
    private JPanel createMyBookingsPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        String[] columns = {"Booking ID", "Room", "Type", "Check-In", "Check-Out", "Total", "Status"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable table = new JTable(model);
        table.setRowHeight(28);
        table.setFont(new Font("SansSerif", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setBackground(new Color(31, 71, 145));
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        loadMyBookings(model);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        JButton btnRefresh = new JButton("🔄 Refresh");
        btnRefresh.setBackground(new Color(70, 130, 180));
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.addActionListener(e -> loadMyBookings(model));
        btnPanel.add(btnRefresh);

        JButton btnCancel = new JButton("❌ Cancel Booking");
        btnCancel.setBackground(new Color(224, 27, 36));
        btnCancel.setForeground(Color.WHITE);
        btnCancel.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) { JOptionPane.showMessageDialog(panel, "Please select a booking to cancel."); return; }
            int    bookingId = (int)    model.getValueAt(row, 0);
            String status    = (String) model.getValueAt(row, 6);

            if (!status.equals("Reserved")) {
                JOptionPane.showMessageDialog(panel, "Only 'Reserved' bookings can be cancelled.");
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(panel,
                "Cancel Booking #" + bookingId + "?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try (Connection conn = DBConnection.getConnection()) {
                    conn.setAutoCommit(false);
                    PreparedStatement ps1 = conn.prepareStatement(
                        "UPDATE bookings SET status = 'Cancelled' WHERE booking_id = ?");
                    ps1.setInt(1, bookingId);
                    ps1.executeUpdate();
                    conn.commit();
                    JOptionPane.showMessageDialog(panel, "Booking #" + bookingId + " cancelled.");
                    loadMyBookings(model);
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(panel, "Error: " + ex.getMessage());
                }
            }
        });
        btnPanel.add(btnCancel);

        panel.add(btnPanel, BorderLayout.SOUTH);
        return panel;
    }

    private void loadMyBookings(DefaultTableModel model) {
        model.setRowCount(0);
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT b.booking_id, r.room_number, rt.type_name, " +
                         "b.check_in_date, b.check_out_date, b.total_amount, b.status " +
                         "FROM bookings b " +
                         "JOIN rooms r  ON b.room_id = r.room_id " +
                         "JOIN room_types rt ON r.room_type_id = rt.room_type_id " +
                         "WHERE b.customer_id = ? ORDER BY b.created_at DESC";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, customerId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("booking_id"),
                    rs.getString("room_number"),
                    rs.getString("type_name"),
                    rs.getString("check_in_date"),
                    rs.getString("check_out_date"),
                    "₱" + String.format("%,.2f", rs.getDouble("total_amount")),
                    rs.getString("status")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading bookings: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────
    // TAB 2 – MY RECEIPTS  (unchanged)
    // ─────────────────────────────────────────────────────────────
    private JPanel createMyReceiptsPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        String[] columns = {"Receipt #", "Booking ID", "Amount", "Date"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable table = new JTable(model);
        table.setRowHeight(28);
        table.setFont(new Font("SansSerif", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setBackground(new Color(100, 50, 150));
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT receipt_number, booking_id, total_amount, printed_at " +
                "FROM receipts WHERE customer_id = ? ORDER BY printed_at DESC");
            ps.setInt(1, customerId);
            ResultSet rs = ps.executeQuery();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getString("receipt_number"),
                    rs.getInt("booking_id"),
                    "₱" + String.format("%,.2f", rs.getDouble("total_amount")),
                    sdf.format(rs.getTimestamp("printed_at"))
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(panel, "Error loading receipts: " + e.getMessage());
        }

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        JButton btnView = new JButton("👁 View Receipt");
        btnView.setBackground(new Color(70, 130, 180));
        btnView.setForeground(Color.WHITE);
        btnView.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) { JOptionPane.showMessageDialog(panel, "Please select a receipt."); return; }
            String receiptNum = (String) model.getValueAt(row, 0);
            try (Connection conn = DBConnection.getConnection()) {
                PreparedStatement ps = conn.prepareStatement(
                    "SELECT receipt_data FROM receipts WHERE receipt_number = ?");
                ps.setString(1, receiptNum);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    JTextArea area = new JTextArea(rs.getString("receipt_data"));
                    area.setFont(new Font("Monospaced", Font.PLAIN, 12));
                    area.setEditable(false);
                    JScrollPane sp = new JScrollPane(area);
                    sp.setPreferredSize(new Dimension(450, 500));
                    JOptionPane.showMessageDialog(panel, sp, "Receipt", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(panel, "Error: " + ex.getMessage());
            }
        });
        btnPanel.add(btnView);

        panel.add(btnPanel, BorderLayout.SOUTH);
        return panel;
    }

    // ─────────────────────────────────────────────────────────────
    // TAB 3 – SELF BOOKING  (NEW)
    // ─────────────────────────────────────────────────────────────
    private JPanel createSelfBookingPanel() {
        JPanel panel = new JPanel(null);   // absolute layout keeps it simple
        panel.setBackground(new Color(248, 248, 255));

        // ── Title ────────────────────────────────────────────────
        JLabel title = new JLabel("Book a Room");
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setForeground(new Color(0, 0, 68));
        title.setBounds(40, 20, 300, 28);
        panel.add(title);

        JLabel subtitle = new JLabel("Fill in the details below. Your booking will be marked as Reserved.");
        subtitle.setFont(new Font("SansSerif", Font.ITALIC, 12));
        subtitle.setForeground(new Color(100, 100, 140));
        subtitle.setBounds(40, 50, 600, 18);
        panel.add(subtitle);

        int lx = 60, fx = 220, fw = 280, fy = 90, gap = 38;

        // ── Room Type ────────────────────────────────────────────
        panel.add(boldLabel("Room Type:", lx, fy));
        cbRoomType = new JComboBox<>(new String[]{
            "Standard Room  –  ₱2,500 / day",
            "Junior Suite  –  ₱3,500 / day",
            "Executive Suite  –  ₱5,000 / day",
            "Presidential Suite  –  ₱8,000 / day"
        });
        cbRoomType.setBounds(fx, fy, fw, 26);
        cbRoomType.setFont(new Font("SansSerif", Font.PLAIN, 13));
        panel.add(cbRoomType);
        fy += gap;

        // ── Room Availability (info only — room number is assigned by staff at check-in) ──
        panel.add(boldLabel("Availability:", lx, fy));
        lblRoomAvailability = new JLabel("Checking...");
        lblRoomAvailability.setBounds(fx, fy, fw, 26);
        lblRoomAvailability.setFont(new Font("SansSerif", Font.BOLD, 13));
        panel.add(lblRoomAvailability);
        fy += gap;

        // ── Check-in Date ────────────────────────────────────────
        panel.add(boldLabel("Check-In Date:", lx, fy));
        spCheckIn  = makeDateSpinner(0);
        spCheckIn.setBounds(fx, fy, fw, 26);
        panel.add(spCheckIn);
        fy += gap;

        // ── Check-out Date ───────────────────────────────────────
        panel.add(boldLabel("Check-Out Date:", lx, fy));
        spCheckOut = makeDateSpinner(1);    // default: tomorrow + 1
        spCheckOut.setBounds(fx, fy, fw, 26);
        panel.add(spCheckOut);
        fy += gap;

        // ── Total Amount (read-only) ─────────────────────────────
        panel.add(boldLabel("Total Amount:", lx, fy));
        txtSelfTotal = new JTextField("₱0.00");
        txtSelfTotal.setBounds(fx, fy, fw, 26);
        txtSelfTotal.setFont(new Font("SansSerif", Font.BOLD, 13));
        txtSelfTotal.setEditable(false);
        txtSelfTotal.setBackground(new Color(230, 255, 230));
        txtSelfTotal.setForeground(new Color(0, 100, 0));
        panel.add(txtSelfTotal);
        fy += gap + 10;

        // ── Note label ───────────────────────────────────────────
        JLabel noteLabel = new JLabel(
            "<html><i>Note: Your room number will be assigned by our staff upon check-in. " +
            "Payment will be settled upon check-in. " +
            "You may cancel from 'My Bookings' anytime before check-in.</i></html>");
        noteLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        noteLabel.setForeground(new Color(120, 80, 0));
        noteLabel.setBounds(40, fy, 560, 34);
        panel.add(noteLabel);
        fy += 44;

        // ── Confirm Button ───────────────────────────────────────
        btnBook = new JButton("✅  Confirm Booking");
        btnBook.setBounds(fx, fy, fw, 36);
        btnBook.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnBook.setBackground(new Color(46, 194, 126));
        btnBook.setForeground(Color.WHITE);
        btnBook.setFocusPainted(false);
        btnBook.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBook.addActionListener(e -> confirmSelfBooking());
        panel.add(btnBook);

        // ── Wire up listeners ────────────────────────────────────
        // Room type → recheck availability + recalc total
        cbRoomType.addActionListener(e -> {
            refreshSelfAvailability();
            calcSelfTotal();
        });

        // Date changes → recalc total + recheck availability (dates affect overlap check)
        ChangeListener dateListener = e -> {
            refreshSelfAvailability();
            calcSelfTotal();
        };
        spCheckIn.addChangeListener(dateListener);
        spCheckOut.addChangeListener(dateListener);

        // Initial load
        refreshSelfAvailability();
        calcSelfTotal();

        return panel;
    }

    /**
     * Check how many rooms of the selected type are free for the selected dates.
     * Does NOT let the customer pick a specific room — that stays an admin/front-desk action.
     * Updates the availability label and enables/disables the Confirm button accordingly.
     */
    private void refreshSelfAvailability() {
        int typeIndex = cbRoomType.getSelectedIndex();
        String typeName = ROOM_TYPE_NAMES[typeIndex];

        Date checkIn  = (Date) spCheckIn.getValue();
        Date checkOut = (Date) spCheckOut.getValue();

        if (!checkOut.after(checkIn)) {
            lblRoomAvailability.setText("Invalid dates");
            lblRoomAvailability.setForeground(new Color(224, 27, 36));
            btnBook.setEnabled(false);
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        try (Connection conn = DBConnection.getConnection()) {
            /*
             * A room of this type counts as available when:
             *  - It is NOT in maintenance mode
             *  - It has NO overlapping Reserved/Checked-In booking in the requested period
             */
            String sql =
                "SELECT COUNT(*) AS free_count " +
                "FROM rooms r " +
                "JOIN room_types rt ON r.room_type_id = rt.room_type_id " +
                "WHERE rt.type_name = ? " +
                "  AND r.is_maintenance = 0 " +
                "  AND r.room_id NOT IN (" +
                "      SELECT b.room_id FROM bookings b " +
                "      WHERE b.status IN ('Reserved','Checked In') " +
                "        AND b.check_in_date  < ? " +
                "        AND b.check_out_date > ? " +
                "  )";

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, typeName);
            ps.setString(2, sdf.format(checkOut));
            ps.setString(3, sdf.format(checkIn));
            ResultSet rs = ps.executeQuery();

            int freeCount = 0;
            if (rs.next()) freeCount = rs.getInt("free_count");

            if (freeCount > 0) {
                lblRoomAvailability.setText(freeCount + " room(s) available");
                lblRoomAvailability.setForeground(new Color(0, 128, 0));
                btnBook.setEnabled(true);
            } else {
                lblRoomAvailability.setText("Fully booked for selected dates");
                lblRoomAvailability.setForeground(new Color(224, 27, 36));
                btnBook.setEnabled(false);
            }
        } catch (SQLException e) {
            lblRoomAvailability.setText("Error checking availability");
            lblRoomAvailability.setForeground(new Color(224, 27, 36));
            btnBook.setEnabled(false);
            JOptionPane.showMessageDialog(this, "Error checking availability: " + e.getMessage());
        }
    }

    /**
     * Pick ONE concrete available room_id of the chosen type for the chosen dates.
     * This is purely an internal bookkeeping detail — the room number is intentionally
     * never shown to the customer. Front-desk staff assign the actual room/keycard at check-in.
     * Returns -1 if none are available (caller should re-check before calling this).
     */
    private int pickAvailableRoomId(String typeName, Date checkIn, Date checkOut) throws SQLException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try (Connection conn = DBConnection.getConnection()) {
            String sql =
                "SELECT r.room_id " +
                "FROM rooms r " +
                "JOIN room_types rt ON r.room_type_id = rt.room_type_id " +
                "WHERE rt.type_name = ? " +
                "  AND r.is_maintenance = 0 " +
                "  AND r.room_id NOT IN (" +
                "      SELECT b.room_id FROM bookings b " +
                "      WHERE b.status IN ('Reserved','Checked In') " +
                "        AND b.check_in_date  < ? " +
                "        AND b.check_out_date > ? " +
                "  ) " +
                "ORDER BY r.room_id LIMIT 1";

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, typeName);
            ps.setString(2, sdf.format(checkOut));
            ps.setString(3, sdf.format(checkIn));
            ResultSet rs = ps.executeQuery();

            if (rs.next()) return rs.getInt("room_id");
            return -1;
        }
    }

    /** Calculate and display total based on type and dates */
    private void calcSelfTotal() {
        Date checkIn  = (Date) spCheckIn.getValue();
        Date checkOut = (Date) spCheckOut.getValue();
        if (!checkOut.after(checkIn)) {
            txtSelfTotal.setText("Invalid dates");
            return;
        }
        long days = (checkOut.getTime() - checkIn.getTime()) / (1000 * 60 * 60 * 24);
        double rate  = BASE_PRICES[cbRoomType.getSelectedIndex()];
        double total = days * rate;
        txtSelfTotal.setText("₱" + String.format("%,.2f", total) + "  (" + days + " night" + (days > 1 ? "s" : "") + ")");
    }

    /** Insert booking with status = 'Reserved'. Room number is auto-assigned, not chosen by the customer. */
    private void confirmSelfBooking() {
        int typeIndex = cbRoomType.getSelectedIndex();
        String typeName = ROOM_TYPE_NAMES[typeIndex];

        // ── Validate dates ───────────────────────────────────────
        Date checkIn  = (Date) spCheckIn.getValue();
        Date checkOut = (Date) spCheckOut.getValue();
        if (!checkOut.after(checkIn)) {
            JOptionPane.showMessageDialog(this, "Check-out must be after check-in."); return;
        }

        // ── Auto-assign an available room of the chosen type ─────
        // (The customer never sees or chooses a room number — that's handled by staff at check-in.)
        int roomId;
        try {
            roomId = pickAvailableRoomId(typeName, checkIn, checkOut);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error checking availability: " + ex.getMessage()); return;
        }
        if (roomId == -1) {
            JOptionPane.showMessageDialog(this,
                "Sorry, no rooms of this type are available for the selected dates.",
                "Fully Booked", JOptionPane.WARNING_MESSAGE);
            refreshSelfAvailability();
            return;
        }

        long   nights = (checkOut.getTime() - checkIn.getTime()) / (1000 * 60 * 60 * 24);
        double rate   = BASE_PRICES[typeIndex];
        double total  = nights * rate;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String ciStr = sdf.format(checkIn);
        String coStr = sdf.format(checkOut);

        // ── Confirm dialog ───────────────────────────────────────
        int confirm = JOptionPane.showConfirmDialog(this,
            "<html>Room Type: <b>" + typeName + "</b><br>" +
            "Check-In: <b>" + ciStr + "</b><br>" +
            "Check-Out: <b>" + coStr + "</b><br>" +
            "Total: <b>₱" + String.format("%,.2f", total) + "</b><br><br>" +
            "Your exact room number will be assigned by our staff at check-in.<br>" +
            "Payment will be collected upon check-in.<br>Confirm booking?</html>",
            "Confirm Your Booking", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) return;

        // ── Insert into DB ───────────────────────────────────────
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO bookings " +
                "(customer_id, room_id, check_in_date, check_out_date, " +
                " total_amount, status, payment_method, " +
                " num_adults, num_seniors_pwd, num_kids, " +
                " senior_discount, extra_charges, created_at) " +
                "VALUES (?, ?, ?, ?, ?, 'Reserved', 'Pay at Check-In', " +
                " 1, 0, 0, 0.00, 0.00, NOW())",
                java.sql.Statement.RETURN_GENERATED_KEYS);

            ps.setInt(1, customerId);
            ps.setInt(2, roomId);
            ps.setString(3, ciStr);
            ps.setString(4, coStr);
            ps.setDouble(5, total);
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            int bookingId = 0;
            if (keys.next()) bookingId = keys.getInt(1);

            // Update visit count
            PreparedStatement upd = conn.prepareStatement(
                "UPDATE customers SET total_visits = total_visits + 1 WHERE customer_id = ?");
            upd.setInt(1, customerId);
            upd.executeUpdate();

            conn.commit();

            final int finalBookingId = bookingId;
            JOptionPane.showMessageDialog(this,
                "Booking confirmed!\nBooking ID: " + finalBookingId + "\n\n" +
                "Status: Reserved\nOur staff will contact you to confirm.",
                "Booking Successful", JOptionPane.INFORMATION_MESSAGE);

            // ── SMS notification ─────────────────────────────────
            try (Connection c2 = DBConnection.getConnection()) {
                PreparedStatement info = c2.prepareStatement(
                    "SELECT first_name, phone_number FROM customers WHERE customer_id = ?");
                info.setInt(1, customerId);
                ResultSet ri = info.executeQuery();
                if (ri.next()) {
                    String name  = ri.getString("first_name");
                    String phone = ri.getString("phone_number");
                    String msg   = "Hi " + name + "! Booking #" + finalBookingId +
                                   " confirmed at Sync Suites Hotel. " +
                                   "Check-in: " + ciStr + ". Total: PHP " +
                                   String.format("%,.2f", total) + ". Thank you!";
                    ServicesManager.sendSMSAlert(phone, msg);
                }
            } catch (Exception smsEx) {
                System.err.println("SMS failed: " + smsEx.getMessage());
            }

            // ── Reset form ───────────────────────────────────────
            cbRoomType.setSelectedIndex(0);
            spCheckIn.setValue(tomorrow(0));
            spCheckOut.setValue(tomorrow(1));
            refreshSelfAvailability();
            calcSelfTotal();

        } catch (SQLException ex) {
            if (conn != null) try { conn.rollback(); } catch (SQLException ignore) {}
            JOptionPane.showMessageDialog(this,
                "Booking failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            if (conn != null) try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignore) {}
        }
    }

    // ─────────────────────────────────────────────────────────────
    // PROFILE DIALOG  (unchanged)
    // ─────────────────────────────────────────────────────────────
    private void showProfileDialog() {
        JDialog dialog = new JDialog(this, "My Profile", true);
        dialog.setSize(400, 350);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setLayout(null);
        dialog.getContentPane().setBackground(new Color(46, 44, 122));

        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT c.*, u.username, u.email as user_email FROM customers c " +
                "JOIN users u ON c.customer_id = u.customer_id WHERE c.customer_id = ?");
            ps.setInt(1, customerId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                int y = 20;
                String[][] fields = {
                    {"First Name:",    rs.getString("first_name")},
                    {"Last Name:",     rs.getString("last_name")},
                    {"Phone:",         rs.getString("phone_number")},
                    {"Email:",         rs.getString("email")},
                    {"Username:",      rs.getString("username")},
                    {"Total Visits:",  String.valueOf(rs.getInt("total_visits"))},
                    {"Member Since:",  rs.getString("created_at")}
                };
                for (String[] field : fields) {
                    JLabel lbl = new JLabel(field[0]);
                    lbl.setBounds(30, y, 120, 25);
                    lbl.setFont(new Font("SansSerif", Font.BOLD, 13));
                    lbl.setForeground(Color.WHITE);
                    dialog.getContentPane().add(lbl);

                    JLabel val = new JLabel(field[1] != null ? field[1] : "");
                    val.setBounds(160, y, 200, 25);
                    val.setFont(new Font("SansSerif", Font.PLAIN, 13));
                    val.setForeground(new Color(255, 215, 0));
                    dialog.getContentPane().add(val);
                    y += 35;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        dialog.setVisible(true);
    }

    // ─────────────────────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────────────────────

    /** Bold label helper */
    private JLabel boldLabel(String text, int x, int y) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 13));
        lbl.setBounds(x, y, 155, 26);
        return lbl;
    }

    /** Date spinner starting N days from today */
    private JSpinner makeDateSpinner(int offsetDays) {
        SpinnerDateModel model = new SpinnerDateModel(tomorrow(offsetDays), null, null, Calendar.DAY_OF_MONTH);
        JSpinner sp = new JSpinner(model);
        sp.setEditor(new JSpinner.DateEditor(sp, "yyyy-MM-dd"));
        sp.setFont(new Font("SansSerif", Font.PLAIN, 13));
        return sp;
    }

    /** Returns a Date = today + offsetDays days (time zeroed) */
    private Date tomorrow(int offsetDays) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 1 + offsetDays);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    // javax.swing.event.ChangeListener shorthand (functional interface)
    @FunctionalInterface
    interface ChangeListener extends javax.swing.event.ChangeListener {
        void stateChanged(javax.swing.event.ChangeEvent e);
    }
}