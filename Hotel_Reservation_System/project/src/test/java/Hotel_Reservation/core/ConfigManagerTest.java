package Hotel_Reservation.core;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ConfigManagerTest {
    private ConfigManager config;

    @BeforeEach
    void reloadConfiguration() {
        config = ConfigManager.getInstance();
        config.reload();
    }

    @Test
    void loadsExpectedDefaultsWhenPropertiesFileIsUnavailable() {
        assertEquals("jdbc:mysql://localhost:3306/hotel_schema", config.getDatabaseUrl());
        assertEquals("root", config.getDatabaseUsername());
        assertEquals(20, config.getConnectionPoolSize());
        assertEquals(30, config.getSessionTimeoutMinutes());
        assertEquals(3, config.getBookingCancellationDays());
        assertTrue(config.isSmsEnabled());
    }

    @Test
    void returnsConfiguredValuesAndFallbacks() {
        config.setProperty("number", "12");
        config.setProperty("decimal", "4.5");
        config.setProperty("enabled", "true");
        config.setProperty("invalidNumber", "not-a-number");
        config.setProperty("invalidDecimal", "not-a-decimal");

        assertEquals("12", config.getString("number"));
        assertEquals("fallback", config.getString("missing", "fallback"));
        assertEquals(12, config.getInt("number"));
        assertEquals(9, config.getInt("missing", 9));
        assertEquals(9, config.getInt("invalidNumber", 9));
        assertEquals(4.5, config.getDouble("decimal", 0), 0.0001);
        assertEquals(2.5, config.getDouble("invalidDecimal", 2.5), 0.0001);
        assertTrue(config.getBoolean("enabled"));
        assertFalse(config.getBoolean("missing"));
    }

    @Test
    void reloadRestoresDefaultsAndToStringIncludesKeySettings() {
        config.setProperty("app.version", "custom");
        config.reload();

        assertEquals("2.1", config.getString("app.version"));
        assertTrue(config.toString().contains("appVersion='2.1'"));
        assertTrue(config.toString().contains("sessionTimeout=30 min"));
    }
}
