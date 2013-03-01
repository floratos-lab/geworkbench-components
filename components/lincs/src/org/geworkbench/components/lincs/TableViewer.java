/**
 * 
 */
package org.geworkbench.components.lincs;

/**
 * @author yc2480
 * @version $Id: TableViewer.java 9733 2012-07-24 13:42:11Z zji $
 * 
 */
import java.awt.BorderLayout; 
import java.awt.Component; 
import java.util.Enumeration; 
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
 
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
		this.data = data;
		this.headerNames = headerNames;
		model = new LincsViewerTableModel();

		table = new JTable(model);
		table.setAutoCreateRowSorter(true);

		Enumeration<TableColumn> columns = table.getColumnModel().getColumns();
		while (columns.hasMoreElements()) {
			columns.nextElement().setCellRenderer(new CellRenderer());
		}

		setLayout(new BorderLayout());
		add(new JScrollPane(table), BorderLayout.CENTER);
	}

	/**
	 * Returns the table.
	 * 
	 * @return table component
	 */
	public JTable getTable() {
		return this.table;
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
	        Class returnValue;
	        if ((column >= 0) && (column < getColumnCount())) {
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
			if ((value != null) && (value instanceof Number)) {
				if (((Number) value).doubleValue() < 0.1)
					value = String.format("%.2E", value);
				else
					value = String.format("%.2f", value);
			}
			super.setValue(value);
		}
	}
	
}
