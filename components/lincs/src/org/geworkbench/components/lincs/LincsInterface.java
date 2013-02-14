/**
 * 
 */
package org.geworkbench.components.lincs;

import java.util.List;
import java.util.ArrayList;
import java.awt.BorderLayout;
import java.awt.Component; 
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
 

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.AbstractListModel;
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
import javax.swing.JTable;
import javax.swing.JTextField;
 

import org.geworkbench.engine.config.VisualPlugin;

/**
 * @author zji
 * 
 */
public class LincsInterface extends JPanel implements VisualPlugin {

	private static final long serialVersionUID = 5478648745183665385L;

	private final static String[] experimentalColumnNames = { "Tissue Type",
			"Cell Line", "Drug 1", "Drug 2", "Assay Type",
			"Synergy Measurement Type", "Score", "P-value", "Titration Curve" };

	private final static String[] computationalColumnNames = { "Tissue Type",
			"Cell Line", "Drug 1", "Drug 2", "Similarity Algorithm", "Score",
			"P-value" };

	/*
	 * final JList tissueTypeBox = null; final JList cellLineBox = null; final
	 * JList drug1Box = null; final JList drug2Box = null; final JList
	 * assayTypeBox = null; final JScrollPane assayTypeBoxPanel = null; final
	 * JList synergyMeasuremetnTypeBox =null; final JScrollPane
	 * synergyMeasuremetnTypeBoxPanel = null; final JList
	 * similarityAlgorithmTypeBox = null;
	 * 
	 * final JCheckBox onlyTitration = null;
	 * 
	 * final JPanel queryTypePanel = null; final JPanel queryConditionPanel1 =
	 * null; final JPanel queryConditionPanel2 = null;
	 * 
	 * final JPanel queryCommandPanel = null; final JPanel queryResultPanel =
	 * null; final JPanel resultProcessingPanel = null;
	 */

	public LincsInterface() {
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

		final JPanel queryTypePanel = new JPanel();
		final JPanel queryConditionPanel1 = new JPanel();
		final JPanel queryConditionPanel2 = new JPanel();

		final JPanel queryCommandPanel = new JPanel();
		final JPanel queryResultPanel = new JPanel();
		final JPanel resultProcessingPanel = new JPanel();

		add(queryTypePanel);
		add(queryConditionPanel1);
		add(queryConditionPanel2);

		add(queryCommandPanel);
		add(queryResultPanel);
		add(resultProcessingPanel);

		final JRadioButton experimental = new JRadioButton("Experimental");
		experimental.setSelected(true);
		final JRadioButton computational = new JRadioButton("Computational");
		ButtonGroup group = new ButtonGroup();
		group.add(experimental);
		group.add(computational);
		queryTypePanel.add(new JLabel("Query Type"));
		queryTypePanel.add(experimental);
		queryTypePanel.add(computational);

		final Lincs lincs = new Lincs(null, null, null);

		queryConditionPanel1.setLayout(new GridLayout(2, 7));
		
		final FilteredJList drug1Box = new FilteredJList();
		final JTextField drug1Search = drug1Box.getFilterField();	 
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

		final JList tissueTypeBox = new JList();
		List<String> tissueTypeList = lincs.getAllTissueNames();
		final JScrollPane tissueTypeBoxPanel = buildJListPanel(tissueTypeList,
				tissueTypeBox);

		final JList cellLineBox = new JList();
		final JScrollPane cellLineBoxPanel = buildJListPanel(null, cellLineBox);
		cellLineBox.setEnabled(false);

		 
		List<String> drug1List = lincs
				.GetDrug1NamesFromExperimental(null, null);
		final JScrollPane drug1BoxPanel = buildFilterJListPanel(drug1List, drug1Box);
	 
		final JList drug2Box = new JList();
		final JScrollPane drug2BoxPanel = buildJListPanel(null, drug2Box);
		drug2Box.setEnabled(false);

		final JList assayTypeBox = new JList();
		List<String> assayTypeList = lincs.getAllAssayTypeNames();
		final JScrollPane assayTypeBoxPanel = buildJListPanel(assayTypeList,
				assayTypeBox);

		final JList synergyMeasuremetnTypeBox = new JList();
		List<String> synergyMeasuremetnTypeList = lincs
				.getAllMeasurementTypeNames();
		final JScrollPane synergyMeasuremetnTypeBoxPanel = buildJListPanel(
				synergyMeasuremetnTypeList, synergyMeasuremetnTypeBox);

		final JList similarityAlgorithmTypeBox = new JList();
		List<String> similarityAlgorithmList = lincs
				.getALLSimilarAlgorithmNames();
		final JScrollPane similarityAlgorithmTypeBoxPanel = buildJListPanel(
				similarityAlgorithmList, similarityAlgorithmTypeBox);

		final JCheckBox onlyTitration = new JCheckBox("Only with titration");
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
					List<String> cellLineDataList = lincs
							.getAllCellLineNamesForTissueTypes(selectedTissueList);
					cellLineBox.setEnabled(true);
					cellLineBox.setModel(new LincsListModel(cellLineDataList));
					cellLineBox.clearSelection();
					List<String> drug1DataList = null;
					if (experimental.isSelected() == true)
						drug1DataList = lincs.GetDrug1NamesFromExperimental(
								selectedTissueList, null);
					else
						drug1DataList = lincs.getDrug1NamesFromComputational(
								selectedTissueList, null);
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
					if (experimental.isSelected() == true)
						drug1DataList = lincs.GetDrug1NamesFromExperimental(
								selectedTissueList, selectedCellLineList);
					else
						drug1DataList = lincs.getDrug1NamesFromComputational(
								selectedTissueList, selectedCellLineList);
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
		drug1Box.addListSelectionListener(new ListSelectionListener() {

			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting()) {
					List<String> selectedTissueList = getSelectedValues(tissueTypeBox);
					List<String> selectedCellLineList = getSelectedValues(cellLineBox);
					List<String> selectedDrug1List = getSelectedValues(drug1Box);

					List<String> drug2DataList = null;
					if (experimental.isSelected() == true)
						drug2DataList = lincs.GetDrug2NamesFromExperimental(
								selectedTissueList, selectedCellLineList,
								selectedDrug1List);
					else
						drug2DataList = lincs.getDrug2NamesFromComputational(
								selectedTissueList, selectedCellLineList,
								selectedDrug1List);

					drug2Box.setModel(new LincsListModel(drug2DataList));
					drug2Box.setEnabled(true);
				}
			}

		});

		JCheckBox maxResult = new JCheckBox("Max results");
		JTextField maxResultNumber = new JTextField("10", 10);
		JButton searchButton = new JButton("Search");
		JButton resetButton = new JButton("Reset");
		JCheckBox colorGradient = new JCheckBox("Color gradient for Score");
		queryCommandPanel.add(maxResult);
		queryCommandPanel.add(maxResultNumber);
		queryCommandPanel.add(searchButton);
		queryCommandPanel.add(resetButton);
		queryCommandPanel.add(colorGradient);

		searchButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				boolean b = experimental.isSelected();
				String queryType = "experimental";
				if (!b)
					queryType = "computational";
				Object tissueType = tissueTypeBox.getSelectedValue();
				Object cellLine = cellLineBox.getSelectedValue();

				JOptionPane.showMessageDialog(null,
						"not implemented\nquery type=" + queryType
								+ "\ntissue type=" + tissueType
								+ "\ncell line=" + cellLine,
						"Placeholder - not implemented",
						JOptionPane.INFORMATION_MESSAGE);
			}

		});
		resetButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				tissueTypeBox.clearSelection();
				cellLineBox.clearSelection();
				drug1Box.clearSelection();
				drug2Box.clearSelection();
				assayTypeBox.clearSelection();
				synergyMeasuremetnTypeBox.clearSelection();
				similarityAlgorithmTypeBox.clearSelection();
				onlyTitration.setSelected(false);
				cellLineBox.setModel(new LincsListModel(null));
				drug2Box.setModel(new LincsListModel(null));
				List<String> drug1DataList = null;
				if (experimental.isSelected() == true)
					drug1DataList = lincs.GetDrug1NamesFromExperimental(null,
							null);
				else
					drug1DataList = lincs.getDrug1NamesFromComputational(null,
							null);
				for (int i = 0; i < drug1DataList.size(); i++)
					drug1Box.addItem(drug1DataList.get(i));
				 

			}

		});

		final JTable resultTable = new JTable(new QueryResultTableModel(
				experimentalColumnNames, null));
		queryResultPanel.setLayout(new BorderLayout());
		queryResultPanel.add(new JScrollPane(resultTable), BorderLayout.CENTER);

		final JComboBox plotOptions = new JComboBox(new String[] { "Heatmap",
				"what else" });
		JButton plotButton = new JButton("Plot");
		JButton exportButton = new JButton("Export");
		resultProcessingPanel.add(new JLabel("Plot options:"));
		resultProcessingPanel.add(plotOptions);
		resultProcessingPanel.add(plotButton);
		resultProcessingPanel.add(exportButton);

		plotButton.addActionListener(dummyListener);
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

				resultTable.setModel(new QueryResultTableModel(
						computationalColumnNames, null));

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

				resultTable.setModel(new QueryResultTableModel(
						experimentalColumnNames, null));
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
	
	private JScrollPane buildFilterJListPanel(List<String> dataList, FilteredJList filteredJList) {

		filteredJList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
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
}
