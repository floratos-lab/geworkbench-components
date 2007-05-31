package org.geworkbench.components.medusa.gui;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JDialog;

import junit.framework.TestCase;

public class DiscreteHitOrMissHeatMapPanelTest extends TestCase {

	private int numElements = 6;

	private double[] row0 = null;

	private double[] row1 = null;

	private double[][] matrix = new double[2][numElements];

	private List<String> names = null;

	private String rulesPath = "data/test/dataset/pssm/rules/";

	private List<String> rulesFiles = null;

	private String sequencePath = "data/test/dataset/pssm/run1";

	@Override
	protected void setUp() {
		row0 = new double[numElements];

		row0[0] = 0;// black
		row0[1] = 1;// red
		row0[2] = -1;// green
		row0[3] = 1;
		row0[4] = 0;
		row0[5] = -1;

		row1 = new double[numElements];

		row1[0] = 0;// black
		row1[1] = 0;// red
		row1[2] = 1;// green
		row1[3] = -1;
		row1[4] = -1;
		row1[5] = 1;

		matrix[0] = row0;
		matrix[1] = row1;

		names = new ArrayList<String>();
		for (int i = 0; i < numElements; i++) {
			names.add("gene_label_" + i);
		}

		rulesFiles = new ArrayList();
		String rulesFile0 = "rule_0.xml";
		rulesFiles.add(rulesFile0);
		String rulesFile1 = "rule_1.xml";
		rulesFiles.add(rulesFile1);
	}

	/**
	 * 
	 * 
	 */
	public void testDrawDiscreteHitOrMissHeatMapPanel() {
		JDialog dialog = new JDialog();

		DiscreteHitOrMissHeatMapPanel heatMap = new DiscreteHitOrMissHeatMapPanel(
				rulesPath, rulesFiles, names, sequencePath);

		dialog.add(heatMap);

		dialog.pack();
		dialog.setModal(true);
		dialog.setVisible(true);
	}
}
