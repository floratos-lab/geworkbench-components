package org.geworkbench.components.medusa.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.analysis.AbstractSaveableParameterPanel;
import org.ginkgo.labs.util.FileTools;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

/**
 * 
 * @author keshav
 * @version $Id: MedusaParamPanel.java,v 1.9 2007-06-19 20:56:31 keshav Exp $
 */
public class MedusaParamPanel extends AbstractSaveableParameterPanel implements
		Serializable {

	private static final String NUMERIC_VALUES_ONLY = "Numeric values only (make sure fields are not empty).";

	private static final String CANNOT_BE_NEGATIVE = "cannot be negative.";

	private Log log = LogFactory.getLog(MedusaParamPanel.class);

	private static final String TRUE = "True";

	private static final String FALSE = "False";

	private static final String YES = "Yes";

	private static final String NO = "No";

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private JTabbedPane parametersTabbedPane = new JTabbedPane();

	/* MAIN PANEL */

	private String configFilePath = "data/medusa/dataset/config.xml";

	private String labelsFilePath = "data/medusa/dataset/web100_test.labels";

	/* features */
	private JButton loadFeaturesButton = new JButton("Load Features");

	private String defaultFeaturesFile = "data/medusa/dataset/web100_test.fasta";

	private String featuresFilePath = defaultFeaturesFile;

	/* regulators */
	private String REGULATOR_LIST = "Specify (csv)";

	private String REGULATOR_ACTIVATED = "Activated Markers";

	private JComboBox regulatorCombo = new JComboBox(new String[] {
			REGULATOR_ACTIVATED, REGULATOR_LIST });

	private boolean useSelectedAsRegulators = true;

	private String DEFAULT_REGULATOR_LIST = null;

	private JTextField regulatorTextField = new JTextField(
			DEFAULT_REGULATOR_LIST);

	private JButton loadRegulatorsButton = new JButton("Load Regulators");

	private String regulatorsFilePath = new String("data/regulators.txt");

	/* targets */
	private String TARGET_LIST = "Specify (csv)";

	private String TARGET_ALL = "Use All";

	private JComboBox targetCombo = new JComboBox(new String[] { TARGET_ALL,
			TARGET_LIST });

	private boolean useAllAsTargets = true;

	private String DEFAULT_TARGET_LIST = null;

	private JTextField targetTextField = new JTextField(DEFAULT_TARGET_LIST);

	private JButton loadTargetsButton = new JButton("Load Targets");

	private String targetsFilePath = new String(
			"data/medusa/dataset/targets.txt");

	/* discretization interval */

	private double intervalBase = 30;

	private JTextField intervalBaseTextField = new JTextField(String
			.valueOf(intervalBase));

	private double intervalBound = 4;

	private JTextField intervalBoundTextField = new JTextField(String
			.valueOf(intervalBound));

	/* boosting iterations */
	private int boostingIterations = 5;

	private JTextField boostingIterationsTextField = new JTextField(String
			.valueOf(boostingIterations));

	/* all arrays */
	private JCheckBox allArraysCheckBox = new JCheckBox("", true);

	/* SECONDARY PANEL */

	/* min kmers */
	private int minKmer = 3;

	private JTextField minKmerTextField = new JTextField();

	/* max kmers */
	private int maxKmer = 7;

	private JTextField maxKmerTextField = new JTextField();

	/* dimers */
	private JComboBox dimersCombo = new JComboBox(new String[] { NO, YES });

	private int minGap = 3;

	private JTextField dimerMinGapTextField = new JTextField();

	private int maxGap = 7;

	private JTextField dimerMaxGapTextField = new JTextField();

	private boolean usingDimers = false;

	/* reverse compliment */
	private boolean reverseComplement = false;

	private JComboBox reverseComplementCombo = new JComboBox(new String[] {
			TRUE, FALSE });

	/* pssm length */
	private int pssmLength = 17;

	private JTextField pssmLengthTextField = new JTextField(pssmLength);

	/* agglomerations per round */
	private int agg = 20;

	private JTextField aggTextField = new JTextField(agg);

	/**
	 * 
	 * 
	 */
	public MedusaParamPanel() {

		this.setLayout(new BorderLayout());

		this.regulatorTextField.setEnabled(false);
		this.loadRegulatorsButton.setEnabled(false);
		this.targetTextField.setEnabled(false);
		this.loadTargetsButton.setEnabled(false);
		this.dimerMinGapTextField.setEnabled(false);
		this.dimerMaxGapTextField.setEnabled(false);

		/* MAIN PANEL */

		FormLayout layout = new FormLayout(
				"right:max(40dlu;pref), 3dlu, 60dlu, 3dlu, 90dlu, 3dlu, 40dlu, 7dlu",
				"");

		DefaultFormBuilder mainBuilder = new DefaultFormBuilder(layout);
		mainBuilder.setDefaultDialogBorder();
		mainBuilder.appendSeparator("MEDUSA Main Paramaters");

		/* features */
		mainBuilder.append("Features File (FASTA)", loadFeaturesButton);
		mainBuilder.nextRow();

		/* regulators */
		mainBuilder.append("Regulators", regulatorCombo);
		mainBuilder.append(regulatorTextField, loadRegulatorsButton);
		// builder.nextRow();

		/* targets */
		mainBuilder.append("Targets", targetCombo);
		mainBuilder.append(targetTextField, loadTargetsButton);
		// builder.nextRow();

		/* intervals */
		mainBuilder.append("Interval Base", intervalBaseTextField);
		mainBuilder.nextRow();
		mainBuilder.append("Interval Bound", intervalBoundTextField);
		mainBuilder.nextRow();

		/* iterations */
		mainBuilder.append("Boosting Iterations", boostingIterationsTextField);
		mainBuilder.nextRow();

		/* all arrays */
		mainBuilder.append("All Arrays", allArraysCheckBox);

		/* SECONDARY PANEL */

		FormLayout secondaryLayout = new FormLayout(
				"right:max(40dlu;pref), 3dlu, 60dlu, 3dlu, 90dlu, 3dlu, 40dlu, 7dlu",
				"");

		DefaultFormBuilder secondaryBuilder = new DefaultFormBuilder(
				secondaryLayout);
		secondaryBuilder.setDefaultDialogBorder();
		secondaryBuilder.appendSeparator("MEDUSA Secondary Parameters");

		/* kmer */
		minKmerTextField.setText(String.valueOf(minKmer));
		secondaryBuilder.append("Minimum Kmer", minKmerTextField);
		secondaryBuilder.nextRow();

		maxKmerTextField.setText(String.valueOf(maxKmer));
		secondaryBuilder.append("Maximum Kmer", maxKmerTextField);
		secondaryBuilder.nextRow();

		/* dimers */
		secondaryBuilder.append("Learn Dimers", dimersCombo);
		secondaryBuilder.nextRow();

		/* dimer gaps */
		dimerMinGapTextField.setText(String.valueOf(minGap));
		secondaryBuilder.append("Dimer Min Gap", dimerMinGapTextField);
		secondaryBuilder.nextRow();

		dimerMaxGapTextField.setText(String.valueOf(maxGap));
		secondaryBuilder.append("Dimer Max Gap", dimerMaxGapTextField);
		secondaryBuilder.nextRow();

		/* reverse complement */
		secondaryBuilder.append("Reverse Complement", reverseComplementCombo);
		secondaryBuilder.nextRow();

		/* pssm length */
		pssmLengthTextField.setText(String.valueOf(pssmLength));
		secondaryBuilder.append("Max PSSM Length", pssmLengthTextField);
		secondaryBuilder.nextRow();

		/* agglomerations per round */
		aggTextField.setText(String.valueOf(agg));
		secondaryBuilder.append("Agglomerations Per Round", aggTextField);
		secondaryBuilder.nextRow();

		/* add tabs */
		parametersTabbedPane.add("Main", mainBuilder.getPanel());
		parametersTabbedPane.add("Secondary", secondaryBuilder.getPanel());

		this.add(parametersTabbedPane);

		addListeners();

	}

	/**
	 * 
	 * 
	 */
	private void addListeners() {

		/* PRIMARY */

		/* features */
		// button listener
		loadFeaturesButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				// StringBuilder geneListBuilder = new StringBuilder();
				try {
					File featuresFile = new File(defaultFeaturesFile);
					JFileChooser chooser = new JFileChooser(featuresFile
							.getParent());
					int retVal = chooser.showOpenDialog(null);

					if (retVal == JFileChooser.APPROVE_OPTION) {
						featuresFilePath = chooser.getSelectedFile().getPath();

						BufferedReader reader = new BufferedReader(
								new FileReader(featuresFilePath));

						if (!FileTools.isFasta(reader)) {
							JOptionPane.showMessageDialog(null,
									"Not in FASTA format.", "Error",
									JOptionPane.ERROR_MESSAGE);
						}

					} else {
						log.debug("cancelled ... ");
					}
				} catch (IOException e) {
					log.error(e);
				}
			}
		});

		/* regulators */
		// combo listener
		regulatorCombo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JComboBox cb = (JComboBox) e.getSource();
				String selectedItem = (String) cb.getSelectedItem();
				if (REGULATOR_ACTIVATED.equals(selectedItem)) {
					regulatorTextField.setEnabled(false);
					loadRegulatorsButton.setEnabled(false);
					useSelectedAsRegulators = true;
				} else {
					regulatorTextField.setEnabled(true);
					loadRegulatorsButton.setEnabled(true);
					useSelectedAsRegulators = false;
				}
			}
		});

		// button listener
		loadRegulatorsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				StringBuilder regulatorListBuilder = new StringBuilder();
				try {
					File regulatorsFile = new File(regulatorsFilePath);
					JFileChooser chooser = new JFileChooser(regulatorsFile
							.getParent());

					// null used to center dialog
					int retVal = chooser.showOpenDialog(null);

					if (retVal == JFileChooser.APPROVE_OPTION) {
						regulatorsFilePath = chooser.getSelectedFile()
								.getPath();

						BufferedReader reader = new BufferedReader(
								new FileReader(regulatorsFilePath));

						String reg = reader.readLine();
						while (!StringUtils.isEmpty(reg)) {
							regulatorListBuilder.append(reg + ", ");
							reg = reader.readLine();
						}

						String regulatorString = regulatorListBuilder
								.toString();
						regulatorTextField.setText(regulatorString.substring(0,
								regulatorString.length() - 2));
					}

					else {
						log.debug("cancelled ... ");
					}

				} catch (IOException e) {
					log.error(e);
				}

			}
		});

		/* targets */
		// combo listener
		targetCombo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JComboBox cb = (JComboBox) e.getSource();
				String selectedItem = (String) cb.getSelectedItem();
				if (TARGET_ALL.equals(selectedItem)) {
					targetTextField.setEnabled(false);
					loadTargetsButton.setEnabled(false);
					useAllAsTargets = true;
				} else {
					targetTextField.setEnabled(true);
					loadTargetsButton.setEnabled(true);
					useAllAsTargets = false;
				}
			}
		});
		// button listener
		loadTargetsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				StringBuilder targetListBuilder = new StringBuilder();
				try {
					File targetFile = new File(targetsFilePath);
					JFileChooser chooser = new JFileChooser(targetFile
							.getParent());
					int retVal = chooser.showOpenDialog(null);

					if (retVal == JFileChooser.APPROVE_OPTION) {
						targetsFilePath = chooser.getSelectedFile().getPath();

						BufferedReader reader = new BufferedReader(
								new FileReader(targetsFilePath));
						String tar = reader.readLine();
						while (!StringUtils.isEmpty(tar)) {
							targetListBuilder.append(tar + ", ");
							tar = reader.readLine();
						}

						String targetString = targetListBuilder.toString();
						targetTextField.setText(targetString.substring(0,
								targetString.length() - 2));
					}

					else {
						log.debug("cancelled ... ");
					}

				} catch (IOException e) {
					log.error(e);
				}

			}
		});

		/* SECONDARY */

		// combo listener
		dimersCombo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JComboBox cb = (JComboBox) e.getSource();
				String selectedItem = (String) cb.getSelectedItem();
				if (YES.equals(selectedItem)) {
					dimerMinGapTextField.setEnabled(true);
					dimerMaxGapTextField.setEnabled(true);
					usingDimers = true;
				} else {
					dimerMinGapTextField.setEnabled(false);
					dimerMaxGapTextField.setEnabled(false);
					usingDimers = false;
				}
			}
		});
	}

	/* accessors */
	/**
	 * 
	 * @return
	 */
	public double getIntervalBase() {
		try {
			this.intervalBase = Double.valueOf(intervalBaseTextField.getText());
		} catch (NumberFormatException nfe) {
			JOptionPane.showMessageDialog(null,
					"Must use numeric values for interval base.", "Error",
					JOptionPane.ERROR_MESSAGE);
			throw new RuntimeException(nfe.getMessage());
		}

		if (intervalBase < 0) {
			JOptionPane.showMessageDialog(null, "Interval base "
					+ CANNOT_BE_NEGATIVE, "Error", JOptionPane.ERROR_MESSAGE);
			throw new RuntimeException("Interval base " + CANNOT_BE_NEGATIVE);
		}

		return intervalBase;
	}

	/**
	 * 
	 * @return
	 */
	public double getIntervalBound() {
		try {
			this.intervalBound = Double.valueOf(intervalBoundTextField
					.getText());
		} catch (NumberFormatException nfe) {
			JOptionPane.showMessageDialog(null,
					"Must use numeric values for interval bound.", "Error",
					JOptionPane.ERROR_MESSAGE);
			throw new RuntimeException(nfe.getMessage());
		}

		if (intervalBound < 0) {
			JOptionPane.showMessageDialog(null, "Interval bound "
					+ CANNOT_BE_NEGATIVE, "Error", JOptionPane.ERROR_MESSAGE);
			throw new RuntimeException("Interval bound " + CANNOT_BE_NEGATIVE);
		}

		return intervalBound;
	}

	/**
	 * 
	 * @return
	 */
	public int getBoostingIterations() {
		try {
			boostingIterations = Integer.valueOf(boostingIterationsTextField
					.getText());
		} catch (NumberFormatException nfe) {
			JOptionPane.showMessageDialog(null, NUMERIC_VALUES_ONLY, "Error",
					JOptionPane.ERROR_MESSAGE);
			throw new RuntimeException(nfe.getMessage());
		}

		if (boostingIterations < 0) {
			JOptionPane.showMessageDialog(null, "Boosting iterations "
					+ CANNOT_BE_NEGATIVE, "Error", JOptionPane.ERROR_MESSAGE);
			throw new RuntimeException("Boosting iterations "
					+ CANNOT_BE_NEGATIVE);
		}

		return boostingIterations;
	}

	/**
	 * 
	 * @return
	 */
	public int getMaxGap() {
		try {
			maxGap = Integer.valueOf(dimerMaxGapTextField.getText());
		} catch (NumberFormatException nfe) {
			JOptionPane.showMessageDialog(null, NUMERIC_VALUES_ONLY, "Error",
					JOptionPane.ERROR_MESSAGE);
			throw new RuntimeException(nfe.getMessage());
		}

		if (maxGap < 0) {
			JOptionPane.showMessageDialog(null,
					"Max gap " + CANNOT_BE_NEGATIVE, "Error",
					JOptionPane.ERROR_MESSAGE);
			throw new RuntimeException("Max gap " + CANNOT_BE_NEGATIVE);
		}
		return maxGap;
	}

	/**
	 * 
	 * @return
	 */
	public int getMinGap() {

		try {
			minGap = Integer.valueOf(dimerMinGapTextField.getText());
		} catch (NumberFormatException nfe) {
			JOptionPane.showMessageDialog(null, NUMERIC_VALUES_ONLY, "Error",
					JOptionPane.ERROR_MESSAGE);
			throw new RuntimeException(nfe.getMessage());
		}

		if (minGap < 0) {
			JOptionPane.showMessageDialog(null, "Min gap cannot be negative.",
					"Error", JOptionPane.ERROR_MESSAGE);
			throw new RuntimeException("Min gap " + CANNOT_BE_NEGATIVE);
		}

		return minGap;
	}

	/**
	 * 
	 * @return
	 */
	public int getMaxKmer() {
		try {
			maxKmer = Integer.valueOf(maxKmerTextField.getText());
		} catch (NumberFormatException nfe) {
			JOptionPane.showMessageDialog(null, NUMERIC_VALUES_ONLY, "Error",
					JOptionPane.ERROR_MESSAGE);
			throw new RuntimeException(nfe.getMessage());
		}

		if (maxKmer < 0) {
			JOptionPane.showMessageDialog(null, "Max kmer "
					+ CANNOT_BE_NEGATIVE, "Error", JOptionPane.ERROR_MESSAGE);
			throw new RuntimeException("Max kmer " + CANNOT_BE_NEGATIVE);
		}
		return maxKmer;
	}

	/**
	 * 
	 * @return
	 */
	public int getMinKmer() {
		try {
			minKmer = Integer.valueOf(minKmerTextField.getText());
		} catch (NumberFormatException nfe) {
			JOptionPane.showMessageDialog(null, NUMERIC_VALUES_ONLY, "Error",
					JOptionPane.ERROR_MESSAGE);
			throw new RuntimeException(nfe.getMessage());
		}

		if (minKmer < 0) {
			JOptionPane.showMessageDialog(null, "Min kmer "
					+ CANNOT_BE_NEGATIVE, "Error", JOptionPane.ERROR_MESSAGE);
			throw new RuntimeException("Min kmer " + CANNOT_BE_NEGATIVE);
		}

		return minKmer;
	}

	/**
	 * 
	 * @return
	 */
	public int getAgg() {
		try {
			agg = Integer.valueOf(aggTextField.getText());
		} catch (NumberFormatException nfe) {
			JOptionPane.showMessageDialog(null, NUMERIC_VALUES_ONLY, "Error",
					JOptionPane.ERROR_MESSAGE);
			throw new RuntimeException(nfe.getMessage());
		}

		if (agg < 0) {
			JOptionPane.showMessageDialog(null, "Agglomerations "
					+ CANNOT_BE_NEGATIVE, "Error", JOptionPane.ERROR_MESSAGE);
			throw new RuntimeException("Agglomerations " + CANNOT_BE_NEGATIVE);
		}

		return agg;
	}

	public int getPssmLength() {
		return pssmLength;
	}

	public boolean isUsingDimers() {
		return usingDimers;
	}

	/**
	 * 
	 * @return
	 */
	public boolean isUseSelectedAsRegulators() {
		return useSelectedAsRegulators;
	}

	public String getRegulatorsFilePath() {
		return regulatorsFilePath;
	}

	/**
	 * 
	 * @return
	 */
	public JTextField getRegulatorTextField() {
		return regulatorTextField;
	}

	public String getTargetsFilePath() {
		return targetsFilePath;
	}

	/**
	 * 
	 * @return
	 */
	public boolean isUseAllAsTargets() {
		return useAllAsTargets;
	}

	public boolean isReverseComplement() {
		return reverseComplement;
	}

	public String getFeaturesFilePath() {
		return this.featuresFilePath;
	}

	public String getConfigFilePath() {
		return this.configFilePath;
	}

	public void setConfigFilePath(String configFilePath) {
		this.configFilePath = configFilePath;
	}

	/* mutators */
	public void setAggTextField(JTextField aggTextField) {
		this.aggTextField = aggTextField;
	}

	public void setAllArraysCheckBox(JCheckBox allArraysCheckBox) {
		this.allArraysCheckBox = allArraysCheckBox;
	}

	public void setDimersCombo(JComboBox dimersCombo) {
		this.dimersCombo = dimersCombo;
	}

	public void setIntervalBaseTextField(JTextField intervalBaseTextField) {
		this.intervalBaseTextField = intervalBaseTextField;
	}

	public void setIntervalBoundTextField(JTextField intervalBoundTextField) {
		this.intervalBoundTextField = intervalBoundTextField;
	}

	public void setPssmLengthTextField(JTextField pssmLengthTextField) {
		this.pssmLengthTextField = pssmLengthTextField;
	}

	public void setRegulatorTextField(JTextField regulatorTextField) {
		this.regulatorTextField = regulatorTextField;
	}

	public void setReverseComplementCombo(JComboBox reverseComplementCombo) {
		this.reverseComplementCombo = reverseComplementCombo;
	}

	public void setTargetCombo(JComboBox targetCombo) {
		this.targetCombo = targetCombo;
	}

	public void setDimerMaxGapTextField(JTextField dimerMaxGapTextField) {
		this.dimerMaxGapTextField = dimerMaxGapTextField;
	}

	public void setDimerMinGapTextField(JTextField dimerMinGapTextField) {
		this.dimerMinGapTextField = dimerMinGapTextField;
	}

	public JTextField getTargetTextField() {
		return targetTextField;
	}

	public String getLabelsFilePath() {
		return labelsFilePath;
	}

	public void setLabelsFilePath(String labelsFilePath) {
		this.labelsFilePath = labelsFilePath;
	}
}
