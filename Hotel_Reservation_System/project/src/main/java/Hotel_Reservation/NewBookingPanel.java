package Hotel_Reservation;

import Hotel_Reservation.core.EnhancedDBConnection;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import java.sql.*;
import java.util.Date;
import java.util.Calendar;
import java.text.SimpleDateFormat;

/**
 * Enhanced New Booking Panel
 * Bug fixes: SQL injection prevention, date validation, transaction safety
 * Features: Customer search, senior discount, extra guest fees, QR payment option
 */
public class NewBookingPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private JTextField FNameField;
    private JTextField LNameField;
    private JTextField PNumField;
    private JTextField emailField;
    private JComboBox<String> roomTypeCBX;
    private JComboBox<String> availableRoomCBX;
    private JSpinner checkInSpinner;
    private JSpinner checkOutSpinner;
    private JTextField txtTotalAmount;
    private JComboBox<String> PMethodCBX;
    private JLabel roomImageLabel;
    private JLabel roomNameLabel;
    private JLabel lblExtraCharge;
    private int currentCustomerId = -1;

    // Guest spinners
    private JSpinner adultSpinner;
    private JSpinner seniorPwdSpinner;
    private JSpinner kidSpinner;

    // Room data
    private double[] basePrices = {2500.0, 3500.0, 5000.0, 8000.0};
    private int[] includedGuests = {2, 4, 6, 10};
    private double[] extraGuestFee = {500.0, 700.0, 1000.0, 1500.0};

    public NewBookingPanel() {
        setForeground(new Color(31, 26, 85));
        setSize(1924, 1083);
        setLayout(null);

        JLabel lblNewBooking = new JLabel("CREATE NEW BOOKING");
        lblNewBooking.setForeground(new Color(0, 0, 68));
        lblNewBooking.setBounds(44, 20, 400, 28);
        lblNewBooking.setFont(new Font("SansSerif", Font.BOLD, 20));
        add(lblNewBooking);

        // Search existing customer button
        JButton btnSearchCustomer = new JButton("🔍 Search Customer");
        btnSearchCustomer.setBounds(239, 56, 241, 28);
        btnSearchCustomer.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnSearchCustomer.setBackground(new Color(0, 128, 255));
        btnSearchCustomer.setForeground(Color.WHITE);
        btnSearchCustomer.setFocusPainted(false);
        btnSearchCustomer.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSearchCustomer.addActionListener(e -> showCustomerSearchDialog());
        add(btnSearchCustomer);

        // Customer Information Section
        JLabel lblCustomerInfo = new JLabel("━━ Customer Information");
        lblCustomerInfo.setForeground(new Color(0, 0, 68));
        lblCustomerInfo.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblCustomerInfo.setBounds(50, 58, 300, 20);
        add(lblCustomerInfo);

        // First Name
        JLabel lblFirst = new JLabel("First Name:");
        lblFirst.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblFirst.setBounds(80, 90, 100, 20);
        add(lblFirst);

        FNameField = new JTextField();
        FNameField.setBounds(190, 88, 250, 24);
        FNameField.setFont(new Font("SansSerif", Font.PLAIN, 13));
        FNameField.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (c >= '0' && c <= '9') e.consume();
                if (Character.isLetter(c)) e.setKeyChar(Character.toUpperCase(c));
            }
        });
        add(FNameField);

        // Last Name
        JLabel lblLastName = new JLabel("Last Name:");
        lblLastName.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblLastName.setBounds(80, 122, 100, 20);
        add(lblLastName);

        LNameField = new JTextField();
        LNameField.setBounds(190, 120, 250, 24);
        LNameField.setFont(new Font("SansSerif", Font.PLAIN, 13));
        LNameField.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (c >= '0' && c <= '9') e.consume();
                if (Character.isLetter(c)) e.setKeyChar(Character.toUpperCase(c));
            }
        });
        add(LNameField);

        // Phone Number
        JLabel lblPhoneNumber = new JLabel("Phone Number:");
        lblPhoneNumber.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblPhoneNumber.setBounds(80, 154, 110, 20);
        add(lblPhoneNumber);

        PNumField = new JTextField();
        PNumField.setBounds(190, 152, 250, 24);
        PNumField.setFont(new Font("SansSerif", Font.PLAIN, 13));
        PNumField.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (!Character.isDigit(c)) e.consume();
                if (PNumField.getText().length() >= 11) e.consume();
            }
        });
        add(PNumField);

        // Email
        JLabel lblEmail = new JLabel("Email:");
        lblEmail.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblEmail.setBounds(80, 186, 100, 20);
        add(lblEmail);

        emailField = new JTextField();
        emailField.setBounds(190, 184, 250, 24);
        emailField.setFont(new Font("SansSerif", Font.PLAIN, 13));
        add(emailField);

        // Room Selection Section
        JLabel lblRoomSelection = new JLabel("━━ Room Selection");
        lblRoomSelection.setForeground(new Color(0, 0, 68));
        lblRoomSelection.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblRoomSelection.setBounds(50, 220, 300, 20);
        add(lblRoomSelection);

        JLabel lblRoomType = new JLabel("Room Type:");
        lblRoomType.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblRoomType.setBounds(80, 250, 100, 20);
        add(lblRoomType);

        String[] roomTypes = {
            "Standard Room - ₱2,500/day",
            "Junior Suite - ₱3,500/day",
            "Executive Suite - ₱5,000/day",
            "Presidential Suite - ₱8,000/day"
        };
        roomTypeCBX = new JComboBox<>(roomTypes);
        roomTypeCBX.setBounds(190, 248, 250, 26);
        roomTypeCBX.setFont(new Font("SansSerif", Font.PLAIN, 13));
        roomTypeCBX.addActionListener(e -> {
            loadAvailableRooms();
            calculateTotalAmount();
            updateRoomImage();
        });
        add(roomTypeCBX);

        JLabel lblAvailableRoom = new JLabel("Available Room:");
        lblAvailableRoom.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblAvailableRoom.setBounds(80, 282, 110, 20);
        add(lblAvailableRoom);

        availableRoomCBX = new JComboBox<>();
        availableRoomCBX.setBounds(190, 280, 250, 26);
        availableRoomCBX.setFont(new Font("SansSerif", Font.PLAIN, 13));
        add(availableRoomCBX);

        // Guests Section
        JLabel lblGuestSection = new JLabel("━━ Guests");
        lblGuestSection.setForeground(new Color(0, 0, 68));
        lblGuestSection.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblGuestSection.setBounds(50, 316, 300, 20);
        add(lblGuestSection);

        // Adults
        JLabel lblAdults = new JLabel("Adults:");
        lblAdults.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblAdults.setBounds(80, 345, 100, 20);
        add(lblAdults);

        adultSpinner = new JSpinner(new SpinnerNumberModel(1, 0, 20, 1));
        adultSpinner.setBounds(190, 343, 70, 24);
        adultSpinner.addChangeListener(e -> calculateTotalAmount());
        add(adultSpinner);

        JLabel lblAdultNote = new JLabel("(Full rate)");
        lblAdultNote.setFont(new Font("SansSerif", Font.ITALIC, 11));
        lblAdultNote.setForeground(Color.GRAY);
        lblAdultNote.setBounds(270, 345, 100, 20);
        add(lblAdultNote);

        // Senior/PWD
        JLabel lblSeniorPwd = new JLabel("Senior / PWD:");
        lblSeniorPwd.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblSeniorPwd.setBounds(80, 375, 100, 20);
        add(lblSeniorPwd);

        seniorPwdSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 20, 1));
        seniorPwdSpinner.setBounds(190, 373, 70, 24);
        seniorPwdSpinner.addChangeListener(e -> calculateTotalAmount());
        add(seniorPwdSpinner);

        JLabel lblSeniorNote = new JLabel("(20% discount)");
        lblSeniorNote.setFont(new Font("SansSerif", Font.ITALIC, 11));
        lblSeniorNote.setForeground(new Color(0, 128, 0));
        lblSeniorNote.setBounds(270, 375, 100, 20);
        add(lblSeniorNote);

        // Kids
        JLabel lblKids = new JLabel("Kids:");
        lblKids.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblKids.setBounds(80, 405, 100, 20);
        add(lblKids);

        kidSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 20, 1));
        kidSpinner.setBounds(190, 403, 70, 24);
        kidSpinner.addChangeListener(e -> calculateTotalAmount());
        add(kidSpinner);

        JLabel lblKidNote = new JLabel("(Free)");
        lblKidNote.setFont(new Font("SansSerif", Font.ITALIC, 11));
        lblKidNote.setForeground(new Color(0, 128, 255));
        lblKidNote.setBounds(270, 405, 100, 20);
        add(lblKidNote);

        // Extra charge label
        lblExtraCharge = new JLabel("");
        lblExtraCharge.setFont(new Font("SansSerif", Font.ITALIC, 12));
        lblExtraCharge.setForeground(new Color(224, 27, 36));
        lblExtraCharge.setBounds(80, 435, 400, 20);
        add(lblExtraCharge);

        // Dates Section
        JLabel lblDates = new JLabel("━━ Dates");
        lblDates.setForeground(new Color(0, 0, 68));
        lblDates.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblDates.setBounds(50, 460, 300, 20);
        add(lblDates);

        JLabel lblCheckIn = new JLabel("Check-in Date:");
        lblCheckIn.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblCheckIn.setBounds(80, 490, 110, 20);
        add(lblCheckIn);

        JLabel lblCheckOut = new JLabel("Check-out Date:");
        lblCheckOut.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblCheckOut.setBounds(80, 520, 110, 20);
        add(lblCheckOut);

        // Date spinners with proper minimum date (today)
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        Date minDate = today.getTime();

        SpinnerDateModel checkInModel = new SpinnerDateModel(minDate, minDate, null, Calendar.DAY_OF_MONTH);
        SpinnerDateModel checkOutModel = new SpinnerDateModel(minDate, minDate, null, Calendar.DAY_OF_MONTH);

        checkInSpinner = new JSpinner(checkInModel);
        checkInSpinner.setBounds(190, 488, 250, 24);
        checkInSpinner.setEditor(new JSpinner.DateEditor(checkInSpinner, "yyyy-MM-dd"));
        checkInSpinner.setFont(new Font("SansSerif", Font.PLAIN, 13));
        add(checkInSpinner);

        checkOutSpinner = new JSpinner(checkOutModel);
        checkOutSpinner.setBounds(190, 518, 250, 24);
        checkOutSpinner.setEditor(new JSpinner.DateEditor(checkOutSpinner, "yyyy-MM-dd"));
        checkOutSpinner.setFont(new Font("SansSerif", Font.PLAIN, 13));
        add(checkOutSpinner);

        checkInSpinner.addChangeListener(e -> {
            calculateTotalAmount();
            loadAvailableRooms();
        });
        checkOutSpinner.addChangeListener(e -> {
            calculateTotalAmount();
            loadAvailableRooms();
        });

        // Payment Section
        JLabel lblPayment = new JLabel("━━ Payment");
        lblPayment.setForeground(new Color(0, 0, 68));
        lblPayment.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblPayment.setBounds(50, 555, 300, 20);
        add(lblPayment);

        JLabel lblPaymentMethod = new JLabel("Payment Method:");
        lblPaymentMethod.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblPaymentMethod.setBounds(80, 585, 110, 20);
        add(lblPaymentMethod);

        String[] paymentMethods = {"Cash", "GCash/QR Scan", "QR PH"};
        PMethodCBX = new JComboBox<>(paymentMethods);
        PMethodCBX.setBounds(190, 583, 250, 26);
        PMethodCBX.setFont(new Font("SansSerif", Font.PLAIN, 13));
        add(PMethodCBX);

        JLabel lblTotal = new JLabel("Total Amount:");
        lblTotal.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblTotal.setBounds(80, 620, 110, 25);
        add(lblTotal);

        txtTotalAmount = new JTextField("₱0.00");
        txtTotalAmount.setBounds(190, 618, 250, 28);
        txtTotalAmount.setEditable(false);
        txtTotalAmount.setFont(new Font("SansSerif", Font.BOLD, 16));
        txtTotalAmount.setBackground(new Color(255, 255, 220));
        txtTotalAmount.setHorizontalAlignment(JTextField.RIGHT);
        add(txtTotalAmount);

        // Room Image Display
        roomNameLabel = new JLabel("Standard Room");
        roomNameLabel.setBounds(500, 20, 400, 25);
        roomNameLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        roomNameLabel.setForeground(new Color(0, 0, 68));
        add(roomNameLabel);

        roomImageLabel = new JLabel();
        roomImageLabel.setBounds(682, 50, 450, 430);
        roomImageLabel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        roomImageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        roomImageLabel.setBackground(new Color(240, 240, 240));
        roomImageLabel.setOpaque(true);
        add(roomImageLabel);

        // Buttons
        JButton btnClearForm = new JButton("🗑 Clear Form");
        btnClearForm.setBounds(80, 665, 160, 32);
        btnClearForm.setForeground(Color.WHITE);
        btnClearForm.setBackground(new Color(224, 27, 36));
        btnClearForm.setFont(new Font("SansSerif", Font.BOLD, 13));
        btnClearForm.setFocusPainted(false);
        btnClearForm.addActionListener(e -> clearForm());
        add(btnClearForm);

        JButton btnSaveBooking = new JButton("💾 Save Booking");
        btnSaveBooking.setBounds(260, 665, 180, 32);
        btnSaveBooking.setForeground(Color.WHITE);
        btnSaveBooking.setBackground(new Color(46, 194, 126));
        btnSaveBooking.setFont(new Font("SansSerif", Font.BOLD, 13));
        btnSaveBooking.setFocusPainted(false);
        btnSaveBooking.addActionListener(e -> saveBooking());
        add(btnSaveBooking);

        // Initialize
        loadAvailableRooms();
        calculateTotalAmount();
    }

    private void updateRoomImage() {
        String selectedRoom = (String) roomTypeCBX.getSelectedItem();
        if (selectedRoom == null) return;

        if (selectedRoom.contains("Standard Room")) {
            roomNameLabel.setText("Standard Room");
            roomImageLabel.setIcon(createScaledIcon("Standard room.jpg", 400, 300));
        } else if (selectedRoom.contains("Junior")) {
            roomNameLabel.setText("Junior Suite");
            roomImageLabel.setIcon(createScaledIcon("Junior room.jpg", 400, 300));
        } else if (selectedRoom.contains("Executive")) {
            roomNameLabel.setText("Executive Suite");
            roomImageLabel.setIcon(createScaledIcon("Executive room.jpg", 400, 300));
        } else if (selectedRoom.contains("Presidential")) {
            roomNameLabel.setText("Presidential Suite");
            roomImageLabel.setIcon(createScaledIcon("Presidential room.jpg", 400, 300));
        }
    }

    private ImageIcon createScaledIcon(String path, int width, int height) {
        try {
            ImageIcon icon = new ImageIcon(path);
            if (icon.getIconWidth() == -1) {
                // Return placeholder if image not found
                return null;
            }
            Image img = icon.getImage();
            Image newImg = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            return new ImageIcon(newImg);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Load available rooms based on selected type and dates
     * BUG FIX: Properly checks for overlapping bookings
     */
    private void loadAvailableRooms() {
        availableRoomCBX.removeAllItems();

        int typeIndex = roomTypeCBX.getSelectedIndex() + 1;

        // Get selected dates
        Date checkIn = (Date) checkInSpinner.getValue();
        Date checkOut = (Date) checkOutSpinner.getValue();
        java.sql.Date sqlCheckIn = new java.sql.Date(checkIn.getTime());
        java.sql.Date sqlCheckOut = new java.sql.Date(checkOut.getTime());

        try (Connection conn = EnhancedDBConnection.getConnection()) {
            // BUG FIX: Removed `is_available = 1` filter — that flag gets set to 0 on booking
            // and is never reliably reset. Availability is correctly determined by checking
            // for overlapping active bookings instead (the NOT IN subquery below).
            String sql = "SELECT r.room_id, r.room_number FROM rooms r " +
                        "WHERE r.room_type_id = ? " +
                        "AND r.is_maintenance = 0 " +
                        "AND r.room_id NOT IN (" +
                        "    SELECT room_id FROM bookings " +
                        "    WHERE status IN ('Reserved', 'Checked In') " +
                        "    AND check_in_date < ? AND check_out_date > ?" +
                        ")";

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, typeIndex);
            ps.setDate(2, sqlCheckOut);   // existing must start before our checkout
            ps.setDate(3, sqlCheckIn);    // existing must end after our checkin

            ResultSet rs = ps.executeQuery();

            boolean found = false;
            while (rs.next()) {
                availableRoomCBX.addItem(rs.getInt("room_id") + " - Room " + rs.getString("room_number"));
                found = true;
            }

            if (!found) {
                availableRoomCBX.addItem("No rooms available for selected dates");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading rooms: " + e.getMessage());
        }
    }

    /**
     * Calculate total amount with proper date handling
     * BUG FIX: Proper calendar date comparison, no time component issues
     */
    private void calculateTotalAmount() {
        try {
            Date checkInDate = (Date) checkInSpinner.getValue();
            Date checkOutDate = (Date) checkOutSpinner.getValue();

            Calendar calIn = Calendar.getInstance();
            calIn.setTime(checkInDate);
            calIn.set(Calendar.HOUR_OF_DAY, 0);
            calIn.set(Calendar.MINUTE, 0);
            calIn.set(Calendar.SECOND, 0);
            calIn.set(Calendar.MILLISECOND, 0);

            Calendar calOut = Calendar.getInstance();
            calOut.setTime(checkOutDate);
            calOut.set(Calendar.HOUR_OF_DAY, 0);
            calOut.set(Calendar.MINUTE, 0);
            calOut.set(Calendar.SECOND, 0);
            calOut.set(Calendar.MILLISECOND, 0);

            long days = (calOut.getTimeInMillis() - calIn.getTimeInMillis()) / (1000 * 60 * 60 * 24);

            if (days <= 0) {
                txtTotalAmount.setText("Invalid Dates");
                lblExtraCharge.setText("Check-out must be after check-in");
                return;
            }

            // BUG FIX: Check max advance booking (90 days)
            Calendar maxDate = Calendar.getInstance();
            maxDate.add(Calendar.DAY_OF_MONTH, 90);
            if (calIn.after(maxDate)) {
                txtTotalAmount.setText("Max 90 days advance");
                lblExtraCharge.setText("Booking too far in advance");
                return;
            }

            int typeIndex = roomTypeCBX.getSelectedIndex();
            if (typeIndex < 0) typeIndex = 0;

            int adults = (int) adultSpinner.getValue();
            int seniors = (int) seniorPwdSpinner.getValue();
            int kids = (int) kidSpinner.getValue();
            int totalGuests = adults + seniors + kids;

            if (totalGuests == 0) {
                txtTotalAmount.setText("₱0.00");
                lblExtraCharge.setText("Add at least 1 guest");
                return;
            }

            // Base room cost
            double baseRoomCost = days * basePrices[typeIndex];

            // Extra guest surcharge
            int allowedBase = includedGuests[typeIndex];
            int extraHeads = Math.max(0, totalGuests - allowedBase);
            double extraChargeTotal = extraHeads * extraGuestFee[typeIndex] * days;

            // Senior discount
            int payingGuests = adults + seniors;
            double seniorDiscount = 0.0;
            if (payingGuests > 0 && seniors > 0) {
                double totalBeforeDiscount = baseRoomCost + extraChargeTotal;
                double seniorFraction = (double) seniors / payingGuests;
                seniorDiscount = totalBeforeDiscount * seniorFraction * 0.20;
            }

            double total = baseRoomCost + extraChargeTotal - seniorDiscount;

            // Display breakdown
            StringBuilder note = new StringBuilder();
            note.append(days).append(" night(s) @ ₱").append(String.format("%,.0f", basePrices[typeIndex])).append("/night");
            if (extraHeads > 0) {
                note.append(" | +₱").append(String.format("%,.0f", extraChargeTotal)).append(" extra guest fee");
            }
            if (seniorDiscount > 0) {
                note.append(" | -₱").append(String.format("%,.0f", seniorDiscount)).append(" senior discount");
            }
            lblExtraCharge.setText(note.toString());
            lblExtraCharge.setForeground(new Color(0, 100, 0));

            txtTotalAmount.setText("₱" + String.format("%,.2f", total));

        } catch (Exception e) {
            txtTotalAmount.setText("Error");
            lblExtraCharge.setText("");
        }
    }

    /**
     * Clear all form fields
     */
    private void clearForm() {
        FNameField.setText("");
        LNameField.setText("");
        PNumField.setText("");
        emailField.setText("");
        roomTypeCBX.setSelectedIndex(0);
        PMethodCBX.setSelectedIndex(0);

        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        checkInSpinner.setValue(today.getTime());
        checkOutSpinner.setValue(today.getTime());

        txtTotalAmount.setText("₱0.00");
        lblExtraCharge.setText("");
        adultSpinner.setValue(1);
        seniorPwdSpinner.setValue(0);
        kidSpinner.setValue(0);
        currentCustomerId = -1;
        FNameField.setEditable(true);
        LNameField.setEditable(true);
        loadAvailableRooms();
    }

    /**
     * Save booking with full validation and transaction safety
     * BUG FIX: Proper transaction handling, input validation, SQL injection prevention
     */
    private void saveBooking() {
        System.out.println("=== SAVE BOOKING STARTED ===");

        // STEP 1: Validate empty fields
        String firstName = FNameField.getText().trim();
        String lastName = LNameField.getText().trim();
        String phone = PNumField.getText().trim();
        String email = emailField.getText().trim();

        System.out.println("STEP 1 - Fields: firstName='" + firstName + "' lastName='" + lastName + "' phone='" + phone + "' email='" + email + "'");

        if (firstName.isEmpty() || lastName.isEmpty() || phone.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all customer information.", 
                "Validation Error", JOptionPane.WARNING_MESSAGE);
            System.out.println("STEP 1 FAILED - Empty field detected");
            return;
        }
        System.out.println("STEP 1 PASSED");

        // STEP 2: Validate phone
        System.out.println("STEP 2 - Phone validation: '" + phone + "' matches=" + phone.matches("\\d{10,11}"));
        if (!phone.matches("\\d{10,11}")) {
            JOptionPane.showMessageDialog(this, "Phone must be 10-11 digits.", 
                "Validation Error", JOptionPane.WARNING_MESSAGE);
            System.out.println("STEP 2 FAILED");
            return;
        }
        System.out.println("STEP 2 PASSED");

        // STEP 3: Validate email
        System.out.println("STEP 3 - Email validation: '" + email + "'");
        if (!email.contains("@") || !email.contains(".") || email.indexOf("@") > email.lastIndexOf(".")) {
            JOptionPane.showMessageDialog(this, "Please enter a valid email address.", 
                "Validation Error", JOptionPane.WARNING_MESSAGE);
            System.out.println("STEP 3 FAILED");
            return;
        }
        System.out.println("STEP 3 PASSED");

        // STEP 4: Validate guests
        int adults = (int) adultSpinner.getValue();
        int seniors = (int) seniorPwdSpinner.getValue();
        int kids = (int) kidSpinner.getValue();
        int totalGuests = adults + seniors + kids;

        System.out.println("STEP 4 - Guests: adults=" + adults + " seniors=" + seniors + " kids=" + kids + " total=" + totalGuests);
        if (totalGuests == 0) {
            JOptionPane.showMessageDialog(this, "Please add at least 1 guest.", 
                "Validation Error", JOptionPane.WARNING_MESSAGE);
            System.out.println("STEP 4 FAILED");
            return;
        }
        System.out.println("STEP 4 PASSED");

        // STEP 5: Validate room selection
        String roomEntry = (String) availableRoomCBX.getSelectedItem();
        System.out.println("STEP 5 - Room entry: '" + roomEntry + "'");
        if (roomEntry == null || roomEntry.startsWith("No rooms")) {
            JOptionPane.showMessageDialog(this, "No available room selected.", 
                "Validation Error", JOptionPane.WARNING_MESSAGE);
            System.out.println("STEP 5 FAILED - No room");
            return;
        }

        int roomId;
        try {
            roomId = Integer.parseInt(roomEntry.split(" - ")[0].trim());
            System.out.println("STEP 5 - Parsed roomId=" + roomId);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Invalid room selection: '" + roomEntry + "'\nCould not parse Room ID.", 
                "Error", JOptionPane.ERROR_MESSAGE);
            System.out.println("STEP 5 FAILED - Parse error on: '" + roomEntry + "'");
            return;
        }
        System.out.println("STEP 5 PASSED");

        // STEP 6: Validate and process dates
        Date checkIn = (Date) checkInSpinner.getValue();
        Date checkOut = (Date) checkOutSpinner.getValue();

        Calendar calIn = Calendar.getInstance();
        calIn.setTime(checkIn);
        calIn.set(Calendar.HOUR_OF_DAY, 0);
        calIn.set(Calendar.MINUTE, 0);
        calIn.set(Calendar.SECOND, 0);
        calIn.set(Calendar.MILLISECOND, 0);

        Calendar calOut = Calendar.getInstance();
        calOut.setTime(checkOut);
        calOut.set(Calendar.HOUR_OF_DAY, 0);
        calOut.set(Calendar.MINUTE, 0);
        calOut.set(Calendar.SECOND, 0);
        calOut.set(Calendar.MILLISECOND, 0);

        long days = (calOut.getTimeInMillis() - calIn.getTimeInMillis()) / (1000 * 60 * 60 * 24);
        System.out.println("STEP 6 - Dates: checkIn=" + checkIn + " checkOut=" + checkOut + " days=" + days);

        if (days <= 0) {
            JOptionPane.showMessageDialog(this, "Check-out must be after check-in.", 
                "Validation Error", JOptionPane.WARNING_MESSAGE);
            System.out.println("STEP 6 FAILED - days <= 0");
            return;
        }

        // Check max stay (30 days)
        if (days > 30) {
            JOptionPane.showMessageDialog(this, "Maximum stay is 30 days.", 
                "Validation Error", JOptionPane.WARNING_MESSAGE);
            System.out.println("STEP 6 FAILED - days > 30");
            return;
        }
        System.out.println("STEP 6 PASSED");

        java.sql.Date sqlCheckIn = new java.sql.Date(calIn.getTimeInMillis());
        java.sql.Date sqlCheckOut = new java.sql.Date(calOut.getTimeInMillis());
        String payment = (String) PMethodCBX.getSelectedItem();

        // STEP 7: Calculate total
        int typeIndex = roomTypeCBX.getSelectedIndex();
        int allowedBase = includedGuests[typeIndex];
        int extraHeads = Math.max(0, totalGuests - allowedBase);

        double baseRoomCost = basePrices[typeIndex] * days;
        double extraChargeTotal = extraHeads * extraGuestFee[typeIndex] * days;

        int payingGuests = adults + seniors;
        double seniorDiscount = 0.0;
        if (payingGuests > 0 && seniors > 0) {
            double totalBeforeDiscount = baseRoomCost + extraChargeTotal;
            double seniorFraction = (double) seniors / payingGuests;
            seniorDiscount = totalBeforeDiscount * seniorFraction * 0.20;
        }

        double total = baseRoomCost + extraChargeTotal - seniorDiscount;

        // KUNIN ANG AMOUNT AT PAYMENT METHOD
        String selectedPayment = (String) PMethodCBX.getSelectedItem();
        // BUG FIX 1: Use the computed `total` directly — never parse the display field
        //            (it has a ₱ sign and commas that break Double.parseDouble).
        // BUG FIX 2: Match the actual combo-box item text "GCash/QR Scan", not "GCash"/"Maya".
        // BUG FIX 4: This block used to have no try/catch. If ServicesManager.generateQRPhCode()
        //            threw anything (timeout, no internet, bad API response, etc.) the exception
        //            propagated straight out of saveBooking() and STEP 8 below — the actual DB
        //            insert — never ran. That made a flaky/offline QR API look like "Save Booking"
        //            was silently doing nothing. Now any failure here is caught and logged, and we
        //            still fall through to saving the booking.
        if (selectedPayment.equals("GCash/QR Scan") || selectedPayment.equals("QR PH")) {
            try {
                // Generate Random Reference No
                String refNo = "SYNC" + (int)(Math.random() * 99999);

                // Call the QR API
                javax.swing.ImageIcon qrCode = ServicesManager.generateQRPhCode(total, refNo);

                if (qrCode != null) {
                    javax.swing.JOptionPane.showMessageDialog(this,
                        "Please scan this QR Ph Code using your GCash or Maya app to pay ₱"
                        + String.format("%,.2f", total) + ".\nReference: " + refNo,
                        "Scan to Pay",
                        javax.swing.JOptionPane.INFORMATION_MESSAGE,
                        qrCode);
                } else {
                    javax.swing.JOptionPane.showMessageDialog(this,
                        "Could not generate QR code. Please check your internet connection.",
                        "QR Error", javax.swing.JOptionPane.WARNING_MESSAGE);
                }
            } catch (Exception qrEx) {
                System.out.println("STEP 7b - QR generation threw an exception, continuing to save anyway: " + qrEx.getMessage());
                qrEx.printStackTrace();
                javax.swing.JOptionPane.showMessageDialog(this,
                    "Could not generate QR code (" + qrEx.getMessage() + "). The booking will still be saved.",
                    "QR Error", javax.swing.JOptionPane.WARNING_MESSAGE);
            }
        }

        // STEP 8: Save to database with transaction
        System.out.println("STEP 8 - Attempting DB connection...");
        Connection conn = null;
        try {
            conn = EnhancedDBConnection.getConnection();
            System.out.println("STEP 8 - DB connected. currentCustomerId=" + currentCustomerId);
            conn.setAutoCommit(false);

            // Check for existing active booking (only for returning customers)
            if (currentCustomerId != -1) {
                PreparedStatement checkPs = conn.prepareStatement(
                    "SELECT b.booking_id, r.room_number, b.status FROM bookings b " +
                    "JOIN rooms r ON b.room_id = r.room_id " +
                    "WHERE b.customer_id = ? AND b.status IN ('Reserved', 'Checked In')");
                checkPs.setInt(1, currentCustomerId);
                ResultSet checkRs = checkPs.executeQuery();

                if (checkRs.next()) {
                    String blockedRoom = checkRs.getString("room_number");
                    String blockedStatus = checkRs.getString("status");
                    conn.rollback();
                    conn.setAutoCommit(true);
                    conn.close();
                    conn = null;
                    JOptionPane.showMessageDialog(this,
                        "This customer already has an active booking at Room " + blockedRoom +
                        " (Status: " + blockedStatus + ").\nCannot create a new booking.",
                        "Booking Blocked", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }

            // Double-check room is not under maintenance and not already booked for these dates
            System.out.println("STEP 8 - Checking room availability for roomId=" + roomId);
            PreparedStatement roomCheckPs = conn.prepareStatement(
                "SELECT r.room_id FROM rooms r WHERE r.room_id = ? AND r.is_maintenance = 0 " +
                "AND r.room_id NOT IN (" +
                "    SELECT room_id FROM bookings " +
                "    WHERE status IN ('Reserved', 'Checked In') " +
                "    AND check_in_date < ? AND check_out_date > ?" +
                ")");
            roomCheckPs.setInt(1, roomId);
            roomCheckPs.setDate(2, sqlCheckOut);
            roomCheckPs.setDate(3, sqlCheckIn);
            ResultSet roomRs = roomCheckPs.executeQuery();
            boolean roomFound = roomRs.next();
            System.out.println("STEP 8 - roomAvailable=" + roomFound);
            if (!roomFound) {
                conn.rollback();
                conn.setAutoCommit(true);
                conn.close();
                conn = null;
                JOptionPane.showMessageDialog(this,
                    "Room is no longer available. Please select another room.",
                    "Room Unavailable", JOptionPane.WARNING_MESSAGE);
                loadAvailableRooms();
                return;
            }

            // Insert or update customer
            int finalCustomerId = currentCustomerId;
            if (finalCustomerId == -1) {
                PreparedStatement custPs = conn.prepareStatement(
                    "INSERT INTO customers (first_name, last_name, phone_number, email, created_at) " +
                    "VALUES (?,?,?,?, NOW())", Statement.RETURN_GENERATED_KEYS);
                custPs.setString(1, firstName);
                custPs.setString(2, lastName);
                custPs.setString(3, phone);
                custPs.setString(4, email);
                custPs.executeUpdate();

                ResultSet keys = custPs.getGeneratedKeys();
                if (keys.next()) finalCustomerId = keys.getInt(1);
            } else {
                // Update customer info
                PreparedStatement updatePs = conn.prepareStatement(
                    "UPDATE customers SET phone_number = ?, email = ? WHERE customer_id = ?");
                updatePs.setString(1, phone);
                updatePs.setString(2, email);
                updatePs.setInt(3, finalCustomerId);
                updatePs.executeUpdate();
            }

            System.out.println("STEP 8 - Inserting booking. finalCustomerId=" + finalCustomerId + " roomId=" + roomId + " total=" + total);
            // Insert booking
            PreparedStatement bookPs = conn.prepareStatement(
                "INSERT INTO bookings (customer_id, room_id, check_in_date, check_out_date, " +
                "total_amount, payment_method, status, adults, seniors, kids, " +
                "senior_discount, extra_guest_charge) " +
                "VALUES (?,?,?,?,?,?, 'Reserved',?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
            bookPs.setInt(1, finalCustomerId);
            bookPs.setInt(2, roomId);
            bookPs.setDate(3, sqlCheckIn);
            bookPs.setDate(4, sqlCheckOut);
            bookPs.setDouble(5, total);
            bookPs.setString(6, payment);
            bookPs.setInt(7, adults);
            bookPs.setInt(8, seniors);
            bookPs.setInt(9, kids);
            bookPs.setDouble(10, seniorDiscount);
            bookPs.setDouble(11, extraChargeTotal);
            bookPs.executeUpdate();

            ResultSet bookKeys = bookPs.getGeneratedKeys();
            int bookingId = 0;
            if (bookKeys.next()) bookingId = bookKeys.getInt(1);

            // BUG FIX: Removed UPDATE rooms SET is_available = 0 here.
            // Availability is now determined dynamically by the booking overlap query
            // in loadAvailableRooms(), so this static flag update is not needed and
            // was causing rooms to never appear as available again after any booking.

            // Update customer visit count
            PreparedStatement visitPs = conn.prepareStatement(
                "UPDATE customers SET total_visits = total_visits + 1 WHERE customer_id = ?");
            visitPs.setInt(1, finalCustomerId);
            visitPs.executeUpdate();

            // If QR payment selected, create transaction record
            if (payment.equals("QR PH")) {
                PreparedStatement qrPs = conn.prepareStatement(
                    "INSERT INTO payment_transactions (booking_id, customer_id, amount, payment_method, qr_reference, transaction_status) " +
                    "VALUES (?,?,?,?,?, 'Pending')");
                qrPs.setInt(1, bookingId);
                qrPs.setInt(2, finalCustomerId);
                qrPs.setDouble(3, total);
                qrPs.setString(4, "QR PH");
                qrPs.setString(5, "QRPH-" + System.currentTimeMillis());
                qrPs.executeUpdate();
            }

            conn.commit();
            System.out.println("STEP 8 - COMMIT SUCCESS. bookingId=" + bookingId);

            // Show success confirmation
            JOptionPane.showMessageDialog(this,
                "Booking saved successfully!\nBooking ID: " + bookingId,
                "Booking Confirmed", JOptionPane.INFORMATION_MESSAGE);

           // ==========================================
// STEP 3: SMS NOTIFICATION AFTER SUCCESSFUL BOOKING
// ==========================================
try {
    // BUG FIX 3: Use the already-validated local variables (firstName, phone, total)
    //            instead of re-reading the display fields (which may have stale/formatted text).
    String smsMessage = "Hi " + firstName + "! Confirmed na ang booking mo sa Sync Suites Hotel."
                      + " Amount: PHP " + String.format("%,.2f", total) + ". Thank you!";

    ServicesManager.sendSMSAlert(phone, smsMessage);

    System.out.println("✅ SMS Triggered para kay: " + firstName);
} catch (Exception smsEx) {
    System.err.println("❌ Failed to send SMS: " + smsEx.getMessage());
}
// ==========================================
            clearForm();

        } catch (SQLException ex) {
            System.out.println("STEP 8 FAILED - SQLException: " + ex.getMessage());
            ex.printStackTrace();
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException e) { /* ignore */ }
            }
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            System.out.println("STEP 8 FAILED - Unexpected Exception: " + ex.getMessage());
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Unexpected error: " + ex.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { /* ignore */ }
            }
        }
    }

    /**
     * Search customer dialog
     */
    private void showCustomerSearchDialog() {
        JDialog searchDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Search Customer", true);
        searchDialog.setSize(550, 400);
        searchDialog.setLocationRelativeTo(this);
        searchDialog.getContentPane().setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new FlowLayout());
        JTextField txtSearch = new JTextField(20);
        txtSearch.setFont(new Font("SansSerif", Font.PLAIN, 13));
        JButton btnSearch = new JButton("🔍 Search");
        btnSearch.setBackground(new Color(70, 130, 180));
        btnSearch.setForeground(Color.WHITE);
        topPanel.add(new JLabel("Name:"));
        topPanel.add(txtSearch);
        topPanel.add(btnSearch);
        searchDialog.getContentPane().add(topPanel, BorderLayout.NORTH);

        txtSearch.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (c >= '0' && c <= '9') e.consume();
                if (Character.isLetter(c)) e.setKeyChar(Character.toUpperCase(c));
            }
        });

        String[] columns = {"ID", "First Name", "Last Name", "Phone", "Email", "Visits"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable table = new JTable(model);
        table.setRowHeight(25);
        table.setFont(new Font("SansSerif", Font.PLAIN, 12));
        searchDialog.getContentPane().add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        JButton btnSelect = new JButton("✓ Select Customer");
        btnSelect.setBackground(new Color(46, 194, 126));
        btnSelect.setForeground(Color.WHITE);
        btnSelect.setFont(new Font("SansSerif", Font.BOLD, 13));
        bottomPanel.add(btnSelect);
        searchDialog.getContentPane().add(bottomPanel, BorderLayout.SOUTH);

        btnSearch.addActionListener(e -> {
            model.setRowCount(0);
            String keyword = "%" + txtSearch.getText().trim() + "%";

            try (Connection conn = EnhancedDBConnection.getConnection()) {
                String sql = "SELECT customer_id, first_name, last_name, phone_number, email, total_visits " +
                            "FROM customers WHERE first_name LIKE ? OR last_name LIKE ? OR phone_number LIKE ?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, keyword);
                ps.setString(2, keyword);
                ps.setString(3, keyword);
                ResultSet rs = ps.executeQuery();

                while (rs.next()) {
                    model.addRow(new Object[]{
                        rs.getInt("customer_id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("phone_number"),
                        rs.getString("email"),
                        rs.getInt("total_visits")
                    });
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(searchDialog, "Search Error: " + ex.getMessage());
            }
        });

        btnSelect.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                currentCustomerId = (int) model.getValueAt(selectedRow, 0);

                try (Connection conn = EnhancedDBConnection.getConnection()) {
                    PreparedStatement ps = conn.prepareStatement("SELECT * FROM customers WHERE customer_id = ?");
                    ps.setInt(1, currentCustomerId);
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        FNameField.setText(rs.getString("first_name"));
                        LNameField.setText(rs.getString("last_name"));
                        PNumField.setText(rs.getString("phone_number"));
                        emailField.setText(rs.getString("email"));
                        FNameField.setEditable(false);
                        LNameField.setEditable(false);
                        PNumField.setEditable(true);
                        emailField.setEditable(true);
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
                searchDialog.dispose();
            }
        });

        // Auto-search on enter
        txtSearch.addActionListener(e -> btnSearch.doClick());

        searchDialog.setVisible(true);
    }
}