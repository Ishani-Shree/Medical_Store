import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.ArrayList;

public class IssueMedicine extends JFrame {
    private static final int FRAME_WIDTH  = 650;
    private static final int FRAME_HEIGHT = 800;

    private JTextField        mobileField, doctorIdField, quantityField;
    private JComboBox<String> medicineDropdown;
    private JTextArea         medicationListArea;
    private JButton           addMedicineButton, resetButton, generateBillButton;
    private JLabel            NameLabel;
    private Connection        conn;
    private ArrayList<String> medications;

    // Each dropdown is just a window + model + list kept together
    private final DropSet mobileDrop = new DropSet();
    private final DropSet doctorDrop = new DropSet();
    private boolean ignoreMobile = false;
    private boolean ignoreDoctor = false;

    private static class DropSet {
        JWindow              win;
        DefaultListModel<String> model = new DefaultListModel<>();
        JList<String>        list  = new JList<>(model);
    }

    public IssueMedicine(Connection conn) {
        this.conn       = conn;
        this.medications = new ArrayList<>();

        setTitle("Issue Medicine");
        setSize(FRAME_WIDTH, FRAME_HEIGHT);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel bg = new JPanel(null);
        bg.setBackground(Color.LIGHT_GRAY);
        add(bg);

        int pad    = 20;
        int labelW = FRAME_WIDTH / 3;
        int fieldW = FRAME_WIDTH - (labelW + 3 * pad);
        int rowH   = 40;
        int top    = 30;

        // Mobile field
        addLabel(bg, "Mobile No.:", pad, top, labelW, rowH);
        mobileField = roundedField();
        mobileField.setBounds(labelW, top, fieldW, rowH);
        bg.add(mobileField);

        // Patient name display
        addLabel(bg, "Patient Name:", pad, top + rowH + pad, labelW, rowH);
        NameLabel = new JLabel("Not Found");
        NameLabel.setFont(new Font("Arial", Font.BOLD, 16));
        NameLabel.setBounds(labelW, top + rowH + pad, FRAME_WIDTH - 2 * pad, rowH);
        bg.add(NameLabel);

        // Doctor ID field
        addLabel(bg, "Doctor ID:", pad, top + 2 * (rowH + pad), labelW, rowH);
        doctorIdField = roundedField();
        doctorIdField.setBounds(labelW, top + 2 * (rowH + pad), fieldW, rowH);
        bg.add(doctorIdField);

        // Medicine dropdown
        addLabel(bg, "Medicine:", pad, top + 3 * (rowH + pad), labelW, rowH);
        medicineDropdown = new JComboBox<>();
        populateMedicineDropdown();
        medicineDropdown.setFont(new Font("Arial", Font.PLAIN, 16));
        medicineDropdown.setBounds(labelW, top + 3 * (rowH + pad), fieldW, rowH);
        bg.add(medicineDropdown);

        // Quantity field
        addLabel(bg, "Quantity:", pad, top + 4 * (rowH + pad), labelW, rowH);
        quantityField = roundedField();
        quantityField.setBounds(labelW, top + 4 * (rowH + pad), fieldW, rowH);
        bg.add(quantityField);

        // Add Medicine button
        addMedicineButton = new JButton("Add Medicine");
        addMedicineButton.setFont(new Font("Arial", Font.BOLD, 16));
        addMedicineButton.setBounds(labelW, top + 5 * (rowH + pad), fieldW / 2, rowH);
        addMedicineButton.setEnabled(false);
        bg.add(addMedicineButton);

        // Medication list
        medicationListArea = new JTextArea();
        medicationListArea.setEditable(false);
        medicationListArea.setFont(new Font("Arial", Font.PLAIN, 16));
        JScrollPane scroll = new JScrollPane(medicationListArea);
        scroll.setBounds(pad, top + 6 * (rowH + pad), FRAME_WIDTH - 3 * pad, 200);
        bg.add(scroll);

        // Buttons
        resetButton = new JButton("Reset");
        resetButton.setFont(new Font("Arial", Font.BOLD, 16));
        resetButton.setBounds(3 * pad, FRAME_HEIGHT - 3 * rowH, 120, rowH);
        bg.add(resetButton);

        generateBillButton = new JButton("Generate Bill");
        generateBillButton.setFont(new Font("Arial", Font.BOLD, 16));
        generateBillButton.setBounds(FRAME_WIDTH / 2 + 77, FRAME_HEIGHT - 3 * rowH, fieldW / 2, rowH);
        bg.add(generateBillButton);

        // Build dropdown windows
        initDropSet(mobileDrop);
        initDropSet(doctorDrop);

        // Mobile selection → fill field + fetch patient
        mobileDrop.list.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                String sel = mobileDrop.list.getSelectedValue();
                if (sel != null) {
                    ignoreMobile = true;
                    mobileField.setText(sel);
                    ignoreMobile = false;
                    mobileDrop.win.setVisible(false);
                    fetchPatientName();
                }
            }
        });

        // Doctor selection → fill field with ID only
        doctorDrop.list.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                String sel = doctorDrop.list.getSelectedValue();
                if (sel != null) {
                    ignoreDoctor = true;
                    doctorIdField.setText(sel.split(" - ")[0].trim());
                    ignoreDoctor = false;
                    doctorDrop.win.setVisible(false);
                }
            }
        });

        // DocumentListeners
        mobileField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e)  { if (!ignoreMobile) SwingUtilities.invokeLater(() -> refreshMobile()); }
            @Override public void removeUpdate(DocumentEvent e)  { if (!ignoreMobile) SwingUtilities.invokeLater(() -> refreshMobile()); }
            @Override public void changedUpdate(DocumentEvent e) {}
        });

        doctorIdField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e)  { if (!ignoreDoctor) SwingUtilities.invokeLater(() -> refreshDoctor()); }
            @Override public void removeUpdate(DocumentEvent e)  { if (!ignoreDoctor) SwingUtilities.invokeLater(() -> refreshDoctor()); }
            @Override public void changedUpdate(DocumentEvent e) {}
        });

        // Enter on mobile triggers name lookup
        mobileField.addActionListener(e -> fetchPatientName());

        // Hide on frame move/deactivate
        addComponentListener(new ComponentAdapter() {
            @Override public void componentMoved(ComponentEvent e)   { hideAll(); }
            @Override public void componentResized(ComponentEvent e) { hideAll(); }
        });
        addWindowListener(new WindowAdapter() {
            @Override public void windowDeactivated(WindowEvent e) { hideAll(); }
        });

        addMedicineButton.addActionListener(e -> handleAddMedicine());
        resetButton.addActionListener(e -> resetFields());
        generateBillButton.addActionListener(e -> handleGenerateBill());

        setVisible(true);
    }

    // ---- Dropdown infrastructure ----

    private void initDropSet(DropSet ds) {
        ds.list.setFont(new Font("Arial", Font.PLAIN, 15));
        ds.list.setFixedCellHeight(32);
        ds.list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ds.list.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> l, Object v,
                    int i, boolean sel, boolean focus) {
                JLabel lbl = (JLabel) super.getListCellRendererComponent(l, v, i, sel, focus);
                lbl.setBackground(sel ? new Color(50, 150, 250) : Color.WHITE);
                lbl.setForeground(sel ? Color.WHITE : Color.DARK_GRAY);
                lbl.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                return lbl;
            }
        });
        JScrollPane sp = new JScrollPane(ds.list,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        sp.setBorder(BorderFactory.createLineBorder(new Color(150, 180, 230)));
        ds.win = new JWindow(this);
        ds.win.add(sp);
        ds.win.setFocusableWindowState(false);
    }

    private void showDrop(DropSet ds, JTextField anchor) {
        if (ds.model.isEmpty()) { ds.win.setVisible(false); return; }
        Point p = anchor.getLocationOnScreen();
        int h   = Math.min(ds.model.size() * 32 + 4, 164);
        ds.win.setBounds(p.x, p.y + anchor.getHeight(), anchor.getWidth(), h);
        ds.win.setVisible(true);
        ds.win.toFront();
    }

    private void refreshMobile() {
        String typed = mobileField.getText().trim();
        mobileDrop.model.clear();
        if (typed.isEmpty()) { mobileDrop.win.setVisible(false); return; }
        try {
            PreparedStatement st = conn.prepareStatement(
                "SELECT phone FROM Patient WHERE phone LIKE ? ORDER BY phone LIMIT 8");
            st.setString(1, typed + "%");
            ResultSet rs = st.executeQuery();
            while (rs.next()) mobileDrop.model.addElement(rs.getString("phone"));
            rs.close(); st.close();
        } catch (SQLException ex) { ex.printStackTrace(); }
        showDrop(mobileDrop, mobileField);
    }

    private void refreshDoctor() {
        String typed = doctorIdField.getText().trim();
        doctorDrop.model.clear();
        if (typed.isEmpty()) { doctorDrop.win.setVisible(false); return; }
        try {
            PreparedStatement st = conn.prepareStatement(
                "SELECT doctor_id, name FROM Doctor WHERE CAST(doctor_id AS CHAR) LIKE ? ORDER BY doctor_id LIMIT 8");
            st.setString(1, typed + "%");
            ResultSet rs = st.executeQuery();
            while (rs.next())
                doctorDrop.model.addElement(rs.getInt("doctor_id") + " - " + rs.getString("name"));
            rs.close(); st.close();
        } catch (SQLException ex) { ex.printStackTrace(); }
        showDrop(doctorDrop, doctorIdField);
    }

    private void hideAll() {
        if (mobileDrop.win != null) mobileDrop.win.setVisible(false);
        if (doctorDrop.win != null) doctorDrop.win.setVisible(false);
    }

    // ---- Business logic ----

    private void fetchPatientName() {
        String mob = mobileField.getText().trim();
        if (mob.length() == 10 && mob.matches("\\d{10}")) {
            try {
                PreparedStatement st = conn.prepareStatement("SELECT name FROM Patient WHERE phone = ?");
                st.setString(1, mob);
                ResultSet rs = st.executeQuery();
                if (rs.next()) {
                    NameLabel.setText(rs.getString("name"));
                    addMedicineButton.setEnabled(true);
                } else {
                    NameLabel.setText("Not Found");
                    addMedicineButton.setEnabled(false);
                }
                rs.close(); st.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
                addMedicineButton.setEnabled(false);
            }
        } else {
            NameLabel.setText("Not Found");
            addMedicineButton.setEnabled(false);
        }
    }

    private boolean validateDoctorId() {
        String id = doctorIdField.getText().trim();
        if (id.isEmpty() || !id.matches("\\d+")) {
            JOptionPane.showMessageDialog(this, "Invalid Doctor ID.", "Input Error", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        try {
            PreparedStatement st = conn.prepareStatement("SELECT name FROM Doctor WHERE doctor_id = ?");
            st.setInt(1, Integer.parseInt(id));
            ResultSet rs = st.executeQuery();
            boolean found = rs.next();
            rs.close(); st.close();
            if (!found) JOptionPane.showMessageDialog(this, "Doctor ID not found.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return found;
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "DB error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private void handleAddMedicine() {
        if (!validateDoctorId()) return;
        String med = (String) medicineDropdown.getSelectedItem();
        String qty = quantityField.getText().trim();
        if (med == null || med.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a medicine.", "Input Error", JOptionPane.WARNING_MESSAGE); return;
        }
        if (qty.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a quantity.", "Input Error", JOptionPane.WARNING_MESSAGE); return;
        }
        try {
            int quantity = Integer.parseInt(qty);
            if (quantity <= 0) {
                JOptionPane.showMessageDialog(this, "Quantity must be positive.", "Input Error", JOptionPane.WARNING_MESSAGE); return;
            }
            PreparedStatement st = conn.prepareStatement("SELECT stock_quantity FROM Medicine WHERE name = ?");
            st.setString(1, med);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                int stock = rs.getInt("stock_quantity");
                if (quantity > stock) {
                    JOptionPane.showMessageDialog(this,
                        "Insufficient stock for " + med + ". Only " + stock + " available.",
                        "Stock Error", JOptionPane.WARNING_MESSAGE);
                    rs.close(); st.close(); return;
                }
                medications.add(med + " (" + quantity + ")");
                updateMedicationListDisplay();
            }
            rs.close(); st.close();
            quantityField.setText("");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid quantity.", "Input Error", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "DB error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleGenerateBill() {
        String mob = mobileField.getText().trim();
        String did = doctorIdField.getText().trim();
        if (mob.isEmpty() || !mob.matches("\\d{10}")) {
            JOptionPane.showMessageDialog(this, "Invalid mobile number.", "Input Error", JOptionPane.WARNING_MESSAGE); return;
        }
        if (did.isEmpty() || !did.matches("\\d+")) {
            JOptionPane.showMessageDialog(this, "Invalid Doctor ID.", "Input Error", JOptionPane.WARNING_MESSAGE); return;
        }
        if (medications.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No medicines added.", "Input Error", JOptionPane.WARNING_MESSAGE); return;
        }
        try {
            PreparedStatement pt = conn.prepareStatement("SELECT patient_id FROM Patient WHERE phone = ?");
            pt.setString(1, mob);
            ResultSet pr = pt.executeQuery();
            if (!pr.next()) {
                JOptionPane.showMessageDialog(this, "Patient not found.", "Input Error", JOptionPane.WARNING_MESSAGE);
                pr.close(); pt.close(); return;
            }
            int patientId = pr.getInt("patient_id");
            pr.close(); pt.close();

            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO Prescription (doctor_id, patient_id, medication) VALUES (?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, Integer.parseInt(did));
            ps.setInt(2, patientId);
            ps.setString(3, String.join("; ", medications));
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                new BillPopup(conn, keys.getInt(1));
                keys.close(); ps.close();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "DB error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateMedicationListDisplay() {
        StringBuilder sb = new StringBuilder();
        for (String m : medications) sb.append(m).append("\n");
        medicationListArea.setText(sb.toString());
    }

    private void resetFields() {
        mobileField.setText("");
        doctorIdField.setText("");
        quantityField.setText("");
        medications.clear();
        updateMedicationListDisplay();
        NameLabel.setText("Not Found");
        addMedicineButton.setEnabled(false);
    }

    private void populateMedicineDropdown() {
        try {
            PreparedStatement st = conn.prepareStatement("SELECT name FROM Medicine");
            ResultSet rs = st.executeQuery();
            while (rs.next()) medicineDropdown.addItem(rs.getString("name"));
            rs.close(); st.close();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "DB error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ---- UI helpers ----

    private void addLabel(JPanel p, String text, int x, int y, int w, int h) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Arial", Font.BOLD, 16));
        l.setBounds(x, y, w, h);
        p.add(l);
    }

    private JTextField roundedField() {
        return new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(245, 245, 245));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.setColor(new Color(200, 200, 200));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                super.paintComponent(g2);
                g2.dispose();
            }
            @Override public void setBorder(Border b) {}
            @Override public Insets getInsets() { return new Insets(5, 10, 5, 10); }
        };
    }
}
