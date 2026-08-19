package Hotel_Reservation;
import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.awt.event.*;

/**
 * Enhanced Login Frame - Matches existing database schema
 * Supports Admin and Customer login
 */
public class LoginFrame extends JFrame {

    private JFrame frmHotelReservation;
    private JTextField userField;
    private JPasswordField passField;
    private JComboBox<String> roleCombo;
    private JCheckBox showPassCheck;

    // Session info
    public static int currentUserId = -1;
    public static String currentUserType = "";
    public static String currentUserName = "";

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                if (!DBConnection.testConnection()) {
                    JOptionPane.showMessageDialog(null, 
                        "Cannot connect to database!\nPlease check:\n1. MySQL is running\n2. Database 'hotel_system' exists\n3. Credentials in DBConnection.java are correct", 
                        "Database Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                new LoginFrame();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public LoginFrame() {
        initialize();
    }

    private void initialize() {
        frmHotelReservation = new JFrame();
        frmHotelReservation.setTitle("SYNC SUITES HOTEL - LOGIN PORTAL");
        frmHotelReservation.getContentPane().setBackground(new Color(46, 44, 122));
        frmHotelReservation.getContentPane().setLayout(null);
        frmHotelReservation.setSize(520, 450);
        frmHotelReservation.setLocationRelativeTo(null);
        frmHotelReservation.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frmHotelReservation.setResizable(false);

        // Title
        JLabel lblTitle = new JLabel("SYNC SUITES HOTEL");
        lblTitle.setBounds(0, 30, 520, 40);
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 32));
        lblTitle.setForeground(new Color(255, 215, 0));
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        frmHotelReservation.getContentPane().add(lblTitle);

        JLabel lblSubtitle = new JLabel("Reservation System v2.0");
        lblSubtitle.setBounds(0, 70, 520, 25);
        lblSubtitle.setFont(new Font("SansSerif", Font.PLAIN, 16));
        lblSubtitle.setForeground(Color.WHITE);
        lblSubtitle.setHorizontalAlignment(SwingConstants.CENTER);
        frmHotelReservation.getContentPane().add(lblSubtitle);

        // Login Panel
        JPanel loginPanel = new JPanel();
        loginPanel.setBounds(60, 110, 400, 260);
        loginPanel.setBackground(new Color(60, 58, 140));
        loginPanel.setLayout(null);
        loginPanel.setBorder(BorderFactory.createLineBorder(new Color(255, 215, 0), 2));
        frmHotelReservation.getContentPane().add(loginPanel);

        // Role Selection
        JLabel lblRole = new JLabel("Login As:");
        lblRole.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblRole.setForeground(Color.WHITE);
        lblRole.setBounds(30, 15, 100, 25);
        loginPanel.add(lblRole);

        String[] roles = {"Admin", "Customer"};
        roleCombo = new JComboBox<>(roles);
        roleCombo.setBounds(130, 15, 230, 25);
        roleCombo.setFont(new Font("SansSerif", Font.PLAIN, 14));
        loginPanel.add(roleCombo);

        // Username
        JLabel lblUsername = new JLabel("Username:");
        lblUsername.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblUsername.setForeground(Color.WHITE);
        lblUsername.setBounds(30, 55, 100, 25);
        loginPanel.add(lblUsername);

        userField = new JTextField();
        userField.setBounds(130, 55, 230, 25);
        userField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        loginPanel.add(userField);

        // Password
        JLabel lblPassword = new JLabel("Password:");
        lblPassword.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblPassword.setForeground(Color.WHITE);
        lblPassword.setBounds(30, 95, 100, 25);
        loginPanel.add(lblPassword);

        passField = new JPasswordField();
        passField.setBounds(130, 95, 230, 25);
        passField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        loginPanel.add(passField);

        // Show password
        showPassCheck = new JCheckBox("Show Password");
        showPassCheck.setBounds(130, 125, 150, 20);
        showPassCheck.setForeground(Color.WHITE);
        showPassCheck.setBackground(new Color(60, 58, 140));
        showPassCheck.setFont(new Font("SansSerif", Font.PLAIN, 12));
        showPassCheck.addActionListener(e -> {
            if (showPassCheck.isSelected()) {
                passField.setEchoChar((char) 0);
            } else {
                passField.setEchoChar('●');
            }
        });
        loginPanel.add(showPassCheck);

        // Login Button
        JButton btnLogin = new JButton("LOGIN");
        btnLogin.setBounds(130, 160, 230, 35);
        btnLogin.setFont(new Font("SansSerif", Font.BOLD, 16));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setBackground(new Color(46, 194, 126));
        btnLogin.setFocusPainted(false);
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogin.addActionListener(e -> handleLogin());
        loginPanel.add(btnLogin);

        // Register Button
        JButton btnRegister = new JButton("New Customer? Register Here");
        btnRegister.setBounds(130, 205, 230, 25);
        btnRegister.setFont(new Font("SansSerif", Font.PLAIN, 12));
        btnRegister.setForeground(new Color(255, 215, 0));
        btnRegister.setBackground(new Color(60, 58, 140));
        btnRegister.setBorderPainted(false);
        btnRegister.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRegister.addActionListener(e -> showRegistrationDialog());
        loginPanel.add(btnRegister);

        // Version
        JLabel lblVersion = new JLabel("v2.0 - Enhanced Edition");
        lblVersion.setBounds(0, 390, 520, 20);
        lblVersion.setFont(new Font("SansSerif", Font.ITALIC, 11));
        lblVersion.setForeground(new Color(180, 180, 180));
        lblVersion.setHorizontalAlignment(SwingConstants.CENTER);
        frmHotelReservation.getContentPane().add(lblVersion);

        passField.addActionListener(e -> handleLogin());
        userField.addActionListener(e -> handleLogin());

        frmHotelReservation.setVisible(true);
    }

    private void handleLogin() {
        String username = userField.getText().trim();
        String password = new String(passField.getPassword());
        String role = (String) roleCombo.getSelectedItem();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(frmHotelReservation, 
                "Please enter both username and password.", 
                "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            if (role.equals("Admin")) {
                // Admin Login - uses existing admins table (adminID, username, password)
                String sql = "SELECT * FROM admins WHERE username = ? AND password = ?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, username);
                ps.setString(2, password);
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    currentUserId = rs.getInt("adminID");
                    currentUserType = "admin";
                    currentUserName = rs.getString("username");

                    JOptionPane.showMessageDialog(frmHotelReservation, 
                        "Welcome, " + currentUserName + "!", 
                        "Login Successful", JOptionPane.INFORMATION_MESSAGE);

                    new DashboardFrame();
                    frmHotelReservation.dispose();
                } else {
                    JOptionPane.showMessageDialog(frmHotelReservation, 
                        "Invalid admin credentials.", 
                        "Login Failed", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                // Customer Login - uses users table
                String sql = "SELECT u.*, c.first_name, c.last_name FROM users u " +
                           "JOIN customers c ON u.customer_id = c.customer_id " +
                           "WHERE u.username = ? AND u.password = ? AND u.is_active = TRUE";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, username);
                ps.setString(2, password);
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    currentUserId = rs.getInt("user_id");
                    currentUserType = "customer";
                    currentUserName = rs.getString("first_name") + " " + rs.getString("last_name");
                    int customerId = rs.getInt("customer_id");

                    PreparedStatement updatePs = conn.prepareStatement(
                        "UPDATE users SET last_login = NOW() WHERE user_id = ?");
                    updatePs.setInt(1, currentUserId);
                    updatePs.executeUpdate();

                    JOptionPane.showMessageDialog(frmHotelReservation, 
                        "Welcome, " + currentUserName + "!", 
                        "Login Successful", JOptionPane.INFORMATION_MESSAGE);

                    new CustomerPortalFrame(customerId);
                    frmHotelReservation.dispose();
                } else {
                    JOptionPane.showMessageDialog(frmHotelReservation, 
                        "Invalid customer credentials or account inactive.", 
                        "Login Failed", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(frmHotelReservation, 
                "Database error: " + ex.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showRegistrationDialog() {
        JDialog dialog = new JDialog(frmHotelReservation, "Customer Registration", true);
        dialog.setSize(450, 500);
        dialog.setLocationRelativeTo(frmHotelReservation);
        dialog.getContentPane().setLayout(null);
        dialog.getContentPane().setBackground(new Color(46, 44, 122));

        JLabel lblTitle = new JLabel("Create Customer Account");
        lblTitle.setBounds(0, 15, 450, 30);
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 20));
        lblTitle.setForeground(new Color(255, 215, 0));
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        dialog.getContentPane().add(lblTitle);

        String[] labels = {"First Name:", "Last Name:", "Username:", "Password:", "Email:", "Phone Number:"};
        JTextField[] fields = new JTextField[6];
        int y = 60;

        for (int i = 0; i < labels.length; i++) {
            JLabel lbl = new JLabel(labels[i]);
            lbl.setBounds(50, y, 120, 25);
            lbl.setFont(new Font("SansSerif", Font.BOLD, 13));
            lbl.setForeground(Color.WHITE);
            dialog.getContentPane().add(lbl);

            if (i == 3) {
                fields[i] = new JPasswordField();
            } else {
                fields[i] = new JTextField();
            }
            fields[i].setBounds(170, y, 220, 25);
            fields[i].setFont(new Font("SansSerif", Font.PLAIN, 13));
            dialog.getContentPane().add(fields[i]);
            y += 45;
        }

        fields[0].addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (Character.isLetter(c)) e.setKeyChar(Character.toUpperCase(c));
            }
        });
        fields[1].addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (Character.isLetter(c)) e.setKeyChar(Character.toUpperCase(c));
            }
        });
        fields[5].addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (!Character.isDigit(c)) e.consume();
                if (fields[5].getText().length() >= 11) e.consume();
            }
        });

        JButton btnRegister = new JButton("REGISTER");
        btnRegister.setBounds(170, 340, 220, 35);
        btnRegister.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnRegister.setForeground(Color.WHITE);
        btnRegister.setBackground(new Color(46, 194, 126));
        btnRegister.addActionListener(e -> {
            String firstName = fields[0].getText().trim();
            String lastName = fields[1].getText().trim();
            String username = fields[2].getText().trim();
            String password = new String(((JPasswordField)fields[3]).getPassword());
            String email = fields[4].getText().trim();
            String phone = fields[5].getText().trim();

            if (firstName.isEmpty() || lastName.isEmpty() || username.isEmpty() || 
                password.isEmpty() || email.isEmpty() || phone.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please fill in all fields!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (password.length() < 6) {
                JOptionPane.showMessageDialog(dialog, "Password must be at least 6 characters!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!email.contains("@") || !email.contains(".")) {
                JOptionPane.showMessageDialog(dialog, "Please enter a valid email!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!phone.matches("\\d{10,11}")) {
                JOptionPane.showMessageDialog(dialog, "Phone must be 10-11 digits!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try (Connection conn = DBConnection.getConnection()) {
                conn.setAutoCommit(false);

                PreparedStatement checkPs = conn.prepareStatement(
                    "SELECT user_id FROM users WHERE username = ?");
                checkPs.setString(1, username);
                if (checkPs.executeQuery().next()) {
                    JOptionPane.showMessageDialog(dialog, "Username already exists!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                PreparedStatement custPs = conn.prepareStatement(
                    "INSERT INTO customers (first_name, last_name, phone_number, email, created_at) VALUES (?,?,?,?, NOW())",
                    Statement.RETURN_GENERATED_KEYS);
                custPs.setString(1, firstName);
                custPs.setString(2, lastName);
                custPs.setString(3, phone);
                custPs.setString(4, email);
                custPs.executeUpdate();

                ResultSet keys = custPs.getGeneratedKeys();
                int customerId = 0;
                if (keys.next()) customerId = keys.getInt(1);

                PreparedStatement userPs = conn.prepareStatement(
                    "INSERT INTO users (customer_id, username, password, email, phone_number) VALUES (?,?,?,?,?)");
                userPs.setInt(1, customerId);
                userPs.setString(2, username);
                userPs.setString(3, password);
                userPs.setString(4, email);
                userPs.setString(5, phone);
                userPs.executeUpdate();

                conn.commit();
                JOptionPane.showMessageDialog(dialog, 
                    "Registration successful! You can now login.", 
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, 
                    "Registration failed: " + ex.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        dialog.getContentPane().add(btnRegister);

        dialog.setVisible(true);
    }
}