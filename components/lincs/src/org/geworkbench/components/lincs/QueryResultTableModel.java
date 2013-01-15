package org.geworkbench.components.lincs;

import java.util.Vector;

import javax.swing.table.AbstractTableModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class QueryResultTableModel extends AbstractTableModel {

	private static final long serialVersionUID = 3421203989282530166L;

	private Log log = LogFactory.getLog(QueryResultTableModel.class);

	final int COLUMN_COUNT = 9;

	// TODO populate the content
	public void setValues(Vector<String[]> data) {
	}

	int rowCount = 10; // FIXME 10 only for test. initially it should be 0.

	@Override
	public int getRowCount() {
		log.debug("getting row count");
		return rowCount;
	}

	@Override
	public int getColumnCount() {
		return COLUMN_COUNT;
	}

	// TODO get the content
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		return "NOT IMPLEMENTED YET";
	}

	@Override
	public String getColumnName(int col) {
		return columnNames[col];
	}

	private static String[] columnNames = { "Tissue Type", "Cell Line",
			"Drug 1", "Drug 2", "Assay Type", "Synergy Measurement Type",
			"Score", "P-value", "Titration Curve" };

}
