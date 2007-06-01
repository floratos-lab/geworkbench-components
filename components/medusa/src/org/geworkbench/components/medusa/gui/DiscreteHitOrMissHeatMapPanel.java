package org.geworkbench.components.medusa.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.components.medusa.MedusaUtil;

import edu.columbia.ccls.medusa.io.SerializedRule;

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

	private Log log = LogFactory.getLog(this.getClass());

	private String rulePath = null;

	private List<String> ruleFiles = null;

	private String sequencePath = null;

	private List<String> targetNames = null;

	private ArrayList<SerializedRule> srules = null;

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

		srules = MedusaUtil.getSerializedRules(ruleFiles, rulePath);

		hitOrMissMatrix = MedusaUtil.generateHitOrMissMatrix(targetNames,
				srules, sequencePath);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	public void paintComponent(Graphics g) {

		clear(g);

		Graphics2D g2d = (Graphics2D) g;

		int row = 120;
		int lcol = 25;
		for (SerializedRule srule : srules) {

			String sequence = MedusaUtil.generateConsensusSequence(srule
					.getPssm());
			drawColumnNames(sequence, g2d, row, lcol);
			lcol = lcol + 15;
		}

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
	 * @param label
	 *            The String to draw.
	 * @param g2d
	 * @param row
	 *            The row from where to draw the text.
	 * @param lcol
	 *            The (incremental) column where the text should be drawn.
	 */
	private void drawColumnNames(String label, Graphics2D g2d, int row, int lcol) {
		// TODO move method into a "gui util" class
		AffineTransform fontAT = new AffineTransform();

		/* slant text backwards */
		// fontAT.shear(0.2, 0.0);
		/* counter-clockwise 90 degrees */
		fontAT.setToRotation(Math.PI * 3.0f / 2.0f);
		Font font = new Font("Helvetica", Font.ITALIC, 12);
		Font theDerivedFont = font.deriveFont(fontAT);

		FontRenderContext frc = g2d.getFontRenderContext();
		TextLayout tstring = new TextLayout(label, theDerivedFont, frc);

		tstring.draw(g2d, lcol, row);
	}

	/**
	 * 
	 * @param g
	 */
	protected void clear(Graphics g) {
		super.paintComponent(g);
	}

}
