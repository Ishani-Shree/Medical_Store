import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;

public class DoctorInfo extends JFrame {

    private JTextField  nameField;
    private JTable      table;
    private DefaultTableModel tableModel;
    private JButton     addButton, deleteButton;
    private Connection  conn;

    public DoctorInfo(Connection conn) {
        this.conn = conn;

        setTitle("Doctor Management");
        setSize(620, 520);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(0, 0));

        // ---- Top: title bar ----
        JPanel titleBar = new JPanel();
        titleBar.setBackground(new Color(40, 100, 200));
        titleBar.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));
        JLabel title = new JLabel("Doctor Management");
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setForeground(Color.WHITE);
        titleBar.add(title);
        add(titleBar, BorderLayout.NORTH);

        // ---- Center: table ----
        String[] cols = {"Doctor ID", "Name"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setFont(new Font("Arial", Font.PLAIN, 15));
        table.setRowHeight(28);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setGridColor(new Color(220, 220, 220));
        table.setShowGrid(true);

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Arial", Font.BOLD, 15));
        header.setBackground(new Color(230, 240, 255));
        header.setReorderingAllowed(false);

        // Center-align both columns
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(center);
        table.getColumnModel().getColumn(0).setPreferredWidth(100);
        table.getColumnModel().getColumn(1).setCellRenderer(center);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        add(scroll, BorderLayout.CENTER);

        // ---- Bottom: add + delete ----
        JPanel bottom = new JPanel(new BorderLayout(12, 0));
        bottom.setBorder(BorderFactory.createEmptyBorder(10, 16, 16, 16));
        bottom.setBackground(new Color(245, 247, 252));

        // Name input row
        JPanel inputRow = new JPanel(new BorderLayout(8, 0));
        inputRow.setOpaque(false);

        JLabel nameLabel = new JLabel("Doctor Name:");
        nameLabel.setFont(new Font("Arial", Font.BOLD, 15));
        inputRow.add(nameLabel, BorderLayout.WEST);

        nameField = new JTextField();
        nameField.setFont(new Font("Arial", Font.PLAIN, 15));
        nameField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(180, 200, 230)),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        inputRow.add(nameField, BorderLayout.CENTER);

        addButton = styledButton("Add Doctor", new Color(50, 150, 250));
        inputRow.add(addButton, BorderLayout.EAST);

        bottom.add(inputRow, BorderLayout.CENTER);

        deleteButton = styledButton("Delete Selected", new Color(210, 60, 60));
        bottom.add(deleteButton, BorderLayout.EAST);

        add(bottom, BorderLayout.SOUTH);

        // ---- Wire up actions ----
        addButton.addActionListener(e -> addDoctor());
        deleteButton.addActionListener(e -> deleteDoctor());
        nameField.addActionListener(e -> addDoctor()); // Enter key adds

        loadDoctors();
        setVisible(true);
    }

    private void loadDoctors() {
        tableModel.setRowCount(0);
        try {
            PreparedStatement st = conn.prepareStatement(
                "SELECT doctor_id, name FROM Doctor ORDER BY doctor_id");
            ResultSet rs = st.executeQuery();
            while (rs.next())
                tableModel.addRow(new Object[]{rs.getInt("doctor_id"), rs.getString("name")});
            rs.close(); st.close();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "DB error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addDoctor() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a doctor name.", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            PreparedStatement st = conn.prepareStatement("INSERT INTO Doctor (name) VALUES (?)");
            st.setString(1, name);
            st.executeUpdate();
            st.close();
            nameField.setText("");
            loadDoctors();
            // Select the last row so user sees the new entry
            int last = table.getRowCount() - 1;
            if (last >= 0) {
                table.setRowSelectionInterval(last, last);
                table.scrollRectToVisible(table.getCellRect(last, 0, true));
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "DB error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteDoctor() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a doctor to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int id   = (int) tableModel.getValueAt(row, 0);
        String nm = (String) tableModel.getValueAt(row, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
            "Delete Dr. " + nm + " (ID: " + id + ")?",
            "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            PreparedStatement st = conn.prepareStatement("DELETE FROM Doctor WHERE doctor_id = ?");
            st.setInt(1, id);
            st.executeUpdate();
            st.close();
            loadDoctors();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Cannot delete: doctor may have existing prescriptions.\n" + ex.getMessage(),
                "Delete Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JButton styledButton(String text, Color bg) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color c = getModel().isPressed()  ? bg.darker()
                        : getModel().isRollover() ? bg.brighter()
                                                  : bg;
                g2.setColor(c);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Arial", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(150, 36));
        return btn;
    }
}
