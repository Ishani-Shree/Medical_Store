import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.sql.Connection;

public class AddMedicine extends JFrame implements ActionListener {
    private JTextField nameField, quantityField, expiryDateField, costField;
    private JButton addButton, resetButton, closeButton;
    private Connection conn; // Shared connection

    public AddMedicine(Connection conn) {
        this.conn = conn;

        // Frame settings
        setTitle("Add Medicine");
        setSize(600, 400);
        setLayout(new BorderLayout());
        setLocationRelativeTo(null); // Center the window
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Background panel
        JPanel backgroundPanel = new JPanel();
        backgroundPanel.setLayout(null);
        backgroundPanel.setBackground(new Color(245, 245, 245));    // Light gray background
        add(backgroundPanel);

        // Form labels and fields
        JLabel nameLabel = new JLabel("Medicine Name:");
        nameLabel.setBounds(50, 50, 150, 30);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 16));
        nameLabel.setForeground(Color.BLACK);
        backgroundPanel.add(nameLabel);

        nameField = new JTextField();
        nameField.setBounds(200, 50, 300, 30);
        backgroundPanel.add(nameField);

        JLabel quantityLabel = new JLabel("Quantity:");
        quantityLabel.setBounds(50, 100, 150, 30);
        quantityLabel.setFont(new Font("Arial", Font.BOLD, 16));
        quantityLabel.setForeground(Color.BLACK);
        backgroundPanel.add(quantityLabel);

        quantityField = new JTextField();
        quantityField.setBounds(200, 100, 300, 30);
        backgroundPanel.add(quantityField);

        JLabel expiryLabel = new JLabel("Expiry Date (YYYY-MM-DD):");
        expiryLabel.setBounds(50, 150, 250, 30);
        expiryLabel.setFont(new Font("Arial", Font.BOLD, 16));
        expiryLabel.setForeground(Color.BLACK);
        backgroundPanel.add(expiryLabel);

        expiryDateField = new JTextField();
        expiryDateField.setBounds(275, 150, 225, 30);
        backgroundPanel.add(expiryDateField);

        JLabel costLabel = new JLabel("Cost per Unit:");
        costLabel.setBounds(50, 200, 150, 30);
        costLabel.setFont(new Font("Arial", Font.BOLD, 16));
        costLabel.setForeground(Color.BLACK);
        backgroundPanel.add(costLabel);

        costField = new JTextField();
        costField.setBounds(200, 200, 300, 30);
        backgroundPanel.add(costField);

        // Buttons
        addButton = new JButton("Add/Update");
        addButton.setBounds(100, 275, 120, 40);
        addButton.setFont(new Font("Arial", Font.BOLD, 16));
        addButton.addActionListener(this);
        backgroundPanel.add(addButton);

        resetButton = new JButton("Reset");
        resetButton.setBounds(250, 275, 100, 40);
        resetButton.setFont(new Font("Arial", Font.BOLD, 16));
        resetButton.addActionListener(this);
        backgroundPanel.add(resetButton);

        closeButton = new JButton("Close");
        closeButton.setBounds(400, 275, 100, 40);
        closeButton.setFont(new Font("Arial", Font.BOLD, 16));
        closeButton.addActionListener(this);
        backgroundPanel.add(closeButton);

        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == addButton) {
            handleAddOrUpdateMedicine();
        } else if (e.getSource() == resetButton) {
            nameField.setText("");
            quantityField.setText("");
            expiryDateField.setText("");
            costField.setText("");
        } else if (e.getSource() == closeButton) {
            dispose();
        }
    }

    private void handleAddOrUpdateMedicine() {
        String name = nameField.getText().trim();
        String quantityText = quantityField.getText().trim();
        String expiryDate = expiryDateField.getText().trim();
        String costText = costField.getText().trim();

        // Validate inputs
        if (name.isEmpty() || quantityText.isEmpty() || expiryDate.isEmpty() || costText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required.", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // Validate quantity
            int quantity = Integer.parseInt(quantityText);
            if (quantity <= 0) {
                JOptionPane.showMessageDialog(this, "Quantity must be a positive number.", "Input Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Validate expiry date
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            dateFormat.setLenient(false); // Strict date parsing
            Date parsedDate = dateFormat.parse(expiryDate);

            if (parsedDate.before(new Date())) {
                JOptionPane.showMessageDialog(this, "Expiry date cannot be in the past.", "Input Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Validate cost
            double cost = Double.parseDouble(costText);
            if (cost <= 0) {
                JOptionPane.showMessageDialog(this, "Cost per unit must be a positive number.", "Input Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Database operation
            if (conn != null) {
                String query = "INSERT INTO Medicine (name, stock_quantity, expiry_date, cost_per_unit) VALUES (?, ?, ?, ?) " +
                               "ON DUPLICATE KEY UPDATE stock_quantity = VALUES(stock_quantity), expiry_date = VALUES(expiry_date), cost_per_unit = VALUES(cost_per_unit)";
                PreparedStatement stmt = conn.prepareStatement(query);

                stmt.setString(1, name);
                stmt.setInt(2, quantity);
                stmt.setString(3, expiryDate);
                stmt.setDouble(4, cost);

                int rowsAffected = stmt.executeUpdate();

                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this, "Medicine added/updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to add/update medicine.", "Error", JOptionPane.ERROR_MESSAGE);
                }

                stmt.close();
            } else {
                JOptionPane.showMessageDialog(this, "Database connection failed.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Quantity and Cost must be valid numbers.", "Input Error", JOptionPane.WARNING_MESSAGE);
        } catch (ParseException ex) {
            JOptionPane.showMessageDialog(this, "Invalid date format. Please use YYYY-MM-DD.", "Input Error", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
