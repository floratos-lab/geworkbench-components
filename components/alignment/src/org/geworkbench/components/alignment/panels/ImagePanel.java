package org.geworkbench.components.alignment.panels;

import javax.swing.*;
import java.awt.*;

/**
 * <p>Title: Sequence and Pattern Plugin</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: AMDeC_Califano lab</p>
 *
 * @author XZ
 * @version 1.0
 */

public class ImagePanel extends JPanel {
    private Image image;

    public ImagePanel(String filename) {
        image = Toolkit.getDefaultToolkit().getImage(filename);
        MediaTracker tracker = new MediaTracker(this);
        tracker.addImage(image, 0);
        try {
            tracker.waitForID(0);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Dimension d = getSize();
        int clientWidth = d.width;
        int clientHeight = d.height;
        g.drawImage(image, 0, 0, clientWidth, clientHeight, this);
    }

}
