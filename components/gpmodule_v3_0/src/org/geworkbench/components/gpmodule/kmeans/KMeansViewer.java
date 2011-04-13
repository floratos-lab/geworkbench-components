package org.geworkbench.components.gpmodule.kmeans;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.BoxLayout;

import org.geworkbench.bison.annotation.CSAnnotationContextManager;
import org.geworkbench.bison.annotation.DSAnnotationContext;
import org.geworkbench.bison.annotation.DSAnnotationContextManager;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.KMeansResult;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.complex.panels.CSAnnotPanel;
import org.geworkbench.bison.datastructure.complex.panels.DSAnnotatedPanel;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.events.ProjectEvent;
import org.geworkbench.events.SubpanelChangedEvent;

/**
 * KMeans Viewer of KMeans clustering analysis component
 * 
 * @author zm2165
 * @version $Id:$
 * 
 */
@AcceptTypes({ KMeansResult.class })
public class KMeansViewer extends JPanel implements VisualPlugin {

	private static final long serialVersionUID = 8763573636321803637L;
	private static final int INDEX_OF_GENE=0;
	DSMicroarraySet<DSMicroarray> maSet;
	
	private ArrayList<List<String[]>> resultList=null;
	int selectedRow=0;
	private JPanel detailedPane=null;

	private ClusterSumTableModel clusterSumTableModel=new ClusterSumTableModel();
	private MarkersDetailTableModel markersDetailTableModel=new MarkersDetailTableModel();
	private ArraysDetailTableModel arraysDetailTableModel=new ArraysDetailTableModel();
	
	private JLabel numOfClustersLabel=new JLabel();
	private JLabel averageLabel=new JLabel();
	private JLabel stdDeviationLabel=new JLabel();
	private JButton jAddBttn=null;
	
	private JLabel clusterIdLabel=new JLabel("1");	//default
	private JLabel clusterSizeLabel=new JLabel();
	
	public KMeansViewer() {			

		JSplitPane splitPane;		
		JPanel clusterSum=new JPanel();
		detailedPane=new JPanel();
		detailedPane.setLayout(new BorderLayout());
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				clusterSum, new JScrollPane(detailedPane));		
		
		JPanel smallSumPane=new JPanel();
		smallSumPane.setLayout(new BoxLayout(smallSumPane, BoxLayout.Y_AXIS));
		smallSumPane.setAlignmentX(Component.LEFT_ALIGNMENT);
		JPanel firstPane=new JPanel();
		firstPane.add(new JLabel("Number of clusters:        "));
		firstPane.add(numOfClustersLabel);		
		smallSumPane.add(firstPane);
		JPanel secondPane=new JPanel();
		secondPane.add(new JLabel("Average cluster size:"));
		secondPane.add(averageLabel);		
		smallSumPane.add(secondPane);
		JPanel thirdPane=new JPanel();
		thirdPane.add(new JLabel("Standard deviation:  "));
		thirdPane.add(stdDeviationLabel);		
		smallSumPane.add(thirdPane);		
		clusterSum.add(smallSumPane);
			
		JTable clusterSumTab=new JTable(clusterSumTableModel);
		clusterSumTab.setPreferredScrollableViewportSize(new Dimension(170, 130));		
		clusterSumTab.setFillsViewportHeight(true);
		clusterSum.add(new JScrollPane(clusterSumTab));		
		
		splitPane.setOneTouchExpandable(true);
		splitPane.setDividerLocation(200);
		splitPane.setAutoscrolls(true);

		//Provide minimum sizes for the two components in the split pane
		Dimension minimumSizeLeft = new Dimension(180, 50);
		clusterSum.setMinimumSize(minimumSizeLeft);
		Dimension minimumSizeRight = new Dimension(650, 50);
		detailedPane.setMinimumSize(minimumSizeRight);

		setLayout(new BorderLayout());
		add(splitPane, BorderLayout.CENTER);
		
		clusterSumTab.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);		
        ListSelectionModel rowSM = clusterSumTab.getSelectionModel();
        rowSM.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {                
                if (e.getValueIsAdjusting()) return;

                ListSelectionModel lsm = (ListSelectionModel)e.getSource();
                selectedRow = lsm.getMinSelectionIndex();
                if (selectedRow==-1) selectedRow=0;//default is 0
                clusterIdLabel.setText(""+(selectedRow+1));
                clusterSizeLabel.setText(""+resultList.get(selectedRow).size());
                if(kmResult.getClusterBy()==INDEX_OF_GENE){    				
    				markersDetailTableModel.setValues(resultList.get(selectedRow));
    				markersDetailTableModel.fireTableDataChanged();
    			}
    			else{    				
    				arraysDetailTableModel.setValues(resultList.get(selectedRow));
    				arraysDetailTableModel.fireTableDataChanged();
    			}               
            }
        });
    
	}
	
	private class ClusterSumTableModel extends AbstractTableModel {

		private static final long serialVersionUID = -2748836864688812980L;

		private static final int COLUMN_COUNT = 2;

		List<String[]> list = null;
		private final String[] columnNames = new String[] { "Cluster Id", "# of Members"};

		public ClusterSumTableModel() {
			list = new ArrayList<String[]>();
		}

		@Override
		public String getColumnName(int col) {
			return columnNames[col];
		}

		@Override
		public int getColumnCount() {
			return COLUMN_COUNT;
		}

		@Override
		public int getRowCount() {
			return list.size();
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			if (kmResult == null)
				return null;			

			switch (columnIndex) {
			case 0:
				return list.get(rowIndex)[0];
			case 1:
				return list.get(rowIndex)[1];			
			}			
			
			return 0;
		}

		public void setValues(List<String[]> list) {
			this.list = list;
		}
	}
	
	private class ArraysDetailTableModel extends AbstractTableModel {
		
		private static final long serialVersionUID = -980873977924192433L;

		private static final int COLUMN_COUNT = 2;

		List<String[]> list = null;
		private final String[] columnNames = new String[] { "Array Id", "Array Set Membership"	};

		public ArraysDetailTableModel() {
			list = new ArrayList<String[]>();
		}

		@Override
		public String getColumnName(int col) {
			return columnNames[col];
		}

		@Override
		public int getColumnCount() {
			return COLUMN_COUNT;
		}

		@Override
		public int getRowCount() {
			return list.size();
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			if (kmResult == null)
				return null;			

			switch (columnIndex) {
			case 0:
				return list.get(rowIndex)[0];
			case 1:
				return list.get(rowIndex)[1];			
			}			
			
			return 0;
		}

		void setValues(List<String[]> list) {
			this.list = list;
		}
	}

	private class MarkersDetailTableModel extends AbstractTableModel {

		private static final long serialVersionUID = 6032960368264989773L;

		private static final int COLUMN_COUNT = 3;

		List<String[]> list = null;
		private final String[] columnNames = new String[] { "ProbeSet Id", "Gene Symbol",
				"Annotation"};

		public MarkersDetailTableModel() {
			list = new ArrayList<String[]>();
		}

		@Override
		public String getColumnName(int col) {
			return columnNames[col];
		}

		@Override
		public int getColumnCount() {
			return COLUMN_COUNT;
		}

		@Override
		public int getRowCount() {
			return list.size();
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			if (kmResult == null)
				return null;			

			switch (columnIndex) {
			case 0:
				return list.get(rowIndex)[0];
			case 1:
				return list.get(rowIndex)[1];
			case 2:
				return list.get(rowIndex)[2];	
			}			
			
			return 0;
		}

		void setValues(List<String[]> list) {
			this.list = list;
		}
	}

	private KMeansResult kmResult=null;
	
	@Subscribe
	public void receive(ProjectEvent event, Object source) {
		DSDataSet<?> dataSet = event.getDataSet();
		if (dataSet instanceof KMeansResult) {
			kmResult=(KMeansResult) dataSet;
			maSet=kmResult.getMaSet();
			
			resultList=kmResult.getResultList();
			List<String[]> clustersBrief=new ArrayList<String[]>();
			double mean=0;
			double stdDev=0;
			if(resultList.size()!=0){
				for(int i=0;i<resultList.size();i++){				
					String[] ss=new String[2];
					ss[0]=""+(i+1);
					ss[1]=""+resultList.get(i).size();
					clustersBrief.add(ss);
					mean+=resultList.get(i).size();
				}			
				mean=mean/resultList.size();
					
				for(int i=0;i<resultList.size();i++){
					stdDev+=(resultList.get(i).size()-mean)*(resultList.get(i).size()-mean);					
				}	
				stdDev=stdDev/resultList.size();
				stdDev=Math.sqrt(stdDev);	
			}	
			
			clusterSumTableModel.setValues(clustersBrief);
			clusterSumTableModel.fireTableDataChanged();
			numOfClustersLabel.setText(""+clustersBrief.size());
			averageLabel.setText(""+((int)(mean*10))/10.0);
			stdDeviationLabel.setText(""+((int)(stdDev*10))/10.0);
			
			detailedPane.removeAll();		
			JPanel detailSum=new JPanel();
			detailSum.setLayout(new GridLayout(0,4));
			detailedPane.add(detailSum,BorderLayout.NORTH);			
			
			JPanel no1Pane=new JPanel();
			no1Pane.add(new JLabel("Showing results for cluster:"));
			no1Pane.add(clusterIdLabel);
			detailSum.add(no1Pane);
			detailSum.add(new JLabel());
			
			final DSItemList<DSGeneMarker> allMarkerList=maSet.getMarkers();//all markers
			
			jAddBttn = new JButton();
	        jAddBttn.setText("Add to Set");	        
	        jAddBttn.addActionListener(new java.awt.event.ActionListener() {
	          public void actionPerformed(ActionEvent e) {
            	List<String[]> clusterStrs=resultList.get(selectedRow);
	            if(kmResult.getClusterBy()==INDEX_OF_GENE){   		//by markers
	            	DSAnnotatedPanel<DSGeneMarker, Float> panelSignificant = new CSAnnotPanel<DSGeneMarker, Float>(
	            			"KMeans_cluster_"+(selectedRow+1));
	            	
	            	for(int i=0;i<clusterStrs.size();i++){
	            		String[] ss=clusterStrs.get(i);
	            		for(DSGeneMarker d: allMarkerList){
	            			if(ss[0].equalsIgnoreCase(d.getLabel())){
	            				panelSignificant.add(d, new Float(i));	
	            			}
	            		}
	            	}
	            	publishSubpanelChangedEvent(new SubpanelChangedEvent<DSGeneMarker>(
	            			DSGeneMarker.class, panelSignificant,
	            			SubpanelChangedEvent.NEW));	
	            	
	            }
	            else{	//by arrays
	            	DSAnnotatedPanel<DSMicroarray, Float> panelToBeAdded = new CSAnnotPanel<DSMicroarray, Float>(
	            			"KMeans_cluster_"+(selectedRow+1));
	            	
	            	for(int i=0;i<clusterStrs.size();i++){
	            		String[] ss=clusterStrs.get(i);
	            		for(DSMicroarray d: maSet){
	            			if(ss[0].equalsIgnoreCase(d.getLabel())){
	            				panelToBeAdded.add(d, new Float(i));	
	            			}
	            		}
	            	}	            		
	            	
	            publishSubpanelChangedEvent(new SubpanelChangedEvent<DSMicroarray>(
	            		DSMicroarray.class, panelToBeAdded,	SubpanelChangedEvent.NEW));	
	            }
	          }
	        });
	        detailSum.add(jAddBttn);
	        detailSum.add(new JLabel());	        
	     
			JPanel no2Pane=new JPanel();
			no2Pane.add(new JLabel("Cluster size:                         "));
			no2Pane.add(clusterSizeLabel);
			clusterSizeLabel.setText(""+resultList.get(0).size());
			detailSum.add(no2Pane);			
			
			JTable clusterDetailTab=null;
			if(kmResult.getClusterBy()==INDEX_OF_GENE){
				clusterDetailTab=new JTable(markersDetailTableModel);
				clusterDetailTab.setAutoCreateRowSorter(true);
				markersDetailTableModel.setValues(resultList.get(0));
				markersDetailTableModel.fireTableDataChanged();
			}
			else{
				clusterDetailTab=new JTable(arraysDetailTableModel);
				clusterDetailTab.setAutoCreateRowSorter(true);
				arraysDetailTableModel.setValues(resultList.get(0));
				arraysDetailTableModel.fireTableDataChanged();
			}
			clusterDetailTab.setPreferredScrollableViewportSize(new Dimension(700, 0));
			clusterDetailTab.setFillsViewportHeight(true);
			JScrollPane tablePane=new JScrollPane(clusterDetailTab);
			
			detailedPane.add(tablePane,BorderLayout.CENTER);
		}
	}

	@Publish
	public org.geworkbench.events.SubpanelChangedEvent<?> publishSubpanelChangedEvent(
			org.geworkbench.events.SubpanelChangedEvent<?> event) {
		return event;
	}	
	
	public Component getComponent() {
		return this;
	}
	
}
