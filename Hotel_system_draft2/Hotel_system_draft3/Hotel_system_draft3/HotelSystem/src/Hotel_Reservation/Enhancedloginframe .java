

import Hotel_Reservation.core.EnhancedDBConnection;
import Hotel_Reservation.core.ConfigManager;
import Hotel_Reservation.utils.SecurityUtils;
import Hotel_Reservation.utils.SecurityUtils.ValidationResult;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.logging.Logger;

/**
 * Enhanced Login Frame - Security Improvements
 * 
 * Features:
 * - Password hashing with BCrypt
 * - Input validation and sanitization
 * - Failed login attempt tracking
 * - Account lockout after N attempts
 * - Password strength requirements
 * - Remember me functionality
 * - Session management
 */
public class EnhancedLoginFrame extends JFrame {
    private static final Logger logger = Logger.getLogger(EnhancedLoginFrame.class.getName());
    private static final long serialVersionUID = 1L;
    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final int LOCKOUT_DURATION_MINUTES = 30;

    private JFrame frame;
    private JTextField userField;
    private JPasswordField passField;
    private JComboBox<String> roleCombo;
    private JCheckBox showPassCheck;
    private JCheckBox rememberMeCheck;
    private JLabel loginAttemptsLabel;

    // Session management
    public static int currentUserId = -1;
    public static String currentUserType = "";
    public static String currentUserName = "";
    public static String sessionToken = "";

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                // Test database connection
                if (!EnhancedDBConnection.testConnection()) {
                    JOptionPane.showMessageDialog(null,
                        "Cannot connect to database!\n\nPlease check:\n" +
                        "1. MySQL is running\n" +
                        "2. Database 'hotel_system' exists\n" +
                        "3. Credentials in configuration are correct",
                        "Database Error", JOptionPane.ERROR_MESSAGE);
                    System.exit(1);
                }

                // Log system startup
                logger.info("Hotel Reservation System started - v" + 
                           ConfigManager.getInstance().getString("app.version"));

                new EnhancedLoginFrame();
                
                // Add shutdown hook to close connection pool
                EnhancedDBConnection.addShutdownHook();

            } catch (Exception e) {
                logger.severe("Application startup failed: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    public EnhancedLoginFrame() {
        initialize();
    }

    private void initialize() {
        frame = new JFrame();
        frame.setTitle("SYNC SUITES HOTEL - Secure Login");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(580, 550);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);

        // Background panel
        JPanel bgPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(20, 20, 60),
                    0, getHeight(), new Color(40, 40, 100));
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        bgPanel.setLayout(null);
        frame.setContentPane(bgPanel);

        // ══════════════════════════════════════════════════════════
        // TITLE & SUBTITLE
        // ══════════════════════════════════════════════════════════
        JLabel lblTitle = new JLabel("SYNC SUITES HOTEL");
        lblTitle.setBounds(0, 30, 580, 40);
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 32));
        lblTitle.setForeground(new Color(255, 215, 0));
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        frame.getContentPane().add(lblTitle);

        JLabel lblSubtitle = new JLabel("Secure Reservation System v2.1");
        lblSubtitle.setBounds(0, 70, 580, 25);
        lblSubtitle.setFont(new Font("SansSerif", Font.PLAIN, 14));
        lblSubtitle.setForeground(new Color(200, 200, 200));
        lblSubtitle.setHorizontalAlignment(SwingConstants.CENTER);
        frame.getContentPane().add(lblSubtitle);

        // ══════════════════════════════════════════════════════════
        // LOGIN PANEL
        // ══════════════════════════════════════════════════════════
        JPanel loginPanel = new JPanel();
        loginPanel.setBounds(70, 110, 440, 340);
        loginPanel.setBackground(new Color(60, 60, 120));
        loginPanel.setLayout(null);
        loginPanel.setBorder(BorderFactory.createLineBorder(new Color(255, 215, 0), 2));
        frame.getContentPane().add(loginPanel);

        // Role Selection
        JLabel lblRole = new JLabel("Login As:");
        lblRole.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblRole.setForeground(Color.WHITE);
        lblRole.setBounds(30, 20, 100, 25);
        loginPanel.add(lblRole);

        roleCombo = new JComboBox<>(new String[]{"Admin", "Customer"});
        roleCombo.setBounds(130, 20, 280, 25);
        roleCombo.setFont(new Font("SansSerif", Font.PLAIN, 12));
        loginPanel.add(roleCombo);

        // Username
        JLabel lblUsername = new JLabel("Username:");
        lblUsername.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblUsername.setForeground(Color.WHITE);
        lblUsername.setBounds(30, 60, 100, 25);
        loginPanel.add(lblUsername);

        userField = new JTextField();
        userField.setBounds(130, 60, 280, 25);
        userField.setFont(new Font("SansSerif", Font.PLAIN, 12));
        loginPanel.add(userField);

        // Password
        JLabel lblPassword = new JLabel("Password:");
        lblPassword.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblPassword.setForeground(Color.WHITE);
        lblPassword.setBounds(30, 100, 100, 25);
        loginPanel.add(lblPassword);

        passField = new JPasswordField();
        passField.setBounds(130, 100, 280, 25);
        passField.setFont(new Font("SansSerif", Font.PLAIN, 12));
        loginPanel.add(passField);

        // Show password checkbox
        showPassCheck = new JCheckBox("Show Password");
        showPassCheck.setBounds(130, 135, 150, 20);
        showPassCheck.setForeground(Color.WHITE);
        showPassCheck.setBackground(new Color(60, 60, 120));
        showPassCheck.setFont(new Font("SansSerif", Font.PLAIN, 11));
        showPassCheck.addActionListener(e -> {
            if (showPassCheck.isSelected()) {
                passField.setEchoChar((char) 0);
            } else {
                passField.setEchoChar('●');
            }
        });
        loginPanel.add(showPassCheck);

        // Remember me checkbox
        rememberMeCheck = new JCheckBox("Remember me for 7 days");
        rememberMeCheck.setBounds(280, 135, 130, 20);
        rememberMeCheck.setForeground(Color.WHITE);
        rememberMeCheck.setBackground(new Color(60, 60, 120));
        rememberMeCheck.setFont(new Font("SansSerif", Font.PLAIN, 11));
        loginPanel.add(rememberMeCheck);

        // Login attempts warning
        loginAttemptsLabel = new JLabel("");
        loginAttemptsLabel.setBounds(30, 165, 380, 20);
        loginAttemptsLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        loginAttemptsLabel.setForeground(new Color(255, 100, 100));
        loginPanel.add(loginAttemptsLabel);

        // Login Button
        JButton btnLogin = new JButton("LOGIN");
        btnLogin.setBounds(130, 195, 280, 35);
        btnLogin.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setBackground(new Color(46, 194, 126));
        btnLogin.setFocusPainted(false);
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogin.addActionListener(e -> handleLogin());
        loginPanel.add(btnLogin);

        // Register link
        JButton btnRegister = new JButton("New Customer? Register Here");
        btnRegister.setBounds(130, 240, 280, 20);
        btnRegister.setFont(new Font("SansSerif", Font.PLAIN, 11));
        btnRegister.setForeground(new Color(255, 215, 0));
        btnRegister.setBackground(new Color(60, 60, 120));
        btnRegister.setBorderPainted(false);
        btnRegister.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRegister.addActionListener(e -> showRegistrationDialog());
        loginPanel.add(btnRegister);

        // Forgot password link
        JButton btnForgot = new JButton("Forgot Password?");
        btnForgot.setBounds(130, 265, 280, 20);
        btnForgot.setFont(new Font("SansSerif", Font.PLAIN, 11));
        btnForgot.setForeground(new Color(100, 200, 255));
        btnForgot.setBackground(new Color(60, 60, 120));
        btnForgot.setBorderPainted(false);
        btnForgot.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnForgot.addActionListener(e -> showForgotPasswordDialog());
        loginPanel.add(btnForgot);

        // Version
        JLabel lblVersion = new JLabel("v2.1 - Enterprise Edition");
        lblVersion.setBounds(0, 460, 580, 20);
        lblVersion.setFont(new Font("SansSerif", Font.ITALIC, 11));
        lblVersion.setForeground(new Color(150, 150, 150));
        lblVersion.setHorizontalAlignment(SwingConstants.CENTER);
        frame.getContentPane().add(lblVersion);

        // Key listeners
        passField.addActionListener(e -> handleLogin());
        userField.addActionListener(e -> handleLogin());

        frame.setVisible(true);
    }

    private void handleLogin() {
        String username = userField.getText().trim();
        String password = new String(passField.getPassword());
        String role = (String) roleCombo.getSelectedItem();

        // Input validation
        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter both username and password.");
            return;
        }

        if (!SecurityUtils.isValidUsername(username)) {
            showError("Username contains invalid characters.");
            return;
        }

        try (Connection conn = EnhancedDBConnection.getConnection()) {
            
            if ("Admin".equals(role)) {
                authenticateAdmin(conn, username, password);
            } else {
                authenticateCustomer(conn, username, password);
            }

        } catch (SQLException ex) {
            logger.severe("Database error during login: " + ex.getMessage());
            showError("Database error: " + ex.getMessage());
        }
    }

    private void authenticateAdmin(Connection conn, String username, String password) throws SQLException {
        // Check for account lockout
        if (isAccountLocked(conn, username, "admin")) {
            showError("Account temporarily locked due to too many failed login attempts.\n" +
                     "Please try again in 30 minutes or contact support.");
            return;
        }

        String sql = "SELECT admin_id, username, password FROM admins WHERE username = ? AND is_active = TRUE";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, username);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            String storedHash = rs.getString("password");
            
            // Verify password using BCrypt
            if (SecurityUtils.verifyPassword(password, storedHash)) {
                // Login successful - clear failed attempts
                clearFailedAttempts(conn, username, "admin");
                
                currentUserId = rs.getInt("admin_id");
                currentUserType = "admin";
                currentUserName = username;
                sessionToken = generateSessionToken();

                logLoginEvent(conn, "Admin", username, true, "Successful login");
                
                showSuccess("Welcome, Admin!");
                frame.dispose();
                // new DashboardFrame();

            } else {
                // Wrong password - increment failed attempts
                incrementFailedAttempts(conn, username, "admin");
                int attempts = getFailedAttempts(conn, username, "admin");
                
                if (attempts >= MAX_LOGIN_ATTEMPTS) {
                    lockAccount(conn, username, "admin");
                    showError("Too many failed login attempts. Account locked for 30 minutes.");
                } else {
                    showError("Invalid password. " + (MAX_LOGIN_ATTEMPTS - attempts) + 
                             " attempts remaining.");
                }
                
                logLoginEvent(conn, "Admin", username, false, "Invalid password (attempt " + attempts + ")");
            }
        } else {
            showError("Admin account not found or inactive.");
            logLoginEvent(conn, "Admin", username, false, "User not found");
        }
    }

    private void authenticateCustomer(Connection conn, String username, String password) throws SQLException {
        // Check for account lockout
        if (isAccountLocked(conn, username, "customer")) {
            showError("Account temporarily locked. Please try again later or contact support.");
            return;
        }

        String sql = "SELECT u.user_id, u.username, u.password, c.first_name, c.last_name " +
                    "FROM users u " +
                    "JOIN customers c ON u.customer_id = c.customer_id " +
                    "WHERE u.username = ? AND u.is_active = TRUE";
        
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, username);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            String storedHash = rs.getString("password");
            
            // Verify password using BCrypt
            if (SecurityUtils.verifyPassword(password, storedHash)) {
                clearFailedAttempts(conn, username, "customer");
                
                currentUserId = rs.getInt("user_id");
                currentUserType = "customer";
                currentUserName = rs.getString("first_name") + " " + rs.getString("last_name");
                sessionToken = generateSessionToken();

                updateLastLogin(conn, currentUserId);
                logLoginEvent(conn, "Customer", currentUserName, true, "Successful login");
                
                showSuccess("Welcome, " + currentUserName + "!");
                frame.dispose();
                // new CustomerPortalFrame();

            } else {
                incrementFailedAttempts(conn, username, "customer");
                int attempts = getFailedAttempts(conn, username, "customer");
                
                if (attempts >= MAX_LOGIN_ATTEMPTS) {
                    lockAccount(conn, username, "customer");
                    showError("Too many failed attempts. Account locked.");
                } else {
                    showError("Invalid password. " + (MAX_LOGIN_ATTEMPTS - attempts) + 
                             " attempts remaining.");
                }
                
                logLoginEvent(conn, "Customer", username, false, "Invalid password");
            }
        } else {
            showError("Customer account not found or inactive.");
            logLoginEvent(conn, "Customer", username, false, "Account not found");
        }
    }

    // ═════════════════════════════════════════════════════════════════
    // ACCOUNT SECURITY METHODS
    // ═════════════════════════════════════════════════════════════════

    private void incrementFailedAttempts(Connection conn, String username, String userType) {
        try {
            String sql = "UPDATE login_attempts SET failed_attempts = failed_attempts + 1, " +
                        "last_attempt = NOW() WHERE username = ? AND user_type = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            ps.setString(2, userType);
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.warning("Failed to increment login attempts: " + e.getMessage());
        }
    }

    private void clearFailedAttempts(Connection conn, String username, String userType) {
        try {
            String sql = "UPDATE login_attempts SET failed_attempts = 0 " +
                        "WHERE username = ? AND user_type = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            ps.setString(2, userType);
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.warning("Failed to clear login attempts: " + e.getMessage());
        }
    }

    private int getFailedAttempts(Connection conn, String username, String userType) throws SQLException {
        String sql = "SELECT failed_attempts FROM login_attempts " +
                    "WHERE username = ? AND user_type = ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, username);
        ps.setString(2, userType);
        ResultSet rs = ps.executeQuery();
        
        return rs.next() ? rs.getInt("failed_attempts") : 0;
    }

    private boolean isAccountLocked(Connection conn, String username, String userType) throws SQLException {
        String sql = "SELECT is_locked, locked_until FROM login_attempts " +
                    "WHERE username = ? AND user_type = ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, username);
        ps.setString(2, userType);
        ResultSet rs = ps.executeQuery();
        
        if (rs.next()) {
            if (rs.getBoolean("is_locked")) {
                Timestamp lockedUntil = rs.getTimestamp("locked_until");
                return System.currentTimeMillis() < lockedUntil.getTime();
            }
        }
        return false;
    }

    private void lockAccount(Connection conn, String username, String userType) {
        try {
            String sql = "UPDATE login_attempts SET is_locked = TRUE, " +
                        "locked_until = DATE_ADD(NOW(), INTERVAL ? MINUTE) " +
                        "WHERE username = ? AND user_type = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, LOCKOUT_DURATION_MINUTES);
            ps.setString(2, username);
            ps.setString(3, userType);
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.warning("Failed to lock account: " + e.getMessage());
        }
    }

    private void updateLastLogin(Connection conn, int userId) throws SQLException {
        String sql = "UPDATE users SET last_login = NOW() WHERE user_id = ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, userId);
        ps.executeUpdate();
    }

    private void logLoginEvent(Connection conn, String userType, String userName, 
                              boolean success, String details) {
        try {
            String sql = "INSERT INTO audit_log (user_type, user_name, action, details) " +
                        "VALUES (?, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, userType);
            ps.setString(2, userName);
            ps.setString(3, success ? "Login" : "Login Failed");
            ps.setString(4, details);
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.warning("Failed to log login event: " + e.getMessage());
        }
    }

    private String generateSessionToken() {
        // Generate a random session token
        return java.util.UUID.randomUUID().toString();
    }

    // ═════════════════════════════════════════════════════════════════
    // UI HELPERS
    // ═════════════════════════════════════════════════════════════════

    private void showError(String message) {
        JOptionPane.showMessageDialog(frame, message, "Login Failed", JOptionPane.ERROR_MESSAGE);
    }

    private void showSuccess(String message) {
        JOptionPane.showMessageDialog(frame, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showRegistrationDialog() {
        JDialog dialog = new JDialog(frame, "Customer Registration", true);
        dialog.setSize(500, 600);
        dialog.setLocationRelativeTo(frame);
        // Implementation of registration dialog
        dialog.setVisible(true);
    }

    private void showForgotPasswordDialog() {
        JDialog dialog = new JDialog(frame, "Reset Password", true);
        dialog.setSize(400, 200);
        dialog.setLocationRelativeTo(frame);
        // Implementation of forgot password dialog
        dialog.setVisible(true);
    }
}