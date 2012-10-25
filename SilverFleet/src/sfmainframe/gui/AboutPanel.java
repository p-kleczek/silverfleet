package sfmainframe.gui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Toolkit;

import javax.swing.JLabel;
import javax.swing.JPanel;

import sfmainframe.MainBoard;

@SuppressWarnings("serial")
public class AboutPanel extends JPanel {
    private static Image aboutImage;
    private static JLabel vesionLabel;

    public AboutPanel() {
        this.setLayout(null);

	vesionLabel = new javax.swing.JLabel();

        java.net.URL imgURL = MainBoard.class.getResource("images/about.png");
	Toolkit tk = this.getToolkit();

        if (imgURL != null) {
            aboutImage  = tk.getImage(imgURL);
        } else {
            System.err.println("Couldn't find file: " + "images/about.png");
        }

	this.repaint();

	vesionLabel.setText("Version: 0.9 (beta)   24/4/2010");
        this.add(vesionLabel);
        vesionLabel.setBounds(20, 415, 280, 20);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(300,450);
    }

    @Override
    public void paintComponent(Graphics g) {
//        super.paintComponent(g);

	Graphics2D g2 = (Graphics2D) g;
	g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

	g.drawImage(aboutImage, 0,0, this);
    }
}
