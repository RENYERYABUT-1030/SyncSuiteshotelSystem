package Hotel_Reservation;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SessionManagerTest {
    @BeforeEach
    void resetSession() {
        SessionManager.logout();
    }

    @Test
    void startsLoggedOut() {
        assertFalse(SessionManager.isLoggedIn());
        assertEquals(-1, SessionManager.getCurrentUserId());
        assertEquals("System", SessionManager.getCurrentUserName());
        assertFalse(SessionManager.isAdmin());
        assertFalse(SessionManager.isCustomer());
    }

    @Test
    void setsCustomerSessionAndGeneratesToken() {
        SessionManager.setCurrentUser(42, "customer", "Anne");

        assertTrue(SessionManager.isLoggedIn());
        assertTrue(SessionManager.isCustomer());
        assertFalse(SessionManager.isAdmin());
        assertEquals(42, SessionManager.getCurrentUserId());
        assertEquals("customer", SessionManager.getCurrentUserType());
        assertEquals("Anne", SessionManager.getCurrentUserName());
        assertFalse(SessionManager.getSessionToken().isEmpty());
    }

    @Test
    void setsAdminSessionAndUsesAdminIdAsUserId() {
        SessionManager.setCurrentAdmin(7, "Admin User");

        assertTrue(SessionManager.isLoggedIn());
        assertTrue(SessionManager.isAdmin());
        assertFalse(SessionManager.isCustomer());
        assertEquals(7, SessionManager.getCurrentAdminId());
        assertEquals(7, SessionManager.getCurrentUserId());
        assertEquals("Admin User", SessionManager.getCurrentUserName());
    }

    @Test
    void logoutClearsSession() {
        SessionManager.setCurrentUser(42, "customer", "Anne");
        SessionManager.logout();

        assertFalse(SessionManager.isLoggedIn());
        assertEquals(-1, SessionManager.getCurrentUserId());
        assertEquals(-1, SessionManager.getCurrentAdminId());
        assertEquals("", SessionManager.getCurrentUserType());
        assertEquals("System", SessionManager.getCurrentUserName());
        assertEquals("", SessionManager.getSessionToken());
    }
}
