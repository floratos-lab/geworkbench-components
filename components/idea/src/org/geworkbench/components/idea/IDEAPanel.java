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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.analysis.AbstractSaveableParameterPanel;
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
	private JTextField pValueTextField = null;
	
	private JPanel selectionPanel = null;
	
	private JTextField networkField = null;
	private JTextField phenotypeField = null;
	private JTextField nullDataField =null;
	private JButton networkLoadButton = null;
	private JButton phenotypeLoadButton = null;
	private JButton nullDataLoadButton =null;
	private JCheckBox nullDataCheckbox;

	private Phenotype phenotype;
	private ArrayList<IdeaNetworkEdge> ideaNetwork;
	private String[] phenotypeAsString;
	private String[] nullDataAsString;
	private ArrayList<String> nullDataList;
	private ArrayList<String> networkList=new ArrayList<String>();
	
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
				try{
					phenotype = new Phenotype(new File(phenotypeField.getText()));
				}
				catch (FileNotFoundException e1) {
					e1.printStackTrace();
					phenotype = null;
				} catch (IOException e2) {
					e2.printStackTrace();
					phenotype = null;
				}			
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
		parameters.put("phenotypeText", phenotypeField.getText());
		parameters.put("nullDataText", nullDataField.getText());
		parameters.put("pValueText", pValueTextField.getText());
		parameters.put("nullDataCheckbox", nullDataCheckbox.isSelected());
		
		
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
		this.setLayout(new BorderLayout());		
		selectionPanel = new JPanel();
		selectionPanel.setLayout(new BorderLayout());
		this.add(selectionPanel, BorderLayout.CENTER);		
		
		{
			FormLayout layout = new FormLayout(
					"right:max(10dlu;pref), 3dlu, pref, 7dlu, "
							+ "left:max(10dlu;pref), 3dlu, pref, 7dlu, "
							+ "right:max(10dlu;pref), 3dlu, pref, 7dlu ", "");
			DefaultFormBuilder builder = new DefaultFormBuilder(layout);
			builder.setDefaultDialogBorder();

			builder.appendSeparator("Inputs required");			
			networkField = new JTextField(20);
			networkLoadButton = new JButton("Load");
			builder.append("Load Network      ",
					networkField, networkLoadButton);			
			builder.nextLine();
			
			phenotypeField = new JTextField(20);
			phenotypeLoadButton = new JButton("Load");
			builder.append("Define Phenotype",
					phenotypeField, phenotypeLoadButton);
			builder.nextLine();
			
			nullDataCheckbox = new JCheckBox("Use the existing null data", false);
			nullDataCheckbox.setToolTipText("Only when gene expression, annotation, network, phenotype files are the same.");
			builder.append(nullDataCheckbox);
			nullDataCheckbox.addActionListener(new NullData_actionAdapter());
			builder.nextLine();			
			
			nullDataField = new JTextField(20);
			nullDataField.setEditable(false);
			nullDataLoadButton = new JButton("Load");
			builder.append("Load null data      ",
					nullDataField, nullDataLoadButton);
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
		if(nullDataCheckbox.isSelected())
				histStr.append("\nNull file: "+nullDataField.getText());
		histStr.append("\np-value: "+pValueTextField.getText());
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
                phenotypeAsString= phenotype.getPhenotypeAsString();
				phenotypeField.setText(filename);			
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
				phenotype = null;
			} catch (IOException e2) {
				e2.printStackTrace();
				phenotype = null;
			}					
		}	
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
				int t=0;
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
		return phenotypeAsString;
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
	
	
	
}
