import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class PatientDetails extends JFrame {
    private static final int FRAME_WIDTH  = 600;
    private static final int FRAME_HEIGHT = 690;

    private JTextField mobileField;
    private JTextField nameField, ageField, genderField, bloodGroupField, addressField;
    private JButton    searchButton;

    private JWindow              dropdownWindow;
    private DefaultListModel<String> listModel;
    private JList<String>        dropdownList;
    private boolean              ignoreChanges = false;

    private Connection conn;

    public PatientDetails(Connection conn) {
        this.conn = conn;

        setTitle("Patient Details");
        setSize(FRAME_WIDTH, FRAME_HEIGHT);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel bg = new JPanel(null);
        bg.setBackground(Color.LIGHT_GRAY);
        add(bg);

        int pad        = 20;
        int leftPad    = 40;
        int labelW     = FRAME_WIDTH / 4;
        int fieldW     = FRAME_WIDTH / 2;
        int rowH       = 40;
        int topMargin  = 30;

        // Mobile label
        JLabel mobileLabel = new JLabel("Mobile No:");
        mobileLabel.setFont(new Font("Arial", Font.BOLD, 18));
        mobileLabel.setBounds(leftPad, topMargin, labelW, rowH);
        bg.add(mobileLabel);

        // Mobile field
        mobileField = buildRoundedField("Enter Mobile Number...");
        mobileField.setBounds(labelW, topMargin, fieldW, rowH);
        bg.add(mobileField);

        // Search button
        searchButton = new JButton("Search");
        searchButton.setFont(new Font("Arial", Font.BOLD, 16));
        searchButton.setBounds((FRAME_WIDTH - 120) / 2, topMargin + rowH + pad, 120, rowH);
        searchButton.addActionListener(e -> handleSearch());
        bg.add(searchButton);

        // Info fields
        String[] labelTxts = {"Name:", "Age:", "Gender:", "Blood Group:", "Address:"};
        JTextField[] infoFields = new JTextField[5];
        for (int i = 0; i < labelTxts.length; i++) {
            JLabel lbl = new JLabel(labelTxts[i]);
            lbl.setFont(new Font("Arial", Font.BOLD, 16));
            lbl.setBounds(leftPad, topMargin + (3 + i) * (rowH + pad), labelW, rowH);
            bg.add(lbl);

            infoFields[i] = new JTextField();
            infoFields[i].setFont(new Font("Arial", Font.PLAIN, 16));
            infoFields[i].setBounds(labelW, topMargin + (3 + i) * (rowH + pad), fieldW, rowH);
            infoFields[i].setEditable(false);
            bg.add(infoFields[i]);
        }
        nameField = infoFields[0]; ageField = infoFields[1];
        genderField = infoFields[2]; bloodGroupField = infoFields[3]; addressField = infoFields[4];

        // Build dropdown window
        buildDropdown();

        // Wire up live search — check ignoreChanges BEFORE scheduling so the flag
        // is still true when the invokeLater would have run after setText() clears it
        mobileField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e)  {
                if (!ignoreChanges) SwingUtilities.invokeLater(() -> refreshDropdown());
            }
            @Override public void removeUpdate(DocumentEvent e)  {
                if (!ignoreChanges) SwingUtilities.invokeLater(() -> refreshDropdown());
            }
            @Override public void changedUpdate(DocumentEvent e) {}
        });

        // Enter key triggers search
        mobileField.addActionListener(e -> handleSearch());

        // Hide dropdown when frame moves/resizes
        addComponentListener(new ComponentAdapter() {
            @Override public void componentMoved(ComponentEvent e)   { hideDropdown(); }
            @Override public void componentResized(ComponentEvent e) { hideDropdown(); }
        });

        addWindowListener(new WindowAdapter() {
            @Override public void windowDeactivated(WindowEvent e) { hideDropdown(); }
        });

        setVisible(true);
    }

    private void buildDropdown() {
        listModel    = new DefaultListModel<>();
        dropdownList = new JList<>(listModel);
        dropdownList.setFont(new Font("Arial", Font.PLAIN, 15));
        dropdownList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        dropdownList.setBackground(Color.WHITE);
        dropdownList.setFixedCellHeight(32);
        dropdownList.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));

        // Hover highlight
        dropdownList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (isSelected) {
                    lbl.setBackground(new Color(50, 150, 250));
                    lbl.setForeground(Color.WHITE);
                } else {
                    lbl.setBackground(Color.WHITE);
                    lbl.setForeground(Color.DARK_GRAY);
                }
                return lbl;
            }
        });

        dropdownList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                String selected = dropdownList.getSelectedValue();
                if (selected != null) {
                    ignoreChanges = true;
                    mobileField.setText(selected);
                    ignoreChanges = false;
                    hideDropdown();
                    handleSearch();
                }
            }
        });

        JScrollPane scroll = new JScrollPane(dropdownList,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(150, 180, 230)));

        dropdownWindow = new JWindow(this);
        dropdownWindow.add(scroll);
        dropdownWindow.setFocusableWindowState(false); // keeps focus on mobileField
    }

    private void refreshDropdown() {
        if (mobileField.getDocument().getProperty("noRefresh") != null) return;

        String typed = mobileField.getText().trim();
        listModel.clear();

        if (typed.isEmpty()) { hideDropdown(); return; }

        try {
            PreparedStatement st = conn.prepareStatement(
                "SELECT phone FROM Patient WHERE phone LIKE ? ORDER BY phone LIMIT 8");
            st.setString(1, typed + "%");
            ResultSet rs = st.executeQuery();
            while (rs.next()) listModel.addElement(rs.getString("phone"));
            rs.close(); st.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        if (listModel.isEmpty()) { hideDropdown(); return; }

        // Position window directly below the mobile field
        Point fieldLoc = mobileField.getLocationOnScreen();
        int popH = Math.min(listModel.size() * 32 + 6, 166);
        dropdownWindow.setBounds(fieldLoc.x, fieldLoc.y + mobileField.getHeight(),
                                 mobileField.getWidth(), popH);
        dropdownWindow.setVisible(true);
        dropdownWindow.toFront();
    }

    private void hideDropdown() {
        if (dropdownWindow != null) dropdownWindow.setVisible(false);
    }

    private void handleSearch() {
        hideDropdown();
        String phone = mobileField.getText().trim();
        if (phone.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a mobile number.", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            PreparedStatement st = conn.prepareStatement(
                "SELECT name, age, gender, blood_group, address FROM Patient WHERE phone = ?");
            st.setString(1, phone);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                nameField.setText(rs.getString("name"));
                ageField.setText(String.valueOf(rs.getInt("age")));
                genderField.setText(rs.getString("gender"));
                bloodGroupField.setText(rs.getString("blood_group"));
                addressField.setText(rs.getString("address"));
            } else {
                JOptionPane.showMessageDialog(this, "No patient found.", "Not Found", JOptionPane.INFORMATION_MESSAGE);
                resetFields();
            }
            rs.close(); st.close();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "DB error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void resetFields() {
        nameField.setText(""); ageField.setText("");
        genderField.setText(""); bloodGroupField.setText(""); addressField.setText("");
    }

    private JTextField buildRoundedField(String placeholder) {
        return new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(245, 245, 245));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.setColor(new Color(200, 200, 200));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                if (getText().isEmpty() && !isFocusOwner()) {
                    g2.setColor(Color.GRAY);
                    g2.setFont(getFont().deriveFont(Font.ITALIC));
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(placeholder, 10, (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
                } else {
                    super.paintComponent(g);
                }
                g2.dispose();
            }
            @Override public void setBorder(Border b) {}
            @Override public Insets getInsets() { return new Insets(5, 10, 5, 10); }
        };
    }
}
