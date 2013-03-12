/**
 * 
 */
package org.geworkbench.components.lincs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Properties;
 
 
import java.awt.Component; 
import java.awt.GridLayout; 
import java.awt.Point; 
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.BorderLayout;
 
import java.io.BufferedWriter;
import java.io.File; 
import java.io.FileInputStream;
import java.io.FileWriter;

import javax.swing.BoxLayout; 
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.AbstractListModel; 
import javax.swing.JTable;
import javax.swing.ListModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener; 
import javax.swing.ListSelectionModel;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants; 
import javax.swing.JTextField;

 
import org.geworkbench.bison.datastructure.biocollections.AdjacencyMatrix;
import org.geworkbench.bison.datastructure.biocollections.AdjacencyMatrixDataSet;
import org.geworkbench.bison.datastructure.biocollections.AdjacencyMatrix.NodeType; 
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet; 
import org.geworkbench.builtin.projects.ProjectPanel; 
import org.geworkbench.engine.config.VisualPlugin;  
import org.geworkbench.parsers.TabDelimitedDataMatrixFileFormat;
import org.geworkbench.service.lincs.data.xsd.ExperimentalData;
import org.geworkbench.service.lincs.data.xsd.ComputationalData;
import org.geworkbench.util.FilePathnameUtils;
 
 
 
/**
 * @author zji
 * 
 */
public class LincsInterface extends JPanel implements VisualPlugin {

	private static final long serialVersionUID = 5478648745183665385L;

	private static final Log log = LogFactory.getLog(LincsInterface.class);

	private final static String[] experimentalColumnNames = { "Tissue Type",
			"Cell Line", "Drug 1", "Drug 2", "Assay Type",
			"Synergy Measurement Type", "Score", "P-value", "Titration Curve" };

	private final static String[] computationalColumnNames = { "Tissue Type",
			"Cell Line", "Drug 1", "Drug 2", "Similarity Algorithm", "Score",
			"P-value" };

	private final static String NETWORK = "Network";
	private final static String HEATMAP = "Heatmap";
	
	public static final String PROPERTIES_FILE = "conf/application.properties";
	public static final String LINCS_WEB_SERVICE_URL = "lincs_web_services_url";
	
	private JPanel queryTypePanel = new JPanel();
	private JPanel queryConditionPanel1 = new JPanel();
	private JPanel queryConditionPanel2 = new JPanel();

	private JPanel queryCommandPanel = new JPanel();
	private JPanel queryResultPanel = new JPanel();
	private JPanel resultProcessingPanel = new JPanel();
	private JRadioButton experimental = new JRadioButton("Experimental");
	private JRadioButton computational = new JRadioButton("Computational");
	private FilteredJList drug1Box = new FilteredJList();
	private JTextField drug1Search = drug1Box.getFilterField();
	private JList tissueTypeBox = new JList();
	private Lincs lincs = null;
	private JList cellLineBox = null;
	private JList drug2Box = null;
	private JList assayTypeBox = null;
	private JList synergyMeasurementTypeBox = null;
	private JList similarityAlgorithmTypeBox = null;
	private JCheckBox onlyTitration = null;
	private JCheckBox maxResult = null;
	private JTextField maxResultNumber = null;
	private JButton searchButton = null;
	private JButton resetButton = null;
	private JCheckBox colorGradient = null;
	private TableViewer resultTable = null;
	private JComboBox plotOptions = null;
 
 	private static final String lincsDir = FilePathnameUtils.getUserSettingDirectoryPath()
 			+ "lincs" + FilePathnameUtils.FILE_SEPARATOR;
  
	public LincsInterface() {
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

		add(queryTypePanel);
		add(queryConditionPanel1);
		add(queryConditionPanel2);

		add(queryCommandPanel);
	 

		experimental.setSelected(true);

		ButtonGroup group = new ButtonGroup();
		group.add(experimental);
		group.add(computational);
		queryTypePanel.add(new JLabel("Query Type"));
		queryTypePanel.add(experimental);
		queryTypePanel.add(computational);
        String url = getLincsWsdlUrl();
		//String url = "http://156.145.28.209:8080/axis2/services/LincsService?wsdl";
		lincs = new Lincs(url,null, null);

		queryConditionPanel1.setLayout(new GridLayout(2, 7));

		final JLabel tissueTypeLabel = new JLabel("Tissue Type");
		final JLabel cellLineLabel = new JLabel("Cell Line");
		final JLabel drug1Label = new JLabel("Drug 1");
		final JLabel drug2Label = new JLabel("Drug 2");
		final JLabel assayTypeLabel = new JLabel("Assay Type");
		final JLabel synergyMeasurementLabel = new JLabel(
				"Synergy Measurement Type");
		final JLabel similarityAlgorithmLabel = new JLabel(
				"Similarity Algorithm");
		final JLabel blankLabel = new JLabel("");

		queryConditionPanel1.add(new JLabel(""));
		queryConditionPanel1.add(new JLabel(""));
		queryConditionPanel1.add(drug1Label);
		queryConditionPanel1.add(new JLabel(""));
		queryConditionPanel1.add(new JLabel(""));
		queryConditionPanel1.add(new JLabel(""));
		queryConditionPanel1.add(blankLabel);

		queryConditionPanel1.add(tissueTypeLabel);
		queryConditionPanel1.add(cellLineLabel);
		queryConditionPanel1.add(drug1Search);
		queryConditionPanel1.add(drug2Label);
		queryConditionPanel1.add(assayTypeLabel);
		queryConditionPanel1.add(synergyMeasurementLabel);
		queryConditionPanel1.add(new JLabel(""));

		queryConditionPanel2.setLayout(new GridLayout(1, 7));

		List<String> tissueTypeList = null;
		List<String> drug1List = null;
		List<String> assayTypeList = null;
		List<String> synergyMeasuremetnTypeList = null;
		List<String> similarityAlgorithmList = null;
		try {
			tissueTypeList = addAll(lincs.getAllTissueNames());
			drug1List = addAll(lincs.GetCompound1NamesFromExperimental(null, null));
			assayTypeList = addAll(lincs.getAllAssayTypeNames());
			synergyMeasuremetnTypeList = addAll(lincs.getAllMeasurementTypeNames());
			similarityAlgorithmList = addAll(lincs.getALLSimilarAlgorithmNames());
		} catch (Exception ex) {
			log.error(ex.getMessage());
		}

		final JScrollPane tissueTypeBoxPanel = buildJListPanel(tissueTypeList,
				tissueTypeBox);

		cellLineBox = new JList();
		final JScrollPane cellLineBoxPanel = buildJListPanel(null, cellLineBox);
		cellLineBox.setEnabled(false);

		final JScrollPane drug1BoxPanel = buildFilterJListPanel(drug1List,
				drug1Box);

		drug2Box = new JList();
		final JScrollPane drug2BoxPanel = buildJListPanel(null, drug2Box);
		drug2Box.setEnabled(false);

		assayTypeBox = new JList();
		final JScrollPane assayTypeBoxPanel = buildJListPanel(assayTypeList,
				assayTypeBox);

		synergyMeasurementTypeBox = new JList();
		final JScrollPane synergyMeasuremetnTypeBoxPanel = buildJListPanel(
				synergyMeasuremetnTypeList, synergyMeasurementTypeBox);

		similarityAlgorithmTypeBox = new JList();

		final JScrollPane similarityAlgorithmTypeBoxPanel = buildJListPanel(
				similarityAlgorithmList, similarityAlgorithmTypeBox);

		onlyTitration = new JCheckBox("Only with titration");
		queryConditionPanel2.add(tissueTypeBoxPanel);
		queryConditionPanel2.add(cellLineBoxPanel);
		queryConditionPanel2.add(drug1BoxPanel);
		queryConditionPanel2.add(drug2BoxPanel);
		queryConditionPanel2.add(assayTypeBoxPanel);
		queryConditionPanel2.add(synergyMeasuremetnTypeBoxPanel);
		queryConditionPanel2.add(onlyTitration);

		// dynamic dependency parts
		tissueTypeBox.addListSelectionListener(new ListSelectionListener() {

			public void valueChanged(ListSelectionEvent e) {

				if (e.getValueIsAdjusting()) {
					List<String> selectedTissueList = getSelectedValues(tissueTypeBox);
					List<String> cellLineDataList = null;
					List<String> drug1DataList = null;
					try {
						cellLineDataList = addAll(lincs
								.getAllCellLineNamesForTissueTypes(selectedTissueList));

						if (experimental.isSelected() == true)
							drug1DataList = addAll(lincs
									.GetCompound1NamesFromExperimental(
											selectedTissueList, null));
						else
							drug1DataList = addAll(lincs
									.getCompound1NamesFromComputational(
											selectedTissueList, null));
					} catch (Exception ex) {
						log.error(ex.getMessage());
					}
					cellLineBox.setEnabled(true);
					cellLineBox.setModel(new LincsListModel(cellLineDataList));
					cellLineBox.clearSelection();
					drug1Box.removeAllItems();
					for (int i = 0; i < drug1DataList.size(); i++)
						drug1Box.addItem(drug1DataList.get(i));
					drug1Box.clearSelection();
					drug2Box.clearSelection();
					drug2Box.setModel(new LincsListModel(null));
					drug2Box.setEnabled(false);

				}
			}

		});

		// dynamic dependency parts
		cellLineBox.addListSelectionListener(new ListSelectionListener() {

			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting()) {
					List<String> selectedTissueList = getSelectedValues(tissueTypeBox);
					List<String> selectedCellLineList = getSelectedValues(cellLineBox);

					List<String> drug1DataList = null;
					try {
						if (experimental.isSelected() == true)
							drug1DataList = addAll(lincs
									.GetCompound1NamesFromExperimental(
											selectedTissueList,
											selectedCellLineList));
						else
							drug1DataList = addAll(lincs
									.getCompound1NamesFromComputational(
											selectedTissueList,
											selectedCellLineList));
					} catch (Exception ex) {
						log.error(ex.getMessage());
					}
					drug1Box.removeAllItems();
					for (int i = 0; i < drug1DataList.size(); i++)
						drug1Box.addItem(drug1DataList.get(i));
					drug1Box.ensureIndexIsVisible(0);
					drug1Box.clearSelection();
					drug2Box.clearSelection();
					drug2Box.setModel(new LincsListModel(null));
					drug2Box.setEnabled(false);
				}
			}

		});

		// dynamic dependency parts
		drug1Box.addListSelectionListener(new ListSelectionListener() {

			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting()) {
					List<String> selectedTissueList = getSelectedValues(tissueTypeBox);
					List<String> selectedCellLineList = getSelectedValues(cellLineBox);
					List<String> selectedDrug1List = getSelectedValues(drug1Box);

					List<String> drug2DataList = null;
					try {

						if (experimental.isSelected() == true)
							drug2DataList = addAll(lincs
									.GetCompound2NamesFromExperimental(
											selectedTissueList,
											selectedCellLineList,
											selectedDrug1List));
						else
							drug2DataList = addAll(lincs
									.getCompound2NamesFromComputational(
											selectedTissueList,
											selectedCellLineList,
											selectedDrug1List));
					} catch (Exception ex) {
						log.error(ex.getMessage());
					}
					drug2Box.setModel(new LincsListModel(drug2DataList));
					drug2Box.setEnabled(true);
					drug2Box.ensureIndexIsVisible(0);
				}
			}

		});

		maxResult = new JCheckBox("Max results");
		maxResultNumber = new JTextField("10", 10);
		searchButton = new JButton("Search");
		resetButton = new JButton("Reset");
		colorGradient = new JCheckBox("Color gradient for Score");
		queryCommandPanel.add(maxResult);
		queryCommandPanel.add(maxResultNumber);
		queryCommandPanel.add(searchButton);
		queryCommandPanel.add(resetButton);
		queryCommandPanel.add(colorGradient);
		resultTable = new TableViewer(experimentalColumnNames, null);
		add(resultTable);
		add(resultProcessingPanel);	 
		queryResultPanel.setLayout(new BorderLayout());
		//queryResultPanel.add(new JScrollPane(resultTable), BorderLayout.CENTER); 

		plotOptions = new JComboBox(new String[] { HEATMAP,
				NETWORK });
		JButton plotButton = new JButton("Plot");
		JButton exportButton = new JButton("Export");
		resultProcessingPanel.add(new JLabel("Plot options:"));
		resultProcessingPanel.add(plotOptions);
		resultProcessingPanel.add(plotButton);
		resultProcessingPanel.add(exportButton);

		plotButton.addActionListener(plotListener);
		exportButton.addActionListener(dummyListener);

		computational.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				queryConditionPanel1.remove(blankLabel);
				queryConditionPanel1.remove(assayTypeLabel);
				queryConditionPanel1.remove(synergyMeasurementLabel);
				queryConditionPanel2.remove(assayTypeBoxPanel);
				queryConditionPanel2.remove(synergyMeasuremetnTypeBoxPanel);
				queryConditionPanel1.add(similarityAlgorithmLabel, 10);
				queryConditionPanel2.add(similarityAlgorithmTypeBoxPanel, 4);

				queryConditionPanel1.updateUI();
				queryConditionPanel2.updateUI();		 
				onlyTitration.setEnabled(false);
				reset();

			}
		});

		experimental.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				queryConditionPanel1.remove(similarityAlgorithmLabel);
				queryConditionPanel2.remove(similarityAlgorithmTypeBoxPanel);
				queryConditionPanel1.add(blankLabel, 6);
				queryConditionPanel1.add(assayTypeLabel, 11);
				queryConditionPanel1.add(synergyMeasurementLabel, 12);
				queryConditionPanel2.add(assayTypeBoxPanel, 4);
				queryConditionPanel2.add(synergyMeasuremetnTypeBoxPanel, 5);
				queryConditionPanel1.updateUI();
				queryConditionPanel2.updateUI();
				onlyTitration.setEnabled(true);
				reset();
			}
		});

		searchButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				int rowLimit = 0;

				List<String> tissueTypes = getSelectedValues(tissueTypeBox);
				List<String> cellLineNames = getSelectedValues(cellLineBox);
				List<String> drug1Names = getSelectedValues(drug1Box);
				List<String> drug2Names = getSelectedValues(drug2Box);

				if ((tissueTypes == null || tissueTypes.isEmpty())
						&& (cellLineNames == null || cellLineNames.isEmpty())
						&& (drug1Names == null || drug1Names.isEmpty())) {

					JOptionPane.showMessageDialog(null,
							"Please select Tissue Type or Cell Line or Drug1.");
					return;

				}
				if (maxResult.isSelected()) {
					try {
						rowLimit = new Integer(maxResultNumber.getText().trim())
								.intValue();
					} catch (NumberFormatException nbe) {
						JOptionPane.showMessageDialog(null,
								"Please enter a number.");
						maxResultNumber.requestFocus();
						return;
					}
				}

				try {
					if (experimental.isSelected()) {
						List<String> assayTypes = getSelectedValues(assayTypeBox);
						List<String> measurementTypes = getSelectedValues(synergyMeasurementTypeBox);
						List<ExperimentalData> dataList = lincs
								.getExperimentalData(tissueTypes,
										cellLineNames, drug1Names, drug2Names,
										measurementTypes, assayTypes,
										onlyTitration.isSelected(), rowLimit);
						Object[][] objects = convertExperimentalData(dataList);
						 
						updateResultTable(experimentalColumnNames, objects);
						 
					 
					} else {
						List<String> similarityAlgorithmTypes = getSelectedValues(similarityAlgorithmTypeBox);
						List<ComputationalData> dataList = lincs
								.getComputationalData(tissueTypes,
										cellLineNames, drug1Names, drug2Names,
										similarityAlgorithmTypes, rowLimit);
						Object[][] objects = convertComputationalData(dataList);
						 
						updateResultTable(computationalColumnNames, objects);
					 
					 
					}
				} catch (Exception ex) {
					log.error(ex.getMessage());
				}
			}

		});

		resetButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				reset();
			}

		});	

		

	}

	@Override
	public Component getComponent() {
		return this;
	}

	private ActionListener dummyListener = new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
			JOptionPane.showMessageDialog(null, "nothing implemented");
		}
	};
	

	private void reset() {
		tissueTypeBox.clearSelection();
		cellLineBox.clearSelection();
		drug1Box.clearSelection();
		drug1Box.getFilterField().setText("");
		drug2Box.clearSelection();
		assayTypeBox.clearSelection();
		synergyMeasurementTypeBox.clearSelection();
		similarityAlgorithmTypeBox.clearSelection();
		onlyTitration.setSelected(false);
		maxResult.setSelected(false);
		maxResultNumber.setText("10");
		cellLineBox.setModel(new LincsListModel(null));
		drug2Box.setModel(new LincsListModel(null));
		List<String> drug1DataList = null;
		try {
			if (experimental.isSelected() == true) {
				drug1DataList = addAll(lincs.GetCompound1NamesFromExperimental(null, null));
			 
				updateResultTable(experimentalColumnNames, null);
			 
			} else {
				drug1DataList = addAll(lincs
						.getCompound1NamesFromComputational(null, null));
				updateResultTable(computationalColumnNames, null);
				 
			}
			drug1Box.removeAllItems();
			for (int i = 0; i < drug1DataList.size(); i++)
				drug1Box.addItem(drug1DataList.get(i));
		} catch (Exception ex) {
			log.error(ex.getMessage());
		}

	}

	private class LincsListModel extends AbstractListModel implements ListModel {

		private static final long serialVersionUID = -6604644155965604030L;
		List<String> list = null;

		public LincsListModel(List<String> list) {
			this.list = list;
		}

		public Object getElementAt(int index) {
			if (list != null)
				return list.get(index);
			else
				return null;
		}

		public int getSize() {
			if (list != null)
				return list.size();
			return 0;
		}
	}

	private JScrollPane buildJListPanel(List<String> dataList, JList aJlist) {

		aJlist.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		aJlist.setModel(new LincsListModel(dataList));
		JScrollPane jScrollPane1 = new javax.swing.JScrollPane(aJlist,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

		jScrollPane1.setPreferredSize(new java.awt.Dimension(200, 70));

		return jScrollPane1;

	}

	private JScrollPane buildFilterJListPanel(List<String> dataList,
			FilteredJList filteredJList) {

		filteredJList
				.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		for (int i = 0; i < dataList.size(); i++)
			filteredJList.addItem(dataList.get(i));
		JScrollPane jScrollPane1 = new javax.swing.JScrollPane(filteredJList,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

		jScrollPane1.setPreferredSize(new java.awt.Dimension(200, 70));

		return jScrollPane1;

	}

	private List<String> getSelectedValues(JList aJList) {
		Object[] selectedValues = (Object[]) aJList.getSelectedValues();
		List<String> selectedList = null;
		if (selectedValues != null && selectedValues.length > 0
				&& !selectedValues[0].toString().equalsIgnoreCase("All")) {
			selectedList = new ArrayList<String>();
			for (int i = 0; i < selectedValues.length; i++)
				selectedList.add(selectedValues[i].toString());
		}
		return selectedList;
	}

	private Object[][] convertExperimentalData(List<ExperimentalData> dataList) {
		if (dataList == null || dataList.size() <= 0)
			return null;
		Object[][] objects = new Object[dataList.size()][9];
		for (int i = 0; i < dataList.size(); i++) {
			objects[i][0] = dataList.get(i).getTissueType();
			if (objects[i][0] == null)
				objects[i][0] = "";
			objects[i][1] = dataList.get(i).getCellLineName();
			if (objects[i][1] == null)
				objects[i][1] = "";
			objects[i][2] = dataList.get(i).getCompound1();
			objects[i][3] = dataList.get(i).getCompound2();			 
			objects[i][4] = dataList.get(i).getAssayType();	
			if (objects[i][4] == null)
				objects[i][4] = "";
			objects[i][5] = dataList.get(i).getMeasurementType();
			if (objects[i][5] == null)
				objects[i][5] = "";
			objects[i][6] = dataList.get(i).getScore();
			objects[i][7] = dataList.get(i).getPvalue();
			objects[i][8] = "view";

		}

		return objects;
	}

	private Object[][] convertComputationalData(List<ComputationalData> dataList) {
		if (dataList == null || dataList.size() <= 0)
			return null;
		Object[][] objects = new Object[dataList.size()][7];
		for (int i = 0; i < dataList.size(); i++) {
			objects[i][0] = dataList.get(i).getTissueType();
			if (objects[i][0] == null)
				objects[i][0] = "";
			objects[i][1] = dataList.get(i).getCellLineName();
			if (objects[i][1] == null)
				objects[i][1] = "";
			objects[i][2] = dataList.get(i).getCompound1();
			objects[i][3] = dataList.get(i).getCompound2();
			objects[i][4] = dataList.get(i).getSimilarityAlgorithm();
			if (objects[i][4] == null)
				objects[i][4] = "";
			objects[i][6] = dataList.get(i).getPvalue();
			objects[i][7] = dataList.get(i).getScore();

		}

		return objects;
	}	
	
	private void updateResultTable(String[] columnNames,  Object[][] data)
	{
		remove(resultTable);
		remove(resultProcessingPanel);
		resultTable = new TableViewer(columnNames, data);
		add(resultTable);
		add(resultTable);
		add(resultProcessingPanel);
		resultTable.updateUI();
	}
	
	private List<String> addAll(List<String> list)
	{        
		if (list != null && list.size() > 0)
				   list.add(0, "All");
		return list;
	}
	
	

	private ActionListener plotListener = new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
			if (plotOptions.getSelectedItem().toString().equals(HEATMAP))
			{
				createHeatmap();
			}else if (plotOptions.getSelectedItem().toString().equals(NETWORK))
			{
				createNetwork();
			}
		}
	};
	
	private void createNetwork()
	{
		AdjacencyMatrix matrix = new AdjacencyMatrix("Adjacency Matrix");
		AdjacencyMatrixDataSet adjacencyMatrixdataSet = null;
		Object[][] data = resultTable.getData();
	        
		if (data == null | data.length == 0)
		{	JOptionPane.showMessageDialog(null,
					"No interactions exist in the current database.",
					"Empty Set", JOptionPane.ERROR_MESSAGE);
		    return ;
		}
			
        int drug1Index = resultTable.getHeaderNameIndex("Drug 1");
        int drug2Index = resultTable.getHeaderNameIndex("Drug 2");
        int scoreIndex = resultTable.getHeaderNameIndex("Score");
		for (int i=0; i<data.length; i++) {
			AdjacencyMatrix.Node node1, node2;
			 
		    
				node1 = new AdjacencyMatrix.Node(NodeType.STRING,
						data[i][drug1Index].toString());
				node2 = new AdjacencyMatrix.Node(NodeType.STRING,
						data[i][drug2Index].toString());
				matrix.add(node1, node2, new Float(data[i][scoreIndex].toString()).floatValue());	
				 	 
			 
		}  
		 
		 
		adjacencyMatrixdataSet = new AdjacencyMatrixDataSet(matrix,
					-1000000000, "Adjacency Matrix", "Lincs", null);

		 
		ProjectPanel.getInstance().addDataSetNode(adjacencyMatrixdataSet);
	  

	}


	private void convertToTabDelimitedDataMatrix(String verticalDataType, String horizontalDataType, File tfaFile)
	{
		List<String> verticalNames = new ArrayList<String>();
		List<String> horizontalNames = new ArrayList<String>();
		
		Object[][] data = resultTable.getData();		
		int verticalDataTypeIndex = resultTable.getHeaderNameIndex("Drug 1");
	    int horizontalDataTypeIndex = resultTable.getHeaderNameIndex("Drug 2");
	    int scoreIndex = resultTable.getHeaderNameIndex("Score");
	    for (int i=0; i<data.length; i++) {
			String verticalName, horizontalName;		    
			verticalName = data[i][verticalDataTypeIndex].toString();
			horizontalName = data[i][horizontalDataTypeIndex].toString();
			if (!verticalNames.contains(verticalName))
				verticalNames.add(verticalName);
			if (!horizontalNames.contains(horizontalName))
				horizontalNames.add(horizontalName);
	    }
	    float[][] scoreMatrix = new float[verticalNames.size()][horizontalNames.size()];
	    Collections.sort(verticalNames);
	    Collections.sort(horizontalNames);
	    for (int i=0; i<data.length; i++) {
	    	String verticalName = data[i][verticalDataTypeIndex].toString();
			String horizontalName = data[i][horizontalDataTypeIndex].toString();
	    	 
			scoreMatrix[verticalNames.indexOf(verticalName)][horizontalNames.indexOf(horizontalName)] = 
				new Float(data[i][scoreIndex].toString()).floatValue();
			 
		}
	    
	    BufferedWriter bw = null;
	 
		try{
			 
			bw = new BufferedWriter(new FileWriter(tfaFile));
			 
			bw.write("name");
			for (int i=0; i<horizontalNames.size(); i++)
			bw.write("\t" + horizontalNames.get(i));
			bw.newLine();
			for(int i=0; i<verticalNames.size(); i++){				 
			   bw.write(verticalNames.get(i) + "\t");
			   for(int j=0; j<horizontalNames.size(); j++)
				   bw.write(scoreMatrix[i][j] + "\t");
			   bw.newLine();
				 
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			try{
				 
				if (bw!=null) bw.close();
			}catch(Exception e){
				e.printStackTrace();
			}
		}		 
		tfaFile.deleteOnExit();
	}
	
	 
	
	private  void createHeatmap()
	{
		 String tfaFname = lincsDir + "lincs_tfa.txt";
		 File tfaFile = new File(tfaFname);
		 convertToTabDelimitedDataMatrix("drug 1", "drug 2",tfaFile );
			DSMicroarraySet dataSet = null;
		try{		 
			dataSet = (DSMicroarraySet)new TabDelimitedDataMatrixFileFormat().getDataFileSkipAnnotation(tfaFile);
			ProjectPanel.getInstance().addProcessedMaSet(dataSet);
			 
		}catch(Exception e){
			e.printStackTrace();
		}

	}
	 
   private String getLincsWsdlUrl() {
	        String lincsUrl = null;
	        Properties lincsProp = new Properties();
			try {
				lincsProp
						.load(new FileInputStream(PROPERTIES_FILE));
			 
				lincsUrl = lincsProp
				.getProperty(LINCS_WEB_SERVICE_URL);
				
				 
			} catch (java.io.IOException ie) {
				log.error(ie.getMessage());
			} catch (Exception e) {
				log.error(e.getMessage());
			}
			if (lincsUrl == null
					|| lincsUrl.trim().equals("")) {

				lincsUrl = "http://afdev.c2b2.columbia.edu:9090/axis2/services/LincsService?wsdl";
			}
			return lincsUrl;
		}
	
	
	

}
