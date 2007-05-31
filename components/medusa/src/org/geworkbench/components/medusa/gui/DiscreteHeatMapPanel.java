package org.geworkbench.components.medusa.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.geom.Rectangle2D;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

/**
 * Creates a discrete heat map panel with the supplied max, medium, and min
 * values.
 * <p>
 * The color scheme has been predefined as max=Color.red, medium=Color.black,
 * min=Color.green.
 * 
 * 
 * @author keshav
 * @version $Id: DiscreteHeatMapPanel.java,v 1.4 2007-05-31 16:41:45 keshav Exp $
 */
public class DiscreteHeatMapPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private double max = 0;

	private double medium = 0;

	private double min = 0;

	private double[] row = null;

	private double[][] matrix = null;

	private List<String> names = null;

	private boolean showLabels = false;

	/**
	 * 
	 * @param matrix
	 * @param max
	 * @param medium
	 * @param min
	 * @param names
	 * @param showLabels
	 */
	public DiscreteHeatMapPanel(double[][] matrix, double max, double medium,
			double min, List<String> names, boolean showLabels) {

		this.setLayout(new GridBagLayout());
		this.matrix = matrix;

		this.max = max;
		this.medium = medium;
		this.min = min;
		this.names = names;
		this.showLabels = showLabels;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	public void paintComponent(Graphics g) {
		clear(g);

		Graphics2D g2d = (Graphics2D) g;

		// TODO abstract me into a DiscreteHeatMap
		int y = 15;
		for (int h = 0; h < matrix.length; h++) {
			JCheckBox checkBox = new JCheckBox();
			checkBox.setText(names.get(h));

			this.row = matrix[h];
			int x = 15;
			for (int i = 0; i < row.length; i++) {
				Rectangle2D.Double rect = new Rectangle2D.Double(x, y, 15, 15);
				if (row[i] == this.medium) {
					g2d.setColor(Color.black);
				} else if (row[i] == this.max) {
					g2d.setColor(Color.red);
				} else if (row[i] == this.min) {
					g2d.setColor(Color.green);
				} else {
					continue;
				}
				g2d.fill(rect);
				x = x + 15;
			}

			y = y + 15;
			if (showLabels) {
				// this.add(checkBox);
				g2d.setColor(Color.black);
				g2d.drawString(names.get(h), x, y);
			}
		}
	}

	/**
	 * 
	 * @param g
	 */
	protected void clear(Graphics g) {
		super.paintComponent(g);
	}
}
