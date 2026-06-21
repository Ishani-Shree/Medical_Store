import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.sql.Connection;

public class MedicineInventory extends JFrame implements ActionListener {

    private Connection conn;

    public MedicineInventory(Connection conn) {
        this.conn = conn;
        setLayout(new BorderLayout());

        JPanel backgroundPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                ImageIcon bg = new ImageIcon("icons/Med_Inventory.jpg");
                g.drawImage(bg.getImage(), 0, 0, getWidth(), getHeight(), this);
            }
        };
        backgroundPanel.setLayout(new GridBagLayout());
        add(backgroundPanel);

        // Title
        JLabel title = new JLabel("Medicine Inventory");
        title.setFont(new Font("Arial", Font.BOLD, 28));
        title.setForeground(Color.WHITE);

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 40, 0));
        buttonRow.setOpaque(false);
        buttonRow.add(createCard("Add Medicine", "icons/Med_Inventory1.jpg"));
        buttonRow.add(createCard("Stock Info",   "icons/Med_Inventory3.jpg"));

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setOpaque(false);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        center.add(title);
        center.add(Box.createVerticalStrut(30));
        center.add(buttonRow);

        backgroundPanel.add(center);

        setSize(720, 520);
        setLocationRelativeTo(null);
        setVisible(true);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    private static class RoundedImagePanel extends JPanel {
        private final Image image;
        RoundedImagePanel(Image image) { this.image = image; setOpaque(false); }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            RoundRectangle2D rr = new RoundRectangle2D.Float(0, 0, getWidth()-1, getHeight()-1, 20, 20);
            g2.setClip(rr);
            g2.drawImage(image, 0, 0, getWidth(), getHeight(), this);
            g2.setColor(new Color(50, 150, 250));
            g2.setStroke(new BasicStroke(2f));
            g2.setClip(null);
            g2.draw(rr);
            g2.dispose();
        }
    }

    private JPanel createCard(String text, String imagePath) {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(220, 260));

        ImageIcon icon = new ImageIcon(imagePath);
        Image img = icon.getImage().getScaledInstance(160, 160, Image.SCALE_SMOOTH);
        RoundedImagePanel imgPanel = new RoundedImagePanel(img);

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
        button.setPreferredSize(new Dimension(0, 40));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setActionCommand(text);
        button.addActionListener(this);

        panel.add(imgPanel, BorderLayout.CENTER);
        panel.add(button,   BorderLayout.SOUTH);
        return panel;
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        String cmd = ae.getActionCommand();
        if      (cmd.equals("Add Medicine")) new AddMedicine(conn);
        else if (cmd.equals("Stock Info"))   new StockInfo(conn);
    }
}
