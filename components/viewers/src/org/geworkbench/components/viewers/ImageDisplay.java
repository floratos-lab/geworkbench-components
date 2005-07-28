package org.geworkbench.components.viewers;

import javax.swing.*;
import java.awt.*;

/**
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: First Genetic Trust Inc.</p>
 *
 * @author First Genetic Trust Inc.
 * @version 1.0
 */
public class ImageDisplay extends JPanel {
    /**
     * Image painted on this <code>Component</code>
     */
    ImageIcon image = null;

    public ImageDisplay() {
    }

    public void setImage(ImageIcon i) {
        image = i;
    }

    /**
     * {@link java.awt.Component Component} method
     *
     * @param g <code>Graphics</code> to be painted with
     */
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.white);
        g.fillRect(0, 0, this.getWidth(), this.getHeight());
        if (image != null)
            g.drawImage(image.getImage(), 0, 0, Color.white, this);
    }

}

