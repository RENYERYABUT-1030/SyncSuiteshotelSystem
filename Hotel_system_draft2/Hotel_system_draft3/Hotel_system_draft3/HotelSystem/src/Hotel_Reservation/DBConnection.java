package Hotel_Reservation;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Database Connection Manager
 * Matches existing hotel_system database
 */
public class DBConnection {

    // Change these to match your MySQL setup
    private static final String URL      = "jdbc:mysql://localhost:3306/hotel_system?useSSL=false&serverTimezone=UTC"; 
    private static final String USERNAME = "root";   
    private static final String PASSWORD = "";       // Add your password here if set

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, USERNAME, PASSWORD);
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not found. Add mysql-connector-java to your project!", e);
        }
    }

    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("Database connection test failed: " + e.getMessage());
            return false;
        }
    }
}