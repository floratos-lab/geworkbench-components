package org.geworkbench.components.medusa;

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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.analysis.AbstractSaveableParameterPanel;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

/**
 * 
 * @author keshav
 * @version $Id: MedusaParamPanel.java,v 1.6 2007-05-01 19:02:47 keshav Exp $
 */
public class MedusaParamPanel extends AbstractSaveableParameterPanel implements
		Serializable {

	private static final String FASTA_PREFIX = ">";

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

	/* features */
	private JButton loadFeaturesButton = new JButton("Load Features");

	private String featuresListFile = new String("data/test.txt");

	/* regulators */
	private String REGULATOR_LIST = "Specify (csv)";

	private String REGULATOR_ACTIVATED = "Activated Markers";

	private JComboBox regulatorCombo = new JComboBox(new String[] {
			REGULATOR_ACTIVATED, REGULATOR_LIST });

	private String DEFAULT_REGULATOR_LIST = null;

	private JTextField regulatorTextField = new JTextField(
			DEFAULT_REGULATOR_LIST);

	private JButton loadRegulatorsButton = new JButton("Load Regulators");

	private String regulatorsFile = new String("data/regulators.txt");

	/* targets */
	private String TARGET_LIST = "Specify (csv)";

	private String TARGET_ALL = "Activated Markers";

	private JComboBox targetCombo = new JComboBox(new String[] { TARGET_ALL,
			TARGET_LIST });

	private String DEFAULT_TARGET_LIST = null;

	private JTextField targetTextField = new JTextField(DEFAULT_TARGET_LIST);

	private JButton loadTargetsButton = new JButton("Load Targets");

	private String targetsFile = new String("data/targets.txt");

	/* discretization interval */

	private double intervalBase = 0;

	private JTextField intervalBaseTextField = new JTextField(String
			.valueOf(intervalBase));

	private double intervalBound = 0;

	private JTextField intervalBoundTextField = new JTextField(String
			.valueOf(intervalBound));

	/* boosting iterations */
	private int boostingIterations = 0;

	private JTextField boostingIterationsTextField = new JTextField(String
			.valueOf(boostingIterations));

	/* all arrays */
	private JCheckBox allArraysCheckBox = new JCheckBox("", true);

	/* SECONDARY PANEL */

	/* min kmers */
	private int minKmer = 3;

	private JTextField minKmerTextField = new JTextField(minKmer);

	/* max kmers */
	private int maxKmer = 7;

	private JTextField maxKmerTextField = new JTextField(maxKmer);

	/* dimers */
	private JComboBox dimersCombo = new JComboBox(new String[] { NO, YES });

	private int minGap = 0;

	private JTextField dimerMinGapTextField = new JTextField(minGap);

	private int maxGap = 2;

	private JTextField dimerMaxGapTextField = new JTextField(maxGap);

	/* reverse compliment */
	private JComboBox reverseComplementCombo = new JComboBox(new String[] {
			TRUE, FALSE });

	/* pssm length */
	private int pssmLength = 5;

	private JTextField pssmLengthTextField = new JTextField(pssmLength);

	/* agglomerations per round */
	private int agg = 5;

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
		// this.intervalBaseTextField.setEnabled(false);
		// this.intervalBoundTextField.setEnabled(false);

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
		secondaryBuilder.append("Minimum Kmer", minKmerTextField);
		secondaryBuilder.nextRow();

		secondaryBuilder.append("Maximum Kmer", maxKmerTextField);
		secondaryBuilder.nextRow();

		/* dimers */
		secondaryBuilder.append("Learn Dimers", dimersCombo);
		secondaryBuilder.nextRow();

		/* dimer gaps */
		secondaryBuilder.append("Dimer Min Gap", dimerMinGapTextField);
		secondaryBuilder.nextRow();

		secondaryBuilder.append("Dimer Max Gap", dimerMaxGapTextField);
		secondaryBuilder.nextRow();

		/* reverse complement */
		secondaryBuilder.append("Reverse Complement", reverseComplementCombo);
		secondaryBuilder.nextRow();

		/* pssm length */
		secondaryBuilder.append("Max PSSM Length", pssmLengthTextField);
		secondaryBuilder.nextRow();

		/* agglomerations per round */
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

		/* features */
		// button listener
		loadFeaturesButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				StringBuilder geneListBuilder = new StringBuilder();
				try {
					File targetFile = new File(featuresListFile);
					JFileChooser chooser = new JFileChooser(targetFile
							.getParent());
					int retVal = chooser.showOpenDialog(MedusaParamPanel.this);

					if (retVal == JFileChooser.APPROVE_OPTION) {
						featuresListFile = chooser.getSelectedFile().getPath();

						BufferedReader reader = new BufferedReader(
								new FileReader(featuresListFile));
						String target = reader.readLine();

						if (!target.startsWith(FASTA_PREFIX)) {
							JOptionPane.showMessageDialog(null,
									"Not in FASTA format.", "Error",
									JOptionPane.ERROR_MESSAGE);
						}

						while (target != null && !"".equals(target)) {

							geneListBuilder.append(target + ", ");
							target = reader.readLine();
						}

						// String geneString = geneListBuilder.toString();
						// featuresList.setText(geneString.substring(0,
						// geneString.length() - 2));
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
				} else {
					regulatorTextField.setEnabled(true);
					loadRegulatorsButton.setEnabled(true);
				}
			}
		});

		// button listener
		loadRegulatorsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				StringBuilder geneListBuilder = new StringBuilder();
				try {
					File hubFile = new File(regulatorsFile);
					JFileChooser chooser = new JFileChooser(hubFile.getParent());
					int retVal = chooser.showOpenDialog(MedusaParamPanel.this);

					if (retVal == JFileChooser.APPROVE_OPTION) {
						regulatorsFile = chooser.getSelectedFile().getPath();

						BufferedReader reader = new BufferedReader(
								new FileReader(regulatorsFile));
						String hub = reader.readLine();
						while (hub != null && !"".equals(hub)) {
							geneListBuilder.append(hub + ", ");
							hub = reader.readLine();
						}

						String geneString = geneListBuilder.toString();
						regulatorTextField.setText(geneString.substring(0,
								geneString.length() - 2));
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
				} else {
					targetTextField.setEnabled(true);
					loadTargetsButton.setEnabled(true);
				}
			}
		});
		// button listener
		loadTargetsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				// targetGenes = new ArrayList<String>();
				StringBuilder geneListBuilder = new StringBuilder();
				try {
					File targetFile = new File(targetsFile);
					JFileChooser chooser = new JFileChooser(targetFile
							.getParent());
					int retVal = chooser.showOpenDialog(MedusaParamPanel.this);

					if (retVal == JFileChooser.APPROVE_OPTION) {
						targetsFile = chooser.getSelectedFile().getPath();

						BufferedReader reader = new BufferedReader(
								new FileReader(targetsFile));
						String target = reader.readLine();
						while (target != null && !"".equals(target)) {
							// targetGenes.add(target);
							geneListBuilder.append(target + ", ");
							target = reader.readLine();
						}

						String geneString = geneListBuilder.toString();
						targetTextField.setText(geneString.substring(0,
								geneString.length() - 2));
					} else {
						log.debug("cancelled ... ");
					}

				} catch (IOException e) {
					log.error(e);
				}

			}
		});
	}

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
			JOptionPane.showMessageDialog(null, "Numeric values only.",
					"Error", JOptionPane.ERROR_MESSAGE);
			throw new RuntimeException(nfe.getMessage());
		}
		return boostingIterations;
	}
}
