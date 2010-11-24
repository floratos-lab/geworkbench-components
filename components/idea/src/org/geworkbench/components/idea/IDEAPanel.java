package org.geworkbench.components.idea;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

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
import org.geworkbench.bison.datastructure.bioobjects.microarray.CSAnovaResultSet;
import org.geworkbench.bison.datastructure.bioobjects.microarray.CSMicroarray;
import org.geworkbench.bison.datastructure.bioobjects.microarray.CSSignificanceResultSet;
import org.geworkbench.bison.datastructure.bioobjects.microarray.CSTTestResultSet;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
import org.geworkbench.builtin.projects.DataSetNode;
import org.geworkbench.builtin.projects.DataSetSubNode;
import org.geworkbench.builtin.projects.ProjectPanel;
import org.geworkbench.builtin.projects.ProjectTreeNode;
import org.geworkbench.events.listeners.ParameterActionListener;
import org.geworkbench.util.BrowserLauncher;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

/**
 * IDEAPanel of IDEA analysis component
 * @author zm2165
 * @version $id$
 */
public class IDEAPanel extends AbstractSaveableParameterPanel {
	
	private static final long serialVersionUID = 5983582161253754386L;
	static Log log = LogFactory.getLog(IDEAPanel.class);
	
	private static final float PValueThresholdDefault = 0.05f;
	private JTextField pValueTextField = null;

	private JTabbedPane jTabbedPane1 = null;
	private JPanel selectionPanel = null;
	private JPanel advancedPanel = null;

	

	/* referent list = population; change list = study set */
	private static JComboBox networkSource = new JComboBox(new String[] {
			"From File", "From Set","All Genes"  });
	private static JComboBox phenotypeSource = new JComboBox(new String[] {
			"From File", "From Set", "From Result Node" });

	private JComboBox networkSets = null;
	private JComboBox phenotypeSets = null;
	private JTextField network = null;
	private JTextField phenotype = null;
	private JButton networkLoadButton = null;
	private JButton phenotypeLoadButton = null;

	
	private JTextField annotationFileNameField = null;	

	private CSMicroarraySet<CSMicroarray> dataset;

	private JRadioButton loadedAnnotationsRadioButton;

	private JRadioButton alternateAnnotationRadioButton;

	private JTextField alternateAnnotationFileName;
	private JButton loadAlternateAnnotationButton;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geworkbench.analysis.AbstractSaveableParameterPanel#setParameters(java.util.Map)
	 *      Set inputed parameters to GUI.
	 */
	public void setParameters(Map<Serializable, Serializable> parameters) {
		Set<Map.Entry<Serializable, Serializable>> set = parameters.entrySet();
		for (Iterator<Map.Entry<Serializable, Serializable>> iterator = set
				.iterator(); iterator.hasNext();) {
			Map.Entry<Serializable, Serializable> parameter = iterator.next();
			Object key = parameter.getKey();
			Object value = parameter.getValue();

			// set the parameters on GUI based on the Map
			if (key.equals("networkSource")) {
				networkSource.setSelectedItem(value);
			}
			if (key.equals("networkSets")) {
				networkSets.setSelectedItem(value);
			}
			if (key.equals("network")) {
				network.setText((String)value);
			}
			if (key.equals("phenotypeSource")) {
				phenotypeSource.setSelectedItem(value);
			}
			if (key.equals("phenotypeSets")) {
				phenotypeSets.setSelectedItem(value);
			}
			if (key.equals("phenotype")) {
				phenotype.setText((String)value);
			}
			
			if (key.equals("loadedAnnotation")) {
				if((Boolean)value)
					loadedAnnotationsRadioButton.setSelected(true);
				else
					alternateAnnotationRadioButton.setSelected(true);
			}
			if (key.equals("loadedAnnotationFile"))
				annotationFileNameField.setText((String)value);
			if (key.equals("alternateAnnotationFile"))
				alternateAnnotationFileName.setText((String)value);			
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geworkbench.analysis.AbstractSaveableParameterPanel#getParameters()
	 */
	public Map<Serializable, Serializable> getParameters() {
		Map<Serializable, Serializable> parameters = new HashMap<Serializable, Serializable>();

		// set the Map with the parameter values retrieved from GUI
		// component
		parameters.put("networkSource", (String)networkSource.getSelectedItem());
		parameters.put("networkSets", (String)networkSets.getSelectedItem());
		parameters.put("network", network.getText());
		parameters.put("phenotypeSource", (String)phenotypeSource.getSelectedItem());
		parameters.put("phenotypeSets", (String)phenotypeSets.getSelectedItem());
		parameters.put("phenotype", phenotype.getText());	
		
		
		parameters.put("loadedAnnotation", loadedAnnotationsRadioButton.isSelected());
		parameters.put("loadedAnnotationFile", annotationFileNameField.getText());
		parameters.put("alternateAnnotationFile", alternateAnnotationFileName.getText());
		return parameters;
	}

	public IDEAPanel() {
		try {
			init();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/* this is called from IDEAAnalysis */
	String getNetwork() {
		return network.getText();
	}

	public String getPhenotype() {
			return phenotype.getText();
	}

	/* this is called from getDataHistory */
	private Object getChangedGeneListAsString() {		
		return phenotype.getText();
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

			builder.appendSeparator("Network");

			networkSets = new JComboBox();
			networkSets.setPrototypeDisplayValue("WWWWWWWWWW"); // set expected width
			network = new JTextField(20);
			networkLoadButton = new JButton("Load");
			builder.append("Load Network", networkSource,
					networkSets, network, networkLoadButton);
			builder.nextLine();
			phenotypeSets = new JComboBox();
			phenotypeSets.setPrototypeDisplayValue("WWWWWWWWWW"); // set expected width
			phenotype = new JTextField(20);
			phenotypeLoadButton = new JButton("Load");
			builder.append("Define Phenotype", phenotypeSource,
					phenotypeSets, phenotype, phenotypeLoadButton);
			builder.nextLine();

			builder.appendSeparator("Significance Threshold");
		
			builder.append("P-value");
			if (pValueTextField == null)
				pValueTextField = new JTextField();
			pValueTextField.setText(Float.toString(PValueThresholdDefault));
			builder.append(pValueTextField);
			builder.nextLine();
			
			selectionPanel.add(builder.getPanel(), BorderLayout.CENTER);
		}

		advancedPanel = new JPanel();
		advancedPanel.setLayout(new BorderLayout());

		// ontologizer 2.0 panel - the second of the two parameter panels
		jTabbedPane1.add(advancedPanel, "Advanced");
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

			ButtonGroup ontologyGroup = new ButtonGroup();
			ontologyGroup.add(loadedAnnotationsRadioButton);
			ontologyGroup.add(alternateAnnotationRadioButton);

			builder.append(""); // just to add empty on GUI
			JLabel linkedLabel = new JLabel(
					"<html><head></head><body><a href=\"\">About IDEA</a></body></html>");
			linkedLabel.addMouseListener(new MouseAdapter() {

				public void mouseClicked(MouseEvent e) {
					try {
						BrowserLauncher
								.openURL("http://www.nature.com/msb/journal/v4/n1/full/msb20082.html");
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			});
			builder.append("", new JLabel(), new JLabel(), linkedLabel);

			advancedPanel.add(builder.getPanel(), BorderLayout.CENTER);

		}

		networkSource.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				JComboBox cb = (JComboBox) e.getSource();
				switch (cb.getSelectedIndex()) {
				case 0:// from file
					networkSets.setEnabled(false);
					network.setEnabled(false);
					networkLoadButton.setEnabled(true);
					break;
				case 1:// from set
					networkSets.setEnabled(true);
					network.setEditable(true);
					networkLoadButton.setEnabled(false);
					IDEAPanel.this.refreshMarkerSetList(networkSets);
					break;
				case 2: // all genes					
					networkSets.setEnabled(false);
					network.setText(getAllGenesAsString());
					network.setEditable(false);
					networkLoadButton.setEnabled(false);
					break;
				}
			}

		});
		phenotypeSource.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				JComboBox cb = (JComboBox) e.getSource();
				switch (cb.getSelectedIndex()) {
				case 0:// from set
					phenotypeSets.setEnabled(false);
					phenotype.setEnabled(false);
					phenotypeLoadButton.setEnabled(false);
					IDEAPanel.this.refreshMarkerSetList(phenotypeSets);
					break;
				case 1: // from file
					phenotypeSets.setEnabled(false);
					phenotype.setEnabled(false);
					phenotypeLoadButton.setEnabled(true);
					break;
				case 2: // from result node
					findAvailableSets();
					phenotype.setEnabled(false);
					phenotypeLoadButton.setEnabled(false);
					break;
				}
			}

		});

		networkLoadButton.addActionListener(new LoadButtonListener(
				network));
		phenotypeLoadButton.addActionListener(new LoadButtonListener(
				phenotype));

//		// this setting maps the source choice of 'From Set'
		networkSets.setEnabled(false);
		network.setEnabled(true);
		network.setEditable(false);
		networkLoadButton.setEnabled(true);
		phenotypeSets.setEnabled(false);
		phenotype.setEnabled(true);
		phenotype.setEditable(false);
		phenotypeLoadButton.setEnabled(true);

		networkSets.addActionListener(new GeneSetComboListener(
				network));
		phenotypeSets.addActionListener(new GeneSetComboListener(
				phenotype));

		// define the 'update/refreshing'behavior of GUI components - see the
		// examples
		// they are (basically) all the same
		ParameterActionListener parameterActionListener = new ParameterActionListener(
				this);
		networkSource.addActionListener(parameterActionListener);
		networkSets.addActionListener(parameterActionListener);
		network.addActionListener(parameterActionListener);
		phenotypeSource.addActionListener(parameterActionListener);
		phenotypeSets.addActionListener(parameterActionListener);
		phenotype.addActionListener(parameterActionListener);
		
		loadedAnnotationsRadioButton.addActionListener(parameterActionListener);
		alternateAnnotationRadioButton.addActionListener(parameterActionListener);
		annotationFileNameField.addActionListener(parameterActionListener);
		alternateAnnotationFileName.addActionListener(parameterActionListener);
	}

	private Map<String, CSSignificanceResultSet<DSGeneMarker>> tTestResult = null;

	protected void findAvailableSets() {
		phenotypeSets.removeAllItems();
		phenotypeSets.setEnabled(true);

		tTestResult = getSignificanceResultNodes();
		for (String dataSetName : tTestResult.keySet()) {
			phenotypeSets.addItem(dataSetName);
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

	@SuppressWarnings("unchecked")
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

	/**
	 * Get the gene names of a set.
	 */
	private Set<String> getGeneList(String setName) {
		Set<String> set = new HashSet<String>();
		if (dataset == null)
			return set; // in case maSet is not properly set

		DSAnnotationContextManager manager = CSAnnotationContextManager
				.getInstance();
		DSAnnotationContext<DSGeneMarker> markerSet = manager
				.getCurrentContext(dataset.getMarkers());

		if (setName == null || setName.trim().length() == 0) {
			return set; // return empty list
		} else {
			for (DSGeneMarker marker : markerSet.getItemsWithLabel(setName)) {
				// list.add(marker.getLabel());
				String geneName = marker.getGeneName();
				if (!geneName.equals("---")) {
					set.add(marker.getGeneName()); // use case says "gene names
													// instead of probeset
													// names" p. 7
				}
			}
		}

		return set;
	}
	
	/**
	 * Get all genes from the marker selection panel.
	 * @param setName
	 * @return
	 */
	private Set<String> getAllGenes() {
		Set<String> set = new HashSet<String>();
		if (dataset == null)
			return set; // in case maSet is not properly set

		DSAnnotationContextManager manager = CSAnnotationContextManager
				.getInstance();
		DSAnnotationContext<DSGeneMarker> markerSet = manager
				.getCurrentContext(dataset.getMarkers());

		DSItemList<DSGeneMarker> markers = null;
		try {
			markers = markerSet.getItemList();
		} catch (NullPointerException e) {
			return set;
		}
		if(markers==null) return set;
		
		for ( DSGeneMarker marker : markers ) {
			String geneName = marker.getGeneName().trim();
			if (!geneName.equals("---")) {
				set.add(geneName);
			}
		}
		return set;
	}

	private String getAllGenesAsString() {
		Set<String> allGenes = getAllGenes();
		if(allGenes==null || allGenes.size()==0)return "";
		
		StringBuffer sb = new StringBuffer("");
		for(String gene: allGenes) {
			if(sb.length()==0)
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
		if(parameters.get("networkSource")==null)
			parameters.put("networkSource", (String)networkSource.getItemAt(0));
		if(parameters.get("networkSets")==null)
			parameters.put("networkSets", (String)networkSets.getItemAt(0));
		if(parameters.get("network")==null)
			parameters.put("network", "");
		if(parameters.get("phenotypeSource")==null)
			parameters.put("phenotypeSource", (String)phenotypeSource.getItemAt(0));
		if(parameters.get("phenotypeSets")==null)
			parameters.put("phenotypeSets", (String)phenotypeSets.getItemAt(0));
		if(parameters.get("phenotype")==null)
			parameters.put("phenotype", "");		
		
		if(parameters.get("loadedAnnotation")==null)
			parameters.put("loadedAnnotation", true);
		if(parameters.get("loadedAnnotationFile")==null)
			parameters.put("loadedAnnotationFile", "");
		if(parameters.get("alternateAnnotationFile")==null)
			parameters.put("alternateAnnotationFile", "");		
	}

	private void refreshMarkerSetList(JComboBox listSets) {
		listSets.removeAllItems();
		List<String> allMarkerSet = getMarkerSets();
		for (String setName : allMarkerSet) {
			listSets.addItem(setName);
		}
	}

	public void setDataset(CSMicroarraySet<CSMicroarray> d) {
		dataset = d;
		annotationFileNameField.setText(dataset.getAnnotationFileName());

		switch (networkSource.getSelectedIndex()) {
		case 1:
			refreshMarkerSetList(networkSets);
			break; // from set
		case 0:
			network.setText(getAllGenesAsString());
			network.setEditable(false);
			break; // all genes
		}
		/* only do this if 'from set' is chosen */
		if (phenotypeSource.getSelectedIndex() == 0) {
			refreshMarkerSetList(phenotypeSets);
		}
		repaint();
	}

	@Override
	public String getDataSetHistory() {
		StringBuilder histStr = new StringBuilder("");
		histStr.append("IDEA Analysis parameters:\n");
		histStr.append("----------------------------------------\n");
		histStr.append("Network: ");
		histStr.append("under construction");
		histStr.append("\nPhenotype: ");
		histStr.append(getChangedGeneListAsString());		
		histStr.append("\nAnnotations: ");
		histStr.append(getAssociationFile()+"\n");		

		return histStr.toString();
	}

	/**
	 * Listener to update the marker list based on marker combo selection.
	 * 
	 */
	private class GeneSetComboListener implements ActionListener {
		private JTextField targetField = null;

		GeneSetComboListener(JTextField targetField) {
			super();
			this.targetField = targetField;
		}

		public void actionPerformed(ActionEvent e) {
			JComboBox cb = (JComboBox) e.getSource();
			String setName = (String) cb.getSelectedItem();

			JComboBox sourceComboBox = (JComboBox) (e.getSource());

			if (setName == null || !sourceComboBox.isFocusOwner())
				return; // so do not clear out existing marker list

			Set<String> geneSet = IDEAPanel.this
					.getGeneList(setName);
			StringBuilder sb = new StringBuilder();
			for (String gene : geneSet) {
				if(sb.length()==0)
					sb.append(gene);
				else
					sb.append(", ").append(gene);
			}
			targetField.setText(sb.toString());
		}
	}

	private class LoadButtonListener implements ActionListener {
		private JTextField network = null;

		public LoadButtonListener(JTextField network) {
			this.network = network;
		}

		public void actionPerformed(ActionEvent e) {
			JFileChooser fc = new JFileChooser();
			int returnVal = fc.showOpenDialog(IDEAPanel.this);

			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				String referenceFileName = file.getAbsolutePath(); 
				// this could be passed to analysis instead of re-creating temporary file
				// nevertheless, we need to get the content only for showing
				BufferedReader br;
				try {
					br = new BufferedReader(new FileReader(referenceFileName));
					String line = br.readLine();
					StringBuffer sb = new StringBuffer();
					if (line == null) {
						network.setText(sb.toString());
						return;
					}
					sb.append(line);
					line = br.readLine();
					while (line != null) {
						sb.append(", ").append(line);
						line = br.readLine();
					}
					network.setText(sb.toString());
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
}
