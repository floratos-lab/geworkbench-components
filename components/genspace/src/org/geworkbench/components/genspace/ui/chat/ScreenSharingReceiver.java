package org.geworkbench.components.genspace.ui.chat;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import org.geworkbench.components.genspace.GenSpace;

/**
 * A specialized JPanel that receives hextile encoded PNG's and then displays
 * them
 * 
 * @author jon
 * 
 */
public class ScreenSharingReceiver extends JPanel {

	private static final long serialVersionUID = 5571873169397535659L;
	BufferedImage my_image;

	public ScreenSharingReceiver() {
		my_image = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.drawImage(my_image, 0, 0, null);
	}

	/**
	 * Receives an image update, drawing it to the screen
	 * 
	 * @param m
	 *            Message
	 * @param w
	 *            Image width
	 * @param h
	 *            Image height
	 */
	public void receiveImageUpdate(HashMap<String, Object> m, int w, int h) {
		ChatWindow.screenShareFrame.setSize(w, h);
		if (w != my_image.getWidth() || h != my_image.getHeight()) {
			my_image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		}
		setSize(w, h);
		WritableRaster r = my_image.getRaster();

		Object[] indices = (Object[]) m.get("indices");
		for (Object o : indices) {
			Integer tileNum = (Integer) o;
			BufferedImage im = null;

			try {
				im = ImageIO.read(new ByteArrayInputStream((byte[]) m.get("img"
						+ tileNum)));
			} catch (IOException e) {
				GenSpace.logger.warn("Error",e);
			}
			WritableRaster update = im.getRaster();
			/*
			 * Tiles are numbered as such: 1 2 3 4 5 6 7 8 9
			 */
			int row = tileNum / w;
			int col = ((tileNum) % w);

			r.setRect(col, row, update);
		}
		repaint();
	}
}
