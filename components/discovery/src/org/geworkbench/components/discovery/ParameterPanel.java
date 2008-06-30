package org.geworkbench.components.discovery;

import org.geworkbench.util.RegularExpressionVerifier;
import polgara.soapPD_wsdl.Parameters;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.text.DecimalFormat;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>
 * ParameterPanel
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2003
 * </p>
 * <p>
 * Company:Califano Lab
 * </p>
 * 
 * @author not attributable
 * @version 1.0
 */

public class ParameterPanel extends JPanel {
	public static final String SUPPORT_OCCURANCES = "# Support Occurances";
	public static final String SUPPORT_SEQUENCES = "# Support Sequences";
	public static final String SUPPORT_PERCENT_1_100 = "Support Percent %(1-100)";
	private JTabbedPane jTabbedPane1 = new JTabbedPane();
	private JPanel BasicPane = new JPanel();
	private JPanel AdvancedPane = new JPanel();
	private JPanel jGroupingPane = new JPanel();

	private BorderLayout jborderLayout = new BorderLayout();

	private GridBagLayout jBasicGridBL = new GridBagLayout();
	private GridBagLayout jAdvancedgridBL = new GridBagLayout();
	private GridBagLayout jGroupsGridBL = new GridBagLayout();
	private GridBagLayout jLimitsGridBL = new GridBagLayout();
	private GridBagLayout jExhaustiveGridBL = new GridBagLayout();

	// Hirachical panel fields
	private JPanel jHierarchicalPane = new JPanel();
	private JTextField jMinPatternNoBox = new JTextField();
	private JLabel jMinClusterSizeLabel = new JLabel();
	private JLabel jMinPatternNoLabel = new JLabel();

	// Basic Panel
	private JComboBox jMinSupportMenu = new JComboBox();
	private JLabel jMinTokensLabel = new JLabel();
	private JLabel jWindowLabel = new JLabel();
	private JLabel jMinWTokensLabel = new JLabel();
	private JTextField jMinSupportBox = new JTextField();
	private JTextField jMinTokensBox = new JTextField();
	private JTextField jMinWTokensBox = new JTextField();
	private JTextField jWindowBox = new JTextField();

	// Advanced Panel
	private JCheckBox jExactOnlyBox = new JCheckBox();
	private JCheckBox jCountSeqBox = new JCheckBox();
	private JCheckBox jPValueBox = new JCheckBox();
	private JComboBox jMatrixBox = new JComboBox();
	private JLabel jSimThresholdLabel = new JLabel();
	private JTextField jSimThresholdBox = new JTextField();
	private JLabel jMinPValueLabel = new JLabel();
	private JTextField jMinPValueBox = new JTextField();

	// Groups Panel
	private JLabel jGroupsLabel = new JLabel();
	private JLabel jGroupSizeLabel = new JLabel();
	private JComboBox jGroupsBox = new JComboBox();
	private JTextField jGroupSizeBox = new JTextField();
	private Vector jGroups = null;

	// Limits Panel
	private JPanel jLimitsPane = new JPanel();
	private JLabel jMaxPatternNoLabel = new JLabel();
	private JTextField jMaxPatternNoBox = new JTextField();
	private JLabel jMaxRunTimeLabel = new JLabel();
	private JTextField jMaxRunTimeBox = new JTextField();

	// Exhaustive Panel
	private JPanel jExhaustivePane = new JPanel();
	private JLabel jDecreaseSupportLabel = new JLabel();
	private JTextField jDecreaseSupportBox = new JTextField();
	private JLabel jDecreaseDensitySupportLabel = new JLabel();
	private JComboBox jDecreaseDensitySupportBox = new JComboBox();

	// profileHMM Panel
	private JPanel jProfileHMMPane = new JPanel();
	private GridBagLayout jProfileHMMGridBL = new GridBagLayout();
	private JLabel jEntThreshLabel = new JLabel();
	private JTextField jEntThreshBox = new JTextField();
	private JLabel jConsRegionExtLabel = new JLabel();
	private JTextField jConsRegionExtBox = new JTextField();
	private JLabel jSlidWindowSizeLabel = new JLabel();
	private JTextField jSlidingWindowBox = new JTextField();
	private ButtonGroup buttonGroup1 = new ButtonGroup();
	private JRadioButton jSequenceRadioButton = new JRadioButton();
	private JRadioButton jOccurenceRadioButton = new JRadioButton();
	private JLabel jLabel1 = new JLabel();
	private JLabel jLabel2 = new JLabel();

	// private JLabel jEntropyThresh = new JLabel();
	// private JTextField jEntropyThreshBox = new JTextField();
	// private JLabel jSlidingWindow = new JLabel();
	// private JTextField jSlidingWindowBox = new JTextField();
	// private JLabel jConservedRegionExtension = new JLabel();
	// private JTextField jConsRegionExtBox = new JTextField();

	private ParametersHandler pHandler = null;
	JLabel jLabel3 = new JLabel();
	JTextField jMinSupportExhaustive = new JTextField();
	JPanel jPanel1 = new JPanel();
	GridBagLayout gridBagLayout1 = new GridBagLayout();
	TitledBorder titledBorder1;
	Border border1;
	Border border2;
	Border border3;
	Border border4;
	Border border5;
	Border border6;
	Border border7;
	JPanel jPanel2 = new JPanel();
	GridBagLayout gridBagLayout2 = new GridBagLayout();
	JLabel jLabel4 = new JLabel();
	JTextField jTextField1 = new JTextField();
	JTextField jMinClusterSizeBox = new JTextField();
	JCheckBox jUseHMMBox = new JCheckBox();
	GridBagLayout gridBagLayout3 = new GridBagLayout();
	private String currentSupportMenuStr = SUPPORT_PERCENT_1_100;
	private int maxSeqNumber;

	public ParameterPanel() {
		try {
			jbInit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	void jbInit() throws Exception {
		// Basic Panel
		titledBorder1 = new TitledBorder(new EtchedBorder(EtchedBorder.RAISED,
				Color.white, new Color(148, 145, 140)),
				"As percent of sequences");
		border1 = BorderFactory.createEmptyBorder(2, 2, 2, 2);
		border2 = BorderFactory.createEmptyBorder(2, 2, 2, 2);
		border3 = BorderFactory.createEmptyBorder(2, 2, 2, 2);
		border4 = BorderFactory.createEmptyBorder(2, 2, 2, 2);
		border5 = BorderFactory.createEmptyBorder(2, 2, 2, 2);
		border6 = BorderFactory.createEmptyBorder(2, 2, 2, 2);
		border7 = BorderFactory.createEmptyBorder(2, 2, 2, 2);
		BasicPane.setLayout(jBasicGridBL);
		BasicPane.setPreferredSize(new Dimension(10, 100));
		BasicPane.setBorder(border3);
		BasicPane.setMinimumSize(new Dimension(10, 100));
		jMinWTokensLabel.setText("Density Tokens:");
		jMinWTokensLabel.setToolTipText("");
		this.setLayout(jborderLayout);
		// jMinSupportMenu.setText("Support:");
		jMinSupportMenu.addItem(SUPPORT_PERCENT_1_100);
		jMinSupportMenu.addItem(SUPPORT_SEQUENCES);
		jMinSupportMenu.addItem(SUPPORT_OCCURANCES);
		jMinSupportMenu.setSelectedIndex(0);
		jMinSupportMenu
				.addActionListener(new ParameterPanel_jSupportMenu_actionAdapter(
						this));
		jWindowLabel.setText("Density Window:");
		jMinTokensBox.setText("7");
		jWindowBox.setText("5");
		jMinWTokensBox.setText("4");
		jMinTokensLabel.setText("Min Tokens:");
		jMinSupportBox.setText("70");
		jProfileHMMPane.setLayout(jProfileHMMGridBL);
		jProfileHMMPane.setBorder(border1);
		jProfileHMMPane.setMaximumSize(new Dimension(32767, 32767));
		jEntThreshLabel.setText("Entropy Threshold:");
		jConsRegionExtLabel.setText("Conserved Region Extension:");
		jConsRegionExtBox
				.addActionListener(new ParameterPanel_jConsRegionExtBox_actionAdapter(
						this));
		jEntThreshBox.setEnabled(true);
		jEntThreshBox.setSelectionEnd(0);
		jEntThreshBox.setSelectionStart(0);
		jEntThreshBox.setText("16");
		jEntThreshBox
				.addActionListener(new ParameterPanel_jEntThreshBox_actionAdapter(
						this));
		jSlidWindowSizeLabel.setText("Sliding Window Size:");
		jSequenceRadioButton.setEnabled(false);
		jSequenceRadioButton.setDoubleBuffered(false);
		jSequenceRadioButton.setText("sequence");
		jOccurenceRadioButton.setEnabled(false);
		jOccurenceRadioButton.setText("occurence");
		jLabel1.setText("By sequence:");
		jLabel1.setVerticalAlignment(javax.swing.SwingConstants.CENTER);
		jLabel2.setText("By occurence:");
		jLabel3.setText("Min. Support:");
		jMinSupportExhaustive.setText("10%");
		jPanel1.setLayout(gridBagLayout1);
		jPanel1.setBorder(null);
		// add a verifier
		jMinTokensBox.setInputVerifier(new RegularExpressionVerifier(
				"(\\d){1,9}"));
		jMinWTokensBox.setInputVerifier(new RegularExpressionVerifier(
				"(\\d){1,9}"));
		jWindowBox
				.setInputVerifier(new RegularExpressionVerifier("(\\d){1,9}"));
		// jMinSupportBox.setInputVerifier(new
		// SupportVerifier("0?\\.?(\\d){1,9}(%)?"));
		jMinSupportBox.setInputVerifier(new SupportVerifier(
				"0?\\.?(\\d){1,9}(%)?"));
		// Advanced Panel
		jMatrixBox.addItem("BLOSUM50");
		jMatrixBox.addItem("BLOSUM62");
		jMatrixBox.addItem("BLOSUM100");
		jMatrixBox.setSelectedIndex(0);
		AdvancedPane.setLayout(jAdvancedgridBL);
		jCountSeqBox.setSelected(false);
		jCountSeqBox.setText("Count Sequences");
		jPValueBox.setSelected(true);
		jPValueBox.setText("ZScore");
		jExactOnlyBox.setSelected(true);
		jExactOnlyBox.setText("Exact Only");
		jSimThresholdLabel.setText("Similarity Threshold:");
		jSimThresholdBox.setText("2");

		jMinPValueLabel.setText("Min. ZScore:");
		jMinPValueBox.setPreferredSize(new Dimension(80, 20));
		jMinPValueBox.setText("-10000");
		// add a verifier
		jSimThresholdBox.setPreferredSize(new Dimension(80, 20));
		jSimThresholdBox.setInputVerifier(new RegularExpressionVerifier(
				"(\\d){1,9}"));
		// check if it's double
		jMinPValueBox.setInputVerifier(new RegularExpressionVerifier(
				"(-)?(\\d){1,10}(.)?(\\d){1,10}?(.)?(\\d){1,10}"));
		// Groups Panel
		jGroupsBox.addItem("Normal");
		jGroupsBox.addItem("Occurs in Group");
		jGroupsBox.addItem("Support in Group");
		jGroupsBox.setEnabled(false);
		jGroupsBox.setMinimumSize(new Dimension(6, 21));
		jGroupsBox.setSelectedIndex(0);
		jGroupsBox.setSelectedItem("Normal");

		jGroupsLabel.setMaximumSize(new Dimension(63, 15));
		jGroupsLabel.setMinimumSize(new Dimension(50, 15));
		jGroupsLabel.setText("Type: ");
		jGroupSizeLabel.setMaximumSize(new Dimension(63, 15));
		jGroupSizeLabel.setMinimumSize(new Dimension(50, 15));
		jGroupSizeLabel.setText("Size: ");
		jGroupingPane.setLayout(jGroupsGridBL);
		jGroupSizeBox.setEnabled(false);
		jGroupSizeBox.setMinimumSize(new Dimension(4, 21));
		jGroupSizeBox.setText("1");
		jGroupingPane.setBorder(border6);
		jGroupingPane.setDebugGraphicsOptions(0);

		// input verifier
		jGroupSizeBox.setInputVerifier(new RegularExpressionVerifier(
				"(\\d){1,9}"));
		// Limits Panel
		jLimitsPane.setLayout(jLimitsGridBL);
		jMaxPatternNoLabel.setMinimumSize(new Dimension(60, 15));
		jMaxPatternNoLabel.setText("Max. Pattern Number: ");
		jMaxPatternNoBox.setMinimumSize(new Dimension(6, 21));
		jMaxPatternNoBox.setText("100000");
		jMaxRunTimeLabel.setMinimumSize(new Dimension(60, 15));
		jMaxRunTimeLabel.setText("Max. Run Time (sec.):");
		jMaxRunTimeBox.setEnabled(false);
		jMaxRunTimeBox.setPreferredSize(new Dimension(36, 21));
		jMaxRunTimeBox.setText("0");
		// input verifier
		jMaxPatternNoBox.setInputVerifier(new RegularExpressionVerifier(
				"(\\d){1,9}"));
		jMaxRunTimeBox.setInputVerifier(new RegularExpressionVerifier(
				"(\\d){1,9}"));
		// Exhaustive Panel
		jExhaustivePane.setLayout(jExhaustiveGridBL);
		jDecreaseDensitySupportLabel.setMinimumSize(new Dimension(50, 15));
		jDecreaseDensitySupportLabel.setText("Dec. density support:");
		jDecreaseDensitySupportBox.addItem("0");
		jDecreaseDensitySupportBox.addItem("1");
		jDecreaseDensitySupportBox.addItem("2");
		jDecreaseDensitySupportBox.setEnabled(false);
		jDecreaseDensitySupportBox.setMinimumSize(new Dimension(6, 21));
		jDecreaseDensitySupportBox.setSelectedIndex(0);
		jDecreaseDensitySupportBox.setSelectedItem("0");
		jDecreaseSupportLabel.setMinimumSize(new Dimension(50, 15));
		jDecreaseSupportLabel.setText("Dec. support (%):");
		jDecreaseSupportBox.setPreferredSize(new Dimension(12, 20));
		jDecreaseSupportBox.setText("5");
		jSlidingWindowBox.setEnabled(true);
		jSlidingWindowBox.setText("10");
		jConsRegionExtBox.setEnabled(false);
		jConsRegionExtBox.setText("");
		jLimitsPane.setBorder(border2);
		jHierarchicalPane.setBorder(border4);
		jHierarchicalPane.setMinimumSize(new Dimension(156, 52));
		jExhaustivePane.setBorder(border5);
		AdvancedPane.setBorder(border7);
		jMinPatternNoBox.setPreferredSize(new Dimension(20, 20));
		jPanel2.setMaximumSize(new Dimension(32767, 32767));
		jPanel2.setLayout(gridBagLayout2);
		jLabel4.setText("Min. Pattern Number:");
		jTextField1.setText("");
		jMinClusterSizeBox.setPreferredSize(new Dimension(20, 20));
		jMinClusterSizeBox.setText("10");
		jMinClusterSizeBox.setInputVerifier(new RegularExpressionVerifier(
				"(\\d){1,9}"));
		jUseHMMBox.setEnabled(false);
		jUseHMMBox.setText("Use ProfileHMM");
		buttonGroup1.add(jOccurenceRadioButton);
		buttonGroup1.add(jSequenceRadioButton);
		jDecreaseSupportBox
				.addActionListener(new ParameterPanel_jDecreaseSupportBox_actionAdapter(
						this));
		// input verifier
		jDecreaseSupportBox.setInputVerifier(new RegularExpressionVerifier(
				"(\\d){1,9}(%)?"));
		// Hierarchical Panel
		jHierarchicalPane.setLayout(gridBagLayout3);
		jHierarchicalPane.add(jMinClusterSizeLabel, new GridBagConstraints(0,
				0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 0, 0));
		jHierarchicalPane.add(jMinClusterSizeBox, new GridBagConstraints(1, 0,
				1, 1, 1.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));
		jHierarchicalPane.add(jMinPatternNoLabel, new GridBagConstraints(0, 1,
				1, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 0, 0));
		jHierarchicalPane.add(jMinPatternNoBox, new GridBagConstraints(1, 1, 1,
				1, 1.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));
		jMinPatternNoLabel.setText("Min. Pattern Number:");
		jMinPatternNoBox.setText("10");
		jMinClusterSizeLabel.setText("Min. Cluster Size:");

		jMinPatternNoBox.setInputVerifier(new RegularExpressionVerifier(
				"(\\d){1,9}"));

		BasicPane.add(jMinSupportBox, new GridBagConstraints(1, 0, 1, 1, 0.7,
				1.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
				new Insets(2, 2, 2, 4), 0, 0));
		BasicPane.add(jMinTokensBox, new GridBagConstraints(1, 1, 1, 1, 0.7,
				1.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
				new Insets(2, 2, 2, 4), 0, 0));
		BasicPane.add(jWindowBox, new GridBagConstraints(1, 2, 1, 1, 0.7, 1.0,
				GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
				new Insets(2, 2, 2, 4), 0, 0));
		BasicPane.add(jMinSupportMenu, new GridBagConstraints(0, 0, 1, 1, 0.0,
				1.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
				new Insets(2, 2, 2, 2), 0, 0));
		BasicPane.add(jMinWTokensBox, new GridBagConstraints(1, 3, 1, 1, 0.7,
				1.0, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL,
				new Insets(2, 2, 2, 4), 0, 0));
		BasicPane.add(jMinTokensLabel, new GridBagConstraints(0, 1, 1, 1, 0.0,
				1.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
				new Insets(2, 2, 2, 2), 0, 0));
		BasicPane.add(jWindowLabel, new GridBagConstraints(0, 2, 1, 1, 0.0,
				1.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
				new Insets(2, 2, 2, 2), 0, 0));
		BasicPane.add(jMinWTokensLabel, new GridBagConstraints(0, 3, 1, 1, 0.0,
				1.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
				new Insets(2, 2, 2, 2), 0, 0));

		AdvancedPane.add(jExactOnlyBox, new GridBagConstraints(0, 0, 1, 1, 0.0,
				0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(1, 2, 1, 2), 0, 0));
		AdvancedPane.add(jCountSeqBox, new GridBagConstraints(0, 1, 1, 1, 0.0,
				0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(1, 2, 1, 40), 0, 0));

		// Remove Z score, fix bug 850
		// AdvancedPane.add(jPValueBox, new GridBagConstraints(0, 3, 1, 1, 0.0,
		// 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(1,
		// 2, 1, 2), 0, 0));
		// AdvancedPane.add(jMinPValueLabel, new GridBagConstraints(1, 3, 1, 1,
		// 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
		// new Insets(1, 2, 2, 2), 0, 0));
		// AdvancedPane.add(jMinPValueBox, new GridBagConstraints(2, 3, 1, 1,
		// 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
		// new Insets(1, 1, 2, 2), 0, 0));
		AdvancedPane.add(jMatrixBox, new GridBagConstraints(1, 0, 2, 1, 1.0,
				0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
				new Insets(2, 2, 2, 2), 0, 0));
		AdvancedPane.add(jSimThresholdLabel, new GridBagConstraints(1, 1, 1, 1,
				0.0, 0.0, GridBagConstraints.CENTER,
				GridBagConstraints.HORIZONTAL, new Insets(1, 2, 2, 2), 0, 0));
		AdvancedPane.add(jSimThresholdBox, new GridBagConstraints(2, 1, 1, 1,
				0.0, 0.0, GridBagConstraints.CENTER,
				GridBagConstraints.HORIZONTAL, new Insets(1, 1, 2, 2), 0, 0));

		jGroupingPane.add(jGroupsBox, new GridBagConstraints(1, 0, 1, 1, 1.0,
				0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
				new Insets(2, 4, 2, 4), 139, 0));
		jGroupingPane.add(jGroupSizeBox, new GridBagConstraints(1, 1, 1, 1,
				1.0, 0.0, GridBagConstraints.NORTHWEST,
				GridBagConstraints.HORIZONTAL, new Insets(2, 4, 2, 4), 0, 0));
		jGroupingPane.add(jGroupSizeLabel, new GridBagConstraints(0, 1, 1, 1,
				0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(2, 2, 2, 2), 0, 0));
		jGroupingPane.add(jGroupsLabel, new GridBagConstraints(0, 0, 1, 1, 0.0,
				0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(2, 2, 2, 2), 0, 0));

		jLimitsPane.add(jMaxPatternNoLabel, new GridBagConstraints(0, 0, 1, 1,
				0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(2, 2, 2, 2), 0, 0));
		jLimitsPane.add(jMaxRunTimeLabel, new GridBagConstraints(0, 1, 1, 1,
				0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(2, 2, 2, 2), 0, 0));
		jLimitsPane.add(jMaxRunTimeBox, new GridBagConstraints(1, 1, 1, 1, 1.0,
				0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
				new Insets(2, 4, 2, 4), 0, 0));
		jLimitsPane.add(jMaxPatternNoBox, new GridBagConstraints(1, 0, 1, 1,
				1.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, new Insets(2, 4, 2, 4), 0, 0));

		jPanel1.add(jDecreaseSupportLabel, new GridBagConstraints(0, 0, 1, 1,
				0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 2), 0, 0));
		jPanel1.add(jDecreaseSupportBox, new GridBagConstraints(1, 0, 1, 1,
				1.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, new Insets(0, 2, 0, 2), 0, 0));
		jPanel1.add(jLabel3, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,
						2, 0, 2), 0, 0));
		jPanel1.add(jMinSupportExhaustive, new GridBagConstraints(3, 0, 1, 1,
				1.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, new Insets(0, 2, 0, 0), 0, 0));
		jExhaustivePane.add(jPanel2, new GridBagConstraints(0, 1, 2, 1, 0.0,
				0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
				new Insets(0, 0, 0, 0), 0, 0));

		jPanel2.add(jDecreaseDensitySupportLabel, new GridBagConstraints(0, 0,
				1, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 0, 0));
		jPanel2.add(jDecreaseDensitySupportBox, new GridBagConstraints(1, 0, 1,
				1, 0.5, 0.0, GridBagConstraints.CENTER,
				GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));
		jPanel2.add(jLabel4, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(
						2, 2, 2, 2), 0, 0));
		jPanel2.add(jTextField1, new GridBagConstraints(3, 0, 1, 1, 0.5, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
				new Insets(0, 0, 0, 0), 0, 0));
		jExhaustivePane.add(jSequenceRadioButton, new GridBagConstraints(1, 3,
				1, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 0, 0));
		jExhaustivePane.add(jLabel1, new GridBagConstraints(0, 3, 1, 1, 0.0,
				0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(2, 2, 2, 2), 0, 0));
		jExhaustivePane.add(jLabel2, new GridBagConstraints(0, 2, 1, 1, 0.0,
				0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(2, 2, 0, 2), 0, 0));
		jExhaustivePane.add(jOccurenceRadioButton, new GridBagConstraints(1, 2,
				1, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 0, 0));
		jExhaustivePane.add(jPanel1, new GridBagConstraints(0, 0, 2, 1, 1.0,
				0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
				new Insets(2, 2, 2, 2), 0, 0));

		jProfileHMMPane.add(jEntThreshBox, new GridBagConstraints(1, 1, 2, 1,
				0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, new Insets(0, 2, 0, 0), 0, 0));
		jProfileHMMPane.add(jConsRegionExtLabel, new GridBagConstraints(0, 2,
				1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
				GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		jProfileHMMPane.add(jConsRegionExtBox, new GridBagConstraints(1, 2, 1,
				1, 1.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, new Insets(0, 2, 0, 0), 0, 0));
		jProfileHMMPane.add(jSlidingWindowBox, new GridBagConstraints(1, 3, 2,
				1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, new Insets(0, 2, 0, 0), 0, 0));
		jProfileHMMPane.add(jSlidWindowSizeLabel, new GridBagConstraints(0, 3,
				1, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		jProfileHMMPane.add(jEntThreshLabel, new GridBagConstraints(0, 1, 1, 1,
				0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		jProfileHMMPane.add(jUseHMMBox, new GridBagConstraints(0, 0, 1, 1, 0.0,
				0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		jTabbedPane1.add(BasicPane, "Basic");
		jTabbedPane1.add(jHierarchicalPane, "Hierarchical");
		jTabbedPane1.add(jExhaustivePane, "Exhaustive");
		jTabbedPane1.add(jLimitsPane, "Limits");
		jTabbedPane1.add(jProfileHMMPane, "ProfileHMM");
		jTabbedPane1.add(jGroupingPane, "Grouping");
		jTabbedPane1.add(AdvancedPane, "Advanced");
		this.add(jTabbedPane1, BorderLayout.NORTH);
		jTabbedPane1.setSelectedIndex(0);
	}

	// ------------------------BASIC PANEL
	public String getMinSupport() {
		return (jMinSupportBox.getText().trim());
	}

	public int getMinWTokens() {
		return (Integer.parseInt(jMinWTokensBox.getText()));
	}

	public int getCountSeqBoxSelected() {
		return (jCountSeqBox.isSelected() ? 1 : 0);
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

	public int getPValueBoxSelected() {
		return (jPValueBox.isSelected() ? 1 : 0);
	}

	public String getMatrixSelection() {
		return ((String) jMatrixBox.getSelectedItem());
	}

	public double getSimilarityThreshold() {
		return (Double.parseDouble(jSimThresholdBox.getText()));
	}

	public double getMinPValue() {
		return (Double.parseDouble(jMinPValueBox.getText()));
	}

	public double getProfileEntropy() {
		return Double.parseDouble(jEntThreshBox.getText());
	}

	public int getProfileWindow() {
		return Integer.parseInt(jSlidingWindowBox.getText());
	}

	// Parsing the GROUPING panel
	public int getGroupingType() {
		return (Math.max(0, jGroupsBox.getSelectedIndex()));
		// parms.setGroupingType(groupType);
	}

	public int getGroupingN() {
		return (Integer.parseInt(jGroupSizeBox.getText()));
		// parms.setGroupingType(groupType);
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

	// parsing ExhaustivePanel
	public boolean getByOccurence() {
		return jOccurenceRadioButton.isSelected();
	}

	public boolean getBySequence() {
		return jSequenceRadioButton.isSelected();
	}

	public void setParameters(Parameters parms) {
		DecimalFormat format = new DecimalFormat("####.##");
		// Parsing the BASIC panel
		// if (parms.getMinPer100Support() > 0) {
		// String support = format.format(parms.getMinPer100Support() * 100);
		// jMinSupportBox.setText(support + "%");
		// jCountSeqBox.setSelected(true);
		// } else {
		// jMinSupportBox.setText(Integer.toString(parms.getMinSupport()));
		// }
		jMinSupportMenu.setSelectedItem(currentSupportMenuStr);
		if (currentSupportMenuStr.equalsIgnoreCase(SUPPORT_PERCENT_1_100)) {
			String support = format.format(parms.getMinPer100Support() * 100);
			jMinSupportBox.setText(support);
			// jMinSupportMenu.setSelectedItem(SUPPORT_PERCENT_1_100);
			jCountSeqBox.setSelected(true);
		} else {
			jMinSupportBox.setText(format.format(parms.getMinSupport()));
			jCountSeqBox.setSelected((parms.getCountSeq() == 1) ? true : false);
		}
		// if (parms.getMinPer100Support() > 0) {
		// String support = format.format(parms.getMinPer100Support() * 100);
		// jMinSupportBox.setText(support);
		// jMinSupportMenu.setSelectedItem(SUPPORT_PERCENT_1_100);
		// jCountSeqBox.setSelected(true);
		// } else {
		// jMinSupportBox.setText(Integer.toString(parms.getMinSupport()));
		// jMinSupportMenu.setSelectedItem(SUPPORT_SEQUENCES);
		// }
		// jCountSeqBox.setSelected((parms.getCountSeq() == 1) ? true : false);

		jMinTokensBox.setText(Integer.toString(parms.getMinTokens()));
		jWindowBox.setText(Integer.toString(parms.getWindow()));
		jMinWTokensBox.setText(Integer.toString(parms.getMinWTokens()));

		// Parsing the ADVANCED panel
		jExactOnlyBox.setSelected((parms.getExact() == 1) ? true : false);
		jPValueBox.setSelected((parms.getComputePValue() == 1) ? true : false);
		jMatrixBox.setSelectedItem(parms.getSimilarityMatrix());
		jSimThresholdBox.setText(format.format(parms.getSimilarityThreshold()));
		jMinPValueBox.setText(format.format(parms.getMinPValue()));

		// Parsing the HIERARCHICAL panel
		if (parms != null && parms.getHierarchical() != null)
			jMinClusterSizeBox.setText(Integer.toString(parms.getHierarchical()
					.getClusterSize()));

		jMinPatternNoBox.setText(Integer.toString(parms.getMinPatternNo()));

		// Parsing the GROUPING panel
		int groupType = Math.max(0, parms.getGroupingType());
		jGroupsBox.setSelectedIndex(groupType);
		if (groupType == 0) {
			jGroupSizeBox.setEnabled(false);
		} else {
			jGroupSizeBox.setEnabled(true);
			jGroupSizeBox.setText(Integer.toString(parms.getGroupingN()));
		}

		// Parsing the LIMITS panel
		jMaxPatternNoBox.setText(Integer.toString(parms.getMaxPatternNo()));
		jMaxRunTimeBox.setText(Integer.toString(parms.getMaxRunTime()));

	}

	public String getDecSupportExhaustive() {
		return jDecreaseSupportBox.getText().trim();
	}

	public String getMinSupportExhaustive() {
		return jMinSupportExhaustive.getText().trim();
	}

	public int getCountSeq() {
		if (jCountSeqBox.isSelected()) {
			return 1;
		}
		return 0;
	}

	public String getDensityConstraint() {
		String decConstraint = (String) jDecreaseDensitySupportBox
				.getSelectedItem();
		return decConstraint;
	}

	void jDecreaseSupportBox_actionPerformed(ActionEvent e) {

	}

	void jEntThreshBox_actionPerformed(ActionEvent e) {
	}

	void jConsRegionExtBox_actionPerformed(ActionEvent e) {
	}

	void jSupportMenu_actionPerformed(ActionEvent e) {
		String selectedSupportStr = (String) jMinSupportMenu.getSelectedItem();
		System.out.println(selectedSupportStr);
		String minSupportStr = getMinSupport();
		if (!currentSupportMenuStr.equalsIgnoreCase(selectedSupportStr)) {
			// if(currentSupportMenuStr.equalsIgnoreCase(SUPPORT_PERCENT_1_100)){
			// int minSupport =Math.min((int)
			// (Math.ceil(Double.parseDouble(minSupportStr)/100 *
			// maxSeqNumber)), maxSeqNumber);
			// jMinSupportBox.setText(new Integer(minSupport).toString());
			// }else{
			// int minSupport =Math.min((int)
			// (Math.ceil(Double.parseDouble(minSupportStr)/maxSeqNumber *
			// 100)), 100);
			// jMinSupportBox.setText(new Integer(minSupport).toString());
			// }
			jMinSupportBox.setText("");
			this.revalidate();

		}
		currentSupportMenuStr = selectedSupportStr;
	}

	// this class is used for the parametorPanel for verification of input.
	// It uses a regular expression for the verification
	class SupportVerifier extends InputVerifier {
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
		}// endof shouldyieldfocus()

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

			// if (!match) {
			// JOptionPane.showMessageDialog(null, "Eggs aren't supposed to be
			// green.");
			// }
			return match;
		}// endof verify()

	}

}

class ParameterPanel_jDecreaseSupportBox_actionAdapter implements
		java.awt.event.ActionListener {
	ParameterPanel adaptee;

	ParameterPanel_jDecreaseSupportBox_actionAdapter(ParameterPanel adaptee) {
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e) {
		adaptee.jDecreaseSupportBox_actionPerformed(e);
	}
}

class ParameterPanel_jEntThreshBox_actionAdapter implements
		java.awt.event.ActionListener {
	ParameterPanel adaptee;

	ParameterPanel_jEntThreshBox_actionAdapter(ParameterPanel adaptee) {
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e) {
		adaptee.jEntThreshBox_actionPerformed(e);
	}
}

class ParameterPanel_jSupportMenu_actionAdapter implements
		java.awt.event.ActionListener {
	ParameterPanel adaptee;

	ParameterPanel_jSupportMenu_actionAdapter(ParameterPanel adaptee) {
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e) {
		adaptee.jSupportMenu_actionPerformed(e);
	}
}

class ParameterPanel_jConsRegionExtBox_actionAdapter implements
		java.awt.event.ActionListener {
	ParameterPanel adaptee;

	ParameterPanel_jConsRegionExtBox_actionAdapter(ParameterPanel adaptee) {
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e) {
		adaptee.jConsRegionExtBox_actionPerformed(e);
	}
}
