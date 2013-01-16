/**
 * 
 */
package org.geworkbench.components.lincs;

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
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
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
		JPanel queryConditionPanel = new JPanel();
		JPanel queryCommandPanel = new JPanel();
		JPanel queryResultPanel = new JPanel();
		JPanel resultProcessingPanel = new JPanel();

		add(queryTypePanel);
		add(queryConditionPanel);
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
		
		queryConditionPanel.setLayout(new GridLayout(2, 7));
		queryConditionPanel.add(new JLabel("Tissue Type"));
		queryConditionPanel.add(new JLabel("Cell Line"));
		queryConditionPanel.add(new JLabel("Drug 1"));
		queryConditionPanel.add(new JLabel("Drug 2"));
		queryConditionPanel.add(new JLabel("Assay Type"));
		queryConditionPanel.add(new JLabel("Synergy Measurement Type"));
		queryConditionPanel.add(new JLabel(""));
		final JComboBox tissueTypeBox = new JComboBox(lincs.getAllTissueNames());
		tissueTypeBox.insertItemAt("All", 0);
		final JComboBox cellLineBox = new JComboBox();
		final JComboBox drug1Box = new JComboBox();
		final JComboBox drug2Box = new JComboBox();
		final JComboBox assayTypeBox = new JComboBox();
		final JComboBox synergyMeasuremetnTypeBox = new JComboBox();
		final JCheckBox onlyTitration = new JCheckBox("Only with titration");
		queryConditionPanel.add(tissueTypeBox);
		queryConditionPanel.add(cellLineBox);
		queryConditionPanel.add(drug1Box);
		queryConditionPanel.add(drug2Box);
		queryConditionPanel.add(assayTypeBox);
		queryConditionPanel.add(synergyMeasuremetnTypeBox);
		queryConditionPanel.add(onlyTitration);
//		queryConditionPanel.setMaximumSize(new Dimension(1000, 100));

		// dynamic dependency parts
		tissueTypeBox.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				String[] s = lincs.getAllCellLineNamesForTissueType((String)tissueTypeBox.getSelectedItem());
				cellLineBox.removeAllItems();
				cellLineBox.addItem("All");
				for(String cellLine : s) {
					cellLineBox.addItem(cellLine);
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
				Object tissueType = tissueTypeBox.getSelectedItem();
				Object cellLine = cellLineBox.getSelectedItem();

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

}
