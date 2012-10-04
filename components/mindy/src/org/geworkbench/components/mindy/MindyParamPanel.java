package org.geworkbench.components.mindy;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

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
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.bison.model.analysis.ParamValidationResults;
import org.geworkbench.events.listeners.ParameterActionListener;
import org.geworkbench.util.ValidationUtils;

import wb.data.Marker;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

/**
 * MINDY analysis GUI. Allows the user to enter parameters to analyze.
 * 
 * @author mhall, ch2514, yc2480
 * @author oshteynb
 * @version $Id: MindyParamPanel.java,v 1.31 2009-06-19 19:22:55 jiz Exp $
 */
@SuppressWarnings("serial")
public class MindyParamPanel extends AbstractSaveableParameterPanel {

	private static final String P_VALUE_DEFAULT_VALUE = "1e-2";

	private static final String MI_DEFAULT_VALUE = "0.1";

	static Log log = LogFactory.getLog(MindyParamPanel.class);

	static final String FROM_ALL = "All Markers";

	private static final String FROM_FILE = "From File";

	private static final String FROM_SETS = "From Set";

	public static final String P_VALUE = "P-Value";

	public static final String MI = "Mutual Info";

	public static final String NONE = "None";

	public static final String BONFERRONI = "Bonferroni";

	private static final String[] MOD_FROM = { FROM_FILE, FROM_SETS };

	private static final String[] TARGET_FROM = { FROM_ALL, FROM_FILE,
			FROM_SETS };

	static final String[] DEFAULT_SET = { " " };

	private static final String[] CONDITIONAL = { MI, P_VALUE };
	private static final String[] UNCONDITIONAL = { MI, P_VALUE };

	private static final String[] CONDITIONAL_DEFAULT_VALUES = {
			MI_DEFAULT_VALUE, P_VALUE_DEFAULT_VALUE };
	private static final String[] UNCONDITIONAL_DEFAULT_VALUES = {
			MI_DEFAULT_VALUE, P_VALUE_DEFAULT_VALUE };

	private static final String[] CORRECTIONS = { NONE, BONFERRONI };

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

	private JComboBox conditionalCombo = new JComboBox(CONDITIONAL);

	private JComboBox unconditionalCombo = new JComboBox(UNCONDITIONAL);

	private JTextField conditional = new JTextField(
			CONDITIONAL_DEFAULT_VALUES[0]);

	private JTextField unconditional = new JTextField(
			UNCONDITIONAL_DEFAULT_VALUES[0]);

	private JComboBox conditionalCorrection = new JComboBox(CORRECTIONS);

	private JComboBox unconditionalCorrection = new JComboBox(CORRECTIONS);

	private JTabbedPane tabs;

	private DSDataSet<?> dataSet;

	private boolean calledFromProgram = false;
	
	private ParameterActionListener parameterActionListener; 

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

	public DSPanel<DSGeneMarker> getSelectorPanel() {
		return selectorPanel;
	}

	public JComboBox getTargetsSets() {
		return targetsSets;
	}

	public JComboBox getTargetsFrom() {
		return targetsFrom;
	}

	void setDataSet(DSDataSet<?> ds) {
		this.dataSet = ds;
	}

	private void init() {
		parameterActionListener = new ParameterActionListener(this);
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
		
		//add parameterActionListener
		modulatorsFrom.addActionListener(parameterActionListener);
		modulatorsSets.addActionListener(parameterActionListener);
		modulatorList.addActionListener(parameterActionListener);
		modulatorList.addFocusListener(parameterActionListener);
		targetsFrom.addActionListener(parameterActionListener);
		targetsSets.addActionListener(parameterActionListener);
		targetList.addActionListener(parameterActionListener);
		targetList.addFocusListener(parameterActionListener);
		transcriptionFactor.addActionListener(parameterActionListener);
		transcriptionFactor.addFocusListener(parameterActionListener);
		
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
					if (!calledFromProgram) {
						modulatorList.setText("");
					}
					loadModulatorsFile.setEnabled(true);
				} else {
					modulatorsSets.setEnabled(true);
					if (!calledFromProgram) {
						modulatorList.setText("");
					}
					loadModulatorsFile.setEnabled(false);
				}
			}
		});

		targetsFrom.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				String selected = (String) targetsFrom.getSelectedItem();
				if (StringUtils.equals(selected, FROM_ALL)) {
					targetsSets.setSelectedIndex(0);
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
				if (!modulatorsSets.isEnabled())
					return;
				String selectedLabel = (String) modulatorsSets
						.getSelectedItem();
				if (!StringUtils.isEmpty(selectedLabel))
					if (!chooseMarkersFromSet(selectedLabel, modulatorList)) {
						modulatorsSets.setSelectedIndex(0);
						if (!calledFromProgram) {
							modulatorList.setText("");
						}
					}
			}
		});

		targetsSets.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				if (!targetsSets.isEnabled())
					return;
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
						reader.close();

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
						reader.close();
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
		
		//add parameterActionListener
		setFraction.addChangeListener(parameterActionListener);
		conditionalCombo.addActionListener(parameterActionListener);
		conditional.addActionListener(parameterActionListener);
		conditional.addFocusListener(parameterActionListener);
		conditionalCorrection.addActionListener(parameterActionListener);
		unconditionalCombo.addActionListener(parameterActionListener);
		unconditional.addActionListener(parameterActionListener);
		unconditional.addFocusListener(parameterActionListener);
		unconditionalCorrection.addActionListener(parameterActionListener);
		dpiAnnotationList.addActionListener(parameterActionListener);
		dpiAnnotationList.addFocusListener(parameterActionListener);
		dpiTolerance.addChangeListener(parameterActionListener);		 
		
		
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

		builder.append("Conditional", this.conditionalCombo, 3);
		builder.append(this.conditional);
		builder.append("Correction", this.conditionalCorrection, 3);
		builder.append(new JLabel(""));

		builder.append("Unconditional", this.unconditionalCombo, 3);
		builder.append(this.unconditional);
		builder.append("Correction", this.unconditionalCorrection, 3);
		builder.append(new JLabel(""));

		unconditionalCombo.setEnabled(false);
		unconditional.setEnabled(false);
		unconditionalCorrection.setEnabled(false);

		builder.append("DPI Target List", dpiAnnotationList, 3);
		builder.append(loadDPIAnnotationFile);
		builder.append("", new JLabel(""));
		builder.append("", new JLabel(""));

		builder.append("DPI Tolerance", dpiTolerance);
		builder.append("", new JLabel(""));
		builder.append("", new JLabel(""));
		builder.append("", new JLabel(""));

		dpiAnnotationList.setEnabled(false);
		loadDPIAnnotationFile.setEnabled(false);
		dpiTolerance.setEnabled(false);

		builder.nextRow();
		result.add(builder.getPanel());

		// setting up default selections
		this.conditionalCombo.setSelectedIndex(0);
		this.conditionalCorrection.setSelectedIndex(0);
		this.unconditionalCombo.setSelectedIndex(0);
		this.unconditionalCorrection.setSelectedIndex(0);

		// Have to set this to default off.
		conditionalCorrection.setEnabled(false);

		this.conditionalCombo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				conditional.setText(""
						+ CONDITIONAL_DEFAULT_VALUES[conditionalCombo
								.getSelectedIndex()]);
				if (getConditional().trim().equals(P_VALUE)) {
					conditionalCorrection.setEnabled(true);
				} else {
					conditionalCorrection.setSelectedIndex(0);
					conditionalCorrection.setEnabled(false);
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
						reader.close();
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
				CONDITIONAL_DEFAULT_VALUES[this.conditionalCombo
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.geworkbench.analysis.AbstractSaveableParameterPanel#setParameters
	 * (java.util.Map) Set inputed parameters to GUI.
	 */
	public void setParameters(Map<Serializable, Serializable> parameters) {
		
		if (getStopNotifyAnalysisPanelTemporaryFlag() == true)
			return;
		stopNotifyAnalysisPanelTemporary(true);	
		
		Set<Map.Entry<Serializable, Serializable>> set = parameters.entrySet();
		for (Iterator<Map.Entry<Serializable, Serializable>> iterator = set
				.iterator(); iterator.hasNext();) {
			Map.Entry<Serializable, Serializable> parameter = iterator.next();
			Object key = parameter.getKey();
			Object value = parameter.getValue();
			calledFromProgram = true;
			if (key.equals("modulators")) {
				this.modulatorList.setText((String) value);
			}
			if (key.equals("modulatorFromType")) {
				this.modulatorsFrom.setSelectedIndex((Integer) value);
			}
			if (key.equals("modulatorsSet")) {
				this.modulatorsSets.setSelectedItem((String) value);
			}			
			if (key.equals("targets")) {
				this.targetList.setText((String) value);
			}
			if (key.equals("targetFromType")) {
				this.targetsFrom.setSelectedIndex((Integer) value);
			}
			if (key.equals("targetSet")) {
				this.targetsSets.setSelectedItem((String) value);
			}
			if (key.equals("annotations")) {
				this.dpiAnnotationList.setText((String) value);
			}
			if (key.equals("tf")) {
				this.transcriptionFactor.setText((String) value);
			}
			if (key.equals("fraction")) {
				this.setFraction.setValue(value);
			}
			if (key.equals("conditionalType")) {
				int conditionalType = (Integer) value;
				if ((conditionalType >= 0)
						&& (conditionalType < this.conditionalCombo.getModel()
								.getSize()))
					this.conditionalCombo.setSelectedIndex(conditionalType);
			}
			if (key.equals("conditionalValue")) {
				this.conditional.setText((String) value);
			}
			if (key.equals("conditionalCorrection")) {
				int conditionalCorrection = (Integer) value;
				if ((conditionalCorrection >= 0)
						&& (conditionalCorrection < this.conditionalCorrection
								.getModel().getSize()))
					this.conditionalCorrection
							.setSelectedIndex(conditionalCorrection);
			}
			if (key.equals("dpitargets")) {
				this.dpiAnnotationList.setText((String) value);
			}
			if (key.equals("dpitolerance")) {
				this.dpiTolerance.setValue(value);
			}
			calledFromProgram = false;
		}
		
		 
		stopNotifyAnalysisPanelTemporary(false);	
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.geworkbench.analysis.AbstractSaveableParameterPanel#getParameters()
	 */
	public Map<Serializable, Serializable> getParameters() {
		Map<Serializable, Serializable> parameters = new HashMap<Serializable, Serializable>();
		parameters.put("modulators", this.modulatorList.getText());
		parameters.put("modulatorFromType",
				this.modulatorsFrom.getSelectedIndex());
		if (modulatorsSets.getSelectedItem() != null)
	         parameters.put("modulatorsSet", this.modulatorsSets.getSelectedItem().toString());
		parameters.put("targets", this.targetList.getText());
		parameters.put("targetFromType", this.targetsFrom.getSelectedIndex());	 
		if (targetsSets.getSelectedItem() != null)
		   parameters.put("targetsSet", this.targetsSets.getSelectedItem().toString());
		parameters.put("annotations", this.dpiAnnotationList.getText());
		parameters.put("tf", this.transcriptionFactor.getText());
		parameters.put("fraction", (Integer) this.setFraction.getValue());
		parameters.put("conditionalType",
				this.conditionalCombo.getSelectedIndex());
		parameters.put("conditionalValue", this.conditional.getText());
		parameters.put("conditionalCorrection",
				this.conditionalCorrection.getSelectedIndex());
		parameters.put("dpitargets", this.dpiAnnotationList.getText());
		parameters.put("dpitolerance", (Double) this.dpiTolerance.getValue());
		return parameters;
	}

	@Override
	public String getDataSetHistory() {
		StringBuilder paramDescB = new StringBuilder(
				"Generated by MINDY run with paramters: \n");

		paramDescB.append("[PARA] Modulator list: ");
		ArrayList<Marker> modulators = new ArrayList<Marker>();
		ArrayList<String> modulatorGeneList = getModulatorGeneList();
		if ((modulatorGeneList != null) && (modulatorGeneList.size() > 0)) {
			for (String modGene : modulatorGeneList) {
				paramDescB.append(modGene);
				paramDescB.append(" ");
				modulators.add(new Marker(modGene));

			}
			paramDescB.append("\n");
		}

		ArrayList<Marker> targets = new ArrayList<Marker>();
		ArrayList<String> targetGeneList = getTargetGeneList();

		paramDescB.append("[PARA] Target list: ");
		 
		if ((!getTargetsFrom().getSelectedItem().toString()
				.equals(MindyParamPanel.FROM_ALL)) && (targetGeneList != null) && (targetGeneList.size() > 0)) {
			for (String modGene : targetGeneList) {

				paramDescB.append(modGene);
				paramDescB.append(" ");
				targets.add(new Marker(modGene));

			}
		} else {
			if (getTargetsFrom().getSelectedItem().toString()
					.equals(MindyParamPanel.FROM_ALL))
				paramDescB.append("All Markers");
		}
		
		
		paramDescB.append("\n");

		String transcriptionFactor = getTranscriptionFactor();

		if (!transcriptionFactor.trim().equals("")) {

			paramDescB.append("[PARA] Hub Marker: ");
			paramDescB.append(transcriptionFactor);
			paramDescB.append("\n");

		}

		float setFraction = getSetFraction() / 100f;
		paramDescB.append("[PARA] Sample per Condition(%): ");
		paramDescB.append((int) (setFraction * 100f));
		paramDescB.append("\n");

		paramDescB.append("[PARA] Conditional:\t");
		paramDescB.append(getConditional());
		paramDescB.append(" at ");
		paramDescB.append(getConditionalValue());
		paramDescB.append("\tCorrection: ");
		paramDescB.append(getConditionalCorrection());
		paramDescB.append("\n");

		paramDescB.append("[PARA] DPI Target List: ");
		ArrayList<Marker> dpiAnnots = new ArrayList<Marker>();
		ArrayList<String> dpiAnnotList = getDPIAnnotatedGeneList();
		for (String modGene : dpiAnnotList) {
			paramDescB.append(modGene);
			paramDescB.append(" ");
			dpiAnnots.add(new Marker(modGene));

		}
		paramDescB.append("\n");

		paramDescB.append("[PARA] DPI Tolerance: ");
		paramDescB.append(getDPITolerance());
		paramDescB.append("\n");

		return paramDescB.toString();

	}

	@Override
	public void fillDefaultValues(Map<Serializable, Serializable> parameters) {
		// TODO Auto-generated method stub

	}

	@Override
	public ParamValidationResults validateParameters() {
		String conditionalValue = conditional.getText();
		try {
			double v = Double.parseDouble(conditionalValue);
			String conditionl = getConditional();
			if (conditionl.equals(MI)) {
				if (v < 0.) {
					return new ParamValidationResults(false, MI
							+ " should be greater than or equal to 0.");
				}
			} else if (conditionl.equals(P_VALUE)) {
				if (v > 1. || v < 0.) {
					return new ParamValidationResults(false, P_VALUE
							+ " should be between 0 and 1.");
				}
			}
		} catch (NumberFormatException e) {
			return new ParamValidationResults(false, getConditional()
					+ " not a number.");
		}

		return new ParamValidationResults(true, "Mindy Parameter validated");
	}

}
