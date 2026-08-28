package Hotel_Reservation;

import Hotel_Reservation.utils.SecurityUtils;

/**
 * ONE-TIME USE UTILITY - Run this once to generate a real BCrypt hash
 * for your admin password, then paste the printed hash into schema.sql.
 *
 * How to run (Eclipse): right-click this file -> Run As -> Java Application
 * How to run (VS Code): open this file, click "Run" above main()
 *
 * You can delete this file afterward - it's not used by the rest of the app.
 */
public class HashGenerator {
    public static void main(String[] args) {
        String plainPassword = "Admin123!"; // <-- change this to whatever password you want

        String hash = SecurityUtils.hashPassword(plainPassword);

        System.out.println("Plain password: " + plainPassword);
        System.out.println("BCrypt hash:    " + hash);
        System.out.println();
        System.out.println("Copy the hash above and paste it into schema.sql, replacing");
        System.out.println("REPLACE_WITH_REAL_HASH in the INSERT INTO users statement.");
    }
}
