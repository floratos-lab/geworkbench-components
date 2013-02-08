/**
 * 
 */
package org.geworkbench.components.lincs;

import java.util.List;
import java.util.ArrayList;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Dimension;
 

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

	public LincsInterface() {
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

		JPanel queryTypePanel = new JPanel();
		JPanel queryConditionPanel1 = new JPanel();
		JPanel queryConditionPanel2 = new JPanel();
	 
		JPanel queryCommandPanel = new JPanel();
		JPanel queryResultPanel = new JPanel();
		JPanel resultProcessingPanel = new JPanel();

		add(queryTypePanel);
		add(queryConditionPanel1);
		add(queryConditionPanel2);
		 
		add(queryCommandPanel);
		add(queryResultPanel);
		add(resultProcessingPanel);

		final JRadioButton experimental = new JRadioButton("Experimental");
		experimental.setSelected(true);
		JRadioButton computational = new JRadioButton("Computational");
		ButtonGroup group = new ButtonGroup();
	    group.add(experimental);
	    group.add(computational);
		queryTypePanel.add(new JLabel("Query Type"));
		queryTypePanel.add(experimental);
		queryTypePanel.add(computational);

		final Lincs lincs = new Lincs(null, null, null);
		 
		queryConditionPanel1.setLayout(new GridLayout(2, 7));
		
		JTextField drug1Search = new JTextField("search");
		drug1Search.setPreferredSize(new Dimension(50, 25));
		drug1Search.setFont(new Font("Courier", Font.ITALIC,12));
			
		drug1Search.setToolTipText("Filter string for drug 1");
		queryConditionPanel1.add(new JLabel(""));
		queryConditionPanel1.add(new JLabel(""));
		queryConditionPanel1.add(new JLabel("Drug 1"));
		queryConditionPanel1.add(new JLabel(""));
		queryConditionPanel1.add(new JLabel(""));
		queryConditionPanel1.add(new JLabel(""));
		queryConditionPanel1.add(new JLabel(""));
		
		queryConditionPanel1.add(new JLabel("Tissue Type"));
		queryConditionPanel1.add(new JLabel("Cell Line"));
		queryConditionPanel1.add(drug1Search);
		queryConditionPanel1.add(new JLabel("Drug 2"));
		queryConditionPanel1.add(new JLabel("Assay Type"));
		queryConditionPanel1.add(new JLabel("Synergy Measurement Type"));
		queryConditionPanel1.add(new JLabel(""));
	
		queryConditionPanel2.setLayout(new GridLayout(1, 7));
		
		final JList tissueTypeBox = new JList();		 
		List<String> tissueTypeList = lincs.getAllTissueNames();
		tissueTypeList.add(0, "All");	
		 
		final JList cellLineBox = new JList();
		List<String> cellLineList = lincs.getAllCellLineNamesForTissueTypes(null);	
		cellLineList.add(0, "All");
		 
		final JList drug1Box = new JList();
		List<String> drug1List = new ArrayList<String>();	
		drug1List.add(0, "All");
		
		
		final JList drug2Box = new JList();
		List<String> drug2List = new ArrayList<String>();	
		drug2List.add(0, "All");
		
		final JList assayTypeBox = new JList();
		List<String> assayTypeList = lincs.getAllAssayTypeNames();	
		assayTypeList.add(0, "All");
		final JList synergyMeasuremetnTypeBox = new JList();
		List<String> synergyMeasuremetnTypeList = lincs.getAllAssayTypeNames();	
		synergyMeasuremetnTypeList.add(0, "All");
		
		final JCheckBox onlyTitration = new JCheckBox("Only with titration");
		queryConditionPanel2.add( buildJListPanel("Tissue Type", tissueTypeList, tissueTypeBox, null ));
		queryConditionPanel2.add(buildJListPanel("Cell Line", cellLineList, cellLineBox, null ));
		queryConditionPanel2.add(buildJListPanel("Drug 1", drug1List, drug1Box, drug1Search));
		queryConditionPanel2.add(buildJListPanel("Drug 2", drug2List, drug2Box, null));
		queryConditionPanel2.add(buildJListPanel("Assay Type", assayTypeList, assayTypeBox, null));
		queryConditionPanel2.add(buildJListPanel("Synergy Measurement Type", synergyMeasuremetnTypeList, synergyMeasuremetnTypeBox, null));
		queryConditionPanel2.add(onlyTitration);
//		queryConditionPanel.setMaximumSize(new Dimension(1000, 100));

		// dynamic dependency parts
		tissueTypeBox.addListSelectionListener(new ListSelectionListener() {

			public void valueChanged(ListSelectionEvent e) {
				List<String> dataList = lincs.getAllCellLineNamesForTissueTypes(null);
				dataList.add(0, "All");
				cellLineBox.setModel(new LincsListModel(dataList));
				 
				 
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
		resetButton.addActionListener(dummyListener);

		JTable resultTable = new JTable(new QueryResultTableModel());
		queryResultPanel.setLayout(new BorderLayout());
		queryResultPanel.add(new JScrollPane(resultTable), BorderLayout.CENTER);
		
		final JComboBox plotOptions = new JComboBox(new String[]{"Heatmap", "what else"});
		JButton plotButton = new JButton("Plot");
		JButton exportButton = new JButton("Export");
		resultProcessingPanel.add(new JLabel("Plot options:"));
		resultProcessingPanel.add(plotOptions);
		resultProcessingPanel.add(plotButton);
		resultProcessingPanel.add(exportButton);
		
		plotButton.addActionListener(dummyListener);
		exportButton.addActionListener(dummyListener);
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
		
        public LincsListModel(List<String> list)
        {
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
	
	private JScrollPane buildJListPanel(String title, List<String> dataList, JList aJlist, JTextField search) {
			 
		aJlist.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);  
		aJlist.setModel(new LincsListModel(dataList));
		JScrollPane jScrollPane1 = new javax.swing.JScrollPane(aJlist,ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
			     ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		 
		jScrollPane1.setPreferredSize(new java.awt.Dimension(200, 70));
		 
		 
		return jScrollPane1;

	}


}
