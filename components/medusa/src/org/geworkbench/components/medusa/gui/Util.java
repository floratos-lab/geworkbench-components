package org.geworkbench.components.medusa.gui;

import java.util.ArrayList;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JRadioButton;
import javax.swing.JTable;

public class Util {
	/**
	 * Creates a button group with the given button labels.
	 * 
	 * @param firstButtonName
	 * @param secondButtonName
	 * @return {@link List}
	 */
	public static List<JRadioButton> createRadioButtonGroup(
			String firstButtonName, String secondButtonName) {

		ButtonGroup buttonGroup = new ButtonGroup();

		JRadioButton firstButton = new JRadioButton(firstButtonName);
		firstButton.setSelected(true);
		firstButton.setActionCommand(firstButtonName);

		JRadioButton secondButton = new JRadioButton(secondButtonName);
		secondButton.setSelected(false);
		secondButton.setActionCommand(secondButtonName);
		/* add to the button group */
		buttonGroup = new ButtonGroup();
		buttonGroup.add(firstButton);
		buttonGroup.add(secondButton);

		List<JRadioButton> buttons = new ArrayList<JRadioButton>();
		buttons.add(firstButton);
		buttons.add(secondButton);
		return buttons;
	}

	/**
	 * 
	 * @param text
	 * @param toolText
	 * @return
	 */
	public static JButton createButton(String text, String toolText) {
		JButton loadTransFacButton = new JButton(text);
		loadTransFacButton.setToolTipText(toolText);
		return loadTransFacButton;
	}

	/**
	 * 
	 * @param scores
	 * @param topLeftLabel
	 * @return
	 */
	public static JTable createPssmTable(double[][] scores, String topLeftLabel) {

		int numCols = scores[0].length + 1;
		String[] colNames = new String[numCols];

		colNames[0] = topLeftLabel;
		for (int k = 1; k < numCols; k++) {
			colNames[k] = String.valueOf(k);
		}

		Object[][] scoresAsObjects = new Object[NUM_NUCLEOTIDES][numCols];
		scoresAsObjects[0][0] = "A";
		scoresAsObjects[1][0] = "C";
		scoresAsObjects[2][0] = "G";
		scoresAsObjects[3][0] = "T";

		for (int k = 0; k < scores.length; k++) {
			for (int l = 1; l <= scores[k].length; l++) {
				scoresAsObjects[k][l] = scores[k][l - 1];
			}
		}
		JTable pssmTable = new JTable(scoresAsObjects, colNames);
		return pssmTable;
	}

	/**
	 * Converts the scores to weights. If the number of rows is greater than 4
	 * (number of nucleotides) the matrix is transposed.
	 * 
	 * 
	 * 
	 * @param psamData
	 * @param transpose
	 *            Set transpose to true if you want the nucleotides to be
	 *            represented in the second dimension.
	 * @return double[][]
	 */
	public static double[][] convertScoresToWeights(double[][] psamData,
			boolean transpose) {

		double[][] data = null;

		if (transpose) {
			int rows = psamData[0].length;
			int columns = psamData.length;
			data = new double[rows][columns];
			for (int i = 0; i < rows; i++) {
				for (int j = 0; j < columns; j++) {
					data[i][j] = psamData[j][i];
				}
			}
		} else {
			data = psamData;
		}

		return scoresToWeights(data);
	}

	/**
	 * Converts the scores to weights.
	 * 
	 * @param psamData
	 *            The pssm data.
	 * @return double[][]
	 */
	private static double[][] scoresToWeights(double[][] psamData) {

		double[][] psamddG = new double[psamData.length][NUM_NUCLEOTIDES];
		for (int i = 0; i < psamData.length; i++) {

			if (psamData[i].length != NUM_NUCLEOTIDES)
				throw new RuntimeException(
						"Check the number of columns in your data.  There are only 4 possible nucleotides.  You have "
								+ psamData[i].length);

			double logMean = 0;
			for (int j = 0; j < NUM_NUCLEOTIDES; j++) {

				logMean += Math.log(psamData[i][j]);
			}
			logMean /= 4;
			for (int j = 0; j < NUM_NUCLEOTIDES; j++) {
				psamddG[i][j] = Math.log(psamData[i][j]) - logMean;
			}
		}
		return psamddG;
	}

	private static final int NUM_NUCLEOTIDES = 4;

}
