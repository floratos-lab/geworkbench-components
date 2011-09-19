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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.analysis.AbstractSaveableParameterPanel;
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
	
	private static final float PValueThresholdDefault = 0.05f;
	private JTextField pValueTextField = new JTextField(20);
	
	private JPanel selectionPanel = null;
	
	private JTextField networkField = new JTextField(20);
	private JTextField phenotypeField = new JTextField(20);
	private JTextField nullDataField = new JTextField(20);
	private JButton networkLoadButton = new JButton("Load");
	private JButton phenotypeLoadButton = new JButton("Load");
	private JButton nullDataLoadButton = new JButton("Load");
	private JCheckBox nullDataCheckbox = new JCheckBox("Use the existing null data", false);
	private JLabel pvalueLabel=new JLabel("P-value");
	private JLabel loadNullDataLabel=new JLabel("      Load null data      ");	
	
	private Phenotype phenotype=new Phenotype();
	private ArrayList<IdeaNetworkEdge> ideaNetwork;
	private ArrayList<String> nullDataList;
	private ArrayList<String> networkList=new ArrayList<String>();
	
	private static final String FROM_FILE = "From File";
	private static final String FROM_SETS = "From Set";	
	private static final String[] NETWORK_FROM = { FROM_FILE };
	private static final String[] PHENOTYPE_FROM = { FROM_FILE,	FROM_SETS };	
	static final String[] DEFAULT_SET = { " " };
	
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
	private DSMicroarraySet<DSMicroarray> maSet=null;
	private boolean firstRunFlag;
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geworkbench.analysis.AbstractSaveableParameterPanel#setParameters(java.util.Map)
	 *      Set inputed parameters to GUI.
	 */
	
	/**
	 * This is added to make the marker sets available.
	 */
	
	
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
		includeSets.setEnabled(false);
		excludeSets.setEnabled(false);
		includeField.setText("");
		excludeField.setText("");
		networkField.setText("");
		phenotypeField.setText("");
		
		//nullDataCheckbox.setVisible(false);
		//nullDataField.setVisible(false);
		//nullDataLoadButton.setVisible(false);
		//loadNullDataLabel.setVisible(false);
		
		pvalueLabel.setVisible(false);			//pvalue is temporarily off on GUI
		pValueTextField.setVisible(false);
		
		this.setLayout(new BorderLayout());		
		selectionPanel = new JPanel();
		selectionPanel.setLayout(new BorderLayout());
		this.add(selectionPanel, BorderLayout.CENTER);		
		
		{
			FormLayout layout = new FormLayout(
					"left:max(100dlu;pref), 10dlu, 100dlu, 10dlu, "
							+ "100dlu, 10dlu, 100dlu, 10dlu, 100dlu", "");
			DefaultFormBuilder builder = new DefaultFormBuilder(layout);
			builder.setDefaultDialogBorder();

			builder.appendSeparator("Inputs required");			
			builder.append("Load Network");
			builder.append(networkFrom);			
			builder.append(networkField);
			builder.append(networkLoadButton);						
			builder.nextLine();			
			
			builder.append("Define Phenotype");
			builder.append(phenotypeFrom);			
			builder.append(phenotypeField);
			builder.append(phenotypeLoadButton);
			builder.nextLine();			
			
			builder.append(includeLabel);
			builder.append(includeSets);
			builder.append(includeField);
			builder.append(" ");
			builder.nextLine();			
			
			builder.append(excludeLabel);
			builder.append(excludeSets);
			builder.append(excludeField);
			builder.append(" ");
			builder.nextLine();		
			
			nullDataCheckbox.setToolTipText("Only when gene expression, annotation, network, phenotype input data set are the same.");
			builder.append(nullDataCheckbox);
			nullDataCheckbox.addActionListener(new NullData_actionAdapter());
			builder.nextLine();			
			
			nullDataField.setEditable(false);			
			builder.append(loadNullDataLabel,
					nullDataField, nullDataLoadButton);
			builder.nextLine();			
			
			//builder.appendSeparator("Significance Threshold");	//pvalue is temporarily off	on GUI
			builder.append(pvalueLabel);
			if (pValueTextField == null)
				pValueTextField = new JTextField();
			pValueTextField.setText(Float.toString(PValueThresholdDefault));
			builder.append(pValueTextField);
			builder.nextLine();
			
			selectionPanel.add(builder.getPanel(), BorderLayout.CENTER);
		}
		
		phenotypeFrom.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {				
				
				String selected = (String) phenotypeFrom.getSelectedItem();
				if (StringUtils.equals(selected, FROM_FILE)) {
					includeSets.setSelectedIndex(0);
					includeSets.setEnabled(false);					
					excludeSets.setSelectedIndex(0);
					excludeSets.setEnabled(false);					
					phenotypeField.setEnabled(true);
					phenotypeLoadButton.setEnabled(true);
				} else {
					phenotypeField.setText("");
					phenotypeField.setEnabled(false);					
					includeSets.setEnabled(true);
					excludeSets.setEnabled(true);
					phenotypeLoadButton.setEnabled(false);
				}
			}
		});
		
		networkFrom.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				networkField.setText("");
			}
		});		
		
		 includeSets.addActionListener(new ActionListener() {
	    		public void actionPerformed(ActionEvent actionEvent) {
	    			String selectedLabel = (String) includeSets.getSelectedItem();
	    			if (!StringUtils.isEmpty(selectedLabel))
	    				if (!chooseArraysFromSet(selectedLabel, includeField)) {
	    					includeSets.setSelectedIndex(0);	    					
	    				}
	    		}
	    	});
		
		excludeSets.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				if(!excludeSets.isEnabled())return;
				String selectedLabel = (String) excludeSets.getSelectedItem();
				if (!StringUtils.isEmpty(selectedLabel))
					if (!chooseArraysFromSet(selectedLabel, excludeField)) {							
					}
			}
		});
		

		nullDataLoadButton.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				nullDataLoadPressed();
			}	
			
		});

		networkLoadButton.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				networkLoadPressed();
			}	
			
		});

		phenotypeLoadButton.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				phenotypeLoadPressed();
			}});


		networkField.setEnabled(true);
		networkField.setEditable(false);
		networkLoadButton.setEnabled(true);		
		phenotypeField.setEnabled(true);
		phenotypeField.setEditable(false);
		phenotypeLoadButton.setEnabled(true);
		nullDataField.setEnabled(false);
		nullDataField.setEditable(false);
		nullDataLoadButton.setEnabled(false);
		
		// define the 'update/refreshing'behavior of GUI components - see the
		// examples
		// they are (basically) all the same
		ParameterActionListener parameterActionListener = new ParameterActionListener(
				this);
		networkField.addActionListener(parameterActionListener);
		phenotypeField.addActionListener(parameterActionListener);
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
	
	public void setMicroarraySet(DSMicroarraySet<DSMicroarray> maSet){
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
			if (key.equals("phenotypeText")) {
				phenotypeField.setText((String)value);
				if(!phenotypeField.getText().equals("")){
					try{
						phenotype = new Phenotype(new File(phenotypeField.getText()));
						loadPhenoForPanel();
					}
					catch (FileNotFoundException e1) {
						e1.printStackTrace();
						phenotype = null;
					} catch (IOException e2) {
						e2.printStackTrace();
						phenotype = null;
					}
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
			if (key.equals("pValueText")) {
				pValueTextField.setText((String)value);
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
		parameters.put("phenotypeText", phenotypeField.getText());
		parameters.put("", (String) this.includeSets.getSelectedItem());
		parameters.put("includeText",includeField.getText());
		parameters.put("", (String) this.excludeSets.getSelectedItem());
		parameters.put("excludeText", excludeField.getText());
		parameters.put("nullDataText", nullDataField.getText());
		parameters.put("pValueText", pValueTextField.getText());
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
		histStr.append("\nPhenotype: "+phenotypeField.getText());
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
		return pValueTextField.getText();
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
	
	public void phenotypeLoadPressed(){
		JFileChooser fc = new JFileChooser(this.getLastDirectory());
		int returnVal = fc.showOpenDialog(IDEAPanel.this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {					
			try {
				String filename = fc.getSelectedFile().getAbsolutePath();			
				String filepath = fc.getCurrentDirectory().getCanonicalPath();
                setLastDirectory(filepath);
                File file=new File(filename);
                phenotype = new Phenotype(file); 
                phenotypeField.setText(filename);
				loadPhenoForPanel();
				
			} catch (IOException e) {				
				phenotype = null;
				includeField.setText("");
				excludeField.setText("");
				log.error(e);
			}					
		}	
	}
	
	private void loadPhenoForPanel(){		
		String str="";
		for(int i:phenotype.getIncludeList()){
			str+=maSet.get(i).getLabel()+",";
		}
		String s=str.substring(0,str.length()-1);
		includeField.setText(s);
		str="";
		for(int i:phenotype.getExcludeList()){
			str+=maSet.get(i).getLabel()+",";
		}
		s=str.substring(0,str.length()-1);
		excludeField.setText(s);
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
