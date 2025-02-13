import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class BillPopup extends JFrame {
    private Connection conn;
    private int prescriptionId;

    public BillPopup(Connection conn, int prescriptionId) {
        this.conn = conn;
        this.prescriptionId = prescriptionId;

        setTitle("Bill");
        setSize(600, 500);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        try {
            // Fetch prescription and patient details
            String query = "SELECT p.name AS patient_name, p.phone, d.name AS doctor_name, pr.medication, pr.patient_id " +
                           "FROM Prescription pr " +
                           "JOIN Patient p ON pr.patient_id = p.patient_id " +
                           "JOIN Doctor d ON pr.doctor_id = d.doctor_id " +
                           "WHERE pr.prescription_id = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, prescriptionId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String patientName = rs.getString("patient_name");
                String phone = rs.getString("phone");
                String doctorName = rs.getString("doctor_name");
                String medication = rs.getString("medication");
                int patientId = rs.getInt("patient_id");

                // Calculate total amount and update stock
                double totalAmount = handleBillAndStockUpdate(medication);

                // Insert into Bill table
                if (totalAmount > 0) {
                    saveBillToDatabase(prescriptionId, patientId, totalAmount);
                } else {
                    JOptionPane.showMessageDialog(this, "Bill generation failed.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Create bill details panel
                JPanel detailsPanel = new JPanel(new GridLayout(0, 1, 10, 10));
                detailsPanel.add(new JLabel("Patient Name: " + patientName));
                detailsPanel.add(new JLabel("Phone Number: " + phone));
                detailsPanel.add(new JLabel("Doctor Name: " + doctorName));
                detailsPanel.add(new JLabel("Medications: " + medication));
                detailsPanel.add(new JLabel("Total Amount: $" + totalAmount));

                // Add to frame
                add(detailsPanel, BorderLayout.CENTER);

                // Add a print button
                JButton printButton = new JButton("Print");
                printButton.addActionListener(e -> JOptionPane.showMessageDialog(this, "Printing Bill..."));
                add(printButton, BorderLayout.SOUTH);
            } else {
                JOptionPane.showMessageDialog(this, "Prescription not found.", "Error", JOptionPane.ERROR_MESSAGE);
            }

            rs.close();
            stmt.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }

        setVisible(true);
    }

    private double handleBillAndStockUpdate(String medication) throws SQLException {
        double totalAmount = 0.0;

        String[] medicines = medication.split("; ");
        conn.setAutoCommit(false); // Start transaction

        try {
            for (String med : medicines) {
                String[] parts = med.split(" \\("); // Extract medicine name and quantity
                String medicineName = parts[0];
                int quantity = Integer.parseInt(parts[1].replace(")", ""));

                // Fetch medicine details
                String query = "SELECT stock_quantity, cost_per_unit FROM Medicine WHERE name = ?";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setString(1, medicineName);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    int stockQuantity = rs.getInt("stock_quantity");
                    double costPerUnit = rs.getDouble("cost_per_unit");

                    if (quantity > stockQuantity) {
                        conn.rollback(); // Rollback the transaction
                        JOptionPane.showMessageDialog(this,
                                "Insufficient stock for " + medicineName + ". Only " + stockQuantity + " available.",
                                "Stock Error", JOptionPane.WARNING_MESSAGE);
                        return 0.0;
                    }

                    // Update stock
                    String updateStockQuery = "UPDATE Medicine SET stock_quantity = stock_quantity - ? WHERE name = ?";
                    PreparedStatement updateStmt = conn.prepareStatement(updateStockQuery);
                    updateStmt.setInt(1, quantity);
                    updateStmt.setString(2, medicineName);
                    updateStmt.executeUpdate();
                    updateStmt.close();

                    // Calculate total amount
                    totalAmount += quantity * costPerUnit;
                } else {
                    conn.rollback(); // Rollback the transaction
                    JOptionPane.showMessageDialog(this, "Medicine " + medicineName + " not found.", "Error", JOptionPane.ERROR_MESSAGE);
                    return 0.0;
                }

                rs.close();
                stmt.close();
            }

            conn.commit(); // Commit transaction
        } catch (SQLException ex) {
            conn.rollback(); // Rollback the transaction
            throw ex;
        } finally {
            conn.setAutoCommit(true); // Reset auto-commit
        }

        return totalAmount;
    }

    private void saveBillToDatabase(int prescriptionId, int patientId, double totalAmount) throws SQLException {
        String billQuery = "INSERT INTO Bill (prescription_id, patient_id, total_amount) VALUES (?, ?, ?)";
        PreparedStatement stmt = conn.prepareStatement(billQuery);
        stmt.setInt(1, prescriptionId);
        stmt.setInt(2, patientId);
        stmt.setDouble(3, totalAmount);

        int rowsInserted = stmt.executeUpdate();

        if (rowsInserted > 0) {
            JOptionPane.showMessageDialog(this, "Bill generated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Failed to save bill.", "Error", JOptionPane.ERROR_MESSAGE);
        }

        stmt.close();
    }
}
