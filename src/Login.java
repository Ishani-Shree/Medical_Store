import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class Login extends JFrame implements ActionListener {
    private static final int FRAME_WIDTH = 600;
    private static final int FRAME_HEIGHT = 600;
    private static final int BUTTON_WIDTH = 200;
    private static final int BUTTON_HEIGHT = 40;
    JButton submit, reset, close;
    JTextField tfusername;
    JPasswordField tfpassword;
    private Connection conn; // Shared connection

    public Login() {
        // Initialize database connection
        initDatabaseConnection();

        setLayout(new BorderLayout());

        // Background Panel with dynamic image rendering
        JPanel backgroundPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                ImageIcon i1 = new ImageIcon("icons/Login.jpg"); // Background image
                Image image = i1.getImage();
                g.drawImage(image, 0, 0, getWidth(), getHeight(), this); // Scale image dynamically
            }
        };
        backgroundPanel.setLayout(null); // Use null layout for precise positioning
        add(backgroundPanel);

        // Username Field

        tfusername = new JTextField();
        tfusername.setBounds((FRAME_WIDTH / 2) - BUTTON_WIDTH, (FRAME_HEIGHT / 4) - 20, 2*BUTTON_WIDTH, 50); // Adjusted size for larger font
        tfusername.setFont(new Font("Arial", Font.PLAIN, 24)); // Increased font size
        tfusername.setForeground(Color.GRAY); // Placeholder color
        addPlaceholderEffect(tfusername, "Username");
        backgroundPanel.add(tfusername);

        // Password Field

        tfpassword = new JPasswordField();
        tfpassword.setBounds((FRAME_WIDTH / 2) - BUTTON_WIDTH, (FRAME_HEIGHT / 4) + 50, 2*BUTTON_WIDTH, 50); // Adjusted size for larger font
        tfpassword.setFont(new Font("Arial", Font.PLAIN, 24)); // Increased font size
        tfpassword.setForeground(Color.GRAY); // Placeholder color
        addPlaceholderEffect(tfpassword, "Password");
        backgroundPanel.add(tfpassword);

        // Reset Button
        reset = new JButton("Reset");
        reset.setBounds((FRAME_WIDTH / 2) - 225, (FRAME_HEIGHT / 2), BUTTON_WIDTH, BUTTON_HEIGHT); // Adjusted size for larger font
        reset.setFont(new Font("Arial", Font.PLAIN, 24)); // Increased font size
        reset.addActionListener(this);
        backgroundPanel.add(reset);

        // Submit Button
        submit = new JButton("Login");
        submit.setBounds((FRAME_WIDTH / 2) + 10, (FRAME_HEIGHT / 2), BUTTON_WIDTH, BUTTON_HEIGHT); // Adjusted size for larger font
        submit.setFont(new Font("Arial", Font.PLAIN, 24)); // Increased font size
        submit.addActionListener(this);
        backgroundPanel.add(submit);

        // Make Submit button the default button for the frame
        getRootPane().setDefaultButton(submit);

        // Close Button
        close = new JButton("Close");
        close.setBounds((FRAME_WIDTH / 2) - 100, (FRAME_HEIGHT / 2) + 90, BUTTON_WIDTH, BUTTON_HEIGHT); // Adjusted size for larger font
        close.setFont(new Font("Arial", Font.PLAIN, 24)); // Increased font size
        close.addActionListener(this);
        backgroundPanel.add(close);

        // Set initial focus to background to avoid triggering focus events
        SwingUtilities.invokeLater(() -> backgroundPanel.requestFocusInWindow());

        // Frame settings
        setSize(FRAME_WIDTH, FRAME_HEIGHT); // Increased frame size
        setLocationRelativeTo(null); // Center the frame on the screen
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Ensures the application exits when closed
        setVisible(true);
    }

    // // Initialize the database connection
    private void initDatabaseConnection() {
        DatabaseConnection dbConnection = new DatabaseConnection();
        conn = dbConnection.getConnection(); // Establish connection once
        if (conn == null) {
            JOptionPane.showMessageDialog(this, "Database connection failed.", "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1); // Exit if connection fails
        }
    }

    private void addPlaceholderEffect(JTextField textField, String placeholderText) {
        // Set the initial placeholder text
        textField.setText(placeholderText);
        // Add FocusListener to handle placeholder effect
        textField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (textField.getText().equals(placeholderText)) {
                    textField.setText("");
                    textField.setForeground(Color.BLACK); // Reset color for user input
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (textField.getText().isEmpty()) {
                    textField.setText(placeholderText);
                    textField.setForeground(Color.GRAY); // Placeholder color
                }
            }
        });
    }

    public void actionPerformed(ActionEvent ae) {
        if (ae.getSource() == submit) {
            handleLogin();
        }
        else if (ae.getSource() == close) {
            dispose(); // Close the application
        }
        else if (ae.getSource() == reset) {
            tfusername.setText("Username...");
            tfusername.setForeground(Color.GRAY);
            tfpassword.setText("Password...");
            tfpassword.setForeground(Color.GRAY);
        }
    }

    private void handleLogin() {
        String username = tfusername.getText();
        String password = new String(tfpassword.getPassword()); // Securely get password

        try {
            // DatabaseConnection dbConnection = new DatabaseConnection();
            // Connection conn = dbConnection.getConnection(); // Get the connection

            if (conn != null) { // Check if the connection is successful
                String query = "SELECT * FROM login WHERE username = ? AND password_hash = ?";
                PreparedStatement stmt = conn.prepareStatement(query);

                // Set parameters
                stmt.setString(1, username);
                stmt.setString(2, password);

                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    JOptionPane.showMessageDialog(this, "Login Successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    new Home(conn); // Redirect to Home screen on successful login
                    dispose(); // Close login window
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid Username or Password", "Login Error", JOptionPane.ERROR_MESSAGE);
                    tfusername.setText("");
                    tfpassword.setText("");
                }

                // Close resources
                rs.close();
                stmt.close ();
                // conn.close(); // Close the database connection

            } else {
                JOptionPane.showMessageDialog(this, "Database connection failed.", "Connection Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
