package org.geworkbench.components.demand;

import java.awt.BorderLayout;
import java.awt.Component;
import java.text.DecimalFormat;
import java.util.Enumeration;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;

import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSDemandResultSet;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.events.ProjectEvent;

@AcceptTypes({DSDemandResultSet.class})
public class DemandViewer extends JPanel implements VisualPlugin{

	private static final long serialVersionUID = 5388748224274628999L;
	private static final String[] rColNames = {"Gene", "ChrLocation", "Conn", "Pvalue", "Adjp"};
	private static final String[] eColNames = {"Gene1", "Gene2", "KLD", "KLD.p"};
	private static final String[] mColNames = {"Gene1", "Gene2", "Dysreg", "Dir"};
	private ViewerTableModel rModel = new ViewerTableModel(rColNames);
	private ViewerTableModel eModel = new ViewerTableModel(eColNames);
	private ViewerTableModel mModel = new ViewerTableModel(mColNames);
	private final DecimalFormat decformatter = new DecimalFormat("0.######E0");
	private final DecimalFormat dblformatter = new DecimalFormat("0.######");
	
	@Override
	public Component getComponent() {
		return this;
	}
	
	@Subscribe
	public void receive(ProjectEvent event, Object source) {
		DSDataSet<?> dataSet = event.getDataSet();
		
		if (dataSet instanceof DSDemandResultSet){	
			DSDemandResultSet resultSet = (DSDemandResultSet)dataSet;
			Object[][] rData = resultSet.getResult();
			Object[][] eData = resultSet.getEdge();
			Object[][] mData = resultSet.getModule();
			rModel.setData(rData);
			eModel.setData(eData);
			mModel.setData(mData);
			rModel.fireTableDataChanged();
			eModel.fireTableDataChanged();
			mModel.setData(mData);
		}
	}
	
	public DemandViewer(){
		JTabbedPane tabbedPane = new JTabbedPane();

		JTable rTable = new JTable(rModel);
		JScrollPane rScrollPane = new JScrollPane(rTable);
		rTable.setFillsViewportHeight(true);
		TableRowSorter<ViewerTableModel> rSorter = new TableRowSorter<ViewerTableModel>(rModel);
		rTable.setRowSorter(rSorter);
		tabbedPane.addTab("Demand Result", rScrollPane);
		
		DecCellRenderer rRenderer = new DecCellRenderer();
		Enumeration<TableColumn> rColumns = rTable.getColumnModel().getColumns();
		while(rColumns.hasMoreElements()){
			rColumns.nextElement().setCellRenderer(rRenderer);
		}
		
		JTable eTable = new JTable(eModel);
		JScrollPane eScrollPane = new JScrollPane(eTable);
		eTable.setFillsViewportHeight(true);
		TableRowSorter<ViewerTableModel> eSorter = new TableRowSorter<ViewerTableModel>(eModel);
		eTable.setRowSorter(eSorter);
		tabbedPane.addTab("KL Edge", eScrollPane);

		DblCellRenderer eRenderer = new DblCellRenderer();
		Enumeration<TableColumn> eColumns = eTable.getColumnModel().getColumns();
		while(eColumns.hasMoreElements()){
			eColumns.nextElement().setCellRenderer(eRenderer);
		}
		
		JTable mTable = new JTable(mModel);
		JScrollPane mScrollPane = new JScrollPane(mTable);
		mTable.setFillsViewportHeight(true);
		TableRowSorter<ViewerTableModel> mSorter = new TableRowSorter<ViewerTableModel>(mModel);
		mTable.setRowSorter(mSorter);
		tabbedPane.addTab("Module", mScrollPane);

		DblCellRenderer mRenderer = new DblCellRenderer();
		Enumeration<TableColumn> mColumns = mTable.getColumnModel().getColumns();
		while(mColumns.hasMoreElements()){
			mColumns.nextElement().setCellRenderer(mRenderer);
		}
		
		setLayout(new BorderLayout());
		add(tabbedPane, BorderLayout.CENTER);
	}
	
	private class DblCellRenderer extends DefaultTableCellRenderer {
		private static final long serialVersionUID = -1427727373354274060L;
		public Component getTableCellRendererComponent(JTable table, Object value,
				boolean isSelected, boolean hasFocus, int row, int col) {
			if (value == null) setText("");
			else{
				if(table.getColumnClass(col) == Double.class)
					setText(dblformatter.format((Double)value).toLowerCase());
				else setText(value.toString());
			}
			if (table.isRowSelected(row))
				setBackground(table.getSelectionBackground());
			else setBackground(table.getBackground());
			return this;
	    }
	}

	private class DecCellRenderer extends DefaultTableCellRenderer {
		private static final long serialVersionUID = -1427727373354274060L;
		public Component getTableCellRendererComponent(JTable table, Object value,
				boolean isSelected, boolean hasFocus, int row, int col) {
			if (value == null) setText("");
			else{
				if(col == 2)
					setText(dblformatter.format((Double)value));
				else if(table.getColumnClass(col) == Double.class)
					setText(decformatter.format((Double)value).toLowerCase());
				else setText(value.toString());
			}
			if (table.isRowSelected(row))
				setBackground(table.getSelectionBackground());
			else setBackground(table.getBackground());
			return this;
	    }
	}

	public class ViewerTableModel extends AbstractTableModel{
		private static final long serialVersionUID = 1158971679105559940L;
		private Object[][] data = new Object[0][0];
		private String[] columnNames = new String[0];
		
		public ViewerTableModel(String[] columnNames){
			this.columnNames = columnNames;
		}
		
		@Override
		public int getColumnCount() {
			return columnNames.length;
		}

		@Override
		public int getRowCount() {
			return data.length;
		}

		@Override
		public String getColumnName(int col){
			return columnNames[col];
		}
		
		@Override
		public Object getValueAt(int row, int col) {
			return data[row][col];
		}
		
		@Override
		public Class<?> getColumnClass(int col){
			return getValueAt(0, col).getClass();
		}
		
		public void setData(Object[][] data){
			this.data = data;
		}
	}
}
