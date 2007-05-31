package org.geworkbench.components.medusa.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

import javax.swing.JPanel;

import org.geworkbench.components.medusa.MedusaUtil;

/**
 * 
 * @author keshav
 * @version $Id: DiscreteHitOrMissHeatMapPanel.java,v 1.1 2007/05/23 17:31:22
 *          keshav Exp $
 */
public class DiscreteHitOrMissHeatMapPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String rulePath = null;

	private List<String> ruleFiles = null;

	private String sequencePath = null;

	private List<String> targetNames = null;

	private boolean[][] hitOrMissMatrix = null;

	/**
	 * 
	 * @param rulePath
	 * @param ruleFiles
	 * @param targetNames
	 */
	public DiscreteHitOrMissHeatMapPanel(String rulePath,
			List<String> ruleFiles, List<String> targetNames,
			String sequencePath) {

		this.rulePath = rulePath;

		this.ruleFiles = ruleFiles;

		this.sequencePath = sequencePath;

		this.targetNames = targetNames;

		hitOrMissMatrix = MedusaUtil.generateHitOrMissMatrix(targetNames,
				ruleFiles, rulePath, sequencePath);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	public void paintComponent(Graphics g) {

		clear(g);

		Graphics2D g2d = (Graphics2D) g;

		int row = 15;
		for (int i = 0; i < targetNames.size(); i++) {
			int col = 15;
			for (int j = 0; j < ruleFiles.size(); j++) {
				boolean isHit = hitOrMissMatrix[i][j];

				Rectangle2D.Double rect = new Rectangle2D.Double(col, row, 15,
						15);
				if (isHit)
					g2d.setColor(Color.blue);
				else
					g2d.setColor(Color.black);

				g2d.fill(rect);
				col = col + 15;
			}
			row = row + 15;
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
