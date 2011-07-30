package org.geworkbench.components.mindy;

import java.awt.Cursor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.table.DefaultTableModel;

import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.util.pathwaydecoder.mutualinformation.MindyData;
import org.geworkbench.util.pathwaydecoder.mutualinformation.MindyResultRow;

/**
	 * Table data model for the list table.
	 *
	 * @author mhall
	 * @author ch2514
	 * @version $Id$
	 */
class ModulatorTargetModel extends DefaultTableModel {
	private static final long serialVersionUID = 1148774320885042265L;

		private boolean[] modChecks;

		private boolean[] targetChecks;

		private ArrayList<DSGeneMarker> enabledModulators;

		private ArrayList<DSGeneMarker> enabledTargets;

		private List<DSGeneMarker> limitedModulators;

		private List<DSGeneMarker> limitedTargets;

		private ArrayList<DSGeneMarker> selectedModulators;

		private ArrayList<DSGeneMarker> selectedTargets;

		private MindyData mindyData;

		private String[] columnNames = new String[] { " ", "Modulator", "  ",
				"Target", "Score" };

		private ArrayList<MindyResultRow> rows = new ArrayList<MindyResultRow>();

		private boolean[] ascendSortStates;

		private boolean showProbeName = false;

		private boolean limMarkers = false;

		private MindyPlugin mindyPlugin;

		public void setLimitedTargets(List<DSGeneMarker> limitedTargets) {
			this.limitedTargets = limitedTargets;
		}

		/**
		 * Constructor.
		 *
		 * @param mindyData -
		 *            data for the MINDY component
		 */
		public ModulatorTargetModel(MindyPlugin mindyPlugin) {
			this.mindyPlugin = mindyPlugin;
			mindyData = mindyPlugin.getMindyData();
			
			this.showProbeName = !mindyData.isAnnotated();
			this.modChecks = new boolean[mindyData.getDataSize()];
			this.targetChecks = new boolean[mindyData.getDataSize()];
			enabledModulators = new ArrayList<DSGeneMarker>(mindyData.getDataSize());
			enabledTargets = new ArrayList<DSGeneMarker>(mindyData.getDataSize());
			limitedModulators = new ArrayList<DSGeneMarker>();
			limitedTargets = null; // it is important to set as null instead of empty list to make the list show up the first time
			selectedModulators = new ArrayList<DSGeneMarker>();
			selectedTargets = new ArrayList<DSGeneMarker>();

			for (int i = 0; i < mindyData.getDataSize(); i++) {
				this.modChecks[i] = false;
				this.targetChecks[i] = false;
			}
			this.ascendSortStates = new boolean[columnNames.length];
			for (int i = 0; i < this.ascendSortStates.length; i++)
				this.ascendSortStates[i] = true;
		}

		/**
		 * Get the enabled modulators.
		 *
		 * @return a list of enabled modulators
		 */
		ArrayList<DSGeneMarker> getEnabledModulators() {
			return enabledModulators;
		}

		/**
		 * Set the list of enabled modulators.
		 *
		 * @param enabledModulators -
		 *            list of enabled modulators
		 */
		void setEnabledModulators(
				ArrayList<DSGeneMarker> enabledModulators) {
			this.enabledModulators = enabledModulators;
		}

		void disableAllModulators() {
			this.enabledModulators.clear();
			this.selectAllModulators(false);
			this.selectAllTargets(false);
		}

		/**
		 * Set the list of enabled targets.
		 *
		 * @param enabledTargets -
		 *            list of enabled targets
		 */
		void setEnabledTargets(ArrayList<DSGeneMarker> enabledTargets) {
			this.enabledTargets = enabledTargets;
		}

		/**
		 * Enable a specified modulator
		 *
		 * @param mod -
		 *            the modulator to enable
		 */
		void enableModulator(DSGeneMarker mod) {
			if (!enabledModulators.contains(mod)) {
				enabledModulators.add(mod);
				redrawTable();
			}
		}

		/**
		 * Disable a specified modulator
		 *
		 * @param mod -
		 *            the modulator to disable
		 */
		void disableModulator(DSGeneMarker mod) {
			enabledModulators.remove(mod);
			redrawTable();
		}

		private void recalculateRows() {
			rows.clear();
			if ((this.enabledModulators != null)
					&& (this.enabledModulators.size() > 0)) {

				List<DSGeneMarker> mods = this.enabledModulators;
				if ((this.limitedModulators != null)
							&& (this.limitedModulators.size() > 0))
						mods = this.limitedModulators;
				if ((this.limitedTargets != null)
//							&& (this.limitedTargets.size() > 0)
							) {
						for (DSGeneMarker modMarker : mods) {
							rows.addAll(mindyData.getRows(modMarker, limitedTargets));
						}
				} else {
						for (DSGeneMarker modMarker : enabledModulators) {
							rows.addAll(mindyData.getRows(modMarker));
						}
				}
			}
			for (int i = 0; i < rows.size(); i++) {
				MindyResultRow r = rows.get(i);
				if ((r != null) && (r.getScore() == 0)) {
					rows.remove(i);
					i--;
				}
			}
			this.rememberSelections();
		}

		void rememberSelections() {
			modChecks = new boolean[rows.size()];
			targetChecks = new boolean[rows.size()];
			for (int i = 0; i < rows.size(); i++) {
				if (this.selectedModulators
						.contains(rows.get(i).getModulator()))
					modChecks[i] = true;
				else
					modChecks[i] = false;
				if (this.selectedTargets.contains(rows.get(i).getTarget()))
					targetChecks[i] = true;
				else
					targetChecks[i] = false;
			}
		}

		/**
		 * Get the number of columns in the list table.
		 *
		 * @return the number of columns in the list table
		 */
		@Override
		public int getColumnCount() {
			return columnNames.length;
		}

		// called from MindyVisualComponent
		// i.e. when SelectionPanel changes marker set selections via
		// GeneSelectorEvent
		/**
		 * Callback method for the list table when the user changes marker set
		 * selections in the Selection Panel.
		 *
		 * @param -
		 *            list of selected markers
		 */
		void limitMarkers(List<DSGeneMarker> limitList) {
			if (limitList == null) {
				limitedModulators = null;
				limitedTargets = null;
				limMarkers = false;
				this.checkSelectedMarkers(true);
				MindyPlugin.log.debug("Cleared modulator and target limits.");
			} else {

				limitedModulators = new ArrayList<DSGeneMarker>();
				limitedTargets = new ArrayList<DSGeneMarker>();
				limMarkers = true;

				for (DSGeneMarker marker : limitList) {
					if (enabledTargets.contains(marker)) {
						limitedTargets.add(marker);
					}
				}

				this.checkSelectedMarkers(false);
				MindyPlugin.log.debug("Limited list table to " + limitedModulators.size()
						+ " mods. and " + limitedTargets.size() + " targets.");
			}

			redrawTable();
			doResizeAndRepaint();
		}

		private void doResizeAndRepaint() {
			mindyPlugin.revalidate();
			mindyPlugin.repaint();
		}

		private void checkSelectedMarkers(boolean showAll) {
			if (showAll) {
				JCheckBox selectAllModsCheckBox = mindyPlugin.selectAllModsCheckBox;
				if ((this.selectedModulators != null)
						&& (this.selectedModulators.size() > 0)
						&& (this.enabledModulators.size() == this.selectedModulators
								.size())) {
					selectAllModsCheckBox.setSelected(true);
				} else {
					selectAllModsCheckBox.setSelected(false);
				}
				JCheckBox selectAllTargetsCheckBox = mindyPlugin.selectAllTargetsCheckBox;
				if ((this.selectedTargets != null)
						&& (this.selectedTargets.size() > 0)
						&& (this.enabledTargets.size() == this.selectedTargets
								.size())) {
					selectAllTargetsCheckBox.setSelected(true);
				} else {
					selectAllTargetsCheckBox.setSelected(false);
				}
			} else {
				JCheckBox selectAllModsCheckBox = mindyPlugin.selectAllModsCheckBox;
				if ((this.selectedModulators != null)
						&& (this.limitedModulators != null)
						&& (this.selectedModulators.size() > 0)
						&& (this.limitedModulators.size() > 0)
						&& (this.limitedModulators.size() <= this.selectedModulators
								.size())) {
					// need to match items in lim and selected
					boolean allMods = true;
					for (DSGeneMarker m : this.limitedModulators) {
						if (!this.selectedModulators.contains(m)) {
							allMods = false;
							break;
						}
					}

					selectAllModsCheckBox.setSelected(allMods);
				} else {
					selectAllModsCheckBox.setSelected(false);
				}
				JCheckBox selectAllTargetsCheckBox = mindyPlugin.selectAllTargetsCheckBox;
				if ((this.selectedTargets != null)
						&& (this.limitedTargets != null)
						&& (this.selectedTargets.size() > 0)
						&& (this.limitedTargets.size() > 0)
						&& (this.limitedTargets.size() <= this.selectedTargets
								.size())) {
					// need to match items in lim and selected
					boolean allTargets = true;
					for (DSGeneMarker t : this.limitedTargets) {
						if (!this.selectedTargets.contains(t)) {
							allTargets = false;
							break;
						}
					}
					selectAllTargetsCheckBox.setSelected(allTargets);
				} else {
					selectAllTargetsCheckBox.setSelected(false);
				}
			}
		}

		void redrawTable() {
			recalculateRows();

			mindyPlugin.setListTableViewOptions();

			fireTableStructureChanged();
			fireTableDataChanged();
		}

		/**
		 * Get the number of rows on the list table.
		 *
		 * @return number of rows on the table
		 */
		@Override
		public int getRowCount() {
			if(mindyPlugin==null)return 0;
			
			if (rows != null)
					return rows.size();
			else
					return 0;
		}

		/**
		 * Whether or not the specified list table cell is editable.
		 *
		 * @param rowIndex -
		 *            row index of the table cell
		 * @prarm columnIndex - column index of the table cell
		 * @return true if the table cell is editable, and false otherwise
		 */
		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			if ((columnIndex == 0) || (columnIndex == 2)) {
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
		@Override
		public Class<?> getColumnClass(int columnIndex) {
			if (columnIndex == 0 || columnIndex == 2) {
				return Boolean.class;
			} else if (columnIndex == columnNames.length - 1) {
				return Float.class;
			} else {
				return String.class;
			}
		}

		/**
		 * Get the values of list table cells.
		 *
		 * @param rowIndex -
		 *            row index of the cell
		 * @param columnIndex -
		 *            column index of the cell
		 * @return the value object of specified table cell
		 */
		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			if (columnIndex == 0) {
				return modChecks[rowIndex];
			} else if (columnIndex == 1) {
				return MindyPlugin.getMarkerDisplayName(showProbeName, rows.get(rowIndex)
						.getModulator());
			} else if (columnIndex == 2) {
				return targetChecks[rowIndex];
			} else if (columnIndex == 3) {
				return MindyPlugin.getMarkerDisplayName(showProbeName, rows.get(rowIndex)
						.getTarget());
			} else if (columnIndex == 4) {
				return rows.get(rowIndex).getScore();
			} else {
				MindyPlugin.log.error("Requested unknown column");
				return null;
			}
		}

		/**
		 * Set values of list table cells.
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
				String marker = MindyPlugin.getMarkerDisplayName(showProbeName, rows.get(rowIndex)
						.getModulator()).trim();
				boolean b = (Boolean) aValue;
				for (int i = 0; i < rows.size(); i++) {
					if (marker.equals(MindyPlugin.getMarkerDisplayName(showProbeName,
							rows.get(i).getModulator()).trim())) {
						modChecks[i] = b;
						DSGeneMarker m = rows.get(rowIndex).getModulator();
						if (modChecks[i] == true) {
							if (!this.selectedModulators.contains(m)) {
								this.selectedModulators.add(m);
							}
						} else {
							this.selectedModulators.remove(m);
						}
					}
				}
				this.fireTableDataChanged();
			} else if (columnIndex == 2) {
				String marker = MindyPlugin.getMarkerDisplayName(showProbeName,
						rows.get(rowIndex).getTarget()).trim();
				boolean b = (Boolean) aValue;
				for (int i = 0; i < rows.size(); i++) {
					if (marker.equals(MindyPlugin.getMarkerDisplayName(showProbeName,
							rows.get(i).getTarget()).trim())) {
						targetChecks[i] = b;
						DSGeneMarker t = rows.get(rowIndex).getTarget();
						if (targetChecks[i] == true) {
							if (!this.selectedTargets.contains(t)) {
								this.selectedTargets.add(t);
							}
						} else {
							this.selectedTargets.remove(t);
						}
					}
				}
				this.fireTableDataChanged();
			}

			mindyPlugin.selectionEnabledCheckBox.setText(MindyPlugin.ENABLE_SELECTION + " "
					+ this.getNumberOfMarkersSelected());

			if (this.limMarkers) {
				JCheckBox selectAllModsCheckBox = mindyPlugin.selectAllModsCheckBox;
				if ((this.selectedModulators.size() > 0)
						&& ((this.limitedModulators.size() > 0) || (this.enabledModulators
								.size() > 0))
						&& ((selectedModulators.size() == this.limitedModulators
								.size()) || (this.selectedModulators.size() == this.enabledModulators
								.size()))) {
					selectAllModsCheckBox.setSelected(true);
				} else {
					selectAllModsCheckBox.setSelected(false);
				}
				JCheckBox selectAllTargetsCheckBox = mindyPlugin.selectAllTargetsCheckBox;
				if ((this.selectedTargets.size() > 0)
						&& ((this.limitedTargets.size() > 0) || (this.enabledTargets
								.size() > 0))
						&& ((selectedTargets.size() == this.limitedTargets
								.size()) || (this.selectedTargets.size() == this.enabledTargets
								.size()))) {
					selectAllTargetsCheckBox.setSelected(true);
				} else {
					selectAllTargetsCheckBox.setSelected(false);
				}
			} else {
				JCheckBox selectAllModsCheckBox = mindyPlugin.selectAllModsCheckBox;
				if ((this.selectedModulators.size() > 0)
						&& (this.enabledModulators.size() > 0)
						&& (selectedModulators.size() == this
								.getEnabledModulators().size())) {
					selectAllModsCheckBox.setSelected(true);
				} else {
					selectAllModsCheckBox.setSelected(false);
				}
				JCheckBox selectAllTargetsCheckBox = mindyPlugin.selectAllTargetsCheckBox;
				if ((this.selectedTargets.size() > 0)
						&& (this.enabledTargets.size() > 0)
						&& (selectedTargets.size() == enabledTargets.size())) {
					selectAllTargetsCheckBox.setSelected(true);
				} else {
					selectAllTargetsCheckBox.setSelected(false);
				}
			}
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

		void selectAllModulators(boolean select) {
			this.selectedModulators.clear();
			if (select) {
				this.selectedModulators.addAll(this.enabledModulators);
			}
			for (int i = 0; i < modChecks.length; i++) {
				modChecks[i] = select;
			}
			if (!select) {
				mindyPlugin.selectAllModsCheckBox.setSelected(false);
			}
			this.fireTableDataChanged();
		}

		/**
		 * Get the sorting states (ascending or descending) of each column in
		 * the list table.
		 *
		 * @return a list of sorting states (ascending = true, descending =
		 *         false)
		 */
		boolean[] getAscendSortStates() {
			return this.ascendSortStates;
		}

		void selectAllTargets(boolean select) {
			this.selectedTargets.clear();
			if (select) {
				if (this.limitedTargets == null){
					this.selectedTargets.addAll(this.enabledTargets);
				} else {
					this.selectedTargets.addAll(this.limitedTargets);
				}
			}

			for (int i = 0; i < targetChecks.length; i++) {
				targetChecks[i] = select;
			}
			if (!select) {
				// selectionEnabledCheckBox.setText(MindyPlugin.ENABLE_SELECTION
				// + " [" + this.getUniqueSelectedMarkers().size() + "]");
				mindyPlugin.selectAllTargetsCheckBox.setSelected(false);
			}
			this.fireTableDataChanged();
		}

		/**
		 * Get the union of selected modulators and targets for the list table.
		 *
		 * @return the union of selected modulators and targets
		 */
		List<DSGeneMarker> getUniqueSelectedMarkers() {
			if ((this.selectedTargets != null)
					&& (this.selectedModulators != null)) {
				int tsize = this.selectedTargets.size();
				int msize = this.selectedModulators.size();
				ArrayList<DSGeneMarker> result = new ArrayList<DSGeneMarker>(
						tsize + msize);
				if (tsize >= msize) {
					result.addAll(this.selectedTargets);
					for (DSGeneMarker m : this.selectedModulators) {
						if (!this.selectedTargets.contains(m))
							result.add(m);
					}
				} else {
					result.addAll(this.selectedModulators);
					for (DSGeneMarker m : this.selectedTargets) {
						if (!this.selectedModulators.contains(m))
							result.add(m);
					}
				}
				result.trimToSize();
				return result;
			}
			if ((this.selectedTargets == null)
					&& (this.selectedModulators != null))
				return this.selectedModulators;
			if ((this.selectedTargets != null)
					&& (this.selectedModulators == null))
				return this.selectedTargets;

			return null;
		}

		int getNumberOfMarkersSelected() {
			int tsize = 0;
			int msize = 0;
			if (this.selectedTargets != null)
				tsize = this.selectedTargets.size();
			if (this.selectedModulators != null)
				msize = this.selectedModulators.size();
			if ((tsize > 0) && (msize > 0)) {
				if (tsize >= msize) {
					int result = tsize;
					for (DSGeneMarker m : this.selectedModulators) {
						if (!this.selectedTargets.contains(m))
							result++;
					}
					return result;
				} else {
					int result = msize;
					for (DSGeneMarker m : this.selectedTargets) {
						if (!this.selectedModulators.contains(m))
							result++;
					}
					return result;
				}
			}
			if ((tsize == 0) && (msize > 0)) {
				return msize;
			}

			if ((tsize > 0) && (msize == 0)) {
				return tsize;
			}

			return 0;
		}

		/**
		 * Handles table column sorting for the list table.
		 *
		 * @param col -
		 *            the column index of the column to sort
		 * @param ascending -
		 *            if true, sort the column in ascending order. Otherwise,
		 *            sort in descending order.
		 */
		void sort(int col, boolean ascending) {
			MindyPlugin.log.debug("\t\tlist model::sort::start::"
					+ System.currentTimeMillis());
			if ((col == 0) || (col == 2))
				return;
			Cursor hourglassCursor = mindyPlugin.hourglassCursor;
			Cursor normalCursor = mindyPlugin.normalCursor;
			if (col == 1) {
				mindyPlugin.setCursor(hourglassCursor);
				Collections.sort(rows, new MindyRowComparator(
						MindyRowComparator.MODULATOR, ascending, mindyData, showProbeName ));
				this.rememberSelections();
				mindyPlugin.setCursor(normalCursor);
			}
			if (col == 3) {
				mindyPlugin.setCursor(hourglassCursor);
				Collections.sort(rows, new MindyRowComparator(
						MindyRowComparator.TARGET, ascending, mindyData, showProbeName));
				this.rememberSelections();
				mindyPlugin.setCursor(normalCursor);
			}
			if (col == 4) {
				mindyPlugin.setCursor(hourglassCursor);
				Collections.sort(rows, new MindyRowComparator(
						MindyRowComparator.SCORE, ascending, mindyData));
				this.rememberSelections();
				mindyPlugin.setCursor(normalCursor);
			}
			fireTableStructureChanged();
			MindyPlugin.log.debug("\t\tlist model::sort::end::"
					+ System.currentTimeMillis());
		}

		/**
		 * Specify whether or not the list table should display probe names or
		 * gene names.
		 *
		 * @param showProbeName -
		 *            if true, the list table displays probe names. If not, the
		 *            list table displays gene names.
		 */
		void setShowProbeName(boolean showProbeName) {
			this.showProbeName = showProbeName;
		}
}