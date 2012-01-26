package org.geworkbench.components.sequenceretriever;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.geworkbench.bison.datastructure.bioobjects.sequence.DSSequence;

/*
 * Table model for the 'line view' of retrieved sequences, 
 * which is implemented as RetrievedSequenceDisplayPanel.
 */
class RetrievedSequenceTableModel extends AbstractTableModel {
	/**
	 * 
	 */
	private final RetrievedSequenceDisplayPanel retrievedSequenceDisplayPanel;

	private static final long serialVersionUID = -6009654606398029902L;

	private List<String> sequenceNames;
	
	RetrievedSequenceTableModel(RetrievedSequenceDisplayPanel retrievedSequenceDisplayPanel) {
		this.retrievedSequenceDisplayPanel = retrievedSequenceDisplayPanel;
		resetNameList(false);
	}
	
	void resetNameList(boolean hideDuplicate) {
		if(this.retrievedSequenceDisplayPanel.sequenceDB==null)return;
		
		sequenceNames = new ArrayList<String>();
		List<String> startingSites = new ArrayList<String>();
		
		for(DSSequence sequence: this.retrievedSequenceDisplayPanel.sequenceDB) {
			String n = sequence.toString();
			int i = n.lastIndexOf("_");
			i = n.lastIndexOf("_", i);
			String startingSite = n.substring(i);
			if(!hideDuplicate || !startingSites.contains(startingSite)) {
				sequenceNames.add(n);
				startingSites.add(startingSite);
			}
		}
		fireTableDataChanged();
	}
	
	@Override
	public String getColumnName(int column) {
		switch (column) {
		case 0:
			return "Include";
		case 1:
			return "Name";
		case 2:
			return "Sequence Detail";
		default:
			return "Sequence Detail";
		}
	}

	@Override
	public int getRowCount() {
		if (sequenceNames == null) {
			return 0;
		} else {
			return sequenceNames.size();
		}
	}

	@Override
	public int getColumnCount() {
		if (sequenceNames == null) {
			return 0;
		} else {
			return 3;
		}
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		String sequenceName = sequenceNames.get(rowIndex);
		RetrievedSequenceView retrievedSequenceView = this.retrievedSequenceDisplayPanel
				.getRetrievedSequenceView(sequenceName);
		if (retrievedSequenceView == null) {
			return null;
		}
		switch (columnIndex) {
		case 0:
			return retrievedSequenceView.isIncluded();
		case 1:
			if (this.retrievedSequenceDisplayPanel.sequenceDB.isDNA()) {
				return sequenceName;
			} else {
				return "<html><font  color=\"#0000FF\"><u>"
						+ sequenceName + "</u></font>";
			}
		case 2:
			return retrievedSequenceView;
		default:
			return sequenceName;
		}
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		switch (columnIndex) {
		case 0:
			return Boolean.class;
		case 2:
			return RetrievedSequenceView.class;
		default:
			return String.class;
		}
	}

	/*
	 * returns if the cell is editable; returns false for all cells in
	 * columns except the first column (check box)
	 */
	@Override
	public boolean isCellEditable(int row, int col) {
		return col == 0;
	}

	/*
	 * detect change in cell at (row, col); set cell to value; update the
	 * table
	 */
	@Override
	public void setValueAt(Object value, int row, int col) {
		String sequenceName = sequenceNames.get(row);
		if (col==0 && sequenceName != null && value != null) {
			RetrievedSequenceView retrievedSequenceView = this.retrievedSequenceDisplayPanel
					.getRetrievedSequenceView(sequenceName);
			retrievedSequenceView.setIncluded(((Boolean) value) // FIXME java.lang.NullPointerException
					.booleanValue());
//			fireTableCellUpdated(row, col);
		}
	}

}