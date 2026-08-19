package Hotel_Reservation;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

public class ServicesManager {

    // === SMS CONFIGURATION ===
    // "textbelt" key = 1 free SMS per day per IP address (for testing).
    // For production, buy a key at https://textbelt.com
    private static final String SMS_API_KEY = "textbelt";

    /**
     * 1. SMS API - Sends an SMS alert to the customer's phone via TextBelt
     *
     * BUG FIX 1: phoneNumber was being reassigned inside a lambda, which is
     *            illegal (effectively final). Fixed by copying to a local variable.
     *
     * BUG FIX 2: TextBelt requires POST data in the REQUEST BODY, not the URL.
     *            The old code built a query-string URL but sent it as a POST —
     *            the API never received the parameters. Fixed with DataOutputStream.
     */
    public static void sendSMSAlert(String phoneNumber, String messageText) {
        // Copy to a new local variable so we can modify it inside the lambda
        final String normalizedPhone;
        if (phoneNumber.startsWith("0")) {
            normalizedPhone = "+63" + phoneNumber.substring(1); // 09XX → +639XX
        } else {
            normalizedPhone = phoneNumber;
        }

        new Thread(() -> {
            try {
                URL url = new URL("https://textbelt.com/text");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true); // Enable writing to request body
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                // Build the POST body (NOT the URL)
                String postData = "phone=" + URLEncoder.encode(normalizedPhone, "UTF-8")
                                + "&message=" + URLEncoder.encode(messageText, "UTF-8")
                                + "&key=" + URLEncoder.encode(SMS_API_KEY, "UTF-8");

                // Write POST body to the connection
                try (DataOutputStream out = new DataOutputStream(conn.getOutputStream())) {
                    out.writeBytes(postData);
                    out.flush();
                }

                // Read the API response
                StringBuilder response = new StringBuilder();
                try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                    String line;
                    while ((line = in.readLine()) != null) {
                        response.append(line);
                    }
                }

                System.out.println("✅ SMS Sent Status: " + response.toString());

            } catch (Exception e) {
                System.err.println("❌ SMS Error: " + e.getMessage());
            }
        }).start();
    }

    /**
     * 2. QR Code Generator - Generates a QR Code image for GCash/Maya payments
     *
     * Uses the free https://api.qrserver.com service (no API key needed).
     * Added charset=UTF-8 to avoid encoding issues with special characters.
     */
    public static ImageIcon generateQRPhCode(double amount, String referenceNo) {
        try {
            // EMVCo / QR Ph payload format
            String qrPhPayload = "00020101021226360016COM.QRPH.MERCHANT0112" + referenceNo
                               + "520459995303608540" + String.format("%.2f", amount)
                               + "5802PH5916SYNC_SUITES_HOTL6006TAGUIG6304A1B2";

            // Free QR Code Generation API — no key required
            String apiUrl = "https://api.qrserver.com/v1/create-qr-code/"
                          + "?size=300x300"
                          + "&charset-source=UTF-8"
                          + "&data=" + URLEncoder.encode(qrPhPayload, "UTF-8");

            URL url = new URL(apiUrl);
            BufferedImage image = ImageIO.read(url);

            if (image == null) {
                System.err.println("❌ QR API returned null image.");
                return null;
            }

            return new ImageIcon(image);

        } catch (Exception e) {
            System.err.println("❌ QR API Error: " + e.getMessage());
            return null;
        }
    }
}