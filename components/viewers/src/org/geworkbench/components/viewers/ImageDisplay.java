package org.geworkbench.components.viewers;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

/**
 * A panel to display image.
 * 
 * @author First Genetic Trust Inc.
 * @version 1.0
 */
public class ImageDisplay extends JPanel {
	private static final long serialVersionUID = -3330449026306112459L;

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
	 * @param g
	 *            <code>Graphics</code> to be painted with
	 */
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.setColor(Color.white);
		g.fillRect(0, 0, this.getWidth(), this.getHeight());
		if (image != null)
			g.drawImage(image.getImage(), 0, 0, Color.white, this);
	}

}
