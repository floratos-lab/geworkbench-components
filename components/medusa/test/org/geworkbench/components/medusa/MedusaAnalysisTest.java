package org.geworkbench.components.medusa;

import javax.swing.JComboBox;
import javax.swing.JTextField;

import junit.framework.TestCase;

/**
 * 
 * @author keshav
 * @version $Id: MedusaAnalysisTest.java,v 1.2 2007-05-04 20:47:05 keshav Exp $
 */
public class MedusaAnalysisTest extends TestCase {
	MedusaParamPanel panel = null;

	MedusaAnalysis analysis = null;

	@Override
	protected void setUp() {
		panel = new MedusaParamPanel();
		analysis = new MedusaAnalysis();
		analysis.setLabel("MEDUSA");
	}

	/**
	 * 
	 * 
	 */
	public void testExecute() {
		panel.setFeaturesFile("dataset/small_yeast/yeast_test.fasta");

		JTextField minKmer = new JTextField();
		minKmer.setText(String.valueOf(3));
		panel.setMinKmerTextField(minKmer);

		JTextField maxKmer = new JTextField();
		maxKmer.setText(String.valueOf(5));
		panel.setMaxKmerTextField(maxKmer);

		JComboBox dimersCombo = new JComboBox(new String[] { "No", "Yes" });
		dimersCombo.setSelectedIndex(1);
		panel.setDimersCombo(dimersCombo);

		JTextField minGap = new JTextField();
		minGap.setText(String.valueOf(0));
		panel.setDimerMinGapTextField(minGap);

		JTextField maxGap = new JTextField();
		maxGap.setText(String.valueOf(3));
		panel.setDimerMaxGapTextField(maxGap);

		JTextField iter = new JTextField();
		iter.setText(String.valueOf(10));
		panel.setBoostingIterationsTextField(iter);

		JTextField pssm = new JTextField();
		pssm.setText(String.valueOf(18));
		panel.setPssmLengthTextField(pssm);

		JComboBox reverseComplementCombo = new JComboBox(new String[] { "True",
				"False" });
		reverseComplementCombo.setSelectedIndex(0);
		panel.setReverseComplementCombo(reverseComplementCombo);

		analysis.setDefaultPanel(panel);
		analysis.execute(null);
		// analysis.printhelp();
	}

}
