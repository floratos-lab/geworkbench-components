package org.geworkbench.components.medusa.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
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
 * @version $Id: DiscreteHeatMapPanel.java,v 1.8 2008-04-21 16:48:47 chiangy Exp $
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

	private int colLabelPadding = 0;
	
	private boolean[] discretizedColumn = null;

	/**
	 * 
	 * @param matrix
	 * @param max
	 * @param medium
	 * @param min
	 * @param names
	 * @param showRowLabels
	 */
	public DiscreteHeatMapPanel(double[][] matrix, double max, double medium,
			double min, List<String> names, boolean showRowLabels) {

		this(matrix, max, medium, min, names, showRowLabels, 15);

	}

	/**
	 * 
	 * @param matrix
	 * @param max
	 * @param medium
	 * @param min
	 * @param names
	 * @param showRowLabels
	 */
	public DiscreteHeatMapPanel(double[][] matrix, double max, double medium,
			double min, List<String> names, boolean showRowLabels,
			int colLabelPadding) {

		this.setLayout(new GridBagLayout());
		this.matrix = matrix;

		this.max = max;
		this.medium = medium;
		this.min = min;
		this.names = names;
		this.showLabels = showRowLabels;
		this.colLabelPadding = colLabelPadding;
		discretizedColumn = new boolean[matrix[0].length];
		for (int cx=0;cx<matrix[0].length;cx++){
			discretizedColumn[cx]=true;
			for (int cy=0;cy<matrix.length;cy++){
				if ((matrix[cy][cx]==this.max)||(matrix[cy][cx]==this.min)||(matrix[cy][cx]==this.medium)){
					//this probably is a discretized Column
				}else
					discretizedColumn[cx]=false;
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	BufferedImage offlineimage=null;
	public void paintComponent(Graphics g) {
		clear(g);
		int margins=15*2;
		if (offlineimage==null){ //first time
			offlineimage = new BufferedImage(matrix[0].length*15+margins, matrix.length*15+30, BufferedImage.TYPE_INT_RGB);
	        Graphics2D offg = (Graphics2D) offlineimage.getGraphics();
	        offg.setColor(this.getParent().getBackground());
	        offg.fillRect(0,0,matrix[0].length*15+margins, matrix.length*15+margins);
			// TODO abstract me into a DiscreteHeatMap
			int y = colLabelPadding;
			for (int h = 0; h < matrix.length; h++) {
				JCheckBox checkBox = new JCheckBox();
				checkBox.setText(names.get(h));
	
				this.row = matrix[h];
				int x = 15;
				for (int i = 0; i < row.length; i++) {
					Rectangle2D.Double rect = new Rectangle2D.Double(x, y, 15, 15);
					if (discretizedColumn[i]){
						if (row[i] == this.medium) {
							offg.setColor(Color.black);
						} else if (row[i] == this.max) {
							offg.setColor(Color.red);
						} else if (row[i] == this.min) {
							offg.setColor(Color.green);
						} else {
							continue;
						}
						offg.fill(rect);
						x = x + 15;
					}
				}
	
				y = y + 15;
				if (showLabels) {
					offg.setColor(Color.black);
					offg.drawString(names.get(h), x, y);
				}
			}
		}
		Graphics2D g2d = (Graphics2D) g;
		g2d.drawImage(offlineimage, null, 0, 0);
	}

	/**
	 * 
	 * @param g
	 */
	protected void clear(Graphics g) {
		super.paintComponent(g);
	}
}
