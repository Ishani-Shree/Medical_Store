import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.border.Border;

public class StockInfo extends JFrame {
    private JTextField searchField;
    private JTable medicineTable;
    private DefaultTableModel tableModel;
    private Connection conn; // Single connection for the class

    public StockInfo(Connection conn) {
        this.conn = conn;

        // Frame settings
        setTitle("Stock Info");
        setSize(800, 600);
        setLayout(new BorderLayout());
        setLocationRelativeTo(null); // Center the window
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Create a hidden dummy panel to shift focus
        JPanel dummyPanel = new JPanel();
        dummyPanel.setFocusable(true); // Make it focusable
        add(dummyPanel, BorderLayout.WEST); // Add it somewhere unobtrusive

        // Search Field with Rounded Corners and Placeholder Text
        searchField = new JTextField() {
            private String placeholder = "Search";

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

        searchField.setFont(new Font("Arial", Font.PLAIN, 16));
        searchField.setOpaque(false); // Transparent to allow custom background

        // Add a key listener to the search bar to filter medicines dynamically
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String searchText = searchField.getText();
                loadMedicineData(searchText.equals("Search") ? "" : searchText);
            }
        });

        // Center the search field
        JPanel wrapperPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 20)); // Center alignment
        wrapperPanel.setOpaque(false); // Transparent wrapper panel
        wrapperPanel.add(searchField);
        searchField.setPreferredSize(new Dimension(400, 40)); // Adjusted width and height

        add(wrapperPanel, BorderLayout.NORTH);

        // Table for medicine data
        String[] columnNames = {"Medicine Name", "Stock Quantity", "Expiry Date"};
        tableModel = new DefaultTableModel(columnNames, 0);
        medicineTable = new JTable(tableModel) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table cells non-editable
            }
        };
        medicineTable.setFont(new Font("Arial", Font.PLAIN, 16));
        medicineTable.setRowHeight(24);

        JTableHeader tableHeader = medicineTable.getTableHeader();
        tableHeader.setFont(new Font("Arial", Font.BOLD, 18)); // Increase header text size
        tableHeader.setBackground(new Color(200, 200, 200)); // Header background color
        tableHeader.setReorderingAllowed(false); // Disable column reordering

        // Center-align table cells
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        medicineTable.setDefaultRenderer(Object.class, centerRenderer);

        // Fetch and display initial data
        loadMedicineData("");

        // Add table inside a scroll pane
        JScrollPane scrollPane = new JScrollPane(medicineTable);

        // Wrap the scroll pane in a panel with padding
        JPanel tableWrapperPanel = new JPanel(new BorderLayout());
        tableWrapperPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 10)); // Add padding
        tableWrapperPanel.add(scrollPane, BorderLayout.CENTER);

        add(tableWrapperPanel, BorderLayout.CENTER); // Add the wrapper panel to the frame

        // Set initial focus to dummy panel
        SwingUtilities.invokeLater(() -> dummyPanel.requestFocusInWindow());

        setVisible(true);
    }

    // Load data into the table from the database, filtering by name if searchText is provided
    private void loadMedicineData(String searchText) {
        try {
            if (conn != null) {
                String query = "SELECT name, stock_quantity, expiry_date FROM Medicine";
                if (!searchText.isEmpty()) {
                    query += " WHERE name LIKE ? ORDER BY name ASC";
                } else {
                    query += " ORDER BY name ASC";
                }

                PreparedStatement stmt = conn.prepareStatement(query);

                if (!searchText.isEmpty()) {
                    stmt.setString(1, searchText + "%");
                }

                ResultSet rs = stmt.executeQuery();

                // Clear the table before adding new data
                tableModel.setRowCount(0);

                while (rs.next()) {
                    String name = rs.getString("name");
                    int stockQuantity = rs.getInt("stock_quantity");
                    String expiryDate = rs.getString("expiry_date");

                    // Add the row to the table
                    tableModel.addRow(new Object[]{name, stockQuantity, expiryDate});
                }

                rs.close();
                stmt.close();
            } else {
                JOptionPane.showMessageDialog(this, "Database connection failed.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
