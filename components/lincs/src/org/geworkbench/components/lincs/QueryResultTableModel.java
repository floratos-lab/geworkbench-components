package org.geworkbench.components.lincs;

import java.awt.BorderLayout;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
 

public class QueryResultTableModel extends AbstractTableModel {

	private static final long serialVersionUID = 3421203989282530166L;

	private Log log = LogFactory.getLog(QueryResultTableModel.class);

	private Object[][] data;
	private String[] columnNames;
	
	
	/** Creates a new instance of TableViewer */
	public QueryResultTableModel() {
	}

	/**
	 * Creates a new TableViewer with header names and data.
	 * 
	 * @param headerNames
	 *            Header name strings.
	 * @param data
	 *            table data
	 */
	public QueryResultTableModel(final String[] columnNames, final Object[][] data) {
		this.data = data;
		this.columnNames = columnNames;	 
	}
	
  
	@Override
	public int getRowCount() {
		if (data == null)
			return 0;
		else
			return data.length;
	}

	@Override
	public int getColumnCount() {
		if (columnNames != null)
		   return columnNames.length;
		else
		return 0;
	}

	// TODO get the content
	@Override
	public Object getValueAt(int row, int col) {
		if (data != null && row < data.length
				&& col < data[row].length)
            return data[row][col];			 
		return null;
	}

	@Override
	public String getColumnName(int col) {
		return columnNames[col];
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
	
 
