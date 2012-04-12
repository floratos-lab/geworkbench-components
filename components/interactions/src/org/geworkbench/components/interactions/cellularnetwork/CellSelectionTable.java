package org.geworkbench.components.interactions.cellularnetwork;

import javax.swing.JTable;

/**
 * A JTable which allows any selection of cells.
 * 
 * @author Min You
 * @version $Id$
 */
public class CellSelectionTable extends JTable {

	private static final long serialVersionUID = 4467886940225929566L;
	private final TableSelectionModel tableSelectionModel = new TableSelectionModel();

	/**
	 * Doing almost the same as its parent except creating its own
	 * SelectionModel and UI
	 */
	public CellSelectionTable() {
		super();

		// The model needs to know how many columns are there
		tableSelectionModel.setColumns(getColumnModel().getColumnCount());
		getModel().addTableModelListener(tableSelectionModel);

		addPropertyChangeListener(tableSelectionModel);

		firePropertyChange("tableSelectionModel", null, tableSelectionModel);
		setUI(new CellSelectionTableUI());
	}

	/**
	 * refers to its TableSelectionModel.
	 */
	@Override
	public boolean isCellSelected(int row, int column) {
		return tableSelectionModel.isSelected(row,
				convertColumnIndexToModel(column));
	}

	/**
	 * @return the current TableSelectionModel.
	 */
	public TableSelectionModel getTableSelectionModel() {
		return tableSelectionModel;
	}
}
