package org.geworkbench.components.discovery;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.InputVerifier;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.border.Border;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.analysis.AbstractSaveableParameterPanel;
import org.geworkbench.bison.datastructure.biocollections.sequences.CSSequenceSet;
import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.util.RegularExpressionVerifier;

import polgara.soapPD_wsdl.Parameters;

/**
 * @author Nikhil
 *
 */
@AcceptTypes( { CSSequenceSet.class })
public class PatternDiscoveryParamPanel extends AbstractSaveableParameterPanel{

	private static final long serialVersionUID = 1L;
	private static Log log = LogFactory.getLog(PatternDiscoveryParamPanel.class);	
	static final String SUPPORT_OCCURRENCES = "Support # Occurrences";
	static final String SUPPORT_SEQUENCES = "Support # Sequences";
	static final String SUPPORT_PERCENT_1_100 = "Support Percent %(1-100)";
	private JTabbedPane jTabbedPane1 = new JTabbedPane();

	private JTextField jMinPatternNoBox = new JTextField();
	private JLabel jMinClusterSizeLabel = new JLabel();
	private JLabel jMinPatternNoLabel = new JLabel();

	// Basic Panel
	private JComboBox jMinSupportMenu = new JComboBox();
	private JTextField jMinSupportBox = new JTextField();
	private JTextField jMinTokensBox = new JTextField();
	private JTextField jMinWTokensBox = new JTextField();
	private JTextField jWindowBox = new JTextField();

	// Advanced Panel
	private JCheckBox jExactOnlyBox = new JCheckBox();
	private JComboBox jAlgoBox = new JComboBox();
	private JComboBox jMatrixBox = new JComboBox();
	private JTextField jSimThresholdBox = new JTextField();

	// Limits Panel
	private JPanel limitPanel = new JPanel();
	private JTextField jMaxPatternNoBox = new JTextField();
	private JTextField jMaxRunTimeBox = new JTextField();

	private JTextField jDecreaseSupportBox = new JTextField();

	private JTextField jEntThreshBox = new JTextField();
	private JTextField jConsRegionExtBox = new JTextField();
	private JTextField jSlidingWindowBox = new JTextField();

	private JTextField jMinSupportExhaustive = new JTextField();

	private JTextField minPatternNumberField = new JTextField();
	private JTextField jMinClusterSizeBox = new JTextField();
	private JCheckBox jUseHMMBox = new JCheckBox();
	private String currentSupportMenuStr = SUPPORT_PERCENT_1_100;
	private int maxSeqNumber;
	
	public PatternDiscoveryParamPanel() {
		this.setLayout(new BorderLayout());
		
		final Dimension size1 = new Dimension(150, 30);
		
		jAlgoBox.addItem("Normal");
		jAlgoBox.addItem("Exhaustive");
		jAlgoBox.setMaximumSize(size1);
		jAlgoBox.setSelectedIndex(0);
		
		jMinSupportMenu.addItem(SUPPORT_PERCENT_1_100);
		jMinSupportMenu.addItem(SUPPORT_SEQUENCES);
		jMinSupportMenu.addItem(SUPPORT_OCCURRENCES);
		jMinSupportMenu.setSelectedIndex(0);
		jMinSupportMenu.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				jSupportMenu_actionPerformed();
			}
		});
		Dimension size2 = new Dimension(200, 30);
		jMinSupportMenu.setMaximumSize(size2);
		jMinSupportBox.setMaximumSize(size1);
		jMinSupportBox.setText("70");
		jMinTokensBox.setMaximumSize(size1);
		jMinTokensBox.setText("7");
		jWindowBox.setMaximumSize(size1);
		jWindowBox.setText("5");
		jMinWTokensBox.setMaximumSize(size1);
		jMinWTokensBox.setText("4");

		jEntThreshBox.setEnabled(true);
		jEntThreshBox.setSelectionEnd(0);
		jEntThreshBox.setSelectionStart(0);
		jEntThreshBox.setText("16");

		jMinSupportExhaustive.setMaximumSize(size1);
		jMinSupportExhaustive.setText("10");

		// add a verifier
		jMinTokensBox.setInputVerifier(new RegularExpressionVerifier(
				"(\\d){1,9}"));
		jMinWTokensBox.setInputVerifier(new RegularExpressionVerifier(
				"(\\d){1,9}"));
		jWindowBox
				.setInputVerifier(new RegularExpressionVerifier("(\\d){1,9}"));
		jMinSupportBox.setInputVerifier(new SupportVerifier(
				"0?\\.?(\\d){1,9}(%)?"));
		// Advanced Panel
		jMatrixBox.addItem("BLOSUM50");
		jMatrixBox.addItem("BLOSUM62");
		jMatrixBox.addItem("BLOSUM100");
		jMatrixBox.setSelectedIndex(0);

		jExactOnlyBox.setSelected(true);
		jMatrixBox.setEnabled(false);
		jSimThresholdBox.setEnabled(false);
		jExactOnlyBox.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if(jExactOnlyBox.isSelected()) {
					jMatrixBox.setEnabled(false);
					jSimThresholdBox.setEnabled(false);
				} else {
					jMatrixBox.setEnabled(true);
					jSimThresholdBox.setEnabled(true);
				}
				
			}
			
		});
		jExactOnlyBox.setText("Exact Only");
		jSimThresholdBox.setText("2");

		// add a verifier
		jSimThresholdBox.setInputVerifier(new RegularExpressionVerifier(
				"(\\d){1,9}"));

		// Limits Panel
		jMaxPatternNoBox.setMaximumSize(size1);
		jMaxPatternNoBox.setText("100000");
		jMaxRunTimeBox.setEnabled(false);
		jMaxRunTimeBox.setMaximumSize(size1);
		jMaxRunTimeBox.setText("0");
		// input verifier
		jMaxPatternNoBox.setInputVerifier(new RegularExpressionVerifier(
				"(\\d){1,9}"));
		jMaxRunTimeBox.setInputVerifier(new RegularExpressionVerifier(
				"(\\d){1,9}"));

		// Exhaustive Panel
		jDecreaseSupportBox.setMaximumSize(size1);
		jDecreaseSupportBox.setText("5");
		jSlidingWindowBox.setEnabled(true);
		jSlidingWindowBox.setText("10");
		jConsRegionExtBox.setEnabled(false);
		jConsRegionExtBox.setText("");

		jMinPatternNoBox.setPreferredSize(new Dimension(20, 20));

		minPatternNumberField.setMaximumSize(size1);
		minPatternNumberField.setText("");
		jMinClusterSizeBox.setPreferredSize(new Dimension(20, 20));
		jMinClusterSizeBox.setText("10");
		jMinClusterSizeBox.setInputVerifier(new RegularExpressionVerifier(
				"(\\d){1,9}"));
		jUseHMMBox.setEnabled(false);
		jUseHMMBox.setText("Use ProfileHMM");

		// input verifier
		jDecreaseSupportBox.setInputVerifier(new RegularExpressionVerifier(
				"(\\d){1,9}(%)?"));

		jMinPatternNoLabel.setText("Min. Pattern Number:");
		jMinPatternNoBox.setText("10");
		jMinClusterSizeLabel.setText("Min. Cluster Size:");

		jMinPatternNoBox.setInputVerifier(new RegularExpressionVerifier(
				"(\\d){1,9}"));

		Border emptyBorder = BorderFactory.createEmptyBorder(5, 5, 5, 5);
		
		JPanel basic0 = new JPanel();
		basic0.setLayout(new BoxLayout(basic0, BoxLayout.LINE_AXIS));
		basic0.setAlignmentX(Component.LEFT_ALIGNMENT);
		JLabel label0 = new JLabel("Algorithm Type:");
		label0.setMaximumSize(size2);
		basic0.add(label0);
		basic0.add(jAlgoBox);
		
		JPanel basic1 = new JPanel();
		basic1.setLayout(new BoxLayout(basic1, BoxLayout.LINE_AXIS));
		basic1.setAlignmentX(Component.LEFT_ALIGNMENT);
		basic1.add(jMinSupportMenu);
		basic1.add(jMinSupportBox);
		
		JPanel basic2 = new JPanel();
		basic2.setLayout(new BoxLayout(basic2, BoxLayout.LINE_AXIS));
		basic2.setAlignmentX(Component.LEFT_ALIGNMENT);
		JLabel label1 = new JLabel("Min. Tokens:");
		label1.setMaximumSize(size2);
		basic2.add(label1);
		basic2.add(jMinTokensBox);
		
		JPanel basic3 = new JPanel();
		basic3.setLayout(new BoxLayout(basic3, BoxLayout.LINE_AXIS));
		basic3.setAlignmentX(Component.LEFT_ALIGNMENT);
		JLabel label2 = new JLabel("Density Window:");
		label2.setMaximumSize(size2);
		basic3.add(label2);
		basic3.add(jWindowBox);
		
		JPanel basic4 = new JPanel();
		basic4.setLayout(new BoxLayout(basic4, BoxLayout.LINE_AXIS));
		basic4.setAlignmentX(Component.LEFT_ALIGNMENT);
		JLabel label3 = new JLabel("Density Window Min. Tokens:");
		label3.setMaximumSize(size2);
		basic4.add(label3);
		basic4.add(jMinWTokensBox);
		
		JPanel basicPanel = new JPanel();
		basicPanel.setLayout(new BoxLayout(basicPanel, BoxLayout.PAGE_AXIS));
		basicPanel.setBorder(emptyBorder);
		basicPanel.add(basic0);
		basicPanel.add(basic1);
		basicPanel.add(basic2);
		basicPanel.add(basic3);
		basicPanel.add(basic4);
		

		jExactOnlyBox.setAlignmentY(Component.TOP_ALIGNMENT);
		JPanel advancedPanel = new JPanel();
		advancedPanel.setLayout(new BoxLayout(advancedPanel, BoxLayout.LINE_AXIS));
		advancedPanel.setBorder(emptyBorder);
		advancedPanel.add(Box.createRigidArea(new Dimension(5,0)));
		advancedPanel.add(jExactOnlyBox);
		advancedPanel.add(Box.createRigidArea(new Dimension(5,0)));

		JPanel advancedRightPanel = new JPanel();
		advancedRightPanel.setAlignmentY(Component.TOP_ALIGNMENT);
		advancedRightPanel.setLayout(new BoxLayout(advancedRightPanel, BoxLayout.PAGE_AXIS));
		jMatrixBox.setMaximumSize(new Dimension(200, 30));
		advancedRightPanel.add(jMatrixBox);
		advancedRightPanel.add(Box.createRigidArea(new Dimension(0,5)));
		JPanel similarityThresholdPanel = new JPanel();
		similarityThresholdPanel.setLayout(new BoxLayout(similarityThresholdPanel, BoxLayout.LINE_AXIS));
		similarityThresholdPanel.setMaximumSize(new Dimension(200, 30));
		similarityThresholdPanel.add(new JLabel("Similarity Threshold:"));
		similarityThresholdPanel.add(jSimThresholdBox);
		advancedRightPanel.add(similarityThresholdPanel);
		advancedPanel.add(advancedRightPanel);
		
		limitPanel.setLayout(new BoxLayout(limitPanel, BoxLayout.PAGE_AXIS));
		limitPanel.setBorder(emptyBorder);
		JPanel limitTopPanel = new JPanel();
		limitTopPanel.setLayout(new BoxLayout(limitTopPanel, BoxLayout.LINE_AXIS));
		limitTopPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		limitTopPanel.add(new JLabel("Max. Pattern Number: "));
		limitTopPanel.add(jMaxPatternNoBox);
		JPanel limitBottomPanel = new JPanel();
		limitBottomPanel.setLayout(new BoxLayout(limitBottomPanel, BoxLayout.LINE_AXIS));
		limitBottomPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		limitBottomPanel.add(new JLabel("Max. Run Time (sec.):"));
		limitBottomPanel.add(jMaxRunTimeBox);
		limitPanel.add(limitTopPanel);
		limitPanel.add(limitBottomPanel);

		JPanel exhaustive1 = new JPanel();
		exhaustive1.setLayout(new BoxLayout(exhaustive1, BoxLayout.LINE_AXIS));
		exhaustive1.setBorder(emptyBorder);
		JLabel label4 = new JLabel("Decrease Support (%):");
		label4.setMaximumSize(size1);
		exhaustive1.add(label4);
		exhaustive1.add(jDecreaseSupportBox);

		JPanel exhaustive2 = new JPanel();
		exhaustive2.setLayout(new BoxLayout(exhaustive2, BoxLayout.LINE_AXIS));
		exhaustive2.setBorder(emptyBorder);
		JLabel label5 = new JLabel("Minimum Support (%):");
		label5.setMaximumSize(size1);
		exhaustive2.add(label5);
		exhaustive2.add(jMinSupportExhaustive);

		JPanel exhaustive3 = new JPanel();
		exhaustive3.setLayout(new BoxLayout(exhaustive3, BoxLayout.LINE_AXIS));
		exhaustive3.setBorder(emptyBorder);
		JLabel label6 = new JLabel("Minimum Pattern Number:");
		label6.setMaximumSize(size1);
		exhaustive3.add(label6);
		exhaustive3.add(minPatternNumberField);
		
		JPanel exhaustivePanel = new JPanel();
		exhaustivePanel.setLayout(new BoxLayout(exhaustivePanel, BoxLayout.PAGE_AXIS));
		exhaustivePanel.setBorder(emptyBorder);
		exhaustive1.setAlignmentX(Component.LEFT_ALIGNMENT);
		exhaustive2.setAlignmentX(Component.LEFT_ALIGNMENT);
		exhaustive3.setAlignmentX(Component.LEFT_ALIGNMENT);
		exhaustivePanel.add(exhaustive1);
		exhaustivePanel.add(exhaustive2);
		exhaustivePanel.add(exhaustive3);
		JPanel exhaustiveOccurrencePanel = new JPanel();
		exhaustiveOccurrencePanel.setLayout(new BoxLayout(exhaustiveOccurrencePanel, BoxLayout.LINE_AXIS));
		exhaustiveOccurrencePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

		JPanel exhaustiveSequencePanel = new JPanel();
		exhaustiveSequencePanel.setLayout(new BoxLayout(exhaustiveSequencePanel, BoxLayout.LINE_AXIS));
		exhaustiveSequencePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

		exhaustivePanel.add(exhaustiveOccurrencePanel);
		exhaustivePanel.add(exhaustiveSequencePanel);

		jTabbedPane1.add(basicPanel, "Basic");
		jTabbedPane1.add(exhaustivePanel, "Exhaustive");
		jTabbedPane1.add(limitPanel, "Limits");
		jTabbedPane1.add(advancedPanel, "Advanced");
		this.add(jTabbedPane1, BorderLayout.NORTH);
		jTabbedPane1.setSelectedIndex(0);
	} 
	
	// this class is used for the parametorPanel for verification of input.
	// It uses a regular expression for the verification
	private class SupportVerifier extends InputVerifier {
		// TEXT_FIELD = "^(\\S)(.){1,75}(\\S)$";
		// NON_NEGATIVE_INTEGER_FIELD = "(\\d){1,9}";
		// INTEGER_FIELD = "(-)?" + NON_NEGATIVE_INTEGER_FIELD;
		// NON_NEGATIVE_FLOATING_POINT_FIELD ="(\\d){1,10}(.)?(\\d){1,10}";
		// FLOATING_POINT_FIELD = "(-)?" +NON_NEGATIVE_FLOATING_POINT_FIELD;
		// NON_NEGATIVE_MONEY_FIELD = "(\\d){1,15}(\\.(\\d){2})?";
		// MONEY_FIELD = "(-)?" + NON_NEGATIVE_MONEY_FIELD;
		Pattern p = null;

		public SupportVerifier(String regexp) {
			p = Pattern.compile(regexp);
		}

		public boolean shouldYieldFocus(JComponent input) {
			if (verify(input)) {
				return true;
			}
			// According to the documentation, should yield focus is allowed to
			// cause
			// side effects. So temporarily remove the input verifier on the
			// text
			// field.
			input.setInputVerifier(null);
			// Pop up the message dialog.
			String message = "Data input is not valid, please check and input correct data ";
			JOptionPane.showMessageDialog(null, message, "Invalid value",
					JOptionPane.WARNING_MESSAGE);

			// Reinstall the input verifier.
			input.setInputVerifier(this);
			// Tell whomever called us that we don't want to yield focus.
			return false;
		}

		public boolean verify(JComponent input) {
			JTextField tf = (JTextField) input;
			Matcher m = p.matcher(tf.getText());
			boolean match = m.matches();
			if (!match) {
				return match;
			}
			if (currentSupportMenuStr.equalsIgnoreCase(SUPPORT_PERCENT_1_100)) {
				String currentSupportStr = tf.getText().trim();
				if (currentSupportStr == null) {
					return false;
				}
				if (currentSupportStr.endsWith("%")) {
					currentSupportStr = currentSupportStr.substring(0,
							currentSupportStr.length() - 1);
				}
				if (new Double(currentSupportStr).doubleValue() > 100) {
					return false;
				}
			}
			if (currentSupportMenuStr.equalsIgnoreCase(SUPPORT_SEQUENCES)) {
				if (new Integer(tf.getText().trim()).intValue() > maxSeqNumber) {
					return false;
				}
			}

			return match;
		}// end of verify()
	}	

	private void jSupportMenu_actionPerformed() {
		String selectedSupportStr = (String) jMinSupportMenu.getSelectedItem();
		if (!currentSupportMenuStr.equalsIgnoreCase(selectedSupportStr)) {
			log.debug(currentSupportMenuStr+"=>"+selectedSupportStr);
			jMinSupportBox.setText("");
			this.revalidate();

		}
		currentSupportMenuStr = selectedSupportStr;
	} 
	
	// ------------------------BASIC PANEL
	public String getSelectedAlgorithmName() {
		return ((String) jAlgoBox.getSelectedItem());
	}
	
	public String getMinSupport() {
		return (jMinSupportBox.getText().trim());
	}

	public int getMinWTokens() {
		return (Integer.parseInt(jMinWTokensBox.getText()));
	}

	public int getMinTokens() {
		return (Integer.parseInt(jMinTokensBox.getText()));
	}

	public int getWindow() {
		return (Integer.parseInt(jWindowBox.getText()));
	}

	/**
	 * @return the maxSeqNumber
	 */
	public int getMaxSeqNumber() {
		return maxSeqNumber;
	}

	/**
	 * @param maxSeqNumber
	 *            the maxSeqNumber to set
	 */
	public void setMaxSeqNumber(int maxSeqNumber) {
		this.maxSeqNumber = maxSeqNumber;
	}

	// Parsing ADVANCED Panel
	public int getExactOnlySelected() {
		return (jExactOnlyBox.isSelected() ? 1 : 0);
	}

	// always true ('selected'). not on GUI
	public int getPValueBoxSelected() {
		return 1;
	}

	public String getMatrixSelection() {
		return ((String) jMatrixBox.getSelectedItem());
	}

	public double getSimilarityThreshold() {
		return (Double.parseDouble(jSimThresholdBox.getText()));
	}

	// always return this. not shown on GUI.
	public double getMinPValue() {
		return -10000.;
	}

	public double getProfileEntropy() {
		return Double.parseDouble(jEntThreshBox.getText());
	}

	public int getProfileWindow() {
		return Integer.parseInt(jSlidingWindowBox.getText());
	}

	// Parsing the LIMITS panel
	public int getMaxPatternNo() {
		return (Integer.parseInt(jMaxPatternNoBox.getText()));
		// parms.setGroupingType(groupType);
	}

	public int getMaxRunTime() {
		return (Integer.parseInt(jMaxRunTimeBox.getText()));
		// parms.setGroupingType(groupType);
	}

	// parsing HIEARCHICAL panel
	public int getMinClusterSize() {
		return (Integer.parseInt(jMinClusterSizeBox.getText()));
	}

	public int getMinPatternNo() {
		return (Integer.parseInt(jMinPatternNoBox.getText()));
	}

	/**
	 * @return the currentSupportMenuStr
	 */
	public String getCurrentSupportMenuStr() {
		return currentSupportMenuStr;
	}

	/**
	 * @param currentSupportMenuStr
	 *            the currentSupportMenuStr to set
	 */
	public void setCurrentSupportMenuStr(String currentSupportMenuStr) {
		this.currentSupportMenuStr = currentSupportMenuStr;

	}

	public boolean useHMM() {
		return jUseHMMBox.isSelected();
	}
	
	public void setParams(Parameters parms) {
		final DecimalFormat format = new DecimalFormat("####.##");

		jMinSupportMenu.setSelectedItem(currentSupportMenuStr);
		if (currentSupportMenuStr.equalsIgnoreCase(SUPPORT_PERCENT_1_100)) {
			String support = format.format(parms.getMinPer100Support() * 100);
			jMinSupportBox.setText(support);
		} else {
			jMinSupportBox.setText(format.format(parms.getMinSupport()));
		}

		jMinTokensBox.setText(Integer.toString(parms.getMinTokens()));
		jWindowBox.setText(Integer.toString(parms.getWindow()));
		jMinWTokensBox.setText(Integer.toString(parms.getMinWTokens()));

		// Parsing the ADVANCED panel
		jExactOnlyBox.setSelected((parms.getExact() == 1) ? true : false);

		jMatrixBox.setSelectedItem(parms.getSimilarityMatrix());
		jSimThresholdBox.setText(format.format(parms.getSimilarityThreshold()));

		// Parsing the HIERARCHICAL panel
		if (parms != null && parms.getHierarchical() != null)
			jMinClusterSizeBox.setText(Integer.toString(parms.getHierarchical()
					.getClusterSize()));

		jMinPatternNoBox.setText(Integer.toString(parms.getMinPatternNo()));

		// Parsing the LIMITS panel
		jMaxPatternNoBox.setText(Integer.toString(parms.getMaxPatternNo()));
		jMaxRunTimeBox.setText(Integer.toString(parms.getMaxRunTime()));

	}
		
	public void setParameters(Map<Serializable, Serializable> parameters) {
		//TODO
	}
	
	public String getDecSupportExhaustive() {
		return jDecreaseSupportBox.getText().trim();
	}

	public String getMinSupportExhaustive() {
		return jMinSupportExhaustive.getText().trim();
	}

	@Override
	public Map<Serializable, Serializable> getParameters() {
		Map<Serializable, Serializable> parameters = new HashMap<Serializable, Serializable>();
		
		return parameters;
	}

	public void fillDefaultValues(Map<Serializable, Serializable> parameters) {
		// TODO Auto-generated method stub
		
	}

}
