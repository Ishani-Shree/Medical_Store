import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.sql.Connection;

public class Home extends JFrame implements ActionListener {

    private Connection conn;

    public Home(Connection conn) {
        this.conn = conn;
        setLayout(new BorderLayout());

        JPanel backgroundPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                ImageIcon i1 = new ImageIcon("icons/front.jpg");
                g.drawImage(i1.getImage(), 0, 0, getWidth(), getHeight(), this);
            }
        };
        backgroundPanel.setLayout(new BorderLayout(0, 0));
        add(backgroundPanel);

        // --- Left: Heading ---
        JLabel heading = new JLabel("<html><center>General<br>Medicine<br>Store</center></html>");
        heading.setHorizontalAlignment(SwingConstants.CENTER);
        heading.setVerticalAlignment(SwingConstants.CENTER);
        heading.setForeground(Color.BLUE);
        heading.setFont(new Font("Cooper Black", Font.BOLD, 72));
        backgroundPanel.add(heading, BorderLayout.CENTER);

        // --- Right: 3 nav buttons ---
        JPanel buttonPanel = new JPanel(new GridLayout(4, 1, 16, 16));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(60, 20, 60, 50));
        buttonPanel.setPreferredSize(new Dimension(300, 0));

        buttonPanel.add(createButtonWithImage("Medicine", "icons/Medicine.jpg"));
        buttonPanel.add(createButtonWithImage("Patient",  "icons/Medicine.jpg"));
        buttonPanel.add(createButtonWithImage("Doctor",   "icons/Medicine.jpg"));
        buttonPanel.add(createButtonWithImage("Record",   "icons/Medicine.jpg"));

        backgroundPanel.add(buttonPanel, BorderLayout.EAST);

        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setVisible(true);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                new DatabaseConnection().closeConnection();
                System.exit(0);
            }
        });
    }

    private static class RoundedImagePanel extends JPanel {
        private final Image image;

        RoundedImagePanel(Image image) {
            this.image = image;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            RoundRectangle2D rr = new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
            g2.setClip(rr);
            g2.drawImage(image, 0, 0, getWidth(), getHeight(), this);
            g2.setColor(new Color(50, 150, 250));
            g2.setStroke(new BasicStroke(2f));
            g2.setClip(null);
            g2.draw(rr);
            g2.dispose();
        }
    }

    private JPanel createButtonWithImage(String text, String imagePath) {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setOpaque(false);

        ImageIcon icon = new ImageIcon(imagePath);
        Image img = icon.getImage().getScaledInstance(120, 120, Image.SCALE_SMOOTH);
        RoundedImagePanel imagePanel = new RoundedImagePanel(img);
        imagePanel.setPreferredSize(new Dimension(120, 120));

        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color base = getModel().isPressed()  ? new Color(20, 110, 210)
                           : getModel().isRollover() ? new Color(40, 140, 240)
                                                     : new Color(50, 150, 250);
                g2.setColor(base);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setForeground(Color.WHITE);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(0, 38));
        button.setActionCommand(text);
        button.addActionListener(this);

        panel.add(imagePanel, BorderLayout.CENTER);
        panel.add(button, BorderLayout.SOUTH);
        return panel;
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        String cmd = ae.getActionCommand();
        if      (cmd.equals("Medicine")) new MedicineInventory(conn);
        else if (cmd.equals("Patient"))  new PatientInfo(conn);
        else if (cmd.equals("Doctor"))   new DoctorInfo(conn);
        else if (cmd.equals("Record"))   new RecordPage(conn);
    }
}
