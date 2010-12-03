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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.analysis.AbstractSaveableParameterPanel;
import org.geworkbench.bison.annotation.CSAnnotationContextManager;
import org.geworkbench.bison.annotation.DSAnnotationContext;
import org.geworkbench.bison.annotation.DSAnnotationContextManager;
import org.geworkbench.bison.datastructure.biocollections.microarrays.CSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.CSMicroarray;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
import org.geworkbench.events.listeners.ParameterActionListener;
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
	
	private JPanel selectionPanel = null;
	
	/* referent list = population; change list = study set */
	private static JComboBox networkSource = new JComboBox(new String[] {
			"From File"});
	private static JComboBox phenotypeSource = new JComboBox(new String[] {
			"From File"});
	private static JComboBox nullDataSource = new JComboBox(new String[] {
	"From File"});
	
	private JTextField network = null;
	private JTextField phenotype = null;
	private JTextField nullData =null;
	private JButton networkLoadButton = null;
	private JButton phenotypeLoadButton = null;
	private JButton nullDataLoadButton =null;
	private JCheckBox nullDataCheckbox;


	private CSMicroarraySet<CSMicroarray> dataset;
	
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
			if (key.equals("network")) {
				network.setText((String)value);
			}
			if (key.equals("phenotypeSource")) {
				phenotypeSource.setSelectedItem(value);
			}
			if (key.equals("nullDataSource")) {
				nullDataSource.setSelectedItem(value);
			}
			
			if (key.equals("phenotype")) {
				phenotype.setText((String)value);
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
		parameters.put("networkSource", (String)networkSource.getSelectedItem());		
		parameters.put("network", network.getText());
		parameters.put("phenotypeSource", (String)phenotypeSource.getSelectedItem());
		parameters.put("nullDataSource", (String)nullDataSource.getSelectedItem());
		parameters.put("phenotype", phenotype.getText());
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
	String getNetwork() {
		return network.getText();
	}
	Boolean getUseNullData(){
		return nullDataCheckbox.isSelected();
	}
	
	public String getNullFileName() {
		return nullData.getText();
	}	
	
	public String getPhenotype() {
			return phenotype.getText();
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
			network = new JTextField(20);
			networkLoadButton = new JButton("Load");
			builder.append("Load Network      ", networkSource,
					network, networkLoadButton);			
			builder.nextLine();
			
			phenotype = new JTextField(20);
			phenotypeLoadButton = new JButton("Load");
			builder.append("Define Phenotype", phenotypeSource,
					phenotype, phenotypeLoadButton);
			builder.nextLine();
			
			nullDataCheckbox = new JCheckBox("Use the existing null data", false);
			builder.append(nullDataCheckbox);
			nullDataCheckbox.addActionListener(new NullData_actionAdapter());
			builder.nextLine();			
			
			nullData = new JTextField(20);
			nullData.setEditable(false);
			nullDataLoadButton = new JButton("Load");
			builder.append("Load null data      ", nullDataSource,
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

		networkLoadButton.addActionListener(new LoadButtonListener(
				network));
		phenotypeLoadButton.addActionListener(new LoadButtonListener(
				phenotype));
		nullDataLoadButton.addActionListener(new LoadFileNameListener(
				nullData));

//		// this setting maps the source choice of 'From Set'
		network.setEnabled(true);
		network.setEditable(false);
		networkLoadButton.setEnabled(true);		
		phenotype.setEnabled(true);
		phenotype.setEditable(false);
		phenotypeLoadButton.setEnabled(true);
		nullData.setEnabled(false);
		nullData.setEditable(false);
		nullDataLoadButton.setEnabled(false);
		
		// define the 'update/refreshing'behavior of GUI components - see the
		// examples
		// they are (basically) all the same
		ParameterActionListener parameterActionListener = new ParameterActionListener(
				this);
		networkSource.addActionListener(parameterActionListener);		
		network.addActionListener(parameterActionListener);
		phenotypeSource.addActionListener(parameterActionListener);		
		phenotype.addActionListener(parameterActionListener);
		nullData.addActionListener(parameterActionListener);
	}
	
	private class NullData_actionAdapter implements	
		java.awt.event.ActionListener {
			public void actionPerformed(ActionEvent e) {
				boolean nullDataOn=nullDataCheckbox.isSelected();
				if(nullDataOn){
					nullDataSource.setEnabled(true);
					nullData.setEnabled(true);					
					nullDataLoadButton.setEnabled(true);
				}
				else{
					nullDataSource.setEnabled(false);
					nullData.setEnabled(false);					
					nullDataLoadButton.setEnabled(false);
				}
		}
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

	@Override
	public void fillDefaultValues(Map<Serializable, Serializable> parameters) {
		if(parameters.get("networkSource")==null)
			parameters.put("networkSource", (String)networkSource.getItemAt(0));		
		if(parameters.get("network")==null)
			parameters.put("network", "");
		if(parameters.get("phenotypeSource")==null)
			parameters.put("phenotypeSource", (String)phenotypeSource.getItemAt(0));		
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

	public void setDataset(CSMicroarraySet<CSMicroarray> d) {
		dataset = d;		
		network.setText(getAllGenesAsString());
		network.setEditable(false);		
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
		histStr.append("\nAnnotations: ");
		return histStr.toString();
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
