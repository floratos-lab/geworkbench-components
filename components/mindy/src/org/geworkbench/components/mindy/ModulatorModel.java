package org.geworkbench.components.mindy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;

/**
 * Modulator table data model.
 *
 * @author mhall
 * @author ch2514
 * @author oshteynb
 * @version $Id: ModulatorModel.java,v 1.2 2009-04-27 15:49:02 keshav Exp $
 */
class ModulatorModel extends AbstractTableModel {
	final MindyPlugin mindyPlugin;

	private boolean[] enabled;

	// TODO probably should be in UserSelections
//	private ArrayList<DSGeneMarker> selectedModulators;

	private ArrayList<DSGeneMarker> modulators;

	/*  do we need these ones*/
//	private List<DSGeneMarker> limitedModulators;


	private MindyData mindyData;

	private String[] columnNames = new String[] { " ", "Modulator", " M# ",
			" M+ ", " M- ", " Mode ", "Modulator Description" };

	private boolean[] ascendSortStates;

	private boolean showProbeName = false;

	//TODO selections, need to go, keep it here for now
	private ModulatorSelections selections;

	/**
	 * Constructor.
	 *
	 * @param mindyData -
	 *            MINDY data
	 * @param mindyPlugin TODO
	 */
	public ModulatorModel(MindyPlugin mindyPlugin, MindyData mindyData) {
		this.mindyPlugin = mindyPlugin;
		this.mindyData = mindyData;

		this.selections = new ModulatorSelections(mindyPlugin, this);

		this.showProbeName = !mindyData.isAnnotated();

		modulators = (ArrayList<DSGeneMarker>) mindyData.getModulators();

		this.enabled = new boolean[modulators.size()];

/*		this.limitedModulators = new ArrayList<DSGeneMarker>(modulators
				.size());
*/
//		this.selectedModulators = new ArrayList<DSGeneMarker>();

		this.ascendSortStates = new boolean[columnNames.length];
		for (int i = 0; i < this.ascendSortStates.length; i++)
			this.ascendSortStates[i] = true;
	}

	public ModulatorSelections getSelections() {
		return selections;
	}

	void rememberSelections() {
		enabled = new boolean[modulators.size()];
		int size = modulators.size();
		for (int i = 0; i < size; i++) {
			if (this.selections.getSelectedModulators().contains(modulators.get(i)))
				enabled[i] = true;
			else
				enabled[i] = false;
		}
	}

	/**
	 * Get the number of columns in the modulator table.
	 *
	 * @return the number of columns in the modulator table
	 */
	public int getColumnCount() {
		return columnNames.length;
	}

	/**
	 * Get the number of rows on the modulator table.
	 *
	 * @return number of rows on the table
	 */
	public int getRowCount() {
		if (enabled == null) {
			return 0;
		} else {
			return modulators.size();
		}

/*		if (this.mindyPlugin.globalSelectionState.allMarkerOverride) {
			if (enabled == null) {
				return 0;
			} else {
				return modulators.size();
			}

		} else {

			if (limitedModulators == null) {
				return 0;
			}
			return limitedModulators.size();

		}
*/
	}

	/**
	 * Whether or not the specified modulator table cell is editable.
	 *
	 * @param rowIndex -
	 *            row index of the table cell
	 * @prarm columnIndex - column index of the table cell
	 * @return true if the table cell is editable, and false otherwise
	 */
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		if (columnIndex == 0) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Get the class object representing the specified table column.
	 *
	 * @param columnIndex -
	 *            column index
	 * @return the class object representing the table column
	 */
	@SuppressWarnings("unchecked")
	public Class getColumnClass(int columnIndex) {
		if (columnIndex == 0) {
			return Boolean.class;
		} else if (columnIndex == 1) {
			return String.class;
		} else if (columnIndex == getColumnCount() - 1
				|| columnIndex == getColumnCount() - 2) {
			return String.class;
		} else {
			return Integer.class;
		}
	}

	/**
	 * Get the values of modulator table cells.
	 *
	 * @param rowIndex -
	 *            row index of the cell
	 * @param columnIndex -
	 *            column index of the cell
	 * @return the value object of specified table cell
	 */
	public Object getValueAt(int rowIndex, int columnIndex) {
		DSGeneMarker mod;
		mod = getModulatorForIndex(rowIndex);
		if (columnIndex == 0) {
			return enabled[rowIndex];
		} else if (columnIndex == 1) {
			return MindyPlugin.getMarkerDisplayName(this.isShowProbeName(), mod);
		} else if (columnIndex == 2) {
			return mindyData.getFilteredStatistics(mod).getCount();
		} else if (columnIndex == 3) {
			return mindyData.getFilteredStatistics(mod).getMover();
		} else if (columnIndex == 4) {
			return mindyData.getFilteredStatistics(mod).getMunder();
		} else if (columnIndex == 5) {
			int mover = mindyData.getFilteredStatistics(mod).getMover();
			int munder = mindyData.getFilteredStatistics(mod).getMunder();
			if (mover > munder) {
				return "+";
			} else if (mover < munder) {
				return "-";
			} else {
				return "=";
			}
		} else {
			return mod.getDescription();
		}
	}

	/**
	 * Set values of modulator table cells.
	 *
	 * @param aValue -
	 *            value of the cell
	 * @param rowIndex -
	 *            row index of the cell
	 * @param columnIndex -
	 *            column index of the cell
	 */
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		if (columnIndex == 0) {
			enableModulator(rowIndex, (Boolean) aValue);
			if (this.getNumberOfModulatorsSelected() == enabled.length)
				mindyPlugin.setSelectAll(true);
			else
				mindyPlugin.setSelectAll(false);

			mindyPlugin.setTextNumModSelected(getNumberOfModulatorsSelected());
		}
	}

	private DSGeneMarker getModulatorForIndex(int rowIndex) {
		DSGeneMarker mod;
		mod = modulators.get(rowIndex);

/*		if (this.mindyPlugin.globalSelectionState.allMarkerOverride) {
			mod = modulators.get(rowIndex);
		} else {
			mod = limitedModulators.get(rowIndex);
		}
*/

		return mod;
	}

	/**
	 * Get the column name of the specified column index.
	 *
	 * @param columnIndex -
	 *            index of the column
	 * @return name of the column
	 */
	public String getColumnName(int columnIndex) {
		return columnNames[columnIndex];
	}

	/**
	 * Get the sorting states (ascending or descending) of each column in
	 * the modulator table.
	 *
	 * @return a list of sorting states (ascending = true, descending =
	 *         false)
	 */
	public boolean[] getAscendSortStates() {
		return this.ascendSortStates;
	}

	/**
	 * Set the sorting states (ascending or descending) of each column in
	 * the modulator table.
	 *
	 * @param states -
	 *            a list of sorting states (ascending = true, descending =
	 *            false)
	 */
/*	public void setAscendSortStates(boolean[] states) {
		this.ascendSortStates = states;
	}
*/
	/**
	 * Select all modulators on the modulator table.
	 *
	 * @param selected -
	 *            true to select all modulators on the table, and false
	 *            otherwise
	 */
	public void selectAllModulators(boolean selected) {
		MindyPlugin.log.debug("\t\tmod model::selectAllModulators::start::"
				+ System.currentTimeMillis());
		for (int i = 0; i < enabled.length; i++) {
			enabled[i] = selected;
		}
		if (selected) {
			this.selections
					.setSelectedModulators((ArrayList<DSGeneMarker>) this.modulators
							.clone());

			// set selected mods to aggregate table model
			this.mindyPlugin.aggregateModel
					.setEnabledModulators((ArrayList<DSGeneMarker>) this.modulators
							.clone());

			// set selected mods and targets to modular target model
			this.mindyPlugin.modTargetModel
					.setEnabledModulators((ArrayList<DSGeneMarker>) this.modulators
							.clone()); // does not redraw table!
			ArrayList<DSGeneMarker> tmpTargets = (ArrayList<DSGeneMarker>) ((ArrayList<DSGeneMarker>) this.mindyPlugin.aggregateModel
					.getActiveTargets()).clone();
			this.mindyPlugin.modTargetModel
					.setEnabledTargets(tmpTargets);
			this.mindyPlugin.modTargetModel
			.setLimitedTargets(tmpTargets);

			// does not redraw table!
			this.mindyPlugin.modTargetModel.redrawTable();

			// set selected mods to heat map tab list
			mindyPlugin.initModulatorListModel();
			mindyPlugin.refreshModulatorListModel();

			mindyPlugin.enableTabs();
		} else {
			this.selections.setSelectedModulators(null);

			mindyPlugin.disableTabs();
		}
		this.fireTableDataChanged();
		MindyPlugin.log.debug("\t\tmod model::selectAllModulators::end::"
				+ System.currentTimeMillis());
	}

	private void enableModulator(int rowIndex, boolean enable) {
		MindyPlugin.log.debug("\t\tmod model::enableModulator::start::"
				+ System.currentTimeMillis());
		enabled[rowIndex] = enable;
		DSGeneMarker mod = getModulatorForIndex(rowIndex);
		if (enabled[rowIndex]) {
			if (!this.selections.getSelectedModulators().contains(mod))
				this.selections.addSelectedModulator(mod);

			this.mindyPlugin.aggregateModel.enableModulator(mod);
			this.mindyPlugin.modTargetModel
					.setEnabledTargets((ArrayList<DSGeneMarker>) ((ArrayList<DSGeneMarker>) this.mindyPlugin.aggregateModel
							.getActiveTargets()).clone());
			// the line above does not redraw table!
			this.mindyPlugin.modTargetModel.enableModulator(mod); // also redraws the
//			mindyPlugin.refreshModulatorListModel();
		} else {
			this.selections.getSelectedModulators().remove(mod);
			this.mindyPlugin.aggregateModel.disableModulator(mod);
			this.mindyPlugin.modTargetModel.disableModulator(mod);
//			mindyPlugin.refreshModulatorListModel();
		}
//		mindyPlugin.initModulatorListModel();

		if (this.getNumberOfModulatorsSelected() > 0) {
			mindyPlugin.enableTabs();
			mindyPlugin.initModulatorListModel();
			mindyPlugin.refreshModulatorListModel();
		} else {
			mindyPlugin.disableTabs();
		}
		MindyPlugin.log.debug("\t\tmod model::enableModulator::end::"
				+ System.currentTimeMillis());
	}

	/**
	 * Get the number of modulator that has been selected.
	 *
	 * @return number of modulator selected
	 */
	public int getNumberOfModulatorsSelected() {
		return this.selections.getSelectedModulators().size();
	}

	/**
	 * Get the list of user selected modulators.
	 *
	 * @return the list of selected modulators
	 */
	public List<DSGeneMarker> getSelectedModulators() {
		return this.selections.getSelectedModulators();
	}

	/**
	 * Handles table column sorting for the modulator table.
	 *
	 * @param col -
	 *            the column index of the column to sort
	 * @param ascending -
	 *            if true, sort the column in ascending order. Otherwise,
	 *            sort in descending order.
	 */
	public void sort(int col, boolean ascending) {
		MindyPlugin.log.debug("\t\tmod model::sort::start::"
				+ System.currentTimeMillis());
		if (col == 0)
			return;
		ArrayList<DSGeneMarker> mods = this.modulators;
		if (col == 1) {
			this.mindyPlugin.setCursor(this.mindyPlugin.getHourglassCursor());
			ArrayList<MindyGeneMarker> mindyMods = mindyData
					.convertToMindyGeneMarker(mods);
			Collections.sort(mindyMods, new MindyMarkerListComparator(
					MindyMarkerListComparator.SHORT_NAME, ascending, showProbeName));
			mods = mindyData.convertToDSGeneMarker(mindyMods);
			this.mindyPlugin.setCursor(this.mindyPlugin.getNormalCursor());
		}
		if (col == 2) {
			this.mindyPlugin.setCursor(this.mindyPlugin.getHourglassCursor());
			Collections.sort(mods, new GeneMarkerListComparator(mindyData,
					GeneMarkerListComparator.M_POUND, ascending));
			this.mindyPlugin.setCursor(this.mindyPlugin.getNormalCursor());
		}
		if (col == 3) {
			this.mindyPlugin.setCursor(this.mindyPlugin.getHourglassCursor());
			Collections.sort(mods, new GeneMarkerListComparator(mindyData,
					GeneMarkerListComparator.M_PLUS, ascending));
			this.mindyPlugin.setCursor(this.mindyPlugin.getNormalCursor());
		}
		if (col == 4) {
			this.mindyPlugin.setCursor(this.mindyPlugin.getHourglassCursor());
			Collections.sort(mods, new GeneMarkerListComparator(mindyData,
					GeneMarkerListComparator.M_MINUS, ascending));
			this.mindyPlugin.setCursor(this.mindyPlugin.getNormalCursor());
		}
		if (col == 5) {
			this.mindyPlugin.setCursor(this.mindyPlugin.getHourglassCursor());
			Collections.sort(mods, new GeneMarkerListComparator(mindyData,
					GeneMarkerListComparator.MODE, ascending));
			this.mindyPlugin.setCursor(this.mindyPlugin.getNormalCursor());
		}
		if (col == 6) {
			this.mindyPlugin.setCursor(this.mindyPlugin.getHourglassCursor());
			ArrayList<MindyGeneMarker> mindyMods = mindyData
					.convertToMindyGeneMarker(mods);
			Collections.sort(mindyMods, new MindyMarkerListComparator(
					MindyMarkerListComparator.DESCRIPTION, ascending, showProbeName));
			mods = mindyData.convertToDSGeneMarker(mindyMods);
			this.mindyPlugin.setCursor(this.mindyPlugin.getNormalCursor());
		}

		modulators = mods;
		enabled = new boolean[modulators.size()];
		for (DSGeneMarker marker : this.selections.getSelectedModulators()) {
			int index = mods.indexOf(marker);
			if ((index >= 0) && (index < this.enabled.length)) {
				this.enabled[index] = true;
			}
		}

		mindyPlugin.setTextNumModSelected(getNumberOfModulatorsSelected());

/*		this.mindyPlugin.numModSelectedInModTab.setText(MindyPlugin.NUM_MOD_SELECTED_LABEL + " "
				+ this.getNumberOfModulatorsSelected());
*/
		if (this.getNumberOfModulatorsSelected() >= mods.size())
			mindyPlugin.setSelectAll(true);
		else
			mindyPlugin.setSelectAll(false);

		fireTableStructureChanged();
		MindyPlugin.log
				.debug("\t\tmod model::sort::end::"
						+ System.currentTimeMillis());
	}

	/**
	 * Check to see if the modulator table should display probe names or
	 * gene names.
	 *
	 * @return If true, the modulator table displays probe names. If not,
	 *         the modulator table displays gene names.
	 */
	public boolean isShowProbeName() {
		return this.showProbeName;
	}

	/**
	 * Specify whether or not the modulator table should display probe names
	 * or gene names.
	 *
	 * @param showProbeName -
	 *            if true, the modulator table displays probe names. If not,
	 *            the modulator table displays gene names.
	 */
	public void setShowProbeName(boolean showProbeName) {
		this.showProbeName = showProbeName;
	}
}