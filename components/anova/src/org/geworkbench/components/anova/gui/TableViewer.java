/**
 * 
 */
package org.geworkbench.components.anova.gui;

/**
 * @author yc2480
 * @version $Id$
 * 
 */
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.util.Enumeration;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel; 

public class TableViewer extends JPanel {
	private static final long serialVersionUID = -1164124089849089214L;
	
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
		model = new AnovaViewerTableModel();

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
	 * Internal Classes
	 * 
	 */
	private class AnovaViewerTableModel extends AbstractTableModel {
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
		
		private JPanel colorPanel = new JPanel();
		private JLabel label;
		private JTextArea textArea;

		/**
		 * Renders basic data input types JLabel, Color,
		 */
		public Component getTableCellRendererComponent(JTable jTable,
				Object obj, boolean param, boolean param3, int row, int col) {
			if (col == 0) {
				Component c = super.getTableCellRendererComponent(table, obj,
						param, param3, row, col);
				c.setBackground(Color.lightGray);
				((JLabel) c).setHorizontalAlignment(JLabel.LEFT);
				((JLabel) c).setBorder(BorderFactory.createRaisedBevelBorder());
				return c;
			} else if (obj instanceof Color) {
				colorPanel.setBackground((Color) obj);
				return colorPanel;
			} else if (obj instanceof JLabel) {
				label = (JLabel) obj;
				label.setOpaque(true);
				label.setFont(new Font("Arial", Font.PLAIN, 12));
				label.setBackground(new Color(225, 0, 0));
				label.setForeground(Color.black);
				label.setHorizontalAlignment(JLabel.CENTER);
				if (table.isRowSelected(row))
					label.setBackground(table.getSelectionBackground());
				return label;
			} else if (obj instanceof JTextArea) {
				textArea = (JTextArea) obj;
				if (table.isRowSelected(row))
					textArea.setBackground(table.getSelectionBackground());
				return textArea;
			}
			Component c = super.getTableCellRendererComponent(table, obj,
					param, param3, row, col);
			((JLabel) c).setHorizontalAlignment(JLabel.RIGHT);

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
