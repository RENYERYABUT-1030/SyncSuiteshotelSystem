package Hotel_Reservation;

import Hotel_Reservation.core.EnhancedDBConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Enhanced Dashboard Frame - Modern UI with Statistics Dashboard
 * 
 * Improvements:
 * - Real-time statistics cards
 * - Sidebar navigation
 * - Modern color scheme
 * - Quick action buttons
 * - Better responsive layout
 */
public class EnhancedDashboardFrame extends JFrame {
    private static final long serialVersionUID = 1L;

    private JPanel contentPanel;
    private JLabel lblLoggedInUser;
    private JLabel lblOccupancyRate;
    private JLabel lblTodayRevenue;
    private JLabel lblPendingBookings;

    public EnhancedDashboardFrame() {
        initialize();
        loadDashboardStatistics();
    }

    private void initialize() {
        setUndecorated(true);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Main container
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(245, 245, 250));
        setContentPane(mainPanel);

        // ═════════════════════════════════════════════════════════════
        // SIDEBAR NAVIGATION
        // ═════════════════════════════════════════════════════════════
        JPanel sidebar = createSidebar();
        mainPanel.add(sidebar, BorderLayout.WEST);

        // ═════════════════════════════════════════════════════════════
        // TOP HEADER
        // ═════════════════════════════════════════════════════════════
        JPanel header = createHeader();
        mainPanel.add(header, BorderLayout.NORTH);

        // ═════════════════════════════════════════════════════════════
        // CONTENT AREA - Dashboard with Statistics
        // ═════════════════════════════════════════════════════════════
        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(new Color(245, 245, 250));
        contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Statistics Dashboard
        JPanel statsPanel = createStatisticsPanel();
        contentPanel.add(statsPanel, BorderLayout.NORTH);

        // Quick Actions
        JPanel actionsPanel = createQuickActionsPanel();
        contentPanel.add(actionsPanel, BorderLayout.CENTER);

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        setVisible(true);
    }

    // ═════════════════════════════════════════════════════════════════
    // SIDEBAR NAVIGATION
    // ═════════════════════════════════════════════════════════════════
    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(31, 31, 60));
        sidebar.setPreferredSize(new Dimension(250, getHeight()));
        sidebar.setBorder(new EmptyBorder(20, 0, 20, 0));

        // Logo
        JLabel logo = new JLabel("SYNC SUITES");
        logo.setForeground(new Color(255, 215, 0));
        logo.setFont(new Font("SansSerif", Font.BOLD, 18));
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(logo);
        sidebar.add(Box.createVerticalStrut(30));

        // Navigation items
        String[] navItems = {
            "📊 Dashboard",
            "🆕 New Booking",
            "🏨 Room Availability",
            "📋 Manage Bookings",
            "🔧 Maintenance",
            "📈 Reports",
            "📋 Audit Log",
            "⚙️ Settings"
        };

        for (String item : navItems) {
            JButton btn = createNavButton(item);
            sidebar.add(btn);
            sidebar.add(Box.createVerticalStrut(10));
        }

        sidebar.add(Box.createVerticalGlue());

        // Logout button
        JButton logoutBtn = new JButton("🚪 Logout");
        logoutBtn.setMaximumSize(new Dimension(230, 40));
        logoutBtn.setBackground(new Color(224, 27, 36));
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        logoutBtn.setFocusPainted(false);
        logoutBtn.addActionListener(e -> handleLogout());
        sidebar.add(logoutBtn);

        return sidebar;
    }

    private JButton createNavButton(String label) {
        JButton btn = new JButton(label);
        btn.setMaximumSize(new Dimension(230, 40));
        btn.setBackground(new Color(50, 50, 100));
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("SansSerif", Font.PLAIN, 12));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(new Color(70, 130, 180));
            }

            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(new Color(50, 50, 100));
            }
        });

        return btn;
    }

    // ═════════════════════════════════════════════════════════════════
    // TOP HEADER
    // ═════════════════════════════════════════════════════════════════
    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(255, 255, 255));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)));
        header.setPreferredSize(new Dimension(getWidth(), 70));

        // Left: Title
        JLabel title = new JLabel("Hotel Management Dashboard");
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        title.setForeground(new Color(31, 31, 60));
        title.setBorder(new EmptyBorder(15, 20, 15, 0));
        header.add(title, BorderLayout.WEST);

        // Right: User info
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 15));
        userPanel.setBackground(Color.WHITE);

        String userName = SessionManager.getCurrentUserName();
        lblLoggedInUser = new JLabel("Logged in as: " + (userName != null ? userName : "Admin"));
        lblLoggedInUser.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblLoggedInUser.setForeground(new Color(100, 100, 100));
        userPanel.add(lblLoggedInUser);

        header.add(userPanel, BorderLayout.EAST);

        return header;
    }

    // ═════════════════════════════════════════════════════════════════
    // STATISTICS CARDS
    // ═════════════════════════════════════════════════════════════════
    private JPanel createStatisticsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 20, 0));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(0, 0, 30, 0));

        // Occupancy Rate Card
        panel.add(createStatCard("OCCUPANCY RATE", "85%", new Color(46, 194, 126), 
            lblOccupancyRate = new JLabel("85%")));

        // Today's Revenue Card
        panel.add(createStatCard("TODAY'S REVENUE", "₱125,500", new Color(70, 130, 180), 
            lblTodayRevenue = new JLabel("₱125,500")));

        // Pending Bookings Card
        panel.add(createStatCard("PENDING BOOKINGS", "12", new Color(255, 165, 0), 
            lblPendingBookings = new JLabel("12")));

        // Available Rooms Card
        panel.add(createStatCard("AVAILABLE ROOMS", "28", new Color(147, 112, 219), 
            new JLabel("28")));

        return panel;
    }

    private JPanel createStatCard(String title, String value, Color bgColor, JLabel valueLabel) {
        JPanel card = new JPanel();
        card.setLayout(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));

        // Colored header
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(bgColor);
        headerPanel.setPreferredSize(new Dimension(0, 6));
        card.add(headerPanel, BorderLayout.NORTH);

        // Content
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        titleLabel.setForeground(new Color(100, 100, 100));
        contentPanel.add(titleLabel);

        contentPanel.add(Box.createVerticalStrut(10));

        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
        valueLabel.setForeground(bgColor);
        contentPanel.add(valueLabel);

        card.add(contentPanel, BorderLayout.CENTER);

        return card;
    }

    // ═════════════════════════════════════════════════════════════════
    // QUICK ACTIONS PANEL
    // ═════════════════════════════════════════════════════════════════
    private JPanel createQuickActionsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setOpaque(false);

        JLabel sectionTitle = new JLabel("Quick Actions");
        sectionTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
        sectionTitle.setForeground(new Color(31, 31, 60));
        sectionTitle.setBorder(new EmptyBorder(0, 0, 20, 0));
        panel.add(sectionTitle, BorderLayout.NORTH);

        JPanel actionsGrid = new JPanel(new GridLayout(2, 3, 20, 20));
        actionsGrid.setOpaque(false);

        String[][] actions = {
            {"🆕 New Booking", "Create a new guest booking"},
            {"📋 View Bookings", "Manage existing bookings"},
            {"🔧 Maintenance", "Room maintenance tasks"},
            {"💰 Process Payment", "Handle guest payments"},
            {"👥 Guest Services", "Request special services"},
            {"📊 View Analytics", "Business analytics & reports"}
        };

        for (String[] action : actions) {
            actionsGrid.add(createActionButton(action[0], action[1]));
        }

        panel.add(actionsGrid, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createActionButton(String title, String description) {
        JPanel button = new JPanel();
        button.setLayout(new BorderLayout());
        button.setBackground(Color.WHITE);
        button.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        titleLabel.setForeground(new Color(31, 31, 60));
        contentPanel.add(titleLabel);

        contentPanel.add(Box.createVerticalStrut(10));

        JLabel descLabel = new JLabel(description);
        descLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        descLabel.setForeground(new Color(150, 150, 150));
        contentPanel.add(descLabel);

        button.add(contentPanel, BorderLayout.CENTER);

        // Hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(new Color(240, 245, 250));
                button.setBorder(BorderFactory.createLineBorder(new Color(70, 130, 180), 2));
            }

            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(Color.WHITE);
                button.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));
            }
        });

        return button;
    }

    // ═════════════════════════════════════════════════════════════════
    // STATISTICS LOADING
    // ═════════════════════════════════════════════════════════════════
    private void loadDashboardStatistics() {
        new Thread(() -> {
            try {
                // Simulate loading with actual DB queries
                updateOccupancyRate();
                updateTodayRevenue();
                updatePendingBookings();
                updateAvailableRooms();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void updateOccupancyRate() {
        try (Connection conn = EnhancedDBConnection.getConnection()) {
            String sql = "SELECT COUNT(*) as total, SUM(CASE WHEN status IN ('Reserved','Checked In') THEN 1 ELSE 0 END) as occupied " +
                        "FROM rooms r LEFT JOIN bookings b ON r.room_id = b.room_id " +
                        "WHERE r.is_maintenance = 0";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                int total = rs.getInt("total");
                int occupied = rs.getInt("occupied");
                int rate = total > 0 ? (occupied * 100 / total) : 0;
                SwingUtilities.invokeLater(() -> lblOccupancyRate.setText(rate + "%"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateTodayRevenue() {
        try (Connection conn = EnhancedDBConnection.getConnection()) {
            String sql = "SELECT COALESCE(SUM(total_amount), 0) as revenue FROM bookings " +
                        "WHERE DATE(created_at) = CURDATE() AND status != 'Cancelled'";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                double revenue = rs.getDouble("revenue");
                SwingUtilities.invokeLater(() -> 
                    lblTodayRevenue.setText("₱" + String.format("%,.2f", revenue)));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updatePendingBookings() {
        try (Connection conn = EnhancedDBConnection.getConnection()) {
            String sql = "SELECT COUNT(*) as pending FROM bookings WHERE status = 'Reserved'";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                int pending = rs.getInt("pending");
                SwingUtilities.invokeLater(() -> lblPendingBookings.setText(String.valueOf(pending)));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateAvailableRooms() {
        // This would update the available rooms card if we had a reference
        // For now it's just a placeholder
    }

    private void handleLogout() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to logout?",
            "Confirm Logout",
            JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            SessionManager.logout();
            dispose();
            new LoginFrame();
        }
    }

    // ═════════════════════════════════════════════════════════════════
    // MAIN - Testing
    // ═════════════════════════════════════════════════════════════════
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new EnhancedDashboardFrame());
    }
}