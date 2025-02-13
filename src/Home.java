import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.sql.Connection;

public class Home extends JFrame implements ActionListener {

    private JLabel heading;
    private Connection conn; // Shared connection

    public Home(Connection conn) {
        this.conn = conn;
        setLayout(new BorderLayout());

        // Initialize database connection
        // initDatabaseConnection();

        // Background Panel with dynamic image rendering
        JPanel backgroundPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                ImageIcon i1 = new ImageIcon("icons/front.jpg"); // Background image
                Image image = i1.getImage();
                g.drawImage(image, 0, 0, getWidth(), getHeight(), this); // Scale image dynamically
            }
        };
        backgroundPanel.setLayout(null); // Use null layout for precise positioning
        add(backgroundPanel);

        // Heading Label
        heading = new JLabel("<html><center>General<br>Medicine<br>Store</center></html>");
        heading.setBounds(300, 250, 1000, 500);
        heading.setForeground(Color.BLUE);
        heading.setFont(new Font("Cooper Black", Font.BOLD, 120));
        backgroundPanel.add(heading);

        // Medicine Panel
        JPanel MedPanel = createButtonWithImage("Medicine", "icons/Medicine.jpg");
        MedPanel.setBounds(1000, 150, 300, 325);
        backgroundPanel.add(MedPanel);

        // Patient Panel
        JPanel PatPanel = createButtonWithImage("Patient", "icons/Medicine.jpg");
        PatPanel.setBounds(1400, 150, 300, 325);
        backgroundPanel.add(PatPanel);

        // Record Panel
        JPanel RecPanel = createButtonWithImage("Record", "icons/Medicine.jpg");
        RecPanel.setBounds(1200, 550, 300, 325);
        backgroundPanel.add(RecPanel);

        // Frame Settings
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Maximize window
        setVisible(true);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Proper exit operation

        // Close connection when Home is closed
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (conn != null) {
                    DatabaseConnection dbConnection = new DatabaseConnection();
                    dbConnection.closeConnection(); // Close the connection on exit
                }
                System.exit(0);
            }
        });
    }

    // Custom JPanel for rounded image
    private class RoundedImagePanel extends JPanel {
        private Image image;
        private int cornerRadius = 25;

        public RoundedImagePanel(Image image) {
            this.image = image;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();

            // Create rounded rectangle
            RoundRectangle2D roundedRectangle = new RoundRectangle2D.Float(
                0, 0, width - 1, height - 1, cornerRadius, cornerRadius);

            // Clip the image to rounded rectangle
            g2.setClip(roundedRectangle);

            // Draw the image
            g2.drawImage(image, 0, 0, width, height, this);

            // Draw border
            g2.setColor(new Color(50, 150, 250));
            g2.setStroke(new BasicStroke(2f));
            g2.draw(roundedRectangle);

            g2.dispose();
        }
    }

    private JPanel createButtonWithImage(String buttonText, String imagePath) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BorderLayout(0, 5));

        // Load and scale image
        ImageIcon icon = new ImageIcon(imagePath);
        Image img = icon.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);

        // Create rounded image panel
        RoundedImagePanel imagePanel = new RoundedImagePanel(img);
        imagePanel.setPreferredSize(new Dimension(150, 150));

        // Create button with custom styling
        JButton button = new JButton(buttonText) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (getModel().isPressed()) {
                    g2.setColor(new Color(30, 130, 230));
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(40, 140, 240));
                } else {
                    g2.setColor(new Color(50, 150, 250));
                }

                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();

                super.paintComponent(g);
            }
        };

        button.setFont(new Font("Comic Sans MS", Font.BOLD, 34));
        button.setForeground(Color.WHITE);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.addActionListener(this);

        button.setActionCommand(buttonText);

        // Add components to panel
        panel.add(imagePanel, BorderLayout.CENTER);
        panel.add(button, BorderLayout.SOUTH);

        return panel;
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        String text = ae.getActionCommand();

        if (text.equals("Medicine")) {
            new MedicineInventory(conn); // Pass the connection to the Medicine Inventory page
        } else if (text.equals("Patient")) {
            new PatientInfo(conn); // Pass the connection to the Patient Info page
        } else if (text.equals("Record")) {
            new RecordPage(conn);
        }
    }
}
