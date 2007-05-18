package org.geworkbench.components.medusa;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.components.medusa.heatmap.MedusaHeatMap;

import ubic.basecode.dataStructure.matrix.DenseDoubleMatrix2DNamed;
import ubic.basecode.dataStructure.matrix.DoubleMatrixNamed;
import ubic.basecode.gui.ColorMatrix;
import ubic.basecode.gui.JMatrixDisplay;

/**
 * 
 * @author keshav
 * @version $Id: MedusaPlugin.java,v 1.4 2007-05-18 21:34:56 keshav Exp $
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

		//MedusaHeatMap heatMap = new MedusaHeatMap(Color.gray, Color.gray,
		//		medusaData);
		// motifPanel.add(heatMap);
		// int i = 0;
		// List<DSGeneMarker> targets = medusaData.getTargets();
		// double[][] rawMatrix = new double[targets.size()][];
		// for (DSGeneMarker target : targets) {
		// double[] data = medusaData.getArraySet().getRow(target);
		// rawMatrix[i] = data;
		// i++;
		// }
		//
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
		//
		// ColorMatrix colorMatrix = new ColorMatrix(namedMatrix);
		// JMatrixDisplay matrixDisplay = new JMatrixDisplay(colorMatrix);
		//		motifPanel.add(matrixDisplay);

		tabbedPane.add("Motif", motifPanel);

		JPanel pssmPanel = new JPanel();
		tabbedPane.add("PSSM", pssmPanel);

		setLayout(new BorderLayout());
		add(tabbedPane, BorderLayout.CENTER);

	}
}
