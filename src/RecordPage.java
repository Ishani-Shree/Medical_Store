import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RecordPage extends JFrame {
    private static final int FRAME_WIDTH = 800;
    private static final int FRAME_HEIGHT = 600;

    private JTable recordTable;
    private DefaultTableModel tableModel;
    private Connection conn;

    public RecordPage(Connection conn) {
        this.conn = conn;

        // Frame settings
        setTitle("Latest Records");
        setSize(FRAME_WIDTH, FRAME_HEIGHT);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Background Panel
        JPanel backgroundPanel = new JPanel();
        backgroundPanel.setLayout(new BorderLayout());
        backgroundPanel.setBackground(Color.LIGHT_GRAY);
        add(backgroundPanel);

        // Table and Table Model
        String[] columnNames = {
            "Patient Name",
            "Phone Number",
            "Doctor Name",
            "Medication",
            "Total Bill",
            "Date/Time"
        };

        tableModel = new DefaultTableModel(columnNames, 0);
        recordTable = new JTable(tableModel) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table cells non-editable
            }

            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component component = super.prepareRenderer(renderer, row, column);

                if (column == 3) { // Medication column
                    String text = (String) getValueAt(row, column);

                    if (text != null) {
                        int lineCount = text.split("\n").length;
                        int rowHeight = Math.max(getRowHeight(), lineCount * getFontMetrics(getFont()).getHeight());
                        setRowHeight(row, rowHeight);
                    }
                }

                return component;
            }
        };

        // Custom Renderer for the Medication Column
        recordTable.getColumnModel().getColumn(3).setCellRenderer(new MedicationCellRenderer());

        recordTable.setFont(new Font("Arial", Font.PLAIN, 14));
        recordTable.setRowHeight(30);
        recordTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 16));
        recordTable.getTableHeader().setBackground(new Color(200, 200, 200));
        recordTable.setFillsViewportHeight(true);

        // Scroll Pane for Table
        JScrollPane scrollPane = new JScrollPane(recordTable);
        backgroundPanel.add(scrollPane, BorderLayout.CENTER);

        // Fetch and Display Records
        fetchLatestRecords();

        setVisible(true);
    }

    private void fetchLatestRecords() {
        String query = "SELECT " +
               "p.name AS patient_name, " +
               "p.phone AS phone_number, " +
               "d.name AS doctor_name, " +
               "pr.medication AS medication, " +
               "b.total_amount AS total_bill, " +
               "b.date_issued AS date_time " +
               "FROM Bill b " +
               "JOIN Prescription pr ON b.prescription_id = pr.prescription_id " +
               "JOIN Patient p ON b.patient_id = p.patient_id " +
               "JOIN Doctor d ON pr.doctor_id = d.doctor_id " +
               "ORDER BY b.date_issued DESC " +
               "LIMIT 10";

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            ResultSet rs = stmt.executeQuery();

            // Clear the table before adding new data
            tableModel.setRowCount(0);

            while (rs.next()) {
                String patientName = rs.getString("patient_name");
                String phoneNumber = rs.getString("phone_number");
                String doctorName = rs.getString("doctor_name");
                String medication = rs.getString("medication").replace(";", "\n"); // Replace ';' with '\n'
                String totalBill = String.format("%.2f", rs.getDouble("total_bill"));
                String dateTime = rs.getString("date_time");

                // Add row to the table
                tableModel.addRow(new Object[]{patientName, phoneNumber, doctorName, medication, totalBill, dateTime});
            }

            rs.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Custom Cell Renderer for the Medication Column
    private class MedicationCellRenderer extends DefaultTableCellRenderer {
        @Override
        public void setValue(Object value) {
            if (value instanceof String) {
                setText("<html>" + ((String) value).replace("\n", "<br>") + "</html>");
            } else {
                super.setValue(value);
            }
        }
    }
}
