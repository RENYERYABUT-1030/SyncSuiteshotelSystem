package Hotel_Reservation.utils;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class SecurityUtilsTest {
    @Test
    void hashesAndVerifiesPassword() {
        String hash = SecurityUtils.hashPassword("StrongPass1!");

        assertNotEquals("StrongPass1!", hash);
        assertTrue(SecurityUtils.verifyPassword("StrongPass1!", hash));
        assertFalse(SecurityUtils.verifyPassword("WrongPass1!", hash));
    }

    @Test
    void rejectsMissingPasswordForHashing() {
        assertThrows(IllegalArgumentException.class, () -> SecurityUtils.hashPassword(null));
        assertThrows(IllegalArgumentException.class, () -> SecurityUtils.hashPassword(""));
        assertFalse(SecurityUtils.verifyPassword(null, "hash"));
        assertFalse(SecurityUtils.verifyPassword("password", null));
        assertFalse(SecurityUtils.verifyPassword("password", "not-a-bcrypt-hash"));
    }

    @Test
    void validatesPasswordAndContactFormats() {
        assertTrue(SecurityUtils.isStrongPassword("StrongPass1!"));
        assertFalse(SecurityUtils.isStrongPassword("weakpassword"));
        assertTrue(SecurityUtils.isValidEmail("guest@example.com"));
        assertFalse(SecurityUtils.isValidEmail("guest@example"));
        assertTrue(SecurityUtils.isValidPhoneNumber("09171234567"));
        assertTrue(SecurityUtils.isValidPhoneNumber("+639171234567"));
        assertFalse(SecurityUtils.isValidPhoneNumber("123456"));
    }

    @Test
    void normalizesPhoneNumbers() {
        assertEquals("+639171234567", SecurityUtils.normalizePhoneNumber("0917 123 4567"));
        assertEquals("+639171234567", SecurityUtils.normalizePhoneNumber("639171234567"));
        assertEquals("+9171234567", SecurityUtils.normalizePhoneNumber("9171234567"));
        assertNull(SecurityUtils.normalizePhoneNumber(null));
    }

    @Test
    void validatesNamesDatesNumbersAndUsernames() {
        assertTrue(SecurityUtils.isValidName("Anne-Marie O'Neil"));
        assertFalse(SecurityUtils.isValidName("Anne123"));
        assertTrue(SecurityUtils.isValidDate("2026-08-28"));
        assertFalse(SecurityUtils.isValidDate("28-08-2026"));
        assertTrue(SecurityUtils.isValidNumber("12.5", 10, 20));
        assertFalse(SecurityUtils.isValidNumber("not-a-number", 10, 20));
        assertTrue(SecurityUtils.isValidUsername("guest_user"));
        assertFalse(SecurityUtils.isValidUsername("ab"));
    }

    @Test
    void sanitizesSqlHtmlAndTextInput() {
        assertEquals("alert(1) hello", SecurityUtils.sanitizeInput(" <script>alert(1)</script> hello "));
        assertEquals("O''Brien", SecurityUtils.sanitizeSQL("O'Brien"));
        assertEquals("&lt;strong&gt;Tom &amp; &quot;Sue&quot;&lt;/strong&gt;", SecurityUtils.escapeHtml("<strong>Tom & \"Sue\"</strong>"));
        assertEquals("", SecurityUtils.sanitizeInput(null));
    }

    @Test
    void validatesRegistrationAndBookingData() {
        SecurityUtils.ValidationResult registration = SecurityUtils.validateRegistration(
                "Anne", "Marie", "anne@example.com", "09171234567", "anne_m", "StrongPass1!");
        SecurityUtils.ValidationResult booking = SecurityUtils.validateBooking("2026-08-28", "2026-08-30", 2);

        assertTrue(registration.isValid);
        assertEquals("", registration.errorMessage);
        assertTrue(booking.isValid);
        assertFalse(SecurityUtils.validateBooking("2026-08-30", "2026-08-28", 2).isValid);
        assertFalse(SecurityUtils.validateRegistration("", "Marie", "anne@example.com", "09171234567", "anne_m", "StrongPass1!").isValid);
    }

    @Test
    void generatesSecurePasswordWithRequiredLengthAndComplexity() {
        String password = SecurityUtils.generateSecurePassword();

        assertEquals(16, password.length());
        assertTrue(password.matches("[A-Za-z0-9@$!%*?&]+"));
    }
}
