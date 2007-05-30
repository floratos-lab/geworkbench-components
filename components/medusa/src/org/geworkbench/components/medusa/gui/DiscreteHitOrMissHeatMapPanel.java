package org.geworkbench.components.medusa.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.List;

import javax.swing.JPanel;

import org.geworkbench.components.medusa.MedusaUtil;

import edu.columbia.ccls.medusa.io.RuleParser;
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

	private String rulePath = null;

	private List<String> ruleFiles = null;

	private String sequencePath = null;

	private int length = 0;

	private List<String> targetNames = null;

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

		this.length = targetNames.size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	public void paintComponent(Graphics g) {
		clear(g);

		Graphics2D g2d = (Graphics2D) g;

		// TODO abstract me into a DiscreteHitOrMissHeatMap
		int x = 15;
		for (String targetName : targetNames) {
			int y = 15;
			for (String ruleFile : ruleFiles) {
				SerializedRule srule = null;
				try {
					srule = RuleParser.read(rulePath + ruleFile);
				} catch (IOException e) {
					e.printStackTrace();
				}

				boolean isHit = MedusaUtil.isHitByPssm(srule.getPssm(), srule
						.getPssmThreshold(), targetName, sequencePath);

				Rectangle2D.Double rect = new Rectangle2D.Double(x, y, 15, 15);
				if (isHit)
					g2d.setColor(Color.blue);
				else
					g2d.setColor(Color.black);

				g2d.fill(rect);
				y = y + 15;

			}
			x = x + 15;
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
