import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class PatientDetails extends JFrame implements ActionListener {
    private static final int FRAME_WIDTH = 600;
    private static final int FRAME_HEIGHT = 690;

    private JTextField mobileField;
    private JTextField nameField, ageField, genderField, bloodGroupField, addressField;
    private JButton searchButton, editButton;
    private Connection conn;

    public PatientDetails(Connection conn) {
        this.conn = conn; // Pass connection from parent page

        // Frame settings
        setTitle("Patient Details");
        setSize(FRAME_WIDTH, FRAME_HEIGHT);
        setLocationRelativeTo(null); // Center the window
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Background Panel
        JPanel backgroundPanel = new JPanel();
        backgroundPanel.setLayout(null); // Absolute layout for precise positioning
        backgroundPanel.setBackground(Color.LIGHT_GRAY);
        add(backgroundPanel); // Add background panel to the JFrame

        int padding = 20;
        int leftpadding = 40;
        int labelWidth = FRAME_WIDTH / 4;
        int fieldWidth = FRAME_WIDTH / 2;
        int rowHeight = 40;
        int topMargin = 30;

        // Mobile No. Label
        JLabel mobileLabel = new JLabel("Mobile No:");
        mobileLabel.setFont(new Font("Arial", Font.BOLD, 18));
        mobileLabel.setBounds(leftpadding, topMargin, labelWidth, rowHeight);
        backgroundPanel.add(mobileLabel);

        // Mobile Field with Rounded Corners and Placeholder Text
        mobileField = new JTextField() {
            private String placeholder = "Enter Mobile Number... ";

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Draw rounded rectangle for the background
                g2.setColor(new Color(245, 245, 245)); // Light gray background
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

                // Draw border
                g2.setColor(new Color(200, 200, 200)); // Light gray border
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);

                // Draw placeholder text
                if (getText().isEmpty() && !isFocusOwner()) {
                    g2.setColor(Color.GRAY); // Placeholder text color
                    g2.setFont(getFont().deriveFont(Font.ITALIC));
                    FontMetrics fm = g2.getFontMetrics();
                    int textY = (getHeight() - fm.getHeight()) / 2 + fm.getAscent(); // Vertically center text
                    g2.drawString(placeholder, 10, textY);
                } else {
                    super.paintComponent(g); // Let the parent class handle normal text rendering
                }

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
        mobileField.setFont(new Font("Arial", Font.PLAIN, 16));
        mobileField.setBounds(labelWidth, topMargin, fieldWidth, rowHeight);
        mobileField.setOpaque(false);
        backgroundPanel.add(mobileField);

        // Search Button
        searchButton = new JButton("Search");
        searchButton.setFont(new Font("Arial", Font.BOLD, 16));
        searchButton.setBounds((FRAME_WIDTH - 120) / 2, topMargin + rowHeight + padding, 120, rowHeight);
        searchButton.addActionListener(this);
        backgroundPanel.add(searchButton);

        // Patient Info Fields
        JLabel nameLabel = new JLabel("Name:");
        nameLabel.setFont(new Font("Arial", Font.BOLD, 16));
        nameLabel.setBounds(leftpadding, topMargin + 3 * (rowHeight + padding), labelWidth, rowHeight);
        backgroundPanel.add(nameLabel);

        nameField = new JTextField();
        nameField.setFont(new Font("Arial", Font.PLAIN, 16));
        nameField.setBounds(labelWidth, topMargin + 3 * (rowHeight + padding), fieldWidth, rowHeight);
        nameField.setEditable(false); // Non-editable by default
        backgroundPanel.add(nameField);

        JLabel ageLabel = new JLabel("Age:");
        ageLabel.setFont(new Font("Arial", Font.BOLD, 16));
        ageLabel.setBounds(leftpadding, topMargin + 4 * (rowHeight + padding), labelWidth, rowHeight);
        backgroundPanel.add(ageLabel);

        ageField = new JTextField();
        ageField.setFont(new Font("Arial", Font.PLAIN, 16));
        ageField.setBounds(labelWidth, topMargin + 4 * (rowHeight + padding), fieldWidth, rowHeight);
        ageField.setEditable(false); // Non-editable by default
        backgroundPanel.add(ageField);

        JLabel genderLabel = new JLabel("Gender:");
        genderLabel.setFont(new Font("Arial", Font.BOLD, 16));
        genderLabel.setBounds(leftpadding, topMargin + 5 * (rowHeight + padding), labelWidth, rowHeight);
        backgroundPanel.add(genderLabel);

        genderField = new JTextField();
        genderField.setFont(new Font("Arial", Font.PLAIN, 16));
        genderField.setBounds(labelWidth, topMargin + 5 * (rowHeight + padding), fieldWidth, rowHeight);
        genderField.setEditable(false); // Non-editable by default
        backgroundPanel.add(genderField);

        JLabel bloodGroupLabel = new JLabel("Blood Group:");
        bloodGroupLabel.setFont(new Font("Arial", Font.BOLD, 16));
        bloodGroupLabel.setBounds(leftpadding, topMargin + 6 * (rowHeight + padding), labelWidth, rowHeight);
        backgroundPanel.add(bloodGroupLabel);

        bloodGroupField = new JTextField();
        bloodGroupField.setFont(new Font("Arial", Font.PLAIN, 16));
        bloodGroupField.setBounds(labelWidth, topMargin + 6 * (rowHeight + padding), fieldWidth, rowHeight);
        bloodGroupField.setEditable(false); // Non-editable by default
        backgroundPanel.add(bloodGroupField);

        JLabel addressLabel = new JLabel("Address:");
        addressLabel.setFont(new Font("Arial", Font.BOLD, 16));
        addressLabel.setBounds(leftpadding, topMargin + 7 * (rowHeight + padding), labelWidth, rowHeight);
        backgroundPanel.add(addressLabel);

        addressField = new JTextField();
        addressField.setFont(new Font("Arial", Font.PLAIN, 16));
        addressField.setBounds(labelWidth, topMargin + 7 * (rowHeight + padding), fieldWidth, rowHeight);
        addressField.setEditable(false); // Non-editable by default
        backgroundPanel.add(addressField);

        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == searchButton) {
            handleSearch();
        }
    }

    private void handleSearch() {
        String mobileNo = mobileField.getText().trim();

        if (mobileNo.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a mobile number.", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            String query = "SELECT name, age, gender, blood_group, address FROM Patient WHERE phone = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, mobileNo);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                nameField.setText(rs.getString("name"));
                ageField.setText(String.valueOf(rs.getInt("age")));
                genderField.setText(rs.getString("gender"));
                bloodGroupField.setText(rs.getString("blood_group"));
                addressField.setText(rs.getString("address"));

                editButton.setEnabled(true); // Enable edit button
            }
            else {
                JOptionPane.showMessageDialog(this, "No patient found with this mobile number.", "Not Found", JOptionPane.INFORMATION_MESSAGE);
                resetFields();
            }

            rs.close();
            stmt.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void resetFields() {
        nameField.setText("");
        ageField.setText("");
        genderField.setText("");
        bloodGroupField.setText("");
        addressField.setText("");
        editButton.setEnabled(false);
    }
}
