package Hotel_Reservation;
import javax.swing.*;
import java.awt.*;
import java.sql.*;

/**
 * Enhanced Dashboard Frame
 * Tabs: New Booking, Room Availability, Manage Booking, Maintenance, Reports, Audit Log
 */
public class DashboardFrame extends JFrame {

    private JFrame frame;

    public DashboardFrame() {
        initialize();
    }

    private void initialize() {
        frame = new JFrame();
        frame.setUndecorated(true);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(new BorderLayout(0, 0));

        // Header — painted hotel-at-night skyline strip instead of a flat color bar
        HotelBackgroundPanel headerPanel = new HotelBackgroundPanel();
        headerPanel.setPreferredSize(new Dimension(900, 70));
        frame.getContentPane().add(headerPanel, BorderLayout.NORTH);
        headerPanel.setLayout(new BorderLayout(0, 0));

        JLabel lblHotel = new JLabel("  SYNC SUITES HOTEL");
        lblHotel.setFont(new Font("SansSerif", Font.BOLD, 22));
        lblHotel.setForeground(new Color(255, 215, 0));
        headerPanel.add(lblHotel, BorderLayout.WEST);

        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        infoPanel.setOpaque(false);
        JLabel lblAdmin = new JLabel("Logged in as: " + LoginFrame.currentUserName + "  ");
        lblAdmin.setFont(new Font("SansSerif", Font.PLAIN, 13));
        lblAdmin.setForeground(Color.WHITE);
        infoPanel.add(lblAdmin);
        headerPanel.add(infoPanel, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel();
        btnPanel.setOpaque(false);
        headerPanel.add(btnPanel, BorderLayout.EAST);

        JButton btnSettings = new JButton("Settings");
        btnSettings.setForeground(Color.WHITE);
        btnSettings.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnSettings.setBackground(new Color(70, 130, 180));
        btnSettings.setFocusPainted(false);
        btnSettings.addActionListener(e -> showSettingsDialog());
        btnPanel.add(btnSettings);

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        logoutBtn.setBackground(new Color(224, 27, 36));
        logoutBtn.setFocusPainted(false);
        logoutBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(frame, 
                "Are you sure you want to logout?", "Confirm Logout", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                LoginFrame.currentUserId = -1;
                LoginFrame.currentUserType = "";
                LoginFrame.currentUserName = "";
                frame.dispose();
                new LoginFrame();
            }
        });
        btnPanel.add(logoutBtn);

        // Tabbed Pane
        JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        tabbedPane.setFont(new Font("SansSerif", Font.BOLD, 13));
        frame.getContentPane().add(tabbedPane, BorderLayout.CENTER);

        tabbedPane.addTab("New Booking", new NewBookingPanel());
        tabbedPane.addTab("Room Availability", new RoomAvailabilityPanel());
        tabbedPane.addTab("Manage Booking", new ManageBookingPanel());
        tabbedPane.addTab("Maintenance", new MaintenanceModePanel());
        tabbedPane.addTab("Reports", new ReportsPanel());
        tabbedPane.addTab("Audit Log", new AuditLogPanel());

        frame.setVisible(true);
    }

    private void showSettingsDialog() {
        JDialog dialog = new JDialog(frame, "System Settings", true);
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(frame);
        dialog.getContentPane().setLayout(new BorderLayout());

        JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        try (Connection conn = DBConnection.getConnection()) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT setting_key, setting_value, description FROM system_settings");

            java.util.List<JTextField> fields = new java.util.ArrayList<>();
            java.util.List<String> keys = new java.util.ArrayList<>();

            while (rs.next()) {
                String key = rs.getString("setting_key");
                String value = rs.getString("setting_value");
                String desc = rs.getString("description");

                JLabel lbl = new JLabel(desc + ":");
                lbl.setFont(new Font("SansSerif", Font.BOLD, 12));
                panel.add(lbl);

                JTextField txt = new JTextField(value != null ? value : "");
                txt.setFont(new Font("SansSerif", Font.PLAIN, 12));
                panel.add(txt);

                fields.add(txt);
                keys.add(key);
            }

            dialog.getContentPane().add(new JScrollPane(panel), BorderLayout.CENTER);

            JPanel btnPanel = new JPanel();
            JButton btnSave = new JButton("Save Settings");
            btnSave.setBackground(new Color(46, 194, 126));
            btnSave.setForeground(Color.WHITE);
            btnSave.setFont(new Font("SansSerif", Font.BOLD, 13));
            btnSave.addActionListener(e -> {
                try (Connection conn2 = DBConnection.getConnection()) {
                    for (int i = 0; i < keys.size(); i++) {
                        PreparedStatement ps = conn2.prepareStatement(
                            "UPDATE system_settings SET setting_value = ? WHERE setting_key = ?");
                        ps.setString(1, fields.get(i).getText().trim());
                        ps.setString(2, keys.get(i));
                        ps.executeUpdate();
                    }
                    AuditLogger.log("Admin", LoginFrame.currentUserName, "Settings Update",
                        "Updated " + keys.size() + " system setting(s)");

                    JOptionPane.showMessageDialog(dialog, "Settings saved!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            });
            btnPanel.add(btnSave);
            dialog.getContentPane().add(btnPanel, BorderLayout.SOUTH);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(dialog, "Error loading settings: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }

        dialog.setVisible(true);
    }
}