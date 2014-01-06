package org.geworkbench.components.demand;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.analysis.AbstractSaveableParameterPanel;
import org.geworkbench.bison.datastructure.biocollections.AdjacencyMatrix;
import org.geworkbench.bison.datastructure.biocollections.AdjacencyMatrixDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.events.listeners.ParameterActionListener;
import org.geworkbench.parsers.AdjacencyMatrixFileFormat;
import org.geworkbench.parsers.InputFileFormatException;
import org.geworkbench.util.FilePathnameUtils;
import org.geworkbench.util.Util;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

public class DemandPanel extends AbstractSaveableParameterPanel{

	private static final long serialVersionUID = 6420773967072071478L;
	static Log log = LogFactory.getLog(DemandPanel.class);
	
	private ArrayList<String> adjModel			=	new ArrayList<String>();

	private static final String FROM_WORKSPACE	=	"From Workspace";
	private static final String FROM_FILE		=	"From File";
	private static final String FROM_SETS		=	"From Set";
	private static final String[] NETWORK_FROM	=	{FROM_WORKSPACE, FROM_FILE};
	private static final String[] DRUG_FROM		=	{FROM_SETS, FROM_FILE};
	private static final String[] CTRL_FROM		=	{FROM_SETS, FROM_FILE};
	private static final String[] DEFAULT_SET	=	{ " " };
	private static final String NETWORK_HR		= "DeMAND Network: ";
	private static final String DRUG_HR			= "DeMAND Drugs: ";
	private static final String CTRL_HR			= "DeMAND Controls: ";
	private static final String SERVICE_HR		= "DeMAND Service: ";
	
	private JLabel networkLabel		=	new JLabel("Load Network");
	private JLabel drugLabel		=	new JLabel("Drug Arrays");
	private JLabel ctrlLabel		=	new JLabel("Control Arrays");
	private JComboBox networkFrom	=	new JComboBox(NETWORK_FROM);	
	private JComboBox drugFrom		=	new JComboBox(DRUG_FROM);
	private JComboBox ctrlFrom		=	new JComboBox(CTRL_FROM);
	private JComboBox networkMatrix	=	new JComboBox();	
	private JComboBox drugSets		=	new JComboBox(new DefaultComboBoxModel(DEFAULT_SET));
	private JComboBox ctrlSets		=	new JComboBox(new DefaultComboBoxModel(DEFAULT_SET));
	private JTextField networkField	=	new JTextField(20);	
	private JTextField drugField	=	new JTextField();
	private JTextField ctrlField	=	new JTextField();
	private JButton networkLoadButton=	new JButton("Load");	
	private JButton drugLoadButton	=	new JButton("Load");
	private JButton ctrlLoadButton	=	new JButton("Load");
	private JButton sampleInfoLoadButton=new JButton("Load Sample Information");
	
	private DSMicroarraySet maSet	=	null;
	private String networkFilename	=	"";
	private AdjacencyMatrixDataSet adjMatrix = null;
	private DSPanel<DSMicroarray> selectorPanelOfArrays;
	
	private static final String lastDirConf = FilePathnameUtils.getUserSettingDirectoryPath()
			+ "demand" + FilePathnameUtils.FILE_SEPARATOR + "lastDir.conf";
	
    private static final String[] SERVICES	= { "local service", "web service" };

    private JComboBox service = new JComboBox(SERVICES);

	
	public DemandPanel() {
		try {
			init();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void init() throws Exception {		
		networkFrom.setSelectedIndex(0);
		drugFrom.setSelectedIndex(0);
		ctrlFrom.setSelectedIndex(0);

		networkField.setText("");	
		drugField.setText("");
		ctrlField.setText("");
		
		networkMatrix.setEnabled(true);
		networkField.setEnabled(false);
		networkField.setEditable(false);
		
		networkLoadButton.setEnabled(false);		
		drugLoadButton.setEnabled(false);
		ctrlLoadButton.setEnabled(false);			
			
		FormLayout layout = new FormLayout(
				"left:max(100dlu;pref), 10dlu, 100dlu, 10dlu, "
						+ "100dlu, 10dlu, 100dlu, 10dlu, 100dlu", "");
		DefaultFormBuilder builder = new DefaultFormBuilder(layout);
		builder.setDefaultDialogBorder();

		builder.appendSeparator("DeMAND Parameters");			
		builder.append("Select Service", service);
		builder.nextLine();

		builder.append(networkLabel);
		builder.append(networkFrom);			
		builder.append(networkMatrix);
		builder.append(networkField);
		builder.append(networkLoadButton);						
		builder.nextLine();			
			
		builder.append(drugLabel);
		builder.append(drugFrom);						
		builder.append(drugSets);
		builder.append(drugField);
		builder.append(drugLoadButton);
		builder.nextLine();			
		
		builder.append(ctrlLabel);
		builder.append(ctrlFrom);
		builder.append(ctrlSets);
		builder.append(ctrlField);
		builder.append(ctrlLoadButton);
		builder.nextLine();			
		
		builder.append(sampleInfoLoadButton);
		builder.nextLine();
		
		this.add(builder.getPanel());
			
		networkFrom.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {				
				networkField.setText("");
				String selected = (String) networkFrom.getSelectedItem();
				if (StringUtils.equals(selected, FROM_WORKSPACE)) {
					networkMatrix.setEnabled(true);
					networkLoadButton.setEnabled(false);					
					networkField.setEnabled(false);					
					refreshNetworkMatrixs();
				} else {
					networkMatrix.setEnabled(false);
					networkLoadButton.setEnabled(true);
					networkField.setEnabled(true);
				}
			}
		});		
		
		drugFrom.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {				
				drugField.setText("");				
				drugSets.setSelectedIndex(0);				
				String selected = (String) drugFrom.getSelectedItem();
				if (StringUtils.equals(selected, FROM_SETS)) {					
					drugLoadButton.setEnabled(false);									
					drugSets.setEnabled(true);					
				} else {					
					drugLoadButton.setEnabled(true);					
					drugSets.setEnabled(false);					
				}
			}
		});
		
		ctrlFrom.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {				
				ctrlField.setText("");				
				ctrlSets.setSelectedIndex(0);
				String selected = (String) ctrlFrom.getSelectedItem();
				if (StringUtils.equals(selected, FROM_SETS)) {					
					ctrlLoadButton.setEnabled(false);					
					ctrlSets.setEnabled(true);
				} else {					
					ctrlLoadButton.setEnabled(true);				
					ctrlSets.setEnabled(false);
				}
			}
		});
		
		networkMatrix.addActionListener(new ActionListener() {
    		public void actionPerformed(ActionEvent actionEvent) {
    			String selectedLabel = (String) networkMatrix.getSelectedItem();
    			if (!StringUtils.isEmpty(selectedLabel))
    				if (!chooseNetworkFromSet(selectedLabel)) {
    					networkMatrix.setSelectedIndex(0);
    					/*ideaNetwork=null;*/
    				}
    		}
    	});
	
		drugSets.addActionListener(new ActionListener() {
	    	public void actionPerformed(ActionEvent actionEvent) {
	    		String selectedLabel = (String) drugSets.getSelectedItem();
	    		if ((!StringUtils.isEmpty(selectedLabel))&&(!selectedLabel.equals(""))
	    				&&(!selectedLabel.equals(" "))){	//!StringUtils.isEmpty(selectedLabel)
	    			if (!chooseArraysFromSet(selectedLabel, drugField)) {
	    				drugSets.setSelectedIndex(0);	    					
	    			}
	    		}
	    		else
	    			drugField.setText("");
	    	}
	    });
		
		ctrlSets.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				if(!ctrlSets.isEnabled())return;
				String selectedLabel = (String) ctrlSets.getSelectedItem();
				if ((!StringUtils.isEmpty(selectedLabel))&&(!selectedLabel.equals(""))&&(!selectedLabel.equals(" "))){
					if (!chooseArraysFromSet(selectedLabel, ctrlField)) {
						ctrlSets.setSelectedIndex(0);	    					
    				}
				}
				else
					ctrlField.setText("");
			}
		});
		
		networkLoadButton.addActionListener(new LoadNetworkButtonListener());

		drugLoadButton.addActionListener(new ActionListener(){			
			public void actionPerformed(ActionEvent e) {
				loadPhenotype(drugField);
			}});
		
		ctrlLoadButton.addActionListener(new ActionListener(){			
			public void actionPerformed(ActionEvent e) {
				loadPhenotype(ctrlField);
			}});
		
		sampleInfoLoadButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				loadSampleInfo();
			}
		});
		
		ParameterActionListener parameterActionListener = new ParameterActionListener(
				this);
		networkField.addActionListener(parameterActionListener);	
		drugField.addActionListener(parameterActionListener);
		ctrlField.addActionListener(parameterActionListener);
		service.addActionListener(parameterActionListener);
	}
	
	void setMicroarraySet(DSMicroarraySet maSet){
		this.maSet = maSet;
	}	
	
    public String getService() {
        return (String)service.getSelectedItem();
    }

    @Override
	public void setParameters(Map<Serializable, Serializable> parameters) {
		if (getStopNotifyAnalysisPanelTemporaryFlag() == true || parameters == null)
    		return;
		stopNotifyAnalysisPanelTemporary(true);
		Set<Map.Entry<Serializable, Serializable>> set = parameters.entrySet();
		for (Iterator<Map.Entry<Serializable, Serializable>> iterator = set
				.iterator(); iterator.hasNext();) {
			Map.Entry<Serializable, Serializable> parameter = iterator.next();
			Object key = parameter.getKey();
			Object value = parameter.getValue();

			if (key.equals(NETWORK_HR)) {
				networkField.setText((String)value);
			}			
			else if (key.equals(DRUG_HR)) {
				drugField.setText((String)value);
			}
			else if (key.equals(CTRL_HR)) {
				ctrlField.setText((String)value);
			}
			else if (key.equals(SERVICE_HR)){
				service.setSelectedItem(value);
			}
		}
		String networkText = parameters.get("networkText")==null?null:parameters.get("networkText").toString();		 
		if (maSet != null && networkField.isEnabled()
				&& networkText != null && !networkText.trim().equals("")) {
			networkField.setText(networkText);
			networkFilename = new File(networkText).getName();
			if (!isLabFormat(networkText, 10)){
				try {
					adjMatrix = new AdjacencyMatrixDataSet(
							0, networkText, networkText, maSet, networkText);
				} catch (InputFileFormatException e) {
					log.error(e.getMessage());
					e.printStackTrace();
				}
			}
		}

		stopNotifyAnalysisPanelTemporary(false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geworkbench.analysis.AbstractSaveableParameterPanel#getParameters()
	 */
	@Override
	public Map<Serializable, Serializable> getParameters() {
		Map<Serializable, Serializable> parameters = new HashMap<Serializable, Serializable>();

		// set the Map with the parameter values retrieved from GUI
		// component
		parameters.put(SERVICE_HR, getService());
		String selected = (String) networkFrom.getSelectedItem();
		if (StringUtils.equals(selected, FROM_WORKSPACE))
			parameters.put(NETWORK_HR, getSelectedAdjMatrix());
		else
			parameters.put(NETWORK_HR, networkField.getText());
		parameters.put(DRUG_HR,drugField.getText());
		parameters.put(CTRL_HR, ctrlField.getText());	
		return parameters;
	}	

	@Override
	public void fillDefaultValues(Map<Serializable, Serializable> parameters) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getDataSetHistory() {
		String histStr = "";
		Map<Serializable, Serializable> pMap = getParameters();
		histStr += "DeMAND Analysis parameters:\n";
		histStr += "----------------------------------------\n";
		for (Iterator<Map.Entry<Serializable, Serializable>> iterator = pMap
				.entrySet().iterator(); iterator.hasNext();) {
			Map.Entry<Serializable, Serializable> parameter = iterator.next();
			Object key = parameter.getKey();
			Object value = parameter.getValue();
			histStr += key.toString() + value.toString() + "\n";
		}
		return histStr;
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
	
	private void loadPhenotype(JTextField textField){

		JFileChooser fc = new JFileChooser(getLastDir());
		fc.addChoosableFileFilter(new FileNameExtensionFilter("Comma Separated Values(CSV) Files", "csv"));
        fc.setAcceptAllFileFilterUsed(false);
        int returnVal = fc.showOpenDialog(DemandPanel.this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {	
        	try{
	        	String filename = fc.getSelectedFile().getAbsolutePath();			
				String filepath = fc.getCurrentDirectory().getCanonicalPath();
	            saveLastDir(filepath);	           
	            BufferedReader br = new BufferedReader(new FileReader(filename));	    		
	    		String line = null;
	    		String text="";
	    		int invalidArrayNo=0;
	    		while((line = br.readLine())!=null) {
	    			String[] tokens = line.split(", *");
	    			if(isValidArray(tokens[0]))
	    				text+=tokens[0]+",";		//only get the column0
	    			else invalidArrayNo++;
	    		}
	    		br.close();
	    		if(invalidArrayNo!=0){
	    			JOptionPane.showMessageDialog(
							null,
							invalidArrayNo+" array(s) listed in the CSV file not present in data.",
							"Warning",
							JOptionPane.ERROR_MESSAGE);
	    		}
	    		if(text.length()>1)
	    			text = text.substring(0,text.length()-1);	//remove the last ,	   		
		    	textField.setText(text);
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
	
	private void loadSampleInfo(){
		JFileChooser fc = new JFileChooser(getLastDir());
		fc.addChoosableFileFilter(new FileNameExtensionFilter("TXT file", "txt"));
        fc.setAcceptAllFileFilterUsed(false);
        int returnVal = fc.showOpenDialog(DemandPanel.this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {	
    		
        	try{
	        	String filename = fc.getSelectedFile().getAbsolutePath();			
				String filepath = fc.getCurrentDirectory().getCanonicalPath();
	            saveLastDir(filepath);	           
	            BufferedReader br = new BufferedReader(new FileReader(filename));	    		
	    		String line = null;
	    		String drugText = "", ctrlText = "";
	    		boolean drugLine = false, ctrlLine = false;
	    		while((line = br.readLine())!=null) {
	    			String[] tokens = line.split("\t");
	    			if(tokens[0].equalsIgnoreCase("Drug")){
	    				drugLine = true;
	    				drugText += idToArrayNames(tokens);
	    			}else if(tokens[0].equalsIgnoreCase("Ctrl")){
	    				ctrlLine = true;
	    				ctrlText += idToArrayNames(tokens);
	    			}
	    		}
	    		br.close();
	    		if (!drugLine || !ctrlLine){
	    			JOptionPane.showMessageDialog(
							null,
							"Drug or Ctrl line not present in the sample info file.",
							"Warning",
							JOptionPane.ERROR_MESSAGE);
	    		}else if(invalidArrayNo > 0){
	    			JOptionPane.showMessageDialog(
							null,
							invalidArrayNo + " array(s) listed in the sample info file not present in data.",
							"Warning",
							JOptionPane.ERROR_MESSAGE);
	    		}
	    		drugField.setText(drugText);
	    		ctrlField.setText(ctrlText);
        	}
        	catch (IOException e) {							
				log.error(e);
				JOptionPane.showMessageDialog(
						null,
						"The input file does not comply with the expected txt format.",
						"Parsing Error",
						JOptionPane.ERROR_MESSAGE);
			}					
        }
	}
	
	private int invalidArrayNo=0;
	private String idToArrayNames(String[] tokens){
		StringBuilder sb = new StringBuilder();
		for (int i = 1; i < tokens.length; i++){
			int id = Integer.parseInt(tokens[i])-1;
			if (id >=0 && id < maSet.size())
				sb.append(maSet.get(id).getLabel()+",");
			else invalidArrayNo++;
		}
		String text = sb.toString();
		if (text.length()==0) return text;
		return text.substring(0, text.length()-1);
	}
	
	private boolean isValidArray(String token){		
		for(int i=0;i<maSet.size();i++){		
			if(token.equalsIgnoreCase(maSet.get(i).getLabel())){
				return true;
			}
		}
	
		return false;
	}
	
	void setSelectorPanelForArray(DSPanel<DSMicroarray> ap) {
		selectorPanelOfArrays = ap;		
		String currentTargetSet = (String) drugSets.getSelectedItem();
		String current2TargetSet = (String) ctrlSets.getSelectedItem();
		DefaultComboBoxModel targetComboModel = (DefaultComboBoxModel) drugSets.getModel();
		DefaultComboBoxModel target2ComboModel = (DefaultComboBoxModel) ctrlSets.getModel();
		targetComboModel.removeAllElements();
		targetComboModel.addElement(" ");
		target2ComboModel.removeAllElements();
		target2ComboModel.addElement(" ");		
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
	
	private boolean chooseArraysFromSet(String setLabel, JTextField toPopulate) {
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
	
	private static DSPanel<DSMicroarray> chooseArraysSet(String setLabel, DSPanel<DSMicroarray> selectorPanel){
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

	Set<Integer> getDrugSet(){
		return getPhenoSet(drugField.getText());
	}
	
	Set<Integer> getCtrlSet(){
		return getPhenoSet(ctrlField.getText());
	}
	
	private Set<Integer> getPhenoSet(String in){
		Set<Integer> oneSet=new TreeSet<Integer>();
		if(maSet==null) return oneSet;
		
		String[] tokens=in.split(", *");
		for(String s:tokens){
			for(int i=0;i<maSet.size();i++){
				if(s.equalsIgnoreCase(maSet.get(i).getLabel())){
					oneSet.add(i+1);	//the first column is 1 not 0 in documentation of phenotype;
				}
			}
		}
		return oneSet;
	}

	public class LoadNetworkButtonListener implements
		java.awt.event.ActionListener {

		public void actionPerformed(java.awt.event.ActionEvent e) {			
			if (e.getActionCommand().equals("Load")) {
				if (maSet != null) {
					String adjMatrixFileStr = "C:\\Documents and Settings\\yc2480\\eclipse_geworkbench_workspace\\geworkbench-core\\data\\testaracne4.adjmat";
					File adjMatrixFile = new File(adjMatrixFileStr);
					JFileChooser chooser = new JFileChooser(adjMatrixFile
							.getParent());
					String lastDir = null;
					if ((lastDir = getLastDir()) != null) {
						chooser.setCurrentDirectory(new File(lastDir));
					}
					chooser.setFileFilter(new AdjacencyMatrixFileFormat().getFileFilter());
					chooser.showOpenDialog(DemandPanel.this);
					if (chooser.getSelectedFile() != null) {
						File selectedFile = chooser.getSelectedFile();
						adjMatrixFileStr = selectedFile.getPath();
						networkField.setText(adjMatrixFileStr);
						networkFilename = selectedFile.getName();
						saveLastDir(selectedFile.getParent());
		
						if (!openDialog()) return;

						// lab format network file is used directly
						if (!selectedFormat.equals(labFormat)){
							try {
								AdjacencyMatrix matrix = AdjacencyMatrixDataSet
								.parseAdjacencyMatrix(adjMatrixFileStr, maSet,
										interactionTypeMap, selectedFormat,
										selectedRepresentedBy, isRestrict);

								adjMatrix = new AdjacencyMatrixDataSet(matrix, 
										0, adjMatrixFileStr, adjMatrixFileStr, maSet);
							} catch (InputFileFormatException e1) {
								log.error(e1.getMessage());
								e1.printStackTrace();
							}
						}else{
							adjMatrix = null;
						}
					} else {
						// user canceled
					}
				}
			}
		}
	}
	
	private String[] representedByList;
	private String selectedRepresentedBy = AdjacencyMatrixDataSet.PROBESET_ID;
	private HashMap<String, String> interactionTypeMap = null;
	private boolean isRestrict = true;
	private boolean isCancel = false;
	private String selectedFormat = AdjacencyMatrixDataSet.ADJ_FORMART;
	private String labFormat = "lab format";

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
			formatJcb.addItem(labFormat);
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
							labFormat)) {
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
			else if (networkFilename.toLowerCase().endsWith(".adj"))
				formatJcb.setSelectedItem(AdjacencyMatrixDataSet.ADJ_FORMART);
			else
				formatJcb.setSelectedItem(labFormat);
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
				||(selectedFormat.equals(labFormat) && !isLabFormat(networkField.getText(), 10))) {
			String theMessage = "The network format selected may not match that of the file.  \nClick \"Cancel\" to terminate this process.";
			Object[] optionChoices = { "Continue", "Cancel"};
			int result = JOptionPane.showOptionDialog(
				(Component) null, theMessage, "Warning",
				JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
				null, optionChoices, optionChoices[1]);
			if (result == JOptionPane.NO_OPTION)
				return false;

		} 

		if (selectedFormat
				.equalsIgnoreCase(AdjacencyMatrixDataSet.SIF_FORMART)) {
			interactionTypeMap = new org.geworkbench.parsers.AdjacencyMatrixFileFormat().getInteractionTypeMap();
		}
		return true;
	}
	
	/**
	 * Test if the network is in lab format.
	 * @param fname    network file name
	 * @param numrows  test format in the first numrows; if numrows <= 0, test whole file.
	 * @return if the network is in lab format
	 */
	private boolean isLabFormat(String fname, int numrows){
		if (!new File(fname).exists())
			return false;
		BufferedReader br = null;
		try{
			br = new BufferedReader(new FileReader(fname));
			String line = null; int i = 0;
			while( (line = br.readLine()) != null && 
					(numrows <= 0 || i++ < numrows)) {
				if (!line.startsWith("Gene1")){
					String[] toks = line.split("\t");
					if (toks.length != 5 || !isInt(toks[0]) || !isInt(toks[1]) 
					  || !isBool(toks[2]) || !isBool(toks[3]) || !isBool(toks[4]))
						return false;
				}
			}
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
	
	private boolean isInt(String s){
		try{
			Integer.parseInt(s);
			return true;
		}catch(NumberFormatException e){
			return false;
		}
	}
	
	private boolean isBool(String s){
		if (s.equals("1") || s.equals("0"))
			return true;
		return false;
	}

	String getLoadedNetworkFileName(){
		return networkField.getText();
	}
	
	boolean useLabFormat(){
		return !networkMatrix.isEnabled() && selectedFormat.equals(labFormat);
	}

	private ArrayList<AdjacencyMatrixDataSet> adjacencymatrixDataSets = new ArrayList<AdjacencyMatrixDataSet>();	

	void renameAdjMatrixToCombobox(AdjacencyMatrixDataSet adjDataSet,
			String oldName, String newName) {
		for (AdjacencyMatrixDataSet adjSet : adjacencymatrixDataSets) {
			if (adjSet == adjDataSet)
				adjSet.setLabel(newName);
		}
		adjModel.remove(oldName);
		adjModel.add(newName);
		refreshNetworkMatrixs();
	}
	
	void removeAdjMatrixToCombobox(AdjacencyMatrixDataSet adjDataSet) {
		try {
			adjacencymatrixDataSets.remove(adjDataSet);			
			adjModel.remove(adjModel.indexOf(adjDataSet.getDataSetName()));
			refreshNetworkMatrixs();			
			
		} catch (Exception ex) {
			log.error(ex.getMessage());
		}
	}
	
	void addAdjMatrixToCombobox(AdjacencyMatrixDataSet adjDataSet) {
		adjacencymatrixDataSets.add(adjDataSet);
		adjModel.add(adjDataSet.getDataSetName());
		refreshNetworkMatrixs();
	}
	
	private void refreshNetworkMatrixs(){
		networkMatrix.removeAllItems();
		networkMatrix.addItem(" ");
    	for(String setName: adjModel) {
    		networkMatrix.addItem(setName);
    	}		
	}
	
	String getSelectedAdjMatrix()
	{		 
		   return (String)networkMatrix.getSelectedItem();
	}
	
	AdjacencyMatrixDataSet getSelectedAdjSet(){
		if (!networkMatrix.isEnabled() && networkField.getText().length()==0) return null;
		return adjMatrix;
	}
	
	void setSelectedAdjMatrix(String datasetName)
	{		 
		networkMatrix.getModel().setSelectedItem(datasetName);
	}
	
	void clearAdjMatrixCombobox() {
		adjacencymatrixDataSets.clear();
		if (adjModel!=null)		adjModel.clear();
		refreshNetworkMatrixs();
	}	
	
	private boolean chooseNetworkFromSet(String setLabel){
		for (AdjacencyMatrixDataSet adjSet : adjacencymatrixDataSets) {
			if (adjSet.getLabel().equals(setLabel)){
				adjMatrix=adjSet;				
				return true;
			}
		}
		return false;
	}

}
