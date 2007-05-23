package org.geworkbench.components.medusa.gui;

import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

import junit.framework.TestCase;

public class DiscreteHitOrMissHeatMapPanelTest extends TestCase {

	private int numElements = 6;

	private double[] row0 = null;

	private double[] row1 = null;

	private double[][] matrix = new double[2][numElements];

	private List<String> names = null;

	private String rulesPath = "data/test/dataset/pssm/rules/";

	private List<String> rulesFiles = new ArrayList();

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
			names.add(String.valueOf(i));
		}

		String rulesFile0 = "rule_0.xml";
		rulesFiles.add(rulesFile0);
	}

	public void testDrawDiscreteHitOrMissHeatMapPanel() {
		JDialog dialog = new JDialog();
		// dialog.setLayout(new GridLayout(2, 3));

		DiscreteHitOrMissHeatMapPanel heatMap = new DiscreteHitOrMissHeatMapPanel(
				rulesPath, rulesFiles, matrix.length);

		dialog.add(heatMap);

		dialog.pack();
		dialog.setModal(true);
		dialog.setVisible(true);
	}
}
