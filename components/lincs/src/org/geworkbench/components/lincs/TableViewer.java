/**
 * 
 */
package org.geworkbench.components.lincs;

/**
 * @author my2248
 * @version $Id: TableViewer.java 9733 2012-07-24 13:42:11Z zji $
 * 
 */
import java.awt.BorderLayout; 
import java.awt.Component; 
import java.awt.Color; 
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Enumeration; 
import java.util.List;
 
 
import javax.swing.JFileChooser; 
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
 
import javax.swing.filechooser.FileFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;  



public class TableViewer extends JPanel {
 
	 
	private static final long serialVersionUID = -8247691800129749646L;
	private JTable table;
	private TableModel model;

	private Object[][] data;
	private String[] headerNames;
	private boolean isColorGradient = false;
	private static Color oldCellBgColor;
	private static Color oldCellfgColor;
	 
	/** Creates a new instance of TableViewer */
	public TableViewer() {
	}

	/**
	 * Creates a new TableViewer with header names and data.
	 * 
	 * @param headerNames
	 *            Header name strings.
	 * @param data
	 *            table data
	 */
	
    public TableViewer(final String[] headerNames, final Object[][] data) {		
		initTable(headerNames, data, null);		
	}
	 
	public TableViewer(final String[] headerNames, final Object[][] data, List<String> hideColumns) {
		
		initTable(headerNames, data, hideColumns);
		
	}
	
	private void initTable(final String[] headerNames, final Object[][] data, List<String> hideColumns)
	{
		this.data = data;
		this.headerNames = headerNames;
		model = new LincsViewerTableModel();

		table = new JTable(model);
		table.setAutoCreateRowSorter(true);

		if (hideColumns != null)
		for(int i =0; i<hideColumns.size(); i++)
		{
			table.getColumnModel().getColumn(getHeaderNameIndex(hideColumns.get(i))).setMaxWidth(0);
		    table.getColumnModel().getColumn(getHeaderNameIndex(hideColumns.get(i))).setMinWidth(0);
		    table.getColumnModel().getColumn(getHeaderNameIndex(hideColumns.get(i))).setPreferredWidth(0);
		}
		
		Enumeration<TableColumn> columns = table.getColumnModel().getColumns();
		while (columns.hasMoreElements()) {
			columns.nextElement().setCellRenderer(new CellRenderer());
		}

		setLayout(new BorderLayout());
		add(new JScrollPane(table), BorderLayout.CENTER);
		
        table.addMouseListener(new MouseAdapter() {
			
			public void mouseClicked(MouseEvent e) {
			 
				if (e.getClickCount() == 2) {				 
					JTable target = (JTable) e.getSource();					 
					Point p = new Point(e.getX(), e.getY());
					int col = target.columnAtPoint(p);
					int row = target.rowAtPoint(p);
					if (data[row][col] instanceof ValueObject )
					{   
						ValueObject v = (ValueObject)data[row][col];
						if (v.getReferenceId() > 0 && v.toString().equalsIgnoreCase("view"))
							new TitrationCurveWindow(v.getReferenceId());
						else
						{
							new FmoaDisplayWindow(v.getValue().toString(), v.getReferenceId());
						}
					}
					 
				}
			}
			
			 

		});  
		
	}

	/**
	 * Returns the table.
	 * 
	 * @return table component
	 */
	public JTable getTable() {
		return table;
	}
	
	/**
	 * Returns the table.
	 * 
	 * @return table component
	 */
	public Object[][] getData() {
		return data;
	}
	public int getHeaderNameIndex(String name) {
		
		 for(int i =0; i<headerNames.length; i++)
		      if (headerNames[i].equalsIgnoreCase(name))
		    	  return i;
		 return -1;
	}

	/**
	 * Internal Classes
	 * 
	 */
	private class LincsViewerTableModel extends AbstractTableModel {
		private static final long serialVersionUID = 647372910387245086L;

		public int getColumnCount() {
			return headerNames.length;
		}

		public int getRowCount() {
			if (data == null)
				return 0;
			else
				return data.length;
		}

		public Object getValueAt(int row, int col) {
			if (data != null && row < data.length
					&& col < data[row].length)
                return data[row][col];			 
			return null;
		}

		public String getColumnName(int index) {
			return headerNames[index];
		}		
		
		
		@SuppressWarnings({ "rawtypes", "unchecked" })
		public Class getColumnClass(int column) {
	        Class returnValue = null;
	        if (getColumnName(column).equalsIgnoreCase("Include"))
	        	return Boolean.class;
	        if ((column >= 0) && (column < getColumnCount())) {
	          if (getValueAt(0, column) != null)
	            returnValue = getValueAt(0, column).getClass();
	        } else {
	          returnValue = Object.class;
	        }
	        return returnValue;
	      }
		

	}

	private class CellRenderer extends DefaultTableCellRenderer {
		private static final long serialVersionUID = -2697909778548788305L;	 

		/**
		 * Renders basic data input types JLabel, Color,
		 */
		public Component getTableCellRendererComponent(JTable jTable,
				Object obj, boolean param, boolean param3, int row, int col) {
			 
			Component c = super.getTableCellRendererComponent(table, obj,
					param, param3, row, col);
			 
			return c;
		}
		
		/*
		 * (non-Javadoc)
		 * @see javax.swing.table.DefaultTableCellRenderer#setValue(java.lang.Object)
		 */
		public void setValue(Object value) {
			if (oldCellBgColor == null)
				oldCellBgColor = this.getBackground();			 
			setBackground(oldCellBgColor);				 
			if (oldCellfgColor == null)
				oldCellfgColor = this.getForeground();			 
			setForeground(oldCellfgColor);	
			if ((value != null) && (value instanceof Number)) {
				if (((Number) value).doubleValue() < 0.1)
					value = String.format("%.2E", value);
				else
					value = String.format("%.8f", value);
			}			
			super.setValue(value);			 
			if (value != null && (value instanceof ValueObject) )
			{
				ValueObject v = (ValueObject)value;
				if (v.getReferenceId() > 0)
				   setText("<html><font color=blue><u><b>" + value.toString() + "</b></u></font></html>");
			 
				 
			}
			else if (isColorGradient && value != null && (value instanceof ScoreObject) )
			{
				
				ScoreObject v = (ScoreObject)value;				 
				if (v.getColor() != null)				{
					
					setBackground(v.getColor());
				}
				 
			}
			
		}
	}
	
	 
	static void export(JTable aTable, List<String> hideColumns, Component parent) {
		JFileChooser jFC = new JFileChooser();

		// We remove "all files" from filter, since we only allow CSV format
		FileFilter ft = jFC.getAcceptAllFileFilter();
		jFC.removeChoosableFileFilter(ft);

		TabularFileFilter filter = new TabularFileFilter();
		jFC.setFileFilter(filter);

		int returnVal = jFC.showSaveDialog(parent);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			try {
				String tabFilename;
				tabFilename = jFC.getSelectedFile().getAbsolutePath();
				if (!tabFilename.toLowerCase().endsWith(
						"." + filter.getExtension().toLowerCase())) {
					tabFilename += "." + filter.getExtension();
				}
				BufferedWriter out = new BufferedWriter(new FileWriter(
						tabFilename));
				out.write(toCVS(aTable, hideColumns));
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}	
	
	private static class TabularFileFilter extends FileFilter {
		public String getDescription() {
			return "CSV Files";
		}

		public boolean accept(File f) {
			String name = f.getName();
			boolean tabFile = name.endsWith("csv") || name.endsWith("CSV");
			if (f.isDirectory() || tabFile) {
				return true;
			}

			return false;
		}

		public String getExtension() {
			return "csv";
		}

	}

	private static String toCVS(JTable aTable, List<String> hideColumns) {
		String answer = "";

		boolean newLine = true;   
		//print header
		for (int cx = 0; cx < aTable.getColumnCount(); cx++) {
			if (newLine) {
				if (hideColumns != null && !hideColumns.contains(aTable.getColumnName(cx)))
				{
					newLine = false;
					answer += "\"" + aTable.getColumnName(cx) + "\"";
				}
			} else {
				if (hideColumns != null && !hideColumns.contains(aTable.getColumnName(cx)))
				{
					answer += ",";
					 answer += "\"" + aTable.getColumnName(cx) + "\"";
				}
			}
			 
		}
		answer += "\n";
		newLine = true;

		// print the table
		for (int cx = 0; cx < aTable.getRowCount(); cx++) {
			if (! isInclude(aTable, cx))
				continue;
			for (int cy = 0; cy < aTable.getColumnCount(); cy++) {				
				if (newLine) {					
					if (hideColumns != null && !hideColumns.contains(aTable.getColumnName(cy)))
					{   
						newLine = false;
						answer += "\"" + aTable.getValueAt(cx, cy) + "\"";
					}
				} else {
					if (hideColumns != null && !hideColumns.contains(aTable.getColumnName(cy)))
					{   
						answer += ",";
						answer += "\"" + aTable.getValueAt(cx, cy) + "\"";
					}
					
				}
				
			}
			answer += "\n";
			newLine = true;
		}
		return answer;
	}
	
	//this is used for fmoa data table
    static boolean isInclude(JTable aTable, int row)
	{
		boolean includeFlag = true;
		if( aTable.getModel().getValueAt(row, 3) instanceof Boolean )
		{
			includeFlag =  ((Boolean) aTable.getModel().getValueAt(row, 3)).booleanValue();
		}
		return includeFlag;
	}
    
    public void setColorGradient(boolean isColorGradient)
    {
    	this.isColorGradient = isColorGradient;
    	table.updateUI();
    }
	
}
