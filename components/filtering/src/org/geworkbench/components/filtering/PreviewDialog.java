/**
 * 
 */
package org.geworkbench.components.filtering;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;

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
	private DefaultTableModel markerTableModel = null;

	private JButton okButton = new JButton("Filter");
	private JButton cancelButton = new JButton("Cancel");
	
	private FilteringPanel filteringPanel = null;
	
	private MarkerListModel markerModel = new MarkerListModel();
	
	private List<DSGeneMarker> preList;
	private PreviewSearchTable markerAutoList;
	static final String MARKERTABLE_DELITMETER= "\t";

	PreviewDialog(final List<DSGeneMarker> list, int total, final FilteringPanel filteringPanel) {
		super( filteringPanel.getFrame(), "Filtering Preview", true);
		
		this.filteringPanel = filteringPanel;
		preList=list;
		
		int count = list.size();
		double percent = (double)count/total;
		
		markerTableModel = new DefaultTableModel(new String[]{"Marker Name", "Gene Symbol (if known)"}, count) {
			private static final long serialVersionUID = -6270626249396521904L;

			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}  
		};		
		
		
		int rowIndex = 0;
		for(DSGeneMarker marker: list) {
			markerTableModel.setValueAt(marker.getLabel(), rowIndex, 0);
			String[] ss=marker.getShortNames();
			String s="";
			if(ss.length>1){
				for(int i=0;i<ss.length-1;i++){
					s+=ss[i]+"///";
				}
				s+=ss[ss.length-1];
			}
			else{
				s=marker.getGeneName();
			}
			markerTableModel.setValueAt(s, rowIndex, 1);
			rowIndex++;
		}
		
		markers = new JTable(markerTableModel);
		markers.setAutoCreateRowSorter(true);		
		
		markerAutoList = new PreviewSearchTable(markerModel, markerTableModel);	     
	    markerAutoList.getList().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	    markerAutoList.getList().setFixedCellWidth(250);
		
		
		JScrollPane listScroller = new JScrollPane(markers);
		listScroller.setPreferredSize(new Dimension(250, 180));	
		listScroller.setAlignmentX(Component.CENTER_ALIGNMENT);
		JPanel listPane = new JPanel(new BorderLayout());	 
		JLabel label = new JLabel("Markers to be filtered out");
		label.setAlignmentX(Component.CENTER_ALIGNMENT);
		 
		JPanel searchMarker = new JPanel(new GridBagLayout());
		JButton searchMarkerButton= new JButton("Search Marker");
		searchMarkerButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				
				for(int i=0; i<markerTableModel.getRowCount(); i++) {
					String markerName = (String)markerTableModel.getValueAt(i, 0);
					if(markerName.toUpperCase().contains(markerToBeSearched.getText().toUpperCase())) {
						
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
		markerToBeSearched.setMinimumSize(markerToBeSearched.getPreferredSize());
		searchMarker.setAlignmentX(Component.CENTER_ALIGNMENT);

		JPanel searchGene = new JPanel(new GridBagLayout());
		JButton searchGeneButton= new JButton("Search Gene");
		searchGeneButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				
				for(int i=0; i<markerTableModel.getRowCount(); i++) {
					String geneName = (String)markerTableModel.getValueAt(i, 1);
					if(geneName.toUpperCase().contains(geneToBeSearched.getText().toUpperCase())) {
						
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
		geneToBeSearched.setMinimumSize(geneToBeSearched.getPreferredSize());		
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
        
	 
		JPanel searchPanel = new JPanel();
		BoxLayout boxLayout = new BoxLayout(searchPanel, BoxLayout.PAGE_AXIS);
		searchPanel.setLayout(boxLayout);
		 
		searchPanel.add(label);
		searchPanel.add(countLabel);
		
		listPane.add(searchPanel, BorderLayout.NORTH);		
		listPane.add(markerAutoList, BorderLayout.CENTER);
		listPane.add(buttonPanel, BorderLayout.SOUTH);	 
		listPane.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		listPane.getPreferredSize();
		listPane.setMinimumSize(listPane.getPreferredSize());
		 
		add(listPane);
		pack();
		
		setLocationRelativeTo(null);
		
	}

	private class MarkerListModel extends AbstractListModel {
			
		private static final long serialVersionUID = 4749206868756162478L;

		public int getSize() {           
            	return preList.size();            
        }

        public Object getElementAt(int index) {        	
			//put a marker's marker name and gene symbol in one string for searching
			String s=preList.get(index).getLabel()+MARKERTABLE_DELITMETER+preList.get(index).getGeneName();
	       	return s;           
        }

       
    }
	
	
}
