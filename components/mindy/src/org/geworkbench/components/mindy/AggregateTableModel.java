package org.geworkbench.components.mindy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.table.DefaultTableModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.components.mindy.MindyPlugin.ModulatorSort;
import org.geworkbench.components.mindy.MindyPlugin.ModulatorStatComparator;
import org.geworkbench.util.pathwaydecoder.mutualinformation.MindyData;
import org.geworkbench.util.pathwaydecoder.mutualinformation.MindyGeneMarker;

/**
	 * Table data model for the targets table.
	 * refactored from MindyPlugin.java, was nested class
	 *
	 * @author mhall
	 * @author ch2514
	 * @author os2201
	 * @version $Id: $
	 */
	class AggregateTableModel extends DefaultTableModel {

		private static Log log = LogFactory.getLog(AggregateTableModel.class);

		private final MindyPlugin mindyPlugin;

		static final int EXTRA_COLS = 2;

		private boolean[] checkedModulators;

		private List<DSGeneMarker> selectedModulators;

		private List<DSGeneMarker> enabledModulators;

		private boolean[] checkedTargets;

		private List<DSGeneMarker> activeTargets;

		private List<DSGeneMarker> limitedTargets;

		private List<DSGeneMarker> selectedTargets;


		private MindyData mindyData;

		private boolean scoreView = false;

		private boolean modulatorsLimited = false;

		private int modLimit = MindyPlugin.DEFAULT_MODULATOR_LIMIT;

		private ModulatorSort modulatorSortMethod = ModulatorSort.Aggregate;

		private boolean[] ascendSortStates;

		private boolean showProbeName = false;

		/**
		 * Constructor.
		 *
		 * @param mindyData
		 * @param mindyPlugin TODO
		 */
		public AggregateTableModel(MindyPlugin mindyPlugin, MindyData mindyData) {
			this.mindyPlugin = mindyPlugin;
			this.showProbeName = !mindyData.isAnnotated();
//			this.checkedTargets = new boolean[mindyData.getData().size()];
			this.checkedTargets = new boolean[mindyData.getDataSize()];
			this.mindyData = mindyData;
//			allModulators = mindyData.getModulators();
			int allModulatorsSize = mindyData.getModulators().size();
			enabledModulators = new ArrayList<DSGeneMarker>();
			activeTargets = new ArrayList<DSGeneMarker>();
//			ascendSortStates = new boolean[allModulators.size()
			ascendSortStates = new boolean[allModulatorsSize
					+ AggregateTableModel.EXTRA_COLS];
//			this.checkedModulators = new boolean[this.allModulators.size()
			this.checkedModulators = new boolean[allModulatorsSize
					+ AggregateTableModel.EXTRA_COLS];
			this.selectedModulators = new ArrayList<DSGeneMarker>();
			this.selectedTargets = new ArrayList<DSGeneMarker>();
		}

		/**
		 * Whether the targets table shows the actual scores or just -1, 0, and
		 * 1.
		 *
		 * @return true if the table is to show the actual scores, and false
		 *         otherwise
		 */
		public boolean isScoreView() {
			return scoreView;
		}

		/**
		 * Set shether the targets table shows the actual scores or just -1, 0,
		 * and 1.
		 *
		 * @param scoreView -
		 *            true if the table is to show the actual scores, and false
		 *            otherwise
		 */
		public void setScoreView(boolean scoreView) {
			this.scoreView = scoreView;
		}

		/**
		 * Get the modulator sort methods for targets table.
		 *
		 * @return the ModulatorSort object representing the sorting scheme for
		 *         the table columns
		 */
		public ModulatorSort getModulatorSortMethod() {
			return modulatorSortMethod;
		}

		/**
		 * Set the sort scheme for modulators in the targets table.
		 *
		 * @param modulatorSortMethod -
		 *            ModulatorSort object that specifies how to sort table
		 *            columns
		 */
		public void setModulatorSortMethod(ModulatorSort modulatorSortMethod) {
			this.modulatorSortMethod = modulatorSortMethod;
			resortModulators();
			fireTableStructureChanged();
		}

		/**
		 * Whether or not to display only the top modulator(s) in the targets
		 * table.
		 *
		 * @return true if to limit display to the specified number of top
		 *         modulator(s), and false otherwise.
		 */
		public boolean isModulatorsLimited() {
			return modulatorsLimited;
		}

		/**
		 * Set whether or not to display only the top modulator(s) in the
		 * targets table.
		 *
		 * @param modulatorsLimited -
		 *            true if to limit display to the specified number of top
		 *            modulator(s), and false otherwise.
		 */
		public void setModulatorsLimited(boolean modulatorsLimited) {
			this.modulatorsLimited = modulatorsLimited;
		}

		/**
		 * Get the number of top modulator(s) to display in the targets table.
		 *
		 * @return number of top modulator(s) to display
		 */
		public int getModLimit() {
			return modLimit;
		}

		/**
		 * Set the number of top modulator(s) to display in the targets table.
		 *
		 * @param modLimit -
		 *            number of top modulator(s) to display
		 */
		public void setModLimit(int modLimit) {
			this.modLimit = modLimit;
		}

		public DSGeneMarker getEnabledModulatorAtPosition(int pos) {
			return enabledModulators.get(pos);
		}

		/**
		 * Get size of enabled modulators.
		 * @return
		 *
		 * @return size of the list that keeps enabled modulators
		 */
		public  int getEnabledModulatorsSize() {
			return enabledModulators.size();
		}

		/**
		 * Set the list of enabled modulators.
		 * called by selectAllModulators
		 *
		 * @param enabledModulators -
		 *            list of enabled modulators
		 */
		public void setEnabledModulators(List<DSGeneMarker> enabledModulators) {
			this.enabledModulators = enabledModulators;
			this.checkedModulators = new boolean[this.enabledModulators.size()
					+ AggregateTableModel.EXTRA_COLS];
			this.ascendSortStates = new boolean[this.enabledModulators.size()
					+ AggregateTableModel.EXTRA_COLS];
			recalcActiveTargets();
			resortModulators(); // This also fires structure changed
			this.mindyPlugin.getTableTab()
					.setTargetCheckboxesVisibility(this.mindyPlugin.getSelectionEnabledCheckBoxTarget()
							.isSelected());

			fireTableStructureChanged();
		}

		/**
		 * Get the list of selected targets.
		 *
		 * @return list of selected targets
		 */
		public List<DSGeneMarker> getCheckedTargets() {
			return this.selectedTargets;
		}

		/**
		 * Get the list of selected modulators.
		 *
		 * @return list of selected modulators
		 */
		public List<DSGeneMarker> getCheckedModulators() {
			return this.selectedModulators;
		}

		/**
		 * Enable the specified modulator.
		 *
		 * @param mod -
		 *            the modulator to enable
		 */
		public void enableModulator(DSGeneMarker mod) {
			if (!enabledModulators.contains(mod)) {
				enabledModulators.add(mod);
				recalcActiveTargets();
				resortModulators(); // This also fires structure changed
				this.mindyPlugin.getTableTab()
						.setTargetCheckboxesVisibility(this.mindyPlugin.getSelectionEnabledCheckBoxTarget()
								.isSelected());

				fireTableStructureChanged();
			}
		}

		/**
		 * Disable the specified modulator.
		 *
		 * @param mod -
		 *            the modulator to disable
		 */
		public void disableModulator(DSGeneMarker mod) {
			enabledModulators.remove(mod);
			// ch2514 -- re-examine this!
			recalcActiveTargets();
			resortModulators(); // This also fires structure changed
			this.mindyPlugin.getTableTab()
					.setTargetCheckboxesVisibility(this.mindyPlugin.getSelectionEnabledCheckBoxTarget()
							.isSelected());

			fireTableStructureChanged();
		}

		public void disableAllModulators() {
			enabledModulators.clear();
		}

		private void recalcActiveTargets() {
			// activeTargets.clear();

			if ((this.enabledModulators != null)
					&& (this.enabledModulators.size() > 0)) {
//				DSGeneMarker modMarker = this.enabledModulators.get(0);
				if (this.mindyPlugin.getTargetAllMarkersCheckBox().isSelected()) {
//					this.activeTargets = mindyData.getTargets(modMarker);
					this.activeTargets = mindyData.getTargets(this.enabledModulators);
				} else {
					if ((this.limitedTargets != null)
							&& (this.limitedTargets.size() > 0)) {
						this.activeTargets = (List<DSGeneMarker>) ((ArrayList<DSGeneMarker>) this.limitedTargets)
								.clone();
					} else {
						// this.activeTargets = mindyData.getAllTargets(); //
						// MindyData.getAllTargets() broken??
//						this.activeTargets = mindyData.getTargets(modMarker);
						this.activeTargets = mindyData.getTargets(this.enabledModulators);
					}
				}

				// yank out the rows with all zero scores in all columns
				for (int i = 0; i < activeTargets.size(); i++) {
					float tally = 0;
					for (int j = 0; j < enabledModulators.size(); j++) {
						tally += mindyData.getScore(enabledModulators.get(j),
								activeTargets.get(i));
					}
					if (tally == 0) {
						activeTargets.remove(i);
						i--;
					}
				}

				checkedTargets = new boolean[activeTargets.size()];
				for (int i = 0; i < checkedTargets.length; i++) {
					if (this.selectedTargets
							.contains(this.activeTargets.get(i))) {
						checkedTargets[i] = true;
					} else {
						checkedTargets[i] = false;
					}
				}

			}
		}

		void rememberSelections() {
			checkedModulators = new boolean[this.enabledModulators.size()];
			checkedTargets = new boolean[this.activeTargets.size()];
			for (int i = 0; i < this.enabledModulators.size(); i++) {
				if (this.selectedModulators.contains(enabledModulators.get(i)))
					checkedModulators[i] = true;
				else
					checkedModulators[i] = false;
				if (this.selectedTargets.contains(this.activeTargets.get(i)))
					checkedTargets[i] = true;
				else
					checkedTargets[i] = false;
			}
		}

		/**
		 * Get the number of columns in the targets table.
		 *
		 * @return the number of columns in the targets table
		 */
		public int getColumnCount() {
			// Number of allModulators plus target name and checkbox column
			if (!modulatorsLimited) {
				int r = enabledModulators.size()
						+ AggregateTableModel.EXTRA_COLS;
				return r;
			} else {
				int r = Math.min(modLimit + AggregateTableModel.EXTRA_COLS,
						enabledModulators.size()
								+ AggregateTableModel.EXTRA_COLS);
				return r;
			}
		}

		// called from MindyVisualComponent
		// i.e. when SelectionPanel changes marker set selections via
		// GeneSelectorEvent
		/**
		 * Callback method for the targets table when the user changes marker
		 * set selections in the Selection Panel.
		 *
		 * @param -
		 *            list of selected markers
		 */
		public void limitMarkers(List<DSGeneMarker> limitList) {
			if (limitList == null) {
				limitedTargets = null;
				log.debug("Cleared modulator and target limits.");
			} else {
				limitedTargets = limitList;
				log.debug("Limited list table to " + limitedTargets.size()
						+ " targets.");
			}

/*			if (!targetAllMarkersCheckBox.isSelected()) {
				redrawTable();
			}
*/
			if (limitList == null) {
				this.checkSelectedMarkers(true);
			} else {
				this.checkSelectedMarkers(false);
			}

			redrawTable();
		}

		// called from "All Markers" checkbox
		/**
		 * Show only the markers selected in the marker sets on the Selector
		 * Panel. Applies only to the targets table.
		 */
		public void showLimitedMarkers() {
			redrawTable();
			this.checkSelectedMarkers(false);
		}

		// called from "All Markers" checkbox
		/**
		 * Show all markers. Applies only to the targets table.
		 */
		public void showAllMarkers() {
			redrawTable();
			this.checkSelectedMarkers(true);
		}

		private void checkSelectedMarkers(boolean showAll) {
			if (showAll) {
				if ((this.selectedModulators != null)
						&& (this.selectedModulators.size() > 0)
						&& (this.enabledModulators.size() == this.selectedModulators
								.size())) {
					this.mindyPlugin.getSelectAllModsCheckBoxTarget().setSelected(true);
				} else {
					this.mindyPlugin.getSelectAllModsCheckBoxTarget().setSelected(false);
				}
				if ((this.selectedTargets != null)
						&& (this.selectedTargets.size() > 0)
						&& (this.activeTargets.size() == this.selectedTargets
								.size())) {
					this.mindyPlugin.getSelectAllTargetsCheckBoxTarget().setSelected(true);
				} else {
					this.mindyPlugin.getSelectAllTargetsCheckBoxTarget().setSelected(false);
				}
			} else {
				if ((this.selectedModulators != null)
						&& (this.enabledModulators != null)
						&& (this.selectedModulators.size() > 0)
						&& (this.enabledModulators.size() > 0)
						&& (this.enabledModulators.size() <= this.selectedModulators
								.size())) {
					// need to match items in lim and selected
					boolean allMods = true;
					for (DSGeneMarker m : this.enabledModulators) {
						if (!this.selectedModulators.contains(m)) {
							allMods = false;
							break;
						}
					}
					this.mindyPlugin.getSelectAllModsCheckBoxTarget().setSelected(allMods);
				} else {
					this.mindyPlugin.getSelectAllModsCheckBoxTarget().setSelected(false);
				}
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
					this.mindyPlugin.getSelectAllTargetsCheckBoxTarget().setSelected(allTargets);
				} else {
					this.mindyPlugin.getSelectAllTargetsCheckBoxTarget().setSelected(false);
				}
			}
		}

		public void redrawTable() {
			recalcActiveTargets();
			this.mindyPlugin.getTableTab()
					.setTargetCheckboxesVisibility(this.mindyPlugin.getSelectionEnabledCheckBoxTarget()
							.isSelected());

			fireTableStructureChanged();
			fireTableDataChanged();
			this.mindyPlugin.getTableTab().columnScrolling();
		}

		/**
		 * Get the number of rows on the targets table.
		 *
		 * @return number of rows on the table
		 */
		public int getRowCount() {
			if (activeTargets == null) {
				return 0;
			}
			return activeTargets.size();
		}

		/**
		 * Get the class object representing the specified table column.
		 *
		 * @param columnIndex -
		 *            column index
		 * @return the class object representing the table column
		 */
		public Class<?> getColumnClass(int i) {
			if (i == 0) {
				return Boolean.class;
			} else if (i == 1) {
				return String.class;
			} else {
				return Float.class;
			}
		}

		/**
		 * Get the values of targets table cells.
		 *
		 * @param rowIndex -
		 *            row index of the cell
		 * @param columnIndex -
		 *            column index of the cell
		 * @return the value object of specified table cell
		 */
		public Object getValueAt(int row, int col) {
			if (col == 1) {
				return MindyPlugin.getMarkerDisplayName(this.isShowProbeName(), (DSGeneMarker) activeTargets
						.get(row));
			} else if (col == 0) {
				return checkedTargets[row];
			} else {
				float score = mindyData.getScore(enabledModulators.get(col
						- AggregateTableModel.EXTRA_COLS), activeTargets.get(row));
				if (score != 0) {
					if (scoreView) {
						return score;
					} else {
						return Math.signum(score) * 1;
					}
				} else {
					return score;
				}
			}
		}

		/**
		 * Get the score of a specified targets table cell.
		 *
		 * @param row -
		 *            row index of the table cell
		 * @param col -
		 *            col index of the table cell
		 * @return the score of the modulator and target
		 */
		public float getScoreAt(int row, int col) {
			float score = mindyData.getScore(enabledModulators.get(col
					- AggregateTableModel.EXTRA_COLS), activeTargets.get(row));
			return score;
		}

		/**
		 * Set values of targets table cells.
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
				boolean select = (Boolean) aValue;
				checkedTargets[rowIndex] = select;
				DSGeneMarker m = this.activeTargets.get(rowIndex);
				if (select) {
					if (!this.selectedTargets.contains(m)) {
						this.selectedTargets.add(m);
					}
				} else {
					this.selectedTargets.remove(m);
				}
				int modColToSelect = this.enabledModulators.indexOf(m);
				if (modColToSelect >= 0)
					this.setModulatorCheckBoxState(modColToSelect
							+ AggregateTableModel.EXTRA_COLS, select);
			}

			this.mindyPlugin.getSelectionEnabledCheckBoxTarget().setText(MindyPlugin.ENABLE_SELECTION
					+ " " + this.getNumberOfMarkersSelected());

			if (this.getCheckedTargets().size() == this.getActiveTargets()
					.size())
				this.mindyPlugin.getSelectAllTargetsCheckBoxTarget().setSelected(true);
			else
				this.mindyPlugin.getSelectAllTargetsCheckBoxTarget().setSelected(false);

		}

		/**
		 * Get the specified table column name.
		 *
		 * @param col -
		 *            column index
		 */
		public String getColumnName(int col) {
			if (col == 0) {
				return " ";
			} else if (col == 1) {
				return "Target";
			} else {
				DSGeneMarker mod = enabledModulators.get(col
						- AggregateTableModel.EXTRA_COLS);
				String colName = MindyPlugin.getMarkerDisplayName(this.isShowProbeName(), mod);
				if (modulatorSortMethod == ModulatorSort.Aggregate) {
					colName += " (M# "
						+ mindyData.getFilteredStatistics(mod).getCount() + ")";
				} else if (modulatorSortMethod == ModulatorSort.Enhancing) {
					colName += " (M+ "
						+ mindyData.getFilteredStatistics(mod).getMover() + ")";
				} else if (modulatorSortMethod == ModulatorSort.Negative) {
					colName += " (M- "
							+ mindyData.getFilteredStatistics(mod).getMunder() + ")";
				}
				return colName;
			}
		}

		/**
		 * Sorts the columns on the targets table based on modulator stat (M#,
		 * M+. M-) selection.
		 */
		public void resortModulators() {
			Collections.sort(enabledModulators, this.mindyPlugin.new ModulatorStatComparator(
					mindyData, modulatorSortMethod));
			for (int i = 0; i < this.enabledModulators.size(); i++) {
				if (this.selectedModulators.contains(this.enabledModulators
						.get(i))) {
					this.checkedModulators[i + AggregateTableModel.EXTRA_COLS] = true;
				} else {
					this.checkedModulators[i + AggregateTableModel.EXTRA_COLS] = false;
				}
			}


			//			fireTableStructureChanged();

			this.mindyPlugin.getTableTab()
					.setTargetCheckboxesVisibility(this.mindyPlugin.getSelectionEnabledCheckBoxTarget()
							.isSelected());
			if ((this.selectedModulators != null)
					&& (this.selectedModulators.size() > 0)
					&& (this.enabledModulators.size() == this.selectedModulators
							.size())) {
				this.mindyPlugin.getSelectAllModsCheckBoxTarget().setSelected(true);
			} else {
				this.mindyPlugin.getSelectAllModsCheckBoxTarget().setSelected(false);
			}
		}

		/**
		 * Clear all modulator selection from the targets table.
		 */
/*		public void clearModulatorSelections() {
			int length = this.checkedModulators.length;
			this.checkedModulators = new boolean[length];
			// for (int i = 0; i < this.checkedModulators.length; i++)
			// this.checkedModulators[i] = false;
			this.selectedModulators.clear();

			fireTableStructureChanged();

			MindyPlugin.this
					.setTargetCheckboxesVisibility(selectionEnabledCheckBoxTarget
							.isSelected());
		}
*/
		/**
		 * Handles table column sorting for the targets table.
		 *
		 * @param col -
		 *            the column index of the column to sort
		 * @param ascending -
		 *            if true, sort the column in ascending order. Otherwise,
		 *            sort in descending order.
		 */
		public void sort(int col, boolean ascending) {
			log.debug("\t\ttable model::sort::start::"
					+ System.currentTimeMillis());
			if (col == 0)
				return;
			if (col == 1) {
				this.mindyPlugin.setCursor(this.mindyPlugin.hourglassCursor);
				ArrayList<MindyGeneMarker> mindyTargets = mindyData
						.convertToMindyGeneMarker(this.activeTargets);
				Collections.sort(mindyTargets, new MindyMarkerListComparator(
						MindyMarkerListComparator.SHORT_NAME, ascending, showProbeName));
				this.activeTargets = mindyData
						.convertToDSGeneMarker(mindyTargets);
				this.mindyPlugin.setCursor(this.mindyPlugin.normalCursor);
			} else {
				this.mindyPlugin.setCursor(this.mindyPlugin.hourglassCursor);
				Collections.sort(this.activeTargets,
						new GeneMarkerListComparator(mindyData,
								enabledModulators.get(col
										- AggregateTableModel.EXTRA_COLS),
								GeneMarkerListComparator.SCORE, ascending));
				this.mindyPlugin.setCursor(this.mindyPlugin.normalCursor);
			}
			for (int i = 0; i < this.checkedTargets.length; i++) {
				if (this.selectedTargets.contains(this.activeTargets.get(i))) {
					this.checkedTargets[i] = true;
				} else {
					this.checkedTargets[i] = false;
				}
			}

			this.mindyPlugin.getSelectionEnabledCheckBoxTarget().setText(MindyPlugin.ENABLE_SELECTION + " "
					+ this.mindyPlugin.getAggregateModel().getNumberOfMarkersSelected());

			this.mindyPlugin.getTableTab()
					.setTargetCheckboxesVisibility(this.mindyPlugin.getSelectionEnabledCheckBoxTarget()
							.isSelected());
			log.debug("\t\ttable model::sort::end::"
					+ System.currentTimeMillis());
		}

		/**
		 * The union of selected modulators and targets from the targets table.
		 *
		 * @return the list of selected markers
		 */
		public List<DSGeneMarker> getUniqueCheckedTargetsAndModulators() {
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

		/**
		 * Get the total number of markers (modulators and targets) selected in
		 * the targets table.
		 *
		 * @return the total number of markers selected
		 */
		public int getNumberOfMarkersSelected() {
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
		 * Get the list of active targets.
		 *
		 * @return list of active targets
		 */
		public List<DSGeneMarker> getActiveTargets() {
			return activeTargets;
		}

		/**
		 * Get the sorting states (ascending or descending) of each column in
		 * the targets table.
		 *
		 * @return a list of sorting states (ascending = true, descending =
		 *         false)
		 */
		public boolean[] getAscendSortStates() {
			return this.ascendSortStates;
		}

		/**
		 * Set the sorting states (ascending or descending) of each column in
		 * the targets table.
		 *
		 * @param b -
		 *            a list of sorting states (ascending = true, descending =
		 *            false)
		 */
		public void setAscendSortStates(boolean[] b) {
			this.ascendSortStates = b;
		}

		/**
		 * Check to see if the targets table should display probe names or gene
		 * names.
		 *
		 * @return If true, the targets table displays probe names. If not, the
		 *         targets table displays gene names.
		 */
		public boolean isShowProbeName() {
			return this.showProbeName;
		}

		/**
		 * Specify whether or not the targets table should display probe names
		 * or gene names.
		 *
		 * @param showProbeName -
		 *            if true, the targets table displays probe names. If not,
		 *            the targets table displays gene names.
		 */
		public void setShowProbeName(boolean showProbeName) {
			this.showProbeName = showProbeName;
		}

		/**
		 * Whether or not the modulator from the specified colum index is
		 * selected.
		 *
		 * @param index -
		 *            table column index
		 * @return true if the modulator represented by the column is selected,
		 *         and false otherwise
		 */
		public boolean getModulatorCheckBoxState(int index) {
			return this.checkedModulators[index];
		}

		/**
		 * Set the modulator checkbox for the specified targets table column
		 * header.
		 *
		 * @param index -
		 *            column index of the interested header
		 * @param b -
		 *            true if the modulator at the specified index is selected,
		 *            and false otherwise
		 */
		public void setModulatorCheckBoxState(int index, boolean b) {
			this.checkedModulators[index] = b;
			DSGeneMarker m = enabledModulators.get(index
					- AggregateTableModel.EXTRA_COLS);
			if (b) {
				if (!this.selectedModulators.contains(m)) {
					this.selectedModulators.add(m);
				}
			} else {
				this.selectedModulators.remove(m);
			}

		}

		/**
		 * Get the number of modulator checkboxes from the table column headers.
		 *
		 * @return the number of modulator checkboxes from the table column
		 *         headers
		 */
		public int getNumberOfModulatorCheckBoxes() {
			return this.checkedModulators.length;
		}

		void selectAllTargets(boolean select) {
			for (int i = 0; i < checkedTargets.length; i++) {
				checkedTargets[i] = select;
			}
			this.selectedTargets.clear();
			if (select) {
				this.selectedTargets.addAll(this.activeTargets);
			}
			this.fireTableDataChanged();

			this.mindyPlugin.getTableTab()
					.setTargetCheckboxesVisibility(this.mindyPlugin.getSelectionEnabledCheckBoxTarget()
							.isSelected());
		}

		void selectAllModulators(boolean select) {
			int top = this.getColumnCount();
			if ((this.modulatorsLimited)
					&& ((this.modLimit + AggregateTableModel.EXTRA_COLS) < top))
				top = this.modLimit + AggregateTableModel.EXTRA_COLS;
			for (int i = AggregateTableModel.EXTRA_COLS; i < top; i++)
				checkedModulators[i] = select;

			this.selectedModulators.clear();
			if (select) {
				this.selectedModulators.addAll(this.enabledModulators);
			}
			this.fireTableStructureChanged();

			this.mindyPlugin.getTableTab()
					.setTargetCheckboxesVisibility(this.mindyPlugin.getSelectionEnabledCheckBoxTarget()
							.isSelected());
		}

		List<DSGeneMarker> getLimitedTargets() {
			return limitedTargets;
		}
	}
