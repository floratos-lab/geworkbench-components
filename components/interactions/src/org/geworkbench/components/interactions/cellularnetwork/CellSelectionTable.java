package org.geworkbench.components.interactions.cellularnetwork;

import javax.swing.JTable;
import javax.swing.table.TableModel;

/**
 * A JTable which allows any selection of cells.
 * 
 * @author Min You
 */
public class CellSelectionTable extends JTable {

	private static final long serialVersionUID = 4467886940225929566L;
	protected TableSelectionModel tableSelectionModel;

	/**
	 * Doing almost the same as its parent except creating its own
	 * SelectionModel and UI
	 */
	public CellSelectionTable() {
		super();
		createDefaultTableSelectionModel();
		setUI(new CellSelectionTableUI());

	}

	/**
	 * Doing almost the same as its parent except creating its own
	 * SelectionModel and UI
	 */
	public CellSelectionTable(TableModel dm) {
		super(dm);
		createDefaultTableSelectionModel();
		setUI(new CellSelectionTableUI());
	}

	/**
	 * refers to its TableSelectionModel.
	 */
	public boolean isCellSelected(int row, int column) {
		return tableSelectionModel.isSelected(row,
				convertColumnIndexToModel(column));
	}

	/**
	 * Creates a default TableSelectionModel.
	 */
	public void createDefaultTableSelectionModel() {
		TableSelectionModel tsm = new TableSelectionModel();
		setTableSelectionModel(tsm);
	}

	/**
	 * same intention as setSelectionModel(ListSelectionModel newModel)
	 */
	public void setTableSelectionModel(TableSelectionModel newModel) {
		// the TableSelectionModel shouldn't be null
		if (newModel == null) {
			throw new IllegalArgumentException(
					"Cannot set a null TableSelectionModel");
		}

		// save the old Model
		TableSelectionModel oldModel = this.tableSelectionModel;
		// set the new Model
		this.tableSelectionModel = newModel;
		// The model needs to know how many columns are there
		newModel.setColumns(getColumnModel().getColumnCount());
		getModel().addTableModelListener(newModel);

		if (oldModel != null) {
			removePropertyChangeListener(oldModel);
		}
		addPropertyChangeListener(newModel);

		firePropertyChange("tableSelectionModel", oldModel, newModel);
	}

	/**
	 * @return the current TableSelectionModel.
	 */
	public TableSelectionModel getTableSelectionModel() {
		return tableSelectionModel;
	}
}
