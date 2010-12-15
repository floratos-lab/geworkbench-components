package org.geworkbench.components.idea;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
	private JTextField nullData =null;
	private JButton networkLoadButton = null;
	private JButton phenotypeLoadButton = null;
	private JButton nullDataLoadButton =null;
	private JCheckBox nullDataCheckbox;

	private Phenotype phenotype;
	private ArrayList<IdeaNetworkEdge> ideaNetwork;
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geworkbench.analysis.AbstractSaveableParameterPanel#setParameters(java.util.Map)
	 *      Set inputed parameters to GUI.
	 */
	@SuppressWarnings("unchecked")
	public void setParameters(Map<Serializable, Serializable> parameters) {
		Set<Map.Entry<Serializable, Serializable>> set = parameters.entrySet();
		for (Iterator<Map.Entry<Serializable, Serializable>> iterator = set
				.iterator(); iterator.hasNext();) {
			Map.Entry<Serializable, Serializable> parameter = iterator.next();
			Object key = parameter.getKey();
			Object value = parameter.getValue();

			// set the parameters on GUI based on the Map
			if (key.equals("network")) {
				ideaNetwork = (ArrayList<IdeaNetworkEdge>)value;
			}
			if (key.equals("phenotype")) {
				phenotype = (Phenotype)value;
			}
			if (key.equals("nullData")) {
				nullData.setText((String)value);
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
		parameters.put("network", ideaNetwork);
		parameters.put("phenotype", phenotype);
		parameters.put("nullData", nullData.getText());
		
		
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
	Boolean getUseNullData(){
		return nullDataCheckbox.isSelected();
	}
	
	public String getNullFileName() {
		return nullData.getText();
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
			
			nullData = new JTextField(20);
			nullData.setEditable(false);
			nullDataLoadButton = new JButton("Load");
			builder.append("Load null data      ",
					nullData, nullDataLoadButton);
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

		nullDataLoadButton.addActionListener(new LoadFileNameListener(
				nullData));

		networkLoadButton.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser();
				int returnVal = fc.showOpenDialog(IDEAPanel.this);

				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = fc.getSelectedFile();
					BufferedReader br;
					try {
						br = new BufferedReader(new FileReader(file));
						String line = br.readLine(); // skip the header line
						line = br.readLine();
						ideaNetwork = new ArrayList<IdeaNetworkEdge>();
						while(line!=null && line.trim().length()>0) {
							IdeaNetworkEdge edge = new IdeaNetworkEdge(line);
							ideaNetwork.add(edge);
							line = br.readLine();
						}
					} catch (FileNotFoundException e1) {
						e1.printStackTrace();
						ideaNetwork = null;
					} catch (IOException e2) {
						e2.printStackTrace();
						ideaNetwork = null;
					}
					networkField.setText(file.getAbsolutePath());			
					
				}
			}});

		phenotypeLoadButton.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser();
				int returnVal = fc.showOpenDialog(IDEAPanel.this);

				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = fc.getSelectedFile();
					try {
						phenotype = new Phenotype(file);
					} catch (IOException e1) {
						e1.printStackTrace();
						phenotype = null;
					}
					phenotypeField.setText(file.getAbsolutePath());			
					
				}
			}});

//		// this setting maps the source choice of 'From Set'
		networkField.setEnabled(true);
		networkField.setEditable(false);
		networkLoadButton.setEnabled(true);		
		phenotypeField.setEnabled(true);
		phenotypeField.setEditable(false);
		phenotypeLoadButton.setEnabled(true);
		nullData.setEnabled(false);
		nullData.setEditable(false);
		nullDataLoadButton.setEnabled(false);
		
		// define the 'update/refreshing'behavior of GUI components - see the
		// examples
		// they are (basically) all the same
		ParameterActionListener parameterActionListener = new ParameterActionListener(
				this);
		networkField.addActionListener(parameterActionListener);
		phenotypeField.addActionListener(parameterActionListener);
		nullData.addActionListener(parameterActionListener);
	}
	
	private class NullData_actionAdapter implements	
		java.awt.event.ActionListener {
			public void actionPerformed(ActionEvent e) {
				boolean nullDataOn=nullDataCheckbox.isSelected();
				if(nullDataOn){
					nullData.setEnabled(true);					
					nullDataLoadButton.setEnabled(true);
				}
				else{
					nullData.setEnabled(false);					
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
		
		if(parameters.get("loadedAnnotation")==null)
			parameters.put("loadedAnnotation", true);
		if(parameters.get("loadedAnnotationFile")==null)
			parameters.put("loadedAnnotationFile", "");
		if(parameters.get("alternateAnnotationFile")==null)
			parameters.put("alternateAnnotationFile", "");		
	}

	@Override
	public String getDataSetHistory() {
		StringBuilder histStr = new StringBuilder("");
		histStr.append("IDEA Analysis parameters:\n");
		histStr.append("----------------------------------------");
		histStr.append("\nNetwork: "+networkField.getText());
		histStr.append("\nPhenotype: "+phenotypeField.getText());
		if(nullDataCheckbox.isSelected())
				histStr.append("\nNull file: "+nullData.getText());
		histStr.append("\np-value: "+pValueTextField.getText());
		return histStr.toString();
	}

	public String getPvalue(){
		return pValueTextField.getText();
	}
	private class LoadFileNameListener implements ActionListener {
		private JTextField network = null;

		public LoadFileNameListener(JTextField network) {
			this.network = network;
		}

		public void actionPerformed(ActionEvent e) {
			JFileChooser fc = new JFileChooser();
			int returnVal = fc.showOpenDialog(IDEAPanel.this);

			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				String referenceFileName = file.getAbsolutePath();
				network.setText(referenceFileName);			
				
			} else {
				// if canceled, do nothing
			}
		}

	}
	
	
}
