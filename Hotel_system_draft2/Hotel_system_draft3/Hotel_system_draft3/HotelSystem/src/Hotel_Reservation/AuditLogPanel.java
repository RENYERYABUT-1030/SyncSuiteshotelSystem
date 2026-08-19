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
 */
public class AuditLogPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private JTable auditTable;
    private DefaultTableModel auditModel;
    private JComboBox<String> filterType;
    private JTextField txtSearch;

    public AuditLogPanel() {
        setLayout(new BorderLayout());

        JLabel titleLbl = new JLabel("System Audit Log");
        titleLbl.setForeground(new Color(0, 0, 47));
        titleLbl.setBackground(new Color(240, 240, 240));
        titleLbl.setOpaque(true);
        titleLbl.setFont(new Font("SansSerif", Font.BOLD, 20));
        titleLbl.setBorder(new EmptyBorder(15, 15, 15, 0));
        add(titleLbl, BorderLayout.NORTH);

        // ... (Filter panel setup)

        // ... (Table setup and logic)
    }

    // ... (loadAuditLog, clearOldLogs methods)
}