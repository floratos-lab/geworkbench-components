package org.geworkbench.components.idea;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.analysis.AbstractSaveableParameterPanel;
import org.geworkbench.bison.datastructure.biocollections.AdjacencyMatrixDataSet;
import org.geworkbench.bison.datastructure.biocollections.AdjacencyMatrix.Edge;
import org.geworkbench.bison.datastructure.biocollections.AdjacencyMatrix.NodeType;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.events.listeners.ParameterActionListener;
import org.geworkbench.util.FilePathnameUtils;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

/**
 * IDEAPanel of IDEA analysis component
 * @author zm2165
 * @version $Id$
 */
public class IDEAPanel extends AbstractSaveableParameterPanel {
	
	private static final long serialVersionUID = 5983582161253754386L;
	static Log log = LogFactory.getLog(IDEAPanel.class);	
	
	private static final String P_VALUE="0.05";
	private Phenotype phenotype=new Phenotype();
	private ArrayList<IdeaNetworkEdge> ideaNetwork;
	private ArrayList<String> nullDataList;
	private ArrayList<String> networkList=new ArrayList<String>();
	private ArrayList<String> adjModel=new ArrayList<String>();
	private static final String FROM_FILE = "From File";
	private static final String FROM_FILE_LAB = "From File (Lab Format)";
	private static final String FROM_SETS = "From Set";
	private static final String FROM_PROJECT = "From Project";
	private static final String[] NETWORK_FROM = { FROM_FILE_LAB, FROM_PROJECT};
	private static final String[] PHENOTYPE_FROM = { FROM_SETS, FROM_FILE};	
	static final String[] DEFAULT_SET = { " " };	
	
	private JPanel selectionPanel = null;	
	private JTextField networkField = new JTextField(20);	
	private JTextField nullDataField = new JTextField(20);
	private JButton networkLoadButton = new JButton("Load");
	
	private JButton includeLoadButton = new JButton("Load");
	private JButton excludeLoadButton = new JButton("Load");
	private JButton nullDataLoadButton = new JButton("Load");
	private JCheckBox nullDataCheckbox = new JCheckBox("Use the existing null data", false);
	private JLabel loadNullDataLabel=new JLabel("      Load null data      ");
	
	private JComboBox networkMatrix = new JComboBox();	
	private JComboBox networkFrom = new JComboBox(NETWORK_FROM);	
	private JComboBox phenotypeFrom = new JComboBox(PHENOTYPE_FROM);	
	private JComboBox includeSets = new JComboBox(new DefaultComboBoxModel(
			DEFAULT_SET));
	private JComboBox excludeSets = new JComboBox(new DefaultComboBoxModel(
			DEFAULT_SET));
	private JLabel includeLabel=new JLabel("                   Include");
	private JLabel excludeLabel=new JLabel("                  Exclude");
	private JTextField includeField = new JTextField();
	private JTextField excludeField = new JTextField();
	
	private DSPanel<DSMicroarray> selectorPanelOfArrays;
	private DSMicroarraySet maSet=null;
	private boolean firstRunFlag;	
	
	public IDEAPanel() {
		try {
			init();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Set<Integer> preparePhenoSet(String in){
		Set<Integer> oneSet=new HashSet<Integer>();
		String[] tokens=in.split(",");
		for(String s:tokens){
			for(int i=0;i<maSet.size();i++){
				if(s.equalsIgnoreCase(maSet.get(i).getLabel())){
					oneSet.add(i+1);	//the first column is 1 not 0 in documentation of phenotype;
				}
			}
		}
		
		return oneSet;
	}
	
	/* this is called from IDEAAnalysis */
	List<IdeaNetworkEdge> getNetwork() {
		return ideaNetwork;
	}	
	
	public String getNullFileName() {
		return nullDataField.getText();
	}	
	
	public Phenotype getPhenotype() {
			return phenotype;
	}
	
	private void init() throws Exception {		
		firstRunFlag=true;
		phenotypeFrom.setSelectedIndex(0);
		includeSets.setEnabled(true);
		excludeSets.setEnabled(true);
		includeField.setText("");
		excludeField.setText("");
		networkField.setText("");		
		
		this.setLayout(new BorderLayout());		
		selectionPanel = new JPanel();
		selectionPanel.setLayout(new BorderLayout());
		this.add(selectionPanel, BorderLayout.CENTER);		
		
		{
			networkField.setEnabled(true);
			networkField.setEditable(false);
			networkLoadButton.setEnabled(true);		
			includeLoadButton.setEnabled(false);
			excludeLoadButton.setEnabled(false);
			nullDataField.setEnabled(false);
			nullDataField.setEditable(false);
			nullDataLoadButton.setEnabled(false);		
			
			FormLayout layout = new FormLayout(
					"left:max(100dlu;pref), 10dlu, 100dlu, 10dlu, "
							+ "100dlu, 10dlu, 100dlu, 10dlu, 100dlu", "");
			DefaultFormBuilder builder = new DefaultFormBuilder(layout);
			builder.setDefaultDialogBorder();

			builder.appendSeparator("Inputs required");			
			builder.append("Load Network");
			builder.append(networkFrom);			
			builder.append(networkField);
			networkMatrix.setEnabled(false);
			builder.append(networkMatrix);
			builder.append(networkLoadButton);						
			builder.nextLine();			
			
			builder.append("Define Phenotype");
			builder.append(phenotypeFrom);			
			builder.nextLine();			
			
			builder.append(includeLabel);
			builder.append(includeSets);
			builder.append(includeField);
			builder.append(includeLoadButton);
			builder.nextLine();			
			
			builder.append(excludeLabel);
			builder.append(excludeSets);
			builder.append(excludeField);
			builder.append(excludeLoadButton);
			builder.nextLine();		
			
			nullDataCheckbox.setToolTipText("Only when gene expression, annotation, network, phenotype input data set are the same.");
			builder.append(nullDataCheckbox);
			nullDataCheckbox.addActionListener(new NullData_actionAdapter());
			builder.nextLine();			
			
			nullDataField.setEditable(false);			
			builder.append(loadNullDataLabel,
					nullDataField, nullDataLoadButton);
			builder.nextLine();			
			
			selectionPanel.add(builder.getPanel(), BorderLayout.CENTER);
		}		
		
		phenotypeFrom.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {				
				
				String selected = (String) phenotypeFrom.getSelectedItem();
				if (StringUtils.equals(selected, FROM_SETS)) {					
					includeLoadButton.setEnabled(false);
					excludeLoadButton.setEnabled(false);
					includeSets.setEnabled(true);
					excludeSets.setEnabled(true);
				} else {					
					includeLoadButton.setEnabled(true);
					excludeLoadButton.setEnabled(true);
					includeSets.setEnabled(false);
					excludeSets.setEnabled(false);
				}
			}
		});
		
		networkFrom.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {				
				
				String selected = (String) networkFrom.getSelectedItem();
				if (StringUtils.equals(selected, FROM_PROJECT)) {
					networkMatrix.setEnabled(true);
					networkLoadButton.setEnabled(false);
					networkField.setEnabled(false);
					
					networkMatrix.removeAllItems();
					networkMatrix.addItem(" ");
                	for(String setName: adjModel) {
                		networkMatrix.addItem(setName);
                	}					
				} else {
					networkMatrix.setEnabled(false);
					networkLoadButton.setEnabled(true);
					networkField.setEnabled(true);
				}
			}
		});		
		
		 includeSets.addActionListener(new ActionListener() {
	    		public void actionPerformed(ActionEvent actionEvent) {
	    			String selectedLabel = (String) includeSets.getSelectedItem();
	    			if ((!StringUtils.isEmpty(selectedLabel))&&(!selectedLabel.equals(""))&&(!selectedLabel.equals(" ")))	//!StringUtils.isEmpty(selectedLabel)
	    				if (!chooseArraysFromSet(selectedLabel, includeField)) {
	    					includeSets.setSelectedIndex(0);	    					
	    				}
	    		}
	    	});
		
		excludeSets.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				if(!excludeSets.isEnabled())return;
				String selectedLabel = (String) excludeSets.getSelectedItem();
				if ((!StringUtils.isEmpty(selectedLabel))&&(!selectedLabel.equals(""))&&(!selectedLabel.equals(" "))){
					if (!chooseArraysFromSet(selectedLabel, excludeField)) {
    					excludeSets.setSelectedIndex(0);	    					
    				}
				}
				else
					excludeField.setText("");
			}
		});
		

		nullDataLoadButton.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				nullDataLoadPressed();
			}	
			
		});

		networkMatrix.addActionListener(new ActionListener() {
    		public void actionPerformed(ActionEvent actionEvent) {
    			String selectedLabel = (String) networkMatrix.getSelectedItem();
    			if (!StringUtils.isEmpty(selectedLabel))
    				if (!chooseNetworkFromSet(selectedLabel)) {
    					networkMatrix.setSelectedIndex(0);
    					ideaNetwork=null;
    				}
    		}
    	});
		
		networkLoadButton.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				networkLoadPressed();
			}	
			
		});		

		includeLoadButton.addActionListener(new ActionListener(){			
			public void actionPerformed(ActionEvent e) {
				includeLoadPressed();
			}});
		
		excludeLoadButton.addActionListener(new ActionListener(){			
			public void actionPerformed(ActionEvent e) {
				excludeLoadPressed();
			}});		
		
		ParameterActionListener parameterActionListener = new ParameterActionListener(
				this);
		networkField.addActionListener(parameterActionListener);		
		nullDataField.addActionListener(parameterActionListener);
		includeField.addActionListener(parameterActionListener);
		excludeField.addActionListener(parameterActionListener);
	}
	
	private class NullData_actionAdapter implements	
		java.awt.event.ActionListener {
			public void actionPerformed(ActionEvent e) {
				boolean nullDataOn=nullDataCheckbox.isSelected();
				if(nullDataOn){
					nullDataField.setEnabled(true);					
					nullDataLoadButton.setEnabled(true);
				}
				else{
					nullDataField.setEnabled(false);					
					nullDataLoadButton.setEnabled(false);
				}
		}
	}
	
	public void setMicroarraySet(DSMicroarraySet maSet){
		this.maSet = maSet;		
	}	
	
	public void setParameters(Map<Serializable, Serializable> parameters) {		
		if(firstRunFlag) {
			firstRunFlag=false;
			return;
		}
		Set<Map.Entry<Serializable, Serializable>> set = parameters.entrySet();
		for (Iterator<Map.Entry<Serializable, Serializable>> iterator = set
				.iterator(); iterator.hasNext();) {
			Map.Entry<Serializable, Serializable> parameter = iterator.next();
			Object key = parameter.getKey();
			Object value = parameter.getValue();

			// set the parameters on GUI based on the Map
			if (key.equals("networkText")) {
				networkField.setText((String)value);
				try {
					getNetworkFromFile(networkField.getText());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					ideaNetwork = null;
					e.printStackTrace();
				}
			}			
		
			if (key.equals("phenotypeFromType")){
				this.phenotypeFrom.setSelectedIndex((Integer)value);
			}
			
			if (key.equals("includeText")) {
				includeField.setText((String)value);
				Set<Integer> includeSet=preparePhenoSet(includeField.getText());
				phenotype.setIncludeList(includeSet);
			}
			
			if (key.equals("excludeText")) {
				excludeField.setText((String)value);
				Set<Integer> excludeSet=preparePhenoSet(excludeField.getText());
				phenotype.setExcludeList(excludeSet);
			}
		
			if (key.equals("nullDataText")) {
				nullDataField.setText((String)value);
			}
			
			if (key.equals("nullDataCheckbox")) {
				nullDataCheckbox.setSelected((Boolean)value);
				nullDataField.setEnabled((Boolean)value);
			}	
					
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
		parameters.put("networkText", networkField.getText());
		parameters.put("phenotypeFromType", this.phenotypeFrom.getSelectedIndex());		
		parameters.put("", (String) this.includeSets.getSelectedItem());
		parameters.put("includeText",includeField.getText());
		parameters.put("", (String) this.excludeSets.getSelectedItem());
		parameters.put("excludeText", excludeField.getText());
		parameters.put("nullDataText", nullDataField.getText());		
		parameters.put("nullDataCheckbox", nullDataCheckbox.isSelected());	
		
		return parameters;
	}	

	@Override
	public void fillDefaultValues(Map<Serializable, Serializable> parameters) {
		if(parameters.get("network")==null)
			parameters.put("network", "");
		if(parameters.get("phenotype")==null)
			parameters.put("phenotype", "");
		if(parameters.get("nullData")==null)
			parameters.put("nullData", "");		
	}

	@Override
	public String getDataSetHistory() {
		StringBuilder histStr = new StringBuilder("");
		histStr.append("IDEA Analysis parameters:\n");
		histStr.append("----------------------------------------");
		histStr.append("\nNetwork: "+networkField.getText());		
		String includeStr=phenotype.getPhenotypeAsString()[0];		
		histStr.append("\n"+includeStr);
		String excludeStr=phenotype.getPhenotypeAsString()[1];
		histStr.append("\n"+excludeStr);
		if(nullDataCheckbox.isSelected())
				histStr.append("\nNull file: "+nullDataField.getText());
		//histStr.append("\np-value: "+pValueTextField.getText());
		return histStr.toString();
	}

	public String getPvalue(){
		return P_VALUE;
	}
	
	public boolean getUseNullData(){
		return nullDataCheckbox.isSelected();
	}
	
	public void networkLoadPressed(){

		JFileChooser fc = new JFileChooser(this.getLastDirectory());
		int returnVal = fc.showOpenDialog(IDEAPanel.this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {					
			try {
				String filename = fc.getSelectedFile().getAbsolutePath();			
				String filepath = fc.getCurrentDirectory().getCanonicalPath();
                setLastDirectory(filepath);			
                getNetworkFromFile(filename);				
				networkField.setText(filename);			
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
				ideaNetwork = null;
			} catch (IOException e2) {
				e2.printStackTrace();
				ideaNetwork = null;
			}					
		}	
	}
	
	private void getNetworkFromFile(String filename) throws IOException{
		BufferedReader br;
		br = new BufferedReader(new FileReader(filename));
		String line = br.readLine(); // skip the header line
		networkList.add(line);
		line = br.readLine();
		networkList.add(line);
		ideaNetwork = new ArrayList<IdeaNetworkEdge>();
		while(line!=null && line.trim().length()>0) {
			IdeaNetworkEdge edge = new IdeaNetworkEdge(line);
			ideaNetwork.add(edge);
			line = br.readLine();
			networkList.add(line);
		}
	}
	
	//prepare networkList and ideaNetwork from project adjacencyMatrix
	private void getNetworkFromProject(AdjacencyMatrixDataSet adjDataSet){
		ideaNetwork = new ArrayList<IdeaNetworkEdge>();		
		NodeType nt=adjDataSet.getMatrix().getEdges().get(0).node1.getNodeType();
		if(nt.equals(NodeType.PROBESET_ID)||nt.equals(NodeType.MARKER)){
			for(Edge ed:adjDataSet.getMatrix().getEdges()){
				
				int i1=ed.node1.getMarker().getGeneId();
				int i2=ed.node2.getMarker().getGeneId();
				IdeaNetworkEdge anEdge=new IdeaNetworkEdge(i1,i2);
				boolean newEdge=true;
				for(IdeaNetworkEdge ie:ideaNetwork){
					if(ie.compareTo(anEdge)==0){
						newEdge=false;
						break;
					}				
				}
				if (newEdge){
					ideaNetwork.add(anEdge);					
				}				
			}
		}
		
		for(IdeaNetworkEdge ie:ideaNetwork){			
			networkList.add(ie.getGene1()+"\t"+ie.getGene2()+"\t1"+"\t0"+"\t0");
		}		
	}
	
	public void includeLoadPressed(){
		loadPhenotype(includeField);
	}
	public void excludeLoadPressed(){
		loadPhenotype(excludeField);
	}	
	
	private void loadPhenotype(JTextField textField){

		JFileChooser fc = new JFileChooser(this.getLastDirectory());
		fc.addChoosableFileFilter(new CSVFilter());
        fc.setAcceptAllFileFilterUsed(false);
        int returnVal = fc.showOpenDialog(IDEAPanel.this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {	
        	try{
	        	String filename = fc.getSelectedFile().getAbsolutePath();			
				String filepath = fc.getCurrentDirectory().getCanonicalPath();
	            setLastDirectory(filepath);	           
	            BufferedReader br;
	    		br = new BufferedReader(new FileReader(filename));	    		
	    		String line = br.readLine();
	    		String text="";
	    		int invalidArrayNo=0;
	    		while(line!=null && line.trim().length()>0) {
	    			String[] tokens = line.split(",");
	    			if(isValidArray(tokens[0])){
	    				text+=tokens[0]+",";		//only get the column0
	    			}
	    			else{
	    				invalidArrayNo++;
	    			}
	    			line = br.readLine();
	    		}
	    		if(invalidArrayNo!=0){
	    			JOptionPane.showMessageDialog(
							null,
							invalidArrayNo+" array(s) listed in the CSV file not present in data.",
							"Warning",
							JOptionPane.ERROR_MESSAGE);
	    		}
	    		if(text.length()>1){
		    		String s=text.substring(0,text.length()-1);	//remove the last ,	    		
		    		textField.setText(s);
	    		}
	    		else{
	    			textField.setText("");
	    		}
	    		
        	}
        	catch (IOException e) {				
				textField.setText("");				
				log.error(e);
				JOptionPane.showMessageDialog(
						null,
						"The input file does not comply with the designated csv format.",
						"Parsing Error",
						JOptionPane.ERROR_MESSAGE);
			}					
        }
		
	
	}
	public boolean isValidArray(String token){		
		for(int i=0;i<maSet.size();i++){		
			if(token.equalsIgnoreCase(maSet.get(i).getLabel())){
				return true;
			}
		}
	
		return false;
	}
	public void nullDataLoadPressed(){
		JFileChooser fc = new JFileChooser(this.getLastDirectory());
		int returnVal = fc.showOpenDialog(IDEAPanel.this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {					
			try {
				String filename = fc.getSelectedFile().getAbsolutePath();			
				String filepath = fc.getCurrentDirectory().getCanonicalPath();
                setLastDirectory(filepath);                
				nullDataField.setText(filename);				
				
				FileReader filereader;				
				filereader = new FileReader(filename);			
				Scanner in = new Scanner(filereader);
				nullDataList =new ArrayList<String>();				
				while (in.hasNextLine()) {
					String line = in.nextLine();
					nullDataList.add(line+"\n");					
				}				
				
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
				nullDataField.setText("");
			} catch (IOException e2) {
				e2.printStackTrace();
				nullDataField.setText("");
			}					
		}	
	}	
	
	public String getLastDirectory() {
        String dir = ".";
        try {
            String filename = FilePathnameUtils.getIDEASettingsPath();

            File file = new File(filename);
            if (file.exists()) {
                BufferedReader br = new BufferedReader(new FileReader(file));

                dir = br.readLine();
                br.close();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        if (dir == null) {
            dir = ".";
        }
        return dir;
    }
	public void setLastDirectory(String dir) {
        try { //save current settings.
            String outputfile = FilePathnameUtils.getIDEASettingsPath();
            BufferedWriter br = new BufferedWriter(new FileWriter(
                    outputfile));
            br.write(dir);
            br.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
	
	public String[] getPhenotypeAsString() {
		return phenotype.getPhenotypeAsString();
	}
	
	public String[] getNullDataAsString() {
		
			String[] nullDataAsString=new String[nullDataList.size()];
			int i=0;
			for(String s:nullDataList){
				nullDataAsString[i]=s;
				i++;
			}
	
			return nullDataAsString;
	}
	
	public String[] getNetworkAsString() {
		String[] networkAsString=new String[networkList.size()];
		int i=0;
		for(String s:networkList){
			networkAsString[i]=s;
			i++;
		}
		return networkAsString;
	}
	
	public String getIncludeString(){
		return includeField.getText();
	}
	
	public String getExcludeString(){
		return excludeField.getText();
	}	
	
	ArrayList<AdjacencyMatrixDataSet> adjacencymatrixDataSets = new ArrayList<AdjacencyMatrixDataSet>();	

	public void renameAdjMatrixToCombobox(AdjacencyMatrixDataSet adjDataSet,
			String oldName, String newName) {
		for (AdjacencyMatrixDataSet adjSet : adjacencymatrixDataSets) {
			if (adjSet == adjDataSet)
				adjSet.setLabel(newName);
		}
		adjModel.remove(oldName);
		adjModel.add(newName);
	}
	
	public void removeAdjMatrixToCombobox(AdjacencyMatrixDataSet adjDataSet) {
		try {
			adjacencymatrixDataSets.remove(adjDataSet);
			// adjModel.remove(adjDataSet.getDataSetName());
			adjModel.remove(adjModel.indexOf(adjDataSet.getDataSetName()));
		} catch (Exception ex) {
			log.error(ex.getMessage());
		}
	}
	
	public void addAdjMatrixToCombobox(AdjacencyMatrixDataSet adjDataSet) {
		adjacencymatrixDataSets.add(adjDataSet);
		adjModel.add(adjDataSet.getDataSetName());		
	}
	
	public String getSelectedAdjMatrix()
	{		 
		   return (String)networkMatrix.getSelectedItem();
	}
	
	public void setSelectedAdjMatrix(String datasetName)
	{		 
		networkMatrix.getModel().setSelectedItem(datasetName);
	}
	public void clearAdjMatrixCombobox() {
		adjacencymatrixDataSets.clear();
		if (adjModel!=null)		adjModel.clear();
	}	
	
	void setSelectorPanelForArray(DSPanel<DSMicroarray> ap) {
		selectorPanelOfArrays = ap;		
		String currentTargetSet = (String) includeSets.getSelectedItem();
		String current2TargetSet = (String) excludeSets.getSelectedItem();
		DefaultComboBoxModel targetComboModel = (DefaultComboBoxModel) includeSets.getModel();
		DefaultComboBoxModel target2ComboModel = (DefaultComboBoxModel) excludeSets.getModel();
		targetComboModel.removeAllElements();
		targetComboModel.addElement(" ");
		target2ComboModel.removeAllElements();
		target2ComboModel.addElement(" ");		
		//includeField.setText("");
		//excludeField.setText("");
		for (DSPanel<DSMicroarray> panel : selectorPanelOfArrays.panels()) {
			String label = panel.getLabel().trim();
			targetComboModel.addElement(label);
			target2ComboModel.addElement(label);
			if(currentTargetSet!=null){
				if (StringUtils.equals(label, currentTargetSet.trim())){
					targetComboModel.setSelectedItem(label);					
				}
			}
			if(current2TargetSet!=null){
				if (StringUtils.equals(label, current2TargetSet.trim())){
					target2ComboModel.setSelectedItem(label);					
				}
			}
			
		}
	}
	
	public boolean chooseNetworkFromSet(String setLabel){
		for (AdjacencyMatrixDataSet adjSet : adjacencymatrixDataSets) {
			if (adjSet.getLabel() == setLabel){				
				getNetworkFromProject(adjSet);
				return true;
			}
		}
		return false;
	}
	private class CSVFilter extends FileFilter {
	    //Accept all directories and csv files.
	    public boolean accept(File f) {
	        if (f.isDirectory()) {
	            return true;
	        }
	        String extension = getExtension(f);
	        if (extension != null) {
	            if (extension.equals("csv")) {
	                    return true;
	            } else {
	                return false;
	            }
	        }
	        return false;
	    }
	    public String getExtension(File f) {
	        String ext = null;
	        String s = f.getName();
	        int i = s.lastIndexOf('.');
	        if (i > 0 &&  i < s.length() - 1) {
	            ext = s.substring(i+1).toLowerCase();
	        }
	        return ext;
	    }
	    //The description of this filter
	    public String getDescription() {
	        return "Comma Separated Values Files";
	    }
	}
	
	
	public boolean chooseArraysFromSet(String setLabel, JTextField toPopulate) {
		DSPanel<DSMicroarray> selectedSet = chooseArraysSet(setLabel, selectorPanelOfArrays);

		if (selectedSet != null) {
			if (selectedSet.size() > 0) {
				StringBuilder sb = new StringBuilder();
				for (DSMicroarray m : selectedSet) {
					sb.append(m.getLabel());
					sb.append(",");
				}
				sb.trimToSize();
				sb.deleteCharAt(sb.length() - 1); // getting rid of last comma
				toPopulate.setText(sb.toString());
				return true;
			} else {				
				JOptionPane.showMessageDialog(null, "Array set, " + setLabel
						+ ", is empty.", "Input Error",
						JOptionPane.ERROR_MESSAGE);				
				
				return false;
			}
		}

		return false;
	}	
	public static DSPanel<DSMicroarray> chooseArraysSet(String setLabel, DSPanel<DSMicroarray> selectorPanel){
		DSPanel<DSMicroarray> selectedSet = null;
		if (selectorPanel != null){
			setLabel = setLabel.trim();
			for (DSPanel<DSMicroarray> panel : selectorPanel.panels()) {
				if (StringUtils.equals(setLabel, panel.getLabel().trim())) {
					selectedSet = panel;
					break;
				}
			}
		}

		return selectedSet;
	}
	
}
