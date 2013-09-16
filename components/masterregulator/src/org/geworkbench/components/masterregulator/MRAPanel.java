package org.geworkbench.components.masterregulator;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.analysis.AbstractSaveableParameterPanel;
import org.geworkbench.bison.datastructure.biocollections.AdjacencyMatrix;
import org.geworkbench.bison.datastructure.biocollections.AdjacencyMatrixDataSet;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.DSBioObject;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.builtin.projects.ProjectPanel;
import org.geworkbench.events.listeners.ParameterActionListener;
import org.geworkbench.parsers.AdjacencyMatrixFileFormat;
import org.geworkbench.parsers.InputFileFormatException;
import org.geworkbench.util.FilePathnameUtils;
import org.geworkbench.util.Util;

import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.list.ArrayListModel;
import com.jgoodies.binding.list.SelectionInList;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * Parameter Panel used for Master Regulator Analysis
 * 
 * @author yc2480
 * @version $Id$
 */
public final class MRAPanel extends AbstractSaveableParameterPanel {
	private static final long serialVersionUID = -6160058089960168299L;

	private static final float PValueThresholdDefault = 0.01f;
	// private static final String TFGeneListDefault =
	// ("AFFX-HUMGAPDH/M33197_3_at, AFFX-HUMGAPDH/M33197_5_at,
	// AFFX-HUMGAPDH/M33197_M_at, AFFX-HUMRGE/M10098_3_at,
	// AFFX-HUMRGE/M10098_M_at");
	private static final String TFGeneListDefault = "";
	private static final String[] DEFAULT_SET = { " " };

	private Log log = LogFactory.getLog(this.getClass());
	private ArrayListModel<String> adjModel; 

	private JTextField pValueTextField = null;
	private JTextField TFGeneListTextField = null; // Marker 1, Marker 2...
	private JTextField networkTextField = null;
	private JTextField sigGeneListTextField = null;
	private AdjacencyMatrixDataSet adjMatrix = null;
	private DSMicroarraySet maSet = null;

	private JComboBox networkMatrix = createNetworkMatrixComboBox();
	private JComboBox tfGroups = new JComboBox(new DefaultComboBoxModel(
			DEFAULT_SET));
	private JComboBox sigGroups = new JComboBox(new DefaultComboBoxModel(
			DEFAULT_SET));;
	private JButton loadNetworkButton = new JButton("Load");
	private JButton loadTFButton = new JButton("Load");
	private JButton loadSigButton = new JButton("Load");
	private JRadioButton fet1Button = new JRadioButton("One (enrichment only)");
	private JRadioButton fet2Button = new JRadioButton("Two (enrichment plus mode of activity)");
	private ButtonGroup fetGroup = new ButtonGroup();
	private JRadioButton ncButton = new JRadioButton("No correction");
	private JRadioButton sbButton = new JRadioButton("Standard Bonferroni");
	private ButtonGroup correctionGroup = new ButtonGroup();
	private JComboBox networkFrom = null;
	private JComboBox tfFrom = null;
	private JComboBox sigFrom = null;

	private static final String lastDirConf = FilePathnameUtils.getUserSettingDirectoryPath()
					+ "masterregulator" + FilePathnameUtils.FILE_SEPARATOR + "lastDir.conf";
	boolean allpos = true;
	private int correlationCol = 3;
	private ArrayList<String> markersets = new ArrayList<String>();
	private ArrayList<String> ttestnodes = new ArrayList<String>();
	

	public MRAPanel() {
		networkTextField = new JTextField();
		networkTextField.setEditable(false);
		FormLayout layout = new FormLayout(
				"left:max(100dlu;pref), 10dlu, 100dlu, 10dlu, "
						+ "100dlu, 10dlu, 100dlu, 10dlu, 100dlu", "");
		DefaultFormBuilder builder = new DefaultFormBuilder(layout);
		builder.setDefaultDialogBorder();
		builder.appendSeparator("Network");
		builder.append("Load Network");
		networkFrom = createNetworkFromComboBox();
		networkFrom.setSelectedIndex(1); // preselect "From File"
		// JComboBox networkMatrix = createNetworkMatrixComboBox();
		builder.append(networkFrom);
		networkMatrix.setEnabled(false);
		builder.append(networkMatrix);

		builder.append(networkTextField);

		// JButton loadNetworkButton=new JButton("Load");
		loadNetworkButton.addActionListener(new LoadNetworkButtonListener());
		builder.append(loadNetworkButton);
		builder.nextLine();

		builder.appendSeparator("Enrichment Threshold");
		builder.append("FET p-value ");
		if (pValueTextField == null)
			pValueTextField = new JTextField();
		pValueTextField.setText(Float.toString(PValueThresholdDefault));
		builder.append(pValueTextField);
		builder.nextLine();

		JTabbedPane jTabbedPane1 = new JTabbedPane();
		jTabbedPane1.addTab("Main", builder.getPanel());

		layout = new FormLayout(
				"left:max(100dlu;pref), 10dlu, 100dlu, 10dlu, "
						+ "100dlu, 10dlu, 100dlu, 10dlu, 100dlu", "");
		builder = new DefaultFormBuilder(layout);
		builder.append("Master Regulators");
		tfFrom = createTFFromComboBox();
		tfFrom.setSelectedIndex(0); // preselect "From File"
		//tfFrom.setEnabled(false);
		// JComboBox tfGroups = createGroupsComboBox();
		builder.append(tfFrom);
		// tfGroups.setEnabled(false);
		builder.append(tfGroups);

		if (TFGeneListTextField == null)
			TFGeneListTextField = new JTextField();
		TFGeneListTextField.setText(TFGeneListDefault);
		builder.append(TFGeneListTextField);
		loadTFButton.addActionListener(new LoadMarkerFileListener());
		builder.append(loadTFButton);
		builder.nextLine();

		if (sigGeneListTextField == null)
			sigGeneListTextField = new JTextField();

		builder.append("Signature Markers");
		sigFrom = createSigFromComboBox();
		sigFrom.setSelectedIndex(0);
		//sigFrom.setEnabled(false);
		// preselect "From File"
		builder.append(sigFrom);
		// sigGroups.setEnabled(false);
		builder.append(sigGroups);

		// sifGeneListTextField.setText(TFGeneListDefault);
		builder.append(sigGeneListTextField);
		loadSigButton.addActionListener(new LoadMarkerFileListener());
		builder.append(loadSigButton);
		builder.nextLine();
		builder.append("");
		builder.nextLine();

		builder.append("FET Runs:");
		builder.append(fet1Button);
		builder.append("");
		builder.append("Multiple Testing Correction:");
		builder.append(ncButton);
		ncButton.setSelected(true);
		builder.nextLine();
		
		builder.append("");
		CellConstraints cc = new CellConstraints();
		builder.add(fet2Button, cc.xyw(builder.getColumn(), builder.getRow(), 3));
		fet2Button.setSelected(true);
		fetGroup.add(fet1Button);
		fetGroup.add(fet2Button);
		builder.append("");builder.append("");builder.append("");
		builder.append(sbButton);
		builder.nextLine();

		builder.append("");builder.append("");builder.append("");builder.append("");
		correctionGroup.add(ncButton);
		correctionGroup.add(sbButton);

		jTabbedPane1.addTab("FET", null, builder.getPanel(), "The paremeters on this tab apply to local FET analysis only.");

		this.add(jTabbedPane1, BorderLayout.CENTER);

		tfGroups.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				String selectedLabel = (String) tfGroups.getSelectedItem();
				if (!StringUtils.isEmpty(selectedLabel))
					if (!chooseMarkersFromSet(selectedLabel,
							TFGeneListTextField)) {
						tfGroups.setSelectedIndex(0);
						TFGeneListTextField.setText(TFGeneListDefault);
					}

			}
		});

		sigGroups.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				String selectedLabel = (String) sigGroups.getSelectedItem();
				if (!StringUtils.isEmpty(selectedLabel) &&
						sigFrom.getSelectedIndex() == 0)
					if (!chooseMarkersFromSet(selectedLabel,
							sigGeneListTextField)) {
						sigGroups.setSelectedIndex(0);
						sigGeneListTextField.setText("");
					}
			}
		});

		parameterActionListener = new ParameterActionListener(
				this);
		TFGeneListTextField.addActionListener(parameterActionListener);
		sigGeneListTextField.addActionListener(parameterActionListener);
		networkTextField.addActionListener(parameterActionListener);
		networkFrom.addActionListener(parameterActionListener);
		networkMatrix.addActionListener(parameterActionListener);
		tfFrom.addActionListener(parameterActionListener);
		sigFrom.addActionListener(parameterActionListener);
		tfGroups.addActionListener(parameterActionListener);
		sigGroups.addActionListener(parameterActionListener);
		pValueTextField.addActionListener(parameterActionListener);

		fet1Button.addActionListener(parameterActionListener);
		fet2Button.addActionListener(parameterActionListener);
		ncButton.addActionListener(parameterActionListener);
		sbButton.addActionListener(parameterActionListener);

		TFGeneListTextField.addFocusListener(parameterActionListener);
		sigGeneListTextField.addFocusListener(parameterActionListener);
		networkTextField.addFocusListener(parameterActionListener);
		networkFrom.addFocusListener(parameterActionListener);
		networkMatrix.addFocusListener(parameterActionListener);
		loadNetworkButton.addFocusListener(parameterActionListener);
		pValueTextField.addFocusListener(parameterActionListener);
	}
	private ParameterActionListener parameterActionListener;

	public boolean twoFET(){
		return fet2Button.isSelected();
	}
	
	public boolean standardBonferroni(){
		return sbButton.isSelected();
	}

	private class LoadNetworkButtonListener implements
			java.awt.event.ActionListener {

		@Override
		public void actionPerformed(java.awt.event.ActionEvent e) {
			if (e.getActionCommand().equals("Load")) {
				if (maSet == null) {
					DSDataSet<? extends DSBioObject> currentDataset = ProjectPanel.getInstance().getDataSet();
					if(currentDataset instanceof DSMicroarraySet) {
						maSet = (DSMicroarraySet) currentDataset;
					} else {
						JOptionPane
								.showMessageDialog(
										MRAPanel.this,
										"The current dataset is not a proper microarray set.",
										"Dataset error",
										JOptionPane.ERROR_MESSAGE);
						return;
					}
				}
				JFileChooser chooser = new JFileChooser();
				String lastDir = null;
				if ((lastDir = getLastDir()) != null) {
					chooser.setCurrentDirectory(new File(lastDir));
				}
				chooser.setFileFilter(new AdjacencyMatrixFileFormat()
						.getFileFilter());
				chooser.showOpenDialog(MRAPanel.this);
				if (chooser.getSelectedFile() != null) {
					File selectedFile = chooser.getSelectedFile();
					String adjMatrixFileStr = selectedFile.getPath();
					networkTextField.setText(adjMatrixFileStr);
					networkFilename = selectedFile.getName();
					saveLastDir(selectedFile.getParent());

					if (!openDialog())
						return;

					// no need to generate adjmatrix for 5col network file
					// because 5col network format is used only by grid mra as a
					// file
					if (!selectedFormat.equals(marina5colformat)) {
						try {
							AdjacencyMatrix matrix = AdjacencyMatrixDataSet
									.parseAdjacencyMatrix(adjMatrixFileStr,
											maSet, interactionTypeMap,
											selectedFormat,
											selectedRepresentedBy, isRestrict);

							adjMatrix = new AdjacencyMatrixDataSet(matrix, 0,
									adjMatrixFileStr, adjMatrixFileStr, maSet);
						} catch (InputFileFormatException e1) {
							log.error(e1.getMessage());
							e1.printStackTrace();
						}
					} else {
						adjMatrix = null;
					}
				} else {
					// user canceled
				}

			}
		}
	}

	private class LoadMarkerFileListener implements
			java.awt.event.ActionListener {

		public void actionPerformed(java.awt.event.ActionEvent e) {
			if (e.getActionCommand().equals("Load")) {
				StringBuilder geneListBuilder = new StringBuilder();
				try {
					String hubMarkersFile = "data/test.txt";
					File hubFile = new File(hubMarkersFile);
					JFileChooser chooser = new JFileChooser(hubFile.getParent());
					chooser.showOpenDialog(MRAPanel.this);
					if (chooser.getSelectedFile() != null) {
						hubMarkersFile = chooser.getSelectedFile().getPath();
						BufferedReader reader = new BufferedReader(
								new FileReader(hubMarkersFile));
						String hub = reader.readLine();
						while (hub != null && !"".equals(hub)) {
							geneListBuilder.append(hub + ", ");
							hub = reader.readLine();
						}
						String geneString = geneListBuilder.toString();
						geneString = geneString.substring(0, geneString
								.length() - 2);
						if (e.getSource().equals(loadTFButton))
							TFGeneListTextField.setText(geneString);
						else if (e.getSource().equals(loadSigButton))
							sigGeneListTextField.setText(geneString);
						reader.close();
					} else {
						// user canceled
					}
				} catch (IOException ioe) {
					log.error(ioe);
				}

			}
		}
	}

	private JComboBox createNetworkFromComboBox() {
		ArrayListModel<String> networkFromModel = new ArrayListModel<String>();
		networkFromModel.add("From Workspace");
		networkFromModel.add("From File");
		NetworkFromListener networkFromListener = new NetworkFromListener();
		SelectionInList<String> selectionInList = new SelectionInList<String>(
				(ListModel) networkFromModel);
		selectionInList.addPropertyChangeListener(networkFromListener);
		return BasicComponentFactory.createComboBox(selectionInList);
	}

	private class NetworkFromListener implements PropertyChangeListener {
		public void propertyChange(PropertyChangeEvent evt) {
			if (evt.getPropertyName() == "value")
				if (evt.getNewValue() == "From Workspace") {
					networkMatrix.setEnabled(true);
					loadNetworkButton.setEnabled(false);
					networkTextField.setEnabled(false);
					// clear combo box
					// load adj matrix into the list
					/*
					 * for (Iterator iterator =
					 * adjacencymatrixDataSets.iterator(); iterator .hasNext();) {
					 * AdjacencyMatrixDataSet element = (AdjacencyMatrixDataSet)
					 * iterator.next();
					 * System.out.println("add"+element.getDataSetName()+"to
					 * combo box."); }
					 */
				} else if (evt.getNewValue() == "From File") {
					networkMatrix.setEnabled(false);
					loadNetworkButton.setEnabled(true);
					networkTextField.setEnabled(true);
					// active load button
					// show file name loaded
				}
		}
	}

	private class TFFromListener implements PropertyChangeListener {
		public void propertyChange(PropertyChangeEvent evt) {
			if (evt.getPropertyName() == "value")
				if (evt.getNewValue() == "From Sets") {
					tfGroups.setEnabled(true);
					loadTFButton.setEnabled(false);
					// hide fileNameField
					// clear combo box
					// load adj matrix into the list
				} else if (evt.getNewValue() == "From File") {
					tfGroups.setEnabled(false);
					loadTFButton.setEnabled(true);
					// active load button
					// show file name loaded
				}
		}
	}

	private class SigFromListener implements PropertyChangeListener {
		public void propertyChange(PropertyChangeEvent evt) {
			if (evt.getPropertyName() == "value")
				if (evt.getNewValue() == "From Sets") {
					sigGroups.setEnabled(true);
					loadSigButton.setEnabled(false);
					sigGeneListTextField.setEnabled(true);
					refreshSigGroups(markersets);
					// hide fileNameField
					// clear combo box
					// load adj matrix into the list
				} else if (evt.getNewValue() == "From File") {
					sigGroups.setEnabled(false);
					loadSigButton.setEnabled(true);
					sigGeneListTextField.setEnabled(true);
					// active load button
					// show file name loaded
				} else if (evt.getNewValue() == "From t-test result node"){
					sigGroups.setEnabled(true);
					loadSigButton.setEnabled(false);
					sigGeneListTextField.setEnabled(false);
					refreshSigGroups(ttestnodes);
				}
		}
	}

	void clearTTestNodes(){
		ttestnodes.clear();
		if (sigFrom.getSelectedItem().equals("From t-test result node"))
			refreshSigGroups(ttestnodes);
	}

	void addTTestNode(String label){
		ttestnodes.add(label);
		if (sigFrom.getSelectedItem().equals("From t-test result node"))
			refreshSigGroups(ttestnodes);
	}
	
	String getTTestNode(){
		if (sigFrom.getSelectedItem().equals("From t-test result node")){
			String label = sigGroups.getSelectedItem().toString().trim();
			return label.length()>0?label:null;
		}
		return null;
	}

	private JComboBox createNetworkMatrixComboBox() {
		adjModel = new ArrayListModel<String>();
		// we'll generate network list in addAdjMatrixToCombobox()
		AdjListener adjListener = new AdjListener();
		SelectionInList<String> selectionInList = new SelectionInList<String>(
				(ListModel) adjModel);
		selectionInList.addPropertyChangeListener(adjListener);
		return BasicComponentFactory.createComboBox(selectionInList);
	}

	private JComboBox createTFFromComboBox() {
		ArrayListModel<String> tfFromModel = new ArrayListModel<String>();
		tfFromModel.add("From Sets");
		tfFromModel.add("From File");
		TFFromListener tfFromListener = new TFFromListener();
		SelectionInList<String> selectionInList = new SelectionInList<String>(
				(ListModel) tfFromModel);
		selectionInList.addPropertyChangeListener(tfFromListener);
		return BasicComponentFactory.createComboBox(selectionInList);
	}

	private JComboBox createSigFromComboBox() {
		ArrayListModel<String> sigFromModel = new ArrayListModel<String>();
		sigFromModel.add("From Sets");
		sigFromModel.add("From File");
		sigFromModel.add("From t-test result node");
		SigFromListener sigFromListener = new SigFromListener();
		SelectionInList<String> selectionInList = new SelectionInList<String>(
				(ListModel) sigFromModel);
		selectionInList.addPropertyChangeListener(sigFromListener);
		return BasicComponentFactory.createComboBox(selectionInList);
	}

	private class AdjListener implements PropertyChangeListener {

		public void propertyChange(PropertyChangeEvent evt) {
			if (evt.getPropertyName() == "value")
				log.info("User select adj matrix: " + evt.getNewValue());
			for (Iterator<AdjacencyMatrixDataSet> iterator = adjacencymatrixDataSets
					.iterator(); iterator.hasNext();) {
				AdjacencyMatrixDataSet adjMatrixDataSet = (AdjacencyMatrixDataSet) iterator
						.next();
				if (adjMatrixDataSet.getDataSetName().equals(evt.getNewValue())) {
					adjMatrix = adjMatrixDataSet;
				}
			}
		}
	}

	// after user selected adjMatrix in the panel, you can use this method to
	// get the adjMatrix user selected.
	public AdjacencyMatrixDataSet getAdjMatrixDataSet() {
		if (!networkMatrix.isEnabled() && networkTextField.getText().length()==0) return null;
		return adjMatrix;
	}

	public double getPValue() {
		try {
			return Double.valueOf(pValueTextField.getText());
		} catch (NumberFormatException nfe) {
			return -1;
		}
	}

	public void setPValue(double d) {
		pValueTextField.setText(Double.toString(d));
	}

	public String getTranscriptionFactor() {
		return TFGeneListTextField.getText();
	}

	public void setTranscriptionFactor(String TFString) {
		TFGeneListTextField.setText(TFString);
	}

	public String getSigMarkers() {
		return sigGeneListTextField.getText();
	}

	public void setSigMarkers(String sigString) {
		sigGeneListTextField.setText(sigString);
	}

	ArrayList<AdjacencyMatrixDataSet> adjacencymatrixDataSets = new ArrayList<AdjacencyMatrixDataSet>();

	public String getSelectedAdjMatrix()
	{		 
		   return (String)networkMatrix.getSelectedItem();
	}
	
	public void setSelectedAdjMatrix(String datasetName)
	{		 
		networkMatrix.getModel().setSelectedItem(datasetName);
	}
	
	
	public void addAdjMatrixToCombobox(AdjacencyMatrixDataSet adjDataSet) {
		adjacencymatrixDataSets.add(adjDataSet);
		adjModel.add(adjDataSet.getDataSetName());		 
		
	}

	public void clearAdjMatrixCombobox() {
		adjacencymatrixDataSets.clear();
		adjModel.clear();
        networkTextField.setText(null);
	}

	public void removeAdjMatrixToCombobox(AdjacencyMatrixDataSet adjDataSet) {
		try {
			adjacencymatrixDataSets.remove(adjDataSet);
			int i = adjModel.indexOf(adjDataSet.getDataSetName());
			if(i>=0) adjModel.remove(i);
		} catch (Exception ex) {
			log.error(ex.getMessage());
		}

	}

	public void renameAdjMatrixToCombobox(AdjacencyMatrixDataSet adjDataSet,
			String oldName, String newName) {
		for (AdjacencyMatrixDataSet adjSet : adjacencymatrixDataSets) {
			if (adjSet == adjDataSet)
				adjSet.setLabel(newName);
		}
		adjModel.remove(oldName);
		adjModel.add(newName);
	}

	public void setMicroarraySet(DSMicroarraySet maSet) {
		this.maSet = maSet;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geworkbench.analysis.AbstractSaveableParameterPanel#setParameters(java.util.Map)
	 *      Set inputed parameters to GUI.
	 */
	public void setParameters(Map<Serializable, Serializable> parameters) {
		if (parameters == null)
			return; // FIXME: this is a quick patch for 0001691, should fix it
					// correctly.
		if (getStopNotifyAnalysisPanelTemporaryFlag() == true)
			return;
		stopNotifyAnalysisPanelTemporary(true);

		if (parameters.get("networkFrom") != null
				&& !parameters.get("networkFrom").toString().trim().equals(""))
			networkFrom.setSelectedIndex((Integer) parameters
					.get("networkFrom"));

     	if (parameters.get("networkMatrix") != null )
	        networkMatrix.setSelectedItem(parameters.get("networkMatrix"));
		
		String networkText = parameters.get("networkField")==null?null:parameters.get("networkField").toString();		 
		if (maSet != null && networkTextField.isEnabled()
				&& networkText != null && !networkText.trim().equals("")) {
			networkTextField.setText(networkText);
			networkFilename = new File(networkText).getName();
			if (!is5colnetwork(networkText, 10)){
				try {
					adjMatrix = new AdjacencyMatrixDataSet(
							0, networkText, networkText, maSet, networkText);
				} catch (InputFileFormatException e) {
					log.error(e.getMessage());
					e.printStackTrace();
				}
			}
		}
		if (parameters.get("tfFrom") != null
				&& !parameters.get("tfFrom").toString().trim().equals(""))
			tfFrom.setSelectedIndex((Integer) parameters.get("tfFrom"));

		if (parameters.get("sigFrom") != null
				&& !parameters.get("sigFrom").toString().trim().equals(""))
			sigFrom.setSelectedIndex((Integer) parameters.get("sigFrom"));	
		
		if (parameters.get("tfGroups") != null)
	    	   tfGroups.setSelectedItem(parameters.get("tfGroups"));
	  	
	    if (parameters.get("sigGroups") != null  )
	    	   sigGroups.setSelectedItem(parameters.get("sigGroups"));

		
		if ((!tfGroups.isEnabled()) && parameters.get("TF") != null) {
			String TF = (String) parameters.get("TF");
			setTranscriptionFactor(TF);
		}

		if ((!sigGroups.isEnabled()) && parameters.get("sigMarkers") != null) {
			String sigMarkers = (String) parameters.get("sigMarkers");
			setSigMarkers(sigMarkers);
		}

		if (parameters.get("FET p-value") != null) {
			double d = (Double) parameters.get("FET p-value");
			//if (d >= 0 && d <= 1)
			   setPValue(d);
			//else
			//   setPValue(0.01);
		}
		if ((Boolean)parameters.get("twoFET"))
			fet2Button.setSelected(true);
		else fet1Button.setSelected(true);

		if ((Boolean)parameters.get("standardBonferroni"))
			sbButton.setSelected(true);
		else ncButton.setSelected(true);

		stopNotifyAnalysisPanelTemporary(false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geworkbench.analysis.AbstractSaveableParameterPanel#getParameters()
	 */
	public Map<Serializable, Serializable> getParameters() {
		Map<Serializable, Serializable> answer = new HashMap<Serializable, Serializable>();
		answer.put("TF", getTranscriptionFactor());
		answer.put("sigMarkers", getSigMarkers());	 
		if (networkFrom.isEnabled())
			answer.put("networkFrom", networkFrom.getSelectedIndex());	
		if (networkMatrix.isEnabled() && networkMatrix.getSelectedItem() != null)
		   answer.put("networkMatrix", (String)networkMatrix.getSelectedItem());
		if (networkTextField.isEnabled())
			answer.put("networkField", networkTextField.getText());
		answer.put("tfFrom", tfFrom.getSelectedIndex());	 
		answer.put("sigFrom", sigFrom.getSelectedIndex());
		if (tfGroups.getSelectedItem() != null)
		   answer.put("tfGroups", (String)tfGroups.getSelectedItem());   
    	if (sigGroups.getSelectedItem() != null)
		answer.put("sigGroups", (String)sigGroups.getSelectedItem());
    	
		//if (getPValue() > 1 || getPValue() < 0)
		//	answer.put("Fisher's Exact P Value", 0.01);
		//else
			answer.put("FET p-value", getPValue());
		answer.put("twoFET", twoFET());
		answer.put("standardBonferroni", standardBonferroni());

		return answer;
	}

	@Override
	public void fillDefaultValues(Map<Serializable, Serializable> parameters) {
		// TODO Auto-generated method stub

	}

	/* this method is called from both EDT and non-EDT. this is not a good situation FIXME*/
	void setSelectorPanel(final DSPanel<DSGeneMarker> ap) {
		if (SwingUtilities.isEventDispatchThread()) {
			setSelectorPanelFromEDT(ap);
		} else {
			try {
				SwingUtilities.invokeAndWait(new Runnable() {

					@Override
					public void run() {
						setSelectorPanelFromEDT(ap);
					}

				});
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void setSelectorPanelFromEDT(final DSPanel<DSGeneMarker> ap) {
		selectorPanel = ap;
		String currentTargetSet = (String) tfGroups.getSelectedItem();
		DefaultComboBoxModel targetComboModel = (DefaultComboBoxModel) tfGroups
				.getModel();
		targetComboModel.removeAllElements();
		targetComboModel.addElement(" ");
		for (DSPanel<DSGeneMarker> panel : selectorPanel.panels()) {
			String label = panel.getLabel().trim();
			targetComboModel.addElement(label);
			if (StringUtils.equals(label, currentTargetSet.trim())) {
				targetComboModel.setSelectedItem(label);
			}
		}

		markersets.clear();
		for (DSPanel<DSGeneMarker> panel : selectorPanel.panels()) {
			String label = panel.getLabel().trim();
			markersets.add(label);
		}
		if (sigFrom.getSelectedItem().equals("From Sets"))
			refreshSigGroups(markersets);
	}

	private void refreshSigGroups(ArrayList<String> nodes){
		String currentSigSet = (String) sigGroups.getSelectedItem();//aspp.sigGroups?
		DefaultComboBoxModel sigComboModel = (DefaultComboBoxModel) sigGroups
				.getModel();
		sigComboModel.removeAllElements();
		sigComboModel.addElement(" ");
		sigGeneListTextField.setText("");
		for (String label : nodes){
			sigComboModel.addElement(label);
			if (StringUtils.equals(label, currentSigSet.trim())) {
				sigComboModel.setSelectedItem(label);
			}
		}
	}

	private String getLastDir(){
		String dir = null;
		try {
			File file = new File(lastDirConf);
			if (file.exists()) {
				BufferedReader br = new BufferedReader(new FileReader(file));
				dir = br.readLine();
				br.close();
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return dir;
	}
	private void saveLastDir(String dir){
		//save as last used dir
		try {
			BufferedWriter br = new BufferedWriter(new FileWriter(lastDirConf));
			br.write(dir);
			br.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Test if the network is in 5-column format, and if all correlation cols are positive.
	 * @param fname    network file name
	 * @param numrows  test format in the first numrows; if numrows <= 0, test whole file.
	 * @return if the network is in 5-column format
	 */
	private boolean is5colnetwork(String fname, int numrows){
		if (!new File(fname).exists())
			return false;
		BufferedReader br = null;
		try{
			br = new BufferedReader(new FileReader(fname));
			allpos = true;
			String line = null; int i = 0;
			while( (line = br.readLine()) != null && 
					(numrows <= 0 || i++ < numrows)) {
				String[] toks = line.split("\t");
				if (toks.length != 5 || !isDouble(toks[2]) 
						|| !isDouble(toks[3]) || !isDouble(toks[4]))
					return false;
				if (allpos && Double.valueOf(toks[correlationCol]) < 0)
					allpos = false;
			}
			log.info("This is a 5-column network");
			return true;
		}catch(IOException e){
			e.printStackTrace();
			return false;
		}finally{
			try{ 
				if (br!=null) br.close(); 
			}catch(IOException e){
				e.printStackTrace();
			}
		}
	}
	
	boolean use5colnetwork(){
		return !networkMatrix.isEnabled() && selectedFormat.equals(marina5colformat);
	}

	private boolean isDouble(String s){
		try{
			Double.parseDouble(s);
			return true;
		}catch(NumberFormatException e){
			return false;
		}
	}

	private String networkFilename = "";
	private String getNetworkFilename(){
		if (!networkTextField.isEnabled()) return "adjMatrix5col.txt";
		return networkFilename;
	}

	private String[] representedByList;
	private String selectedRepresentedBy = AdjacencyMatrixDataSet.PROBESET_ID;
	private HashMap<String, String> interactionTypeMap = null;
	private boolean isRestrict = true;
	private boolean isCancel = false;
	private String selectedFormat = AdjacencyMatrixDataSet.ADJ_FORMART;
	String marina5colformat = "marina 5-column format";

	private class LoadInteractionNetworkPanel extends JPanel {

		static final long serialVersionUID = -1855255412334333328L;

		final JDialog parent;

		private JComboBox formatJcb;
		private JComboBox presentJcb;

		public LoadInteractionNetworkPanel(JDialog parent) {

			setLayout(new BorderLayout());
			this.parent = parent;
			init();

		}

		private void init() {

			JPanel panel1 = new JPanel(new GridLayout(3, 2));
			JPanel panel3 = new JPanel(new GridLayout(0, 3));
			JLabel label1 = new JLabel("File Format:    ");

			formatJcb = new JComboBox();
			formatJcb.addItem(AdjacencyMatrixDataSet.ADJ_FORMART);
			formatJcb.addItem(AdjacencyMatrixDataSet.SIF_FORMART);
			formatJcb.addItem(marina5colformat);
			JLabel label2 = new JLabel("Node Represented By:   ");

			representedByList = new String[4];
			representedByList[0] = AdjacencyMatrixDataSet.PROBESET_ID;
			representedByList[1] = AdjacencyMatrixDataSet.GENE_NAME;
			representedByList[2] = AdjacencyMatrixDataSet.ENTREZ_ID;
			representedByList[3] = AdjacencyMatrixDataSet.OTHER;
			presentJcb = new JComboBox(representedByList);

			JButton continueButton = new JButton("Continue");
			JButton cancelButton = new JButton("Cancel");
			formatJcb.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent evt) {
					if (formatJcb.getSelectedItem().toString().equals(
							AdjacencyMatrixDataSet.ADJ_FORMART)) {
						representedByList = new String[4];
						representedByList[0] = AdjacencyMatrixDataSet.PROBESET_ID;
						representedByList[1] = AdjacencyMatrixDataSet.GENE_NAME;
						representedByList[2] = AdjacencyMatrixDataSet.ENTREZ_ID;
						representedByList[3] = AdjacencyMatrixDataSet.OTHER;
						presentJcb.setModel(new DefaultComboBoxModel(
								representedByList));
					} else if (formatJcb.getSelectedItem().toString().equals(
							marina5colformat)) {
						representedByList = new String[1];
						representedByList[0] = AdjacencyMatrixDataSet.PROBESET_ID;
						presentJcb.setModel(new DefaultComboBoxModel(
								representedByList));
					} else {
						representedByList = new String[3];
						representedByList[0] = AdjacencyMatrixDataSet.GENE_NAME;
						representedByList[1] = AdjacencyMatrixDataSet.ENTREZ_ID;
						representedByList[2] = AdjacencyMatrixDataSet.OTHER;
						presentJcb.setModel(new DefaultComboBoxModel(
								representedByList));
					}
				}
			});

			if (networkFilename.toLowerCase().endsWith(".sif"))
				formatJcb.setSelectedItem(AdjacencyMatrixDataSet.SIF_FORMART);
			else if (networkFilename.toLowerCase().contains("5col"))
				formatJcb.setSelectedItem(marina5colformat);
			else
				formatJcb.setSelectedItem(AdjacencyMatrixDataSet.ADJ_FORMART);
			continueButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					continueButtonActionPerformed();
					parent.dispose();
					isCancel = false;
				}
			});
			cancelButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					parent.dispose();
					isCancel = true;
				}
			});

			panel1.add(label1);
			panel1.add(formatJcb);

			panel1.add(label2);
			panel1.add(presentJcb);

			panel3.add(cancelButton);
			panel3.add(new JLabel("  "));
			panel3.add(continueButton);
			
			this.add(panel1, BorderLayout.CENTER);
			this.add(panel3, BorderLayout.SOUTH);
			parent.getRootPane().setDefaultButton(continueButton);
		}

		private void continueButtonActionPerformed() {
			selectedFormat = formatJcb.getSelectedItem().toString();
			selectedRepresentedBy = presentJcb.getSelectedItem().toString();
		}

	}
	private boolean openDialog(){
		JDialog loadDialog = new JDialog();

		loadDialog.addWindowListener(new WindowAdapter() {

			public void windowClosing(WindowEvent e) {
				isCancel = true;
			}
		});

		isCancel = false;
		loadDialog.setTitle("Load Interaction Network");
		LoadInteractionNetworkPanel loadPanel = new LoadInteractionNetworkPanel(
				loadDialog);

		loadDialog.add(loadPanel);
		loadDialog.setModal(true);
		loadDialog.pack();
		Util.centerWindow(loadDialog);
		loadDialog.setVisible(true);

		if (isCancel)
			return false;

		if ((selectedFormat
				.equalsIgnoreCase(AdjacencyMatrixDataSet.SIF_FORMART) && !networkFilename
				.toLowerCase().endsWith(".sif"))
				|| (networkFilename.toLowerCase().endsWith(".sif") && !selectedFormat
						.equalsIgnoreCase(AdjacencyMatrixDataSet.SIF_FORMART))
				||(selectedFormat.equals(marina5colformat) && !is5colnetwork(networkTextField.getText(), 10))){
			JOptionPane.showMessageDialog(null,  "The network format selected does not match that of the file.",
					"Format Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}

		if (selectedFormat
				.equalsIgnoreCase(AdjacencyMatrixDataSet.SIF_FORMART)) {
			interactionTypeMap = new org.geworkbench.parsers.AdjacencyMatrixFileFormat().getInteractionTypeMap();
		}
		return true;
	}
	
	
	@Override
	public String getDataSetHistory() {			 
		StringBuffer histStr = new StringBuffer("Generated with MRA run with parameters:\n\n");		 
		
		AdjacencyMatrixDataSet set = getAdjMatrixDataSet();
		String setname = (set != null) ? set.getDataSetName() : this
				.getNetworkFilename();
		histStr.append("[PARA] Load Network: " + setname).append("\n");
		histStr.append("[PARA] FET p-value : " + getPValue()).append("\n");
		histStr.append("[PARA] Master Regulators: " + getTranscriptionFactor()).append("\n");
		histStr.append("[PARA] Signature Markers: " + getSigMarkers()).append("\n");
		histStr.append("[PARA] FET Runs: " + (twoFET()?fet2Button.getText():fet1Button.getText())).append("\n");
		histStr.append("[PARA] Multiple Testing Correction: " +
				(standardBonferroni()?sbButton.getText():ncButton.getText())).append("\n");
		
		return histStr.toString();
	}

}
