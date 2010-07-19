/**
 * 
 */
package org.geworkbench.components.filtering;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.engine.skin.Skin;

/**
 * @author zji
 * @version $Id$
 *
 */
public class PreviewDialog extends JDialog {
	private static final long serialVersionUID = -1081378856178472680L;
	
	private JTextField markerToBeSearched = new JTextField(20);
	private JTextField geneToBeSearched = new JTextField(20);
	
	private JTable markers = null;
	private TableModel markerTableModel = null;

	private JButton okButton = new JButton("Filter");
	private JButton cancelButton = new JButton("Cancel");
	
	private FilteringPanel filteringPanel = null;

	PreviewDialog(final List<DSGeneMarker> list, int total, FilteringPanel filteringPanel) {
		super(Skin.getFrame(), "Filtering Preview", true);
		
		this.filteringPanel = filteringPanel;
		
		int count = list.size();
		double percent = (double)count/total;
		
		markerTableModel = new DefaultTableModel(new String[]{"Marker Name", "Gene Symbol (if known)"}, count) {
			private static final long serialVersionUID = -6270626249396521904L;

			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}  
		};
		markers = new JTable(markerTableModel);
		markers.setAutoCreateRowSorter(true);

		int rowIndex = 0;
		for(DSGeneMarker marker: list) {
			markerTableModel.setValueAt(marker.getLabel(), rowIndex, 0);
			markerTableModel.setValueAt(marker.getGeneName(), rowIndex, 1);
			rowIndex++;
		}
		
		JScrollPane listScroller = new JScrollPane(markers);
		listScroller.setPreferredSize(new Dimension(250, 80));
		listScroller.setAlignmentX(Component.CENTER_ALIGNMENT);
		JPanel listPane = new JPanel();
		listPane.setLayout(new BoxLayout(listPane, BoxLayout.PAGE_AXIS));
		JLabel label = new JLabel("Markers to be filtered out");
		label.setAlignmentX(Component.CENTER_ALIGNMENT);
		listPane.add(label);
		
		JPanel searchMarker = new JPanel();
		JButton searchMarkerButton= new JButton("Search Marker");
		searchMarkerButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				
				for(int i=0; i<markerTableModel.getRowCount(); i++) {
					String markerName = (String)markerTableModel.getValueAt(i, 0);
					if(markerName.contains(markerToBeSearched.getText())) {
						
						int row = markers.convertRowIndexToView(i);
						Rectangle r = markers.getCellRect(row, 0, false);  
						markers.scrollRectToVisible(r);
						markers.setRowSelectionInterval(row, row);
						
						return;
					}
				}
				
				JOptionPane.showMessageDialog(PreviewDialog.this,
						"Marker name "+markerToBeSearched.getText()+" is not found.", 
					    "Not Found", JOptionPane.PLAIN_MESSAGE);
				
			}
			
		});
		searchMarker.add(searchMarkerButton);
		searchMarker.add(markerToBeSearched);
		searchMarker.setAlignmentX(Component.CENTER_ALIGNMENT);

		JPanel searchGene = new JPanel();
		JButton searchGeneButton= new JButton("Search Gene");
		searchGeneButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				
				for(int i=0; i<markerTableModel.getRowCount(); i++) {
					String geneName = (String)markerTableModel.getValueAt(i, 1);
					if(geneName.contains(geneToBeSearched.getText())) {
						
						int row = markers.convertRowIndexToView(i);
						Rectangle r = markers.getCellRect(row, 0, false);  
						markers.scrollRectToVisible(r);
						markers.setRowSelectionInterval(row, row);
						
						return;
					}
				}
				
				JOptionPane.showMessageDialog(PreviewDialog.this,
					    "Gene symbol "+geneToBeSearched.getText()+" is not found.", 
					    "Not Found", JOptionPane.PLAIN_MESSAGE);
				
			}
			
		});
		searchGene.add(searchGeneButton);
		searchGene.add(geneToBeSearched);
		searchGene.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		JPanel buttonPanel = new JPanel();
		okButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				PreviewDialog.this.filteringPanel.filtering_actionPerformed();
				PreviewDialog.this.dispose();
			}
			
		});
		cancelButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				PreviewDialog.this.dispose();
			}
			
		});
		buttonPanel.add(okButton);
		buttonPanel.add(cancelButton);
		
		final NumberFormat mf = NumberFormat.getPercentInstance();
		JLabel countLabel = new JLabel("Total number "+count+" ("+mf.format(percent)+")");
		countLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

		listPane.add(countLabel);
		listPane.add(searchMarker);
		listPane.add(searchGene);

		listPane.add(Box.createRigidArea(new Dimension(0,5)));
		listPane.add(listScroller);
		listPane.add(Box.createRigidArea(new Dimension(0,5)));
		listPane.add(buttonPanel);
		listPane.add(Box.createRigidArea(new Dimension(0,5)));
		listPane.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

		add(listPane);
		pack();
		
		setLocationRelativeTo(null);
		
	}
}
