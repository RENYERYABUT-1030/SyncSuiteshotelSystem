package Hotel_Reservation;

import Hotel_Reservation.core.EnhancedDBConnection;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;
import java.awt.event.*;

/**
 * Enhanced Room Availability Panel
 * Features: Color-coded status, maintenance mode indicator, refresh
 * Bug fixes: Proper status detection, handles multiple bookings correctly
 */
public class RoomAvailabilityPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private JTable table;
    private DefaultTableModel tableModel;

    public RoomAvailabilityPanel() {
        setLayout(new BorderLayout());

        // Title
        JLabel titleLabel = new JLabel("Room Availability Overview");
        titleLabel.setForeground(new Color(0, 0, 68));
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        titleLabel.setBorder(new EmptyBorder(15, 15, 10, 0));
        add(titleLabel, BorderLayout.NORTH);

        // Table
        String[] columns = {"Room No.", "Room Type", "Rate / Day (₱)", "Floor", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        JScrollPane scrollPane = new JScrollPane();
        add(scrollPane, BorderLayout.CENTER);

        table = new JTable(tableModel);
        scrollPane.setViewportView(table);
        table.setRowHeight(32);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        table.getTableHeader().setForeground(new Color(255, 255, 255));
        table.getTableHeader().setBackground(new Color(31, 71, 145));
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));

        // Status column renderer with colors
        table.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setFont(new Font("SansSerif", Font.BOLD, 13));

                if (value != null) {
                    String status = value.toString();
                    switch (status) {
                        case "Available":
                            c.setForeground(new Color(46, 194, 126)); // Green
                            break;
                        case "Occupied":
                            c.setForeground(new Color(224, 27, 36));  // Red
                            break;
                        case "Reserved":
                            c.setForeground(new Color(0, 128, 255));  // Blue
                            break;
                        case "Maintenance":
                            c.setForeground(new Color(255, 165, 0)); // Orange
                            break;
                        default:
                            c.setForeground(Color.BLACK);
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

        // South panel with buttons
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        add(panel, BorderLayout.SOUTH);

        JButton refreshBtn = new JButton("🔄 Refresh");
        refreshBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        refreshBtn.setForeground(Color.WHITE);
        refreshBtn.setBackground(new Color(70, 130, 180));
        refreshBtn.setFocusPainted(false);
        refreshBtn.addActionListener(e -> loadRoom());
        panel.add(refreshBtn);

        JButton btnMaintenance = new JButton("🔧 Toggle Maintenance");
        btnMaintenance.setFont(new Font("SansSerif", Font.BOLD, 13));
        btnMaintenance.setForeground(Color.WHITE);
        btnMaintenance.setBackground(new Color(255, 165, 0));
        btnMaintenance.setFocusPainted(false);
        btnMaintenance.addActionListener(e -> toggleMaintenance());
        panel.add(btnMaintenance);

        loadRoom();
    }

    /**
     * Load room data with proper status detection
     * BUG FIX: Uses proper LEFT JOIN to handle rooms without bookings
     */
    private void loadRoom() {
        tableModel.setRowCount(0);
        try (Connection conn = EnhancedDBConnection.getConnection()) {
            String sql = "SELECT r.room_id, r.room_number, rt.type_name, rt.rate_per_day, r.floor_number, " +
                        "r.is_available, r.is_maintenance, " +
                        "(SELECT b.status FROM bookings b WHERE b.room_id = r.room_id " +
                        " AND b.status IN ('Reserved', 'Checked In') " +
                        " ORDER BY b.created_at DESC LIMIT 1) as booking_status " +
                        "FROM rooms r " +
                        "JOIN room_types rt ON r.room_type_id = rt.room_type_id " +
                        "ORDER BY r.floor_number, r.room_number";

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                String status;
                if (rs.getBoolean("is_maintenance")) {
                    status = "Maintenance";
                } else if (rs.getString("booking_status") != null) {
                    if (rs.getString("booking_status").equals("Checked In")) {
                        status = "Occupied";
                    } else {
                        status = "Reserved";
                    }
                } else {
                    status = "Available";
                }

                tableModel.addRow(new Object[] {
                    rs.getString("room_number"),
                    rs.getString("type_name"),
                    String.format("%,.2f", rs.getDouble("rate_per_day")),
                    rs.getInt("floor_number"),
                    status
                });
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading rooms: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Toggle maintenance mode for selected room
     */
    private void toggleMaintenance() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a room.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String roomNum = (String) tableModel.getValueAt(row, 0);
        String currentStatus = (String) tableModel.getValueAt(row, 4);

        if (currentStatus.equals("Occupied") || currentStatus.equals("Reserved")) {
            JOptionPane.showMessageDialog(this, "Cannot set maintenance on occupied/reserved room.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        boolean setMaintenance = !currentStatus.equals("Maintenance");
        String action = setMaintenance ? "set to maintenance" : "restored to available";

        int confirm = JOptionPane.showConfirmDialog(this,
            "Room " + roomNum + " will be " + action + ". Continue?",
            "Confirm", JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) return;

        try (Connection conn = EnhancedDBConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                "UPDATE rooms SET is_maintenance = ?, is_available = ? WHERE room_number = ?");
            ps.setBoolean(1, setMaintenance);
            ps.setBoolean(2, !setMaintenance);
            ps.setString(3, roomNum);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Room " + roomNum + " " + action + "!", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadRoom();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }
}