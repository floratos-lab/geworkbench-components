package org.geworkbench.components.discovery;

import java.lang.NumberFormatException;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.DefaultSingleSelectionModel;
import java.awt.KeyboardFocusManager;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.InputVerifier;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.border.Border;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.analysis.AbstractSaveableParameterPanel;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.sequences.CSSequenceSet;
import org.geworkbench.bison.datastructure.biocollections.sequences.DSSequenceSet;
import org.geworkbench.bison.datastructure.complex.pattern.PatternResult;
import org.geworkbench.bison.model.analysis.ParamValidationResults;
import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.events.listeners.ParameterActionListener;
import org.geworkbench.util.RegularExpressionVerifier;

/**
 * @author Nikhil
 * 
 */
@AcceptTypes({ CSSequenceSet.class })
public class PatternDiscoveryParamPanel extends AbstractSaveableParameterPanel {

	private static final long serialVersionUID = 7792584092654744039L;

	private static Log log = LogFactory
			.getLog(PatternDiscoveryParamPanel.class);
	static final String SUPPORT_OCCURRENCES = "Support (Number of Occurrences)";
	static final String SUPPORT_SEQUENCES = "Support (Number of Sequences)";
	static final String SUPPORT_PERCENT_1_100 = "Support (Percent of Sequences)";

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
	private JComboBox jMatrixBox = new JComboBox();
	private JTextField jSimThresholdBox = new JTextField();

	// Limits Panel
	private JTextField jMaxPatternNoBox = new JTextField();

	private JTextField jDecreaseSupportBox = new JTextField();

	private JTextField jEntThreshBox = new JTextField();
	private JTextField jConsRegionExtBox = new JTextField();
	private JTextField jSlidingWindowBox = new JTextField();

	private JTextField jMinSupportExhaustive = new JTextField();

	private JTextField minPatternNumberField = new JTextField();
	private JTextField jMinClusterSizeBox = new JTextField();
	private JCheckBox jUseHMMBox = new JCheckBox();

	private String currentSupportMenuStr = SUPPORT_PERCENT_1_100;
	private int maxSeqNumber = Integer.MAX_VALUE;

	private JRadioButton normal = new JRadioButton("Normal");
	private JRadioButton exhaustive = new JRadioButton("Exhaustive");

	private ParameterActionListener parameterActionListener;

	public PatternDiscoveryParamPanel() {
		this.setLayout(new BorderLayout());

		final Dimension size1 = new Dimension(150, 30);

		parameterActionListener = new ParameterActionListener(this);

		normal.setSelected(true);
		ButtonGroup algorithmGroup = new ButtonGroup();
		algorithmGroup.add(normal);
		algorithmGroup.add(exhaustive);

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

		Dimension size2 = new Dimension(220, 30);
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
		jMinSupportExhaustive.setInputVerifier(new PercentNumberVerifier(
				"((\\d){1,10}(.))?(\\d){1,10}"));

		// add a verifier
		jMinTokensBox.setInputVerifier(new RegularExpressionVerifier(
				"(\\d){1,9}"));
		jMinWTokensBox.setInputVerifier(new RegularExpressionVerifier(
				"(\\d){1,9}"));
		jWindowBox
				.setInputVerifier(new RegularExpressionVerifier("(\\d){1,9}"));
		jMinSupportBox.setInputVerifier(new BasicMinSupportVerifier(
				"((\\d){1,10}(.))?(\\d){1,10}"));

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
				if (jExactOnlyBox.isSelected()) {
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

		// input verifier
		jMaxPatternNoBox.setInputVerifier(new RegularExpressionVerifier(
				"(\\d){1,9}"));

		// Exhaustive Panel
		jDecreaseSupportBox.setMaximumSize(size1);
		jDecreaseSupportBox.setText("5");
		jSlidingWindowBox.setEnabled(true);
		jSlidingWindowBox.setText("10");
		jConsRegionExtBox.setEnabled(false);
		jConsRegionExtBox.setText("");

		minPatternNumberField.setMaximumSize(size1);
		minPatternNumberField.setText("10");
		minPatternNumberField.setInputVerifier(new RegularExpressionVerifier(
				"((\\d){1,10})?"));
		jMinClusterSizeBox.setPreferredSize(new Dimension(20, 20));
		jMinClusterSizeBox.setText("10");
		jMinClusterSizeBox.setInputVerifier(new RegularExpressionVerifier(
				"(\\d){1,9}"));
		jUseHMMBox.setEnabled(false);
		jUseHMMBox.setText("Use ProfileHMM");

		// input verifier
		jDecreaseSupportBox.setInputVerifier(new PercentNumberVerifier(
				"((\\d){1,10}(.))?(\\d){1,10}"));

		jMinPatternNoLabel.setText("Min. Pattern Number:");
		jMinClusterSizeLabel.setText("Min. Cluster Size:");

		minPatternNumberField.setInputVerifier(new RegularExpressionVerifier(
				"(\\d){1,9}"));

		Border emptyBorder = BorderFactory.createEmptyBorder(5, 5, 5, 5);

		JPanel basic0 = new JPanel();
		basic0.setLayout(new BoxLayout(basic0, BoxLayout.LINE_AXIS));
		basic0.setAlignmentX(Component.LEFT_ALIGNMENT);
		JLabel label0 = new JLabel("Algorithm Type:");
		label0.setMaximumSize(size2);
		basic0.add(label0);
		basic0.add(normal);
		basic0.add(exhaustive);

		JPanel basic1 = new JPanel();
		basic1.setLayout(new BoxLayout(basic1, BoxLayout.LINE_AXIS));
		basic1.setAlignmentX(Component.LEFT_ALIGNMENT);
		basic1.add(jMinSupportMenu);
		basic1.add(jMinSupportBox);

		JPanel basic2 = new JPanel();
		basic2.setLayout(new BoxLayout(basic2, BoxLayout.LINE_AXIS));
		basic2.setAlignmentX(Component.LEFT_ALIGNMENT);
		JLabel label1 = new JLabel("Minimum Tokens:");
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

		JPanel advancedPanel1 = new JPanel();
		advancedPanel1.setLayout(new BoxLayout(advancedPanel1,
				BoxLayout.LINE_AXIS));
		JLabel advLabel1 = new JLabel("Substitution Matrix:");
		advLabel1.setMaximumSize(size1);
		advancedPanel1.add(advLabel1);
		jExactOnlyBox.setMaximumSize(size1);
		advancedPanel1.add(jExactOnlyBox);

		JPanel advancedPanel2 = new JPanel();
		advancedPanel2.setLayout(new BoxLayout(advancedPanel2,
				BoxLayout.LINE_AXIS));
		JLabel advLabel2 = new JLabel("Similarity Matrix:");
		advLabel2.setMaximumSize(size1);
		advancedPanel2.add(advLabel2);
		jMatrixBox.setMaximumSize(size1);
		advancedPanel2.add(jMatrixBox);

		JPanel similarityThresholdPanel = new JPanel();
		similarityThresholdPanel.setLayout(new BoxLayout(
				similarityThresholdPanel, BoxLayout.LINE_AXIS));
		JLabel advLabel3 = new JLabel("Similarity Threshold:");
		advLabel3.setMaximumSize(size1);
		similarityThresholdPanel.add(advLabel3);
		jSimThresholdBox.setMaximumSize(size1);
		similarityThresholdPanel.add(jSimThresholdBox);

		JPanel advancedPanel = new JPanel();
		advancedPanel.setLayout(new BoxLayout(advancedPanel,
				BoxLayout.PAGE_AXIS));
		advancedPanel.setBorder(emptyBorder);

		advancedPanel1.setAlignmentX(Component.LEFT_ALIGNMENT);
		advancedPanel2.setAlignmentX(Component.LEFT_ALIGNMENT);
		similarityThresholdPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		advancedPanel.add(advancedPanel1);
		advancedPanel.add(advancedPanel2);
		advancedPanel.add(similarityThresholdPanel);

		JPanel limitPanel = new JPanel();
		limitPanel.setLayout(new BoxLayout(limitPanel, BoxLayout.PAGE_AXIS));
		limitPanel.setBorder(emptyBorder);
		JPanel limitTopPanel = new JPanel();
		limitTopPanel.setLayout(new BoxLayout(limitTopPanel,
				BoxLayout.LINE_AXIS));
		limitTopPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		limitTopPanel.add(new JLabel("Maximum Pattern Number: "));
		limitTopPanel.add(jMaxPatternNoBox);
		limitPanel.add(limitTopPanel);
		/* no longer used for now */
		/*
		 * JPanel limitBottomPanel = new JPanel();
		 * limitBottomPanel.setLayout(new BoxLayout(limitBottomPanel,
		 * BoxLayout.LINE_AXIS));
		 * limitBottomPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		 * limitBottomPanel.add(new JLabel("Maximum Run Time (sec.):"));
		 * limitBottomPanel.add(jMaxRunTimeBox);
		 * limitPanel.add(limitBottomPanel);
		 */

		Dimension size3 = new Dimension(235, 30);
		JPanel exhaustive1 = new JPanel();
		exhaustive1.setLayout(new BoxLayout(exhaustive1, BoxLayout.LINE_AXIS));
		exhaustive1.setBorder(emptyBorder);
		JLabel label4 = new JLabel("Decrement Support (%):");
		label4.setMaximumSize(size3);
		exhaustive1.add(label4);
		exhaustive1.add(Box.createRigidArea(new Dimension(1, 0)));
		exhaustive1.add(jDecreaseSupportBox);

		JPanel exhaustive2 = new JPanel();
		exhaustive2.setLayout(new BoxLayout(exhaustive2, BoxLayout.LINE_AXIS));
		exhaustive2.setBorder(emptyBorder);
		JLabel label5 = new JLabel("Minimum Support (Number of Sequences):");
		label5.setMaximumSize(size3);
		exhaustive2.add(label5);
		exhaustive2.add(jMinSupportExhaustive);

		JPanel exhaustive3 = new JPanel();
		exhaustive3.setLayout(new BoxLayout(exhaustive3, BoxLayout.LINE_AXIS));
		exhaustive3.setBorder(emptyBorder);
		JLabel label6 = new JLabel("Minimum Pattern Number:");
		label6.setMaximumSize(size3);
		exhaustive3.add(label6);
		exhaustive3.add(Box.createRigidArea(new Dimension(1, 0)));
		exhaustive3.add(minPatternNumberField);

		JPanel exhaustivePanel = new JPanel();
		exhaustivePanel.setLayout(new BoxLayout(exhaustivePanel,
				BoxLayout.PAGE_AXIS));
		exhaustivePanel.setBorder(emptyBorder);
		exhaustive1.setAlignmentX(Component.LEFT_ALIGNMENT);
		exhaustive2.setAlignmentX(Component.LEFT_ALIGNMENT);
		exhaustive3.setAlignmentX(Component.LEFT_ALIGNMENT);
		exhaustivePanel.add(exhaustive1);
		exhaustivePanel.add(exhaustive2);
		exhaustivePanel.add(exhaustive3);

		JTabbedPane jTabbedPane1;
		jTabbedPane1 = new JTabbedPane();
		jTabbedPane1.add(basicPanel, "Basic");
		jTabbedPane1.add(exhaustivePanel, "Exhaustive");
		jTabbedPane1.add(limitPanel, "Limits");
		jTabbedPane1.add(advancedPanel, "Advanced");
		jTabbedPane1.setSelectedIndex(0);

		// set a new model with a custom setSelectedIndex method
		jTabbedPane1.setModel(new DefaultSingleSelectionModel() {

			private static final long serialVersionUID = -6555370509260654118L;

			public void setSelectedIndex(int index) {
				Component compWithFocus = KeyboardFocusManager
						.getCurrentKeyboardFocusManager().getFocusOwner();

				if (compWithFocus != null
						&& compWithFocus instanceof JTextField) {
					JTextField tf = (JTextField) compWithFocus;
					if (tf.getInputVerifier() != null) {
						if (!tf.getInputVerifier().verify(tf)) {
							String message = "Data input is not valid, please check and input correct data ";
							JOptionPane.showMessageDialog(null, message,
									"Invalid value",
									JOptionPane.WARNING_MESSAGE);
							return;
						}
					}

				}
				super.setSelectedIndex(index);
			}

		});

		normal.addActionListener(parameterActionListener);
		exhaustive.addActionListener(parameterActionListener);
		jMinSupportMenu.addActionListener(parameterActionListener);	 
		jMinSupportBox.addActionListener(parameterActionListener);
		jMinTokensBox.addActionListener(parameterActionListener);
		jWindowBox.addActionListener(parameterActionListener);
		jMinWTokensBox.addActionListener(parameterActionListener);

		jDecreaseSupportBox.addActionListener(parameterActionListener);
		jMinSupportExhaustive.addActionListener(parameterActionListener);
		minPatternNumberField.addActionListener(parameterActionListener);

		jMaxPatternNoBox.addActionListener(parameterActionListener);

		jExactOnlyBox.addActionListener(parameterActionListener);
		jMatrixBox.addActionListener(parameterActionListener);
		jSimThresholdBox.addActionListener(parameterActionListener);   

		this.setPreferredSize(new Dimension(400, 200));
		this.add(jTabbedPane1, BorderLayout.CENTER);
	}

	// this class is used for the parametorPanel for verification of input.
	// It uses a regular expression for the verification
	private class BasicMinSupportVerifier extends InputVerifier {
		// TEXT_FIELD = "^(\\S)(.){1,75}(\\S)$";
		// NON_NEGATIVE_INTEGER_FIELD = "(\\d){1,9}";
		// INTEGER_FIELD = "(-)?" + NON_NEGATIVE_INTEGER_FIELD;
		// NON_NEGATIVE_FLOATING_POINT_FIELD ="(\\d){1,10}(.)?(\\d){1,10}";
		// FLOATING_POINT_FIELD = "(-)?" +NON_NEGATIVE_FLOATING_POINT_FIELD;
		// NON_NEGATIVE_MONEY_FIELD = "(\\d){1,15}(\\.(\\d){2})?";
		// MONEY_FIELD = "(-)?" + NON_NEGATIVE_MONEY_FIELD;
		Pattern p = null;

		public BasicMinSupportVerifier(String regexp) {
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
				if (new Double(currentSupportStr).doubleValue() > 100) {
					return false;
				}
			}
			if (currentSupportMenuStr.equalsIgnoreCase(SUPPORT_SEQUENCES)
					|| currentSupportMenuStr
							.equalsIgnoreCase(SUPPORT_OCCURRENCES)) {
				try {
					if (new Integer(tf.getText().trim()).intValue() > maxSeqNumber) {
						return false;
					}
				} catch (NumberFormatException ne) {
					return false;
				}
			}

			return match;
		}// end of verify()
	}

	private class PercentNumberVerifier extends InputVerifier {
		// TEXT_FIELD = "^(\\S)(.){1,75}(\\S)$";
		// NON_NEGATIVE_INTEGER_FIELD = "(\\d){1,9}";
		// INTEGER_FIELD = "(-)?" + NON_NEGATIVE_INTEGER_FIELD;
		// NON_NEGATIVE_FLOATING_POINT_FIELD ="(\\d){1,10}(.)?(\\d){1,10}";
		// FLOATING_POINT_FIELD = "(-)?" +NON_NEGATIVE_FLOATING_POINT_FIELD;
		// NON_NEGATIVE_MONEY_FIELD = "(\\d){1,15}(\\.(\\d){2})?";
		// MONEY_FIELD = "(-)?" + NON_NEGATIVE_MONEY_FIELD;
		Pattern p = null;

		public PercentNumberVerifier(String regexp) {
			p = Pattern.compile(regexp);
		}

		public boolean shouldYieldFocus(JComponent input) {
			if (verify(input)) {
				return true;
			}
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

			String currentSupportStr = tf.getText().trim();
			if (currentSupportStr == null) {
				return false;
			}
			if (new Double(currentSupportStr).doubleValue() > 100) {
				return false;
			}

			return match;
		}// end of verify()
	}

	private void jSupportMenu_actionPerformed() {
		String selectedSupportStr = (String) jMinSupportMenu.getSelectedItem();
		if (!currentSupportMenuStr.equalsIgnoreCase(selectedSupportStr)) {
			log.debug(currentSupportMenuStr + "=>" + selectedSupportStr);
			jMinSupportBox.setText("");
			this.revalidate();

		}
		currentSupportMenuStr = selectedSupportStr;
	}

	// ------------------------BASIC PANEL
	public String getSelectedAlgorithmName() {
		if (normal.isSelected()) {
			return PatternResult.DISCOVER;
		} else if (exhaustive.isSelected()) {
			return PatternResult.EXHAUSTIVE;
		} else {
			log.error("Unexpected choice");
			return null;
		}
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

	// not implemented. It was from jMaxRunTimeBox before.
	public int getMaxRunTime() {
		return 0;
	}

	// parsing HIEARCHICAL panel
	public int getMinClusterSize() {
		return (Integer.parseInt(jMinClusterSizeBox.getText()));
	}

	public int getMinPatternNo() {
		return (Integer.parseInt(minPatternNumberField.getText()));
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

	public void setParameters(Map<Serializable, Serializable> parameters) {
		Set<Map.Entry<Serializable, Serializable>> set = parameters.entrySet();

		if ((getStopNotifyAnalysisPanelTemporaryFlag()==true)&&(parameterActionListener.getCalledFromProgramFlag()==true)) return;
    	stopNotifyAnalysisPanelTemporary(true);		
		
		for (Iterator<Map.Entry<Serializable, Serializable>> iterator = set
				.iterator(); iterator.hasNext();) {
			Map.Entry<Serializable, Serializable> parameter = iterator.next();
			Object key = parameter.getKey();
			Object value = parameter.getValue();

			if (key.equals("jMinSupportMenu")) {
				if (value != null
						&& !value.toString().equalsIgnoreCase(
								jMinSupportMenu.getSelectedItem().toString()))
					jMinSupportMenu.setSelectedItem(value);
			} else if (key.equals("normal")) {
				if (value != null) {
					normal.setSelected((Boolean) value);
				}

			} else if (key.equals("exhaustive")) {
				if (value != null) {
					exhaustive.setSelected((Boolean) value);
				}

			} else if (key.equals("jMinSupportBox")) {
				if (value != null && !value.toString().trim().equals("")) {
					jMinSupportBox.setText(value.toString());
				}

			} else if (key.equals("jMinTokensBox")) {
				if (value != null && !value.toString().trim().equals("")) {
					jMinTokensBox.setText(value.toString());
				}

			} else if (key.equals("jWindowBox")) {
				if (value != null && !value.toString().trim().equals("")) {
					jWindowBox.setText(value.toString());
				}

			} else if (key.equals("jMinWTokensBox")) {
				if (value != null && !value.toString().trim().equals("")) {
					jMinWTokensBox.setText(value.toString());
				}
			} else if (key.equals("jDecreaseSupportBox")) {
				if (value != null && !value.toString().trim().equals("")) {
					jDecreaseSupportBox.setText(value.toString());
				}
			} else if (key.equals("jMinSupportExhaustive")) {
				if (value != null && !value.toString().trim().equals("")) {
					jMinSupportExhaustive.setText(value.toString());
				}
			} else if (key.equals("minPatternNumberField")) {
				if (value != null && !value.toString().trim().equals("")) {
					minPatternNumberField.setText(value.toString());
				}
			} else if (key.equals("jMaxPatternNoBox")) {
				if (value != null && !value.toString().trim().equals("")) {
					jMaxPatternNoBox.setText(value.toString());
				}
			} else if (key.equals("jExactOnlyBox")) {
				if (value != null) {
					jExactOnlyBox.setSelected((Boolean) value);
				}
				if (jExactOnlyBox.isSelected()) {
					jMatrixBox.setEnabled(false);
					jSimThresholdBox.setEnabled(false);
				} else {
					jMatrixBox.setEnabled(true);
					jSimThresholdBox.setEnabled(true);
				}

			} else if (key.equals("jMatrixBox")) {
				if (value != null && !value.toString().trim().equals("")) {
					jMatrixBox.setSelectedItem(value);
				}
			} else if (key.equals("jSimThresholdBox")) {
				if (value != null && !value.toString().trim().equals("")) {
					jSimThresholdBox.setText(value.toString());
				}

			}

		}
		stopNotifyAnalysisPanelTemporary(false);

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

		parameters.put("normal", new Boolean(normal.isSelected()));
		parameters.put("exhaustive", new Boolean(exhaustive.isSelected()));
		parameters.put("jMinSupportMenu", jMinSupportMenu.getSelectedItem()
				.toString());
		parameters.put("jMinSupportBox", jMinSupportBox.getText().trim());
		parameters.put("jMinTokensBox", jMinTokensBox.getText().trim());
		parameters.put("jWindowBox", jWindowBox.getText().trim());
		parameters.put("jMinWTokensBox",jMinWTokensBox.getText().trim());
		parameters.put("jDecreaseSupportBox", jDecreaseSupportBox.getText().trim());
		parameters.put("jMinSupportExhaustive", jMinSupportExhaustive.getText().trim());
		parameters.put("minPatternNumberField", minPatternNumberField.getText().trim());
		parameters.put("jMaxPatternNoBox", jMaxPatternNoBox.getText().trim());
		parameters.put("jExactOnlyBox",new Boolean(jExactOnlyBox.isSelected()));
		parameters.put("jMatrixBox", jMatrixBox.getSelectedItem().toString());
		parameters.put("jSimThresholdBox", jSimThresholdBox.getText().trim());

		return parameters;
	}

	@Override
	public ParamValidationResults validateParameters() {

		if (jMinSupportBox.getInputVerifier().verify(jMinSupportBox) == false) {
			jMinSupportBox.requestFocus();
			return new ParamValidationResults(false,
					"Please enter valid data for " + currentSupportMenuStr
							+ "in Basic window.");

		}

		if (jMinTokensBox.getInputVerifier().verify(jMinTokensBox) == false) {
			jMinTokensBox.requestFocus();
			return new ParamValidationResults(false,
					"Please enter valid data for Minimum Tokens in Basic window.");
		}

		if (jWindowBox.getInputVerifier().verify(jWindowBox) == false) {
			jWindowBox.requestFocus();
			return new ParamValidationResults(false,
					"Please enter valid data for Density Window in Basic window.");
		}

		if (jMinWTokensBox.getInputVerifier().verify(jMinWTokensBox) == false) {
			jMinWTokensBox.requestFocus();
			return new ParamValidationResults(false,
					"Please enter valid data for Density Window Min. Tokens in Basic window.");
		}

		if (jDecreaseSupportBox.getInputVerifier().verify(jDecreaseSupportBox) == false) {
			jDecreaseSupportBox.requestFocus();
			return new ParamValidationResults(false,
					"Please enter valid data for Decrement Support in Exhaustive window.");
		}
		if (jMinSupportExhaustive.getInputVerifier().verify(
				jMinSupportExhaustive) == false) {
			jMinSupportExhaustive.requestFocus();
			return new ParamValidationResults(false,
					"Please enter valid data for Minimum Support in Exhaustive window.");
		}
		if (minPatternNumberField.getInputVerifier().verify(
				minPatternNumberField) == false) {
			minPatternNumberField.requestFocus();
			return new ParamValidationResults(false,
					"Please enter valid data for Minimum Pattern Support in Exhaustive window.");
		}

		if (jMaxPatternNoBox.getInputVerifier().verify(jMaxPatternNoBox) == false) {
			jMaxPatternNoBox.requestFocus();
			return new ParamValidationResults(false,
					"Please enter valid data for Maximum Pattern Number in Limit window.");
		}

		if (jSimThresholdBox.getInputVerifier().verify(jSimThresholdBox) == false) {
			jSimThresholdBox.requestFocus();
			return new ParamValidationResults(false,
					"Please enter valid data for Similarity Threshold in Advanced window.");
		}

		return new ParamValidationResults(true, null);

	}

	public void fillDefaultValues(Map<Serializable, Serializable> parameters) {
		// TODO Auto-generated method stub

	}

	// TODO such refreshing should be replaced by getting the triggering dataset
	// directly
	@Subscribe
	public void receive(org.geworkbench.events.ProjectEvent e, Object source) {

		DSDataSet<?> data = e.getDataSet();
		if (data instanceof DSSequenceSet) {
			DSSequenceSet<?> d = (DSSequenceSet<?>) data;
			maxSeqNumber = d.size();
		}
	}

}
