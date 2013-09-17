package org.geworkbench.components.promoter;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.larvalabs.chart.PSAMPlot;

/**
 * 
 * @author zji
 * @version $Id$
 *
 */
class ImagePanel extends JPanel {
	private static final long serialVersionUID = 2663743406941254956L;
	
	static private Log log = LogFactory.getLog(ImagePanel.class);
	
	private Image img;
	private int WIDTH = 400;
	private int HEIGHT = 200;

	public ImagePanel() {
		this.setBackground(Color.white);
	}

	@Override
	public void paintComponent(Graphics g) {
		update(g);
	}

	@Override
	public void update(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		g2.setColor(this.getBackground());
		g2.fillRect(0, 0, this.getWidth(), this.getHeight());
		if (img != null) {
			g2.drawImage(img, 0, 0, this);
		} else {
			log.debug("img is null");
		}

	}

	public void setImage(double scores[][]) {
		PSAMPlot psamPlot = new PSAMPlot(scores);
		psamPlot.setMaintainProportions(false);
		psamPlot.setAxisDensityScale(1);
		psamPlot.setYTitle("bits");
		psamPlot.setAxisLabelScale(3);
		psamPlot.setAllowXLabelRotation(false);
		BufferedImage image = new BufferedImage(WIDTH, HEIGHT,
				BufferedImage.TYPE_INT_RGB);
		Graphics2D graphics = (Graphics2D) image.getGraphics();
		psamPlot.layoutChart(WIDTH, HEIGHT, graphics.getFontRenderContext());

		psamPlot.paint(graphics);
		this.img = image;
		repaint();
	}

	@Override
	public Dimension getPreferredSize() {
		if (img != null) {
			return new Dimension(img.getWidth(this), img.getHeight(this));
		}
		return new Dimension(50, 50);
	}

	@Override
	public Dimension getMinimumSize() {
		if (img != null) {
			return new Dimension(img.getWidth(this), img.getHeight(this));
		}
		return new Dimension(40, 40);
	}
}