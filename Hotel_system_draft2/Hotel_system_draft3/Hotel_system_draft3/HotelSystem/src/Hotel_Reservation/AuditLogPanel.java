package Hotel_Reservation;

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
 *
 * Reads from the audit_log table (see AuditLogger.java for the writer side).
 * Run this once against your hotel_system database if the table doesn't exist yet:
 *
 * CREATE TABLE audit_log (
 *     log_id INT AUTO_INCREMENT PRIMARY KEY,
 *     user_type VARCHAR(20) NOT NULL,
 *     user_name VARCHAR(100) NOT NULL,
 *     action VARCHAR(100) NOT NULL,
 *     details TEXT,
 *     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
 * );
 */
public class AuditLogPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private JTable auditTable;
    private DefaultTableModel auditModel;
    private JComboBox<String> filterType;
    private JTextField txtSearch;

    private final SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy hh:mm a");

    public AuditLogPanel() {
        setLayout(new BorderLayout());

        JLabel titleLbl = new JLabel("System Audit Log");
        titleLbl.setForeground(new Color(0, 0, 47));
        titleLbl.setBackground(new Color(240, 240, 240));
        titleLbl.setOpaque(true);
        titleLbl.setFont(new Font("SansSerif", Font.BOLD, 20));
        titleLbl.setBorder(new EmptyBorder(15, 15, 15, 0));
        add(titleLbl, BorderLayout.NORTH);

        // ─── Filter panel ───
        JPanel topPanel = new JPanel(new BorderLayout());

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        filterPanel.setBorder(new EmptyBorder(0, 15, 0, 0));

        JLabel lblFilter = new JLabel("Filter:");
        lblFilter.setFont(new Font("SansSerif", Font.BOLD, 13));
        filterPanel.add(lblFilter);

        filterType = new JComboBox<>(new String[] {"All Activity", "Admin", "Customer"});
        filterType.setFont(new Font("SansSerif", Font.PLAIN, 13));
        filterType.addActionListener(e -> loadAuditLog());
        filterPanel.add(filterType);

        JLabel lblSearch = new JLabel("Search:");
        lblSearch.setFont(new Font("SansSerif", Font.BOLD, 13));
        filterPanel.add(lblSearch);

        txtSearch = new JTextField(18);
        txtSearch.setFont(new Font("SansSerif", Font.PLAIN, 13));
        txtSearch.addActionListener(e -> loadAuditLog());
        filterPanel.add(txtSearch);

        JButton btnSearch = new JButton("🔍 Search");
        btnSearch.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnSearch.setBackground(new Color(70, 130, 180));
        btnSearch.setForeground(Color.WHITE);
        btnSearch.setFocusPainted(false);
        btnSearch.addActionListener(e -> loadAuditLog());
        filterPanel.add(btnSearch);

        JButton btnRefresh = new JButton("🔄 Refresh");
        btnRefresh.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnRefresh.setBackground(new Color(46, 194, 126));
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setFocusPainted(false);
        btnRefresh.addActionListener(e -> {
            txtSearch.setText("");
            filterType.setSelectedIndex(0);
            loadAuditLog();
        });
        filterPanel.add(btnRefresh);

        topPanel.add(filterPanel, BorderLayout.WEST);

        JPanel clearPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        JButton btnClearOld = new JButton("🗑 Clear Logs Older Than 90 Days");
        btnClearOld.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnClearOld.setBackground(new Color(224, 27, 36));
        btnClearOld.setForeground(Color.WHITE);
        btnClearOld.setFocusPainted(false);
        btnClearOld.addActionListener(e -> clearOldLogs());
        clearPanel.add(btnClearOld);
        topPanel.add(clearPanel, BorderLayout.EAST);

        add(topPanel, BorderLayout.PAGE_START);

        // ─── Table setup ───
        String[] columns = {"Log ID", "Date/Time", "User Type", "User", "Action", "Details"};
        auditModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        auditTable = new JTable(auditModel);
        auditTable.setRowHeight(28);
        auditTable.setFont(new Font("SansSerif", Font.PLAIN, 12));
        auditTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        auditTable.getTableHeader().setForeground(Color.WHITE);
        auditTable.getTableHeader().setBackground(new Color(31, 71, 145));
        auditTable.getColumnModel().getColumn(0).setPreferredWidth(60);
        auditTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        auditTable.getColumnModel().getColumn(2).setPreferredWidth(90);
        auditTable.getColumnModel().getColumn(3).setPreferredWidth(130);
        auditTable.getColumnModel().getColumn(4).setPreferredWidth(150);
        auditTable.getColumnModel().getColumn(5).setPreferredWidth(300);

        JScrollPane scrollPane = new JScrollPane(auditTable);
        add(scrollPane, BorderLayout.CENTER);

        loadAuditLog();
    }

    /**
     * Load audit log entries, applying the selected filter and search keyword.
     */
    private void loadAuditLog() {
        auditModel.setRowCount(0);

        String filter = (String) filterType.getSelectedItem();
        String keyword = txtSearch.getText().trim();

        StringBuilder sql = new StringBuilder(
            "SELECT log_id, user_type, user_name, action, details, created_at FROM audit_log WHERE 1=1");

        if (filter != null && !filter.equals("All Activity")) {
            sql.append(" AND user_type = ?");
        }
        if (!keyword.isEmpty()) {
            sql.append(" AND (user_name LIKE ? OR action LIKE ? OR details LIKE ?)");
        }
        sql.append(" ORDER BY created_at DESC LIMIT 500");

        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(sql.toString());
            int idx = 1;
            if (filter != null && !filter.equals("All Activity")) {
                ps.setString(idx++, filter);
            }
            if (!keyword.isEmpty()) {
                String like = "%" + keyword + "%";
                ps.setString(idx++, like);
                ps.setString(idx++, like);
                ps.setString(idx++, like);
            }

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Timestamp ts = rs.getTimestamp("created_at");
                String formattedDate = ts != null ? sdf.format(ts) : "";

                auditModel.addRow(new Object[] {
                    rs.getInt("log_id"),
                    formattedDate,
                    rs.getString("user_type"),
                    rs.getString("user_name"),
                    rs.getString("action"),
                    rs.getString("details")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Error loading audit log: " + e.getMessage() +
                "\n\nIf this is a 'table doesn't exist' error, create the audit_log table first " +
                "(see the SQL comment at the top of AuditLogPanel.java).",
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Delete audit log entries older than 90 days.
     */
    private void clearOldLogs() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "This will permanently delete all audit log entries older than 90 days.\nContinue?",
            "Confirm Clear Old Logs", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) return;

        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM audit_log WHERE created_at < (NOW() - INTERVAL 90 DAY)");
            int deleted = ps.executeUpdate();

            JOptionPane.showMessageDialog(this,
                deleted + " old log entr" + (deleted == 1 ? "y" : "ies") + " deleted.",
                "Success", JOptionPane.INFORMATION_MESSAGE);

            AuditLogger.log(LoginFrame.currentUserType.equalsIgnoreCase("admin") ? "Admin" : "Customer",
                LoginFrame.currentUserName, "Clear Old Logs",
                "Deleted " + deleted + " audit log entries older than 90 days");

            loadAuditLog();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error clearing old logs: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}