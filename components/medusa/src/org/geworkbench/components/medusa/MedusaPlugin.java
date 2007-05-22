package org.geworkbench.components.medusa;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.components.medusa.heatmap.DiscreteHeatMapPanel;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

/**
 * 
 * @author keshav
 * @version $Id: MedusaPlugin.java,v 1.5 2007-05-22 00:08:13 keshav Exp $
 */
public class MedusaPlugin extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private MedusaData medusaData = null;

	public MedusaPlugin(MedusaData medusaData) {
		super();
		this.medusaData = medusaData;

		JTabbedPane tabbedPane = new JTabbedPane();

		JPanel motifPanel = new JPanel();

		// MedusaHeatMap heatMap = new MedusaHeatMap(Color.gray, Color.gray,
		// medusaData);
		// motifPanel.add(heatMap);
		int i = 0;
		List<DSGeneMarker> targets = medusaData.getTargets();

		List<DSGeneMarker> regulators = medusaData.getRegulators();

		double[][] targetMatrix = new double[targets.size()][];
		for (DSGeneMarker target : targets) {
			double[] data = medusaData.getArraySet().getRow(target);
			targetMatrix[i] = data;
			i++;
		}

		int j = 0;
		double[][] regulatorMatrix = new double[regulators.size()][];
		for (DSGeneMarker regulator : regulators) {
			double[] data = medusaData.getArraySet().getRow(regulator);
			regulatorMatrix[j] = data;
			j++;
		}

		// int j = 0;
		// DoubleMatrixNamed namedMatrix = new
		// DenseDoubleMatrix2DNamed(rawMatrix);
		// for (DSGeneMarker target : targets) {
		// namedMatrix.addRowName(target.getLabel(), j);
		// j++;
		// }
		//
		// for (int k = 0; i < rawMatrix[0].length; k++) {
		// namedMatrix.addColumnName(String.valueOf(k), k);
		// }

		// ColorMatrix colorMatrix = new ColorMatrix(namedMatrix);
		// JMatrixDisplay matrixDisplay = new JMatrixDisplay(colorMatrix);
		// motifPanel.add(matrixDisplay);

		List<String> targetNames = new ArrayList<String>();
		for (DSGeneMarker marker : targets) {
			targetNames.add(marker.getLabel());
		}

		List<String> regulatorNames = new ArrayList<String>();
		for (DSGeneMarker marker : regulators) {
			regulatorNames.add(marker.getLabel());
		}

		motifPanel.setLayout(new GridLayout(2, 3));

		JPanel dummyPanel = new JPanel();
		motifPanel.add(dummyPanel);

		DiscreteHeatMapPanel regulatorHeatMap = new DiscreteHeatMapPanel(
				regulatorMatrix, 1, 0, -1, regulatorNames, true);
		motifPanel.add(regulatorHeatMap);

		FormLayout layout = new FormLayout("pref,60dlu", // columns
				"5dlu"); // add rows dynamically
		DefaultFormBuilder regulatorLabelBuilder = new DefaultFormBuilder(
				layout);
		regulatorLabelBuilder.nextRow();

		for (String name : regulatorNames) {
			JCheckBox checkBox = new JCheckBox();
			checkBox.setText(name);
			checkBox.setSelected(true);
			regulatorLabelBuilder.append(checkBox);
			regulatorLabelBuilder.appendRow("10dlu");
		}
		motifPanel.add(regulatorLabelBuilder.getPanel());

		// PUT PSSM HERE
		DiscreteHeatMapPanel heatMap2 = new DiscreteHeatMapPanel(targetMatrix,
				1, 0, -1, targetNames, false);
		motifPanel.add(heatMap2);
		// END PSSM

		DiscreteHeatMapPanel targetHeatMap = new DiscreteHeatMapPanel(
				targetMatrix, 1, 0, -1, targetNames, true);
		motifPanel.add(targetHeatMap);

		JPanel dummyPanel3 = new JPanel();
		motifPanel.add(dummyPanel3);

		tabbedPane.add("Motif", motifPanel);

		JPanel pssmPanel = new JPanel();
		tabbedPane.add("PSSM", pssmPanel);

		setLayout(new BorderLayout());
		add(tabbedPane, BorderLayout.CENTER);

	}
}
