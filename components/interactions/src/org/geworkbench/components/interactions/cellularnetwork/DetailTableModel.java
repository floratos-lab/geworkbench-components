package org.geworkbench.components.interactions.cellularnetwork;

import java.util.Vector;

import javax.swing.table.DefaultTableModel;

import org.geworkbench.util.network.CellularNetWorkElementInformation;

class DetailTableModel extends DefaultTableModel {

	private static final long serialVersionUID = -3314439294428139176L;

	final private CellularNetworkKnowledgeWidget widget;
	private String[] columnLabels;

	DetailTableModel(CellularNetworkKnowledgeWidget widget,
			String[] columnLabels) {
		this.widget = widget;
		this.columnLabels = columnLabels;
	}

	@Override
	public int getColumnCount() {
		return columnLabels.length;
	}

	@Override
	public int getRowCount() {
		if(widget==null) return 0;
		
		Vector<CellularNetWorkElementInformation> hits = widget.getHits();
		if (hits != null)
			return hits.size();
		return 0;
	}

	@Override
	public String getColumnName(int index) {

		if (index >= 0 && index < columnLabels.length)
			return columnLabels[index];
		else
			return "";

	}

	/* get the Object data to be displayed at (row, col) in table */
	@Override
	public Object getValueAt(int row, int col) {

		Vector<CellularNetWorkElementInformation> hits = widget.getHits();

		if (hits == null || hits.size() == 0)
			return null;
		CellularNetWorkElementInformation hit = hits.get(row);
		/* display data depending on which column is chosen */
		switch (col) {

		case 0:
			return hit.getdSGeneMarker().getLabel();
		case 1:
			return hit.getdSGeneMarker().getGeneName();
		case 2:
			return hit.getGeneType();
		case 3:
			return hit.getGoInfoStr();
		default:
			String interactionType = columnLabels[col].substring(
					0,
					columnLabels[col].length()
							- Constants.COLUMNLABELPOSTFIX.length());
			Integer num = hit.getInteractionNum(interactionType);
			if (num != null)
				return num;
			else
				return 0;

		}

	}

	/* returns the Class type of the column c */
	@Override
	public Class<?> getColumnClass(int c) {
		if (getValueAt(0, c) != null) {
			return getValueAt(0, c).getClass();
		}
		return String.class;
	}

	/*
	 * returns if the cell is editable; returns false for all cells in columns
	 * except column 6
	 */
	@Override
	public boolean isCellEditable(int row, int col) {
		// Note that the data/cell address is constant,
		// no matter where the cell appears onscreen.
		return false;
	}

	public void setColumnLabels(String[] columnLabels) {
		this.columnLabels = columnLabels;
	}

}