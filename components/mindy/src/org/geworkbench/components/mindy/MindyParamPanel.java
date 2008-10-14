package org.geworkbench.components.mindy;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.ArrayList;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.analysis.AbstractSaveableParameterPanel;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.util.ValidationUtils;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

/**
 * MINDY analysis GUI. Allows the user to enter parameters to analyze.
 * 
 * @author mhall
 * @author ch2514
 * @version $ID$
 */
@SuppressWarnings("serial")
public class MindyParamPanel extends AbstractSaveableParameterPanel implements
		Serializable {

	static Log log = LogFactory.getLog(MindyParamPanel.class);

	private static final String FROM_ALL = "All Markers";

	private static final String FROM_FILE = "From File";

	private static final String FROM_SETS = "From Set";

	public static final String P_VALUE = "P-Value";

	public static final String MI = "Mutual Info";

	public static final String NONE = "None";

	public static final String BONFERRONI = "Bonferroni";

	private static final String[] MOD_FROM = { FROM_FILE, FROM_SETS };

	private static final String[] TARGET_FROM = { FROM_ALL, FROM_FILE,
			FROM_SETS };

	private static final String[] DEFAULT_SET = { " " };

	private static final String[] CONDITIONAL_UNCONDITIONAL = { P_VALUE, MI };

	private static final String[] CONDITIONAL_UNCONDITIONAL_DEFAULT_VALUES = {
			"1e-2", "0" };

	private static final String[] CORRECTIONS = { NONE, BONFERRONI };

	private static final String DEFAULT_THRESHOLD_VALUE = CONDITIONAL_UNCONDITIONAL_DEFAULT_VALUES[0];

	private static final int MAX_ERROR_MESSAGE_LENGTH = 100;

	private static final String DEFAULT_ERROR_MESSAGE_MARKER = "Please use a valid marker file.";

	private JButton loadModulatorsFile = new JButton("Load");

	private JButton loadDPIAnnotationFile = new JButton("Load");

	private JButton loadTargetsFile = new JButton("Load");

	private String candidateModulatorsFile = new String(
			"data/mindy/candidateModulators.txt");

	private String modulatorFile = "data/mindy/candidate_modulator.lst";

	private String targetFile = "data/mindy/candidate_target.lst";

	private String dpiAnnotationFile = "data/mindy/transcription_factor.lst";

	private JComboBox modulatorsFrom = new JComboBox(MOD_FROM);

	private JComboBox modulatorsSets = new JComboBox(new DefaultComboBoxModel(
			DEFAULT_SET));

	private JComboBox targetsFrom = new JComboBox(TARGET_FROM);

	private JComboBox targetsSets = new JComboBox(new DefaultComboBoxModel(
			DEFAULT_SET));

	private JTextField modulatorList = new JTextField("");

	private JTextField targetList = new JTextField("");

	private JTextField dpiAnnotationList = new JTextField("");

	private JTextField transcriptionFactor = new JTextField("");

	private JSpinner setFraction = new JSpinner(new SpinnerNumberModel(35, 1,
			49, 1));

	private JSpinner dpiTolerance = new JSpinner(new SpinnerNumberModel(0.1d,
			0d, 1d, 0.1d));

	private JComboBox conditionalCombo = new JComboBox(
			CONDITIONAL_UNCONDITIONAL);

	private JComboBox unconditionalCombo = new JComboBox(
			CONDITIONAL_UNCONDITIONAL);

	private JTextField conditional = new JTextField(DEFAULT_THRESHOLD_VALUE);

	private JTextField unconditional = new JTextField(DEFAULT_THRESHOLD_VALUE);

	private JComboBox conditionalCorrection = new JComboBox(CORRECTIONS);

	private JComboBox unconditionalCorrection = new JComboBox(CORRECTIONS);

	private JTabbedPane tabs;

	private DSDataSet dataSet;

	private DSPanel<DSGeneMarker> selectorPanel;

	/**
	 * Constructor. Creates the parameter panel GUI.
	 * 
	 */
	public MindyParamPanel() {
		super();
		try {
			init();
		} catch (Exception e) {
			e.printStackTrace();
			log.debug("Cannot initialize MINDY parameter panel.", e);
		}
	}

	void setDataSet(DSDataSet ds) {
		this.dataSet = ds;
	}

	private void init() {
		tabs = new JTabbedPane();
		tabs.addTab("Main", initMainPanel());
		tabs.addTab("Advanced", initAdvancedPanel());
		this.setLayout(new BorderLayout());
		this.add(tabs, BorderLayout.PAGE_START);
	}

	private JPanel initMainPanel() {
		modulatorsFrom.setSelectedIndex(0);
		modulatorsSets.setSelectedIndex(0);
		modulatorsSets.setEditable(false);
		modulatorsSets.setEnabled(false);
		targetsFrom.setSelectedIndex(0);
		targetsSets.setSelectedIndex(0);
		targetsSets.setEditable(false);
		targetsSets.setEnabled(false);
		targetList.setEditable(false);
		targetList.setEnabled(false);
		loadTargetsFile.setEnabled(false);

		JPanel result = new JPanel(new BorderLayout());
		FormLayout layout = new FormLayout(
				"left:max(100dlu;pref), 10dlu, 100dlu, 10dlu, "
						+ "100dlu, 10dlu, 100dlu, 10dlu, 100dlu", "");
		DefaultFormBuilder builder = new DefaultFormBuilder(layout);
		builder.setDefaultDialogBorder();
		builder.appendSeparator("MINDY Main Parameters");
		builder.append("Modulators List");
		builder.append(modulatorsFrom);
		builder.append(modulatorsSets);
		builder.append(modulatorList);
		builder.append(loadModulatorsFile);
		builder.nextLine();

		builder.append("Target List");
		builder.append(targetsFrom);
		builder.append(targetsSets);
		builder.append(targetList);
		builder.append(loadTargetsFile);
		builder.nextLine();

		builder.append("Hub Marker", transcriptionFactor);
		result.add(builder.getPanel());

		modulatorsFrom.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				String selected = (String) modulatorsFrom.getSelectedItem();
				if (StringUtils.equals(selected, FROM_FILE)) {
					modulatorsSets.setSelectedIndex(0);
					modulatorsSets.setEnabled(false);
					modulatorList.setText("");
					loadModulatorsFile.setEnabled(true);
				} else {
					modulatorsSets.setEnabled(true);
					modulatorList.setText("");
					loadModulatorsFile.setEnabled(false);
				}
			}
		});

		targetsFrom.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				String selected = (String) targetsFrom.getSelectedItem();
				if (StringUtils.equals(selected, FROM_ALL)) {
					targetsSets.setEnabled(false);
					targetList.setText("");
					targetList.setEditable(false);
					targetList.setEnabled(false);
					loadTargetsFile.setEnabled(false);
				} else if (StringUtils.equals(selected, FROM_FILE)) {
					targetsSets.setSelectedIndex(0);
					targetsSets.setEnabled(false);
					targetList.setText("");
					targetList.setEditable(true);
					targetList.setEnabled(true);
					loadTargetsFile.setEnabled(true);
				} else {
					targetsSets.setEnabled(true);
					targetList.setText("");
					targetList.setEditable(true);
					targetList.setEnabled(true);
					loadTargetsFile.setEnabled(false);
				}
			}
		});

		modulatorsSets.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				String selectedLabel = (String) modulatorsSets
						.getSelectedItem();
				if (!StringUtils.isEmpty(selectedLabel))
					if (!chooseMarkersFromSet(selectedLabel, modulatorList)) {
						modulatorsSets.setSelectedIndex(0);
						modulatorList.setText("");
					}
			}
		});

		targetsSets.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				String selectedLabel = (String) targetsSets.getSelectedItem();
				if (!StringUtils.isEmpty(selectedLabel))
					if (!chooseMarkersFromSet(selectedLabel, targetList)) {
						targetsSets.setSelectedIndex(0);
						targetList.setText("");
					}
			}
		});

		loadModulatorsFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				StringBuilder geneListBuilder = new StringBuilder();
				try {
					File hubFile = new File(modulatorFile);
					JFileChooser chooser = new JFileChooser(hubFile.getParent());
					chooser.showOpenDialog(MindyParamPanel.this);
					if ((chooser.getSelectedFile() != null)
							&& (chooser.getSelectedFile().getPath() != null)) {
						modulatorFile = chooser.getSelectedFile().getPath();

						BufferedReader reader = new BufferedReader(
								new FileReader(modulatorFile));
						String hub = reader.readLine();
						while (hub != null && !"".equals(hub)) {
							geneListBuilder.append(hub + ", ");
							hub = reader.readLine();
						}

						String geneString = geneListBuilder.toString();
						String s = geneString.substring(0,
								geneString.length() - 2);
						MindyParamPanel.this.getParent().setCursor(
								Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
						boolean valid = ValidationUtils
								.validateMicroarrayMarkers(dataSet, s);
						MindyParamPanel.this.getParent().setCursor(
								Cursor.getDefaultCursor());
						if (valid)
							modulatorList.setText(s);
						else {
							String msg = ValidationUtils.getErrorMessage();
							if (msg.length() > MAX_ERROR_MESSAGE_LENGTH)
								msg = DEFAULT_ERROR_MESSAGE_MARKER;
							JOptionPane.showMessageDialog(null, msg,
									"Parameter and Input Validation Error",
									JOptionPane.ERROR_MESSAGE);
						}
					}

				} catch (IOException e) {
					log.error(e);
				}

			}
		});

		loadTargetsFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				StringBuilder geneListBuilder = new StringBuilder();
				try {
					File hubFile = new File(targetFile);
					JFileChooser chooser = new JFileChooser(hubFile.getParent());
					chooser.showOpenDialog(MindyParamPanel.this);
					if ((chooser.getSelectedFile() != null)
							&& (chooser.getSelectedFile().getPath() != null)) {
						targetFile = chooser.getSelectedFile().getPath();

						BufferedReader reader = new BufferedReader(
								new FileReader(targetFile));
						String line = reader.readLine();
						while (line != null && !"".equals(line)) {
							geneListBuilder.append(line + ", ");
							line = reader.readLine();
						}
						String geneString = geneListBuilder.toString();
						String s = geneString.substring(0,
								geneString.length() - 2);
						MindyParamPanel.this.getParent().setCursor(
								Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
						boolean valid = ValidationUtils
								.validateMicroarrayMarkers(dataSet, s);
						MindyParamPanel.this.getParent().setCursor(
								Cursor.getDefaultCursor());
						if (valid)
							targetList.setText(s);
						else {
							String msg = ValidationUtils.getErrorMessage();
							if (msg.length() > MAX_ERROR_MESSAGE_LENGTH)
								msg = DEFAULT_ERROR_MESSAGE_MARKER;
							JOptionPane.showMessageDialog(null, msg,
									"Parameter and Input Validation Error",
									JOptionPane.ERROR_MESSAGE);
						}
					}

				} catch (IOException e) {
					log.error(e);
				}

			}
		});

		return result;
	}

	private JPanel initAdvancedPanel() {
		JPanel result = new JPanel(new BorderLayout());
		FormLayout layout = new FormLayout(
				"left:max(100dlu;pref), 3dlu, 40dlu, 7dlu, "
						+ "right:max(40dlu;pref), 3dlu, 40dlu, 7dlu, "
						+ "right:max(100dlu;pref), 3dlu, 40dlu, 7dlu, "
						+ "right:max(40dlu;pref), 3dlu, 40dlu, 7dlu", "");
		DefaultFormBuilder builder = new DefaultFormBuilder(layout);
		builder.setDefaultDialogBorder();
		builder.appendSeparator("MINDY Advanced Parameters");
		builder.append("Sample per Condition (%)", setFraction);
		builder.append("", new JLabel(""));
		builder.append("", new JLabel(""));
		builder.append("", new JLabel(""));

		builder.append("Conditional (MINDY)", this.conditionalCombo, 3);
		builder.append(this.conditional);
		builder.append("Correction", this.conditionalCorrection, 3);
		builder.append(new JLabel(""));

		builder.append("Unconditional (ARACNE)", this.unconditionalCombo, 3);
		builder.append(this.unconditional);
		builder.append("Correction", this.unconditionalCorrection, 3);
		builder.append(new JLabel(""));

		builder.append("DPI Target List", dpiAnnotationList, 3);
		builder.append(loadDPIAnnotationFile);
		builder.append("", new JLabel(""));
		builder.append("", new JLabel(""));

		builder.append("DPI Tolerance", dpiTolerance);
		builder.append("", new JLabel(""));
		builder.append("", new JLabel(""));
		builder.append("", new JLabel(""));

		builder.nextRow();
		result.add(builder.getPanel());

		// setting up default selections
		this.conditionalCombo.setSelectedIndex(0);
		this.conditionalCorrection.setSelectedIndex(0);
		this.unconditionalCombo.setSelectedIndex(0);
		this.unconditionalCorrection.setSelectedIndex(0);

		this.conditionalCombo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				conditional
						.setText(""
								+ CONDITIONAL_UNCONDITIONAL_DEFAULT_VALUES[conditionalCombo
										.getSelectedIndex()]);
				if (getConditional().trim().equals(P_VALUE)) {
					conditionalCorrection.setEnabled(true);
				} else {
					conditionalCorrection.setSelectedIndex(0);
					conditionalCorrection.setEnabled(false);
				}
			}
		});
		this.unconditionalCombo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				unconditional
						.setText(""
								+ CONDITIONAL_UNCONDITIONAL_DEFAULT_VALUES[unconditionalCombo
										.getSelectedIndex()]);
				if (getUnconditional().trim().equals(P_VALUE)) {
					unconditionalCorrection.setEnabled(true);
				} else {
					unconditionalCorrection.setSelectedIndex(0);
					unconditionalCorrection.setEnabled(false);
				}
			}
		});

		loadDPIAnnotationFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				StringBuilder geneListBuilder = new StringBuilder();
				try {
					File hubFile = new File(dpiAnnotationFile);
					JFileChooser chooser = new JFileChooser(hubFile.getParent());
					chooser.showOpenDialog(MindyParamPanel.this);
					if ((chooser.getSelectedFile() != null)
							&& (chooser.getSelectedFile().getPath() != null)) {
						dpiAnnotationFile = chooser.getSelectedFile().getPath();

						BufferedReader reader = new BufferedReader(
								new FileReader(dpiAnnotationFile));
						String hub = reader.readLine();
						while (hub != null && !"".equals(hub)) {
							geneListBuilder.append(hub + ", ");
							hub = reader.readLine();
						}
						String geneString = geneListBuilder.toString();
						String s = geneString.substring(0,
								geneString.length() - 2);
						MindyParamPanel.this.getParent().setCursor(
								Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
						boolean valid = ValidationUtils
								.validateMicroarrayMarkers(dataSet, s);
						MindyParamPanel.this.getParent().setCursor(
								Cursor.getDefaultCursor());
						if (valid)
							dpiAnnotationList.setText(s);
						else {
							String msg = ValidationUtils.getErrorMessage();
							if (msg.length() > MAX_ERROR_MESSAGE_LENGTH)
								msg = DEFAULT_ERROR_MESSAGE_MARKER;
							JOptionPane.showMessageDialog(null, msg,
									"Parameter and Input Validation Error",
									JOptionPane.ERROR_MESSAGE);
						}
					}

				} catch (IOException e) {
					log.error(e);
				}

			}
		});

		return result;
	}

	/**
	 * Sets the transcription factor
	 * 
	 * @param label
	 */
	public void setTranscriptionFactor(String label) {
		transcriptionFactor.setText(label);
	}

	/**
	 * Gets the candidate modulator file name.
	 * 
	 * @return candidate modulator file name.
	 */
	public String getCandidateModulatorsFile() {
		return candidateModulatorsFile;
	}

	/**
	 * Gets the set fraction.
	 * 
	 * @return the set fraction
	 */
	public int getSetFraction() {
		return ((Number) setFraction.getModel().getValue()).intValue();
	}

	/**
	 * Gets the DPI tolerance.
	 * 
	 * @return the DPI tolerance
	 */
	public float getDPITolerance() {
		return ((Number) dpiTolerance.getModel().getValue()).floatValue();
	}

	public String getConditional() {
		return (String) this.conditionalCombo.getSelectedItem();
	}

	public float getConditionalValue() {
		float result = new Double(
				CONDITIONAL_UNCONDITIONAL_DEFAULT_VALUES[this.conditionalCombo
						.getSelectedIndex()]).floatValue();
		try {
			result = new Double(this.conditional.getText()).floatValue();
		} catch (NumberFormatException e) {
			log.debug("Failed to get the conditional value. " + e.getMessage());
		}
		return result;
	}

	public String getConditionalCorrection() {
		return (String) this.conditionalCorrection.getSelectedItem();
	}

	public String getUnconditional() {
		return (String) this.unconditionalCombo.getSelectedItem();
	}

	public String getUnconditionalCorrection() {
		return (String) this.unconditionalCorrection.getSelectedItem();
	}

	public float getUnconditionalValue() {
		float result = new Double(
				CONDITIONAL_UNCONDITIONAL_DEFAULT_VALUES[this.unconditionalCombo
						.getSelectedIndex()]).floatValue();
		try {
			result = new Double(this.unconditional.getText()).floatValue();
		} catch (NumberFormatException e) {
			log.debug("Failed to get the conditional value. " + e.getMessage());
		}
		return result;
	}

	/**
	 * Gets the transcription factor.
	 * 
	 * @return the transcription factor
	 */
	public String getTranscriptionFactor() {
		return transcriptionFactor.getText();
	}

	/**
	 * Gets the modulator gene list.
	 * 
	 * @return the modulator gene list
	 */
	public ArrayList<String> getModulatorGeneList() {
		String geneString = modulatorList.getText();
		ArrayList<String> geneList = breakStringIntoGenes(geneString);
		return geneList;
	}

	/**
	 * Gets the target gene list.
	 * 
	 * @return the target gene list
	 */
	public ArrayList<String> getTargetGeneList() {
		String geneString = targetList.getText();
		ArrayList<String> geneList = breakStringIntoGenes(geneString);
		return geneList;
	}

	/**
	 * Gets the DPI annotated gene list.
	 * 
	 * @return the DPI annotated gene list
	 */
	public ArrayList<String> getDPIAnnotatedGeneList() {
		String geneString = dpiAnnotationList.getText();
		ArrayList<String> geneList = breakStringIntoGenes(geneString);
		return geneList;
	}

	private ArrayList<String> breakStringIntoGenes(String geneString) {
		String[] genes = geneString.split(",");
		ArrayList<String> geneList = new ArrayList<String>();
		for (String gene : genes) {
			if (gene != null && !"".equals(gene)) {
				geneList.add(gene.trim());
			}
		}
		return geneList;
	}

	void setSelectorPanel(MindyParamPanel aspp, DSPanel<DSGeneMarker> ap) {
		// everything is keyed off aspp to make sure the project panel, the
		// selector panel, and the analysis panel are in synch.
		String currentModSet = (String) aspp.modulatorsSets.getSelectedItem();
		String currentTargetSet = (String) aspp.targetsSets.getSelectedItem();
		aspp.selectorPanel = ap;
		DefaultComboBoxModel modComboModel = (DefaultComboBoxModel) aspp.modulatorsSets
				.getModel();
		modComboModel.removeAllElements();
		modComboModel.addElement(" ");
		DefaultComboBoxModel targetComboModel = (DefaultComboBoxModel) aspp.targetsSets
				.getModel();
		targetComboModel.removeAllElements();
		targetComboModel.addElement(" ");
		for (DSPanel<DSGeneMarker> panel : selectorPanel.panels()) {
			String label = panel.getLabel().trim();
			modComboModel.addElement(label);
			if (StringUtils.equals(label, currentModSet.trim()))
				modComboModel.setSelectedItem(label);
			targetComboModel.addElement(label);
			if (StringUtils.equals(label, currentTargetSet.trim()))
				targetComboModel.setSelectedItem(label);
		}
	}

	private boolean chooseMarkersFromSet(String setLabel, JTextField toPopulate) {
		if (selectorPanel == null)
			return false;
		DSPanel<DSGeneMarker> selectedSet = null;
		setLabel = setLabel.trim();
		for (DSPanel<DSGeneMarker> panel : selectorPanel.panels()) {
			if (StringUtils.equals(setLabel, panel.getLabel().trim())) {
				selectedSet = panel;
				break;
			}
		}
		if (selectedSet != null) {
			if (selectedSet.size() > 0) {
				StringBuilder sb = new StringBuilder();
				for (DSGeneMarker m : selectedSet) {
					sb.append(m.getLabel());
					sb.append(",");
				}
				sb.trimToSize();
				sb.deleteCharAt(sb.length() - 1); // getting rid of last comma
				toPopulate.setText(sb.toString());
				return true;
			} else {
				JOptionPane.showMessageDialog(null, "Marker set, " + setLabel
						+ ", is empty.", "Input Error",
						JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}
		return false;
	}

	// For framework serialization process
	private static class SerializedInstance implements Serializable {
		private int modulatorFromType, targetFromType;

		private String modulators;

		private String targets;

		private String annotations;

		private String tf;

		private Object fraction;

		private int conditionalType;

		private String conditionalValue;

		private int conditionalCorrection;

		private int unconditionalType;

		private String unconditionalValue;

		private int unconditionalCorrection;

		private String dpitargets;

		private Object dpitolerance;

		public SerializedInstance(String modulators, int modulatorFromType,
				String modulatorSet, String targets, int targetFromType,
				String targetSet, String annotations, String tf,
				Object fraction, int conditionalType, String conditionalValue,
				int conditionalCorrection, int unconditionalType,
				String unconditionalValue, int unconditionalCorrection,
				String dpitargets, Object dpitolerance) {
			this.modulators = modulators;
			this.modulatorFromType = modulatorFromType;
			this.targets = targets;
			this.targetFromType = targetFromType;
			this.annotations = annotations;
			this.tf = tf;
			this.fraction = fraction;
			this.conditionalType = conditionalType;
			this.conditionalValue = conditionalValue;
			this.conditionalCorrection = conditionalCorrection;
			this.unconditionalType = unconditionalType;
			this.unconditionalValue = unconditionalValue;
			this.unconditionalCorrection = unconditionalCorrection;
			this.dpitargets = dpitargets;
			this.dpitolerance = dpitolerance;
		}

		Object readResolve() throws ObjectStreamException {
			MindyParamPanel panel = new MindyParamPanel();
			panel.modulatorsFrom.setSelectedIndex(this.modulatorFromType);
			panel.modulatorList.setText(this.modulators);
			panel.targetsFrom.setSelectedIndex(this.targetFromType);
			panel.targetList.setText(this.targets);
			panel.dpiAnnotationList.setText(this.annotations);
			panel.transcriptionFactor.setText(this.tf);
			panel.setFraction.setValue(this.fraction);
			if ((this.conditionalType >= 0)
					&& (this.conditionalType < panel.conditionalCombo
							.getModel().getSize()))
				panel.conditionalCombo.setSelectedIndex(this.conditionalType);
			panel.conditional.setText(this.conditionalValue);
			if ((this.conditionalCorrection >= 0)
					&& (this.conditionalCorrection < panel.conditionalCorrection
							.getModel().getSize()))
				panel.conditionalCorrection
						.setSelectedIndex(this.conditionalCorrection);
			if ((this.unconditionalType >= 0)
					&& (this.unconditionalType < panel.unconditionalCombo
							.getModel().getSize()))
				panel.unconditionalCombo
						.setSelectedIndex(this.unconditionalType);
			panel.unconditional.setText(this.unconditionalValue);
			if ((this.unconditionalCorrection >= 0)
					&& (this.unconditionalCorrection < panel.unconditionalCorrection
							.getModel().getSize()))
				panel.unconditionalCorrection
						.setSelectedIndex(this.unconditionalCorrection);
			panel.dpiAnnotationList.setText(this.dpitargets);
			panel.dpiTolerance.setValue(this.dpitolerance);

			return panel;
		}
	}

	Object writeReplace() throws ObjectStreamException {
		return new SerializedInstance(this.modulatorList.getText(),
				this.modulatorsFrom.getSelectedIndex(),
				(String) this.modulatorsSets.getSelectedItem(), this.targetList
						.getText(), this.targetsFrom.getSelectedIndex(),
				(String) this.targetsSets.getSelectedItem(),
				this.dpiAnnotationList.getText(), this.transcriptionFactor
						.getText(), this.setFraction.getValue(),
				this.conditionalCombo.getSelectedIndex(), this.conditional
						.getText(), this.conditionalCorrection
						.getSelectedIndex(), this.unconditionalCombo
						.getSelectedIndex(), this.unconditional.getText(),
				this.unconditionalCorrection.getSelectedIndex(),
				this.dpiAnnotationList.getText(), this.dpiTolerance.getValue());
	}

	/**
	 * {@link java.io.Serializable} method
	 * 
	 * @param out
	 *            <code>ObjectOutputStream</code>
	 * @throws IOException
	 */
	private void writeObject(java.io.ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
	}

	/**
	 * {@link java.io.Serializable} method
	 * 
	 * @param in
	 *            <code>ObjectInputStream</code>
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private void readObject(java.io.ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		in.defaultReadObject();
		revalidate();
	}

}
