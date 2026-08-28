package Hotel_Reservation;



import java.util.UUID;

/**
 * Session Manager - Centralized session management
 * Replaces static LoginFrame references
 */
public class SessionManager {
    private static int currentUserId = -1;
    private static String currentUserType = "";
    private static String currentUserName = "";
    private static String sessionToken = "";
    private static int currentAdminId = -1;

    public static int getCurrentUserId() { 
        return currentUserId; 
    }
    
    public static String getCurrentUserType() { 
        return currentUserType; 
    }
    
    public static String getCurrentUserName() { 
        return currentUserName != null && !currentUserName.isEmpty() 
            ? currentUserName 
            : "System"; 
    }
    
    public static String getSessionToken() { 
        return sessionToken; 
    }
    
    public static int getCurrentAdminId() {
        return currentAdminId;
    }
    
    public static void setCurrentUser(int userId, String userType, String userName) {
        currentUserId = userId;
        currentUserType = userType;
        currentUserName = userName;
        sessionToken = UUID.randomUUID().toString();
    }
    
    public static void setCurrentAdmin(int adminId, String userName) {
        currentAdminId = adminId;
        currentUserId = adminId;
        currentUserType = "admin";
        currentUserName = userName;
        sessionToken = UUID.randomUUID().toString();
    }
    
    public static void logout() {
        currentUserId = -1;
        currentUserType = "";
        currentUserName = "";
        sessionToken = "";
        currentAdminId = -1;
    }
    
    public static boolean isLoggedIn() {
        return currentUserId != -1;
    }
    
    public static boolean isAdmin() {
        return "admin".equals(currentUserType);
    }
    
    public static boolean isCustomer() {
        return "customer".equals(currentUserType);
    }
}