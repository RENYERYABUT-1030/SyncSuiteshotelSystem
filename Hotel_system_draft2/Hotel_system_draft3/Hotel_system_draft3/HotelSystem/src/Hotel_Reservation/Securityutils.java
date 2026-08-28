package Hotel_Reservation.utils;

import java.security.SecureRandom;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Security Utilities - Password Hashing, Validation & Sanitization
 * 
 * Uses BCrypt for password hashing (secure, salt-included)
 * Provides input validation for common fields
 * 
 * Maven Dependency:
 * <dependency>
 *     <groupId>org.mindrot</groupId>
 *     <artifactId>jbcrypt</artifactId>
 *     <version>0.4</version>
 * </dependency>
 */
public class SecurityUtils {
    private static final Logger logger = Logger.getLogger(SecurityUtils.class.getName());

    // Regex Patterns
    private static final Pattern EMAIL_PATTERN = 
        Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}$");
    
    private static final Pattern PHONE_PATTERN = 
        Pattern.compile("^(09|\\+639)\\d{9}$"); // PH format: 09XXXXXXXXX or +639XXXXXXXXX
    
    private static final Pattern USERNAME_PATTERN = 
        Pattern.compile("^[a-zA-Z0-9._-]{3,20}$");
    
    private static final Pattern PASSWORD_PATTERN = 
        Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$");

    private static final SecureRandom random = new SecureRandom();

    // ═══════════════════════════════════════════════════════════════
    //  PASSWORD SECURITY
    // ═══════════════════════════════════════════════════════════════

    /**
     * Hash a password using BCrypt
     * @param password plaintext password
     * @return hashed password with salt
     */
    public static String hashPassword(String password) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        // Using BCrypt library: org.mindrot.jbcrypt.BCrypt
        return org.mindrot.jbcrypt.BCrypt.hashpw(password, org.mindrot.jbcrypt.BCrypt.gensalt(12));
    }

    /**
     * Verify a plaintext password against a BCrypt hash
     * @param password plaintext password to check
     * @param hash BCrypt hash from database
     * @return true if password matches
     */
    public static boolean verifyPassword(String password, String hash) {
        if (password == null || hash == null) {
            return false;
        }
        try {
            return org.mindrot.jbcrypt.BCrypt.checkpw(password, hash);
        } catch (Exception e) {
            logger.warning("Password verification failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Check if password meets complexity requirements
     * Requirements: 8+ chars, uppercase, lowercase, digit, special char
     * @param password password to validate
     * @return true if password is strong
     */
    public static boolean isStrongPassword(String password) {
        return password != null && PASSWORD_PATTERN.matcher(password).matches();
    }

    /**
     * Generate a random secure password
     * @return 16-character random password
     */
    public static String generateSecurePassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@$!%*?&";
        StringBuilder password = new StringBuilder(16);
        for (int i = 0; i < 16; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }
        return password.toString();
    }

    // ═══════════════════════════════════════════════════════════════
    //  INPUT VALIDATION
    // ═══════════════════════════════════════════════════════════════

    /**
     * Validate email format
     * @param email email to validate
     * @return true if valid email
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Validate Philippine phone number
     * @param phone phone number (format: 09XXXXXXXXX or +639XXXXXXXXX)
     * @return true if valid
     */
    public static boolean isValidPhoneNumber(String phone) {
        if (phone == null || phone.isEmpty()) {
            return false;
        }
        return PHONE_PATTERN.matcher(phone).matches();
    }

    /**
     * Normalize Philippine phone number to +63 format
     * @param phone phone number in any format
     * @return normalized phone number
     */
    public static String normalizePhoneNumber(String phone) {
        if (phone == null) {
            return null;
        }
        phone = phone.replaceAll("[^0-9+]", ""); // Remove non-digits
        
        if (phone.startsWith("0")) {
            return "+63" + phone.substring(1); // 09XX → +639XX
        } else if (phone.startsWith("63")) {
            return "+" + phone; // 63XX → +63XX
        } else if (!phone.startsWith("+")) {
            return "+" + phone;
        }
        return phone;
    }

    /**
     * Validate username format
     * @param username username to validate
     * @return true if valid
     */
    public static boolean isValidUsername(String username) {
        if (username == null || username.isEmpty()) {
            return false;
        }
        return USERNAME_PATTERN.matcher(username).matches();
    }

    /**
     * Validate name (letters, spaces, hyphens only)
     * @param name name to validate
     * @return true if valid
     */
    public static boolean isValidName(String name) {
        if (name == null || name.isEmpty() || name.length() > 100) {
            return false;
        }
        return name.matches("^[a-zA-Z\\s-']+$");
    }

    /**
     * Validate date format (YYYY-MM-DD)
     * @param date date string
     * @return true if valid
     */
    public static boolean isValidDate(String date) {
        if (date == null || date.isEmpty()) {
            return false;
        }
        return date.matches("^\\d{4}-\\d{2}-\\d{2}$");
    }

    /**
     * Validate numeric input (integers and decimals)
     * @param value value to validate
     * @param min minimum value (inclusive)
     * @param max maximum value (inclusive)
     * @return true if valid
     */
    public static boolean isValidNumber(String value, double min, double max) {
        if (value == null || value.isEmpty()) {
            return false;
        }
        try {
            double num = Double.parseDouble(value);
            return num >= min && num <= max;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  INPUT SANITIZATION
    // ═══════════════════════════════════════════════════════════════

    /**
     * Sanitize text input - remove potentially dangerous characters
     * Prevents XSS attacks in audit logs and comments
     * @param input user input
     * @return sanitized string
     */
    public static String sanitizeInput(String input) {
        if (input == null) {
            return "";
        }
        
        // Remove HTML tags and JavaScript
        input = input.replaceAll("<[^>]*>", ""); // Remove HTML tags
        input = input.replaceAll("(?i)javascript:", ""); // Remove javascript:
        input = input.replaceAll("(?i)on\\w+\\s*=", ""); // Remove event handlers
        
        // Trim whitespace
        input = input.trim();
        
        // Limit length to prevent abuse
        if (input.length() > 5000) {
            input = input.substring(0, 5000);
        }
        
        return input;
    }

    /**
     * Sanitize SQL string input (for non-parameterized queries)
     * Note: Always use PreparedStatements instead!
     * @param input user input
     * @return sanitized string
     */
    public static String sanitizeSQL(String input) {
        if (input == null) {
            return "";
        }
        // Escape single quotes
        return input.replace("'", "''");
    }

    /**
     * Sanitize HTML output (encode special characters)
     * @param input text to encode
     * @return HTML-encoded string
     */
    public static String escapeHtml(String input) {
        if (input == null) {
            return "";
        }
        return input.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }

    // ═══════════════════════════════════════════════════════════════
    //  VALIDATION RESULTS
    // ═══════════════════════════════════════════════════════════════

    /**
     * Comprehensive input validation result
     */
    public static class ValidationResult {
        public boolean isValid;
        public String errorMessage;

        public ValidationResult(boolean isValid, String errorMessage) {
            this.isValid = isValid;
            this.errorMessage = errorMessage;
        }

        public static ValidationResult valid() {
            return new ValidationResult(true, "");
        }

        public static ValidationResult invalid(String message) {
            return new ValidationResult(false, message);
        }
    }

    /**
     * Validate registration data
     * @param firstName first name
     * @param lastName last name
     * @param email email address
     * @param phone phone number
     * @param username username
     * @param password password
     * @return ValidationResult with details
     */
    public static ValidationResult validateRegistration(
            String firstName, String lastName, String email, 
            String phone, String username, String password) {
        
        if (!isValidName(firstName)) {
            return ValidationResult.invalid("First name must contain only letters, spaces, or hyphens");
        }
        if (!isValidName(lastName)) {
            return ValidationResult.invalid("Last name must contain only letters, spaces, or hyphens");
        }
        if (!isValidEmail(email)) {
            return ValidationResult.invalid("Email format is invalid");
        }
        if (!isValidPhoneNumber(phone)) {
            return ValidationResult.invalid("Phone number must be in format 09XXXXXXXXX");
        }
        if (!isValidUsername(username)) {
            return ValidationResult.invalid("Username must be 3-20 characters (letters, numbers, ._- only)");
        }
        if (!isStrongPassword(password)) {
            return ValidationResult.invalid(
                "Password must be 8+ characters with uppercase, lowercase, number, and special character");
        }
        
        return ValidationResult.valid();
    }

    /**
     * Validate booking data
     * @param checkInDate check-in date (YYYY-MM-DD)
     * @param checkOutDate check-out date (YYYY-MM-DD)
     * @param guestCount number of guests
     * @return ValidationResult with details
     */
    public static ValidationResult validateBooking(
            String checkInDate, String checkOutDate, int guestCount) {
        
        if (!isValidDate(checkInDate)) {
            return ValidationResult.invalid("Check-in date format is invalid (use YYYY-MM-DD)");
        }
        if (!isValidDate(checkOutDate)) {
            return ValidationResult.invalid("Check-out date format is invalid (use YYYY-MM-DD)");
        }
        if (checkOutDate.compareTo(checkInDate) <= 0) {
            return ValidationResult.invalid("Check-out date must be after check-in date");
        }
        if (guestCount < 1 || guestCount > 100) {
            return ValidationResult.invalid("Guest count must be between 1 and 100");
        }
        
        return ValidationResult.valid();
    }
}