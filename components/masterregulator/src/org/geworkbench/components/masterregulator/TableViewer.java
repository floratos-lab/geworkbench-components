/**
 * 
 */
package org.geworkbench.components.masterregulator;

/**
 * @author yc2480
 * @version $Id$
 * 
 */
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.Expression;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.engine.properties.PropertiesManager;
import org.geworkbench.util.OWFileChooser;

public class TableViewer extends JPanel {
	private static final long serialVersionUID = -5229999809803617742L;

	private Log log = LogFactory.getLog(TableViewer.class);
	private static final String EXPORTDIR = "exportDir";

	protected JTable table;
	protected TableModel model;
	protected JScrollPane pane;

	Object[][] data;
	String[] headerNames;

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
	public TableViewer(String[] headerNames, Object[][] data) {
		this.data = data;
		this.headerNames = headerNames;
		model = new DefaultViewerTableModel(headerNames, data);

		table = new JTable(model);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.getTableHeader().addMouseListener(new TableHeaderMouseListener());

		// table.getColumnModel().getColumn(0).setCellRenderer(new
		// CellRenderer());
		// table.getColumnModel().getColumn(1).setCellRenderer(new
		// CellRenderer());
		Enumeration<TableColumn> columns = table.getColumnModel().getColumns();
		while (columns.hasMoreElements()) {
			columns.nextElement().setCellRenderer(new CellRenderer());
			Thread.yield();
		}

		pane = new JScrollPane(table);
		exportButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				if (table.getRowCount() > 0)
					exportTableData();
			}
		});

		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		add(pane);
		this.setAlignmentX(LEFT_ALIGNMENT);

	}
	JButton exportButton = new JButton("Export table");

	public Expression getExpression() {
		return new Expression(this, this.getClass(), "new", new Object[] {
				headerNames, data });
	}

	/**
	 * Allows the substitution of a specific table model.
	 * 
	 * @param model
	 *            This model replaces the TableViewer's default TableModel.
	 */
	public void setTableModel(TableModel model) {
		this.model = model;
		table.setModel(model);
	}

	public void setTableModel(Object[][] data) {
		this.model = new DefaultViewerTableModel(headerNames, data);
		;
		table.setModel(model);
		Enumeration<TableColumn> columns = table.getColumnModel().getColumns();
		while (columns.hasMoreElements()) {
			columns.nextElement().setCellRenderer(new CellRenderer());
			Thread.yield();
		}
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
	 * Returns the active TableModel.
	 * 
	 * @return The tables data model
	 */
	public TableModel getTableModel() {
		return this.table.getModel();
	}

	/**
	 * Returns the table's header component
	 * 
	 * @return table header
	 */
	public JTableHeader getTableHeader() {
		return this.table.getTableHeader();
	}

	/**
	 * Indicates that the indexed column should be set to numerical regardles of
	 * the object type. This will assist in proper sorting if a numerical column
	 * is represented by Strings. (By default columns are not numerical)
	 * 
	 * @param columnIndex
	 *            index to the table column
	 * @param setting
	 *            sets as numerical.
	 */
	public void setNumerical(int columnIndex, boolean setting) {
		if (this.model instanceof DefaultViewerTableModel)
			((DefaultViewerTableModel) this.model).setNumerical(columnIndex,
					setting);
	}

	public boolean isNumerical(int columnIndex) {
		if (this.model instanceof DefaultViewerTableModel)
			return ((DefaultViewerTableModel) this.model)
					.isNumerical(columnIndex);
		return false;
	}

	public int getSelectedRow() {
		int index = table.getSelectedRow();
		if (index < 0)
			index = -1;
		else
			index = ((DefaultViewerTableModel) this.model).getRow(index);
		return index;
	}

	/**
	 * Internal Classes
	 * 
	 */
	public class DefaultViewerTableModel extends AbstractTableModel {

		private static final long serialVersionUID = 1L;
		String[] columnNames;
		Object[][] tableData;
		boolean[] numerical;
		Row[] rows;
		int colToSort = 0;
		boolean ascending = false;

		/**
		 * This inner class is used to support basic manipulation of the table.
		 * The table helps to support ascending and descending row sorting based
		 * on numerical or alphabetical column contents.
		 * 
		 * @param headerNames
		 *            header names
		 * @param data
		 *            data matrix
		 */
		public DefaultViewerTableModel(String[] headerNames, Object[][] data) {
			columnNames = headerNames;
			tableData = data;
			numerical = new boolean[headerNames.length];
			rows = new Row[data.length];
			for (int i = 0; i < rows.length; i++) {
				rows[i] = new Row();
				rows[i].index = i;
			}
		}

		/**
		 * Sets column as numerical for sorting.
		 * 
		 * @param col
		 *            column index
		 * @param numericalBool
		 *            sets as numerical or not numerical
		 */
		public void setNumerical(int col, boolean numericalBool) {
			if (col > -1 && col < numerical.length)
				numerical[col] = numericalBool;
		}

		public int getColumnCount() {
			return columnNames.length;
		}

		public int getRowCount() {
			if (tableData == null)
				return 0;
			else
				return tableData.length;
		}

		public Object getValueAt(int param, int param1) {
			if (tableData != null && param < tableData.length
					&& param1 < tableData[param].length)
				return tableData[rows[param].index][param1];
			return null;
		}

		public String getColumnName(int index) {
			return columnNames[index];
		}

		private boolean isNumerical(int col) {
			return numerical[col];
		}

		public void sort(int col) {
			ascending = !ascending;
			colToSort = col;
			Arrays.sort(rows);
			fireTableDataChanged();
		}

		public int getRow(int tableRow) {
			return rows[tableRow].index;
		}

		private class Row implements Comparable<Object>, java.io.Serializable {
			private static final long serialVersionUID = 1L;
			public int index;
			private String myString, otherString;

			/*
			 * (non-Javadoc)
			 * 
			 * @see java.lang.Comparable#compareTo(java.lang.Object)
			 */
			public int compareTo(Object other) {
				if (ascending)
					return compareToOther(other);
				return compareToOther(other) * (-1);
			}

			@SuppressWarnings("unchecked")
			public int compareToOther(Object other) {
				Row otherRow = (Row) other;
				Object myObject = tableData[index][colToSort];
				Object otherObject = tableData[otherRow.index][colToSort];
				if (myObject instanceof Comparable) {
					if (isNumerical(colToSort)) { // catch string designation
						// of a number
						if (myObject instanceof String) {
							if(((String)myObject).equalsIgnoreCase("Inf")) return 1;
							if(((String)otherObject).equalsIgnoreCase("Inf")) return -1;
							try{
								Float myFloat = new Float((String) myObject);
								Float otherFloat = new Float((String) otherObject);
								return myFloat.compareTo(otherFloat);
							}catch(NumberFormatException e){
								log.error("NumberFormatException: "+e.getMessage());
								return 1;
							}
						}
					}
					return ((Comparable<Object>) myObject)
							.compareTo(otherObject);
				}
				if (myObject instanceof JLabel) {
					myString = ((JLabel) (myObject)).getText();
					otherString = ((JLabel) (otherObject)).getText();
					return myString.compareTo(otherString);
				} else if (myObject instanceof JRadioButton) {
					myString = ((JRadioButton) (myObject)).getText();
					otherString = ((JRadioButton) (otherObject)).getText();
					return myString.compareTo(otherString);
				} else
					return index - otherRow.index;
			}
		}

	}

	public class CellRenderer extends DefaultTableCellRenderer {

		private static final long serialVersionUID = 1L;
		JPanel colorPanel = new JPanel();
		JLabel label;
		JTextArea textArea;
		JRadioButton jRadioButton;

		/**
		 * Renders basic data input types JLabel, Color,
		 */
		public Component getTableCellRendererComponent(JTable jTable,
				Object obj, boolean param, boolean param3, int row, int col) {
			// System.out.println(obj.getClass().getName());
			if (obj instanceof JRadioButton) {
				jRadioButton = (JRadioButton) obj;
				if (table.isRowSelected(row) && (!jRadioButton.isSelected())) {
					// means user selected a new row, we need to clean the old
					// one
					for (int cx = 0; cx < model.getRowCount(); cx++) {
						JRadioButton oldButton = (JRadioButton) model
								.getValueAt(cx, col);
						oldButton.setSelected(false);
					}
				}
				jRadioButton.setSelected(table.isRowSelected(row));
				jRadioButton.setBackground(Color.white);
				return jRadioButton;
			} else if (col == 0) {
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
			} else if (col == 1 && !isNumerical(col)) {
				Component c = super.getTableCellRendererComponent(table, obj,
						param, param3, row, col);
				((JLabel) c).setHorizontalAlignment(JLabel.LEFT);
				return c;
			}
			Component c = super.getTableCellRendererComponent(table, obj,
					param, param3, row, col);
			((JLabel) c).setHorizontalAlignment(JLabel.RIGHT);

			return c;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * javax.swing.table.DefaultTableCellRenderer#setValue(java.lang.Object)
		 */
		public void setValue(Object value) {
			if ((value != null)
					&& ((value instanceof Double) || (value instanceof Float))) {
				if (((Number) value).doubleValue() < 0.1)
					value = String.format("%.2E", value);
				else
					value = String.format("%.2f", value);
			} else if ((value != null) && (value instanceof Integer)) {
				value = String.valueOf(value);
			}
			super.setValue(value);
		}
	}

	/**
	 * 
	 * @author yc2480
	 * 
	 */
	public class TableHeaderMouseListener extends MouseAdapter {

		public void mouseClicked(MouseEvent evt) {
			// if(evt.getModifiers() == MouseEvent.BUTTON1_MASK &&
			// evt.getClickCount() > 1){
			if (evt.getModifiers() == MouseEvent.BUTTON1_MASK) {
				if (model instanceof DefaultViewerTableModel) {
					JTableHeader header = (JTableHeader) evt.getSource();
					int tableCol = header.columnAtPoint(evt.getPoint());
					int modelCol = table.convertColumnIndexToModel(tableCol);
					((DefaultViewerTableModel) model).sort(modelCol);
					if (mrviewer != null)   mrviewer.updateGraph();
				}
			}
		}
	}

	private MasterRegulatorViewer mrviewer = null;
	void setMRViewer(MasterRegulatorViewer v){
		mrviewer = v;
	}

	public void exportTableData() {

		try {
			String exportFileStr = "exportTable.csv";
			PropertiesManager properties = PropertiesManager.getInstance();
			String exportDir = properties.getProperty(this.getClass(),
					EXPORTDIR, exportFileStr);
			File exportFile = new File(exportDir);
			OWFileChooser chooser = new OWFileChooser(exportFile);
			chooser.setAcceptAllFileFilterUsed(false);
			chooser.addChoosableFileFilter(new ExportFileFilter(".csv", "Comma Separated Value Files (.csv)", ","));
			chooser.addChoosableFileFilter(new ExportFileFilter(".txt", "Tab Separated Value Files (.txt)", "\t"));
			chooser.setDialogTitle("Export MRA Table Results");
			int c = chooser.showSaveDialog(table);
			if (c == OWFileChooser.APPROVE_OPTION
					&& chooser.getSelectedFile() != null) {
				String extension = ".csv"; // default
				String delimiter = ","; // default
				FileFilter chosenFilter = chooser.getFileFilter();
				if(chosenFilter instanceof ExportFileFilter) {
					ExportFileFilter exportFileFilter = (ExportFileFilter)chosenFilter;
					extension = exportFileFilter.getExtension();
					delimiter = exportFileFilter.getDelimiter();
				}
				
				exportFileStr = chooser.getSelectedFile().getPath();
				properties.setProperty(this.getClass(), EXPORTDIR, chooser
						.getSelectedFile().getParent());
				if (!exportFileStr.endsWith(extension))
					exportFileStr += extension;
				BufferedWriter out = new BufferedWriter(new FileWriter(
						exportFileStr));
				// foreach tfA
				TableModel model = table.getModel();

				for (int i = 0; i < model.getColumnCount()-1; i++) {
					out.write(model.getColumnName(i) + delimiter);
				}
				out.write( model.getColumnName(model.getColumnCount()-1) );
				out.write("\n");

				for (int i = 0; i < model.getRowCount(); i++) {
					for (int j = 0; j < model.getColumnCount(); j++) {
						Object obj = model.getValueAt(i, j);
						if (obj instanceof JRadioButton)
							obj = ((JRadioButton) obj).getText();
						else if (obj instanceof JLabel)
							obj = ((JLabel) (obj)).getText();
						else if ((obj instanceof Double)
								|| (obj instanceof Float)) {
							if (((Number) obj).doubleValue() < 0.1)
								obj = String.format("%.2E", obj);
							else
								obj = String.format("%.2f", obj);

						} else if (obj instanceof Integer) {
							obj = String.valueOf(obj);
						}

						if(j<model.getColumnCount()-1) {
							out.write(obj.toString() + delimiter);
						} else {
							out.write(obj.toString());
						}
					}
					out.write("\n");
				}

				out.close();
				JOptionPane.showMessageDialog(null, "File " + exportFileStr
						+ " has been saved.", "File saved.",
						JOptionPane.INFORMATION_MESSAGE);
			} else {
				// user canceled
			}
		} catch (IOException ioe) {
			log.error(ioe);
		}

	}

	/**
	 * Returns a component to be inserted into scroll pane view port.
	 * 
	 * @return content component (JTable)
	 */
	public JComponent getContentComponent() {
		return this.table;
	}

	/**
	 * Returns a component to be inserted into scroll pane header.
	 * 
	 * @return table header component.
	 */
	public JComponent getHeaderComponent() {
		return table.getTableHeader();
	}

}
