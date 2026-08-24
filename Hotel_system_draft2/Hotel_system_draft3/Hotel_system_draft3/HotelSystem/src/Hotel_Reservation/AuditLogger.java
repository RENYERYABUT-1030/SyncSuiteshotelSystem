package Hotel_Reservation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * AuditLogger
 * ─────────────
 * Small static helper for writing entries into the audit_log table.
 * Call AuditLogger.log(...) from anywhere (login, settings changes,
 * bookings, maintenance toggles, etc.) to record admin/customer activity.
 *
 * Logging failures are swallowed (printed to stderr only) so that a
 * logging problem never blocks the actual feature that triggered it.
 */
public class AuditLogger {

    /**
     * @param userType "Admin" or "Customer"
     * @param userName display name of the user who performed the action
     * @param action   short action label, e.g. "Login", "Settings Update", "Room Maintenance Toggle"
     * @param details  free-text description of what happened
     */
    public static void log(String userType, String userName, String action, String details) {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "INSERT INTO audit_log (user_type, user_name, action, details) VALUES (?,?,?,?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, userType);
            ps.setString(2, userName);
            ps.setString(3, action);
            ps.setString(4, details);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("⚠️ AuditLogger failed to record entry: " + e.getMessage());
        }
    }
}