package org.geworkbench.components.analysis.clustering;

import java.awt.BorderLayout;
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

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ListModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.analysis.AbstractSaveableParameterPanel;
import org.geworkbench.bison.annotation.CSAnnotationContextManager;
import org.geworkbench.bison.annotation.DSAnnotationContext;
import org.geworkbench.bison.annotation.DSAnnotationContextManager;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.util.pathwaydecoder.mutualinformation.AdjacencyMatrixDataSet;

import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.adapter.ComboBoxAdapter;
import com.jgoodies.binding.list.ArrayListModel;
import com.jgoodies.binding.list.SelectionInList;
import com.jgoodies.binding.value.ValueHolder;
import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import org.geworkbench.components.analysis.clustering.TtestAnalysisPanel;
/**
 *	Parameter Panel used for Master Regulator Analysis
 *	@author yc2480 $id$
 */
public class MasterRegulatorPanel extends AbstractSaveableParameterPanel
		implements Serializable {

	private static final long serialVersionUID = 1L;
	private Log log = LogFactory.getLog(this.getClass());
	private ArrayListModel<String> networkFromModel; //used for 0,0 drop down box
	private ArrayListModel<String> adjModel; //used for 0,1 drop down box
	private ArrayListModel<String> tfFromModel; //used for 1,0 drop down box
	private ArrayListModel<String> groupModel; //used for 0,1 drop down box
	private ValueModel correctionHolder; //No correction, Standard Bonferroni, Adj Bonferroni
	private ValueModel pValueHolder; // 0.05
	private ValueModel TFGeneList; //Marker 1, Marker 2...
	private HashMap<String,AdjacencyMatrixDataSet> adjMatrix=new HashMap<String,AdjacencyMatrixDataSet>();
	private DSMicroarraySet<DSMicroarray> maSet=null;
	private MRATtestPanel tTestPanel= new MRATtestPanel();
	private JComboBox networkMatrix = createNetworkMatrixComboBox();
	private JComboBox tfGroups = createGroupsComboBox();
	private JButton loadNetworkButton=new JButton("Load");
	private JButton loadTFButton=new JButton("Load");
	public MasterRegulatorPanel(){
		FormLayout layout = new FormLayout(
                "left:max(100dlu;pref), 10dlu, 100dlu, 10dlu, "
              + "100dlu, 10dlu, 100dlu, 10dlu, 100dlu",
                "");
		DefaultFormBuilder builder = new DefaultFormBuilder(layout);
		builder.setDefaultDialogBorder();
		builder.appendSeparator("Network");
		builder.append("Load Network");
		JComboBox networkFrom = createNetworkFromComboBox();
		networkFrom.setSelectedIndex(1);	//preselect "From File"
		//JComboBox networkMatrix = createNetworkMatrixComboBox();
		builder.append(networkFrom);
		networkMatrix.setEnabled(false);
		builder.append(networkMatrix);
		//JButton loadNetworkButton=new JButton("Load");
		loadNetworkButton.addActionListener(new LoadNetworkButtonListener(adjMatrix));		
		builder.append(loadNetworkButton);
		builder.nextLine();
		
		builder.append("Transcription Factors");
		JComboBox tfFrom = createTFFromComboBox();
		tfFrom.setSelectedIndex(1);			//preselect "From File"
		//JComboBox tfGroups = createGroupsComboBox();
		builder.append(tfFrom);
		tfGroups.setEnabled(false);
		builder.append(tfGroups);
		TFGeneList = new ValueHolder("AFFX-HUMGAPDH/M33197_3_at, AFFX-HUMGAPDH/M33197_5_at, AFFX-HUMGAPDH/M33197_M_at, AFFX-HUMRGE/M10098_3_at, AFFX-HUMRGE/M10098_M_at");
		JTextField tfGenes= BasicComponentFactory.createTextField(TFGeneList);
		builder.append(tfGenes);
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
		                    TFGeneList.setValue(geneString);
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
		this.pValueHolder = new ValueHolder("0.05");
        JTextField pValueTextField = BasicComponentFactory.createTextField(pValueHolder);
		builder.append(pValueTextField);
		builder.nextLine();

//		builder.append("Multiple testing correction");
		ArrayList<String> correctionComboBoxStrings = new ArrayList<String>();
		correctionComboBoxStrings.add("No correction");
		correctionComboBoxStrings.add("Standard Bonferroni");
		correctionComboBoxStrings.add("Adjusted Bonferroni");
		correctionHolder = new ValueHolder("No correction");
        ComboBoxAdapter comboBoxAdapter = new ComboBoxAdapter(correctionComboBoxStrings, correctionHolder);
        JComboBox correctionComboBox = new JComboBox();
        correctionComboBox.setModel(comboBoxAdapter);
//        builder.append(correctionComboBox);

        builder.nextLine();
        JTabbedPane jTabbedPane1 = new JTabbedPane();
        jTabbedPane1.add(builder.getPanel(),"Main");
        jTabbedPane1.add(tTestPanel,"T-test");
        //t-test panel
//        builder.appendSeparator("P-value parameters");
//        builder.append(tTestPanel,9);
//		this.add(builder.getPanel());
        this.add(jTabbedPane1,BorderLayout.CENTER);
	}
	public class LoadNetworkButtonListener implements java.awt.event.ActionListener{
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
		SelectionInList<String> selectionInList=new SelectionInList((ListModel)networkFromModel);
		selectionInList.addPropertyChangeListener(networkFromListener);
		return BasicComponentFactory.createComboBox(selectionInList);
	}
	private class NetworkFromListener implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent evt) {
        	if (evt.getPropertyName()=="value")
        		if (evt.getNewValue()=="From Project"){
        			networkMatrix.setEnabled(true);
        			loadNetworkButton.setEnabled(false);
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
        			//active load button
        			//show file name loaded
        		}
        }
    }
	private class TFFromListener implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent evt) {
        	if (evt.getPropertyName()=="value")
        		if (evt.getNewValue()=="From Groups"){
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
		SelectionInList<String> selectionInList=new SelectionInList((ListModel)adjModel);
		selectionInList.addPropertyChangeListener(adjListener);
		return BasicComponentFactory.createComboBox(selectionInList);
	}
	private JComboBox createTFFromComboBox(){
		tfFromModel = new ArrayListModel<String>();
		tfFromModel.add("From Groups");
		tfFromModel.add("From File");
		TFFromListener tfFromListener= new TFFromListener();
		SelectionInList<String> selectionInList=new SelectionInList((ListModel)tfFromModel);
		selectionInList.addPropertyChangeListener(tfFromListener);
		return BasicComponentFactory.createComboBox(selectionInList);
	}
	private JComboBox createGroupsComboBox(){
		groupModel = new ArrayListModel<String>();
		//we'll generate group list in getGroups() 
		GroupListener groupListener= new GroupListener();
		SelectionInList<String> selectionInList=new SelectionInList((ListModel)groupModel);
		selectionInList.addPropertyChangeListener(groupListener);
		return BasicComponentFactory.createComboBox(selectionInList);
	}
	private class GroupListener implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent evt) {
        	if ((evt.getPropertyName()=="value")&&(evt.getNewValue()!=null)&&(evt.getNewValue().toString()!="")){
        		log.info("User select "+evt.getNewValue()+" group in probe set.");
        		DSAnnotationContextManager manager = CSAnnotationContextManager
        		.getInstance();
        		DSAnnotationContext<DSGeneMarker> markerGroups = manager
        				.getCurrentContext(maSet.getMarkers());
    			StringBuilder geneListBuilder = new StringBuilder();
        		for (DSGeneMarker marker : markerGroups.getItemsWithLabel(evt.getNewValue().toString())){
        			log.debug("add marker "+marker.getLabel()+" to text field.");
        			geneListBuilder.append(marker.getLabel() + ", ");
        		}
                String geneString = geneListBuilder.toString();
                if (geneString.length()>2)
                	geneString = geneString.substring(0, geneString.length() - 2);
                TFGeneList.setValue(geneString);
        	}
        }
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
		String pValueTxt=pValueHolder.getValue().toString();
		Double pValue=Double.valueOf(pValueTxt);
		return pValue;
	}
	public String getTranscriptionFactor(){
		return TFGeneList.getValue().toString();
	}
	public TtestAnalysisPanel getTTestPanel(){
		return this.tTestPanel;
	}
	
	ArrayList<AdjacencyMatrixDataSet> adjacencymatrixDataSets=new ArrayList<AdjacencyMatrixDataSet>();

	public void addAdjMatrixToCombobox(AdjacencyMatrixDataSet adjDataSet){
		adjacencymatrixDataSets.add(adjDataSet);
		adjModel.add(adjDataSet.getDataSetName());
	}

	public void removeAdjMatrixToCombobox(AdjacencyMatrixDataSet adjDataSet){
		adjacencymatrixDataSets.remove(adjDataSet);
		//adjModel.remove(adjDataSet.getDataSetName());
		adjModel.remove(adjModel.indexOf(adjDataSet.getDataSetName()));
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
		DSAnnotationContextManager manager = CSAnnotationContextManager
		.getInstance();
		DSAnnotationContext<DSMicroarray> microArrayGroups = manager
				.getCurrentContext(maSet);
		DSAnnotationContext<DSGeneMarker> markerGroups = manager
				.getCurrentContext(maSet.getMarkers());
		groupModel.clear();
		for (int cx=0;cx<markerGroups.getNumberOfLabels();cx++){
			log.debug("get group name "+markerGroups.getLabel(cx)+" from probe set and add to dropdown box.");
			groupModel.add(markerGroups.getLabel(cx));
		}
	}
	public void addGroupsToComboBox(){
		
	}
}
