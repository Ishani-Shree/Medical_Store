import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.border.Border;

public class NewPatient extends JFrame implements ActionListener {
    private static final int FRAME_WIDTH = 600;
    private static final int FRAME_HEIGHT = 700;

    private JTextField nameField, ageField, phoneField, addressField;
    private JComboBox<String> genderBox, bloodGroupBox;
    private JButton addButton, resetButton;
    private Connection conn;

    public NewPatient(Connection conn) {
        this.conn = conn; // Database connection passed from parent

        // Frame settings
        setTitle("New Patient");
        setSize(FRAME_WIDTH, FRAME_HEIGHT);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Background Panel
        JPanel backgroundPanel = new JPanel();
        backgroundPanel.setLayout(null);
        backgroundPanel.setBackground(Color.LIGHT_GRAY);
        add(backgroundPanel);

        // Heading Panel
        JPanel headingPanel = new JPanel();
        headingPanel.setLayout(new BorderLayout());
        headingPanel.setBackground(Color.LIGHT_GRAY); // Cornflower Blue
        headingPanel.setBounds(0, 20, FRAME_WIDTH, 50);

        JLabel headingLabel = new JLabel("New Patient", SwingConstants.CENTER);
        headingLabel.setFont(new Font("Comic Sans MS", Font.BOLD, 30));
        headingLabel.setForeground(Color.BLACK);
        headingPanel.add(headingLabel);

        backgroundPanel.add(headingPanel);

        int padding = 20;
        int labelWidth = FRAME_WIDTH / 3;
        int fieldWidth = FRAME_WIDTH - (labelWidth + 3 * padding);
        int rowHeight = 40;
        int topMargin = 30;

        // Create and add fields
        nameField = createRoundedTextField();
        ageField = createRoundedTextField();
        phoneField = createRoundedTextField();
        addressField = createRoundedTextField();

        // Gender Dropdown
        genderBox = new JComboBox<>(new String[]{"Select Gender", "M", "F", "O"});
        genderBox.setFont(new Font("Arial", Font.PLAIN, 16));
        genderBox.setBounds(7*(labelWidth / 8), topMargin + 3 * (rowHeight + padding), fieldWidth, rowHeight);
        backgroundPanel.add(genderBox);

        // Blood Group Dropdown
        bloodGroupBox = new JComboBox<>(new String[]{"Select Blood Group", "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"});
        bloodGroupBox.setFont(new Font("Arial", Font.PLAIN, 16));
        bloodGroupBox.setBounds(7*(labelWidth / 8), topMargin + 5 * (rowHeight + padding), fieldWidth, rowHeight);
        backgroundPanel.add(bloodGroupBox);

        // Labels and Input Fields
        addLabelAndField("Name:", nameField, backgroundPanel, padding, labelWidth, fieldWidth, rowHeight, topMargin, 1);
        addLabelAndField("Age:", ageField, backgroundPanel, padding, labelWidth, fieldWidth, rowHeight, topMargin, 2);
        addLabelAndField("Gender:", null, backgroundPanel, padding, labelWidth, fieldWidth, rowHeight, topMargin, 3);
        addLabelAndField("Phone:", phoneField, backgroundPanel, padding, labelWidth, fieldWidth, rowHeight, topMargin, 4);
        addLabelAndField("Blood Group:", null, backgroundPanel, padding, labelWidth, fieldWidth, rowHeight, topMargin, 5);
        addLabelAndField("Address:", addressField, backgroundPanel, padding, labelWidth, fieldWidth, rowHeight, topMargin, 6);

        // Buttons
        addButton = new JButton("Add");
        addButton.setFont(new Font("Arial", Font.BOLD, 16));
        addButton.setBounds((FRAME_WIDTH - 220) / 2, topMargin + 8 * (rowHeight + padding), 100, rowHeight);
        addButton.addActionListener(this);
        backgroundPanel.add(addButton);

        resetButton = new JButton("Reset");
        resetButton.setFont(new Font("Arial", Font.BOLD, 16));
        resetButton.setBounds((FRAME_WIDTH - 220) / 2 + 120, topMargin + 8 * (rowHeight + padding), 100, rowHeight);
        resetButton.addActionListener(this);
        backgroundPanel.add(resetButton);

        setVisible(true);
    }

    private JTextField createRoundedTextField() {
        return new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Draw rounded rectangle
                g2.setColor(new Color(245, 245, 245)); // Light gray background
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

                // Draw border
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
                // Add padding for user input text
                return new Insets(5, 10, 5, 10); // Top, Left, Bottom, Right
            }
        };
    }

    private void addLabelAndField(String labelText, JComponent field, JPanel panel, int padding, int labelWidth, int fieldWidth, int rowHeight, int topMargin, int rowNumber) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Arial", Font.BOLD, 16));
        label.setBounds(2*padding, topMargin + rowNumber * (rowHeight + padding), labelWidth, rowHeight);
        panel.add(label);

        if (field != null) {
            field.setFont(new Font("Arial", Font.PLAIN, 16));
            field.setBounds(7*(labelWidth / 8), topMargin + rowNumber * (rowHeight + padding), fieldWidth, rowHeight);
            field.setOpaque(false);
            panel.add(field);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == addButton) {
            handleAddPatient();
        } else if (e.getSource() == resetButton) {
            resetFields();
        }
    }

    private void handleAddPatient() {
        String name = nameField.getText().trim();
        String ageText = ageField.getText().trim();
        String gender = (String) genderBox.getSelectedItem();
        String phone = phoneField.getText().trim();
        String bloodGroup = (String) bloodGroupBox.getSelectedItem();
        String address = addressField.getText().trim();

        // Validate input fields
        if (name.isEmpty() || ageText.isEmpty() || phone.isEmpty() || address.isEmpty() || gender.equals("Select Gender") || bloodGroup.equals("Select Blood Group")) {
            JOptionPane.showMessageDialog(this, "All fields are required.", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // Validate age
            int age = Integer.parseInt(ageText);
            if (age <= 0) {
                JOptionPane.showMessageDialog(this, "Age must be a positive number.", "Input Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Validate phone number
            if (!phone.matches("\\d{10}")) {
                JOptionPane.showMessageDialog(this, "Phone number must be exactly 10 digits.", "Input Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Insert into database
            String query = "INSERT INTO Patient (name, age, gender, phone, blood_group, address) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, name);
            stmt.setInt(2, age);
            stmt.setString(3, gender);
            stmt.setString(4, phone);
            stmt.setString(5, bloodGroup);
            stmt.setString(6, address);

            int rowsInserted = stmt.executeUpdate();
            if (rowsInserted > 0) {
                JOptionPane.showMessageDialog(this, "Patient added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                resetFields();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add patient.", "Error", JOptionPane.ERROR_MESSAGE);
            }

            stmt.close();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid age entered.", "Input Error", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void resetFields() {
        nameField.setText("");
        ageField.setText("");
        phoneField.setText("");
        addressField.setText("");
        genderBox.setSelectedIndex(0);
        bloodGroupBox.setSelectedIndex(0);
    }
}
