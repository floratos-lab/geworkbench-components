package org.geworkbench.components.ttest;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ListModel;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.analysis.AbstractSaveableParameterPanel;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.events.listeners.ParameterActionListener;
import org.geworkbench.util.pathwaydecoder.mutualinformation.AdjacencyMatrixDataSet;

import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.list.ArrayListModel;
import com.jgoodies.binding.list.SelectionInList;
import com.jgoodies.binding.value.ValueHolder;
import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
/**
 *	Parameter Panel used for Master Regulator Analysis
 *	@author yc2480 
 *  @version $Id$
 */
public class MasterRegulatorPanel extends AbstractSaveableParameterPanel {
	private static final long serialVersionUID = -6160058089960168299L;
	
	private static final float PValueThresholdDefault = 0.05f;
	private static final String TFGeneListDefault = ("AFFX-HUMGAPDH/M33197_3_at, AFFX-HUMGAPDH/M33197_5_at, AFFX-HUMGAPDH/M33197_M_at, AFFX-HUMRGE/M10098_3_at, AFFX-HUMRGE/M10098_M_at");
	static final String[] DEFAULT_SET = { " " };

	private Log log = LogFactory.getLog(this.getClass());
	private ArrayListModel<String> networkFromModel; //used for 0,0 drop down box
	private ArrayListModel<String> adjModel; //used for 0,1 drop down box
	private ArrayListModel<String> tfFromModel; //used for 1,0 drop down box
	private ValueModel correctionHolder; //No correction, Standard Bonferroni, Adj Bonferroni
	private JTextField pValueTextField = null;
	private JTextField TFGeneListTextField = null; //Marker 1, Marker 2...
	private JTextField networkTextField = null;
	private HashMap<String,AdjacencyMatrixDataSet> adjMatrix=new HashMap<String,AdjacencyMatrixDataSet>();
	private DSMicroarraySet<DSMicroarray> maSet=null;
	private MRATtestPanel tTestPanel= new MRATtestPanel();
	private JComboBox networkMatrix = createNetworkMatrixComboBox();
	private JComboBox tfGroups = new JComboBox(new DefaultComboBoxModel(DEFAULT_SET));
	private JButton loadNetworkButton=new JButton("Load");
	private JButton loadTFButton=new JButton("Load");
	private JComboBox networkFrom = null;
	private JComboBox tfFrom = null;	
	
	public MasterRegulatorPanel(){
		networkTextField = new JTextField();
		networkTextField.setEditable(false);
		FormLayout layout = new FormLayout(
                "left:max(100dlu;pref), 10dlu, 100dlu, 10dlu, "
              + "100dlu, 10dlu, 100dlu, 10dlu, 100dlu",
                "");
		DefaultFormBuilder builder = new DefaultFormBuilder(layout);
		builder.setDefaultDialogBorder();
		builder.appendSeparator("Network");
		builder.append("Load Network");
		networkFrom = createNetworkFromComboBox();
		networkFrom.setSelectedIndex(1);	//preselect "From File"
		//JComboBox networkMatrix = createNetworkMatrixComboBox();
		builder.append(networkFrom);
		networkMatrix.setEnabled(false);
		builder.append(networkMatrix);
		
		builder.append(networkTextField);
		
		//JButton loadNetworkButton=new JButton("Load");
		loadNetworkButton.addActionListener(new LoadNetworkButtonListener(adjMatrix));		
		builder.append(loadNetworkButton);
		builder.nextLine();
		
		builder.append("Master Regulators");
		tfFrom = createTFFromComboBox();
		tfFrom.setSelectedIndex(1);			//preselect "From File"
		//JComboBox tfGroups = createGroupsComboBox();
		builder.append(tfFrom);
		tfGroups.setEnabled(false);
		builder.append(tfGroups);
		
		if (TFGeneListTextField == null)
			TFGeneListTextField = new JTextField();
		TFGeneListTextField.setText(TFGeneListDefault);
		builder.append(TFGeneListTextField);
		//JButton loadTFButton=new JButton("Load");
		loadTFButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				if (e.getActionCommand().equals("Load")) {
	                StringBuilder geneListBuilder = new StringBuilder();
	                try {
	                	String hubMarkersFile="data/test.txt";
	                    File hubFile = new File(hubMarkersFile);
	                    JFileChooser chooser = new JFileChooser(hubFile.getParent());
	                    chooser.showOpenDialog(MasterRegulatorPanel.this);
	                    if (chooser.getSelectedFile()!=null){
		                    hubMarkersFile = chooser.getSelectedFile().getPath();
		                    BufferedReader reader = new BufferedReader(new FileReader(hubMarkersFile));
		                    String hub = reader.readLine();
		                    while (hub != null && !"".equals(hub)) {
		                        geneListBuilder.append(hub + ", ");
		                        hub = reader.readLine();
		                    }
		                    String geneString = geneListBuilder.toString();
		                    geneString = geneString.substring(0, geneString.length() - 2);
		                    TFGeneListTextField.setText(geneString);
	                    }else{
	                    	//user canceled
	                    }
	                } catch (IOException ioe) {
	                    log.error(ioe);
	                }

			    }
			}
		});
		builder.append(loadTFButton);
		builder.nextLine();
		
		builder.appendSeparator("Significance Threshold");
		builder.append("T-test p-value (alpha)");
		if (pValueTextField == null)
			pValueTextField = new JTextField();
		pValueTextField.setText(Float.toString(PValueThresholdDefault));
		builder.append(pValueTextField);
		builder.nextLine();

//		builder.append("Multiple testing correction");
		ArrayList<String> correctionComboBoxStrings = new ArrayList<String>();
		correctionComboBoxStrings.add("No correction");
		correctionComboBoxStrings.add("Standard Bonferroni");
		correctionComboBoxStrings.add("Adjusted Bonferroni");
		correctionHolder = new ValueHolder("No correction");

        builder.nextLine();
        JTabbedPane jTabbedPane1 = new JTabbedPane();
        jTabbedPane1.add(builder.getPanel(),"Main");
        jTabbedPane1.add(tTestPanel,"T-test");
        //t-test panel
        this.add(jTabbedPane1,BorderLayout.CENTER);
        
        tfGroups.addActionListener(new ActionListener() {
    		public void actionPerformed(ActionEvent actionEvent) {
    			String selectedLabel = (String) tfGroups.getSelectedItem();
    			if (!StringUtils.isEmpty(selectedLabel))
    				if (!chooseMarkersFromSet(selectedLabel, TFGeneListTextField)) {
    					tfGroups.setSelectedIndex(0);
    					TFGeneListTextField.setText(TFGeneListDefault);
    				}
    		}
    	});
        
        ParameterActionListener parameterActionListener = new ParameterActionListener(this);
        pValueTextField.addActionListener(parameterActionListener);
        TFGeneListTextField.addActionListener(parameterActionListener);
        tTestPanel.setParamActionListener(parameterActionListener);
        networkFrom.addActionListener(parameterActionListener);
        tfFrom.addActionListener(parameterActionListener);
	}
	public class LoadNetworkButtonListener implements java.awt.event.ActionListener {
		private HashMap<String, AdjacencyMatrixDataSet> adjMatrixHolder;
		public LoadNetworkButtonListener(HashMap<String, AdjacencyMatrixDataSet> adjMatrixHolder){
			this.adjMatrixHolder = adjMatrixHolder; 
		}
		public void actionPerformed(java.awt.event.ActionEvent e) {
			if (e.getActionCommand().equals("Load")) {
	            if (maSet!=null){
	            	String adjMatrixFileStr="C:\\Documents and Settings\\yc2480\\eclipse_geworkbench_workspace\\geworkbench-core\\data\\testaracne4.adjmat";
	            	File adjMatrixFile = new File(adjMatrixFileStr);
	                JFileChooser chooser = new JFileChooser(adjMatrixFile.getParent());
	                chooser.showOpenDialog(MasterRegulatorPanel.this);
	                if (chooser.getSelectedFile()!=null){
		                adjMatrixFileStr = chooser.getSelectedFile().getPath();
		                AdjacencyMatrixDataSet adjMatrix=new AdjacencyMatrixDataSet(null, 0, 0, 0, adjMatrixFileStr, adjMatrixFileStr, maSet); 
		                adjMatrix.readFromFile(adjMatrixFileStr, maSet);
		                this.adjMatrixHolder.remove("adjMatrix");
		                this.adjMatrixHolder.put("adjMatrix", adjMatrix);
		                networkTextField.setText(adjMatrixFileStr);
	                }else{
	                	//user canceled
	                }
	            }
		    }
		}
	}

	private JComboBox createNetworkFromComboBox(){
		networkFromModel = new ArrayListModel<String>();
		networkFromModel.add("From Project");
		networkFromModel.add("From File");
		NetworkFromListener networkFromListener= new NetworkFromListener();
		SelectionInList<String> selectionInList=new SelectionInList<String>((ListModel)networkFromModel);
		selectionInList.addPropertyChangeListener(networkFromListener);
		return BasicComponentFactory.createComboBox(selectionInList);
	}
	private class NetworkFromListener implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent evt) {
        	if (evt.getPropertyName()=="value")
        		if (evt.getNewValue()=="From Project"){
        			networkMatrix.setEnabled(true);
        			loadNetworkButton.setEnabled(false);
        			networkTextField.setEnabled(false);
        			//clear combo box 
        			//load adj matrix into the list
/*        			
        			for (Iterator iterator = adjacencymatrixDataSets.iterator(); iterator
							.hasNext();) {
        				AdjacencyMatrixDataSet element = (AdjacencyMatrixDataSet) iterator.next();
						System.out.println("add"+element.getDataSetName()+"to combo box.");
					}
*/					
        		}else if (evt.getNewValue()=="From File"){
        			networkMatrix.setEnabled(false);
        			loadNetworkButton.setEnabled(true);
        			networkTextField.setEnabled(true);
        			//active load button
        			//show file name loaded
        		}
        }
    }
	private class TFFromListener implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent evt) {
        	if (evt.getPropertyName()=="value")
        		if (evt.getNewValue()=="From Sets"){
        			tfGroups.setEnabled(true);
        			loadTFButton.setEnabled(false);
        			getGroups();
        			//hide fileNameField
        			//clear combo box 
        			//load adj matrix into the list
        		}else if (evt.getNewValue()=="From File"){
        			tfGroups.setEnabled(false);
        			loadTFButton.setEnabled(true);
        			//active load button
        			//show file name loaded
        		}
        }
    }

	private JComboBox createNetworkMatrixComboBox(){
		adjModel = new ArrayListModel<String>();
		//we'll generate network list in addAdjMatrixToCombobox() 
		AdjListener adjListener= new AdjListener(this.adjMatrix);
		SelectionInList<String> selectionInList=new SelectionInList<String>((ListModel)adjModel);
		selectionInList.addPropertyChangeListener(adjListener);
		return BasicComponentFactory.createComboBox(selectionInList);
	}
	private JComboBox createTFFromComboBox(){
		tfFromModel = new ArrayListModel<String>();
		tfFromModel.add("From Sets");
		tfFromModel.add("From File");
		TFFromListener tfFromListener= new TFFromListener();
		SelectionInList<String> selectionInList=new SelectionInList<String>((ListModel)tfFromModel);
		selectionInList.addPropertyChangeListener(tfFromListener);
		return BasicComponentFactory.createComboBox(selectionInList);
	}

	private class AdjListener implements PropertyChangeListener {
		HashMap<String, AdjacencyMatrixDataSet> adjMatrix;
		public AdjListener(HashMap<String, AdjacencyMatrixDataSet> adjMatrix){
			this.adjMatrix = adjMatrix;
		};
        public void propertyChange(PropertyChangeEvent evt) {
        	if (evt.getPropertyName()=="value")
        		log.info("User select adj matrix: "+evt.getNewValue());
        	for (Iterator<AdjacencyMatrixDataSet> iterator = adjacencymatrixDataSets.iterator(); iterator.hasNext();) {
        		AdjacencyMatrixDataSet adjMatrixDataSet = (AdjacencyMatrixDataSet) iterator.next();
        		if (adjMatrixDataSet.getDataSetName().equals(evt.getNewValue())){
        			this.adjMatrix.remove("adjMatrix");
        			this.adjMatrix.put("adjMatrix", adjMatrixDataSet);
        		}
			}
        }
    }	
	//after user selected adjMatrix in the panel, you can use this method to get the adjMatrix user selected.
	public AdjacencyMatrixDataSet getAdjMatrixDataSet(){
		return this.adjMatrix.get("adjMatrix");
	}
	public String getCorrection(){
		return correctionHolder.getValue().toString();
	}
	public double getPValue(){
		return Double.valueOf(pValueTextField.getText());
	}
	public void setPValue(double d){
		pValueTextField.setText(Double.toString(d));
	}
	public String getTranscriptionFactor(){
		return TFGeneListTextField.getText();
	}
	public void setTranscriptionFactor(String TFString){
		TFGeneListTextField.setText(TFString);
	}
	public TtestAnalysisPanel getTTestPanel(){
		return tTestPanel;
	}
	
	ArrayList<AdjacencyMatrixDataSet> adjacencymatrixDataSets=new ArrayList<AdjacencyMatrixDataSet>();

	public void addAdjMatrixToCombobox(AdjacencyMatrixDataSet adjDataSet){
		adjacencymatrixDataSets.add(adjDataSet);
		adjModel.add(adjDataSet.getDataSetName());
	}

	public void removeAdjMatrixToCombobox(AdjacencyMatrixDataSet adjDataSet){		
		try
		{
		   adjacencymatrixDataSets.remove(adjDataSet);
		   //adjModel.remove(adjDataSet.getDataSetName());
		   adjModel.remove(adjModel.indexOf(adjDataSet.getDataSetName()));
		}
		catch(Exception ex)
		{
			log.error(ex.getMessage());
		}
		
	}

	public void renameAdjMatrixToCombobox(AdjacencyMatrixDataSet adjDataSet, String oldName, String newName){
		for(AdjacencyMatrixDataSet adjSet: adjacencymatrixDataSets){
			if (adjSet == adjDataSet)
				adjSet.setLabel(newName);
		}
		adjModel.remove(oldName);
		adjModel.add(newName);
	}

	public void setMicroarraySet(DSMicroarraySet<DSMicroarray> maSet){
		this.maSet = maSet;
	}
	public void getGroups(){
	}
	public void addGroupsToComboBox(){
		
	}

	/*
	 * (non-Javadoc)
	 * @see org.geworkbench.analysis.AbstractSaveableParameterPanel#setParameters(java.util.Map)
	 * Set inputed parameters to GUI.
	 */
    public void setParameters(Map<Serializable, Serializable> parameters){
    	if (parameters==null) return;	//FIXME: this is a quick patch for 0001691, should fix it correctly.
    	if (getStopNotifyAnalysisPanelTemporaryFlag()==true) return;
    	stopNotifyAnalysisPanelTemporary(true);
    	tTestPanel.setParameters(parameters);
    	double d = (Double)parameters.get("alpha");
    	setPValue(d);
    	String TF = (String)parameters.get("TF");
    	setTranscriptionFactor(TF);
    	networkFrom.setSelectedIndex((Integer)parameters.get("networkFrom"));
    	networkTextField.setText((String)parameters.get("networkField"));
    	if (maSet!=null){
    		AdjacencyMatrixDataSet adjMatrix2=new AdjacencyMatrixDataSet(null, 0, 0, 0, networkTextField.getText(), networkTextField.getText(), maSet); 
    		adjMatrix2.readFromFile(networkTextField.getText(), maSet);
    		this.adjMatrix.remove("adjMatrix");
			this.adjMatrix.put("adjMatrix", adjMatrix2);
    	}
    	tfFrom.setSelectedIndex((Integer)parameters.get("tfFrom"));
    	stopNotifyAnalysisPanelTemporary(false);
    }
    
    /*
	 * (non-Javadoc)
	 * 
	 * @see org.geworkbench.analysis.AbstractSaveableParameterPanel#getParameters()
	 */
    public Map<Serializable, Serializable> getParameters() {
    	Map<Serializable, Serializable> answer = tTestPanel.getParameters();
    	answer.put("alpha",getPValue());
    	answer.put("TF",getTranscriptionFactor());
    	answer.put("networkFrom", networkFrom.getSelectedIndex());
    	answer.put("networkField", networkTextField.getText());
    	answer.put("tfFrom", tfFrom.getSelectedIndex());
    	return answer;
    }
	@Override
	public void fillDefaultValues(Map<Serializable, Serializable> parameters) {
		// TODO Auto-generated method stub
		
	}
	void setSelectorPanel(MasterRegulatorPanel aspp, DSPanel<DSGeneMarker> ap) {
		aspp.selectorPanel = ap;		
		String currentTargetSet = (String) aspp.tfGroups.getSelectedItem();
		DefaultComboBoxModel targetComboModel = (DefaultComboBoxModel) aspp.tfGroups.getModel();
		targetComboModel.removeAllElements();
		targetComboModel.addElement(" ");
		TFGeneListTextField.setText(TFGeneListDefault);
		for (DSPanel<DSGeneMarker> panel : selectorPanel.panels()) {
			String label = panel.getLabel().trim();
			targetComboModel.addElement(label);
			if (StringUtils.equals(label, currentTargetSet.trim())){
				targetComboModel.setSelectedItem(label);				
			}
		}
	}	
	
	
	
	
}
