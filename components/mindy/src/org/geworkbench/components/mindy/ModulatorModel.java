package org.geworkbench.components.mindy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.util.pathwaydecoder.mutualinformation.MindyData;
import org.geworkbench.util.pathwaydecoder.mutualinformation.MindyGeneMarker;

/**
 * Modulator table data model.
 *
 * @author mhall
 * @author ch2514
 * @author oshteynb
 * @version $Id$
 */
class ModulatorModel extends AbstractTableModel {
	/**
	 * 	serialVersionUID generated by Eclipse
	 */
	private static final long serialVersionUID = -9142392951049374863L;

	private static Log log = LogFactory.getLog(ModulatorModel.class);

	MindyPlugin mindyPlugin;

	private boolean[] enabled;

	ArrayList<DSGeneMarker> modulators;

	private MindyData mindyData;

	private String[] columnNames = new String[] { " ", "Modulator", " M# ",
			" M+ ", " M- ", " Mode ", "Modulator Description" };

	private boolean[] ascendSortStates;

	private boolean showProbeName = false;

	//TODO selections, need to go, keep it here for now
	private List<DSGeneMarker> selectedModulators;

	/**
	 * Constructor.
	 *
	 * @param mindyData -
	 *            MINDY data
	 * @param mindyPlugin
	 */
	ModulatorModel() {
	}
	
	void setMindyPlugin(final MindyPlugin mindyPlugin) {
		this.mindyPlugin = mindyPlugin;
		mindyData = mindyPlugin.getMindyData();

		selectedModulators = new ArrayList<DSGeneMarker>();

		this.showProbeName = !mindyData.isAnnotated();

		modulators = (ArrayList<DSGeneMarker>) mindyData.getModulators();

		this.enabled = new boolean[modulators.size()];

		this.ascendSortStates = new boolean[columnNames.length];
		for (int i = 0; i < this.ascendSortStates.length; i++)
			this.ascendSortStates[i] = true;
	}

	void rememberSelections() {
		enabled = new boolean[modulators.size()];
		int size = modulators.size();
		for (int i = 0; i < size; i++) {
			if (selectedModulators.contains(modulators.get(i)))
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
	@Override
	public int getColumnCount() {
		return columnNames.length;
	}

	/**
	 * Get the number of rows on the modulator table.
	 *
	 * @return number of rows on the table
	 */
	@Override
	public int getRowCount() {
		if (enabled == null) {
			return 0;
		} else {
			return modulators.size();
		}
	}

	/**
	 * Whether or not the specified modulator table cell is editable.
	 *
	 * @param rowIndex -
	 *            row index of the table cell
	 * @prarm columnIndex - column index of the table cell
	 * @return true if the table cell is editable, and false otherwise
	 */
	@Override
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
	@Override
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
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		DSGeneMarker mod;
		mod = getModulatorForIndex(rowIndex);
		if (columnIndex == 0) {
			return enabled[rowIndex];
		} else if (columnIndex == 1) {
			return MindyPlugin.getMarkerDisplayName(showProbeName, mod);
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
			return mod.toString();
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
	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		if (columnIndex == 0) {
			enableModulator(rowIndex, (Boolean) aValue);
			if (this.getNumberOfModulatorsSelected() == enabled.length)
				mindyPlugin.setSelectAll(true);
			else
				mindyPlugin.setSelectAll(false);

			mindyPlugin.setTextNumModSelected(getNumberOfModulatorsSelected());
			mindyPlugin.getTableTab().setFirstColumnWidth(30);
		}
	}

	private DSGeneMarker getModulatorForIndex(int rowIndex) {
		DSGeneMarker mod;
		mod = modulators.get(rowIndex);

		return mod;
	}

	/**
	 * Get the column name of the specified column index.
	 *
	 * @param columnIndex -
	 *            index of the column
	 * @return name of the column
	 */
	@Override
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
	boolean[] getAscendSortStates() {
		return this.ascendSortStates;
	}
	
	void setSelectedModulators(List<DSGeneMarker> selectedModulators) {
		if (selectedModulators == null) {

			/* clear modulators in tabs */
			this.selectedModulators.clear();

			this.mindyPlugin.tableTab.getAggregateModel().disableAllModulators();

			this.mindyPlugin.modTargetModel.disableAllModulators();
		} else {
			this.selectedModulators = selectedModulators;
		}
	}


	/**
	 * Select all modulators on the modulator table.
	 *
	 * @param selected -
	 *            true to select all modulators on the table, and false
	 *            otherwise
	 */
	@SuppressWarnings("unchecked")
	void selectAllModulators(boolean selected) {
		log.debug("\t\tmod model::selectAllModulators::start::"
				+ System.currentTimeMillis());
		for (int i = 0; i < enabled.length; i++) {
			enabled[i] = selected;
		}
		if (selected) {
			this.setSelectedModulators((ArrayList<DSGeneMarker>) this.modulators
							.clone());

			// set selected mods to aggregate table model
			this.mindyPlugin.tableTab.getAggregateModel()
					.setEnabledModulators((ArrayList<DSGeneMarker>) this.modulators
							.clone());

			// set selected mods and targets to modular target model
			this.mindyPlugin.modTargetModel
					.setEnabledModulators((ArrayList<DSGeneMarker>) this.modulators
							.clone()); // does not redraw table!
			ArrayList<DSGeneMarker> tmpTargets = (ArrayList<DSGeneMarker>) ((ArrayList<DSGeneMarker>) this.mindyPlugin.tableTab.getAggregateModel()
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
			this.setSelectedModulators(null);

			mindyPlugin.disableTabs();
		}
		this.fireTableDataChanged();
		log.debug("\t\tmod model::selectAllModulators::end::"
				+ System.currentTimeMillis());
		mindyPlugin.getTableTab().setFirstColumnWidth(30);
	}
	
	@SuppressWarnings("unchecked")
	private void enableModulator(int rowIndex, boolean enable) {
		log.debug("\t\tmod model::enableModulator::start::"
				+ System.currentTimeMillis());
		enabled[rowIndex] = enable;
		DSGeneMarker mod = getModulatorForIndex(rowIndex);
		if (enabled[rowIndex]) {
			if (!selectedModulators.contains(mod))
				selectedModulators.add(mod);

			this.mindyPlugin.tableTab.getAggregateModel().enableModulator(mod);
			this.mindyPlugin.modTargetModel
					.setEnabledTargets((ArrayList<DSGeneMarker>) ((ArrayList<DSGeneMarker>) this.mindyPlugin.tableTab.getAggregateModel()
							.getActiveTargets()).clone());
			// the line above does not redraw table!
			this.mindyPlugin.modTargetModel.enableModulator(mod); // also redraws the
//			mindyPlugin.refreshModulatorListModel();
		} else {
			this.selectedModulators.remove(mod);
			this.mindyPlugin.tableTab.getAggregateModel().disableModulator(mod);
			this.mindyPlugin.modTargetModel.disableModulator(mod);
		}

		if (this.getNumberOfModulatorsSelected() > 0) {
			mindyPlugin.enableTabs();
			mindyPlugin.initModulatorListModel();
			mindyPlugin.refreshModulatorListModel();
		} else {
			mindyPlugin.disableTabs();
		}
		log.debug("\t\tmod model::enableModulator::end::"
				+ System.currentTimeMillis());
	}

	/**
	 * Get the number of modulator that has been selected.
	 *
	 * @return number of modulator selected
	 */
	int getNumberOfModulatorsSelected() {
		return this.selectedModulators.size();
	}

	/**
	 * Get the list of user selected modulators.
	 *
	 * @return the list of selected modulators
	 */
	List<DSGeneMarker> getSelectedModulators() {
		return this.selectedModulators;
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
	void sort(int col, boolean ascending) {
		log.debug("\t\tmod model::sort::start::"
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
		for (DSGeneMarker marker : this.selectedModulators) {
			int index = mods.indexOf(marker);
			if ((index >= 0) && (index < this.enabled.length)) {
				this.enabled[index] = true;
			}
		}

		mindyPlugin.setTextNumModSelected(getNumberOfModulatorsSelected());

		if (this.getNumberOfModulatorsSelected() >= mods.size())
			mindyPlugin.setSelectAll(true);
		else
			mindyPlugin.setSelectAll(false);

		fireTableStructureChanged();
		log
				.debug("\t\tmod model::sort::end::"
						+ System.currentTimeMillis());
	}

	/**
	 * Specify whether or not the modulator table should display probe names
	 * or gene names.
	 *
	 * @param showProbeName -
	 *            if true, the modulator table displays probe names. If not,
	 *            the modulator table displays gene names.
	 */
	void setShowProbeName(boolean showProbeName) {
		this.showProbeName = showProbeName;
	}
}