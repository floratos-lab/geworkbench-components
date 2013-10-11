package org.geworkbench.components.geneontology2;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.DefaultListCellRenderer;
import javax.swing.SwingUtilities;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.analysis.AbstractSaveableParameterPanel;
import org.geworkbench.bison.annotation.CSAnnotationContextManager;
import org.geworkbench.bison.annotation.DSAnnotationContext;
import org.geworkbench.bison.annotation.DSAnnotationContextManager;
import org.geworkbench.bison.datastructure.biocollections.DSAncillaryDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.CSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.DSBioObject;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.markers.goterms.GeneOntologyTree;
import org.geworkbench.bison.datastructure.bioobjects.microarray.CSAnovaResultSet;
import org.geworkbench.bison.datastructure.bioobjects.microarray.CSSignificanceResultSet;
import org.geworkbench.bison.datastructure.bioobjects.microarray.CSTTestResultSet;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.builtin.projects.DataSetNode;
import org.geworkbench.builtin.projects.DataSetSubNode;
import org.geworkbench.builtin.projects.OboSourcePreference;
import org.geworkbench.builtin.projects.ProjectPanel;
import org.geworkbench.builtin.projects.ProjectTreeNode;
import org.geworkbench.events.listeners.ParameterActionListener;
import org.geworkbench.util.AnnotationInformationManager;
import org.geworkbench.util.BrowserLauncher;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

/**
 * 
 * @author zji
 * @version $Id$
 * 
 */
public class GoAnalysisParameterPanel extends AbstractSaveableParameterPanel {
	private static final long serialVersionUID = -57544738480207581L;
	static Log log = LogFactory.getLog(GoAnalysisParameterPanel.class);

	private JTabbedPane jTabbedPane1 = null;
	private JPanel selectionPanel = null;
	private JPanel ontologizer20Panel = null;

	/*
	 * these names must match the exact same names coded in ontologizer2.0's
	 * classes that implement interface AbstractTestCorrection.
	 */
	private static String[] correctionMethodName = { "Benjamini-Hochberg",
			"Benjamini-Yekutieli", "Bonferroni", "Bonferroni-Holm", "None",
			"Westfall-Young-Single-Step", "Westfall-Young-Step-Down" };
	/*
	 * these names must match the exact same names coded in ontologizer2.0's
	 * classes that implement interface ICalculation.
	 */
	private static String[] calculationMethodName = { "Parent-Child-Union",
			"Parent-Child-Intersection", "Probabilistic", "Term-For-Term",
			"Topology-Elim", "Topology-Weighted" };

	/* referent list = population; change list = study set */
	private static JComboBox referenceListSource = new JComboBox(new String[] {
			"All Genes", "From Set", "From File" });
	private static JComboBox changedListSource = new JComboBox(new String[] {
			"From Set", "From File", "From Result Node" });

	private JComboBox referenceListSets = null;
	private JComboBox changedListSets = null;
	private JTextField referenceList = null;
	private JTextField changedList = null;
	private JButton referenceListLoadButton = null;
	private JButton changedListLoadButton = null;

	private JTextField ontologyFileNameField = null;
	private JTextField annotationFileNameField = null;

	private JComboBox calculationMethod = null;
	private JComboBox correctionMethod = null;

	private CSMicroarraySet dataset;

	private JRadioButton loadedAnnotationsRadioButton;

	private JRadioButton alternateAnnotationRadioButton;

	private JTextField alternateAnnotationFileName;
	private JButton loadAlternateAnnotationButton;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.geworkbench.analysis.AbstractSaveableParameterPanel#setParameters
	 * (java.util.Map) Set inputed parameters to GUI.
	 */
	public void setParameters(Map<Serializable, Serializable> parameters) {
		Set<Map.Entry<Serializable, Serializable>> set = parameters.entrySet();
		String changedListSetName = null;
		for (Iterator<Map.Entry<Serializable, Serializable>> iterator = set
				.iterator(); iterator.hasNext();) {
			Map.Entry<Serializable, Serializable> parameter = iterator.next();
			Object key = parameter.getKey();
			Object value = parameter.getValue();

			// set the parameters on GUI based on the Map
			if (key.equals("referenceListSource")) {
				referenceListSource.setSelectedItem(value);
			}
			if (key.equals("referenceListSets")) {
				referenceListSets.setSelectedItem(value);
			}
			if (key.equals("referenceList")) {
				referenceList.setText((String) value);
			}
			if (key.equals("changedListSource")) {
				changedListSource.setSelectedItem(value);
			}
			if (key.equals("changedListSets")) {
				changedListSetName = (String)value;
			}
			if (key.equals("changedList")) {
				changedList.setText((String) value);
			}

			if (key.equals("ontologyFile")) {
				ontologyFileNameField.setText((String) value);
			}
			if (key.equals("loadedAnnotation")) {
				if ((Boolean) value)
					loadedAnnotationsRadioButton.setSelected(true);
				else
					alternateAnnotationRadioButton.setSelected(true);
			}
			if (key.equals("loadedAnnotationFile"))
				annotationFileNameField.setText((String) value);
			if (key.equals("alternateAnnotationFile"))
				alternateAnnotationFileName.setText((String) value);

			if (key.equals("calculationMethod")) {
				calculationMethod.setSelectedItem(value);
			}
			if (key.equals("correctionMethod")) {
				correctionMethod.setSelectedItem(value);
			}
		}
		
		changedListSets.setSelectedItem(changedListSetName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.geworkbench.analysis.AbstractSaveableParameterPanel#getParameters()
	 */
	public Map<Serializable, Serializable> getParameters() {
		Map<Serializable, Serializable> parameters = new HashMap<Serializable, Serializable>();

		// set the Map with the parameter values retrieved from GUI
		// component
		parameters.put("referenceListSource",
				(String) referenceListSource.getSelectedItem());
		parameters.put("referenceListSets",
				(String) referenceListSets.getSelectedItem());
		parameters.put("referenceList", referenceList.getText());
		parameters.put("changedListSource",
				(String) changedListSource.getSelectedItem());
		parameters.put("changedListSets",
				(String) changedListSets.getSelectedItem());
		parameters.put("changedList", changedList.getText());

		parameters.put("ontologyFile", ontologyFileNameField.getText());

		parameters.put("loadedAnnotation",
				loadedAnnotationsRadioButton.isSelected());
		parameters.put("loadedAnnotationFile",
				annotationFileNameField.getText());
		parameters.put("alternateAnnotationFile",
				alternateAnnotationFileName.getText());

		parameters.put("calculationMethod",
				(String) calculationMethod.getSelectedItem());
		parameters.put("correctionMethod",
				(String) correctionMethod.getSelectedItem());

		return parameters;
	}

	public GoAnalysisParameterPanel() {
		try {
			init();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/* this is called from GoAnalysis */
	String[] getReferenceGeneList() {
		String listString = referenceList.getText().trim();
		if (listString.length() == 0)
			return null;

		return listString.split(",| ");
	}

	/* this is called from getDataHistory */
	private String getReferenceGeneListAsString() {
		String[] genes = getReferenceGeneList();
		StringBuffer sb = new StringBuffer(genes[0]);
		for (int index = 1; index < genes.length; index++) {
			sb.append(", ").append(genes[index]);
		}
		return sb.toString();
	}

	public String[] getChangedGeneList() {
		String listString = changedList.getText().trim();

		switch (changedListSource.getSelectedIndex()) {
		case 0: // from set
			if (listString.length() == 0)
				return null;
			return listString.split(",| ");
		case 1: // from file - duplicate for now - in case this is changed to
				// file name instead
			if (listString.length() == 0)
				return null;
			return listString.split(",| ");
		case 2: // from result node of T-test and anova
			if (tTestResult == null)
				return null;

			// it is really a scene to use bison stuff
			CSSignificanceResultSet<DSGeneMarker> significanceResultSet = tTestResult
					.get((String) (changedListSets.getSelectedItem()));
			DSPanel<DSGeneMarker> markers = significanceResultSet
					.getSignificantMarkers();
			List<String> geneFromTTest = new ArrayList<String>();
			for (DSGeneMarker marker : markers) {
				String name = marker.getGeneName().trim(); // can be multiple
															// names - not
															// implement in
															// geWorkbench yet
				if (!name.equals("---")) {
					geneFromTTest.add(name);
				}
			}
			return geneFromTTest.toArray(new String[geneFromTTest.size()]);
		default:
			return null; // never-happen case
		}
	}

	/* this is called from getDataHistory */
	private Object getChangedGeneListAsString() {
		String[] genes = getChangedGeneList();
		StringBuffer sb = new StringBuffer(genes[0]);
		for (int index = 1; index < genes.length; index++) {
			sb.append(", ").append(genes[index]);
		}
		return sb.toString();
	}

	public String getOntologyFile() {
		return GeneOntologyTree.getInstanceUntilAvailable().getActualFile();
	}

	public String getAssociationFile() {
		if (loadedAnnotationsRadioButton.isSelected())
			return annotationFileNameField.getText();
		else if (alternateAnnotationRadioButton.isSelected())
			return alternateAnnotationFileName.getText();
		else {
			log.error("invalid annotation/association choice");
			return null;
		}
	}

	public String getCalculationMethod() {
		return (String) calculationMethod.getSelectedItem();
	}

	public String getCorrectionMethod() {
		return (String) correctionMethod.getSelectedItem();
	}

	private void init() throws Exception {
		this.setLayout(new BorderLayout());
		jTabbedPane1 = new JTabbedPane();
		this.add(jTabbedPane1, BorderLayout.CENTER);

		selectionPanel = new JPanel();
		selectionPanel.setLayout(new BorderLayout());

		// Selection panel - the first of the two parameter panel
		jTabbedPane1.add(selectionPanel, "Selection");
		{
			FormLayout layout = new FormLayout(
					"right:max(10dlu;pref), 3dlu, pref, 7dlu, "
							+ "left:max(10dlu;pref), 3dlu, pref, 7dlu, "
							+ "right:max(10dlu;pref), 3dlu, pref, 7dlu ", "");
			DefaultFormBuilder builder = new DefaultFormBuilder(layout);
			builder.setDefaultDialogBorder();

			builder.appendSeparator("Gene lists selection");

			referenceListSets = new JComboBox();
			referenceListSets.setRenderer(new ComboboxToolTipRenderer());
			referenceListSets.setPrototypeDisplayValue("WWWWWWWWWWWWWWWWWW"); // set
			// expected
			// width
			referenceList = new JTextField(20);
			referenceListLoadButton = new JButton("Load");
			builder.append("Reference Gene List", referenceListSource,
					referenceListSets, referenceList, referenceListLoadButton);
			builder.nextLine();
			changedListSets = new JComboBox();
		    changedListSets.setRenderer(new ComboboxToolTipRenderer());
			changedListSets.setPrototypeDisplayValue("WWWWWWWWWWWWWWWWWW"); // set
			// expected
			// width
			changedList = new JTextField(20);
			changedListLoadButton = new JButton("Load");
			builder.append("Changed Gene List", changedListSource,
					changedListSets, changedList, changedListLoadButton);
			builder.nextLine();

			builder.appendSeparator("Ontology selection");

			JRadioButton geneOntologyRadioButton = new JRadioButton();
			geneOntologyRadioButton.setText("Gene Ontlogy");
			geneOntologyRadioButton.setSelected(true);
			geneOntologyRadioButton.setEnabled(false);

			ontologyFileNameField = new JTextField(20);
			GeneOntologyTree goTree = GeneOntologyTree.getInstance();
			if(goTree!=null) {
				ontologyFileNameField.setText(goTree.getActualSource());
			}
			ontologyFileNameField.setEnabled(false); // this is always
														// disabled even in the
														// future version with
														// alternate ontology
			builder.append("", geneOntologyRadioButton, ontologyFileNameField);
			builder.nextLine();

			JRadioButton alternateOntologyRadioButton = new JRadioButton();
			alternateOntologyRadioButton.setText("Alternate Ontlogy"); // this
																		// is
																		// only
																		// for
																		// 'later'
																		// implementation
																		// according
																		// to
																		// spec
			alternateOntologyRadioButton.setEnabled(false);
			JTextField alternateOntologyField = new JTextField(20);
			alternateOntologyField.setEnabled(false);
			JButton loadButton = new JButton("Load");
			loadButton.setEnabled(false);
			builder.append("", alternateOntologyRadioButton,
					alternateOntologyField, loadButton);
			builder.nextLine();

			ButtonGroup ontologyGroup = new ButtonGroup();
			ontologyGroup.add(geneOntologyRadioButton);
			ontologyGroup.add(alternateOntologyRadioButton);
			selectionPanel.add(builder.getPanel(), BorderLayout.CENTER);
		}

		ontologizer20Panel = new JPanel();
		ontologizer20Panel.setLayout(new BorderLayout());

		// ontologizer 2.0 panel - the second of the two parameter panels
		jTabbedPane1.add(ontologizer20Panel, "Ontologizer 2.0");
		{
			FormLayout layout = new FormLayout(
					"right:max(10dlu;pref), 3dlu, pref, 7dlu, "
							+ "left:max(10dlu;pref), 3dlu, pref, 7dlu, "
							+ "right:max(10dlu;pref), 3dlu, pref, 7dlu ", "");
			DefaultFormBuilder builder = new DefaultFormBuilder(layout);
			builder.setDefaultDialogBorder();

			builder.append("", new JLabel("Annotations"));
			builder.nextLine();

			annotationFileNameField = new JTextField(20);
			annotationFileNameField.setEditable(false);
			loadedAnnotationsRadioButton = new JRadioButton();
			loadedAnnotationsRadioButton.setSelected(true);
			loadedAnnotationsRadioButton.setText("Use loaded annotation");
			builder.append("", loadedAnnotationsRadioButton,
					annotationFileNameField);
			builder.nextLine();

			alternateAnnotationRadioButton = new JRadioButton();
			alternateAnnotationRadioButton
					.setText("Use alternate annotation file");
			alternateAnnotationFileName = new JTextField(20);
			loadAlternateAnnotationButton = new JButton("Browse");
			builder.append("", alternateAnnotationRadioButton,
					alternateAnnotationFileName, loadAlternateAnnotationButton);
			loadAlternateAnnotationButton
					.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							loadAlternateAnnotation();
						}
					});
			builder.nextLine();

			calculationMethod = new JComboBox(calculationMethodName);
			calculationMethod.setSelectedIndex(3); // default to Term-For-Term
			builder.append("", new JLabel("Enrichment Method"),
					calculationMethod);
			builder.nextLine();
			correctionMethod = new JComboBox(correctionMethodName);
			correctionMethod.setSelectedIndex(4); // default to None
			builder.append("", new JLabel("Multiple testing Correction"),
					correctionMethod);
			// builder.nextLine();

			ButtonGroup ontologyGroup = new ButtonGroup();
			ontologyGroup.add(loadedAnnotationsRadioButton);
			ontologyGroup.add(alternateAnnotationRadioButton);

			builder.nextLine();
			builder.append(""); // just to add empty on GUI
			builder.nextLine();
			builder.append(""); // just to add empty on GUI
			builder.nextLine();
			builder.append(""); // just to add empty on GUI
			JLabel linkedLabel = new JLabel(
					"<html><head></head><body><a href=\"\">About Ontologizer 2.0</a></body></html>");
			linkedLabel.addMouseListener(new MouseAdapter() {

				public void mouseClicked(MouseEvent e) {
					try {
						BrowserLauncher
								.openURL("http://compbio.charite.de/index.php/ontologizer2.html");
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			});
			builder.append("", new JLabel(), new JLabel(), linkedLabel);

			ontologizer20Panel.add(builder.getPanel(), BorderLayout.CENTER);

		}

		referenceListSource.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				JComboBox cb = (JComboBox) e.getSource();
				switch (cb.getSelectedIndex()) {
				case 0:// all genes
					referenceListSets.setEnabled(false);
					if (referenceListSets.getItemCount() > 0)
					   referenceListSets.setSelectedIndex(0);
					referenceList.setText(getAllGenesAsString());
					referenceList.setEditable(false);
					referenceList.setEnabled(true);
					referenceListLoadButton.setEnabled(false);
					break;
				case 1:// from set
					referenceListSets.setEnabled(true);
					referenceList.setEnabled(true);
					referenceList.setEditable(true);
					referenceListLoadButton.setEnabled(false);
					GoAnalysisParameterPanel.this
							.refreshMarkerSetList(referenceListSets);
					break;
				case 2: // from file
					referenceListSets.setEnabled(false);
					if (referenceListSets.getItemCount() > 0)
					    referenceListSets.setSelectedIndex(0);
					referenceList.setEnabled(false);
					referenceListLoadButton.setEnabled(true);
					break;
				}
			}

		});
		changedListSource.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				JComboBox cb = (JComboBox) e.getSource();
				switch (cb.getSelectedIndex()) {
				case 0:// from set
					changedListSets.setEnabled(true);
					changedList.setEnabled(true);
					changedListLoadButton.setEnabled(false);
					GoAnalysisParameterPanel.this
							.refreshMarkerSetList(changedListSets);
					break;
				case 1: // from file
					changedListSets.setEnabled(false);
					if (changedListSets.getItemCount() > 0)
						changedListSets.setSelectedIndex(0);				 
					changedList.setEnabled(false);
					changedListLoadButton.setEnabled(true);
					break;
				case 2: // from result node
					findAvailableSets();
					changedList.setEnabled(false);
					changedListLoadButton.setEnabled(false);
					break;
				}
			}

		});

		referenceListLoadButton.addActionListener(new LoadButtonListener(
				referenceList));
		changedListLoadButton.addActionListener(new LoadButtonListener(
				changedList));

		// // this setting maps the source choice of 'From Set'
		// referenceListSets.setEnabled(true);
		// referenceList.setEnabled(true);
		referenceListLoadButton.setEnabled(false);
		changedListSets.setEnabled(true);
		changedList.setEnabled(true);
		changedListLoadButton.setEnabled(false);

		referenceListSets.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				String selectedLabel = (String) referenceListSets
						.getSelectedItem();
				if (!StringUtils.isEmpty(selectedLabel))
					if (!chooseMarkersFromSet(selectedLabel, referenceList)) {
						referenceListSets.setSelectedIndex(0);
						referenceList.setText("");
					}
			}
		});

		changedListSets.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				String selectedLabel = (String) changedListSets
						.getSelectedItem();
				if (!StringUtils.isEmpty(selectedLabel))
					if (!chooseMarkersFromSet(selectedLabel, changedList)) {
						changedListSets.setSelectedIndex(0);
						changedList.setText("");
					}
			}
		});

		// define the 'update/refreshing'behavior of GUI components - see the
		// examples
		// they are (basically) all the same
		ParameterActionListener parameterActionListener = new ParameterActionListener(
				this);
		referenceListSource.addActionListener(parameterActionListener);
		referenceListSets.addActionListener(parameterActionListener);
		referenceList.addActionListener(parameterActionListener);
		changedListSource.addActionListener(parameterActionListener);
		changedListSets.addActionListener(parameterActionListener);
		changedList.addActionListener(parameterActionListener);
		ontologyFileNameField.addActionListener(parameterActionListener);
		loadedAnnotationsRadioButton.addActionListener(parameterActionListener);
		alternateAnnotationRadioButton
				.addActionListener(parameterActionListener);
		annotationFileNameField.addActionListener(parameterActionListener);
		alternateAnnotationFileName.addActionListener(parameterActionListener);

		calculationMethod.addActionListener(parameterActionListener);
		correctionMethod.addActionListener(parameterActionListener);
	}

	private Map<String, CSSignificanceResultSet<DSGeneMarker>> tTestResult = null;

	protected void findAvailableSets() {
		changedListSets.removeAllItems();
		changedListSets.setEnabled(true);

		tTestResult = getSignificanceResultNodes();
		for (String dataSetName : tTestResult.keySet()) {
			changedListSets.addItem(dataSetName);
			log.debug("t-test and ANOVA result node: " + dataSetName);
		}
	}

	static private Map<String, CSSignificanceResultSet<DSGeneMarker>> getSignificanceResultNodes() {
		Map<String, CSSignificanceResultSet<DSGeneMarker>> map = new HashMap<String, CSSignificanceResultSet<DSGeneMarker>>();
		DataSetNode dataSetNode = ProjectPanel.getInstance().getSelection()
				.getSelectedDataSetNode();
		searchTestResultNodes(dataSetNode, map, CSTTestResultSet.class);
		searchTestResultNodes(dataSetNode, map, CSAnovaResultSet.class);
		return map;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static void searchTestResultNodes(ProjectTreeNode pnode,
			Map<String, CSSignificanceResultSet<DSGeneMarker>> map,
			Class<? extends CSSignificanceResultSet> clazz) {
		if (pnode instanceof DataSetSubNode) {
			DSAncillaryDataSet<DSBioObject> dNodeFile = ((DataSetSubNode) pnode)._aDataSet;
			if (clazz.isInstance(dNodeFile)) {
				map.put(dNodeFile.getDataSetName(),
						(CSSignificanceResultSet) dNodeFile);
			}
		}

		Enumeration children = pnode.children();
		while (children.hasMoreElements()) {
			ProjectTreeNode child = (ProjectTreeNode) children.nextElement();
			searchTestResultNodes(child, map, clazz);
		}
	}

	protected void loadAlternateAnnotation() {
		final JFileChooser fc = new JFileChooser();
		int returnVal = fc.showOpenDialog(this);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			alternateAnnotationFileName.setText(file.getAbsolutePath());
		} else {
			// if canceled, do nothing
		}
	}

	private Set<String> getAllGenes() {
		Set<String> set = new HashSet<String>();
		if (dataset == null)
			return set; // in case maSet is not properly set

		for (DSGeneMarker marker : dataset.getMarkers()) {
			String geneName = marker.getGeneName().trim();
			if (!geneName.equals("---")) {
				set.add(geneName);
			}
		}
		return set;
	}

	private String getAllGenesAsString() {
		Set<String> allGenes = getAllGenes();
		if (allGenes == null || allGenes.size() == 0)
			return "";

		StringBuffer sb = new StringBuffer("");
		for (String gene : allGenes) {
			if (sb.length() == 0)
				sb.append(gene);
			else
				sb.append(", ").append(gene);
		}
		return sb.toString();
	}

	/**
	 * Get the list of available mark sets.
	 */
	private List<String> getMarkerSets() {
		List<String> list = new ArrayList<String>();
		if (dataset == null)
			return list; // in case maSet is not properly set

		DSAnnotationContextManager manager = CSAnnotationContextManager
				.getInstance();
		DSAnnotationContext<DSGeneMarker> markerSet = manager
				.getCurrentContext(dataset.getMarkers());

		for (int cx = 0; cx < markerSet.getNumberOfLabels(); cx++) {
			list.add(markerSet.getLabel(cx));
		}

		return list;
	}

	@Override
	public void fillDefaultValues(Map<Serializable, Serializable> parameters) {
		if (parameters.get("referenceListSource") == null)
			parameters.put("referenceListSource",
					(String) referenceListSource.getItemAt(0));
		if (parameters.get("referenceListSets") == null)
			parameters.put("referenceListSets",
					(String) referenceListSets.getItemAt(0));
		if (parameters.get("referenceList") == null)
			parameters.put("referenceList", "");
		if (parameters.get("changedListSource") == null)
			parameters.put("changedListSource",
					(String) changedListSource.getItemAt(0));
		if (parameters.get("changedListSets") == null)
			parameters.put("changedListSets",
					(String) changedListSets.getItemAt(0));
		if (parameters.get("changedList") == null)
			parameters.put("changedList", "");

		if (parameters.get("ontologyFile") == null)
			parameters.put("ontologyFile", OboSourcePreference.getInstance()
					.getSourceLocation());

		if (parameters.get("loadedAnnotation") == null)
			parameters.put("loadedAnnotation", true);
		if (parameters.get("loadedAnnotationFile") == null)
			parameters.put("loadedAnnotationFile", "");
		if (parameters.get("alternateAnnotationFile") == null)
			parameters.put("alternateAnnotationFile", "");

		if (parameters.get("calculationMethod") == null)
			parameters.put("calculationMethod",
					(String) calculationMethod.getItemAt(3)); // default to
																// term-for-term
		if (parameters.get("correctionMethod") == null)
			parameters.put("correctionMethod",
					(String) correctionMethod.getItemAt(4)); // default to
																// 'none'
	}

	private void refreshMarkerSetList(JComboBox listSets) {
		listSets.removeAllItems();
		List<String> allMarkerSet = getMarkerSets();
		listSets.addItem(" ");
		for (String setName : allMarkerSet) {
			listSets.addItem(setName);
		}
	}

	public void setDataset(CSMicroarraySet d) {
		dataset = d;
		if(AnnotationInformationManager.getInstance().is3Prime(d))
			annotationFileNameField.setText(dataset.getAnnotationFileName());
		else
			annotationFileNameField.setText("");

		switch (referenceListSource.getSelectedIndex()) {
		case 1:
			refreshMarkerSetList(referenceListSets);
			break; // from set
		case 0:
			referenceList.setText(getAllGenesAsString());
			referenceList.setEditable(false);
			break; // all genes
		}
		/* only do this if 'from set' is chosen */
		if (changedListSource.getSelectedIndex() == 0) {
			refreshMarkerSetList(changedListSets);
		}
		repaint();
	}

	@Override
	public String getDataSetHistory() {
		StringBuilder histStr = new StringBuilder("");
		histStr.append("GO Terms Analysis parameters:\n");
		histStr.append("----------------------------------------\n");
		histStr.append("Reference Gene List: ");
		histStr.append(getReferenceGeneListAsString());
		histStr.append("\nChanged Gene List: ");
		histStr.append(getChangedGeneListAsString());
		histStr.append("\nGene Ontology: ");
		histStr.append(GeneOntologyTree.getInstance().getActualSource());
		histStr.append("\nAnnotations: ");
		histStr.append(getAssociationFile());
		histStr.append("\nEnrichment Method: ");
		histStr.append(getCalculationMethod());
		histStr.append("\nMultiple Testing Correction: ");
		histStr.append(getCorrectionMethod() + "\n");

		return histStr.toString();
	}

	private class ComboboxToolTipRenderer extends DefaultListCellRenderer  {
  
	 
	private static final long serialVersionUID = -1299748207172613887L;

	@Override
	public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			
		  Component comp = (Component) super.getListCellRendererComponent(list,
	                value, index, isSelected, cellHasFocus);

		  if (isSelected && value != null) {
				if (-1 < index) {
					list.setToolTipText((value == null) ? "" : value.toString());
				}		      
		      
		  }
			 
		  
		 return comp;
		}
	}

	private class LoadButtonListener implements ActionListener {
		private JTextField referenceList = null;

		public LoadButtonListener(JTextField referenceList) {
			this.referenceList = referenceList;
		}

		public void actionPerformed(ActionEvent e) {
			JFileChooser fc = new JFileChooser();
			int returnVal = fc.showOpenDialog(GoAnalysisParameterPanel.this);

			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				String referenceFileName = file.getAbsolutePath();
				// this could be passed to analysis instead of re-creating
				// temporary file
				// nevertheless, we need to get the content only for showing
				BufferedReader br;
				try {
					br = new BufferedReader(new FileReader(referenceFileName));
					String line = br.readLine();
					StringBuffer sb = new StringBuffer();
					if (line == null) {
						referenceList.setText(sb.toString());
						br.close();
						return;
					}
					sb.append(line);
					line = br.readLine();
					while (line != null) {
						sb.append(", ").append(line);
						line = br.readLine();
					}
					referenceList.setText(sb.toString());
				} catch (FileNotFoundException e1) {
					e1.printStackTrace();
				} catch (IOException e2) {
					e2.printStackTrace();
				}
			} else {
				// if canceled, do nothing
			}
		}

	}

	/* This method is invoked from both EDT and non-EDT. TODO */
	void setSelectorPanel(final GoAnalysisParameterPanel aspp,
			final DSPanel<DSGeneMarker> ap) {
		if (SwingUtilities.isEventDispatchThread()) {
			setSelectorPanelFromEDT(aspp, ap);
		} else {
			try {
				SwingUtilities.invokeAndWait(new Runnable() {

					@Override
					public void run() {
						setSelectorPanelFromEDT(aspp, ap);
					}

				});
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}
	}

	private void setSelectorPanelFromEDT(final GoAnalysisParameterPanel aspp,
			final DSPanel<DSGeneMarker> ap) {
		aspp.selectorPanel = ap;
		if (changedListSource.getSelectedIndex() == 0) // from set
			modifyListSets(aspp, changedListSets, changedList);
		if (referenceListSource.getSelectedIndex() == 1) // from set
			modifyListSets(aspp, referenceListSets, referenceList);
	}

	private void modifyListSets(GoAnalysisParameterPanel aspp, JComboBox setListSets,
			JTextField setList) {
		String currentTargetSet = (String) setListSets.getSelectedItem();
		DefaultComboBoxModel targetComboModel = (DefaultComboBoxModel) setListSets
				.getModel();
		targetComboModel.removeAllElements();
		targetComboModel.addElement(" ");
		setList.setText("");
		for (DSPanel<DSGeneMarker> panel : selectorPanel.panels()) {
			String label = panel.getLabel().trim();
			targetComboModel.addElement(label);
			if (currentTargetSet != null) {
				if (StringUtils.equals(label, currentTargetSet.trim())) {
					targetComboModel.setSelectedItem(label);
				}
			}
		}
	}

	@Override
	public boolean chooseMarkersFromSet(String setLabel, JTextField toPopulate) {
		DSPanel<DSGeneMarker> selectedSet = chooseMarkersSet(setLabel,
				selectorPanel);

		if (selectedSet != null) {
			if (selectedSet.size() > 0) {
				StringBuilder sb = new StringBuilder();
				for (DSGeneMarker m : selectedSet) {
					String geneName = m.getGeneName().trim();
					if (!geneName.equals("---")) {
						sb.append(m.getGeneName());
						sb.append(",");
					}
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

}
