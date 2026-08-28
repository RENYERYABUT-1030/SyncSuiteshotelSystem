package Hotel_Reservation.core;

import java.io.*;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * ConfigManager - Centralized Configuration Management
 * Loads configuration from application.properties file
 * Provides thread-safe access to application settings
 */
public class ConfigManager {
    private static final Logger logger = Logger.getLogger(ConfigManager.class.getName());
    private static ConfigManager instance;
    private Properties props;

    private ConfigManager() {
        props = new Properties();
        loadConfiguration();
    }

    public static synchronized ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager();
        }
        return instance;
    }

    private void loadConfiguration() {
        try (InputStream input = getClass().getClassLoader()
                .getResourceAsStream("application.properties")) {
            
            if (input == null) {
                logger.warning("application.properties not found, using defaults");
                loadDefaults();
                return;
            }
            
            props.load(input);
            logger.info("Configuration loaded successfully");
            
        } catch (IOException e) {
            logger.severe("Error loading configuration: " + e.getMessage());
            loadDefaults();
        }
    }

    private void loadDefaults() {
        // Database Defaults
        props.setProperty("db.url", "jdbc:mysql://localhost:3306/hotel_system");
        props.setProperty("db.username", "root");
        props.setProperty("db.password", "");
        props.setProperty("db.pool.size", "20");
        props.setProperty("db.pool.min", "5");
        props.setProperty("db.connection.timeout", "30");

        // Application Defaults
        props.setProperty("app.version", "2.1");
        props.setProperty("app.name", "Sync Suites Hotel Reservation System");
        props.setProperty("app.debug", "false");

        // Session Configuration
        props.setProperty("session.timeout.minutes", "30");
        props.setProperty("session.remember.me.days", "7");

        // Business Rules
        props.setProperty("booking.cancellation.days", "3");
        props.setProperty("booking.minimum.stay.days", "1");
        props.setProperty("maintenance.alert.days", "7");

        // External Services
        props.setProperty("sms.api.key", "textbelt");
        props.setProperty("sms.enabled", "true");
        props.setProperty("qr.api.url", "https://api.qrserver.com");

        // Email Configuration
        props.setProperty("email.smtp.host", "smtp.gmail.com");
        props.setProperty("email.smtp.port", "587");
        props.setProperty("email.from", "syncsuiteshotel@gmail.com");
        props.setProperty("email.enabled", "fokc zhcu wtlg sfyo");

        // UI Configuration
        props.setProperty("ui.theme", "dark");
        props.setProperty("ui.language", "en");
    }

    // String Properties
    public String getString(String key) {
        return props.getProperty(key, "");
    }

    public String getString(String key, String defaultValue) {
        return props.getProperty(key, defaultValue);
    }

    // Integer Properties
    public int getInt(String key) {
        return getInt(key, 0);
    }

    public int getInt(String key, int defaultValue) {
        try {
            return Integer.parseInt(props.getProperty(key, String.valueOf(defaultValue)));
        } catch (NumberFormatException e) {
            logger.warning("Invalid integer config for: " + key);
            return defaultValue;
        }
    }

    // Boolean Properties
    public boolean getBoolean(String key) {
        return getBoolean(key, false);
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        String value = props.getProperty(key, String.valueOf(defaultValue));
        return Boolean.parseBoolean(value);
    }

    // Double Properties
    public double getDouble(String key, double defaultValue) {
        try {
            return Double.parseDouble(props.getProperty(key, String.valueOf(defaultValue)));
        } catch (NumberFormatException e) {
            logger.warning("Invalid double config for: " + key);
            return defaultValue;
        }
    }

    // Database Configuration
    public String getDatabaseUrl() {
        return getString("db.url");
    }

    public String getDatabaseUsername() {
        return getString("db.username");
    }

    public String getDatabasePassword() {
        return getString("db.password");
    }

    public int getConnectionPoolSize() {
        return getInt("db.pool.size", 20);
    }

    // Session Configuration
    public int getSessionTimeoutMinutes() {
        return getInt("session.timeout.minutes", 30);
    }

    // Business Rules
    public int getBookingCancellationDays() {
        return getInt("booking.cancellation.days", 3);
    }

    public boolean isSmsEnabled() {
        return getBoolean("sms.enabled", true);
    }

    // Utility Methods
    public void setProperty(String key, String value) {
        props.setProperty(key, value);
    }

    public void reload() {
        props.clear();
        loadConfiguration();
        logger.info("Configuration reloaded");
    }

    @Override
    public String toString() {
        return "ConfigManager{" +
                "appVersion='" + getString("app.version") + '\'' +
                ", dbUrl='" + getString("db.url") + '\'' +
                ", sessionTimeout=" + getSessionTimeoutMinutes() + " min" +
                '}';
    }
}