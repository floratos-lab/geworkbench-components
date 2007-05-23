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
 * 
 * @author keshav
 * @version $Id: DiscreteHeatMapPanel.java,v 1.1 2007-05-23 16:02:38 keshav Exp $
 */
public class DiscreteHeatMapPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	double max = 0;

	double medium = 0;

	double min = 0;

	double[] row = null;

	double[][] matrix = null;

	List<String> names = null;

	boolean showLabels = false;

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

	public void paintComponent(Graphics g) {
		clear(g);

		Graphics2D g2d = (Graphics2D) g;

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

	protected void clear(Graphics g) {
		super.paintComponent(g);
	}
}
