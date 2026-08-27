package Hotel_Reservation;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

public class NewBookingPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private JTextField FNameFIeld;
    private JTextField LNameField;
    private JTextField PNumField;
    private JTextField emailField;
    private JComboBox<String> roomTypeCBX;
    private JComboBox<String> availableRoomCBX;
    private JSpinner checkInSpinner;
    private JSpinner checkOutSPinner;
    private JTextField txtTotalAmount;
    private JComboBox<String> PMethodCBX;
    private JLabel roomImageLabel;
    private JLabel roomNameLabel;
    private JSpinner guestCountSpinner;
    private JSpinner childCountSpinner;
    private JSpinner seniorPwdCountSpinner;
    private JLabel lblExtraCharge;
    private JLabel lblDiscountNote;

    private int currentCustomerId = -1;

    private static final double[] BASE_PRICES = {2500.0, 3500.0, 5000.0, 8000.0};
    private static final int[] INCLUDED_GUESTS = {2, 4, 6, 10};
    private static final double[] EXTRA_GUEST_FEE = {500.0, 700.0, 1000.0, 1500.0};

    public NewBookingPanel() {
        this.setForeground(new Color(31, 26, 85));
        this.setSize(1924, 1083);
        this.setLayout(null);

        JLabel lblNewBooking = new JLabel("CREATE NEW BOOKING");
        lblNewBooking.setForeground(new Color(0, 0, 68));
        lblNewBooking.setBounds(44, 20, 364, 28);
        lblNewBooking.setFont(new Font("SansSerif", Font.BOLD, 20));
        this.add(lblNewBooking);

        JButton btnSearchCustomer = new JButton("Search Customer");
        btnSearchCustomer.setBounds(331, 58, 241, 25);
        btnSearchCustomer.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnSearchCustomer.setBackground(new Color(0, 128, 255));
        btnSearchCustomer.setForeground(Color.WHITE);
        btnSearchCustomer.addActionListener(e -> showCustomerSearchDialog());
        this.add(btnSearchCustomer);

        JLabel lblcostumerInformation = new JLabel("--Customer Information");
        lblcostumerInformation.setForeground(new Color(0, 0, 68));
        lblcostumerInformation.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblcostumerInformation.setBounds(101, 58, 241, 17);
        this.add(lblcostumerInformation);

        JLabel lblFirst = new JLabel("First Name:");
        lblFirst.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblFirst.setBounds(193, 98, 104, 17);
        this.add(lblFirst);

        FNameFIeld = new JTextField();
        FNameFIeld.setBounds(331, 93, 241, 21);
        FNameFIeld.setColumns(10);
        FNameFIeld.addKeyListener(new UppercaseLettersOnlyAdapter());
        this.add(FNameFIeld);

        JLabel lblLastName = new JLabel("Last Name:");
        lblLastName.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblLastName.setBounds(193, 140, 104, 17);
        this.add(lblLastName);

        JLabel lblPhoneNumber = new JLabel("Phone Number:");
        lblPhoneNumber.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblPhoneNumber.setBounds(193, 186, 128, 17);
        this.add(lblPhoneNumber);

        JLabel lblEmail = new JLabel("Email:");
        lblEmail.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblEmail.setBounds(200, 236, 121, 17);
        this.add(lblEmail);

        JLabel lblroomSelection = new JLabel("--Room Selection");
        lblroomSelection.setForeground(new Color(0, 0, 68));
        lblroomSelection.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblroomSelection.setBounds(101, 274, 214, 17);
        this.add(lblroomSelection);

        JLabel lblRoomType = new JLabel("Room type:");
        lblRoomType.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblRoomType.setBounds(193, 313, 121, 17);
        this.add(lblRoomType);

        JLabel lblAvailableRoom = new JLabel("Available Room");
        lblAvailableRoom.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblAvailableRoom.setBounds(193, 359, 121, 17);
        this.add(lblAvailableRoom);

        JLabel lblGuestCount = new JLabel("No. of Guests:");
        lblGuestCount.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblGuestCount.setBounds(193, 401, 121, 17);
        this.add(lblGuestCount);

        JLabel lblChildCount = new JLabel("No. of Children (Free):");
        lblChildCount.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblChildCount.setBounds(193, 428, 160, 17);
        this.add(lblChildCount);

        JLabel lbldates = new JLabel("--Dates");
        lbldates.setForeground(new Color(0, 0, 68));
        lbldates.setFont(new Font("SansSerif", Font.BOLD, 18));
        lbldates.setBounds(101, 468, 214, 17);
        this.add(lbldates);

        JLabel lblCheck = new JLabel("Check-in Date");
        lblCheck.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblCheck.setBounds(193, 495, 121, 17);
        this.add(lblCheck);

        JLabel lblCheckoutDate = new JLabel("Check-out Date");
        lblCheckoutDate.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblCheckoutDate.setBounds(193, 541, 128, 17);
        this.add(lblCheckoutDate);

        JLabel lblNewLabel = new JLabel("Payment");
        lblNewLabel.setForeground(new Color(0, 0, 68));
        lblNewLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblNewLabel.setBounds(101, 580, 214, 17);
        this.add(lblNewLabel);

        JLabel lblPaymentMethod = new JLabel("Payment Method");
        lblPaymentMethod.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblPaymentMethod.setBounds(193, 650, 147, 17);
        this.add(lblPaymentMethod);

        JLabel lblCheckOut = new JLabel("Total Amount");
        lblCheckOut.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblCheckOut.setBounds(193, 688, 121, 17);
        this.add(lblCheckOut);

        LNameField = new JTextField();
        LNameField.setBounds(331, 140, 241, 21);
        LNameField.setColumns(10);
        LNameField.addKeyListener(new UppercaseLettersOnlyAdapter());
        this.add(LNameField);

        PNumField = new JTextField();
        PNumField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (!Character.isDigit(c)) {
                    e.consume();
                }
                if (PNumField.getText().length() >= 11) {
                    e.consume();
                }
            }
        });
        PNumField.setBounds(331, 186, 241, 21);
        PNumField.setColumns(10);
        this.add(PNumField);

        emailField = new JTextField();
        emailField.setBounds(331, 236, 241, 21);
        emailField.setColumns(10);
        this.add(emailField);

        String[] roomTypes = {
                "Standard Room - \u20b12,500/day",
                "Junior suite - \u20b13,500/day",
                "Executive Suite - \u20b15,000/day",
                "Presidential Suite - \u20b18,000/day"
        };
        roomTypeCBX = new JComboBox<>(roomTypes);
        roomTypeCBX.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadAvailableRooms();
                calculateTotalAmount();
                String selectedRoom = (String) roomTypeCBX.getSelectedItem();
                String imageFile = "";
                if (selectedRoom.contains("Standard Room")) {
                    imageFile = "Standard_room.jpg";
                    roomNameLabel.setText("Standard Room");
                } else if (selectedRoom.contains("Junior")) {
                    roomNameLabel.setText("Junior Suite");
                    imageFile = "Junior_room.jpg";
                } else if (selectedRoom.contains("Executive")) {
                    imageFile = "Executive_room.jpg";
                    roomNameLabel.setText("Executive Suite");
                } else if (selectedRoom.contains("Presidential")) {
                    imageFile = "Presindetial_room.jpg";
                    roomNameLabel.setText("Presidential Suite");
                }
                roomImageLabel.setIcon(createScaledIcon(imageFile, 815, 649));
            }
        });
        roomTypeCBX.setBounds(331, 310, 241, 26);
        this.add(roomTypeCBX);

        roomNameLabel = new JLabel("");
        roomNameLabel.setBounds(654, 5, 815, 20);
        roomNameLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        roomNameLabel.setForeground(new Color(0, 0, 68));
        roomNameLabel.setHorizontalAlignment(JLabel.RIGHT);
        this.add(roomNameLabel);

        roomImageLabel = new JLabel();
        roomImageLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        roomImageLabel.setBounds(654, 35, 848, 698);
        roomImageLabel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        roomImageLabel.setHorizontalAlignment(JLabel.CENTER);
        this.add(roomImageLabel);

        availableRoomCBX = new JComboBox<>();
        availableRoomCBX.setBounds(331, 356, 241, 26);
        this.add(availableRoomCBX);

        guestCountSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 20, 1));
        guestCountSpinner.setBounds(334, 401, 74, 22);
        guestCountSpinner.addChangeListener(e -> {
            clampDiscountSpinners();
            calculateTotalAmount();
        });
        this.add(guestCountSpinner);

        lblExtraCharge = new JLabel("");
        lblExtraCharge.setFont(new Font("SansSerif", Font.ITALIC, 12));
        lblExtraCharge.setForeground(new Color(224, 27, 36));
        lblExtraCharge.setBounds(416, 401, 258, 22);
        this.add(lblExtraCharge);

        childCountSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 20, 1));
        childCountSpinner.setBounds(363, 425, 74, 22);
        childCountSpinner.addChangeListener(e -> {
            clampDiscountSpinners();
            calculateTotalAmount();
        });
        this.add(childCountSpinner);

        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        java.util.Date minDate = today.getTime();

        SpinnerDateModel checkInModel = new SpinnerDateModel(minDate, minDate, null, Calendar.DAY_OF_MONTH);
        SpinnerDateModel checkOutModel = new SpinnerDateModel(minDate, minDate, null, Calendar.DAY_OF_MONTH);

        checkInSpinner = new JSpinner(checkInModel);
        checkInSpinner.setBounds(331, 495, 241, 22);
        checkInSpinner.setEditor(new JSpinner.DateEditor(checkInSpinner, "yyyy-MM-dd"));
        this.add(checkInSpinner);

        checkOutSPinner = new JSpinner(checkOutModel);
        checkOutSPinner.setBounds(331, 541, 241, 22);
        checkOutSPinner.setEditor(new JSpinner.DateEditor(checkOutSPinner, "yyyy-MM-dd"));
        this.add(checkOutSPinner);

        checkInSpinner.addChangeListener(e -> {
            calculateTotalAmount();
            loadAvailableRooms();
        });
        checkOutSPinner.addChangeListener(e -> {
            calculateTotalAmount();
            loadAvailableRooms();
        });

        txtTotalAmount = new JTextField();
        txtTotalAmount.setBounds(331, 688, 241, 21);
        txtTotalAmount.setEditable(false);
        txtTotalAmount.setFont(new Font("SansSerif", Font.BOLD, 14));
        this.add(txtTotalAmount);

        String[] paymentMethods = {"Cash", "GCash/QR Scan"};
        PMethodCBX = new JComboBox<>(paymentMethods);
        PMethodCBX.setBounds(331, 647, 241, 26);
        this.add(PMethodCBX);

        JButton btnClearForm = new JButton("Clear Form");
        btnClearForm.addActionListener(e -> clearForm());
        btnClearForm.setForeground(Color.WHITE);
        btnClearForm.setBackground(new Color(224, 27, 36));
        btnClearForm.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnClearForm.setBounds(1198, 783, 140, 27);
        this.add(btnClearForm);

        JButton btnSaveBooking = new JButton("Save Booking");
        btnSaveBooking.addActionListener(e -> saveBooking());
        btnSaveBooking.setForeground(Color.WHITE);
        btnSaveBooking.setBackground(new Color(46, 194, 126));
        btnSaveBooking.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnSaveBooking.setBounds(1358, 783, 140, 27);
        this.add(btnSaveBooking);

        seniorPwdCountSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 20, 1));
        seniorPwdCountSpinner.setBounds(331, 604, 74, 22);
        seniorPwdCountSpinner.addChangeListener(e -> {
            clampDiscountSpinners();
            calculateTotalAmount();
        });
        this.add(seniorPwdCountSpinner);

        lblDiscountNote = new JLabel("");
        lblDiscountNote.setFont(new Font("SansSerif", Font.ITALIC, 12));
        lblDiscountNote.setForeground(new Color(224, 27, 36));
        lblDiscountNote.setBounds(416, 604, 258, 22);
        this.add(lblDiscountNote);

        JLabel lblDiscount = new JLabel("No. of Senior/PWD:");
        lblDiscount.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblDiscount.setBounds(193, 607, 145, 15);
        this.add(lblDiscount);

        loadAvailableRooms();
        roomImageLabel.setIcon(createScaledIcon("Standard_room.jpg", 815, 649));
        roomNameLabel.setText("Standard Room");
    }

    /** Shared key adapter: blocks digits, forces letters to uppercase. */
    private static class UppercaseLettersOnlyAdapter extends KeyAdapter {
        @Override
        public void keyTyped(KeyEvent e) {
            char c = e.getKeyChar();
            if (c >= '0' && c <= '9') {
                e.consume();
            }
            if (Character.isLetter(c)) {
                e.setKeyChar(Character.toUpperCase(c));
            }
        }
    }

    private void loadAvailableRooms() {
        availableRoomCBX.removeAllItems();
        int typeIndex = roomTypeCBX.getSelectedIndex() + 1;

        String sql = "SELECT room_id, room_number FROM rooms WHERE room_type_id = ? AND is_available = 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, typeIndex);
            boolean found = false;
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    availableRoomCBX.addItem(rs.getInt("room_id") + " - Room " + rs.getString("room_number"));
                    found = true;
                }
            }
            if (!found) {
                availableRoomCBX.addItem("No rooms available");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading rooms: " + e.getMessage());
        }
    }

    private void calculateTotalAmount() {
        try {
            long days = daysBetween();
            if (days <= 0L) {
                txtTotalAmount.setText("Invalid Dates");
                lblExtraCharge.setText("");
                lblDiscountNote.setText("");
                return;
            }

            int typeIndex = roomTypeCBX.getSelectedIndex();
            int totalGuests = (Integer) guestCountSpinner.getValue();
            int childCount = (Integer) childCountSpinner.getValue();
            int seniorPwdCount = (Integer) seniorPwdCountSpinner.getValue();
            int allowedBaseGuests = INCLUDED_GUESTS[typeIndex];

            // Children are free: they don't count towards the billable/extra-guest headcount.
            int billableGuests = Math.max(0, totalGuests - childCount);

            double total = days * BASE_PRICES[typeIndex];
            int extraGuests = Math.max(0, billableGuests - allowedBaseGuests);
            double extraChargeTotal = extraGuests * EXTRA_GUEST_FEE[typeIndex] * days;

            if (extraGuests > 0) {
                lblExtraCharge.setText(String.format("(+ \u20b1%,.2f for %d extra guests)", extraChargeTotal, extraGuests));
            } else {
                lblExtraCharge.setText("");
            }

            total += extraChargeTotal;

            double discount = computeSeniorPwdDiscount(total, seniorPwdCount, totalGuests);
            if (discount > 0) {
                lblDiscountNote.setText(String.format("(- \u20b1%,.2f for %d Senior/PWD)", discount, seniorPwdCount));
            } else {
                lblDiscountNote.setText("");
            }
            total -= discount;

            txtTotalAmount.setText("\u20b1" + String.format("%,.2f", total));
        } catch (Exception e) {
            txtTotalAmount.setText("Error");
        }
    }

    /** Keeps children + Senior/PWD counts from exceeding the total number of guests. */
    private void clampDiscountSpinners() {
        int totalGuests = (Integer) guestCountSpinner.getValue();

        int childCount = (Integer) childCountSpinner.getValue();
        if (childCount > totalGuests) {
            childCountSpinner.setValue(totalGuests);
            childCount = totalGuests;
        }

        int seniorPwdCount = (Integer) seniorPwdCountSpinner.getValue();
        int maxSeniorPwd = Math.max(0, totalGuests - childCount);
        if (seniorPwdCount > maxSeniorPwd) {
            seniorPwdCountSpinner.setValue(maxSeniorPwd);
        }
    }

    /** Returns the whole-day difference between check-in and check-out, normalized to midnight. */
    private long daysBetween() {
        java.util.Date checkInDate = (java.util.Date) checkInSpinner.getValue();
        java.util.Date checkOutDate = (java.util.Date) checkOutSPinner.getValue();

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

        return (calOut.getTimeInMillis() - calIn.getTimeInMillis()) / 86400000L;
    }

    private void clearForm() {
        FNameFIeld.setText("");
        LNameField.setText("");
        PNumField.setText("");
        emailField.setText("");
        roomTypeCBX.setSelectedIndex(0);
        PMethodCBX.setSelectedIndex(0);
        checkInSpinner.setValue(new java.util.Date());
        checkOutSPinner.setValue(new java.util.Date());
        txtTotalAmount.setText("\u20b1 0.00");
        roomImageLabel.setIcon(null);
        roomNameLabel.setText("");
        guestCountSpinner.setValue(1);
        childCountSpinner.setValue(0);
        seniorPwdCountSpinner.setValue(0);
        lblExtraCharge.setText("");
        lblDiscountNote.setText("");
        currentCustomerId = -1;
        FNameFIeld.setEditable(true);
        LNameField.setEditable(true);
    }

    private void saveBooking() {
        if (FNameFIeld.getText().trim().isEmpty() || LNameField.getText().trim().isEmpty()
                || PNumField.getText().trim().isEmpty() || emailField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all customer information.",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String phone = PNumField.getText().trim();
        if (!phone.matches("\\d{10,11}")) {
            JOptionPane.showMessageDialog(this, "Please enter a valid phone number (10-11 digits).",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String email = emailField.getText().trim();
        if (!email.contains("@") || !email.contains(".")) {
            JOptionPane.showMessageDialog(this, "Please enter a valid email address.",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (currentCustomerId != -1 && hasActiveBooking(currentCustomerId)) {
            return; // message already shown inside hasActiveBooking
        }

        String roomEntry = (String) availableRoomCBX.getSelectedItem();
        if (roomEntry == null || roomEntry.startsWith("No rooms")) {
            JOptionPane.showMessageDialog(this, "No available room selected.",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int roomId = Integer.parseInt(roomEntry.split(" - ")[0].trim());

        if (!isRoomStillAvailable(roomId, roomEntry)) {
            return; // message already shown inside isRoomStillAvailable
        }

        long days = daysBetween();
        if (days <= 0L) {
            JOptionPane.showMessageDialog(this, "Check-out must be after check-in.",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Calendar calIn = Calendar.getInstance();
        calIn.setTime((java.util.Date) checkInSpinner.getValue());
        calIn.set(Calendar.HOUR_OF_DAY, 0);
        calIn.set(Calendar.MINUTE, 0);
        calIn.set(Calendar.SECOND, 0);
        calIn.set(Calendar.MILLISECOND, 0);

        Calendar calOut = Calendar.getInstance();
        calOut.setTime((java.util.Date) checkOutSPinner.getValue());
        calOut.set(Calendar.HOUR_OF_DAY, 0);
        calOut.set(Calendar.MINUTE, 0);
        calOut.set(Calendar.SECOND, 0);
        calOut.set(Calendar.MILLISECOND, 0);

        int typeIndex = roomTypeCBX.getSelectedIndex();
        int totalGuests = (Integer) guestCountSpinner.getValue();
        int childCount = (Integer) childCountSpinner.getValue();
        int seniorPwdCount = (Integer) seniorPwdCountSpinner.getValue();

        if (childCount + seniorPwdCount > totalGuests) {
            JOptionPane.showMessageDialog(this,
                    "No. of Children + No. of Senior/PWD cannot exceed the total No. of Guests.",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int allowedBaseGuests = INCLUDED_GUESTS[typeIndex];
        int billableGuests = Math.max(0, totalGuests - childCount);
        int extraGuests = Math.max(0, billableGuests - allowedBaseGuests);

        double baseTotal = BASE_PRICES[typeIndex] * days;
        double extraChargeTotal = extraGuests * EXTRA_GUEST_FEE[typeIndex] * days;
        double subTotal = baseTotal + extraChargeTotal;
        double discount = computeSeniorPwdDiscount(subTotal, seniorPwdCount, totalGuests);
        double total = subTotal - discount;

        Date sqlCheckIn = new Date(calIn.getTimeInMillis());
        Date sqlCheckOut = new Date(calOut.getTimeInMillis());
        String payment = (String) PMethodCBX.getSelectedItem();

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int finalCustomerId = currentCustomerId;

                if (finalCustomerId == -1) {
                    String custSQL = "INSERT INTO customers (first_name, last_name, phone_number, email, created_at) VALUES (?,?,?,?, NOW())";
                    try (PreparedStatement custPS = conn.prepareStatement(custSQL, PreparedStatement.RETURN_GENERATED_KEYS)) {
                        custPS.setString(1, FNameFIeld.getText().trim());
                        custPS.setString(2, LNameField.getText().trim());
                        custPS.setString(3, phone);
                        custPS.setString(4, email);
                        custPS.executeUpdate();
                        try (ResultSet keys = custPS.getGeneratedKeys()) {
                            if (keys.next()) {
                                finalCustomerId = keys.getInt(1);
                            }
                        }
                    }
                } else {
                    String updateSQL = "UPDATE customers SET phone_number = ?, email = ? WHERE customer_id = ?";
                    try (PreparedStatement updatePS = conn.prepareStatement(updateSQL)) {
                        updatePS.setString(1, phone);
                        updatePS.setString(2, email);
                        updatePS.setInt(3, finalCustomerId);
                        updatePS.executeUpdate();
                    }
                }

                String bookSQL = "INSERT INTO bookings (customer_id, room_id, check_in_date, check_out_date, total_amount, payment_method, status) VALUES (?,?,?,?,?,?, 'Reserved')";
                try (PreparedStatement bookPS = conn.prepareStatement(bookSQL)) {
                    bookPS.setInt(1, finalCustomerId);
                    bookPS.setInt(2, roomId);
                    bookPS.setDate(3, sqlCheckIn);
                    bookPS.setDate(4, sqlCheckOut);
                    bookPS.setDouble(5, total);
                    bookPS.setString(6, payment);
                    bookPS.executeUpdate();
                }

                try (PreparedStatement roomPS = conn.prepareStatement("UPDATE rooms SET is_available = 0 WHERE room_id = ?")) {
                    roomPS.setInt(1, roomId);
                    roomPS.executeUpdate();
                }

                conn.commit();
                JOptionPane.showMessageDialog(this,
                        String.format("\u2705 Booking saved!\nCustomer: %s %s\nTotal: \u20b1%,.2f",
                                FNameFIeld.getText(), LNameField.getText(), total),
                        "Booking Confirmed", JOptionPane.INFORMATION_MESSAGE);
                clearForm();
                loadAvailableRooms();
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Returns true (and shows a dialog) if the customer already has a Reserved/Checked In booking. */
    private boolean hasActiveBooking(int customerId) {
        String checkSQL = "SELECT b.booking_id, r.room_number, b.status FROM bookings b "
                + "JOIN rooms r ON b.room_id = r.room_id "
                + "WHERE b.customer_id = ? AND b.status IN ('Reserved', 'Checked In')";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement checkPS = conn.prepareStatement(checkSQL)) {

            checkPS.setInt(1, customerId);
            try (ResultSet checkRS = checkPS.executeQuery()) {
                if (checkRS.next()) {
                    String roomNum = checkRS.getString("room_number");
                    String status = checkRS.getString("status");
                    JOptionPane.showMessageDialog(this,
                            "This customer already has an active booking at Room " + roomNum
                                    + " (Status: " + status + ").\nCannot create a new booking while a reservation or check-in is active.",
                            "Booking Blocked", JOptionPane.ERROR_MESSAGE);
                    return true;
                }
            }
            return false;
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error checking customer status: " + ex.getMessage());
            return true; // fail safe: block the booking if we couldn't verify
        }
    }

    /** Returns true if the room is still available; shows a dialog and refreshes the list otherwise. */
    private boolean isRoomStillAvailable(int roomId, String roomEntry) {
        String sql = "SELECT is_available FROM rooms WHERE room_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, roomId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt("is_available") == 0) {
                    JOptionPane.showMessageDialog(this,
                            "Room " + roomEntry + " was just taken by another booking.\nPlease select a different room.",
                            "Room No Longer Available", JOptionPane.ERROR_MESSAGE);
                    loadAvailableRooms();
                    return false;
                }
            }
            return true;
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error checking room availability: " + ex.getMessage());
            return false;
        }
    }

    private ImageIcon createScaledIcon(String path, int width, int height) {
        java.net.URL imgURL = getClass().getResource("images/" + path);
        if (imgURL == null) {
            System.out.println("Warning: Could not find image at /images/" + path);
            return null;
        }
        ImageIcon icon = new ImageIcon(imgURL);
        Image img = icon.getImage();
        Image newImg = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(newImg);
    }

    /**
     * Senior Citizen / PWD discount (RA 9994 / RA 10754) applies only to the portion of the
     * bill attributable to each qualifying guest, not the whole booking. We approximate each
     * guest's share as an equal split of the total across all guests in the room, then give a
     * 20% discount on the share belonging to Senior/PWD guests.
     */
    private double computeSeniorPwdDiscount(double subTotal, int seniorPwdCount, int totalGuests) {
        if (seniorPwdCount <= 0 || totalGuests <= 0) {
            return 0.0;
        }
        int cappedCount = Math.min(seniorPwdCount, totalGuests);
        double perGuestShare = subTotal / totalGuests;
        return perGuestShare * cappedCount * 0.20;
    }

    private void showCustomerSearchDialog() {
        JDialog searchDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Search Customer", true);
        searchDialog.setSize(500, 350);
        searchDialog.setLocationRelativeTo(this);
        searchDialog.getContentPane().setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new FlowLayout());
        JTextField txtSearch = new JTextField(20);
        JButton btnSearch = new JButton("Search");
        topPanel.add(new JLabel("Name:"));
        topPanel.add(txtSearch);
        topPanel.add(btnSearch);
        searchDialog.getContentPane().add((Component) topPanel, BorderLayout.NORTH);

        txtSearch.addKeyListener(new UppercaseLettersOnlyAdapter());

        String[] columns = {"ID", "First Name", "Last Name", "Phone"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(model);
        searchDialog.getContentPane().add((Component) new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        JButton btnSelect = new JButton("Select Customer");
        bottomPanel.add(btnSelect);
        searchDialog.getContentPane().add((Component) bottomPanel, BorderLayout.SOUTH);

        btnSearch.addActionListener(e -> {
            model.setRowCount(0);
            String keyword = "%" + txtSearch.getText().trim() + "%";
            String sql = "SELECT customer_id, first_name, last_name, phone_number, email FROM customers "
                    + "WHERE first_name LIKE ? OR last_name LIKE ?";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setString(1, keyword);
                ps.setString(2, keyword);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        model.addRow(new Object[]{
                                rs.getInt("customer_id"),
                                rs.getString("first_name"),
                                rs.getString("last_name"),
                                rs.getString("phone_number")
                        });
                    }
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(searchDialog, "Search Error: " + ex.getMessage());
            }
        });

        btnSelect.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                return;
            }
            currentCustomerId = (Integer) model.getValueAt(selectedRow, 0);

            String sql = "SELECT * FROM customers WHERE customer_id = ?";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setInt(1, currentCustomerId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        FNameFIeld.setText(rs.getString("first_name"));
                        LNameField.setText(rs.getString("last_name"));
                        PNumField.setText(rs.getString("phone_number"));
                        emailField.setText(rs.getString("email"));
                        FNameFIeld.setEditable(false);
                        LNameField.setEditable(false);
                        PNumField.setEditable(true);
                        emailField.setEditable(true);
                    }
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error loading customer: " + ex.getMessage());
            }
            searchDialog.dispose();
        });

        searchDialog.setVisible(true);
    }
}