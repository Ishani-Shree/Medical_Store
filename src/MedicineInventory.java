import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.sql.Connection;

public class MedicineInventory extends JFrame implements ActionListener {

    private static final int FRAME_WIDTH = 800;
    private static final int FRAME_HEIGHT = 600;
    private Connection conn;

    public MedicineInventory(Connection conn) {

        this.conn = conn;   // Use the connection passed from Home
        setLayout(new BorderLayout());

        // Background Panel
        JPanel backgroundPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                ImageIcon backgroundImage = new ImageIcon("icons\\Med_Inventory.jpg");
                Image image = backgroundImage.getImage();
                g.drawImage(image, 0, 0, getWidth(), getHeight(), this);
            }
        };
        backgroundPanel.setLayout(null);
        add(backgroundPanel);

        // Add Medicine Section
        JPanel addPanel = createButtonWithImage("Add Medicine", "icons\\Med_Inventory1.jpg");
        addPanel.setBounds((FRAME_WIDTH / 2) - 275, (FRAME_HEIGHT / 2) - 185, 250, 275);
        backgroundPanel.add(addPanel);

        // Stock Info Section
        JPanel infoPanel = createButtonWithImage("Stock Info", "icons\\Med_Inventory3.jpg");
        infoPanel.setBounds((FRAME_WIDTH / 2) + 25, (FRAME_HEIGHT / 2) - 185, 250, 275);
        backgroundPanel.add(infoPanel);

        // setExtendedState(JFrame.MAXIMIZED_BOTH);
        setSize(FRAME_WIDTH, FRAME_HEIGHT);
        setLocationRelativeTo(null);
        setVisible(true);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    // Custom JPanel for rounded image
    private class RoundedImagePanel extends JPanel {
        private Image image;
        private int cornerRadius = 20;

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
                0, 0, width-1, height-1, cornerRadius, cornerRadius);

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

        button.setFont(new Font("Comic Sans MS", Font.BOLD, 20));
        button.setForeground(Color.WHITE);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.addActionListener(this);

        // Add components to panel
        panel.add(imagePanel, BorderLayout.CENTER);
        panel.add(button, BorderLayout.SOUTH);

        return panel;
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        String text = ae.getActionCommand();

        if (text.equals("Add Medicine")) {
            new AddMedicine(conn);
        } 
        else if (text.equals("Stock Info")) {
            new StockInfo(conn);
        }
    }
}