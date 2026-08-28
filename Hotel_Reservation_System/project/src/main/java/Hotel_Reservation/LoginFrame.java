package Hotel_Reservation;

import Hotel_Reservation.core.EnhancedDBConnection;
import Hotel_Reservation.utils.SecurityUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

/**
 * Login Frame - Application entry point
 *
 * NOTE: This file was uploaded empty (0 bytes) even though several panels
 * (CustomerPortalFrame, ManageBookingPanel, EnhancedDashboardFrame) call
 * `new LoginFrame()` or reference LoginFrame's session state. This is a
 * from-scratch reconstruction so the project actually compiles and runs.
 *
 * Session state (currentUserId/currentUserType/currentUserName) now lives
 * in SessionManager, not in this class as static fields, matching the
 * pattern already used elsewhere in the codebase.
 *
 * Assumes a `users` table with columns:
 *   user_id, username, password_hash, user_type ('admin' | 'customer'), customer_id (nullable for admins)
 * Adjust the query below if your actual schema differs.
 */
public class LoginFrame extends JFrame {
    private static final long serialVersionUID = 1L;

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private JLabel lblStatus;

    public LoginFrame() {
        initialize();
    }

    private void initialize() {
        setTitle("Sync Suites Hotel - Login");
        setSize(420, 380);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        // ── Header ──────────────────────────────────────────────
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(39, 0, 110));
        headerPanel.setPreferredSize(new Dimension(420, 90));

        JLabel titleLbl = new JLabel("Sync Suites Hotel");
        titleLbl.setForeground(Color.WHITE);
        titleLbl.setFont(new Font("SansSerif", Font.BOLD, 22));
        headerPanel.add(titleLbl);
        add(headerPanel, BorderLayout.NORTH);

        // ── Form ─────────────────────────────────────────────────
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBorder(new EmptyBorder(30, 40, 20, 40));
        formPanel.setBackground(Color.WHITE);

        JLabel lblUser = new JLabel("Username");
        lblUser.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblUser.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(lblUser);

        txtUsername = new JTextField();
        txtUsername.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        txtUsername.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(txtUsername);
        formPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        JLabel lblPass = new JLabel("Password");
        lblPass.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblPass.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(lblPass);

        txtPassword = new JPasswordField();
        txtPassword.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        txtPassword.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtPassword.addActionListener(e -> attemptLogin());
        formPanel.add(txtPassword);
        formPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        btnLogin = new JButton("Login");
        btnLogin.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnLogin.setBackground(new Color(70, 130, 180));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnLogin.setFocusPainted(false);
        btnLogin.addActionListener(e -> attemptLogin());
        formPanel.add(btnLogin);
        formPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        lblStatus = new JLabel(" ");
        lblStatus.setForeground(new Color(224, 27, 36));
        lblStatus.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblStatus.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(lblStatus);

        add(formPanel, BorderLayout.CENTER);

        setVisible(true);
    }

    private void attemptLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            lblStatus.setText("Please enter username and password.");
            return;
        }

        btnLogin.setEnabled(false);
        lblStatus.setText(" ");

        try (Connection conn = EnhancedDBConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT user_id, username, password_hash, user_type, customer_id FROM users WHERE username = ?");
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                lblStatus.setText("Invalid username or password.");
                btnLogin.setEnabled(true);
                return;
            }

            String storedHash = rs.getString("password_hash");
            if (!SecurityUtils.verifyPassword(password, storedHash)) {
                lblStatus.setText("Invalid username or password.");
                btnLogin.setEnabled(true);
                return;
            }

            String userType = rs.getString("user_type");
            int userId = rs.getInt("user_id");

            if ("admin".equalsIgnoreCase(userType)) {
                SessionManager.setCurrentAdmin(userId, username);
                dispose();
                new EnhancedDashboardFrame();
            } else {
                int customerId = rs.getInt("customer_id");
                SessionManager.setCurrentUser(customerId, "customer", username);
                dispose();
                new CustomerPortalFrame(customerId);
            }

        } catch (SQLException e) {
            lblStatus.setText("Login error: " + e.getMessage());
            btnLogin.setEnabled(true);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(LoginFrame::new);
    }
}
