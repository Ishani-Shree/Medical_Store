import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class Login extends JFrame implements ActionListener {

    private static final int W = 520;
    private static final int H = 480;

    JButton submit, reset, close;
    JTextField     tfusername;
    JPasswordField tfpassword;
    private Connection conn;

    public Login() {
        initDatabaseConnection();
        setLayout(new BorderLayout());

        JPanel backgroundPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                ImageIcon i1 = new ImageIcon("icons/Login.jpg");
                g.drawImage(i1.getImage(), 0, 0, getWidth(), getHeight(), this);
            }
        };
        backgroundPanel.setLayout(new GridBagLayout());
        add(backgroundPanel);

        // --- Glass card ---
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(new Color(255, 255, 255, 200));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(180, 200, 230), 1),
            BorderFactory.createEmptyBorder(32, 40, 32, 40)
        ));
        card.setOpaque(true);

        // Title
        JLabel title = new JLabel("Medical Store Login");
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setForeground(new Color(30, 80, 160));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(title);
        card.add(Box.createVerticalStrut(24));

        // Username field
        tfusername = new JTextField(18);
        tfusername.setFont(new Font("Arial", Font.PLAIN, 16));
        tfusername.setForeground(Color.GRAY);
        tfusername.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        addPlaceholderEffect(tfusername, "Username");
        card.add(tfusername);
        card.add(Box.createVerticalStrut(14));

        // Password field
        tfpassword = new JPasswordField(18);
        tfpassword.setFont(new Font("Arial", Font.PLAIN, 16));
        tfpassword.setForeground(Color.GRAY);
        tfpassword.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        addPlaceholderEffect(tfpassword, "Password");
        card.add(tfpassword);
        card.add(Box.createVerticalStrut(24));

        // Button row
        JPanel btnRow = new JPanel(new GridLayout(1, 2, 12, 0));
        btnRow.setOpaque(false);
        btnRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));

        reset  = styledButton("Reset",  new Color(120, 130, 145));
        submit = styledButton("Login",  new Color(50, 150, 250));
        btnRow.add(reset);
        btnRow.add(submit);
        card.add(btnRow);
        card.add(Box.createVerticalStrut(10));

        close = styledButton("Close", new Color(200, 60, 60));
        close.setAlignmentX(Component.CENTER_ALIGNMENT);
        close.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        card.add(close);

        backgroundPanel.add(card);

        getRootPane().setDefaultButton(submit);
        SwingUtilities.invokeLater(() -> backgroundPanel.requestFocusInWindow());

        setSize(W, H);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setVisible(true);
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
        btn.setFont(new Font("Arial", Font.BOLD, 15));
        btn.setForeground(Color.WHITE);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(this);
        return btn;
    }

    private void initDatabaseConnection() {
        DatabaseConnection db = new DatabaseConnection();
        conn = db.getConnection();
        if (conn == null) {
            JOptionPane.showMessageDialog(this, "Database connection failed.", "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    private void addPlaceholderEffect(JTextField field, String placeholder) {
        field.setText(placeholder);
        field.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(Color.BLACK);
                }
            }
            @Override public void focusLost(FocusEvent e) {
                if (field.getText().isEmpty()) {
                    field.setText(placeholder);
                    field.setForeground(Color.GRAY);
                }
            }
        });
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        if (ae.getSource() == submit) handleLogin();
        else if (ae.getSource() == close) dispose();
        else if (ae.getSource() == reset) {
            tfusername.setText("Username"); tfusername.setForeground(Color.GRAY);
            tfpassword.setText("Password"); tfpassword.setForeground(Color.GRAY);
        }
    }

    private void handleLogin() {
        String username = tfusername.getText();
        String password = new String(tfpassword.getPassword());
        try {
            String query = "SELECT * FROM login WHERE username = ? AND password_hash = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                JOptionPane.showMessageDialog(this, "Login Successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
                new Home(conn);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Invalid Username or Password", "Login Error", JOptionPane.ERROR_MESSAGE);
            }
            rs.close(); stmt.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
