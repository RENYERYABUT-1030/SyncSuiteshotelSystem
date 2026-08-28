package Hotel_Reservation;

import Hotel_Reservation.core.EnhancedDBConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;

/**
 * Audit Log Panel
 * View all system activity - admin actions, logins, data changes
 */
public class AuditLogPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private JTable auditTable;
    private DefaultTableModel auditModel;
    private JComboBox<String> filterType;
    private JTextField txtSearch;
    private JButton btnClearOld;
    private JLabel lblCount;

    public AuditLogPanel() {
        setLayout(new BorderLayout());

        JLabel titleLbl = new JLabel("System Audit Log");
        titleLbl.setForeground(new Color(0, 0, 47));
        titleLbl.setBackground(new Color(240, 240, 240));
        titleLbl.setOpaque(true);
        titleLbl.setFont(new Font("SansSerif", Font.BOLD, 20));
        titleLbl.setBorder(new EmptyBorder(15, 15, 15, 0));
        add(titleLbl, BorderLayout.NORTH);

        // Filter Panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        filterPanel.setBackground(new Color(240, 240, 240));

        filterPanel.add(new JLabel("Filter:"));
        filterType = new JComboBox<>(new String[]{"All", "Login", "Login Failed", "Booking Created", 
                                                   "Booking Cancelled", "Check-In", "Check-Out", 
                                                   "Payment", "Maintenance", "System"});
        filterType.setFont(new Font("SansSerif", Font.PLAIN, 12));
        filterType.addActionListener(e -> loadAuditLog());
        filterPanel.add(filterType);

        filterPanel.add(new JLabel("Search:"));
        txtSearch = new JTextField(20);
        txtSearch.setFont(new Font("SansSerif", Font.PLAIN, 12));
        txtSearch.addActionListener(e -> loadAuditLog());
        filterPanel.add(txtSearch);

        JButton btnSearch = new JButton("Search");
        btnSearch.setBackground(new Color(70, 130, 180));
        btnSearch.setForeground(Color.WHITE);
        btnSearch.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnSearch.addActionListener(e -> loadAuditLog());
        filterPanel.add(btnSearch);

        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.setBackground(new Color(46, 194, 126));
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnRefresh.addActionListener(e -> loadAuditLog());
        filterPanel.add(btnRefresh);

        btnClearOld = new JButton("Clear Old Logs (30+ days)");
        btnClearOld.setBackground(new Color(224, 27, 36));
        btnClearOld.setForeground(Color.WHITE);
        btnClearOld.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnClearOld.addActionListener(e -> clearOldLogs());
        filterPanel.add(btnClearOld);

        add(filterPanel, BorderLayout.SOUTH);

        // Table
        String[] columns = {"Log ID", "Timestamp", "User Type", "User Name", "Action", "Details"};
        auditModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        auditTable = new JTable(auditModel);
        auditTable.setRowHeight(28);
        auditTable.setFont(new Font("SansSerif", Font.PLAIN, 12));
        auditTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        auditTable.getTableHeader().setForeground(Color.WHITE);
        auditTable.getTableHeader().setBackground(new Color(31, 71, 145));

        JScrollPane scroll = new JScrollPane(auditTable);
        add(scroll, BorderLayout.CENTER);

        // Count label at bottom
        lblCount = new JLabel(" ");
        lblCount.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblCount.setBorder(new EmptyBorder(5, 10, 5, 10));
        add(lblCount, BorderLayout.EAST);

        loadAuditLog();
    }

    private void loadAuditLog() {
        auditModel.setRowCount(0);
        String filter = (String) filterType.getSelectedItem();
        String search = txtSearch.getText().trim();

        try (Connection conn = EnhancedDBConnection.getConnection()) {
            StringBuilder sql = new StringBuilder("SELECT * FROM audit_log WHERE 1=1 ");

            if (!filter.equals("All")) {
                sql.append("AND action = ? ");
            }
            if (!search.isEmpty()) {
                sql.append("AND (user_name LIKE ? OR details LIKE ?) ");
            }
            sql.append("ORDER BY log_id DESC LIMIT 500");

            PreparedStatement ps = conn.prepareStatement(sql.toString());
            int idx = 1;

            if (!filter.equals("All")) {
                ps.setString(idx++, filter);
            }
            if (!search.isEmpty()) {
                String pattern = "%" + search + "%";
                ps.setString(idx++, pattern);
                ps.setString(idx++, pattern);
            }

            ResultSet rs = ps.executeQuery();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            int count = 0;

            while (rs.next()) {
                Timestamp ts = rs.getTimestamp("timestamp");
                String timeStr = ts != null ? sdf.format(ts) : "N/A";
                auditModel.addRow(new Object[]{
                    rs.getInt("log_id"),
                    timeStr,
                    rs.getString("user_type"),
                    rs.getString("user_name"),
                    rs.getString("action"),
                    rs.getString("details")
                });
                count++;
            }

            lblCount.setText("Showing " + count + " records");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading audit log: " + e.getMessage());
        }
    }

    private void clearOldLogs() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Delete all audit logs older than 30 days?\nThis action cannot be undone.",
            "Clear Old Logs", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) return;

        try (Connection conn = EnhancedDBConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM audit_log WHERE timestamp < DATE_SUB(NOW(), INTERVAL 30 DAY)");
            int deleted = ps.executeUpdate();
            JOptionPane.showMessageDialog(this, deleted + " old records deleted!");
            loadAuditLog();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }
}