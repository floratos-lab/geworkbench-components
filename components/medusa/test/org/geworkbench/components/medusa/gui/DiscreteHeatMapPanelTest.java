package org.geworkbench.components.medusa.gui;

import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;

import org.geworkbench.components.medusa.gui.DiscreteHeatMapPanel;

import junit.framework.TestCase;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

/**
 * 
 * @author keshav
 * @version $Id: DiscreteHeatMapPanelTest.java,v 1.1 2007-05-23 16:13:05 keshav Exp $
 */
public class DiscreteHeatMapPanelTest extends TestCase {

	int numElements = 6;

	double[] row0 = null;

	double[] row1 = null;

	double[][] matrix = new double[2][numElements];

	List<String> names = null;

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
	}

	public void testDrawHeatMap() {

		JDialog dialog = new JDialog();
		dialog.setLayout(new GridLayout(2, 3));

		DiscreteHeatMapPanel heatMap = new DiscreteHeatMapPanel(matrix, 1, 0,
				-1, names, false);
		DiscreteHeatMapPanel heatMap1 = new DiscreteHeatMapPanel(matrix, 1, 0,
				-1, names, false);
		DiscreteHeatMapPanel heatMap2 = new DiscreteHeatMapPanel(matrix, 1, 0,
				-1, names, false);

		dialog.add(new JPanel());// dummy
		dialog.add(heatMap);
		FormLayout layout = new FormLayout("right:pref, 20dlu", // columns
				"2dlu"); // add rows dynamically, with 30dlu at the top

		DefaultFormBuilder labelBuilder = new DefaultFormBuilder(layout);
		// labelBuilder.appendSeparator("Regulators");
		labelBuilder.nextRow();

		for (String name : names) {
			JCheckBox checkBox = new JCheckBox();
			checkBox.setText(name);
			checkBox.setSelected(true);
			labelBuilder.append(checkBox);
			labelBuilder.appendRow("10dlu");
		}
		dialog.add(labelBuilder.getPanel());

		dialog.add(heatMap1);
		dialog.add(heatMap2);
		dialog.add(new JPanel());// dummy

		dialog.pack();
		dialog.setModal(true);
		dialog.setVisible(true);
	}

}
