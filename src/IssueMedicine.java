import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.ArrayList;
import javax.swing.border.Border;

public class IssueMedicine extends JFrame {
    private static final int FRAME_WIDTH = 650;
    private static final int FRAME_HEIGHT = 800;

    private JTextField mobileField, doctorIdField, quantityField;
    private JComboBox<String> medicineDropdown;
    private JTextArea medicationListArea;
    private JButton addMedicineButton, resetButton, generateBillButton;
    private JLabel patientNameLabel, NameLabel;
    private Connection conn;

    private ArrayList<String> medications;

    public IssueMedicine(Connection conn) {
        this.conn = conn;
        this.medications = new ArrayList<>();

        // Frame settings
        setTitle("Issue Medicine");
        setSize(FRAME_WIDTH, FRAME_HEIGHT);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Background Panel
        JPanel backgroundPanel = new JPanel();
        backgroundPanel.setLayout(null);
        backgroundPanel.setBackground(Color.LIGHT_GRAY);
        add(backgroundPanel);

        int padding = 20;
        int labelWidth = FRAME_WIDTH / 3;
        int fieldWidth = FRAME_WIDTH - (labelWidth + 3 * padding);
        int rowHeight = 40;
        int topMargin = 30;

        // Mobile Number FieldL
        mobileField = createRoundedTextField();
        mobileField.setOpaque(false);
        JLabel mobileLabel = createLabel("Mobile No.:");
        mobileLabel.setBounds(padding, topMargin, labelWidth, rowHeight);
        backgroundPanel.add(mobileLabel);
        mobileField.setBounds(labelWidth, topMargin, fieldWidth, rowHeight);
        backgroundPanel.add(mobileField);

        // Patient Name Field
        patientNameLabel = new JLabel("Patient Name: ");
        patientNameLabel.setFont(new Font("Arial", Font.BOLD, 16));
        patientNameLabel.setBounds(padding, topMargin + rowHeight + padding, FRAME_WIDTH - (2 * padding), rowHeight);
        backgroundPanel.add(patientNameLabel);

        // Name Field
        NameLabel = new JLabel("Not Found");
        NameLabel.setFont(new Font("Arial", Font.BOLD, 16));
        NameLabel.setBounds(labelWidth, topMargin + rowHeight + padding, FRAME_WIDTH - (2 * padding), rowHeight);
        backgroundPanel.add(NameLabel);

        // Doctor ID Field
        doctorIdField = createRoundedTextField();
        doctorIdField.setOpaque(false);
        JLabel doctorIdLabel = createLabel("Doctor ID:");
        doctorIdLabel.setBounds(padding, topMargin + 2 * (rowHeight + padding), labelWidth, rowHeight);
        backgroundPanel.add(doctorIdLabel);
        doctorIdField.setBounds(labelWidth, topMargin + 2 * (rowHeight + padding), fieldWidth, rowHeight);
        backgroundPanel.add(doctorIdField);

        // Medicine Lable
        JLabel medicineLabel = createLabel("Medicine:");
        medicineLabel.setBounds(padding, topMargin + 3 * (rowHeight + padding), labelWidth, rowHeight);
        backgroundPanel.add(medicineLabel);

        // Medicine Dropdown
        medicineDropdown = new JComboBox<>();
        populateMedicineDropdown(); // Fetch medicines from the database
        medicineDropdown.setFont(new Font("Arial", Font.PLAIN, 16));
        medicineDropdown.setBounds(labelWidth, topMargin + 3 * (rowHeight + padding), fieldWidth, rowHeight);
        backgroundPanel.add(medicineDropdown);

        // Quantity Field
        quantityField = createRoundedTextField();
        quantityField.setOpaque(false);
        JLabel quantityLabel = createLabel("Quantity:");
        quantityLabel.setBounds(padding, topMargin + 4 * (rowHeight + padding), labelWidth, rowHeight);
        backgroundPanel.add(quantityLabel);
        quantityField.setBounds(labelWidth, topMargin + 4 * (rowHeight + padding), fieldWidth, rowHeight);
        backgroundPanel.add(quantityField);

        // Add Medicine Button
        addMedicineButton = new JButton("Add Medicine");
        addMedicineButton.setFont(new Font("Arial", Font.BOLD, 16));
        addMedicineButton.setBounds(labelWidth, topMargin + 5 * (rowHeight + padding), fieldWidth / 2, rowHeight);
        addMedicineButton.setEnabled(false);
        backgroundPanel.add(addMedicineButton);

        // Medication List Area
        medicationListArea = new JTextArea();
        medicationListArea.setEditable(false);
        medicationListArea.setFont(new Font("Arial", Font.PLAIN, 16));
        JScrollPane scrollPane = new JScrollPane(medicationListArea);
        scrollPane.setBounds(padding, topMargin + 6 * (rowHeight + padding), FRAME_WIDTH - (3*padding), 250);
        backgroundPanel.add(scrollPane);

        // Buttons
        resetButton = new JButton("Reset");
        resetButton.setFont(new Font("Arial", Font.BOLD, 16));
        resetButton.setBounds(3*padding, FRAME_HEIGHT - 3 * rowHeight, 120, rowHeight);
        backgroundPanel.add(resetButton);

        generateBillButton = new JButton("Generate Bill");
        generateBillButton.setFont(new Font("Arial", Font.BOLD, 16));
        generateBillButton.setBounds(FRAME_WIDTH/2 + 77, FRAME_HEIGHT - 3 * rowHeight, fieldWidth/2, rowHeight);
        backgroundPanel.add(generateBillButton);

        // Add Action Listeners
        addMedicineButton.addActionListener(e -> handleAddMedicine());
        resetButton.addActionListener(e -> resetFields());
        generateBillButton.addActionListener(e -> handleGenerateBill());
        mobileField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                fetchPatientName();
            }
        });

        setVisible(true);
    }

    private void fetchPatientName() {
        String mobileNumber = mobileField.getText().trim();
        if (mobileNumber.length() == 10 && mobileNumber.matches("\\d{10}")) {
            try {
                String query = "SELECT name FROM Patient WHERE phone = ?";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setString(1, mobileNumber);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    NameLabel.setText(rs.getString("name"));
                    addMedicineButton.setEnabled(true);
                } else {
                    NameLabel.setText("Not Found");
                    addMedicineButton.setEnabled(false);
                }
                rs.close();
                stmt.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                addMedicineButton.setEnabled(false);
            }
        } else {
            NameLabel.setText("Not Found");
            addMedicineButton.setEnabled(false);
        }
    }

    private boolean validateDoctorId() {
        String doctorId = doctorIdField.getText().trim();
        if (doctorId.isEmpty() || !doctorId.matches("\\d+")) {
            JOptionPane.showMessageDialog(this, "Invalid Doctor ID. Please enter a valid numeric ID.", "Input Error", JOptionPane.WARNING_MESSAGE);
            return false;
        }
    
        try {
            String query = "SELECT name FROM Doctor WHERE doctor_id = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, Integer.parseInt(doctorId));
            ResultSet rs = stmt.executeQuery();
    
            if (rs.next()) {
                // Doctor exists, validation passed
                rs.close();
                stmt.close();
                return true;
            } else {
                // Doctor not found
                JOptionPane.showMessageDialog(this, "Doctor ID not found in the database.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                rs.close();
                stmt.close();
                return false;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
    
    private void handleAddMedicine() {
        if (!validateDoctorId()) {
            return; // Exit if the doctor ID is invalid
        }
        String selectedMedicine = (String) medicineDropdown.getSelectedItem();
        String quantityText = quantityField.getText().trim();

        if (selectedMedicine == null || selectedMedicine.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a medicine.", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (quantityText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a quantity.", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int quantity = Integer.parseInt(quantityText);

            if (quantity <= 0) {
                JOptionPane.showMessageDialog(this, "Quantity must be a positive number.", "Input Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Check stock
            String stockQuery = "SELECT stock_quantity FROM Medicine WHERE name = ?";
            PreparedStatement stockStmt = conn.prepareStatement(stockQuery);
            stockStmt.setString(1, selectedMedicine);
            ResultSet stockRs = stockStmt.executeQuery();

            if (stockRs.next()) {
                int availableStock = stockRs.getInt("stock_quantity");

                if (quantity > availableStock) {
                    JOptionPane.showMessageDialog(this,
                            "Insufficient stock for " + selectedMedicine + ". Only " + availableStock + " available.",
                            "Stock Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // Medicine is valid, add to list
                medications.add(selectedMedicine + " (" + quantity + ")");
                updateMedicationListDisplay();
            } else {
                JOptionPane.showMessageDialog(this, "Selected medicine not found in stock.", "Database Error", JOptionPane.ERROR_MESSAGE);
            }

            stockRs.close();
            stockStmt.close();
            quantityField.setText("");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid quantity entered. Please enter a valid number.", "Input Error", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleGenerateBill() {
        String mobileNumber = mobileField.getText().trim();
        String doctorId = doctorIdField.getText().trim();
    
        if (mobileNumber.isEmpty() || !mobileNumber.matches("\\d{10}")) {
            JOptionPane.showMessageDialog(this, "Invalid mobile number.", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
    
        if (doctorId.isEmpty() || !doctorId.matches("\\d+")) {
            JOptionPane.showMessageDialog(this, "Invalid Doctor ID.", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
    
        if (medications.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No medicines added.", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
    
        try {
            // Get patient_id from the database
            String patientQuery = "SELECT patient_id FROM Patient WHERE phone = ?";
            PreparedStatement patientStmt = conn.prepareStatement(patientQuery);
            patientStmt.setString(1, mobileNumber);
            ResultSet patientRs = patientStmt.executeQuery();
    
            if (!patientRs.next()) {
                JOptionPane.showMessageDialog(this, "Patient not found.", "Input Error", JOptionPane.WARNING_MESSAGE);
                patientRs.close();
                patientStmt.close();
                return;
            }
    
            int patientId = patientRs.getInt("patient_id");
            patientRs.close();
            patientStmt.close();
    
            // Insert into Prescription table
            String medicationString = String.join("; ", medications);
            String prescriptionQuery = "INSERT INTO Prescription (doctor_id, patient_id, medication) VALUES (?, ?, ?)";
            PreparedStatement prescriptionStmt = conn.prepareStatement(prescriptionQuery, Statement.RETURN_GENERATED_KEYS);
            prescriptionStmt.setInt(1, Integer.parseInt(doctorId));
            prescriptionStmt.setInt(2, patientId);
            prescriptionStmt.setString(3, medicationString);
    
            int rowsInserted = prescriptionStmt.executeUpdate();
    
            if (rowsInserted > 0) {
                ResultSet generatedKeys = prescriptionStmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int prescriptionId = generatedKeys.getInt(1);
                    generatedKeys.close();
                    prescriptionStmt.close();
    
                    // Open BillPopup with the prescription_id
                    new BillPopup(conn, prescriptionId);
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to retrieve prescription ID.", "Database Error", JOptionPane.ERROR_MESSAGE);
                    prescriptionStmt.close();
                    return;
                }
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add prescription.", "Database Error", JOptionPane.ERROR_MESSAGE);
                prescriptionStmt.close();
                return;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    

    private void updateMedicationListDisplay() {
        StringBuilder medicationText = new StringBuilder();
        for (String medication : medications) {
            medicationText.append(medication).append("\n");
        }
        medicationListArea.setText(medicationText.toString());
    }

    private void resetFields() {
        mobileField.setText("");
        doctorIdField.setText("");
        quantityField.setText("");
        medications.clear();
        updateMedicationListDisplay();
        patientNameLabel.setText("Patient Name: ");
    }

    private void populateMedicineDropdown() {
        try {
            String query = "SELECT name FROM Medicine";
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                medicineDropdown.addItem(rs.getString("name"));
            }

            rs.close();
            stmt.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JTextField createRoundedTextField() {
        return new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(new Color(245, 245, 245)); // Light gray background
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

                g2.setColor(new Color(200, 200, 200)); // Light gray border
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);

                super.paintComponent(g2);
                g2.dispose();
            }

            @Override
            public void setBorder(Border border) {
                // Disable default border
            }

            @Override
            public Insets getInsets() {
                return new Insets(5, 10, 5, 10);
            }
        };
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 16));
        return label;
    }
}
