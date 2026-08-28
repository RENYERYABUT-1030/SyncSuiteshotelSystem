package Hotel_Reservation;

import Hotel_Reservation.core.EnhancedDBConnection;
import Hotel_Reservation.utils.SecurityUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.*;

/**
 * Settings Panel
 * - Shows the currently logged-in account's info
 * - Lets the user change their own password
 * - Email (SMTP) settings, used by ReportsPanel's "Send via Email" receipt
 *   feature, which reads smtp_host/smtp_port/smtp_username/smtp_password
 *   from the system_settings table. Previously there was no screen to set
 *   these, so email sending always failed with "SMTP not configured!".
 * - SMS settings, used by ServicesManager.sendSMSAlert() for the TextBelt
 *   API key, which was previously hardcoded in source.
 *
 * This panel was missing entirely before; the dashboard's "Settings" nav
 * button previously just showed a "Coming Soon" dialog.
 */
public class SettingsPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private JPasswordField txtCurrentPassword;
    private JPasswordField txtNewPassword;
    private JPasswordField txtConfirmPassword;
    private JLabel lblStatus;

    // Email (SMTP) settings
    private JTextField txtSmtpHost;
    private JTextField txtSmtpPort;
    private JTextField txtSmtpUsername;
    private JPasswordField txtSmtpPassword;
    private JLabel lblEmailStatus;
    private JLabel lblMailLibStatus;

    // SMS settings
    private JPasswordField txtSmsApiKey;
    private JTextField txtSmsTestPhone;
    private JLabel lblSmsStatus;

    public SettingsPanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(245, 245, 250));

        JLabel titleLbl = new JLabel("Settings");
        titleLbl.setForeground(new Color(0, 0, 47));
        titleLbl.setFont(new Font("SansSerif", Font.BOLD, 20));
        titleLbl.setBorder(new EmptyBorder(15, 15, 15, 15));
        add(titleLbl, BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(0, 15, 15, 15));

        body.add(buildAccountInfoCard());
        body.add(Box.createVerticalStrut(20));
        body.add(buildChangePasswordCard());
        body.add(Box.createVerticalStrut(20));
        body.add(buildEmailSettingsCard());
        body.add(Box.createVerticalStrut(20));
        body.add(buildSmsSettingsCard());

        JScrollPane scroll = new JScrollPane(body);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);

        // FIX: loading settings used to run synchronously in the constructor,
        // on the EDT. If the DB connection was slow or the system_settings
        // table didn't exist yet, this could stall rendering and make the
        // page appear blank. Now it runs on a background thread, same
        // pattern as EnhancedDashboardFrame's loadDashboardStatistics().
        new Thread(this::loadEmailAndSmsSettings).start();
    }

    // ─────────────────────────────────────────────────────────
    // ACCOUNT INFO CARD
    // ─────────────────────────────────────────────────────────
    private JPanel buildAccountInfoCard() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            new EmptyBorder(20, 20, 20, 20)));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(500, 160));

        JLabel header = new JLabel("Account Information");
        header.setFont(new Font("SansSerif", Font.BOLD, 15));
        header.setForeground(new Color(31, 31, 60));
        card.add(header);
        card.add(Box.createVerticalStrut(12));

        card.add(infoRow("Username:", SessionManager.getCurrentUserName()));
        card.add(Box.createVerticalStrut(6));
        card.add(infoRow("Role:", capitalize(SessionManager.getCurrentUserType())));

        return card;
    }

    private JPanel infoRow(String label, String value) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        lbl.setForeground(new Color(100, 100, 100));
        row.add(lbl);

        JLabel val = new JLabel(value != null ? value : "N/A");
        val.setFont(new Font("SansSerif", Font.PLAIN, 12));
        val.setForeground(new Color(31, 31, 60));
        row.add(val);

        return row;
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return "N/A";
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    // ─────────────────────────────────────────────────────────
    // CHANGE PASSWORD CARD
    // ─────────────────────────────────────────────────────────
    private JPanel buildChangePasswordCard() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            new EmptyBorder(20, 20, 20, 20)));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(500, 320));

        JLabel header = new JLabel("Change Password");
        header.setFont(new Font("SansSerif", Font.BOLD, 15));
        header.setForeground(new Color(31, 31, 60));
        card.add(header);
        card.add(Box.createVerticalStrut(15));

        card.add(fieldLabel("Current Password"));
        txtCurrentPassword = new JPasswordField();
        stylePasswordField(txtCurrentPassword);
        card.add(txtCurrentPassword);
        card.add(Box.createVerticalStrut(12));

        card.add(fieldLabel("New Password"));
        txtNewPassword = new JPasswordField();
        stylePasswordField(txtNewPassword);
        card.add(txtNewPassword);
        card.add(Box.createVerticalStrut(12));

        card.add(fieldLabel("Confirm New Password"));
        txtConfirmPassword = new JPasswordField();
        stylePasswordField(txtConfirmPassword);
        card.add(txtConfirmPassword);
        card.add(Box.createVerticalStrut(15));

        JButton btnSave = new JButton("Update Password");
        btnSave.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnSave.setBackground(new Color(70, 130, 180));
        btnSave.setForeground(Color.WHITE);
        btnSave.setFont(new Font("SansSerif", Font.BOLD, 13));
        btnSave.setFocusPainted(false);
        btnSave.addActionListener(e -> changePassword());
        card.add(btnSave);
        card.add(Box.createVerticalStrut(10));

        lblStatus = new JLabel(" ");
        lblStatus.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblStatus.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(lblStatus);

        return card;
    }

    private JLabel fieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        lbl.setBorder(new EmptyBorder(0, 0, 4, 0));
        return lbl;
    }

    private void stylePasswordField(JPasswordField field) {
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setFont(new Font("SansSerif", Font.PLAIN, 13));
    }

    private void changePassword() {
        String current = new String(txtCurrentPassword.getPassword());
        String newPass = new String(txtNewPassword.getPassword());
        String confirm = new String(txtConfirmPassword.getPassword());

        if (current.isEmpty() || newPass.isEmpty() || confirm.isEmpty()) {
            showStatus("Please fill in all fields.", true);
            return;
        }
        if (newPass.length() < 6) {
            showStatus("New password must be at least 6 characters.", true);
            return;
        }
        if (!newPass.equals(confirm)) {
            showStatus("New password and confirmation do not match.", true);
            return;
        }

        String username = SessionManager.getCurrentUserName();

        try (Connection conn = EnhancedDBConnection.getConnection()) {
            PreparedStatement check = conn.prepareStatement(
                "SELECT password_hash FROM users WHERE username = ?");
            check.setString(1, username);
            ResultSet rs = check.executeQuery();

            if (!rs.next()) {
                showStatus("Could not find your account record.", true);
                return;
            }

            String storedHash = rs.getString("password_hash");
            if (!SecurityUtils.verifyPassword(current, storedHash)) {
                showStatus("Current password is incorrect.", true);
                return;
            }

            String newHash = SecurityUtils.hashPassword(newPass);
            PreparedStatement update = conn.prepareStatement(
                "UPDATE users SET password_hash = ? WHERE username = ?");
            update.setString(1, newHash);
            update.setString(2, username);
            update.executeUpdate();

            txtCurrentPassword.setText("");
            txtNewPassword.setText("");
            txtConfirmPassword.setText("");
            showStatus("Password updated successfully!", false);

        } catch (SQLException e) {
            showStatus("Error: " + e.getMessage(), true);
        }
    }

    // ─────────────────────────────────────────────────────────
    // EMAIL (SMTP) SETTINGS CARD
    // ─────────────────────────────────────────────────────────
    private JPanel buildEmailSettingsCard() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            new EmptyBorder(20, 20, 20, 20)));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(500, 400));

        JLabel header = new JLabel("📧 Email Settings (SMTP)");
        header.setFont(new Font("SansSerif", Font.BOLD, 15));
        header.setForeground(new Color(31, 31, 60));
        card.add(header);

        JLabel sub = new JLabel("Used when sending receipts via email from Reports.");
        sub.setFont(new Font("SansSerif", Font.PLAIN, 11));
        sub.setForeground(new Color(150, 150, 150));
        sub.setBorder(new EmptyBorder(2, 0, 8, 0));
        card.add(sub);

        // FIX: Previously there was NO way to tell, from the UI, whether the
        // JavaMail/Activation jars were actually visible to the running app.
        // "I added the jar but email still doesn't work" is almost always
        // this — the jar is in the IDE build path but not on the classpath
        // that actually runs. This label shows the live, real reason.
        lblMailLibStatus = new JLabel(" ");
        lblMailLibStatus.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lblMailLibStatus.setBorder(new EmptyBorder(0, 0, 15, 0));
        lblMailLibStatus.setAlignmentX(Component.LEFT_ALIGNMENT);
        refreshMailLibStatus();
        card.add(lblMailLibStatus);

        card.add(fieldLabel("SMTP Host (e.g. smtp.gmail.com)"));
        txtSmtpHost = new JTextField();
        styleTextField(txtSmtpHost);
        card.add(txtSmtpHost);
        card.add(Box.createVerticalStrut(12));

        card.add(fieldLabel("SMTP Port (e.g. 587)"));
        txtSmtpPort = new JTextField();
        styleTextField(txtSmtpPort);
        card.add(txtSmtpPort);
        card.add(Box.createVerticalStrut(12));

        card.add(fieldLabel("SMTP Username / Sender Email"));
        txtSmtpUsername = new JTextField();
        styleTextField(txtSmtpUsername);
        card.add(txtSmtpUsername);
        card.add(Box.createVerticalStrut(12));

        card.add(fieldLabel("SMTP Password / App Password"));
        txtSmtpPassword = new JPasswordField();
        stylePasswordField(txtSmtpPassword);
        card.add(txtSmtpPassword);
        card.add(Box.createVerticalStrut(15));

        JPanel emailBtnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        emailBtnRow.setOpaque(false);
        emailBtnRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        emailBtnRow.setMaximumSize(new Dimension(500, 40));

        JButton btnSaveEmail = new JButton("Save Email Settings");
        btnSaveEmail.setBackground(new Color(70, 130, 180));
        btnSaveEmail.setForeground(Color.WHITE);
        btnSaveEmail.setFont(new Font("SansSerif", Font.BOLD, 13));
        btnSaveEmail.setFocusPainted(false);
        btnSaveEmail.addActionListener(e -> saveEmailSettings());
        emailBtnRow.add(btnSaveEmail);

        // FIX: New — lets the admin verify SMTP host/port/credentials
        // actually work *before* a guest gets a failed-email error, and
        // reports the real underlying reason (bad app password, wrong
        // port, host unreachable, etc.) instead of a generic failure.
        JButton btnTestEmail = new JButton("🔌 Test Connection");
        btnTestEmail.setBackground(new Color(46, 194, 126));
        btnTestEmail.setForeground(Color.WHITE);
        btnTestEmail.setFont(new Font("SansSerif", Font.BOLD, 13));
        btnTestEmail.setFocusPainted(false);
        btnTestEmail.addActionListener(e -> testEmailConnection());
        emailBtnRow.add(btnTestEmail);

        card.add(emailBtnRow);
        card.add(Box.createVerticalStrut(10));

        lblEmailStatus = new JLabel(" ");
        lblEmailStatus.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblEmailStatus.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(lblEmailStatus);

        return card;
    }

    private void saveEmailSettings() {
        String host = txtSmtpHost.getText().trim();
        String port = txtSmtpPort.getText().trim();
        String username = txtSmtpUsername.getText().trim();
        String password = new String(txtSmtpPassword.getPassword());

        if (host.isEmpty() || port.isEmpty() || username.isEmpty()) {
            lblEmailStatus.setForeground(new Color(224, 27, 36));
            lblEmailStatus.setText("Host, port, and username are required.");
            return;
        }

        try (Connection conn = EnhancedDBConnection.getConnection()) {
            upsertSetting(conn, "smtp_host", host);
            upsertSetting(conn, "smtp_port", port);
            upsertSetting(conn, "smtp_username", username);
            // Only overwrite the stored password if the admin actually typed
            // a new one; leaves the existing one intact otherwise.
            if (!password.isEmpty()) {
                upsertSetting(conn, "smtp_password", password);
            }

            lblEmailStatus.setForeground(new Color(46, 194, 126));
            lblEmailStatus.setText("Email settings saved!");
        } catch (SQLException e) {
            lblEmailStatus.setForeground(new Color(224, 27, 36));
            lblEmailStatus.setText("Error: " + e.getMessage());
        }
    }

    /**
     * Shows whether the JavaMail/Activation jars are actually visible to the
     * running application, and — if not — exactly which class is missing.
     * Delegates the actual check to ReportsPanel, which performs it once in
     * a static initializer.
     */
    private void refreshMailLibStatus() {
        if (Hotel_Reservation.ReportsPanel.isJavaMailAvailable()) {
            lblMailLibStatus.setForeground(new Color(46, 194, 126));
            lblMailLibStatus.setText("✅ Mail library detected on classpath.");
        } else {
            lblMailLibStatus.setForeground(new Color(224, 27, 36));
            lblMailLibStatus.setText("<html><body style='width:440px'>❌ "
                + Hotel_Reservation.ReportsPanel.getJavaMailStatusMessage() + "</body></html>");
        }
    }

    /**
     * Test the SMTP host/port/credentials currently in the form WITHOUT
     * sending a real email — just opens and closes an authenticated SMTP
     * connection via javax.mail.Transport, using reflection so this class
     * still compiles even when mail.jar is absent.
     *
     * This is what actually answers "bakit hindi gumagana ang email" —
     * it tells you whether the failure is (a) library missing, (b) wrong
     * host/port, (c) bad username/app-password, or (d) network/firewall.
     */
    private void testEmailConnection() {
        refreshMailLibStatus();

        if (!Hotel_Reservation.ReportsPanel.isJavaMailAvailable()) {
            JOptionPane.showMessageDialog(this,
                "Can't test — the mail library isn't loaded.\n\n"
                + Hotel_Reservation.ReportsPanel.getJavaMailStatusMessage(),
                "Library Missing", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String host = txtSmtpHost.getText().trim();
        String port = txtSmtpPort.getText().trim();
        String username = txtSmtpUsername.getText().trim();
        String password = new String(txtSmtpPassword.getPassword());

        if (host.isEmpty() || port.isEmpty() || username.isEmpty() || password.isEmpty()) {
            lblEmailStatus.setForeground(new Color(224, 27, 36));
            lblEmailStatus.setText("Fill in host, port, username, and password before testing.");
            return;
        }

        lblEmailStatus.setForeground(new Color(70, 130, 180));
        lblEmailStatus.setText("Testing connection...");

        // Run off the EDT so the UI doesn't freeze while connecting.
        new Thread(() -> {
            String resultText;
            Color resultColor;
            try {
                java.util.Properties props = new java.util.Properties();
                props.setProperty("mail.smtp.auth", "true");
                props.setProperty("mail.smtp.starttls.enable", "true");
                props.setProperty("mail.smtp.host", host);
                props.setProperty("mail.smtp.port", port);
                // Reasonable timeouts so a blocked port fails fast instead of hanging.
                props.setProperty("mail.smtp.connectiontimeout", "8000");
                props.setProperty("mail.smtp.timeout", "8000");

                Class<?> sessionClass = Class.forName("javax.mail.Session");
                Class<?> transportClass = Class.forName("javax.mail.Transport");

                Object session = sessionClass.getMethod("getInstance", java.util.Properties.class)
                                              .invoke(null, props);
                Object transport = sessionClass.getMethod("getTransport", String.class)
                                                .invoke(session, "smtp");

                transportClass.getMethod("connect", String.class, String.class, String.class)
                              .invoke(transport, host, username, password);
                transportClass.getMethod("close").invoke(transport);

                resultText = "✅ Connected and authenticated successfully!";
                resultColor = new Color(46, 194, 126);
            } catch (Exception ex) {
                Throwable cause = ex;
                while (cause.getCause() != null && cause.getCause() != cause) {
                    cause = cause.getCause();
                }
                resultText = "❌ Connection failed: "
                    + (cause.getMessage() != null ? cause.getMessage() : cause.toString());
                resultColor = new Color(224, 27, 36);
            }

            final String finalText = resultText;
            final Color finalColor = resultColor;
            SwingUtilities.invokeLater(() -> {
                lblEmailStatus.setForeground(finalColor);
                lblEmailStatus.setText(finalText);
            });
        }).start();
    }

    // ─────────────────────────────────────────────────────────
    // SMS SETTINGS CARD
    // ─────────────────────────────────────────────────────────
    private JPanel buildSmsSettingsCard() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            new EmptyBorder(20, 20, 20, 20)));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(500, 300));

        JLabel header = new JLabel("📱 SMS Settings (TextBelt)");
        header.setFont(new Font("SansSerif", Font.BOLD, 15));
        header.setForeground(new Color(31, 31, 60));
        card.add(header);

        JLabel sub = new JLabel("Used when sending SMS alerts to guests. Get a key at textbelt.com.");
        sub.setFont(new Font("SansSerif", Font.PLAIN, 11));
        sub.setForeground(new Color(150, 150, 150));
        sub.setBorder(new EmptyBorder(2, 0, 15, 0));
        card.add(sub);

        card.add(fieldLabel("TextBelt API Key"));
        txtSmsApiKey = new JPasswordField();
        stylePasswordField(txtSmsApiKey);
        card.add(txtSmsApiKey);
        card.add(Box.createVerticalStrut(15));

        JButton btnSaveSms = new JButton("Save SMS Settings");
        btnSaveSms.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnSaveSms.setBackground(new Color(70, 130, 180));
        btnSaveSms.setForeground(Color.WHITE);
        btnSaveSms.setFont(new Font("SansSerif", Font.BOLD, 13));
        btnSaveSms.setFocusPainted(false);
        btnSaveSms.addActionListener(e -> saveSmsSettings());
        card.add(btnSaveSms);
        card.add(Box.createVerticalStrut(15));

        card.add(fieldLabel("Send Test SMS to (e.g. 09171234567)"));
        JPanel testRow = new JPanel(new BorderLayout(8, 0));
        testRow.setOpaque(false);
        testRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        testRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));

        txtSmsTestPhone = new JTextField();
        txtSmsTestPhone.setFont(new Font("SansSerif", Font.PLAIN, 13));
        testRow.add(txtSmsTestPhone, BorderLayout.CENTER);

        JButton btnTestSms = new JButton("Send Test");
        btnTestSms.setBackground(new Color(46, 194, 126));
        btnTestSms.setForeground(Color.WHITE);
        btnTestSms.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnTestSms.setFocusPainted(false);
        btnTestSms.addActionListener(e -> sendTestSms());
        testRow.add(btnTestSms, BorderLayout.EAST);
        card.add(testRow);
        card.add(Box.createVerticalStrut(10));

        lblSmsStatus = new JLabel(" ");
        lblSmsStatus.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblSmsStatus.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(lblSmsStatus);

        return card;
    }

    private void saveSmsSettings() {
        String apiKey = new String(txtSmsApiKey.getPassword()).trim();

        if (apiKey.isEmpty()) {
            lblSmsStatus.setForeground(new Color(224, 27, 36));
            lblSmsStatus.setText("Please enter an API key.");
            return;
        }

        try (Connection conn = EnhancedDBConnection.getConnection()) {
            upsertSetting(conn, "sms_api_key", apiKey);
            lblSmsStatus.setForeground(new Color(46, 194, 126));
            lblSmsStatus.setText("SMS settings saved!");
        } catch (SQLException e) {
            lblSmsStatus.setForeground(new Color(224, 27, 36));
            lblSmsStatus.setText("Error: " + e.getMessage());
        }
    }

    private void sendTestSms() {
        String phone = txtSmsTestPhone.getText().trim();
        if (phone.isEmpty()) {
            lblSmsStatus.setForeground(new Color(224, 27, 36));
            lblSmsStatus.setText("Enter a phone number to send the test to.");
            return;
        }

        // ServicesManager.sendSMSAlert() sends asynchronously on a background
        // thread already, so this won't freeze the UI.
        ServicesManager.sendSMSAlert(phone, "This is a test message from Sync Suites Hotel.");
        lblSmsStatus.setForeground(new Color(46, 194, 126));
        lblSmsStatus.setText("Test SMS queued — check your phone in a moment.");
    }

    // ─────────────────────────────────────────────────────────
    // SHARED: system_settings load/save helpers
    // ─────────────────────────────────────────────────────────
    private void loadEmailAndSmsSettings() {
        try (Connection conn = EnhancedDBConnection.getConnection()) {
            // FIX: auto-create the settings table if it doesn't exist yet,
            // instead of just failing. This table is new (SettingsPanel and
            // ReportsPanel's email feature are the only things that use it),
            // so most existing databases won't have it.
            try (Statement create = conn.createStatement()) {
                create.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS system_settings (" +
                    "setting_key VARCHAR(100) PRIMARY KEY, " +
                    "setting_value TEXT)");
            }

            PreparedStatement ps = conn.prepareStatement(
                "SELECT setting_key, setting_value FROM system_settings " +
                "WHERE setting_key LIKE 'smtp_%' OR setting_key = 'sms_api_key'");
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String key = rs.getString("setting_key");
                String val = rs.getString("setting_value");
                if (val == null) continue;

                final String fKey = key;
                final String fVal = val;
                SwingUtilities.invokeLater(() -> {
                    switch (fKey) {
                        case "smtp_host": txtSmtpHost.setText(fVal); break;
                        case "smtp_port": txtSmtpPort.setText(fVal); break;
                        case "smtp_username": txtSmtpUsername.setText(fVal); break;
                        // Password intentionally left blank in the UI even
                        // though it's stored — only overwritten if the admin
                        // retypes it.
                        case "sms_api_key": txtSmsApiKey.setText(fVal); break;
                        default: break;
                    }
                });
            }
        } catch (Exception e) {
            // Broad catch on purpose: this runs on a background thread during
            // panel construction, so nothing here should ever be able to
            // prevent the panel itself from rendering.
            System.err.println("Could not load system_settings: " + e.getMessage());
        }
    }

    /**
     * Insert-or-update a single key in system_settings.
     * Assumes: system_settings(setting_key VARCHAR PRIMARY KEY, setting_value VARCHAR)
     * Adjust the ON DUPLICATE KEY clause if your schema differs.
     */
    private void upsertSetting(Connection conn, String key, String value) throws SQLException {
        try (Statement create = conn.createStatement()) {
            create.executeUpdate(
                "CREATE TABLE IF NOT EXISTS system_settings (" +
                "setting_key VARCHAR(100) PRIMARY KEY, " +
                "setting_value TEXT)");
        }

        PreparedStatement ps = conn.prepareStatement(
            "INSERT INTO system_settings (setting_key, setting_value) VALUES (?, ?) " +
            "ON DUPLICATE KEY UPDATE setting_value = ?");
        ps.setString(1, key);
        ps.setString(2, value);
        ps.setString(3, value);
        ps.executeUpdate();
    }

    private void styleTextField(JTextField field) {
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setFont(new Font("SansSerif", Font.PLAIN, 13));
    }

    private void showStatus(String message, boolean isError) {
        lblStatus.setForeground(isError ? new Color(224, 27, 36) : new Color(46, 194, 126));
        lblStatus.setText(message);
    }
}